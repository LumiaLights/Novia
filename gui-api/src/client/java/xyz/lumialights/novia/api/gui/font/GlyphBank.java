/**
           .-----------------. .----------------.  .----------------.  .----------------.  .----------------.
          | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
          | | ____  _____  | || |     ____     | || | ____   ____  | || |     _____    | || |      __      | |
          | ||_   \|_   _| | || |   .'    `.   | || ||_  _| |_  _| | || |    |_   _|   | || |     /  \     | |
          | |  |   \ | |   | || |  /  .--.  \  | || |  \ \   / /   | || |      | |     | || |    / /\ \    | |
          | |  | |\ \| |   | || |  | |    | |  | || |   \ \ / /    | || |      | |     | || |   / ____ \   | |
          | | _| |_\   |_  | || |  \  `--'  /  | || |    \ ' /     | || |     _| |_    | || | _/ /    \ \_ | |
          | ||_____|\____| | || |   `.____.'   | || |     \_/      | || |    |_____|   | || ||____|  |____|| |
          | |              | || |              | || |              | || |              | || |              | |
          | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
           '----------------'  '----------------'  '----------------'  '----------------'  '----------------'

    MIT License

    Copyright (c) 2025 LumiaLights

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
package xyz.lumialights.novia.api.gui.font;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.EmptyBakedGlyph;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.gui.canvas.AffineTransform;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.brush.DrawUtil;
import xyz.lumialights.novia.api.gui.canvas.brush.IBrush;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.IGradientProvider;
import xyz.lumialights.novia.api.gui.canvas.impl.SolidUnitRenderState;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.impl.BakedGlyphAccessor;
import xyz.lumialights.novia.api.gui.impl.StyleAccessor;

import java.util.*;
import java.util.function.Consumer;



//**********************************************************************************************************************
/** Represents a list of glyphs gathered from the characters of a string. */
public final class GlyphBank
    implements Iterable<GlyphBank.Unit>
{
    //******************************************************************************************************************
    public record Unit(
        @Nullable BakedGlyphAccessor glyph,
        @Nullable Integer            override,
        @Nullable Integer            shadowOverride,
                  float              scale,
                  float              x,
                  float              y,
                  float              advance,
                  float              boldOffset,
                  float              shadowOffset,
                  float              lineOffset,
                  boolean            italic,
                  boolean            bold,
                  boolean            underlined,
                  boolean            strikethrough
    )
    {
        //**************************************************************************************************************
        public float getEffectiveMinX()
        {
            float min_rect_x = (this.x + (this.underlined || this.strikethrough ? this.lineOffset : 0));
            
            if (this.glyph != null)
            {
                min_rect_x = Math.min(min_rect_x, (this.x + this.glyph.minX()));
            }
            else
            {
                return min_rect_x;
            }
            
            return Math.min(
                (this.x
                    + this.glyph.minX()
                    + (this.italic ? Math.min(this.getItalicOffsetAtMinY(), this.getItalicOffsetAtMaxY()) : 0.0F)
                    - this.getXExpansion()),
                min_rect_x
            );
        }
        
        public float getEffectiveMinY()
        {
            final int font_height = MinecraftClient.getInstance().textRenderer.fontHeight;
            float min_rect_y = (this.y + (this.strikethrough
                ? (font_height * 0.5f)
                : (this.underlined ? (font_height - 1) : 0)));
            
            if (this.glyph != null)
            {
                min_rect_y = Math.min(min_rect_y, (this.y + this.glyph.minY()));
            }
            else
            {
                return min_rect_y;
            }
            
            return Math.min((this.y + this.glyph.minY() - this.getXExpansion()), min_rect_y);
        }
        
        public float getEffectiveMaxX()
        {
            float max_rect_x = (this.x + (this.underlined || this.strikethrough ? this.advance : 0));
            
            if (this.glyph != null)
            {
                max_rect_x = Math.max(max_rect_x, (this.x + this.glyph.maxX()));
            }
            else
            {
                return max_rect_x;
            }
            
            return Math.max(
                (this.x
                    + this.glyph.maxX()
                    + this.shadowOffset
                    + (this.italic ? Math.max(this.getItalicOffsetAtMinY(), this.getItalicOffsetAtMaxY()) : 0.0F)
                    + this.getXExpansion()),
                max_rect_x
            );
        }

        public float getEffectiveMaxY()
        {
            final int font_height = MinecraftClient.getInstance().textRenderer.fontHeight;
            float max_rect_y = (this.y + (this.strikethrough
                ? (font_height * 0.5f + 1)
                : (this.underlined ? font_height : 0)));
            
            if (this.glyph != null)
            {
                max_rect_y = Math.max(max_rect_y, (this.y + this.glyph.maxY()));
            }
            else
            {
                return max_rect_y;
            }
            
            return Math.max((this.y + this.glyph.maxY() + this.shadowOffset + this.getXExpansion()), max_rect_y);
        }
        
        public float getXExpansion() { return (this.bold ? 0.1F : 0.0F); }
        
        public float getItalicOffsetAtMaxY()
        {
            assert (this.glyph != null);
            return (1.0F - 0.25F * this.glyph.maxY());
        }
    
        public float getItalicOffsetAtMinY()
        {
            assert (this.glyph != null);
            return (1.0F - 0.25F * this.glyph.minY());
        }
        
        //==============================================================================================================
        public void draw(@NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                         final @NotNull BakedGlyphAccessor rectangleGlyph, final @NotNull IGradientProvider palette,
                         final @Nullable IGradientProvider shadowPalette, final int originX, final int originY,
                         final int light)
        {
            if (this.scale != 1.0f)
            {
                matrix = (new Matrix4f(matrix)).scale(this.scale);
            }
            
            if (this.glyph != null)
            {
                this.drawGlyph(matrix, consumer, palette, shadowPalette, originX, originY, light);
            }
            
            final int font_height = MinecraftClient.getInstance().textRenderer.fontHeight;
            
            if (this.strikethrough)
            {
                this.drawRectangle(rectangleGlyph, (font_height * 0.5f - 1), matrix, consumer, palette, shadowPalette,
                                   originX, originY, light);
            }
            
            if (this.underlined)
            {
                this.drawRectangle(rectangleGlyph, (font_height - 1), matrix, consumer, palette, shadowPalette,
                                   originX, originY, light);
            }
        }
        
        public void drawGlyph(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                              final @NotNull IGradientProvider palette, final @Nullable IGradientProvider shadowPalette,
                              final int originX, final int originY, final int light)
        {
            final float x               = this.x();
            final float y               = this.y();
            final float shadow_offset_x = (x + this.shadowOffset() - (this.italic ? 0.25F : 0.0F));
            final float shadow_offset_y = (y + this.shadowOffset());
            
            if (shadowPalette != null)
            {
                this.drawGlyphVerts(shadow_offset_x, shadow_offset_y, 0.0F, matrix, consumer, shadowPalette, originX,
                                    originY, light);
                
                if (this.bold())
                {
                    this.drawGlyphVerts((shadow_offset_x + this.boldOffset), shadow_offset_y, 0.0f, matrix, consumer,
                                        shadowPalette, originX, originY, light);
                }
            }
      
            this.drawGlyphVerts(x, y, 0.0f, matrix, consumer, palette, originX, originY, light);
      
            if (this.bold())
            {
                this.drawGlyphVerts((x + this.boldOffset), y, 0.0f, matrix, consumer, palette, originX, originY, light);
            }
        }
        
        public void drawRectangle(final @NotNull BakedGlyphAccessor glyph, final float lineHeight,
                                  final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                                  final @NotNull IGradientProvider palette,
                                  final @Nullable IGradientProvider shadowPalette, final int originX, final int originY,
                                  final int light)
        {
            final float x = this.x();
            final float y = this.y();
            
            if (shadowPalette != null)
            {
                this.drawRectangleVerts(glyph, x, y, 0.0f, lineHeight, matrix, consumer, this.shadowOffset,
                                        shadowPalette, originX, originY, light);
            }
            
            this.drawRectangleVerts(glyph, x, y, 0.0f, lineHeight, matrix, consumer, 0.0f, palette, originX, originY,
                                    light);
        }
        
        //--------------------------------------------------------------------------------------------------------------
        private void drawGlyphVerts(final float x, final float y, final float z, final @NotNull Matrix4f matrix,
                                    final @NotNull VertexConsumer vertexConsumer,
                                    final @NotNull IGradientProvider palette, final int originX, final int originY,
                                    final int light)
        {
            assert (this.glyph != null);
            
            final float x1          = (x + this.glyph.minX());
            final float y1          = (y + this.glyph.minY());
            final float x2          = (x + this.glyph.maxX());
            final float y2          = (y + this.glyph.maxY());
            final float italic_y1   = (this.italic ? this.getItalicOffsetAtMinY() : 0.0f);
            final float italic_y2   = (this.italic ? this.getItalicOffsetAtMaxY() : 0.0f);
            final float bold_offset = this.getXExpansion();
            
            palette.accept((x1 - originX), (y1 - originY), (x2 - originX), (y2 - originY), ((tl, tr, bl, br) ->
            {
                vertexConsumer
                    .vertex(matrix, (x1 + italic_y1 - bold_offset), (y1 - bold_offset), z)
                    .color(tl)
                    .texture(this.glyph.minU(), this.glyph.minV())
                    .light(light);
                vertexConsumer
                    .vertex(matrix, (x1 + italic_y2 - bold_offset), (y2 + bold_offset), z)
                    .color(bl)
                    .texture(this.glyph.minU(), this.glyph.maxV())
                    .light(light);
                vertexConsumer
                    .vertex(matrix, (x2 + italic_y2 + bold_offset), (y2 + bold_offset), z)
                    .color(br)
                    .texture(this.glyph.maxU(), this.glyph.maxV())
                    .light(light);
                vertexConsumer
                    .vertex(matrix, (x2 + italic_y1 + bold_offset), (y1 - bold_offset), z)
                    .color(tr)
                    .texture(this.glyph.maxU(), this.glyph.minV())
                    .light(light);
            }));
        }
        
        private void drawRectangleVerts(final @NotNull BakedGlyphAccessor glyph, final float x, final float y,
                                        final float z, final float lineHeight, final @NotNull Matrix4f matrix,
                                        final @NotNull VertexConsumer consumer, final float offset,
                                        final @NotNull IGradientProvider palette, final int originX, final int originY,
                                        final int light)
        {
            final float x1 = (x + this.lineOffset + offset);
            final float y1 = (y + lineHeight + offset);
            final float x2 = (x + this.advance + offset + Math.abs(this.lineOffset()));
            final float y2 = (y1 + 1);
            
            palette.accept((x1 - originX), (y1 - originY), (x2 - originX), (y2 - originY), ((tl, tr, bl, br) ->
                DrawUtil.drawTexturedRect(
                    matrix, consumer,
                    z, x1, y1, x2, y2,
                    glyph.minU(), glyph.minV(), glyph.maxU(), glyph.maxV(),
                    tl, tr, bl, br,
                    light)));
        }
    }
    
    //******************************************************************************************************************
    private static @Nullable Integer getShadowColor(final @NotNull Style style, final @Nullable Integer override)
    {
        final Integer shadow_colour = style.getShadowColor();

        if (override == null || shadow_colour != null)
        {
            return style.getShadowColor();
        }

        return null;
    }

    private static boolean compareGpuTexture(final @Nullable GpuTextureView t1, final @Nullable GpuTextureView t2)
    {
        if (t1 == null || t2 == null)
        {
            return (t1 == t2);
        }

        return (
               t1.mipLevels()    == t2.mipLevels()
            && t1.baseMipLevel() == t2.baseMipLevel()
            && t1.texture()      == t2.texture()
        );
    }

    //******************************************************************************************************************
    private final List<Unit> units = new ArrayList<>();

    private transient GuiFont defaultFont;
    private transient GuiFont font;
    private transient float   scale;
    private transient boolean shaded;
    private transient float   left;
    private transient float   top;

    //******************************************************************************************************************
    /** Constructs a new empty glyph bank. */
    public GlyphBank() {}

    /** Constructs a new glyph bank and copies all units from the given other glyph bank. */
    public GlyphBank(final @NotNull GlyphBank other) { this.units.addAll(other.units); }

    //==================================================================================================================
    /**
     * Gets the minimum area that encompasses all stored units in this glyph bank.
     * @return The area rectangle
     */
    public @Nullable Rectangle getBoundingBox()
    {
        float min_x = Float.MAX_VALUE;
        float min_y = Float.MAX_VALUE;
        float max_x = -Float.MAX_VALUE;
        float max_y = -Float.MAX_VALUE;
        
        for (final var unit : this.units)
        {
            min_x = Math.min(min_x, unit.getEffectiveMinX());
            min_y = Math.min(min_y, unit.getEffectiveMinY());
            max_x = Math.max(max_x, unit.getEffectiveMaxX());
            max_y = Math.max(max_y, unit.getEffectiveMaxY());
        }
        
        return ((min_x <= max_x && min_y <= max_y)
            ? new Rectangle((int) min_x, (int) min_y, (int) (max_x - min_x), (int) (max_y - min_y))
            : null);
    }

    /**
     * Gets a unit object stored in this bank at the given index.
     * @param index The index of the {@link Unit}
     * @return The {@link Unit} at the given index
     * @throws IndexOutOfBoundsException If the index is out of bounds
     */
    public @NotNull Unit getUnit(final int index) { return this.units.get(index); }

    /**
     * Gets a list of all units in this glyph bank.
     * @return A {@link List} of all stored {@link Unit} objects
     */
    public @NotNull List<Unit> toList() { return new ArrayList<>(this.units); }

    /**
     * Accepts a consumer for each unit stored in this glyph bank.
     * @param consumer The {@link Consumer} that is called for each {@link Unit}
     */
    public void forEachUnit(final @NotNull Consumer<Unit> consumer) { this.units.forEach(consumer); }
    
    //==================================================================================================================
    /** {@return the number of units in this glyph bank} */
    public int size() { return this.units.size(); }
    
    //==================================================================================================================
    /** {@return whether this glyph bank contains no units}*/
    public boolean isEmpty() { return this.units.isEmpty(); }
    
    //==================================================================================================================
    /** Clears all units from this bank. */
    public void clear() { this.units.clear(); }
    
    //==================================================================================================================
    /**
     * Adds the given string as text to this glyph bank object and uses the given font and style as base information
     * on how the text should be rendered.
     *
     * @param font  The base font and formatting to use when no style overrides it
     * @param text  The text to add
     * @param style The style to override the font with
     * @param x     The x position of the text
     * @param y     The y position of the text
     */
    public void addText(final @NotNull GuiFont font, final @NotNull String text, final @NotNull Style style,
                        final float x, final float y)
    {
        this.prepare(font, x, y);
        TextVisitFactory.visitFormatted(text, style, this::addUnit);
    }

    /**
     * Adds the given string as text to this glyph bank object and uses the given font as base information
     * on how the text should be rendered.
     *
     * @param font The base font and formatting to use when no style overrides it
     * @param text The text to add
     * @param x    The x position of the text
     * @param y    The y position of the text
     */
    public void addText(final @NotNull GuiFont font, final @NotNull String text, final float x, final float y)
    {
        this.addText(font, text, Style.EMPTY, x, y);
    }

    /**
     * Adds the given string as text to this glyph bank object and uses the given font as base information
     * on how the text should be rendered.
     *
     * @param font The base font and formatting to use when no style overrides it
     * @param text The text to add
     * @param x    The x position of the text
     * @param y    The y position of the text
     */
    public void addText(final @NotNull GuiFont font, final @NotNull OrderedText text, final float x, final float y)
    {
        this.prepare(font, x, y);
        text.accept(this::addUnit);
    }

    /**
     * Adds the given string as text to this glyph bank object and uses the given font as base information
     * on how the text should be rendered.
     *
     * @param font The base font and formatting to use when no style overrides it
     * @param text The text to add
     * @param x    The x position of the text
     * @param y    The y position of the text
     */
    public void addText(final @NotNull GuiFont font, final @NotNull Text text, final float x, final float y)
    {
        this.addText(font, text.asOrderedText(), x, y);
    }

    /**
     * Adds all units from the given glyph bank to this one (if {@code bank} is not {@code this}).
     * @param bank The {@link GlyphBank} to copy all {@link Unit} objects from
     */
    public void addBank(final @NotNull GlyphBank bank)
    {
        if (bank != this)
        {
            this.units.addAll(bank.units);
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void prepare(final @NotNull GuiFont font, final float x, final float y)
    {
        this.defaultFont = Objects.requireNonNull(font, "font must not be null");
        this.font        = font;
        this.scale       = font.getSize();
        this.shaded      = font.isShaded();
        this.left        = x;
        this.top         = y;
    }

    private boolean addUnit(final int i, final @NotNull Style style, final int codePoint)
    {
        final StyleAccessor style_acc = (StyleAccessor) style;
        
        if (!style_acc.novia$isFontSet() || style.getFont().equals(this.defaultFont.getId()))
        {
            this.font = this.defaultFont;
        }
        else
        {
            final Identifier font_id = style.getFont();
            
            if (!font_id.equals(this.font.getId()))
            {
                this.font = GuiFont.getFont(font_id);
            }
        }
        
        final float      line_offset   = (i == 0 ? -1.0F : 0);
        final boolean    bold          = style_acc.novia$getFormatting(Formatting.BOLD)  .orElse(this.font.isBold());
        final boolean    italic        = style_acc.novia$getFormatting(Formatting.ITALIC).orElse(this.font.isItalic());
        final Glyph      glyph         = this.font.getGlyph(codePoint);
        final float      advance       = glyph.getAdvance(bold);
        final boolean    underlined    = style_acc.novia$getFormatting(Formatting.UNDERLINE)
                                                  .orElse(this.font.isUnderlined());
        final boolean    strikethrough = style_acc.novia$getFormatting(Formatting.STRIKETHROUGH)
                                                  .orElse(this.font.isStrikethrough());
        final Integer    override      = (style.getColor() != null ? (style.getColor().getRgb() | 0xFF000000) : null);
        final boolean    obfuscated    = style_acc.novia$getFormatting(Formatting.OBFUSCATED)
                                                  .orElse(this.font.isObfuscated());
        final BakedGlyph baked         = (obfuscated && codePoint != 32 ? this.font.getObfuscatedBakedGlyph(glyph)
                                                                        : this.font.getBaked(codePoint));
        final float      shadow_offset;
        final Integer    shadow_colour;

        if (this.shaded)
        {
            shadow_offset = glyph.getShadowOffset();
            shadow_colour = GlyphBank.getShadowColor(style, override);
        }
        else
        {
            shadow_offset = 0.0f;
            shadow_colour = null;
        }

        Unit unit = null;
        
        if (!(baked instanceof EmptyBakedGlyph))
        {
            final float bold_offset = (bold ? glyph.getBoldOffset() : 0.0F);
            unit = new Unit((BakedGlyphAccessor) baked, override, shadow_colour, this.scale, this.left, this.top,
                            advance, bold_offset, shadow_offset, line_offset, italic, bold, underlined, strikethrough);
        }
        else if (underlined || strikethrough)
        {
            unit = new Unit(null, override, shadow_colour, this.scale, this.left, this.top, advance, 0, shadow_offset,
                            line_offset, italic, bold, underlined, strikethrough);
        }
        
        if (unit != null)
        {
            this.units.add(unit);
        }
        
        this.left += advance;
        
        return true;
    }
    
    //==================================================================================================================
    /**
     * Draws this bank's units to the given canvas object.
     * @param canvas The {@link Canvas} to render the {@link Unit} objects with
     */
    public void draw(final @NotNull Canvas canvas)
    {
        if (this.units.isEmpty())
        {
            return;
        }
        
        Rectangle area = this.getBoundingBox();
        
        if (area == null || area.isEmpty())
        {
            return;
        }
        
        float min_x =  Float.MAX_VALUE;
        float min_y =  Float.MAX_VALUE;
        float max_x = -Float.MAX_VALUE;
        float max_y = -Float.MAX_VALUE;
        
        final List<GlyphBank.Unit> units = new ArrayList<>();
        final IBrush               brush = canvas.getBrush().copy();
        
        final BakedGlyph     rect_glyph     = GuiFont.getDefault().getRectangleBakedGlyph();
        final GpuTextureView blank          = Objects.requireNonNull(rect_glyph.getTexture());
        final RenderPipeline blank_pipeline = rect_glyph.getPipeline();
        
        GpuTextureView texture  = null;
        RenderPipeline pipeline = null;
        
        {
            final BakedGlyphAccessor first = this.units.getFirst().glyph();
            
            if (first != null)
            {
                texture  = first.getTexture();
                pipeline = (first.getPipeline());
            }
            else
            {
                pipeline = blank_pipeline;
            }
        }
        
        for (final var unit : this.units)
        {
            final GpuTextureView unit_texture;
            final RenderPipeline unit_pipeline;
            
            if (unit.glyph() != null)
            {
                unit_texture  = unit.glyph().getTexture();
                unit_pipeline = unit.glyph().getPipeline();
            }
            else
            {
                unit_texture  = null;
                unit_pipeline = blank_pipeline;
            }
            
            if (!GlyphBank.compareGpuTexture(texture, unit_texture) || pipeline != unit_pipeline)
            {
                if (!units.isEmpty() && min_x < max_x && min_y < max_y)
                {
                    final TextureSetup setup = new TextureSetup(
                        texture,
                        blank,
                        MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().getGlTextureView());
                    this.addUnitsToCanvas(canvas, new ArrayList<>(units), brush, pipeline, setup, area, min_x, min_y,
                                          max_x, max_y);
                }
                
                units.clear();
                
                min_x    =  Float.MAX_VALUE;
                min_y    =  Float.MAX_VALUE;
                max_x    = -Float.MAX_VALUE;
                max_y    = -Float.MAX_VALUE;
                texture  = unit_texture;
                pipeline = unit_pipeline;
            }
            
            units.add(unit);
            
            min_x = Math.min(min_x, unit.getEffectiveMinX());
            min_y = Math.min(min_y, unit.getEffectiveMinY());
            max_x = Math.max(max_x, unit.getEffectiveMaxX());
            max_y = Math.max(max_y, unit.getEffectiveMaxY());
        }
        
        if (!units.isEmpty() && min_x < max_x && min_y < max_y)
        {
            final TextureSetup setup = new TextureSetup(
                texture,
                blank,
                MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().getGlTextureView());
            this.addUnitsToCanvas(canvas, units, brush, pipeline, setup, area, min_x,  min_y, max_x, max_y);
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void addUnitsToCanvas(final @NotNull Canvas canvas, final @NotNull List<Unit> units,
                                  final @NotNull IBrush brush, final @NotNull RenderPipeline pipeline,
                                  final @NotNull TextureSetup texture, final @NotNull Rectangle area,
                                  final float minX, final float minY, final float maxX, final float maxY)
    {
        final AffineTransform transform = new AffineTransform(canvas.getTransform());
        final float           opacity   = canvas.getOpacity();
        
        canvas.draw(new SolidUnitRenderState(
            pipeline,
            transform.getMatrix(),
            texture,
            canvas.getClippingRegion().toScreenRect(),
            (new ScreenRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY))),
            ((consumer, depth) -> brush.drawUnits(units, consumer, transform.getMatrixWithDepth(depth), area,
                                                  opacity))));
    }
    
    //==================================================================================================================
    /** {@return the iterator for this glyph bank} */
    @Override public @NotNull Iterator<Unit> iterator() { return this.units.iterator(); }
}

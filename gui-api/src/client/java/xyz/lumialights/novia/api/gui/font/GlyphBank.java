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
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.text.*;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.canvas.AffineTransform;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.brush.IBrush;
import xyz.lumialights.novia.api.gui.canvas.impl.SolidUnitRenderState;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.impl.BakedGlyphAccessor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
/** Represents a list of glyphs gathered from the characters of a string. */
public final class GlyphBank
    implements Iterable<TextGlyph>
{
    //******************************************************************************************************************
    private record Run(@NotNull List<TextGlyph> textGlyphs, @NotNull Rectangle area) {}

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
    private final List<Run> runs = new ArrayList<>(1);

    private Rectangle bounds = null;

    //==================================================================================================================
    private transient float           minX;
    private transient float           minY;
    private transient float           maxX;
    private transient float           maxY;
    private transient List<TextGlyph> textGlyphs;
    private transient boolean         isFirst;
    private transient float           top;
    private transient float           left;
    private transient float           height;

    //******************************************************************************************************************
    /** Constructs a new empty glyph bank. */
    public GlyphBank() {}

    /** Constructs a new glyph bank and copies all text glyphs from the given other glyph bank. */
    public GlyphBank(final @NotNull GlyphBank other)
    {
        this.runs.addAll(other.runs);
        this.bounds = new Rectangle(other.bounds);
    }

    //==================================================================================================================
    /**
     * Gets the minimum area that encompasses all stored units in this glyph bank.
     * @return The area rectangle
     */
    public @NotNull Rectangle getBoundingBox() { return this.bounds; }

    /**
     * Gets a text glyph object stored in this bank at the given index.
     * @param index The index of the {@link TextGlyph}
     * @return The {@link TextGlyph} at the given index
     * @throws IndexOutOfBoundsException If the index is out of bounds
     */
    public @NotNull TextGlyph getTextGlyph(final int index)
    {
        return this.stream()
            .skip(index)
            .findFirst()
            .orElseThrow(() -> new IndexOutOfBoundsException("index " + index + " is out of bounds"));
    }

    /**
     * Gets a list of all text glyphs in this glyph bank in insertion order.
     * @return A {@link List} of all stored {@link TextGlyph} objects
     */
    public @NotNull List<TextGlyph> toList() { return this.stream().collect(Collectors.toList()); }

    /**
     * Gets all text glyphs as a stream in insertion order.
     * @return The text glyph {@link Stream}
     */
    public @NotNull Stream<TextGlyph> stream() { return this.runs.stream().flatMap(run -> run.textGlyphs.stream()); }
    
    //==================================================================================================================
    /** {@return the number of text glyphs in this glyph bank} */
    public int size() { return this.textGlyphs.size(); }
    
    //==================================================================================================================
    /** {@return whether this glyph bank contains no text glyphs}*/
    public boolean isEmpty() { return this.textGlyphs.isEmpty(); }
    
    //==================================================================================================================
    /** Clears all text glyphs from this bank. */
    public void clear()
    {
        this.runs.clear();
        this.bounds.reset();
    }
    
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
        this.addText(font, Language.getInstance().reorder(StringVisitable.styled(text, style)), x, y);
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
        this.addText(font, Language.getInstance().reorder(StringVisitable.plain(text)), x, y);
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
        this.prepare();
        IGuiCharacterVisitor.of(this::addGlyph).visitFormatted(font, text);

        if (this.minX < this.maxX && this.minY < this.maxY)
        {
            final int min_x = Math.round(x + this.minX);
            final int min_y = Math.round(y + this.minY);
            final int max_x = Math.round(x + this.maxX);
            final int max_y = Math.round(y + this.maxY);

            this.runs.add(new Run(this.textGlyphs, new Rectangle(x, y, this.left, this.height)));

            if (this.bounds != null)
            {
                this.bounds.combine(min_x, min_y, (max_x - min_x), (max_y - min_y));
            }
            else
            {
                this.bounds = new Rectangle(min_x, min_y, (max_x - min_x), (max_y - min_y));
            }
        }
    }

    public void addTextAligned(final @NotNull GuiFont   font,
                               final @NotNull String    text,
                               final          float     x,
                               final          float     y,
                               final          float     width,
                               final          float     height,
                               final @NotNull Alignment alignment)
    {
        this.addTextAligned(font, Language.getInstance().reorder(StringVisitable.plain(text)), x, y, width, height,
                            alignment);
    }

    public void addTextAligned(final @NotNull GuiFont   font,
                               final @NotNull String    text,
                               final @NotNull Rectangle container,
                               final @NotNull Alignment alignment)
    {
        container.accept((x, y, w, h) -> this.addTextAligned(font, text, x, y, w, h, alignment));
    }

    public void addTextAligned(final @NotNull GuiFont   font,
                               final @NotNull Text      text,
                               final          float     x,
                               final          float     y,
                               final          float     width,
                               final          float     height,
                               final @NotNull Alignment alignment)
    {
        this.addTextAligned(font, text.asOrderedText(), x, y, width, height, alignment);
    }

    public void addTextAligned(final @NotNull GuiFont   font,
                               final @NotNull Text      text,
                               final @NotNull Rectangle container,
                               final @NotNull Alignment alignment)
    {
        container.accept((x, y, w, h) -> this.addTextAligned(font, text, x, y, w, h, alignment));
    }

    public void addTextAligned(final @NotNull GuiFont     font,
                               final @NotNull OrderedText text,
                               final @NotNull Rectangle   container,
                               final @NotNull Alignment   alignment)
    {
        container.accept((x, y, w, h) -> this.addTextAligned(font, text, x, y, w, h, alignment));
    }

    public void addTextAligned(final @NotNull GuiFont     font,
                               final @NotNull OrderedText text,
                               final          float       x,
                               final          float       y,
                               final          float       width,
                               final          float       height,
                               final @NotNull Alignment   alignment)
    {
        this.prepare();
        IGuiCharacterVisitor.of(this::addGlyph).visitFormatted(font, text);

        if (this.minX < this.maxX && this.minY < this.maxY)
        {
            final Rectangle aligned = alignment.align(x, y, width, height, this.left, this.height);

            final int min_x = Math.round(aligned.x() + this.minX);
            final int min_y = Math.round(aligned.y() + this.minY);
            final int max_x = Math.round(aligned.x() + this.maxX);
            final int max_y = Math.round(aligned.y() + this.maxY);

            this.runs.add(new Run(this.textGlyphs, aligned));

            if (this.bounds != null)
            {
                this.bounds.combine(min_x, min_y, (max_x - min_x), (max_y - min_y));
            }
            else
            {
                this.bounds = new Rectangle(min_x, min_y, (max_x - min_x), (max_y - min_y));
            }
        }
    }

    /**
     * Adds all text glyphs from the given glyph bank to this one (if {@code bank} is not {@code this}).
     * @param bank The {@link GlyphBank} to copy all {@link TextGlyph} objects from
     */
    public void addBank(final @NotNull GlyphBank bank)
    {
        if (bank != this)
        {
            this.textGlyphs.addAll(bank.textGlyphs);
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void prepare()
    {
        this.isFirst    = true;
        this.top        = 0f;
        this.left       = 0f;
        this.height     = 0f;
        this.textGlyphs = new ArrayList<>(64);
        this.minX       = Float.MAX_VALUE;
        this.minY       = Float.MAX_VALUE;
        this.maxX       = -Float.MAX_VALUE;
        this.maxY       = -Float.MAX_VALUE;
    }

    private boolean addGlyph(final int i, final @NotNull GuiFont font, final @NotNull Style style, final int codePoint)
    {
        final float   scale    = font.getScale();
        final Glyph   glyph    = font.getGlyph(codePoint);
        final Integer override = (style.getColor() != null ? (style.getColor().getRgb() | 0xFF000000) : null);

        final float   shadow_offset;
        final Integer shadow_colour;

        if (font.isShaded())
        {
            shadow_offset = (glyph.getShadowOffset() * scale);
            shadow_colour = GlyphBank.getShadowColor(style, override);
        }
        else
        {
            shadow_offset = 0.0f;
            shadow_colour = null;
        }
        
        final boolean bold        = font.isBold();
        final boolean underlined  = font.isUnderlined();
        final float   line_offset = (this.isFirst ? -scale : 0);
        final float   bold_offset = (bold ? (glyph.getBoldOffset() * scale) : 0.0F);
        final float   advance     = (glyph.getAdvance() * scale + bold_offset);
        final float   font_size   = (GuiFont.getRenderHeight() * scale);
        final float   rect_start  = (this.left + line_offset);
        final float   rect_extent = (this.left + advance);
        
        final TextRect underline_rect;
        final TextRect strikethrough_rect;
        
        TextGlyph text_glyph = null;

        if (underlined)
        {
            final float line_start = (font_size - scale);
            underline_rect = new TextRect(rect_start, line_start, rect_extent, (line_start + scale), shadow_offset);
        } else underline_rect = null;
        
        final boolean strikethrough = font.isStrikethrough();
        
        if (strikethrough)
        {
            final float line_start = ((font_size * 0.5f) - scale);
            strikethrough_rect = new TextRect(rect_start, line_start, rect_extent, (line_start + scale), shadow_offset);
        } else strikethrough_rect = null;
        
        final boolean    italic         = font.isItalic();
        final float      kerning_offset = (!this.isFirst ? (font_size * font.getTracking()) : 0f);
        final BakedGlyph baked          = (font.isObfuscated() && codePoint != 32
            ? font.getObfuscatedBakedGlyph(glyph)
            : font.getBaked(codePoint));
        
        if (!(baked instanceof EmptyBakedGlyph))
        {
            text_glyph = new TextGlyph((BakedGlyphAccessor) baked, override, shadow_colour, underline_rect,
                                       strikethrough_rect, scale, (this.left + kerning_offset), 0, advance, bold_offset,
                                       shadow_offset, line_offset, italic, bold);
        }
        else if (underlined || strikethrough)
        {
            text_glyph = new TextGlyph(null, override, shadow_colour, underline_rect, strikethrough_rect, scale,
                                       (this.left + kerning_offset), 0, advance, 0, shadow_offset, line_offset, italic,
                                       bold);
        }
        
        if (text_glyph != null)
        {
            this.textGlyphs.add(text_glyph);

            this.minX   = Math.min(this.minX,   text_glyph.getEffectiveMinX());
            this.minY   = Math.min(this.minY,   text_glyph.getEffectiveMinY());
            this.maxX   = Math.max(this.maxX,   text_glyph.getEffectiveMaxX());
            this.maxY   = Math.max(this.maxY,   text_glyph.getEffectiveMaxY());
            this.height = Math.max(this.height, (this.maxY - this.minY));
        }

        this.isFirst = false;
        this.left   += (advance + kerning_offset);
        
        return true;
    }

    //==================================================================================================================
    /**
     * Draws this bank's text glyphs to the given canvas object.
     * @param canvas The {@link Canvas} to render the {@link TextGlyph} objects with
     */
    public void draw(final @NotNull Canvas canvas)
    {
        if (this.runs.isEmpty())
        {
            return;
        }

        final IBrush brush = canvas.getBrush().copy();

        for (final var run : this.runs)
        {
            final Rectangle area = run.area();

            if (area.isEmpty())
            {
                continue;
            }

            final BakedGlyph     rect_glyph     = GuiFont.DEFAULT.get().getRectangleBakedGlyph();
            final GpuTextureView blank          = Objects.requireNonNull(rect_glyph.getTexture());
            final RenderPipeline blank_pipeline = rect_glyph.getPipeline();

            final List<TextGlyph> text_glyphs = new ArrayList<>();
            float                 min_x       =  Float.MAX_VALUE;
            float                 min_y       =  Float.MAX_VALUE;
            float                 max_x       = -Float.MAX_VALUE;
            float                 max_y       = -Float.MAX_VALUE;

            GpuTextureView texture;
            RenderPipeline pipeline;

            {
                final BakedGlyphAccessor first = this.textGlyphs.getFirst().glyph();

                if (first != null)
                {
                    texture  = first.getTexture();
                    pipeline = (first.getPipeline());
                }
                else
                {
                    texture  = null;
                    pipeline = blank_pipeline;
                }
            }

            for (final var text_glyph : run.textGlyphs)
            {
                final GpuTextureView unit_texture;
                final RenderPipeline unit_pipeline;

                if (text_glyph.glyph() != null)
                {
                    unit_texture  = text_glyph.glyph().getTexture();
                    unit_pipeline = text_glyph.glyph().getPipeline();
                }
                else
                {
                    unit_texture  = null;
                    unit_pipeline = blank_pipeline;
                }

                if (!GlyphBank.compareGpuTexture(texture, unit_texture) || pipeline != unit_pipeline)
                {
                    if (!text_glyphs.isEmpty() && min_x < max_x && min_y < max_y)
                    {
                        final TextureSetup setup = new TextureSetup(
                            texture,
                            blank,
                            MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().getGlTextureView());
                        this.addUnitsToCanvas(canvas, new ArrayList<>(text_glyphs), brush, pipeline, setup, area,
                                              min_x, min_y, max_x, max_y);
                    }

                    text_glyphs.clear();

                    min_x    =  Float.MAX_VALUE;
                    min_y    =  Float.MAX_VALUE;
                    max_x    = -Float.MAX_VALUE;
                    max_y    = -Float.MAX_VALUE;
                    texture  = unit_texture;
                    pipeline = unit_pipeline;
                }

                text_glyphs.add(text_glyph);

                min_x = Math.min(min_x, text_glyph.getEffectiveMinX());
                min_y = Math.min(min_y, text_glyph.getEffectiveMinY());
                max_x = Math.max(max_x, text_glyph.getEffectiveMaxX());
                max_y = Math.max(max_y, text_glyph.getEffectiveMaxY());
            }

            if (!text_glyphs.isEmpty() && min_x < max_x && min_y < max_y)
            {
                final TextureSetup setup = new TextureSetup(
                    texture,
                    blank,
                    MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().getGlTextureView());
                this.addUnitsToCanvas(canvas, text_glyphs, brush, pipeline, setup, area, min_x, min_y, max_x, max_y);
            }
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void addUnitsToCanvas(final @NotNull Canvas canvas, final @NotNull List<TextGlyph> textGlyphs,
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
            ((consumer, depth) -> brush.drawTextGlyphs(textGlyphs, consumer, transform.getMatrixWithDepth(depth), area,
                                                       opacity))));
    }
    
    //==================================================================================================================
    /** {@return the iterator for this glyph bank} */
    @Override public @NotNull Iterator<TextGlyph> iterator() { return this.textGlyphs.iterator(); }
}

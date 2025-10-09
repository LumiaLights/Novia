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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.impl.FontManagerAccessor;
import xyz.lumialights.novia.api.gui.impl.MinecraftClientAccessor;

import java.util.*;
import java.util.function.Function;



//**********************************************************************************************************************
/**
 * The gui text renderer is a compatibility layer for the Minecraft {@link TextRenderer} class. Since text renderer do
 * not support advanced features such as scaling, gradients or texturing, it should not really be used except for cases
 * where the text renderer is required; it is almost always better to use {@link GlyphBank} instead.
 */
public class GuiTextRenderer
    extends TextRenderer
{
    //******************************************************************************************************************
    public record GuiGlyphDrawable(
        @NotNull GlyphBank glyphBank,
        @NotNull Rectangle boundingBox,
        int                colour,
        boolean            shadow
    )
        implements GlyphDrawable
    {
        //**************************************************************************************************************
        @Override
        public void draw(final @NotNull GlyphDrawer glyphDrawer)
        {
            final BakedGlyph blank_glyph = GuiFont.DEFAULT.get().getRectangleBakedGlyph();
            
            for (final var unit : this.glyphBank)
            {
                final Style style           = Style.EMPTY.withItalic(unit.italic()).withBold(unit.bold());
                final int   override        = Objects.requireNonNullElse(unit.override(), this.colour);
                final int   shadow_override = Objects.requireNonNullElse(
                    unit.shadowOverride(),
                    ColorHelper.scaleRgb(this.colour, 0.25f));

                if (unit.glyph() != null)
                {
                    glyphDrawer.drawGlyph(new BakedGlyph.DrawnGlyph(unit.x(), unit.y(), override, shadow_override,
                                                                    (BakedGlyph) unit.glyph(), style, unit.boldOffset(),
                                                                    unit.shadowOffset()));
                }
                
                if (unit.strikethroughRect() != null)
                {
                    glyphDrawer.drawRectangle(blank_glyph, unit.strikethroughRect().toBaked(
                        0.01f,
                        override, shadow_override,
                        unit.shadowOffset()));
                }
                
                if (unit.underlineRect() != null)
                {
                    glyphDrawer.drawRectangle(blank_glyph, unit.underlineRect().toBaked(
                        0.01f,
                        override, shadow_override,
                        unit.shadowOffset()));
                }
            }
        }
        
        @Override
        public @Nullable ScreenRect getScreenRect()
        {
            return (!this.boundingBox.isEmpty() ? this.boundingBox.toScreenRect() : null);
        }
    }
    
    //******************************************************************************************************************
    private static GuiTextRenderer INSTANCE = null;
    
    //******************************************************************************************************************
    public static @NotNull GuiTextRenderer getInstance()
    {
        if (GuiTextRenderer.INSTANCE == null)
        {
            GuiTextRenderer.INSTANCE = new GuiTextRenderer(GuiFont.DEFAULT.get());
        }
        
        return GuiTextRenderer.INSTANCE;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("resource")
    private static @NotNull Function<Identifier, FontStorage> storageIdentifierFunction()
    {
        final FontManagerAccessor manager =
            (FontManagerAccessor) (((MinecraftClientAccessor) MinecraftClient.getInstance()).novia$getFontManager());
        return manager::novia$getFontStorage;
    }
    
    //******************************************************************************************************************
    private GuiFont font;
    
    //******************************************************************************************************************
    /**
     * Constructs a new text renderer.
     * @param font            The base {@link GuiFont} the renderer should draw unstyled text with
     * @param validateAdvance Whether to validate a glyph's advance (the horizontal extent of a glyph)
     */
    public GuiTextRenderer(final @NotNull GuiFont font, final boolean validateAdvance)
    {
        super(GuiTextRenderer.storageIdentifierFunction(), validateAdvance);
        this.font = new GuiFont(Objects.requireNonNull(font, "font must not be null"));
    }
    
    /**
     * Constructs a new text renderer that does not validate its glyph's advance.
     * @param font The base {@link GuiFont} the renderer should draw unstyled text with
     */
    public GuiTextRenderer(final @NotNull GuiFont font) { this(font, false); }
    
    /** Constructs a new text renderer with {@link GuiFont#DEFAULT} that does not validate its glyph's advance. */
    public GuiTextRenderer() { this(GuiFont.DEFAULT.get()); }
    
    //==================================================================================================================
    public @NotNull GuiFont getFont() { return this.font; }
    
    //==================================================================================================================
    public void setFont(final @NotNull GuiFont font)
    {
        this.font = Objects.requireNonNull(font, "font must not be null");
    }
    
    //==================================================================================================================
    @Override
    public void draw(final @NotNull String                 string,
                     final          float                  x,
                     final          float                  y,
                     final          int                    color,
                     final          boolean                shadow,
                     final @NotNull Matrix4f               matrix,
                     final @NotNull VertexConsumerProvider vertexConsumers,
                     final @NotNull TextLayerType          layerType,
                     final          int                    backgroundColor,
                     final          int                    light)
    {
        this.prepare(string, x, y, color, shadow, backgroundColor)
            .draw(GlyphDrawer.drawing(vertexConsumers, matrix, layerType, light));
    }
    
    @Override
    public void draw(final @NotNull Text                   text,
                     final          float                  x,
                     final          float                  y,
                     final          int                    color,
                     final          boolean                shadow,
                     final @NotNull Matrix4f               matrix,
                     final @NotNull VertexConsumerProvider vertexConsumers,
                     final @NotNull TextLayerType          layerType,
                     final          int                    backgroundColor,
                     final          int                    light)
    {
        this.prepare(text.asOrderedText(), x, y, color, shadow, backgroundColor)
            .draw(GlyphDrawer.drawing(vertexConsumers, matrix, layerType, light));
    }
    
    @Override
    public void draw(final @NotNull OrderedText            text,
                     final          float                  x,
                     final          float                  y,
                     final          int                    color,
                     final          boolean                shadow,
                     final @NotNull Matrix4f               matrix,
                     final @NotNull VertexConsumerProvider vertexConsumers,
                     final @NotNull TextLayerType          layerType,
                     final          int                    backgroundColor,
                     final          int                    light)
    {
        this.prepare(text, x, y, color, shadow, backgroundColor)
            .draw(GlyphDrawer.drawing(vertexConsumers, matrix, layerType, light));
    }
    
    //==================================================================================================================
    @Override
    public @NotNull TextRenderer.GlyphDrawable prepare(      @NotNull String  string,
                                                       final          float   x,
                                                       final          float   y,
                                                       final          int     color,
                                                       final          boolean shadow,
                                                       final          int     backgroundColor)
    {
        if (this.isRightToLeft())
        {
            string = this.mirror(string);
        }
        
        return this.prepare(Text.of(string).asOrderedText(), x, y, color, shadow, backgroundColor);
    }
    
    @Override
    public @NotNull TextRenderer.GlyphDrawable prepare(final @NotNull OrderedText text,
                                                       final          float       x,
                                                       final          float       y,
                                                       final          int         color,
                                                       final          boolean     shadow,
                                                       final          int         backgroundColor)
    {
        final GlyphBank bank = new GlyphBank();
        this.font.setShaded(shadow);
        this.font.setScale(1.0f);
        bank.addText(this.font, text, x, y);
        return new GuiGlyphDrawable(bank, bank.getBoundingBox(), color, shadow);
    }
}

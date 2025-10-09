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
package xyz.lumialights.novia.api.gui.canvas.brush;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.IGradientProvider;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.font.TextGlyph;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.impl.BakedGlyphAccessor;
import xyz.lumialights.novia.api.gui.util.DrawUtil;

import java.util.*;



//**********************************************************************************************************************
/** Describes a brush that applies a gradient on shapes and textures. */
public class GradientBrush
    implements IBrush
{
    //******************************************************************************************************************
    private Gradient gradient;
    
    //******************************************************************************************************************
    /**
     * Constructs a new gradient brush.
     * @param gradient The {@link Gradient}
     */
    public GradientBrush(final @NotNull Gradient gradient)
    {
        this.gradient = Objects.requireNonNull(gradient, "gradient must not be null");
    }

    /**
     * Copies another gradient brush.
     * @param other The other {@link GradientBrush}
     */
    public GradientBrush(final @NotNull GradientBrush other) { this(other.gradient); }
    
    //==================================================================================================================
    /**
     * Gets the brush's gradient.
     * @return The current {@link Gradient}
     */
    public @NotNull Gradient getGradient() { return this.gradient; }
    
    //==================================================================================================================
    /**
     * Sets this brush's gradient.
     * @param gradient The new {@link Gradient}
     */
    public void setGradient(final @NotNull Gradient gradient)
    {
        this.gradient = Objects.requireNonNull(gradient, "gradient must not be null");
    }
    
    //==================================================================================================================
    @Override
    public void rectangle(final @NotNull VertexConsumer consumer, final @NotNull Matrix3x2f matrix,
                          final int x1, final int y1, final int x2, final int y2,
                          final float depth, final float opacity)
    {
        IGradientProvider
            .of(this.gradient.withOpacity(opacity), 0, 0)
            .accept((tl, tr, bl, br) -> DrawUtil.drawRect(matrix, consumer, depth, x1, y1, x2, y2, tl, tr, bl, br));
    }
    
    @Override
    public void border(final @NotNull VertexConsumer consumer, final @NotNull Matrix3x2f matrix,
                       final int x1, final int y1, final int x2, final int y2,
                       final float depth, final float opacity)
    {
        IGradientProvider
            .of(this.gradient.withOpacity(opacity), 0, 0)
            .accept((tl, tr, bl, br) ->
                DrawUtil.drawBorder(matrix, consumer, depth, 1.0f, x1, y1, x2, y2, tl, tr, bl, br));
    }
    
    @Override
    public void drawTexturedQuad(final @NotNull VertexConsumer consumer, final @NotNull Matrix3x2f matrix,
                                 final int x1, final int y1, final int x2, final int y2,
                                 final float u1, final float v1, final float u2, final float v2,
                                 final float depth, final float opacity)
    {
        IGradientProvider
            .of(this.gradient.withOpacity(opacity), 0, 0)
            .accept((tl, tr, bl, br) ->
                DrawUtil.drawTexturedRect(matrix, consumer, depth, x1, y1, x2, y2, u1, v1, u2, v2, tl, tr, bl, br));
    }
    
    @Override
    public void drawTextGlyphs(final @NotNull List<TextGlyph> textGlyphs, final @NotNull VertexConsumer consumer,
                               final @NotNull Matrix4f matrix, final @NotNull Rectangle area, final float opacity)
    {
        final Gradient           adjusted_gradient   = this.gradient.withOpacity(opacity);
        final BakedGlyphAccessor line_glyph          = (BakedGlyphAccessor) GuiFont.DEFAULT.get()
                                                                                   .getRectangleBakedGlyph();
        final IGradientProvider def_gradient        = IGradientProvider.of(adjusted_gradient,
                                                                           area.width(), area.height());
        final IGradientProvider def_shadow_gradient = IGradientProvider.of(
            adjusted_gradient.withColours(
                ColorHelper.scaleRgb(adjusted_gradient.startColour(), 0.25F),
                ColorHelper.scaleRgb(adjusted_gradient.endColour(),   0.25F)),
            area.width(), area.height());
        
        for (final var text_glyph : textGlyphs)
        {
            final IGradientProvider gradient;
            final IGradientProvider shadow_gradient;

            final Integer override = text_glyph.override();

            if (override == null)
            {
                gradient = def_gradient;

                if (text_glyph.shadowOffset() != 0.0f)
                {
                    final Integer shadow_override = text_glyph.shadowOverride();
                    shadow_gradient = (shadow_override != null
                        ? IGradientProvider.of(
                            adjusted_gradient.withColours(
                                IBrush.mixAlpha(adjusted_gradient.startColour(), shadow_override),
                                IBrush.mixAlpha(adjusted_gradient.endColour(),   shadow_override)),
                            area.width(), area.height())
                        : def_shadow_gradient);
                }
                else { shadow_gradient = null; }
            }
            else
            {
                final int override_adjusted = IBrush.forOpacity(override, opacity);
                gradient = IGradientProvider.solid(override_adjusted);

                if (text_glyph.shadowOffset() != 0.0f)
                {
                    final Integer shadow_override = text_glyph.shadowOverride();
                    shadow_gradient = IGradientProvider.solid((shadow_override != null
                        ? IBrush.mixAlpha(override_adjusted, shadow_override)
                        : ColorHelper.scaleRgb(override_adjusted, 0.25f)));
                }
                else { shadow_gradient = null; }
            }
            
            text_glyph.draw(matrix, consumer, line_glyph, gradient, shadow_gradient, area.x(), area.y(), 15728880);
        }
    }
    
    //==================================================================================================================
    @Override public @NotNull GradientBrush copy() { return new GradientBrush(this); }
}

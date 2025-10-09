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
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.gui.font.TextGlyph;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;

import java.util.*;



//**********************************************************************************************************************
/**
 * Describes the base class for canvas brushes used to describe how texturing and colours are applied to shapes and
 * textures.
 */
public interface IBrush
{
    //******************************************************************************************************************
    /**
     * Applies the opacity level to the given colour.
     * @param colour  The colour to mix
     * @param opacity The opacity level to mix with the colours opacity
     * @return The colour with the new opacity level
     */
    static int forOpacity(final int colour, final float opacity)
    {
        if (opacity == 1.0f)
        {
            return colour;
        }

        final float alpha = ColorHelper.getAlphaFloat(colour);
        return ColorHelper.withAlpha(ColorHelper.channelFromFloat(opacity * alpha), colour);
    }

    /**
     * Mixes the alpha channel of two colour and applies it to the target colour.
     * @param baseColour   The base colour to get the alpha channel from
     * @param targetColour The target colour to apply the new alpha value to
     * @return {@code targetColour} with the new alpha channel
     */
    static int mixAlpha(final int baseColour, final int targetColour)
    {
        final float base_alpha   = ColorHelper.getAlphaFloat(baseColour);
        final float target_alpha = ColorHelper.getAlphaFloat(targetColour);

        return (base_alpha != 1.0f
            ? ColorHelper.withAlpha(ColorHelper.channelFromFloat(base_alpha * target_alpha), targetColour)
            : targetColour);
    }

    //******************************************************************************************************************
    /**
     * Provides textures for a brush, if the brush should provide textures.
     * @return The {@link TextureSetup}
     */
    default @NotNull TextureSetup getTextureSetup() { return TextureSetup.empty(); }

    //==================================================================================================================
    /**
     * Fills a rectangular area.
     * @param consumer The vertex consumer
     * @param matrix   The transformation matrix
     * @param x1       The top-left x coordinate
     * @param y1       The top-left y coordinate
     * @param x2       The bottom-right x coordinate
     * @param y2       The bottom-right y coordinate
     * @param depth    The z coordinate
     * @param opacity  The opacity level to prepare colours with
     */
    void rectangle(@NotNull VertexConsumer consumer, @NotNull Matrix3x2f matrix, int x1, int y1, int x2, int y2,
                   float depth, float opacity);

    /**
     * Draws a border around a rectangular area.
     * @param consumer The vertex consumer
     * @param matrix   The transformation matrix
     * @param x1       The top-left x coordinate
     * @param y1       The top-left y coordinate
     * @param x2       The bottom-right x coordinate
     * @param y2       The bottom-right y coordinate
     * @param depth    The z coordinate
     * @param opacity  The opacity level to prepare colours with
     */
    void border(@NotNull VertexConsumer consumer, @NotNull Matrix3x2f matrix, int x1, int y1, int x2, int y2,
                float depth, float opacity);

    /**
     * Draws a texture in a rectangular area.
     * @param consumer The vertex consumer
     * @param matrix   The transformation matrix
     * @param x1       The top-left x coordinate
     * @param y1       The top-left y coordinate
     * @param x2       The bottom-right x coordinate
     * @param y2       The bottom-right y coordinate
     * @param u1       The normalised top-left U coordinate of the area inside the texture that should be mapped
     *                 (see <a href="https://en.wikipedia.org/wiki/UV_mapping">UV mapping</a>)
     * @param v1       The normalised top-left V coordinate of the area inside the texture that should be mapped
     *                 (see <a href="https://en.wikipedia.org/wiki/UV_mapping">UV mapping</a>)
     * @param u2       The normalised bottom-right U coordinate of the area inside the texture that should be mapped
     *                 (see <a href="https://en.wikipedia.org/wiki/UV_mapping">UV mapping</a>)
     * @param v2       The normalised bottom-right V coordinate of the area inside the texture that should be mapped
     *                 (see <a href="https://en.wikipedia.org/wiki/UV_mapping">UV mapping</a>)
     * @param depth    The z coordinate
     * @param opacity  The opacity level to prepare colours with
     */
    void drawTexturedQuad(@NotNull VertexConsumer consumer, @NotNull Matrix3x2f matrix,
                          int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2,
                          float depth, float opacity);

    /**
     * Draws the given text glyph arrangement.
     * @param textGlyphs The text glyphs to draw
     * @param consumer   The vertex consumer
     * @param matrix     The transformation matrix
     * @param area       The area of the text to draw
     * @param opacity    The opacity level to prepare colours with
     */
    void drawTextGlyphs(@NotNull List<TextGlyph> textGlyphs, @NotNull VertexConsumer consumer, @NotNull Matrix4f matrix,
                        @NotNull Rectangle area, float opacity);

    //==================================================================================================================
    /**
     * Must be overridden to copy all the necessary data in a brush.
     * @return The new copied brush
     */
    @NotNull IBrush copy();
}

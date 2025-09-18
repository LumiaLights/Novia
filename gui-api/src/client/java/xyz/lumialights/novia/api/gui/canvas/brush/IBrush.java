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
import xyz.lumialights.novia.api.gui.font.GlyphBank;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;

import java.util.*;



//**********************************************************************************************************************
public interface IBrush
{
    //******************************************************************************************************************
    static int forOpacity(final int colour, final float opacity)
    {
        if (opacity == 1.0f)
        {
            return colour;
        }

        final float alpha = ColorHelper.getAlphaFloat(colour);
        return ColorHelper.withAlpha(ColorHelper.channelFromFloat(opacity * alpha), colour);
    }

    static int mixAlpha(final int baseColour, final int targetColour)
    {
        final float base_alpha   = ColorHelper.getAlphaFloat(baseColour);
        final float target_alpha = ColorHelper.getAlphaFloat(targetColour);

        return (base_alpha != 1.0f
            ? ColorHelper.withAlpha(ColorHelper.channelFromFloat(base_alpha * target_alpha), targetColour)
            : targetColour);
    }
    
    //******************************************************************************************************************
    default @NotNull TextureSetup getTextureSetup() { return TextureSetup.empty(); }
    
    //==================================================================================================================
    void rectangle(@NotNull VertexConsumer consumer, @NotNull Matrix3x2f matrix, int x1, int y1, int x2, int y2,
                   float depth, float opacity);
    void border(@NotNull VertexConsumer consumer, @NotNull Matrix3x2f matrix,int x1, int y1, int x2, int y2,
                float depth, float opacity);
    void drawTexturedQuad(@NotNull VertexConsumer consumer, @NotNull Matrix3x2f matrix,
                          int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2,
                          float depth, float opacity);
    void drawUnits(@NotNull List<GlyphBank.Unit> unit, @NotNull VertexConsumer consumer, @NotNull Matrix4f matrix,
                   @NotNull Rectangle area, float opacity);
    
    //==================================================================================================================
    @NotNull IBrush copy();
}

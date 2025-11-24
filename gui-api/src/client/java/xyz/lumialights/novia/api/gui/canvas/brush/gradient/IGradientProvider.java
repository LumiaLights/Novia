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
package xyz.lumialights.novia.api.gui.canvas.brush.gradient;

import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.brush.VertexPalette;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;



//**********************************************************************************************************************
public interface IGradientProvider
{
    //******************************************************************************************************************
    static @NotNull IGradientProvider of(final @NotNull Gradient gradient,
                                         final          float    width,
                                         final          float    height)
    {
        return switch (gradient.direction())
        {
            case HORIZONTAL -> new HGradientProvider(gradient, (1.0f / width));
            case VERTICAL   -> new VGradientProvider(gradient, (1.0f / height));
        };
    }

    static @NotNull IGradientProvider of(final          int       startColour,
                                         final          int       endColour,
                                         final          float     width,
                                         final          float     height,
                                         final @NotNull Direction direction)
    {
        if (startColour == endColour)
        {
            return IGradientProvider.solid(startColour);
        }

        return IGradientProvider.of(new Gradient(startColour, endColour, direction), width, height);
    }
    
    static @NotNull IGradientProvider of(final @NotNull Colour    startColour,
                                         final @NotNull Colour    endColour,
                                         final          float     width,
                                         final          float     height,
                                         final @NotNull Direction direction)
    {
        return IGradientProvider.of(startColour.colour(), endColour.colour(), width, height, direction);
    }

    static @NotNull SGradientProvider solid(final int colour)
    {
        return new SGradientProvider(new GradientSolid(colour));
    }
    
    static @NotNull SGradientProvider solid(final @NotNull Colour colour)
    {
        return IGradientProvider.solid(colour.colour());
    }

    //******************************************************************************************************************
    @NotNull Gradient gradient();
    float step();
    
    //==================================================================================================================
    @NotNull VertexPalette getPalette(final float x1, final float y1, final float x2, final float y2);
    
    default @NotNull VertexPalette getPalette(final @NotNull Rectangle rectangle)
    {
        return this.getPalette(rectangle.width(), rectangle.height(), rectangle.getRight(), rectangle.getBottom());
    }
    
    //==================================================================================================================
    void accept(float x1, float y1, float x2, float y2, @NotNull VertexPalette.VertexConsumer consumer);
    void accept(@NotNull VertexPalette.VertexConsumer consumer);
}

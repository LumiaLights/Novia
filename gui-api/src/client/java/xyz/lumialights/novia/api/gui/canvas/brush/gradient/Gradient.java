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

import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.brush.VertexPalette;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;

import java.util.Objects;



//**********************************************************************************************************************
public class Gradient
{
    //******************************************************************************************************************
    private final int       startColour;
    private final int       endColour;
    private final float     skew;
    private final Direction direction;

    //******************************************************************************************************************
    public Gradient(final int startColour, final int endColour, final @NotNull Direction direction, final float skew)
    {
        this.startColour = startColour;
        this.endColour   = endColour;
        this.direction   = Objects.requireNonNull(direction, "direction must not be null");
        this.skew        = skew;

        if (this.skew <= 0.0f)
        {
            throw new IllegalArgumentException("skew value must not be equal or less than 0");
        }
    }

    public Gradient(final @NotNull Colour    startColour,
                    final @NotNull Colour    endColour,
                    final @NotNull Direction direction,
                    final          float     skew)
    {
        this(startColour.colour(), endColour.colour(), direction, skew);
    }

    public Gradient(final @NotNull Colour    startColour,
                    final @NotNull Colour    endColour,
                    final @NotNull Direction direction)
    {
        this(startColour, endColour, direction, 1.0f);
    }

    public Gradient(final int startColour, final int endColour, final @NotNull Direction direction)
    {
        this(startColour, endColour, direction, 1.0f);
    }
    
    //==================================================================================================================
    public @NotNull Gradient withColours(final int startColour, final int endColour)
    {
        return new Gradient(startColour, endColour, this.direction, this.skew);
    }

    public @NotNull Gradient withColours(final @NotNull Colour startColour, final @NotNull Colour endColour)
    {
        return new Gradient(startColour, endColour, this.direction, this.skew);
    }

    public @NotNull Gradient withOpacity(final float opacity)
    {
        return this.withColours(
            new Colour(this.startColour).withOpacityRel(opacity),
            new Colour(this.endColour)  .withOpacityRel(opacity));
    }

    //==================================================================================================================
    public int startColour() { return this.startColour; }
    public int endColour() { return this.endColour; }
    public float skew() { return this.skew; }
    public @NotNull Direction direction() { return this.direction; }

    //==================================================================================================================
    public int getColour(float delta)
    {
        delta = Math.clamp(delta, 0f, 1f);

        if (this.skew != 1f && delta > 0f)
        {
            delta = (float) Math.exp(Math.log(delta) / this.skew);
        }

        return ColorHelper.lerp(delta, this.startColour, this.endColour);
    }
    
    public @NotNull VertexPalette getPalette()
    {
        return switch (this.direction)
        {
            case VERTICAL   -> new VertexPalette(this.startColour, this.startColour, this.endColour, this.endColour);
            case HORIZONTAL -> new VertexPalette(this.startColour, this.endColour, this.startColour, this.endColour);
        };
    }
    
    //==================================================================================================================
    public @NotNull Gradient partition(final int x,
                                       final int y,
                                       final int width,
                                       final int height,
                                       final int deltaXStart,
                                       final int deltaXEnd,
                                       final int deltaYStart,
                                       final int deltaYEnd)
    {
        return switch (this.direction)
        {
            case HORIZONTAL -> this.partition(x, (x + width),  deltaXStart, deltaXEnd);
            case VERTICAL   -> this.partition(y, (y + height), deltaYStart, deltaYEnd);
        };
    }
    
    public @NotNull Gradient partition(final @NotNull Rectangle area,
                                       final          int       deltaXStart,
                                       final          int       deltaXEnd,
                                       final          int       deltaYStart,
                                       final          int       deltaYEnd)
    {
        return area.transform((x, y, w, h) ->
            this.partition(x, y, w, h, deltaXStart, deltaXEnd, deltaYStart, deltaYEnd));
    }
    
    public @NotNull Gradient partition(final @NotNull Rectangle area, final @NotNull Rectangle subArea)
    {
        return area.transform((x, y, w, h) ->
            this.partition(x, y, w, h, subArea.x(), subArea.getRight(), subArea.y(), subArea.getBottom()));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    public @NotNull Gradient partition(final int axisStart, final int axisEnd, final int deltaStart, final int deltaEnd)
    {
        if (deltaStart > deltaEnd)
        {
            throw new IllegalArgumentException("start delta must not be greater than end delta");
        }
        
        if (deltaStart >= axisEnd || deltaStart < axisStart)
        {
            throw new IllegalArgumentException("start delta must be inside the given area");
        }
        
        if (deltaEnd >= axisEnd)
        {
            throw new IllegalArgumentException("end delta must be inside the given area");
        }
        
        final int start = this.getColour((float) deltaStart / axisEnd);
        final int end   = this.getColour((float) deltaEnd   / axisEnd);
        
        return this.withColours(start, end);
    }
    
    //==================================================================================================================
    @Override
    public String toString()
    {
        return "Gradient{startColour=%d, endColour=%d, skew=%f, direction=%s}"
            .formatted(this.startColour, this.endColour, this.skew, this.direction);
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)                      return true;
        if (!(obj instanceof Gradient other)) return false;
        
        return (
               this.startColour == other.startColour
            && this.endColour   == other.endColour
            && this.skew        == other.skew
            && this.direction   == other.direction
        );
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.startColour, this.endColour, this.skew, this.direction);
    }
}

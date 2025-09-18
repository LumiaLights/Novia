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
import xyz.lumialights.novia.api.gui.canvas.brush.IBrush;

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
            IBrush.forOpacity(this.startColour, opacity),
            IBrush.forOpacity(this.endColour,   opacity));
    }

    //==================================================================================================================
    public int startColour() { return this.startColour; }
    public int endColour() { return this.endColour; }
    public float skew() { return this.skew; }
    public @NotNull Direction direction() { return this.direction; }

    //==================================================================================================================
    public int getColour(float delta)
    {
        delta = Math.clamp(delta, 0.0f, 1.0f);

        if (this.skew != 1.0 && delta > 0.0f)
        {
            delta = (float) Math.exp(Math.log(delta) / this.skew);
        }

        return ColorHelper.lerp(delta, this.startColour, this.endColour);
    }
}

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

import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;



//**********************************************************************************************************************
public record FontSize(@NotNull FontSize.Unit unit, float value)
{
    //******************************************************************************************************************
    public enum Unit
    {
        //**************************************************************************************************************
        /** Specifies the font's scale in pixels. */
        PIXEL("px", (base -> (1.0f / base))),
        
        /** Specifies the font's scale in points, points will be calculated regardless of applied window scaling. */
        POINT("pt", (base -> (1.0f / (base * 0.75f)))),
        
        /** Specifies the font's size in a relative font scale (e.g. 1em: font height, 2em: double font height). */
        EM("em", (base -> 1.0f)),
        ;
        
        //**************************************************************************************************************
        public final Function<Integer, Float> scaleFunc;
        public final String                   literal;
        
        //**************************************************************************************************************
        Unit(final @NotNull String literal, final @NotNull Function<Integer, Float> scaleFunc)
        {
            this.literal   = Objects.requireNonNull(literal);
            this.scaleFunc = Objects.requireNonNull(scaleFunc);
        }
    }
    
    //******************************************************************************************************************
    @Language("RegExp")
    public static final String FONT_SIZE_PATTERN = "^([+-]?(?:[0-9]*[.])?[0-9]+)(px|pt|em)$";
    
    //******************************************************************************************************************
    /**
     * Returns a font size object in pixels.
     * @param value The value in pixels
     * @return The {@link FontSize}
     * @see Unit#PIXEL
     */
    public static @NotNull FontSize pixels(final float value) { return new FontSize(Unit.PIXEL, value); }
    
    /**
     * Returns a font size object in points.
     * @param value The value in points
     * @return The {@link FontSize}
     * @see Unit#POINT
     */
    public static @NotNull FontSize points(final float value) { return new FontSize(Unit.POINT, value); }
    
    /**
     * Returns a font size object in relative em unit.
     * @param value The value in em scale
     * @return The {@link FontSize}
     * @see Unit#EM
     */
    public static @NotNull FontSize em(final float value) { return new FontSize(Unit.EM, value); }
    
    public static @NotNull FontSize fromString(final @Pattern(FONT_SIZE_PATTERN) @NotNull String value)
    {
        if (!value.matches(FONT_SIZE_PATTERN))
        {
            throw new IllegalArgumentException(value + " is not a valid number or size unit (px, pt, em)");
        }
        
        return new FontSize(
            Arrays
                .stream(Unit.values())
                .filter(e -> e.literal.equalsIgnoreCase(value.substring((value.length() - 2))))
                .findFirst()
                .orElseThrow(),
            Float.parseFloat(value.substring(0, (value.length() - 2))));
    }
    
    //******************************************************************************************************************
    public float getScaledValue(final int baseHeight)
    {
        return (this.unit.scaleFunc.apply(baseHeight) * this.value);
    }
}

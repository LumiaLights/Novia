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



//**********************************************************************************************************************
/// Provides a conversion structure that calculates EM size (relative to [GuiFont#getRenderHeight()]) based on the
/// given unit and value.
///
/// Consumers of this class will then internally convert the EM size to [logical pixels].
///
/// @param unit  The unit to use to convert to the EM scale
/// @param value The value to convert
public record FontSize(@NotNull FontSize.Unit unit, float value)
{
    //******************************************************************************************************************
    /// Represents the conversion algorithm to use.
    public enum Unit
    {
        //**************************************************************************************************************
        /// Specifies the font's scale in logical pixels.
        PIXEL("px", (1f / GuiFont.getRenderHeight())),
        
        /// Specifies the font's scale in "fixed points" (ignoring the screen's resolution),
        /// where 1 logical pixel is 6.75pt.
        POINT("pt", (1f / (GuiFont.getRenderHeight() * 0.75f))),
        
        /// Specifies the font's size in a relative font scale (e.g. 1em: 9 logical pixels, 2em: 18 logical pixels).
        EM("em", 1f),
        ;
        
        //**************************************************************************************************************
        /// The multiplier used for the scale conversion.
        public final float scaleFactor;
        
        /// The literal used after for this unit, after the number in a style string.
        public final String literal;
        
        //**************************************************************************************************************
        Unit(final @NotNull String literal, final float scaleFactor)
        {
            this.literal     = Objects.requireNonNull(literal);
            this.scaleFactor = scaleFactor;
        }
    }
    
    //******************************************************************************************************************
    /// The pattern used to parse font size style strings.
    /// - Pixels: `<number>px`
    /// - Points: `<number>pt`
    /// - Relative scale: `<number>em`
    @Language("RegExp")
    public static final String FONT_SIZE_PATTERN = "^([+-]?(?:[0-9]*[.])?[0-9]+)(px|pt|em)$";
    
    //******************************************************************************************************************
    /// Returns a font size object in logical pixels.
    /// @param value The value in logical pixels
    /// @return The [FontSize]
    /// @see Unit#PIXEL
    public static @NotNull FontSize pixels(final float value) { return new FontSize(Unit.PIXEL, value); }
    
    /// Returns a font size object in points.
    /// @param value The value in points
    /// @return The [FontSize]
    /// @see Unit#POINT
    public static @NotNull FontSize points(final float value) { return new FontSize(Unit.POINT, value); }
    
    /// Returns a font size object in relative em unit.
    /// @param value The value in em scale
    /// @return The [FontSize]
    /// @see Unit#EM
    public static @NotNull FontSize em(final float value) { return new FontSize(Unit.EM, value); }
    
    /// Creates a [FontSize] from the given style string, which is a number and the unit literal.
    /// @param value The style string (e.g. `1px` or `1pt`)
    /// @return A new [FontSize]
    /// @throws IllegalArgumentException If the `value` string was in an invalid format (see [#FONT_SIZE_PATTERN])
    public static @NotNull FontSize fromString(final @Pattern(FontSize.FONT_SIZE_PATTERN) @NotNull String value)
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
    public float getScaledValue() { return (this.unit.scaleFunc.get() * this.value); }
}

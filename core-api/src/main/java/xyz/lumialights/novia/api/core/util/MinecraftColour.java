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
package xyz.lumialights.novia.api.core.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;



//**********************************************************************************************************************
public enum MinecraftColour
    implements StringIdentifiable
{
    //******************************************************************************************************************
    BLACK            (0xFF000000),
    DARK_BLUE        (0xFF0000AA),
    DARK_GREEN       (0xFF00AA00),
    DARK_AQUA        (0xFF00AAAA),
    DARK_RED         (0xFFAA0000),
    DARK_PURPLE      (0xFFAA00AA),
    GOLD             (0xFFFFAA00),
    GRAY             (0xFFAAAAAA),
    DARK_GRAY        (0xFF555555),
    BLUE             (0xFF5555FF),
    GREEN            (0xFF55FF55),
    AQUA             (0xFF55FFFF),
    RED              (0xFFFF5555),
    LIGHT_PURPLE     (0xFFFF55FF),
    YELLOW           (0xFFFFFF55),
    WHITE            (0xFFFFFFFF),
    ;

    //******************************************************************************************************************
    public static final int ALPHA_CHANNEL_MASK  = 0xFF000000;
    public static final int RED_CHANNEL_MASK    = 0x00FF0000;
    public static final int GREEN_CHANNEL_MASK  = 0x0000FF00;
    public static final int BLUE_CHANNEL_MASK   = 0x000000FF;
    public static final int SINGLE_CHANNEL_MASK = 0xFF;
    
    public static final Codec<MinecraftColour> CODEC = StringIdentifiable.createCodec(
        MinecraftColour::values,
        String::toLowerCase);
    
    //******************************************************************************************************************
    public final int code;
    
    //******************************************************************************************************************
    MinecraftColour(final int colourCode)
    {
        this.code = colourCode;
    }
    
    //==================================================================================================================
    public int getAlpha() { return 0xFF; }
    public int getRed() { return ColorHelper.getRed(this.code); }
    public int getGreen() { return ColorHelper.getGreen(this.code); }
    public int getBlue() { return ColorHelper.getBlue(this.code); }
    
    public float getBrightness()
    {
        return (Math.max(Math.max(this.getRed(), this.getGreen()), this.getBlue())) / 255f;
    }
    
    //==================================================================================================================
    public int withAlpha(final int alpha)
    {
        return ((alpha << 24) | (this.code & ALPHA_CHANNEL_MASK));
    }
    
    public int withRed(final int red)
    {
        return (((red & SINGLE_CHANNEL_MASK) << 16) | (this.code & RED_CHANNEL_MASK));
    }
    
    public int withGreen(final int green)
    {
        return (((green & SINGLE_CHANNEL_MASK) << 8) | (this.code & GREEN_CHANNEL_MASK));
    }
    
    public int withBlue(final int blue)
    {
        return ((blue & SINGLE_CHANNEL_MASK) | (this.code & BLUE_CHANNEL_MASK));
    }
    
    //==================================================================================================================
    @Override public String asString() { return this.name().toLowerCase(); }
    
    public @NotNull Colour toColour()
    {
        return new Colour(this.code);
    }
}

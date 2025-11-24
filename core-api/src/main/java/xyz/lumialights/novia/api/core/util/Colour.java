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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



//**********************************************************************************************************************
public record Colour(int colour)
    implements Comparable<Integer>
{
    //******************************************************************************************************************
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Named
    {
        @NotNull String value() default "";
    }

    //******************************************************************************************************************
    /** Ordinary red <span style="color:#FF0000">██</span> */
    @Named public static final Colour RED = new Colour(0xFFFF0000);
    
    /** Ordinary green <span style="color:#00FF00">██</span> */
    @Named public static final Colour GREEN = new Colour(0xFF00FF00);
    
    /** Ordinary blue <span style="color:#0000FF">██</span> */
    @Named public static final Colour BLUE = new Colour(0xFF0000FF);
    
    /** Ordinary yellow <span style="color:#FFFF00">██</span> */
    @Named public static final Colour YELLOW = new Colour(0xFFFFFF00);
    
    /** Ordinary purple <span style="color:#800080">██</span> */
    @Named public static final Colour PURPLE = new Colour(0xFF800080);
    
    /** Ordinary orange <span style="color:#FF8000">██</span> */
    @Named public static final Colour ORANGE = new Colour(0xFFFF8000);
    
    /** Ordinary brown <span style="color:#804000">██</span> */
    @Named public static final Colour BROWN = new Colour(0xFF804000);
    
    /** Ordinary pink <span style="color:#FFC0CB">██</span> */
    @Named public static final Colour PINK = new Colour(0xFFFFC0CB);
    
    /** Ordinary violet <span style="color:#EE82EE">██</span> */
    @Named public static final Colour VIOLET = new Colour(0xFFEE82EE);
    
    /** A vibrant greenish-blue colour <span style="color:#00FFFF">██</span> */
    @Named public static final Colour CYAN = new Colour(0xFF00FFFF);
    
    /** A bright reddish-purple colour <span style="color:#FF00FF">██</span> */
    @Named public static final Colour MAGENTA = new Colour(0xFFFF00FF);
    
    /** A medium grey hue <span style="color:#808080">██</span> */
    @Named public static final Colour GREY = new Colour(0xFF808080);
    
    /** A bright pale violet colour <span style="color:#E6E6FA">██</span> */
    @Named public static final Colour LAVENDER = new Colour(0xFFE6E6FA);
    
    /** A soft light shade of pink <span style="color:#A47DAB">██</span> */
    @Named public static final Colour LILAC = new Colour(0xFFA47DAB);
    
    /** A bright pale brown colour <span style="color:#D2B48C">██</span> */
    @Named public static final Colour TAN = new Colour(0xFFD2B48C);
    
    /** A medium grey hue <span style="color:#C0C0C0">██</span> */
    @Named public static final Colour SILVER = new Colour(0xFFC0C0C0);
    
    /** A luxurious warm bright yellow <span style="color:#FFD700">██</span> */
    @Named public static final Colour GOLD = new Colour(0xFFFFD700);
    
    /** A bright light greenish-blue colour <span style="color:#40E0D0">██</span> */
    @Named public static final Colour TURQUOISE = new Colour(0xFF40E0D0);
    
    /** A dark shade of red <span style="color:#800000">██</span> */
    @Named public static final Colour MAROON = new Colour(0xFF800000);
    
    /** A deep dark shade of blue <span style="color:#000080">██</span> */
    @Named public static final Colour NAVY_BLUE = new Colour(0xFF000080);
    
    /** A pale brown colour <span style="color:#F5F5DC">██</span> */
    @Named public static final Colour BEIGE = new Colour(0xFFF5F5DC);
    
    /** A light yellowish-orange colour <span style="color:#FFDAB9">██</span> */
    @Named public static final Colour PEACH = new Colour(0xFFFFDAB9);
    
    /** A bright vivid green <span style="color:#32CD32">██</span> */
    @Named public static final Colour LIME = new Colour(0xFF32CD32);
    
    /** A dark pale green <span style="color:#808000">██</span> */
    @Named public static final Colour OLIVE = new Colour(0xFF808000);
    
    /** A vibrant greenish-blue colour <span style="color:#008080">██</span> */
    @Named public static final Colour TEAL = new Colour(0xFF008080);
    
    /** A rich dark violet colour <span style="color:#4B0082">██</span> */
    @Named public static final Colour INDIGO = new Colour(0xFF4B0082);
    
    /** A vibrant greenish-yellow colour <span style="color:#7FFF00">██</span> */
    @Named public static final Colour CHARTREUSE = new Colour(0xFF7FFF00);
    
    /** White <span style="color:#FFFFFF">██</span> */
    @Named public static final Colour WHITE = new Colour(0xFFFFFFFF);
    
    /** Black <span style="color:#000000">██</span> */
    @Named public static final Colour BLACK = new Colour(0xFF000000);
    
    /** No colour. */
    @Named public static final Colour TRANSPARENT = new Colour(0);
    
    //==================================================================================================================
    public static final PacketCodec<ByteBuf, Colour> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, Colour::colour,
        Colour::new);

    /** Codec for hex string colour values. (#AARRGGBB) */
    public static final Codec<Colour> HEX_CODEC;

    /** Codec for colour name strings (see this class' constants for names).  */
    public static final Codec<Colour> NAME_CODEC;

    /** Codec for integer colour values. */
    public static final Codec<Colour> INT_CODEC;

    /**
     * Codec for dynamic properties, either hex strings, name strings or integers.
     * @see #HEX_CODEC
     * @see #NAME_CODEC
     * @see #INT_CODEC
     */
    public static final Codec<Colour> CODEC;

    //------------------------------------------------------------------------------------------------------------------
    private static final Map<String, Colour> NAMED;

    //==================================================================================================================
    static
    {
        NAMED = Arrays
            .stream(Colour.class.getDeclaredFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Named.class))
            .map(field ->
            {
                final Named ann = field.getDeclaredAnnotation(Named.class);

                if (ann != null)
                {
                    final String name = (!ann.value().isEmpty() ? ann.value() : field.getName()).toLowerCase();

                    try
                    {
                        return Pair.of(name, (Colour) field.get(null));
                    }
                    catch (final IllegalAccessException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }

                return Pair.of((String) null, (Colour) null);
            })
            .filter(p -> p.first() != null)
            .collect(ImmutableMap.toImmutableMap(Pair::first, Pair::second));

        HEX_CODEC = Codec.STRING.comapFlatMap(
            (str ->
            {
                try
                {
                    return DataResult.success(Colour.fromHexString(str));
                }
                catch (final NumberFormatException ex)
                {
                    return DataResult.error(ex::getMessage);
                }
            }),
            Colour::toString);

        NAME_CODEC = Codec.STRING.flatXmap(
            (str -> Optional
                .ofNullable(NAMED.get(str.toLowerCase()))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "no colour for name " + str))),
            (col -> NAMED
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(col))
                .map(Map.Entry::getKey)
                .findFirst()
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "colour value does not have a name"))));

        INT_CODEC = Codec.INT.xmap(Colour::new, Colour::colour);

        CODEC = Codec.withAlternative(INT_CODEC, Codec.withAlternative(NAME_CODEC, HEX_CODEC));
    }

    //******************************************************************************************************************
    public static @NotNull Colour fromHexString(final @NotNull String str)
    {
        final Matcher m = Pattern.compile("^#([0-9a-fA-F]{6})([0-9a-fA-F]{2})?$").matcher(str);
        
        if (!m.matches())
        {
            throw new NumberFormatException("Invalid hex/rgb(a) colour '" + str + "'");
        }
        
        final int colour = (
            Integer.parseUnsignedInt(m.group(1), 16)
                | (m.group(2) != null
                   ? (Integer.parseUnsignedInt(m.group(2), 16) << 24)
                   : 0xff000000));
        return new Colour(colour);
    }
    
    public static @NotNull Colour fromRgb(final int rgb) { return new Colour(0xFF000000 | rgb); }
    
    //==================================================================================================================
    public static int getAlphaChannel(final int colour) { return (colour >> 24 & 0xff); }
    
    public static int getRedChannel(final int colour) { return (colour >> 16 & 0xff); }
    
    public static int getGreenChannel(final int colour) { return (colour >> 8 & 0xff); }
    
    public static int getBlueChannel(final int colour) { return (colour & 0xff); }
    
    /**
     * Gets the colour's relative luminance value as described here:
     * <a href="https://www.w3.org/WAI/GL/wiki/Relative_luminance">W3C Relative Luminance</a>
     * @param colour The colour value
     * @return The colour's relative luminance
     */
    public static float calculateLuminance(final int colour)
    {
        final double RsRGB = (getRedChannel  (colour) / 255.0d);
        final double GsRGB = (getGreenChannel(colour) / 255.0d);
        final double BsRGB = (getBlueChannel (colour) / 255.0d);
        final double R     = (RsRGB <= 0.04045 ? (RsRGB / 12.92) : (Math.pow(((RsRGB + 0.055) / 1.055), 2.4)));
        final double G     = (GsRGB <= 0.04045 ? (GsRGB / 12.92) : (Math.pow(((GsRGB + 0.055) / 1.055), 2.4)));
        final double B     = (BsRGB <= 0.04045 ? (BsRGB / 12.92) : (Math.pow(((BsRGB + 0.055) / 1.055), 2.4)));
        
        return (float)((0.2126f * R) + (0.7152f * G) + (0.0722f * B));
    }
    
    //******************************************************************************************************************
    public Colour(final int red, final int green, final int blue)
    {
        this(red, green, blue, 0xff);
    }
    
    public Colour(final int red, final int green, final int blue, final int alpha)
    {
        this((alpha & 0xff << 24) | (red & 0xff << 16) | (green & 0xff << 8) | (blue & 0xff));
    }
    
    //==================================================================================================================
    /**
     * Gets the colour's relative luminance value as described here:
     * <a href="https://www.w3.org/WAI/GL/wiki/Relative_luminance">W3C Relative Luminance</a>
     * @return The colour's relative luminance
     */
    public float getLuminance() { return Colour.calculateLuminance(this.colour); }

    /**
     * Gets the best fitting contrasting colour of the current colour, which will be either black or white.
     * @return The contrasting {@link Colour}
     */
    public @NotNull Colour getContrasting() { return this.getContrasting(Colour.WHITE, Colour.BLACK); }

    /**
     * Gets the best fitting contrasting colour of the two colours given.
     * @param colour1 The first option
     * @param colour2 The second option
     * @return The better fitting of the two colours in terms of contrast
     */
    public @NotNull Colour getContrasting(final int colour1, final int colour2)
    {
        final float L1 = Colour.calculateLuminance(colour1);
        final float L2 = Colour.calculateLuminance(colour2);
        final float L3 = this.getLuminance();

        if (L1 > L2)
        {
            return new Colour((((L3 + 0.05f) / (L2 + 0.05f)) > ((L1 + 0.05f) / (L3 + 0.05f))) ? colour2 : colour1);
        }
        else
        {
            return new Colour((((L3 + 0.05f) / (L1 + 0.05f)) > ((L2 + 0.05f) / (L3 + 0.05f))) ? colour1 : colour2);
        }
    }

    /**
     * Gets the best fitting contrasting colour of the two colours given.
     * @param colour1 The first option
     * @param colour2 The second option
     * @return The better fitting of the two colours in terms of contrast
     */
    public @NotNull Colour getContrasting(final @NotNull Colour colour1, final @NotNull Colour colour2)
    {
        return this.getContrasting(colour1.colour, colour2.colour);
    }

    public @NotNull Colour getInverted() { return new Colour(this.colour ^ 0x00FFFFFF); }
    
    //==================================================================================================================
    public int alpha() { return Colour.getAlphaChannel(this.colour); }
    public int red()   { return Colour.getRedChannel  (this.colour); }
    public int green() { return Colour.getGreenChannel(this.colour); }
    public int blue()  { return Colour.getBlueChannel (this.colour); }
    
    public float alphaNormalised() { return (this.alpha() / 255F); }
    public float redNormalised()   { return (this.red()   / 255F); }
    public float greenNormalised() { return (this.green() / 255F); }
    public float blueNormalised()  { return (this.blue()  / 255F); }
    
    //==================================================================================================================
    public @NotNull Colour withAlpha(final int alpha)
    {
        return new Colour((this.colour & 0x00FFFFFF) | (alpha << 24));
    }
    
    public @NotNull Colour withAlphaMixed(final int other)
    {
        final float base_alpha   = ColorHelper.getAlphaFloat(other);
        final float target_alpha = this.alpha();
        return new Colour(base_alpha != 1.0f
            ? ColorHelper.withAlpha(ColorHelper.channelFromFloat(base_alpha * target_alpha), this.colour)
            : this.colour);
    }
    
    public @NotNull Colour withAlphaMixed(final @NotNull Colour other) { return this.withAlphaMixed(other.colour); }
    
    public @NotNull Colour withRed(final int red)
    {
        return new Colour((this.colour & 0xFF00FFFF) | ((red & 0xFF) << 16));
    }
    
    public @NotNull Colour withGreen(final int green)
    {
        return new Colour((this.colour & 0xFFFF00FF) | ((green & 0xFF) << 8));
    }
    
    public @NotNull Colour withBlue(final int blue)
    {
        return new Colour((this.colour & 0xFFFFFF00) | (blue & 0xFF));
    }
    
    public @NotNull Colour withOpacity(final float opacity)
    {
        return this.withAlpha(MathHelper.floor(opacity * 0xFF));
    }
    
    public @NotNull Colour withOpacityRel(final float opacity)
    {
        if (opacity == 1.0f)
        {
            return this;
        }

        final float alpha = ColorHelper.getAlphaFloat(this.colour);
        return new Colour(ColorHelper.withAlpha(ColorHelper.channelFromFloat(opacity * alpha), this.colour));
    }
    
    //==================================================================================================================
    @Override public @NotNull String toString() { return "#%08x".formatted(this.colour); }

    //==================================================================================================================
    @Override public int compareTo(final @NotNull Integer o) { return Integer.compare(o, this.colour); }
}

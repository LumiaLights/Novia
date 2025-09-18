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
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.atomic.*;


//**********************************************************************************************************************
public enum NumberType
    implements StringIdentifiable
{
    //******************************************************************************************************************
    BYTE(Byte.class),
    SHORT(Short.class),
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    NON_SCALAR(Number.class);

    //******************************************************************************************************************
    /**
     * The packet codec for the number type.
     */
    public static final PacketCodec<ByteBuf, NumberType> PACKET_CODEC = PacketCodecs
        .indexed(
            (i -> NumberType.values()[i]),
            NumberType::ordinal);

    /**
     * The serialisation codec for the number type.
     */
    public static final Codec<NumberType> CODEC = StringIdentifiable.createCodec(NumberType::values);

    //******************************************************************************************************************

    /**
     * Gets the associated enum constant for the instantiated subclass of <code>number</code>.
     * If the number is neither {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, {@link Float} nor
     * {@link Double}, {@link NumberType#NON_SCALAR} will be returned.
     * @param number The number
     * @return The {@link NumberType}
     */
    public static @NotNull NumberType of(@NotNull final Number number)
    {
        final Class<? extends Number> clazz = number.getClass();
        return Arrays
            .stream(NumberType.values())
            .filter(c -> c.numberClass.equals(clazz))
            .findFirst()
            .orElse(NON_SCALAR);
    }

    public static boolean isFloatingPointScalar(@Nullable final Number number)
    {
        return (number instanceof Float || number instanceof Double);
    }

    public static boolean isNonScalarFloatingPoint(@Nullable final Number number)
    {
        return (number instanceof BigDecimal
             || number instanceof DoubleAccumulator
             || number instanceof DoubleAdder);
    }
    
    public static boolean isFloatingPoint(@Nullable final Number number)
    {
        return (isFloatingPointScalar(number) || isNonScalarFloatingPoint(number));
    }

    public static boolean isIntegralScalar(@Nullable final Number number)
    {
        return (number instanceof Byte
             || number instanceof Short
             || number instanceof Integer
             || number instanceof Long);
    }
    
    public static boolean isNonScalarIntegral(@Nullable final Number number)
    {
        return (number instanceof BigInteger
             || number instanceof AtomicInteger
             || number instanceof AtomicLong
             || number instanceof LongAccumulator
             || number instanceof LongAdder);
    }
    
    public static boolean isIntegral(@Nullable final Number number)
    {
        return (isIntegralScalar(number) || isNonScalarIntegral(number));
    }

    public static boolean isScalar(@Nullable final Number number)
    {
        return (isIntegralScalar(number) || isFloatingPointScalar(number));
    }

    //******************************************************************************************************************
    private final Class<? extends Number> numberClass;

    //******************************************************************************************************************
    <T extends Number> NumberType(@NotNull final Class<T> clazz)
    {
        this.numberClass = clazz;
    }

    //==================================================================================================================
    /**
     * Gets the associated type class.
     * @return The number type class
     */
    public Class<? extends Number> getTypeClass()
    {
        return this.numberClass;
    }

    //==================================================================================================================
    public boolean isFloatingPointScalar()
    {
        return (this == FLOAT || this == DOUBLE);
    }

    public boolean isIntegralScalar()
    {
        return (this == BYTE || this == SHORT || this == INT || this == LONG);
    }

    public boolean isNonScalar()
    {
        return this == NON_SCALAR;
    }

    //==================================================================================================================
    @Override
    public String asString() { return this.name().toLowerCase(); }
}

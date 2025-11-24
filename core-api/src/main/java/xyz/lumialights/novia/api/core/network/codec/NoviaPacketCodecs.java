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
package xyz.lumialights.novia.api.core.network.codec;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.util.NumberType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.function.IntFunction;



//**********************************************************************************************************************
public abstract class NoviaPacketCodecs
{
    //******************************************************************************************************************
    /** Provides a packet codec for {@link BigInteger} types. */
    public static final PacketCodec<ByteBuf, BigInteger> BIG_INTEGER;

    /** Provides a packet codec for {@link BigDecimal} types. */
    public static final PacketCodec<ByteBuf, BigDecimal> BIG_DECIMAL;

    /**
     * Provides a {@link PacketCodec} that serialises objects of type {@link Number}
     * depending on the dynamic type of the object. (e.g. {@link Integer} or {@link Float})
     * <p>
     * Types with bigger precision (like {@link BigDecimal}) will be converted to their scalar counter-parts,
     * resulting in precision loss.
     */
    public static final PacketCodec<ByteBuf, Number> NUMBER;
    
    //------------------------------------------------------------------------------------------------------------------
    private static final PacketCodec<ByteBuf, Number> NON_SCALAR;

    //==================================================================================================================
    static
    {
        BIG_INTEGER = PacketCodecs.BYTE_ARRAY.xmap(BigInteger::new, BigInteger::toByteArray);
        BIG_DECIMAL = PacketCodec
            .tuple(
                BIG_INTEGER,          BigDecimal::unscaledValue,
                PacketCodecs.INTEGER, BigDecimal::scale,
                PacketCodecs.INTEGER, BigDecimal::precision,
                (i, s, p) -> new BigDecimal(i, s, new MathContext(p)));

        NUMBER = NumberType.PACKET_CODEC.dispatch(NumberType::of, (type ->
            switch (type)
            {
                case BYTE       -> PacketCodecs.BYTE;
                case SHORT      -> PacketCodecs.SHORT;
                case INT        -> PacketCodecs.INTEGER;
                case LONG       -> PacketCodecs.LONG;
                case FLOAT      -> PacketCodecs.FLOAT;
                case DOUBLE     -> PacketCodecs.DOUBLE;
                case NON_SCALAR -> NoviaPacketCodecs.NON_SCALAR;
            }));

        NON_SCALAR = new PacketCodec<>()
        {
            @Override
            public Number decode(final ByteBuf buf)
            {
                return (buf.readBoolean() ? PacketCodecs.LONG.decode(buf) : PacketCodecs.DOUBLE.decode(buf));
            }

            @Override
            public void encode(final ByteBuf buf, final Number value)
            {
                if (value.longValue() == value.doubleValue())
                {
                    buf.writeBoolean(true);
                    PacketCodecs.LONG.encode(buf, value.longValue());
                }
                else
                {
                    buf.writeBoolean(false);
                    PacketCodecs.DOUBLE.encode(buf, value.doubleValue());
                }
            }
        };
    }
    
    //==================================================================================================================
    public static <B extends ByteBuf, V, C extends ImmutableCollection.Builder<V>>
        @NotNull PacketCodec<B, ImmutableCollection<V>> immutableCollection(
		    @NotNull final IntFunction<C>            builderFactory,
            @NotNull final PacketCodec<? super B, V> elementCodec)
    {
        return immutableCollection(builderFactory, elementCodec, Integer.MAX_VALUE);
    }
    
    public static <B extends ByteBuf, V, C extends ImmutableCollection.Builder<V>>
        @NotNull PacketCodec<B, ImmutableCollection<V>> immutableCollection(
            @NotNull final IntFunction<C>            builderFactory,
            @NotNull final PacketCodec<? super B, V> elementCodec,
                     final int                       maxSize)
    {
		return new PacketCodec<>()
        {
			public ImmutableCollection<V> decode(final B buf)
            {
				final int i       = PacketCodecs.readCollectionSize(buf, maxSize);
				final C   builder = builderFactory.apply(Math.min(i, 65536));

				for (int j = 0; j < i; j++)
                {
					builder.add(elementCodec.decode(buf));
				}
                
                return builder.build();
			}

			public void encode(final B buf, final ImmutableCollection<V> collection)
            {
				PacketCodecs.writeCollectionSize(buf, collection.size(), maxSize);

				for (final var object : collection)
                {
					elementCodec.encode(buf, object);
				}
			}
		};
	}
    
    public static <B extends ByteBuf, V>
        PacketCodec.@NotNull ResultFunction<B, V, ? extends ImmutableCollection<V>> toImmutableCollection(
            @NotNull final IntFunction<ImmutableCollection.Builder<V>> collectionFactory)
    {
		return (codec -> immutableCollection(collectionFactory, codec));
	}
    
    //==================================================================================================================
    public static <B extends ByteBuf, K, V, M extends ImmutableMap.Builder<K, V>>
        @NotNull PacketCodec<B, ImmutableMap<K, V>> immutableMap(
		    @NotNull final IntFunction<? extends M>  factory,
            @NotNull final PacketCodec<? super B, K> keyCodec,
            @NotNull final PacketCodec<? super B, V> valueCodec)
    {
		return immutableMap(factory, keyCodec, valueCodec, Integer.MAX_VALUE);
	}

	public static <B extends ByteBuf, K, V, M extends ImmutableMap.Builder<K, V>>
        @NotNull PacketCodec<B, ImmutableMap<K, V>> immutableMap(
            @NotNull final IntFunction<? extends M>  factory,
            @NotNull final PacketCodec<? super B, K> keyCodec,
            @NotNull final PacketCodec<? super B, V> valueCodec,
                     final int                       maxSize)
    {
		return new PacketCodec<>()
        {
            public ImmutableMap<K, V> decode(final B byteBuf)
            {
				final int i       = PacketCodecs.readCollectionSize(byteBuf, maxSize);
				final M   builder = factory.apply(Math.min(i, 65536));

				for (int j = 0; j < i; j++)
                {
					K object = keyCodec.decode(byteBuf);
					V object2 = valueCodec.decode(byteBuf);
					builder.put(object, object2);
				}

				return builder.build();
			}
   
			public void encode(final B byteBuf, final ImmutableMap<K, V> map)
            {
				PacketCodecs.writeCollectionSize(byteBuf, map.size(), maxSize);
				map.forEach((k, v) -> {
					keyCodec.encode(byteBuf, (K)k);
					valueCodec.encode(byteBuf, (V)v);
				});
			}
		};
	}
    
    //==================================================================================================================
    public static <B extends ByteBuf, V, C extends ImmutableList<V>> @NotNull PacketCodec<B, C> immutableList(
        @NotNull final PacketCodec<B, V> elementCodec)
    {
        return immutableList(elementCodec, Integer.MAX_VALUE);
    }
    
    @SuppressWarnings("unchecked")
    public static <B extends ByteBuf, V, C extends ImmutableList<V>> @NotNull PacketCodec<B, C> immutableList(
        @NotNull final PacketCodec<B, V> elementCodec,
                 final int               maxSize)
    {
        return immutableCollection(ImmutableList::builderWithExpectedSize, elementCodec, maxSize)
            .xmap((coll) -> (C) coll, (coll) -> coll);
    }
    
    public static <B extends ByteBuf, V, C extends ImmutableList<V>>
        PacketCodec.@NotNull ResultFunction<B, V, C> toImmutableList()
    {
		return NoviaPacketCodecs::immutableList;
	}
    
    //==================================================================================================================
    public static <B extends ByteBuf, V, C extends ImmutableSet<V>> @NotNull PacketCodec<B, C> immutableSet(
        @NotNull final PacketCodec<B, V> elementCodec)
    {
        return immutableSet(elementCodec, Integer.MAX_VALUE);
    }
    
    @SuppressWarnings("unchecked")
    public static <B extends ByteBuf, V, C extends ImmutableSet<V>> @NotNull PacketCodec<B, C> immutableSet(
        @NotNull final PacketCodec<B, V> elementCodec,
                 final int               maxSize)
    {
        return immutableCollection(ImmutableSet::builderWithExpectedSize, elementCodec, maxSize)
            .xmap((coll) -> (C) coll, (coll) -> coll);
    }
    
    public static <B extends ByteBuf, V, C extends ImmutableSet<V>>
        PacketCodec.@NotNull ResultFunction<B, V, C> toImmutableSet()
    {
		return NoviaPacketCodecs::immutableSet;
	}
    
    //==================================================================================================================
    public static <E extends Enum<E>> @NotNull PacketCodec<ByteBuf, E> enumeration(final @NotNull Class<E> enumeration)
    {
        final E[] constants = enumeration.getEnumConstants();
        return PacketCodecs.VAR_INT.xmap((ord -> constants[ord]), Enum::ordinal);
    }
}

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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;



//**********************************************************************************************************************
public record Triple<T, U, V>(T first, U second, V third)
{
    //******************************************************************************************************************
    public static <T, U, V> @NotNull Triple<T, U, V> of(final T first, final U second, final V third)
    {
        return new Triple<>(first, second, third);
    }
    
    //==================================================================================================================
    /**
     * Utility function providing a converter for the first element of a {@link Triple}.
     * @param converter The function converting the first object of a {@link Triple}
     * @return The conversion function
     * @param <T> Type of first object in {@link Triple}
     * @param <U> Type of second object in {@link Triple}
     * @param <V> Type of third object in {@link Triple}
     * @param <R> Resulting type of the conversion of the first object
     */
    public static <T, U, V, R> @NotNull Function<Triple<T, U, V>, R> forFirst(@NotNull final Function<T, R> converter)
    {
        return (triple -> converter.apply(triple.first()));
    }

    /**
     * Utility function providing a converter for the second element of a {@link Triple}.
     * @param converter The function converting the second object of a {@link Triple}
     * @return The conversion function
     * @param <T> Type of first object in {@link Triple}
     * @param <U> Type of second object in {@link Triple}
     * @param <V> Type of third object in {@link Triple}
     * @param <R> Resulting type of the conversion of the second object
     */
    public static <T, U, V, R> @NotNull Function<Triple<T, U, V>, R> forSecond(@NotNull final Function<U, R> converter)
    {
        return (triple -> converter.apply(triple.second()));
    }
    
    /**
     * Utility function providing a converter for the third element of a {@link Triple}.
     * @param converter The function converting the third object of a {@link Triple}
     * @return The conversion function
     * @param <T> Type of first object in {@link Triple}
     * @param <U> Type of second object in {@link Triple}
     * @param <V> Type of third object in {@link Triple}
     * @param <R> Resulting type of the conversion of the third object
     */
    public static <T, U, V, R> @NotNull Function<Triple<T, U, V>, R> forThird(@NotNull final Function<V, R> converter)
    {
        return (triple -> converter.apply(triple.third()));
    }
    
    //==================================================================================================================
    /**
     * Creates a packet codec for a given {@link Triple}.
     * @param codecFirst  The packet codec of the first element
     * @param codecSecond The packet codec of the second element
     * @param codecThird  The packet codec of the third element
     * @return The new combined packet codec for both objects
     * @param <T> Type of first object in {@link Triple}
     * @param <U> Type of second object in {@link Triple}
     * @param <V> Type of third object in {@link Triple}
     * @param <B> The {@link ByteBuf} type
     */
    public static <T, U, V, B extends ByteBuf> @NotNull PacketCodec<B, Triple<T, U, V>> createPacketCodec(
        @NotNull final PacketCodec<B, T> codecFirst,
        @NotNull final PacketCodec<B, U> codecSecond,
        @NotNull final PacketCodec<B, V> codecThird
    )
    {
        return PacketCodec.tuple(
            codecFirst,  Triple::first,
            codecSecond, Triple::second,
            codecThird,  Triple::third,
            Triple::new);
    }
    
    public static <T, U, V> @NotNull MapCodec<Triple<T, U, V>> createMapCodec(
        @NotNull final Codec<T> codecFirst,
        @NotNull final Codec<U> codecSecond,
        @NotNull final Codec<V> codecThird
    )
    {
        return RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                codecFirst
                    .fieldOf("first")
                    .forGetter(Triple::first),
                codecSecond
                    .fieldOf("second")
                    .forGetter(Triple::second),
                codecThird
                    .fieldOf("third")
                    .forGetter(Triple::third))
            .apply(instance, Triple::new));
    }
    
    public static <T, U, V> @NotNull Codec<Triple<T, U, V>> createCodec(
        @NotNull final Codec<T> codecFirst,
        @NotNull final Codec<U> codecSecond,
        @NotNull final Codec<V> codecThird
    )
    {
        return createMapCodec(codecFirst, codecSecond, codecThird).codec();
    }
    
    //******************************************************************************************************************
    public <F> @NotNull Triple<F, U, V> withFirst(final F first)
    {
        return Triple.of(first, this.second, this.third);
    }
    
    public <S> @NotNull Triple<T, S, V> withSecond(final S second)
    {
        return Triple.of(this.first, second, this.third);
    }
    
    public <D> @NotNull Triple<T, U, D> withThird(final D third)
    {
        return Triple.of(this.first, this.second, third);
    }
    
    //==================================================================================================================
    public <F, S, D> @NotNull Triple<F, S, D> map(@NotNull final Function<T, F> firstMapper,
                                                  @NotNull final Function<U, S> secondMapper,
                                                  @NotNull final Function<V, D> thirdMapper)
    {
        return Triple.of(firstMapper.apply(this.first), secondMapper.apply(this.second), thirdMapper.apply(this.third));
    }
    
    public <F> @NotNull Triple<F, U, V> mapFirst(@NotNull final Function<T, F> mapper)
    {
        return Triple.of(mapper.apply(this.first), this.second, this.third);
    }
    
    public <S> @NotNull Triple<T, S, V> mapSecond(@NotNull final Function<U, S> mapper)
    {
        return Triple.of(this.first, mapper.apply(this.second), this.third);
    }
    
    public <D> @NotNull Triple<T, U, D> mapThird(@NotNull final Function<V, D> mapper)
    {
        return Triple.of(this.first, this.second, mapper.apply(this.third));
    }
    
    public <R> R flatMapFirst(final @NotNull Function<T, R> mapper) { return mapper.apply(this.first); }
    
    public <R> R flatMapSecond(final @NotNull Function<U, R> mapper) { return mapper.apply(this.second); }
    
    public <R> R flatMapThird(final @NotNull Function<V, R> mapper) { return mapper.apply(this.third); }
    
    public void apply(final @NotNull Consumer<T> first,
                      final @NotNull Consumer<U> second,
                      final @NotNull Consumer<V> third)
    {
        first .accept(this.first);
        second.accept(this.second);
        third .accept(this.third);
    }
    
    public void applyFirst(final @NotNull Consumer<T> consumer) { consumer.accept(this.first); }
    
    public void applySecond(final @NotNull Consumer<U> consumer) { consumer.accept(this.second); }
    
    public void applyThird(final @NotNull Consumer<V> consumer) { consumer.accept(this.third); }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        return ("{first="  + this.first
              + ",second=" + this.second
              + ",third="  + this.third
              + "}");
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                                                               return true;
        if (!(obj instanceof Triple(Object o_first, Object o_second, Object o_third))) return false;

        return (Objects.equals(this.first,  o_first)
             && Objects.equals(this.second, o_second)
             && Objects.equals(this.third,  o_third));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.first, this.second, this.third);
    }
}

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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


//**********************************************************************************************************************
/**
 * Provides a utility tuple with two objects.
 * @param first  The first object
 * @param second The second object
 * @param <T> Type of first object
 * @param <U> Type of second object
 */
public record Pair<T, U>(T first, U second)
{
    //******************************************************************************************************************
    /**
     * Creates a pair of the given two objects.
     * @param first  The first object
     * @param second The second object
     * @return The new {@link Pair}
     * @param <T> Type of first object
     * @param <U> Type of second object
     */
    public static <T, U> @NotNull Pair<T, U> of(final T first, final U second)
    {
        return new Pair<>(first, second);
    }
    
    /**
     * Creates a pair from the given {@link Map.Entry}
     * @param entry The map entry
     * @return The new {@link Pair}
     * @param <T> Type of first object
     * @param <U> Type of second object
     */
    public static <T, U> @NotNull Pair<T, U> of(@NotNull final Map.Entry<T, U> entry)
    {
        return of(entry.getKey(), entry.getValue());
    }
    
    //==================================================================================================================
    /**
     * Utility function providing a converter for the first element of a {@link Pair}.
     * @param converter The function converting the first object of a {@link Pair}
     * @return The conversion function
     * @param <T> Type of first object in {@link Pair}
     * @param <U> Type of second object in {@link Pair}
     * @param <R> Resulting type of the conversion of the first object
     */
    public static <T, U, R> @NotNull Function<Pair<T, U>, R> forFirst(@NotNull final Function<T, R> converter)
    {
        return (pair -> converter.apply(pair.first()));
    }

    /**
     * Utility function providing a converter for the second element of a {@link Pair}.
     * @param converter The function converting the second object of a {@link Pair}
     * @return The conversion function
     * @param <T> Type of first object in {@link Pair}
     * @param <U> Type of second object in {@link Pair}
     * @param <R> Resulting type of the conversion of the second object
     */
    public static <T, U, R> @NotNull Function<Pair<T, U>, R> forSecond(@NotNull final Function<U, R> converter)
    {
        return (pair -> converter.apply(pair.second()));
    }
    
    public static <T, U, R> @NotNull Function<Pair<T, U>, R> forBoth(@NotNull final BiFunction<T, U, R> converter)
    {
        return (pair -> converter.apply(pair.first(), pair.second()));
    }

    //==================================================================================================================
    /**
     * Creates a packet codec for a given pair.
     * @param codecFirst  The packet codec of the first element
     * @param codecSecond The packet codec of the second element
     * @return The new combined packet codec for both objects
     * @param <T> Type of first object in {@link Pair}
     * @param <U> Type of second object in {@link Pair}
     * @param <B> The {@link ByteBuf} type
     */
    public static <T, U, B extends ByteBuf> @NotNull PacketCodec<B, Pair<T, U>> getPacketCodec(
        @NotNull final PacketCodec<B, T> codecFirst,
        @NotNull final PacketCodec<B, U> codecSecond)
    {
        return PacketCodec.tuple(
            codecFirst,  Pair::first,
            codecSecond, Pair::second,
            Pair::new);
    }

    public static <T, U> @NotNull MapCodec<Pair<T, U>> createMapCodec(
        @NotNull final Codec<T> codecFirst,
        @NotNull final Codec<U> codecSecond
    )
    {
        return RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                codecFirst
                    .fieldOf("first")
                    .forGetter(Pair::first),
                codecSecond
                    .fieldOf("second")
                    .forGetter(Pair::second))
            .apply(instance, Pair::new));
    }
    
    public static <T, U> @NotNull Codec<Pair<T, U>> createCodec(
        @NotNull final Codec<T> codecFirst,
        @NotNull final Codec<U> codecSecond
    )
    {
        return createMapCodec(codecFirst, codecSecond).codec();
    }
    
    //******************************************************************************************************************
    public <F> @NotNull Pair<F, U> withFirst(final F first)
    {
        return Pair.of(first, this.second);
    }
    
    public <S> @NotNull Pair<T, S> withSecond(final S second)
    {
        return Pair.of(this.first, second);
    }
    
    //==================================================================================================================
    public <F, S> @NotNull Pair<F, S> map(@NotNull final Function<T, F> firstMapper,
                                          @NotNull final Function<U, S> secondMapper)
    {
        return Pair.of(firstMapper.apply(this.first), secondMapper.apply(this.second));
    }
    
    public <F> @NotNull Pair<F, U> mapFirst(@NotNull final Function<T, F> mapper)
    {
        return Pair.of(mapper.apply(this.first), this.second);
    }
    
    public <S> @NotNull Pair<T, S> mapSecond(@NotNull final Function<U, S> mapper)
    {
        return Pair.of(this.first, mapper.apply(this.second));
    }
    
    public <R> R flatMapFirst(final @NotNull Function<T, R> mapper) { return mapper.apply(this.first); }
    
    public <R> R flatMapSecond(final @NotNull Function<U, R> mapper) { return mapper.apply(this.second); }
    
    public void apply(final @NotNull Consumer<T> first, final @NotNull Consumer<U> second)
    {
        first .accept(this.first);
        second.accept(this.second);
    }
    
    public void applyFirst(final @NotNull Consumer<T> consumer) { consumer.accept(this.first); }
    
    public void applySecond(final @NotNull Consumer<U> consumer) { consumer.accept(this.second); }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        return ("{first=" + this.first
                + ",second=" + this.second + "}");
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                                             return true;
        if (!(obj instanceof Pair(Object o_first, Object o_second))) return false;
        return (Objects.equals(this.first,  o_first)
             && Objects.equals(this.second, o_second));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.first, this.second);
    }
}

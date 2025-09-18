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
package xyz.lumialights.novia.api.core.serialisation.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;



//**********************************************************************************************************************
public record CollectionCodec<E, C extends Collection<E>>(@NotNull Codec<E>                   elementCodec,
                                                          @NotNull Function<Collection<E>, C> copyFunction,
                                                                   int                        minSize,
                                                                   int                        maxSize)
    implements Codec<C>
{
    //******************************************************************************************************************
    private class DecoderState<T>
    {
        //**************************************************************************************************************
        private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

        //**************************************************************************************************************
        private final DynamicOps<T>     ops;
        private final C                 elements = copyFunction.apply(new ArrayList<>());
        private final Stream.Builder<T> failed   = Stream.builder();
        
        private int              totalCount;
        private DataResult<Unit> result     = INITIAL_RESULT;
        
        //**************************************************************************************************************
        private DecoderState(@NotNull final DynamicOps<T> ops)
        {
            this.ops = ops;
        }

        //==============================================================================================================
        public void accept(final T value)
        {
            this.totalCount++;
            
            if (this.elements.size() >= maxSize)
            {
                this.failed.add(value);
                return;
            }
            
            final DataResult<Pair<E, T>> element_result = elementCodec.decode(this.ops, value);
            
            element_result.error().ifPresent(error -> this.failed.add(value));
            element_result.resultOrPartial().ifPresent(pair -> this.elements.add(pair.getFirst()));
            
            this.result = this.result.apply2stable((result, element) -> result, element_result);
        }
        
        //==============================================================================================================
        public @NotNull DataResult<Pair<C, T>> build()
        {
            if (this.elements.size() < minSize)
            {
                return createTooShortError(this.elements.size());
            }
            
            final T          errors = this.ops.createList(this.failed.build());
            final Pair<C, T> pair   = Pair.of(copyFunction.apply(this.elements), errors);
            
            if (this.totalCount > maxSize)
            {
                result = createTooLongError(totalCount);
            }
            
            return result.map(ignored -> pair).setPartial(pair);
        }
    }
    
    //******************************************************************************************************************
    private <R> @NotNull DataResult<R> createTooShortError(final int size)
    {
        return DataResult.error(() ->
            "List is too short: %s, expected range [%s-%s]".formatted(size, this.minSize, this.maxSize));
    }

    private <R> @NotNull DataResult<R> createTooLongError(final int size)
    {
        return DataResult.error(() ->
            "List is too long: %s, expected range [%s-%s]".formatted(size, this.minSize, this.maxSize));
    }

    @Override
    public <T> DataResult<T> encode(final C input, final DynamicOps<T> ops, final T prefix)
    {
        if (input.size() < minSize)
        {
            return createTooShortError(input.size());
        }
        
        if (input.size() > maxSize)
        {
            return createTooLongError(input.size());
        }
        
        final ListBuilder<T> builder = ops.listBuilder();
        
        for (final E element : input)
        {
            builder.add(elementCodec.encodeStart(ops, element));
        }
        
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<C, T>> decode(final DynamicOps<T> ops, final T input)
    {
        return ops
            .getList(input)
            .setLifecycle(Lifecycle.stable())
            .flatMap(stream ->
            {
                final DecoderState<T> decoder = new DecoderState<>(ops);
                stream.accept(decoder::accept);
                return decoder.build();
            });
    }

    @Override
    public String toString()
    {
        return "CollectionCodec[" + elementCodec + ']';
    }
}

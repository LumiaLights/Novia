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
package xyz.lumialights.novia.api.core.serialisation.dynops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

import java.io.*;



//**********************************************************************************************************************
public interface IDynamicOpsSerialiser<T>
{
    //******************************************************************************************************************
    @NotNull String getFileExtension();
    @NotNull DynamicOps<T> getOps();
    
    //==================================================================================================================
    boolean isReadableDataFormat();
    
    //==================================================================================================================
    @NotNull T deserialise(@NotNull InputStream stream) throws IOException, ConfigSerialisationException;
    void serialise(@NotNull T input, @NotNull OutputStream stream) throws IOException;
    
    @NotNull T deserialiseString(@NotNull String input) throws ConfigSerialisationException;
    @NotNull String serialiseString(@NotNull T input);
    
    //==================================================================================================================
    default <R> @NotNull DataResult<R> decode(@NotNull final InputStream stream, @NotNull final Codec<R> codec)
        throws IOException
    {
        try
        {
            final T input = this.deserialise(stream);
            return codec.parse(this.getOps(), input);
        }
        catch (final ConfigSerialisationException ex)
        {
            return DataResult.error(ex::getMessage);
        }
    }
    
    default <R> @NotNull DataResult<R> decodeString(@NotNull final String string, @NotNull final Codec<R> codec)
    {
        try
        {
            final T input = this.deserialiseString(string);
            return codec.parse(this.getOps(), input);
        }
        catch (final ConfigSerialisationException ex)
        {
            return DataResult.error(ex::getMessage);
        }
    }
    
    default <R> @NotNull DataResult<T> encode(@NotNull final OutputStream stream,
                                              @NotNull final R            input,
                                              @NotNull final Codec<R>     codec)
        throws IOException
    {
        final DataResult<T> result = codec.encodeStart(this.getOps(), input);
        
        if (result.isSuccess())
        {
            this.serialise(result.getOrThrow(), stream);
        }
        
        return result;
    }
    
    default <R> @NotNull DataResult<String> encodeString(@NotNull final R input, @NotNull final Codec<R> codec)
    {
        final DataResult<T> result = codec.encodeStart(this.getOps(), input);
        return result.map(this::serialiseString);
    }
}

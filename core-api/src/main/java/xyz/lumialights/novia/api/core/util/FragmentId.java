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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.NotNull;



//**********************************************************************************************************************
public record FragmentId(@NotNull Identifier id, @NotNull String fragment)
{
    //******************************************************************************************************************
    public static final Codec<FragmentId> CODEC = Codec.withAlternative(
        Codec.STRING
            .comapFlatMap(
                (pid ->
                {
                    try
                    {
                        return DataResult.success(FragmentId.of(pid));
                    }
                    catch (final InvalidIdentifierException ex)
                    {
                        return DataResult.error(ex::getMessage);
                    }
                }),
                FragmentId::toString),
        RecordCodecBuilder.create(instance -> instance
            .group(
                Identifier.CODEC
                    .fieldOf("id")
                    .forGetter(FragmentId::id),
                Codec.STRING
                    .fieldOf("fragment")
                    .forGetter(FragmentId::fragment))
            .apply(instance, FragmentId::new)));
    
    //******************************************************************************************************************
    public static @NotNull FragmentId of(@NotNull final String namespace,
                                         @NotNull final String path,
                                         @NotNull final String fragment)
    {
        return new FragmentId(Identifier.of(namespace, path), fragment);
    }
    
    public static @NotNull FragmentId of(@NotNull final String id, @NotNull final String fragment)
    {
        return new FragmentId(Identifier.of(id), fragment);
    }
    
    public static @NotNull FragmentId of(@NotNull final String id)
    {
        if (!id.contains("#"))
        {
            return new FragmentId(Identifier.of(id), "");
        }
        
        final int hash_index = id.indexOf('#');
        return of(id.substring(0, hash_index), id.substring(hash_index + 1));
    }

    //******************************************************************************************************************
    @Override
    public String toString()
    {
        return (this.id + "#" + this.fragment);
    }
}

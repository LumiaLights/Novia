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
package xyz.lumialights.novia.api.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.util.JsonPointer;



//**********************************************************************************************************************
public record PropertyId(@NotNull Identifier providerId, @NotNull JsonPointer pointer)
{
    //******************************************************************************************************************
    public static final Codec<PropertyId> CODEC = Codec.withAlternative(
        Codec.STRING
            .comapFlatMap(
                (pid ->
                {
                    try
                    {
                        return DataResult.success(PropertyId.of(pid));
                    }
                    catch (final InvalidIdentifierException ex)
                    {
                        return DataResult.error(ex::getMessage);
                    }
                }),
                PropertyId::toString),
        RecordCodecBuilder.create(instance -> instance
            .group(
                Identifier.CODEC
                    .fieldOf("provider")
                    .forGetter(PropertyId::providerId),
                JsonPointer.CODEC
                    .fieldOf("pointer")
                    .forGetter(PropertyId::pointer))
            .apply(instance, PropertyId::new)));
    
    //******************************************************************************************************************
    public static @NotNull PropertyId of(@NotNull final String      namespace,
                                         @NotNull final String      path,
                                         @NotNull final JsonPointer pointer)
    {
        return new PropertyId(Identifier.of(namespace, path), pointer);
    }
    
    public static @NotNull PropertyId of(@NotNull final String namespace,
                                         @NotNull final String path,
                                         
                                         @Pattern(JsonPointer.FULL_PATTERN)
                                                 @Subst("/pointer")
                                                 @NotNull final String pointerString)
    {
        final JsonPointer pointer = JsonPointer.compile(pointerString);
        
        if (pointer == null)
        {
            throw new InvalidIdentifierException("invalid JSON pointer '" + pointerString + "'");
        }
        
        return new PropertyId(Identifier.of(namespace, path), pointer);
    }
    
    public static @NotNull PropertyId of(@NotNull final String id,
                                         
                                         @Pattern(JsonPointer.FULL_PATTERN)
                                                 @Subst("/pointer")
                                                 @NotNull final String pointerString)
    {
        final JsonPointer pointer = JsonPointer.compile(pointerString);
        
        if (pointer == null)
        {
            throw new InvalidIdentifierException("invalid JSON pointer '" + pointerString + "'");
        }
        
        return new PropertyId(Identifier.of(id), pointer);
    }
    
    public static @NotNull PropertyId of(@NotNull final String identifier)
    {
        if (!identifier.contains("#"))
        {
            return new PropertyId(Identifier.of(identifier), JsonPointer.ROOT);
        }
        
        final int hash_index = identifier.indexOf('#');
        
        @Subst("/pointer")
        final String ptr_expression = identifier.substring(hash_index + 1);
        final String id_expression  = identifier.substring(0, hash_index);
        
        return of(id_expression, ptr_expression);
    }

    //******************************************************************************************************************
    @Override
    public String toString()
    {
        return (this.providerId() + "#" + this.pointer());
    }
}

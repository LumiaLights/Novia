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
package xyz.lumialights.novia.api.config.schema.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.Triple;

import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public record EnumSchema(@NotNull ImmutableList<Value> values, boolean ignoreCase)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final EnumSchema           DEFAULT = new EnumSchema(ImmutableList.of(), false);
    public static final MapCodec<EnumSchema> MAP_CODEC;
    
    //==================================================================================================================
    static
    {
        MAP_CODEC = RecordCodecBuilder
            .<Triple<Optional<Value.Type>, List<Value>, Boolean>>mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .forGetter(Triple::first),
                    Codec.list(Value.CODEC)
                        .fieldOf("enum")
                        .forGetter(Triple::second),
                    Codec.BOOL
                        .optionalFieldOf("ignoreCase", false)
                        .forGetter(Triple::third))
                .apply(instance, Triple::of))
            .validate(EnumSchema::validateEnumeration)
            .xmap(
                (t -> new EnumSchema(ImmutableList.copyOf(t.second()), t.third())),
                (schema -> Triple.of(Optional.empty(), schema.values, schema.ignoreCase)));
    }
    
    //******************************************************************************************************************
    private static @NotNull DataResult<Triple<Optional<Value.Type>, List<Value>, Boolean>> validateEnumeration(
        @NotNull Triple<Optional<Value.Type>, List<Value>, Boolean> input
    )
    {
        if (input.first().isPresent())
        {
            final Value.Type type = input.first().orElseThrow();
            
            if (input.second().stream().anyMatch(schema -> (schema.getType() != type)))
            {
                return DataResult.error(() ->
                    "Some enum constants did not satisfy typed requirement '%s'".formatted(type.asString()));
            }
        }
        
        return DataResult.success(input);
    }
    
    //******************************************************************************************************************
    public EnumSchema
    {
        Objects.requireNonNull(values, "values must not be null");
        
        for (int i = 0; i < values.size(); i++)
        {
            Objects.requireNonNull(values.get(i), "enum element " + (i + 1) + " must not be null");
        }
    }
    
    public EnumSchema(@NotNull final List<Value> values)
    {
        this(ImmutableList.copyOf(values), false);
    }
    
    public EnumSchema(@NotNull final List<Value> values, final boolean ignoreCase)
    {
        this(ImmutableList.copyOf(values), ignoreCase);
    }
    
    //==================================================================================================================
    @Override public @Nullable Value.Type getType()     { return null; }
    @Override public @NotNull  String     getTrigger()  { return "enum"; }
    
    @Override
    public @NotNull MapCodec<EnumSchema> createMapCodec(@NotNull final Codec<IPropertySchema> parent)
    { return MAP_CODEC; }
    
    //==================================================================================================================
    @Override
    public @NotNull DataResult<Value> validateValue(@NotNull final Value value)
    {
        final boolean is_invalid = this.values
            .stream()
            .noneMatch(c ->
            {
                if (c.getType() != value.getType())
                {
                    return false;
                }
                
                if (this.ignoreCase && c.isString())
                {
                    return c.getString().equalsIgnoreCase(value.getString());
                }
                
                return c.equals(value);
            });
        
        if (is_invalid)
        {
            return DataResult.error(() ->
                "Invalid enum value '%s', must be any of [%s] (%s)".formatted(
                value.asString(),
                this.values.stream().map(Value::asString).collect(Collectors.joining(", ")),
                (this.ignoreCase ? "case-insensitive" : "case-sensitive")));
        }
        
        return DataResult.success(value);
    }
}

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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.config.schema.SchemaCodecHelper;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
public record InterfaceSchema(@NotNull ImmutableMap<String, IPropertySchema> properties)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final InterfaceSchema DEFAULT = new InterfaceSchema(ImmutableMap.of());
    
    //******************************************************************************************************************
    public InterfaceSchema(@NotNull final Map<String, IPropertySchema> properties)
    {
        this(ImmutableMap.copyOf(properties));
    }
    
    //==================================================================================================================
    @Override public @NotNull Value.Type getType()    { return Value.Type.MAP; }
    @Override public @NotNull String     getTrigger() { return "properties"; }
    
    @Override
    public @NotNull MapCodec<InterfaceSchema> createMapCodec(@NotNull final Codec<IPropertySchema> parent)
    {
        return RecordCodecBuilder
            .mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .validate(SchemaCodecHelper.createTypedValidator(Value.Type.MAP, "properties"))
                        .forGetter(s -> Optional.empty()),
                    Codec
                        .unboundedMap(Codec.STRING, parent)
                        .fieldOf("properties")
                        .forGetter(InterfaceSchema::properties))
                .apply(instance, ((type, map) -> new InterfaceSchema(ImmutableMap.copyOf(map)))));
    }
    
    //==================================================================================================================
    @Override
    public @NotNull DataResult<Value> validateValue(@NotNull final Value value)
    {
        final Map<Value, Value> entries = value.getMap();
        
        for (final var entry : entries.entrySet())
        {
            final String          key        = entry.getKey().asString();
            final IPropertySchema sub_schema = this.properties.get(key);
            
            if (sub_schema == null)
            {
                return DataResult.error(() ->
                    "None of map schema property definitions ([%s]) matched property name '%s'".formatted(
                        String.join(", ", this.properties.keySet()),
                        key));
            }
            
            final DataResult<Value> result = sub_schema.validateValue(entry.getValue());
            
            if (result.isError())
            {
                return DataResult.error(() ->
                    "Map sub-schema for property definition '%s' did not match given value '%s'".formatted(
                        key,
                        entry.getValue().asString()));
            }
        }
        
        return DataResult.success(value);
    }
}

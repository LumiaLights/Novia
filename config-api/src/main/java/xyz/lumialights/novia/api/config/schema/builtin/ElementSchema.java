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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.config.schema.SchemaCodecHelper;
import xyz.lumialights.novia.api.config.schema.builtin.atomic.NullSchema;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
public record ElementSchema(@NotNull IPropertySchema elementSchema)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final ElementSchema DEFAULT = new ElementSchema(NullSchema.INSTANCE);
    
    //******************************************************************************************************************
    @Override public @NotNull Value.Type              getType()    { return Value.Type.LIST; }
    @Override public @NotNull String                  getTrigger() { return "items"; }
    
    @Override
    public @NotNull MapCodec<ElementSchema> createMapCodec(final @NotNull Codec<IPropertySchema> parent)
    {
        return RecordCodecBuilder
            .mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .validate(SchemaCodecHelper.createTypedValidator(Value.Type.LIST, "items"))
                        .forGetter(schema -> Optional.empty()),
                    parent
                        .fieldOf("items")
                        .forGetter(ElementSchema::elementSchema))
                .apply(instance, ((type, sub_schemas) -> new ElementSchema(sub_schemas))));
    }
    
    //==================================================================================================================
    @Override
    public @NotNull DataResult<Value> validateValue(final @NotNull Value value)
    {
        final List<Value> values = value.getList();
        
        for (int i = 0; i < values.size(); ++i)
        {
            final DataResult<Value> result = this.elementSchema.validate(values.get(i));
            
            if (result.isError())
            {
                final int pos = (i + 1);
                return DataResult.error(() -> String.format(
                    "List element %d does not match schema: %s",
                    pos, result.error().orElseThrow().message()));
            }
        }
        
        return DataResult.success(value);
    }
}

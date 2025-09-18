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
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.config.schema.SchemaCodecHelper;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.Pair;

import java.util.*;



//**********************************************************************************************************************
public record ConstantSchema(@NotNull Value constant)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final ConstantSchema           DEFAULT = new ConstantSchema(new Value());
    public static final MapCodec<ConstantSchema> MAP_CODEC;
    
    //==================================================================================================================
    static
    {
        MAP_CODEC = RecordCodecBuilder
            .<Pair<Optional<Value.Type>, Value>>mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .forGetter(Pair::first),
                    Value.CODEC
                        .fieldOf("const")
                        .forGetter(Pair::second))
                .apply(instance, Pair::of))
            .validate(SchemaCodecHelper::validateTypedRequirement)
            .xmap((p -> new ConstantSchema(p.second())), (schema -> Pair.of(Optional.empty(), schema.constant)));
    }
    
    //******************************************************************************************************************
    public ConstantSchema
    {
        Objects.requireNonNull(constant, "constant value cannot be null");
    }
    
    //==================================================================================================================
    @Override public @Nullable Value.Type getType()    { return null; }
    @Override public @NotNull  String     getTrigger() { return "const"; }
    
    @Override
    public @NotNull MapCodec<ConstantSchema> createMapCodec(@NotNull final Codec<IPropertySchema> parent)
    { return MAP_CODEC; }
    
    //==================================================================================================================
    @Override
    public @NotNull DataResult<Value> validateValue(@NotNull final Value value)
    {
        return (value.equals(this.constant)
            ? DataResult.success(value)
            : DataResult.error(() -> String.format("Value '%s' does not equal constant '%s'", value, constant)));
    }
}

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
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.serialisation.codec.NoviaCodecs;

import java.util.*;
import java.util.regex.Pattern;



//**********************************************************************************************************************
public record PatternSchema(@NotNull Pattern pattern)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final PatternSchema           DEFAULT = new PatternSchema(Pattern.compile(".*"));
    public static final MapCodec<PatternSchema> MAP_CODEC;
    
    //==================================================================================================================
    static
    {
        MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .validate(SchemaCodecHelper.createTypedValidator(Value.Type.STRING, "pattern"))
                        .forGetter(schema -> Optional.empty()),
                    NoviaCodecs.PATTERN
                        .fieldOf("pattern")
                        .forGetter(PatternSchema::pattern))
                .apply(instance, ((type, pattern) -> new PatternSchema(pattern))));
    }
    
    //******************************************************************************************************************
    public PatternSchema
    {
        Objects.requireNonNull(pattern, "pattern must not be null");
    }
    
    //==================================================================================================================
    @Override public @NotNull Value.Type getType()    { return Value.Type.STRING; }
    @Override public @NotNull String     getTrigger() { return "pattern"; }
    
    @Override
    public @NotNull MapCodec<PatternSchema> createMapCodec(@NotNull final Codec<IPropertySchema> parent)
    { return MAP_CODEC; }
    
    //==================================================================================================================
    @Override
    public @NotNull DataResult<Value> validateValue(@NotNull final Value value)
    {
        final String string = value.getString();
        
        if (!this.pattern.matcher(string).matches())
        {
            return DataResult.error(() -> String.format(
                "Invalid string '%s', did not match pattern '%s'",
                string, this.pattern.pattern()));
        }
        
        return DataResult.success(value);
    }
}

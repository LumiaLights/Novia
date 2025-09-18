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
package xyz.lumialights.novia.api.config.schema;

import com.mojang.serialization.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
/** Provides the interface for config property schemata. */
public interface IPropertySchema
{
    //******************************************************************************************************************
    /**
     * Gets the type of the value this schema represents.
     * <p>
     * If this returns null, the schema is a variant schema, which has no fixed type and,
     * which result depends on the dynamic properties of the schema.
     * @return The value type
     */
    @Nullable Value.Type getType();
    
    /**
     * Creates the map codec that this schema interface serialises as.
     * @return The map codec
     */
    @NotNull MapCodec<? extends IPropertySchema> createMapCodec(@NotNull Codec<IPropertySchema> parent);
    
    /**
     * Creates the codec defined by this interface's map codec. ({@link IPropertySchema#createMapCodec(Codec)})
     * @return The codec
     */
    default @NotNull Codec<? extends IPropertySchema> createCodec(@NotNull final Codec<IPropertySchema> parent)
    {
        return this.createMapCodec(parent).codec();
    }
    
    /**
     * Gets the trigger keyword (JSON property name) that the decoder uses to determine the schema to be parsed.
     * @return The trigger keyword
     */
    @NotNull String getTrigger();
    
    //==================================================================================================================
    @NotNull DataResult<Value> validateValue(@NotNull Value value);
    
    /**
     * Validates the given value and returns the operation's result.
     * @param value The value to validate
     * @return The result of the operation
     */
    default @NotNull DataResult<Value> validate(@NotNull final Value value)
    {
        Objects.requireNonNull(value, "value must not be null");
        
        if (getType() != null && !value.is(this.getType()))
        {
            return DataResult.error(() -> "Value '" + value.asString() + "' is not of type '" + this.getType() + "'");
        }
        
        return this.validateValue(value);
    }
}

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
package xyz.lumialights.novia.api.config.schema.factory;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.config.schema.builtin.VariantSchema;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;



//**********************************************************************************************************************
public class VariantBuilder
{
    //******************************************************************************************************************
    private final List<IPropertySchema> schemas = new ArrayList<>();
    
    //==================================================================================================================
    // STRING SCHEMAS
    public @NotNull VariantBuilder stringSchema() { return this.customSchema(SchemaFactory.stringSchema()); }

    public @NotNull VariantBuilder patternSchema(@Language("RegExp") @NotNull final String pattern)
    {
        return this.customSchema(SchemaFactory.patternSchema(pattern));
    }
    
    public @NotNull VariantBuilder patternSchema(@NotNull final Pattern pattern)
    {
        return this.customSchema(SchemaFactory.patternSchema(pattern));
    }
    
    //==================================================================================================================
    // NUMBER SCHEMAS
    public @NotNull VariantBuilder numberSchema() { return this.customSchema(SchemaFactory.numberSchema()); }
    
    public @NotNull VariantBuilder rangeSchema(final double start, final double end)
    {
        return this.customSchema(SchemaFactory.rangeSchema(start, end));
    }
    
    //==================================================================================================================
    // ATOMIC SCHEMAS
    public @NotNull VariantBuilder booleanSchema() { return this.customSchema(SchemaFactory.booleanSchema()); }
    
    public @NotNull VariantBuilder nullSchema() { return this.customSchema(SchemaFactory.nullSchema()); }
    
    //==================================================================================================================
    // LIST SCHEMAS
    public @NotNull VariantBuilder listSchema() { return this.customSchema(SchemaFactory.listSchema()); }
    
    public @NotNull VariantBuilder elementSchema(@NotNull final IPropertySchema subSchema)
    {
        return this.customSchema(SchemaFactory.elementSchema(subSchema));
    }
    
    public @NotNull VariantBuilder tupleSchema(@NotNull final IPropertySchema @NotNull ...schemas)
    {
        return this.customSchema(SchemaFactory.tupleSchema(schemas));
    }
    
    public @NotNull VariantBuilder tupleSchema(@NotNull final Collection<IPropertySchema> schemas)
    {
        return this.customSchema(SchemaFactory.tupleSchema(schemas));
    }
    
    //==================================================================================================================
    // MAP SCHEMAS
    public @NotNull VariantBuilder mapSchema()
    {
        return this.customSchema(SchemaFactory.mapSchema());
    }
    
    public @NotNull VariantBuilder interfaceSchema(@NotNull final Map<String, IPropertySchema> properties)
    {
        return this.customSchema(SchemaFactory.interfaceSchema(properties));
    }
    
    public @NotNull VariantBuilder patternMapSchema(@NotNull final Map<Pattern, IPropertySchema> patternProperties)
    {
        return this.customSchema(SchemaFactory.patternMapSchema(patternProperties));
    }
    
    //==================================================================================================================
    // VARIANT SCHEMAS
    public @NotNull VariantBuilder constantSchema(@NotNull final Value constant)
    {
        return this.customSchema(SchemaFactory.constantSchema(constant));
    }
    
    public @NotNull VariantBuilder enumSchema(@NotNull final Value @NotNull ...values)
    {
        return this.customSchema(SchemaFactory.enumSchema(values));
    }
    
    public @NotNull VariantBuilder enumSchema(@NotNull final Collection<Value> values)
    {
        return this.customSchema(SchemaFactory.enumSchema(values));
    }
    
    public @NotNull VariantBuilder enumSchema(@NotNull final Collection<Value> values, final boolean ignoreCase)
    {
        return this.customSchema(SchemaFactory.enumSchema(values, ignoreCase));
    }

    public <E extends Enum<E>> @NotNull VariantBuilder enumSchema(@NotNull final Class<E> enumClass)
    {
        return this.customSchema(SchemaFactory.enumSchema(enumClass));
    }
    
    public <E extends Enum<E>> @NotNull VariantBuilder enumSchema(@NotNull final Class<E> enumClass,
                                                                           final boolean  ignoreCase)
    {
        return this.customSchema(SchemaFactory.enumSchema(enumClass, ignoreCase));
    }
    
    public @NotNull VariantBuilder variantSchema(@NotNull final Function<VariantBuilder, VariantBuilder> builder)
    {
        return this.customSchema(SchemaFactory.variantSchema(builder));
    }
    
    //==================================================================================================================
    public @NotNull VariantBuilder customSchema(@NotNull final IPropertySchema schema)
    {
        this.schemas.add(schema);
        return this;
    }
    
    //==================================================================================================================
    @NotNull IPropertySchema build()
    {
        return new VariantSchema(this.schemas);
    }
}

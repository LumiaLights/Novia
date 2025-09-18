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
package xyz.lumialights.novia.api.config.spec.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.schema.document.SchemaDocument;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.config.spec.ConfigSpec;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.core.util.FragmentId;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.config.spec.PropertySpec;

import java.util.*;
import java.util.function.Function;



//**********************************************************************************************************************
public class PropertyBuilder<Container>
    extends SpecBuilderBase<Container>
{
    //******************************************************************************************************************
    private final Function<Container, Property> getter;
    private final SchemaDocument                schemaDoc;
    
    private IPropertySchema schema = null;
    
    //******************************************************************************************************************
    PropertyBuilder(@NotNull final Function<Container, Property> getter, @Nullable final SchemaDocument schema)
    {
        this.getter    = getter;
        this.schemaDoc = schema;
    }

    //==================================================================================================================
    public @NotNull PropertyBuilder<Container> withSchema(@Nullable final IPropertySchema schema)
    {
        Objects.requireNonNull(schema, "schema must not be null");
        this.schema = schema;
        
        return this;
    }
    
    public @NotNull PropertyBuilder<Container> withSchema(@NotNull final String tag)
    {
        Objects.requireNonNull(tag, "schema tag must not be null");
        
        if (this.schemaDoc == null)
        {
            Novia.LOGGER.error("Could not fetch schema tag '{}', no schema definition loaded", tag);
        }
        else
        {
            this.schemaDoc.findTagged(tag).ifPresentOrElse(
                (schema -> this.schema = schema),
                (() ->
                {
                    this.schema = null;
                    Novia.LOGGER.error("Could not fetch schema tag '{}', not defined", tag);
                }));
        }
        
        return this;
    }
    
    public @NotNull PropertyBuilder<Container> withSchema(@NotNull final FragmentId schemaId)
    {
        Objects.requireNonNull(schemaId, "schema id must not be null");
        
        if (this.schemaDoc == null)
        {
            Novia.LOGGER.error("Could not fetch schema id '{}', no schema definition loaded", schemaId);
        }
        else
        {
            final IPropertySchema schema = ConfigSpec.SCHEMA_STORE.resolve(schemaId);

            if (schema != null)
            {
                return this.withSchema(schema);
            }
            
            Novia.LOGGER.error("Could not fetch schema id and tag '{}', not defined", schemaId);
        }
        
        return this;
    }

    //==================================================================================================================
    @Override
    protected @NotNull PropertySpec<Container> build(@NotNull final JsonPointer pointer)
    {
        IPropertySchema schema = this.schema;
        
        if (schema == null && this.schemaDoc != null)
        {
            schema = this.schemaDoc.findSchema(pointer).orElse(null);
        }
        
        return new PropertySpec<>(pointer, schema, this.getter);
    }
}

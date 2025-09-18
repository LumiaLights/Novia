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
package xyz.lumialights.novia.api.config.spec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.PropertyValidationException;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.property.PropertyAttorney;
import xyz.lumialights.novia.api.config.property.impl.IPropertyConfigHandler;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.function.Function;



//**********************************************************************************************************************
/**
 * Describes a property in a configuration.
 * @param pointer The absolute path to this property (JSON Pointer).
 * @param schema  The optional schema of the property.
 * @param getter  The method reference that fetches a property from a config container.
 * @param <Container> The container type
 */
public record PropertySpec<Container>(
    @NotNull  JsonPointer                   pointer,
    @Nullable IPropertySchema               schema,
    @NotNull  Function<Container, Property> getter)
    implements IConfigSpec<Container>
{
    //******************************************************************************************************************
    public @NotNull Property get(@NotNull final Container container)
    {
        return getter.apply(container);
    }
    
    //==================================================================================================================
    @Override public @NotNull JsonPointer getPointer() { return this.pointer; }
    
    //==================================================================================================================
    /**
     * Binds the property managed by this spec to the given container.
     * @param container The container to bind this property spec to
     * @param handler   The handler to bind to the container property
     */
    public void bind(@NotNull final Container container, @Nullable final IPropertyConfigHandler handler)
    {
        PropertyAttorney.bind(this.getter.apply(container), this, handler);
    }

    //==================================================================================================================
    public void validate(@NotNull final Value newValue)
        throws PropertyValidationException
    {
        if (this.schema != null)
        {
            final DataResult<Value> result = this.schema.validate(newValue);
            
            if (!result.isSuccess())
            {
                throw new PropertyValidationException(
                    "validation for property '%s' failed: %s"
                        .formatted(this.pointer, result.error().orElseThrow().message()));
            }
        }
    }
    
    public void validate(@NotNull final Container container)
        throws PropertyValidationException
    {
        final Value value = get(container).getValue();
        this.validate(value);
    }

    //==================================================================================================================
    @Override
    public <T> DataResult<T> decode(@NotNull final Container     context,
                                    @NotNull final DynamicOps<T> ops,
                                    @NotNull final T             input)
    {
        final Value value = Value.CODEC.decode(ops, input).getOrThrow().getFirst();

        try
        {
            get(context).setValue(value);
        }
        catch (final PropertyValidationException ex)
        {
            return DataResult.error(ex::getMessage, input);
        }

        return DataResult.success(input);
    }

    @Override
    public <T> DataResult<T> encode(@NotNull final Container     context,
                                    @NotNull final DynamicOps<T> ops,
                                    @NotNull final T             prefix)
    {
        final Property property = get(context);
        return Value.CODEC.encode(property.getValue(), ops, prefix);
    }
}

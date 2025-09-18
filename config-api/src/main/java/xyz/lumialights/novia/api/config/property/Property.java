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
package xyz.lumialights.novia.api.config.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.IPropertyChangeHandler;
import xyz.lumialights.novia.api.config.property.impl.IPropertyConfigHandler;
import xyz.lumialights.novia.api.config.spec.PropertySpec;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.config.PropertyValidationException;

import xyz.lumialights.novia.api.config.LockedPropertyChangeException;

import java.util.*;



//**********************************************************************************************************************
/**
 * Represents a configuration property used in configuration containers.<p>
 * Every property contains a value and reference to its specification, the specification houses all the attributes
 * of a property that do not directly belong to the property itself. (such as its validator)
 */
public class Property
{
    //******************************************************************************************************************
    /**
     * Creates a new {@link Map} property.
     * @param value The map to initialise this property with
     * @return The new {@link Property}
     */
    public static @NotNull Property create(@NotNull final Map<Value, Value> value)
    {
        return new Property(new Value(value));
    }

    /**
     * Creates a new {@link List} property.
     * @param value The list to initialise this property with
     * @return The new {@link Property}
     */
    public static @NotNull Property create(@NotNull final List<Value> value)
    {
        return new Property(new Value(value));
    }

    /**
     * Creates a new {@link String} property.
     * @param value The string to initialise this property with
     * @return The new {@link Property}
     */
    public static @NotNull Property create(@NotNull final String value)
    {
        return new Property(new Value(value));
    }

    /**
     * Creates a new {@link List} property.
     * @param value The list to initialise this property with
     * @return The new {@link Property}
     */
    public static @NotNull Property create(@NotNull final Number value)
    {
        return new Property(new Value(value));
    }

    /**
     * Creates a new {@link Boolean} property.
     * @param value The boolean value to initialise this property with
     * @return The new {@link Property}
     */
    public static @NotNull Property create(@NotNull final Boolean value)
    {
        return new Property(new Value(value));
    }

    /**
     * Creates a new {@link String} property with the name of the enum constant. (lowercased)
     * @param value The enum constant to initialise this property with
     * @return The new {@link Property}
     */
    public static <E extends Enum<E>> @NotNull Property create(@NotNull final E value)
    {
        return new Property(new Value(value.name().toLowerCase()));
    }

    /**
     * Creates a new empty property.
     * @return The new {@link Property}
     */
    public static @NotNull Property create()
    {
        return new Property(new Value());
    }

    //******************************************************************************************************************
    private final Value                       defaultValue;
    private final Set<IPropertyChangeHandler> handlers = new HashSet<>();
    
    private PropertySpec<?>        spec     = null;
    private IPropertyConfigHandler handler  = null;
    private Value                  override = null;
    private Value                  value;
    
    //******************************************************************************************************************
    private Property(@NotNull final Value value)
    {
        this.defaultValue = value;
        this.value        = value;
    }

    //==================================================================================================================
    void bind(@NotNull final PropertySpec<?> spec, @Nullable final IPropertyConfigHandler handler)
    {
        this.spec    = spec;
        this.handler = handler;
    }

    //==================================================================================================================
    /**
     * Gets the value that this property is currently managing.
     * @return The managed property value
     */
    public @NotNull Value getValue()
    {
        return (this.override != null ? this.override : this.value);
    }
    
    /**
     * Gets the enum constant associated with the given enum value stored as string.
     * <p>
     * If this is an enum validated property, which accepts {@code null} values,
     * {@link Property#getValue()} should be checked for {@code null} first.
     * @return The enum constant
     * @param <E> The enum type
     * @throws IllegalStateException If the underlying value is not a string and not a valid enum constant name
     */
    public <E extends Enum<E>> @NotNull E getEnumValue(@NotNull final Class<E> enumClass)
    {
        if (!this.getValue().isString())
        {
            throw new IllegalStateException("Underlying value is not a string");
        }
        
        final String      name     = this.getValue().getString();
        final Optional<E> constant = Arrays
            .stream(enumClass.getEnumConstants())
            .filter(c -> c.name().equalsIgnoreCase(name))
            .findFirst();
        
        if (constant.isEmpty())
        {
            throw new IllegalStateException(String.format("Given value '%s' is not a valid enum constant name", name));
        }
        
        return constant.get();
    }
    
    /**
     * Gets the property's attached {@link PropertySpec}.
     * @return The property specification, or null if the property has not been bound to a specification
     */
    public @Nullable PropertySpec<?> getSpec() { return this.spec; }

    /**
     * Gets the specified default value for this property.
     * @return The value
     */
    public @NotNull Value getDefaultValue() { return this.defaultValue; }
    
    //==================================================================================================================
    /**
     * Sets the value of this property without validating the new value.<p>
     * This method should only ever be used when there is some manual checking before the value is being set,
     * otherwise {@link Property#setValue(Value)} should almost always be preferred over this one.<p>
     * If the new value is different to current, this will signal the attached handler to send updates.<p>
     * If this property is part of a non-standalone config container, this method should never be called on a client
     * container, instead, the mutual server container should handle the update.
     * It should also never be called on the render or any other thread that is not the server thread.<p>
     * For standalone config containers, it should be called on the thread it was designated for.
     * @param newValue The new newValue of this property
     * @throws LockedPropertyChangeException If the property is part of a client container of a mutual configuration
     */
    public void setValueUnchecked(@NotNull final Value newValue)
    {
        if (newValue.equals(this.value))
        {
            return;
        }
        
        this.setValueImpl(newValue);
    }

    /**
     * Sets the value of this property.<p>
     * If the new value is different to current, this will signal the attached handler to send updates.<p>
     * If this property is part of a non-standalone config container, this method should never be called on a client
     * container, instead, the mutual server container should handle the update.
     * It should also never be called on the render or any other thread that is not the server thread.<p>
     * For standalone config containers, it should be called on the thread it was designated for.
     * @param newValue The new newValue of this property
     * @throws PropertyValidationException If the associated validator failed validation of the new value
     * @throws LockedPropertyChangeException If the property is part of a client container of a mutual configuration
     */
    public void setValue(@NotNull final Value newValue)
        throws PropertyValidationException
    {
        if (newValue.equals(this.value))
        {
            return;
        }

        if (this.spec != null)
        {
            this.spec.validate(newValue);
        }

        this.setValueImpl(newValue);
    }

    /**
     * Resets the value of this property to its default.<p>
     * If the default is different to the current, this will signal the attached handler to send updates.<p>
     * If this property is part of a non-standalone config container, this method should never be called on a client
     * container, instead, the mutual server container should handle the update.
     * It should also never be called on the render or any other thread that is not the server thread.<p>
     * For standalone config containers, it should be called on the thread it was designated for.
     * @throws LockedPropertyChangeException If the property is part of a client container of a mutual configuration
     */
    public void reset()
    {
        this.setValueUnchecked(this.defaultValue);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    void setOverride(@Nullable final Value override)
    {
        final Value old_value = this.getValue();
        this.override = override;
        
        if (!old_value.equals(this.getValue()))
        {
            this.handlers.forEach(handler -> handler.propertyChanged(this, old_value));
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setValueImpl(@NotNull final Value value)
    {
        final Value old_value = this.value;
        this.value = value;
        
        this.handler.notify(this, this.value);
        
        if (this.override == null)
        {
            this.handlers.forEach(handler -> handler.propertyChanged(this, old_value));
        }
    }
    
    //==================================================================================================================
    public void registerChangeHandler(@NotNull final IPropertyChangeHandler handler)
    {
        this.handlers.add(handler);
    }
}

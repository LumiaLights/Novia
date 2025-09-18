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
package xyz.lumialights.novia.api.gui.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.util.RefUtils;
import xyz.lumialights.novia.api.gui.component.GuiComponent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;



//**********************************************************************************************************************
/**
 * A utility class that allows {@link GuiComponent} classes define its own easily accessible properties.
 * <p>
 * Properties can be used for any sort of state in a component; however, it is best to only use them for things that
 * change a component's behaviour or look, so no data like text in a text box but more like item orientation in a
 * list-box.
 * <p>
 * When declaring property fields, it is always best to use the most recent subclass as field type; e.g. for non-null
 * properties, use {@link GuiProperty.NonNull}.
 *
 * @param <T> The type of content the property holds
 */
public sealed class GuiProperty<T>
    implements IGuiProperty<T>
    permits
        GuiProperty.NonNull
{
    //******************************************************************************************************************
    /**
     * A {@link GuiProperty} variant that does not allow {@code null} values, meaning it must be set.
     * @param <T> The type of content the property holds
     * @see GuiProperty
     */
    public static final class NonNull<T>
        extends GuiProperty<T>
    {
        //**************************************************************************************************************
        /**
         * Constructs a new gui property.
         * @param initValue The initial value of the property
         * @param setter    A setter that is executed whenever the internal value is changed
         * @param validator A validator that will throw an exception if the value is invalid
         */
        public NonNull(final @NotNull T                     initValue,
                       final @NotNull Consumer <@NotNull T> setter,
                       final @NotNull Predicate<@NotNull T> validator)
        {
            super(Objects.requireNonNull(initValue, "gui property value must not be null"), setter, validator);
        }

        //==============================================================================================================
        @Override public @NotNull T get() { return this.value; }

        //==============================================================================================================
        /**
         * Returns always {@code true} for {@link GuiProperty.NonNull} (see {@link IGuiProperty#isSet()}).
         * @return <code>true</code>
         */
        @Override public boolean isSet() { return true; }

        //==============================================================================================================
        /**
         * Sets the value currently held in the property to a new, non-null, value.
         * <p>
         * If unsure and a validator is attached, it is best to test before setting a value with {@link #isValid(Object)};
         * otherwise if the value is not accepted by this property, this will throw a {@link IllegalArgumentException}
         * exception.
         *
         * @param value The new non-null value
         * @return {@code true} if the value was successfully changed; this is true if it was different from the previous
         *
         * @throws IllegalArgumentException If the value was not correct according to the attached validator
         * @throws NullPointerException     If the new value was {@code null}
         */
        @Override
        public boolean set(final @NotNull T value)
        {
            return super.set(Objects.requireNonNull(value, "gui property value must not be null"));
        }

        //==============================================================================================================
        @Override
        public @NotNull GuiProperty<T> ifSet(final @NotNull Consumer<T> consumer)
        {
            consumer.accept(this.value);
            return this;
        }

        public @NotNull GuiProperty<T> ifEmpty(final @NotNull Runnable consumer) { return this; }
    }

    /**
     * A {@link GuiProperty} variant that references a property from another component.
     * <p>
     * This can be used for cases where a component consists of one or more child components and the components shall
     * not be exposed but its properties do need to be exposed; this allows those properties to be
     * aliased in the parent.
     *
     * @param <T> The type of content the property holds
     * @see GuiProperty
     */
    @SuppressWarnings("ClassCanBeRecord")
    public static final class Reference<T>
        implements IGuiProperty<T>
    {
        //**************************************************************************************************************
        final GuiComponent   owner;
        final GuiProperty<T> reference;

        //**************************************************************************************************************
        public Reference(final @NotNull GuiComponent owner, final @NotNull GuiProperty<T> reference)
        {
            this.owner     = Objects.requireNonNull(owner,     "owner must not be null");
            this.reference = Objects.requireNonNull(reference, "gui property value must not be null");
        }

        //==============================================================================================================
        @Override public T get() { return this.reference.get(); }

        public @NotNull GuiComponent getOwner() { return this.owner; }

        //==============================================================================================================
        @Override public boolean isSet() { return this.reference.isSet(); }

        //==============================================================================================================
        @Override public boolean set(final T value) { return this.reference.set(value); }
    }

    //******************************************************************************************************************
    /**
     * Creates a new nullable gui property.
     * @param initValue The initial value of the property
     * @param setter    A setter that is executed whenever the internal value is changed
     * @return The new {@link GuiProperty}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull GuiProperty<T> nullable(final @Nullable T           initValue,
                                                       final @NotNull  Consumer<T> setter)
    {
        return new GuiProperty<>(initValue, setter, RefUtils.alwaysTrue());
    }

    /**
     * Creates a new nullable gui property.
     * @param initValue The initial value of the property
     * @return The new {@link GuiProperty}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull GuiProperty<T> nullable(final @Nullable T initValue)
    {
        return new GuiProperty<>(initValue, RefUtils.emptyConsumer(), RefUtils.alwaysTrue());
    }

    /**
     * Creates a new nullable gui property that validates its internal value.
     * @param initValue The initial value of the property
     * @param setter    A setter that is executed whenever the internal value is changed
     * @param validator A validator that will throw an exception if the value is invalid
     * @return The new {@link GuiProperty}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull GuiProperty<T> nullableChecked(final @Nullable T            initValue,
                                                              final @NotNull  Consumer<T>  setter,
                                                              final @NotNull  Predicate<T> validator)
    {
        return new GuiProperty<>(initValue, setter, validator);
    }

    /**
     * Creates a new nullable gui property that validates its internal value.
     * @param initValue The initial value of the property
     * @param validator A validator that will throw an exception if the value is invalid
     * @return The new {@link GuiProperty}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull GuiProperty<T> nullableChecked(final @Nullable T            initValue,
                                                              final @NotNull  Predicate<T> validator)
    {
        return new GuiProperty<>(initValue, RefUtils.emptyConsumer(), validator);
    }

    /**
     * Creates a new non-null gui property that throws an exception when it encounters {@code null}.
     * @param initValue The initial value of the property
     * @param setter    A setter that is executed whenever the internal value is changed
     * @return The new {@link GuiProperty.NonNull}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull NonNull<T> nonNull(final @NotNull T                    initValue,
                                                  final @NotNull Consumer<@NotNull T> setter)
    {
        return new NonNull<>(initValue, setter, RefUtils.alwaysTrue());
    }

    /**
     * Creates a new non-null gui property that throws an exception when it encounters {@code null}.
     * @param initValue The initial value of the property
     * @return The new {@link GuiProperty.NonNull}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull NonNull<T> nonNull(final @NotNull T initValue)
    {
        return new NonNull<>(initValue, RefUtils.emptyConsumer(), RefUtils.alwaysTrue());
    }

    /**
     * Creates a new non-null gui property that throws an exception when it encounters {@code null} and which further
     * evaluates its value based on the given {@code validator}.
     * @param initValue The initial value of the property
     * @param setter    A setter that is executed whenever the internal value is changed
     * @param validator A validator that will throw an exception if the value is invalid
     * @return The new {@link GuiProperty.NonNull}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull NonNull<T> nonNullChecked(final @NotNull T                     initValue,
                                                         final @NotNull Consumer<@NotNull T>  setter,
                                                         final @NotNull Predicate<@NotNull T> validator)
    {
        return new NonNull<>(initValue, setter, validator);
    }

    /**
     * Creates a new non-null gui property that throws an exception when it encounters {@code null} and which further
     * evaluates its value based on the given {@code validator}.
     * @param initValue The initial value of the property
     * @param validator A validator that will throw an exception if the value is invalid
     * @return The new {@link GuiProperty.NonNull}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull NonNull<T> nonNullChecked(final @NotNull T                     initValue,
                                                         final @NotNull Predicate<@NotNull T> validator)
    {
        return new NonNull<>(initValue, RefUtils.emptyConsumer(), validator);
    }

    /**
     * Creates a reference to another property.
     * @param owner    The owning {@link GuiComponent}
     * @param property The {@link GuiProperty} that should be referenced
     * @return The new {@link GuiProperty.Reference}
     * @param <T> The type of content the property holds
     */
    public static <T> @NotNull Reference<T> referTo(final @NotNull GuiComponent   owner,
                                                    final @NotNull GuiProperty<T> property)
    {
        return new Reference<>(owner, property);
    }

    //******************************************************************************************************************
    final Consumer<T>  setter;
    final Predicate<T> validator;

    T value;

    //******************************************************************************************************************
    /**
     * Constructs a new gui property.
     * @param initValue The initial value of the property
     * @param setter    A setter that is executed whenever the internal value is changed
     * @param validator A validator that will throw an exception if the value is invalid
     */
    public GuiProperty(final T initValue, final @NotNull Consumer<T> setter, final @NotNull Predicate<T> validator)
    {
        if (!validator.test(initValue))
        {
            throw new IllegalArgumentException(
                "initial value '%s' is invalid according to the given validator".formatted(initValue));
        }

        this.value     = initValue;
        this.setter    = setter;
        this.validator = validator;
    }

    //==================================================================================================================
    @Override public T get() { return this.value; }

    //==================================================================================================================
    @Override public boolean isSet() { return (this.value != null); }

    /**
     * Can be used to check if a value is valid before giving it to the property.
     * @param value The value to test
     * @return {@code true} if the value is valid, {@code false} otherwise
     */
    public boolean isValid(final T value) { return this.validator.test(value); }

    //==================================================================================================================
    /**
     * Sets the value currently held in the property to a new value.
     * <p>
     * If unsure and a validator is attached, it is best to test before setting a value with {@link #isValid(Object)};
     * otherwise if the value is not accepted by this property, this will throw a {@link IllegalArgumentException}
     * exception.
     *
     * @param value The new value
     * @return {@code true} if the value was successfully changed; this is true if it was different from the previous
     *
     * @throws IllegalArgumentException If the value was not correct according to the attached validator
     */
    @Override
    public boolean set(final T value)
    {
        if (!Objects.equals(this.value, value))
        {
            if (!this.validator.test(value))
            {
                throw new IllegalArgumentException("value '%s' is invalid for this property".formatted(value));
            }

            this.value = value;
            this.setter.accept(this.value);

            return true;
        }

        return false;
    }

    //==================================================================================================================
    /**
     * Executes a {@link Consumer} with the currently held value if {@link #isSet()} returns {@code true}
     * (if the content is non-null).
     * @param consumer The consumer to execute
     * @return {@code this}
     */
    public @NotNull GuiProperty<T> ifSet(final @NotNull Consumer<T> consumer)
    {
        if (this.value != null)
        {
            consumer.accept(this.value);
        }

        return this;
    }

    /**
     * Executes a {@link Runnable} if {@link #isSet()} returns {@code false} (if the content is {@code null}).
     * @param runnable The runnable to execute
     * @return {@code this}
     */
    public @NotNull GuiProperty<T> ifEmpty(final @NotNull Runnable runnable)
    {
        if (this.value == null)
        {
            runnable.run();
        }

        return this;
    }
}

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
package xyz.lumialights.novia.api.core.serialisation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import xyz.lumialights.novia.api.core.serialisation.IVariant.MapVariant;
import xyz.lumialights.novia.api.core.serialisation.IVariant.ListVariant;
import xyz.lumialights.novia.api.core.serialisation.IVariant.StringVariant;
import xyz.lumialights.novia.api.core.serialisation.IVariant.NumberVariant;
import xyz.lumialights.novia.api.core.serialisation.IVariant.BooleanVariant;
import xyz.lumialights.novia.api.core.serialisation.IVariant.VoidVariant;



//**********************************************************************************************************************
/**
 * Represents an immutable limited variant type that can take a set of specified types only.
 * <p>
 * Supported types are lists, maps, numbers, booleans and strings. {@code null} is also supported as a distinct
 * type from others, where a Value object that contains {@code null} is considered an “empty” Value object.
 * <p>
 * Throughout this library there will be mentions of "this is a &lt;type&gt; qualified Value", which just means that
 * the API is expecting value objects of only a certain subset of types and might or might not throw an exception if
 * different type values are given. If no qualification is specified, it is qualified for any of the supported
 * value types.
 */
public class Value
{
    //******************************************************************************************************************
    /** Type of object contained in a {@link Value} instance. */
    public enum Type
        implements StringIdentifiable
    {
        //**************************************************************************************************************
        /** Represents an {@link ImmutableMap} where both “key” and “value” are {@link Value} objects. */
        MAP(new Value(new MapVariant(ImmutableMap.of()))),
        
        /** Represents an {@link ImmutableList} of {@link Value} objects. */
        LIST(new Value(new ListVariant(ImmutableList.of()))),
        
        /** Represents a {@link Number} object. */
        NUMBER(new Value(new NumberVariant(0))),
        
        /** Represents a {@link Boolean} object. */
        BOOLEAN(new Value(new BooleanVariant(false))),
        
        /** Represents a {@link String} object. */
        STRING(new Value(new StringVariant(""))),
        
        /** Represents an empty {@link Value} instance. */
        VOID(new Value(VoidVariant.INSTANCE));
        
        //**************************************************************************************************************
        public static final PacketCodec<ByteBuf, Type> PACKET_CODEC = PacketCodecs
            .indexed((i -> Type.values()[i]), Enum::ordinal);
        public static final Codec<Type>                CODEC        = StringIdentifiable.createCodec(Type::values);
        
        //**************************************************************************************************************
        private final Value defaultValue;
        
        //**************************************************************************************************************
        Type(@NotNull final Value defaultValue)
        {
            this.defaultValue = defaultValue;
        }
        
        //==============================================================================================================
        private @NotNull Value getDefault()
        {
            return this.defaultValue;
        }
        
        //==============================================================================================================
        @Override
        public String asString()
        {
            return (this == VOID ? "null" : name().toLowerCase());
        }
    }

    //******************************************************************************************************************
    /** Describes the packet codec of a {@link Value} object. */
    public static final PacketCodec<ByteBuf, Value> PACKET_CODEC;

    /** Describes the serialisation codec of a {@link Value} object. */
    public static final Codec<Value> CODEC;
    
    /** A constant for {@link Value} objects representing a {@code true} boolean object. */
    public static final Value TRUE = new Value(true);
    
    /** A constant for {@link Value} objects representing a {@code false} boolean object. */
    public static final Value FALSE = new Value(false);
    
    /** A constant for {@link Value} objects representing a {@code null} object. */
    public static final Value EMPTY = new Value();
    
    //==================================================================================================================
    static
    {
        PACKET_CODEC = PacketCodec.recursive(ignored -> Type.PACKET_CODEC
            .dispatch(
                IVariant::getType,
                (type -> switch (type)
                {
                    case VOID    -> VoidVariant   .PACKET_CODEC;
                    case MAP     -> MapVariant    .PACKET_CODEC;
                    case LIST    -> ListVariant   .PACKET_CODEC;
                    case STRING  -> StringVariant .PACKET_CODEC;
                    case NUMBER  -> NumberVariant .PACKET_CODEC;
                    case BOOLEAN -> BooleanVariant.PACKET_CODEC;
                }))
            .xmap(Value::new, (var -> var.variant)));

        CODEC = Codec.recursive("NoviaValue", ignored -> new Codec<>()
        {
            //**********************************************************************************************************
            @SuppressWarnings("unchecked")
            private static <T> T getVar(@NotNull final Value value) { return (T) value.variant; }

            //**********************************************************************************************************
            @Override
            public <T> DataResult<Pair<Value, T>> decode(final DynamicOps<T> ops, final T input)
            {
                final DataResult<Pair<MapVariant, T>> m_res = MapVariant.CODEC.decode(ops, input);
                if (m_res.isSuccess()) return m_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<ListVariant, T>> l_res = ListVariant.CODEC.decode(ops, input);
                if (l_res.isSuccess()) return l_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<NumberVariant, T>> n_res = NumberVariant.CODEC.decode(ops, input);
                if (n_res.isSuccess()) return n_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<BooleanVariant, T>> b_res = BooleanVariant.CODEC.decode(ops, input);
                if (b_res.isSuccess()) return b_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<StringVariant, T>> s_res = StringVariant.CODEC.decode(ops, input);
                if (s_res.isSuccess()) return s_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                return DataResult.success(Pair.of(new Value(), input));
            }
            
            @Override
            public <T> DataResult<T> encode(final Value input, final DynamicOps<T> ops, final T prefix)
            {
                return switch (input.getType())
                {
                    case MAP     -> MapVariant    .CODEC.encode(getVar(input), ops, prefix);
                    case LIST    -> ListVariant   .CODEC.encode(getVar(input), ops, prefix);
                    case STRING  -> StringVariant .CODEC.encode(getVar(input), ops, prefix);
                    case NUMBER  -> NumberVariant .CODEC.encode(getVar(input), ops, prefix);
                    case BOOLEAN -> BooleanVariant.CODEC.encode(getVar(input), ops, prefix);
                    case VOID    -> VoidVariant   .CODEC.encode(getVar(input), ops, prefix);
                };
            }
        });
    }

    //******************************************************************************************************************
    /**
     * Tries to construct a {@link Value} from a given object if possible.
     *
     * @param object The object to construct the {@link Value} object from
     * @return The new {@link Value} object, or {@code null} on failure
     */
    public static @Nullable Value fromObject(@Nullable final Object object)
    {
        if (object instanceof Value value)
        {
            return value;
        }

        try
        {
            return switch (object)
            {
                case String  value -> new Value(value);
                case Boolean value -> new Value(value);
                case Number  value ->
                {
                    try
                    {
                        yield new Value(value);
                    }
                    catch (UnsupportedOperationException ignored)
                    {
                        yield null;
                    }
                }
                case null          -> Value.EMPTY;
                case List<?> value -> new Value(value
                    .stream()
                    .map(Value::tryFromObject).toList());

                case Map<?, ?> value -> new Value(value
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        e -> tryFromObject(e.getKey()),
                        e -> tryFromObject(e.getValue()))));

                default -> null;
            };
        }
        catch (final ValueConversionException ignored) {}

        return null;
    }

    /**
     * Tries to construct a {@link Value} from a given object if possible.
     *
     * @param object The object to construct the {@link Value} object from
     * @return The new {@link Value} object
     * @throws ValueConversionException If the given object could not be converted to {@link Value}
     */
    public static @NotNull Value tryFromObject(@Nullable final Object object)
    {
        final Value result = Value.fromObject(object);

        if (result == null)
        {
            assert object != null;
            throw new ValueConversionException(String.format(
                "invalid object of type '%s', is not a valid value type",
                object.getClass().getName()));
        }

        return result;
    }

    /**
     * Tries to parse a {@link Value} object from a given string.
     * For the parsing, {@link JsonOps} will be used to check if the value is a valid JSON string and return a
     * successful result, otherwise the input will be treated as a string value.
     * <p>
     * Any output from {@link Value#asString()} can be parsed back to the original value with this factory.
     *
     * @param string The string to parse from
     * @return The new {@link Value} object
     */
    public static @NotNull Value parse(@NotNull final String string)
    {
        try
        {
            return tryParse(string);
        }
        catch (final ValueConversionException ignored) {}

        return new Value(string);
    }

    /**
     * Tries to parse a {@link Value} object from a given string.
     * For the parsing, {@link JsonOps} will be used to check if the value is a valid JSON string and return a
     * successful result, otherwise a {@link ValueConversionException} is thrown.
     * <p>
     * Any output from {@link Value#asString()} can be parsed back to the original value with this factory.
     *
     * @param string The string to parse from
     * @return The new {@link Value} object
     * @throws ValueConversionException If the input string could not be parsed
     */
    public static @NotNull Value tryParse(@NotNull final String string)
    {
        final JsonElement       json   = JsonParser.parseString(string);
        final DataResult<Value> result = Value.CODEC.parse(JsonOps.INSTANCE, json);

        if (result.isSuccess())
        {
            return result.result().orElseThrow();
        }

        throw new ValueConversionException(String.format("could not parse value '%s'", string));
    }

    //******************************************************************************************************************
    private final IVariant<?> variant;

    //******************************************************************************************************************
    /**
     * Constructs a new empty Value.
     */
    public Value() { this(VoidVariant.INSTANCE); }

    /**
     * Constructs a Value with a specified default depending on the given type.
     * <ul>
     *     <li>{@link Type#MAP} An empty map</li>
     *     <li>{@link Type#LIST} An empty list</li>
     *     <li>{@link Type#STRING} An empty string ({@code ""})</li>
     *     <li>{@link Type#NUMBER} {@code 0}</li>
     *     <li>{@link Type#BOOLEAN} {@code false}</li>
     *     <li>{@link Type#VOID} {@code null}</li>
     * </ul>
     *
     * @param type The {@link Type}
     */
    public Value(@NotNull final Type type) { this(type.getDefault().variant); }

    /**
     * Constructs a new map value.
     * @param value The map object
     */
    public Value(@NotNull final Map<Value, Value> value) { this(new MapVariant(ImmutableMap.copyOf(value))); }

    /**
     * Constructs a new list variant.
     * @param value The list object
     */
    public Value(@NotNull final Collection<Value> value) { this(new ListVariant(ImmutableList.copyOf(value))); }
    
    /**
     * Constructs a new string variant.
     * @param value The string
     */
    public Value(@NotNull final String value) { this(new StringVariant(value)); }

    /**
     * Constructs a new boolean variant.
     * @param value The boolean value
     */
    public Value(final boolean value) { this(new BooleanVariant(value)); }

    /**
     * Constructs a new number variant.
     * <p>
     * This constructor takes a {@link Number} object and casts it to any one of the primitive types.
     * For the BigX, AtomicX, XAccumulator and XAdder variants, it will gather the appropriate scalar variant type.
     * For any remaining non hard-coded types, it will just gather the double value.
     * <p>
     * Do note that the BigX variants may lose precision as they are converted to their scalar variants.
     *
     * @param value The number value
     * @throws UnsupportedOperationException If the number object is not supported
     */
    public Value(@NotNull final Number value) { this(new NumberVariant(value)); }
    
    /**
     * Copies the internal reference to the given {@link Value} object.
     * <p>
     * Since {@link Value} objects are immutable, this does not create a new object but rather just acts as a view to
     * the internal object of the given object.
     * @param value The value to copy
     */
    public Value(@NotNull final Value value)
    {
        this(Objects.requireNonNull(value, "value must not be null").variant);
    }
    
    /**
     * Constructs a new {@link Value} from a {@link IValueConvertible}.
     * <p>
     * If the convertible's {@link IValueConvertible#getValue()} returns null, this will throw an exception.
     * @param convertible The convertible
     */
    public Value(@NotNull final IValueConvertible convertible)
    {
        this(Objects.requireNonNull(convertible, "convertible must not be null").getValue());
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private Value(@NotNull final IVariant<?> variant) { this.variant = variant; }

    //==================================================================================================================
    /**
     * If the object represented by this {@link Value} object is a map, will return the value as a map object.
     *
     * @return The underlying map object
     * @throws ClassCastException If the underlying value object was not a map
     */
    public @NotNull Map<Value, Value> getMap() { return castOrThrow(Type.MAP); }

    /**
     * If the object represented by this {@link Value} object is a list, will return the value as a list object.
     *
     * @return The underlying list object
     * @throws ClassCastException If the underlying value object was not a list
     */
    public @NotNull List<Value> getList() { return castOrThrow(Type.LIST); }

    /**
     * If the object represented by this {@link Value} object is a {@link Number},
     * will return the value as a {@link Number} object.
     *
     * @return The underlying {@link Number} object
     * @throws ClassCastException If the underlying value object was not a {@link Number}
     */
    public @NotNull Number getNumber() { return castOrThrow(Type.NUMBER); }

    /**
     * If the object represented by this {@link Value} object is a boolean, will return the value as a boolean object.
     *
     * @return The underlying boolean object
     * @throws ClassCastException If the underlying value object was not a boolean object
     */
    public @NotNull Boolean getBoolean() { return castOrThrow(Type.BOOLEAN); }

    /**
     * If the object represented by this {@link Value} object is a string, will return the value as a string.
     * <p>
     * Other than {@link Value#asString()} and {@link Value#toString()} this will only return a string if the underlying
     * object actually is a string object and throw otherwise.
     *
     * @return The underlying string
     * @throws ClassCastException If the underlying value object was not a string
     */
    public @NotNull String getString() { return castOrThrow(Type.STRING); }

    /**
     * Gets the underlying value object.
     *
     * @return The underlying object
     */
    public @Nullable Object getValue() { return this.variant.getValue(); }

    /**
     * Gets the type this Value has been overloaded with.
     *
     * @return The current value type
     */
    public @NotNull Type getType() { return this.variant.getType(); }

    //------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private <T> T castOrThrow(final @NotNull Type type)
    {
        if (!is(type))
        {
            throw new ClassCastException("Cannot convert " + getType().name() + " to " + type.name());
        }

        return (T) getValue();
    }

    //==================================================================================================================

    /**
     * Gets the current value as map and transforms the value if it isn't already one.
     * <ul>
     *     <li>{@link Type#MAP} As-is</li>
     *     <li>{@link Type#LIST} A map with each value of the list mapped to its respective index</li>
     *     <li>{@link Type#VOID} An empty map</li>
     *     <li><b>Others</b> A singleton map with the value mapped to a {@code value} key</li>
     * </ul>
     *
     * @return The transformed map object
     */
    public @NotNull Map<Value, Value> asMap() { return this.variant.asMap(); }

    /**
     * Gets the current value as map and transforms the value if it isn't already one.
     * <ul>
     *     <li>{@link Type#LIST} As-is</li>
     *     <li>{@link Type#MAP} A list with all values of the map and keys dropped (order is unspecified) </li>
     *     <li>{@link Type#VOID} An empty list</li>
     *     <li><b>Others</b> A singleton list with the value inserted at the beginning</li>
     * </ul>
     *
     * @return The transformed list object
     */
    public @NotNull List<Value> asList() { return this.variant.asList(); }

    /**
     * Gets the current value as map and transforms the value if it isn't already one.
     * <ul>
     *     <li>{@link Type#NUMBER} As-is</li>
     *     <li>{@link Type#STRING} The parsed number if successful, otherwise {@code 0}</li>
     *     <li>{@link Type#BOOLEAN} If the boolean value is true {@code 1}, otherwise {@code 0}</li>
     *     <li>{@link Type#LIST} The amount of elements in the list</li>
     *     <li>{@link Type#MAP} The amount of entries in the list</li>
     *     <li>{@link Type#VOID} {@code 0}</li>
     * </ul>
     *
     * @return The transformed {@link Number}> object
     */
    public @NotNull Number asNumber() { return this.variant.asNumber(); }

    /**
     * Gets the current value as map and transforms the value if it isn't already one.
     * <ul>
     *     <li>{@link Type#BOOLEAN} As-is</li>
     *     <li>{@link Type#STRING} The parsed boolean if successful, otherwise {@code false}</li>
     *     <li>{@link Type#NUMBER} If 0 then {@code false}, otherwise {@code true}</li>
     *     <li>{@link Type#LIST} {@code true} if there's at least one element in the list</li>
     *     <li>{@link Type#MAP} {@code true} if there's at least one entry in the map</li>
     *     <li>{@link Type#VOID} {@code false}</li>
     * </ul>
     *
     * @return The transformed boolean object
     */
    public @NotNull Boolean asBoolean() { return this.variant.asBoolean(); }

    /**
     * Gets the current value as map and transforms the value if it isn't already one.
     * <ul>
     *     <li>{@link Type#STRING} As-is</li>
     *     <li>{@link Type#MAP} A textually mapped list of all entries (e.g. {@code {key="val",key2=2}})</li>
     *     <li>{@link Type#LIST} A textually enumerated list of all elements (e.g. {@code ["val", 2]})</li>
     *     <li>{@link Type#VOID} {@code "null"}</li>
     *     <li><b>Others</b> The {@code toString()} value (e.g. {@code "true"}, {@code "0"})</li>
     * </ul>
     * Other than {@link Value#toString()} and {@link Value#getString()} this will transform the object's textual
     * representation in a way that represents the whole contents of the object.
     *
     * @return The transformed string object
     */
    public @NotNull String asString() { return this.variant.asString(); }

    //==================================================================================================================

    /**
     * Determines whether the current held object is a {@link HashMap}.
     *
     * @return True if it is a map
     */
    public boolean isMap() { return (getType() == Type.MAP); }

    /**
     * Determines whether the current held object is an {@link ArrayList}.
     *
     * @return True if it is a list
     */
    public boolean isList() { return (getType() == Type.LIST); }

    /**
     * Determines whether the current held object is a string.
     *
     * @return True if it is a string
     */
    public boolean isString() { return (getType() == Type.STRING); }

    /**
     * Determines whether the current held object is a {@link Number}.
     *
     * @return True if it is a number value
     */
    public boolean isNumber() { return (getType() == Type.NUMBER); }

    /**
     * Determines whether the current held object is a boolean value.
     *
     * @return True if it is a boolean value
     */
    public boolean isBoolean() { return (getType() == Type.BOOLEAN); }

    /**
     * Determines whether the current held object is {@code null}.
     *
     * @return True if this value is empty
     */
    public boolean isEmpty() { return (getType() == Type.VOID); }

    /**
     * Determines whether current held object is of type {@code type}.
     *
     * @param type The {@link Type} to check
     * @return True if the types match
     */
    public boolean is(final @NotNull Type type) { return (getType() == type); }

    //==================================================================================================================
    /**
     * Returns the string representation of the currently held value.
     * <p>
     * Other than {@link Value#getString()} and {@link Value#asString()},
     * this will use the object's {@code toString()} method to gather the textual representation,
     * which may or may not reflect the actual contents of the object.
     *
     * @return The string representation
     */
    @Override
    public @NotNull String toString()
    {
        return (super.toString() + "{type=" + this.getType() + ",value=" + this.getValue() + '}');
    }

    /**
     * Returns true if this and {@code obj} are references to the same object or if the underlying object of both
     * contain the same value.
     * <p>
     * If {@code obj} is not a {@link Value}, then {@code false} is returned.
     *
     * @return {@code true} if obj and the current {@link Value} instance are equal
     */
    @Override
    public boolean equals(@NotNull final Object obj)
    {
        if (obj == this)                   return true;
        if (!(obj instanceof Value other)) return false;
        return this.variant.equals(other.variant);
    }

    /**
     * Gets the hash code of the underlying value object or null if this {@link Value} object is empty.
     * @return The hash code
     */
    @Override
    public int hashCode()
    {
        return (this.variant.hashCode() + getType().ordinal());
    }
}

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
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.serialisation.codec.NoviaCodecs;
import xyz.lumialights.novia.api.core.network.codec.NoviaPacketCodecs;
import xyz.lumialights.novia.api.core.util.NumberType;
import xyz.lumialights.novia.api.core.util.Pair;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
sealed interface IVariant<T>
    permits
        IVariant.MapVariant,
        IVariant.ListVariant,
        IVariant.StringVariant,
        IVariant.NumberVariant,
        IVariant.BooleanVariant,
        IVariant.VoidVariant
{
    //******************************************************************************************************************
    record MapVariant(@NotNull Map<Value, Value> value)
        implements IVariant<Map<Value, Value>>
    {
        //**************************************************************************************************************
        public static final PacketCodec<ByteBuf, MapVariant> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(
                HashMap::new, Value.PACKET_CODEC,
                Value.PACKET_CODEC),
            MapVariant::value,
            (m -> new MapVariant(ImmutableMap.copyOf(m))));
        public static final Codec<MapVariant> CODEC = Codec
            .unboundedMap(Value.CODEC, Value.CODEC)
            .xmap((map -> new MapVariant(ImmutableMap.copyOf(map))), MapVariant::value);

        //**************************************************************************************************************
        private static String toKeyValuePair(@NotNull final Map.Entry<Value, Value> entry)
        {
            final String key = entry.getKey().asString();
            final Value  val = entry.getValue();
            return (getQuoted(key) + ":" + (val.isString() ? getQuoted(val.getString()) : val.asString()));
        }

        //**************************************************************************************************************
        @Override public @NotNull Map<Value, Value> getValue() { return this.value; }
        @Override public @NotNull Value.Type        getType()  { return Value.Type.MAP; }
        
        //==============================================================================================================
        @Override public @NotNull Map<Value, Value> asMap()     { return this.value; }
        @Override public @NotNull List<Value>       asList()    { return this.value.values().stream().toList(); }
        @Override public          boolean           asBoolean() { return !this.value.isEmpty(); }
        @Override public @NotNull Number            asNumber()  { return this.value.size(); }

        @Override
        public @NotNull String asString()
        {
            return this.value.entrySet()
                .stream()
                .map(MapVariant::toKeyValuePair)
                .collect(Collectors.joining(",", "{", "}"));
        }
    }

    record ListVariant(@NotNull List<Value> value)
        implements IVariant<List<Value>>
    {
        //**************************************************************************************************************
        public static final PacketCodec<ByteBuf, ListVariant> PACKET_CODEC = PacketCodec
            .tuple(
                PacketCodecs.collection(ArrayList::new, Value.PACKET_CODEC), ListVariant::value,
                (l -> new ListVariant(ImmutableList.copyOf(l))));
        public static final Codec<ListVariant> CODEC = Value.CODEC
            .listOf()
            .xmap((list -> new ListVariant(ImmutableList.copyOf(list))), ListVariant::value);

        //**************************************************************************************************************
        @Override public @NotNull List<Value> getValue() { return this.value; }
        @Override public @NotNull Value.Type  getType()  { return Value.Type.LIST; }
        
        //==============================================================================================================
        @Override public @NotNull List<Value> asList()    { return this.value; }
        @Override public          boolean     asBoolean() { return !this.value.isEmpty(); }
        @Override public @NotNull Number      asNumber()  { return this.value.size(); }

        @Override public @NotNull String asString()
        {
            return this.value
                .stream()
                .map(val -> (val.isString() ? IVariant.getQuoted(val.asString()) : val.asString()))
                .collect(Collectors.joining(",", "[", "]"));
        }

        @Override
        public @NotNull Map<Value, Value> asMap()
        {
            return Streams
                .mapWithIndex(this.value.stream(), (v, i) -> Pair.of(i, v))
                .collect(Collectors.toMap(Pair.forFirst(Value::new), Pair::second));
        }
    }

    record StringVariant(@NotNull String value)
        implements IVariant<String>
    {
        //**************************************************************************************************************
        public static final PacketCodec<ByteBuf, StringVariant> PACKET_CODEC = PacketCodec
            .tuple(
                PacketCodecs.STRING, StringVariant::value,
                StringVariant::new);
        public static final Codec<StringVariant> CODEC = Codec.STRING
            .xmap(StringVariant::new, StringVariant::value);

        //**************************************************************************************************************
        @Override public @NotNull String               getValue() { return this.value; }
        @Override public @NotNull Value.Type           getType()  { return Value.Type.STRING; }

        //==============================================================================================================
        @Override public @NotNull String            asString()  { return this.value; }
        @Override public          boolean           asBoolean() { return this.value.equalsIgnoreCase("true"); }
        @Override public @NotNull List<Value>       asList() { return createSingletonList(new Value(this.value)); }
        @Override public @NotNull Map<Value, Value> asMap()     { return createSingletonMap(new Value(this.value)); }

        @Override public @NotNull Number asNumber()
        {
            try
            {
                return NumberUtils.createNumber(this.value);
            }
            catch (final NumberFormatException ignored) {}

            return 0;
        }
    }

    record NumberVariant(@NotNull Number value)
        implements IVariant<Number>
    {
        //**************************************************************************************************************
        public static final PacketCodec<ByteBuf, NumberVariant> PACKET_CODEC = PacketCodec
            .tuple(
                NoviaPacketCodecs.NUMBER, NumberVariant::getValue,
                NumberVariant::new);
        public static final Codec<NumberVariant> CODEC = NoviaCodecs.NUMBER
            .xmap(NumberVariant::new, NumberVariant::getValue);

        //**************************************************************************************************************
        private static final double EPS = 1.0e-6;
        
        //**************************************************************************************************************
        public NumberVariant(@NotNull final Number value)
        {
            Objects.requireNonNull(value, "value must not be null");
            
            if (NumberType.isScalar(value))
            {
                this.value = value;
            }
            else if (NumberType.isNonScalarIntegral(value))
            {
                this.value = value.longValue();
            }
            else
            {
                this.value = value.doubleValue();
            }
        }
        
        //==============================================================================================================
        @Override public @NotNull Number     getValue() { return this.value; }
        @Override public @NotNull Value.Type getType()  { return Value.Type.NUMBER; }
        
        //==============================================================================================================
        @Override public @NotNull Number            asNumber()  { return this.value; }
        @Override public @NotNull String            asString()  { return this.value.toString(); }
        @Override public          boolean           asBoolean() { return (Math.abs(this.value.doubleValue()) > EPS); }
        @Override public @NotNull List<Value>       asList()    { return createSingletonList(new Value(this.value)); }
        @Override public @NotNull Map<Value, Value> asMap()     { return createSingletonMap(new Value(this.value)); }

        //==============================================================================================================
        @Override
        public boolean equals(@NotNull final Object obj)
        {
            if (obj == this)                                   return true;
            if (!(obj instanceof NumberVariant(Number other))) return false;
            
            if (NumberType.isFloatingPointScalar(this.value) || NumberType.isFloatingPointScalar(other))
            {
                final BigDecimal bd1 = new BigDecimal(this.value.toString());
                final BigDecimal bd2 = new BigDecimal(other.toString());
                return (bd1.compareTo(bd2) == 0);
            }

            return (this.value.longValue() == other.longValue());
        }
    }

    record BooleanVariant(@NotNull Boolean value)
        implements IVariant<Boolean>
    {
        //**************************************************************************************************************
        public static final PacketCodec<ByteBuf, BooleanVariant> PACKET_CODEC = PacketCodec
            .tuple(
                PacketCodecs.BOOLEAN, BooleanVariant::value,
                BooleanVariant::new);
        public static final Codec<BooleanVariant> CODEC = Codec.BOOL
            .xmap(BooleanVariant::new, BooleanVariant::value);

        //**************************************************************************************************************
        @Override public @NotNull Boolean    getValue() { return this.value; }
        @Override public @NotNull Value.Type getType()  { return Value.Type.BOOLEAN; }
        
        //==============================================================================================================
        @Override public          boolean           asBoolean() { return this.value; }
        @Override public @NotNull Number            asNumber()  { return (this.value ? 1 : 0); }
        @Override public @NotNull String            asString()  { return String.valueOf(this.value); }
        @Override public @NotNull List<Value>       asList()    { return createSingletonList(new Value(this.value)); }
        @Override public @NotNull Map<Value, Value> asMap()     { return createSingletonMap(new Value(this.value)); }
    }

    final class VoidVariant
        implements IVariant<Void>
    {
        //**************************************************************************************************************
        public static final VoidVariant                       INSTANCE     = new VoidVariant();
        public static final PacketCodec<ByteBuf, VoidVariant> PACKET_CODEC = PacketCodec.unit(INSTANCE);
        public static final Codec<VoidVariant>                CODEC        = NoviaCodecs.unit(INSTANCE);
        
        //**************************************************************************************************************
        private VoidVariant() {}
        
        //==============================================================================================================
        @Override public @Nullable Void       getValue() { return null; }
        @Override public @NotNull  Value.Type getType()  { return Value.Type.VOID; }

        //==============================================================================================================
        @Override public          boolean           asBoolean() { return false; }
        @Override public @NotNull Number            asNumber()  { return 0; }
        @Override public @NotNull String            asString()  { return "null"; }
        @Override public @NotNull List<Value>       asList()    { return Lists.newArrayList(); }
        @Override public @NotNull Map<Value, Value> asMap()     { return Maps.newHashMap(); }
    }

    //******************************************************************************************************************
    private static @NotNull Map<Value, Value> createSingletonMap(@NotNull final Value value)
    {
        return Maps.newHashMap(Collections.singletonMap(new Value("value"), value));
    }

    private static @NotNull List<Value> createSingletonList(@NotNull final Value value)
    {
        return Lists.newArrayList(Collections.singletonList(value).iterator());
    }

    //==================================================================================================================
    private static @NotNull String getQuoted(@NotNull final String str)
    {
        return ('"' + str + '"');
    }

    //******************************************************************************************************************
    @Nullable T           getValue();
    @NotNull  Value.Type  getType();

    //==================================================================================================================
    @NotNull Map<Value, Value> asMap();
    @NotNull List<Value>       asList();
    @NotNull String            asString();
             boolean           asBoolean();
    @NotNull Number            asNumber();
}

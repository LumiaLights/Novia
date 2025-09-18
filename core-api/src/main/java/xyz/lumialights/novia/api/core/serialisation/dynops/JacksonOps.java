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
package xyz.lumialights.novia.api.core.serialisation.dynops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;



//**********************************************************************************************************************
/** Provides an alternative {@link DynamicOps} implementation to {@link JsonOps} for JSON by using Jackson instead. */
public class JacksonOps
    implements DynamicOps<JsonNode>
{
    //******************************************************************************************************************
    private static final class ArrayBuilder
        implements ListBuilder<JsonNode>
    {
        //**************************************************************************************************************
        private DataResult<ArrayNode> builder = DataResult.success(
            JsonNodeFactory.instance.arrayNode(),
            Lifecycle.stable());

        //**************************************************************************************************************
        @Override public DynamicOps<JsonNode> ops() { return INSTANCE; }
        
        //==============================================================================================================
        @Override
        public ListBuilder<JsonNode> add(final JsonNode value)
        {
            builder = builder.map(b ->
            {
                b.add(value);
                return b;
            });
            
            return this;
        }

        @Override
        public ListBuilder<JsonNode> add(final DataResult<JsonNode> value)
        {
            builder = builder.apply2stable((b, node) ->
            {
                b.add(node);
                return b;
            }, value);
            
            return this;
        }

        //==============================================================================================================
        @Override
        public ListBuilder<JsonNode> withErrorsFrom(final DataResult<?> result)
        {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<JsonNode> mapError(final UnaryOperator<String> onError)
        {
            builder = builder.mapError(onError);
            return this;
        }

        //==============================================================================================================
        @Override
        public DataResult<JsonNode> build(final JsonNode prefix)
        {
            final DataResult<JsonNode> result = builder.flatMap(b ->
            {
                if (!prefix.isArray() && !prefix.isNull())
                {
                    return DataResult.error(() -> "Cannot append a list to not a list: " + prefix, prefix);
                }

                final ArrayNode array = JsonNodeFactory.instance.arrayNode();
                
                if (prefix != ops().empty())
                {
                    array.addAll((ArrayNode) prefix);
                }
                
                array.addAll(b);
                return DataResult.success(array, Lifecycle.stable());
            });

            builder = DataResult.success(JsonNodeFactory.instance.arrayNode(), Lifecycle.stable());
            return result;
        }
    }
    
    private class JsonRecordBuilder
        extends RecordBuilder.AbstractStringBuilder<JsonNode, ObjectNode>
    {
        //**************************************************************************************************************
        protected JsonRecordBuilder()
        {
            super(JacksonOps.this);
        }

        //==============================================================================================================
        @Override protected ObjectNode initBuilder() { return JsonNodeFactory.instance.objectNode(); }

        //==============================================================================================================
        @Override
        protected ObjectNode append(final String key, final JsonNode value, final ObjectNode builder)
        {
            builder.set(key, value);
            return builder;
        }

        //==============================================================================================================
        @Override
        protected DataResult<JsonNode> build(final ObjectNode builder, final JsonNode prefix)
        {
            if (prefix == null || prefix.isNull())
            {
                return DataResult.success(builder);
            }
            
            if (!prefix.isObject())
            {
                return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
            }
            
            final ObjectNode result = JsonNodeFactory.instance.objectNode();
            result.setAll((ObjectNode) prefix);
            result.setAll(builder);
            
            return DataResult.success(result);
        }
    }
    
    //******************************************************************************************************************
    public static final JacksonOps INSTANCE = new JacksonOps();

    //******************************************************************************************************************
    @Override public JsonNode empty() { return NullNode.getInstance(); }

    //==================================================================================================================
    @Override
    public <U> U convertTo(final DynamicOps<U> outOps, final JsonNode input)
    {
        return switch (input.getNodeType())
        {
            case OBJECT  -> convertMap(outOps, input);
            case ARRAY   -> convertList(outOps, input);
            case NULL    -> outOps.empty();
            case STRING  -> outOps.createString(input.textValue());
            case BINARY  -> outOps.createString(input.asText());
            case BOOLEAN -> outOps.createBoolean(input.booleanValue());
            case NUMBER  -> outOps.createNumeric(input.numberValue());
            
            case POJO, MISSING ->
                throw new UnsupportedOperationException("Unsupported node type: " + input.getNodeType().name());
        };
    }

    //==================================================================================================================
    @Override
    public DataResult<Number> getNumberValue(final JsonNode input)
    {
        if (!input.isNumber())
        {
            return DataResult.error(() -> "Not a number: " + input);
        }
        
        return DataResult.success(input.numberValue());
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final JsonNode input)
    {
        if (!input.isBoolean())
        {
            return DataResult.error(() -> "Not a boolean: " + input);
        }

        return DataResult.success(input.booleanValue());
    }
    
    @Override
    public DataResult<String> getStringValue(final JsonNode input)
    {
        if (!input.isTextual())
        {
            return DataResult.error(() -> "Not a string: " + input);
        }
        
        return DataResult.success(input.textValue());
    }
    
    @Override
    public DataResult<Stream<Pair<JsonNode, JsonNode>>> getMapValues(final JsonNode input)
    {
        if (!input.isObject())
        {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        
        return DataResult.success(Streams
            .stream(input.fields())
            .map(e -> Pair.of(createString(e.getKey()), (!e.getValue().isNull() ? e.getValue() : null))));
    }
    
    @Override
    public DataResult<Consumer<BiConsumer<JsonNode, JsonNode>>> getMapEntries(final JsonNode input)
    {
        if (!input.isObject())
        {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        
        return DataResult.success(c ->
        {
            var it = input.fields();
            
            while (it.hasNext())
            {
                final var entry = it.next();
                c.accept(createString(entry.getKey()), (!entry.getValue().isNull() ? entry.getValue() : null));
            }
        });
    }
    
    @Override
    public DataResult<MapLike<JsonNode>> getMap(final JsonNode input)
    {
        if (!input.isObject())
        {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        
        return DataResult.success(new MapLike<>()
        {
            @Override
            public @Nullable JsonNode get(final JsonNode key)
            {
                return get(key.textValue());
            }

            @Override
            public @Nullable JsonNode get(final String key)
            {
                final JsonNode node = input.get(key);
                return (!node.isNull() ? node : null);
            }

            @Override
            public Stream<Pair<JsonNode, JsonNode>> entries()
            {
                return Streams
                    .stream(input.fields())
                    .map(e -> Pair.of(createString(e.getKey()), e.getValue()));
            }

            @Override
            public String toString()
            {
                return "MapLike[" + input + "]";
            }
        });
    }
    
    @Override
    public DataResult<Stream<JsonNode>> getStream(final JsonNode input)
    {
        if (!input.isArray())
        {
            return DataResult.error(() -> "Not a JSON array: " + input);
        }
        
        return DataResult.success(StreamSupport
            .stream(input.spliterator(), false)
            .map(e -> (!e.isNull() ? e : null)));
    }

    @Override
    public DataResult<Consumer<Consumer<JsonNode>>> getList(final JsonNode input)
    {
        if (!input.isArray())
        {
            return DataResult.error(() -> "Not a JSON array: " + input);
        }
        
        return DataResult.success(c ->
        {
            for (final var node : input)
            {
                c.accept(!node.isNull() ? node : null);
            }
        });
    }
    
    //==================================================================================================================
    @Override
    public JsonNode createNumeric(final Number i)
    {
        return switch (i)
        {
            case Byte       val -> JsonNodeFactory.instance.numberNode(val);
            case Short      val -> JsonNodeFactory.instance.numberNode(val);
            case Integer    val -> JsonNodeFactory.instance.numberNode(val);
            case Long       val -> JsonNodeFactory.instance.numberNode(val);
            case Float      val -> JsonNodeFactory.instance.numberNode(val);
            case Double     val -> JsonNodeFactory.instance.numberNode(val);
            case BigInteger val -> JsonNodeFactory.instance.numberNode(val);
            case BigDecimal val -> JsonNodeFactory.instance.numberNode(val);
            
            default -> (i.longValue() == i.doubleValue()
                ? JsonNodeFactory.instance.numberNode(i.longValue())
                : JsonNodeFactory.instance.numberNode(i.doubleValue()));
        };
    }

    @Override
    public JsonNode createBoolean(final boolean value)
    {
        return JsonNodeFactory.instance.booleanNode(value);
    }

    

    @Override
    public JsonNode createString(final String value)
    {
        return JsonNodeFactory.instance.textNode(value);
    }

    @Override
    public DataResult<JsonNode> mergeToList(final JsonNode list, final JsonNode value)
    {
        if (!list.isArray() && !list.isNull())
        {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        final ArrayNode result = JsonNodeFactory.instance.arrayNode();
        
        if (list != empty())
        {
            result.addAll((ArrayNode) list);
        }
        
        result.add(value);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonNode> mergeToList(final JsonNode list, final List<JsonNode> values)
    {
        if (!list.isArray() && !list.isNull())
        {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        final ArrayNode result = JsonNodeFactory.instance.arrayNode();
        
        if (list != empty())
        {
            result.addAll((ArrayNode) list);
        }
        
        values.forEach(result::add);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonNode> mergeToMap(final JsonNode map, final JsonNode key, final JsonNode value)
    {
        if (!map.isObject() && !map.isNull())
        {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }
        
        if (!key.isTextual())
        {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }

        final ObjectNode output = JsonNodeFactory.instance.objectNode();
        
        if (map != empty())
        {
            Streams.stream(map.fields()).forEach(e -> output.set(e.getKey(), e.getValue()));
        }
        
        output.set(key.textValue(), value);
        return DataResult.success(output);
    }

    @Override
    public DataResult<JsonNode> mergeToMap(final JsonNode map, final MapLike<JsonNode> values)
    {
        if (!map.isObject() && !map.isNull())
        {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        final ObjectNode output = JsonNodeFactory.instance.objectNode();
        
        if (!map.isNull())
        {
            Streams.stream(map.fields()).forEach(e -> output.set(e.getKey(), e.getValue()));
        }

        final List<JsonNode> missed = Lists.newArrayList();
        values.entries().forEach(entry ->
        {
            final JsonNode key = entry.getFirst();
            
            if (!key.isTextual())
            {
                missed.add(key);
                return;
            }
            
            output.set(key.textValue(), entry.getSecond());
        });

        if (!missed.isEmpty())
        {
            return DataResult.error(() -> "some keys are not strings: " + missed, output);
        }

        return DataResult.success(output);
    }

    @Override
    public JsonNode createMap(final Stream<Pair<JsonNode, JsonNode>> map)
    {
        final ObjectNode result = JsonNodeFactory.instance.objectNode();
        map.forEach(p -> result.set(p.getFirst().textValue(), p.getSecond()));
        return result;
    }

    @Override
    public JsonNode createList(final Stream<JsonNode> input)
    {
        final ArrayNode result = JsonNodeFactory.instance.arrayNode();
        input.forEach(result::add);
        return result;
    }

    //==================================================================================================================
    @Override
    public JsonNode remove(final JsonNode input, final String key)
    {
        if (input.isObject())
        {
            final ObjectNode result = input.deepCopy();
            result.remove(key);
            return result;
        }
        
        return input;
    }

    //==================================================================================================================
    @Override public ListBuilder<JsonNode>   listBuilder() { return new ArrayBuilder(); }
    @Override public RecordBuilder<JsonNode> mapBuilder()  { return new JsonRecordBuilder(); }
    
    //==================================================================================================================
    @Override public String toString() { return "JSON"; }
}

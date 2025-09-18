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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.Strictness;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.*;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.InternalModException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



//**********************************************************************************************************************
public abstract class DynamicOpsSerialisers
{
    //******************************************************************************************************************
    public static abstract class TextSerialiser<T>
        implements IDynamicOpsSerialiser<T>
    {
        //**************************************************************************************************************
        @Override public boolean isReadableDataFormat() { return true; }
        
        //==============================================================================================================
        @Override
        public @NotNull T deserialise(@NotNull final InputStream stream)
            throws
                IOException,
                ConfigSerialisationException
        {
            Objects.requireNonNull(stream, "input stream must not be null");
            
            final String document = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return deserialiseString(document);
        }
        
        @Override
        public void serialise(@NotNull final T input, @NotNull final OutputStream stream)
            throws IOException
        {
            Objects.requireNonNull(input,  "input must not be null");
            Objects.requireNonNull(stream, "output stream must not be null");
            
            final String document = serialiseString(input);
            IOUtils.write(document, stream, StandardCharsets.UTF_8);
        }
    }
    
    public static abstract class BinarySerialiser<T>
        implements IDynamicOpsSerialiser<T>
    {
        //**************************************************************************************************************
        @Override public boolean isReadableDataFormat() { return false; }
        
        //==============================================================================================================
        @Override
        public @NotNull String serialiseString(@NotNull final T input)
        {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public @NotNull T deserialiseString(@NotNull final String input)
        {
            throw new UnsupportedOperationException();
        }
    }
    
    //==================================================================================================================
    public static class JsonGson
        extends TextSerialiser<JsonElement>
    {
        //**************************************************************************************************************
        public static final JsonGson INSTANCE = new JsonGson();
        
        //--------------------------------------------------------------------------------------------------------------
        private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setStrictness(Strictness.LENIENT)
            .create();
        
        //**************************************************************************************************************
        @Override public @NotNull String                  getFileExtension() { return "json"; }
        @Override public @NotNull DynamicOps<JsonElement> getOps()           { return JsonOps.INSTANCE; }
        
        //==============================================================================================================
        @Override
        public @NotNull JsonElement deserialiseString(@NotNull final String input)
            throws ConfigSerialisationException
        {
            final JsonNode node = JsonJackson.INSTANCE.deserialiseString(input);
            return JacksonOps.INSTANCE.convertTo(JsonOps.INSTANCE, node);
        }
        
        @Override
        public @NotNull String serialiseString(@NotNull final JsonElement input)
        
        {
            final JsonNode node = JsonOps.INSTANCE.convertTo(JacksonOps.INSTANCE, input);
            return JsonJackson.INSTANCE.serialiseString(node);
        }
    }
    
    //==================================================================================================================
    public static class JsonJackson
        extends TextSerialiser<JsonNode>
    {
        //**************************************************************************************************************
        public static final JsonJackson INSTANCE = new JsonJackson();
        
        //--------------------------------------------------------------------------------------------------------------
        private static final JsonFactory FACTORY = JsonFactory
            .builder()
            .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
            .build();
        
        //**************************************************************************************************************
        @Override public @NotNull String               getFileExtension() { return "json"; }
        @Override public @NotNull DynamicOps<JsonNode> getOps()           { return JacksonOps.INSTANCE; }
        
        //==============================================================================================================
        @Override
        public @NotNull JsonNode deserialiseString(@NotNull final String input)
            throws ConfigSerialisationException
        {
            try
            {
                final JsonMapper mapper = new JsonMapper(FACTORY);
                final JsonNode   node   = mapper.readTree(input);
                
                if (node == null)
                {
                    throw new ConfigSerialisationException("Invalid JSON document");
                }
                
                return node;
            }
            catch (final JsonProcessingException ex)
            {
                throw new ConfigSerialisationException(ex);
            }
        }
        
        @Override
        public @NotNull String serialiseString(@NotNull final JsonNode input)
        {
            final JsonMapper mapper = new JsonMapper(FACTORY);
            
            try
            {
                return mapper.writeValueAsString(input);
            }
            catch (final JsonProcessingException ex)
            {
                throw new InternalModException(ex);
            }
        }
    }
    
    //==================================================================================================================
    public static class Nbt
        extends BinarySerialiser<NbtElement>
    {
        //**************************************************************************************************************
        public static final Nbt INSTANCE = new Nbt();
        
        //**************************************************************************************************************
        @Override public @NotNull String                 getFileExtension() { return "dat"; }
        @Override public @NotNull DynamicOps<NbtElement> getOps()           { return NbtOps.INSTANCE; }
        
        //==============================================================================================================
        @Override
        public @NotNull NbtElement deserialise(@NotNull final InputStream input)
            throws
                IOException,
                ConfigSerialisationException
        {
            try
            {
                return NbtIo.readCompressed(input, NbtSizeTracker.ofUnlimitedBytes());
            }
            catch (final NbtSizeValidationException ex)
            {
                throw new ConfigSerialisationException(ex);
            }
        }

        @Override
        public void serialise(@NotNull final NbtElement input, @NotNull final OutputStream output)
            throws IOException
        {
            NbtIo.writeCompressed((NbtCompound) input, output);
        }
    }
    
    public static class Java
        extends BinarySerialiser<Object>
    {
        //**************************************************************************************************************
        public static final Java INSTANCE = new Java();
        
        //**************************************************************************************************************
        @Override public @NotNull String             getFileExtension() { return "ser"; }
        @Override public @NotNull DynamicOps<Object> getOps()           { return JavaOps.INSTANCE; }
        
        //==============================================================================================================
        @Override
        public @NotNull Object deserialise(@NotNull final InputStream input)
            throws
                IOException,
                ConfigSerialisationException
        {
            try
            {
                final ObjectInputStream ois = new ObjectInputStream(input);
                return ois.readObject();
            }
            catch (final ClassNotFoundException | InvalidClassException | StreamCorruptedException
                         | OptionalDataException ex)
            {
                throw new ConfigSerialisationException(ex);
            }
        }

        @Override
        public void serialise(@NotNull final Object input, @NotNull final OutputStream output)
            throws IOException
        {
            final ObjectOutputStream oos = new ObjectOutputStream(output);
            oos.writeObject(input);
        }
    }
    
    //******************************************************************************************************************
    private static final Map<Class<? extends DynamicOps<?>>, IDynamicOpsSerialiser<?>> PROVIDERS;
    
    //==================================================================================================================
    static
    {
        PROVIDERS = new ConcurrentHashMap<>();
        
        registerHelper(JsonOps.class,    new JsonGson());
        registerHelper(JacksonOps.class, new JsonJackson());
        registerHelper(NbtOps.class,     new Nbt());
        registerHelper(JavaOps.class,    new Java());
    }
    
    //******************************************************************************************************************
    /**
     * Gets a {@link DynamicOps} serialiser that can read from and write to files based on the given dynops instance.
     * @param opsClass The {@link DynamicOps} class to get the serialiser for
     * @return The serialiser or null if none was found
     * @param <T> Type of object the dynops instance manages
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable IDynamicOpsSerialiser<T> get(@NotNull final Class<? extends DynamicOps<T>> opsClass)
    {
        return (IDynamicOpsSerialiser<T>) PROVIDERS.get(opsClass);
    }
    
    /**
     * Gets a {@link DynamicOps} serialiser that can read from and write to files based on the given dynops instance.
     * @param ops The {@link DynamicOps} instance to get the serialiser for
     * @return The serialiser or null if none was found
     * @param <T> Type of object the dynops instance manages
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable IDynamicOpsSerialiser<T> get(@NotNull final DynamicOps<T> ops)
    {
        return (IDynamicOpsSerialiser<T>) PROVIDERS.get(ops.getClass());
    }
    
    /**
     * Registers a custom serialiser for the given {@link DynamicOps} class.
     * @param opsClass   The {@link DynamicOps} class to register this serialiser for
     * @param serialiser The serialiser to register
     * @throws DynOpsRegistryException If a serialiser with that {@link DynamicOps} class had already been registered
     * @param <T> Type of object the dynops instance manages
     */
    public static <T> void register(@NotNull final Class<? extends DynamicOps<T>> opsClass,
                                    @NotNull final IDynamicOpsSerialiser<T>       serialiser)
    {
        Objects.requireNonNull(opsClass,   "dynamic ops class must not be null");
        Objects.requireNonNull(serialiser, "dynamic ops serialiser must not be null");
        
        if (PROVIDERS.containsKey(opsClass))
        {
            throw new DynOpsRegistryException(opsClass.getCanonicalName() + " is already registered");
        }
        
        registerHelper(opsClass, serialiser);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static <T> void registerHelper(@NotNull final Class<? extends DynamicOps<T>> opsClass,
                                           @NotNull final IDynamicOpsSerialiser<T>       serialiser)
    {
        PROVIDERS.put(opsClass, serialiser);
    }
}

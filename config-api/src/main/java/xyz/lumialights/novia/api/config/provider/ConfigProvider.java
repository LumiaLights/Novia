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
package xyz.lumialights.novia.api.config.provider;

import com.mojang.serialization.DynamicOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.*;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.core.InternalModException;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.spec.ConfigSpec;
import xyz.lumialights.novia.api.config.spec.PropertySpec;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.core.util.Pair;
import xyz.lumialights.novia.api.core.util.PropagatingException;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public class ConfigProvider<Container>
    extends BaseNetworkProvider<Container>
{
    //******************************************************************************************************************
    public class UpdateBuilder
    {
        //**************************************************************************************************************
        private record UpdateEntry<Container>(@NotNull Function<Container, Property> getter, @Nullable Value value) {}

        //**************************************************************************************************************
        private final List<UpdateEntry<Container>> entries = new LinkedList<>();
        private final Container                    container;

        //**************************************************************************************************************
        private UpdateBuilder(@NotNull final Container container)
        {
            this.container = container;
        }

        //==============================================================================================================
        /**
         * Sets a specified container property to the given value.
         * @param getter   The getter method for the container property
         * @param newValue The new value to set the property to
         * @return This
         */
        public @NotNull UpdateBuilder set(@NotNull final Function<Container, Property> getter,
                                          @NotNull final Value                         newValue)
        {
            this.entries.add(new UpdateEntry<>(getter, newValue));
            return this;
        }

        /**
         * Resets a specified container property to its default value.
         * @param getter The getter method for the container property
         * @return This
         */
        public @NotNull UpdateBuilder reset(@NotNull final Function<Container, Property> getter)
        {
            this.entries.add(new UpdateEntry<>(getter, null));
            return this;
        }

        //==============================================================================================================
        void send()
            throws PropertyValidationException
        {
            final Map<JsonPointer, Pair<Property, Value>> update_map = new HashMap<>();

            for (final var entry : this.entries)
            {
                final Property        property = entry.getter.apply(container);
                final PropertySpec<?> spec     = Objects.requireNonNull(property.getSpec());

                if (entry.value == null)
                {
                    continue;
                }

                spec.validate(entry.value);
                update_map.put(spec.pointer(), Pair.of(property, entry.value));
            }

            update_map.forEach((pointer, pair) ->
            {
                final Value    value    = pair.second();
                final Property property = pair.first();

                if (value == null)
                {
                    property.reset();
                }
                else
                {
                    property.setValueUnchecked(value);
                }
            });

            ConfigProvider.this.sendUpdates(
                update_map.entrySet()
                    .stream()
                    .map(e -> Pair.of(e.getKey(), e.getValue().second()))
                    .collect(Collectors.toList()));
        }
    }
    
    public static class Builder<Container>
        extends IConfigProvider.Builder<Builder<Container>, Container, ConfigProvider<Container>>
    {
        //**************************************************************************************************************
        private boolean optional = false;
        
        //**************************************************************************************************************
        protected Builder(@NotNull final ConfigSpec<Container> spec)
        {
            super(spec);
        }
        
        //==============================================================================================================
        /**
         * Marks this provider optional, by that means, the client is not required to have it.
         * @return {@code this}
         */
        public @NotNull Builder<Container> setOptional()
        {
            this.optional = true;
            return this;
        }
        
        //==============================================================================================================
        @Override
        public @NotNull ConfigProvider<Container> build(@NotNull final Identifier id)
        {
            return new ConfigProvider<>(this.spec, id, this.ops, this.optional);
        }
    }
    
    //******************************************************************************************************************
    /**
     * Creates a new {@link ConfigProvider} builder.
     * @param spec The config specification
     * @return The new builder
     */
    public static <Container> @NotNull Builder<Container> builder(@NotNull final ConfigSpec<Container> spec)
    {
        return new Builder<>(spec);
    }
    
    //******************************************************************************************************************
    private final ConfigSpec<Container> spec;
    
    private final Container serverContainer;
    private final Container clientContainer;

    private boolean batchMode           = false;
    private boolean lockClientContainer = true;

    //******************************************************************************************************************
    private ConfigProvider(@NotNull final ConfigSpec<Container> spec,
                           @NotNull final Identifier            id,
                           @NotNull final DynamicOps<?>         ops,
                                    final boolean               optional)
    {
        super(id, optional, ops);
        
        this.spec            = spec;
        this.serverContainer = spec.create();
        
        this.spec.bindContainer(this.serverContainer, this::serverValueChanged);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
        {
            this.clientContainer = spec.create();
            this.spec.bindContainer(this.clientContainer, this::clientValueChanged);
        }
        else
        {
            this.clientContainer = null;
        }
    }

    //==================================================================================================================
    @Override
    public @NotNull ConfigSpec<Container> getSpec() { return this.spec; }
    
    /**
     * Gets the server container of this config provider.
     * <p>
     * Do note that on a client that is connected to a dedicated server, this is unused and any changes to it on
     * the render thread, regardless of whether the server is dedicated or not, will likely result in a race condition.
     * <p>
     * If it is not immediately apparent what context this call currently is in, and there is a non-null
     * {@link net.minecraft.world.World} instance at your disposal,
     * it is almost always better to use {@link ConfigProvider#getContainer(net.minecraft.world.World)}
     * @return The server container instance
     */
    @Override
    public @NotNull Container getManagedContainer() { return this.serverContainer; }
    
    /**
     * Returns the server container of this config provider.
     * <p>
     * On a dedicated server this will return null as there is no need for a client configuration.
     * Calling this on the integrated server thread, however, might result in a race condition if the properties
     * are read or changed.
     * <p>
     * Generally, client containers should never be written to but only read from, that's what server containers are
     * there for. In fact, writing to a client container will inevitably throw a {@link LockedPropertyChangeException}.
     * @return The client container instance
     */
    public @Nullable Container getClientContainer() { return this.clientContainer; }

    /**
     * Gets the biased container instance for the given {@link World} object.
     * <p>
     * This will determine based on {@link World#isClient} what container to get, if this flag is false it will
     * return {@link ConfigProvider#getManagedContainer()}, otherwise {@link ConfigProvider#getClientContainer()}.
     * <p>
     * This should only ever be used when reading from a container, as writing to a client container
     * will throw {@link LockedPropertyChangeException}.
     * @param world The current world instance
     * @return The container for that world object
     */
    public @Nullable Container getContainer(@NotNull final World world)
    {
        return (world.isClient() ? this.clientContainer : this.serverContainer);
    }
    
    //==================================================================================================================
    /**
     * Executes a batch update to multiple properties at the same time.<p>
     * If any of the properties throws a {@link PropertyValidationException}, all changes are discarded.<p>
     * This is useful if there need to be several properties updated at the same time, but to bundle the updates sent
     * to the client to reduce network traffic. It is also useful to change a set of properties at the same time
     * and making sure all properties are only set if all receive valid values.<p>
     * This method should only ever be called on the server thread, otherwise a race condition might occur.<p>
     * Here is a simple example on how to use this:<br>
     * We assume we have a {@code SheepConfig} with properties that define how a custom sheep entity
     * spawns when summoned. This is defined by the properties {@code SheepConfig.defaultColour},
     * {@code SheepConfig.defaultSize} and {@code SheepConfig.defaultHostile}.<p>
     * We also provide a command, that defines some presets that, when applied, modifies these properties with a
     * specified set of new values. For our example we have a preset "thunder-sheep", which has yellow colour, is twice
     * the size of a normal sheep and is hostile towards the player, to implement this preset we do the following:
     * <pre>
     * {@code
     * // Our configuration
     * ConfigProvider<SheepConfig> SHEEP_CONFIG;
     *
     * // Let's build a batch update
     * SHEEP_CONFIG.batchUpdate(updater -> updater
     *     .set(SheepConfig::defaultColour,  new Value("yellow"))
     *     .set(SheepConfig::defaultSize,    new Value(2))
     *     .set(SheepConfig::defaultHostile, new Value(true)));
     * }
     * </pre>
     * @param updateBuilder The updater lambda expression
     * @throws PropertyValidationException If any of the properties failed validation before setting the value
     */
    public void batchUpdate(@NotNull final Function<UpdateBuilder, UpdateBuilder> updateBuilder)
        throws PropertyValidationException
    {
        this.batchMode = true;

        try
        {
            updateBuilder
                .apply(new UpdateBuilder(this.serverContainer))
                .send();
        }
        finally
        {
            this.batchMode = false;
        }
    }

    //==================================================================================================================
    @Override
    void updateClient(@NotNull final List<Pair<JsonPointer, Value>> updates, final boolean isRemote)
        throws PropertyValidationException
    {
        final BiConsumer<Property, Value> setter = ((property, value) ->
        {
            try
            {
                property.setValue(value);
            }
            catch (final PropertyValidationException ex)
            {
                throw new PropagatingException(ex);
            }
        });
        
        try
        {
            this.processClientUpdate(updates, setter, isRemote);
        }
        catch (final PropagatingException ex)
        {
            if (ex.getCause() instanceof PropertyValidationException val_ex)
            {
                throw val_ex;
            }
            
            throw new InternalModException("unexpected exception encountered", ex.getCause());
        }
    }
    
    @Override
    void forceUpdateClient(@NotNull final List<Pair<JsonPointer, Value>> updates, boolean isRemote)
    {
        this.processClientUpdate(updates, Property::setValueUnchecked, isRemote);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void processClientUpdate(@NotNull final List<Pair<JsonPointer, Value>> updates,
                                     @NotNull final BiConsumer<Property, Value>    consumer,
                                              final boolean                        isRemote)
    {
        {
            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
            {
                Novia.LOGGER.error("Tried updating client provider on physical server");
                return;
            }
            
            if (!Novia.getInstance().isRenderThread())
            {
                Novia.LOGGER.error("Tried updating client provider not on logical client");
                return;
            }
        }
        
        this.lockClientContainer = false;
        
        try
        {
            updates.forEach(update -> consumer.accept(
                this.spec.flatPropertySpecs()
                    .get(update.first())
                    .get(this.clientContainer),
                update.second()));
        }
        finally
        {
            this.lockClientContainer = true;
            this.setRemote(isRemote);
        }
    }

    //==================================================================================================================
    private void serverValueChanged(@NotNull final Property property, @NotNull final Value value)
    {
        if (!this.batchMode)
        {
            this.sendUpdates(List.of(Pair.of(Objects.requireNonNull(property.getSpec()).pointer(), value)));
        }
    }

    private void clientValueChanged(@NotNull final Property property, @NotNull final Value ignored)
    {
        if (this.lockClientContainer)
        {
            throw new LockedPropertyChangeException(Objects.requireNonNull(property.getSpec()).pointer());
        }
    }
    
    //==================================================================================================================
    private void sendUpdates(@NotNull final List<Pair<JsonPointer, Value>> values)
    {
        ConfigManagerNetwork.sendUpdates(this.getId(), values);
    }
}

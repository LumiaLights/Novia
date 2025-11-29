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
package xyz.lumialights.novia.api.config.client;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.ConfigManager;
import xyz.lumialights.novia.api.config.PropertyValidationException;
import xyz.lumialights.novia.api.config.client.network.ConfigSyncException;
import xyz.lumialights.novia.api.config.client.network.RemotePhase;
import xyz.lumialights.novia.api.config.registry.ProviderOverride;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.config.network.payload.ConfigC2SConfigAcknowledgePayload;
import xyz.lumialights.novia.api.config.network.payload.ConfigS2CConfigSyncPayload;
import xyz.lumialights.novia.api.config.network.payload.IPlayS2CUpdateConfigPayload;
import xyz.lumialights.novia.api.config.network.payload.PlayS2CConfigSyncPayload;
import xyz.lumialights.novia.api.config.provider.BaseNetworkProvider;
import xyz.lumialights.novia.api.config.registry.SnapshotCache;
import xyz.lumialights.novia.api.config.provider.ProviderClientUpdater;
import xyz.lumialights.novia.api.config.server.ServerConfigManagerHandler;
import xyz.lumialights.novia.api.config.spec.PropertySpec;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.core.util.Pair;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;



//**********************************************************************************************************************
@ApiStatus.Internal
public class ClientConfigManagerHandler
    extends ServerConfigManagerHandler
    implements
        ClientConfigurationNetworking.ConfigurationPayloadHandler<ConfigS2CConfigSyncPayload>,
        ClientPlayNetworking         .PlayPayloadHandler         <PlayS2CConfigSyncPayload>
{
    //******************************************************************************************************************
    private final Deque<IPlayS2CUpdateConfigPayload> pending = new ArrayDeque<>();
    
    private volatile PacketByteBuf configBuf = null;
    private volatile RemotePhase   phase     = null;
    
    //******************************************************************************************************************
    public ClientConfigManagerHandler()
    {
        ClientConfigurationConnectionEvents.INIT.register((handler, client) ->
        {
            if (!client.isInSingleplayer())
            {
                ClientConfigurationNetworking.registerReceiver(ConfigS2CConfigSyncPayload.ID, this);
                
                this.configBuf = PacketByteBufs.create();
                this.phase     = RemotePhase.CONFIGURATION;
            }
        });
        ClientPlayConnectionEvents.INIT.register((handler, client) ->
        {
            if (this.phase == RemotePhase.CONFIGURATION)
            {
                ClientPlayNetworking.registerReceiver(
                    IPlayS2CUpdateConfigPayload.Single.ID,
                    this::processPlaySyncPacket);
                ClientPlayNetworking.registerReceiver(
                    IPlayS2CUpdateConfigPayload.Bulk.ID,
                    this::processPlaySyncPacket);
                
                ClientPlayNetworking.registerReceiver(PlayS2CConfigSyncPayload.ID, this);
                
                this.configBuf = PacketByteBufs.create();
                this.phase     = RemotePhase.PLAY_INIT;
            }
        });

        ClientConfigurationConnectionEvents.DISCONNECT.register(this::disconnect);
        ClientPlayConnectionEvents         .DISCONNECT.register(this::disconnect);
        ClientLoginConnectionEvents        .DISCONNECT.register(this::disconnect);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> void disconnect(final @NotNull T handler, final @NotNull MinecraftClient client)
    {
        if (this.phase != null)
        {
            this.resetProviders(client);
        }
        
        this.phase = null;
    }
    
    //==================================================================================================================
    // This will only be called in single-player, as a remote connection to a server won't update local providers.
    @Override
    public void sendUpdates(final @NotNull Identifier configId, final @NotNull List<Pair<JsonPointer, Value>> updates)
    {
        if (updates.isEmpty())
        {
            return;
        }
        
        final MinecraftClient  client = MinecraftClient.getInstance();
        final IntegratedServer server = client.getServer();
        
        if (server == null)
        {
            Novia.LOGGER.warn("Tried sending client updates for provider '{}' in remote or invalid session", configId);
            return;
        }
        
        this.updateRollingCache(configId, updates);
        client.execute(() -> ProviderClientUpdater.forceUpdateClient(this.providers.get(configId), updates, false));
        
        if (server.getCurrentPlayerCount() < 2)
        {
            return;
        }
        
        final List<ServerPlayerEntity> players = PlayerLookup
            .all(server)
            .stream()
            .filter(player -> !server.isHost(player.getGameProfile()))
            .toList();
        this.sendPacket(configId, players, updates);
    }
    
    @Override
    public @Nullable ProviderOverride getRegistryOverride(final @NotNull Identifier providerId)
    {
        final MinecraftClient client = MinecraftClient.getInstance();
        return (client.world != null ? ConfigManager.REGISTRY.getOverride(client.world, providerId) : null);
    }
    
    //==================================================================================================================
    @Override
    protected void processConfigTask(final @NotNull ServerConfigurationNetworkHandler handler,
                                     final @NotNull MinecraftServer                   server)
    {
        if (server.isHost(handler.getDebugProfile()))
        {
            return;
        }
        
        super.processConfigTask(handler, server);
    }
    
    @Override
    protected void processRollingUpdates(final @NotNull ServerPlayNetworkHandler handler,
                                         final @NotNull PacketSender             sender,
                                         final @NotNull MinecraftServer          server)
    {
        if (server.isHost(handler.player.getGameProfile()))
        {
            return;
        }
        
        super.processRollingUpdates(handler, sender, server);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void processPlaySyncPacket(final @NotNull CustomPayload                payload,
                                       final @NotNull ClientPlayNetworking.Context ctx)
    {
        //noinspection resource
        ctx.client().execute(() ->
        {
            final IPlayS2CUpdateConfigPayload sync_payload = (IPlayS2CUpdateConfigPayload) payload;

            if (this.phase != RemotePhase.PLAY)
            {
                this.pending.addLast(sync_payload);
                return;
            }
            
            this.processPlaySyncPacketImpl(sync_payload);
        });
    }
    
    private void processPlaySyncPacketImpl(final @NotNull IPlayS2CUpdateConfigPayload payload)
    {
        final BaseNetworkProvider<?> provider = this.providers.get(payload.getProviderId());
        
        if (provider == null)
        {
            return;
        }
        
        ProviderClientUpdater.forceUpdateClient(provider, payload.getUpdates(), true);
    }
    
    //==================================================================================================================
    @Override
    public void receive(final @NotNull ConfigS2CConfigSyncPayload            payload,
                        final @NotNull ClientConfigurationNetworking.Context context)
    {
        if (this.phase != RemotePhase.CONFIGURATION)
        {
            return;
        }
        
        if (this.readIncomingBuffer(payload.byteBuf()) > 0)
        {
            return;
        }
        
        final Map<Identifier, Pair<Boolean, List<SnapshotCache.Snapshot>>> origin_flat = new HashMap<>();
        final int namespace_count = this.configBuf.readVarInt();
        
        for (int i = 0; i < namespace_count; i++)
        {
            final int    config_count = this.configBuf.readVarInt();
            final String namespace    = this.configBuf.readString();
            
            for (int j = 0; j < config_count; j++)
            {
                final int     value_count = this.configBuf.readVarInt();
                final String  config_path = this.configBuf.readString();
                final boolean optional    = this.configBuf.readBoolean();
                
                final List<SnapshotCache.Snapshot> snapshots = new ArrayList<>();
                origin_flat.put(Identifier.of(namespace, config_path), Pair.of(optional, snapshots));
                
                for (int k = 0; k < value_count; k++)
                {
                    snapshots.add(SnapshotCache.Snapshot.PACKET_CODEC.decode(this.configBuf));
                }
            }
        }
        
        this.resetBuffers();
        
        //noinspection resource
        context.client()
            .submit(() ->
            {
                try
                {
                    this.processConfigSync(origin_flat);
                }
                catch (final ConfigSyncException ex)
                {
                    throw new CompletionException(ex);
                }
            })
            .whenComplete((result, throwable) ->
            {
                if (throwable != null)
                {
                    Novia.LOGGER.error("configuration sync failed", throwable);
                    
                    //noinspection resource
                    context.client().execute(() -> context
                        .responseSender()
                        .disconnect(getDisconnectionMessage(throwable)));
                    
                    return;
                }
                
                context.responseSender().sendPacket(new ConfigC2SConfigAcknowledgePayload());
            });
    }
    
    @Override
    public void receive(final @NotNull PlayS2CConfigSyncPayload     payload,
                        final @NotNull ClientPlayNetworking.Context context)
    {
        if (this.phase != RemotePhase.PLAY_INIT)
        {
            return;
        }
        
        if (readIncomingBuffer(payload.byteBuf()) > 0)
        {
            return;
        }
        
        final SnapshotCache cache = SnapshotCache.PACKET_CODEC.decode(this.configBuf);
        this.resetBuffers();
        
        final Map<Identifier, List<Pair<JsonPointer, Value>>> client_updates = transformCache(cache);
        
        //noinspection resource
        context.client()
            .submit(() -> client_updates.forEach((id, updates) ->
            {
                final BaseNetworkProvider<?> provider = this.providers.get(id);
                
                if (provider != null)
                {
                    ProviderClientUpdater.forceUpdateClient(provider, updates, true);
                }
            }))
            .whenComplete((result, throwable) -> context.client().execute(() ->
            {
                final Object2ObjectMap<Identifier, List<Pair<JsonPointer, Value>>> updates
                    = new Object2ObjectOpenHashMap<>();
                IPlayS2CUpdateConfigPayload next;
                
                while ((next = this.pending.poll()) != null)
                {
                    final List<Pair<JsonPointer, Value>> list = updates.computeIfAbsent(
                        next.getProviderId(),
                        (k -> new ArrayList<>()));
                    list.addAll(next.getUpdates());
                }
                
                updates.forEach((id, list) ->
                {
                    final BaseNetworkProvider<?> provider = this.providers.get(id);
        
                    if (provider != null)
                    {
                        ProviderClientUpdater.forceUpdateClient(provider, list, true);
                    }
                });
                
                this.phase = RemotePhase.PLAY;
            }));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private int readIncomingBuffer(final @NotNull PacketByteBuf buf)
    {
        final int bytes_to_read = buf.readableBytes();

        if (bytes_to_read > 0)
        {
            this.configBuf.writeBytes(buf, bytes_to_read);
        }
        
        return bytes_to_read;
    }
    
    private @NotNull Text getDisconnectionMessage(final @NotNull Throwable throwable)
    {
        if (throwable instanceof CompletionException)
        {
            return getDisconnectionMessage(throwable.getCause());
        }
        else if (throwable instanceof ConfigSyncException ex)
        {
            return Text.of(String.format("Config providers could not by synced: %s", ex.getMessage()));
        }
        
        return Text.of(String.format(
            "Unexpected problem during config provider synchronisation: %s",
            throwable.getMessage()));
    }
    
    //==================================================================================================================
    private void processConfigSync(final @NotNull Map<Identifier, Pair<Boolean, List<SnapshotCache.Snapshot>>> origin)
        throws ConfigSyncException
    {
        final Map<Identifier, BaseNetworkProvider<?>> temp_providers = new HashMap<>(this.providers);
   
        for (final var entry : origin.entrySet())
        {
            final BaseNetworkProvider<?> provider = temp_providers.remove(entry.getKey());
            final boolean                optional = entry.getValue().first();
            
            if (provider == null)
            {
                if (!optional)
                {
                    throw new ConfigSyncException(String.format(
                        "mismatching config provider '%s', not supported by the client",
                        entry.getKey()));
                }
                
                continue;
            }
            
            if (optional != provider.isOptional())
            {
                throw new ConfigSyncException(String.format(
                    "mismatching config provider '%s', specification out of sync",
                    entry.getKey()));
            }
            
            final List<Pair<JsonPointer, Value>>    updates        = new ArrayList<>();
            final Map<JsonPointer, PropertySpec<?>> property_specs = new HashMap<>(provider
                .getSpec()
                .flatPropertySpecs());
            
            for (final var snapshot : entry.getValue().second())
            {
                final PropertySpec<?> property_spec = property_specs.remove(snapshot.pointer());
                
                if (property_spec == null)
                {
                    throw new ConfigSyncException("mismatching config provider '%s', specification out of sync"
                        .formatted(entry.getKey()));
                }
                
                updates.add(Pair.of(snapshot.pointer(), snapshot.value()));
            }
            
            if (!property_specs.isEmpty())
            {
                throw new ConfigSyncException("mismatching config provider '%s', specification out of sync"
                    .formatted(entry.getKey()));
            }
            
            try
            {
                ProviderClientUpdater.updateClient(provider, updates, true);
            }
            catch (final PropertyValidationException ex)
            {
                throw new ConfigSyncException("mismatching config provider '%s', specification out of sync"
                    .formatted(entry.getKey()), ex);
            }
        }
        
        for (final var entry : temp_providers.entrySet())
        {
            if (!entry.getValue().isOptional())
            {
                throw new ConfigSyncException("mismatching config provider '%s', not supported by the server"
                    .formatted(entry.getKey()));
            }
        }
    }
    
    // we are resetting the providers in a single player setting, because we might have been on a previously synced
    // remote
    // since the server container is untouched on a remote session, we just copy back the data from the server container
    // to the client container, as this is the one that is always up-to-date
    private void resetProviders(final @NotNull MinecraftClient client)
    {
        final Map<Identifier, BaseNetworkProvider<?>> filtered = this.providers
            .entrySet()
            .stream()
            .filter(e -> e.getValue().isRemote())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        @SuppressWarnings("DataFlowIssue")
        final Map<Identifier, List<Pair<JsonPointer, Value>>> temp_cache = filtered
            .entrySet()
            .stream()
            .collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                (e -> e.getValue()
                    .getManagedProperties()
                    .stream()
                    .map(prop -> Pair.of(Objects.requireNonNull(prop.getSpec()).pointer(), prop.getValue()))
                    .collect(Collectors.toList()))));
        
        client.execute(() -> filtered.forEach((id, provider) -> ProviderClientUpdater.forceUpdateClient(
            provider,
            Objects.requireNonNull(temp_cache.remove(id)),
            false)));
        
        this.phase = null;
    }
    
    //==================================================================================================================
    private void resetBuffers()
    {
        if (this.configBuf == null)
        {
            return;
        }
        
        this.configBuf.release();
        this.configBuf = null;
    }
    
    //==================================================================================================================
    private @NotNull Map<Identifier, List<Pair<JsonPointer, Value>>> transformCache(final @NotNull SnapshotCache cache)
    {
        return cache
            .namespaces()
            .entrySet()
            .stream()
            .flatMap(ns_e -> ns_e.getValue()
                .stream()
                .map(prov_e -> Pair.of(
                    Identifier.of(ns_e.getKey(), prov_e.path()),
                    prov_e.snapshots()
                        .stream()
                        .map(prop_e -> Pair.of(prop_e.pointer(), prop_e.value()))
                        .toList())))
            .collect(Collectors.toMap(Pair::first, Pair::second));
    }
}

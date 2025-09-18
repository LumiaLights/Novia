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
package xyz.lumialights.novia.api.config.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.config.provider.IConfigProvider;
import xyz.lumialights.novia.api.config.spec.PropertySpec;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.JsonPointer;

import java.util.*;



//**********************************************************************************************************************
@ApiStatus.Internal
public record SnapshotCache(@NotNull Map<String, List<SnapshotGroup>> namespaces)
{
    //******************************************************************************************************************
    public record Snapshot(@NotNull JsonPointer pointer, @NotNull Value value)
    {
        //**************************************************************************************************************
        public static final PacketCodec<PacketByteBuf, Snapshot> PACKET_CODEC = PacketCodec
            .tuple(
                JsonPointer.PACKET_CODEC, Snapshot::pointer,
                Value      .PACKET_CODEC, Snapshot::value,
                Snapshot::new);

        //**************************************************************************************************************
        @SuppressWarnings("unchecked")
        static <Container> @NotNull Snapshot from(@NotNull final Object                  container,
                                                  @NotNull final PropertySpec<Container> spec)
        {
            return new Snapshot(spec.pointer(), spec.get((Container) container).getValue());
        }

        //==============================================================================================================
        public @NotNull Snapshot withValue(@NotNull final Value value)
        {
            return new Snapshot(this.pointer, value);
        }
    }

    public record SnapshotGroup(@NotNull String path, List<Snapshot> snapshots)
    {
        //**************************************************************************************************************
        public static final PacketCodec<PacketByteBuf, SnapshotGroup> PACKET_CODEC = PacketCodec
            .tuple(
                PacketCodecs.STRING,                                  SnapshotGroup::path,
                Snapshot.PACKET_CODEC.collect(PacketCodecs.toList()), SnapshotGroup::snapshots,
                SnapshotGroup::new);

        //**************************************************************************************************************
        static @NotNull SnapshotGroup from(@NotNull final Identifier id, @NotNull final IConfigProvider<?> provider)
        {
            return new SnapshotGroup(
                id.getPath(),
                provider.getSpec().flatPropertySpecs().values()
                    .stream()
                    .map(spec -> Snapshot.from(provider.getManagedContainer(), spec))
                    .collect(ImmutableList.toImmutableList()));
        }
        
        //==============================================================================================================
        public @Nullable Snapshot findSnapshot(@NotNull final JsonPointer pointer)
        {
            final Optional<Snapshot> opt_snapshot = this.snapshots
                .stream()
                .filter(snap -> snap.pointer.equals(pointer))
                .findFirst();
            
            return opt_snapshot.orElse(null);
        }
        
        public void setSnapshot(@NotNull final JsonPointer pointer, @NotNull final Value newValue)
        {
            for (int i = 0; i < this.snapshots.size(); ++i)
            {
                final Snapshot snapshot = this.snapshots.get(i);
                
                if (snapshot.pointer.equals(pointer))
                {
                    this.snapshots.set(i, snapshot.withValue(newValue));
                }
            }
        }
    }

    //******************************************************************************************************************
    public static final PacketCodec<PacketByteBuf, SnapshotCache> PACKET_CODEC = PacketCodec
        .tuple(
            PacketCodecs.map(
                LinkedHashMap::new, PacketCodecs.STRING,
                SnapshotGroup.PACKET_CODEC.collect(PacketCodecs.toList())),
            SnapshotCache::namespaces,
            SnapshotCache::new);

    //******************************************************************************************************************
    public static @NotNull SnapshotCache from(@NotNull final Map<Identifier, IConfigProvider<?>> registry)
    {
        final Map<String, List<SnapshotGroup>> result = new LinkedHashMap<>();
        registry.keySet()
            .stream()
            .map(Identifier::getNamespace)
            .distinct()
            .forEach(namespace ->
                result.put(
                    namespace,
                    registry.entrySet()
                        .stream()
                        .filter(e -> e.getKey().getNamespace().equals(namespace))
                        .map(e -> SnapshotGroup.from(e.getKey(), e.getValue()))
                        .collect(ImmutableList.toImmutableList())));
        return new SnapshotCache(ImmutableMap.copyOf(result));
    }

    //******************************************************************************************************************
    public @Nullable SnapshotGroup getGroup(@NotNull final Identifier id)
    {
        final List<SnapshotGroup> groups = this.namespaces.get(id.getNamespace());
        
        if (groups == null)
        {
            return null;
        }
        
        for (final var group : groups)
        {
            if (group.path.equals(id.getPath()))
            {
                return group;
            }
        }

        return null;
    }
    
    public @Nullable Snapshot getSnapshot(@NotNull final Identifier id, @NotNull final JsonPointer pointer)
    {
        final SnapshotGroup group = this.getGroup(id);
        
        if (group == null)
        {
            return null;
        }
        
        for (final Snapshot snapshot : group.snapshots)
        {
            if (snapshot.pointer.equals(pointer))
            {
                return snapshot;
            }
        }

        return null;
    }
    
    public @Nullable Snapshot getSnapshot(@NotNull final PropertyId propertyId)
    {
        return getSnapshot(propertyId.providerId(), propertyId.pointer());
    }
    
    //==================================================================================================================
    public boolean setSnapshot(@NotNull final Identifier  id,
                               @NotNull final JsonPointer pointer,
                               @NotNull final Value       value)
    {
        final SnapshotGroup group = this.getGroup(id);
        
        if (group == null)
        {
            return false;
        }
        
        for (int i = 0; i < group.snapshots.size(); ++i)
        {
            final Snapshot snapshot = group.snapshots.get(i);
            
            if (snapshot.pointer.equals(pointer))
            {
                group.snapshots.set(i, snapshot.withValue(value));
                return true;
            }
        }

        return false;
    }
    
    public boolean setSnapshot(@NotNull final PropertyId propertyId, @NotNull final Value value)
    {
        return this.setSnapshot(propertyId.providerId(), propertyId.pointer(), value);
    }
}

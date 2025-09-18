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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.PropertyValidationException;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.core.util.Pair;

import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public abstract class BaseNetworkProvider<Container>
    implements IConfigProvider<Container>
{
    //******************************************************************************************************************
    private final Identifier    id;
    private final boolean       optional;
    private final DynamicOps<?> ops;
    
    private boolean isRemote = false;
    
    //******************************************************************************************************************
    public BaseNetworkProvider(@NotNull final Identifier id, final boolean optional, @NotNull final DynamicOps<?> ops)
    {
        this.id       = id;
        this.optional = optional;
        this.ops      = ops;
    }
    
    //==================================================================================================================
    /**
     * Gets the client container managed by this config provider.
     * @return The instance of the client container
     */
    public abstract @Nullable Container getClientContainer();
    
    @Override public @NotNull Identifier    getId()  { return this.id; }
    @Override public @NotNull DynamicOps<?> getOps() { return this.ops; }
    
    /**
     * Returns a list of all properties that is managed by the client container.
     * @return A list of client container properties
     */
    public @NotNull List<Property> getClientProperties()
    {
        final Container client = getClientContainer();
        
        if (client == null)
        {
            return new ArrayList<>();
        }
        
        return getSpec().flatPropertySpecs()
            .values()
            .stream()
            .map(propertySpec -> propertySpec.get(client))
            .collect(Collectors.toList());
    }
    
    //==================================================================================================================
    protected void setRemote(final boolean isRemote)
    {
        this.isRemote = isRemote;
    }
    
    //==================================================================================================================
    @Override
    public final boolean isSynced() { return true; }
    
    /**
     * Determines whether this config provider is an optional provider.
     * <p>
     * A non-optional provider determines that a network provider needs to be available on the client,
     * and the server side. If this is not the case, the client is unable to join.
     * @return True if this provider is optional
     */
    public boolean isOptional() { return optional; }
    
    /**
     * Determines whether the current provider is in a state, that is connected to a remote server.
     * <p>
     * If this is called on the physical server, it will always return true.
     * @return Whether if it is remotely synced
     */
    public boolean isRemotelySynced()
    {
        return (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || this.isRemote);
    }
    
    //==================================================================================================================
    abstract void updateClient(@NotNull final List<Pair<JsonPointer, Value>> values, boolean isRemote)
        throws PropertyValidationException;
    
    abstract void forceUpdateClient(@NotNull final List<Pair<JsonPointer, Value>> values, boolean isRemote);
}

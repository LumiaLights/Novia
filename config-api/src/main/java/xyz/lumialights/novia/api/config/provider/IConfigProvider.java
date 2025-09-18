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
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.spec.ConfigSpec;

import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public interface IConfigProvider<Container>
{
    //******************************************************************************************************************
    /** Provides a builder interface that provider sub-implementations can inherit from to build a provider. */
    abstract class Builder<Self, Container, Provider extends IConfigProvider<Container>>
    {
        //**************************************************************************************************************
        protected final ConfigSpec<Container> spec;
        
        protected DynamicOps<?> ops = JsonOps.INSTANCE;
        
        //**************************************************************************************************************
        protected Builder(@NotNull final ConfigSpec<Container> spec)
        {
            Objects.requireNonNull(spec, "spec must not be null");
            this.spec = spec;
        }
        
        //==============================================================================================================
        /**
         * Sets the serialisation back-end for this provider, by that means, the configuration language to be used.
         * By default, this will use {@link JsonOps#INSTANCE}.
         * @param dynOps The dynamic ops instance
         */
        @SuppressWarnings("unchecked")
        public @NotNull Self setBackend(@NotNull final DynamicOps<?> dynOps)
        {
            Objects.requireNonNull(dynOps, "dynamic ops must not be null");
            this.ops = dynOps;
            
            return (Self) this;
        }
        
        //==============================================================================================================
        public abstract @NotNull Provider build(@NotNull final Identifier id);
    }
    
    //******************************************************************************************************************
    /**
     * Gets the internal config specification.
     * @return The {@link ConfigSpec<Container>}
     */
    @NotNull ConfigSpec<Container> getSpec();

    /**
     * Gets the internally managed container.
     * @return The container object
     */
    @NotNull Container getManagedContainer();
    
    /**
     * Returns a list of all properties that is managed by the managed container.
     * @return A list of container properties
     */
    default @NotNull List<Property> getManagedProperties()
    {
        return getSpec().flatPropertySpecs()
            .values()
            .stream()
            .map(propertySpec -> propertySpec.get(getManagedContainer()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets the ID of this provider.
     * @return The ID of this provider
     */
    @NotNull Identifier getId();
    
    /**
     * Gets the dynamic ops instance associated with this provider.
     * @return The dynamic ops instance
     */
    @NotNull DynamicOps<?> getOps();
    
    //==================================================================================================================
    /**
     * Returns true if the provider needs to be synced with the client.
     * @return True if the provider is synced
     */
    boolean isSynced();
}

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
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.ConfigManager;
import xyz.lumialights.novia.api.config.spec.ConfigSpec;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.core.serialisation.dynops.ConfigSerialisationException;

import java.nio.file.Path;



//**********************************************************************************************************************
public class StandaloneProvider<Container>
    implements IConfigProvider<Container>
{
    //******************************************************************************************************************
    public static class Builder<Container>
        extends IConfigProvider.Builder<Builder<Container>, Container, StandaloneProvider<Container>>
    {
        //**************************************************************************************************************
        protected Builder(@NotNull final ConfigSpec<Container> spec)
        {
            super(spec);
        }
        
        //==============================================================================================================
        @Override
        public @NotNull StandaloneProvider<Container> build(@NotNull final Identifier id)
        {
            return new StandaloneProvider<>(this.spec, id, this.ops);
        }
    }
    
    //******************************************************************************************************************
    /**
     * Creates a new {@link StandaloneProvider} builder.
     * @param spec The config specification
     * @return The new builder
     */
    public static <Container> @NotNull Builder<Container> builder(@NotNull final ConfigSpec<Container> spec)
    {
        return new Builder<>(spec);
    }
    
    //******************************************************************************************************************
    private final ConfigSpec<Container> spec;
    private final Identifier            id;
    private final Container             container;
    private final DynamicOps<?>         ops;

    //******************************************************************************************************************
    private StandaloneProvider(@NotNull final ConfigSpec<Container> spec,
                               @NotNull final Identifier            id,
                               @NotNull final DynamicOps<?>         ops)
    {
        this.spec      = spec;
        this.id        = id;
        this.container = spec.create();
        this.ops       = ops;
        
        spec.bindContainer(this.container, null);
    }

    //==================================================================================================================
    @Override
    public @NotNull ConfigSpec<Container> getSpec() { return this.spec; }

    @Override
    public @NotNull Container getManagedContainer() { return this.container; }
    
    @Override
    public @NotNull Identifier getId() { return this.id; }
    
    @Override public @NotNull DynamicOps<?> getOps() { return this.ops; }
    
    //==================================================================================================================
    @Override
    public boolean isSynced() { return false; }
}

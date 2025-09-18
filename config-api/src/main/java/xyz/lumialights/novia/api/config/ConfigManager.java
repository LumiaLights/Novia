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
package xyz.lumialights.novia.api.config;

import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.registry.ProviderOverride;
import xyz.lumialights.novia.api.config.registry.ConfigRegistry;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.config.provider.IConfigProvider;
import xyz.lumialights.novia.api.core.serialisation.dynops.*;
import xyz.lumialights.novia.api.core.serialisation.dynops.DynamicOpsSerialisers;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;



//**********************************************************************************************************************
public final class ConfigManager
{
    //******************************************************************************************************************
    public static final ConfigRegistry REGISTRY;

    //------------------------------------------------------------------------------------------------------------------
    private static final String DEFAULTS_FILE_NAME = "%s.config.%s";
    
    private static ConfigManager INSTANCE = null;
    
    //==================================================================================================================
    static
    {
        REGISTRY = new ConfigRegistry(ConfigApiIds.Registry.CONFIG_OVERRIDE);
    }
    
    //******************************************************************************************************************
    public static @NotNull ConfigManager getInstance()
    {
        if (INSTANCE == null)
        {
            throw new IllegalStateException("Config manager has not yet been initialized");
        }
        
        return INSTANCE;
    }
    
    //******************************************************************************************************************
    private final Path           configDir;
    private final IConfigHandler handler;
    
    //******************************************************************************************************************
    public ConfigManager(@NotNull final Path configDir, @NotNull final IConfigHandler handler)
    {
        if (INSTANCE != null)
        {
            throw new IllegalStateException("Config manager had already been initialized");
        }
        
        INSTANCE = this;
        
        this.configDir = configDir;
        this.handler   = handler;
        
        REGISTRY
            .freeze()
            .parallelStream()
            .forEach(p -> initialiseProvider(p.first(), p.second()));
        this.handler.onRegistryFrozen(REGISTRY);
        
        System.gc();
    }
    
    //==================================================================================================================
    /**
     * Gets the configurations base dir.
     * @return The config directory path
     */
    public @NotNull Path getConfigDir() { return this.configDir; }
    
    /**
     * Gets the file path for the given config ID.
     * @param id The ID of the configuration
     * @return The file path
     */
    public @NotNull Path getFileForId(@NotNull final Identifier id)
    {
        final IConfigProvider<?> provider = REGISTRY.get(id);
        return this.getFileForId(id, provider);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private @NotNull Path getFileForId(@NotNull final Identifier id, @Nullable final IConfigProvider<?> provider)
    {
        return this.configDir.resolve(id.getNamespace()).resolve(id.getPath() + '.' + getFileExtension(provider));
    }
    
    private @NotNull String getFileExtension(@Nullable final IConfigProvider<?> provider)
    {
        String ext = "txt";
        
        if (provider != null)
        {
            final IDynamicOpsSerialiser<?> serialiser = DynamicOpsSerialisers.get(provider.getOps());
            
            if (serialiser != null)
            {
                ext = serialiser.getFileExtension();
            }
        }
        
        return ext;
    }
    
    //==================================================================================================================
    public @Nullable ProviderOverride getProviderOverride(@NotNull final Identifier providerId)
    {
        return this.handler.getRegistryOverride(providerId);
    }
    
    //==================================================================================================================
    private <Container> void initialiseProvider(@NotNull final Identifier                 id,
                                                @NotNull final IConfigProvider<Container> provider)
    {
        final Path file = getFileForId(id, provider);
        
        if (!Files.exists(file))
        {
            final String template = DEFAULTS_FILE_NAME.formatted(
                id.toTranslationKey().replace('/', '.'),
                this.getFileExtension(provider));
            
            try
            {
                Files.createDirectories(file.getParent());
                
                final URL template_url = this.getClass().getClassLoader().getResource(template);
                
                if (template_url != null)
                {
                    IOUtils.copy(template_url, file.toFile());
                    return;
                }
                
                Novia.LOGGER.debug(
                    "Could not find configuration template '{}' for '{}', generating default configuration file",
                    template, id);
            }
            catch (final IOException ex)
            {
                Novia.LOGGER.error(
                    "Could not copy configuration template '{}' for '{}')",
                    template, id, ex);
            }
            
            try
            {
                provider.getSpec().write(provider.getOps(), file, provider.getManagedContainer());
            }
            catch (final ConfigSerialisationException ex)
            {
                Novia.LOGGER.error(
                    "Could not save default configuration file '{}' for '{}'",
                    file, id, ex);
            }
        }
        
        try
        {
            provider.getSpec().read(provider.getOps(), file, provider.getManagedContainer());
        }
        catch (final ConfigSerialisationException ex)
        {
            Novia.LOGGER.error(
                "Could not read configuration file '{}' for '{}'",
                file, id, ex);
        }
    }
    
    //==================================================================================================================
    void sendUpdates(@NotNull final Identifier id, @NotNull final List<Pair<JsonPointer, Value>> updates)
    {
        if (!Novia.getInstance().isServerThread())
        {
            Novia.LOGGER.debug("Tried updating server provider '{}' on logical client", id);
            return;
        }
        
        this.handler.sendUpdates(id, updates);
    }
}

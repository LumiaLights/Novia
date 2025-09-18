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
package xyz.lumialights.novia.api.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.mutual.LogicalSide;

import java.nio.file.Path;
import java.util.*;



//**********************************************************************************************************************
public class Novia
    extends BaseEnvironmentHandler
    implements ModInitializer
{
    //******************************************************************************************************************
    public static final Logger LOGGER = LogManager.getLogger();
    
    //------------------------------------------------------------------------------------------------------------------
    private static Novia INSTANCE = null;

    //******************************************************************************************************************
    public static @NotNull Novia getInstance()
    {
        if (INSTANCE == null)
        {
            throw new InternalModException("Novia environment handler has not yet been initialised");
        }

        return INSTANCE;
    }

    //******************************************************************************************************************
    private final Path                   dirMods;
    private final BaseEnvironmentHandler handler;
    
    //******************************************************************************************************************
    public Novia()
    {
        if (INSTANCE != null)
        {
            throw new UnsupportedOperationException("Novia mutual handler had already been initialised");
        }
        
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
        {
            this.handler = getHandler(ClientModInitializer.class);
        }
        else
        {
            this.handler = getHandler(DedicatedServerModInitializer.class);
        }
        
        this.dirMods = FabricLoader.getInstance().getGameDir().resolve("mods");
        
        INSTANCE = this;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> @NotNull BaseEnvironmentHandler getHandler(@NotNull final Class<T> initializerClass)
    {
        final String                          environment_name
            = FabricLoader.getInstance().getEnvironmentType().toString().toLowerCase();
        final List<EntrypointContainer<T>>    initializers
            = FabricLoader.getInstance().getEntrypointContainers(environment_name, initializerClass);
        
        return (BaseEnvironmentHandler) initializers
            .stream()
            .filter(init -> init.getProvider().getMetadata().getId().equals(ApiDefine.API_ID))
            .findFirst()
            .orElseThrow()
            .getEntrypoint();
    }
    
    //==================================================================================================================
    @Override
    public void onInitialize()
    {
        ServerLifecycleEvents.SERVER_STARTING.register(this::initServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::shutdownServer);

        CoreApiBootstrap.init();
    }
    
    //==================================================================================================================
    /**
     * Gets the path to the current mod directory.
     * @return The mods directory
     */
    public @NotNull Path getModsFolder() { return this.dirMods; }
    
    @Override public @Nullable MinecraftServer getServer()     { return this.handler.getServer(); }
    @Override public @NotNull  Path            getDataFolder() { return this.handler.getDataFolder(); }
    
    //==================================================================================================================
    @Override
    public void initServer(@Nullable final MinecraftServer server)
    {
        this.handler.initServer(server);
    }

    @Override
    public void shutdownServer(@Nullable final MinecraftServer server)
    {
        this.handler.shutdownServer(server);
    }

    //==================================================================================================================
    @Override
    public boolean isRenderThread() { return this.handler.isRenderThread(); }

    @Override
    public boolean isServerThread() { return this.handler.isServerThread(); }

    //==================================================================================================================
    @Override
    public @Nullable LogicalSide guessLogicalSide() { return this.handler.guessLogicalSide(); }
}

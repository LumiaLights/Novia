package xyz.lumialights.novia.api.core.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.BaseEnvironmentHandler;
import xyz.lumialights.novia.api.core.mutual.LogicalSide;

import java.nio.file.Path;



//**********************************************************************************************************************
public final class NoviaClientHandler
    extends BaseEnvironmentHandler
    implements ClientModInitializer
{
    //******************************************************************************************************************
    private static NoviaClientHandler INSTANCE = null;

    //******************************************************************************************************************
    public static @Nullable NoviaClientHandler getInstance() { return INSTANCE; }

    //******************************************************************************************************************
    private final Path serverDataFolder;
    
    private MinecraftServer server = null;

    //******************************************************************************************************************
    public NoviaClientHandler()
    {
        if (NoviaClientHandler.INSTANCE != null)
        {
            throw new IllegalStateException("Handler had already been initialised");
        }

        NoviaClientHandler.INSTANCE = this;
        this.serverDataFolder       = FabricLoader.getInstance().getConfigDir().resolve("novia");
    }

    //==================================================================================================================
    @Override public void onInitializeClient() { CoreApiClientBootstrap.init(); }

    //==================================================================================================================
    @Override public void initServer(@Nullable final MinecraftServer server) { this.server = server; }

    @Override public void shutdownServer(@Nullable MinecraftServer server) { this.server = null; }

    //==================================================================================================================
    @Override public @Nullable MinecraftServer getServer()       { return this.server; }
    @Override public @NotNull  Path            getDataFolder()   { return this.serverDataFolder; }
    @Override public @NotNull  EnvType         getPhysicalSide() { return EnvType.CLIENT; }

    //==================================================================================================================
    @Override public boolean isRenderThread() { return RenderSystem.isOnRenderThread(); }

    //==================================================================================================================
    @Override
    public @Nullable LogicalSide guessLogicalSide()
    {
        return (this.isServerThread() ? LogicalSide.SERVER : (this.isRenderThread() ? LogicalSide.CLIENT : null));
    }
}

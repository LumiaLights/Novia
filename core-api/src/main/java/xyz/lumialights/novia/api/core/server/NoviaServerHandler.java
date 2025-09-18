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
package xyz.lumialights.novia.api.core.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.BaseEnvironmentHandler;

import xyz.lumialights.novia.api.core.mutual.LogicalSide;

import java.nio.file.Path;



//**********************************************************************************************************************
public class NoviaServerHandler
    extends BaseEnvironmentHandler
    implements DedicatedServerModInitializer
{
    //******************************************************************************************************************
    private static NoviaServerHandler INSTANCE = null;

    //******************************************************************************************************************
    public static @Nullable NoviaServerHandler getInstance()
    {
        return INSTANCE;
    }

    //******************************************************************************************************************
    private final Path serverDataFolder;

    private MinecraftServer server = null;

    //******************************************************************************************************************
    public NoviaServerHandler()
    {
        if (INSTANCE != null)
        {
            throw new UnsupportedOperationException("server handler already initialised");
        }

        INSTANCE = this;

        this.serverDataFolder = FabricLoader.getInstance().getConfigDir().resolve("constructeer");
    }

    //==================================================================================================================
    @Override
    public void initServer(@Nullable MinecraftServer server)
    {
        this.server = server;
    }

    @Override
    public void shutdownServer(@Nullable MinecraftServer server)
    {
        this.server = null;
    }

    //==================================================================================================================
    @Override
    public void onInitializeServer() {}

    //==================================================================================================================
    @Override
    public boolean isRenderThread()
    {
        return false;
    }

    @Override
    public boolean isServerThread()
    {
        return (this.server != null && this.server.isOnThread());
    }

    //==================================================================================================================
    @Override
    public @Nullable LogicalSide guessLogicalSide()
    {
        return (isServerThread() ? LogicalSide.SERVER : null);
    }

    //==================================================================================================================
    @Override
    public @Nullable MinecraftServer getServer()
    {
        return server;
    }

    @Override
    public @NotNull Path getDataFolder()
    {
        return this.serverDataFolder;
    }
}

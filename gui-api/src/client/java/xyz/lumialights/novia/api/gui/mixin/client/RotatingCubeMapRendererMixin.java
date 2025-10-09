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
package xyz.lumialights.novia.api.gui.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.impl.RotatingCubeMapRendererExtension;



//**********************************************************************************************************************
@Mixin(RotatingCubeMapRenderer.class)
public abstract class RotatingCubeMapRendererMixin
    implements RotatingCubeMapRendererExtension
{
    //******************************************************************************************************************
    @Invoker("wrapOnce")
    public static float wrapOnce(float a, float b) { throw new AssertionError(); }
    
    //******************************************************************************************************************
    @Shadow
    private float pitch;
    
    @Shadow
    @Final
    private MinecraftClient client;
    
    @Shadow
    @Final
    private CubeMapRenderer cubeMap;
    
    //******************************************************************************************************************
    @Override
    public void novia$renderPositioned(final @NotNull Canvas canvas,
                                       final int x, final int y, final int width, final int height,
                                       boolean rotate)
    {
        if (rotate)
        {
			final float ticks = this.client.getRenderTickCounter().getFixedDeltaTicks();
			final float rot   = (float)(ticks * this.client.options.getPanoramaSpeed().getValue());
			this.pitch        = RotatingCubeMapRendererMixin.wrapOnce(this.pitch + rot * 0.1F, 360.0F);
		}

		this.cubeMap.draw(this.client, 10.0F, -this.pitch);
		canvas.drawTexture(RotatingCubeMapRenderer.OVERLAY_TEXTURE, x, y, width, height, 0, 0, 16, 128, 16, 128,
                           false);
    }
}

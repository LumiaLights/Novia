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
package xyz.lumialights.novia.api.gui.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.Canvas;

import java.util.*;



//**********************************************************************************************************************
public class GuiItemRenderer
{
    //******************************************************************************************************************
    private final MinecraftClient client = MinecraftClient.getInstance();
    
    private KeyedItemRenderState keyedItemRenderState = null;
    private ItemStack            stack                = null;
    private int                  seed                 = 0;
    private boolean              withEntity           = true;
    
    //******************************************************************************************************************
    public GuiItemRenderer(final @Nullable ItemStack stack)
    {
        this.stack = Objects.requireNonNull(stack, "stack must not be null");
        this.updateStack();
    }
    
    public GuiItemRenderer() {}
    
    //==================================================================================================================
    public @NotNull ItemStack getStack() { return this.stack; }
    
    public int getSeed() { return this.seed; }
    
    public boolean shouldDrawWithEntity() { return this.withEntity; }
    
    //==================================================================================================================
    public final void setItem(final @NotNull ItemStack stack)
    {
        if (!this.stack.equals(stack))
        {
            this.stack = Objects.requireNonNull(stack, "stack must not be null");
            this.updateStack();
        }
    }
    
    public final void setSeed(final int seed)
    {
        if (this.seed != seed)
        {
            this.seed = seed;
            this.updateStack();
        }
    }
    
    public final void setDrawWithEntity(final boolean withEntity)
    {
        if (this.withEntity != withEntity)
        {
            this.withEntity = withEntity;
            this.updateStack();
        }
    }
    
    //==================================================================================================================
    public void drawItem(final @NotNull Canvas canvas, final int x, final int y)
    {
        if (this.keyedItemRenderState == null)
        {
            return;
        }
        
        try
        {
            canvas.drawItem(new ItemGuiElementRenderState(
                this.stack.getItem().getName().toString(),
                canvas.getTransform().getMatrix(),
                this.keyedItemRenderState,
                x, y,
                canvas.getClippingRegion().toScreenRect()));
        }
        catch (final Throwable throwable)
        {
            final CrashReport        report  = CrashReport.create(throwable, "Rendering item");
            final CrashReportSection section = report.addElement("Item being rendered");
            
            section.add("Item Type",       (() -> String.valueOf(this.stack.getItem())));
            section.add("Item Components", (() -> String.valueOf(this.stack.getComponents())));
            section.add("Item Foil",       (() -> String.valueOf(this.stack.hasGlint())));
            
            throw new CrashException(report);
        }
    }
    
    public void drawStackOverlay(final @NotNull  Canvas canvas,
                                 final           int    x,
                                 final           int    y,
                                 final @Nullable String stackCountText)
    {
        if (this.stack != null && !this.stack.isEmpty())
        {
            canvas.runWithState(() ->
            {
                this.drawItemBar(canvas, x, y);
                this.drawCooldownProgress(canvas, x, y);
                this.drawStackCount(canvas, x, y, stackCountText);
            });
		}
    }
    
    public void drawItemBar(final @NotNull Canvas canvas, int x, int y)
    {
        if (this.stack.isItemBarVisible())
        {
            x += 2;
            y += 13;
            
            canvas.setColour(-16777216);
			canvas.fill(x, y, (x + 13), (y + 2));
            
            canvas.setColour(ColorHelper.fullAlpha(this.stack.getItemBarColor()));
			canvas.fill(x, y, (x + this.stack.getItemBarStep()), (y + 1));
		}
    }
    
    public void drawCooldownProgress(final @NotNull Canvas canvas, final int x, final int y)
    {
        final ClientPlayerEntity player   = this.client.player;
		final float              cooldown = (player != null
			? player
                .getItemCooldownManager()
                .getCooldownProgress(this.stack, this.client.getRenderTickCounter().getTickProgress(true))
			: 0f);
   
		if (cooldown > 0f)
        {
			final int draw_y = (y + MathHelper.floor(16f * (1f - cooldown)));
			
            canvas.setColour(Integer.MAX_VALUE);
            canvas.fill(x, draw_y, (x + 16), (draw_y + MathHelper.ceil(16f * cooldown)));
		}
    }
    
    public void drawStackCount(final @NotNull Canvas canvas, int x, int y, final @Nullable String stackCountText)
    {
        if (this.stack.getCount() != 1 || stackCountText != null)
        {
			final String text = (stackCountText == null ? String.valueOf(this.stack.getCount()) : stackCountText);
            
            x += (17 - canvas.getFont().getTextWidthFitted(text));
            y += 9;
            
            canvas.setColour(Colour.WHITE);
            canvas.drawText(text, x, y);
		}
    }
    
    //==================================================================================================================
    private void updateStack()
    {
        if (this.stack == null || this.stack.isEmpty())
        {
            this.keyedItemRenderState = null;
            return;
        }
        
        this.keyedItemRenderState = new KeyedItemRenderState();
        this.client.getItemModelManager().clearAndUpdate(
            this.keyedItemRenderState,
            this.stack,
            ItemDisplayContext.GUI,
            this.client.world,
            (this.withEntity ? this.client.player : null),
            this.seed);
    }
}

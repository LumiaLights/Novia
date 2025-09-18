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
package xyz.lumialights.novia.api.gui.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.tooltip.WidgetTooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.IPaletteProvider;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.renderer.GuiTooltipRenderer;

import java.util.*;



//**********************************************************************************************************************
public class TooltipLayer
    extends ScreenLayer
{
    //******************************************************************************************************************
    private GuiComponent      target = null;
    private List<OrderedText> lines;
    private long              delay;
    private long              tooltipTime;
    private boolean           isFocusMode;
    
    //******************************************************************************************************************
    TooltipLayer(final @NotNull ScreenInterop screen) { super(screen); }
    
    //==================================================================================================================
    public @Nullable GuiComponent getTarget() { return target; }
    
    //==================================================================================================================
    public void set(final @Nullable GuiComponent target, final boolean hovered)
    {
        final MinecraftClient mc = MinecraftClient.getInstance();
        
        if (target != null && (hovered || target.isFocused() && mc.getNavigationType().isKeyboard()))
        {
            final Tooltip tooltip = target.getTooltip();
            
            if (tooltip != null)
            {
                this.target      = target;
                this.isFocusMode = !hovered;
                this.lines       = tooltip.getLines(MinecraftClient.getInstance());
                this.delay       = target.getTooltipDelay().toMillis();
                this.tooltipTime = Util.getMeasuringTimeMs();
                
                return;
            }
        }
        
        this.target = null;
    }
    
    public void updateTarget(final @NotNull GuiComponent target)
    {
        Objects.requireNonNull(target, "target must not be null");
        
        if (this.target != target)
        {
            return;
        }
        
        final Tooltip tooltip = target.getTooltip();
        
        if (tooltip == null)
        {
            this.target = null;
            return;
        }
        
        this.lines       = tooltip.getLines(MinecraftClient.getInstance());
        this.delay       = this.target.getTooltipDelay().toMillis();
        this.tooltipTime = Util.getMeasuringTimeMs();
    }
    
    //==================================================================================================================
    @Override public void init() {}
    @Override public void release() {}
    
    @Override public @NotNull IPaletteProvider getPalette() { return this.getLayerTemplate(); }
    
    //==================================================================================================================
    @Override public void resized(final @NotNull Rectangle bounds) {}
    
    //==================================================================================================================
    @Override
    public void render(final @NotNull Canvas canvas)
    {
        if (this.target == null)
        {
            return;
        }
        
        if ((Util.getMeasuringTimeMs() - this.tooltipTime) > this.delay)
        {
            final ScreenRect focus = this.target.getScreenRect();
            final TooltipPositioner positioner = (this.isFocusMode
                ? new FocusedTooltipPositioner(focus)
                : new WidgetTooltipPositioner(focus));
            
            canvas.setLayer(this);
            GuiTooltipRenderer.draw(canvas, this.lines, positioner, canvas.mousePos.x(), canvas.mousePos.y());
        }
    }
    
    //==================================================================================================================
    @Override public void close() { this.screen.close(); }
}

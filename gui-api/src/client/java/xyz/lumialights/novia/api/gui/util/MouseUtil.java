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
package xyz.lumialights.novia.api.gui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.geometry.Point;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;



//**********************************************************************************************************************
public abstract class MouseUtil
{
    //******************************************************************************************************************
    public static @NotNull Point getScaledMousePos()
    {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Mouse           mouse  = client.mouse;
        final Window          window = client.getWindow();
        
        return new Point(
            (int) (mouse.getX() * (double) window.getScaledWidth() / (double) window.getWidth()),
            (int) (mouse.getY() * (double) window.getScaledHeight() / (double) window.getHeight()));
    }
    
    public static @NotNull Point getMousePos()
    {
        final Mouse mouse = MinecraftClient.getInstance().mouse;
        return new Point((int) mouse.getX(), (int) mouse.getY());
    }
    
    public static boolean hitTest(final @NotNull GuiComponent component, final int mouseX, final int mouseY)
    {
        final Rectangle screen_bounds = component.getScreenBounds();
        
        if (!screen_bounds.contains(mouseX, mouseY))
        {
            return false;
        }
        
        return component.hitTest((mouseX - screen_bounds.x()), (mouseY - screen_bounds.y()));
    }
    
    public static boolean hitTest(final @NotNull GuiComponent component, final double mouseX, final double mouseY)
    {
        return MouseUtil.hitTest(component, (int) mouseX, (int) mouseY);
    }
    
    public static boolean hitTest(final @NotNull GuiComponent component, final @NotNull Point mousePos)
    {
        return MouseUtil.hitTest(component, mousePos.x(), mousePos.y());
    }

    //******************************************************************************************************************
    private MouseUtil() {}
}

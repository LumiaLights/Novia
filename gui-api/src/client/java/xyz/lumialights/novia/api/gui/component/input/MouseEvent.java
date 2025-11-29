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
package xyz.lumialights.novia.api.gui.component.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xyz.lumialights.novia.api.gui.geometry.Point;
import xyz.lumialights.novia.api.gui.component.GuiComponent;

import java.util.*;



//**********************************************************************************************************************
/// Provides data about the action of a mouse input event, such as the left mouse button or the mouse-wheel.
/// This is used for component events in the [GuiComponent] class such as for
/// [GuiComponent#onMouseDown(MouseEvent)].
public final class MouseEvent
    extends AbstractInputEvent<MouseEvent>
{
    //******************************************************************************************************************
    /// The mouse device that has triggered this event.
    public final @NotNull Mouse device = MinecraftClient.getInstance().mouse;
    
    /// Determines a change on the left-axis since the last mouse event.
    ///
    /// For scroll events this will determine the horizontal scroll amount, for drag events the change in mouse position
    /// since the last drag event and for any other mouse event 0.
    public final double deltaX;
    
    /// Determines a change on the y-axis since the last mouse event.
    ///
    /// For scroll events this will determine the vertical scroll amount, for drag events the change in mouse position
    /// since the last drag event and for any other mouse event 0.
    public final double deltaY;
    
    /// The mouse position in screen coordinates.
    public final @NotNull Point mousePos;
    
    //------------------------------------------------------------------------------------------------------------------
    private final BitSet buttons = new BitSet();
    
    //******************************************************************************************************************
    /// Constructs a new mouse button event.
    /// @param source   The [GuiComponent] that originally triggered the event
    /// @param mousePos The position of the cursor in screen coordinates
    /// @param deltaX   The amount the position has changed from the last move on the left-axis
    /// @param deltaY   The amount the position has changed from the last move on the y-axis
    /// @param button   The code of the mouse button that has been pressed (see [GLFW])
    public MouseEvent(final @NotNull GuiComponent source,
                      final @NotNull Point        mousePos,
                      final          double       deltaX,
                      final          double       deltaY,
                      final          int          button)
    {
        super(source);
    
        this.mousePos = mousePos;
        this.deltaX   = deltaX;
        this.deltaY   = deltaY;
        
        this.buttons.set(button);
    }

    /// Constructs a new scroll event.
    /// @param source   The [GuiComponent] that originally triggered the event
    /// @param mousePos The position of the cursor in screen coordinates
    /// @param deltaX   The amount the mouse-wheel has been horizontally scrolled since its last position
    /// @param deltaY   The amount the mouse-wheel has been vertically scrolled since its last position
    public MouseEvent(final @NotNull GuiComponent source,
                      final @NotNull Point        mousePos,
                      final          double       deltaX,
                      final          double       deltaY)
    {
        super(source);
    
        this.mousePos = mousePos;
        this.deltaX   = deltaX;
        this.deltaY   = deltaY;
        
        if (device.wasLeftButtonClicked())
        {
            this.buttons.set(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }
        
        if (device.wasRightButtonClicked())
        {
            this.buttons.set(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }
        
        if (device.wasMiddleButtonClicked())
        {
            this.buttons.set(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        }
    }
    
    //==================================================================================================================
    /// {@return the mouse coordinates relative to the [#target()] component}
    public @NotNull Point localMousePos() { return this.target().toRelativePos(this.mousePos); }
    
    /// {@return the X mouse coordinate relative to the [#target()] component}
    public int localMouseX() { return this.target().toRelativeX(this.mousePos.x()); }
    
    /// {@return the Y mouse coordinate relative to the [#target()] component}
    public int localMouseY() { return this.target().toRelativeY(this.mousePos.y()); }
    
    //==================================================================================================================
    /// {@return whether the left mouse button was pressed when the event triggered}
    public boolean isLeftButtonDown() { return this.buttons.get(GLFW.GLFW_MOUSE_BUTTON_LEFT); }
    
    /// {@return whether the right mouse button was pressed when the event triggered}
    public boolean isRightButtonDown() { return this.buttons.get(GLFW.GLFW_MOUSE_BUTTON_RIGHT); }
    
    /// {@return whether the middle mouse button was pressed when the event triggered}
    public boolean isMiddleButtonDown() { return this.buttons.get(GLFW.GLFW_MOUSE_BUTTON_MIDDLE); }
    
    //==================================================================================================================
    /// Disables the cursor. [#enableCursor()] should be called after every call to this one, otherwise the cursor won't
    /// reappear.
    public void disableCursor()
    {
        GLFW.glfwSetInputMode(
            MinecraftClient.getInstance().getWindow().getHandle(),
            GLFW.GLFW_CURSOR,
            GLFW.GLFW_CURSOR_DISABLED);
    }
    
    /// Re-enables the cursor. This should be called after every call to [#disableCursor()], otherwise the cursor won't
    /// reappear.
    public void enableCursor()
    {
        GLFW.glfwSetInputMode(
            MinecraftClient.getInstance().getWindow().getHandle(),
            GLFW.GLFW_CURSOR,
            GLFW.GLFW_CURSOR_NORMAL);
    }
}

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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.canvas.Canvas;



//**********************************************************************************************************************
/// The base class to be used for creating new Minecraft screens.
/// Novia screen classes are not compatible with Minecraft screen and widget classes, however, Novia comes with a handful
/// of useful classes called “components”, which are advanced screen elements that are to gui screens what
/// widgets/elements are to Minecraft [Screen].
///
/// Screens cannot be opened as modal components.
/// @see xyz.lumialights.novia.api.gui.component.provided
public class GuiScreen
    extends GuiComponent
{
    //******************************************************************************************************************
    static GuiScreen CURRENT_SCREEN = null;
    
    //******************************************************************************************************************
    /// Gets the gui screen currently shown.
    /// @return The current screen if it is of type [GuiScreen], otherwise `null`
    public static @Nullable GuiScreen getCurrentScreen() { return GuiScreen.CURRENT_SCREEN; }
    
    //******************************************************************************************************************
    private final ScreenInterop interop;
    
    //******************************************************************************************************************
    /// Constructs a new GuiScreen.
    /// @param title  The title of the screen
    /// @param parent The screen that should be shown on closing, or `null` to close all screens
    public GuiScreen(final @NotNull Text title, final @Nullable Screen parent)
    {
        super(title);
        this.interop = new ScreenInterop(parent, this);
    }
    
    /// Constructs a new GuiScreen.
    /// @param title  The title of the screen
    /// @param parent The screen that should be shown on closing, or `null` to close all screens
    public GuiScreen(final @NotNull Text title, final @Nullable GuiScreen parent)
    {
        this(title, (parent != null ? parent.getMcScreen() : null));
    }
    
    /// Constructs a new GuiScreen with the current shown screen as parent screen.
    /// @param title The title of the screen
    public GuiScreen(final @NotNull Text title) { this(title, MinecraftClient.getInstance().currentScreen); }
    
    //==================================================================================================================
    /// Gets the component currently under the mouse cursor, or when currently dragging,
    /// the component, which started the drag gesture.
    /// @return The [GuiComponent] or `null` if the mouse is outside any hoverable area
    public final @Nullable GuiComponent getHovered() { return this.interop.getHovered(); }
    
    /// Gets the Minecraft screen layer of this GUI screen.
    ///
    /// This should not be used for anything other than API compatibility with Minecraft or other mods,
    /// anything screen and GUI-wise should be done through the [GuiScreen] class and its components.
    /// @return The Minecraft screen layer
    public final @NotNull Screen getMcScreen() { return this.interop; }
    
    /// Gets the music sound, which should be played while this screen is open.
    /// @return The music sound or `null` if no music should be played
    public @Nullable MusicSound getMusic() { return null; }
    
    //==================================================================================================================
    /// Whether any of this screen's components is currently being dragged. To get the component, which is being
    /// dragged, use [#getHovered()].
    /// @return `true` if any of this screen's components is currently being dragged
    public final boolean isInDrag() { return this.interop.isDragging(); }
    
    public final boolean isOpen() { return (GuiScreen.CURRENT_SCREEN == this); }
    
    //------------------------------------------------------------------------------------------------------------------
    /// Disallowing modal promotion for screens because of potential issues with the way screens work.
    /// @return [false]
    @Override public final boolean isModalPromotionAllowed() { return false; }
    
    //==================================================================================================================
    /// Shows the given `component` as a modal on top of all other components on the screen.
    ///
    /// A modal is on top of all other components that are on the screen base-layer, if the component is already part
    /// of the screen, the component will be detached and added as its own modal layer.
    ///
    /// If a component is already open as a modal, nothing will happen, and the result will return
    /// [ModalResult.Code#ALREADY_SHOWING].
    /// @param component The component to open as a modal
    /// @param args      The arguments of how the modal should be treated and behave
    /// @return The result of the modal operation
    /// @throws IllegalArgumentException If the component is a [GuiScreen]
    public final @NotNull ModalResult showModal(final @NotNull GuiComponent component, final @NotNull ModalArgs args)
    {
        if (!component.isModalPromotionAllowed())
        {
            return new ModalResult(ModalResult.Code.NOT_SUPPORTED);
        }
        
        if (GuiScreen.CURRENT_SCREEN != this)
        {
            return new ModalResult(ModalResult.Code.SCREEN_NOT_OPEN);
        }
        
        if (!args.associatedComponent().map(GuiComponent::isDrawing).orElse(true))
        {
            return new ModalResult(ModalResult.Code.ASSOCIATED_MISSING);
        }
        
        final ModalResult result = new ModalResult();
        
        if (!this.interop.addLayer(new ModalLayer(component, this.interop, args, result)))
        {
            return new ModalResult(ModalResult.Code.ALREADY_SHOWING);
        }
        
        return result;
    }
    
    /// If the given component is currently a modal on screen, this will the modal and finish its associated result
    /// object.
    /// @param component The component, that is open as a modal and should be closed
    /// @return `true` if the component was a modal and could be closed, otherwise `false`
    public final boolean hideModal(final @NotNull GuiComponent component)
    {
        if (component instanceof GuiScreen)
        {
            return false;
        }
        
        return this.interop.removeModal(component);
    }
    
    /// If this screen is not currently showing, the old screen will be closed and this one opened.
    /// @return `true` if the screen was not already open, otherwise `false`
    public final boolean showScreen()
    {
        if (GuiScreen.CURRENT_SCREEN != this)
        {
            MinecraftClient.getInstance().setScreen(this.interop);
            return true;
        }
        
        return false;
    }
    
    /// If this screen is currently showing, will close the screen.
    ///
    /// If this screen did not explicitly set the parent screen to null (supplied to constructor), it will re-open the
    /// screen that it was handed upon construction; if no screen was explicitly given in the constructor,
    /// it will be the screen shown during construction.
    /// @return `true` if the screen was open and closed, otherwise `false`
    public final boolean hideScreen()
    {
        if (GuiScreen.CURRENT_SCREEN == this)
        {
            this.interop.close();
            return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    /// Can be overridden to change the background being drawn at the bottom of the screen.
    /// @param canvas The [Canvas]
    protected void drawBackground(final @NotNull Canvas canvas)
    {
        canvas.drawPanorama(0, 0, this.getWidth(), this.getHeight(), true);
        canvas.drawDarkening(0, 0, this.getWidth(), this.getHeight());
    }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        final Text title = this.getMessage();
        return ("GuiScreen#" + (title.equals(Text.EMPTY) ? "Unknown" : title.getString()));
    }
    
    //==================================================================================================================
    /// Can be overridden to let the screen know whenever it has opened.
    protected void onScreenOpened() {}
    
    /// Can be overridden to let the screen know whenever it has closed.
    protected void onScreenClosed() {}
    
    //==================================================================================================================
    @ApiStatus.Internal
    public final void windowFocusChanged()
    {
        if (!MinecraftClient.getInstance().isWindowFocused())
        {
            this.interop.setDragging(false);
            this.interop.setHovered(null);
        }
        else
        {
            this.updateHoverState();
        }
    }
    
    //==================================================================================================================
    /// Updates the current screen's mouse-component hover state. This should usually not be called manually,
    /// but it is public for when an exceptional need arises.
    public final void updateHoverState() { this.interop.updateHoverState(); }
}

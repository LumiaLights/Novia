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
package xyz.lumialights.novia.api.config.client.gui;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.component.provided.NVLabel;



//**********************************************************************************************************************
public interface ConfigScreenLookAndFeel
{
    //******************************************************************************************************************
    @NotNull Rectangle getConfigPanelClientBounds();
    
    /** Specifies the bounds of the tab list. */
    @NotNull Rectangle getTabListBounds();
    
    /** Specifies the bounds of the options list. */
    @NotNull Rectangle getOptionListBounds();

    //==================================================================================================================
    @NotNull GuiFont getDefaultFont();

    //==================================================================================================================
    @NotNull Rectangle getEditPanelClientBounds();
    
    /** Specifies the bounds of the extra panel, which is shown when editing lists, maps or enum values. */
    @NotNull Rectangle getEditPanelListBounds();
    
    //==================================================================================================================
    /**
     * Specifies the bounds of the config screen title. (mod ID)
     * @param text The text that should be drawn to the screen
     */
    @NotNull Rectangle getTitleBounds(@NotNull final Text text, @NotNull final Rectangle clientBounds);
    
    //==================================================================================================================
    /**
     * Creates the title style to display.
     * @param text The text that should be drawn to the screen
     */
    @NotNull NVLabel createTitleWidget(@NotNull final Text text);
    
    //==================================================================================================================
    /**
     * Called when the screen changes bounds, can be used to update local state.
     * @param screenBounds The current bounds of the screen.
     */
    void resize(@NotNull final Rectangle screenBounds);
    
    //==================================================================================================================
    /** Used to draw the background of the config screen. */
    void drawBackground(@NotNull Canvas context);
    
    /** Draws the background of the extra panel screen. */
    void drawEditPanelBackground(@NotNull Canvas context);
}

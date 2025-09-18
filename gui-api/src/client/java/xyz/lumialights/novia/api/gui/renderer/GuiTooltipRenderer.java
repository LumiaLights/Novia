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

import net.minecraft.client.gui.tooltip.*;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;

import java.util.*;



//**********************************************************************************************************************
public final class GuiTooltipRenderer
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void guiTooltipDrawBackground(@NotNull Canvas canvas, @NotNull Rectangle bounds);
        void guiTooltipDrawLines(@NotNull Canvas canvas, int x, int y, @NotNull List<OrderedText> lines);
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_TEXT = ColourId.reserve();
    
    public static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("tooltip/background");
    public static final Identifier FRAME_TEXTURE      = Identifier.ofVanilla("tooltip/frame");
    public static final int        LINE_HEIGHT        = 10;
    
    //==================================================================================================================
    public static void draw(final @NotNull Canvas canvas, final @NotNull Text text, final int x, final int y)
    {
		GuiTooltipRenderer.draw(canvas, Collections.singletonList(text), x, y);
	}
 
	public static void draw(final @NotNull Canvas     canvas,
                            final @NotNull List<Text> lines,
                            final          int        x,
                            final          int        y)
    {
		GuiTooltipRenderer.draw(canvas, lines.stream().map(Text::asOrderedText).toList(),
                                HoveredTooltipPositioner.INSTANCE, x, y);
	}
 
	public static void drawOrdered(final @NotNull  Canvas                      canvas,
                                   final @NotNull  List<? extends OrderedText> text,
                                   final           int                         x,
                                   final           int                         y)
    {
		GuiTooltipRenderer.draw(canvas, new ArrayList<>(text), HoveredTooltipPositioner.INSTANCE, x, y);
	}
    
    public static void draw(final @NotNull  Canvas            canvas,
                            final @NotNull  List<OrderedText> lines,
                            final @NotNull  TooltipPositioner positioner,
                            final           int               x,
                            final           int               y)
    {
        final GuiFont font = canvas.getFont();
     
        int width  = 0;
        int height = (lines.size() == 1 ? -2 : 0);

        for (final var line : lines)
        {
            final int k = font.getWidthFitted(line);
            
            if (k > width)
            {
                width = k;
            }

            height += GuiTooltipRenderer.LINE_HEIGHT;
        }
        
        final Vector2ic pos_vec = positioner.getPosition(canvas.getScaledWindowWidth(), canvas.getScaledWindowHeight(),
                                                         x, y, width, height);
        
        final IGuiTemplate template = canvas.getTemplate();
        template.guiTooltipDrawBackground(canvas, new Rectangle(pos_vec.x(), pos_vec.y(), width, height));
        template.guiTooltipDrawLines(canvas, pos_vec.x(), pos_vec.y(), lines);
    }
    
    //******************************************************************************************************************
    private GuiTooltipRenderer() {}
}

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

import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.GuiApiLang;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.GuiScreen;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumialights.novia.api.gui.component.provided.NVLabelButton;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.component.provided.NVLabel;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;
import java.util.function.BiConsumer;



//**********************************************************************************************************************
public class EditScreen
    extends GuiScreen
{
    //******************************************************************************************************************
    private record Content(@NotNull IStatefulComponent<?> content) {}
    
    private record StackFrame(@NotNull Text title, @NotNull Content content) {}
    
    //******************************************************************************************************************
    private final NVLabel                       titleLabel;
    private final NVLabelButton                 backButton;
    private final ConfigScreenLookAndFeel       lookAndFeel;
    private final BiConsumer<PropertyId, Value> valueChangeHandler;
    private final Deque<StackFrame>             contentStack = new LinkedList<>();
    
    private Content    content    = null;
    private PropertyId propertyId = null;
    
    //******************************************************************************************************************
    public EditScreen(final @NotNull ConfigScreenLookAndFeel       lookAndFeel,
                      final @NotNull BiConsumer<PropertyId, Value> valueChangeHandler)
    {
        super(ScreenTexts.EMPTY);
        
        this.lookAndFeel        = lookAndFeel;
        this.valueChangeHandler = valueChangeHandler;
        
        this.titleLabel = new NVLabel(ScreenTexts.EMPTY);
        this.titleLabel.textAlign.set(Alignment.MIDDLE_CENTRE);
        this.titleLabel.setColour(NVLabel.COLOUR_TEXT, 0xFF333333);
        
        this.backButton = new NVLabelButton((button -> this.popFrame()), GuiApiLang.GUI_GO_BACK_BUTTON);
        this.backButton.setColour(NVLabel.COLOUR_TEXT, 0xFF333333);
    }
    
    //==================================================================================================================
    void setContent(final @Nullable PropertyId            propertyId,
                    final @NotNull  Text                  title,
                    final @NotNull  IStatefulComponent<?> content)
    {
        if (this.content != null)
        {
            this.pushFrame(this.titleLabel.getMessage(), this.content.content);
            return;
        }
        
        this.propertyId = propertyId;
        this.contentStack.clear();
        this.updateContent(title, content);
        this.showScreen();
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void pushFrame(final @NotNull Text title, final @NotNull IStatefulComponent<?> content)
    {
        this.contentStack.addLast(new StackFrame(this.titleLabel.getMessage(), this.content));
        this.updateContent(title, content);
    }
    
    private void popFrame()
    {
        if (this.contentStack.size() < 2)
        {
            this.hideScreen();
            return;
        }
        
        final StackFrame frame = this.contentStack.pollLast();
        this.updateContent(frame.title, frame.content.content);
    }
    
    //==================================================================================================================
    @Override
    protected void resized()
    {
        this.lookAndFeel.resize(this.getBounds());
        this.titleLabel.setBounds(this.lookAndFeel.getTitleBounds(
            this.getMessage(),
            this.lookAndFeel.getEditPanelClientBounds()));
        
        if (this.content != null)
        {
            ((GuiComponent) this.content.content).setBounds(this.lookAndFeel.getEditPanelListBounds());
        }
    }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        this.lookAndFeel.drawEditPanelBackground(canvas);
    }
    
    //==================================================================================================================
    private void updateContent(final @NotNull Text title, final @NotNull IStatefulComponent<?> content)
    {
        this.content = new Content(content);
        this.titleLabel.setMessage(title);
        
        this.removeAllChildren();
        this.addChild(this.titleLabel);
        this.addChild(this.backButton);
        this.addChild((GuiComponent) this.content.content);
        this.resized();
    }
    
    //==================================================================================================================
    private void reset()
    {
        this.removeAllChildren();
        this.contentStack.clear();
        this.content    = null;
        this.propertyId = null;
    }
    
    //==================================================================================================================
    @Override
    protected void onScreenClosed()
    {
        final Content root = (!this.contentStack.isEmpty()
            ? this.contentStack.getFirst().content()
            : this.content);
        this.valueChangeHandler.accept(this.propertyId, root.content.getValue());
        this.reset();
    }
}

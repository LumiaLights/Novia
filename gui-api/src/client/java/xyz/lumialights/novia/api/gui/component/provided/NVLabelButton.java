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
package xyz.lumialights.novia.api.gui.component.provided;

import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.util.*;
import java.util.stream.Stream;


//**********************************************************************************************************************
/** An {@link NVAbstractButton} implementation that can render a given text. */
public class NVLabelButton
    extends NVAbstractButton<NVLabelButton>
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void nvLabelButtonDrawBackground(@NotNull Canvas canvas, @NotNull NVLabelButton button);
        
        void nvLabelButtonDrawText(@NotNull Canvas canvas, @NotNull NVLabelButton button);
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_TEXT          = ColourId.reserve();
    public static final ColourId COLOUR_TEXT_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    public static final Alignment DEFAULT_TEXT_ALIGNMENT = Alignment.MIDDLE_CENTRE;
    
    //==================================================================================================================
    public static final Text DEFAULT_TEXT = Text.literal("LabelButton");
    
    //******************************************************************************************************************
    /** Describes the alignment of the text inside the component bounds. */
    public final GuiProperty.NonNull<Alignment> textAlign;
    
    //------------------------------------------------------------------------------------------------------------------
    private Text text;
    
    //******************************************************************************************************************
    /**
     * Constructs a new button with the given action and label.
     * @param action  The {@link ActionListener}
     * @param label   The text drawn onto the button
     * @param message The message and the initial text on the button
     */
    public NVLabelButton(final @NotNull ActionListener<NVLabelButton> action,
                         final @NotNull Text                          label,
                         final @NotNull Text                          message)
    {
        super(action, message);
        
        this.textAlign = GuiProperty.nonNull(NVLabelButton.DEFAULT_TEXT_ALIGNMENT);
        this.text      = Objects.requireNonNull(label, "label must not be null");
    }
    
    /**
     * Constructs a new button with the given action and label.
     * @param action The action when clicking the button
     * @param label   The text drawn onto the button
     */
    public NVLabelButton(final @NotNull ActionListener<NVLabelButton> action, final @NotNull Text label)
    {
        this(action, label, ScreenTexts.EMPTY);
    }
    
    /**
     * Constructs a new button with the given action and no text.
     * @param action The action when clicking the button
     */
    public NVLabelButton(final @NotNull ActionListener<NVLabelButton> action)
    {
        this(action, NVLabelButton.DEFAULT_TEXT, ScreenTexts.EMPTY);
    }
    
    /**
     * Constructs a new button with the given text and no action.
     * @param label The text drawn onto the button
     */
    public NVLabelButton(final @NotNull Text label) { this((t -> {}), label, ScreenTexts.EMPTY); }
    
    /** Constructs a new button with no text and no action. */
    public NVLabelButton() { this(NVLabelButton.DEFAULT_TEXT); }
    
    //==================================================================================================================
    /**
     * Gets the text displayed on the button.
     * @return The button {@link Text}
     */
    public @NotNull Text getText() { return this.text; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.concat(super.getGuiProperties(), Stream.of(new GuiPropertyDescription<>(
            GuiApiId.GuiProperty.LABEL_BUTTON_TEXT_ALIGN,
            this.textAlign,
            Alignment.CODEC)));
    }
    
    //==================================================================================================================
    /**
     * Sets the text displayed on the button.
     * @param text The button {@link Text}
     */
    public void setText(final @NotNull Text text) { this.text = Objects.requireNonNull(text, "text must not be null"); }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvLabelButtonDrawBackground(canvas, this);
        template.nvLabelButtonDrawText(canvas, this);
    }
}

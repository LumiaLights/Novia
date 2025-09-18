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
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.IComponentNavigator;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.util.*;
import java.util.stream.Stream;



//**********************************************************************************************************************
/**
 * A component, which renders an associated text on the screen.
 * <p>
 * The advantage of using a component over directly drawing the text to the screen is the ease of positioning and
 * transformation.
 */
public class NVLabel
    extends GuiComponent
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void nvLabelDrawBackground(@NotNull Canvas canvas, @NotNull NVLabel nvLabel);
        void nvLabelDrawText(@NotNull Canvas canvas, @NotNull NVLabel nvLabel);
    }
    
    @FunctionalInterface
    public interface TrimFunction
    {
        OrderedText trim(@NotNull Text text, @NotNull GuiFont font, @NotNull NVLabel label);
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_TEXT          = ColourId.reserve();
    public static final ColourId COLOUR_TEXT_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /** See {@link NVLabel#textAlign}. */
    public static final Alignment DEFAULT_ALIGNMENT = Alignment.LEFT;
    
    /** See {@link NVLabel#trimFunction}. */
    public static final TrimFunction DEFAULT_TRIM_FUNCTION;
    
    //==================================================================================================================
    /** A {@link TrimFunction} that does no trimming and just converts the {@link Text} to {@link OrderedText}. */
    public static final TrimFunction NO_TRIM_FUNCTION;
    
    //==================================================================================================================
    static
    {
        DEFAULT_TRIM_FUNCTION = ((text, font, label) ->
        {
            final int             width     = (label.getWidth() - font.getWidthFitted(ScreenTexts.ELLIPSIS));
            final StringVisitable visitable = font.trimToWidth(text, width);
            return Language.getInstance().reorder(StringVisitable.concat(visitable, ScreenTexts.ELLIPSIS));
        });
        
        NO_TRIM_FUNCTION = ((text, font, label) -> text.asOrderedText());
    }
    
    //******************************************************************************************************************
    /** Describes the alignment of the text inside the label's bounds. */
    public final GuiProperty.NonNull<Alignment> textAlign;
    
    /**
     * Describes the function to be used to trim the text if it is too long, and how this should happen.
     * If the text should not be trimmed {@link NVLabel#NO_TRIM_FUNCTION} can be used instead.
     */
    public final GuiProperty.NonNull<TrimFunction> trimFunction;
    
    //------------------------------------------------------------------------------------------------------------------
    private Text text;
    
    //******************************************************************************************************************
    /**
     * Constructs a new label component with the given text and message.
     * @param text    The text to draw on the label
     * @param message The options of this label
     */
    public NVLabel(final @NotNull Text text, final @NotNull Text message)
    {
        super(message);
        
        this.textAlign    = GuiProperty.nonNull(NVLabel.DEFAULT_ALIGNMENT);
        this.trimFunction = GuiProperty.nonNull(NVLabel.DEFAULT_TRIM_FUNCTION);
        
        this.setText(text);
    }
    
    /**
     * Constructs a new label component with the given text and an empty message.
     * @param text The text to draw on the label and the message of the component
     */
    public NVLabel(final @NotNull Text text) { this(text, ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }

    /**
     * Gets the text, that is drawn on this label.
     * @return The labels text
     */
    public @NotNull Text getText() { return this.text; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LABEL_TEXT_ALIGN,
                this.textAlign,
                Alignment.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LABEL_TRIM_FUNC,
                this.trimFunction));
    }
    
    //==================================================================================================================
    /**
     * Sets the text that should be drawn on the label
     * @param text The new text
     */
    public void setText(final @NotNull Text text) { this.text = Objects.requireNonNull(text, "text must not be null"); }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvLabelDrawBackground(canvas, this);
        template.nvLabelDrawText(canvas, this);
    }
}

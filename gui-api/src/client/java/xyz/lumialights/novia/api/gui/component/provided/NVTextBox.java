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

import com.mojang.serialization.Codec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.lumialights.novia.api.core.serialisation.IValueConvertible;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.core.util.RefUtils;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.component.*;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Frame;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;


//**********************************************************************************************************************
/**
 * A text box implementation modelled after Minecraft's {@link TextFieldWidget}, adjusted to the Novia GUI system.
 * <p>
 * A text box is a component, which can be used to type text into a rectangular area, you can copy and paste text into
 * this area and use it to display strings.
 * <p>
 * This is a stateful GUI component, the text it contains can be converted to and from {@link Value}.
 * (for more details, see {@link #getValue()}, {@link #setValue(Value)} and {@link IValueConvertible})
 */
public class NVTextBox
    extends StatefulGuiComponent<NVTextBox>
{
    //******************************************************************************************************************
    /**
     * An interface that defines how the text-box's text {@link String} is converted to {@link OrderedText} for
     * rendering.
     */
    @FunctionalInterface
    public interface RenderTextProvider
    {
        /**
         * Converts the string to {@link OrderedText}.
         *
         * @param text       The text string to convert
         * @param startIndex The index of the character to start the text with
         * @return The converted {@link OrderedText}
         */
        @NotNull OrderedText provide(@NotNull String text, int startIndex);
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        /**
         * Draws the text box in its entirety.
         * @param canvas              The {@link Canvas}
         * @param textBox             The text box object
         * @param bounds              The bounds of the content (see {@link NVTextBox#getBorderSize()})
         * @param lastSwitchFocusTime The time since the last focus change
         */
        void nvTextboxDrawContent(@NotNull Canvas canvas, @NotNull NVTextBox textBox, @NotNull Rectangle bounds,
                                  long lastSwitchFocusTime);
        
        void nvTextboxDrawBackground(@NotNull Canvas canvas, @NotNull NVTextBox textBox);
        
        void nvTextboxDrawText(@NotNull Canvas canvas, @NotNull NVTextBox textBox, @NotNull OrderedText text,
                               int x, int y, @NotNull Colour textColour);
        
        void nvTextboxDrawSuggestion(@NotNull Canvas canvas, @NotNull NVTextBox textBox, int x, int y);
        
        void nvTextboxDrawPlaceholder(@NotNull Canvas canvas, @NotNull NVTextBox textBox, int x, int y);
        
        void nvTextboxDrawSelection(@NotNull Canvas canvas, @NotNull NVTextBox textBox,
                                    int x, int y, int width, int height);
    }
    
    //******************************************************************************************************************
    /** The colour of the text when the text box is editable and no predicate error occurred. */
    public static final ColourId COLOUR_TEXT_EDITABLE = ColourId.reserve();
    
    /** The colour of the text when the text box is uneditable. */
    public static final ColourId COLOUR_TEXT_UNEDITABLE = ColourId.reserve();
    
    /** The colour of the text when the text box contains text that did not satisfy the predicate. */
    public static final ColourId COLOUR_TEXT_ERROR = ColourId.reserve();
    
    /** The colour of the text box suggestion text. */
    public static final ColourId COLOUR_TEXT_SUGGESTION = ColourId.reserve();
    
    /** The colour of the text box placeholder when the text box is empty.*/
    public static final ColourId COLOUR_TEXT_PLACEHOLDER = ColourId.reserve();
    
    //==================================================================================================================
    /** See {@link NVTextBox#renderTextProvider}. */
    public static final RenderTextProvider DEFAULT_PROVIDER = ((string, ignored) ->
        OrderedText.styledForwardsVisitedString(string, Style.EMPTY));
    
    /** See {@link NVTextBox#textPredicate}. */
    public static final Predicate<String> DEFAULT_PREDICATE = Objects::nonNull;
    
    /** See {@link NVTextBox#maxLength}. */
    public static final int DEFAULT_MAX_LENGTH = 1024;
    
    /** See {@link NVTextBox#editable}. */
    public static final boolean DEFAULT_IS_EDITABLE = true;
    
    /** See {@link NVTextBox#predicateStopsInput}. */
    public static final boolean DEFAULT_PREDICATE_STOPS_INPUT = false;
    
    //==================================================================================================================
    /** The textures used for the text box background. */
    public static final ButtonTextures TEXTURES = new ButtonTextures(
        Identifier.ofVanilla("widget/text_field"),
        Identifier.ofVanilla("widget/text_field_highlighted"));
    
    public static final Frame DEFAULT_BORDER_FRAME = new Frame(4);
    
    //******************************************************************************************************************
    /**
     * Describes the string conversion provider.
     * @see RenderTextProvider
     */
    public final GuiProperty.NonNull<RenderTextProvider> renderTextProvider;
    
    /**
     * Describes the pre-set predicate for the text that's being inputted.
     * <p>
     * Whenever the text inside the text-box changes, this will first check whether it is valid, and if it is not
     * set the text box to its “invalid” state.
     * @see #COLOUR_TEXT_ERROR
     */
    public final GuiProperty.NonNull<Predicate<String>> textPredicate;
    
    /**
     * Describes the maximum length of the text content inside this text-box.
     * The maximum length must not be less than 0.
     * <p>
     * When changing the max length and the original text is longer than the new limit, the text will be trimmed and
     * updated accordingly.
     */
    public final GuiProperty.NonNull<Integer> maxLength;
    
    /**
     * Describes whether any given input that is invalid according to {@link #textPredicate}, should be prevented
     * from being inputted to the text-box.
     * <p>
     * This concerns both input devices and text modifying methods.
     */
    public final GuiProperty.NonNull<Boolean> predicateStopsInput;
    
    /**
     * Describes the suggestive text that should be rendered at the end of the text inside the text-box.
     * @see #COLOUR_TEXT_SUGGESTION
     */
    public final GuiProperty<String> suggestion;
    
    /**
     * Describes the text that should be rendered as a placeholder if the text-box is currently holding no text.
     * @see #COLOUR_TEXT_PLACEHOLDER
     */
    public final GuiProperty<Text> placeholder;
    
    //------------------------------------------------------------------------------------------------------------------
    private final Rectangle textBounds = new Rectangle();
    
    private long    lastSwitchFocusTime = Util.getMeasuringTimeMs();
    private int     firstCharacterIndex = 0;
    private int     selectionStart      = 0;
    private int     selectionEnd        = 0;
    private Frame   borderSize          = NVTextBox.DEFAULT_BORDER_FRAME;
    private boolean editable            = true;
    private String  text;
    private boolean erroneous;
    
    //******************************************************************************************************************
    /**
     * Constructs a new text box with the text.
     * @param text    The content of the text box
     * @param message The component message
     */
    public NVTextBox(final @NotNull String text, final @NotNull Text message)
    {
        super(message);
        
        this.renderTextProvider  = GuiProperty.nonNull(NVTextBox.DEFAULT_PROVIDER);
        this.predicateStopsInput = GuiProperty.nonNull(NVTextBox.DEFAULT_PREDICATE_STOPS_INPUT);
        this.suggestion          = GuiProperty.nullable(null);
        this.placeholder         = GuiProperty.nullable(null);
        this.textPredicate       = GuiProperty.nonNull(NVTextBox.DEFAULT_PREDICATE, this::updatePredicate);
        this.maxLength           = GuiProperty.nonNullChecked(NVTextBox.DEFAULT_MAX_LENGTH, this::updateMaxLength,
                                                              RefUtils.greaterThanOrEqual(0));
        
        this.text = Objects
            .requireNonNull(text, "text must not be null")
            .substring(0, Math.min(text.length(), this.maxLength.get()));
        
        this.setWantsFocus(true);
        
        this.setCursorToEnd(false);
        this.updatePredicate();
    }
    
    /**
     * Constructs a new text box with the given text.
     * @param text The content of the text box
     */
    public NVTextBox(final @NotNull String text) { this(text, ScreenTexts.EMPTY); }
    
    /** Constructs a new empty text box. */
    public NVTextBox() { this("", ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    /**
     * Gets the text currently in this text box.
     * <p>
     * This returns the text in the text-box as-is, meaning, if a predicate was set,
     * and the text in the box did not match, this will return the erroneous text.
     *
     * @return The text currently in the text box
     */
    public @NotNull String getText() { return this.text; }
    
    /**
     * Gets the current cursor position in the text box.
     * @return The cursor position
     */
    public int getCursorPos() { return this.selectionStart; }
    
    /**
     * Gets the end index of the current selection. If no text is selected, this will always be the same as
     * {@link #getCursorPos()}.
     * @return The selection end index
     */
    public int getSelectionEnd() { return this.selectionEnd; }
    
    /**
     * Gets the selected portion of the text in the box.
     * @return The selected string
     */
    public @NotNull String getSelectedText()
    {
        return this.text.substring(
            Math.min(this.selectionStart, this.selectionEnd),
            Math.max(this.selectionStart, this.selectionEnd));
    }
    
    /**
     * Gets the index of the first character to the next word starting from the current cursor position in the text.
     * <p>
     * If moving forward and no new word could be found, this will return the length of the text.
     *
     * @param wordOffset The number of words to skip, negative to skip backwards; if this is zero, this will return the
     *                   current cursor position
     * @return The index of the first character to the next word
     */
    public int getWordSkipPosition(final int wordOffset)
    {
        return this.getWordSkipPosition(wordOffset, this.getCursorPos());
    }
    
    /**
     * Gets the local x position of the character at the given index, or the position of the first character if the
     * index is bigger than the length of the text.
     * <p>
     * If this component has no explicitly set font, it will use the associated {@link IGuiTemplate}'s font.
     *
     * @param index The index of the character (negative values are not allowed)
     * @return The x position of the character at the given index
     */
    public int getCharacterX(final int index)
    {
        return ((index > this.text.length()) ? 0 : this.getFont().getWidthFitted(this.text.substring(0, index)));
    }
    
    /**
     * Gets the text currently contained in this text box.
     * <p>
     * If the text does not match the given predicate (see {@link #textPredicate}),
     * this will throw an {@link UndefinedComponentStateException}.
     *
     * @return The new text {@link Value}
     * @throws UndefinedComponentStateException If the text does not match the predicate
     */
    @Override
    public @NotNull Value getValue()
    {
        if (this.erroneous)
        {
            throw new UndefinedComponentStateException("The text box contains invalid text: " + this.text);
        }
        
        return new Value(this.text);
    }
    
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    /**
     * Gets the range of the characters currently in the given selection.
     * @return The selection {@link Range}
     */
    public @NotNull Range<Integer> getSelectionRange()
    {
        return new Range<>(
            Math.min(this.selectionStart, this.selectionEnd),
            Math.max(this.selectionStart, this.selectionEnd));
    }
    
    /**
     * Gets the index of the first visible character in the text-box.
     * @return The first character index
     */
    public int getFirstCharacterIndex() { return this.firstCharacterIndex; }
    
    /**
     * Gets the size of the border, which defines the distance from the text to the component's edges.
     * @return The border {@link Frame}
     */
    public @NotNull Frame getBorderSize() { return this.borderSize; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    protected @NotNull MutableText getNarrationMessage()
    {
        return Text.translatable("gui.narrate.editBox", this.getMessage(), this.text);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.TEXT_BOX_MAX_LENGTH,
                this.maxLength,
                Codec.INT),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.TEXT_BOX_PREDICATE_STOPS_INPUT,
                this.predicateStopsInput,
                Codec.BOOL),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.TEXT_BOX_SUGGESTION,
                this.suggestion,
                Codec.STRING),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.TEXT_BOX_PLACEHOLDER,
                this.placeholder,
                TextCodecs.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.TEXT_BOX_TEXT_PREDICATE,
                this.textPredicate),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.TEXT_BOX_TEXT_PROVIDER,
                this.renderTextProvider)
        );
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private int getWordSkipPosition(final int wordOffset, final int cursorPosition)
    {
        final boolean left = (wordOffset < 0);
        final int     j    = Math.abs(wordOffset);
        
        int pos = cursorPosition;
        
        for (int k = 0; k < j; ++k)
        {
            if (!left)
            {
                final int len = this.text.length();
                pos           = this.text.indexOf(32, pos);
                
                if (pos == -1)
                {
                    pos = len;
                }
                else
                {
                    while (pos < len && this.text.charAt(pos) == ' ')
                    {
                        ++pos;
                    }
                }
            }
            else
            {
                while (pos > 0 && this.text.charAt(pos - 1) == ' ')
                {
                    --pos;
                }

                while (pos > 0 && this.text.charAt(pos - 1) != ' ')
                {
                    --pos;
                }
            }
        }

        return pos;
    }
    
    private int getCursorPosWithOffset(final int offset)
    {
        return Util.moveCursor(this.text, this.selectionStart, offset);
    }
    
    //==================================================================================================================
    /**
     * Gets whether the current text in the box did satisfy the predicate.
     * @return {@code true} if the text is valid
     */
    public boolean isTextValid() { return !this.erroneous; }
    
    /**
     * Checks if the given text is valid according to the set {@link #textPredicate}.
     * @param text The text to check
     * @return {@code true} if the text is valid, otherwise {@code false}
     */
    public boolean verifyTextValid(final @NotNull String text)
    {
        return this.textPredicate.get().test(Objects.requireNonNull(text, "text must not be null"));
    }
    
    /**
     * Gets whether the text of this text box can be modified.
     * @return {@code true} if the contents are modifiable
     */
    public boolean isEditable() { return this.editable; }
    
    //==================================================================================================================
    /**
     * Gets whether the text in the box is empty.
     * @return {@code true} if the text is empty
     */
    public boolean isEmpty() { return this.text.isEmpty(); }
    
    //==================================================================================================================
    /**
     * Sets the position of the text box cursor.
     *
     * @param position The character index of where the cursor should be
     * @param select   Whether the text box should expand the selection from the previous to the new position
     */
    public void setCursor(final int position, final boolean select)
    {
        this.setSelectionStart(position);
        
        if (!select)
        {
            this.setSelectionEnd(this.selectionStart);
        }
    }
    
    /**
     * Sets the cursor back all the way to the start of the text.
     * @param select Whether the text box should expand the selection from the previous to the new position
     */
    public void setCursorToStart(final boolean select) { this.setCursor(0, select); }
    
    /**
     * Sets the cursor back all the way to the end of the text.
     * @param select Whether the text box should expand the selection from the previous to the new position
     */
    public void setCursorToEnd(final boolean select) { this.setCursor(this.text.length(), select); }
    
    /**
     * Sets the selection start position to the given character in the text.
     * <p>
     * If the position goes beyond the beginning or end of the text, it will clamp to either side.
     *
     * @param position The character index
     */
    public void setSelectionStart(final int position)
    {
        this.selectionStart = MathHelper.clamp(position, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionStart);
    }
    
    /**
     * Sets the selection end position to the given character in the text.
     * <p>
     * If the position goes beyond the beginning or end of the text, it will clamp to either side.
     *
     * @param index The character index
     */
    public void setSelectionEnd(final int index)
    {
        this.selectionEnd = MathHelper.clamp(index, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionEnd);
    }

    /**
     * Sets the current text in the text box to the new string.
     * <p>
     * Any selections will be cleared, and the cursor will be placed at the end of the new text.
     * <p>
     * If the new text is not the same and valid, according to the predicate (if one is set),
     * listeners will be notified about the change.
     * @param text The new text
     */
    public void setText(@NotNull String text)
    {
        Objects.requireNonNull(text, "text must not be null");
        
        final int maxlen = this.maxLength.get();
        text = ((text.length() > maxlen) ? text.substring(0, maxlen) : text);
        
        if (this.text.equals(text))
        {
            return;
        }
        
        final boolean valid = this.verifyTextValid(text);
        
        if (!valid && this.predicateStopsInput.get())
        {
            return;
        }
        
        this.text = text;
        
        this.setCursorToEnd(false);
        this.setSelectionEnd(this.selectionStart);
        this.erroneous = !valid;
        
        this.sendTextChanged();
    }
    
    /**
     * Sets the textual content from the given {@link Value} object, if it is a string, otherwise does nothing.
     * @param value The new {@link Value}
     */
    @Override
    public void setValue(@NotNull final Value value)
    {
        if (!value.isString())
        {
            return;
        }
        
        this.setText(value.getString());
    }
    
    public void setBorderSize(final @NotNull Frame borderSize)
    {
        if (!this.borderSize.equals(borderSize))
        {
            this.borderSize = Objects.requireNonNull(borderSize, "border size frame must not be null");
            this.updateTextBounds();
        }
    }
    
    /**
     * Sets whether the contents of this text box can be modified
     * @param editable {@code true} if the contents are modifiable
     */
    public void setEditable(final boolean editable) { this.editable = editable; }
    
    //==================================================================================================================
    /**
     * Writes the given text at the current cursor position.
     * <p>
     * If there is currently a text selection, the text in the selection will be replaced with the given text.
     *
     * @param text The text to insert
     */
    public void write(final String text)
    {
        final int sel_start = Math.min(this.selectionStart, this.selectionEnd);
        final int sel_end   = Math.max(this.selectionStart, this.selectionEnd);
        int       space     = (this.maxLength.get() - this.text.length() - (sel_start - sel_end));
        
        if (space > 0)
        {
            String string = StringHelper.stripInvalidChars(text);
            int    l      = string.length();
            
            if (space < l)
            {
                if (Character.isHighSurrogate(string.charAt(space - 1)))
                {
                    --space;
                }
                
                string = string.substring(0, space);
                l      = space;
            }
            
            final String  new_text = (new StringBuilder(this.text)).replace(sel_start, sel_end, string).toString();
            final boolean valid    = this.verifyTextValid(new_text);
            
            if (!valid && this.predicateStopsInput.get())
            {
                return;
            }
            
            this.text      = new_text;
            this.erroneous = !valid;
            this.setSelectionStart(sel_start + l);
            this.setSelectionEnd(this.selectionStart);
            
            this.sendTextChanged();
        }
    }
    
    //==================================================================================================================
    /**
     * Erases the given number of words from the text box.
     * <p>
     * If there is currently a text selection, the text in the selection will be erased
     *
     * @param wordOffset The number of words to erase, negative to erase backwards; if this is zero, this will return
     *                   the current cursor position
     */
    public void eraseWords(final int wordOffset)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.selectionStart)
            {
                this.write("");
            }
            else
            {
                this.eraseCharactersTo(this.getWordSkipPosition(wordOffset));
            }
        }
    }
    
    /**
     * Erases a number of characters starting from the cursor.
     * @param characterOffset The character offset from the cursor, negative numbers erase backwards
     */
    public void eraseCharacters(final int characterOffset)
    {
        this.eraseCharactersTo(this.getCursorPosWithOffset(characterOffset));
    }

    /**
     * Erases all characters from the given character index to the current position of the cursor.
     * <p>
     * If there is currently a text selection, the text in the selection will be erased instead.
     *
     * @param position The position of the character to start the erasure at
     */
    public void eraseCharactersTo(final int position)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.selectionStart)
            {
                this.write("");
            }
            else
            {
                final int start = Math.min(position, this.selectionStart);
                final int end   = Math.max(position, this.selectionStart);
                
                if (start != end)
                {
                    final String  string = (new StringBuilder(this.text)).delete(start, end).toString();
                    final boolean valid  = this.verifyTextValid(string);
                    
                    if (!valid && this.predicateStopsInput.get())
                    {
                        return;
                    }
                    
                    this.text      = string;
                    this.erroneous = !valid;
                    this.setCursor(start, false);
                    
                    this.sendTextChanged();
                }
            }
        }
    }
    
    /**
     * Moves the cursor by the given offset.
     * @param offset The offset in characters to move the cursor, negative numbers move backwards
     * @param select Whether the text box should expand the selection from the previous to the new position
     */
    public void moveCursor(final int offset, final boolean select)
    {
        this.setCursor(this.getCursorPosWithOffset(offset), select);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void erase(final int offset)
    {
        if (Screen.hasControlDown())
        {
            this.eraseWords(offset);
        }
        else
        {
            this.eraseCharacters(offset);
        }
    }
    
    //==================================================================================================================
    @Override
    protected boolean onMouseDown(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        final GuiFont font       = this.getFont();
        final String  string     = font.trimToWidth(
            this.text.substring(this.firstCharacterIndex),
            this.textBounds.width());
        final int     text_width = (
            (MathHelper.floor(e.mousePos.x()) - this.getScreenX())
            - (int) ((this.getWidth() - this.textBounds.width()) * 0.5)
        );
        
        this.setCursor((font.trimToWidth(string, text_width, Style.EMPTY).length() + this.firstCharacterIndex),
                       Screen.hasShiftDown());
        
        return true;
    }
    
    @Override
    protected boolean onKeyDown(final @NotNull KeyEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        switch (e.input)
        {
            case GLFW.GLFW_KEY_BACKSPACE ->
            {
                if (this.editable)
                {
                    this.erase(-1);
                }
            }
            
            case GLFW.GLFW_KEY_DELETE ->
            {
                if (this.editable)
                {
                    this.erase(1);
                }
            }
            
            case GLFW.GLFW_KEY_RIGHT ->
            {
                if (Screen.hasControlDown())
                {
                    this.setCursor(this.getWordSkipPosition(1), Screen.hasShiftDown());
                }
                else
                {
                    this.moveCursor(1, Screen.hasShiftDown());
                }
            }
            
            case GLFW.GLFW_KEY_LEFT ->
            {
                if (Screen.hasControlDown())
                {
                    this.setCursor(this.getWordSkipPosition(-1), Screen.hasShiftDown());
                }
                else
                {
                    this.moveCursor(-1, Screen.hasShiftDown());
                }
            }
            
            case GLFW.GLFW_KEY_HOME -> this.setCursorToStart(Screen.hasShiftDown());
            case GLFW.GLFW_KEY_END  -> this.setCursorToEnd  (Screen.hasShiftDown());
            
            default ->
            {
                if (Screen.isSelectAll(e.input))
                {
                    this.setCursorToEnd(false);
                    this.setSelectionEnd(0);
                }
                else if (Screen.isCopy(e.input))
                {
                    MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                }
                else if (Screen.isPaste(e.input))
                {
                    if (this.editable)
                    {
                        this.write(MinecraftClient.getInstance().keyboard.getClipboard());
                    }
                }
                else
                {
                    if (!Screen.isCut(e.input))
                    {
                        return false;
                    }
                    
                    MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                    
                    if (this.editable)
                    {
                        this.write("");
                    }
                }
            }
        }
        
        return true;
    }
    
    @Override
    protected boolean onInput(final @NotNull KeyEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        final char chr = e.character();
        
        if (StringHelper.isValidChar(chr))
        {
            if (this.editable)
            {
                this.write(Character.toString(chr));
            }
            
            return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    @Override
    protected void onFocusChanged(final @NotNull GuiNavigationType type)
    {
        if (this.isFocused())
        {
            this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
        }
    }
    
    //==================================================================================================================
    /** Can be overridden by child classes to determine whenever the text has been modified. */
    protected void onTextChanged() {}
    
    //==================================================================================================================
    @Override
    protected void resized()
    {
        this.updateTextBounds();
        this.setCursor(this.selectionStart, false);
    }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        final Template template = canvas.getTemplate();
        template.nvTextboxDrawBackground(canvas, this);
        template.nvTextboxDrawContent   (canvas, this, this.textBounds, this.lastSwitchFocusTime);
    }
    
    //==================================================================================================================
    private void sendTextChanged()
    {
        this.onTextChanged();
        this.notifyChangeListeners();
    }
    
    //==================================================================================================================
    private void updatePredicate(final @NotNull Predicate<String> predicate)
    {
        this.erroneous = !predicate.test(this.text);
    }
    
    private void updatePredicate() { this.updatePredicate(this.textPredicate.get()); }
    
    private void updateMaxLength(final int length)
    {
        if (this.text.length() > length)
        {
            this.text      = this.text.substring(0, length);
            this.erroneous = !this.textPredicate.get().test(this.text);
            
            if (this.selectionStart > length)
            {
                this.setSelectionStart(length);
            }
            
            this.sendTextChanged();
        }
    }
    
    private void updateTextBounds() { this.textBounds.setBounds(this.getLocalBounds().pad(this.borderSize)); }
    
    private void updateFirstCharacterIndex(final int cursor)
    {
        this.firstCharacterIndex = Math.min(this.firstCharacterIndex, this.text.length());
        
        final GuiFont font   = this.getFont();
        final String  text   = font.trimToWidth(this.text.substring(this.firstCharacterIndex), this.textBounds.width());
        final int     length = (text.length() + this.firstCharacterIndex);
        
        if (cursor == this.firstCharacterIndex)
        {
            this.firstCharacterIndex -= font.trimToWidth(this.text, this.textBounds.width(), true).length();
        }
        
        if (cursor > length)
        {
            this.firstCharacterIndex += (cursor - length);
            
            if (cursor >= this.text.length())
            {
                final float cursor_size = font.getWidth("_");
                
                if ((font.getWidth(text) + cursor_size) > this.textBounds.width())
                {
                    ++this.firstCharacterIndex;
                }
            }
        }
        else if (cursor <= this.firstCharacterIndex)
        {
            this.firstCharacterIndex -= (this.firstCharacterIndex - cursor);
        }
        
        this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, this.text.length());
    }
}

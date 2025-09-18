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
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.lumialights.novia.api.GuiApiLang;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.Pair;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.component.*;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class NVDropdown
    extends StatefulGuiComponent<NVDropdown>
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void nvDropdownDrawMenuBackground(@NotNull Canvas canvas, @NotNull NVDropdown dropdown, int width, int height);
        
        void nvDropdownDrawMenuItem(@NotNull Canvas canvas, @NotNull NVDropdown dropdown, @NotNull Text title,
                                    int width, int height, int index, boolean selected, boolean hovered,
                                    boolean focused);
        
        void nvDropdownDrawButton(@NotNull Canvas canvas, @NotNull NVDropdown dropdown,
                                  @NotNull NVAbstractButton<?> button, @NotNull String text);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private class Item
        implements INVItemModel
    {
        //**************************************************************************************************************
        public final String name;
        public final Value  value;
        public final Text   title;
        
        //--------------------------------------------------------------------------------------------------------------
        private final Tooltip tooltip;
        
        //**************************************************************************************************************
        public Item(final @NotNull String name, final @NotNull Value value, final @NotNull Text title)
        {
            this.name  = Objects.requireNonNull(name,  "name must not be null");
            this.value = Objects.requireNonNull(value, "value must not be null");
            this.title = Objects.requireNonNull(title, "title must not be null");
            
            this.tooltip = Tooltip.of(title);
        }
        
        //==============================================================================================================
        @Override public @Nullable Tooltip getTooltip(final boolean selected) { return this.tooltip; }
        
        //==============================================================================================================
        @Override
        public void draw(final @NotNull Canvas    canvas,
                         final @NotNull Rectangle bounds,
                         final int                index,
                         final boolean            selected,
                         final boolean            hovered,
                         final boolean            focused)
        {
            canvas.getTemplate().nvDropdownDrawMenuItem(canvas, NVDropdown.this, this.title, bounds.width(),
                                                        bounds.height(), index, selected, hovered, focused);
        }
    }
    
    private class Menu
        extends GuiComponent
    {
        //**************************************************************************************************************
        private final NVListBox<NVDropdown.Item> listBox;
        
        //**************************************************************************************************************
        public Menu(final @NotNull List<Item> options)
        {
            this.listBox = new NVListBox<>();
            this.listBox.selectionMode.set(NVListBox.SelectionMode.SINGLE);
            this.listBox.itemSize.set(NVDropdown.ITEM_HEIGHT);
            this.listBox.addAllItems(options);
            this.listBox.refreshList();
            this.listBox.selectItem(NVDropdown.this.selected);
            this.listBox.addSelectionListener((item, index, selected) -> this.hideModal());
            this.addChild(this.listBox);
            
            this.setPositioner(this::calculateBounds);
        }
        
        //==============================================================================================================
        public @NotNull Integer getSelected() { return this.listBox.getIndexOfFirstSelectedItem(); }
        
        //==============================================================================================================
        @Override protected void resized() { this.listBox.setBounds(this.getLocalBounds().pad(1)); }
        
        //==============================================================================================================
        @Override
        protected void draw(final @NotNull Canvas canvas)
        {
            canvas.getTemplate().nvDropdownDrawMenuBackground(canvas, NVDropdown.this, this.getWidth(),
                                                              this.getHeight());
        }
        
        //==============================================================================================================
        private @NotNull Rectangle calculateBounds(final int x, final int y, final int width, final int height)
        {
            final int window_height = MinecraftClient.getInstance().getWindow().getScaledHeight();
            final int new_y         = (y + height - 1);
            
            return new Rectangle(x, new_y, width, (NVDropdown.ITEM_HEIGHT * this.listBox.getItemCount() + 2))
                .constrainToMax(Integer.MAX_VALUE, (window_height - new_y - 2))
                .constrainToMin(0, NVDropdown.MIN_LIST_HEIGHT);
        }
    }
    
    private class DropDownButton
        extends NVAbstractButton<DropDownButton>
    {
        //**************************************************************************************************************
        private String text = "▾";
        
        //**************************************************************************************************************
        public DropDownButton(final @NotNull NVAbstractButton.ActionListener<DropDownButton> actionListener)
        {
            super(actionListener, ScreenTexts.EMPTY);
            this.setWantsFocus(false);
        }
        
        //==============================================================================================================
        public void setText(final @NotNull String text) { this.text = text; }
        
        //==============================================================================================================
        @Override public boolean hitTest(final int x, final int y) { return (x > 0 && x < this.getWidth()); }
        
        //==============================================================================================================
        @Override
        protected void draw(final @NotNull Canvas canvas)
        {
            canvas.getTemplate().nvDropdownDrawButton(canvas, NVDropdown.this, this, this.text);
        }
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_OPTION_TEXT                 = ColourId.reserve();
    public static final ColourId COLOUR_OPTION_BACKGROUND_HIGHLIGHT = ColourId.reserve();
    public static final ColourId COLOUR_OPTION_BACKGROUND_SELECTED  = ColourId.reserve();
    
    //==================================================================================================================
    /** See {@link NVDropdown#showSuggestion}. */
    public static final boolean DEFAULT_SHOW_SUGGESTION = true;
    
    /** See {@link NVDropdown#optionAlignment}. */
    public static final Alignment DEFAULT_OPTION_ALIGNMENT = Alignment.MIDDLE_LEFT;
    
    //==================================================================================================================
    private static final int ITEM_HEIGHT     = 15;
    private static final int MIN_LIST_HEIGHT = (ITEM_HEIGHT * 3);
    
    //******************************************************************************************************************
    /**
     * Describes the ID of the option, which should be automatically selected.
     * <p>
     * If this property is set and has an empty string, or an ID not referring to a valid option, it will always
     * select the first option. If it is not set, the drop-down can be in an empty state.
     * <p>
     * If no options are in the list, none will be selected.
     */
    public final GuiProperty<String> defaultOption;
    
    /**
     * Describes whether the text box should show the first closest match to the given input if it doesn't match any
     * option at that point.
     */
    public final GuiProperty.NonNull<Boolean> showSuggestion;
    
    /** Describes the alignment of options inside the drop-down list. */
    public final GuiProperty.NonNull<Alignment> optionAlignment;
    
    //==================================================================================================================
    private final List<Item>     options;
    private final NVTextBox      input;
    private final DropDownButton arrowButton;
    
    private int     selected = -1;
    private boolean isOpen   = false;
    
    //******************************************************************************************************************
    /**
     * Creates new drop-down with the given options pre-initialised.
     * @param values  The pre-initialised options
     * @param message The component message
     */
    public NVDropdown(final @NotNull Collection<Pair<Text, Value>> values, final @NotNull Text message)
    {
        super(message);
        
        this.options = values
            .stream()
            .map(p -> new Item(p.first().getString(), p.second(), p.first()))
            .collect(Collectors.toList());
        
        this.defaultOption   = GuiProperty.nullable(null, (v -> this.setDefaultSelectedOption()));
        this.showSuggestion  = GuiProperty.nonNull (NVDropdown.DEFAULT_SHOW_SUGGESTION, this::updateSuggestionMode);
        this.optionAlignment = GuiProperty.nonNull (NVDropdown.DEFAULT_OPTION_ALIGNMENT);
        
        this.input = this.addChild(new NVTextBox());
        this.input.textPredicate.set(this::hasOption);
        this.input.placeholder.set(GuiApiLang.GUI_DROPDOWN_PLACEHOLDER);
        this.input.addChangeListener(this::textChanged);
        
        this.arrowButton = this.addChild(new DropDownButton(this::openMenu));
        
        this.setDefaultSelectedOption();
    }
    
    /**
     * Creates new drop-down with the given options pre-initialised.
     * @param values The pre-initialised options
     */
    public NVDropdown(final @NotNull Collection<Pair<Text, Value>> values) { this(values, ScreenTexts.EMPTY); }
    
    /** Creates new empty drop-down. */
    public NVDropdown() { this(Collections.emptyList()); }
    
    //==================================================================================================================
    /**
     * Gets the currently selected value from the drop-down.
     * <p>
     * If there is no selected option, this will throw a {@link UndefinedComponentStateException},
     * so make sure to test with {@link #hasSelectedOption()} first.
     *
     * @return The currently selected {@link Value}
     * @throws UndefinedComponentStateException If there is no selected item
     */
    @Override
    public @NotNull Value getValue()
    {
        if (this.selected < 0)
        {
            throw new UndefinedComponentStateException("Dropdown has no selected option");
        }
        
        return this.options.get(this.selected).value;
    }
    
    /**
     * Gets the internal text box, this should only be used for styling purposes.
     * @return The internal {@link NVTextBox}
     */
    public @NotNull NVTextBox getTextBox() { return this.input; }
    
    /**
     * Gets the text of the currently selected option as string.
     * @return The selected text
     */
    public @NotNull String getSelectedOptionName() { return this.options.get(this.selected).name; }
    
    /**
     * Whether this drop down currently has an option selected or not.
     * @return {@code true} if there is an item selected
     */
    public boolean hasSelectedOption() { return (this.selected > -1); }
    
    /**
     * Gets the number of options in the drop-down.
     * @return The number of options
     */
    public int getOptionCount() { return this.options.size(); }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.DROPDOWN_DEFAULT_OPT,
                this.defaultOption,
                Codec.STRING),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.DROPDOWN_OPT_ALIGNMENT,
                this.optionAlignment,
                Alignment.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.DROPDOWN_SHOW_SUGGESTION,
                this.showSuggestion,
                Codec.BOOL)
        );
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> @Nullable Pair<Integer, Item> getOptionFor(final @NotNull T                 optionComponent,
                                                           final @NotNull Function<Item, T> getter)
    {
        return IntStream
            .range(0, this.options.size())
            .mapToObj(i -> Pair.of(i, this.options.get(i)))
            .filter(p -> getter.apply(p.second()).equals(optionComponent))
            .findFirst()
            .orElse(null);
    }
    
    //==================================================================================================================
    /**
     * Determines whether this drop-down contains an option with the given name.
     * @param optionName The unique name of the option
     * @return {@code true} if the option already exists by that name
     */
    public boolean hasOption(final @NotNull String optionName)
    {
        return this.options.stream().anyMatch(opt -> opt.name.equals(optionName));
    }
    
    /**
     * Determines whether the drop-down has any options to pick from.
     * @return {@code true} if there is at least one option
     */
    public boolean hasAnyOptions() { return !this.options.isEmpty(); }
    
    //==================================================================================================================
    /**
     * Sets the option to be selected based on its associated value.
     * @param value The {@link Value} the item to be selected has
     */
    @Override
    public void setValue(final @NotNull Value value)
    {
        Objects.requireNonNull(value, "value must not be null");
        this.options
            .stream()
            .filter(e -> e.value.equals(value))
            .findFirst()
            .ifPresent(e -> this.setSelectedOption(e.name));
    }
    
    /**
     * Sets the selected option from its unique name.
     * @param optionName The name of the option
     */
    public void setSelectedOption(final @NotNull String optionName)
    {
        Objects.requireNonNull(optionName, "option name must not be null");
        
        final Pair<Integer, Item> opt = this.getOptionFor(optionName, (opt2 -> opt2.name));
        
        if (opt == null || opt.first() == this.selected)
        {
            return;
        }
        
        this.setSelectedIndex(opt.first());
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setDefaultSelectedOption()
    {
        this.defaultOption.ifSet(optionName ->
        {
            if (this.selected < 0 && this.hasAnyOptions())
            {
                final Pair<Integer, Item> opt = this.getOptionFor(optionName, (opt2 -> opt2.name));
                this.setSelectedIndex(opt != null ? opt.first() : 0);
            }
        });
    }
    
    private void setSelectedIndex(final int index)
    {
        if (index < 0 || index >= this.getOptionCount() || index == this.selected)
        {
            return;
        }
        
        this.selected = index;
        
        this.updateText();
        this.notifyChangeListeners();
    }
    
    //==================================================================================================================
    /**
     * Adds the given option with the given value and text to the drop-down list.
     * @param text  The text of the option
     * @param value The value of the option
     * @return {@code true} if the option was added, {@code false} if an option by that name already existed
     */
    public boolean addOption(final @NotNull String text, final @NotNull Value value)
    {
        return this.addOptionInternal(text, value, Text.of(text));
    }
    
    /**
     * Adds the given option with the given value and text to the drop-down list.
     * @param text  The text of the option
     * @param value The value of the option
     * @return {@code true} if the option was added, {@code false} if an option by that name already existed
     */
    public boolean addOption(final @NotNull Text text, final @NotNull Value value)
    {
        return this.addOptionInternal(text.getString(), value, text);
    }
    
    /**
     * Adds the given option with the given value as name and title to the drop-down list.
     * <p>
     * If the string converted value is bigger than 64, the title will be truncated to a length of 64 characters.
     *
     * @param value The value, name and title of the option
     * @return {@code true} if the option was added, {@code false} if an option by that name already existed
     */
    public boolean addOption(final @NotNull Value value)
    {
        final String text = value.asString();
        return this.addOptionInternal(text, value, Text.of(text));
    }
    
    public void addAllOptions(final @NotNull Collection<Pair<Text, Value>> values)
    {
        this.options.addAll(values
            .stream()
            .flatMap(p ->
            {
                final String name = p.first().getString();
                return (!this.hasOption(name)
                    ? Stream.of(new Item(name, p.second(), p.first()))
                    : Stream.empty());
            })
            .toList());
        this.setDefaultSelectedOption();
    }
    
    /**
     * Removes the option with the given name from the drop-down list.
     * @param optionName The unique name of the option
     * @return {@code true} if the option was removed, {@code false} if no option by that name existed
     */
    public boolean removeOption(final @NotNull String optionName)
    {
        if (this.options.removeIf(opt -> opt.name.equals(optionName)))
        {
            this.setDefaultSelectedOption();
            
            if (this.selected > -1 && !this.hasAnyOptions())
            {
                this.selected = -1;
            }
            
            return true;
        }
        
        return false;
    }
    
    /** Clears all options in the drop-down list. */
    public void clearOptions()
    {
        this.options.clear();
        
        if (this.selected > -1)
        {
            this.selected = -1;
            
            this.updateText();
            this.notifyChangeListeners();
        }
    }
    
    /** Clears the selected option in the drop-down if {@link #defaultOption} is not set, otherwise does nothing. */
    public void clearSelectedOption()
    {
        if (this.defaultOption.isSet() || this.selected < 0)
        {
            return;
        }
        
        this.selected = -1;
        this.updateText();
        this.notifyChangeListeners();
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private boolean addOptionInternal(final @NotNull String name, final @NotNull Value value, final @NotNull Text text)
    {
        Objects.requireNonNull(name,  "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(text,  "text must not be null");
        
        if (this.hasOption(name))
        {
            return false;
        }
        
        this.options.add(new Item(name, value, text));
        this.setDefaultSelectedOption();
        
        return true;
    }
    
    //==================================================================================================================
    private void openMenu(final @Nullable Object button)
    {
        if (this.isOpen)
        {
            return;
        }
        
        (new Menu(this.options))
            .showModal(ModalArgs.popup(this))
            .onOpened(() ->
            {
                this.isOpen = true;
                this.arrowButton.setText("▴");
            })
            .thenAccept(comp ->
            {
                this.isOpen = false;
                this.arrowButton.setText("▾");
                
                final int selected = ((Menu) comp).getSelected();
                this.setSelectedIndex(selected);
            });
    }
    
    //==================================================================================================================
    @Override
    protected boolean onKeyDown(final @NotNull KeyEvent e)
    {
        if (this.isActive() && e.source == this.input && e.input == GLFW.GLFW_KEY_TAB)
        {
            final String suggestion = this.input.suggestion.get();
            
            if (suggestion != null)
            {
                this.input.setText(this.input.getText() + suggestion);
                return true;
            }
        }
        
        return false;
    }
    
    //==================================================================================================================
    @Override
    protected void resized()
    {
        final Rectangle input_bounds = this.getLocalBounds();
        this.arrowButton.setBounds(input_bounds.removeRight(10));
        this.input      .setBounds(input_bounds.padRight(-3));
    }
    
    //==================================================================================================================
    @Override
    protected void childFocusChanged(final @NotNull GuiComponent child, final @NotNull GuiNavigationType type)
    {
        if (child == this.input && !child.isFocused())
        {
            this.updateText();
        }
    }
    
    //==================================================================================================================
    private void textChanged(final @NotNull NVTextBox box)
    {
        final String text = box.getText();
        box.suggestion.set(null);
        
        if (text.isEmpty())
        {
            return;
        }
        
        final Pair<Integer, Item> opt = this.getOptionFor(text, (opt2 -> opt2.title));
        
        if (opt != null)
        {
            if (this.selected != opt.first())
            {
                this.selected = opt.first();
                this.notifyChangeListeners();
            }
            
            return;
        }
        
        if (this.showSuggestion.get())
        {
            this.options
                .stream()
                .filter(opt2 -> opt2.name.startsWith(text))
                .findFirst()
                .map(opt2 -> opt2.name)
                .ifPresent(str -> box.suggestion.set(str.substring(text.length())));
        }
    }
    
    //==================================================================================================================
    private void updateSuggestionMode(final boolean enable)
    {
        if (!enable)
        {
            this.input.suggestion.set(null);
        }
    }
    
    private void updateText()
    {
        if (this.selected < 0 || !this.hasAnyOptions())
        {
            return;
        }
        
        this.input.mute();
        this.input.setText(this.options.get(this.selected).name);
        this.input.unmute();
        
        this.input.setCursorToStart(false);
        this.input.suggestion.set(null);
    }
}

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
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.IComponentNavigator;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.event.GuiEvent;
import xyz.lumialights.novia.api.gui.event.GuiEventArgs;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.property.GuiProperty;
import xyz.lumialights.novia.api.gui.property.GuiPropertyBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;



//**********************************************************************************************************************
/**
 * A component, which allows listing custom-defined items in a list.
 * <p>
 * Do note, however, that changing order, adding or removing items, {@link NVListBox#refreshList()} should be called
 * after these operations to not leave the list in an intermediary state, working on the list could have
 * unexpected results otherwise.
 *
 * @param <T> The item implementation based on {@link INVItemModel}
 */
public class NVListBox<T extends INVItemModel>
    extends GuiComponent
{
    //******************************************************************************************************************
    /** Defines the selection mode for the list box. */
    public enum SelectionMode
        implements StringIdentifiable
    {
        /** Only one item can be selected at a time. */
        SINGLE,
        
        /** Multiple items can be selected at a time. */
        MULTIPLE,
        
        /** No items can be selected at all. */
        NONE,
        ;
        
        //**************************************************************************************************************
        public static final Codec<SelectionMode> CODEC = StringIdentifiable.createCodec(SelectionMode::values);
        
        //**************************************************************************************************************
        @Override public String asString() { return this.name().toLowerCase(); }
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        /**
         * Draws the list box's background.
         * @param canvas  The {@link Canvas}
         * @param listBox The {@link NVListBox}
         * @param <T> The item model type
         */
        <T extends INVItemModel> void nvListBoxDrawBackground(@NotNull Canvas canvas, @NotNull NVListBox<T> listBox);
    }
    
    public interface ItemEventArgsBase<T extends INVItemModel>
        extends GuiEventArgs
    {
        //**************************************************************************************************************
        int itemIndex();
        
        //==============================================================================================================
        /**
         * Gets the item at the index specified by the current event.
         * <p>
         * Do only use the current handler's sender object!
         * @param sender The sender object of the current handler
         * @return {@link INVItemModel} the item model
         */
        @SuppressWarnings("unchecked")
        default @NotNull T getItem(final GuiComponent sender)
        {
            return ((NVListBox<T>) sender).getItemAt(this.itemIndex()).orElseThrow();
        }
        
        /**
         * Gets whether the item at the index specified by the current event is currently selected.
         * <p>
         * Do only use the current handler's sender object!
         * @param sender The sender object of the current handler
         * @return {@code true} if the item is selected
         */
        @SuppressWarnings("unchecked")
        default boolean isSelected(final GuiComponent sender)
        {
            return ((NVListBox<T>) sender).isItemSelectedAt(this.itemIndex());
        }
    }
    
    public record ItemEventArgs<T extends INVItemModel>(int itemIndex) implements ItemEventArgsBase<T> {}
    
    public record ItemRemovedEventArgs<T extends INVItemModel>(@NotNull T item) implements GuiEventArgs {}
    
    public record ItemMovedEventArgs<T extends INVItemModel>(int itemIndex, int oldIndex) implements GuiEventArgs {}
    
    //------------------------------------------------------------------------------------------------------------------
    private interface ListHandler
    {
        //**************************************************************************************************************
        @NotNull Rectangle getItemBounds(int pos);
        
        //==============================================================================================================
        int itemPos(@NotNull GuiComponent item);
        
        //==============================================================================================================
        void setPos(@NotNull GuiComponent item, int pos);
        
        //==============================================================================================================
        void accommodateItems(int size);
        void resize();
    }
    
    private final class VerticalHandler
        implements ListHandler
    {
        //**************************************************************************************************************
        @Override
        public @NotNull Rectangle getItemBounds(final int y)
        {
            final int size = NVListBox.this.itemSize.get();
            return new Rectangle(0, y, NVListBox.this.container.getWidth(), size);
        }
        
        //==============================================================================================================
        @Override public int itemPos(final @NotNull GuiComponent item) { return item.getY(); }
        
        //==============================================================================================================
        @Override public void setPos(final @NotNull GuiComponent item, final int pos) { item.setY(pos); }
        
        //==============================================================================================================
        @Override
        public void accommodateItems(final int size)
        {
            NVListBox.this.container.setHeight(size);
            this.resize();
        }
        
        @Override
        public void resize()
        {
            final int new_width = NVListBox.this.viewport.getViewWidth();
            
            if (new_width != NVListBox.this.container.getWidth())
            {
                NVListBox.this.container.setWidth(new_width);
                
                for (final var item : NVListBox.this.items)
                {
                    item.setWidth(new_width);
                }
            }
        }
    }
    
    private final class HorizontalHandler
        implements ListHandler
    {
        //**************************************************************************************************************
        @Override
        public @NotNull Rectangle getItemBounds(final int x)
        {
            final int size = NVListBox.this.itemSize.get();
            return new Rectangle(x, 0, size, NVListBox.this.container.getHeight());
        }
        
        //==============================================================================================================
        @Override public int itemPos(final @NotNull GuiComponent item) { return item.getX(); }
        
        //==============================================================================================================
        @Override public void setPos(final @NotNull GuiComponent item, final int pos) { item.setX(pos); }
        
        //==============================================================================================================
        @Override
        public void accommodateItems(final int size)
        {
            NVListBox.this.container.setWidth(size);
            this.resize();
        }
        
        @Override
        public void resize()
        {
            final int new_height = NVListBox.this.viewport.getViewHeight();
            
            if (new_height != NVListBox.this.container.getHeight())
            {
                NVListBox.this.container.setHeight(new_height);
            
                for (final var item : NVListBox.this.items)
                {
                    item.setHeight(new_height);
                }
            }
        }
    }
    
    private class Item
        extends GuiComponent
    {
        //**************************************************************************************************************
        public final T model;
        
        public boolean selected    = false;
        public boolean initialised = false;
        
        //**************************************************************************************************************
        public Item(final @NotNull T model)
        {
            this.model = model;
            this.setMonitorChildren(true);
        }
        
        //==============================================================================================================
        public void setSelected(final boolean selected)
        {
            if (selected == this.selected)
            {
                return;
            }
            
            this.selected = selected;
            NVListBox.this.sendSelectionChangeNotification(this);
        }
        
        //==============================================================================================================
        @Override
        public boolean onMouseDown(final @NotNull MouseEvent e)
        {
            if (!this.isActive())
            {
                return false;
            }
            
            if (this.model.onClick(e.localMousePos(), this.selected))
            {
                final boolean new_selected = !this.selected;
                NVListBox.this.selectInternal(this.getNavigationOrder(), new_selected);
            }
            
            return true;
        }
        
        @Override
        public boolean onKeyDown(final @NotNull KeyEvent e)
        {
            if (!this.isActive())
            {
                return false;
            }
            
            if (
                this.getParent() != null
                && (e.input == GLFW.GLFW_KEY_LEFT_SHIFT || e.input == GLFW.GLFW_KEY_RIGHT_SHIFT)
            )
            {
                NVListBox.this.multiSelectMode = this.selected;
            }
            
            return switch (e.input)
            {
                case GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER ->
                {
                    final boolean new_selected = !this.selected;
                    NVListBox.this.selectInternal(this.getNavigationOrder(), new_selected);
                    
                    if (NVListBox.this.multiSelectMode != null)
                    {
                        NVListBox.this.multiSelectMode = new_selected;
                    }
                    
                    yield true;
                }
                
                default ->
                {
                    if (!Screen.hasAltDown() && Screen.hasControlDown() && e.input == GLFW.GLFW_KEY_A)
                    {
                        if (NVListBox.this.selectionMode.get() == SelectionMode.MULTIPLE)
                        {
                            final boolean should_select = !Screen.hasShiftDown();
                            NVListBox.this.items.forEach(item -> item.setSelected(should_select));
                            
                            yield true;
                        }
                    }
                    
                    yield false;
                }
            };
        }
        
        @Override
        public boolean onKeyUp(final @NotNull KeyEvent e)
        {
            if (!this.isActive())
            {
                return false;
            }
            
            if (e.input == GLFW.GLFW_KEY_LEFT_SHIFT || e.input == GLFW.GLFW_KEY_RIGHT_SHIFT)
            {
                NVListBox.this.multiSelectMode = null;
            }
            
            return super.onKeyUp(e);
        }
        
        //==============================================================================================================
        @Override
        public void resized()
        {
            this.model.resized(this.getLocalBounds().setPosition(
                (this.getScreenX() - NVListBox.this.getScreenX()),
                (this.getScreenY() - NVListBox.this.getScreenY())));
        }
        
        //==============================================================================================================
        @Override
        public void draw(final @NotNull Canvas canvas)
        {
            this.model.draw(
                canvas,
                this.getLocalBounds().setPosition(
                    (this.getScreenX() - NVListBox.this.getScreenX()),
                    (this.getScreenY() - NVListBox.this.getScreenY())),
                this.getNavigationOrder(),
                this.selected,
                (canvas.clipRegionContains(canvas.mousePos)),
                this.isFocused());
        }
        
        //==============================================================================================================
        @Override
        public void onFocusChanged(final @NotNull GuiNavigationType type)
        {
            this.onChildFocusChanged(this, type);
        }
        
        @Override
        public void onChildFocusChanged(final @NotNull GuiComponent child, final @NotNull GuiNavigationType type)
        {
            if (child.isFocused())
            {
                NVListBox.this.scrollToItem(this.getNavigationOrder(), false);
                
                if (NVListBox.this.multiSelectMode != null && type == GuiNavigationType.KEYBOARD_ARROW)
                {
                    NVListBox.this.selectInternal(this.getNavigationOrder(), NVListBox.this.multiSelectMode);
                }
            }
        }
        
        //==============================================================================================================
        public void refresh()
        {
            if (!this.initialised)
            {
                return;
            }
            
            this.removeAllChildren();
            this.addAllChildren(this.model.getChildren());
            this.setTooltip(this.model.getTooltip(this.selected));
            this.resized();
        }
    }
    
    private class Container
        extends GuiComponent
    {
        //**************************************************************************************************************
        @Override public boolean isPinningAllowed(@NotNull GuiComponent child) { return false; }
        
        //==============================================================================================================
        @SuppressWarnings("unchecked")
        public @NotNull Optional<Item> getItemAt(final int index)
        {
            return this
                .getChildAt(index)
                .map(comp -> (Item) comp);
        }
        
        @SuppressWarnings("unchecked")
        public @NotNull Stream<Item> streamItems()
        {
            return (this.hasChildren() ? this.getChildren().stream().map(comp -> (Item) comp) : Stream.empty());
        }
        
        //==============================================================================================================
        public void deleteChildren() { this.removeAllChildren(); }
        public void addChildren(final @NotNull Collection<Item> items) { this.addAllChildren(items); }
    }
    
    //******************************************************************************************************************
    /** See {@link NVListBox#vertical}. */
    public static final boolean DEFAULT_VERTICAL = true;
    
    /** See {@link NVListBox#itemSize}. */
    public static final int DEFAULT_ITEM_SIZE = 20;
    
    /** See {@link NVListBox#defaultItem}. */
    public static final int DEFAULT_DEFAULT_ITEM_INDEX = 0;
    
    /** See {@link NVListBox#alwaysSelected}. */
    public static final boolean DEFAULT_ALWAYS_SELECTED = false;
    
    /** See {@link NVListBox#canDeselect}. */
    public static final boolean DEFAULT_CAN_DESELECT = true;
    
    /** See {@link NVListBox#canFocusItems}. */
    public static final boolean DEFAULT_CAN_FOCUS_ITEMS = false;
    
    /** See {@link NVListBox#selectionMode}. */
    public static final SelectionMode DEFAULT_SELECTION_MODE = SelectionMode.SINGLE;
    
    //******************************************************************************************************************
    /** Describes whether the orientation of the list box is vertical or horizontal. */
    public final GuiProperty.NonNull<Boolean> vertical;
    
    /**
     * Describes the mode of selection this list allows.
     * @see SelectionMode
     */
    public final GuiProperty.NonNull<SelectionMode> selectionMode;
    
    /**
     * Describes the default item to be selected if {@link #alwaysSelected} is {@code true} and no item is currently
     * selected.
     * <p>
     * If no item is in the list, none will be selected, and if the index is out of bounds the closest item.
     */
    public final GuiProperty.NonNull<Integer> defaultItem;
    
    /**
     * Describes whether this list box must always have at least one item selected,
     * if {@link #selectionMode} is anything other than {@link SelectionMode#NONE}.
     * <p>
     * If {@code true} and no item is selected, {@link #defaultItem} will be automatically selected.
     */
    public final GuiProperty.NonNull<Boolean> alwaysSelected;
    
    /**
     * Describes whether selected items can be deselected in {@link SelectionMode#SINGLE} if
     * {@link #alwaysSelected} is {@code false}.
     */
    public final GuiProperty.NonNull<Boolean> canDeselect;
    
    /** Describes whether items in the list box can gain focus through input devices. */
    public final GuiProperty.NonNull<Boolean> canFocusItems;
    
    /** Describes the size of the items in the list box (height for vertical and width for horizontal). */
    public final GuiProperty.NonNull<Integer> itemSize;
    
    //==================================================================================================================
    /** Triggered whenever an item has changed its selection state. */
    public final GuiEvent<ItemEventArgs<T>> selectionChanged = new GuiEvent<>();
    
    /** Triggered whenever an item has been added to the list (not necessarily on screen). */
    public final GuiEvent<ItemEventArgs<T>> itemAdded = new GuiEvent<>();
    
    /** Triggered whenever an item has been removed from the list (not necessarily on screen). */
    public final GuiEvent<ItemRemovedEventArgs<T>> itemRemoved = new GuiEvent<>();
    
    /** Triggered whenever an item has moved inside the list (not necessarily on screen). */
    public final GuiEvent<ItemMovedEventArgs<T>> itemMoved = new GuiEvent<>();
    
    //==================================================================================================================
    private final List<Item> items     = new ArrayList<>();
    private final Container  container = new Container();
    private final NVViewport viewport;
    
    private boolean        needsRefresh    = false;
    private boolean        sendUpdate      = true;
    private Range<Integer> moveRange       = null;
    private Boolean        multiSelectMode = null;
    private ListHandler    handler;
    
    //******************************************************************************************************************
    /**
     * Constructs a new list box with the given message.
     * @param message The message
     */
    public NVListBox(final @NotNull Text message)
    {
        super(message);
        
        this.vertical       = GuiPropertyBuilder.nonNull(NVListBox.DEFAULT_VERTICAL)
            .withSetter(this::updateOrientation)
            .build();
        this.selectionMode  = GuiPropertyBuilder.nonNull(NVListBox.DEFAULT_SELECTION_MODE)
            .withSetter(this::updateSelectionMode)
            .build();
        this.alwaysSelected = GuiPropertyBuilder.nonNull(NVListBox.DEFAULT_ALWAYS_SELECTED)
            .withSetter(val ->
            {
                if (val && this.selectionMode.get() != SelectionMode.NONE && !this.hasSelectedItems())
                {
                    this.getDefaultItem().ifPresent(def_item -> def_item.setSelected(true));
                }
            })
            .build();
        this.defaultItem    = GuiProperty.nonNull(NVListBox.DEFAULT_DEFAULT_ITEM_INDEX);
        this.canDeselect    = GuiProperty.nonNull(NVListBox.DEFAULT_CAN_DESELECT);
        this.canFocusItems  = GuiPropertyBuilder.nonNull(NVListBox.DEFAULT_CAN_FOCUS_ITEMS)
            .withNoArgSetter(this::updateFocusState)
            .build();
        this.itemSize       = GuiPropertyBuilder.nonNull(NVListBox.DEFAULT_ITEM_SIZE)
            .withSetter(val ->
            {
                int pos = 0;
                
                for (final var item : NVListBox.this.items)
                {
                    item.setBounds(this.handler.getItemBounds(pos));
                    pos += val;
                }
            })
            .build();
        
        final boolean vertical = this.vertical.get();
        
        this.handler  = (vertical ? new VerticalHandler() : new HorizontalHandler());
        this.viewport = this.addChild(new NVViewport(this.container));
        
        this.viewport.behaviourHorizontal.set(vertical
            ? NVViewport.ScrollbarBehaviour.NEVER
            : NVViewport.ScrollbarBehaviour.AUTO);
        this.viewport.behaviourVertical.set(vertical
            ? NVViewport.ScrollbarBehaviour.AUTO
            : NVViewport.ScrollbarBehaviour.NEVER);
    }
    
    /** Constructs a new list box component with an empty message. */
    public NVListBox() { this(ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    /**
     * Gets the item at the given index, or an empty optional if there is no item at the given index.
     * <p>
     * Do note that the index given is the index in the list's internal item list and not the index of the items
     * currently on screen (if {@link #refreshList()} had not been called yet).
     *
     * @param index The index to get
     * @return The item or -1 if no item was found
     */
    public @NotNull Optional<T> getItemAt(final int index)
    {
        return this
            .getInternalItemAt(index)
            .map(item -> item.model);
    }
    
    /**
     * Gets the first item in the list that is selected, or an empty optional if no item is selected or if selections
     * are disabled.
     * <p>
     * This is useful when using {@link SelectionMode#SINGLE}, when there usually is only one selected item.
     *
     * @return The selected item
     */
    public @NotNull Optional<T> getFirstSelectedItem() { return this.streamSelectedItems().findFirst(); }
    
    /**
     * Gets the index of the first selected item or {@code -1} if no item is selected or if selections are disabled.
     * <p>
     * This is useful when using {@link SelectionMode#SINGLE}, when there usually is only one selected item.
     * <p>
     * Do note that the returned index of the item is the index in the list's internal item list,
     * if there have been items that are deleted, moved or added and {@link #refreshList()} has not been called yet,
     * this index might not reflect the index of the items on screen.
     *
     * @return The index of the first selected item
     */
    public int getIndexOfFirstSelectedItem()
    {
        return IntStream
            .range(0, this.items.size())
            .filter(i -> this.items.get(i).selected)
            .findFirst()
            .orElse(-1);
    }
    
    /**
     * Gets the index of the given item.
     * <p>
     * Items are compared by identity and not by contents, this will only return a valid index if exactly the given item
     * instance is part of the list.
     * <p>
     * Do note that the returned index of the item is the index in the list's internal item list,
     * if there have been items that are deleted, moved or added and {@link #refreshList()} has not been called yet,
     * this index might not reflect the index of the items on screen.
     *
     * @param item The item instance
     * @return The index of the item, or -1 if no such item is currently part of the list
     */
    public int getIndexOfItem(final @Nullable T item)
    {
        return IntStream
            .range(0, this.items.size())
            .filter(index -> (this.items.get(index).model == item))
            .findFirst()
            .orElse(-1);
    }
    
    /**
     * Returns the number of items this list box has.
     * <p>
     * The returned number will reflect the list's internal item count, if there are items that have been removed or
     * added without calling {@link #refreshList()}, this number will not represent the items on screen.
     *
     * @return The number of items
     */
    public int getItemCount() { return this.items.size(); }
    
    /**
     * Returns the number of selected items this list box has.
     * <p>
     * The returned number will reflect the list's internal item count, if there are items that have been removed or
     * added without calling {@link #refreshList()}, this number will not represent the items on screen.
     *
     * @return The number of items
     */
    public int getSelectedCount() { return (int) this.streamSelectedItems().count(); }
    
    /**
     * Gets a list of all items currently contained in this list box.
     * <p>
     * This does not necessarily return the list of items that are currently shown on screen, especially if the items
     * were updated and {@link #refreshList()} has not been called yet.
     *
     * @return The list of items in this list box
     */
    public @NotNull List<T> getItems() { return this.streamItems().collect(Collectors.toList()); }
    
    /**
     * Gets a list of all selected items currently contained in this list box.
     * This does not necessarily return the list of items that are currently shown on screen, especially if the items
     * were updated and {@link #refreshList()} has not been called yet.
     *
     * @return The list of all selected items in this list box
     */
    public @NotNull List<T> getSelectedItems() { return this.streamSelectedItems().collect(Collectors.toList()); }
    
    /**
     * Gets the item at the given point on screen relative to the viewpoints top left corner.
     * <p>
     * If the point lies outside the viewport's bounds, or the list has no items, an empty optional will be returned.
     * <p>
     * This will only get items that are currently drawn on screen and ignore any item, which has been added or removed
     * prior to a call to {@link #refreshList()}.
     *
     * @param x The viewport relative coordinate on the x-axis
     * @param y The viewport relative coordinate on the y-axis
     * @return The item at the given point within the viewport
     */
    public @NotNull Optional<T> getItemAtPoint(final int x, final int y)
    {
        return this
            .getItemAtPointInternal(x, y)
            .map(item -> item.model);
    }
    
    /**
     * Gets the item at the given point on screen relative to the viewpoints top left corner.
     * <p>
     * If the point lies outside the viewport's bounds, or the list has no items, an empty optional will be returned.
     * <p>
     * This will only get items that are currently drawn on screen and ignore any item, which has been added or removed
     * prior to a call to {@link #refreshList()}. However, the returned index will represent the internal item index
     * and not the on-screen index.
     *
     * @param x The viewport relative coordinate on the x-axis
     * @param y The viewport relative coordinate on the y-axis
     * @return The item at the given point within the viewport
     */
    public int getItemIndexAtPoint(final int x, final int y)
    {
        return this
            .getItemAtPointInternal(x, y)
            .map(item -> this.getIndexOfItem(item.model))
            .orElse(-1);
    }
    
    @Override
    public @Nullable IComponentNavigator getNavigator()
    {
        return (this.hasItems() && this.canFocusItems.get() ? this.container.getNavigator() : null);
    }
    
    /**
     * Gets the internal viewport component managed by this list box. Use this with caution, generally only for styling
     * purposes or for changing the viewport's scroll-bar behaviour.
     * @return The {@link NVViewport}
     */
    public @NotNull NVViewport getViewport() { return this.viewport; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LIST_BOX_VERTICAL,
                this.vertical,
                Codec.BOOL),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LIST_BOX_SELECTION_MODE,
                this.selectionMode,
                SelectionMode.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LIST_BOX_DEFAULT_ITEM,
                this.defaultItem,
                Codec.INT),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LIST_BOX_ALWAYS_SELECTED,
                this.alwaysSelected,
                Codec.BOOL),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LIST_BOX_CAN_DESELECT,
                this.canDeselect,
                Codec.BOOL),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LIST_BOX_CAN_FOCUS_ITEMS,
                this.canFocusItems,
                Codec.BOOL),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LIST_BOX_ITEM_SIZE,
                this.itemSize,
                Codec.INT)
        );
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private @NotNull Optional<Item> getDefaultItem()
    {
        if (!this.hasItems())
        {
            return Optional.empty();
        }
        
        final int default_index = this.defaultItem.get();
        
        return this
            .getInternalItemAt(default_index)
            .or(() -> this.getInternalItemAt(Math.clamp(default_index, 0, this.items.size())));
    }
    
    private @NotNull Optional<Item> getInternalItemAt(final int index)
    {
        if (index < 0 || index >= this.items.size())
        {
            return Optional.empty();
        }
        
        return Optional.of(this.items.get(index));
    }
    
    private @NotNull Optional<Item> getItemAtPointInternal(final int x, final int y)
    {
        if (!this.container.hasChildren())
        {
            return Optional.empty();
        }
        
        final int screen_x = this.viewport.toScreenX(x);
        final int screen_y = this.viewport.toScreenY(y);
        
        return this.container
            .streamItems()
            .filter(item -> item.containsScreenPoint(screen_x, screen_y))
            .findFirst();
    }
    
    //==================================================================================================================
    /**
     * Gets whether this item at the given index is currently selected. If the item does not exist, this will also
     * return {@code false}
     *
     * @param index The index of the item to check
     * @return {@code true} if the item is selected
     */
    public boolean isItemSelectedAt(final int index)
    {
        return (index >= 0 && index < this.items.size() && this.items.get(index).selected);
    }
    
    /**
     * Gets whether this list box currently holds any items.
     * @return {@code true} if this list box holds items
     */
    public boolean hasItems() { return !this.items.isEmpty(); }
    
    /**
     * Gets whether this list box currently has at least one selected item.
     * @return {@code true} if there is a selected item
     */
    public boolean hasSelectedItems() { return this.streamSelectedItems().findAny().isPresent(); }
    
    /**
     * Determines whether items can be deselected.
     * <p>
     * If {@link #alwaysSelected} is {@code true} and {@link #selectionMode} is {@link SelectionMode#SINGLE},
     * this will always return {@code false}.
     * <p>
     * Other than {@link #canDeselect}, this will also return {@code true} if the current {@link #selectionMode} is
     * {@link SelectionMode#MULTIPLE}.
     *
     * @return {@code true} if items can be deselected
     */
    public boolean canDeselectItems()
    {
        final SelectionMode mode = this.selectionMode.get();
        return (mode == SelectionMode.MULTIPLE || (mode == SelectionMode.SINGLE && this.canDeselect.get()));
    }
    
    //==================================================================================================================
    /**
     * Streams the items this list box currently holds.
     * <p>
     * Do note that the returned stream concerns the items currently held inside the list box and not the items
     * currently drawn on screen.
     *
     * @return The item stream
     */
    public @NotNull Stream<T> streamItems() { return this.items.stream().map(item -> item.model); }
    
    /**
     * Streams the selected items this list box currently holds.
     * <p>
     * Do note that the returned stream concerns the items currently held inside the list box and not the items
     * currently drawn on screen.
     *
     * @return The selected item stream
     */
    public @NotNull Stream<T> streamSelectedItems()
    {
        if (this.selectionMode.get() == SelectionMode.NONE)
        {
            return Stream.empty();
        }
        
        return this.items
            .stream()
            .filter(item -> item.selected)
            .map   (item -> item.model);
    }
    
    //==================================================================================================================
    /**
     * Selects the item at the given index (if the item exists and selections are enabled).
     * <p>
     * Do note that the given index concerns the index inside the list box's internal item list and not the index of the
     * item currently drawn on screen.
     *
     * @param index The index of the item to select
     */
    public void selectItem(final int index) { this.selectInternal(index, true); }
    
    /**
     * Selects the given item (if the item is part of this list box and selections are enabled).
     * @param item The item to select
     */
    public void selectItem(final @NotNull T item) { this.selectInternal(this.getIndexOfItem(item), true); }
    
    /**
     * Selects all items in this list box.
     * <p>
     * If selection mode is not set to {@link SelectionMode#MULTIPLE}, no items will be selected.
     */
    public void selectAll()
    {
        if (this.selectionMode.get() != SelectionMode.MULTIPLE)
        {
            return;
        }
        
        IntStream
            .range(0, this.getItemCount())
            .forEach(this::selectItem);
    }
    
    /**
     * Removes the selection from the item at the given index (if the item exists and selections are enabled).
     * <p>
     * Do note that the given index concerns the index inside the list box's internal item list and not the index of the
     * item currently drawn on screen.
     *
     * @param index The index of the item to deselect
     */
    public void deselectItem(final int index) { this.selectInternal(index, false); }
    
    /**
     * Removes the selection from the given item (if the item is part of this list box and selections are enabled).
     * @param item The item to deselect
     */
    public void deselectItem(final @NotNull T item) { this.selectInternal(this.getIndexOfItem(item), false); }
    
    /** Deselects all items in this list box. */
    public void deselectAll()
    {
        final SelectionMode mode = this.selectionMode.get();
        final Item          first_selected;
        
        if (this.alwaysSelected.get() && mode != SelectionMode.NONE)
        {
            if (mode == SelectionMode.SINGLE)
            {
                return;
            }
            
            first_selected = this.items.stream().filter(i -> i.selected).findFirst().orElse(null);
        }
        else
        {
            first_selected = null;
        }
        
        this.items.forEach(item ->
        {
            if (item != first_selected)
            {
                item.setSelected(false);
            }
        });
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void selectInternal(final int index, final boolean select)
    {
        this.getInternalItemAt(index).ifPresent(item ->
        {
            final SelectionMode mode = this.selectionMode.get();
            
            if (mode == SelectionMode.NONE || item.selected == select)
            {
                return;
            }
            
            if (select)
            {
                if (mode == SelectionMode.SINGLE)
                {
                    this.sendUpdate = false;
                    this.items
                        .stream()
                        .filter(item2 -> (item2.selected))
                        .findFirst()
                        .ifPresent(item2 -> item2.setSelected(false));
                    this.sendUpdate = true;
                }
            }
            else
            {
                final boolean one_selected = (mode == SelectionMode.MULTIPLE && this.getSelectedCount() == 1);
                
                if (
                    !this.canDeselectItems()
                    || (this.alwaysSelected.get() && (mode == SelectionMode.SINGLE || one_selected))
                )
                {
                    return;
                }
            }
            
            item.setSelected(select);
        });
    }
    
    //==================================================================================================================
    /**
     * If the item is not already contained, will add the item to this list box.
     * <p>
     * This will not update the list box on the screen, to add the items to the screen,
     * {@link #refreshList()} needs to be called. Because of this, the given index represents the index of the item
     * inside the list-box's internal item list and not the index on screen.
     *
     * @param item  The item to add
     * @param index The index to insert the item at
     */
    public void addItem(final @NotNull T item, int index)
    {
        if (this.getIndexOfItem(item) != -1)
        {
            return;
        }
        
        index = Math.clamp(index, 0, this.getItemCount());
        
        final Item new_item = new Item(item);
        new_item.setWantsFocus(this.canFocusItems.get());
        this.items.add(index, new_item);
        
        this.onItemAdded(index);
        this.itemAdded.post(this, new ItemEventArgs<>(index));
        
        this.needsRefresh = true;
    }
    
    /**
     * If the item is not already contained, will add the item to the end of this list box.
     * <p>
     * This will not update the list box on the screen, to add the items to the GUI {@link #refreshList()} needs to be
     * called.
     *
     * @param item The item to add
     */
    public void addItem(final @NotNull T item) { this.addItem(item, this.getItemCount()); }
    
    /**
     * Adds all the given items that are not already contained to the end of this list box.
     * <p>
     * This will not update the list box on the screen, to add the items to the GUI {@link #refreshList()} needs to be
     * called.
     *
     * @param items The list of items to add
     */
    public void addAllItems(final @NotNull Collection<T> items) { items.forEach(this::addItem); }
    
    /**
     * Removes the item at the index from the list box if it exists.
     * <p>
     * This will not update the list box on the screen, to remove the items from the GUI {@link #refreshList()} needs to
     * be called. Because of this, the given index represents the index of the item
     * inside the list-box's internal item list and not the index on screen.
     *
     * @param index The index of the item to remove
     * @return The item, which was removed or {@code null} if there was no item at the given index
     */
    public @Nullable T removeItem(final int index)
    {
        if (index < 0 || index >= this.getItemCount())
        {
            return null;
        }
        
        this.needsRefresh = true;
        
        final T removed = this.items.remove(index).model;
        this.sendItemRemovedNotification(removed);
        
        return removed;
    }
    
    /**
     * Removes the given item from the list box if it exists.
     * <p>
     * This will not update the list box on the screen, to remove the items from the GUI {@link #refreshList()} needs to
     * be called.
     *
     * @param item The item to remove
     */
    public void removeItem(final @NotNull T item) { this.removeItem(this.getIndexOfItem(item)); }
    
    /**
     * Moves the item at the given index to the new specified index if it exists.
     * <p>
     * If the new index is below 0, it will be moved to the front; if it is above the size of items, it will be moved to
     * the end.
     * <p>
     * This will not update the list box on the screen, to rearrange the items {@link #refreshList()} needs to be
     * called. Because of this, the given indices represent the indices of the items inside the list-box's internal
     * item list and not the items on screen.
     *
     * @param oldIndex The index of the item to move
     * @param newIndex The index to move the item to
     */
    public void moveItem(final int oldIndex, int newIndex)
    {
        if (oldIndex < 0 || oldIndex >= this.getItemCount())
        {
            return;
        }
        
        newIndex = Math.clamp(newIndex, 0, this.getItemCount());
        
        if (oldIndex == newIndex)
        {
            return;
        }
        
        this.items.add(newIndex, this.items.remove(oldIndex));
        
        final int move_start = Math.min(oldIndex, newIndex);
        final int move_end   = Math.max(oldIndex, newIndex);
        
        if (this.moveRange != null)
        {
            this.moveRange = new Range<>(
                Math.min(this.moveRange.minInclusive(), move_start),
                Math.max(this.moveRange.maxInclusive(), move_end));
        }
        else
        {
            this.moveRange = new Range<>(move_start, move_end);
        }
        
        this.onItemMoved(newIndex, oldIndex);
        this.itemMoved.post(this, new ItemMovedEventArgs<>(newIndex, oldIndex));
    }
    
    /**
     * Moves the given item to the new specified index if it exists.
     * <p>
     * If the new index is below 0, it will be moved to the front; if it is above the size of items, it will be moved to
     * the end.
     * <p>
     * This will not update the list box on the screen, to rearrange the items {@link #refreshList()} needs to be
     * called. Because of this, the given index represents the index of the item inside the list-box's internal
     * item list and not the index on screen.
     *
     * @param item     The item to move
     * @param newIndex The index to move the item to
     */
    public void moveItem(final @NotNull T item, final int newIndex)
    {
        this.moveItem(this.getIndexOfItem(item), newIndex);
    }
    
    /**
     * Clears all the items that this list box currently contains.
     * <p>
     * This will not update the list box on the screen, to remove the items from the GUI {@link #refreshList()} needs to
     * be called.
     * <p>
     * However, this means the items on screen will be ghost items, and any functionality concerning them will treat
     * the list-box as empty even if they are still drawn on screen.
     */
    public void clearItems()
    {
        final List<Item> items = new ArrayList<>(this.items);
        
        this.items.clear();
        this.needsRefresh = true;
        
        items.forEach(item -> this.sendItemRemovedNotification(item.model));
    }
    
    /**
     * Scrolls the list box content by the given offset amount in pixels. If the offset is negative, this will scroll
     * backwards.
     *
     * @param offset The offset to scroll
     */
    public void scroll(final int offset)
    {
        if (this.vertical.get()) this.viewport.move(0, offset);
        else                     this.viewport.move(offset, 0);
    }
    
    /**
     * Scrolls the list box content by the given scroll delta applied {@code steps} times. If {@code steps} is
     * negative, this will scroll backwards.
     *
     * @param steps The number of times, delta should be applied to the current list box offset
     */
    public void scrollSteps(final int steps)
    {
        if (this.vertical.get()) this.viewport.moveSteps(0, steps);
        else                     this.viewport.moveSteps(steps, 0);
    }
    
    /**
     * Scrolls the list box content by the given page amount, by that means, {@code pages} times the view size.
     * If {@code pages} is negative, this will scroll backwards.
     *
     * @param pages The number of pages to scroll
     */
    public void scrollPages(final int pages)
    {
        if (this.vertical.get()) this.viewport.movePages(0, pages);
        else                     this.viewport.movePages(pages, 0);
    }
    
    /** Scrolls the list box all the way to the start. */
    public void scrollToStart()
    {
        if (this.vertical.get()) this.viewport.moveToTop();
        else                     this.viewport.moveToLeft();
    }
    
    /** Scrolls the list box all the way to the end. */
    public void scrollToEnd()
    {
        if (this.vertical.get()) this.viewport.moveToBottom();
        else                     this.viewport.moveToRight();
    }
    
    /**
     * Scrolls to the given item in the list.
     * <p>
     * If the item is not yet part of the list on the screen because it was not yet refreshed,
     * or the item is not part of the list at all, this will do nothing.
     *
     * @param item   The item to scroll to
     * @param select Whether to select the item if selections are enabled, and it is not yet selected
     */
    public void scrollToItem(final @NotNull T item, final boolean select)
    {
        this.scrollToItem(this.getIndexOfItem(item), select);
    }
    
    /**
     * Scrolls to the item at the given index in the list.
     * <p>
     * If the item is not yet part of the list on the screen because it was not yet refreshed,
     * or the item is not part of the list at all, this will do nothing.
     * <p>
     * The index of the item to scroll to, represents the index inside the list-box's internal item list and not the
     * index of the item on screen.
     *
     * @param index  The index of the item to scroll to
     * @param select Whether to select the item if selections are enabled, and it is not yet selected
     */
    public void scrollToItem(final int index, final boolean select)
    {
        if (index < 0 || index >= this.items.size())
        {
            return;
        }
        
        final Item item = this.items.get(index);
        
        if (!item.initialised)
        {
            return;
        }
        
        final int x           = (item.getScreenX() - this.viewport.getScreenX());
        final int y           = (item.getScreenY() - this.viewport.getScreenY());
        final int right       = (x + item.getWidth());
        final int bottom      = (y + item.getHeight());
        final int this_right  = this.viewport.getRight();
        final int this_bottom = this.viewport.getBottom();
        
        if (x < 0 || y < 0)
        {
            this.viewport.move(x, y);
        }
        else if (right > this_right || bottom > this_bottom)
        {
            this.viewport.move((right - this_right), (bottom - this_bottom));
        }
        
        if (select)
        {
            this.selectItem(index);
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void sendSelectionChangeNotification(final @NotNull Item item)
    {
        if (!this.sendUpdate)
        {
            return;
        }
        
        this.onSelectionChanged(item.getNavigationOrder());
        this.selectionChanged.post(this, new ItemEventArgs<>(item.getNavigationOrder()));
    }
    
    //==================================================================================================================
    /**
     * Refreshes the item at the given index inside the list box if there is an item at the index.
     * <p>
     * This is different from {@link #refreshList()} in that it refreshes only the item itself, but not the list box.
     * This can be used to update things like newly added components to an item or when the tooltip is changed.
     * <p>
     * The index of the item to refresh, represents the index inside the list-box's internal item list and not the
     * index of the items on screen.
     *
     * @param index The index of the item to update
     */
    public void refreshItem(final int index)
    {
        if (index < 0 || index >= this.getItemCount())
        {
            return;
        }
        
        this.refreshItemInternal(index);
    }
    
    /**
     * Refreshes the given item inside the list box if it exists.
     * <p>
     * This is different from {@link #refreshList()} in that it refreshes only the item itself, but not the list box.
     * This can be used to update things like newly added components to an item or when the tooltip is changed.
     *
     * @param item The item to update
     */
    public void refreshItem(final @Nullable T item)
    {
        final int index = this.getIndexOfItem(item);
        
        if (index < 0)
        {
            return;
        }
        
        this.refreshItemInternal(index);
    }
    
    /**
     * Refreshes the items inside the list box.
     * <p>
     * This is different from {@link #refreshList()} in that it refreshes only the items, but not the list box. This can
     * be used to update things like newly added components to an item or when the tooltip is changed.
     */
    public void refreshItems()
    {
        IntStream
            .range(0, this.getItemCount())
            .forEach(this::refreshItemInternal);
    }
    
    /**
     * Refreshes the component hierarchy based on the contained items in this list.
     * <p>
     * Because modifying items in a list do not automatically update the component hierarchy,
     * this needs to be called to do this. If the added or removed items are not yet refreshed, the list box is in an
     * undefined state and any undefined behaviour is possible.
     */
    public void refreshList()
    {
        if (this.needsRefresh)
        {
            final int item_size = this.itemSize.get();
            this.container.deleteChildren();
            
            if (item_size <= 0)
            {
                return;
            }
            
            this.container.addChildren(this.items);
            
            int pos = 0;
            
            for (int i = 0; i < this.items.size(); ++i)
            {
                final Item item = this.items.get(i);
                
                item.setBounds(this.handler.getItemBounds(pos));
                pos += item_size;
                item.setNavigationOrder(i);
                
                if (!item.initialised)
                {
                    item.initialised = true;
                    item.refresh();
                }
            }
            
            this.handler.accommodateItems(pos);
            
            if (this.selectionMode.get() != SelectionMode.NONE && this.alwaysSelected.get() && !this.hasSelectedItems())
            {
                this.getDefaultItem().ifPresent(def_item -> def_item.setSelected(true));
            }
        }
        else if (this.moveRange != null && this.moveRange.minInclusive() < this.getItemCount())
        {
            final int item_size = this.itemSize.get();
            final int start     = this.moveRange.minInclusive();
            final int end       = Math.min((this.moveRange.maxInclusive() + 1), this.getItemCount());
            
            int pos = 0;
            
            if (start > 0)
            {
                for (int i = (start - 1); i > 0; --i)
                {
                    final Item item = this.items.get(i);
                    
                    if (!item.initialised)
                    {
                        continue;
                    }
                    
                    pos = (this.handler.itemPos(item) + item_size);
                    break;
                }
            }
            
            for (int i = start; i < end; ++i)
            {
                final Item item = this.items.get(i);
                
                if (!item.initialised)
                {
                    continue;
                }
                
                this.handler.setPos(item, pos);
                pos += item_size;
                item.setNavigationOrder(i);
            }
        }
        
        this.needsRefresh = false;
        this.moveRange    = null;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void refreshItemInternal(final int index)
    {
        final Item item = this.items.get(index);
        item.refresh();
        item.setNavigationOrder(index);
    }
    
    //==================================================================================================================
    @Override
    public boolean onKeyDown(final @NotNull KeyEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        return switch (e.input)
        {
            case GLFW.GLFW_KEY_END ->
            {
                if (Screen.hasShiftDown()) this.viewport.moveToRight();
                else                       this.viewport.moveToBottom();
                
                if (this.canFocusItems.get() && this.container.hasChildren())
                {
                    this.container
                        .getChildAt(this.container.getChildCount() - 1)
                        .ifPresent(GuiComponent::focus);
                }
                
                yield true;
            }
            
            case GLFW.GLFW_KEY_HOME ->
            {
                if (Screen.hasShiftDown()) this.viewport.moveToLeft();
                else                       this.viewport.moveToTop();
                
                if (this.canFocusItems.get() && this.container.hasChildren())
                {
                    this.container
                        .getChildAt(0)
                        .ifPresent(GuiComponent::focus);
                }
                
                yield true;
            }
            
            case GLFW.GLFW_KEY_PAGE_DOWN ->
            {
                if (Screen.hasShiftDown()) this.viewport.movePages(1, 0);
                else                       this.viewport.movePages(0, 1);
                yield true;
            }
            
            case GLFW.GLFW_KEY_PAGE_UP ->
            {
                if (Screen.hasShiftDown()) this.viewport.movePages(-1, 0);
                else                       this.viewport.movePages(0, -1);
                yield true;
            }
            
            default -> false;
        };
    }
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        this.viewport.setBounds(this.getLocalBounds());
        this.handler.resize();
    }
    
    //==================================================================================================================
    @Override
    public void draw(final @NotNull Canvas canvas)
    {
        canvas.getTemplate().nvListBoxDrawBackground(canvas, this);
    }
    
    //==================================================================================================================
    private void sendItemRemovedNotification(final @NotNull T item)
    {
        this.onItemRemoved(item);
        this.itemRemoved.post(this, new ItemRemovedEventArgs<>(item));
    }
    
    //==================================================================================================================
    /**
     * Called whenever the selection state of an item changed.
     * @param itemIndex The index of the item
     */
    public void onSelectionChanged(int itemIndex) {}
    
    /**
     * Called whenever an item has been added to this list box (not necessarily on screen).
     * @param itemIndex The index of the item
     */
    public void onItemAdded(int itemIndex) {}
    
    /**
     * Called whenever an item has been removed from this list box (not necessarily on screen).
     * @param item The item that was removed
     */
    public void onItemRemoved(@NotNull T item) {}
    
    /**
     * Called whenever an item has moved inside the list (not necessarily on screen).
     * @param itemIndex The new item index
     * @param oldIndex  The old item index
     */
    public void onItemMoved(int itemIndex, int oldIndex) {}
    
    //==================================================================================================================
    private void updateFocusState()
    {
        final boolean can_focus = this.canFocusItems.get();
        
        if (!can_focus)
        {
            this.items
                .stream()
                .filter(GuiComponent::hasFocus)
                .findAny()
                .ifPresent(GuiComponent::blur);
        }
        
        this.items.forEach(component -> component.setWantsFocus(can_focus));
    }
    
    private void updateSelectionMode(final @NotNull SelectionMode mode)
    {
        this.deselectAll();
        this.updateFocusState();
        
        if (mode != SelectionMode.NONE && this.alwaysSelected.get())
        {
            this.getDefaultItem().ifPresent(def_item -> def_item.setSelected(true));
        }
    }
    
    private void updateOrientation(final boolean vertical)
    {
        this.handler = (vertical ? new VerticalHandler() : new HorizontalHandler());
        this.needsRefresh = true;
        this.refreshList();
        this.scrollToStart();
    }
}

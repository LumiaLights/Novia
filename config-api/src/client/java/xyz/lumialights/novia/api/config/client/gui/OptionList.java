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

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.core.serialisation.IValueConvertible;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumialights.novia.api.gui.component.provided.INVItemModel;
import xyz.lumialights.novia.api.gui.component.provided.NVListBox;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.component.provided.NVLabel;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class OptionList
    extends NVListBox<OptionList.BaseItem>
{
    //******************************************************************************************************************
    public sealed interface BaseItem
        extends INVItemModel
        permits
            GroupItem,
            OptionItem
    {}
    
    public record GroupItem(@NotNull Text text)
        implements BaseItem
    {
        //**************************************************************************************************************
        @Override
        public void draw(@NotNull Canvas    canvas,
                         @NotNull Rectangle bounds,
                         int                index,
                         boolean            selected,
                         boolean            hovered,
                         boolean            focused)
        {
            canvas.drawText(this.text, bounds.withLocalPos(), Alignment.BOTTOM_LEFT);
        }
    }
    
    public final class OptionItem
        implements
            BaseItem,
            IValueConvertible
    {
        //**************************************************************************************************************
        public final PropertyId            id;
        public final NVLabel               label;
        public final IStatefulComponent<?> component;
        
        //**************************************************************************************************************
        public OptionItem(final @NotNull  PropertyId            id,
                          final @NotNull  Text                  title,
                          final @Nullable Text                  description,
                          final @NotNull  IStatefulComponent<?> component)
        {
            this.id        = id;
            this.component = component;
            
            this.label = new NVLabel(title);
            this.label.textAlign.set(Alignment.MIDDLE_LEFT);
            this.label.setColour(NVLabel.COLOUR_TEXT, 0xFFAAAAAA);
            
            if (description != null)
            {
                this.label.setTooltip(Tooltip.of(description));
            }
        }
        
        //==============================================================================================================
        @Override
        public @NotNull List<GuiComponent> getChildren()
        {
            return List.of(this.label, this.getComponent());
        }
        
        public @NotNull GuiComponent getComponent() { return (GuiComponent) this.component; }
        
        //==============================================================================================================
        @Override public @NotNull Value getValue() { return this.component.getValue(); }
        
        //==============================================================================================================
        @Override public void setValue(final @NotNull Value value) { this.component.setValue(value); }
        
        //==============================================================================================================
        @Override
        public void resized(final @NotNull Rectangle bounds)
        {
            final Rectangle local_bounds = bounds.withLocalPos();
            final int       indent       = (OptionList.this.displayMode.categorised ? 8 : 0);
            
            this.label.setBounds(local_bounds
                .withRightPadding(OPTION_COMPONENT_WIDTH + indent)
                .withTranslationX(indent));

            final GuiComponent component = this.getComponent();
            component.setBounds(local_bounds
                .withWidth(OPTION_COMPONENT_WIDTH)
                .withX(this.label.getRight())
                .withPadding(0, 1, 4, 0));
            component.setPosition(
                component.getX(),
                (int) (local_bounds.y() + ((local_bounds.height() - component.getHeight()) * 0.5)));
        }
    }
    
    //==================================================================================================================
    public record DisplayMode(@Nullable Comparator<Text> groupComparator,
                              @Nullable Comparator<Text> optionComparator,
                                        boolean          categorised)
    {
        //**************************************************************************************************************
        public @NotNull List<BaseItem> apply(final @NotNull List<Group> options)
        {
            return (this.categorised ? this.applyCategorised(options) : this.applyUncategorised(options));
        }
        
        //--------------------------------------------------------------------------------------------------------------
        private @NotNull List<BaseItem> applyCategorised(final @NotNull List<Group> options)
        {
            Stream<Group> stream = options.stream();
            
            if (this.groupComparator != null)
            {
                stream = stream.sorted(Comparator.comparing((g -> g.groupItem.text), this.groupComparator));
            }
            
            return stream
                .flatMap(e ->
                {
                    Stream<OptionItem> items = e.optionItems.stream();
                    
                    if (this.optionComparator != null)
                    {
                        items = items.sorted(Comparator.comparing((o -> o.label.getMessage()), this.optionComparator));
                    }

                    return (e.groupItem != null ? Stream.concat(Stream.of(e.groupItem), items) : items);
                })
                .toList();
        }
        
        private @NotNull List<BaseItem> applyUncategorised(final @NotNull List<Group> options)
        {
            Stream<OptionItem> stream = options
                .stream()
                .flatMap(e -> e.optionItems.stream());
            
            if (this.optionComparator != null)
            {
                stream = stream.sorted(Comparator.comparing((o -> o.label.getMessage()), this.optionComparator));
            }
            
            return stream.collect(Collectors.toUnmodifiableList());
        }
    }
    
    public class Group
    {
        //**************************************************************************************************************
        private final GroupItem        groupItem;
        private final List<OptionItem> optionItems;
        
        //**************************************************************************************************************
        public Group(final @Nullable Text title, final @NotNull List<OptionItem> optionItems)
        {
            this.groupItem   = (title != null ? new GroupItem(title) : null);
            this.optionItems = optionItems;
        }
        
        //==============================================================================================================
        public void addProperty(final @NotNull  PropertyId            propertyId,
                                final @NotNull  Text                  title,
                                final @Nullable Text                  description,
                                final @NotNull  IStatefulComponent<?> component)
        {
            this.optionItems.add(new OptionItem(propertyId, title, description, component));
        }
    }
    
    //******************************************************************************************************************
    private static final int OPTION_COMPONENT_WIDTH = 120;
    
    //******************************************************************************************************************
    private final List<Group> groups = new ArrayList<>();
    
    private DisplayMode displayMode = new DisplayMode(null, null, true);
    
    //******************************************************************************************************************
    public OptionList()
    {
        this.itemSize.set(18);
        this.selectionMode.set(SelectionMode.NONE);
    }
    
    //==================================================================================================================
    public @NotNull DisplayMode getDisplayMode() { return this.displayMode; }
    
    //==================================================================================================================
    public void setDisplayMode(final @NotNull DisplayMode displayMode)
    {
        if (!this.displayMode.equals(displayMode))
        {
            this.displayMode = displayMode;
            this.refreshList();
        }
    }
    
    //==================================================================================================================
    public @NotNull Group addGroup(final @Nullable Text title)
    {
        final Group option = new Group(title, new ArrayList<>());
        this.groups.add(option);
        return option;
    }
    
    public void clearOptions()
    {
        this.groups.clear();
        this.refreshList();
    }
    
    @Override
    public void refreshList()
    {
        this.clearItems();
        this.addAllItems(this.displayMode.apply(this.groups));
        super.refreshList();
    }
}

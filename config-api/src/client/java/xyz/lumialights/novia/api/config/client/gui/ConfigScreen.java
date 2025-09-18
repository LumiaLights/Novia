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

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.ConfigManager;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.config.client.document.ScreenDocument;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.provider.BaseNetworkProvider;
import xyz.lumialights.novia.api.config.provider.IConfigProvider;

import java.util.*;

import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.gui.component.GuiScreen;
import xyz.lumialights.novia.api.gui.component.IComponentNavigator;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumialights.novia.api.gui.component.provided.NVLabel;
import xyz.lumialights.novia.api.gui.util.KeyboardUtil;



//**********************************************************************************************************************
public class ConfigScreen
    extends GuiScreen
{
    //******************************************************************************************************************
    private final ConfigScreenLookAndFeel    lookAndFeel;
    private final TabManager                 tabManager;
    private final TabList                    tabList;
    private final OptionList                 optList;
    private final NVLabel                    title;
    private final TreeMap<PropertyId, Value> cache;
    //private final EditScreen                 editScreen;
    
    //******************************************************************************************************************
    ConfigScreen(@NotNull final Screen                  parent,
                 @NotNull final String                  modId,
                 @NotNull final ScreenDocument          document,
                 @NotNull final ConfigScreenLookAndFeel lookAndFeel)
    {
        super(document.title().orElseGet(() -> Text.literal(modId)), parent);
        
        Objects.requireNonNull(modId,       "modId must not be null");
        Objects.requireNonNull(document,    "document must not be null");
        Objects.requireNonNull(lookAndFeel, "Look and feel must not be null");
        
        this.lookAndFeel = lookAndFeel;
        this.cache       = new TreeMap<>(Comparator.comparing(e -> e.providerId().toString()));
        
        this.title = this.addChild(this.lookAndFeel.createTitleWidget(this.getMessage()));
        
        this.tabList = this.addChild(new TabList());
        this.tabList.addSelectionListener(this::onTabChanged);
        
        this.optList = this.addChild(new OptionList());
        
        this.tabManager = new TabManager(1);
        this.tabManager.reloadTabs(document);
        this.tabManager.getEntries().forEach(tab -> this.tabList.addTab(
            tab.title(),
            tab.colour().orElse(new Colour(0xFF3C8527))));
        this.tabList.refreshList();
        
        //this.editScreen = new EditScreen(this, this.lookAndFeel, this::valueChanged);

        this.setFont(lookAndFeel.getDefaultFont());
    }
    
    //==================================================================================================================
    @Override public @Nullable IComponentNavigator getNavigator() { return this.optList.getNavigator(); }
    
    //==================================================================================================================
    public void showEditScreen(@Nullable final PropertyId            propertyId,
                               @NotNull  final Text                  title,
                               @NotNull  final IStatefulComponent<?> content)
    {
        //this.editScreen.setContent(propertyId, title, content);
    }
    
    //==================================================================================================================
    @Override
    protected void resized()
    {
        this.lookAndFeel.resize(this.getLocalBounds());
        
        this.title.setBounds(this.lookAndFeel.getTitleBounds(
            this.title.getMessage(),
            this.lookAndFeel.getConfigPanelClientBounds()));
        this.tabList.setBounds(this.lookAndFeel.getTabListBounds());
        this.optList.setBounds(this.lookAndFeel.getOptionListBounds());
    }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        super.draw(canvas);
        this.lookAndFeel.drawBackground(canvas);
    }
    
    //==================================================================================================================
    private void onTabChanged(final @NotNull TabList.Item tabItem, final int index, final boolean selected)
    {
        this.optList.clearOptions();
        this.tabManager.streamGroups(index).forEach(this::addGroup);
        this.optList.refreshList();
    }
    
    private void addGroup(@NotNull final TabManager.FactoryGroup group)
    {
        final OptionList.Group option = this.optList.addGroup(group.title().orElse(null));
        group.entries().forEach(property ->
        {
            final IStatefulComponent<?> component = property.factory().create(this);
            component.setValue(property.getProperty().getValue());
            component.addChangeListener(comp -> this.valueChanged(property.id(), comp.getValue()));
            option.addProperty(property.id(), property.title(), property.description(), component);
        });
    }
    
    //==================================================================================================================
    @Override
    protected boolean onKeyDown(final @NotNull KeyEvent e)
    {
        final int number = KeyboardUtil.getKeyNumber(e.input);

        if (number > -1 && number < this.tabList.getItemCount())
        {
            this.tabList.selectItem(number);
            return true;
        }

        return super.onKeyDown(e);
    }

    //==================================================================================================================
    private void valueChanged(@NotNull final PropertyId id, @Nullable final Value value)
    {
        if (value == null)
        {
            this.cache.remove(id);
            return;
        }
        
        final Property property = this.getProperty(
            id.pointer(),
            Objects.requireNonNull(ConfigManager.REGISTRY.get(id.providerId())));
        
        if (property.getValue().equals(value))
        {
            this.cache.remove(id);
        }
        else
        {
            this.cache.put(id, value);
        }
    }
    
    private <Container> @NotNull Property getProperty(@NotNull final JsonPointer                pointer,
                                                      @NotNull final IConfigProvider<Container> provider)
    {
        final Container container = (provider instanceof BaseNetworkProvider<Container> bnp
            ? bnp.getClientContainer()
            : provider.getManagedContainer());
        assert (container != null);
        
        return provider.getSpec().flatPropertySpecs().get(pointer).get(container);
    }
}

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
package xyz.lumialights.novia.api.gui.component;

import com.mojang.datafixers.util.Function3;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.CanvasAttorney;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Point;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.impl.GuiRenderStateAccessor;
import xyz.lumialights.novia.api.gui.util.MouseUtil;

import java.util.*;



//**********************************************************************************************************************
public final class ScreenInterop
    extends Screen
{
    //******************************************************************************************************************
    private record ComponentNarrationData(
        @NotNull GuiComponent             selectable,
                 int                      index,
        @NotNull Selectable.SelectionType type
    ) {}
    
    //******************************************************************************************************************
    private static @Nullable ComponentNarrationData findSelectedComponentData(
        final @NotNull  List<? extends GuiComponent> components,
        final @Nullable GuiComponent                 selectable
    )
    {
        ComponentNarrationData narration_data  = null;
        ComponentNarrationData narration_data2 = null;
        int                                i               = 0;

        for (int j = components.size(); i < j; ++i)
        {
            final GuiComponent             selectable2 = components.get(i);
            final Selectable.SelectionType type;
            
            if      (selectable2.isHovered()) type = Selectable.SelectionType.HOVERED;
            else if (selectable2.isFocused()) type = Selectable.SelectionType.FOCUSED;
            else                              type = Selectable.SelectionType.NONE;
            
            if (type.isFocused())
            {
                if (selectable2 != selectable)
                {
                    return new ComponentNarrationData(selectable2, i, type);
                }

                narration_data2 = new ComponentNarrationData(selectable2, i, type);
            }
            else if (type.compareTo(narration_data != null ? narration_data.type : Selectable.SelectionType.NONE) > 0)
            {
                narration_data = new ComponentNarrationData(selectable2, i, type);
            }
        }

        return (narration_data != null ? narration_data : narration_data2);
    }

    //******************************************************************************************************************
    private final List<ContentLayer> layers = new ArrayList<>(2);
    private final List<ScreenLayer>  addonLayers;
    private final GuiScreen          guiScreen;
    private final Screen             parent;
    private final TooltipLayer       tooltipLayer;
    private final BackgroundLayer    backgroundLayer;
    
    private GuiComponent hovered    = null;
    private GuiComponent selected   = null;
    private DevConsole   devConsole = null;
    private boolean      isF3Down   = false;
    
    //******************************************************************************************************************
    ScreenInterop(final @Nullable Screen parent, final @NotNull GuiScreen guiScreen)
    {
        super(guiScreen.getMessage());
        
        this.guiScreen       = guiScreen;
        this.parent          = parent;
        this.backgroundLayer = new BackgroundLayer(this, guiScreen);
        this.addonLayers     = List.of(
            (this.tooltipLayer = new TooltipLayer(this))
        );

        this.setDragging(false);
    }
    
    //==================================================================================================================
    @Override public @Nullable Element getFocused() { return null; }
    public @Nullable GuiComponent getHovered() { return this.hovered; }
    
    public @Nullable ScreenLayer getLayerForComponent(final @NotNull GuiComponent layerComponent)
    {
        return this.layers
            .reversed()
            .stream()
            .filter(layer -> layer.content == layerComponent)
            .findFirst()
            .orElse(null);
    }
    
    public @Nullable GuiComponent getScreenComponentAt(final int screenX, final int screenY)
    {
        if (this.devConsole != null)
        {
            final GuiComponent component = this.devConsole.getComponentAt(screenX, screenY);
            
            if (component != this.devConsole)
            {
                return component;
            }
        }
        
        for (final var layer : this.layers.reversed())
        {
            final GuiComponent target = layer.getComponentAt(screenX, screenY);
            
            if (target != null)
            {
                if (target == ModalLayer.END)
                {
                    return null;
                }
                
                return target;
            }
        }
        
        return null;
    }
    
    @Override public @Nullable MusicSound getMusic() { return this.guiScreen.getMusic(); }
    
    public @NotNull IGuiTemplate getTemplate()
    {
        return Objects.requireNonNullElse(this.guiScreen.template, IGuiTemplate.DEFAULT);
    }
    
    public @NotNull GuiFont getFont()
    {
        return Objects.requireNonNullElseGet(this.guiScreen.font, GuiFont.DEFAULT);
    }

    //==================================================================================================================
    public boolean isShowing() { return (GuiScreen.CURRENT_SCREEN == this.guiScreen); }
    
    @Override public boolean isFocused() { return this.guiScreen.hasFocus(); }
    
    //==================================================================================================================
    public void setHovered(final @Nullable GuiComponent component)
    {
        final GuiComponent prev_hovered = this.hovered;
        
        if (component == prev_hovered)
        {
            return;
        }
        
        this.hovered = component;
        
        if (prev_hovered != null)
        {
            prev_hovered.onMouseExit(new MouseEvent(prev_hovered, MouseUtil.getScaledMousePos(), 0, 0));
        }
        
        if (component != null)
        {
            component.onMouseEnter(new MouseEvent(component, MouseUtil.getScaledMousePos(), 0, 0));
        }
        
        this.tooltipLayer.set(this.hovered, true);
    }
    
    @Override
    public void setFocused(final boolean focused)
    {
        if (focused) this.guiScreen.focus();
        else         this.blur();
    }
    
    @Override public void setFocused(@Nullable Element focused) {}
    
    //==================================================================================================================
    public boolean addLayer(final @NotNull ContentLayer layer)
    {
        if (this.getLayerForComponent(layer.content) != null)
        {
            return false;
        }
        
        if (!this.layers.isEmpty() && this.layers.getLast() instanceof ModalLayer modal)
        {
            if (modal.args.closeOnDistraction())
            {
                this.removeLayer(modal);
            }
        }
        
        this.layers.addLast(layer);
        layer.init();
        
        this.updateHoverState();
        
        return true;
    }
    
    public boolean removeLayer(final @NotNull ContentLayer layer)
    {
        if (!this.layers.contains(layer))
        {
            return false;
        }
        
        if (this.layers.getLast() instanceof ModalLayer modal && modal != layer)
        {
            if (modal.args.closeOnDistraction())
            {
                this.removeLayer(modal);
            }
        }
        
        layer.release();
        this.layers.remove(layer);
        
        this.updateHoverState();
        
        return true;
    }
    
    public boolean removeModal(final @NotNull GuiComponent layerComponent)
    {
        final ScreenLayer layer = this.getLayerForComponent(layerComponent);
        
        if (layer instanceof ModalLayer modal)
        {
            this.removeLayer(modal);
            return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    @Override
    protected void addElementNarrations(final @NotNull NarrationMessageBuilder builder)
    {
        final List<GuiComponent> list = this.layers.reversed()
            .stream()
            .map(layer -> layer.content)
            .filter(component -> (component.hasActiveFlag() && component.isVisible()))
            .toList();
        
        final ComponentNarrationData selected_data = ScreenInterop.findSelectedComponentData(list, this.selected);
        
        if (selected_data != null)
        {
            if (selected_data.type.isFocused())
            {
                this.selected = selected_data.selectable;
            }

            if (list.size() > 1)
            {
                builder.put(
                    NarrationPart.POSITION,
                    Text.translatable("narrator.position.screen", (selected_data.index + 1), list.size()));
                
                if (selected_data.type.isFocused())
                {
                    builder.put(NarrationPart.USAGE, this.getUsageNarrationText());
                }
            }

            selected_data.selectable.appendNarrations(builder.nextMessage());
        }
    }
    
    private void toggleDevConsole()
    {
        // TODO FUTURE add dev console
        if (true) return;
        
        if (this.devConsole != null)
        {
            this.devConsole = null;
        }
        else
        {
            this.devConsole = new DevConsole();
            this.devConsole.setBounds(0, 0, this.width, this.height);
        }
    }
    
    //==================================================================================================================
    @Override
    public void blur()
    {
        if (this.devConsole != null && this.devConsole.hasFocus())
        {
            this.devConsole.blur();
            return;
        }
        
        this.layers
            .stream()
            .filter(layer -> layer.content.hasFocus())
            .findFirst()
            .ifPresent(layer -> layer.content.blur());
    }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    protected void setInitialFocus()
    {
        assert (this.client != null);
        
        if (!this.client.getNavigationType().isKeyboard())
        {
            return;
        }
        
        this.findComponent(IComponentNavigator::findFirst, new GuiNavigation.Tab(true))
            .ifPresent(this::switchFocus);
    }

    //------------------------------------------------------------------------------------------------------------------
    void switchFocus(final @NotNull GuiComponent component)
    {
        component.updateFocusState(MinecraftClient.getInstance().getNavigationType(), true, true);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private @NotNull Optional<GuiComponent> findComponent(
        final @NotNull Function3<IComponentNavigator, GuiComponent, GuiNavigation, Optional<GuiComponent>> function,
        final @NotNull GuiNavigation                                                                       nav
    )
    {
        if (this.devConsole != null)
        {
            final IComponentNavigator navigator = this.devConsole.getNavigator();
            
            if (navigator != null)
            {
                final Optional<GuiComponent> component = function.apply(navigator, this.devConsole, nav);
                
                if (component.isPresent())
                {
                    return component;
                }
            }
        }
        
        for (int i = (this.layers.size() - 1); i > 0; --i)
        {
            final ModalLayer modal = (ModalLayer) this.layers.get(i);
            
            final IComponentNavigator navigator = modal.content.getNavigator();
            
            if (navigator == null)
            {
                continue;
            }
            
            final Optional<GuiComponent> component = function.apply(navigator, modal.content, nav);
            
            if (component.isPresent())
            {
                return component;
            }
            
            if (modal.args.wantsAllInput())
            {
                return Optional.empty();
            }
        }
        
        final GuiComponent base = this.layers.getFirst().content;
        return Optional
            .ofNullable(base.getNavigator())
            .flatMap(navigator -> function.apply(navigator, base, nav));
    }
    
    //==================================================================================================================
    @Override
    protected void init()
    {
        final Rectangle bounds = new Rectangle(0, 0, this.width, this.height);
        this.layers     .forEach(layer -> layer.resized(bounds));
        this.addonLayers.forEach(layer -> layer.resized(bounds));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void initScreen()
    {
        this.addLayer(new RootContentLayer(this.guiScreen, this));
        GuiScreen.CURRENT_SCREEN = this.guiScreen;
        this.guiScreen.onScreenOpened();
    }
    
    private void resetScreen()
    {
        this.blur();
        
        this.setHovered(null);
        this.tooltipLayer.set(null, false);
        this.setDragging(false);
        
        List.copyOf(this.layers.reversed()).forEach(this::removeLayer);
        
        GuiScreen.CURRENT_SCREEN = null;
        this.guiScreen.onScreenClosed();
    }
    
    //==================================================================================================================
    @Override
    public void mouseMoved(final double mouseX, final double mouseY)
    {
        if (this.isDragging())
        {
            return;
        }
        
        final Point        mouse_pos = new Point((int) mouseX, (int) mouseY);
        final GuiComponent target    = mouse_pos.transform(this::getScreenComponentAt);
        
        this.setHovered(target);
        
        if (this.hovered != null)
        {
            (new MouseEvent(this.hovered, mouse_pos, 0, 0)).post(GuiComponent::onMouseMove);
        }
    }
    
    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
    {
        final ModalLayer modal_to_remove;
        
        if (
            this.layers.getLast() instanceof ModalLayer modal
            && modal.args.closeOnDistraction()
            && !MouseUtil.hitTest(modal.content, mouseX, mouseY)
        )
        {
            if (modal.args.wantsAllInput())
            {
                this.removeLayer(modal);
                return false;
            }
            
            modal_to_remove = modal;
        }
        else
        {
            modal_to_remove = null;
        }
        
        if (this.hovered == null)
        {
            return false;
        }
        
        if (!this.hovered.isFocused())
        {
            this.hovered.updateFocusState(GuiNavigationType.MOUSE, true, true);
        }
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            this.setDragging(true);
        }
        
        final boolean result = (new MouseEvent(this.hovered, new Point((int) mouseX, (int) mouseY), 0, 0, button))
            .post(GuiComponent::onMouseDown);
        
        if (modal_to_remove != null)
        {
            this.removeLayer(modal_to_remove);
        }
        
        return result;
    }
    
    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button)
    {
        final boolean was_dragging = this.isDragging();
        this.setDragging(false);
    
        if (!was_dragging || this.hovered == null)
        {
            return false;
        }
        
        final Point   mouse_pos = new Point((int) mouseX, (int) mouseY);
        final boolean result    = (new MouseEvent(this.hovered, mouse_pos, 0, 0, button)).post(GuiComponent::onMouseUp);
        
        this.updateHoverState(mouse_pos);
        
        return result;
    }
    
    @Override
    public boolean mouseDragged(final double mouseX,
                                final double mouseY,
                                final int    button,
                                final double deltaX,
                                final double deltaY)
    {
        if (!this.isDragging() || this.hovered == null)
        {
            this.setDragging(false);
            return false;
        }
        
        return (new MouseEvent(this.hovered, new Point((int) mouseX, (int) mouseY), deltaX, deltaY, button))
            .post(GuiComponent::onMouseDrag);
    }
    
    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double deltaX, final double deltaY)
    {
        if (this.isDragging() || this.hovered == null)
        {
            return false;
        }
        
        return (new MouseEvent(this.hovered, new Point((int) mouseX, (int) mouseY), deltaX, deltaY))
            .post(GuiComponent::onMouseScroll);
    }
    
    //==================================================================================================================
    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers)
    {
        if (FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            if (keyCode == GLFW.GLFW_KEY_F3)
            {
                this.isF3Down = true;
            }
            else if (this.isF3Down && keyCode == GLFW.GLFW_KEY_X)
            {
                this.toggleDevConsole();
                return true;
            }
        }
        
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            int most_recent_closeable = 0;
            
            for (int i = (this.layers.size() - 1); i > 0; i--)
            {
                final ModalLayer modal = (ModalLayer) this.layers.get(i);
                
                if (modal.args.closeOnEscape())
                {
                    most_recent_closeable = i;
                    break;
                }
            }
            
            if (most_recent_closeable > 0)
            {
                final List<ContentLayer> prev_layers = this.layers.subList(most_recent_closeable, this.layers.size());
                prev_layers.reversed().forEach(ScreenLayer::release);
                
                final List<ContentLayer> new_layers = this.layers.subList(0, most_recent_closeable);
                this.layers.clear();
                this.layers.addAll(new_layers);
                
                this.updateHoverState();
            }
            else
            {
                this.close();
            }
            
            return false;
        }
        
        if (
            GuiComponent.FOCUSED != null
            && (new KeyEvent(GuiComponent.FOCUSED, keyCode, scanCode, modifiers)).post(GuiComponent::onKeyDown)
        )
        {
            return true;
        }
        
        final GuiNavigation nav = switch (keyCode)
        {
            case GLFW.GLFW_KEY_TAB   -> new GuiNavigation.Tab  (!Screen.hasShiftDown());
            case GLFW.GLFW_KEY_RIGHT -> new GuiNavigation.Arrow(NavigationDirection.RIGHT);
            case GLFW.GLFW_KEY_LEFT  -> new GuiNavigation.Arrow(NavigationDirection.LEFT);
            case GLFW.GLFW_KEY_DOWN  -> new GuiNavigation.Arrow(NavigationDirection.DOWN);
            case GLFW.GLFW_KEY_UP    -> new GuiNavigation.Arrow(NavigationDirection.UP);
            default                  -> null;
        };
        
        if (nav == null)
        {
            return false;
        }
        
        this.findComponent(IComponentNavigator::findNext, nav)
            .or(() -> this.findComponent(IComponentNavigator::findFirst, nav))
            .ifPresent(next ->
            {
                this.switchFocus(next);
                this.tooltipLayer.set(next, false);
            });
        
        return false;
    }
    
    @Override
    public boolean keyReleased(final int keyCode, final int scanCode, final int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_F3)
        {
            this.isF3Down = false;
        }
        
        if (GuiComponent.FOCUSED == null)
        {
            return false;
        }
        
        return (new KeyEvent(GuiComponent.FOCUSED, keyCode, scanCode, modifiers)).post(GuiComponent::onKeyUp);
    }
    
    @Override
    public boolean charTyped(final char character, final int modifiers)
    {
        if (GuiComponent.FOCUSED == null)
        {
            return false;
        }
        
        return (new KeyEvent(GuiComponent.FOCUSED, character, modifiers)).post(GuiComponent::onInput);
    }
    
    //==================================================================================================================
    @ApiStatus.Internal
    @Override
    public void onDisplayed()
    {
        if (MinecraftClient.getInstance().currentScreen == this && GuiScreen.CURRENT_SCREEN != this.guiScreen)
        {
            this.initScreen();
        }
    }
    
    @ApiStatus.Internal
    @Override
    public void removed()
    {
        if (MinecraftClient.getInstance().currentScreen == this && GuiScreen.CURRENT_SCREEN == this.guiScreen)
        {
            this.resetScreen();
        }
    }
    
    //==================================================================================================================
    @Override
    public void render(final @NotNull DrawContext context, final int mouseX, final int mouseY, final float deltaTicks)
    {
        ((GuiRenderStateAccessor) context.state).novia$disableSorting(true);
        
        final Canvas canvas = new Canvas(context, new Point(mouseX, mouseY), deltaTicks);
        CanvasAttorney.initFramebuffer(canvas, this.guiScreen);
        
        CanvasAttorney.setLayer(canvas, this.backgroundLayer);
        this.backgroundLayer.render(canvas);
        
        for (final var layer : this.layers)
        {
            CanvasAttorney.setLayer(canvas, layer);
            layer.render(canvas);
        }
        
        for (final var addon_layer : this.addonLayers)
        {
            addon_layer.render(canvas);
        }
    }

    //==================================================================================================================
    @Override
    public void close()
    {
        if (GuiScreen.CURRENT_SCREEN != this.guiScreen)
        {
            return;
        }
        
        assert (this.client != null);
        this.client.setScreen(this.parent);
    }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        final Text title = this.getTitle();
        return "Screen#" + (title.equals(Text.EMPTY) ? "Unnamed" : title.getString());
    }
    
    //==================================================================================================================
    public void updateHoverState(final @NotNull Point mousePos)
    {
        if (!this.isDragging())
        {
            this.setHovered(mousePos.transform(this::getScreenComponentAt));
        }
    }
    
    public void updateHoverState() { this.updateHoverState(MouseUtil.getScaledMousePos()); }
    
    public void updateTooltip(final @NotNull GuiComponent component) { this.tooltipLayer.updateTarget(component); }
}

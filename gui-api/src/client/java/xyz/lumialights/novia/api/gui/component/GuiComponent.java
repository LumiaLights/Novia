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

import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.GuiApiLang;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.core.util.NoviaCollectors;
import xyz.lumialights.novia.api.gui.canvas.*;
import xyz.lumialights.novia.api.gui.component.integration.IInputListener;
import xyz.lumialights.novia.api.gui.component.integration.INavigable;
import xyz.lumialights.novia.api.gui.component.integration.IHierarchyListener;
import xyz.lumialights.novia.api.gui.event.GuiEvent;
import xyz.lumialights.novia.api.gui.event.GuiEventArgs;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Point;
import xyz.lumialights.novia.api.gui.geometry.Positioner;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.geometry.Restrainer;
import xyz.lumialights.novia.api.gui.property.GuiProperty;
import xyz.lumialights.novia.api.gui.property.IGuiProperty;

import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;



//**********************************************************************************************************************
/**
 * Represents a (usually) rectangular area that draws onto the screen and can receive mouse events, this is the
 * replacement for Minecraft's {@link ClickableWidget} class.
 * <p>
 * A gui component has bounds, properties and state (such as visibility, enablement, focus etc.), which all define
 * how the component will be rendered and how it behaves. Other than Minecraft's widgets, components draw in a relative
 * coordinate space, by that means drawing code does not declare render actions in absolute screen coordinates but
 * start at [0, 0], which represents the top-left corner of the current component. This is also the case for setting
 * a components position inside its parent (see {@link GuiComponent#setBounds(Rectangle)}).
 * Additionally, every component is also a parent component so that each of them can be infinitely nested;
 * though this should be done in moderation as every level introduces additional indirections such as for mouse
 * events and drawing.
 * <p>
 * Each component has a {@link GuiFont} and {@link IGuiTemplate} that aid components in instructing how they should
 * be rendered, a component can either be a font/template inheritor or declarator. If a font/template is explicitly set
 * for component, it and all its children (that do not explicitly themselves declare any) will use that font/template
 * for rendering; otherwise if it is an inheritor, the font/template applied will be inherited from its closest
 * explicitly declaring parent. If, however, no parent is a font/template declarator, the used fonts and templates will
 * resort to its defaults (namely {@link GuiFont#DEFAULT)} and {@link IGuiTemplate#DEFAULT}).
 * <p>
 * For mouse and keyboard events, every component has a set of functions that can be overridden on demand:
 * <table>
 *     <tr>
 *         <th>Name</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onMouseMove(MouseEvent)}</td>
 *         <td>Called whenever the mouse moved over the component directly and not over any of its children</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onMouseEnter(MouseEvent)}</td>
 *         <td>Called whenever the mouse entered the bounds of the component (not if it is a child)</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onMouseExit(MouseEvent)}</td>
 *         <td>Called whenever the mouse left the bounds of the component (also when it entered a child)</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onMouseDrag(MouseEvent)}</td>
 *         <td>Called whenever any mouse button was pressed on a component and is now being moved around</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onMouseDown(MouseEvent)}}</td>
 *         <td>Called whenever any mouse button was pressed on a component</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onMouseUp(MouseEvent)}}</td>
 *         <td>Called whenever any mouse button was released on a component</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onMouseScroll(MouseEvent)}}</td>
 *         <td>Called whenever the mouse wheel was scrolled when over a component</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onKeyDown(KeyEvent)}}</td>
 *         <td>Called whenever a keyboard button was pressed while the component is focused</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onKeyUp(KeyEvent)}}</td>
 *         <td>Called whenever a keyboard button was released while the component is focused</td>
 *     </tr>
 *     <tr>
 *         <td>{@link GuiComponent#onInput(KeyEvent)}}</td>
 *         <td>Called whenever a keyboard button, that corresponds to a text character, was pressed and released</td>
 *     </tr>
 * </table>
 * Whenever a component receives any of the aforementioned events it will try to handle it, it is the developer's
 * responsibility to let the hierarchy know an event was successfully handled by returning {@code true}, in which case
 * the event is finished. If, however, a component returns {@code false}, this means that an event was not handled and
 * that any of the parents gets the chance of handling the event itself until one returns {@code true}.
 */
public class GuiComponent
    implements
        IPaletteProvider,
        INavigable,
        IInputListener
{
    //******************************************************************************************************************
    /**
     * Specifies the descriptions of a {@link GuiProperty} for a component that can be given to
     * {@link #getGuiProperties()}.
     * <p>
     * Do note that {@code id} should be unique across all properties and all components, a good convention is to use
     * the component name and the property name paired with the mod ID:
     * <pre>{@code Identifier.of("modId", "my_component/some_property")}</pre>
     *
     * @param id       A unique {@link Identifier} for the property
     * @param property A reference to the {@link GuiProperty} inside of the component
     * @param codec    An optional {@link Codec} for the property; leave empty if the property should not be serialised
     *                 but still be recorded for other features
     * @param <T>      The type the {@link GuiProperty} holds
     *
     * @see #getGuiProperties()
     */
    public record GuiPropertyDescription<T>(
        @NotNull Identifier         id,
        @NotNull IGuiProperty<T>    property,
        @NotNull Optional<Codec<T>> codec
    )
    {
        //**************************************************************************************************************
        /**
         * Constructs a property description with a non-optional codec.
         * @param id       A unique {@link Identifier} for the property
         * @param property A reference to the {@link GuiProperty} inside of the component
         * @param codec    The {@link Codec} for the property
         */
        public GuiPropertyDescription(final @NotNull Identifier      id,
                                      final @NotNull IGuiProperty<T> property,
                                      final @NotNull Codec<T>        codec)
        {
            this(id, property, Optional.of(codec));
        }

        /**
         * Constructs a property description with an empty codec.
         * @param id       A unique {@link Identifier} for the property
         * @param property A reference to the {@link GuiProperty} inside of the component
         */
        public GuiPropertyDescription(final @NotNull Identifier id, final @NotNull IGuiProperty<T> property)
        {
            this(id, property, Optional.empty());
        }

        //==============================================================================================================
        /**
         * Combines the codec of the content with the codec for the {@link IGuiProperty} and returns the full one.
         * @return The {@link Codec} for this property description
         */
        public @NotNull Optional<Codec<IGuiProperty<T>>> getCodec() { return this.codec.map(this.property::getCodec); }
    }
    
    public record BoundsChangedEventArgs(boolean resized, boolean moved) implements GuiEventArgs {}
    public record TemplateChangedEventArgs(@Nullable IGuiTemplate newTemplate) implements GuiEventArgs {}
    public record FontChangedEventArgs(@Nullable GuiFont newFont) implements GuiEventArgs {}
    public record FocusEventArgs(@NotNull GuiNavigationType navigationType) implements GuiEventArgs {}
    public record ComponentEventArgs(@NotNull GuiComponent component) implements GuiEventArgs {}
    public record ColourEventArgs(@NotNull ColourId id) implements GuiEventArgs {}
    
    //------------------------------------------------------------------------------------------------------------------
    enum ComponentFlag
    {
        //**************************************************************************************************************
        VISIBLE     (true),  // Can be drawn and receive input events
        ACTIVE      (true),  // Is active
        PINNED      (false), // Is atop other non-pinned components
        MONITOR     (false), // Should get child events
        WANTS_FOCUS (false), // Wants to get keyboard events
        FOCUS_PATH  (false), // Component is part of the focus path of the currently focused component
        ;
        
        //**************************************************************************************************************
        public static @NotNull BitSet createDefaults()
        {
            return Arrays
                .stream(ComponentFlag.values())
                .map(c -> c.defaultValue)
                .collect(NoviaCollectors.toBitset());
        }
        
        //**************************************************************************************************************
        public final boolean defaultValue;
        
        //**************************************************************************************************************
        ComponentFlag(final boolean defaultValue) { this.defaultValue = defaultValue; }
        
        //==============================================================================================================
        public boolean get(final @NotNull GuiComponent component) { return component.flags.get(this.ordinal()); }
        
        //==============================================================================================================
        public void set(final @NotNull GuiComponent component, final boolean value)
        {
            component.flags.set(this.ordinal(), value);
        }
    }
    
    //******************************************************************************************************************
    static GuiComponent FOCUSED = null;
    
    //******************************************************************************************************************
    /**
     * Gets the currently focused component or {@code null} if no component is focused.
     * @return The focused {@link GuiComponent}
     */
    public static @Nullable GuiComponent getCurrentFocused() { return GuiComponent.FOCUSED; }
    
    //------------------------------------------------------------------------------------------------------------------
    private static int clampToPinBounds(final int value, final int size, final int pins, boolean isPinned)
    {
        final int pin_start = (size - pins);
        
        if (isPinned)
        {
            return Math.clamp(value, pin_start, (size - 1));
        }
        
        return Math.clamp(value, 0, (pin_start - 1));
    }
    
    //******************************************************************************************************************
    /** Triggered whenever the size of the component changed. */
    public final GuiEvent.Simple sizeChanged = new GuiEvent.Simple();
    
    /** Triggered whenever the position (inside the parent) of the component changed. */
    public final GuiEvent.Simple positionChanged = new GuiEvent.Simple();
    
    /**
     * Triggered whenever the component's visibility flag changed. This will not trigger if any of the parents changed
     * their visibility (see {@link IHierarchyListener} instead).
     */
    public final GuiEvent.Simple visibilityChanged = new GuiEvent.Simple();
    
    /**
     * Triggered whenever the component's activity flag changed. This will not trigger if any of the parents changed
     * their activity state.
     */
    public final GuiEvent.Simple activityChanged = new GuiEvent.Simple();
    
    /**
     * Triggered whenever the associated {@link IGuiTemplate} for this component changed. This will only trigger for
     * the component that changed its immediate template and not for children that inherit this template.
     */
    public final GuiEvent<TemplateChangedEventArgs> templateChanged = new GuiEvent<>();
    
    /**
     * Triggered whenever the associated {@link GuiFont} for this component changed. This will only trigger for
     * the component that changed its immediate font and not for children that inherit this font.
     */
    public final GuiEvent<FontChangedEventArgs> fontChanged = new GuiEvent<>();
    
    /** Triggered whenever the component received focus. */
    public final GuiEvent<FocusEventArgs> focused = new GuiEvent<>();
    
    /** Triggered whenever the component lost focus. */
    public final GuiEvent.Simple blurred = new GuiEvent.Simple();
    
    /** Triggered whenever the component message changed. */
    public final GuiEvent.Simple messageChanged = new GuiEvent.Simple();
    
    /** Triggered whenever the tooltip changed. */
    public final GuiEvent.Simple tooltipChanged = new GuiEvent.Simple();
    
    /** Triggered whenever a child has been added to this component. */
    public final GuiEvent<ComponentEventArgs> childAdded = new GuiEvent<>();
    
    /** Triggered whenever a child has been removed from this component. */
    public final GuiEvent<ComponentEventArgs> childRemoved = new GuiEvent<>();
    
    /** Triggered whenever the parent component changed. */
    public final GuiEvent.Simple parentChanged = new GuiEvent.Simple();
    
    /** Triggered whenever the navigation order of this component changed. */
    public final GuiEvent.Simple navigationOrderChanged = new GuiEvent.Simple();
    
    /** Triggered whenever the component's pin state changed (either through being pinned or unpinned). */
    public final GuiEvent.Simple pinStateChanged = new GuiEvent.Simple();
    
    /** Triggered whenever one of the component's colours changed. */
    public final GuiEvent<ColourEventArgs> colourChanged = new GuiEvent<>();
    
    //==================================================================================================================
    /**
     * A map for additional component properties that can be used to attach custom user data.
     * <p>
     * Since this map is fully controlled by the user and not used anywhere else in this mod's GUI code,
     * the data attached should be end-user-defined and not at library scope.
     * This is so that it can be used for particular scenarios like e.g. in customised component drawing ect.
     * <p>
     * If there needs to be re-usable data attached to a component, as in a custom library component,
     * it should be provided as a class field.
     */
    public final Object2ObjectMap<String, Value> properties = new Object2ObjectOpenHashMap<>();

    //------------------------------------------------------------------------------------------------------------------
    @Nullable GuiComponent  parent   = null;
    @Nullable IGuiTemplate  template = null;
    @Nullable GuiFont       font     = null;
    @Nullable ScreenInterop screen   = null;
    
    //------------------------------------------------------------------------------------------------------------------
    private final List<GuiComponent>      children           = new ArrayList<>();
    private final Set<IHierarchyListener> hierarchyListeners = new HashSet<>();
    private final BitSet                  flags              = ComponentFlag.createDefaults();
    private final Rectangle               bounds             = new Rectangle();
    private final Palette                 palette            = new Palette(null);
    
    private Positioner    positioner   = null;
    private Restrainer    restrainer   = null;
    private Tooltip       tooltip      = null;
    private Duration      tooltipDelay = Duration.ZERO;
    private int           screenX      = 0;
    private int           screenY      = 0;
    private int           numPinned    = 0;
    private float         opacity      = 1.0f;
    private int           focusOrder   = 0;
    private Text          message;
    
    //******************************************************************************************************************
    /**
     * Constructs a new gui component.
     * @param message The initial message of the component
     */
    public GuiComponent(final @NotNull Text message)
    {
        this.message = Objects.requireNonNull(message, "message must not be null");
    }
    
    /** Constructs a new gui component with an empty message. */
    public GuiComponent() { this(ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    /**
     * Gets the bounds of this component where x and y represent the location of the component in the parent.
     * @return The bounds of this component
     */
    public final @NotNull Rectangle getBounds() { return new Rectangle(this.bounds); }
    
    /**
     * Gets the local bounds of this component where x and y will both be 0.
     * @return The local bounds of this component
     */
    public final @NotNull Rectangle getLocalBounds() { return this.getBounds().resetPos(); }
    
    /**
     * Gets the positioner of this component.
     * @return The {@link Positioner} of this component or an empty {@link Optional} if there is none set
     */
    public final @NotNull Optional<Positioner> getPositioner() { return Optional.ofNullable(this.positioner); }

    /**
     * Gets the restrainer of this component.
     * @return The {@link Restrainer} of this component or an empty {@link Optional} if there is none set
     */
    public final @NotNull Optional<Restrainer> getRestrainer() { return Optional.ofNullable(this.restrainer); }

    /**
     * Gets the screen bounds of this component where x and y represent the location of the component on the screen.
     * @return The local bounds of this component
     */
    public final @NotNull Rectangle getScreenBounds()
    {
        return new Rectangle(this.screenX, this.screenY, this.getWidth(), this.getHeight());
    }
    
    /**
     * Gets this component's bounds as {@link ScreenRect}.
     * @return The {@link ScreenRect}
     */
    public final @NotNull ScreenRect getScreenRect()
    {
        return new ScreenRect(this.screenX, this.screenY, this.getWidth(), this.getHeight());
    }
    
    /**
     * Gets the position of this component in the parent component.
     * @return The position
     */
    public final @NotNull Point getPosition() { return this.bounds.getPosition(); }
    
    /**
     * Gets the message of this component. (see {@link #setMessage(Text)})
     * @return The component's message
     */
    public final @NotNull Text getMessage() { return this.message; }
    
    /**
     * Gets the opacity of this component.
     * <p>
     * Opacity of 0 is different from an invisible component, as the component will still be drawn and receive input
     * events.
     * @return The opacity
     */
    public final float getOpacity() { return this.opacity; }
    
    /**
     * Gets the component (child or itself) that is located at the given coordinate,
     * or {@code null} if the coordinate is outside the bounds of this component, or the hit test failed for the given
     * coordinates.
     * <p>
     * This does not check whether the component is active but will ignore invisible components.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return The found {@link GuiComponent}, otherwise {@code null}
     */
    public final @Nullable GuiComponent getComponentAt(final int x, final int y)
    {
        if (!this.containsPoint(x, y) || !this.isVisible() || !this.hitTest(x, y))
        {
            return null;
        }
        
        for (int i = (this.children.size() - 1); i >= 0; --i)
        {
            final GuiComponent child  = this.children.get(i);
            final GuiComponent target = child.getComponentAt((x - child.getX()), (y - child.getY()));
            
            if (target != null)
            {
                return target;
            }
        }
        
        return this;
    }
    
    /**
     * Gets the component (child or itself) that is located at the given coordinate,
     * or {@code null} if the coordinate is outside the bounds of this component.
     * <p>
     * This does not check whether the component is active but will ignore invisible components.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return The found {@link GuiComponent}, or {@code null} if this component was invisible, or the coordinates were
     *         out of bounds
     */
    public final @Nullable GuiComponent getComponentAt(final double x, final double y)
    {
        return this.getComponentAt((int) x, (int) y);
    }
    
    /**
     * Gets the component (child or itself) that is located at the given coordinate,
     * or {@code null} if the coordinate is outside the bounds of this component.
     * <p>
     * This does not check whether the component is active but will ignore invisible components.
     *
     * @param location The location to look for the component
     * @return The found {@link GuiComponent}, or {@code null} if this component was invisible, or the coordinates were
     *         out of bounds
     */
    public final @Nullable GuiComponent getComponentAt(final @NotNull Point location)
    {
        return this.getComponentAt(location.x(), location.y());
    }
    
    /**
     * Gets the x coordinate (relative to parent) of this component.
     * @return The x coordinate
     */
    public final int getX() { return this.bounds.x(); }
    
    /**
     * Gets the y coordinate (relative to parent) of this component.
     * @return The y coordinate
     */
    public final int getY() { return this.bounds.y(); }
    
    /**
     * Gets the right coordinate (relative to parent) of this component.
     * @return The right coordinate
     */
    public final int getRight() { return this.bounds.getRight(); }
    
    /**
     * Gets the bottom coordinate (relative to parent) of this component.
     * @return The bottom coordinate
     */
    public final int getBottom() { return this.bounds.getBottom(); }
    
    /**
     * Gets the width of this component.
     * @return The width
     */
    public final int getWidth() { return this.bounds.width(); }
    
    /**
     * Gets the height of this component.
     * @return The height
     */
    public final int getHeight() { return this.bounds.height(); }
    
    /**
     * Gets the absolute X coordinate of this component in screen-space.
     * @return The X coordinate
     */
    public final int getScreenX() { return this.screenX; }
    
    /**
     * Gets the absolute Y coordinate of this component in screen-space.
     * @return The Y coordinate
     */
    public final int getScreenY() { return this.screenY; }

    /**
     * Gets the coordinates of this component in screen coordinates.
     * @return The screen {@link Point}
     */
    public final @NotNull Point getScreenPosition() { return new Point(this.screenX, this.screenY); }
    
    /**
     * Gets the absolute right coordinate of this component in screen-space.
     * @return The right coordinate
     */
    public final int getScreenRight() { return (this.screenX + this.getWidth()); }
    
    /**
     * Gets the absolute bottom coordinate of this component in screen-space.
     * @return The bottom coordinate
     */
    public final int getScreenBottom() { return (this.screenY + this.getHeight()); }
    
    /**
     * Returns the number of children this component has.
     * @return The number of children
     */
    public final int getChildCount() { return this.children.size(); }
    
    /**
     * Gets the parent component of this component.
     * @return The parent component or {@code null} if this component is the top-level component
     */
    public final @Nullable GuiComponent getParent() { return this.parent; }
    
    /**
     * Gets the top-level component in the component hierarchy. (the component without a parent)
     * @return The top-level component, or itself if this component is the top-level component
     */
    public final @NotNull GuiComponent getTopLevelComponent()
    {
        GuiComponent component = this;
        
        while (component.parent != null)
        {
            component = component.parent;
        }
        
        return component;
    }
    
    /**
     * Gets the associated tooltip for this component or {@code null} if there is no tooltip for this component.
     * @return The tooltip or {@code null}
     */
    public final @Nullable Tooltip getTooltip() { return this.tooltip; }
    
    /**
     * Gets this component's set tooltip delay.
     * @return The delay {@link Duration}
     */
    public final @NotNull Duration getTooltipDelay() { return this.tooltipDelay; }
    
    /**
     * Gets the child component at the given index, or an empty optional if there is no child at the given index.
     * <p>
     * Do note that the position of children is not as deterministic as it might seem, children might change their
     * z-index as they see fit.
     *
     * @param index The index to get
     * @return The child component or -1 if no component found
     */
    public final @NotNull Optional<GuiComponent> getChildAt(final int index)
    {
        if (index < 0 || index >= this.children.size())
        {
            return Optional.empty();
        }
        
        return Optional.of(this.children.get(index));
    }
    
    /** {@return the list of all child components} */
    public final @NotNull List<GuiComponent> getChildren() { return new ArrayList<>(this.children); }
    
    @Override
    public @Nullable IComponentNavigator getNavigator()
    {
        return (this.hasChildren() ? NaturalNavigator.INSTANCE : null);
    }
    
    /**
     * Gets the narration title message for the screen narrator, or {@code null} if the narration title is disabled
     * for this component.
     * @return The narration message
     */
    public @Nullable MutableText getNarrationMessage()
    {
        return Text.translatable(
            (this.isActive()
                ? GuiApiLang.GUI_NARRATION_COMPONENT_TITLE
                : GuiApiLang.GUI_NARRATION_COMPONENT_TITLE_INACTIVE).toTranslationKey(),
            this.message);
    }
    
    @Override public final int getNavigationOrder() { return this.focusOrder; }
    
    /**
     * Finds the first template that can be applied to this component. This will search all the way up the component
     * hierarchy until a template could be found, if none was found this will return {@link IGuiTemplate#DEFAULT}.
     * <p>
     * For {@link GuiScreen} objects that have their own explicit template specified, all modal layers of that screen
     * will get the given template as root template.
     * <p>
     * Do not modify a template this way, if the template that is returned is an inherited template from a parent
     * component, this will result in other children of that parent, that also inherit this template, to adapt these
     * changes as well. Instead, find a way to copy the template and explicitly set it via
     * {@link #setTemplate(IGuiTemplate)}; or even better, make sure to set it up entirely upon construction.
     * <p>
     * Additionally, since this has to scan the entire hierarchy until it finds a template, it should not be used
     * to get the template inside any of the draw calls, {@link Canvas} has {@link Canvas#getTemplate()}
     * for exactly this reason.
     *
     * @return The {@link IGuiTemplate} applied to this component
     */
    public final @NotNull IGuiTemplate getTemplate()
    {
        return Objects.requireNonNullElse(this.findParentObject(comp -> comp.template), IGuiTemplate.DEFAULT);
    }
    
    /**
     * Finds the first gui font that can be applied to this component. This will search all the way up the component
     * hierarchy until a font could be found, if none was found this will return {@link GuiFont#DEFAULT}.
     * <p>
     * For {@link GuiScreen} objects that have their own explicit font specified, all modal layers of that screen
     * will get the given font as base font.
     * <p>
     * Do note that the {@link GuiFont} returned is a copy of the font that was found, that means it is not possible
     * to modify the font directly but requires a call to {@link #setFont(GuiFont)}.
     * <p>
     * Additionally, since this has to scan the entire hierarchy until it finds a font, it should not be used
     * to get the font inside any of the draw calls, {@link Canvas} has {@link Canvas#getFont()}
     * for exactly this reason.
     *
     * @return The {@link IGuiTemplate} applied to this component
     */
    public final @NotNull GuiFont getFont()
    {
        final GuiFont font = this.findParentObject(comp -> comp.font);
        return (font != null ? new GuiFont(font) : GuiFont.DEFAULT.get());
    }
    
    @Override public @NotNull Palette getPalette() { return this.palette; }
    
    /**
     * Can be overridden to let the component system know that the component provides a few gui properties.
     * <p>
     * This is not strictly necessary for the component to function but allows some additional features such as
     * serialisation and advanced integration for development utils, or anything related to property management.
     *
     * @return A map of {@link GuiProperty} objects mapped to a unique string identifier
     */
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties() { return Stream.empty(); }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> @Nullable T findParentObject(final @NotNull Function<GuiComponent, T> getter)
    {
        for (GuiComponent comp = this; comp != null; comp = comp.parent)
        {
            final T object = getter.apply(comp);
            
            if (object != null)
            {
                return object;
            }
        }
        
        return null;
    }
    
    //==================================================================================================================
    /**
     * Determines whether this component is currently being focused.
     * @return {@code true} if this element is focused
     */
    public final boolean isFocused() { return (GuiComponent.FOCUSED == this); }
    
    /**
     * Determines whether this component OR any of its children (at any depth) currently hold focus.
     * @return {@code true} if this element or any of its children is focused
     */
    public final boolean hasFocus() { return ComponentFlag.FOCUS_PATH.get(this); }
    
    /**
     * Whether the mouse cursor is currently hovering over this component.
     * <p>
     * If the mouse is currently in a drag gesture, this will not be the component it is currently over but instead the
     * component, which is currently being dragged.
     *
     * @return {@code true} if this element is focused
     */
    public final boolean isHovered()
    {
        final GuiScreen screen = GuiScreen.CURRENT_SCREEN;
        return (screen != null && screen.getHovered() == this);
    }
    
    /**
     * Whether this component is currently being dragged.
     * @return {@code true} if this component is currently being dragged
     */
    public final boolean isDragging()
    {
        final GuiScreen screen = GuiScreen.CURRENT_SCREEN;
        return (screen != null && screen.isInDrag() && screen.getHovered() == this);
    }
    
    /**
     * Gets whether this component is currently active.
     * <p>
     * This will return {@code false} if either this component is marked inactive, or if any of the parent components
     * is inactive.
     * <p>
     * Because this needs to go upwards to find out if any component is inactive, this should not be used to check the
     * activity state for rendering, the {@link Canvas} provided during rendering will automatically provide the most
     * recent activity state with {@link Canvas#isActive()}.
     *
     * @return {@code true} if this component is active
     */
    public final boolean isActive()
    {
        for (GuiComponent comp = this; comp.hasActiveFlag(); comp = comp.parent)
        {
            if (comp.parent == null)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets whether this component has the activity flag set, by that means, if the component is considered active on
     * its own.
     * <p>
     * Do note that this should not be used to test whether the component is currently active, as this will only return
     * the activity state of this component and not whether it is actually active, considering the activity of its
     * parent components. For cases like these, {@link #isActive()} should be used instead, which will check the entire
     * parent hierarchy.
     *
     * @return {@code true} if the activity flag for this component is set
     */
    public final boolean hasActiveFlag() { return ComponentFlag.ACTIVE.get(this); }
    
    /**
     * Gets whether this component is currently visible.
     * <p>
     * This only checks whether the internal visibility flag is set for this component; it does not check whether
     * the entire parent hierarchy is visible or not. If you want to know whether it is actually visible for drawing,
     * use {@link #isDrawing()} instead.
     *
     * @return {@code true} if this component is visible
     */
    public final boolean isVisible() { return ComponentFlag.VISIBLE.get(this); }
    
    /**
     * Determine whether this component is focusable.
     * <p>
     * Note that a component denying focus does not mean children can't get the focus.
     * @return {@code true} if this component is focusable
     */
    public final boolean wantsFocus() { return ComponentFlag.WANTS_FOCUS.get(this); }
    
    /**
     * A simple check, which determines whether the mouse is currently over this component or not. Since this will
     * check the screen bounds of the components, this will return {@code true} even if the component is outside its
     * parent bounds, there are a few alternatives such as {@link Canvas#clipRegionContains(Point)} when inside the
     * drawing routine of the component, which will check its current visible bounds.
     * <p>
     * If child components should be ignored, to check whether the mouse is inside the bounds regardless,
     * {@link #isMouseOver(double, double, boolean)} with {@code ignoreChildren} set to {@code true} should be used
     * instead.
     * <p>
     * This does not check whether the child is active or visible.
     * <p>
     * Do note that this might not be accurate if the component is not part of an active screen, additionally.
     *
     * @param mouseX The absolute (screen-space) mouse position X coordinate
     * @param mouseY The absolute (screen-space) mouse position Y coordinate
     * @return {@code true} if the mouse is hovering over this component
     */
    public final boolean isMouseOver(final double mouseX, final double mouseY)
    {
        return this.isMouseOver(mouseX, mouseY, false);
    }
    
    /**
     * A simple check, which determines whether the mouse is currently over this component or not. Since this will
     * check the screen bounds of the components, this will return {@code true} even if the component is outside its
     * parent bounds, there are a few alternatives such as {@link Canvas#clipRegionContains(Point)} when inside the
     * drawing routine of the component, which will check its current visible bounds.
     * <p>
     * If child components should be ignored, to check whether the mouse is inside the bounds regardless,
     * {@link #isMouseOver(double, double, boolean)} with {@code ignoreChildren} set to {@code true} should be used
     * instead.
     * <p>
     * This does not check whether the child is active or visible.
     * <p>
     * Do note that this might not be accurate if the component is not part of an active screen.
     *
     * @param mousePos The mouse position
     * @return {@code true} if the mouse is hovering over this component
     */
    public final boolean isMouseOver(final @NotNull Point mousePos)
    {
        return this.isMouseOver(mousePos.x(), mousePos.y());
    }
    
    /**
     * A simple check, which determines whether the mouse is currently over this component or not. Since this will
     * check the screen bounds of the components, this will return {@code true} even if the component is outside its
     * parent bounds, there are a few alternatives such as {@link Canvas#clipRegionContains(Point)} when inside the
     * drawing routine of the component, which will check its current visible bounds.
     * <p>
     * This does not check whether the child is active or visible.
     * <p>
     * Do note that this might not be accurate if the component is not part of an active screen.
     *
     * @param mouseX         The absolute (screen-space) mouse position X coordinate
     * @param mouseY         The absolute (screen-space) mouse position Y coordinate
     * @param ignoreChildren If {@code false}, this will return {@code false} if the cursor is above a child component
     * @return {@code true} if the mouse is hovering over this component
     */
    public final boolean isMouseOver(final double mouseX, final double mouseY, final boolean ignoreChildren)
    {
        if (
            (mouseX >= this.screenX && mouseX < this.getScreenRight())
            && (mouseY >= this.screenY && mouseY < this.getScreenBottom())
        )
        {
            return (
                ignoreChildren
                || this.children.stream().noneMatch(child -> child.isMouseOver(mouseX, mouseY, true))
            );
        }
        
        return false;
    }
    
    /**
     * A simple check, which determines whether the mouse is currently over this component or not. Since this will
     * check the screen bounds of the components, this will return {@code true} even if the component is outside its
     * parent bounds, there are a few alternatives such as {@link Canvas#clipRegionContains(Point)} when inside the
     * drawing routine of the component, which will check its current visible bounds.
     * <p>
     * This does not check whether the child is active or visible.
     * <p>
     * Do note that this might not be accurate if the component is not part of an active screen.
     *
     * @param mousePos       The mouse position
     * @param ignoreChildren If {@code false}, this will return {@code false} if the cursor is above a child component
     * @return {@code true} if the mouse is hovering over this component
     */
    public final boolean isMouseOver(final @NotNull Point mousePos, final boolean ignoreChildren)
    {
        return this.isMouseOver(mousePos.x(), mousePos.y(), ignoreChildren);
    }
    
    /**
     * Determines whether this component, besides its own, is monitoring mouse events also from children.
     * <p>
     * If a child somewhere down the hierarchy gets a mouse event, this will also receive that mouse event.
     * @return {@code true} if this component is monitoring mouse events from children
     */
    public final boolean isMonitoringChildren() { return ComponentFlag.MONITOR.get(this); }
    
    /**
     * Checks whether this component is currently on screen, by that means, it is part of the hierarchy of the currently
     * displayed screen. This ignores whether it is currently active or visible and just determines whether it
     * is part of the hierarchy.
     *
     * @return {@code true} if this component is part of the screen component hierarchy
     */
    public final boolean isOnScreen()
    {
        final GuiComponent top = this.getTopLevelComponent();
        return (top.isScreenContainer() && Objects.requireNonNull(top.screen).isShowing());
    }
    
    /**
     * Checks whether this component as well as any of its parents are visible and that it is part of the active
     * screen.
     * @return {@code true} if this component is visible on screen
     */
    public final boolean isDrawing()
    {
        for (GuiComponent comp = this; comp.isVisible(); comp = comp.parent)
        {
            if (comp.parent == null)
            {
                return (comp.isScreenContainer() && Objects.requireNonNull(comp.screen).isShowing());
            }
        }
        
        return false;
    }
    
    /**
     * Gets whether this component has any child components.
     * @return {@code true} if this component has child components
     */
    public final boolean hasChildren() { return !this.children.isEmpty(); }
    
    /**
     * Gets whether this component is a pinned component in the parent.
     * <p>
     * To know more about pinned components, see {@link #setPinned}.
     * @return {@code true} if this component is pinned
     */
    public final boolean isPinned() { return ComponentFlag.PINNED.get(this); }
    
    /**
     * Whether the narrator should consider this component narratable.
     * @return {@code true} if this component is narratable
     */
    public final boolean isNarratable() { return this.isVisible(); }
    
    /**
     * Determines whether this component can be focused at all.
     * <p>
     * Other than {@link #canBeNavigatedTo()}, this will return {@code true} even if focus order is below 0.
     * <p>
     * Note that this does not check whether the component is part of an active component hierarchy but only check
     * whether this component qualifies as focusable on its own.
     *
     * @return {@code true} if this component can be focused
     */
    public final boolean canBeFocused() { return (this.hasActiveFlag() && this.isVisible() && this.wantsFocus()); }
    
    /**
     * Determines whether this component can be focused by navigating through the keyboard.
     * <p>
     * Other than {@link #canBeFocused()}, this will return {@code false} if focus order is below 0.
     * <p>
     * Please note that this does not take into account if {@link #getNavigator()} is overridden, as at this point the
     * navigation order is unpredictable.
     * <p>
     * Also note that this does not check whether the component is part of an active component hierarchy but only check
     * whether this component qualifies as navigable on its own.
     *
     * @return {@code true} if this component can be focused through keyboard navigation
     */
    public final boolean canBeNavigatedTo() { return (this.canBeFocused() && this.getNavigationOrder() >= 0); }
    
    /**
     * Gets whether this component contains a given component (by identity) as a child.
     * <p>
     * This will either check only its immediate children or if {@code recursive} is {@code true}, check the children
     * at any depth.
     *
     * @param component The component to find
     * @param recursive Whether to search at any depth
     * @return {@code true} if the component contains the given child, otherwise {@code false}
     */
    public final boolean containsChild(final @Nullable GuiComponent component, final boolean recursive)
    {
        if (component == null)
        {
            return false;
        }
        
        if (recursive)
        {
            for (final var child : this.children)
            {
                if (child == component || child.containsChild(component, true))
                {
                    return true;
                }
            }
        }
        
        return this.children.stream().anyMatch(child -> (child == component));
    }
    
    /**
     * Gets whether this component contains a given component (by identity) as a child.
     * <p>
     * This will recursively check the children at any depth. If instead only the immediate children should be checked,
     * use {@link #containsChild(GuiComponent, boolean)} instead with {@code recursive} set to {@code true}.
     *
     * @param component The component to find
     * @return {@code true} if the component contains the given child, otherwise {@code false}
     */
    public final boolean containsChild(final @Nullable GuiComponent component)
    {
        return this.containsChild(component, true);
    }
    
    /**
     * Gets whether the given point lies within the bounds of this component.
     *
     * @param x The coordinate on the x-axis
     * @param y The coordinate on the y-axis
     * @return {@code true} if the point lies within the bounds of this component
     */
    public final boolean containsPoint(final int x, final int y)
    {
        return (x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight());
    }
    
    /**
     * Gets whether the given point lies within the bounds of this component.
     *
     * @param point The point
     * @return {@code true} if the point lies within the bounds of this component
     */
    public final boolean containsPoint(final @NotNull Point point)
    {
        return point.apply(this::containsPoint);
    }
    
    /**
     * Gets whether the given screen point lies within the screen bounds of this component.
     *
     * @param screenX The screen coordinate on the x-axis
     * @param screenY The screen coordinate on the y-axis
     * @return {@code true} if the point lies within the bounds of this component
     */
    public final boolean containsScreenPoint(final int screenX, final int screenY)
    {
        return (
            screenX >= this.screenX && screenX < (this.screenX + this.getWidth())
            && screenY >= this.screenY && screenY < (this.screenY + this.getHeight()));
    }
    
    /**
     * Gets whether the given screen point lies within the screen bounds of this component.
     *
     * @param screenPoint The screen point
     * @return {@code true} if the point lies within the bounds of this component
     */
    public final boolean containsScreenPoint(final @NotNull Point screenPoint)
    {
        return screenPoint.apply(this::containsScreenPoint);
    }
    
    /**
     * Can be overridden to determine if a child can be pinned.
     *
     * @param child The child component in question
     * @return {@code true} if the parent allows pinning this child, otherwise {@code false}
     */
    public boolean isPinningAllowed(final @NotNull GuiComponent child) { return true; }
    
    /**
     * Can be overridden to determine if a child can be added.
     *
     * @param child The component to add
     * @return {@code true} if the parent allows adding this child
     */
    public boolean isAddingChildAllowed(final @NotNull GuiComponent child) { return true; }
    
    /**
     * Can be overridden to determine if a child can be removed.
     *
     * @param child The component to remove
     * @return {@code true} if the parent allows removing this child
     */
    public boolean isRemovingChildAllowed(final @NotNull GuiComponent child) { return true; }
    
    /**
     * Can be overridden to determine whether children of this component are allowed to use {@link Positioner} to
     * automatically determine their bounds inside this component.
     *
     * @param child The component to auto-position
     * @return {@code true} if auto-positioning is allowed
     */
    public boolean isAutoPositioningAllowed(final @NotNull GuiComponent child) { return true; }
    
    /**
     * Can be overridden to determine whether this component is allowed to get promoted to a modal layer.
     * <p>
     * This is useful for components that might not work as a modal component, or that might make it difficult to
     * integrate in a well functioning way, like {@link GuiScreen}.
     *
     * @return {@code true} if this component can be made a modal, {@code false} otherwise
     */
    public boolean isModalPromotionAllowed() { return true; }
    
    /**
     * Determines whether this component is a screen container, that is, if this is the root content component of a
     * screen.
     * @return {@code true} if this is a screen container
     */
    public boolean isScreenContainer() { return (this.screen != null); }
    
    /**
     * Determines whether this component is currently shown as a modal component and not part of another parent.
     * @return {@code true} if this is a modal component
     */
    public boolean isModal() { return (this.screen != null && this.screen.getLayerForComponent(this) != null); }
    
    //==================================================================================================================
    /**
     * Converts a given point on the x-axis, relative to this component, into screen coordinates.
     * @param relativeX The point relative to this component
     * @return The absolute point
     */
    public final int toScreenX(final int relativeX) { return (this.screenX + relativeX); }
    
    /**
     * Converts a given point on the y-axis, relative to this component, into screen coordinates.
     * @param relativeY The point relative to this component
     * @return The absolute point
     */
    public final int toScreenY(final int relativeY) { return (this.screenY + relativeY); }
    
    /**
     * Converts a given point on the x-axis, relative to this component, into screen coordinates.
     * @param relativeX The point relative to this component
     * @return The absolute point
     */
    public final double toScreenX(final double relativeX)
    {
        return (this.screenX + relativeX);
    }
    
    /**
     * Converts a given point on the y-axis, relative to this component, into screen coordinates.
     * @param relativeY The point relative to this component
     * @return The absolute point
     */
    public final double toScreenY(final double relativeY)
    {
        return (this.screenY + relativeY);
    }
    
    /**
     * Converts a given point, relative to this component, into screen coordinates.
     * @param relativeX The point on the x-axis relative to this component
     * @param relativeY The point on the y-axis relative to this component
     * @return The absolute point
     */
    public final @NotNull Point toScreenPos(final int relativeX, final int relativeY)
    {
        return new Point(this.toScreenX(relativeX), this.toScreenY(relativeY));
    }
    
    /**
     * Converts a given point, relative to this component, into screen coordinates.
     * @param relativeX The point on the x-axis relative to this component
     * @param relativeY The point on the y-axis relative to this component
     * @return The absolute point
     */
    public final @NotNull Point toScreenPos(final double relativeX, final double relativeY)
    {
        return new Point((int) this.toScreenX(relativeX), (int) this.toScreenY(relativeY));
    }
    
    /**
     * Converts a given point, relative to this component, into screen coordinates.
     * @param relativePos The relative point
     * @return The absolute point
     */
    public final @NotNull Point toScreenPos(final @NotNull Point relativePos)
    {
        return relativePos.translated(this.screenX, this.screenY);
    }
    
    /**
     * Converts a given point on the x-axis, absolute to the screen, into a local component point.
     * @param absoluteX The absolute point on the screen
     * @return The component local point
     */
    public final int toRelativeX(final int absoluteX) { return (absoluteX - this.screenX); }
    
    /**
     * Converts a given point on the y-axis, absolute to the screen, into a local component point.
     * @param absoluteY The absolute point on the screen
     * @return The component local point
     */
    public final int toRelativeY(final int absoluteY) { return (absoluteY - this.screenY); }
    
    /**
     * Converts a given point on the x-axis, absolute to the screen, into a local component point.
     * @param absoluteX The absolute point on the screen
     * @return The component local point
     */
    public final int toRelativeX(final double absoluteX) { return (int) (absoluteX - this.screenX); }
    
    /**
     * Converts a given point on the y-axis, absolute to the screen, into a local component point.
     * @param absoluteY The absolute point on the screen
     * @return The component local point
     */
    public final int toRelativeY(final double absoluteY) { return (int) (absoluteY - this.screenY); }
    
    /**
     * Converts a given point, absolute to the screen, into local component coordinates.
     * @param absoluteX The point on the x-axis relative absolute to the screen
     * @param absoluteY The point on the y-axis relative absolute to the screen
     * @return The component local point
     */
    public final @NotNull Point toRelativePos(final int absoluteX, final int absoluteY)
    {
        return new Point(this.toRelativeX(absoluteX), this.toRelativeY(absoluteY));
    }
    
    /**
     * Converts a given point, absolute to the screen, into local component coordinates.
     * @param absoluteX The point on the x-axis relative absolute to the screen
     * @param absoluteY The point on the y-axis relative absolute to the screen
     * @return The component local point
     */
    public final @NotNull Point toRelativePos(final double absoluteX, final double absoluteY)
    {
        return new Point((int) this.toRelativeX(absoluteX), (int) this.toRelativeY(absoluteY));
    }
    
    /**
     * Converts a given point, absolute to the screen, into local component coordinates.
     * @param absolutePos The absolute point
     * @return The component local point
     */
    public final @NotNull Point toRelativePos(final @NotNull Point absolutePos)
    {
        return absolutePos.translated(-this.screenX, -this.screenY);
    }
    
    /**
     * Gets the index of the given child component.
     * <p>
     * Components are compared by identity and not by contents, this will only return a valid index if exactly
     * the given component instance is part of the tree.
     *
     * @param child The child component
     * @return The index of the component, or -1 if no such component is currently part of the child hierarchy
     */
    public final int indexOfChild(final @Nullable GuiComponent child)
    {
        return IntStream
            .range(0, this.children.size())
            .filter(i -> this.children.get(i) == child)
            .findFirst()
            .orElse(-1);
    }
    
    /**
     * Tests whether the mouse actually executes events on this component. If this returns {@code false}, the event
     * will fall through to the component behind it.
     * <p>
     * By default, this will always return {@code true} which means that as long as the cursor is over this component it
     * will always pick this component for handling a mouse event, but overriding it allows fine-tuning this behaviour.
     *
     * @param x The relative x-coordinate to test
     * @param y The relative y-coordinate to test
     * @return {@code true} if the mouse actually executes events on this component at the given location inside the
     *         component's bounds
     */
    public boolean hitTest(final int x, final int y) { return true; }
    
    //==================================================================================================================
    /**
     * Sets whether this component should be visible.
     * <p>
     * An invisible component will not receive input events.
     * @param shouldBeVisible The visibility
     */
    public final void setVisible(final boolean shouldBeVisible)
    {
        if (this.isVisible() == shouldBeVisible)
        {
            return;
        }
        
        ComponentFlag.VISIBLE.set(this, shouldBeVisible);
        
        if (!shouldBeVisible && this.hasFocus())
        {
            if (this.parent != null)
            {
                this.parent.focus();
            }
            
            this.blur();
        }
        
        this.onVisibilityChanged();
        this.visibilityChanged.post(this);
        this.sendHierarchyVisibilityChangeNotification();
        
        if (this.isOnScreen())
        {
            GuiScreen.CURRENT_SCREEN.updateHoverState();
        }
    }
    
    /**
     * Sets whether this component should be active.
     * <p>
     * An inactive component will not receive input events.
     * @param shouldBeActive The activity state
     */
    public final void setActive(final boolean shouldBeActive)
    {
        if (ComponentFlag.ACTIVE.get(this) == shouldBeActive)
        {
            return;
        }
        
        ComponentFlag.ACTIVE.set(this, shouldBeActive);
        
        if (!shouldBeActive && this.hasFocus())
        {
            if (this.parent != null)
            {
                this.parent.focus();
            }
            
            this.blur();
        }
        
        this.sendActivityChangeNotification();
        this.children.forEach(GuiComponent::sendActivityChangeNotification);
        this.activityChanged.post(this);
    }
    
    /**
     * Sets the opacity of this component.
     * @param opacity The new opacity value (0-1, where 0 means fully transparent and 1 means fully opaque)
     */
    public final void setOpacity(final float opacity) { this.opacity = Math.clamp(opacity, 0.0f, 1.0f); }
    
    /**
     * Sets the x coordinate of this component local to the parent component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param x The x coordinate
     */
    public final void setX(final int x)
    {
        this.setBounds(x, this.bounds.y(), this.bounds.width(), this.bounds.height());
    }
    
    /**
     * Sets the y coordinate of this component local to the parent component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param y The y coordinate
     */
    public final void setY(final int y)
    {
        this.setBounds(this.bounds.x(), y, this.bounds.width(), this.bounds.height());
    }
    
    /**
     * Sets the width of this component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param width The component width
     */
    public final void setWidth(final int width)
    {
        this.setBounds(this.bounds.x(), this.bounds.y(), width, this.bounds.height());
    }
    
    /**
     * Sets the height of this component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param height The component height
     */
    public final void setHeight(final int height)
    {
        this.setBounds(this.bounds.x(), this.bounds.y(), this.bounds.width(), height);
    }
    
    /**
     * Sets the position of this component local to the parent component. The position is relative to the top-left
     * corner of the parent component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public final void setPosition(final int x, final int y)
    {
        this.setBounds(x, y, this.bounds.width(), this.bounds.height());
    }
    
    /**
     * Sets the position of this component local to the parent component. The position is relative to the top-left
     * corner of the parent component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param position The new position
     */
    public final void setPosition(final @NotNull Point position) { position.accept(this::setPosition); }
    
    /**
     * Sets the size of this component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param width  The new width
     * @param height The new height
     */
    public final void setDimensions(final int width, final int height)
    {
        this.setBounds(this.bounds.x(), this.bounds.y(), width, height);
    }
    
    /**
     * Sets the bounds of this component inside the parent component. The position is relative to the top-left
     * corner of the parent component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param x      The x coordinate relative to the parent component
     * @param y      The y coordinate relative to the parent component
     * @param width  The width
     * @param height The height
     */
    public final void setBounds(final int x, final int y, int width, int height)
    {
        if (this.positioner == null || (this.parent != null && !this.parent.isAutoPositioningAllowed(this)))
        {
            this.setBoundsInternal(x, y, width, height);
        }
    }
    
    /**
     * Sets the bounds of this component inside the parent component. The position is relative to the top-left
     * corner of the parent component.
     * <p>
     * If this component has a {@link Positioner}, this will do nothing.
     *
     * @param bounds The new bounds inside the parent component
     */
    public final void setBounds(final @NotNull Rectangle bounds)
    {
        bounds.accept(this::setBounds);
    }
    
    /**
     * Sets the component's message.
     * <p>
     * A component message basically allows a narrator to narrate the component with the given {@link Text} object.
     * Custom components can also use the message for other purposes like rendering it on screen and use it as narration
     * at the same time.
     * <p>
     * By default, a component will use the component's message as narration title via {@link #getNarrationMessage()}.
     *
     * @param message The new message of the component
     */
    public final void setMessage(final @NotNull Text message)
    {
        Objects.requireNonNull(message, "message must not be null");
        
        if (!Objects.equals(this.message, message))
        {
            this.message = message;
            
            this.onMessageChanged();
            this.messageChanged.post(this);
        }
    }
    
    /**
     * Sets the tooltip message for this component.
     * @param tooltip The new tooltip
     */
    public final void setTooltip(final @Nullable Tooltip tooltip)
    {
        if (!Objects.equals(this.tooltip, tooltip))
        {
            this.tooltip = tooltip;
            
            final GuiComponent top_level = this.getTopLevelComponent();
            
            if (top_level.isScreenContainer() && top_level.screen.isShowing())
            {
                top_level.screen.updateTooltip(this);
            }
            
            this.onTooltipChanged();
            this.tooltipChanged.post(this);
        }
    }
    
    @Override
    public @Nullable Colour setColour(final @NotNull ColourId id, final @Nullable Colour colour)
    {
        final Colour old_colour = IPaletteProvider.super.setColour(id, colour);
        
        if (!Objects.equals(old_colour, colour))
        {
            this.onColoursChanged();
            this.colourChanged.post(this, new ColourEventArgs(id));
        }
        
        return old_colour;
    }
    
    /**
     * Sets the template of this component to the new one, or clears the template by passing {@code null}.
     * <p>
     * This will raise {@link #templateChanged} for this and invoke {@link #onTemplateChanged(IGuiTemplate)}
     * for all children up to the end of the hierarchy or when a component provides its own explicit template.
     *
     * @param template The new {@link IGuiTemplate} or {@code null} to clear the template
     */
    public final void setTemplate(final @Nullable IGuiTemplate template)
    {
        if (this.template != template)
        {
            this.template = template;
            
            this.sendTemplateChangeNotification(template);
            this.templateChanged.post(this, new TemplateChangedEventArgs(template));
        }
    }
    
    /**
     * Sets the font of this component to the new one, or clears the font by passing {@code null}.
     * <p>
     * This will raise {@link #fontChanged} for this component and invoke {@link #onFontChanged(GuiFont)} for all
     * children up to the end of the hierarchy or when a component provides its own explicit font.
     *
     * @param font The new {@link GuiFont} or {@code null} to clear the font
     */
    public final void setFont(final @NotNull GuiFont font)
    {
        if (!Objects.equals(this.font, font))
        {
            this.font = font;
            
            this.sendFontChangeNotification(font);
            this.fontChanged.post(this, new FontChangedEventArgs(font));
        }
    }
    
    /**
     * Sets the positioner for this component.
     * <p>
     * A positioner allows a parent to automatically adjust the bounds of a child component based on its positioner.
     * <p>
     * It should be preferred to, nonetheless, set children bounds manually in {@link #resized()}, however, in a more
     * dynamic context it can be useful to let this be done automatically.
     * <p>
     * This will happen before {@link #resized()} is called.
     *
     * @param positioner The {@link Positioner}
     */
    public final void setPositioner(final @Nullable Positioner positioner)
    {
        if (!Objects.equals(this.positioner, positioner))
        {
            this.positioner = positioner;
            
            if (this.parent != null && this.parent.isAutoPositioningAllowed(this))
            {
                if (positioner != null)
                {
                    positioner
                        .getBounds(this.parent.getLocalBounds())
                        .accept(this::setBoundsInternal);
                }
                else
                {
                    this.parent.resized();
                }
            }
        }
    }

    /**
     * Sets a component restrainer that, upon resizing, limits the component in what size it can adapt.
     * @param restrainer The {@link Restrainer}
     */
    public final void setRestrainer(final @Nullable Restrainer restrainer)
    {
        if (!Objects.equals(this.restrainer, restrainer))
        {
            this.restrainer = restrainer;
            this.setBoundsInternal(this.getX(), this.getY(), this.getWidth(), this.getHeight());

            if (this.parent != null)
            {
                this.parent.resized();
            }
        }
    }
    
    /**
     * Sets the time the tooltip waits before showing upon hovering over this component.
     * @param tooltipDelay The delay duration
     */
    public final void setTooltipDelay(final @NotNull Duration tooltipDelay) { this.tooltipDelay = tooltipDelay; }
    
    /**
     * Sets whether this component wants to get keyboard focus or not.
     * <p>
     * Setting a component to not wanting focus does not stop its children from being able to receive focus.
     *
     * @param wantsFocus Whether this component wants to get keyboard focus
     */
    public final void setWantsFocus(final boolean wantsFocus) { ComponentFlag.WANTS_FOCUS.set(this, wantsFocus); }
    
    /**
     * Sets this component to be a pinned component in its parent.
     * <p>
     * A pinned component is a component that will always be in front of all other non-pinned components, where their
     * z-index cannot be smaller than that of unpinned components.
     * <p>
     * The z-index among pinned components can be less or greater, but will always be greater than that of unpinned
     * components.
     * <p>
     * This will send this component all the way to the front of all other components, pinned and unpinned and upon
     * unpinning to the front of all other unpinned components.
     *
     * @param shouldBePinned {@code true} if this component should be pinned
     */
    public final void setPinned(final boolean shouldBePinned)
    {
        if (shouldBePinned == this.isPinned())
        {
            return;
        }
        
        ComponentFlag.PINNED.set(this, shouldBePinned);
        
        if (this.parent != null)
        {
            if (shouldBePinned)
            {
                if (!this.parent.isPinningAllowed(this))
                {
                    ComponentFlag.PINNED.set(this, false);
                    return;
                }
                
                ++this.parent.numPinned;
                this.setZIndex(this.parent.getChildCount() - 1);
            }
            else
            {
                final int order = this.parent.numPinned--;
                this.setZIndex(this.parent.getChildCount() - order);
            }
            
            this.pinStateChanged.post(this);
        }
    }
    
    /**
     * Sets the navigation order of this component inside the parent (see {@link #getNavigationOrder()}).
     * @param navigationOrder The navigation order
     */
    public final void setNavigationOrder(final int navigationOrder)
    {
        if (this.focusOrder != navigationOrder)
        {
            this.focusOrder = navigationOrder;
            this.navigationOrderChanged.post(this);
        }
    }
    
    /**
     * Sets this component's z-index in the parent component.
     * <p>
     * If the new index is below zero, will put it all the way in the back, if it is above the number of children,
     * will put it all the way to the front.
     *
     * @param newIndex The new z-index of this component
     */
    public final void setZIndex(int newIndex)
    {
        if (this.parent == null)
        {
            return;
        }
        
        final int old_index = this.parent.indexOfChild(this);
        newIndex = GuiComponent.clampToPinBounds(
            newIndex,
            this.parent.getChildCount(),
            this.parent.numPinned,
            this.isPinned());
        
        if (old_index == newIndex)
        {
            return;
        }
        
        this.parent.setChildZIndex(old_index, newIndex);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    /**
     * Sets whether this component, besides its own, is monitoring mouse events also from children.
     * If a child somewhere down the hierarchy gets a mouse event, this will also receive that mouse event.
     * <p>
     * Monitoring components only get events from children if one of them successfully handled it,
     * otherwise the monitoring component will get it as an ordinary event. If a component got a monitored event,
     * then {@link MouseEvent#target()} will return the component, which handled it and not the monitoring component.
     *
     * @param shouldMonitor If this is {@code true}, this component will be monitoring mouse events from children
     */
    protected final void setMonitorChildren(final boolean shouldMonitor)
    {
        ComponentFlag.MONITOR.set(this, shouldMonitor);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    final void setBoundsInternal(final int x, final int y, int width, int height)
    {
        if (this.restrainer == null)
        {
            width  = Math.max(0, width);
            height = Math.max(0, height);
        }
        else
        {
            width  = Math.clamp(width,  this.restrainer.minWidth(),  this.restrainer.maxWidth());
            height = Math.clamp(height, this.restrainer.minHeight(), this.restrainer.maxHeight());
        }
        
        final boolean resized = (width != this.getWidth() || height != this.getHeight());
        final boolean moved   = (x     != this.getX()     || y      != this.getY());
        
        if (resized || moved)
        {
            this.bounds.setBounds(x, y, width, height);
            
            if (this.isDrawing())
            {
                GuiScreen.CURRENT_SCREEN.updateHoverState();
            }
            
            for (final var child : this.children)
            {
                if (child.positioner != null && this.isAutoPositioningAllowed(child))
                {
                    child.positioner
                        .getBounds(bounds)
                        .accept(child::setBoundsInternal);
                }
            }
            
            if (resized)
            {
                this.resized();
                this.sizeChanged.post(this);
            }
            
            if (moved)
            {
                this.moved();
                this.positionChanged.post(this);
            }
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setChildZIndex(final int oldIndex, int newIndex)
    {
        newIndex = GuiComponent.clampToPinBounds(
            newIndex,
            (this.getChildCount() + 1),
            this.numPinned,
            this.children.get(oldIndex).isPinned());
        
        if (oldIndex == newIndex)
        {
            return;
        }
        
        this.children.add(newIndex, this.children.remove(oldIndex));
        
        if (this.isDrawing())
        {
            GuiScreen.CURRENT_SCREEN.updateHoverState();
        }
    }
    
    //==================================================================================================================
    /**
     * Attaches a hierarchy listener to this component (see {@link IHierarchyListener}).
     * @param listener The {@link IHierarchyListener} to add
     */
    public void addHierarchyListeners(final @NotNull IHierarchyListener listener)
    {
        this.hierarchyListeners.add(listener);
    }
    
    /**
     * Detaches a hierarchy listener from this component, if it exists (see {@link IHierarchyListener}).
     * @param listener The {@link IHierarchyListener} to remove
     */
    public void removeHierarchyListeners(final @NotNull IHierarchyListener listener)
    {
        this.hierarchyListeners.remove(listener);
    }
    
    //==================================================================================================================
    /** Tries to give this component focus. */
    public void focus() { this.updateFocusState(GuiNavigationType.NONE, true, true); }
    
    /** If this component or a child (at any depth) of this component is currently focused, will blur that component. */
    public void blur() { this.updateFocusState(GuiNavigationType.NONE, true, false); }
    
    //==================================================================================================================
    /**
     * Appends narration messages to the builder.
     * <p>
     * When overriding this method, make sure to call the super method to allow the default narrations to be added.
     * @param builder The narration builder
     */
    public void appendNarrations(final @NotNull NarrationMessageBuilder builder)
    {
        if (!this.isNarratable())
        {
            return;
        }
        
        if (this.getNarrationMessage() != null)
        {
            builder.put(NarrationPart.TITLE, this.getNarrationMessage());
        }
        
        this.appendCustomNarrations(builder);
        this.children.forEach(child -> child.appendNarrations(builder));
        
        if (this.tooltip != null)
        {
            this.tooltip.appendNarrations(builder);
        }
    }
    
    /**
     * Pushes this component the given amount to the front in its parent component.
     * <p>
     * If this component is not a child component, nothing will be done.
     * @param amount The number of indices this should be pushed to the front, if 0 or less, nothing will be done and if
     *               the amount is higher than the remaining slots this will push it all the way to the front
     */
    public final void sendForward(final int amount)
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.setZIndex(this.parent.indexOfChild(this) + amount);
    }
    
    /**
     * Pushes this component one index to the front in its parent component.
     * <p>
     * If this component is not a child component or is already at the top, nothing will be done.
     */
    public final void sendForward() { this.sendForward(1); }
    
    /**
     * Pushes this component the given amount to the back in its parent component.
     * <p>
     * If this component is not a child component, nothing will be done.
     * @param amount The number of indices this should be pushed to the back, if 0 or less, nothing will be done and if
     *               the amount is higher than the remaining slots this will push it all the way to the back
     */
    public final void sendBackward(final int amount)
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.setZIndex(this.parent.indexOfChild(this) - amount);
    }
    
    /**
     * Pushes this component one index to the back in its parent component.
     * <p>
     * If this component is not a child component or is already at the bottom, nothing will be done.
     */
    public final void sendBackward() { this.sendBackward(1); }
    
    /**
     * Pushes this component all the way to the top in its parent component.
     * <p>
     * If this component is not a child component, nothing will be done.
     */
    public final void sendToFront()
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.sendForward(this.parent.children.size());
    }
    
    /**
     * Pushes this component all the way to the bottom in its parent component.
     * <p>
     * If this component is not a child component, nothing will be done.
     */
    public final void sendToBack()
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.sendBackward(this.parent.children.size());
    }
    
    public void appendCustomNarrations(@NotNull NarrationMessageBuilder builder) {}
    
    /**
     * Adds a child component to this component and updates the component hierarchy.
     * <p>
     * If the given component is already a child of this component, this does nothing. If the given component has a
     * different parent, the component will first be removed from its old parent and then added to this component.
     * <p>
     * The specified z-index determines the z order of the component in relation to all other children, where a higher z
     * order means the component will be treated as above all other components with lower z-indices. If the component is
     * pinned, it will be added to the pin range of the child list, and it will be added at least above all non-pinned
     * components regardless of the specified z-index.
     * <p>
     * Components are treated by reference and not equality, by that means, two children with the same data but
     * different instances will not be the same component.
     *
     * @param child  The new component to add
     * @param zIndex The desired z-index of the component
     * @return The added, or the already contained child component
     */
    public final <T extends GuiComponent> @NotNull T addChild(final @NotNull T child, int zIndex)
    {
        Objects.requireNonNull(child, "child component must not be null");
        
        if (child.parent == this)
        {
            return child;
        }
        
        if (!this.isAddingChildAllowed(child))
        {
            return child;
        }
        
        if (child.parent != null)
        {
            child.parent.removeChild(child);
        }
        
        if (child.isPinned())
        {
            if (this.isPinningAllowed(child))
            {
                ++this.numPinned;
            }
            else
            {
                child.setPinned(false);
            }
        }
        
        if (zIndex < 0)
        {
            zIndex = this.getChildCount();
        }
        
        zIndex = GuiComponent.clampToPinBounds(
            zIndex,
            (this.getChildCount() + 1),
            this.numPinned,
            child.isPinned());
        this.children.add(zIndex, child);
        
        child.parent = this;
        child.onParentChanged();
        child.parentChanged.post(this);
        child.sendHierarchyChangeNotification();
        
        if (this.isDrawing())
        {
            GuiScreen.CURRENT_SCREEN.updateHoverState();
        }
        
        child.getPositioner().ifPresent(positioner ->
        {
            if (!this.bounds.isEmpty())
            {
                positioner
                    .getBounds(this.bounds)
                    .accept(child::setBoundsInternal);
            }
        });
        
        this.onChildAdded(child);
        this.childAdded.post(this, new ComponentEventArgs(child));
        
        return child;
    }
    
    /**
     * Adds a list of child components to this component and updates the component hierarchy.
     * <p>
     * The same rules as with {@link #addChild(GuiComponent, int)} apply.
     *
     * @param children The list of components to add
     * @return The number of children that were added
     */
    public final int addAllChildren(final @NotNull Collection<? extends GuiComponent> children)
    {
        Objects.requireNonNull(children, "children collection must not be null");
        
        final List<? extends GuiComponent> new_list = children
            .stream()
            .filter(component ->
            {
                Objects.requireNonNull(component, "component must not be null");
                return (component.parent != this && this.isAddingChildAllowed(component));
            })
            .peek(component ->
            {
                if (component.isPinned() && !this.isPinningAllowed(component))
                {
                    component.setPinned(false);
                }
            })
            .toList();
        
        if (new_list.isEmpty())
        {
            return 0;
        }
        
        final List<GuiComponent> old_list = new ArrayList<>(this.children);
        
        this.children.clear();
        this.children.addAll(Stream
            .concat(old_list.stream(), new_list.stream())
            .sorted(Comparator
                .comparing(GuiComponent::isPinned)
                .thenComparing(e -> (e.parent != this)))
            .toList());

        final GuiFont      font     = this.getFont();
        final IGuiTemplate template = this.getTemplate();

        new_list.forEach(child ->
        {
            final GuiComponent c_parent = child.getParent();
            
            if (c_parent != null)
            {
                c_parent.removeChild(child);
            }
            
            if (child.isPinned())
            {
                ++this.numPinned;
            }
            
            child.parent = this;
            child.onParentChanged();
            child.parentChanged.post(this);
            child.sendHierarchyChangeNotification();
            
            child.getPositioner().ifPresent(positioner ->
            {
                if (!this.bounds.isEmpty())
                {
                    positioner
                        .getBounds(this.bounds)
                        .accept(child::setBoundsInternal);
                }
            });
            
            this.onChildAdded(child);
            this.childAdded.post(this, new ComponentEventArgs(child));
        });
        
        if (this.isDrawing())
        {
            GuiScreen.CURRENT_SCREEN.updateHoverState();
        }
        
        return new_list.size();
    }
    
    /**
     * Adds a child component to this component and updates the component hierarchy.
     * <p>
     * If the given component is already a child of this component, this does nothing. If the given component has a
     * different parent, the component will first be removed from its old parent and then added to this component.
     * <p>
     * This will add the component at the front of all other components. If this component has pinned components and the
     * new child is not pinned, it will be added at the front of all non-pinned components but behind all pinned
     * components.
     * <p>
     * Components are treated by reference and not equality, by that means, two children with the same data but
     * different instances will not be the same component.
     *
     * @param child The new component to add
     * @return The added, or the already contained child component
     */
    public final <T extends GuiComponent> @NotNull T addChild(final @NotNull T child)
    {
        return this.addChild(child, -1);
    }
    
    /**
     * Removes the given child component from this component and updates the component hierarchy.
     * <p>
     * If there is no component at the given index, this does nothing.
     *
     * @param index The index of the child component to remove
     * @return The removed component or {@code null} if there was no component at the given index
     */
    public final @Nullable GuiComponent removeChild(final int index)
    {
        if (index < 0 || index >= this.children.size())
        {
            return null;
        }
        
        final GuiComponent child = this.children.get(index);
        
        if (!this.isRemovingChildAllowed(child))
        {
            return null;
        }
        
        this.children.remove(index);
        this.removeChildInternal(child);
        
        if (this.isDrawing())
        {
            GuiScreen.CURRENT_SCREEN.updateHoverState();
        }
        
        return child;
    }
    
    /**
     * Removes the given child component from this component and updates the component hierarchy.
     * <p>
     * If the given component is not a child of this component, this does nothing.
     *
     * @param child The child to remove
     * @return The removed component or {@code null} if the given component was not a child of this
     */
    public final @Nullable GuiComponent removeChild(final @NotNull GuiComponent child)
    {
        Objects.requireNonNull(child, "child must not be null");
        return this.removeChild(this.indexOfChild(child));
    }
    
    /**
     * Removes all child components from this component and updates the component hierarchy.
     * @return The number of children that were removed
     */
    public final int removeAllChildren()
    {
        int removed = 0;
        
        for (int i = (this.getChildCount() - 1); i >= 0; --i)
        {
            final GuiComponent child = this.children.get(i);
            
            if (!this.isRemovingChildAllowed(child))
            {
                continue;
            }
            
            this.children.remove(i);
            this.removeChildInternal(child);
            ++removed;
        }
        
        if (removed > 0)
        {
            if (this.isDrawing())
            {
                GuiScreen.CURRENT_SCREEN.updateHoverState();
            }
        }
        
        return removed;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void removeChildInternal(final @NotNull GuiComponent child)
    {
        if (child.isPinned())
        {
            --this.numPinned;
        }
        
        if (child.hasFocus())
        {
            GuiComponent.FOCUSED.updateFocusedComponent(GuiNavigationType.NONE, false);
            this.focus();
        }
        
        child.parent = null;
        child.parentChanged.post(this);
        child.sendHierarchyChangeNotification();
        
        this.onChildRemoved(child);
        this.childRemoved.post(this, new ComponentEventArgs(child));
    }
    
    //==================================================================================================================
    void sendActivityChangeNotification()
    {
        if (this.hasActiveFlag())
        {
            this.onActivityChanged();
            this.children.forEach(GuiComponent::sendActivityChangeNotification);
        }
    }
    
    void sendTemplateChangeNotification(final @Nullable IGuiTemplate template)
    {
        this.onTemplateChanged(template);
        this.onColoursChanged();
        this.children
            .stream()
            .filter(child -> (child.template == null))
            .forEach(child -> child.sendTemplateChangeNotification(template));
    }
    
    void sendFontChangeNotification(final @Nullable GuiFont font)
    {
        this.onFontChanged(font);
        this.children
            .stream()
            .filter(child -> (child.font == null))
            .forEach(child -> child.sendFontChangeNotification(font));
    }
    
    void sendHierarchyChangeNotification()
    {
        this.onHierarchyChanged();
        this.hierarchyListeners.forEach(listener -> listener.hierarchyChanged(this));
        this.children.forEach(GuiComponent::sendHierarchyChangeNotification);
    }
    
    void sendHierarchyVisibilityChangeNotification()
    {
        this.onHierarchyVisibilityChanged();
        this.hierarchyListeners.forEach(listener -> listener.visibilityChanged(this));
        this.children.forEach(GuiComponent::sendHierarchyVisibilityChangeNotification);
    }
    
    //==================================================================================================================
    /** Called whenever this component's own visibility flag has changed. */
    public void onVisibilityChanged() {}
    
    /** Called whenever this component's or any of its parent's activity state has changed. */
    public void onActivityChanged() {}
    
    /** Called whenever the message of this component changed. */
    public void onMessageChanged() {}
    
    /** Called whenever the component's internal colours or when the component's associated template changes. */
    public void onColoursChanged() {}
    
    /**
     * Called whenever the component's associated template changes. This event will propagate all the way down
     * the child hierarchy for every component until it encounters an explicit template.
     * <p>
     * Do note that this event is not triggered if this component is inheriting its template from a parent and the
     * template changes through component hierarchy updates, like when it gets a new parent. For these cases overriding
     * {@link #onHierarchyChanged()} will be necessary.
     * @param template The new {@link IGuiTemplate} that is applied to this component
     */
    public void onTemplateChanged(@Nullable IGuiTemplate template) {}
    
    /**
     * Called whenever the component's associated font changes. This event will propagate all the way down
     * the child hierarchy for every component until it encounters an explicit font.
     * <p>
     * Do note that this event is not triggered if this component is inheriting its font from a parent and the font
     * changes through component hierarchy updates, like when it gets a new parent. For these cases overriding
     * {@link #onHierarchyChanged()} will be necessary.
     * @param font The new {@link GuiFont} that is applied to this component
     */
    public void onFontChanged(@Nullable GuiFont font) {}
    
    /** Called whenever this component's tooltip has changed. */
    public void onTooltipChanged() {}
    
    /**
     * Called every draw call, useful to update things based on timing.
     * <p>
     * This method can be ignored as everything in here can just ordinarily be done in {@code #draw(Canvas)}, however,
     * it is useful for keeping the draw method clean or when something needs to be updated before the component
     * is starting to draw.
     * <p>
     * It is also worth noting that this will be called regardless of the component being drawn to the canvas
     * or not. If the component is invisible, has zero bounds or is not inside the draw area of its parents, this
     * will be called regardless.
     */
    public void onDeltaTick(@NotNull Point mousePos, float delta) {}
    
    /**
     * Called whenever this component has either lost or gained focus.
     * @param type The cause of the focus change
     */
    public void onFocusChanged(@NotNull GuiNavigationType type) {}
    
    /**
     * Called whenever the focus of this or any of its children (at any depth) migrated in our out of the component.
     * This will not be called if the focus just switched between components inside the hierarchy of this component.
     *
     * @param target The component, which changed its focus
     * @param type   The type of focus change event
     */
    public void onFocusMigrated(@NotNull GuiComponent target, @NotNull GuiNavigationType type) {}
    
    /** Called whenever the component has opened as a modal, not if a modal has been opened as a screen. */
    public void onModalOpened() {}
    
    /** Called whenever the component has closed its modal state. */
    public void onModalClosed() {}
    
    /** Called whenever the immediate parent component changed. */
    public void onParentChanged() {}
    
    /** Called whenever the parent hierarchy changed. */
    public void onHierarchyChanged() {}
    
    /** Called whenever the visibility of a parent changed. */
    public void onHierarchyVisibilityChanged() {}
    
    /**
     * Called whenever a child component was added.
     * @param child The child {@link GuiComponent} that was added
     */
    public void onChildAdded(@NotNull GuiComponent child) {}
    
    /**
     * Called whenever a child component was removed.
     * @param child The child {@link GuiComponent} that was removed
     */
    public void onChildRemoved(@NotNull GuiComponent child) {}
    
    /**
     * Called whenever the focus of a child component (at any depth) has changed.
     * @param child The child that changed focus
     * @param type  The type of focus change
     */
    public void onChildFocusChanged(@NotNull GuiComponent child, @NotNull GuiNavigationType type) {}
    
    //==================================================================================================================
    /**
     * Called whenever the bounds of this component have changed.
     * <p>
     * This method is used to set the bounds of child components.
     */
    public void resized() {}
    
    /** Called whenever the position of this component have changed inside the parent. */
    public void moved() {}
    
    //==================================================================================================================
    /**
     * Shows this component in a modal state, by that means, above all other screens currently shown. If there is
     * currently no Novia specific screen opened, this will return {@link ModalResult.Code#SCREEN_NOT_OPEN}.
     * <p>
     * If this component is already open as a modal component, nothing will happen and
     * {@link ModalResult.Code#ALREADY_SHOWING} will be returned.
     * If this component is part of another component, it will be removed from that component and
     * treated as a modal component.
     * <p>
     * Modals are bound to the screen they were opened on, so if the main screen is closed, the modal component
     * will close as well.
     * <p>
     * Note: It is recommended for modals, that should not cover the entire screen, to set a {@link Positioner}
     * for that component; this allows declaring the positioning behaviour of a modal component. The positioner of
     * a modal will always get the bounds of the entire screen if the modal has no associated component,
     * otherwise it will get the screen bounds of the associated component.
     * <p>
     * For more details see {@link ModalArgs}.
     *
     * @param args The args that define how the modal component should behave
     *
     * @return If the modal opened successfully, the returned {@link ModalResult} will complete once
     *         it closed, otherwise if it couldn't be opened, {@link ModalResult#getCode()} will return a
     *         non-empty optional, and the future will never complete.
     *         This future is not asynchronous to the render thread,
     *         thus it would not be advisable to also wait for it on the render thread.
     */
    public @NotNull ModalResult showModal(final @NotNull ModalArgs args)
    {
        if (GuiScreen.CURRENT_SCREEN == null)
        {
            return new ModalResult(ModalResult.Code.SCREEN_NOT_OPEN);
        }
        
        return GuiScreen.CURRENT_SCREEN.showModal(this, args);
    }
    
    /**
     * If this component is currently a modal component, will remove it from its modal state.
     * If it is a screen, it will close it and reopen the associated previous screen (if one was set).
     *
     * @return {@code true} if the component actually was open as a modal
     */
    public final boolean hideModal()
    {
        if (GuiScreen.CURRENT_SCREEN == null)
        {
            return false;
        }
        
        return GuiScreen.CURRENT_SCREEN.hideModal(this);
    }
    
    //==================================================================================================================
    /**
     * Draws the component below its children.
     * <p>
     * Do note that this is only called if the component is visible, has non-zero bounds and is inside the draw area
     * of its parents. If you need periodical updates regardless its visibility, it is preferable to do this in
     * {@link #onDeltaTick(Point, float)}.
     *
     * @param canvas The drawing context
     */
    public void draw(@NotNull Canvas canvas) {}
    
    /**
     * Draws the component above its children (but below its later siblings inside the parent).
     * <p>
     * Do note that this is only called if the component is visible, has non-zero bounds and is inside the draw area
     * of its parents. If you need periodical updates regardless its visibility, it is preferable to do this in
     * {@link #onDeltaTick(Point, float)}.
     * <p>
     * This will not affect mouse events to children but only rendering, any child that is getting drawn over will still
     * receive mouse events.
     *
     * @param canvas The drawing context
     */
    public void drawOnTop(@NotNull Canvas canvas) {}
    
    //==================================================================================================================
    /**
     * Used internally to render the component on screen.
     * <p>
     * This should normally not be called manually as components will call this themselves.
     *
     * @param canvas  The {@link Canvas} object to draw onto
     * @param screenX The absolute x coordinate of this component on screen
     * @param screenY The absolute y coordinate of this component on screen
     */
    public final void renderComponent(final @NotNull Canvas canvas, final int screenX, final int screenY)
    {
        final boolean inside_frame = canvas.frameIntersects(screenX, screenY, this.getWidth(), this.getHeight());
        
        if (!this.isVisible() || this.bounds.isEmpty() || !inside_frame)
        {
            this.renderUpdateHierarchy(screenX, screenY, canvas.mousePos, canvas.deltaTime);
            return;
        }
        
        this.screenX = screenX;
        this.screenY = screenY;
        
        this.onDeltaTick(canvas.mousePos, canvas.deltaTime);

        CanvasAttorney.pushFrame(canvas, this);
        {
            // draw the contents of the component
            this.draw(canvas);
            
            // draw all the children
            for (final var child : this.children)
            {
                child.renderComponent(canvas, (screenX + child.getX()), (screenY + child.getY()));
            }
            
            // reset the internal state buffer and draw on top of the component and all its children
            canvas.resetState();
            this.drawOnTop(canvas);
        }
        CanvasAttorney.popFrame(canvas);
    }
    
    //==================================================================================================================
    void updateFocusState(final @NotNull GuiNavigationType type, final boolean parent, final boolean focus)
    {
        if (!focus)
        {
            if (this.hasFocus())
            {
                GuiComponent.FOCUSED.updateFocusedComponent(type, false);
            }
        }
        else if (this.isDrawing())
        {
            if (this.wantsFocus() && this.isActive())
            {
                this.updateFocusedComponent(type, true);
                return;
            }
            
            if (this.hasFocus() && GuiComponent.FOCUSED.isDrawing())
            {
                return;
            }
            
            final IComponentNavigator navigator = this.getNavigator();
            
            if (navigator != null)
            {
                final Optional<GuiComponent> child = navigator.findFirst(this, new GuiNavigation.Tab(true));
                
                if (child.isPresent())
                {
                    child.orElseThrow().updateFocusState(type, false, true);
                    return;
                }
            }
            
            if (parent && this.parent != null)
            {
                this.parent.updateFocusState(type, true, true);
            }
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void renderUpdateHierarchy(final int screenX, final int screenY, final @NotNull Point mousePos,
                                       final float deltaTime)
    {
        this.screenX = screenX;
        this.screenY = screenY;
        
        this.onDeltaTick(mousePos, deltaTime);
        
        for (final var child : this.children)
        {
            child.renderUpdateHierarchy((screenX + child.getX()), (screenY + child.getY()), mousePos, deltaTime);
        }
    }
    
    private void updateFocusedComponent(final @NotNull GuiNavigationType type, final boolean focus)
    {
        if (!focus)
        {
            GuiComponent.FOCUSED = null;
        }
        else if (!this.isFocused())
        {
            final GuiComponent focused = GuiComponent.FOCUSED;
            GuiComponent.FOCUSED = this;
            
            if (focused != null)
            {
                focused.onFocusChanged(type);
                focused.blurred.post(focused);
                focused.updateFocusPath(focused, type);
            }
        }
        else
        {
            return;
        }
        
        this.onFocusChanged(type);
        
        if (focus)
        {
            this.focused.post(this, new FocusEventArgs(type));
        }
        else
        {
            this.blurred.post(this);
        }
        
        this.updateFocusPath(this, type);
    }
    
    private void updateFocusPath(final @NotNull GuiComponent changed, final @NotNull GuiNavigationType type)
    {
        final boolean is_focused = (this.isFocused() || this.containsChild(GuiComponent.FOCUSED));
        
        if (is_focused != ComponentFlag.FOCUS_PATH.get(this))
        {
            ComponentFlag.FOCUS_PATH.set(this, is_focused);
            this.onFocusMigrated(changed, type);
        }
        
        if (this.parent != null)
        {
            this.parent.onChildFocusChanged(changed, type);
            this.parent.updateFocusPath(changed, type);
        }
    }
}

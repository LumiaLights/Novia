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

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// Describes the natural navigation order that is used by default to navigate between components on screen.
public class NaturalNavigator
    implements IComponentNavigator
{
    //******************************************************************************************************************
    public static final NaturalNavigator INSTANCE = new NaturalNavigator();
    
    //------------------------------------------------------------------------------------------------------------------
    private static final Comparator<GuiComponent>       NATURAL_TAB_COMPARATOR;
    private static final List<Comparator<GuiComponent>> NATURAL_ARROW_COMPARATORS;
    
    //==================================================================================================================
    static
    {
        NATURAL_TAB_COMPARATOR    = Comparator
            .comparingInt(GuiComponent::getNavigationOrder)
            .thenComparing(component1 -> !component1.isPinned());
        NATURAL_ARROW_COMPARATORS = Arrays
            .stream(NavigationDirection.values())
            .map(direction ->
            {
                final NavigationDirection other_direction = direction.getAxis().getOther().getPositiveDirection();
                
                return Comparator
                    .<GuiComponent, Integer>comparing(
                        (child -> child.getScreenRect().getBoundingCoordinate(direction.getOpposite())),
                        direction.getComparator())
                    .thenComparing(
                        (child -> child.getScreenRect().getBoundingCoordinate(other_direction.getOpposite())),
                        other_direction.getComparator());
            })
            .toList();
    }
    
    //******************************************************************************************************************
    public static @NotNull NaturalNavigator withComponents(final @NotNull Collection<GuiComponent> components)
    {
        return new NaturalNavigator()
        {
            //**********************************************************************************************************
            @Override
            public @NotNull List<GuiComponent> getAll(final @NotNull GuiComponent  container,
                                                      final @NotNull GuiNavigation nav)
            {
                return List.copyOf(components);
            }
        };
    }
    
    //------------------------------------------------------------------------------------------------------------------
    protected static @NotNull Stream<GuiComponent> getAllSorted(final @NotNull GuiComponent             container,
                                                                final @NotNull Comparator<GuiComponent> comparator,
                                                                final @NotNull GuiNavigation            nav)
    {
        return container
            .getChildren()
            .stream()
            .filter(component -> component.isVisible() && component.hasActiveFlag())
            .sorted(comparator)
            .flatMap(child ->
            {
                final IComponentNavigator child_navigator = child.getNavigator();
                return Stream.concat(
                    (child.canBeNavigatedTo() ? Stream.of(child) : Stream.empty()),
                    (child_navigator != null ? child_navigator.getAll(child, nav).stream() : Stream.empty()));
            });
    }
    
    //******************************************************************************************************************
    protected NaturalNavigator() {}
    
    //==================================================================================================================
    @Override
    public @NotNull Optional<GuiComponent> findNext(final @NotNull GuiComponent  container,
                                                    final @NotNull GuiNavigation nav)
    {
        if (!container.hasFocus())
        {
            return Optional.empty();
        }
        
        final List<GuiComponent> all = this.getAll(container, nav);
        
        if (all.isEmpty())
        {
            return Optional.empty();
        }
        
        int focused_index = -1;
        
        for (int i = 0; i < all.size(); i++)
        {
            if (all.get(i).isFocused())
            {
                focused_index = i;
                break;
            }
        }
        
        if (nav instanceof GuiNavigation.Tab(final boolean forward))
        {
            final int index = (forward ? (focused_index + 1) : (focused_index - 1));
            return (index >= 0 && index < all.size() ? Optional.of(all.get(index)) : Optional.empty());
        }
        
        final GuiComponent        focused    = all.get(focused_index);
        final NavigationDirection dir        = (nav instanceof GuiNavigation.Arrow(final NavigationDirection direction)
            ? direction
            : NavigationDirection.DOWN);
        final ScreenRect          focus_rect = focused.getScreenRect();
        final NavigationAxis      other_axis = dir.getAxis().getOther();
		final int                 focus_pos  = focus_rect.getBoundingCoordinate(dir.getOpposite());
        
        return all
            .stream()
            .filter(child ->
            {
                final ScreenRect child_rect = child.getScreenRect();

                if (!child_rect.overlaps(focus_rect, other_axis) || child == focused)
                {
                    return false;
                }
                
                final int child_pos = child_rect.getBoundingCoordinate(dir.getOpposite());
                return (
                    dir.isAfter(child_pos, focus_pos)
                    || (
                        child_pos == focus_pos
                        && dir.isAfter(
                            child_rect.getBoundingCoordinate(dir),
                            focus_rect .getBoundingCoordinate(dir))
                    )
                );
            })
            .findFirst();
    }
    
    @Override
    public @NotNull Optional<GuiComponent> findFirst(final @NotNull GuiComponent  container,
                                                     final @NotNull GuiNavigation nav)
    {
        return this.getAll(container, nav).stream().findFirst();
    }
    
    @Override
    public @NotNull List<GuiComponent> getAll(final @NotNull GuiComponent container, final @NotNull GuiNavigation nav)
    {
        return NaturalNavigator
            .getAllSorted(
                container,
                (switch (nav)
                {
                    case GuiNavigation.Tab   ignored -> NaturalNavigator.NATURAL_TAB_COMPARATOR;
                    case GuiNavigation.Arrow arrow   -> NaturalNavigator.NATURAL_ARROW_COMPARATORS
                                                                        .get(arrow.getDirection().ordinal());
                    case GuiNavigation.Down  ignored -> NaturalNavigator.NATURAL_ARROW_COMPARATORS
                                                                        .get(NavigationDirection.DOWN.ordinal());
                    
                    default -> throw new UnsupportedOperationException("Unsupported navigation type: " + nav);
                }),
                nav)
            .collect(Collectors.toList());
    }
}

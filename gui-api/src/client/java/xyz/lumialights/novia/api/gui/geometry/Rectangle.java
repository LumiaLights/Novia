/**
 * .-----------------. .----------------.  .----------------.  .----------------.  .----------------.
 * | .--------------.|| .--------------. || .--------------. || .--------------. || .--------------.
 * | | | ____  _____  | || |     ____
 * | || | ____   ____  | || |     _____    | || |      __      | | | ||_   \|_   _| | || |   .'    `.   | || ||_  _| |_
 *   _| | || |    |_   _|   | || |     /  \     | | | |  |   \ | |   | || |  /  .--.  \  | || |  \ \   / /   | || |
 *   | |     | || |    / /\ \    | | | |  | |\ \| |   | || |  | |    | |  | || |   \ \ / /    | || |      | |     | || |
 *    / ____ \   | | | | _| |_\   |_  | || |  \  `--'  /  | || |    \ ' /     | || |     _| |_    | || | _/ /    \ \_ | | |
 * ||_____|\____| | || |   `.____.'   | || |     \_/      | || |    |_____|   | || ||____|  |____|| | | |              |
 * || |              | || |              | || |              | || |              | | | '--------------' ||
 * '--------------' || '--------------' || '--------------' || '--------------' | '----------------'  '----------------'
 * '----------------'  '----------------'  '----------------'
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2025 LumiaLights
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package xyz.lumialights.novia.api.gui.geometry;

import com.mojang.datafixers.util.*;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;



//**********************************************************************************************************************
/// A geometric utility class providing tools for handling positional and dimensional processes for rectangular areas
/// on screens.
///
/// ## Behaviour
/// This class is a mutable container for positional and dimensional data; modifications with any of the methods in this
/// class that do not begin with the `with` prefix will alter the instance they are executed on.
///
/// This class also provides a set of `with` prefixed methods that do not alter the current rectangle's state, but
/// instead alters the state on a newly created copy of the current instance, which is then returned.
///
/// ## Precision
/// A rectangle is always dealing with integers due to Minecraft's limited geometry. Nevertheless, all methods in the
/// Rectangle class take a [Number] object which can be passed any numeric object that inherits this interface
/// of which all will be internally converted to integers (via [Number#intValue()]). This may lead to loss of precision
/// when dealing with floating point values, as they are truncated to integral values, hence any rounding should be
/// done before passing them on to the Rectangle API.
///
/// ## Consumable
/// Rectangles provide a functional layer, that allows its bounds to be directly embedded into a different invocation
/// or to be transformed into another object entirely. For this occasion, the API provides [#accept(BoundsConsumer)]
/// and [#transform(Transformer)] methods.
///
/// ## Reusable
/// The Rectangle class provides the [Rectangle.Builder] class, with which it is possible to streamline the process
/// of creating specific transformations to rectangle objects in a functional. This is especially useful when the need
/// to apply a set of transformations to a set of rectangles arises.
///
/// Additionally, builders can be used to produce [Positioner] objects in a Java Stream-like way.
///
/// ## Java
/// Comparing instances of this class (see [Object#equals(Object)]) will compare the components of this rectangle
/// and not its identity. It also provides a custom [Object#hashCode()] implementation based on these components.
///
/// Converting this class to string (see [Object#toString()]) will be in the following format:
/// `Rectangle{pos=Point{left=,y=},width=,height=}`
public class Rectangle
{
    //******************************************************************************************************************
    /// A function interface that allows consuming a rectangle's components.
    @FunctionalInterface
    public interface BoundsConsumer
    {
        //**************************************************************************************************************
        /// Accepts the rectangle bounds.
        /// @param x      The left coordinate of the rectangle
        /// @param y      The y coordinate of the rectangle
        /// @param width  The width of the rectangle
        /// @param height The height of the rectangle
        void accept(int x, int y, int width, int height);
    }
    
    /// A function interface that allows consuming a rectangle's components and transforming them to a different
    /// object.
    @FunctionalInterface
    public interface Transformer<T>
    {
        //**************************************************************************************************************
        /// Applies the transformation.
        /// @param x      The left coordinate of the rectangle
        /// @param y      The y coordinate of the rectangle
        /// @param width  The width of the rectangle
        /// @param height The height of the rectangle
        /// @return The new object that this rectangle's components were transformed to
        T apply(int x, int y, int width, int height);
    }
    
    public static class Builder
    {
        //**************************************************************************************************************
        private Function<Rectangle, Rectangle> pipeline = Function.identity();
        
        //**************************************************************************************************************
        /// Adds an arg-less build function to the pipeline.
        /// ```java
        /// // sets the left position to 2
        /// builder.with(rect -> rect.setX(2));
        /// ```
        /// @param function The build function which no modifier arguments
        /// @return `this`
        public @NotNull Builder with(final @NotNull Function<Rectangle, Rectangle> function)
        {
            Objects.requireNonNull(function, "function must not be null");
            this.pipeline = this.pipeline.andThen(function);
            return this;
        }
        
        /// Adds a one-arg build function to the pipeline.
        /// ```java
        /// // sets the left position to 2
        /// builder.with(Rectangle::setX, 2);
        /// ```
        /// @param function The build function which takes one modifier argument
        /// @return `this`
        public <T> @NotNull Builder with(final @NotNull BiFunction<Rectangle, T, Rectangle> function,
                                         final          T                                   arg)
        {
            return this.with(rect -> function.apply(rect, arg));
        }
        
        /// Adds a two-arg build function to the pipeline.
        /// ```java
        /// // sets the size to 100 width and 50 height
        /// builder.with(Rectangle::setSize, 100, 50);
        /// ```
        /// @param function The build function which takes two modifier arguments
        /// @return `this`
        public <T1, T2> @NotNull Builder with(
            final @NotNull Function3<Rectangle, T1, T2, Rectangle> function,
            final T1 arg1, final T2 arg2)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2));
        }
        
        /// Adds a three-arg build function to the pipeline.
        /// ```java
        /// // pads 5 on top, 100 left/right and 10 on the bottom
        /// builder.with(Rectangle::pad, 5, 100, 10);
        /// ```
        /// @param function The build function which takes three modifier arguments
        /// @return `this`
        public <T1, T2, T3> @NotNull Builder with(
            final @NotNull Function4<Rectangle, T1, T2, T3, Rectangle> function,
            final T1 arg1, final T2 arg2, final T3 arg3)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2, arg3));
        }
        
        /// Adds a four-arg build function to the pipeline.
        /// ```java
        /// // sets the position left to 0, y to 10 and the size 100 width and 50 height
        /// builder.with(Rectangle::setBounds, 0, 10, 100, 50);
        /// ```
        /// @param function The build function which takes four modifier arguments
        /// @return `this`
        public <T1, T2, T3, T4> @NotNull Builder with(
            final @NotNull Function5<Rectangle, T1, T2, T3, T4, Rectangle> function,
            final T1 arg1, final T2 arg2, final T3 arg3, final T4 arg4)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2, arg3, arg4));
        }
        
        /// Adds a five-arg build function to the pipeline.
        /// ```java
        /// // aligns the rectangle horizontally left and vertically centred inside the given container bounds
        /// builder.with(Rectangle::align, Alignment.CENTRE, 0, 0, 500, 100);
        /// ```
        /// @param function The build function which takes five modifier arguments
        /// @return `this`
        public <T1, T2, T3, T4, T5> @NotNull Builder with(
            final @NotNull Function6<Rectangle, T1, T2, T3, T4, T5, Rectangle> function,
            final T1 arg1, final T2 arg2, final T3 arg3, final T4 arg4, final T5 arg5)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2, arg3, arg4, arg5));
        }
        
        //==============================================================================================================
        /// Creates a position from this builder.
        /// @return The new [Positioner]
        public @NotNull Positioner toPositioner()
        {
            return ((x, y, w, h) -> this.pipeline.apply(new Rectangle(x, y, w, h)));
        }
        
        //==============================================================================================================
        /// Builds a new [Rectangle] from this builder and an empty rectangle.
        /// @return The built [Rectangle]
        public @NotNull Rectangle build() { return this.build(new Rectangle()); }
        
        /// Builds a new [Rectangle] from this builder and the given start rect.
        /// @param initialRect The rectangle to start building on
        /// @return The built [Rectangle]
        public @NotNull Rectangle build(final @NotNull Rectangle initialRect)
        {
            return this.pipeline.apply(new Rectangle(initialRect));
        }
        
        /// Builds a new [Rectangle] from this builder and the given start rect.
        /// @param initialRect The rectangle to start building on
        /// @return The built [Rectangle]
        public @NotNull Rectangle build(final @NotNull ScreenRect initialRect)
        {
            return this.build(new Rectangle(initialRect));
        }
        
        /// Builds a new [Rectangle] from this builder and the given start rect.
        /// @param x      The left coordinate of the initial rectangle to start building on
        /// @param y      The y coordinate of the initial rectangle to start building on
        /// @param width  The width of the initial rectangle to start building on
        /// @param height The height of the initial rectangle to start building on
        /// @return The built [Rectangle]
        public @NotNull Rectangle build(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                        final @NotNull Number height)
        {
            return this.build(new Rectangle(x, y, width, height));
        }
    }
    
    //******************************************************************************************************************
    public static @NotNull Rectangle fromPoints(final @NotNull Number x1,
                                                final @NotNull Number y1,
                                                final @NotNull Number x2,
                                                final @NotNull Number y2)
    {
        final int x1v = x1.intValue();
        final int y1v = y1.intValue();
        final int x2v = x2.intValue();
        final int y2v = y2.intValue();
        return new Rectangle(Math.min(x1v, x2v), Math.min(y1v, y2v), Math.abs(x1v - x2v), Math.abs(y1v - y2v));
    }
    
    public static @NotNull Rectangle fromPoints(final @NotNull Point p1, final @NotNull Point p2)
    {
        return Rectangle.fromPoints(p1.x(), p1.y(), p2.x(), p2.y());
    }
    
    /// Creates a combined [Rectangle] with the result being the minimum area to encompass all of the rectangles in
    /// `rectangles`.
    /// @param rectangles The [Rectangle] objects to combine
    /// @return The new unified [Rectangle]
    public static @NotNull Rectangle union(final @NotNull Collection<Rectangle> rectangles)
    {
        Objects.requireNonNull(rectangles, "rectangles must not be null");
        
        int x      = Integer.MAX_VALUE;
        int y      = Integer.MAX_VALUE;
        int right  = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        
        for (final var rect : rectangles)
        {
            if (rect.x() < x)
            {
                x = rect.x();
            }
            
            if (rect.y() < y)
            {
                y = rect.y();
            }
            
            final int r_right = rect.getRight();
            
            if (r_right > right)
            {
                right = r_right;
            }
            
            final int r_bottom = rect.getBottom();
            
            if (r_bottom > bottom)
            {
                bottom = r_bottom;
            }
        }
        
        return new Rectangle(x, y, right, bottom);
    }
    
    //******************************************************************************************************************
    private final Point pos;
    
    private int width;
    private int height;
    
    //******************************************************************************************************************
    /// Constructs a new [Rectangle].
    /// @param pos    The position of the rectangle
    /// @param width  The width of the rectangle
    /// @param height The height of the rectangle
    public Rectangle(final @NotNull Point pos, final @NotNull Number width, final @NotNull Number height)
    {
        this.pos    = Objects.requireNonNull(pos, "pos must not be null");
        this.width  = width .intValue();
        this.height = height.intValue();
    }
    
    /// Constructs a new [Rectangle].
    /// @param x      The position of the rectangle on the left-axis
    /// @param y      The position of the rectangle on the y-axis
    /// @param width  The width of the rectangle
    /// @param height The height of the rectangle
    public Rectangle(final @NotNull Number x,
                     final @NotNull Number y,
                     final @NotNull Number width,
                     final @NotNull Number height)
    {
        this(new Point(x, y), width, height);
    }
    
    /// Constructs a new [Rectangle] with the given size and position zero.
    /// @param width  The width of the rectangle
    /// @param height The height of the rectangle
    public Rectangle(final @NotNull Number width, final @NotNull Number height) { this(0, 0, width, height); }
    
    /// Constructs a copy from `other`.
    /// @param other The other [Rectangle] to copy from
    public Rectangle(final @NotNull Rectangle other) { this(other.x(), other.y(), other.width(), other.height()); }
    
    /// Constructs a new [Rectangle] from the given [ScreenRect].
    /// @param screenRect The [ScreenRect] to copy from
    public Rectangle(final @NotNull ScreenRect screenRect)
    {
        this(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    /// Constructs a new empty [Rectangle] with position and size zero.
    public Rectangle() { this(0, 0, 0, 0); }
    
    //==================================================================================================================
    /// {@return the position of the rectangle on the left-axis}
    public int x() { return this.pos.x(); }
    
    /// {@return the position of the rectangle on the y-axis}
    public int y() { return this.pos.y(); }
    
    /// {@return the width of the rectangle}
    public int width() { return this.width; }
    
    /// {@return the height of the rectangle}
    public int height() { return this.height; }
    
    //==================================================================================================================
    /// {@return the coordinate on the left-axis of the right edge of this rectangle}
    public int getRight() { return (this.x() + this.width()); }
    
    /// {@return the coordinate on the y-axis of the bottom edge of this rectangle}
    public int getBottom() { return (this.y() + this.height()); }
    
    /// {@return the exact coordinate of the centre on the left-axis of this rectangle}
    public double getExactCentreX() { return (this.x() + (this.width() / 2.0)); }
    
    /// {@return the exact coordinate of the centre on the y-axis of this rectangle}
    public double getExactCentreY() { return (this.y() + (this.height() / 2.0)); }
    
    /// {@return the rounded coordinate of the centre on the left-axis of this rectangle}
    public int getCentreX() { return (int) Math.round(this.getExactCentreX()); }
    
    /// {@return the rounded coordinate of the centre on the y-axis of this rectangle}
    public int getCentreY() { return (int) Math.round(this.getExactCentreY()); }
    
    /// Gets the rounded centre point of this rectangle.
    /// @return The centre [Point]
    public @NotNull Point getCentre() { return new Point(this.getCentreX(), this.getCentreY()); }
    
    /// {@return the position of this rectangle}
    public @NotNull Point getPosition() { return this.pos; }
    
    /// Gets the top-left corner position of this rectangle. This is the same as [#getPosition()].
    /// @return The top-left corner [Point]
    public @NotNull Point getTopLeftCorner() { return this.getPosition(); }
    
    /// Gets the top-right corner position of this rectangle.
    /// @return The top-right corner [Point]
    public @NotNull Point getTopRightCorner() { return new Point(this.getRight(), this.y()); }
    
    /// Gets the bottom-left corner position of this rectangle.
    /// @return The bottom-left corner [Point]
    public @NotNull Point getBottomLeftCorner() { return new Point(this.x(), this.getBottom()); }
    
    /// Gets the bottom-right corner position of this rectangle.
    /// @return The bottom-right corner [Point]
    public @NotNull Point getBottomRightCorner() { return new Point(this.getRight(), this.getBottom()); }
    
    /// Gets the coordinate on the rectangle border from the given navigation direction.
    /// - [NavigationDirection#UP]    -> [Rectangle#y()]
    /// - [NavigationDirection#DOWN]  -> [Rectangle#getBottom()]
    /// - [NavigationDirection#LEFT]  -> [Rectangle#x()]
    /// - [NavigationDirection#RIGHT] -> [Rectangle#getRight()]
    /// @param direction The [NavigationDirection]
    /// @return The coordinate
    public int getBoundingCoordinate(final @NotNull NavigationDirection direction)
    {
        return switch (direction)
        {
            case UP    -> this.y();
            case DOWN  -> this.getBottom();
            case LEFT  -> this.x();
            case RIGHT -> this.getRight();
        };
    }
    
    /// Gets the length of the rectangle border from the given navigation direction.
    /// - [NavigationDirection#UP]    -> [Rectangle#width()]
    /// - [NavigationDirection#DOWN]  -> [Rectangle#width()]
    /// - [NavigationDirection#LEFT]  -> [Rectangle#height()]
    /// - [NavigationDirection#RIGHT] -> [Rectangle#height()]
    /// @param direction The [NavigationDirection]
    /// @return The coordinate
    public int getLength(final @NotNull NavigationDirection direction)
    {
        return switch (direction)
        {
            case UP,   DOWN  -> this.width();
            case LEFT, RIGHT -> this.height();
        };
    }
    
    /// Gets the length of the rectangle from the given axis.
    /// - [NavigationAxis#HORIZONTAL] -> [Rectangle#width()]
    /// - [NavigationAxis#VERTICAL]   -> [Rectangle#height()]
    /// @param axis The [NavigationAxis]
    /// @return The coordinate
    public int getLength(final @NotNull NavigationAxis axis)
    {
        return (axis == NavigationAxis.HORIZONTAL ? this.width() : this.height());
    }
    
    //==================================================================================================================
    /// Gets whether this rectangle is empty. A rectangle is considered empty if either its width or height
    /// is equal to zero.
    /// @return `true` if this rectangle is empty
    public boolean isEmpty() { return (this.width() < 1 || this.height() < 1); }
    
    /// Gets whether the given coordinates lie within this rectangle.
    /// @param x The left coordinate
    /// @param y The y coordinate
    /// @return `true` if the given coordinates are inside this rectangle
    public boolean contains(final @NotNull Number x, final @NotNull Number y)
    {
        return (
            x.intValue() >= this.x()
            && x.intValue() < getRight()
            && y.intValue() >= this.y()
            && y.intValue() < getBottom()
        );
    }
    
    /// Gets whether the given point lies within this rectangle.
    /// @param point The [Point] to check
    /// @return `true` if the given coordinates are inside this rectangle
    public boolean contains(final @NotNull Point point)
    {
        Objects.requireNonNull(point, "point must not be null");
        return contains(point.x(), point.y());
    }
    
    /// Gets whether the given rectangular region is fully contained within this rectangle.
    /// @param x      The left coordinate of the region
    /// @param y      The y coordinate of the region
    /// @param width  The width of the region
    /// @param height The height of the region
    /// @return `true` if the given region is fully contained
    public boolean contains(final int x, final int y, final int width, final int height)
    {
        return (this.x() <= x && this.y() <= y && this.getRight() >= (x + width) && this.getBottom() >= (y + height));
    }
    
    /// Gets whether the given [Rectangle] is fully contained within this rectangle.
    /// @param rect The [Rectangle] to check
    /// @return `true` if the given [Rectangle] is fully contained
    public boolean contains(final @NotNull Rectangle rect) { return rect.transform(this::contains); }
    
    /**
     * Gets whether the given [ScreenRect] is fully contained within this rectangle.
     * @param screenRect The [ScreenRect] to check
     * @return `true` if the given [ScreenRect] is fully contained
     */
    public boolean contains(final @NotNull ScreenRect screenRect)
    {
        Objects.requireNonNull(screenRect, "screen rect must not be null");
        return this.contains(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    /// Gets whether the given left coordinate lies within the bounds of this rect.
    /// @param x The left coordinate to test
    /// @return `true` if the coordinate is contained
    public boolean containsX(final int x) { return (this.x() <= x && this.getRight() > x); }
    
    /// Gets whether the given y coordinate lies within the bounds of this rect.
    /// @param y The y coordinate to test
    /// @return `true` if the coordinate is contained
    public boolean containsY(final int y) { return (this.y() <= y && this.getBottom() > y); }
    
    /// Gets whether this rectangle intersects with the given region.
    /// @param x      The left coordinate of the region
    /// @param y      The y coordinate of the region
    /// @param width  The width of the region
    /// @param height The height of the region
    /// @return `true` if the given region intersects with this rectangle
    public boolean intersects(final int x, final int y, final int width, final int height)
    {
        return (
            this.x() < (x + width)
            && x < this.getRight()
            && this.y() < (y + height)
            && y < this.getBottom()
        );
    }
    
    /// Gets whether this rectangle intersects with the given [Rectangle].
    /// @param rect The [Rectangle] to check
    /// @return `true` if the given [Rectangle] intersects with this rectangle
    public boolean intersects(final @NotNull Rectangle rect)
    {
        Objects.requireNonNull(rect, "rectangle must not be null");
        return rect.transform(this::intersects);
    }
    
    /// Gets whether this rectangle intersects with the given [ScreenRect].
    /// @param screenRect The [ScreenRect] to check
    /// @return `true` if the given [ScreenRect] intersects with this rectangle
    public boolean intersects(final @NotNull ScreenRect screenRect)
    {
        Objects.requireNonNull(screenRect, "screen rect must not be null");
        return this.intersects(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    //==================================================================================================================
    /// Converts this [Rectangle] into a [ScreenRect].
    /// @return A new [ScreenRect] with the same properties as this [Rectangle]
    public @NotNull ScreenRect toScreenRect() { return this.transform(ScreenRect::new); }
    
    /// Converts an absolute left coordinate into a coordinate relative to this rectangle's origin.
    /// @param absoluteX The absolute left coordinate
    /// @return The relative left coordinate
    public int toRelativeX(final int absoluteX) { return (absoluteX - this.x()); }
    
    /// Converts an absolute left coordinate into a coordinate relative to this rectangle's origin.
    /// @param absoluteX The absolute left coordinate
    /// @return The relative left coordinate
    public double toRelativeX(final double absoluteX) { return (absoluteX - this.x()); }
    
    /// Converts an absolute y coordinate into a coordinate relative to this rectangle's origin.
    /// @param absoluteY The absolute y coordinate
    /// @return The relative y coordinate
    public int toRelativeY(final int absoluteY) { return (absoluteY - this.y()); }
    
    /// Converts an absolute y coordinate into a coordinate relative to this rectangle's origin.
    /// @param absoluteY The absolute y coordinate
    /// @return The relative y coordinate
    public double toRelativeY(final double absoluteY) { return (absoluteY - this.y()); }
    
    /// Converts an absolute point into a point relative to this rectangle's origin.
    /// @param absoluteX The absolute left coordinate
    /// @param absoluteY The absolute y coordinate
    /// @return The relative [Point]
    public @NotNull Point toRelativePoint(final int absoluteX, final int absoluteY)
    {
        return this.getPosition().toRelativePoint(absoluteX, absoluteY);
    }
    
    /// Converts an absolute point into a point relative to this rectangle's origin.
    /// @param absoluteX The absolute left coordinate
    /// @param absoluteY The absolute y coordinate
    /// @return The relative [Point]
    public @NotNull Point toRelativePoint(final double absoluteX, final double absoluteY)
    {
        return this.getPosition().toRelativePoint(absoluteX, absoluteY);
    }
    
    /// Converts an absolute point into a point relative to this rectangle's origin.
    /// @param absolutePoint The absolute [Point]
    /// @return The relative [Point]
    public @NotNull Point toRelativePoint(final @NotNull Point absolutePoint)
    {
        return this.getPosition().toRelativePoint(absolutePoint);
    }
    
    //==================================================================================================================
    /// Sets the left position of this rectangle.
    /// @param x The new left position
    /// @return `this`
    public @NotNull Rectangle setX(final @NotNull Number x)
    {
        this.pos.setX(x);
        return this;
    }
    
    /// Sets the y position of this rectangle.
    /// @param y The new y position
    /// @return `this`
    public @NotNull Rectangle setY(final @NotNull Number y)
    {
        this.pos.setY(y);
        return this;
    }
    
    /// Sets the width of this rectangle.
    /// @param width The new width
    /// @return `this`
    public @NotNull Rectangle setWidth(final @NotNull Number width)
    {
        this.width = width.intValue();
        return this;
    }
    
    /// Sets the height of this rectangle.
    /// @param height The new height
    /// @return `this`
    public @NotNull Rectangle setHeight(final @NotNull Number height)
    {
        this.height = height.intValue();
        return this;
    }
    
    /// Sets the position of this rectangle.
    /// @param x The new left position
    /// @param y The new y position
    /// @return `this`
    public @NotNull Rectangle setPosition(final @NotNull Number x, final @NotNull Number y)
    {
        this.pos.setPosition(x, y);
        return this;
    }
    
    /// Sets the position of this rectangle.
    /// @param position The new position
    /// @return `this`
    public @NotNull Rectangle setPosition(final @NotNull Point position)
    {
        this.setPosition(position.x(), position.y());
        return this;
    }
    
    /// Sets the position of this rectangle.
    /// @param rect The other rect to get the position from
    /// @return `this`
    public @NotNull Rectangle setPosition(final @NotNull Rectangle rect)
    {
        this.setPosition(rect.x(), rect.y());
        return this;
    }
    
    /// Sets the size of this rectangle.
    /// @param width  The new width
    /// @param height The new height
    /// @return `this`
    public @NotNull Rectangle setSize(final @NotNull Number width, final @NotNull Number height)
    {
        this.width  = width.intValue();
        this.height = height.intValue();
        return this;
    }
    
    /// Sets the size of this rectangle.
    /// @param rect The other rect to get the size from
    /// @return `this`
    public @NotNull Rectangle setSize(final @NotNull Rectangle rect)
    {
        this.width  = rect.width();
        this.height = rect.height();
        
        return this;
    }
    
    /// Sets the bounds of this rectangle.
    /// @param x      The new left position
    /// @param y      The new y position
    /// @param width  The new width
    /// @param height The new height
    /// @return `this`
    public @NotNull Rectangle setBounds(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                        final @NotNull Number height)
    {
        this.setPosition(x, y);
        this.setSize(width, height);
        return this;
    }
    
    /// Sets the bounds of this rectangle.
    /// @param other Another [Rectangle] to adapt the bounds from
    /// @return `this`
    public @NotNull Rectangle setBounds(final @NotNull Rectangle other) { return other.transform(this::setBounds); }
    
    /// Sets the bounds of this rectangle.
    /// @param rect A [ScreenRect] to adapt the bounds from
    /// @return `this`
    public @NotNull Rectangle setBounds(final @NotNull ScreenRect rect)
    {
        return this.setBounds(rect.getLeft(), rect.getTop(), rect.width(), rect.height());
    }
    
    /// Sets the position of this rectangle so that the given left and y coordinates are at the centre of this rectangle.
    /// @param x The left coordinate to set the centre of this rectangle to
    /// @param y The y coordinate to set the centre of this rectangle to
    /// @return `this`
    public @NotNull Rectangle setCentre(final @NotNull Number x, final @NotNull Number y)
    {
        this.setPosition(
            Math.round(x.doubleValue() - (this.width  / 2.0)),
            Math.round(y.doubleValue() - (this.height / 2.0)));
        return this;
    }
    
    /// Sets the position of this rectangle so that the given position is at the centre of this rectangle.
    /// @param position The position to set the centre of this rectangle to. Must not be null.
    /// @return `this`
    public @NotNull Rectangle setCentre(final @NotNull Point position)
    {
        Objects.requireNonNull(position, "position must not be null");
        this.setCentre(position.x(), position.y());
        
        return this;
    }
    
    /// Sets the left position of this rectangle and adjusts the width of the rectangle so that the right side
    /// of the rectangle stays the same.
    /// @param left The new left position of this rectangle
    /// @return `this`
    public @NotNull Rectangle setLeft(final @NotNull Number left)
    {
        final int right = this.getRight();
        
        this.setX(left);
        this.width = Math.max(0, (right - this.x()));
        
        return this;
    }
    
    /// Sets the top position of this rectangle and adjusts the height of the rectangle so that the bottom side
    /// of the rectangle stays the same.
    /// @param top The new top position of this rectangle
    /// @return `this`
    public @NotNull Rectangle setTop(final @NotNull Number top)
    {
        final int bottom = this.getBottom();
        
        this.setY(top);
        this.height = Math.max(0, (bottom - this.y()));
        
        return this;
    }
    
    /// Sets the right position of this rectangle and adjusts the width of the rectangle so that the left side
    /// of the rectangle stays the same.
    /// @param right The new right position of this rectangle
    /// @return `this`
    public @NotNull Rectangle setRight(final @NotNull Number right)
    {
        this.width = Math.max(0, (right.intValue() - this.x()));
        return this;
    }
    
    /// Sets the bottom position of this rectangle and adjusts the height of the rectangle so that the top side
    /// of the rectangle stays the same.
    /// @param bottom The new bottom position of this rectangle
    /// @return `this`
    public @NotNull Rectangle setBottom(final @NotNull Number bottom)
    {
        this.height = Math.max(0, (bottom.intValue() - this.y()));
        return this;
    }
    
    /// Sets the left position of this rectangle so that the right edge is aligned to the left edge of the neighbouring
    /// rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return `this`
    public @NotNull Rectangle setLeftOf(final @NotNull Rectangle neighbour)
    {
        this.setX(neighbour.x() - this.width);
        return this;
    }
    
    /// Sets the y position of this rectangle so that the bottom edge is aligned to the top edge of the neighbouring
    /// rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return `this`
    public @NotNull Rectangle setTopOf(final @NotNull Rectangle neighbour)
    {
        this.setY(neighbour.y() - this.height);
        return this;
    }
    
    /// Sets the left position of this rectangle so that the left edge is aligned to the right edge of the neighbouring
    /// rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return `this`
    public @NotNull Rectangle setRightOf(final @NotNull Rectangle neighbour)
    {
        this.setX(neighbour.getRight());
        return this;
    }
    
    /// Sets the y position of this rectangle so that the top edge is aligned to the bottom edge of the neighbouring
    /// rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return `this`
    public @NotNull Rectangle setBottomOf(final @NotNull Rectangle neighbour)
    {
        this.setY(neighbour.getBottom());
        return this;
    }

    /// Resets the position and size of this rectangle to an empty rectangle.
    /// @return `this`
    public @NotNull Rectangle reset()
    {
        this.setPosition(0, 0);
        this.setSize(0, 0);
        return this;
    }

    /// Resets the position of this rectangle to [0, 0].
    /// @return `this`
    public @NotNull Rectangle resetPos()
    {
        this.setPosition(0, 0);
        return this;
    }
    
    /// Sets the size of this rectangle to fit within the given maximum constraints, which it will be clamped to.
    ///
    /// Negative values will be clamped to the positive range.
    /// @param maxWidth  The maximum width this rectangle can have
    /// @param maxHeight The maximum height this rectangle can have
    /// @return `this`
    public @NotNull Rectangle constrainToMax(final @NotNull Number maxWidth, final @NotNull Number maxHeight)
    {
        this.width  = Math.min(this.width,  Math.max(0, maxWidth .intValue()));
        this.height = Math.min(this.height, Math.max(0, maxHeight.intValue()));
        return this;
    }
    
    /// Sets the size of this rectangle to fit within the given minimum constraints, which it will be clamped to.
    ///
    /// Negative values will be clamped to the positive range.
    /// @param minWidth  The minimum width this rectangle can have
    /// @param minHeight The minimum height this rectangle can have
    /// @return `this`
    public @NotNull Rectangle constrainToMin(final @NotNull Number minWidth, final @NotNull Number minHeight)
    {
        this.width  = Math.max(this.width,  Math.max(0, minWidth .intValue()));
        this.height = Math.max(this.height, Math.max(0, minHeight.intValue()));
        return this;
    }
    
    /// Sets the size of this rectangle to fit within the given minimum/maximum constraints, which it will be
    /// clamped to.
    ///
    /// Negative values will be clamped to the positive range.
    /// @param minWidth  The minimum width this rectangle can have
    /// @param minHeight The minimum height this rectangle can have
    /// @param maxWidth  The maximum width this rectangle can have
    /// @param maxHeight The maximum height this rectangle can have
    /// @return `this`
    /// @throws IllegalArgumentException If minWidth/minHeight is greater than maxWidth/maxHeight
    public @NotNull Rectangle constrainToMinMax(final @NotNull Number minWidth,
                                                final @NotNull Number minHeight,
                                                final @NotNull Number maxWidth,
                                                final @NotNull Number maxHeight)
    {
        this.width  = Math.clamp(this.width,  Math.max(0, minWidth .intValue()), Math.max(0, maxWidth .intValue()));
        this.height = Math.clamp(this.height, Math.max(0, minHeight.intValue()), Math.max(0, maxHeight.intValue()));
        return this;
    }
    
    //==================================================================================================================
    /// Translates this rectangle by the given amount.
    /// @param xOffset The offset on the left-axis
    /// @param yOffset The offset on the y-axis
    /// @return `this`
    public @NotNull Rectangle translate(final @NotNull Number xOffset, final @NotNull Number yOffset)
    {
        this.pos.translate(xOffset, yOffset);
        return this;
    }
    
    /// Translates this rectangle's left-axis by the given offset.
    /// @param offset The offset on the left-axis
    /// @return `this`
    public @NotNull Rectangle translateX(final @NotNull Number offset)
    {
        this.pos.translateX(offset);
        return this;
    }
    
    /// Translates this rectangle's y-axis by the given offset.
    /// @param offset The offset on the y-axis
    /// @return `this`
    public @NotNull Rectangle translateY(final @NotNull Number offset)
    {
        this.pos.translateY(offset);
        return this;
    }
    
    /// Pads this rectangle by the given amount for the given side. If the amount is positive, the rectangle will
    /// shrink, and if it is negative, the rectangle will grow on that side.
    /// @param left   The amount to pad on the left side
    /// @param top    The amount to pad on the top side
    /// @param right  The amount to pad on the right side
    /// @param bottom The amount to pad on the bottom side
    /// @return `this`
    public @NotNull Rectangle pad(final @NotNull Number left, final @NotNull Number top, final @NotNull Number right,
                                  final @NotNull Number bottom)
    {
        this.translate(left, top);
        this.width  -= Math.min(this.width,  (left.intValue() + right.intValue()));
        this.height -= Math.min(this.height, (top .intValue() + bottom.intValue()));
        
        return this;
    }
    
    /// Pads this rectangle by the given amount for the given side. If the amount is positive, the rectangle will
    /// shrink, and if it is negative, the rectangle will grow on that side.
    /// @param top          The amount to pad on the top side
    /// @param leftAndRight The amount to pad on the left and right side
    /// @param bottom       The amount to pad on the bottom side
    /// @return `this`
    public @NotNull Rectangle pad(final @NotNull Number top,
                                  final @NotNull Number leftAndRight,
                                  final @NotNull Number bottom)
    {
        return this.pad(leftAndRight, top, leftAndRight, bottom);
    }
    
    /// Pads this rectangle by the given amount for the given side. If the amount is positive, the rectangle will
    /// shrink, and if it is negative, the rectangle will grow on that side.
    /// @param leftAndRight The amount to pad on the left and right side
    /// @param topAndBottom The amount to pad on the top and bottom side
    /// @return `this`
    public @NotNull Rectangle pad(final @NotNull Number leftAndRight, final @NotNull Number topAndBottom)
    {
        return this.pad(leftAndRight, topAndBottom, leftAndRight, topAndBottom);
    }
    
    /// Pads this rectangle by the given amount for the given side. If the amount is positive, the rectangle will
    /// shrink, and if it is negative, the rectangle will grow on that side.
    /// @param allSides The amount to pad on all sides of this rectangle
    /// @return `this`
    public @NotNull Rectangle pad(final @NotNull Number allSides)
    {
        return this.pad(allSides, allSides, allSides, allSides);
    }
    
    /// Pads this rectangle by the given frame. If the frame's coefficients are positive, the rectangle will shrink,
    /// and if they are negative, the rectangle will grow on that side.
    /// @param frame The frame to pad this rectangle by
    /// @return `this`
    public @NotNull Rectangle pad(final @NotNull Frame frame) { return frame.transform(this::pad); }
    
    /// Pads this rectangle on the left side by the given amount. If the amount is positive, the rectangle will shrink,
    /// and if it is negative, the rectangle will grow on the left.
    /// @param amount The amount to pad on the left side
    /// @return `this`
    public @NotNull Rectangle padLeft(final @NotNull Number amount) { return this.pad(amount, 0, 0, 0); }
    
    /// Pads this rectangle on the top side by the given amount. If the amount is positive, the rectangle will shrink,
    /// and if it is negative, the rectangle will grow on the top.
    /// @param amount The amount to pad on the top side
    /// @return `this`
    public @NotNull Rectangle padTop(final @NotNull Number amount) { return this.pad(0, amount, 0, 0); }
    
    /// Pads this rectangle on the right side by the given amount. If the amount is positive, the rectangle will shrink,
    /// and if it is negative, the rectangle will grow on the right.
    /// @param amount The amount to pad on the right side
    /// @return `this`
    public @NotNull Rectangle padRight(final @NotNull Number amount) { return this.pad(0, 0, amount, 0); }
    
    /// Pads this rectangle on the bottom side by the given amount. If the amount is positive, the rectangle will
    /// shrink, and if it is negative, the rectangle will grow on the bottom.
    /// @param amount The amount to pad on the bottom side
    /// @return `this`
    public @NotNull Rectangle padBottom(final @NotNull Number amount) { return this.pad(0, 0, 0, amount); }
    
    //==================================================================================================================
    /// Removes the given amount from the left side of this rectangle and returns a new rectangle with the removed
    /// portion.
    /// @param amount The amount to remove from the left side of this rectangle
    /// @return A new rectangle with the removed portion
    public @NotNull Rectangle removeLeft(@NotNull Number amount)
    {
        amount = Math.min(this.width, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setWidth(amount);
        this.padLeft(amount);
        
        return result;
    }
    
    /// Removes the given amount from the top side of this rectangle and returns a new rectangle with the removed
    /// portion.
    /// @param amount The amount to remove from the top side of this rectangle
    /// @return A new rectangle with the removed portion
    public @NotNull Rectangle removeTop(@NotNull Number amount)
    {
        amount = Math.min(this.height, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setHeight(amount);
        this.padTop(amount);
        
        return result;
    }
    
    /// Removes the given amount from the right side of this rectangle and returns a new rectangle with the removed
    /// portion.
    /// @param amount The amount to remove from the right side of this rectangle
    /// @return A new rectangle with the removed portion
    public @NotNull Rectangle removeRight(@NotNull Number amount)
    {
        amount = Math.min(this.width, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setWidth(amount);
        this.padRight(amount);
        
        return result.setX(this.getRight());
    }
    
    /// Removes the given amount from the bottom side of this rectangle and returns a new rectangle with the removed
    /// portion.
    /// @param amount The amount to remove from the bottom side of this rectangle
    /// @return A new rectangle with the removed portion
    public @NotNull Rectangle removeBottom(@NotNull Number amount)
    {
        amount = Math.min(this.height, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setHeight(amount);
        this.padBottom(amount);
        
        return result.setY(this.getBottom());
    }
    
    /// Combines this rectangle with the given other rectangle. The resulting rectangle will be the smallest rectangle
    /// that contains both of the original rectangles.
    /// @param x      The other rectangle's coordinate on the left-axis
    /// @param y      The other rectangle's coordinate on the y-axis
    /// @param width  The other rectangle's width
    /// @param height The other rectangle's height
    /// @return `this`
    public @NotNull Rectangle combine(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                      final @NotNull Number height)
    {
        final int min_x = Math.min(this.x(), x.intValue());
        final int min_y = Math.min(this.y(), y.intValue());
        final int max_x = Math.max(this.getRight(),  (x.intValue() + width .intValue()));
        final int max_y = Math.max(this.getBottom(), (y.intValue() + height.intValue()));

        return this.setBounds(min_x, min_y, (max_x - min_x), (max_y - min_y));
    }
    
    /// Combines this rectangle with the given other rectangle. The resulting rectangle will be the smallest rectangle
    /// that contains both of the original rectangles.
    /// @param other The other rectangle to combine this rectangle with
    /// @return `this`
    public @NotNull Rectangle combine(final @NotNull Rectangle other) { return other.transform(this::combine); }
    
    /// Intersects this rectangle with the given area and sets this rectangle to the intersection result.
    /// @param x      The left coordinate of the area to intersect
    /// @param y      The y coordinate of the area to intersect
    /// @param width  The width of the area to intersect
    /// @param height The height coordinate of the area to intersect
    /// @return `this`
    public @NotNull Rectangle intersect(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                        final @NotNull Number height)
    {
        final int new_x = Math.max(this.x(), x.intValue());
        final int new_y = Math.max(this.y(), y.intValue());
        
        this.width  = Math.max(0, (Math.min(this.getRight(),  (x.intValue() + width .intValue())) - new_x));
        this.height = Math.max(0, (Math.min(this.getBottom(), (y.intValue() + height.intValue())) - new_y));
        this.setPosition(new_x, new_y);
        
        return this;
    }
    
    /// Intersects this rectangle with the given other [Rectangle] and sets this rectangle to the intersection result.
    /// @param other The other [Rectangle] to intersect
    /// @return `this`
    public @NotNull Rectangle intersect(final @NotNull Rectangle other) { return other.transform(this::intersect); }
    
    /// Intersects this rectangle with the given [ScreenRect] and sets this rectangle to the intersection result.
    /// @param rect The [ScreenRect] to intersect
    /// @return `this`
    public @NotNull Rectangle intersect(final @NotNull ScreenRect rect)
    {
        return this.intersect(rect.getLeft(), rect.getTop(), rect.width(), rect.height());
    }
    
    //==================================================================================================================
    /// Aligns this rectangle to the given parent container rectangle.
    /// @param alignment The alignment to align this rectangle to
    /// @param container The parent container rectangle
    /// @return `this`
    public @NotNull Rectangle align(final @NotNull Alignment alignment, final @NotNull Rectangle container)
    {
        Objects.requireNonNull(alignment, "alignment must not be null");
        Objects.requireNonNull(container, "container must not be null");
        
        this.setBounds(alignment.align(container, this));
        
        return this;
    }
    
    /// Aligns this rectangle to the given parent container rectangle.
    /// @param alignment       The alignment to align this rectangle to
    /// @param containerX      The position of the area that the target area should be aligned to on the left-axis
    /// @param containerY      The position of the area that the target area should be aligned to on the y-axis
    /// @param containerWidth  The width of the area that the target area should be aligned to
    /// @param containerHeight The height of the area that the target area should be aligned to
    /// @return `this`
    public @NotNull Rectangle align(final @NotNull Alignment alignment,
                                    final @NotNull Number    containerX,
                                    final @NotNull Number    containerY,
                                    final @NotNull Number    containerWidth,
                                    final @NotNull Number    containerHeight)
    {
        Objects.requireNonNull(alignment, "alignment must not be null");
        this.setBounds(alignment.align(containerX, containerY, containerWidth, containerHeight, this));
        return this;
    }
    
    /// Aligns this rectangle to be at the centre of the given parent container rectangle.
    /// @param container The parent container rectangle
    /// @return `this`
    public @NotNull Rectangle centre(final @NotNull Rectangle container)
    {
        this.align(Alignment.MIDDLE_CENTRE, container);
        return this;
    }
    
    /// Aligns this rectangle to be at the centre of the given parent container rectangle.
    /// @param containerX      The position of the area that the target area should be aligned to on the left-axis
    /// @param containerY      The position of the area that the target area should be aligned to on the y-axis
    /// @param containerWidth  The width of the area that the target area should be aligned to
    /// @param containerHeight The height of the area that the target area should be aligned to
    /// @return `this`
    public @NotNull Rectangle centre(final @NotNull Number containerX,
                                     final @NotNull Number containerY,
                                     final @NotNull Number containerWidth,
                                     final @NotNull Number containerHeight)
    {
        this.align(Alignment.MIDDLE_CENTRE, containerX, containerY, containerWidth, containerHeight);
        return this;
    }
    
    //==================================================================================================================
    /// Returns a new rectangle with the given left coordinate.
    /// @param x The new left coordinate
    /// @return The new [Rectangle]
    public @NotNull Rectangle withX(final @NotNull Number x)
    {
        return new Rectangle(x, this.y(), this.width, this.height);
    }
    
    /// Returns a new rectangle with the given y coordinate.
    /// @param y The new y coordinate
    /// @return The new [Rectangle]
    public @NotNull Rectangle withY(final @NotNull Number y)
    {
        return new Rectangle(this.x(), y, this.width, this.height);
    }
    
    /// Returns a new rectangle with the given width.
    /// @param width The new width
    /// @return The new [Rectangle]
    public @NotNull Rectangle withWidth(final @NotNull Number width)
    {
        return new Rectangle(this.x(), this.y(), width, this.height);
    }
    
    /// Returns a new rectangle with the given height.
    /// @param height The new height
    /// @return The new [Rectangle]
    public @NotNull Rectangle withHeight(final @NotNull Number height)
    {
        return new Rectangle(this.x(), this.y(), this.width, height);
    }
    
    /// Returns a new rectangle with the given position on the left side of the rectangle and adjusted width so the
    /// right side stays the same.
    /// @param left The new left position of the rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withLeft(final @NotNull Number left)
    {
        return new Rectangle(left, this.y(), (this.width + (this.x() - left.intValue())), this.height);
    }
    
    /// Returns a new rectangle with the given position on the top side of the rectangle and adjusted height so the
    /// bottom side stays the same.
    /// @param top The new top position of the rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withTop(final @NotNull Number top)
    {
        return new Rectangle(this.x(), top, this.width, (this.height + (this.y() - top.intValue())));
    }
    
    /// Returns a new rectangle with the given position on the right side of the rectangle and adjusted width so the
    /// right side stays the same.
    /// @param right The new right position of the rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withRight(final @NotNull Number right)
    {
        return new Rectangle(this.x(), this.y(), (this.width - (this.getRight() - right.intValue())), this.height);
    }
    
    /// Returns a new rectangle with the given position on the bottom side of the rectangle and adjusted height so the
    /// bottom side stays the same.
    /// @param bottom The new bottom position of the rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withBottom(final @NotNull Number bottom)
    {
        return new Rectangle(this.x(), this.y(), this.width, (this.height - (this.getBottom() - bottom.intValue())));
    }
    
    /// Returns a new rectangle with the new position.
    /// @param x The new left position
    /// @param y The new y position
    /// @return The new [Rectangle]
    public @NotNull Rectangle withPosition(final @NotNull Number x, final @NotNull Number y)
    {
        return new Rectangle(x, y, this.width, this.height);
    }
    
    /// Returns a new rectangle with the new position.
    /// @param position The new position
    /// @return The new [Rectangle]
    public @NotNull Rectangle withPosition(final @NotNull Point position)
    {
        Objects.requireNonNull(position, "position must not be null");
        return this.withPosition(position.x(), position.y());
    }
    
    /// Returns a new rectangle with the new size.
    /// @param width  The new width
    /// @param height The new height
    /// @return The new [Rectangle]
    public @NotNull Rectangle withSize(final @NotNull Number width, final @NotNull Number height)
    {
        return new Rectangle(this.x(), this.y(), width, height);
    }
    
    /// Returns this rectangle as a new rectangle with the given minimum size applied so that it will at least have
    /// the given minimum size.
    /// @param minWidth  The minimum width of the new rectangle
    /// @param minHeight The minimum height of the new rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withMinimumSize(final @NotNull Number minWidth, final @NotNull Number minHeight)
    {
        return new Rectangle(this).constrainToMin(minWidth, minHeight);
    }
    
    /// Returns this rectangle as a new rectangle with the given maximum size applied so that it will at most have
    /// the given maximum size.
    /// @param maxWidth  The maximum width of the new rectangle
    /// @param maxHeight The maximum height of the new rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withMaximumSize(final int maxWidth, final int maxHeight)
    {
        return new Rectangle(this).constrainToMax(maxWidth, maxHeight);
    }
    
    /// Returns this rectangle as a new rectangle with the given minimum and maximum size applied so that it will
    /// at least have the given minimum size and at most have the given maximum size.
    /// @param minWidth  The minimum width of the new rectangle
    /// @param minHeight The minimum height of the new rectangle
    /// @param maxWidth  The maximum width of the new rectangle
    /// @param maxHeight The maximum height of the new rectangle
    /// @return The new [Rectangle]
    /// @throws IllegalArgumentException If minWidth/minHeight is greater than maxWidth/maxHeight
    public @NotNull Rectangle withMinMaxSize(final @NotNull Number minWidth,
                                             final @NotNull Number minHeight,
                                             final @NotNull Number maxWidth,
                                             final @NotNull Number maxHeight)
    {
        return new Rectangle(this).constrainToMinMax(minWidth, minHeight, maxWidth, maxHeight);
    }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param padding The padding to apply
    /// @return The new [Rectangle]
    public @NotNull Rectangle withPadding(final @NotNull Number padding) { return new Rectangle(this).pad(padding); }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param leftAndRight The padding to apply on the left and right side
    /// @param topAndBottom The padding to apply on the top and bottom side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withPadding(final @NotNull Number leftAndRight, final @NotNull Number topAndBottom)
    {
        return new Rectangle(this).pad(leftAndRight, topAndBottom);
    }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param left   The padding to apply on the left side
    /// @param top    The padding to apply on the top side
    /// @param right  The padding to apply on the right side
    /// @param bottom The padding to apply on the bottom side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withPadding(final @NotNull Number left, final @NotNull Number top,
                                          final @NotNull Number right, final @NotNull Number bottom)
    {
        return new Rectangle(this).pad(left, top, right, bottom);
    }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param frame The [Frame] containing the padding
    /// @return The new [Rectangle]
    public @NotNull Rectangle withPadding(final @NotNull Frame frame) { return new Rectangle(this).pad(frame); }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param amount The amount to pad on the left side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withLeftPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padLeft(amount);
    }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param amount The amount to pad on the top side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withTopPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padTop(amount);
    }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param amount The amount to pad on the right side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withRightPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padRight(amount);
    }
    
    /// Returns a new [Rectangle] with the given padding applied.
    /// @param amount The amount to pad on the bottom side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withBottomPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padBottom(amount);
    }
    
    /// Returns a new [Rectangle] with the given translation applied.
    /// @param offsetX The left translation to apply
    /// @param offsetY The y translation to apply
    /// @return The new [Rectangle]
    public @NotNull Rectangle withTranslation(final @NotNull Number offsetX, final @NotNull Number offsetY)
    {
        return new Rectangle((this.x() + offsetX.intValue()), (this.y() + offsetY.intValue()), this.width, this.height);
    }
    
    /// Returns this rectangle as a new rectangle with the translation applied to the left-axis of the new rectangle.
    /// @param offset The left translation to apply
    /// @return The new [Rectangle]
    public @NotNull Rectangle withTranslationX(final @NotNull Number offset)
    {
        return new Rectangle((this.x() + offset.intValue()), this.y(), this.width, this.height);
    }
    
    /// Returns this rectangle as a new rectangle with the translation applied to the y-axis of the new rectangle.
    /// @param offset The y translation to apply
    /// @return The new [Rectangle]
    public @NotNull Rectangle withTranslationY(final @NotNull Number offset)
    {
        return new Rectangle(this.x(), (this.y() + offset.intValue()), this.width, this.height);
    }
    
    /// Returns this rectangle as a new rectangle with the given alignment applied inside the given parent container
    /// rectangle.
    /// @param alignment The [Alignment] to apply
    /// @param container The parent rectangle to position this rectangle in
    /// @return The new aligned [Rectangle]
    public @NotNull Rectangle withAlignment(final @NotNull Alignment alignment, final @NotNull Rectangle container)
    {
        return new Rectangle(this).align(alignment, container);
    }
    
    /// Returns this rectangle as a new rectangle with the position being centred inside the given parent container
    /// rectangle.
    /// @param container The parent rectangle to position this rectangle in
    /// @return The new centred [Rectangle]
    public @NotNull Rectangle withCentering(final @NotNull Rectangle container)
    {
        return new Rectangle(this).centre(container);
    }

    /// Returns this rectangle as a new rectangle with the position set to zero.
    /// @return The new localised [Rectangle]
    public @NotNull Rectangle withLocalPos() { return new Rectangle(0, 0, this.width, this.height); }
    
    /// Returns this rectangle as a new rectangle with only the portion that was cut from the left side.
    /// @param amount The amount to cut on the left side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withLeftCut(final @NotNull Number amount)
    {
        return new Rectangle(this).removeLeft(amount);
    }
    
    /// Returns this rectangle as a new rectangle with only the portion that was cut from the right side.
    /// @param amount The amount to cut on the right side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withRightCut(final @NotNull Number amount)
    {
        return new Rectangle(this).removeRight(amount);
    }
    
    /// Returns this rectangle as a new rectangle with only the portion that was cut from the top side.
    /// @param amount The amount to cut on the top side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withTopCut(final @NotNull Number amount) { return new Rectangle(this).removeTop(amount); }
    
    /// Returns this rectangle as a new rectangle with only the portion that was cut from the bottom side.
    /// @param amount The amount to cut on the bottom side
    /// @return The new [Rectangle]
    public @NotNull Rectangle withBottomCut(final @NotNull Number amount)
    {
        return new Rectangle(this).removeBottom(amount);
    }
    
    /// Returns this rectangle positioned so that the right side of the new rectangle is aligned to the left side of
    /// the neighbouring rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withLeftOf(final @NotNull Rectangle neighbour)
    {
        return this.withX(neighbour.x() - this.width);
    }
    
    /// Returns this rectangle positioned so that the left side of the new rectangle is aligned to the right side of
    /// the neighbouring rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withRightOf(final @NotNull Rectangle neighbour)
    {
        return this.withX(neighbour.getRight());
    }
    
    /// Returns this rectangle positioned so that the top side of the new rectangle is aligned to the bottom side of
    /// the neighbouring rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withTopOf(final @NotNull Rectangle neighbour)
    {
        return this.withY(neighbour.y() - this.height);
    }
    
    /// Returns this rectangle positioned so that the bottom side of the new rectangle is aligned to the top side of
    /// the neighbouring rectangle.
    /// @param neighbour The neighbouring rectangle
    /// @return The new [Rectangle]
    public @NotNull Rectangle withBottomOf(final @NotNull Rectangle neighbour)
    {
        return this.withY(neighbour.getBottom());
    }
    
    //==================================================================================================================
    /// Accepts this rectangle's components with the given consumer.
    /// @param consumer The [BoundsConsumer] to apply this rectangle's components to
    public void accept(final @NotNull BoundsConsumer consumer)
    {
        consumer.accept(this.x(), this.y(), this.width(), this.height());
    }
    
    /// Accepts this rectangle's components with the given consumer.
    ///
    /// This is similar to [#accept(BoundsConsumer)], but applies this rectangle's size components first as opposed
    /// to the position components. This is majorly a compatibility function for cases when
    /// [ClickableWidget#setDimensionsAndPosition(int, int, int, int)] is used as there size and position are
    /// reversed.
    /// @param consumer The [BoundsConsumer] to apply this rectangle's components to
    public void acceptSf(final @NotNull BoundsConsumer consumer)
    {
        consumer.accept(this.width(), this.height(), this.x(), this.y());
    }
    
    /// Applies this rectangle's components to the given transformer and returns the result of this operation.
    /// @param transformer The [Transformer] to apply this rectangle's components to
    /// @return The result of this operation
    /// @param <T> The result type of this operation
    public <T> T transform(final @NotNull Transformer<T> transformer)
    {
        return transformer.apply(this.x(), this.y(), this.width(), this.height());
    }
    
    /// Applies this rectangle's components to the given transformer and returns the result of this operation.
    ///
    /// This is similar to [#transform(Transformer)], but applies this rectangle's size components first as opposed to the
    /// position components. This is majorly a compatibility function for cases when
    /// [ClickableWidget#setDimensionsAndPosition(int, int, int, int)] is used as there size and position are
    /// reversed.
    /// @param transformer The [Transformer] to apply this rectangle's components to
    /// @return The result of this operation
    /// @param <T> The result type of this operation
    public <T> T transformSf(final @NotNull Transformer<T> transformer)
    {
        return transformer.apply(this.width(), this.height(), this.x(), this.y());
    }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        return "Rectangle{pos=%s, width=%d, height=%d}".formatted(this.pos, this.width, this.height);
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                       return true;
        if (!(obj instanceof Rectangle other)) return false;
        
        return (this.pos.equals(other.pos) && this.width == other.width && this.height == other.height);
    }
    
    @Override public int hashCode() { return Objects.hash(this.pos, this.width, this.height); }
}

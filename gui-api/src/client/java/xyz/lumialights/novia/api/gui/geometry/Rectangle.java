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
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;



//**********************************************************************************************************************
public class Rectangle
{
    //******************************************************************************************************************
    @FunctionalInterface
    public interface Consumer
    {
        //**************************************************************************************************************
        void accept(int x, int y, int width, int height);
    }
    
    @FunctionalInterface
    public interface Function<T>
    {
        //**************************************************************************************************************
        T apply(int x, int y, int width, int height);
    }
    
    /** A stream like rectangle builder helper that can be used to define re-usable rectangle builders. */
    public static class Builder
    {
        //**************************************************************************************************************
        private java.util.function.Function<Rectangle, Rectangle> pipeline = java.util.function.Function.identity();
        
        //**************************************************************************************************************
        public @NotNull Builder with(final @NotNull java.util.function.Function<Rectangle, Rectangle> function)
        {
            Objects.requireNonNull(function, "function must not be null");
            this.pipeline = this.pipeline.andThen(function);
            return this;
        }
        
        public <T> @NotNull Builder with(
            final @NotNull BiFunction<Rectangle, T, Rectangle> function,
            final T arg)
        {
            return this.with(rect -> function.apply(rect, arg));
        }
        
        public <T1, T2> @NotNull Builder with(
            final @NotNull Function3<Rectangle, T1, T2, Rectangle> function,
            final T1 arg1, final T2 arg2)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2));
        }
        
        public <T1, T2, T3> @NotNull Builder with(
            final @NotNull Function4<Rectangle, T1, T2, T3, Rectangle> function,
            final T1 arg1, final T2 arg2, final T3 arg3)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2, arg3));
        }
        
        public <T1, T2, T3, T4> @NotNull Builder with(
            final @NotNull Function5<Rectangle, T1, T2, T3, T4, Rectangle> function,
            final T1 arg1, final T2 arg2, final T3 arg3, final T4 arg4)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2, arg3, arg4));
        }
        
        public <T1, T2, T3, T4, T5> @NotNull Builder with(
            final @NotNull Function6<Rectangle, T1, T2, T3, T4, T5, Rectangle> function,
            final T1 arg1, final T2 arg2, final T3 arg3, final T4 arg4, final T5 arg5)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2, arg3, arg4, arg5));
        }
        
        public <T1, T2, T3, T4, T5, T6> @NotNull Builder with(
            final @NotNull Function7<Rectangle, T1, T2, T3, T4, T5, T6, Rectangle> function,
            final T1 arg1, final T2 arg2, final T3 arg3, final T4 arg4, final T5 arg5, final T6 arg6)
        {
            return this.with(rect -> function.apply(rect, arg1, arg2, arg3, arg4, arg5, arg6));
        }
        
        //==============================================================================================================
        public @NotNull Positioner toPositioner()
        {
            return ((x, y, w, h) -> this.pipeline.apply(new Rectangle(x, y, w, h)));
        }
        
        //==============================================================================================================
        public @NotNull Rectangle build(final @NotNull Rectangle initialRect)
        {
            return this.pipeline.apply(new Rectangle(initialRect));
        }
        
        public @NotNull Rectangle build(final @NotNull ScreenRect screenRect)
        {
            return this.build(new Rectangle(screenRect));
        }
        
        public @NotNull Rectangle build(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                        final @NotNull Number height)
        {
            return this.build(new Rectangle(x, y, width, height));
        }
    }
    
    //******************************************************************************************************************
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
    /**
     * Constructs a new {@link Rectangle}.
     * @param pos    The position of the rectangle
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     */
    public Rectangle(final @NotNull Point pos, final @NotNull Number width, final @NotNull Number height)
    {
        this.pos    = Objects.requireNonNull(pos, "pos must not be null");
        this.width  = width .intValue();
        this.height = height.intValue();
    }
    
    /**
     * Constructs a new {@link Rectangle}.
     * @param x      The position of the rectangle on the x-axis
     * @param y      The position of the rectangle on the y-axis
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     */
    public Rectangle(final @NotNull Number x,
                     final @NotNull Number y,
                     final @NotNull Number width,
                     final @NotNull Number height)
    {
        this(new Point(x, y), width, height);
    }
    
    /** Constructs a new empty {@link Rectangle} with position and size zero. */
    public Rectangle() { this(0, 0, 0, 0); }
    
    /**
     * Constructs a new {@link Rectangle} with the given size and position zero.
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     */
    public Rectangle(final @NotNull Number width, final @NotNull Number height) { this(0, 0, width, height); }
    
    /**
     * Constructs a new {@link Rectangle} comprised of the two given points.
     * @param point1 The first point
     * @param point2 The second point
     */
    public Rectangle(final @NotNull Point point1, final @NotNull Point point2)
    {
        this(
            Math.min(
                Objects.requireNonNull(point1, "point 1 must not be null").x(),
                Objects.requireNonNull(point2, "point 2 must not be null").x()),
            Math.min(point1.y(), point2.y()),
            Math.abs(point1.x() - point2.x()),
            Math.abs(point1.y() - point2.y()));
    }
    
    /**
     * Constructs a new {@link Rectangle} from a copy of the given other {@link Rectangle}.
     * @param other The other {@link Rectangle} to copy from
     */
    public Rectangle(final @NotNull Rectangle other) { this(other.x(), other.y(), other.width(), other.height()); }
    
    /**
     * Constructs a new {@link Rectangle} from the given {@link ScreenRect}.
     * @param screenRect The {@link ScreenRect} to copy from
     */
    public Rectangle(final @NotNull ScreenRect screenRect)
    {
        this(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    //==================================================================================================================
    public int x()      { return this.pos.x(); }
    public int y()      { return this.pos.y(); }
    public int width()  { return this.width; }
    public int height() { return this.height; }
    
    //==================================================================================================================
    /**
     * Gets the coordinate on the x-axis on the right border of this rectangle.
     * @return The right-hand x coordinate
     */
    public int getRight() { return (this.x() + this.width()); }
    
    /**
     * Gets the coordinate on the y-axis on the bottom border of this rectangle.
     * @return The bottom y coordinate
     */
    public int getBottom() { return (this.y() + this.height()); }
    
    /**
     * Gets the “exact” centre coordinate of this rectangle on the x-axis, without truncation.
     * @return The centre x coordinate
     */
    public double getExactCentreX() { return (this.x() + (this.width() / 2.0)); }
    
    /**
     * Gets the “exact” centre coordinate of this rectangle on the y-axis, without truncation.
     * @return The centre y coordinate
     */
    public double getExactCentreY() { return (this.y() + (this.height() / 2.0)); }
    
    /**
     * Gets the truncated centre coordinate of this rectangle on the x-axis.
     * @return The centre x coordinate
     */
    public int getCentreX() { return (int) Math.round(this.getExactCentreX()); }
    
    /**
     * Gets the truncated centre coordinate of this rectangle on the y-axis.
     * @return The centre y coordinate
     */
    public int getCentreY() { return (int) Math.round(this.getExactCentreY()); }
    
    /**
     * Gets the position of this rectangle as a {@link Point} object.
     * @return The {@link Point}
     */
    public @NotNull Point getPosition() { return this.pos; }
    
    /**
     * Gets the top-left corner point of this rectangle. This is equivalent to {@link #getPosition()}.
     * @return The top-left corner {@link Point}
     */
    public @NotNull Point getTopLeftCorner() { return this.getPosition(); }
    
    /**
     * Gets the top-right corner point of this rectangle.
     * @return The top-right corner {@link Point}
     */
    public @NotNull Point getTopRightCorner() { return new Point(this.getRight(), this.y()); }
    
    /**
     * Gets the bottom-left corner point of this rectangle.
     * @return The bottom-left corner {@link Point}
     */
    public @NotNull Point getBottomLeftCorner() { return new Point(this.x(), this.getBottom()); }
    
    /**
     * Gets the bottom-right corner point of this rectangle.
     * @return The bottom-right corner {@link Point}
     */
    public @NotNull Point getBottomRightCorner() { return new Point(this.getRight(), this.getBottom()); }
    
    /**
     * Gets the centre point of this rectangle as a {@link Point} object.
     * @return The centre {@link Point}
     */
    public @NotNull Point getCentre() { return new Point(this.getCentreX(), this.getCentreY()); }
    
    /**
     * Gets the coordinate on the rectangle border from the given navigation direction.
     * @param direction The {@link NavigationDirection} to get the coordinate for
     * @return The coordinate
     */
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
    
    //==================================================================================================================
    /**
     * Gets whether this rectangle is empty. A rectangle is considered empty if its width or height is equal to zero.
     * @return {@code true} if this rectangle is empty
     */
    public boolean isEmpty() { return (this.width() < 1 || this.height() < 1); }
    
    /**
     * Gets whether the given coordinates lie within this rectangle.
     * @param x The x coordinate
     * @param y The y coordinate
     * @return {@code true} if the given coordinates are inside this rectangle
     */
    public boolean contains(final @NotNull Number x, final @NotNull Number y)
    {
        return (
            x.intValue() >= this.x()
            && x.intValue() < getRight()
            && y.intValue() >= this.y()
            && y.intValue() < getBottom()
        );
    }
    
    /**
     * Gets whether the given point lies within this rectangle.
     * @param point The {@link Point} to check
     * @return {@code true} if the given coordinates are inside this rectangle
     */
    public boolean contains(final @NotNull Point point)
    {
        Objects.requireNonNull(point, "point must not be null");
        return contains(point.x(), point.y());
    }
    
    /**
     * Gets whether the given rectangular region is fully contained within this rectangle.
     * @param x      The x coordinate of the region
     * @param y      The y coordinate of the region
     * @param width  The width of the region
     * @param height The height of the region
     * @return {@code true} if the given region is fully contained
     */
    public boolean contains(final int x, final int y, final int width, final int height)
    {
        return (this.x() <= x && this.y() <= y && this.getRight() >= (x + width) && this.getBottom() >= (y + height));
    }
    
    /**
     * Gets whether the given {@link Rectangle} is fully contained within this rectangle.
     * @param rect The {@link Rectangle} to check
     * @return {@code true} if the given {@link Rectangle} is fully contained
     */
    public boolean contains(final @NotNull Rectangle rect)
    {
        Objects.requireNonNull(rect, "rectangle must not be null");
        return rect.apply(this::contains);
    }
    
    /**
     * Gets whether the given {@link ScreenRect} is fully contained within this rectangle.
     * @param screenRect The {@link ScreenRect} to check
     * @return {@code true} if the given {@link ScreenRect} is fully contained
     */
    public boolean contains(final @NotNull ScreenRect screenRect)
    {
        Objects.requireNonNull(screenRect, "screen rect must not be null");
        return this.contains(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    /**
     * Gets whether this rectangle intersects with the given region.
     * @param x      The x coordinate of the region
     * @param y      The y coordinate of the region
     * @param width  The width of the region
     * @param height The height of the region
     * @return {@code true} if the given region intersects with this rectangle
     */
    public boolean intersects(final int x, final int y, final int width, final int height)
    {
        return (
            this.x() < (x + width)
            && x < this.getRight()
            && this.y() < (y + height)
            && y < this.getBottom()
        );
    }
    
    /**
     * Gets whether this rectangle intersects with the given {@link Rectangle}.
     * @param rect The {@link Rectangle} to check
     * @return {@code true} if the given {@link Rectangle} intersects with this rectangle
     */
    public boolean intersects(final @NotNull Rectangle rect)
    {
        Objects.requireNonNull(rect, "rectangle must not be null");
        return rect.apply(this::intersects);
    }
    
    /**
     * Gets whether this rectangle intersects with the given {@link ScreenRect}.
     * @param screenRect The {@link ScreenRect} to check
     * @return {@code true} if the given {@link ScreenRect} intersects with this rectangle
     */
    public boolean intersects(final @NotNull ScreenRect screenRect)
    {
        Objects.requireNonNull(screenRect, "screen rect must not be null");
        return this.intersects(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    //==================================================================================================================
    /**
     * Converts this {@link Rectangle} into a {@link ScreenRect}.
     * @return A new {@link ScreenRect} with the same properties as this {@link Rectangle}
     */
    public @NotNull ScreenRect toScreenRect() { return this.apply(ScreenRect::new); }
    
    public int toRelativeX(final int absoluteX) { return (absoluteX - this.x()); }
    
    public double toRelativeX(final double absoluteX) { return (absoluteX - this.x()); }
    
    public int toRelativeX(final @NotNull Point absolutePoint)
    {
        Objects.requireNonNull(absolutePoint, "point must not be null");
        return this.toRelativeX(absolutePoint.x());
    }
    
    public int toRelativeY(final int absoluteY) { return (absoluteY - this.y()); }
    
    public double toRelativeY(final double absoluteY) { return (absoluteY - this.y()); }
    
    public int toRelativeY(final @NotNull Point absolutePoint)
    {
        Objects.requireNonNull(absolutePoint, "point must not be null");
        return this.toRelativeY(absolutePoint.y());
    }
    
    public @NotNull Point toRelativePoint(final int absoluteX, final int absoluteY)
    {
        return this.getPosition().toRelativePoint(absoluteX, absoluteY);
    }
    
    public @NotNull Point toRelativePoint(final double absoluteX, final double absoluteY)
    {
        return this.getPosition().toRelativePoint(absoluteX, absoluteY);
    }
    
    public @NotNull Point toRelativePoint(final @NotNull Point absolutePoint)
    {
        return this.getPosition().toRelativePoint(absolutePoint);
    }
    
    public int toScreenX(final int relativeX) { return (relativeX + this.x()); }
    
    public double toScreenX(final double relativeX) { return (relativeX + this.x()); }
    
    public int toScreenX(final @NotNull Point relativePoint)
    {
        Objects.requireNonNull(relativePoint, "point must not be null");
        return this.toScreenX(relativePoint.x());
    }
    
    public int toScreenY(final int relativeY) { return (relativeY + this.y()); }
    
    public double toScreenY(final double relativeY) { return (relativeY + this.y()); }
    
    public int toScreenY(final @NotNull Point relativePoint)
    {
        Objects.requireNonNull(relativePoint, "point must not be null");
        return this.toScreenY(relativePoint.y());
    }
    
    public @NotNull Point toScreenPoint(final int relativeX, final int relativeY)
    {
        return new Point((relativeX + this.x()), (relativeY + this.y()));
    }
    
    public @NotNull Point toScreenPoint(final double relativeX, final double relativeY)
    {
        return new Point((relativeX + this.x()), (relativeY + this.y()));
    }
    
    public @NotNull Point toScreenPoint(final @NotNull Point relativePoint)
    {
        return relativePoint.apply(this::toScreenPoint);
    }
    
    //==================================================================================================================
    /**
     * Sets the x position of this rectangle.
     * @param x The new x position
     * @return {@code this}
     */
    public @NotNull Rectangle setX(final @NotNull Number x)
    {
        this.pos.setX(x);
        return this;
    }
    
    /**
     * Sets the y position of this rectangle.
     * @param y The new y position
     * @return {@code this}
     */
    public @NotNull Rectangle setY(final @NotNull Number y)
    {
        this.pos.setY(y);
        return this;
    }
    
    /**
     * Sets the width of this rectangle.
     * @param width The new width
     * @return {@code this}
     */
    public @NotNull Rectangle setWidth(final @NotNull Number width)
    {
        this.width = width.intValue();
        return this;
    }
    
    /**
     * Sets the height of this rectangle.
     * @param height The new height
     * @return {@code this}
     */
    public @NotNull Rectangle setHeight(final @NotNull Number height)
    {
        this.height = height.intValue();
        return this;
    }
    
    /**
     * Sets the position of this rectangle.
     * @param x The new x position
     * @param y The new y position
     * @return {@code this}
     */
    public @NotNull Rectangle setPosition(final @NotNull Number x, final @NotNull Number y)
    {
        this.pos.setPoint(x, y);
        return this;
    }
    
    /**
     * Sets the position of this rectangle.
     * @param position The new position
     * @return {@code this}
     */
    public @NotNull Rectangle setPosition(final @NotNull Point position)
    {
        Objects.requireNonNull(position, "position must not be null");
        this.setPosition(position.x(), position.y());
        return this;
    }
    
    /**
     * Sets the position of this rectangle.
     * @param rect The other rect to get the position from
     * @return {@code this}
     */
    public @NotNull Rectangle setPosition(final @NotNull Rectangle rect)
    {
        this.setPosition(rect.x(), rect.y());
        return this;
    }
    
    /**
     * Sets the size of this rectangle.
     * @param width  The new width
     * @param height The new height
     * @return {@code this}
     */
    public @NotNull Rectangle setSize(final @NotNull Number width, final @NotNull Number height)
    {
        this.width  = width.intValue();
        this.height = height.intValue();
        return this;
    }
    
    /**
     * Sets the size of this rectangle.
     * @param rect The other rect to get the size from
     * @return {@code this}
     */
    public @NotNull Rectangle setSize(final @NotNull Rectangle rect)
    {
        this.width  = rect.width();
        this.height = rect.height();
        
        return this;
    }
    
    /**
     * Sets the bounds of this rectangle.
     * @param x      The new x position
     * @param y      The new y position
     * @param width  The new width
     * @param height The new height
     * @return {@code this}
     */
    public @NotNull Rectangle setBounds(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                        final @NotNull Number height)
    {
        this.setPosition(x, y);
        this.setSize(width, height);
        return this;
    }
    
    public @NotNull Rectangle setBounds(final @NotNull Rectangle bounds)
    {
        return Objects
            .requireNonNull(bounds, "bounds must not be null")
            .apply(this::setBounds);
    }
    
    public @NotNull Rectangle setBounds(final @NotNull ScreenRect screenRect)
    {
        return this.setBounds(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    /**
     * Sets the position of this rectangle so that the given x and y coordinates are at the centre of this rectangle.
     * @param x The x coordinate to set the centre of this rectangle to
     * @param y The y coordinate to set the centre of this rectangle to
     * @return {@code this}
     */
    public @NotNull Rectangle setCentre(final @NotNull Number x, final @NotNull Number y)
    {
        this.setPosition(
            Math.round(x.doubleValue() - (this.width  / 2.0)),
            Math.round(y.doubleValue() - (this.height / 2.0)));
        return this;
    }
    
    /**
     * Sets the position of this rectangle so that the given position is at the centre of this rectangle.
     * @param position The position to set the centre of this rectangle to. Must not be null.
     * @return {@code this}
     */
    public @NotNull Rectangle setCentre(final @NotNull Point position)
    {
        Objects.requireNonNull(position, "position must not be null");
        this.setCentre(position.x(), position.y());
        
        return this;
    }
    
    /**
     * Sets the left position of this rectangle and adjusts the width of the rectangle so that the right side
     * of the rectangle stays the same.
     * @param left The new left position of this rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setLeft(final @NotNull Number left)
    {
        final int right = this.getRight();
        
        this.setX(left);
        this.width = Math.max(0, (right - this.x()));
        
        return this;
    }
    
    /**
     * Sets the top position of this rectangle and adjusts the height of the rectangle so that the bottom side
     * of the rectangle stays the same.
     * @param top The new top position of this rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setTop(final @NotNull Number top)
    {
        final int bottom = this.getBottom();
        
        this.setY(top);
        this.height = Math.max(0, (bottom - this.y()));
        
        return this;
    }
    
    /**
     * Sets the right position of this rectangle and adjusts the width of the rectangle so that the left side
     * of the rectangle stays the same.
     * @param right The new right position of this rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setRight(final @NotNull Number right)
    {
        this.width = Math.max(0, (right.intValue() - this.x()));
        return this;
    }
    
    /**
     * Sets the bottom position of this rectangle and adjusts the height of the rectangle so that the top side
     * of the rectangle stays the same.
     * @param bottom The new bottom position of this rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setBottom(final @NotNull Number bottom)
    {
        this.height = Math.max(0, (bottom.intValue() - this.y()));
        return this;
    }
    
    /**
     * Sets the x position of this rectangle so that the right edge is aligned to the left edge of the neighbouring
     * rectangle.
     *
     * @param neighbour The neighbouring rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setLeftOf(final @NotNull Rectangle neighbour)
    {
        this.setX(neighbour.x() - this.width);
        return this;
    }
    
    /**
     * Sets the y position of this rectangle so that the bottom edge is aligned to the top edge of the neighbouring
     * rectangle.
     *
     * @param neighbour The neighbouring rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setTopOf(final @NotNull Rectangle neighbour)
    {
        this.setY(neighbour.y() - this.height);
        return this;
    }
    
    /**
     * Sets the x position of this rectangle so that the left edge is aligned to the right edge of the neighbouring
     * rectangle.
     *
     * @param neighbour The neighbouring rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setRightOf(final @NotNull Rectangle neighbour)
    {
        this.setX(neighbour.getRight());
        return this;
    }
    
    /**
     * Sets the y position of this rectangle so that the top edge is aligned to the bottom edge of the neighbouring
     * rectangle.
     *
     * @param neighbour The neighbouring rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle setBottomOf(final @NotNull Rectangle neighbour)
    {
        this.setY(neighbour.getBottom());
        return this;
    }

    /**
     * Resets the position and size of this rectangle to an empty rectangle.
     * @return {@code this}
     */
    public @NotNull Rectangle reset()
    {
        this.setPosition(0, 0);
        this.setSize(0, 0);
        return this;
    }

    /**
     * Resets the position of this rectangle to [0, 0].
     * @return {@code this}
     */
    public @NotNull Rectangle resetPos()
    {
        this.setPosition(0, 0);
        return this;
    }
    
    public @NotNull Rectangle constrainToMax(final @NotNull Number maxWidth, final @NotNull Number maxHeight)
    {
        this.width  = Math.min(this.width,  Math.max(0, maxWidth .intValue()));
        this.height = Math.min(this.height, Math.max(0, maxHeight.intValue()));
        
        return this;
    }
    
    public @NotNull Rectangle constrainToMin(final @NotNull Number minWidth, final @NotNull Number minHeight)
    {
        this.width  = Math.max(this.width,  Math.max(0, minWidth .intValue()));
        this.height = Math.max(this.height, Math.max(0, minHeight.intValue()));
        
        return this;
    }
    
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
    /**
     * Translates this rectangle by the given amount.
     * @param xOffset The offset on the x-axis
     * @param yOffset The offset on the y-axis
     * @return {@code this}
     */
    public @NotNull Rectangle translate(final @NotNull Number xOffset, final @NotNull Number yOffset)
    {
        this.pos.translate(xOffset, yOffset);
        return this;
    }
    
    /**
     * Translates this rectangle's x-axis by the given offset.
     * @param offset The offset on the x-axis
     * @return {@code this}
     */
    public @NotNull Rectangle translateX(final @NotNull Number offset)
    {
        this.pos.translateX(offset);
        return this;
    }
    
    /**
     * Translates this rectangle's y-axis by the given offset.
     * @param offset The offset on the y-axis
     * @return {@code this}
     */
    public @NotNull Rectangle translateY(final @NotNull Number offset)
    {
        this.pos.translateY(offset);
        return this;
    }
    
    /**
     * Pads this rectangle by the given amount for the given side. If the amount is positive, the rectangle will shrink,
     * and if it is negative, the rectangle will grow on that side.
     *
     * @param left   The amount to pad on the left side
     * @param top    The amount to pad on the top side
     * @param right  The amount to pad on the right side
     * @param bottom The amount to pad on the bottom side
     * @return {@code this}
     */
    public @NotNull Rectangle pad(final @NotNull Number left, final @NotNull Number top, final @NotNull Number right,
                                  final @NotNull Number bottom)
    {
        this.translate(left, top);
        this.width  -= Math.min(this.width,  (left.intValue() + right.intValue()));
        this.height -= Math.min(this.height, (top .intValue() + bottom.intValue()));
        
        return this;
    }
    
    /**
     * Pads this rectangle by the given amount for the given side. If the amount is positive, the rectangle will shrink,
     * and if it is negative, the rectangle will grow on that side.
     *
     * @param leftAndRight The amount to pad on the left and right side
     * @param topAndBottom The amount to pad on the top and bottom side
     * @return {@code this}
     */
    public @NotNull Rectangle pad(final @NotNull Number leftAndRight, final @NotNull Number topAndBottom)
    {
        return this.pad(leftAndRight, topAndBottom, leftAndRight, topAndBottom);
    }
    
    /**
     * Pads this rectangle by the given amount for the given side. If the amount is positive, the rectangle will shrink,
     * and if it is negative, the rectangle will grow on that side.
     *
     * @param allSides The amount to pad on all sides of this rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle pad(final @NotNull Number allSides)
    {
        return this.pad(allSides, allSides, allSides, allSides);
    }
    
    /**
     * Pads this rectangle by the given frame. If the frame's coefficients are positive, the rectangle will shrink,
     * and if they are negative, the rectangle will grow on that side.
     *
     * @param frame The frame to pad this rectangle by
     * @return {@code this}
     */
    public @NotNull Rectangle pad(final @NotNull Frame frame)
    {
        return Objects
            .requireNonNull(frame, "frame must not be null")
            .apply(this::pad);
    }
    
    /**
     * Pads this rectangle on the left side by the given amount. If the amount is positive, the rectangle will shrink,
     * and if it is negative, the rectangle will grow on the left.
     *
     * @param amount The amount to pad on the left side
     * @return {@code this}
     */
    public @NotNull Rectangle padLeft(final @NotNull Number amount) { return this.pad(amount, 0, 0, 0); }
    
    /**
     * Pads this rectangle on the top side by the given amount. If the amount is positive, the rectangle will shrink,
     * and if it is negative, the rectangle will grow on the top.
     *
     * @param amount The amount to pad on the top side
     * @return {@code this}
     */
    public @NotNull Rectangle padTop(final @NotNull Number amount) { return this.pad(0, amount, 0, 0); }
    
    /**
     * Pads this rectangle on the right side by the given amount. If the amount is positive, the rectangle will shrink,
     * and if it is negative, the rectangle will grow on the right.
     *
     * @param amount The amount to pad on the right side
     * @return {@code this}
     */
    public @NotNull Rectangle padRight(final @NotNull Number amount) { return this.pad(0, 0, amount, 0); }
    
    /**
     * Pads this rectangle on the bottom side by the given amount. If the amount is positive, the rectangle will shrink,
     * and if it is negative, the rectangle will grow on the bottom.
     *
     * @param amount The amount to pad on the bottom side
     * @return {@code this}
     */
    public @NotNull Rectangle padBottom(final @NotNull Number amount) { return this.pad(0, 0, 0, amount); }
    
    //==================================================================================================================
    /**
     * Removes the given amount from the left side of this rectangle and returns a new rectangle with the removed
     * portion.
     * @param amount The amount to remove from the left side of this rectangle
     * @return A new rectangle with the removed portion
     */
    public @NotNull Rectangle removeLeft(@NotNull Number amount)
    {
        amount = Math.min(this.width, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setWidth(amount);
        this.padLeft(amount);
        
        return result;
    }
    
    /**
     * Removes the given amount from the top side of this rectangle and returns a new rectangle with the removed
     * portion.
     * @param amount The amount to remove from the top side of this rectangle
     * @return A new rectangle with the removed portion
     */
    public @NotNull Rectangle removeTop(@NotNull Number amount)
    {
        amount = Math.min(this.height, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setHeight(amount);
        this.padTop(amount);
        
        return result;
    }
    
    /**
     * Removes the given amount from the right side of this rectangle and returns a new rectangle with the removed
     * portion.
     * @param amount The amount to remove from the right side of this rectangle
     * @return A new rectangle with the removed portion
     */
    public @NotNull Rectangle removeRight(@NotNull Number amount)
    {
        amount = Math.min(this.width, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setWidth(amount);
        this.padRight(amount);
        
        return result.setX(this.getRight());
    }
    
    /**
     * Removes the given amount from the bottom side of this rectangle and returns a new rectangle with the removed
     * portion.
     * @param amount The amount to remove from the bottom side of this rectangle
     * @return A new rectangle with the removed portion
     */
    public @NotNull Rectangle removeBottom(@NotNull Number amount)
    {
        amount = Math.min(this.height, amount.intValue());
        
        final Rectangle result = (new Rectangle(this)).setHeight(amount);
        this.padBottom(amount);
        
        return result.setY(this.getBottom());
    }
    
    /**
     * Combines this rectangle with the given other rectangle. The resulting rectangle will be the smallest rectangle
     * that contains both of the original rectangles.
     *
     * @param x      The other rectangle's coordinate on the x-axis
     * @param y      The other rectangle's coordinate on the y-axis
     * @param width  The other rectangle's width
     * @param height The other rectangle's height
     * @return {@code this}
     */
    public @NotNull Rectangle combine(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                      final @NotNull Number height)
    {
        final int min_x = Math.min(this.x(), x.intValue());
        final int min_y = Math.min(this.y(), y.intValue());
        final int max_x = Math.max(this.getRight(),  (x.intValue() + width .intValue()));
        final int max_y = Math.max(this.getBottom(), (y.intValue() + height.intValue()));

        return this.setBounds(min_x, min_y, (max_x - min_x), (max_y - min_y));
    }
    
    /**
     * Combines this rectangle with the given other rectangle. The resulting rectangle will be the smallest rectangle
     * that contains both of the original rectangles.
     *
     * @param other The other rectangle to combine this rectangle with
     * @return {@code this}
     */
    public @NotNull Rectangle combine(final @NotNull Rectangle other)
    {
        Objects.requireNonNull(other, "other rectangle must not be null");
        return other.apply(this::combine);
    }
    
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
    
    public @NotNull Rectangle intersect(final @NotNull Rectangle other)
    {
        Objects.requireNonNull(other, "other rectangle must not be null");
        return other.apply(this::intersect);
    }
    
    public @NotNull Rectangle intersect(final @NotNull ScreenRect other)
    {
        Objects.requireNonNull(other, "other rectangle must not be null");
        return this.intersect(other.getLeft(), other.getTop(), other.getRight(), other.getBottom());
    }
    
    //==================================================================================================================
    /**
     * Aligns this rectangle to the given parent container rectangle.
     * @param alignment The alignment to align this rectangle to
     * @param container The parent container rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle align(final @NotNull Alignment alignment, final @NotNull Rectangle container)
    {
        Objects.requireNonNull(alignment, "alignment must not be null");
        Objects.requireNonNull(container, "container must not be null");
        
        this.setBounds(alignment.align(container, this));
        
        return this;
    }
    
    /**
     * Aligns this rectangle to the given parent container rectangle.
     *
     * @param alignment       The alignment to align this rectangle to
     * @param containerX      The position of the area that the target area should be aligned to on the x-axis
     * @param containerY      The position of the area that the target area should be aligned to on the y-axis
     * @param containerWidth  The width of the area that the target area should be aligned to
     * @param containerHeight The height of the area that the target area should be aligned to
     * @return {@code this}
     */
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
    
    /**
     * Aligns this rectangle to be at the centre of the given parent container rectangle.
     * @param container The parent container rectangle
     * @return {@code this}
     */
    public @NotNull Rectangle centre(final @NotNull Rectangle container)
    {
        this.align(Alignment.MIDDLE_CENTRE, container);
        return this;
    }
    
    /**
     * Aligns this rectangle to be at the centre of the given parent container rectangle.
     *
     * @param containerX      The position of the area that the target area should be aligned to on the x-axis
     * @param containerY      The position of the area that the target area should be aligned to on the y-axis
     * @param containerWidth  The width of the area that the target area should be aligned to
     * @param containerHeight The height of the area that the target area should be aligned to
     * @return {@code this}
     */
    public @NotNull Rectangle centre(final @NotNull Number containerX,
                                     final @NotNull Number containerY,
                                     final @NotNull Number containerWidth,
                                     final @NotNull Number containerHeight)
    {
        this.align(Alignment.MIDDLE_CENTRE, containerX, containerY, containerWidth, containerHeight);
        return this;
    }
    
    public @NotNull Rectangle clip(final @NotNull Rectangle container)
    {
        Objects.requireNonNull(container, "container must not be null");
        
        final int p_right  = container.getRight();
        final int p_bottom = container.getBottom();
        
        final int new_x      = Math.clamp(this.x(), container.x(), p_right);
        final int new_y      = Math.clamp(this.y(), container.y(), p_bottom);
        final int new_right  = (new_x + this.width);
        final int new_bottom = (new_y + this.height);
        
        final int new_width  = (p_right  >= new_right  ? this.width  : (this.width  - (new_right  - p_right)));
        final int new_height = (p_bottom >= new_bottom ? this.height : (this.height - (new_bottom - p_bottom)));
        
        this.setPosition(new_x, new_y);
        this.width  = new_width;
        this.height = new_height;
        
        return this;
    }
    
    //==================================================================================================================
    /**
     * Returns a new rectangle with the given x coordinate.
     * @param x The new x coordinate
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withX(final @NotNull Number x)
    {
        return new Rectangle(x, this.y(), this.width, this.height);
    }
    
    /**
     * Returns a new rectangle with the given y coordinate.
     * @param y The new y coordinate
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withY(final @NotNull Number y)
    {
        return new Rectangle(this.x(), y, this.width, this.height);
    }
    
    /**
     * Returns a new rectangle with the given width.
     * @param width The new width
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withWidth(final @NotNull Number width)
    {
        return new Rectangle(this.x(), this.y(), width, this.height);
    }
    
    /**
     * Returns a new rectangle with the given height.
     * @param height The new height
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withHeight(final @NotNull Number height)
    {
        return new Rectangle(this.x(), this.y(), this.width, height);
    }
    
    /**
     * Returns a new rectangle with the given position on the left side of the rectangle and adjusted width so the
     * right side stays the same.
     *
     * @param left The new left position of the rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withLeft(final @NotNull Number left)
    {
        return new Rectangle(left, this.y(), (this.width + (this.x() - left.intValue())), this.height);
    }
    
    /**
     * Returns a new rectangle with the given position on the top side of the rectangle and adjusted height so the
     * bottom side stays the same.
     *
     * @param top The new top position of the rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withTop(final @NotNull Number top)
    {
        return new Rectangle(this.x(), top, this.width, (this.height + (this.y() - top.intValue())));
    }
    
    /**
     * Returns a new rectangle with the given position on the right side of the rectangle and adjusted width so the
     * right side stays the same.
     *
     * @param right The new right position of the rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withRight(final @NotNull Number right)
    {
        return new Rectangle(this.x(), this.y(), (this.width - (this.getRight() - right.intValue())), this.height);
    }
    
    /**
     * Returns a new rectangle with the given position on the bottom side of the rectangle and adjusted height so the
     * bottom side stays the same.
     *
     * @param bottom The new bottom position of the rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withBottom(final @NotNull Number bottom)
    {
        return new Rectangle(this.x(), this.y(), this.width, (this.height - (this.getBottom() - bottom.intValue())));
    }
    
    /**
     * Returns a new rectangle with the new position.
     * @param x The new x position
     * @param y The new y position
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withPosition(final @NotNull Number x, final @NotNull Number y)
    {
        return new Rectangle(x, y, this.width, this.height);
    }
    
    /**
     * Returns a new rectangle with the new position.
     * @param position The new position
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withPosition(final @NotNull Point position)
    {
        Objects.requireNonNull(position, "position must not be null");
        return this.withPosition(position.x(), position.y());
    }
    
    /**
     * Returns a new rectangle with the new size.
     * @param width  The new width
     * @param height The new height
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withSize(final @NotNull Number width, final @NotNull Number height)
    {
        return new Rectangle(this.x(), this.y(), width, height);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given minimum size applied so that it will at least have
     * the given minimum size.
     *
     * @param minWidth  The minimum width of the new rectangle
     * @param minHeight The minimum height of the new rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withMinimumSize(final @NotNull Number minWidth, final @NotNull Number minHeight)
    {
        return new Rectangle(this).constrainToMin(minWidth, minHeight);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given maximum size applied so that it will at most have
     * the given maximum size.
     *
     * @param maxWidth  The maximum width of the new rectangle
     * @param maxHeight The maximum height of the new rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withMaximumSize(final int maxWidth, final int maxHeight)
    {
        return new Rectangle(this).constrainToMax(maxWidth, maxHeight);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given minimum and maximum size applied so that it will
     * at least have the given minimum size and at most have the given maximum size.
     *
     * @param minWidth  The minimum width of the new rectangle
     * @param minHeight The minimum height of the new rectangle
     * @param maxWidth  The maximum width of the new rectangle
     * @param maxHeight The maximum height of the new rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withMinMaxSize(final @NotNull Number minWidth, final @NotNull Number minHeight,
                                             final @NotNull Number maxWidth, final @NotNull Number maxHeight)
    {
        return new Rectangle(this).constrainToMinMax(minWidth, minHeight, maxWidth, maxHeight);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given padding applied to all sides.
     * @param padding The padding to apply
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withPadding(final @NotNull Number padding) { return new Rectangle(this).pad(padding); }
    
    /**
     * Returns this rectangle as a new rectangle with the given padding applied to all sides.
     * @param leftAndRight The padding to apply on the left and right side
     * @param topAndBottom The padding to apply on the top and bottom side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withPadding(final @NotNull Number leftAndRight, final @NotNull Number topAndBottom)
    {
        return new Rectangle(this).pad(leftAndRight, topAndBottom);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given padding applied to all sides.
     * @param left   The padding to apply on the left side
     * @param top    The padding to apply on the top side
     * @param right  The padding to apply on the right side
     * @param bottom The padding to apply on the bottom side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withPadding(final @NotNull Number left, final @NotNull Number top,
                                          final @NotNull Number right, final @NotNull Number bottom)
    {
        return new Rectangle(this).pad(left, top, right, bottom);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given padding applied to all sides.
     * @param frame The {@link Frame} containing the padding
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withPadding(final @NotNull Frame frame) { return new Rectangle(this).pad(frame); }
    
    /**
     * Returns this rectangle as a new rectangle with the given left side padded the given amount.
     * @param amount The amount to pad on the left side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withLeftPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padLeft(amount);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given top side padded the given amount.
     * @param amount The amount to pad on the top side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withTopPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padTop(amount);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given right side padded the given amount.
     * @param amount The amount to pad on the right side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withRightPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padRight(amount);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given bottom side padded the given amount.
     * @param amount The amount to pad on the bottom side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withBottomPadding(final @NotNull Number amount)
    {
        return new Rectangle(this).padBottom(amount);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the translation applied to the position of the new rectangle.
     * @param xOffset The x translation to apply
     * @param yOffset The y translation to apply
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withTranslation(final @NotNull Number xOffset, final @NotNull Number yOffset)
    {
        return new Rectangle((this.x() + xOffset.intValue()), (this.y() + yOffset.intValue()), this.width, this.height);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the translation applied to the x-axis of the new rectangle.
     * @param offset The x translation to apply
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withTranslationX(final @NotNull Number offset)
    {
        return new Rectangle((this.x() + offset.intValue()), this.y(), this.width, this.height);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the translation applied to the y-axis of the new rectangle.
     * @param offset The y translation to apply
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withTranslationY(final @NotNull Number offset)
    {
        return new Rectangle(this.x(), (this.y() + offset.intValue()), this.width, this.height);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given alignment applied inside the given parent container
     * rectangle.
     *
     * @param alignment The {@link Alignment} to apply
     * @param container The parent rectangle to position this rectangle in
     * @return The new aligned {@link Rectangle}
     */
    public @NotNull Rectangle withAlignment(final @NotNull Alignment alignment, final @NotNull Rectangle container)
    {
        return new Rectangle(this).align(alignment, container);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the position being centred inside the given parent container
     * rectangle.
     *
     * @param container The parent rectangle to position this rectangle in
     * @return The new centred {@link Rectangle}
     */
    public @NotNull Rectangle withCentering(final @NotNull Rectangle container)
    {
        return new Rectangle(this).centre(container);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the given container rectangle acting as the maximum bounds the
     * new rectangle can be positioned in.
     *
     * @param container The container rectangle to position this rectangle in
     * @return The new clipped {@link Rectangle}
     */
    public @NotNull Rectangle withClipped(final @NotNull Rectangle container)
    {
        return new Rectangle(this).clip(container);
    }
    
    /**
     * Returns this rectangle as a new rectangle with the position set to zero.
     * @return The new localised {@link Rectangle}
     */
    public @NotNull Rectangle withLocalPos() { return new Rectangle(0, 0, this.width, this.height); }
    
    /**
     * Returns this rectangle as a new rectangle with only the portion that was cut from the left side.
     * @param amount The amount to cut on the left side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withLeftCut(final @NotNull Number amount)
    {
        return new Rectangle(this).removeLeft(amount);
    }
    
    /**
     * Returns this rectangle as a new rectangle with only the portion that was cut from the right side.
     * @param amount The amount to cut on the right side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withRightCut(final @NotNull Number amount)
    {
        return new Rectangle(this).removeRight(amount);
    }
    
    /**
     * Returns this rectangle as a new rectangle with only the portion that was cut from the top side.
     * @param amount The amount to cut on the top side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withTopCut(final @NotNull Number amount) { return new Rectangle(this).removeTop(amount); }
    
    /**
     * Returns this rectangle as a new rectangle with only the portion that was cut from the bottom side.
     * @param amount The amount to cut on the bottom side
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withBottomCut(final @NotNull Number amount)
    {
        return new Rectangle(this).removeBottom(amount);
    }
    
    /**
     * Returns this rectangle positioned so that the right side of the new rectangle is aligned to the left side of
     * the neighbouring rectangle.
     *
     * @param neighbour The neighbouring rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withLeftOf(final @NotNull Rectangle neighbour)
    {
        return this.withX(neighbour.x() - this.width);
    }
    
    /**
     * Returns this rectangle positioned so that the left side of the new rectangle is aligned to the right side of
     * the neighbouring rectangle.
     *
     * @param neighbour The neighbouring rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withRightOf(final @NotNull Rectangle neighbour)
    {
        return this.withX(neighbour.getRight());
    }
    
    /**
     * Returns this rectangle positioned so that the top side of the new rectangle is aligned to the bottom side of
     * the neighbouring rectangle.
     * @param neighbour The neighbouring rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withTopOf(final @NotNull Rectangle neighbour)
    {
        return this.withY(neighbour.y() - this.height);
    }
    
    /**
     * Returns this rectangle positioned so that the bottom side of the new rectangle is aligned to the top side of
     * the neighbouring rectangle.
     * @param neighbour The neighbouring rectangle
     * @return The new {@link Rectangle}
     */
    public @NotNull Rectangle withBottomOf(final @NotNull Rectangle neighbour)
    {
        return this.withY(neighbour.getBottom());
    }
    
    //==================================================================================================================
    /**
     * Applies this rectangle's components to the given consumer.
     * @param consumer The consumer to apply this rectangle's components to
     */
    public void accept(final @NotNull Consumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        consumer.accept(this.x(), this.y(), this.width(), this.height());
    }
    
    /**
     * Applies this rectangle's components to the given consumer and returns the result of this operation.
     * @param function The function to apply this rectangle's components to
     * @return The result of this operation
     * @param <T> The result type of this operation
     */
    public <T> T apply(final @NotNull Function<T> function)
    {
        Objects.requireNonNull(function, "function must not be null");
        return function.apply(this.x(), this.y(), this.width(), this.height());
    }
    
    /**
     * Similar to {@link #apply(Rectangle.Function)}, but applies this rectangle's size components first as opposed to the
     * position components.
     * <p>
     * This is majorly a compatibility function for cases when
     * {@link ClickableWidget#setDimensionsAndPosition(int, int, int, int)} is used as there size and position are
     * reversed.
     *
     * @param consumer The consumer to apply this rectangle's components to
     */
    public void acceptSf(final @NotNull Consumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        consumer.accept(this.width(), this.height(), this.x(), this.y());
    }
    
    /**
     * Similar to {@link #apply(Rectangle.Function)}, but applies this rectangle's size components first as opposed to the
     * position components.
     * <p>
     * This is majorly a compatibility function for cases when
     * {@link ClickableWidget#setDimensionsAndPosition(int, int, int, int)} is used as there size and position are
     * reversed.
     *
     * @param function The function to apply this rectangle's components to
     * @return The result of this operation
     * @param <T> The result type of this operation
     */
    public <T> T applySf(final @NotNull Function<T> function)
    {
        Objects.requireNonNull(function, "function must not be null");
        return function.apply(this.width(), this.height(), this.x(), this.y());
    }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        return String.format("Rectangle{x=%d, y=%d, width=%d, height=%d}", this.x(), this.y(), this.width, this.height);
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

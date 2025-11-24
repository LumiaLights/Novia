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
package xyz.lumialights.novia.api.gui.geometry;

import net.minecraft.client.gui.ScreenRect;
import org.jetbrains.annotations.NotNull;

import java.util.*;



//**********************************************************************************************************************
/// A geometric utility class providing tools for handling positional processes on screens.
///
/// ## Behaviour
/// This class is a mutable container for positional data; modifications with any of the methods in this class that do
/// not begin with the `with` prefix will alter the instance they are executed on.
///
/// This class also provides a set of `with` prefixed methods that do not alter the current point's state, but
/// instead alters the state on a newly created copy of the current instance, which is then returned.
///
/// ## Precision
/// A point is always dealing with integers due to Minecraft's limited geometry. Nevertheless, all methods in the
/// Point class take a [Number] object which can be passed any numeric object that inherits this interface
/// of which all will be internally converted to integers (via [Number#intValue()]). This may lead to loss of precision
/// when dealing with floating point values, as they are truncated to integral values, hence any rounding should be
/// done before passing them on to the Point API.
///
/// ## Consumable
/// Points provide a functional layer, that allows its position to be directly embedded into a different invocation
/// or to be transformed into another object entirely. For this occasion, the API provides [#accept(PositionConsumer)]
/// and [#transform(Transformer)] methods.
///
/// ## Java
/// Comparing instances of this class (see [Object#equals(Object)]) will compare the positional components of this
/// point and not its identity. It also provides a custom [Object#hashCode()] implementation based on these components.
///
/// Converting this class to string (see [Object#toString()]) will be in the following format: `Point{left=,y=}`
public class Point
{
    //******************************************************************************************************************
    /// A functional interface that allows consuming a point's positional components.
    @FunctionalInterface
    public interface PositionConsumer
    {
        //**************************************************************************************************************
        /// Accepts the coordinates.
        /// @param x The left coordinate of the rectangle
        /// @param y The y coordinate of the rectangle
        void apply(int x, int y);
    }
    
    /// A functional interface that allows consuming a point's positional components and transforming them to a
    /// different object.
    @FunctionalInterface
    public interface Transformer<T>
    {
        //**************************************************************************************************************
        /// Applies the coordinates.
        /// @param x The left coordinate of the rectangle
        /// @param y The y coordinate of the rectangle
        /// @return The new object that this point's positional components were transformed to
        T accept(int x, int y);
    }
    
    //******************************************************************************************************************
    private int x;
    private int y;
    
    //******************************************************************************************************************
    /// Constructs a new [Point].
    /// @param x The coordinate of the point on the left-axis
    /// @param y The coordinate of the point on the y-axis
    public Point(final @NotNull Number x, final @NotNull Number y)
    {
        this.x = x.intValue();
        this.y = y.intValue();
    }
    
    /// Constructs a copy from `other`.
    /// @param other The other [Point] to copy from
    public Point(final @NotNull Point other) { this(other.x, other.y); }
    
    /// Constructs a new [Point] with position zero.
    public Point() { this(0, 0); }
    
    //==================================================================================================================
    /// {@return the position of the point on the left-axis}
    public int x() { return this.x; }
    
    /// {@return the position of the point on the y-axis}
    public int y() { return this.y; }
    
    //==================================================================================================================
    /// Converts an absolute left coordinate into a coordinate relative to this point's origin.
    /// @param absoluteX The absolute left coordinate
    /// @return The relative left coordinate
    public int toRelativeX(final int absoluteX) { return (absoluteX - this.x()); }
    
    /// Converts an absolute left coordinate into a coordinate relative to this point's origin.
    /// @param absoluteX The absolute left coordinate
    /// @return The relative left coordinate
    public double toRelativeX(final double absoluteX) { return (absoluteX - this.x()); }
    
    /// Converts an absolute y coordinate into a coordinate relative to this point's origin.
    /// @param absoluteY The absolute y coordinate
    /// @return The relative y coordinate
    public int toRelativeY(final int absoluteY) { return (absoluteY - this.y()); }
    
    /// Converts an absolute y coordinate into a coordinate relative to this point's origin.
    /// @param absoluteY The absolute y coordinate
    /// @return The relative y coordinate
    public double toRelativeY(final double absoluteY) { return (absoluteY - this.y()); }
    
    /// Converts an absolute point into a point relative to this point's origin.
    /// @param absoluteX The absolute left coordinate
    /// @param absoluteY The absolute y coordinate
    /// @return The relative [Point]
    public @NotNull Point toRelativePoint(final int absoluteX, final int absoluteY)
    {
        return new Point(this.toRelativeX(absoluteX), this.toRelativeY(absoluteY));
    }
    
    /// Converts an absolute point into a point relative to this point's origin.
    /// @param absoluteX The absolute left coordinate
    /// @param absoluteY The absolute y coordinate
    /// @return The relative [Point]
    public @NotNull Point toRelativePoint(final double absoluteX, final double absoluteY)
    {
        return new Point(this.toRelativeX(absoluteX), this.toRelativeY(absoluteY));
    }
    
    /// Converts an absolute point into a point relative to this point's origin.
    /// @param absolutePoint The absolute [Point]
    /// @return The relative [Point]
    public @NotNull Point toRelativePoint(final @NotNull Point absolutePoint)
    {
        return this.toRelativePoint(absolutePoint.x(), absolutePoint.y());
    }
    
    //==================================================================================================================
    /// Sets the left position of this point.
    /// @param x The new left position
    /// @return `this`
    public @NotNull Point setX(final @NotNull Number x)
    {
        this.x = x.intValue();
        return this;
    }
    
    /// Sets the left position of this point.
    /// @param y The new left position
    /// @return `this`
    public @NotNull Point setY(final @NotNull Number y)
    {
        this.y = y.intValue();
        return this;
    }
    
    /// Sets the position of this point.
    /// @param x The new left position
    /// @param y The new y position
    /// @return `this`
    public @NotNull Point setPosition(final @NotNull Number x, final @NotNull Number y)
    {
        this.x = x.intValue();
        this.y = y.intValue();
        return this;
    }
    
    /// Forces the point to not leave the bounds of the given area, if any of the positional components lie outside
    /// the bounds, they will be clamped to the area.
    /// @param x      The left coordinate of the constraining area
    /// @param y      The y coordinate of the constraining area
    /// @param width  The width of the constraining area
    /// @param height The height of the constraining area
    /// @return `this`
    public @NotNull Point constrainToArea(final @NotNull Number x,
                                          final @NotNull Number y,
                                          final @NotNull Number width,
                                          final @NotNull Number height)
    {
        this.x = Math.clamp(this.x, x.intValue(), (x.intValue() + width.intValue()));
        this.y = Math.clamp(this.y, y.intValue(), (y.intValue() + height.intValue()));
        return this;
    }
    
    /// Forces the point to not leave the bounds of the given area, if any of the positional components lie outside
    /// the bounds, they will be clamped to the area.
    /// @param area The [Rectangle] defining the constraining area
    /// @return `this`
    public @NotNull Point constrainToArea(final @NotNull Rectangle area)
    {
        return area.transform(this::constrainToArea);
    }
    
    /// Forces the point to not leave the bounds of the given area, if any of the positional components lie outside
    /// the bounds, they will be clamped to the area.
    /// @param area The [ScreenRect] defining the constraining area
    /// @return `this`
    public @NotNull Point constrainToArea(final @NotNull ScreenRect area)
    {
        return this.constrainToArea(area.getLeft(), area.getTop(), area.width(), area.height());
    }
    
    //==================================================================================================================
    /// Translates this point by the given amount.
    /// @param xOffset The offset on the left-axis
    /// @param yOffset The offset on the y-axis
    /// @return `this`
    public @NotNull Point translate(final @NotNull Number xOffset, final @NotNull Number yOffset)
    {
        this.x += xOffset.intValue();
        this.y += yOffset.intValue();
        return this;
    }
    
    /// Translates this point's left-axis by the given offset.
    /// @param offset The offset on the left-axis
    /// @return `this`
    public @NotNull Point translateX(final @NotNull Number offset)
    {
        this.x += offset.intValue();
        return this;
    }
    
    /// Translates this point's y-axis by the given offset.
    /// @param offset The offset on the y-axis
    /// @return `this`
    public @NotNull Point translateY(final @NotNull Number offset)
    {
        this.y += offset.intValue();
        return this;
    }
    
    //==================================================================================================================
    /// Returns a new point with the given left coordinate.
    /// @param x The new left coordinate
    /// @return The new [Point]
    public @NotNull Point withX(final @NotNull Number x) { return new Point(x, this.y); }
    
    /// Returns a new point with the given y coordinate.
    /// @param y The new y coordinate
    /// @return The new [Point]
    public @NotNull Point withY(final @NotNull Number y) { return new Point(this.x, y); }
    
    /// Returns a new point with the given translation applied.
    /// @param offsetX The left translation to apply
    /// @param offsetY The y translation to apply
    /// @return The new [Point]
    public @NotNull Point withTranslation(final @NotNull Number offsetX, final @NotNull Number offsetY)
    {
        return new Point((this.x + offsetX.intValue()), (this.y + offsetY.intValue()));
    }
    
    /// Returns a new point with the given translation on the left-axis applied.
    /// @param offset The left translation to apply
    /// @return The new [Point]
    public @NotNull Point withTranslationX(final @NotNull Number offset)
    {
        return new Point((this.x + offset.intValue()), this.y);
    }
    
    /// Returns a new point with the given translation on the y-axis applied.
    /// @param offset The y translation to apply
    /// @return The new [Point]
    public @NotNull Point withTranslationY(final @NotNull Number offset)
    {
        return new Point(this.x, (this.y + offset.intValue()));
    }
    
    /// Returns a new point with the given area constraints, the point will be clamped to the given area.
    /// @param x      The minimum left position it can have
    /// @param y      The minimum y position it can have
    /// @param width  The width of the area the point can't escape
    /// @param height The height of the area the point can't escape
    /// @return The new constrained [Point]
    /// @throws IllegalArgumentException If `width` or `height` is negative
    public @NotNull Point withAreaConstraint(final @NotNull Number x,
                                             final @NotNull Number y,
                                             final @NotNull Number width,
                                             final @NotNull Number height)
    {
        return (new Point(this)).constrainToArea(x, y, width, height);
    }
    
    /// Returns a new point with the given area constraints, the point will be clamped to the given area.
    /// @param area The [Rectangle] the point can't escape
    /// @return The new constrained [Point]
    public @NotNull Point withAreaConstraint(final @NotNull Rectangle area)
    {
        return (new Point(this)).constrainToArea(area);
    }
    
    /// Returns a new point with the given area constraints, the point will be clamped to the given area.
    /// @param area The [ScreenRect] the point can't escape
    /// @return The new constrained [Point]
    public @NotNull Point withAreaConstraint(final @NotNull ScreenRect area)
    {
        return (new Point(this)).constrainToArea(area);
    }
    
    //==================================================================================================================
    /// Accepts this point's positional components with the given consumer.
    /// @param consumer The [PositionConsumer] to accept
    public void accept(final @NotNull PositionConsumer consumer) { consumer.apply(this.x, this.y); }
    
    /// Applies this point's positional components to the given transformer and returns the result of this operation.
    /// @param transformer The [Transformer] to apply this point's positional components to
    /// @return The result of this operation
    /// @param <T> The result type of this operation
    public <T> T transform(final @NotNull Transformer<T> transformer) { return transformer.accept(this.x, this.y); }
    
    //==================================================================================================================
    @Override public @NotNull String toString() { return "Point{x=%d, y=%d}".formatted(this.x, this.y); }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                   return true;
        if (!(obj instanceof Point other)) return false;
        
        return (this.x == other.x && this.y == other.y);
    }
    
    @Override public int hashCode() { return Objects.hash(this.x, this.y); }
}

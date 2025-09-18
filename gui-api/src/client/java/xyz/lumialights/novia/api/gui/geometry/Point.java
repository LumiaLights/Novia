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
public class Point
{
    //******************************************************************************************************************
    @FunctionalInterface
    public interface Consumer
    {
        //**************************************************************************************************************
        void apply(int x, int y);
    }
    
    @FunctionalInterface
    public interface Function<T>
    {
        //**************************************************************************************************************
        T accept(int x, int y);
    }
    
    //******************************************************************************************************************
    private int x;
    private int y;
    
    //******************************************************************************************************************
    public Point(final @NotNull Number x, final @NotNull Number y)
    {
        this.x = x.intValue();
        this.y = y.intValue();
    }
    
    public Point(final @NotNull Point other) { this(other.x, other.y); }
    
    public Point() { this(0, 0); }
    
    //==================================================================================================================
    public int x() { return this.x; }
    public int y() { return this.y; }
    
    //==================================================================================================================
    public @NotNull Point translated(final @NotNull Number offsetX, final @NotNull Number offsetY)
    {
        return new Point((this.x + offsetX.intValue()), (this.y + offsetY.intValue()));
    }
    
    public @NotNull Point translatedX(final @NotNull Number offsetX)
    {
        return new Point((this.x + offsetX.intValue()), this.y);
    }
    
    public @NotNull Point translatedY(final @NotNull Number offsetY)
    {
        return new Point(this.x, (this.y + offsetY.intValue()));
    }
    
    public @NotNull Point restrained(final @NotNull Number x, final @NotNull Number y, final @NotNull Number width,
                                     final @NotNull Number height)
    {
        return new Point(
            Math.clamp(this.x, x.intValue(), (x.intValue() + width.intValue())),
            Math.clamp(this.y, y.intValue(), (y.intValue() + height.intValue())));
    }
    
    public @NotNull Point restrained(final @NotNull Rectangle area) { return area.apply(this::restrained); }
    
    public @NotNull Point restrained(final @NotNull ScreenRect screenRect)
    {
        return this.restrained(screenRect.getLeft(), screenRect.getTop(), screenRect.width(), screenRect.height());
    }
    
    //==================================================================================================================
    public int toRelativeX(final @NotNull Number absoluteX) { return (absoluteX.intValue() - this.x); }
    
    public int toRelativeX(final @NotNull Point absolutePoint)
    {
        Objects.requireNonNull(absolutePoint, "point must not be null");
        return this.toRelativeX(absolutePoint.x);
    }
    
    public int toRelativeY(final @NotNull Number absoluteY) { return (absoluteY.intValue() - this.y); }
    
    public int toRelativeY(final @NotNull Point absolutePoint)
    {
        Objects.requireNonNull(absolutePoint, "point must not be null");
        return this.toRelativeY(absolutePoint.y);
    }
    
    public @NotNull Point toRelativePoint(final @NotNull Number absoluteX, final @NotNull Number absoluteY)
    {
        return new Point((absoluteX.intValue() - this.x), (absoluteY.intValue() - this.y));
    }
    
    public @NotNull Point toRelativePoint(final @NotNull Point absolutePoint)
    {
        return absolutePoint.apply(this::toRelativePoint);
    }
    
    public int toScreenX(final @NotNull Number relativeX) { return (relativeX.intValue() + this.x); }
    
    public int toScreenX(final @NotNull Point relativePoint)
    {
        Objects.requireNonNull(relativePoint, "point must not be null");
        return this.toScreenX(relativePoint.x);
    }
    
    public int toScreenY(final @NotNull Number relativeY) { return (relativeY.intValue() + this.y); }
    
    public int toScreenY(final @NotNull Point relativePoint)
    {
        Objects.requireNonNull(relativePoint, "point must not be null");
        return this.toScreenY(relativePoint.y);
    }
    
    public @NotNull Point toScreenPoint(final @NotNull Number relativeX, final @NotNull Number relativeY)
    {
        return new Point((relativeX.intValue() + this.x), (relativeY.intValue() + this.y));
    }
    
    public @NotNull Point toScreenPoint(final @NotNull Point relativePoint)
    {
        return relativePoint.apply(this::toScreenPoint);
    }
    
    //==================================================================================================================
    public @NotNull Point setX(final @NotNull Number x)
    {
        this.x = x.intValue();
        return this;
    }
    
    public @NotNull Point setY(final @NotNull Number y)
    {
        this.y = y.intValue();
        return this;
    }
    
    public @NotNull Point setPoint(final @NotNull Number x, final @NotNull Number y)
    {
        this.x = x.intValue();
        this.y = y.intValue();
        return this;
    }
    
    //==================================================================================================================
    /**
     * Translates this rectangle by the given amount.
     * @param xOffset The offset on the x-axis
     * @param yOffset The offset on the y-axis
     * @return {@code this}
     */
    public @NotNull Point translate(final @NotNull Number xOffset, final @NotNull Number yOffset)
    {
        this.x += xOffset.intValue();
        this.y += yOffset.intValue();
        return this;
    }
    
    /**
     * Translates this rectangle's x-axis by the given offset.
     * @param offset The offset on the x-axis
     * @return {@code this}
     */
    public @NotNull Point translateX(final @NotNull Number offset)
    {
        this.x += offset.intValue();
        return this;
    }
    
    /**
     * Translates this rectangle's y-axis by the given offset.
     * @param offset The offset on the y-axis
     * @return {@code this}
     */
    public @NotNull Point translateY(final @NotNull Number offset)
    {
        this.y += offset.intValue();
        return this;
    }
    
    //==================================================================================================================
    public @NotNull Point withX(final @NotNull Number newX) { return new Point(newX, this.y); }
    
    public @NotNull Point withY(final @NotNull Number newY) { return new Point(this.x, newY); }
    
    //==================================================================================================================
    public void accept(final @NotNull Point.Consumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        consumer.apply(this.x, this.y);
    }
    
    public <T> T apply(final @NotNull Point.Function<T> function)
    {
        Objects.requireNonNull(function, "function must not be null");
        return function.accept(this.x, this.y);
    }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        return String.format("Point{x=%d, y=%d}", this.x, this.y);
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                   return true;
        if (!(obj instanceof Point other)) return false;
        
        return (this.x == other.x && this.y == other.y);
    }
    
    @Override public int hashCode() { return Objects.hash(this.x, this.y); }
}

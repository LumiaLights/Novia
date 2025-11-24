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

import org.jetbrains.annotations.NotNull;
import java.util.Objects;



//**********************************************************************************************************************
/// A geometric utility class providing tools for handling outlines around other shapes such as margins, padding or
/// borders. Frames are immutable.
///
/// ## Precision
/// A frame is always dealing with integers due to Minecraft's limited geometry. Nevertheless, all methods in the
/// Frame class take a [Number] object which can be passed any numeric object that inherits this interface
/// of which all will be internally converted to integers (via [Number#intValue()]). This may lead to loss of precision
/// when dealing with floating point values, as they are truncated to integral values, hence any rounding should be
/// done before passing them on to the Frame API.
///
/// ## Consumable
/// Frames provide a functional layer, that allows its bounding components to be directly embedded into a different
/// invocation or to be transformed into another object entirely. For this occasion, the API provides
/// [#accept(Frame.BoundingConsumer)] and [#transform(Frame.Transformer)] methods.
///
/// ## Java
/// Comparing instances of this class (see [Object#equals(Object)]) will compare the bounding components of this
/// frame and not its identity. It also provides a custom [Object#hashCode()] implementation based on these components.
///
/// Converting this class to string (see [Object#toString()]) will be in the following format:
/// `Frame{left=,top=,right=,bottom=}`
public class Frame
{
    //******************************************************************************************************************
    /// A functional interface that allows consuming a frame's bounding components.
    @FunctionalInterface
    public interface BoundingConsumer
    {
        //**************************************************************************************************************
        /// Accepts the bounding components.
        /// @param left   The thickness of the left side of the frame
        /// @param top    The thickness of the top side of the frame
        /// @param right  The thickness of the right side of the frame
        /// @param bottom The thickness of the bottom side of the frame
        void accept(int left, int top, int right, int bottom);
    }
    
    /// A functional interface that allows consuming a frame's bounding components and transforming them to a
    /// different object.
    @FunctionalInterface
    public interface Transformer<T>
    {
        //**************************************************************************************************************
        /// Applies the bounding components.
        /// @param left   The thickness of the left side of the frame
        /// @param top    The thickness of the top side of the frame
        /// @param right  The thickness of the right side of the frame
        /// @param bottom The thickness of the bottom side of the frame
        /// @return The new object that this frame's bounding components were transformed to
        T apply(int left, int top, int right, int bottom);
    }
    
    //******************************************************************************************************************
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;
    
    //******************************************************************************************************************
    /// Constructs a new [Frame].
    /// @param left   The thickness of the left side of the frame
    /// @param top    The thickness of the top side of the frame
    /// @param right  The thickness of the right side of the frame
    /// @param bottom The thickness of the bottom side of the frame
    public Frame(final @NotNull Number left,
                 final @NotNull Number top,
                 final @NotNull Number right,
                 final @NotNull Number bottom)
    {
        this.left   = left  .intValue();
        this.top    = top   .intValue();
        this.right  = right .intValue();
        this.bottom = bottom.intValue();
    }
    
    /// Constructs a new [Frame].
    /// @param allSides The thickness of all sides of the frame
    public Frame(final @NotNull Number allSides) { this(allSides, allSides, allSides, allSides); }
    
    /// Constructs a new [Frame].
    /// @param top          The thickness of the top side of the frame
    /// @param leftAndRight The thickness of the top and bottom side of the frame
    /// @param bottom       The thickness of the bottom side of the frame
    public Frame(final @NotNull Number top, final @NotNull Number leftAndRight, final @NotNull Number bottom)
    {
        this(leftAndRight, top, leftAndRight, bottom);
    }
    
    /// Constructs a new [Frame].
    /// @param leftAndRight The thickness of the left and right side of the frame
    /// @param topAndBottom The thickness of the top and bottom side of the frame
    public Frame(final @NotNull Number leftAndRight, final @NotNull Number topAndBottom)
    {
        this(leftAndRight, topAndBottom, leftAndRight, topAndBottom);
    }
    
    /// Constructs a new empty [Frame] with the thickness of all sides initialised to zero.
    public Frame() { this(0, 0, 0, 0); }
    
    //==================================================================================================================
    /// {@return the thickness of the left side of this frame}
    public int left() { return this.left; }
    
    /// {@return the thickness of the top side of this frame}
    public int top() { return this.top; }
    
    /// {@return the thickness of the right side of this frame}
    public int right() { return this.right; }
    
    /// {@return the thickness of the bottom side of this frame}
    public int bottom() { return this.bottom; }
    
    //==================================================================================================================
    /// Gets a new [Frame] with the left side set to the given thickness `left`.
    /// @param left The new thickness for the left side
    /// @return The new [Frame]
    public @NotNull Frame withLeft(final @NotNull Number left)
    {
        return new Frame(left.intValue(), this.top, this.right, this.bottom);
    }
    
    /// Gets a new [Frame] with the top side set to the given thickness `top`.
    /// @param top The new thickness for the top side
    /// @return The new [Frame]
    public @NotNull Frame withTop(final @NotNull Number top)
    {
        return new Frame(this.left, top.intValue(), this.right, this.bottom);
    }
    
    /// Gets a new [Frame] with the right side set to the given thickness `right`.
    /// @param right The new thickness for the right side
    /// @return The new [Frame]
    public @NotNull Frame withRight(final @NotNull Number right)
    {
        return new Frame(this.right, this.top, right.intValue(), this.bottom);
    }
    
    /// Gets a new [Frame] with the bottom side set to the given thickness `bottom`.
    /// @param bottom The new thickness for the bottom side
    /// @return The new [Frame]
    public @NotNull Frame withBottom(final @NotNull Number bottom)
    {
        return new Frame(this.left, this.top, this.right, bottom.intValue());
    }
    
    /// Gets a new [Frame] with the given sides expanded by the given amounts, negative values will reduce the
    /// thickness.
    /// @param left   The amount to expand the left
    /// @param top    The amount to expand the top
    /// @param right  The amount to expand the right
    /// @param bottom The amount to expand the bottom
    /// @return The new [Frame]
    public @NotNull Frame withExpansion(final @NotNull Number left,
                                        final @NotNull Number top,
                                        final @NotNull Number right,
                                        final @NotNull Number bottom)
    {
        return new Frame(
            (this.left   + left  .intValue()),
            (this.top    + top   .intValue()),
            (this.right  + right .intValue()),
            (this.bottom + bottom.intValue()));
    }
    
    /// Gets a new [Frame] with the given sides expanded by the given amount, negative values will reduce the
    /// thickness.
    /// @param allSide The amount to expand on all sides
    /// @return The new [Frame]
    public @NotNull Frame withExpansion(final @NotNull Number allSide)
    {
        final int value = allSide.intValue();
        return new Frame((this.left + value), (this.top + value), (this.right + value), (this.bottom + value));
    }
    
    /// Gets a new [Frame] with the given sides expanded by the given amount, negative values will reduce the
    /// thickness.
    /// @param leftAndRight The amount to expand on the left and right side
    /// @param topAndBottom The amount to expand on the top and bottom side
    /// @return The new [Frame]
    public @NotNull Frame withExpansion(final @NotNull Number leftAndRight, final @NotNull Number topAndBottom)
    {
        final int lr = leftAndRight.intValue();
        final int tb = topAndBottom.intValue();
        return new Frame((this.left + lr), (this.top + tb), (this.right + lr), (this.bottom + tb));
    }
    
    /// Gets a new [Frame] with the left side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the left side
    /// @return The new [Frame]
    public @NotNull Frame leftExpanded(final @NotNull Number amount)
    {
        return new Frame((this.left + amount.intValue()), this.top, this.right, this.bottom);
    }
    
    /// Gets a new [Frame] with the top side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the top side
    /// @return The new [Frame]
    public @NotNull Frame topExpanded(final @NotNull Number amount)
    {
        return new Frame(this.left, (this.top + amount.intValue()), this.right, this.bottom);
    }
    
    /// Gets a new [Frame] with the right side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the right side
    /// @return The new [Frame]
    public @NotNull Frame rightExpanded(final @NotNull Number amount)
    {
        return new Frame(this.left, this.top, (this.right + amount.intValue()), this.bottom);
    }
    
    /// Gets a new [Frame] with the bottom side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the bottom side
    /// @return The new [Frame]
    public @NotNull Frame bottomExpanded(final @NotNull Number amount)
    {
        return new Frame(this.left, this.top, this.right, (this.bottom + amount.intValue()));
    }
    
    //==================================================================================================================
    /// Accepts this frame's bounding components with the given consumer.
    /// @param consumer The [BoundingConsumer] to accept
    public void accept(final @NotNull BoundingConsumer consumer)
    {
        consumer.accept(this.left, this.top, this.right, this.bottom);
    }
    
    /// Applies this frame's bounding components to the given transformer and returns the result of this operation.
    /// @param transformer The [Transformer] to apply this frame's bounding components to
    /// @return The result of this operation
    /// @param <T> The result type of this operation
    public <T> T transform(final @NotNull Transformer<T> transformer)
    {
        return transformer.apply(this.left, this.top, this.right, this.bottom);
    }
    
    //==================================================================================================================
    @Override
    public @NotNull String toString()
    {
        return "Frame{left=%d, top=%d, right=%d, bottom=%d}".formatted(this.left, this.top, this.right, this.bottom);
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                   return true;
        if (!(obj instanceof Frame other)) return false;
        
        return (
               this.left   == other.left
            && this.top    == other.top
            && this.right  == other.right
            && this.bottom == other.bottom
        );
    }
    
    @Override public int hashCode() { return Objects.hash(this.left, this.top, this.right, this.bottom); }
}

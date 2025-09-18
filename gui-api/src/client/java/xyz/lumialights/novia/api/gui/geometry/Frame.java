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

import java.util.*;



//**********************************************************************************************************************
public record Frame(int left, int top, int right, int bottom)
{
    //******************************************************************************************************************
    @FunctionalInterface
    public interface Consumer
    {
        //**************************************************************************************************************
        void accept(int left, int top, int right, int bottom);
    }
    
    @FunctionalInterface
    public interface Function<T>
    {
        //**************************************************************************************************************
        T apply(int left, int top, int right, int bottom);
    }
    
    //******************************************************************************************************************
    public Frame() { this(0, 0, 0, 0); }
    
    public Frame(final int allSides) { this(allSides, allSides, allSides, allSides); }
    
    public Frame(final int leftAndRight, final int topAndBottom)
    {
        this(leftAndRight, topAndBottom, leftAndRight, topAndBottom);
    }
    
    //==================================================================================================================
    public @NotNull Frame withLeft(final int left)
    {
        return new Frame(left, this.top, this.right, this.bottom);
    }
    
    public @NotNull Frame withTop(final int top)
    {
        return new Frame(this.left, top, this.right, this.bottom);
    }
    
    public @NotNull Frame withRight(final int right)
    {
        return new Frame(this.right, this.top, right, this.bottom);
    }
    
    public @NotNull Frame withBottom(final int bottom)
    {
        return new Frame(this.left, this.top, this.right, bottom);
    }
    
    //==================================================================================================================
    public @NotNull Frame expanded(final int left, final int top, final int right, final int bottom)
    {
        return new Frame((this.left + left), (this.top + top), (this.right + right), (this.bottom + bottom));
    }
    
    public @NotNull Frame expanded(final int allSide)
    {
        return new Frame((this.left + allSide), (this.top + allSide), (this.right + allSide), (this.bottom + allSide));
    }
    
    public @NotNull Frame expanded(final int leftAndRight, final int topAndBottom)
    {
        return new Frame(
            (this.left + leftAndRight), (this.top + topAndBottom),
            (this.right + leftAndRight), (this.bottom + topAndBottom));
    }
    
    public @NotNull Frame leftExpanded(final int amount)
    {
        return new Frame((this.left + amount), this.top, this.right, this.bottom);
    }
    
    public @NotNull Frame topExpanded(final int amount)
    {
        return new Frame(this.left, (this.top + amount), this.right, this.bottom);
    }
    
    public @NotNull Frame rightExpanded(final int amount)
    {
        return new Frame(this.left, this.top, (this.right + amount), this.bottom);
    }
    
    public @NotNull Frame bottomExpanded(final int amount)
    {
        return new Frame(this.left, this.top, this.right, (this.bottom + amount));
    }
    
    //==================================================================================================================
    public void accept(final @NotNull Consumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        consumer.accept(this.left, this.top, this.right, this.bottom);
    }
    
    public <T> T apply(final @NotNull Function<T> function)
    {
        Objects.requireNonNull(function, "function must not be null");
        return function.apply(this.left, this.top, this.right, this.bottom);
    }
}

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
package xyz.lumialights.novia.api.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;



//**********************************************************************************************************************
public abstract class RefUtils
{
    //******************************************************************************************************************
    public static <T> @NotNull Predicate<T> equal(final @Nullable T other)
    {
        return (t -> Objects.equals(t, other));
    }
    
    public static <T> @NotNull Predicate<T> same(final @Nullable T other)
    {
        return (t -> t == other);
    }
    
    public static <T extends Comparable<T>> @NotNull Predicate<T> greaterThan(final @NotNull T other)
    {
        return (t -> t.compareTo(other) > 0);
    }
    
    public static <T extends Comparable<T>> @NotNull Predicate<T> greaterThanOrEqual(final @NotNull T other)
    {
        return (t -> t.compareTo(other) >= 0);
    }
    
    public static <T extends Comparable<T>> @NotNull Predicate<T> lessThan(final @NotNull T other)
    {
        return (t -> t.compareTo(other) < 0);
    }
    
    public static <T extends Comparable<T>> @NotNull Predicate<T> lessThanOrEqual(final @NotNull T other)
    {
        return (t -> t.compareTo(other) <= 0);
    }
    
    public static <T> @NotNull Predicate<T> alwaysTrue() { return (t -> true); }
    
    public static <T> @NotNull Predicate<T> alwaysFalse() { return (t -> false); }
    
    public static <T> @NotNull Consumer<T> emptyConsumer() { return (t -> {}); }
    
    public static @NotNull Runnable emptyRunnable() { return (() -> {}); }

    public static <T> @NotNull Supplier<@Nullable T> nullSupplier() { return (() -> null); }
}

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
package xyz.lumialights.novia.api.gui.canvas;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;



//**********************************************************************************************************************
public final class ColourId
    extends Number
{
    //******************************************************************************************************************
    public static final class Range
    {
        //**************************************************************************************************************
        public final @NotNull ImmutableList<ColourId> ids;
        
        //**************************************************************************************************************
        Range(final int start, final int count)
        {
            this.ids = IntStream
                .range(start, (start + count))
                .mapToObj(ColourId::new)
                .collect(ImmutableList.toImmutableList());
        }
        
        //==============================================================================================================
        public @NotNull ColourId get(final int index)
        {
            return this.ids.get(index);
        }
    }
    
    //******************************************************************************************************************
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(0);
    
    //==================================================================================================================
    public static @NotNull ColourId reserve() { return new ColourId(CURRENT_ID.getAndIncrement()); }
    
    public static @NotNull Range reserveRange(final int count) { return new Range(CURRENT_ID.getAndAdd(count), count); }
    
    public static int getSlotCount() { return CURRENT_ID.get(); }
    
    //******************************************************************************************************************
    /** The integral ID of this colour ID object. */
    public final int id;
    
    //******************************************************************************************************************
    ColourId(final int id) { this.id = id; }
    
    //==================================================================================================================
    @Override public int    intValue()    { return this.id; }
    @Override public long   longValue()   { return this.id; }
    @Override public float  floatValue()  { return this.id; }
    @Override public double doubleValue() { return this.id; }
    
    //==================================================================================================================
    @Override public @NotNull String toString() { return ("ColourId#" + this.id); }
    
    @Override
    public boolean equals(final @NotNull Object obj)
    {
        if (obj == this)                      return true;
        if (!(obj instanceof ColourId other)) return false;
        return (this.id == other.id);
    }
    
    @Override public int hashCode() { return this.id; }
}

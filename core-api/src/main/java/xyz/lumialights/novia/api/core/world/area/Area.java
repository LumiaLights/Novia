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
package xyz.lumialights.novia.api.core.world.area;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.world.area.shapes.CuboidShape;

import java.util.*;
import java.util.function.Consumer;



//**********************************************************************************************************************
public class Area
{
    //******************************************************************************************************************
    public static @NotNull Area cuboid(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2)
    {
        return new Area(new CuboidShape(pos1, pos2));
    }

    public static @NotNull Area cuboid(final int x1, final int y1, final int z1,
                                       final int x2, final int y2, final int z2)
    {
        return new Area(new CuboidShape(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2)));
    }

    //******************************************************************************************************************
    private final Set<BlockPos> blocks;
    private final IAreaShape    shape;

    //******************************************************************************************************************
    public Area(@NotNull final IAreaShape shape)
    {
        this.shape  = shape;
        this.blocks = ImmutableSet.copyOf(shape.build());
    }

    //==================================================================================================================
    public @NotNull IAreaShape getShape() { return this.shape; }
    
    /**
     * Returns an immutable set of blocks currently held by this {@link Area} object.
     * @return The position set
     */
    public @NotNull Set<BlockPos> getBlocks() { return this.blocks; }
    
    //==================================================================================================================
    public void apply(@NotNull final Consumer<BlockPos> consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        this.blocks.forEach(consumer);
    }

    //==================================================================================================================
    public boolean intersectsWith(@NotNull final Area other)
    {
        Objects.requireNonNull(other, "other must not be null");
        return this.blocks.stream().anyMatch(other.blocks::contains);
    }

    //==================================================================================================================
    public @NotNull Set<BlockPos> intersection(@NotNull final Area other)
    {
        Objects.requireNonNull(other, "other must not be null");
        
        if (this.blocks.isEmpty() || other.blocks.isEmpty()) return new LinkedHashSet<>();
        return Sets.intersection(this.blocks, other.blocks);
    }

    public @NotNull Set<BlockPos> difference(@NotNull final Area other)
    {
        Objects.requireNonNull(other, "other must not be null");
        
        if (this.blocks.isEmpty())  return new LinkedHashSet<>(other.blocks);
        if (other.blocks.isEmpty()) return new LinkedHashSet<>(this.blocks);
        
        final Set<BlockPos> set1 = Sets.difference(this.blocks,  other.blocks);
        final Set<BlockPos> set2 = Sets.difference(other.blocks, this.blocks);
        
        final BlockPos pos1 = set1.stream().findFirst().orElseThrow();
        final BlockPos pos2 = set2.stream().findFirst().orElseThrow();
        
        return (pos1.compareTo(pos2) <= 0
            ? new LinkedHashSet<>(Sets.union(set1, set2))
            : new LinkedHashSet<>(Sets.union(set2, set1)));
    }

    public @NotNull Set<BlockPos> union(@NotNull final Area other)
    {
        Objects.requireNonNull(other, "other must not be null");
        
        if (this.blocks.isEmpty())  return new LinkedHashSet<>(other.blocks);
        if (other.blocks.isEmpty()) return new LinkedHashSet<>(this.blocks);
        
        return Sets.union(this.blocks, other.blocks);
    }

    //==================================================================================================================
    public @NotNull Area expanded(@NotNull final Direction direction, final int amount)
    {
        if (amount == 0) return this;
        return new Area(shape.expand(direction, amount));
    }

    public @NotNull Area reduced(@NotNull final Direction direction, final int amount)
    {
        if (amount == 0) return this;
        return new Area(shape.reduce(direction, amount));
    }

    public @NotNull Area moved(@NotNull final Direction direction, final int amount)
    {
        if (amount == 0) return this;
        return new Area(shape.move(direction, amount));
    }
}

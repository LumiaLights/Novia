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
package xyz.lumialights.novia.api.core.world.area.shapes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.world.area.IAreaShape;
import xyz.lumialights.novia.api.core.util.Pair;

import java.util.*;



//**********************************************************************************************************************
public class CuboidShape
    implements IAreaShape
{
    //******************************************************************************************************************
    private static Pair<BlockPos, BlockPos> reorder(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2)
    {
        Objects.requireNonNull(pos1, "pos1 must not be null");
        Objects.requireNonNull(pos2, "pos2 must not be null");
        
        final int min_x = Math.min(pos1.getX(), pos2.getX());
        final int min_y = Math.min(pos1.getY(), pos2.getY());
        final int min_z = Math.min(pos1.getZ(), pos2.getZ());
        final int max_x = Math.max(pos1.getX(), pos2.getX());
        final int max_y = Math.max(pos1.getY(), pos2.getY());
        final int max_z = Math.max(pos1.getZ(), pos2.getZ());
        
        return Pair.of(new BlockPos(min_x, min_y, min_z), new BlockPos(max_x, max_y, max_z));
    }

    //******************************************************************************************************************
    public final BlockPos minPos;
    public final BlockPos maxPos;
    
    //******************************************************************************************************************
    public CuboidShape(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2)
    {
        this(reorder(
            Objects.requireNonNull(pos1, "pos1 must not be null"),
            Objects.requireNonNull(pos2, "pos2 must not be null")));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private CuboidShape(@NotNull final Pair<BlockPos, BlockPos> posPair)
    {
        this.minPos = posPair.first();
        this.maxPos = posPair.second();
    }

    //==================================================================================================================
    @Override
    public @NotNull Set<BlockPos> build()
    {
        final Set<BlockPos> result = new LinkedHashSet<>();
        
        for (int z = this.minPos.getZ(); z <= this.maxPos.getZ(); ++z)
        {
            for (int y = this.minPos.getY(); y <= this.maxPos.getY(); ++y)
            {
                for (int x = this.minPos.getX(); x <= this.maxPos.getX(); ++x)
                {
                    result.add(new BlockPos(x, y, z));
                }
            }
        }
        
        return result;
    }

    //==================================================================================================================
    @Override
    public @NotNull IAreaShape expand(@NotNull final Direction direction, final int amount)
    {
        Objects.requireNonNull(direction, "direction must not be null");
        
        return switch (direction)
        {
            case DOWN  -> new CuboidShape(this.minPos.down (amount), this.maxPos);
            case NORTH -> new CuboidShape(this.minPos.north(amount), this.maxPos);
            case WEST  -> new CuboidShape(this.minPos.west (amount), this.maxPos);
            
            case UP    -> new CuboidShape(this.minPos, this.maxPos.up   (amount));
            case SOUTH -> new CuboidShape(this.minPos, this.maxPos.south(amount));
            case EAST  -> new CuboidShape(this.minPos, this.maxPos.east (amount));
        };
    }

    @Override
    public @NotNull IAreaShape reduce(@NotNull final Direction direction, final int amount)
    {
        Objects.requireNonNull(direction, "direction must not be null");
        
        final BlockPos new_min = switch (direction)
        {
            case DOWN  -> this.minPos.up   (amount);
            case NORTH -> this.minPos.south(amount);
            case WEST  -> this.minPos.east (amount);
            default    -> this.minPos;
        };
        final BlockPos new_max = switch (direction)
        {
            case UP    -> this.maxPos.down (amount);
            case SOUTH -> this.maxPos.north(amount);
            case EAST  -> this.maxPos.west (amount);
            default    -> this.maxPos;
        };

        if (new_min.getX() > new_max.getX() || new_min.getY() > new_max.getY() || new_min.getZ() > new_max.getZ())
        {
            return new CuboidShape(reorder(new_min, new_max));
        }
        
        return new CuboidShape(new_min, new_max);
    }

    @Override
    public @NotNull IAreaShape move(@NotNull final Direction direction, final int amount)
    {
        Objects.requireNonNull(direction, "direction must not be null");
        return new CuboidShape(this.minPos.offset(direction, amount), this.maxPos.offset(direction, amount));
    }
}

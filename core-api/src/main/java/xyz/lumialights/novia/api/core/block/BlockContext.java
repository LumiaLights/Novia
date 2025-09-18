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
package xyz.lumialights.novia.api.core.block;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;



//**********************************************************************************************************************
public class BlockContext
{
    //******************************************************************************************************************
    public static @NotNull BlockContext forWorld(@NotNull final World world, @NotNull final BlockPos pos)
    {
        return new BlockContext(world, world.getBlockState(pos), pos);
    }
    
    //******************************************************************************************************************
    private final World      world;
    private final BlockPos   pos;
    private       BlockState state;
    
    //******************************************************************************************************************
    public BlockContext(@NotNull World world, @NotNull BlockState state, @NotNull BlockPos pos)
    {
        this.world = world;
        this.pos   = pos;
        this.state = state;
    }
    
    //==================================================================================================================
    public @NotNull BlockState state() { return this.state; }
    
    public @NotNull BlockPos pos() { return this.pos; }
    
    public @NotNull World world() { return this.world; }
    
    //==================================================================================================================
    public float getHardness()
    {
        return this.state.getHardness(this.world, this.pos);
    }
    
    //==================================================================================================================
    public void setBlockState(@NotNull final BlockState state)
    {
        if (this.state == state)
        {
            return;
        }
        
        this.world.setBlockState(pos, state);
        this.state = state;
    }
    
    //==================================================================================================================
    public <T> boolean hasTag(final TagKey<T> tag)
    {
        return this.state.streamTags().anyMatch(tag::equals);
    }
    
    //==================================================================================================================
    public boolean isSolid()
    {
        return this.state.isSolidBlock(world, pos);
    }
}

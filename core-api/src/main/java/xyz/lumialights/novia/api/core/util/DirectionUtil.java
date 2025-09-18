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

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;



//======================================================================================================================
public class DirectionUtil
{
    //==================================================================================================================
    private static final ImmutableList<Direction> HORIZONTAL_DIRECTIONS = ImmutableList.of(
        Direction.NORTH,
        Direction.WEST,
        Direction.SOUTH,
        Direction.EAST
    );

    //==================================================================================================================
    /**
     * Gathers the direction of the block face the player is looking at. The returned value determines what
     * direction the block's face is looking at and not in what direction the player is looking.
     * <p>
     * Note that the block position vector should indicate the centre of a block, due to this, this function might not
     * be very accurate for non-full blocks, especially with very complex ones like anvils.
     * @param posCentreVec The centre vector of the block position to determine the direction from
     * @param rayHitVec    The vector of the target location the player was looking at
     * @return The direction computed from the given vectors
     */
    @NotNull
    public static Direction getFullBlockFaceFromLookVec(@NotNull final Vec3d posCentreVec,
                                                        @NotNull final Vec3d rayHitVec)
    {
        final double x = (rayHitVec.x - posCentreVec.x);
        final double y = (rayHitVec.y - posCentreVec.y);
        final double z = (rayHitVec.z - posCentreVec.z);

        final double x_rad = Math.atan2(y, x);
        final double z_rad = Math.atan2(y, z);
        final int    x_deg = (int)(x_rad * (180.0 / Math.PI));
        final int    z_deg = (int)(z_rad * (180.0 / Math.PI));

        if (x_deg >= 45 && x_deg < 135 && z_deg >= 45 && z_deg < 135)
        {
            return Direction.UP;
        }
        else if (x_deg <= -45 && x_deg > -135 && z_deg <= -45 && z_deg > -135)
        {
            return Direction.DOWN;
        }

        final double y_rad = Math.atan2(x, z);
        final int    y_deg = (((int)(y_rad * (180.0 / Math.PI) + 180.0) + 45) % 360);

        return (HORIZONTAL_DIRECTIONS.get(y_deg / 90));
    }
}

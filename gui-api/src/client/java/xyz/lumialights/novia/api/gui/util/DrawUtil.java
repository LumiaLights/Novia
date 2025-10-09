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
package xyz.lumialights.novia.api.gui.util;

import net.minecraft.client.render.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;



//**********************************************************************************************************************
public abstract class DrawUtil
{
    //******************************************************************************************************************
    public static @NotNull Matrix4f toDepthMatrix(final @NotNull Matrix3x2f matrix, final float depth)
    {
        return (new Matrix4f()).mul(matrix).translate(0f, 0f, depth);
    }
    
    //==================================================================================================================
    public static void drawRect(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                                final float z,
                                final float x1, final float y1, final float x2, final float y2,
                                final int ctl, final int ctr, final int cbl, final int cbr,
                                final int light)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl).light(light);
        consumer.vertex(matrix, x1, y2, z).color(cbl).light(light);
        consumer.vertex(matrix, x2, y2, z).color(cbr).light(light);
        consumer.vertex(matrix, x2, y1, z).color(ctr).light(light);
    }
    
    public static void drawRect(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer, final float z,
                                final float x1, final float y1, final float x2, final float y2,
                                final int ctl, final int ctr, final int cbl, final int cbr)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl);
        consumer.vertex(matrix, x1, y2, z).color(cbl);
        consumer.vertex(matrix, x2, y2, z).color(cbr);
        consumer.vertex(matrix, x2, y1, z).color(ctr);
    }
    
    public static void drawRect(final @NotNull Matrix3x2f matrix, final @NotNull VertexConsumer consumer,
                                final float z,
                                final float x1, final float y1, final float x2, final float y2,
                                final int ctl, final int ctr, final int cbl, final int cbr,
                                final int light)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl).light(light);
        consumer.vertex(matrix, x1, y2, z).color(cbl).light(light);
        consumer.vertex(matrix, x2, y2, z).color(cbr).light(light);
        consumer.vertex(matrix, x2, y1, z).color(ctr).light(light);
    }
    
    public static void drawRect(final @NotNull Matrix3x2f matrix, final @NotNull VertexConsumer consumer, final float z,
                                final float x1, final float y1, final float x2, final float y2,
                                final int ctl, final int ctr, final int cbl, final int cbr)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl);
        consumer.vertex(matrix, x1, y2, z).color(cbl);
        consumer.vertex(matrix, x2, y2, z).color(cbr);
        consumer.vertex(matrix, x2, y1, z).color(ctr);
    }
    
    public static void drawBorder(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                                  final float z, final float thickness,
                                  final float x1, final float y1, final float x2, final float y2,
                                  final int ctl, final int ctr, final int cbl, final int cbr,
                                  final int light)
    {
        final float top_t    = (y1 + thickness);
        final float bottom_t = (y2 - thickness);
        final float left_t   = (x1 + thickness);
        final float right_t  = (x2 - thickness);
        
        DrawUtil.drawRect(matrix, consumer, z, x1,      y1,       x2,     top_t,    ctl, ctr, ctl, ctr, light);
        DrawUtil.drawRect(matrix, consumer, z, x1,      bottom_t, x2,     y2,       cbl, cbr, cbl, cbr, light);
        DrawUtil.drawRect(matrix, consumer, z, x1,      top_t,    left_t, bottom_t, ctl, ctl, cbl, cbl, light);
        DrawUtil.drawRect(matrix, consumer, z, right_t, top_t,    x2,     bottom_t, ctr, ctr, cbr, cbr, light);
    }
    
    public static void drawBorder(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                                  final float z, final float thickness,
                                  final float x1, final float y1, final float x2, final float y2,
                                  final int ctl, final int ctr, final int cbl, final int cbr)
    {
        final float top_t    = (y1 + thickness);
        final float bottom_t = (y2 - thickness);
        final float left_t   = (x1 + thickness);
        final float right_t  = (x2 - thickness);
        
        DrawUtil.drawRect(matrix, consumer, z, x1,      y1,       x2,     top_t,    ctl, ctr, ctl, ctr);
        DrawUtil.drawRect(matrix, consumer, z, x1,      bottom_t, x2,     y2,       cbl, cbr, cbl, cbr);
        DrawUtil.drawRect(matrix, consumer, z, x1,      top_t,    left_t, bottom_t, ctl, ctl, cbl, cbl);
        DrawUtil.drawRect(matrix, consumer, z, right_t, top_t,    x2,     bottom_t, ctr, ctr, cbr, cbr);
    }
    
    public static void drawBorder(final @NotNull Matrix3x2f matrix, final @NotNull VertexConsumer consumer,
                                  final float z, final float thickness,
                                  final float x1, final float y1, final float x2, final float y2,
                                  final int ctl, final int ctr, final int cbl, final int cbr,
                                  final int light)
    {
        final float top_t    = (y1 + thickness);
        final float bottom_t = (y2 - thickness);
        final float left_t   = (x1 + thickness);
        final float right_t  = (x2 - thickness);
        
        DrawUtil.drawRect(matrix, consumer, z, x1,      y1,       x2,     top_t,    ctl, ctr, ctl, ctr, light);
        DrawUtil.drawRect(matrix, consumer, z, x1,      bottom_t, x2,     y2,       cbl, cbr, cbl, cbr, light);
        DrawUtil.drawRect(matrix, consumer, z, x1,      top_t,    left_t, bottom_t, ctl, ctl, cbl, cbl, light);
        DrawUtil.drawRect(matrix, consumer, z, right_t, top_t,    x2,     bottom_t, ctr, ctr, cbr, cbr, light);
    }
    
    public static void drawBorder(final @NotNull Matrix3x2f matrix, final @NotNull VertexConsumer consumer,
                                  final float z, final float thickness,
                                  final float x1, final float y1, final float x2, final float y2,
                                  final int ctl, final int ctr, final int cbl, final int cbr)
    {
        final float top_t    = (y1 + thickness);
        final float bottom_t = (y2 - thickness);
        final float left_t   = (x1 + thickness);
        final float right_t  = (x2 - thickness);
        
        DrawUtil.drawRect(matrix, consumer, z, x1,      y1,       x2,     top_t,    ctl, ctr, ctl, ctr);
        DrawUtil.drawRect(matrix, consumer, z, x1,      bottom_t, x2,     y2,       cbl, cbr, cbl, cbr);
        DrawUtil.drawRect(matrix, consumer, z, x1,      top_t,    left_t, bottom_t, ctl, ctl, cbl, cbl);
        DrawUtil.drawRect(matrix, consumer, z, right_t, top_t,    x2,     bottom_t, ctr, ctr, cbr, cbr);
    }
    
    public static void drawTexturedRect(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                                        final float z, final float x1, final float y1, final float x2, final float y2,
                                        final float u1, final float v1, final float u2, final float v2,
                                        final int ctl, final int ctr, final int cbl, final int cbr,
                                        final int light)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl).texture(u1, v1).light(light);
        consumer.vertex(matrix, x1, y2, z).color(cbl).texture(u1, v2).light(light);
        consumer.vertex(matrix, x2, y2, z).color(cbr).texture(u2, v2).light(light);
        consumer.vertex(matrix, x2, y1, z).color(ctr).texture(u2, v1).light(light);
    }
    
    public static void drawTexturedRect(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                                        final float z,
                                        final float x1, final float y1, final float x2, final float y2,
                                        final float u1, final float v1, final float u2, final float v2,
                                        final int ctl, final int ctr, final int cbl, final int cbr)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl).texture(u1, v1);
        consumer.vertex(matrix, x1, y2, z).color(cbl).texture(u1, v2);
        consumer.vertex(matrix, x2, y2, z).color(cbr).texture(u2, v2);
        consumer.vertex(matrix, x2, y1, z).color(ctr).texture(u2, v1);
    }
    
    public static void drawTexturedRect(final @NotNull Matrix3x2f matrix, final @NotNull VertexConsumer consumer,
                                        final float z,
                                        final float x1, final float y1, final float x2, final float y2,
                                        final float u1, final float v1, final float u2, final float v2,
                                        final int ctl, final int ctr, final int cbl, final int cbr,
                                        final int light)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl).texture(u1, v1).light(light);
        consumer.vertex(matrix, x1, y2, z).color(cbl).texture(u1, v2).light(light);
        consumer.vertex(matrix, x2, y2, z).color(cbr).texture(u2, v2).light(light);
        consumer.vertex(matrix, x2, y1, z).color(ctr).texture(u2, v1).light(light);
    }
    
    public static void drawTexturedRect(final @NotNull Matrix3x2f matrix, final @NotNull VertexConsumer consumer,
                                        final float z,
                                        final float x1, final float y1, final float x2, final float y2,
                                        final float u1, final float v1, final float u2, final float v2,
                                        final int ctl, final int ctr, final int cbl, final int cbr)
    {
        consumer.vertex(matrix, x1, y1, z).color(ctl).texture(u1, v1);
        consumer.vertex(matrix, x1, y2, z).color(cbl).texture(u1, v2);
        consumer.vertex(matrix, x2, y2, z).color(cbr).texture(u2, v2);
        consumer.vertex(matrix, x2, y1, z).color(ctr).texture(u2, v1);
    }
}

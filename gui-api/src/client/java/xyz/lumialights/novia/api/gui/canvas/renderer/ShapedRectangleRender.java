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
package xyz.lumialights.novia.api.gui.canvas.renderer;

import net.minecraft.client.render.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.brush.VertexPalette;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.geometry.UvMapping;
import xyz.lumialights.novia.api.gui.util.DrawUtil;



//**********************************************************************************************************************
public abstract class ShapedRectangleRender
    implements IShapedRender
{
    //******************************************************************************************************************
    /// Provides a shaped renderer that outlines a rectangular area.
    public static class Border
        extends ShapedRectangleRender
    {
        //**************************************************************************************************************
        private final float x1;
        private final float y1;
        private final float x2;
        private final float y2;
        private final float thickness;
        
        //**************************************************************************************************************
        /// Constructs a new border renderer.
        /// @param x1        The x coordinate of where the rectangle starts
        /// @param y1        The y coordinate of where the rectangle starts
        /// @param x2        The x coordinate of where the rectangle ends
        /// @param y2        The y coordinate of where the rectangle ends
        /// @param thickness The thickness of the border rectangle
        public Border(final float x1, final float y1, final float x2, final float y2, final float thickness)
        {
            this.x1        = x1;
            this.y1        = y1;
            this.x2        = x2;
            this.y2        = y2;
            this.thickness = thickness;
        }
        
        /// Constructs a new border renderer.
        /// @param rectangle The rectangle to draw
        /// @param thickness The thickness of the border rectangle
        public Border(final @NotNull Rectangle rectangle, final float thickness)
        {
            this(rectangle.x(), rectangle.y(), rectangle.getRight(), rectangle.getBottom(), thickness);
        }
        
        //==============================================================================================================
        @Override
        public void draw(final @NotNull VertexConsumer vertices, final @NotNull Matrix4f matrix,
                         final int tl, final int tr, final int bl, final int br)
        {
            DrawUtil.drawBorder(matrix, vertices, 0f, this.thickness,
                                this.x1, this.y1, this.x2, this.y2,
                                tl, tr, bl, br);
        }
    }
    
    /// Provides a shaped renderer that fill a rectangular area.
    public static class Fill
        extends ShapedRectangleRender
    {
        //**************************************************************************************************************
        private final float x1;
        private final float y1;
        private final float x2;
        private final float y2;
        
        //**************************************************************************************************************
        /// Constructs a new fill renderer.
        /// @param x1 The x coordinate of where the rectangle starts
        /// @param y1 The y coordinate of where the rectangle starts
        /// @param x2 The x coordinate of where the rectangle ends
        /// @param y2 The y coordinate of where the rectangle ends
        public Fill(final float x1, final float y1, final float x2, final float y2)
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        
        /// Constructs a new fill renderer.
        /// @param rectangle The rectangle to fill
        public Fill(final @NotNull Rectangle rectangle)
        {
            this(rectangle.x(), rectangle.y(), rectangle.getRight(), rectangle.getBottom());
        }
        
        //==============================================================================================================
        @Override
        public void draw(final @NotNull VertexConsumer vertices, final @NotNull Matrix4f matrix,
                         final int tl, final int tr, final int bl, final int br)
        {
            DrawUtil.drawRect(matrix, vertices, 0f, this.x1, this.y1, this.x2, this.y2, tl, tr, bl, br);
        }
    }
    
    /// Provides a shaped renderer that fills a rectangular area with a texture.
    public static class Texture
        extends ShapedRectangleRender
    {
        //**************************************************************************************************************
        private final float x1;
        private final float y1;
        private final float x2;
        private final float y2;
        private final float u1;
        private final float v1;
        private final float u2;
        private final float v2;
        
        //**************************************************************************************************************
        /// Constructs a new fill renderer.
        /// @param x1 The x coordinate of where the rectangle starts
        /// @param y1 The y coordinate of where the rectangle starts
        /// @param x2 The x coordinate of where the rectangle ends
        /// @param y2 The y coordinate of where the rectangle ends
        /// @param u1 The u texture start coordinate
        /// @param v1 The v texture start coordinate
        /// @param u2 The u texture end coordinate
        /// @param v2 The v texture end coordinate
        public Texture(final float x1, final float y1, final float x2, final float y2,
                       final float u1, final float v1, final float u2, final float v2)
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
        }
        
        /// Constructs a new fill renderer.
        /// @param rectangle The rectangle to fill
        /// @param u1        The u texture start coordinate
        /// @param v1        The v texture start coordinate
        /// @param u2        The u texture end coordinate
        /// @param v2        The v texture end coordinate
        public Texture(final @NotNull Rectangle rectangle,
                       final float u1, final float v1, final float u2, final float v2)
        {
            this(rectangle.x(), rectangle.y(), rectangle.getRight(), rectangle.getBottom(), u1, v1, u2, v2);
        }
        
        /// Constructs a new fill renderer.
        /// @param x1 The x coordinate of where the rectangle starts
        /// @param y1 The y coordinate of where the rectangle starts
        /// @param x2 The x coordinate of where the rectangle ends
        /// @param y2 The y coordinate of where the rectangle ends
        /// @param uv The [UvMapping] of the texture to fill
        public Texture(final float x1, final float y1, final float x2, final float y2, final @NotNull UvMapping uv)
        {
            this(x1, y1, x2, y2, uv.minU(), uv.minV(), uv.maxU(), uv.maxV());
        }
        
        /// Constructs a new fill renderer.
        /// @param rectangle The rectangle to fill
        /// @param uv        The [UvMapping] of the texture to fill
        public Texture(final @NotNull Rectangle rectangle, final @NotNull UvMapping uv)
        {
            this(rectangle.x(), rectangle.y(), rectangle.getRight(), rectangle.getBottom(), uv);
        }
        
        //==============================================================================================================
        @Override
        public void draw(final @NotNull VertexConsumer vertices, final @NotNull Matrix4f matrix,
                         final int tl, final int tr, final int bl, final int br)
        {
            DrawUtil.drawTexturedRect(matrix, vertices, 0f,
                                      this.x1, this.y1, this.x2, this.y2,
                                      this.u1, this.v1, this.u2, this.v2,
                                      tl, tr, bl, br);
        }
    }
    
    //******************************************************************************************************************
    private VertexPalette palette;
    
    //******************************************************************************************************************
    @Override
    public void applySolid(final int colour, final float opacity)
    {
        this.palette = new VertexPalette(new Colour(colour).withOpacityRel(opacity));
    }
    
    @Override
    public void applyGradient(final @NotNull Gradient gradient, final float opacity)
    {
        this.palette = gradient.withOpacity(opacity).getPalette();
    }
    
    //==================================================================================================================
    @Override
    public void render(final @NotNull VertexConsumer vertices, final @NotNull Matrix4f matrix)
    {
        this.palette.accept((tl, tr, bl, br) -> this.draw(vertices, matrix, tl, tr, bl, br));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    protected abstract void draw(@NotNull VertexConsumer vertices, @NotNull Matrix4f matrix, int tl, int tr, int bl,
                                 int br);
}

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
package xyz.lumialights.novia.api.gui.canvas.brush;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.IGradientProvider;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.SGradientProvider;
import xyz.lumialights.novia.api.gui.font.GlyphBank;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.impl.BakedGlyphAccessor;

import java.util.*;



//**********************************************************************************************************************
public class SolidBrush
    implements IBrush
{
    //******************************************************************************************************************
    private int colour;
    
    //******************************************************************************************************************
    public SolidBrush(final int colour)                { this.colour = colour; }
    public SolidBrush(final @NotNull Colour colour)    { this(colour.colour()); }
    public SolidBrush(final @NotNull SolidBrush other) { this(other.getColour()); }
    
    //==================================================================================================================
    public int getColour() { return this.colour; }
    
    //==================================================================================================================
    public void setColour(final int colour) { this.colour = colour; }
    
    //==================================================================================================================
    @Override
    public void rectangle(final @NotNull VertexConsumer consumer, final @NotNull Matrix3x2f matrix,
                          final int x1, final int y1, final int x2, final int y2,
                          final float depth, final float opacity)
    {
        final int colour = IBrush.forOpacity(this.colour, opacity);
        DrawUtil.drawRect(matrix, consumer, depth, x1, y1, x2, y2, colour, colour, colour, colour);
    }
    
    @Override
    public void border(final @NotNull VertexConsumer consumer, final @NotNull Matrix3x2f matrix,
                       final int x1, final int y1, final int x2, final int y2,
                       final float depth, final float opacity)
    {
        final int colour = IBrush.forOpacity(this.colour, opacity);
        DrawUtil.drawBorder(matrix, consumer, depth, 1.0f, x1, y1, x2, y2, colour, colour, colour, colour);
    }
    
    @Override
    public void drawTexturedQuad(final @NotNull VertexConsumer consumer, final @NotNull Matrix3x2f matrix,
                                 final int x1, final int y1, final int x2, final int y2,
                                 final float u1, final float v1, final float u2, final float v2,
                                 final float depth, final float opacity)
    {
        final int colour = IBrush.forOpacity(this.colour, opacity);
        DrawUtil.drawTexturedRect(matrix, consumer, depth, x1, y1, x2, y2, u1, v1, u2, v2,
                                  colour, colour, colour, colour);
    }
    
    @Override
    public void drawUnits(final @NotNull List<GlyphBank.Unit> units, final @NotNull VertexConsumer consumer,
                          final @NotNull Matrix4f matrix, final @NotNull Rectangle area, final float opacity)
    {
        final int                def_colour         = IBrush.forOpacity(this.colour, opacity);
        final int                def_shadow_colour  = ColorHelper.scaleRgb(def_colour, 0.25F);
        final SGradientProvider def_palette        = IGradientProvider.solid(def_colour);
        final SGradientProvider def_shadow_palette = IGradientProvider.solid(def_shadow_colour);
        final BakedGlyphAccessor line_glyph         = (BakedGlyphAccessor) GuiFont
            .getDefault()
            .getRectangleBakedGlyph();
        
        for (final var unit : units)
        {
            final SGradientProvider palette;
            final SGradientProvider shadow_palette;

            final Integer override = unit.override();

            if (override == null)
            {
                palette = def_palette;
                
                if (unit.shadowOffset() != 0.0f)
                {
                    final Integer shadow_override = unit.shadowOverride();
                    shadow_palette = (shadow_override != null
                        ? IGradientProvider.solid(IBrush.mixAlpha(def_colour, shadow_override))
                        : def_shadow_palette);
                }
                else { shadow_palette = null; }
            }
            else
            {
                final int override_adjusted = IBrush.forOpacity(override, opacity);
                palette = IGradientProvider.solid(override_adjusted);
                
                if (unit.shadowOffset() != 0.0f)
                {
                    final Integer shadow_override = unit.shadowOverride();
                    shadow_palette = IGradientProvider.solid(shadow_override != null
                        ? IBrush.mixAlpha(override_adjusted, shadow_override)
                        : ColorHelper.scaleRgb(override_adjusted, 0.25f));
                }
                else { shadow_palette = null; }
            }
            
            unit.draw(matrix, consumer, line_glyph, palette, shadow_palette, 0, 0, 15728880);
        }
    }
    
    //==================================================================================================================
    @Override public @NotNull SolidBrush copy() { return new SolidBrush(this); }
}

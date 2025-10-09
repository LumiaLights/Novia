package xyz.lumialights.novia.api.gui.font;

import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.render.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.gui.util.DrawUtil;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.IGradientProvider;
import xyz.lumialights.novia.api.gui.impl.BakedGlyphAccessor;



//**********************************************************************************************************************
public record TextRect(float minX, float minY, float maxX, float maxY, float shadowOffset)
{
    //******************************************************************************************************************
    public float getEffectiveMinX() { return this.minX; }
    public float getEffectiveMinY() { return this.minY; }
    public float getEffectiveMaxX() { return (this.maxX + this.shadowOffset); }
    public float getEffectiveMaxY() { return (this.maxY + this.shadowOffset); }

    //==================================================================================================================
    public BakedGlyph.Rectangle toBaked(final float zIndex, final int colour, final int shadowColour,
                                        final float shadowOffset)
    {
        return new BakedGlyph.Rectangle(this.minX, this.minY, this.maxX, this.maxY, zIndex, colour, shadowColour,
                                        shadowColour);
    }

    //==================================================================================================================
    public void draw(final @NotNull BakedGlyphAccessor glyph, final @NotNull Matrix4f matrix,
                     final @NotNull VertexConsumer consumer, final @NotNull IGradientProvider palette,
                     final @Nullable IGradientProvider shadowPalette, final float originX, final float originY,
                     final int light)
    {
        float z = 0.1f;

        if (shadowPalette != null)
        {
            this.drawVerts(glyph, matrix, consumer, shadowPalette, this.shadowOffset, originX, originY, z,
                           light);
            z += 0.03F;
        }

        this.drawVerts(glyph, matrix, consumer, palette, 0.0f, originX, originY, z, light);
    }

    //------------------------------------------------------------------------------------------------------------------
    private void drawVerts(final @NotNull BakedGlyphAccessor glyph, final @NotNull Matrix4f matrix,
                           final @NotNull VertexConsumer consumer, final @NotNull IGradientProvider palette,
                           final float shadowOffset, final float originX, final float originY, final float z,
                           final int light)
    {
        final float x1 = (originX + this.minX);
        final float y1 = (originY + this.minY);
        final float x2 = (originX + this.maxX);
        final float y2 = (originY + this.maxY);

        palette.accept((x1 - originX), (y1 - originY), (x2 - originX), (y2 - originY), ((tl, tr, bl, br) ->
            DrawUtil.drawTexturedRect(
                matrix, consumer,
                z, (x1 + shadowOffset), (y1 + shadowOffset), (x2 + shadowOffset), (y2 + shadowOffset),
                glyph.minU(), glyph.minV(), glyph.maxU(), glyph.maxV(),
                tl, tr, bl, br,
                light)));
    }
}

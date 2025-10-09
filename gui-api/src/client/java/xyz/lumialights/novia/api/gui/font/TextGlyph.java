package xyz.lumialights.novia.api.gui.font;

import net.minecraft.client.render.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.IGradientProvider;
import xyz.lumialights.novia.api.gui.impl.BakedGlyphAccessor;



//**********************************************************************************************************************
public record TextGlyph(
    @Nullable BakedGlyphAccessor glyph,
    @Nullable Integer            override,
    @Nullable Integer            shadowOverride,
    @Nullable TextRect           underlineRect,
    @Nullable TextRect           strikethroughRect,
              float              scale,
              float              x,
              float              y,
              float              advance,
              float              boldOffset,
              float              shadowOffset,
              float              lineOffset,
              boolean            italic,
              boolean            bold
)
{
    //******************************************************************************************************************
    public float getEffectiveMinX()
    {
        final float line_min_x = (this.strikethroughRect != null
            ? this.strikethroughRect.getEffectiveMinX()
            : (this.underlineRect != null ? this.underlineRect.getEffectiveMinX() : Float.MAX_VALUE));

        if (this.glyph == null)
        {
            return line_min_x;
        }

        return Math.min(
            (this.x
                + (this.glyph.minX() * this.scale)
                + (this.italic ? Math.min(this.getItalicOffsetAtMinY(), this.getItalicOffsetAtMaxY()) : 0f)
                - this.getXExpansion()),
            line_min_x
        );
    }

    public float getEffectiveMinY()
    {
        final float line_min_y = (this.strikethroughRect != null
            ? this.strikethroughRect.getEffectiveMinY()
            : (this.underlineRect != null ? this.underlineRect.getEffectiveMinY() : Float.MAX_VALUE));

        if (this.glyph == null)
        {
            return line_min_y;
        }

        return Math.min(
            (this.y
                + (this.glyph.minY() * this.scale)
                - this.getXExpansion()),
            line_min_y
        );
    }

    public float getEffectiveMaxX()
    {
        final float line_max_x = (this.underlineRect != null
            ? this.underlineRect.getEffectiveMaxX()
            : (this.strikethroughRect != null ? this.strikethroughRect.getEffectiveMaxX() : 0f));

        if (this.glyph == null)
        {
            return line_max_x;
        }

        return Math.max(
            (this.x
                + (this.glyph.maxX() * this.scale)
                + this.shadowOffset
                + (this.italic ? Math.max(this.getItalicOffsetAtMinY(), this.getItalicOffsetAtMaxY()) : 0f)
                + this.getXExpansion()),
            line_max_x
        );
    }

    public float getEffectiveMaxY()
    {
        final float line_max_y = (this.underlineRect != null
            ? this.underlineRect.getEffectiveMaxY()
            : (this.strikethroughRect != null ? this.strikethroughRect.getEffectiveMaxY() : 0f));

        if (this.glyph == null)
        {
            return line_max_y;
        }

        return Math.max(
            (this.y
                + (this.glyph.maxY() * this.scale)
                + this.shadowOffset
                + this.getXExpansion()),
            line_max_y
        );
    }

    public float getXExpansion() { return (this.bold ? (0.1f * this.scale) : 0f); }

    public float getItalicOffsetAtMaxY()
    {
        assert (this.glyph != null);
        return (this.scale - 0.25f * this.glyph.maxY() * this.scale);
    }

    public float getItalicOffsetAtMinY()
    {
        assert (this.glyph != null);
        return (this.scale - 0.25f * this.glyph.minY() * this.scale);
    }

    //==================================================================================================================
    public void draw(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                     final @NotNull BakedGlyphAccessor rectangleGlyph, final @NotNull IGradientProvider palette,
                     final @Nullable IGradientProvider shadowPalette, final float originX, final float originY,
                     final int light)
    {
        if (this.glyph != null)
        {
            this.drawGlyph(matrix, consumer, palette, shadowPalette, originX, originY, light);
        }

        if (this.strikethroughRect != null)
        {
            this.strikethroughRect.draw(rectangleGlyph, matrix, consumer, palette, shadowPalette, originX, originY,
                                        light);
        }

        if (this.underlineRect != null)
        {
            this.underlineRect.draw(rectangleGlyph, matrix, consumer, palette, shadowPalette, originX, originY,
                                    light);
        }
    }

    public void drawGlyph(final @NotNull Matrix4f matrix, final @NotNull VertexConsumer consumer,
                          final @NotNull IGradientProvider palette, final @Nullable IGradientProvider shadowPalette,
                          final float originX, final float originY, final int light)
    {
        final float x               = this.x();
        final float y               = this.y();
        final float shadow_offset_x = (x + this.shadowOffset - (this.italic ? (0.25F * this.scale) : 0.0F));
        final float shadow_offset_y = (y + this.shadowOffset);

        if (shadowPalette != null)
        {
            this.drawGlyphVerts(shadow_offset_x, shadow_offset_y, matrix, consumer, shadowPalette, originX, originY,
                                light);

            if (this.bold())
            {
                this.drawGlyphVerts((shadow_offset_x + this.boldOffset), shadow_offset_y, matrix, consumer,
                                    shadowPalette, originX, originY, light);
            }
        }

        this.drawGlyphVerts(x, y, matrix, consumer, palette, originX, originY, light);

        if (this.bold())
        {
            this.drawGlyphVerts((x + this.boldOffset), y, matrix, consumer, palette, originX, originY, light);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void drawGlyphVerts(final float x, final float y, final @NotNull Matrix4f matrix,
                                final @NotNull VertexConsumer vertexConsumer,
                                final @NotNull IGradientProvider palette, final float originX, final float originY,
                                final int light)
    {
        assert (this.glyph != null);

        final float x1          = (x + this.glyph.minX() * this.scale + originX);
        final float y1          = (y + this.glyph.minY() * this.scale + originY);
        final float x2          = (x + this.glyph.maxX() * this.scale + originX);
        final float y2          = (y + this.glyph.maxY() * this.scale + originY);
        final float italic_y1   = (this.italic ? this.getItalicOffsetAtMinY() : 0.0f);
        final float italic_y2   = (this.italic ? this.getItalicOffsetAtMaxY() : 0.0f);
        final float bold_offset = this.getXExpansion();

        palette.accept((x1 - originX), (y1 - originY), (x2 - originX), (y2 - originY), ((tl, tr, bl, br) ->
        {
            vertexConsumer
                .vertex(matrix, (x1 + italic_y1 - bold_offset), (y1 - bold_offset), 0.0f)
                .color(tl)
                .texture(this.glyph.minU(), this.glyph.minV())
                .light(light);
            vertexConsumer
                .vertex(matrix, (x1 + italic_y2 - bold_offset), (y2 + bold_offset), 0.0f)
                .color(bl)
                .texture(this.glyph.minU(), this.glyph.maxV())
                .light(light);
            vertexConsumer
                .vertex(matrix, (x2 + italic_y2 + bold_offset), (y2 + bold_offset), 0.0f)
                .color(br)
                .texture(this.glyph.maxU(), this.glyph.maxV())
                .light(light);
            vertexConsumer
                .vertex(matrix, (x2 + italic_y1 + bold_offset), (y1 - bold_offset), 0.0f)
                .color(tr)
                .texture(this.glyph.maxU(), this.glyph.minV())
                .light(light);
        }));
    }
}

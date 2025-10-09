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

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;

import java.lang.Math;
import java.util.*;



//**********************************************************************************************************************
/** Describes a transform that applies scaling, translation or rotation to a {@link Canvas}. */
@SuppressWarnings("ClassCanBeRecord")
public final class AffineTransform
{
    //******************************************************************************************************************
    /**
     * Creates a new transform with the given translation.
     * @param x The x offset
     * @param y The y offset
     * @return The new {@link AffineTransform}
     */
    public static @NotNull AffineTransform translation(final float x, final float y)
    {
        return (new AffineTransform()).translate(x, y);
    }

    /**
     * Creates a new transform with the given scaling.
     * @param x The scale on the x-axis
     * @param y The scale on the y-axis
     * @return The new {@link AffineTransform}
     */
    public static @NotNull AffineTransform scaling(final float x, final float y)
    {
        return (new AffineTransform()).scale(x, y);
    }

    /**
     * Creates a new transform with the given scaling.
     * @param xy The scale on the x and y-axis
     * @return The new {@link AffineTransform}
     */
    public static @NotNull AffineTransform scaling(final float xy) { return (new AffineTransform()).scale(xy); }

    /**
     * Creates a new transform with the given rotation.
     * @param angle The rotation angle in radians
     * @return The new {@link AffineTransform}
     */
    public static @NotNull AffineTransform rotation(final float angle) { return (new AffineTransform()).rotate(angle); }

    /**
     * Creates a new transform that is multiplied by two other transforms.
     * @param transform1 The transform to be multiplied
     * @param transform2 The transform to multiply
     * @return The new {@link AffineTransform}
     */
    public static @NotNull AffineTransform multiplication(final @NotNull AffineTransform transform1,
                                                          final @NotNull AffineTransform transform2)
    {
        return (new AffineTransform(transform1)).multiply(transform2);
    }

    //******************************************************************************************************************
    private final Matrix3x2f matrix;

    //******************************************************************************************************************
    /** Creates a new empty transform. */
    public AffineTransform() { this.matrix = new Matrix3x2f(); }

    /**
     * Creates a new transform from the given matrix.
     * @param matrix The matrix to construct the transform from
     */
    public AffineTransform(final @NotNull Matrix3x2f matrix) { this.matrix = new Matrix3x2f(matrix); }

    /**
     * Copies the given transform.
     * @param transform The transform to copy
     */
    public AffineTransform(final @NotNull AffineTransform transform) { this(transform.matrix); }

    //==================================================================================================================
    /**
     * Gets the internal matrix of the transform.
     * @return The {@link Matrix3x2f}
     */
    public @NotNull Matrix3x2f getMatrix() { return this.matrix; }

    /**
     * Creates a 4 component matrix from this transform's internal 3 component matrix and applies the given depth
     * value.
     * @param depth The depth value (z-axis) to apply to the matrix
     * @return The new {@link Matrix4f}
     */
    public @NotNull Matrix4f getMatrixWithDepth(final float depth)
    {
        return (new Matrix4f())
            .mul(this.matrix)
            .translate(0.0f, 0.0f, depth);
    }

    //==================================================================================================================
    /**
     * Sets the transform to the given matrix.
     * @param matrix The {@link Matrix3x2f}
     * @return {@code this}
     */
    public @NotNull AffineTransform set(final @NotNull Matrix3x2f matrix)
    {
        this.matrix.set(matrix);
        return this;
    }

    /**
     * Sets the transform to the given other transform.
     * @param transform The other {@link AffineTransform}
     * @return {@code this}
     */
    public @NotNull AffineTransform set(final @NotNull AffineTransform transform)
    {
        return this.set(transform.matrix);
    }

    /**
     * Resets the transform to its identity state.
     * @return {@code this}
     */
    public @NotNull AffineTransform reset()
    {
        this.matrix.identity();
        return this;
    }

    //==================================================================================================================
    /**
     * Multiplies the transform with the given matrix.
     * @param matrix The {@link Matrix3x2f}
     * @return {@code this}
     */
    public @NotNull AffineTransform multiply(final @NotNull Matrix3x2f matrix)
    {
        Objects.requireNonNull(matrix, "matrix must not be null");
        this.matrix.mul(matrix);

        return this;
    }

    /**
     * Multiplies the transform with the given other transform.
     * @param transform The other {@link AffineTransform}
     * @return {@code this}
     */
    public @NotNull AffineTransform multiply(final @NotNull AffineTransform transform)
    {
        return this.multiply(transform.matrix);
    }

    //==================================================================================================================
    /**
     * Applies the transform to the given {@link ScreenRect}.
     * @param rect The {@link ScreenRect} to apply the transform to
     * @return The newly transformed {@link ScreenRect}
     */
    public @NotNull ScreenRect applyToRect(final @NotNull ScreenRect rect)
    {
        return rect.transform(this.matrix);
    }

    /**
     * Applies the transform to the given {@link Rectangle}.
     * @param rect The {@link Rectangle} to apply the transform to
     * @return The newly transformed {@link Rectangle}
     */
    public @NotNull Rectangle applyToRect(final @NotNull Rectangle rect)
    {
        return rect.apply(this::applyToRect);
    }

    /**
     * Applies the transform to the given area.
     * @param x      The x coordinate of the area to transform
     * @param y      The y coordinate of the area to transform
     * @param width  The width of the area to transform
     * @param height The height of the area to transform
     * @return The newly transformed {@link Rectangle}
     */
    public @NotNull Rectangle applyToRect(final int x, final int y, final int width, final int height)
    {
        final Vector2f vec_tl = this.matrix.transformPosition(x, y, new Vector2f());
        final Vector2f vec_br = this.matrix.transformPosition((x + width), (y + height), new Vector2f());

        return new Rectangle(
            MathHelper.floor(vec_tl.x),
            MathHelper.floor(vec_tl.y),
            MathHelper.floor(vec_br.x - vec_tl.x),
            MathHelper.floor(vec_br.y - vec_tl.y));
    }

    /**
     * Applies the transform to all vertices of the given {@link ScreenRect}.
     * @param rect The {@link ScreenRect} to apply the transform to
     * @return The newly transformed {@link ScreenRect}
     */
    public @NotNull ScreenRect applyToVertices(final @NotNull ScreenRect rect)
    {
        return rect.transformEachVertex(this.matrix);
    }

    /**
     * Applies the transform to all vertices of the given {@link Rectangle}.
     * @param rect The {@link Rectangle} to apply the transform to
     * @return The newly transformed {@link Rectangle}
     */
    public @NotNull Rectangle applyToVertices(final @NotNull Rectangle rect)
    {
        return rect.apply(this::applyToVertices);
    }

    /**
     * Applies the transform to all vertices of the given area.
     * @param x      The x coordinate of the area to transform
     * @param y      The y coordinate of the area to transform
     * @param width  The width of the area to transform
     * @param height The height of the area to transform
     * @return The newly transformed {@link Rectangle}
     */
    public @NotNull Rectangle applyToVertices(final int x, final int y, final int width, final int height)
    {
        final int right  = (x + width);
        final int bottom = (y + height);

        final Vector2f vec_tl = this.matrix.transformPosition(x,     y,      new Vector2f());
		final Vector2f vec_tr = this.matrix.transformPosition(right, y,      new Vector2f());
		final Vector2f vec_bl = this.matrix.transformPosition(x,     bottom, new Vector2f());
		final Vector2f vec_br = this.matrix.transformPosition(right, bottom, new Vector2f());

		final float min_x = Math.min(Math.min(vec_tl.x(), vec_bl.x()), Math.min(vec_tr.x(), vec_br.x()));
		final float max_x = Math.max(Math.max(vec_tl.x(), vec_bl.x()), Math.max(vec_tr.x(), vec_br.x()));
		final float min_y = Math.min(Math.min(vec_tl.y(), vec_bl.y()), Math.min(vec_tr.y(), vec_br.y()));
		final float max_y = Math.max(Math.max(vec_tl.y(), vec_bl.y()), Math.max(vec_tr.y(), vec_br.y()));

		return new Rectangle(
            MathHelper.floor(min_x),
            MathHelper.floor(min_y),
            MathHelper.ceil(max_x - min_x),
            MathHelper.ceil(max_y - min_y));
    }

    //==================================================================================================================
    /**
     * Translates the transform the given offset.
     * @param offsetX The offset on the x-axis
     * @param offsetY The offset on the y-axis
     * @return {@code this}
     */
    public @NotNull AffineTransform translate(final float offsetX, final float offsetY)
    {
        this.matrix.translate(offsetX, offsetY);
        return this;
    }

    /**
     * Rotates the transform the given angle.
     * @param angleRadians The angle of the rotation in radians
     * @return {@code this}
     */
    public @NotNull AffineTransform rotate(final float angleRadians)
    {
        this.matrix.rotate(angleRadians);
        return this;
    }

    /**
     * Rotates the transform the given angle around the given coordinates.
     * @param angleRadians The angle of the rotation in radians
     * @param pivotX       The x coordinate to rotate around
     * @param pivotY       The y coordinate to rotate around
     * @return {@code this}
     */
    public @NotNull AffineTransform rotatePivot(final float angleRadians, final float pivotX, final float pivotY)
    {
        this.matrix.rotateAbout(angleRadians, pivotX, pivotY);
        return this;
    }

    /**
     * Scales the transform the given amount.
     * @param x The scale on the x-axis
     * @param y The scale on the y-axis
     * @return {@code this}
     */
    public @NotNull AffineTransform scale(final float x, final float y)
    {
        this.matrix.scale(x, y);
        return this;
    }

    /**
     * Scales the transform the given amount.
     * @param xy The scale on the x and y-axis
     * @return {@code this}
     */
    public @NotNull AffineTransform scale(final float xy)
    {
        this.matrix.scale(xy);
        return this;
    }
}

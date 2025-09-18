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
package xyz.lumialights.novia.api.gui.geometry;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;



//**********************************************************************************************************************
public enum Alignment
    implements StringIdentifiable
{
    LEFT(1),
    CENTRE(2),
    RIGHT(4),
    TOP(8),
    MIDDLE(16),
    BOTTOM(32),
    
    TOP_LEFT     (TOP.flags    | LEFT.flags),
    TOP_CENTRE   (TOP.flags    | CENTRE.flags),
    TOP_RIGHT    (TOP.flags    | RIGHT.flags),
    MIDDLE_LEFT  (MIDDLE.flags | LEFT.flags),
    MIDDLE_CENTRE(MIDDLE.flags | CENTRE.flags),
    MIDDLE_RIGHT (MIDDLE.flags | RIGHT.flags),
    BOTTOM_LEFT  (BOTTOM.flags | LEFT.flags),
    BOTTOM_CENTRE(BOTTOM.flags | CENTRE.flags),
    BOTTOM_RIGHT (BOTTOM.flags | RIGHT.flags),
    ;
    
    //******************************************************************************************************************
    public static final Codec<Alignment> CODEC = StringIdentifiable.createCodec(Alignment::values);
    
    //******************************************************************************************************************
    public final int flags;
    
    //******************************************************************************************************************
    Alignment(final int flags)
    {
        this.flags = flags;
    }
    
    //==================================================================================================================
    public boolean has(@NotNull final Alignment alignment)
    {
        return ((this.flags & alignment.flags) == alignment.flags);
    }
    
    //==================================================================================================================
    /**
     * Aligns the given target bounds so that it is aligned to the given container area.
     *
     * @param containerX      The position of the area that the target area should be aligned to on the x-axis
     * @param containerY      The position of the area that the target area should be aligned to on the y-axis
     * @param containerWidth  The width of the area that the target area should be aligned to
     * @param containerHeight The height of the area that the target area should be aligned to
     * @param targetWidth     The width of the to be aligned area
     * @param targetHeight    The height of the to be aligned area
     * @return A new {@link Rectangle} aligned to the given container rect
     */
    public @NotNull Rectangle align(final @NotNull Number containerX,
                                    final @NotNull Number containerY,
                                    final @NotNull Number containerWidth,
                                    final @NotNull Number containerHeight,
                                    final @NotNull Number targetWidth,
                                    final @NotNull Number targetHeight)
    {
        int x = containerX.intValue();
        int y = containerY.intValue();
        
        if (this.has(Alignment.CENTRE))
        {
            x += (int) Math.round((containerWidth.intValue() - targetWidth.intValue()) * 0.5);
        }
        else if (this.has(Alignment.RIGHT))
        {
            x += (containerWidth.intValue() - targetWidth.intValue());
        }
        
        if (this.has(Alignment.MIDDLE))
        {
            y += (int) Math.round((containerHeight.intValue() - targetHeight.intValue()) * 0.5);
        }
        else if (this.has(Alignment.BOTTOM))
        {
            y += (containerHeight.intValue() - targetHeight.intValue());
        }
        
        return new Rectangle(x, y, targetWidth, targetHeight);
    }
    
    /**
     * Aligns {@code target} so that it is aligned relatively to the given container bounds.
     *
     * @param containerX      The position of the area that the target area should be aligned to on the x-axis
     * @param containerY      The position of the area that the target area should be aligned to on the y-axis
     * @param containerWidth  The width of the area that the target area should be aligned to
     * @param containerHeight The height of the area that the target area should be aligned to
     * @param target          The {@link Rectangle} to be aligned
     * @return A new {@link Rectangle} aligned to the given container rect
     */
    public @NotNull Rectangle align(final @NotNull Number    containerX,
                                    final @NotNull Number    containerY,
                                    final @NotNull Number    containerWidth,
                                    final @NotNull Number    containerHeight,
                                    final @NotNull Rectangle target)
    {
        return target.apply((x, y, w, h) -> this.align(containerX, containerY, containerWidth, containerHeight, w, h));
    }
    
    /**
     * Aligns the target area so that it is aligned relatively to {@code container} based on the given alignment flags.
     *
     * @param container    The containing area that the target area should be aligned to
     * @param targetWidth  The width of the to be aligned area
     * @param targetHeight The height of the to be aligned area
     * @return A new {@link Rectangle} aligned to the given container rect
     */
    public @NotNull Rectangle align(final @NotNull Rectangle container, final @NotNull Number targetWidth,
                                    final @NotNull Number targetHeight)
    {
        return container.apply((x, y, w, h) -> this.align(x, y, w, h, targetWidth, targetHeight));
    }
    
    /**
     * Aligns {@code target} so that it is aligned relatively to {@code container} based on the given alignment flags.
     *
     * @param container The {@link Rectangle} to align to
     * @param target    The {@link Rectangle} to be aligned
     * @return A new {@link Rectangle} aligned to the given container rect
     */
    public @NotNull Rectangle align(final @NotNull Rectangle container, final @NotNull Rectangle target)
    {
        return container.apply((cx, cy, cw, ch) ->
            target.apply((tx, ty, tw, th) ->
                this.align(cx, cy, cw, ch, tw, th)));
    }
    
    //==================================================================================================================
    @Override public String asString() { return this.name().toLowerCase(); }
}

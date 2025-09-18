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
package xyz.lumialights.novia.api.gui.component.provided;

import com.mojang.serialization.Codec;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.serialisation.IValueConvertible;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.RefUtils;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.component.IComponentNavigator;
import xyz.lumialights.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumialights.novia.api.gui.geometry.Point;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.util.*;
import java.util.stream.Stream;


//**********************************************************************************************************************
/**
 * A slider like component, which can be used to modify the position of other components upon scrolling.
 * <p>
 * Scroll bars provide input listeners for drag events where the mouse can pull the thumb around to scroll as well as
 * mouse wheel listeners that allow scrolling upon rotating the wheel when it is hovered.
 * <p>
 * By default, this component does not participate in the focus chain of the component hierarchy.
 * <p>
 * This is a stateful GUI component; the value it contains determines the scroll offset of the specified overflow area.
 * This value can be converted to and from {@link Value}.
 * (for more details, see {@link #getValue()}, {@link #setValue(Value)} and {@link IValueConvertible})
 */
public class NVScrollbar
    extends StatefulGuiComponent<NVScrollbar>
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void nvScrollbarDrawBackground(@NotNull Canvas canvas, @NotNull NVScrollbar scrollbar,
                                       @NotNull Rectangle trackBounds);
        void nvScrollbarDrawThumb(@NotNull Canvas canvas, @NotNull NVScrollbar scrollbar, @NotNull Rectangle bounds);
    }
    
    /**
     * Represents the scroll space handler.
     * @param view    The minimised size (in pixels) that is being viewed
     * @param content The content size (in pixels) of the area that can be scrolled
     */
    public record ScrollSpace(int view, int content)
    {
        //**************************************************************************************************************
        public ScrollSpace
        {
            if (view < 0)
            {
                throw new IllegalArgumentException("view space must be positive");
            }
            
            if (content < 0)
            {
                throw new IllegalArgumentException("content space must be positive");
            }
        }
        
        //==============================================================================================================
        public int getOverflow() { return Math.max(0, (this.content - this.view)); }
        
        public int getThumbSize(final int totalSize, final int minThumbSize)
        {
            if (totalSize == 0)
            {
                return minThumbSize;
            }
            
            if (this.content == 0)
            {
                return totalSize;
            }
            
            return Math.clamp(
                (int) Math.round((double) this.view * totalSize / this.content),
                Math.min(minThumbSize, (totalSize - 1)),
                totalSize);
        }
        
        //==============================================================================================================
        public boolean isEmpty() { return (this.content == 0); }
        
        public boolean isOverflowing() { return (this.content > this.view); }
    }
    
    //******************************************************************************************************************
    /** See {@link NVScrollbar#delta}. */
    public static final int DEFAULT_DELTA = 10;
    
    /** See {@link NVScrollbar#vertical}. */
    public static final boolean DEFAULT_IS_VERTICAL = true;
    
    /** See {@link NVScrollbar#minThumbSize}. */
    public static final int DEFAULT_MIN_THUMB_SIZE = 32;
    
    //==================================================================================================================
    /** The texture used for the scroll bar thumb. */
    public static final Identifier TEXTURE_SCROLLBAR_THUMB = Identifier.ofVanilla("widget/scroller");
    
    /** The texture used for the background of the scroll bar. */
	public static final Identifier TEXTURE_SCROLLBAR_BACKGROUND = Identifier.ofVanilla("widget/scroller_background");
    
    public static final int PULL_START_DELAY_MS = 500;
    public static final int PULL_APPLY_DELAY_MS = 100;
    
    //******************************************************************************************************************
    /**
     * Describes the scroll delta used to scroll with the mouse wheel or the keyboard.
     * <p>
     * The scroll delta is a value that determines how much the scroll bar thumb will jump upon scrolling
     * with the mouse wheel or the keyboard, based on the given height of the content to scroll.
     * <p>
     * A value of 0 means no scrolling at all with these devices.
     */
    public final GuiProperty.NonNull<Integer> delta;
    
    /**
     * Describes the orientation of the scrollbar, if {@code true} this is a vertical scrollbar (top to bottom),
     * or {@code false} if horizontal (left to right).
     */
    public final GuiProperty.NonNull<Boolean> vertical;
    
    /**
     * Describes the minimum size the thumb should have if the scroll space gets too big. This value can only take
     * positive values starting at 1.
     * <p>
     * It is best to leave this at the specified default; however, for exceptional cases it is provided.
     */
    public final GuiProperty.NonNull<Integer> minThumbSize;
    
    //==================================================================================================================
    private final Rectangle trackBounds = new Rectangle();
    private final Rectangle thumbBounds = new Rectangle();
    
    private int         offset   = 0;
    private int         overflow = 0;
    private boolean     dragging = false;
    private boolean     using    = false;
    private boolean     canPull  = false;
    private long        startMs  = 0;
    private long        applyMs  = 0;
    private ScrollSpace space;
    
    //******************************************************************************************************************
    /**
     * Constructs a new vertical scroll bar.
     * @param scrollSpace The initial {@link ScrollSpace}
     * @param message The scrollbar message
     */
    public NVScrollbar(final @NotNull ScrollSpace scrollSpace, final @NotNull Text message)
    {
        super(message);
        
        this.vertical     = GuiProperty.nonNull(NVScrollbar.DEFAULT_IS_VERTICAL, (val -> this.resized()));
        this.delta        = GuiProperty.nonNull(NVScrollbar.DEFAULT_DELTA);
        this.minThumbSize = GuiProperty.nonNullChecked(NVScrollbar.DEFAULT_MIN_THUMB_SIZE,
                                                       (val -> this.updateThumbBounds()), RefUtils.greaterThan(0));
        
        this.space = Objects.requireNonNull(scrollSpace, "scroll space must not be null");
    }
    
    /**
     * Constructs a new vertical scroll bar.
     * @param scrollSpace The initial {@link ScrollSpace}
     */
    public NVScrollbar(final @NotNull ScrollSpace scrollSpace) { this(scrollSpace, ScreenTexts.EMPTY); }
    
    /** Constructs a new vertical scrollbar with an empty scroll space. */
    public NVScrollbar() { this(new ScrollSpace(0, 0), ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    /**
     * Gets the current scroll offset as a {@link Value} object
     * (for more details see {@link #setOffset(int)}).
     * @return The current scroll value
     */
    @Override public @NotNull Value getValue() { return new Value(this.offset); }
    
    /**
     * Gets the current offset from the target's origin point.
     * <p>
     * If the scroll bar's thumb is all the way on the top this will return 0, if it is all the way on the bottom
     * this will return {@link #getOverflow()}.
     *
     * @return The offset of the overflow
     */
    public int getOffset() { return this.offset; }
    
    /**
     * Gets the maximum overflow the scroll bar can scroll to.
     * @return The maximum overflow
     */
    public int getOverflow() { return this.overflow; }
    
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    //------------------------------------------------------------------------------------------------------------------
    /** Track start position. */
    protected int getTrackStart() { return 0; }
    
    /** Track end position. */
    protected int getTrackEnd() { return (this.vertical.get() ? this.getHeight() : this.getWidth()); }
    
    /** Track size. */
    protected int getTrackSize() { return Math.max(0, (this.getTrackEnd() - this.getTrackStart())); }
    
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SCROLLBAR_DELTA,
                this.delta,
                Codec.INT),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SCROLLBAR_VERTICAL,
                this.vertical,
                Codec.BOOL),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SCROLLBAR_MIN_THUMB_SIZE,
                this.minThumbSize,
                Codec.INT)
        );
    }
    
    //==================================================================================================================
    /**
     * Sets the scroll offset from the given {@link Value} object.
     * <p>
     * If the {@link Value} object is not a number, or the scroll bar is already at the given offset,
     * this will do nothing.
     *
     * @param value The scroll offset value
     */
    @Override
    public void setValue(final @NotNull Value value)
    {
        if (!value.isNumber())
        {
            return;
        }
        
        this.setOffset(value.getNumber().intValue());
    }
    
    /**
     * Sets the scroll offset of the target on this scrollbar.
     * The value is between 0, and the {@link #getOverflow()}; everything above or below will be clamped.
     *
     * @param value The scroll offset
     */
    public void setOffset(int value)
    {
        value = Math.clamp(value, 0, this.overflow);
        
        if (value != this.offset)
        {
            this.offset = value;
            this.updateThumbBounds();
            this.notifyChangeListeners();
        }
    }
    
    /**
     * Updates the overflow based on the given view and content space, where view space is the container of the content
     * and content space is the space of the content.
     * <p>
     * This will not be automatically determined upon resizing, the developer is responsible to update this manually.
     *
     * @param viewSpace    The maximum size of the area that is visible of the clipped content
     * @param contentSpace The maximum size of the content
     */
    public void setOverflow(final int viewSpace, final int contentSpace)
    {
        this.setOverflow(new ScrollSpace(viewSpace, contentSpace));
    }
    
    /**
     * Updates the overflow based on the given view and content space, where view space is the container of the content
     * and content space is the space of the content.
     * <p>
     * This will not be automatically determined upon resizing, the developer is responsible to update this manually.
     *
     * @param scrollSpace The {@link ScrollSpace}
     */
    public void setOverflow(final @NotNull ScrollSpace scrollSpace)
    {
        if (scrollSpace.equals(this.space))
        {
            return;
        }
        
        this.space    = scrollSpace;
        this.overflow = scrollSpace.getOverflow();
        
        if (this.offset > this.overflow)
        {
            this.setOffset(this.overflow);
        }
        else
        {
            this.updateThumbBounds();
        }
    }
    
    //==================================================================================================================
    /**
     * Moves the offset by the given offset amount in pixels.
     * If {@code offset} is negative, this will scroll backwards.
     *
     * @param offset The offset to scroll
     */
    public void move(final int offset) { this.setOffset(this.offset + offset); }
    
    /**
     * Moves the offset by the given number of {@link #delta} applied {@code steps} times.
     * If {@code steps} is negative, this will scroll backwards.
     *
     * @param steps The number of times delta should be applied to the current scroll offset
     */
    public void moveSteps(final int steps) { this.move(this.delta.get() * steps); }
    
    /**
     * Moves the offset by the given page amount, by that means, {@code pages} times the view size.
     * If {@code pages} is negative, this will scroll backwards.
     *
     * @param pages The number of pages to scroll
     */
    public void movePages(final int pages) { this.move(this.space.view * pages); }
    
    /** Scrolls all the way to the start. */
    public void moveToStart() { this.setOffset(0); }
    
    /** Scrolls all the way to the end. */
    public void moveToEnd() { this.setOffset(this.overflow); }
    
    //==================================================================================================================
    @Override
    protected boolean onMouseDown(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            this.using = false;
            return false;
        }
        
        this.canPull = false;
        this.using   = true;
        
        final int mouse_pos;
        final int thumb_start;
        final int thumb_end;
        
        if (this.vertical.get())
        {
            mouse_pos   = (int) e.localMouseY();
            thumb_start = this.thumbBounds.y();
            thumb_end   = this.thumbBounds.getBottom();
        }
        else
        {
            mouse_pos   = (int) e.localMouseX();
            thumb_start = this.thumbBounds.x();
            thumb_end   = this.thumbBounds.getRight();
        }
        
        if (mouse_pos < thumb_start)
        {
            this.movePages(-1);
            this.canPull  = true;
            this.startMs = Util.getMeasuringTimeMs();
            this.applyMs = Util.getMeasuringTimeMs();
        }
        else if (mouse_pos > thumb_end)
        {
            this.movePages(1);
            this.canPull  = true;
            this.startMs = Util.getMeasuringTimeMs();
            this.applyMs = Util.getMeasuringTimeMs();
        }
        else
        {
            this.dragging = true;
        }
        
        return true;
    }
    
    @Override
    protected boolean onMouseScroll(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.moveSteps((int) -e.deltaY);
        return true;
    }
    
    @Override
    protected boolean onMouseDrag(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            this.using    = false;
            this.dragging = false;
            
            return false;
        }
        
        if (!this.dragging)
        {
            return this.using;
        }
        
        final Point local_mouse_pos = e.localMousePos();
        
        final int    m_pos;
        final double m_delta;
        final int    m_area;
        
        if (this.vertical.get())
        {
            m_pos   = local_mouse_pos.y();
            m_delta = e.deltaY;
            m_area  = (this.getTrackSize() - this.thumbBounds.height());
        }
        else
        {
            m_pos   = local_mouse_pos.x();
            m_delta = e.deltaX;
            m_area  = (this.getTrackSize() - this.thumbBounds.width());
        }
        
        if (m_pos < this.getTrackStart())
        {
            this.moveToStart();
        }
        else if (m_pos > this.getTrackEnd())
        {
            this.moveToEnd();
        }
        else if (m_area > 0)
        {
            this.move((int) Math.round(m_delta * this.overflow / m_area));
        }
        
        return true;
    }
    
    @Override
    protected boolean onMouseUp(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.dragging = false;
        this.canPull  = false;
        
        return true;
    }
    
    //==================================================================================================================
    @Override
    protected void onDeltaTick(final @NotNull Point mousePos, final float delta)
    {
        if (!this.canPull)
        {
            return;
        }
        
        final Point local_mouse_pos = this.toRelativePos(mousePos);
        
        if (this.thumbBounds.contains(local_mouse_pos))
        {
            this.canPull = false;
            return;
        }
        
        final long now = Util.getMeasuringTimeMs();
        
        if (
            (now - this.startMs) > NVScrollbar.PULL_START_DELAY_MS
            && (now - this.applyMs) > NVScrollbar.PULL_APPLY_DELAY_MS
        )
        {
            final int mouse_pos;
            final int thumb_start;
            final int thumb_end;
            
            if (this.vertical.get())
            {
                mouse_pos   = local_mouse_pos.y();
                thumb_start = this.thumbBounds.y();
                thumb_end   = this.thumbBounds.getBottom();
            }
            else
            {
                mouse_pos   = local_mouse_pos.x();
                thumb_start = this.thumbBounds.x();
                thumb_end   = this.thumbBounds.getRight();
            }
            
            if (mouse_pos < thumb_start)
            {
                this.movePages(-1);
            }
            else if (mouse_pos > thumb_end)
            {
                this.movePages(1);
            }
            
            this.applyMs = now;
        }
    }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvScrollbarDrawBackground(canvas, this, new Rectangle(this.trackBounds));
        template.nvScrollbarDrawThumb(canvas, this, new Rectangle(this.thumbBounds));
    }
    
    //==================================================================================================================
    @Override
    protected void resized()
    {
        if (this.vertical.get())
        {
            this.trackBounds.setBounds(0, this.getTrackStart(), this.getWidth(), this.getTrackSize());
        }
        else
        {
            this.trackBounds.setBounds(this.getTrackStart(), 0, this.getTrackSize(), this.getHeight());
        }
        
        this.updateThumbBounds();
    }
    
    //==================================================================================================================
    private void updateThumbBounds()
    {
        final int track_size = this.getTrackSize();
        final int thumb_size = (!this.space.isEmpty()
            ? this.space.getThumbSize(track_size, this.minThumbSize.get())
            : track_size);
        
        int thumb_pos = this.getTrackStart();
        
        if (this.space.isOverflowing())
        {
            thumb_pos += (int) Math.round((double) this.offset * (track_size - thumb_size) / this.overflow);
        }
        
        if (this.vertical.get())
        {
            this.thumbBounds.setBounds(0, thumb_pos, this.getWidth(), thumb_size);
        }
        else
        {
            this.thumbBounds.setBounds(thumb_pos, 0, thumb_size, this.getHeight());
        }
    }
}

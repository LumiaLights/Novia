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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xyz.lumialights.novia.api.core.serialisation.IValueConvertible;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.NormalisedRange;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.geometry.Point;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.text.DecimalFormat;
import java.util.function.Supplier;
import java.util.stream.Stream;



//**********************************************************************************************************************
/**
 * An extension to {@link NVTextBox} which can be used to display and edit numeric values specifically.
 * <p>
 * This special text box comes with two {@link NVSimpleButton}, used to decrement and increment the current numeric
 * value and a {@link NVTextBox} that displays the current value.
 * <p>
 * This is a stateful GUI component, the number it contains can be converted to and from {@link Value}.
 * (for more details, see {@link #getValue()}, {@link #setValue(Value)} and {@link IValueConvertible})
 */
public class NVNumericBox
    extends StatefulGuiComponent<NVNumericBox>
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void nvNumericBoxDrawArrowButton(@NotNull Canvas canvas, @NotNull NVNumericBox numericBox,
                                         @NotNull NVAbstractButton<?> button, @NotNull String text);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private class ArrowButton
        extends NVAbstractButton<ArrowButton>
    {
        //**************************************************************************************************************
        private final String text;
        
        //**************************************************************************************************************
        public ArrowButton(final @NotNull Runnable action, final @NotNull String text)
        {
            super((btt -> action.run()), Text.of(text));
            
            this.text = text;
            
            this.setWantsFocus(false);
            this.setPinned(true);
        }
        
        //==============================================================================================================
        @Override
        protected void draw(final @NotNull Canvas canvas)
        {
            canvas.getTemplate().nvNumericBoxDrawArrowButton(canvas, NVNumericBox.this, this, this.text);
        }
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_ARROW          = ColourId.reserve();
    public static final ColourId COLOUR_ARROW_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /** See {@link NVNumericBox#range}. */
    public static final NormalisedRange DEFAULT_RANGE = NormalisedRange.FULL_RANGE;
    
    /** See {@link NVNumericBox#numberFormat}. */
    public static final Supplier<DecimalFormat> DEFAULT_FORMAT_SUPPLIER = (() ->
    {
        final DecimalFormat format = new DecimalFormat("0");
        format.setMaximumFractionDigits(300);
        return format;
    });
    
    //------------------------------------------------------------------------------------------------------------------
    private static final int COUNT_START_DELAY_MS = 500;
    private static final int COUNT_APPLY_DELAY_MS = 100;
    
    //******************************************************************************************************************
    /**
     * Describes the range the box can step between.
     * @see NormalisedRange
     */
    public final GuiProperty.NonNull<NormalisedRange> range;
    
    /**
     * Describes the formatting of the number as text in the text box.
     * @see DecimalFormat
     */
    public final GuiProperty.NonNull<DecimalFormat> numberFormat;
    
    //------------------------------------------------------------------------------------------------------------------
    private final NVTextBox   textBox;
    private final ArrowButton upButton;
    private final ArrowButton downButton;
    
    private int    countMode = 0;
    private long   startMs   = 0;
    private long   applyMs   = 0;
    private double value;
    
    //******************************************************************************************************************
    /**
     * Constructs a new numeric text box.
     * @param value   The initial value for the numeric box
     * @param message The component message
     */
    public NVNumericBox(final double value, final @NotNull Text message)
    {
        super(message);
        
        this.range        = GuiProperty.nonNull(NVNumericBox.DEFAULT_RANGE,                 this::updateRange);
        this.numberFormat = GuiProperty.nonNull(NVNumericBox.DEFAULT_FORMAT_SUPPLIER.get(), this::updateText);
        
        this.upButton   = this.addChild(new ArrowButton(this::countUp,   "▴"));
        this.downButton = this.addChild(new ArrowButton(this::countDown, "▾"));
        this.value      = this.range.get().clamp(value);
        
        this.textBox = this.addChild(new NVTextBox());
        this.textBox.predicateStopsInput.set(true);
        this.textBox.textPredicate.set(NumberUtils::isParsable);
        this.textBox.addChangeListener(this::textboxTextChanged);
        
        this.setMonitorChildren(true);
        this.updateText(this.numberFormat.get());
    }
    
    /**
     * Constructs a new numeric text box with an empty message.
     * @param value The initial value for the numeric box
     */
    public NVNumericBox(final double value) { this(value, ScreenTexts.EMPTY); }
    
    /** Constructs a new numeric text box with an initial value of 0. */
    public NVNumericBox() { this(0, ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    /**
     * Gets the last valid number of this text box as a {@link Value} object.
     * @return The number {@link Value}
     */
    @Override public @NotNull Value getValue() { return new Value(this.value); }
    
    /**
     * Gets the last valid number of this number box.
     * @return The number
     */
    public double getDouble() { return this.value; }
    
    /**
     * Gets the textual representation of the number inside the numeric text box.
     * @return The value string
     */
    public @NotNull String getText() { return this.textBox.getText(); }
    
    /**
     * Gets the internal text box, this should only be used for styling and templating the text box.
     * @return The {@link NVTextBox}
     */
    public @NotNull NVTextBox getTextBox() { return this.textBox; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.NUMERIC_BOX_RANGE,
                this.range,
                NormalisedRange.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.NUMERIC_BOX_FORMAT,
                this.numberFormat,
                Codec.STRING.xmap(DecimalFormat::new, DecimalFormat::toPattern))
        );
    }
    
    //==================================================================================================================
    /**
     * Sets the value of this box to the new value.
     * <p>
     * If the value is outside the range, it will be adjusted to fit.
     *
     * @param value The new value to set
     */
    public void setValue(double value)
    {
        value = this.range.get().clamp(value);
        
        if (this.value != value)
        {
            this.value = value;
            
            this.updateText(this.numberFormat.get());
            this.notifyChangeListeners();
        }
    }
    
    /**
     * Sets the number content from the given {@link Value} object if it is a number, otherwise does nothing.
     * @param value The new {@link Value}
     */
    @Override
    public void setValue(final @NotNull Value value)
    {
        if (!value.isNumber())
        {
            return;
        }
        
        this.setValue(value.getNumber().doubleValue());
    }
    
    public void setText(final @NotNull String text) { this.textBox.setText(text); }
    
    //==================================================================================================================
    private void countUp  () { this.setValue(this.value + this.range.get().step()); }
    private void countDown() { this.setValue(this.value - this.range.get().step()); }
    
    //==================================================================================================================
    @Override
    protected void resized()
    {
        final int       size        = (this.getHeight() / 2);
        final Rectangle local       = this.getLocalBounds();
        final Rectangle button_rect = local
            .withWidth(size)
            .align(Alignment.MIDDLE_RIGHT, local);
        
        this.upButton  .setBounds(button_rect.removeTop(size));
        this.downButton.setBounds(button_rect);
        this.textBox   .setBounds(local);
    }
    
    //==================================================================================================================
    @Override
    protected boolean onMouseScroll(final @NotNull MouseEvent e)
    {
        if (!this.isFocused() || !this.isActive())
        {
            return false;
        }
        
        if (e.deltaY > 0)
        {
            this.countUp();
            return true;
        }
        else if (e.deltaY < 0)
        {
            this.countDown();
            return true;
        }
        
        return false;
    }
    
    @Override
    protected boolean onMouseDown(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.countMode = 0;
        
        if (this.upButton.isDragging() || this.downButton.isDragging())
        {
            this.countMode = (this.upButton.isDragging() ? 1 : -1);
            this.startMs   = Util.getMeasuringTimeMs();
            this.applyMs   = Util.getMeasuringTimeMs();
            
            return true;
        }
        
        return super.onMouseDown(e);
    }
    
    @Override
    protected boolean onMouseUp(@NotNull MouseEvent e)
    {
        e.enableCursor();
        this.countMode = 0;
        
        return super.onMouseUp(e);
    }
    
    @Override
    protected boolean onMouseDrag(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        if (this.countMode != 0)
        {
            this.countMode = 0;
            e.disableCursor();
        }
        
        if (this.upButton.isDragging() || this.downButton.isDragging())
        {
            if      (e.deltaY < 0) this.countUp();
            else if (e.deltaY > 0) this.countDown();
            
            return true;
        }
        
        return super.onMouseDrag(e);
    }
    
    @Override
    protected boolean onKeyDown(final @NotNull KeyEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        if (Screen.hasControlDown())
        {
            switch (e.input)
            {
                case GLFW.GLFW_KEY_DOWN:
                    this.countDown();
                    return true;
                
                case GLFW.GLFW_KEY_UP:
                    this.countUp();
                    return true;
            }
        }
        
        return super.onKeyDown(e);
    }
    
    //==================================================================================================================
    protected void textboxTextChanged(final @NotNull NVTextBox box)
    {
        this.value = NumberUtils.toDouble(box.getText());
        this.notifyChangeListeners();
    }
    
    //==================================================================================================================
    @Override
    protected void onDeltaTick(final @NotNull Point mousePos, final float delta)
    {
        if (this.countMode == 0)
        {
            return;
        }
        
        final long now = Util.getMeasuringTimeMs();
        
        if (
            (now - this.startMs) > NVNumericBox.COUNT_START_DELAY_MS
            && (now - this.applyMs) > NVNumericBox.COUNT_APPLY_DELAY_MS
        )
        {
            if (this.countMode > 0) this.countUp();
            else                    this.countDown();
            
            this.applyMs = now;
        }
    }
    
    //==================================================================================================================
    private void updateRange(final @NotNull NormalisedRange range) { this.setValue(this.value); }
    
    private void updateText(final @NotNull DecimalFormat format)
    {
        this.textBox.mute();
        this.textBox.setText(format.format(this.value));
        this.textBox.unmute();
    }
}

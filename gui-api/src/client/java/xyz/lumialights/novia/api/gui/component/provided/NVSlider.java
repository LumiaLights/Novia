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

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.lumialights.novia.api.GuiApiLang;
import xyz.lumialights.novia.api.core.serialisation.IValueConvertible;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.NormalisedRange;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.component.*;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.text.DecimalFormat;
import java.util.function.Function;
import java.util.stream.Stream;



//**********************************************************************************************************************
/**
 * A slider implementation modelled after Minecraft's {@link SliderWidget}, adjusted to the Novia GUI system.
 * <p>
 * A slider is a component, which can be used to gradually interpolate a number range, where the value will be between
 * the given minimum and maximum values (both inclusive).
 * <p>
 * This is a stateful GUI component, the value it contains can be converted to and from {@link Value}.
 * (for more details, see {@link #getValue()}, {@link #setValue(Value)} and {@link IValueConvertible})
 */
public class NVSlider
    extends StatefulGuiComponent<NVSlider>
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void nvSliderDrawBackground(@NotNull Canvas canvas, @NotNull NVSlider slider);
        
        void nvSliderDrawThumb(@NotNull Canvas canvas, @NotNull NVSlider slider, @NotNull Rectangle thumbBounds);
        
        void nvSliderDrawText(@NotNull Canvas canvas, @NotNull NVSlider slider, @NotNull Text text);
    }
    
    //******************************************************************************************************************
    /** The colour of the slider text when the component is active. */
    public static final ColourId COLOUR_TEXT = ColourId.reserve();
    
    /** The colour of the slider text when the component is inactive. */
    public static final ColourId COLOUR_TEXT_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /** See {@link NVSlider#displayTextProvider}. */
    public static final Function<Double, Text> DEFAULT_DISPLAY_TEXT_PROVIDER;
    
    /**
     * Ranges from 0 to 100 with a step size of {@code 1.0}.
     * @see NVSlider#range
     */
    public static final NormalisedRange DEFAULT_RANGE;
    
    //==================================================================================================================
    /** The textures used for the slider's background (unfocused). */
    public static final Identifier TEXTURE;
    
    /** The textures used for the slider's background (focused). */
    public static final Identifier TEXTURE_HIGHLIGHTED;
    
    /** The textures used for the slider's handle (unfocused). */
    public static final Identifier TEXTURE_HANDLE;
    
    /** The textures used for the slider's handle (focused). */
    public static final Identifier TEXTURE_HANDLE_HIGHLIGHT;
    
    //==================================================================================================================
    static
    {
        TEXTURE                  = Identifier.ofVanilla("widget/slider");
        TEXTURE_HIGHLIGHTED      = Identifier.ofVanilla("widget/slider_highlighted");
        TEXTURE_HANDLE           = Identifier.ofVanilla("widget/slider_handle");
        TEXTURE_HANDLE_HIGHLIGHT = Identifier.ofVanilla("widget/slider_handle_highlighted");
        
        final DecimalFormat format = new DecimalFormat("0");
        format.setMaximumFractionDigits(300);
        DEFAULT_DISPLAY_TEXT_PROVIDER = (val -> GuiApiLang.GUI_COMPONENT_SLIDER_MESSAGE.withArgs(format.format(val)));
        
        DEFAULT_RANGE = new NormalisedRange(0, 100);
    }
    
    //******************************************************************************************************************
    /**
     * Describes the range the slider can slide between.
     * @see NormalisedRange
     */
    public final GuiProperty.NonNull<NormalisedRange> range;
    
    /**
     * The text provider that provides the text to be displayed on the slider,
     * where the given parameter is the value currently held by the slider.
     */
    public final GuiProperty<Function<Double, Text>> displayTextProvider;
    
    //------------------------------------------------------------------------------------------------------------------
    private final Rectangle thumbBounds = new Rectangle();
    
    private double normalised;
    private double value;
    private Text   text;
    
    //******************************************************************************************************************
    /**
     * Constructs a new slider with the given initial value.
     * @param value   The initial slider value (will be clamped to the slider range)
     * @param message The component message
     */
    public NVSlider(final double value, final @NotNull Text message)
    {
        super(message);
        
        this.range               = GuiProperty.nonNull(NVSlider.DEFAULT_RANGE,                 this::updateRange);
        this.displayTextProvider = GuiProperty.nonNull(NVSlider.DEFAULT_DISPLAY_TEXT_PROVIDER, this::updateMessage);
        
        this.value      = this.range.get().clamp(value);
        this.normalised = this.range.get().normalise(this.value);
        
        this.setWantsFocus(true);
        this.updateMessage();
    }
    
    /**
     * Constructs a new slider with the given initial value.
     * @param value The initial slider value (will be clamped to the slider range)
     */
    public NVSlider(final double value) { this(value, ScreenTexts.EMPTY); }
    
    /** Constructs a new slider with value {@link NormalisedRange#min()} of {@link NVSlider#DEFAULT_RANGE}. */
    public NVSlider() { this(NVSlider.DEFAULT_RANGE.min(), ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    /**
     * Gets the current slider value as a {@code double} {@link Value} object
     * (for more details see {@link #setValue(double)}).
     * @return The current slider value
     */
    @Override public @NotNull Value getValue() { return new Value(this.value); }
    
    public double getValueAsDouble() { return this.value; }
    
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    protected @NotNull MutableText getNarrationMessage()
    {
        return Text.translatable("gui.narrate.slider", this.getMessage());
    }
    
    /** Track start X pos. */
    protected int getTrackStart() { return 0; }
    
    /** Track end X pos. */
    protected int getTrackEnd() { return this.getWidth(); }
    
    /** Track length. */
    protected int getTrackLength() { return (this.getTrackEnd() - this.getTrackStart()); }
    
    /** Thumb width. */
    protected int getThumbSize() { return 8; }
    
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SLIDER_RANGE,
                this.range,
                NormalisedRange.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SLIDER_TEXT_PROVIDER,
                this.displayTextProvider));
    }
    
    //==================================================================================================================
    /**
     * Sets the slider value from the given {@link Value} object (for more information see {@link #setValue(double)}).
     * <p>
     * If the {@link Value} object is not a number, this will do nothing.
     *
     * @param value The slider value value
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
    
    /**
     * Sets the slider's current value.
     * <p>
     * The value is any valid number defined for the range of this slider's given {@link NormalisedRange}.
     * If the new value is outside the range, it will be automatically adjusted to the nearest valid value.
     *
     * @param value The new slider value
     */
    public void setValue(final double value)
    {
        if (this.setValueInternal(value))
        {
            this.notifyChangeListeners();
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private boolean setValueNormalised(final double normalised)
    {
        if (this.normalised != normalised)
        {
            this.normalised = Math.clamp(normalised, 0.0f, 1.0f);
            
            final NormalisedRange range = this.range.get();
            final double          old   = this.value;
            
            this.value = range.snap(range.denormalise(normalised));
            
            if (this.value != old)
            {
                this.updateThumbPos();
                this.updateMessage();
                
                return true;
            }
        }
        
        return false;
    }
    
    private boolean setValueInternal(final double value)
    {
        final NormalisedRange range   = this.range.get();
        final double          snapped = range.snap(value);
        
        if (this.value != snapped)
        {
            this.value      = snapped;
            this.normalised = range.normalise(snapped);
            
            this.updateThumbPos();
            this.updateMessage();
            
            return true;
        }
        
        return false;
    }
    
    private boolean setValueFromMouse(final @NotNull MouseEvent e)
    {
        final int thumb_size = this.getThumbSize();
        return this.setValueNormalised(
            (e.localMouseX() - (this.getTrackStart() + (thumb_size / 2.0)))
            / (double) (this.getTrackLength() - thumb_size)
        );
    }
    
    //==================================================================================================================
    @Override
    protected void resized()
    {
        this.thumbBounds.setBounds(this
            .getLocalBounds()
            .setWidth(this.getThumbSize()));
        this.updateThumbPos();
    }
    
    //==================================================================================================================
    @Override
    protected boolean onMouseDown(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.setValueFromMouse(e);
        return true;
    }
    
    @Override
    protected boolean onMouseUp(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        ComponentUtil.playClickSound();
        this.notifyChangeListeners();
        
        return true;
    }
    
    @Override
    protected boolean onMouseDrag(final @NotNull MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.setValueFromMouse(e);
        return true;
    }
    
    @Override
    protected boolean onKeyDown(final @NotNull KeyEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        final boolean is_left_arrow = (e.input == GLFW.GLFW_KEY_LEFT);
        
        if (is_left_arrow || e.input == GLFW.GLFW_KEY_RIGHT)
        {
            this.setValueInternal(this.value + (this.range.get().step() * (is_left_arrow ? -1.0 : 1.0)));
            return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    @Override protected void onMessageChanged() { this.updateMessage(); }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvSliderDrawBackground(canvas, this);
        template.nvSliderDrawThumb     (canvas, this, new Rectangle(this.thumbBounds));
        template.nvSliderDrawText      (canvas, this, this.text);
    }
    
    //==================================================================================================================
    @Override
    protected void appendCustomNarrations(final @NotNull NarrationMessageBuilder builder)
    {
        builder.put(
            NarrationPart.USAGE,
            Text.translatable("narration.slider.usage." + (this.isFocused() ? "focused" : "hovered")));
    }
    
    //==================================================================================================================
    private void updateRange(final @NotNull NormalisedRange range)
    {
        if (!range.isInRange(this.value))
        {
            this.setValue(range.clamp(this.value));
        }
        else
        {
            this.updateThumbPos();
        }
    }
    
    private void updateThumbPos()
    {
        final int length = (this.getTrackEnd() - this.getThumbSize());
        this.thumbBounds.setX(this.getTrackStart() + (int) (this.normalised * length));
    }
    
    private void updateMessage(final @Nullable Function<Double, Text> provider)
    {
        this.displayTextProvider
            .ifSet  (prov -> this.text = prov.apply(this.value))
            .ifEmpty(()   -> this.text = Text.empty());
    }
    
    private void updateMessage() { this.updateMessage(this.displayTextProvider.get()); }
}

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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.ColoredQuadGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.brush.IBrush;
import xyz.lumialights.novia.api.gui.canvas.brush.SolidBrush;
import xyz.lumialights.novia.api.gui.component.provided.*;
import xyz.lumialights.novia.api.gui.font.GlyphBank;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.renderer.GuiTooltipRenderer;

import java.util.*;
import java.util.function.Supplier;



//**********************************************************************************************************************
public interface IGuiTemplate
    extends
        IPaletteProvider,
        
        NVTextBox.Template,
        NVViewport.Template,
        NVSlider.Template,
        NVSimpleButton.Template,
        NVNumericBox.Template,
        NVScrollbar.Template,
        NVLabel.Template,
        NVListBox.Template,
        NVDropdown.Template,
        NVCheckBox.Template,
        NVLabelButton.Template,
        GuiTooltipRenderer.Template
{
    //******************************************************************************************************************
    /**
     * The default template used if no explicit template was set.
     * <p>
     * This template does not permit changing colours as it defines the defaults used for all components, otherwise
     * it will throw an {@link UnsupportedOperationException}.
     */
    IGuiTemplate DEFAULT = new DefaultGuiTemplate()
    {
        @Override
        public @Nullable Colour setColour(@NotNull ColourId id, int colour)
        {
            throw new UnsupportedOperationException("cannot change default template colours");
        }
        
        @Override
        public @Nullable Colour setColour(@NotNull ColourId id, @Nullable Colour colour)
        {
            throw new UnsupportedOperationException("cannot change default template colours");
        }
    };
    
    //******************************************************************************************************************
    /**
     * Gets the default brush for this template.
     * <p>
     * The default brush is the brush that is used whenever the {@link Canvas} visits the next component to draw.
     *
     * @return The default {@link IBrush}
     */
    @Contract(pure = true)
    default @NotNull Supplier<IBrush> getDefaultBrush() { return (() -> new SolidBrush(Colour.WHITE)); }
    
    //==================================================================================================================
    @Override
    default void guiTooltipDrawBackground(final @NotNull Canvas canvas, final @NotNull Rectangle bounds)
    {
        bounds.pad(-12);
        canvas.drawGuiTexture(GuiTooltipRenderer.BACKGROUND_TEXTURE, bounds, false);
        canvas.drawGuiTexture(GuiTooltipRenderer.FRAME_TEXTURE,      bounds, false);
    }
    
    @Override
    default void guiTooltipDrawLines(final @NotNull Canvas canvas, final int x, int y,
                                     final @NotNull List<OrderedText> lines)
    {
        final GuiFont   font = canvas.getFont();
        final GlyphBank bank = new GlyphBank();

        canvas.setColour(canvas.findColour(GuiTooltipRenderer.COLOUR_TEXT));
        bank.addText(font, lines.getFirst(), x, y);

        y += (GuiTooltipRenderer.LINE_HEIGHT + 2);
        
        for (final var line : lines.subList(1, lines.size()))
        {
            bank.addText(font, line, x, y);
            y += GuiTooltipRenderer.LINE_HEIGHT;
        }

        bank.draw(canvas);
    }
    
    //==================================================================================================================
    @Override
    default void nvTextboxDrawContent(final @NotNull Canvas canvas, final @NotNull NVTextBox box,
                                      final @NotNull Rectangle bounds, final long lastSwitchFocusTime)
    {
        final int     first_index = box.getFirstCharacterIndex();
        final GuiFont font        = canvas.getFont();
        final String  text        = font.trimToWidth(box.getText().substring(first_index), bounds.width());
        
        if (!canvas.isActive())
        {
            final OrderedText ordered = box.renderTextProvider.get().provide(text, first_index);
            final Colour      colour  = canvas.findColour(NVTextBox.COLOUR_TEXT_UNEDITABLE);
            
            this.nvTextboxDrawText(canvas, box, ordered, bounds.x(), bounds.y(), colour);
            
            return;
        }
        
        final Colour  text_colour  = canvas.findColour(!box.isEditable()
            ? NVTextBox.COLOUR_TEXT_UNEDITABLE
            : (box.isTextValid() ? NVTextBox.COLOUR_TEXT_EDITABLE : NVTextBox.COLOUR_TEXT_ERROR));
        final int     text_len     = text.length();
        final int     font_height  = MinecraftClient.getInstance().textRenderer.fontHeight;
        final int     selection_1  = box.getCursorPos();
        final int     selection_2  = box.getSelectionEnd();
        final int     caret_pos    = (selection_1 - first_index);
        final boolean is_caret_set = (caret_pos >= 0 && caret_pos <= text_len);
        
        final int text_y = (((bounds.y() + bounds.getBottom()) / 2 - font_height / 2) + 1);
        int running_x = (bounds.x() + 1);
        
        canvas.setClippingRegion(bounds);
        
        if (!text.isEmpty())
        {
            final OrderedText ordered = box.renderTextProvider.get().provide(
                (is_caret_set ? text.substring(0, caret_pos) : text),
                first_index);
            this.nvTextboxDrawText(canvas, box, ordered, running_x, text_y, text_colour);
            
            running_x += font.getWidthFitted(ordered);
        }
        
        if (!text.isEmpty() && is_caret_set && caret_pos < text_len)
        {
            final OrderedText ordered = box.renderTextProvider.get().provide(text.substring(caret_pos), selection_1);
            this.nvTextboxDrawText(canvas, box, ordered, running_x, text_y, text_colour);
        }
        
        if (box.isEmpty() && !box.isFocused() && box.placeholder.isSet())
        {
            this.nvTextboxDrawPlaceholder(canvas, box, running_x, text_y);
        }
        
        final int     box_text_len   = box.getText().length();
        final boolean is_insert_mode = (selection_1 < box_text_len || box_text_len >= box.maxLength.get());
        final int     caret_x        = (!is_caret_set
            ? (caret_pos > 0 ? (bounds.x() + box.getWidth()) : bounds.x())
            : (is_insert_mode ? (running_x - 1) : running_x));
        
        if (!is_insert_mode && box.suggestion.isSet())
        {
            this.nvTextboxDrawSuggestion(canvas, box, (caret_x - 1), text_y);
        }
        
        final int end_caret_pos = MathHelper.clamp((selection_2 - first_index), 0, text_len);
        
        if (end_caret_pos != caret_pos)
        {
            final int sel_s = (bounds.x() + font.getWidthFitted(text.substring(0, end_caret_pos)));
            final int sel_x = (Math.min(Math.min((caret_x + 1), (sel_s - 1)), bounds.width()));
            final int sel_y = (text_y - 1);
            final int sel_w = Math.min((Math.max(sel_s, caret_x) - sel_x), bounds.width());
            final int sel_h = (GuiFont.getRenderHeight() + 1);
            this.nvTextboxDrawSelection(canvas, box, sel_x, sel_y, sel_w, sel_h);
        }
        
        if (
            box.isFocused()
            && is_caret_set
            && ((Util.getMeasuringTimeMs() - lastSwitchFocusTime) / 300L % 2L == 0L)
        )
        {
            if (is_insert_mode)
            {
                canvas.setColour(0xFFD0D0D0);
                canvas.fill(caret_x, (text_y - 1), 1, (font_height + 1));
            }
            else
            {
                canvas.setColour(text_colour);
                canvas.drawText("_", caret_x, text_y);
            }
        }
    }
    
    @Override
    default void nvTextboxDrawBackground(final @NotNull Canvas canvas, final @NotNull NVTextBox textBox)
    {
        canvas.drawGuiTexture(
            NVTextBox.TEXTURES.get(canvas.isActive(), textBox.isFocused()),
            0, 0, textBox.getWidth(), textBox.getHeight(),
            false);
    }
    
    @Override
    default void nvTextboxDrawText(final @NotNull Canvas canvas, final @NotNull NVTextBox textBox,
                                   final @NotNull OrderedText text, final int x, final int y,
                                   final @NotNull Colour textColour)
    {
        canvas.setColour(textColour);
        canvas.drawText(text, x, y);
    }
    
    @Override
    default void nvTextboxDrawSuggestion(final @NotNull Canvas canvas, final @NotNull NVTextBox textBox, final int x,
                                         final int y)
    {
        canvas.setColour(canvas.findColour(NVTextBox.COLOUR_TEXT_SUGGESTION));
        canvas.drawText(Objects.requireNonNull(textBox.suggestion.get()), x, y);
    }
    
    @Override
    default void nvTextboxDrawPlaceholder(final @NotNull Canvas canvas, final @NotNull NVTextBox textBox, final int x,
                                          final int y)
    {
        canvas.setColour(canvas.findColour(NVTextBox.COLOUR_TEXT_PLACEHOLDER));
        canvas.drawText(Objects.requireNonNull(textBox.placeholder.get()), x, y);
    }
    
    @Override
    default void nvTextboxDrawSelection(final @NotNull Canvas canvas, final @NotNull NVTextBox textBox, final int x,
                                        final int y, final int width, final int height)
    {
        final Matrix3x2f pose         = canvas.getTransform().getMatrix();
        final ScreenRect scissor_area = canvas.getClippingRegion().toScreenRect();
        final int        colour_1     = Colour.WHITE.colour();
        final int        colour_2     = 0xFF0000FF;
        final int        x2           = (x + width);
        final int        y2           = (y + height);
        
        canvas.draw(new ColoredQuadGuiElementRenderState(
            RenderPipelines.GUI_INVERT,
            TextureSetup.empty(),
            pose,
            x, y, x2, y2,
            colour_1, colour_1,
            scissor_area));
        canvas.draw(new ColoredQuadGuiElementRenderState(
            RenderPipelines.GUI_TEXT_HIGHLIGHT,
            TextureSetup.empty(),
            pose,
            x, y, x2, y2,
            colour_2, colour_2,
            scissor_area));
    }
    
    //==================================================================================================================
    @Override default void nvViewportDrawBackground(@NotNull Canvas canvas, @NotNull NVViewport viewport) {}
    
    @Override
    default void nvViewportDrawCorner(final @NotNull Canvas canvas, final @NotNull NVViewport viewport, final int width,
                                      final int height)
    {
        canvas.drawGuiTexture(NVScrollbar.TEXTURE_SCROLLBAR_BACKGROUND, 0, 0, width, height, false);
    }
    
    //==================================================================================================================
    @Override
    default void nvSliderDrawBackground(final @NotNull Canvas canvas, final @NotNull NVSlider slider)
    {
        final Identifier texture = (slider.isFocused() ? NVSlider.TEXTURE_HIGHLIGHTED : NVSlider.TEXTURE);
        canvas.drawGuiTexture(texture, 0, 0, slider.getWidth(), slider.getHeight(), false);
    }
    
    @Override
    default void nvSliderDrawThumb(final @NotNull Canvas canvas, final @NotNull NVSlider slider,
                                   final @NotNull Rectangle thumbBounds)
    {
        final Identifier texture = ((slider.isFocused() || slider.isHovered())
            ? NVSlider.TEXTURE_HANDLE_HIGHLIGHT
            : NVSlider.TEXTURE_HANDLE);
        canvas.drawGuiTexture(texture, thumbBounds, false);
    }
    
    @Override
    default void nvSliderDrawText(final @NotNull Canvas canvas, final @NotNull NVSlider slider,
                                  final @NotNull Text text)
    {
        canvas.setColour(canvas.findColour(canvas.isActive() ? NVSlider.COLOUR_TEXT : NVSlider.COLOUR_TEXT_INACTIVE));
        canvas.drawScrollableText(text, 2, 0, (slider.getWidth() - 4), slider.getHeight());
    }
    
    //==================================================================================================================
    @Override
    default void nvSimpleButtonDrawBackground(final @NotNull Canvas canvas, final @NotNull NVSimpleButton button)
    {
        final Identifier texture = NVSimpleButton.BACKGROUND_TEXTURE.get(
            canvas.isActive(),
            (button.isFocused() || button.isHovered()));
        canvas.drawGuiTexture(texture, 0, 0, button.getWidth(), button.getHeight(), false);
    }
    
    @Override
    default void nvSimpleButtonDrawIcon(final @NotNull Canvas canvas, final @NotNull NVSimpleButton button,
                                        final @NotNull Rectangle bounds)
    {
        assert (button.getIcons() != null);
        
        final Identifier texture = button.getIcons().get(canvas.isActive(), (button.isFocused() || button.isHovered()));
        canvas.drawGuiTexture(texture, bounds, false);
    }
    
    @Override
    default void nvSimpleButtonDrawText(final @NotNull Canvas canvas, final @NotNull NVSimpleButton button,
                                        final @NotNull Rectangle bounds)
    {
        assert (button.getText() != null);
        
        canvas.setColour(canvas.findColour(canvas.isActive()
            ? NVSimpleButton.COLOUR_TEXT
            : NVSimpleButton.COLOUR_TEXT_INACTIVE));
        canvas.drawText(button.getText(), bounds, Alignment.MIDDLE_CENTRE);
    }
    
    //==================================================================================================================
    @Override
    default void nvNumericBoxDrawArrowButton(final @NotNull Canvas canvas, final @NotNull NVNumericBox numericBox,
                                             final @NotNull NVAbstractButton<?> button, final @NotNull String text)
    {
        canvas.setColour(canvas.findColour(canvas.isActive()
            ? NVNumericBox.COLOUR_ARROW
            : NVNumericBox.COLOUR_ARROW_INACTIVE));
        canvas.drawText(text, button.getLocalBounds(), Alignment.MIDDLE_CENTRE);
    }
    
    //==================================================================================================================
    @Override
    default void nvScrollbarDrawBackground(final @NotNull Canvas canvas, final @NotNull NVScrollbar scrollbar,
                                           final @NotNull Rectangle trackBounds)
    {
        canvas.drawGuiTexture(NVScrollbar.TEXTURE_SCROLLBAR_BACKGROUND, trackBounds, false);
    }
    
    @Override
    default void nvScrollbarDrawThumb(final @NotNull Canvas canvas, final @NotNull NVScrollbar scrollbar,
                                      final @NotNull Rectangle bounds)
    {
        if (!canvas.isActive())
        {
            return;
        }
        
        canvas.drawGuiTexture(NVScrollbar.TEXTURE_SCROLLBAR_THUMB, bounds, false);
    }
    
    //==================================================================================================================
    @Override default void nvLabelDrawBackground(@NotNull Canvas canvas, @NotNull NVLabel nvLabel) {}
    
    @Override
    default void nvLabelDrawText(final @NotNull Canvas canvas, final @NotNull NVLabel label)
    {
        final GuiFont     font      = canvas.getFont();
        final Text        text      = label.getText();
        final OrderedText draw_text = (font.getWidth(text) > label.getWidth()
            ? label.trimFunction.get().trim(text, font, label)
            : text.asOrderedText());

        canvas.setColour(canvas.findColour(canvas.isActive() ? NVLabel.COLOUR_TEXT : NVLabel.COLOUR_TEXT_INACTIVE));
        canvas.drawText(draw_text, label.getLocalBounds(), label.textAlign.get());
    }
    
    //==================================================================================================================
    @Override
    default <T extends INVItemModel>
    void nvListBoxDrawBackground(@NotNull Canvas canvas, @NotNull NVListBox<T> nvListBox) {}
    
    //==================================================================================================================
    @Override
    default void nvDropdownDrawMenuBackground(final @NotNull Canvas canvas, final @NotNull NVDropdown dropdown,
                                              final int width, final int height)
    {
        final Identifier texture = NVTextBox.TEXTURES.get(true, dropdown.hasFocus());
        canvas.drawGuiTexture(texture, 0, 0, width, height, false);
    }
    
    @Override
    default void nvDropdownDrawMenuItem(final @NotNull Canvas canvas, @NotNull NVDropdown dropdown,
                                        final @NotNull Text title, final int width, final int height, final int index,
                                        final boolean selected, final boolean hovered, final boolean focused)
    {
        if (hovered || selected)
        {
            canvas.setColour(canvas.findColour(selected
                ? NVDropdown.COLOUR_OPTION_BACKGROUND_SELECTED
                : NVDropdown.COLOUR_OPTION_BACKGROUND_HIGHLIGHT));
            canvas.fill();
        }
        
        final int draw_x     = 5;
        final int draw_width = (width - (draw_x * 2));

        canvas.setColour(canvas.findColour(NVDropdown.COLOUR_OPTION_TEXT));
        canvas.setTransform(AffineTransform.translation(draw_x, 1));
        canvas.setClippingRegion(0, 0, draw_width, height);
        
        final GuiFont font       = canvas.getFont();
        final int     text_width = font.getWidthFitted(title);
        
        if (draw_width < text_width)
        {
            final int             trim_width = (draw_width - font.getWidthFitted(ScreenTexts.ELLIPSIS));
            final StringVisitable visitable  = font.trimToWidth(title, trim_width);
            final OrderedText     text       = Language.getInstance().reorder(StringVisitable.concat(
                visitable,
                ScreenTexts.ELLIPSIS));
            canvas.drawText(text, 0, 0, draw_width, height, dropdown.optionAlignment.get());
        }
        else
        {
            canvas.drawText(title, 0, 0, draw_width, height, dropdown.optionAlignment.get());
        }
    }
    
    @Override
    default void nvDropdownDrawButton(final @NotNull Canvas canvas, final @NotNull NVDropdown dropdown,
                                      final @NotNull NVAbstractButton<?> button, final @NotNull String text)
    {
        final Rectangle  bounds  = button.getLocalBounds();
        final Identifier texture = NVTextBox.TEXTURES.get(canvas.isActive(), dropdown.hasFocus());
        
        canvas.drawGuiTexture(texture, bounds.withLeftPadding(1), 1, 0, bounds.width(), bounds.height(), false);
        canvas.setColour(canvas.findColour(canvas.isActive()
            ? NVNumericBox.COLOUR_ARROW
            : NVNumericBox.COLOUR_ARROW_INACTIVE));
        canvas.drawText(text, bounds, Alignment.MIDDLE_CENTRE);




    }
    
    //==================================================================================================================
    @Override default void nvCheckBoxDrawBackground(@NotNull Canvas canvas, @NotNull NVCheckBox checkBox) {}
    
    @Override
    default void nvCheckBoxDrawCheckMark(final @NotNull Canvas canvas, final @NotNull NVCheckBox checkBox)
    {
        canvas.drawGuiTexture(
            NVCheckBox.TEXTURES.get(
                checkBox.isChecked(),
                (checkBox.isFocused() || checkBox.isHovered()),
                canvas.isActive()),
            0, 0, checkBox.getWidth(), checkBox.getHeight(),
            false);
    }
    
    //==================================================================================================================
    @Override default void nvLabelButtonDrawBackground(@NotNull Canvas canvas, @NotNull NVLabelButton button) {}
    
    @Override
    default void nvLabelButtonDrawText(final @NotNull Canvas canvas, final @NotNull NVLabelButton button)
    {
        canvas.setColour(canvas.findColour(canvas.isActive()
            ? NVLabelButton.COLOUR_TEXT
            : NVLabelButton.COLOUR_TEXT_INACTIVE));
        canvas.drawText(button.getText(), 0, 0, button.getWidth(), button.getHeight(), button.textAlign.get());
    }
}

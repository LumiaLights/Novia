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
package xyz.lumialights.novia.test.mod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.core.util.NormalisedRange;
import xyz.lumialights.novia.api.gui.canvas.AffineTransform;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.Direction;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.GuiScreen;
import xyz.lumialights.novia.api.gui.component.provided.*;
import xyz.lumialights.novia.api.gui.font.FontSize;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;

import java.time.Duration;
import java.util.*;



//**********************************************************************************************************************
// Inherit from GuiScreen instead of Screen
public class ComponentTestScreen
    extends GuiScreen
{
    //******************************************************************************************************************
    public static class Item
        implements INVItemModel
    {
        //**************************************************************************************************************
        public NVLabel title;
        
        //**************************************************************************************************************
        public Item(final @NotNull String text)
        {
            this.title = new NVLabel(Text.literal(text));
            this.title.textAlign.set(Alignment.MIDDLE_LEFT);
        }
        
        //==============================================================================================================
        @Override public @NotNull List<GuiComponent> getChildren() { return List.of(this.title); }
        
        @Override
        public void resized(final @NotNull Rectangle bounds)
        {
            this.title.setBounds(bounds.withLocalPos().withPadding(5, 0));
        }
        
        //==============================================================================================================
        @Override
        public void draw(final @NotNull Canvas canvas, final @NotNull Rectangle bounds, final int index,
                         final boolean selected, final boolean hovered, final boolean focused)
        {
            final Rectangle local_bounds = bounds.withLocalPos();
            
            if (focused)
            {
                canvas.setColour(Colour.WHITE);
                canvas.drawRect(local_bounds);
            }
            
            if (selected || hovered)
            {
                canvas.setColour(Colour.WHITE.withAlpha(selected ? 0x66 : 0x33));
                canvas.fill(local_bounds);
            }
        }
    }
    
    //******************************************************************************************************************
    public NVSimpleButton  basicComponentsButton;
    public NVListBox<Item> testListBox;
    public NVSlider        testSlider;
    public NVDropdown      testDropdown;
    
    //******************************************************************************************************************
    // In the constructor we set up our child components and add them to the screen
    public ComponentTestScreen(final @NotNull Text title)
    {
        super(title);

        // create and add component
        // note: we can also create the component and add it at the end
        this.basicComponentsButton = this.addChild(new NVSimpleButton(
            (btt ->
            {
                this.testListBox.addItem(new Item(UUID.randomUUID().toString()));
                this.testListBox.refreshList();
            }),
            Text.literal("Basic Components")));
        this.basicComponentsButton.setTooltip(Tooltip.of(Text.literal("Add Listbox Item\nThis is a button haha")));
        this.basicComponentsButton.setTooltipDelay(Duration.ofSeconds(1));
        
        this.testListBox = this.addChild(new NVListBox<>());
        this.testListBox.itemSize.set(20);
        this.testListBox.selectionMode.set(NVListBox.SelectionMode.MULTIPLE);
        this.testListBox.canFocusItems.set(true);
        this.testListBox.setTooltip(Tooltip.of(Text.literal("Test List Box")));
        this.testListBox.setTooltipDelay(Duration.ofSeconds(3));
        this.testListBox.addAllItems(List.of(
            new Item("Hello"),
            new Item("2"),
            new Item("2rfeswafewfef"),
            new Item("This is your mother speaking"),
            new Item("Can i has rice pls?"),
            new Item("Don't stop the bleedin', hold on to that feeeeeeelin'"),
            new Item("Hello 2")
        ));
        this.testListBox.refreshList();
        
        this.testSlider = this.addChild(new NVSlider());
        this.testSlider.range.set(new NormalisedRange(0, 100, 2));
        this.testSlider.displayTextProvider.set(val -> Text.literal("Value: " + val));
        this.testSlider.setTooltip(Tooltip.of(Text.literal("Slider value: 0%")));
        this.testSlider.addChangeListener(slider ->
        {
            final double normalised = slider.range.get().normalise(slider.getValueAsDouble());
            slider.setTooltip(Tooltip.of(Text.literal("Slider value: " + ((int)(normalised * 100.0) / 100.0) + "%")));
        });
        this.testSlider.setFont(GuiFont.getUnicode());
        
        this.testDropdown = this.addChild(new NVDropdown());
        this.testDropdown.addOption(new Value(323));
        this.testDropdown.addOption(new Value(true));
        this.testDropdown.addOption(new Value("this is some string lel"));
        this.testDropdown.addOption(new Value(Map.of(new Value("key"), new Value("value"))));
        this.testDropdown.addOption(new Value(List.of(new Value(1), new Value(2), new Value(3))));
        this.testDropdown.addOption("Map copy", new Value(Map.of(new Value("key"), new Value("value"))));
        this.testDropdown.addOption("List copy", new Value(List.of(new Value(1), new Value(2), new Value(3))));
        this.testDropdown.addOption(new Value());
        this.testDropdown.addOption(new Value(434.0f));

        // setting our gui default font to be shadowed
        this.setFont(this.getFont().withShadow(true));
    }

    //==================================================================================================================
    @Override
    protected void resized()
    {
        final Rectangle bounds = this.getLocalBounds();
        
        this.basicComponentsButton.setBounds(5, 5, 100, 20);
        this.testListBox.setBounds(bounds
            .withLeft(this.basicComponentsButton.getRight() + 5)
            .withBottom(bounds.getCentreY()));
        this.testSlider.setBounds((new Rectangle(0, 0, 100, 20))
            .withRightOf(this.basicComponentsButton.getBounds())
            .withBottomOf(this.testListBox.getBounds())
            .withTranslationY(5));
        
        final Rectangle dropdown_bounds = this.testSlider.getBounds();
        this.testDropdown.setBounds(dropdown_bounds
            .withBottomOf(dropdown_bounds)
            .translateY(5));
    }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        // lets draw white text
        canvas.setColour(Colour.WHITE);
        canvas.drawText(
            ("Selected Items: " + this.testListBox.streamSelectedItems().count()),
            (this.testSlider.getRight() + 10),
            (this.testListBox.getBottom() + 10));
        
        final Rectangle gradient_test_bounds = this.getLocalBounds()
            .setTop(this.testDropdown.getBottom())
            .padTop(10);

        // let's make some gradient stuff
        canvas.runWithState(() ->
        {
            canvas.addTransform(AffineTransform.translation(gradient_test_bounds.x(), gradient_test_bounds.y()));
            
            final GuiFont font = canvas.getFont();
            font.setSize(FontSize.pixels(20));

            canvas.setGradient(new Gradient(0x77898989, 0xAA323232, Direction.HORIZONTAL));
            canvas.fill();
            
            canvas.setGradient(new Gradient(0xFFFF0000, 0xFF0000FF, Direction.HORIZONTAL));
            canvas.setFont(font.withFormattingPreserved(GuiFont.Format.BOLD, GuiFont.Format.STRIKETHROUGH));
            canvas.drawText(
                Text
                    .literal("Hello this is a ")
                    .copy()
                    .append(Text
                        .literal("horizontal")
                        .setStyle(Style.EMPTY
                            .withStrikethrough(false)
                            .withUnderline(true)
                            .withColor(Colour.GOLD.colour())))
                    .append(Text.literal(" test gradient")),
                10, 10);
            
            canvas.setGradient(new Gradient(0xFFFFFF00, 0xFF00FF00, Direction.VERTICAL));
            canvas.setFont(font.withFormattingPreserved(GuiFont.Format.ITALIC, GuiFont.Format.UNDERLINED));
            canvas.drawText(
                Text
                    .literal("Hello this is a ")
                    .copy()
                    .append(Text
                        .literal("vertical")
                        .setStyle(Style.EMPTY
                            .withBold(true)
                            .withItalic(false)
                            .withColor(Colour.GOLD.colour())))
                    .append(Text.literal(" test gradient")),
                10, 30);
        });

        canvas.setColour(Colour.GOLD);
        canvas.drawText(
            Text.of("FPS: " + MinecraftClient.getInstance().getCurrentFps()),
            this.getLocalBounds().removeBottom(15).translateX(5).getPosition());
    }
}

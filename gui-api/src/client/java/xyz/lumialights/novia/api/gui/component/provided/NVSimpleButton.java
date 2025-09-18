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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.ColourId;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.util.*;
import java.util.stream.Stream;



//**********************************************************************************************************************
/**
 * An {@link NVAbstractButton} implementation that can render a given text, and an optional icon on top of the button
 * background.
 */
public class NVSimpleButton
    extends NVAbstractButton<NVSimpleButton>
{
    //******************************************************************************************************************
    public enum IconPlacement
        implements StringIdentifiable
    {
        /** Places the icon left of the text. */
        LEFT,
        
        /** Places the icon right of the text. */
        RIGHT,
        
        /** Places the icon above the text. */
        ABOVE,
        
        /** Places the icon below the text. */
        BELOW,
        ;
        
        //**************************************************************************************************************
        public static final Codec<IconPlacement> CODEC = StringIdentifiable.createCodec(IconPlacement::values);
        
        //**************************************************************************************************************
        @Override public @NotNull String asString() { return this.name().toLowerCase(); }
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        void nvSimpleButtonDrawBackground(@NotNull Canvas canvas, @NotNull NVSimpleButton button);
        
        void nvSimpleButtonDrawIcon(@NotNull Canvas canvas, @NotNull NVSimpleButton button, @NotNull Rectangle bounds);
        
        void nvSimpleButtonDrawText(@NotNull Canvas canvas, @NotNull NVSimpleButton button, @NotNull Rectangle bounds);
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_TEXT          = ColourId.reserve();
    public static final ColourId COLOUR_TEXT_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /** See {@link NVSimpleButton#iconPlacement}. */
    public static final IconPlacement DEFAULT_PLACEMENT = IconPlacement.LEFT;
    
    /** See {@link NVSimpleButton#iconSize}. */
    public static final int DEFAULT_ICON_SIZE = 18;
    
    /** See {@link NVSimpleButton#iconMargin}. */
    public static final int DEFAULT_ICON_MARGIN = 2;
    
    //==================================================================================================================
    /** The textures used to render the button. */
    public static final ButtonTextures BACKGROUND_TEXTURE = new ButtonTextures(
        Identifier.ofVanilla("widget/button"),
        Identifier.ofVanilla("widget/button_disabled"),
        Identifier.ofVanilla("widget/button_highlighted")
	);
    
    //******************************************************************************************************************
    /** Describes the placement of the icon next to the button text. (if both are set) */
    public final GuiProperty.NonNull<IconPlacement> iconPlacement;
    
    /** Describes the size of the icon texture that should be rendered on the button. */
    public final GuiProperty.NonNull<Integer> iconSize;
    
    /** Describes the margin between the icon and text if both are set. */
    public final GuiProperty.NonNull<Integer> iconMargin;
    
    //------------------------------------------------------------------------------------------------------------------
    private Rectangle      iconBounds = new Rectangle();
    private Rectangle      textBounds = new Rectangle();
    private ButtonTextures textures   = null;
    private Text           text       = null;
    
    //******************************************************************************************************************
    /**
     * Constructs a new button with the given text and action.
     * @param action  The {@link ActionListener}
     * @param text    The text displayed on the button
     * @param message The component message
     */
    public NVSimpleButton(final @NotNull  ActionListener<NVSimpleButton> action,
                          final @Nullable Text                           text,
                          final @NotNull  Text                           message)
    {
        super(action, message);
        
        this.iconPlacement = GuiProperty.nonNull(NVSimpleButton.DEFAULT_PLACEMENT,   this::updateIconStyle);
        this.iconMargin    = GuiProperty.nonNull(NVSimpleButton.DEFAULT_ICON_MARGIN, this::updateIconStyle);
        this.iconSize      = GuiProperty.nonNull(
            NVSimpleButton.DEFAULT_ICON_SIZE,
            (t ->
            {
                if (this.textures != null)
                {
                    this.updateContentBounds();
                }
            }));
        
        this.text = text;
    }
    
    /**
     * Constructs a new button with the given text and action.
     * @param action The {@link ActionListener}
     * @param text   The text displayed on the button
     */
    public NVSimpleButton(final @NotNull ActionListener<NVSimpleButton> action, final @NotNull Text text)
    {
        this(action, text, ScreenTexts.EMPTY);
    }
    
    /**
     * Constructs a new button with the given text, and an empty action.
     * @param text The text displayed on the button
     */
    public NVSimpleButton(final @Nullable Text text) { this((t -> {}), text, ScreenTexts.EMPTY); }
    
    /**
     * Constructs a new button with the given action and no text.
     * @param action The {@link ActionListener}
     */
    public NVSimpleButton(final @NotNull ActionListener<NVSimpleButton> action)
    {
        this(action, null, ScreenTexts.EMPTY);
    }
    
    /** Constructs a new button without an action and no text. */
    public NVSimpleButton() { this((t -> {}), null, ScreenTexts.EMPTY); }
    
    //==================================================================================================================
    /**
     * Gets the textures for the icon that is to be drawn on the button.
     * @return The {@link ButtonTextures}
     */
    public @Nullable ButtonTextures getIcons() { return this.textures; }
    
    /**
     * Gets the text displayed on the button.
     * @return The button {@link Text}
     */
    public @Nullable Text getText() { return this.text; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.concat(
            super.getGuiProperties(),
            Stream.of(
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.SIMPLE_BUTTON_ICON_SIZE,
                    this.iconSize,
                    Codec.INT),
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.SIMPLE_BUTTON_ICON_PLACEMENT,
                    this.iconMargin,
                    Codec.INT),
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.SIMPLE_BUTTON_ICON_MARGIN,
                    this.iconPlacement,
                    IconPlacement.CODEC)));
    }
    
    
    //==================================================================================================================
    /**
     * Sets the icon textures to draw on the button.
     * @param textures The {@link ButtonTextures}
     */
    public void setIcons(final @Nullable ButtonTextures textures)
    {
        final ButtonTextures old = this.textures;
        this.textures = textures;
        
        if ((old == null || textures == null) && old != textures)
        {
            this.updateContentBounds();
        }
    }
    
    /**
     * Sets the text displayed on the button.
     * @param text The button {@link Text}
     */
    public void setText(final @Nullable Text text)
    {
        if (!Objects.equals(this.text, text))
        {
            this.text = text;
            this.updateContentBounds();
        }
    }
    
    //==================================================================================================================
    @Override protected void resized() { this.updateContentBounds(); }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvSimpleButtonDrawBackground(canvas, this);
        
        if (this.textures != null)
        {
            template.nvSimpleButtonDrawIcon(canvas, this, new Rectangle(this.iconBounds));
        }
        
        if (this.text != null)
        {
            template.nvSimpleButtonDrawText(canvas, this, new Rectangle(this.textBounds));
        }
    }
    
    //==================================================================================================================
    @Override
    protected void onFontChanged(final @Nullable GuiFont font)
    {
        this.updateContentBounds(Objects.requireNonNullElseGet(font, this::getFont));
    }
    
    @Override protected void onScreenStateChanged() { this.updateContentBounds(); }
    
    //==================================================================================================================
    private void updateIconStyle(final Object obj)
    {
        if (this.textures != null && this.text != null)
        {
            this.updateContentBounds();
        }
    }
    
    private void updateContentBounds() { this.updateContentBounds(this.getFont()); }
    
    private void updateContentBounds(final @NotNull GuiFont font)
    {
        final boolean has_icons = (this.textures != null);
        
        if (!has_icons && text == null)
        {
            return;
        }
        
        final IconPlacement placement   = this.iconPlacement.get();
        final int           icon_size   = (has_icons ? this.iconSize.get() : 0);
        final int           font_height = MinecraftClient.getInstance().textRenderer.fontHeight;
        
        switch (placement)
        {
            case LEFT:
            case RIGHT:
            {
                int width      = 0;
                int height     = 0;
                int text_width = 0;
                
                if (this.text != null)
                {
                    text_width = font.getWidthFitted(this.text);
                    width      = text_width;
                    height     = font_height;
                }
                
                if (has_icons)
                {
                    width  += icon_size;
                    height  = Math.max(height, this.iconSize.get());
                    
                    if (this.text != null)
                    {
                        width += this.iconMargin.get();
                    }
                }
                
                final Rectangle content_bounds = new Rectangle(0, 0, width, height).centre(this.getLocalBounds());
                
                if (placement == IconPlacement.LEFT)
                {
                    this.iconBounds = content_bounds.removeLeft(icon_size);
                    this.textBounds = content_bounds;
                }
                else
                {
                    this.textBounds = content_bounds.removeLeft(text_width);
                    this.iconBounds = content_bounds;
                }
            } break;
            
            case ABOVE:
            case BELOW:
            {
                int width       = 0;
                int height      = 0;
                int text_height = 0;
                
                if (this.text != null)
                {
                    text_height = font_height;
                    width       = font.getWidthFitted(this.text);
                    height      = text_height;
                }
                
                if (has_icons)
                {
                    width   = Math.max(width, this.iconSize.get());
                    height += icon_size;
                    
                    if (this.text != null)
                    {
                        height += this.iconMargin.get();
                    }
                }
                
                final Rectangle content_bounds = new Rectangle(0, 0, width, height).centre(this.getLocalBounds());
                
                if (placement == IconPlacement.ABOVE)
                {
                    this.iconBounds = content_bounds.removeTop(icon_size);
                    this.textBounds = content_bounds;
                }
                else
                {
                    this.textBounds = content_bounds.removeTop(text_height);
                    this.iconBounds = content_bounds;
                }
            } break;
        }
    }
}

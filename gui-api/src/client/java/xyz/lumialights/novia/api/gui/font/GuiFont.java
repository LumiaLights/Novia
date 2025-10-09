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
package xyz.lumialights.novia.api.gui.font;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.impl.*;

import java.util.*;



//**********************************************************************************************************************
public final class GuiFont
{
    //******************************************************************************************************************
    /** Specifies the formatting flags for the font. */
    public enum Format
    {
        BOLD(Formatting.BOLD),
        ITALIC(Formatting.ITALIC),
        STRIKETHROUGH(Formatting.STRIKETHROUGH),
        UNDERLINED(Formatting.UNDERLINE),
        OBFUSCATED(Formatting.OBFUSCATED),
        SHADED(null);
        
        //**************************************************************************************************************
        public final Formatting minecraftFormat;
        
        //**************************************************************************************************************
        Format(final @Nullable Formatting minecraftFormat) { this.minecraftFormat = minecraftFormat; }
    }
    
    //******************************************************************************************************************
    /**
     * The currently set default font, usually <a href="https://minecraft.fandom.com/de/wiki/Mojangles">Mojangles</a>.
     */
    public static final Supplier<@NotNull GuiFont> DEFAULT;
    
    /** <a href="https://minecraft.wiki/w/GNU_Unifont">Unicode</a> */
    public static final Supplier<@NotNull GuiFont> UNICODE;
    
    /** <a href="https://minecraft.wiki/w/Standard_Galactic_Alphabet">Standard Galactic Alphabet</a> */
    public static final Supplier<@NotNull GuiFont> ALT;
    
    /** <a href="https://minecraft.wiki/w/Illageralt">Illageralt</a> */
    public static final Supplier<@NotNull GuiFont> ILLAGERALT;
    
    //------------------------------------------------------------------------------------------------------------------
    private static final GuiFont CACHED_DEFAULT;
    private static final GuiFont CACHED_UNICODE;
    private static final GuiFont CACHED_ALT;
    private static final GuiFont CACHED_ILLAGERALT;
    
    //==================================================================================================================
    static
    {
        CACHED_DEFAULT    = GuiFont.findFont(MinecraftClient.DEFAULT_FONT_ID)     .orElseThrow();
        CACHED_UNICODE    = GuiFont.findFont(MinecraftClient.UNICODE_FONT_ID)     .orElseThrow();
        CACHED_ALT        = GuiFont.findFont(MinecraftClient.ALT_TEXT_RENDERER_ID).orElseThrow();
        CACHED_ILLAGERALT = GuiFont.findFont(Identifier.ofVanilla("illageralt"))  .orElseThrow();
        DEFAULT           = (() -> new GuiFont(GuiFont.CACHED_DEFAULT));
        UNICODE           = (() -> new GuiFont(GuiFont.CACHED_UNICODE));
        ALT               = (() -> new GuiFont(GuiFont.CACHED_ALT));
        ILLAGERALT        = (() -> new GuiFont(GuiFont.CACHED_ILLAGERALT));
    }
    
    //******************************************************************************************************************
    /**
     * Finds a font from its ID of the font register.
     * @param fontId The ID of the font
     * @return An optional {@link GuiFont} or an empty optional if no font was found for that ID
     */
    public static @NotNull Optional<GuiFont> findFont(final @NotNull Identifier fontId)
    {
        return GuiFont.getFontStorage(fontId).map(GuiFont::new);
    }
    
    /**
     * Gets the standard render height of text in Minecraft, this is the default size that font in Minecraft uses to
     * render as text on screen.
     * @return {@link TextRenderer#fontHeight}
     */
    public static int getRenderHeight() { return MinecraftClient.getInstance().textRenderer.fontHeight; }
    
    //------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("resource")
    private static @NotNull Optional<FontStorage> getFontStorage(final @NotNull Identifier fontId)
    {
        final FontManager manager = ((MinecraftClientAccessor) MinecraftClient.getInstance()).novia$getFontManager();
        final FontStorage storage = ((FontManagerAccessor) manager).novia$getFontStorage(fontId);
        return Optional.ofNullable(!storage.getId().equals(FontManager.MISSING_STORAGE_ID) ? storage : null);
    }
    
    //******************************************************************************************************************
    private final BitSet      formats = new BitSet(Format.values().length);
    private final FontStorage storage;
    private final float       scaleFactor;
    
    private float fontScale = 1f;
    private float tracking  = 0f;
    
    //******************************************************************************************************************
    /**
     * Creates a new font from the given font ID, with the given size and formatting. If a font could not be found by
     * its ID, the default font will be used.
     * @param fontId   The {@link Identifier} of the font
     * @param fontScale The size of the font
     * @param formats  The formatting to use
     */
    public GuiFont(final @NotNull Identifier          fontId,
                   final @NotNull FontSize fontScale,
                   final @NotNull Format @NotNull ... formats)
    {
        this(GuiFont.getFontStorage(fontId).orElse(GuiFont.CACHED_DEFAULT.storage));
        this.fontScale = fontScale.getScaledValue(GuiFont.getRenderHeight());
        this.addFormatting(formats);
    }
    
    /**
     * Creates a new font with the default font.
     * @param fontScale The size of the font
     * @param formats  The formatting to use
     */
    public GuiFont(final @NotNull FontSize fontScale, final @NotNull Format @NotNull ... formats)
    {
        this(GuiFont.CACHED_DEFAULT.storage);
        this.fontScale = fontScale.getScaledValue(GuiFont.getRenderHeight());
        this.addFormatting(formats);
    }
    
    /**
     * Creates a new font from the given font ID with the default size and no formatting. If a font could not be found
     * by its ID, the default font will be used.
     * @param fontId The {@link Identifier} of the font
     */
    public GuiFont(final @NotNull Identifier fontId) { this(GuiFont.findFont(fontId).orElseGet(GuiFont.DEFAULT)); }
    
    /**
     * Constructs a new copy of the given font object.
     * @param other The {@link GuiFont} to copy from
     */
    public GuiFont(final @NotNull GuiFont other)
    {
        this(other.storage);
        
        this.formats.or(other.formats);
        this.fontScale = other.fontScale;
        this.tracking  = other.tracking;
    }
    
    /**
     * Constructs a new font from the given {@link Style}. If font ID is null or couldn't be found, the default font
     * will be used.
     * @param style The {@link Style}
     */
    public GuiFont(final @NotNull Style style)
    {
        this(style.getFont());
        this.setStyle(style);
    }
    
    /** Constructs a new font with the default font, the default size and no formatting. */
    public GuiFont() { this(GuiFont.CACHED_DEFAULT.storage); }
    
    //------------------------------------------------------------------------------------------------------------------
    GuiFont(final @NotNull FontStorage storage)
    {
        this.storage     = storage;
        this.scaleFactor = (GuiFont.getRenderHeight() / ((FontStorageAccessor) storage).novia$getMetrics().height());
    }
    
    //==================================================================================================================
    /**
     * Gets the {@link Identifier} of this font.
     * @return The {@link Identifier}
     */
    public @NotNull Identifier getId() { return this.storage.getId(); }
    
    /**
     * Gets the (possibly) cached glyph for the given code point. If no glyph could be found, it will return
     * {@link BuiltinEmptyGlyph#MISSING}. (an in-built glyph represented by a white square)
     * @param codePoint The code point to look up the glyph for
     * @return The {@link Glyph}
     */
    public @NotNull Glyph getGlyph(final int codePoint) { return this.storage.getGlyph(codePoint, false); }
    
    /**
     * Gets the (possibly) cached glyph for the given code point. If no glyph could be found, it will return
     * an empty optional.
     * @param codePoint The code point to look up the glyph for
     * @return An {@link Optional} containing the {@link Glyph}, or empty if no glyph was found
     */
    public @NotNull Optional<Glyph> getOptionalGlyph(final int codePoint)
    {
        final Glyph glyph = this.getGlyph(codePoint);
        return Optional.ofNullable(glyph != BuiltinEmptyGlyph.MISSING ? glyph : null);
    }
    
    /**
     * Gets the (possibly) cached baked glyph for the given code point. If no glyph could be found, it will return
     * the baked version of {@link BuiltinEmptyGlyph#MISSING}. (an in-built glyph represented by a white square)
     * @param codePoint The code point to look up the baked glyph for
     * @return The {@link BakedGlyph}
     */
    public @NotNull BakedGlyph getBaked(final int codePoint) { return this.storage.getBaked(codePoint); }
    
    /**
     * Gets the obfuscated version of a given glyph.
     * @param glyph The glyph to get an obfuscated baked glyph for
     * @return The obfuscated {@link BakedGlyph}
     */
    public @NotNull BakedGlyph getObfuscatedBakedGlyph(final @NotNull Glyph glyph)
    {
        return this.storage.getObfuscatedBakedGlyph(glyph);
    }
    
    /**
     * Gets the baked rectangle glyph used to draw the lines for things like underlined or strike-through texts.
     * @return The rectangle {@link BakedGlyph}
     */
    public @NotNull BakedGlyph getRectangleBakedGlyph() {return this.storage.getRectangleBakedGlyph();}
    
    /**
     * Gets the current scale override applied to the font.
     * <p>
     * Do not confuse with {@link #getHeight()}, which is the current font object's underlying estimate of all given
     * providers.
     * @param unit The unit to convert to
     * @return The font size
     */
    public float getScale(final @NotNull FontSize.Unit unit)
    {
        return (this.fontScale / unit.scaleFunc.apply(MinecraftClient.getInstance().textRenderer.fontHeight));
    }
    
    /**
     * Gets the current scale applied to the font upon rendering in em unit (relative to {@link #getRenderHeight()}).
     * <p>
     * Do not confuse with {@link #getHeight()}, which is the current font object's underlying estimate of all given
     * providers.
     * @return The font size in em
     */
    public float getScale() { return this.fontScale; }
    
    /**
     * Gets this font's current tracking space, which is the tracking factor times the font's height.
     * @return The tracking space
     */
    public float getTracking() { return (this.tracking * this.fontScale); }
    
    /**
     * Gets this font's tracking factor, where 0 means default and anything above is added space
     * (see {@link #getTrackingFactor(float)}).
     * @return The tracking factor
     */
    public float getTrackingFactor() { return this.tracking; }
    
    /**
     * Gets the scaled height of the font. For fonts with multiple providers, this will be the maximum height
     * of all providers.
     * <p>
     * Do not confuse with {@link #getScale()}, which is the current font object's override for the render scale.
     * @return The scaled height of the font
     */
    public float getHeight()
    {
        return (((FontStorageAccessor) this.storage).novia$getMetrics().height() * this.scaleFactor * this.fontScale);
    }
    
    /**
     * Gets the scaled ascent of the font, this is the distance between the top of the font down to the baseline.
     * For fonts with multiple providers, this will be the maximum ascent of all providers.
     * @return The scaled ascent of the font
     */
    public float getAscent()
    {
        return (((FontStorageAccessor) this.storage).novia$getMetrics().ascent() * this.scaleFactor * this.fontScale);
    }
    
    /**
     * Gets the scaled descent of the font, this is the distance between the bottom of the font up to the baseline.
     * For fonts with multiple providers, this will be the maximum descent of all providers.
     * @return The scaled descent of the font
     */
    public float getDescent()
    {
        return (((FontStorageAccessor) this.storage).novia$getMetrics().descent() * this.scaleFactor * this.fontScale);
    }
    
    /**
     * Gets the unscaled metrics for this font.
     * <p>
     * Do note that the returned metrics is an estimate of the largest values of all the providers for the font with
     * the given ID, if there is a smaller glyph being used for drawing it will still comply with the measurements for
     * the tallest.
     * @return The {@link FontMetrics} for this font
     */
    public @NotNull FontMetrics getMetrics() { return ((FontStorageAccessor) this.storage).novia$getMetrics(); }
    
    //==================================================================================================================
    /**
     * Gets this font as {@link GuiStyle} object with all the formatting data converted.
     * @param ignoreFontId If {@code true}, the font ID will not be applied to the style
     * @return The new {@link GuiStyle}
     */
    public @NotNull GuiStyle asStyle(final boolean ignoreFontId)
    {
        return new GuiStyle(null, null, this.isBold(), this.isItalic(), this.isUnderlined(), this.isStrikethrough(),
                            this.isObfuscated(), null, null, null, (ignoreFontId ? null : this.getId()),
                            this.isShaded(), this.fontScale, this.tracking);
    }
    
    /**
     * Gets this font as {@link GuiStyle} object with all the formatting data converted.
     * @return The new {@link GuiStyle}
     */
    public @NotNull GuiStyle asStyle() { return this.asStyle(false); }
    
    //==================================================================================================================
    /** {@return whether the font is bold} */
    public boolean isBold() { return this.formats.get(Format.BOLD.ordinal()); }
    
    /** {@return whether the font is italic} */
    public boolean isItalic() { return this.formats.get(Format.ITALIC.ordinal()); }
    
    /** {@return whether the font is underlined} */
    public boolean isUnderlined() { return this.formats.get(Format.UNDERLINED.ordinal()); }
    
    /** {@return whether the font is strikethrough} */
    public boolean isStrikethrough() { return this.formats.get(Format.STRIKETHROUGH.ordinal()); }
    
    /** {@return whether the font is obfuscated} */
    public boolean isObfuscated() { return this.formats.get(Format.OBFUSCATED.ordinal()); }
    
    /** {@return whether the text draws a shadow beneath} */
    public boolean isShaded() { return this.formats.get(Format.SHADED.ordinal()); }
    
    /** {@return <code>true</code> if no formatting flag was set} */
    public boolean isPlain() { return this.formats.isEmpty(); }
    
    /** {@return <code>true</code> if no formatting flag was set, and if tracking and font size are at their defaults} */
    public boolean isDefaultFormatted() { return (this.isPlain() && this.fontScale == 1f && this.tracking == 0f); }
    
    //==================================================================================================================
    /**
     * Copies the formatting of this font and creates a new font object with the given font ID to use instead. If the
     * given font ID does not exist, {@link #DEFAULT} will be applied instead.
     *
     * @param fontId The {@link Identifier} of the font to use
     * @return The new {@link GuiStyle}
     */
    public @NotNull GuiFont withFont(final @NotNull Identifier fontId)
    {
        if (fontId.equals(this.getId()))
        {
            return new GuiFont(this);
        }
        
        final GuiFont font = new GuiFont(fontId);
        font.formats.or(this.formats);
        font.fontScale = this.fontScale;
        font.tracking  = this.tracking;
        
        return font;
    }
    
    /**
     * Copies this font and specified whether it should be bold or not.
     * @param bold {@code true} if the font should be bold
     * @return The new font
     */
    public @NotNull GuiFont withBold(final boolean bold)
    {
        final GuiFont font = new GuiFont(this);
        font.setBold(bold);
        return font;
    }
    
    /**
     * Copies this font and specified whether it should be italicised or not.
     * @param italic {@code true} if the font should be italic
     * @return The new font
     */
    public @NotNull GuiFont withItalicisation(final boolean italic)
    {
        final GuiFont font = new GuiFont(this);
        font.setItalic(italic);
        return font;
    }
    
    /**
     * Copies this font and specified whether it should be underlined or not.
     * @param underlined {@code true} if the font should be underlined
     * @return The new font
     */
    public @NotNull GuiFont withUnderline(final boolean underlined)
    {
        final GuiFont font = new GuiFont(this);
        font.setUnderlined(underlined);
        return font;
    }
    
    /**
     * Copies this font and specified whether it should be strikethrough or not.
     * @param strikethrough {@code true} if the font should be strikethrough
     * @return The new font
     */
    public @NotNull GuiFont withStrikethrough(final boolean strikethrough)
    {
        final GuiFont font = new GuiFont(this);
        font.setStrikethrough(strikethrough);
        return font;
    }
    
    /**
     * Copies this font and specified whether it should be obfuscated or not.
     * @param obfuscated {@code true} if the font should be obfuscated
     * @return The new font
     */
    public @NotNull GuiFont withObfuscation(final boolean obfuscated)
    {
        final GuiFont font = new GuiFont(this);
        font.setObfuscated(obfuscated);
        return font;
    }
    
    /**
     * Copies this font and specified whether it should be shaded or not.
     * @param shaded {@code true} if the font should be shaded
     * @return The new font
     */
    public @NotNull GuiFont withShadow(final boolean shaded)
    {
        final GuiFont font = new GuiFont(this);
        font.setShaded(shaded);
        return font;
    }
    
    /**
     * Copies this font and sets the flags to the given {@code formats}.
     * @param formats The set of {@link Format} flags to apply to the font
     * @return The new font
     */
    public @NotNull GuiFont withFormatting(final @NotNull Format @NotNull ... formats)
    {
        final GuiFont font = new GuiFont(this);
        font.setFormatting(formats);
        return font;
    }
    
    /**
     * Copies this font and sets the flags to the given {@code formats}.
     * <p>
     * This is similar to {@link #withFormatting(Format...)}, with the difference that any pre-existing flags won't be
     * cleared and instead appended to.
     * @param formats The set of {@link Format} flags to apply to the font
     * @return The new font
     */
    public @NotNull GuiFont withFormattingPreserved(final @NotNull Format @NotNull ... formats)
    {
        final GuiFont font = new GuiFont(this);
        font.addFormatting(formats);
        return font;
    }
    
    /**
     * Copies this font and sets all format flags off.
     * @return The new plain font
     */
    public @NotNull GuiFont withPlain()
    {
        final GuiFont font = new GuiFont(this);
        font.clearFormatting();
        return font;
    }
    
    //==================================================================================================================
    /**
     * Sets the font to be bold.
     * @param bold {@code true} if the font should be bold
     */
    public void setBold(final boolean bold) { this.setFormat(Format.BOLD, bold); }
    
    /**
     * Sets the font to be italic.
     * @param italic {@code true} if the font should be italic
     */
    public void setItalic(final boolean italic) { this.setFormat(Format.ITALIC, italic); }
    
    /**
     * Sets the font to be underlined.
     * @param underlined {@code true} if the font should be underlined
     */
    public void setUnderlined(final boolean underlined) { this.setFormat(Format.UNDERLINED, underlined); }
    
    /**
     * Sets the font to be strikethrough.
     * @param strikethrough {@code true} if the font should be strikethrough
     */
    public void setStrikethrough(final boolean strikethrough) { this.setFormat(Format.STRIKETHROUGH, strikethrough); }
    
    /**
     * Sets the font to be obfuscated.
     * @param obfuscated {@code true} if the font should be obfuscated
     */
    public void setObfuscated(final boolean obfuscated) { this.setFormat(Format.OBFUSCATED, obfuscated); }
    
    /**
     * Sets the font to draw a shadow beneath.
     * @param shaded {@code true} if the font should be shaded
     */
    public void setShaded(final boolean shaded) { this.setFormat(Format.SHADED, shaded); }
    
    /**
     * Sets a specific formatting flag for this font.
     * @param format  The flag to enable/disable
     * @param enabled {@code true} if the flag should be enabled, otherwise {@code false}
     */
    public void setFormat(final @NotNull Format format, final boolean enabled)
    {
        this.formats.set(format.ordinal(), enabled);
    }
    
    /**
     * Sets a set of flags to be enabled for this font and flags that are not specified will be disabled.
     * @param formats The set of flags to enable
     */
    public void setFormatting(final @NotNull Format @NotNull ... formats)
    {
        this.clearFormatting();
        this.addFormatting(formats);
    }
    
    /**
     * Adds a set of flags that should be enabled for this font.
     * @param formats The set of flags to enable
     */
    public void addFormatting(final @NotNull Format @NotNull ... formats)
    {
        for (final var format : formats)
        {
            this.formats.set(format.ordinal(), true);
        }
    }
    
    /**
     * Gets the width of the string with the current font. Formatting codes will not be consumed and considered part
     * of the string. If this is not the desired behaviour, use {@link TextLayout} instead.
     * @param text The text to measure
     * @return The exact width of the text
     */
    public float getStringWidth(final @NotNull String text)
    {
        final MutableFloat width = new MutableFloat();
        TextVisitFactory.visitForwards(text, Style.EMPTY, ((i, style, codePoint) ->
        {
            width.add(this.measureGlyph(i, codePoint));
            return true;
        }));
        return width.getValue();
    }
    
    /**
     * Gets the width of the string with the current font, but rounded up so all text fits perfectly.
     * Formatting codes will not be consumed and considered part of the string. If this is not the desired behaviour,
     * use {@link TextLayout} instead.
     * @param text The text to measure
     * @return The fitted width of the text
     */
    public int getStringWidthFitted(final @NotNull String text) { return MathHelper.ceil(this.getStringWidth(text)); }
    
    /**
     * Sets the font's scale override. The scale is applied relative to {@link #getRenderHeight()}, by that means, 1em
     * is considered 9 pixels so 2em means 18 pixels and so on.
     * @param size The size of the font
     * @see FontSize
     */
    public void setScale(final @NotNull FontSize size)
    {
        this.fontScale = size.getScaledValue(MinecraftClient.getInstance().textRenderer.fontHeight);
    }
    
    /**
     * Sets the font size in em scale. The scale is applied relative to {@link #getRenderHeight()},
     * by that means, 1em is considered 9 pixels so 2em means 18 pixels and so on.
     * @param em The size of the font
     */
    public void setScale(final float em) { this.fontScale = em; }
    
    /**
     * Sets this font's tracking factor.
     * <p>
     * The tracking factor is additional space that is added between glyphs uniformly. By default, this is 0,
     * which means no extra space is added, and anything above will be extra added space (see {@link #getTracking()}).
     * <p>
     * Do not that this is less kerning than font tracking, which applies the spacing uniformly across the characters.
     *
     * @param trackingFactor The tracking factor
     */
    public void getTrackingFactor(final float trackingFactor) { this.tracking = trackingFactor; }
    
    /**
     * Sets the formatting of this font to the given {@link Style} or {@link GuiStyle}. Font ID will be ignored, use
     * {@link GuiFont#GuiFont(Style)} instead.
     * <p>
     * When applying a style to a font, it is worth noting that styles that do not explicitly set a formatting
     * attribute, will, for those values, not override the font. By that means, if an attribute of a style is internally
     * {@code null}, this font will retain its current value for that style attribute, otherwise it will be overridden
     * regardless of its value.
     * @param style The {@link Style} to apply
     */
    public void setStyle(final @NotNull Style style)
    {
        final StyleAccessor accessor = (StyleAccessor) style;
        
        final Boolean bold          = accessor.novia$isBold();
        final Boolean italic        = accessor.novia$isItalic();
        final Boolean underline     = accessor.novia$isUnderlined();
        final Boolean strikethrough = accessor.novia$isStrikethrough();
        final Boolean obfuscated    = accessor.novia$isObfuscated();
        
        if (bold != null) {this.setBold(bold);}
        if (italic != null) {this.setItalic(italic);}
        if (underline != null) {this.setUnderlined(underline);}
        if (strikethrough != null) {this.setStrikethrough(strikethrough);}
        if (obfuscated != null) {this.setObfuscated(obfuscated);}
        
        if (style instanceof GuiStyle g_style)
        {
            if (g_style.shaded != null) {this.setShaded(g_style.shaded);}
            if (g_style.size != null) {this.setScale(g_style.size);}
            if (g_style.kerning != null) {this.getTrackingFactor(g_style.kerning);}
        }
    }
    
    /** Clears all format flags and makes this a plain font. */
    public void clearFormatting() {this.formats.clear();}
    
    //------------------------------------------------------------------------------------------------------------------
    float measureGlyph(final int i, final int codePoint)
    {
        final Glyph glyph = this.getGlyph(codePoint);
        final float kerning_offset = ((i > 0 && this.tracking != 0f)
                                          ? (GuiFont.getRenderHeight() * this.fontScale * this.tracking)
                                          : 0f);
        
        return (
            (glyph.getAdvance() * this.fontScale)
            + kerning_offset
            + (this.isBold() ? (glyph.getBoldOffset() * this.fontScale) : 0f)
        );
    }
    
    //==================================================================================================================
    @Override
    public String toString()
    {
        return MoreObjects
            .toStringHelper(this)
            .add("id", this.getId())
            .add("bold", this.isBold())
            .add("italic", this.isItalic())
            .add("underline", this.isUnderlined())
            .add("strikethrough", this.isStrikethrough())
            .add("obfuscated", this.isObfuscated())
            .add("shaded", this.isShaded())
            .add("size", this.getScale())
            .add("kerning", this.getTracking())
            .toString();
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this) {return true;}
        if (!(obj instanceof GuiFont other)) {return false;}
        return (
            this.getId().equals(other.getId())
            && this.formats.equals(other.formats)
            && this.fontScale == other.fontScale
            && this.tracking == other.tracking
        );
    }
    
    @Override
    public int hashCode() {return Objects.hash(this.getId(), this.formats, this.fontScale, this.tracking);}
}

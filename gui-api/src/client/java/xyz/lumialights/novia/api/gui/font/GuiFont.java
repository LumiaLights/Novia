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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.gui.impl.FontManagerAccessor;
import xyz.lumialights.novia.api.gui.impl.MinecraftClientAccessor;

import java.util.*;



//**********************************************************************************************************************
public class GuiFont
    extends TextHandler
{
    //******************************************************************************************************************
    /** Specifies the formatting flags for the font. */
    public enum Format
    {
        BOLD,
        ITALIC,
        STRIKETHROUGH,
        UNDERLINED,
        OBFUSCATED,
        SHADED
        ;
    }
    
    //******************************************************************************************************************
    /**
     * Finds a font from its ID of the font register.
     * @param fontId The ID of the font
     * @return The {@link GuiFont} or {@link GuiFont#getDefault()} if none was found for that ID
     */
    public static @NotNull GuiFont getFont(final @NotNull Identifier fontId)
    {
        return GuiFont.getOptionalFont(fontId).orElseGet(GuiFont::getDefault);
    }
    
    /**
     * Finds a font from its ID of the font register.
     * @param fontId The ID of the font
     * @return An optional {@link GuiFont} or an empty optional if no font was found for that ID
     */
    @SuppressWarnings("resource")
    public static @NotNull Optional<GuiFont> getOptionalFont(final @NotNull Identifier fontId)
    {
        final FontManager manager = ((MinecraftClientAccessor) MinecraftClient.getInstance()).novia$getFontManager();
        final FontStorage storage = ((FontManagerAccessor) manager).novia$getFontStorage(fontId);
        
        return Optional.ofNullable(!storage.getId().equals(FontManager.MISSING_STORAGE_ID)
            ? new GuiFont(storage)
            : null);
    }
    
    public static @NotNull GuiFont getDefault()
    {
        return GuiFont.getOptionalFont(MinecraftClient.DEFAULT_FONT_ID).orElseThrow();
    }
    
    public static @NotNull GuiFont getUnicode()
    {
        return GuiFont.getOptionalFont(MinecraftClient.UNICODE_FONT_ID).orElseThrow();
    }
    
    public static @NotNull GuiFont getAlt()
    {
        return GuiFont.getOptionalFont(MinecraftClient.ALT_TEXT_RENDERER_ID).orElseThrow();
    }
    
    public static @NotNull GuiFont getIllageralt()
    {
        return GuiFont.getOptionalFont(Identifier.ofVanilla("illageralt")).orElseThrow();
    }
    
    /**
     * Gets the standard render height of text in Minecraft, this is what is used to calculate text bounds ect.
     * <p>
     * This is not to be confused with {@link GuiFont#getSize(FontSize.Unit)}, which scales this natural render
     * height.
     *
     * @return {@link TextRenderer#fontHeight}
     */
    public static int getRenderHeight() { return MinecraftClient.getInstance().textRenderer.fontHeight; }
    
    //******************************************************************************************************************
    private final BitSet      formats = new BitSet(Format.values().length);
    private final FontStorage storage;
    
    private float fontScale = 1.0f;

    //******************************************************************************************************************
    /**
     * Constructs a new copy of the given font object.
     * @param other The {@link GuiFont} to copy from
     */
    public GuiFont(final @NotNull GuiFont other)
    {
        this(other.storage);

        this.fontScale = other.fontScale;
        this.formats.or(other.formats);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    GuiFont(final @NotNull FontStorage storage)
    {
        super((codePoint, style) -> storage.getGlyph(codePoint, false).getAdvance(style.isBold()));
        this.storage = storage;
    }
    
    //==================================================================================================================
    /**
     * Gets the {@link Identifier} of this font.
     * @return The {@link Identifier}
     */
    public final @NotNull Identifier getId() { return this.storage.getId(); }
    
    /**
     * Gets the (possibly) cached glyph for the given code point. If no glyph could be found, it will return
     * {@link BuiltinEmptyGlyph#MISSING}. (an in-built glyph represented by a white square)
     *
     * @param codePoint The code point to look up the glyph for
     * @return The {@link Glyph}
     */
    public final @NotNull Glyph getGlyph(final int codePoint) { return this.storage.getGlyph(codePoint, false); }
    
    /**
     * Gets the (possibly) cached glyph for the given code point. If no glyph could be found, it will return
     * an empty optional.
     *
     * @param codePoint The code point to look up the glyph for
     * @return An {@link Optional} containing the {@link Glyph}, or empty if no glyph was found
     */
    public final @NotNull Optional<Glyph> getOptionalGlyph(final int codePoint)
    {
        final Glyph glyph = this.getGlyph(codePoint);
        return Optional.ofNullable(glyph != BuiltinEmptyGlyph.MISSING ? glyph : null);
    }
    
    /**
     * Gets the (possibly) cached baked glyph for the given code point. If no glyph could be found, it will return
     * the baked version of {@link BuiltinEmptyGlyph#MISSING}. (an in-built glyph represented by a white square)
     *
     * @param codePoint The code point to look up the baked glyph for
     * @return The {@link BakedGlyph}
     */
    public final @NotNull BakedGlyph getBaked(final int codePoint) { return this.storage.getBaked(codePoint); }
    
    /**
     * Gets the obfuscated version of a given glyph.
     *
     * @param glyph The glyph to get an obfuscated baked glyph for
     * @return The obfuscated {@link BakedGlyph}
     */
    public final @NotNull BakedGlyph getObfuscatedBakedGlyph(final @NotNull Glyph glyph)
    {
        return this.storage.getObfuscatedBakedGlyph(glyph);
    }
    
    /**
     * Gets the baked rectangle glyph used to draw the lines for things like underlined or strike-through texts.
     * @return The rectangle {@link BakedGlyph}
     */
    public @NotNull BakedGlyph getRectangleBakedGlyph() { return this.storage.getRectangleBakedGlyph(); }
    
    /**
     * Gets the current scale applied to the font upon rendering.
     * @param unit The unit to convert to
     * @return The font size
     */
    public float getSize(final @NotNull FontSize.Unit unit)
    {
        return (this.fontScale / unit.scaleFunc.apply(MinecraftClient.getInstance().textRenderer.fontHeight));
    }

    /**
     * Gets the current scale applied to the font upon rendering in em unit (real scale).
     * @return The font size in em
     */
    public float getSize() { return this.fontScale; }

    /**
     * Gets the width of the given text as an {@link Integer} with enough headroom to fit the entire text.
     * @param string The text to determine
     * @return The width of the text
     */
    public int getWidthFitted(final @NotNull String string) { return (int) Math.ceil(this.getWidth(string)); }
    
    /**
     * Gets the width of the given text as an {@link Integer} with enough headroom to fit the entire text.
     * @param visitable The text to determine
     * @return The width of the text
     */
    public int getWidthFitted(final @NotNull StringVisitable visitable)
    {
        return (int) Math.ceil(this.getWidth(visitable));
    }
    
    /**
     * Gets the width of the given text as an {@link Integer} with enough headroom to fit the entire text.
     * @param text The text to determine
     * @return The width of the text
     */
    public int getWidthFitted(final @NotNull OrderedText text) { return (int) Math.ceil(this.getWidth(text)); }
    
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

    //==================================================================================================================
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
    public @NotNull GuiFont withFormatting(final @NotNull Format @NotNull ...formats)
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
     *
     * @param formats The set of {@link Format} flags to apply to the font
     * @return The new font
     */
    public @NotNull GuiFont withFormattingPreserved(final @NotNull Format @NotNull ...formats)
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
	 * Trims a string to be at most {@code maxWidth} wide.
	 * @return the trimmed string
	 */
	public @NotNull String trimToWidth(final @NotNull String text, final int maxWidth, final boolean backwards)
    {
		return (backwards
            ? this.trimToWidthBackwards(text, maxWidth, Style.EMPTY)
            : this.trimToWidth(text, maxWidth, Style.EMPTY));
	}

	/**
	 * Trims a string to be at most {@code maxWidth} wide.
	 * @return the trimmed string
	 * @see TextHandler#trimToWidth(String, int, Style)
	 */
	public @NotNull String trimToWidth(final @NotNull String text, final int maxWidth)
    {
		return this.trimToWidth(text, maxWidth, Style.EMPTY);
	}

	/**
	 * Trims a string to be at most {@code maxWidth} wide.
	 *
	 * @return the text
	 * @see TextHandler#trimToWidth(StringVisitable, int, Style)
	 */
	public @NotNull StringVisitable trimToWidth(final @NotNull StringVisitable text, final int width)
    {
		return this.trimToWidth(text, width, Style.EMPTY);
	}
 
	/**
	 * Gets the height of the text when it has been wrapped.
	 *
	 * @return the height of the wrapped text
	 * @see TextRenderer#wrapLines(StringVisitable, int)
	 * @see #getWrappedLinesHeight(StringVisitable, int)
	 */
	public int getWrappedLinesHeight(final @NotNull String text, final int maxWidth)
    {
		return (
            MinecraftClient.getInstance().textRenderer.fontHeight
            * this.wrapLines(text, maxWidth, Style.EMPTY).size()
        );
	}

	/**
	 * {@return the height of the text, after it has been wrapped, in pixels}
	 * @see TextRenderer#wrapLines(StringVisitable, int)
	 * @see #getWrappedLinesHeight(String, int)
	 */
	public int getWrappedLinesHeight(final @NotNull StringVisitable text, final int maxWidth)
    {
		return (
            MinecraftClient.getInstance().textRenderer.fontHeight
            * this.wrapLines(text, maxWidth, Style.EMPTY).size()
        );
	}

	/**
	 * Wraps text when the rendered width of text exceeds the {@code width}.
	 * @return a list of ordered text which has been wrapped
	 */
	public @NotNull List<OrderedText> wrapLines(final @NotNull StringVisitable text, final int width)
    {
		return Language.getInstance().reorder(this.wrapLines(text, width, Style.EMPTY));
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
     *
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
    public void setFormatting(final @NotNull Format @NotNull ...formats)
    {
        this.clearFormatting();
        this.addFormatting(formats);
    }

    /**
     * Adds a set of flags that should be enabled for this font.
     * @param formats The set of flags to enable
     */
    public void addFormatting(final @NotNull Format @NotNull ...formats)
    {
        for (final Format format : formats)
        {
            this.formats.set(format.ordinal(), true);
        }
    }
    
    /**
     * Sets the font size. This will affect the fonts scaling during drawing.
     * @param size The size of the font
     * @see FontSize
     */
    public void setSize(final @NotNull FontSize size)
    {
        this.fontScale = size.getScaledValue(MinecraftClient.getInstance().textRenderer.fontHeight);
    }

    /**
     * Sets the font size in em (real scale). This will affect the fonts scaling during drawing.
     * @param em The size of the font
     * @see FontSize
     */
    public void setSize(final float em) { this.fontScale = em; }

    /** Clears all format flags and makes this a plain font. */
    public void clearFormatting() { this.formats.clear(); }
}

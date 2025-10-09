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

import net.minecraft.client.font.Glyph;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;



//**********************************************************************************************************************
/**
 * Provides utilities that can be used in conjunction with {@link GuiFont}, this is similar to
 * {@link net.minecraft.client.font.TextHandler} but appropriated to work with Novia font objects.
 */
public abstract class FontUtil
{
    //******************************************************************************************************************
    private static class WidthLimitingVisitor
        implements IGuiCharacterVisitor
    {
        //**************************************************************************************************************
        private float widthLeft;
        private int   length;

        //**************************************************************************************************************
        public WidthLimitingVisitor(final int maxWidth) { this.widthLeft = maxWidth; }

        //==============================================================================================================
        public int getLength() { return this.length; }

        //==============================================================================================================
        public boolean accept(final int i, final @NotNull GuiFont font, final @NotNull Style style, final int codePoint)
        {
            this.widthLeft -= FontUtil.measureGlyph(i, font, codePoint);

            if (this.widthLeft >= 0.0F)
            {
                this.length = (i + Character.charCount(codePoint));
                return true;
            }

            return false;
        }

        public void resetLength() { this.length = 0; }
    }
    
    private static class LineBreakingVisitor
        implements IGuiCharacterVisitor
    {
        //**************************************************************************************************************
        private final float maxWidth;
        
        private int     lastSpaceBreak = -1;
        private int     endIndex       = -1;
        private int     count;
        private int     startOffset;
        private boolean nonEmpty;
        private float   totalWidth;
        
        //**************************************************************************************************************
        public LineBreakingVisitor(final float maxWidth) { this.maxWidth = Math.max(maxWidth, 1.0F); }
        
        //==============================================================================================================
        public int getEndingIndex() { return (this.hasLineBreak() ? this.endIndex : this.count); }
        
        //==============================================================================================================
        private boolean hasLineBreak() { return (this.endIndex != -1); }
        
        //==============================================================================================================
        public void addOffset(final int extraOffset) { this.startOffset += extraOffset; }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final @NotNull GuiFont font, final @NotNull Style style, final int codePoint)
        {
            final int line_index = (i + this.startOffset);
            
            if (codePoint == '\n')
            {
                this.endIndex = line_index;
                return false;
            }
            
            if (codePoint == ' ')
            {
                this.lastSpaceBreak = line_index;
            }
            
            final float char_width = FontUtil.measureGlyph(i, font, codePoint);
            this.totalWidth += char_width;
            
            if (!this.nonEmpty || this.totalWidth <= this.maxWidth)
            {
                this.nonEmpty |= (char_width != 0f);
                this.count     = (line_index + Character.charCount(codePoint));
                
                return true;
            }
            
            this.endIndex = (this.lastSpaceBreak != -1 ? this.lastSpaceBreak : line_index);
            return false;
        }
    }

	private static class LineWrappingCollector
    {
        //**************************************************************************************************************
		final List<StyledString> parts;
        
        //--------------------------------------------------------------------------------------------------------------
		private String joined;

        //**************************************************************************************************************
		public LineWrappingCollector(final @NotNull List<StyledString> parts)
        {
			this.parts  = parts;
			this.joined = parts.stream().map(part -> part.literal).collect(Collectors.joining());
		}

        //==============================================================================================================
		public char charAt(final int index) { return this.joined.charAt(index); }

        //==============================================================================================================
		public @NotNull StringVisitable collectLine(final int lineLength, final int skippedLength,
                                                    final @NotNull Style style)
        {
			final TextCollector              collector = new TextCollector();
			final ListIterator<StyledString> it        = this.parts.listIterator();
   
			int     i  = lineLength;
			boolean bl = false;

			while (it.hasNext())
            {
				final StyledString styled_string = it.next();
				final String       string        = styled_string.literal;
				final int          strlen        = string.length();
    
				if (!bl)
                {
					if (i > strlen)
                    {
						collector.add(styled_string);
						it.remove();
						i -= strlen;
					}
                    else
                    {
						final String string2 = string.substring(0, i);
      
						if (!string2.isEmpty())
                        {
							collector.add(StringVisitable.styled(string2, styled_string.style));
						}

						i += skippedLength;
						bl = true;
					}
				}

				if (bl)
                {
					if (i <= strlen)
                    {
						final String string2 = string.substring(i);
      
						if (string2.isEmpty())
                        {
							it.remove();
						}
                        else
                        {
							it.set(new StyledString(string2, style));
						}
      
						break;
					}

					it.remove();
					i -= strlen;
				}
			}

			this.joined = this.joined.substring(lineLength + skippedLength);
			return collector.getCombined();
		}

        //==============================================================================================================
		public @Nullable StringVisitable collectRemainders()
        {
			TextCollector textCollector = new TextCollector();
			this.parts.forEach(textCollector::add);
			this.parts.clear();
			return textCollector.getRawCombined();
		}
	}
    
    private record StyledString(@NotNull String literal, @NotNull Style style)
        implements StringVisitable
    {
        //**************************************************************************************************************
        @Override
        public <T> @NotNull Optional<T> visit(final @NotNull StringVisitable.Visitor<T> visitor)
        {
            return visitor.accept(this.literal);
        }
        
        @Override
        public <T> @NotNull Optional<T> visit(final @NotNull StringVisitable.StyledVisitor<T> styledVisitor,
                                              final @NotNull Style style)
        {
            return styledVisitor.accept(this.style.withParent(style), this.literal);
        }
    }
    
    //******************************************************************************************************************
    /**
     * Gets the exact width of the given text as float. This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text The text to measure
     * @return The width of the text
     */
    public static float getWidth(final @NotNull GuiFont baseFont, final @NotNull String text)
    {
        final MutableFloat result = new MutableFloat();
        IGuiCharacterVisitor
            .of((i, font, style, codePoint) ->
            {
                result.add(FontUtil.measureGlyph(i, font, codePoint));
                return true;
            })
            .visitFormatted(baseFont, text);
        
        return result.getValue();
    }

    /**
     * Gets the exact width of the given text as float. This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text The text to measure
     * @return The width of the text
     */
    public static float getWidth(final @NotNull GuiFont baseFont, final @NotNull StringVisitable text)
    {
        final MutableFloat result = new MutableFloat();
        IGuiCharacterVisitor
            .of((i, font, style, codePoint) ->
            {
                result.add(FontUtil.measureGlyph(i, font, codePoint));
                return true;
            })
            .visitFormatted(baseFont, text);
        
        return result.getValue();
    }
    
    /**
     * Gets the exact width of the given text as float. This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text     The text to measure
     * @return The width of the text
     */
    public static float getWidth(final @NotNull GuiFont baseFont, final @NotNull Text text)
    {
        return FontUtil.getWidth(baseFont, text.asOrderedText());
    }
    
    /**
     * Gets the exact width of the given text as float. This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text     The text to measure
     * @return The width of the text
     */
    public static float getWidth(final @NotNull GuiFont baseFont, final @NotNull OrderedText text)
    {
        final MutableFloat result = new MutableFloat();
        IGuiCharacterVisitor
            .of((i, font, style, codePoint) ->
            {
                result.add(FontUtil.measureGlyph(i, font, codePoint));
                return true;
            })
            .visitFormatted(baseFont, text);
        
        return result.getValue();
    }
    
    /**
     * Gets the width of the given text as rounded up integer so that there is enough space for a little overshoot.
     * This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text The text to measure
     * @return The width of the text
     */
    public static int getWidthFitted(final @NotNull GuiFont baseFont, final @NotNull String text)
    {
        return MathHelper.ceil(FontUtil.getWidth(baseFont, text));
    }
    
    /**
     * Gets the width of the given text as rounded up integer so that there is enough space for a little overshoot.
     * This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text The text to measure
     * @return The width of the text
     */
    public static int getWidthFitted(final @NotNull GuiFont baseFont, final @NotNull StringVisitable text)
    {
        return MathHelper.ceil(FontUtil.getWidth(baseFont, text));
    }
    
    /**
     * Gets the width of the given text as rounded up integer so that there is enough space for a little overshoot.
     * This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text The text to measure
     * @return The width of the text
     */
    public static int getWidthFitted(final @NotNull GuiFont baseFont, final @NotNull Text text)
    {
        return MathHelper.ceil(FontUtil.getWidth(baseFont, text));
    }
    
    /**
     * Gets the width of the given text as rounded up integer so that there is enough space for a little overshoot.
     * This WILL consume format codes.
     * @param baseFont The base font to use if not overridden by style attributes
     * @param text The text to measure
     * @return The width of the text
     */
    public static int getWidthFitted(final @NotNull GuiFont baseFont, final @NotNull OrderedText text)
    {
        return MathHelper.ceil(FontUtil.getWidth(baseFont, text));
    }
    
    /**
     * Gets the string length of the given text with the specified font that is trimmed to the specified width. This
     * will NOT consume format codes.
     * <p>
     * Other than {@link #getTrimmedLengthFormatted(GuiFont, String, int)}, this ignores any formatting codes inside the
     * string.
     * @param font     The font to use for the text
     * @param text     The text to determine the length of
     * @param maxWidth The maximum width of the area that should be trimmed to
     * @return The length of the text trimmed to the given width
     */
    public static int getTrimmedLength(final @NotNull GuiFont font, final @NotNull String text, final int maxWidth)
    {
        final WidthLimitingVisitor visitor = new WidthLimitingVisitor(maxWidth);
        visitor.visit(font, text);
        return visitor.getLength();
    }

    /**
     * Gets the string length of the given text with the specified font that is trimmed to the specified width. This
     * WILL consume format codes.
     * <p>
     * Other than {@link #getTrimmedLength(GuiFont, String, int)}, this does NOT ignore formatting codes inside the
     * string (see <a href="https://minecraft.fandom.com/wiki/Formatting_codes">Formatting codes</a>).
     * @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
     * @param text     The text to determine the length of
     * @param maxWidth The maximum width of the area that should be trimmed to
     * @return The length of the text trimmed to the given width
     */
    public static int getTrimmedLengthFormatted(final @NotNull GuiFont baseFont,
                                                final @NotNull String  text,
                                                final          int     maxWidth)
    {
        final WidthLimitingVisitor visitor = new WidthLimitingVisitor(maxWidth);
        visitor.visitFormatted(baseFont, text);
        return visitor.getLength();
    }
    
    /**
     * Gets the index of the last character of a string, that still fit into the maximum specified width. This WILL
     * consume format codes.
     * @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
     * @param text     The text to get the index of
     * @param maxWidth The maximum width the text can fit in
     * @return The last index of the fitted text
     */
    public static int getEndingIndex(final @NotNull GuiFont baseFont, final @NotNull String text, final int maxWidth)
    {
		final LineBreakingVisitor visitor = new LineBreakingVisitor(maxWidth);
        visitor.visitFormatted(baseFont, text);
		return visitor.getEndingIndex();
	}
    
    //------------------------------------------------------------------------------------------------------------------
    private static float measureGlyph(final int i, final @NotNull GuiFont font, final int codePoint)
    {
        final float scale          = font.getScale();
        final float kerning        = font.getTracking();
        final Glyph glyph          = font.getGlyph(codePoint);
        final float kerning_offset = ((i > 0 && kerning != 0f)
            ? (GuiFont.getRenderHeight() * scale * kerning)
            : 0f);
        
        return (
            (glyph.getAdvance() * scale)
            + kerning_offset
            + (font.isBold() ? (glyph.getBoldOffset() * scale) : 0f)
        );
    }
    
    //==================================================================================================================
    /**
     * Trims the given text to fit into specified width. This will NOT consume format codes.
     * @param font     The font to use
     * @param text     The text to trim
     * @param maxWidth The maximum width that the text cannot exceed
     * @return The trimmed text
     */
    public static @NotNull String trimToWidth(final @NotNull GuiFont font,
                                              final @NotNull String  text,
                                              final          int     maxWidth)
    {
        return text.substring(0, FontUtil.getTrimmedLength(font, text, maxWidth));
    }

    /**
     * Trims the given text to fit into specified width. This WILL consume format codes.
     * @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
     * @param text     The text to trim
     * @param maxWidth The maximum width that the text cannot exceed
     * @return The trimmed text
     */
    public static @NotNull String trimToWidthFormatted(final @NotNull GuiFont baseFont,
                                                       final @NotNull String  text,
                                                       final          int     maxWidth)
    {
        return text.substring(0, FontUtil.getTrimmedLengthFormatted(baseFont, text, maxWidth));
    }
    
    /**
     * Trims the given {@link StringVisitable} to fit into specified width. This WILL consume format codes.
     * @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
     * @param text     The {@link StringVisitable} to trim
     * @param maxWidth The maximum width that the text cannot exceed
     * @return The trimmed {@link StringVisitable}
     */
    public static @NotNull StringVisitable trimToWidth(final @NotNull GuiFont         baseFont,
                                                       final @NotNull StringVisitable text,
                                                       final          int             maxWidth)
    {
        final WidthLimitingVisitor visitor = new WidthLimitingVisitor(maxWidth);
        return text
            .visit(
                new StringVisitable.StyledVisitor<StringVisitable>()
                {
                    //**************************************************************************************************
                    private final TextCollector collector = new TextCollector();

                    //**************************************************************************************************
                    public @NotNull Optional<StringVisitable> accept(final @NotNull Style style,
                                                                     final @NotNull String string)
                    {
                        visitor.resetLength();

                        if (!visitor.visitFormatted(baseFont, string))
                        {
                            final String string2 = string.substring(0, visitor.getLength());

                            if (!string2.isEmpty())
                            {
                                this.collector.add(StringVisitable.styled(string2, style));
                            }

                            return Optional.of(this.collector.getCombined());
                        }

                        if (!string.isEmpty())
                        {
                            this.collector.add(StringVisitable.styled(string, style));
                        }

                        return Optional.empty();
                    }
                },
                Style.EMPTY)
            .orElse(text);
    }
    
    /**
     * Trims the given string to fit into specified width but trimming on the front instead of on the back. This will
     * NOT consume format codes.
     * @param font     The font to use
     * @param text     The text to trim
     * @param maxWidth The maximum width that the text cannot exceed
     * @return The front trimmed string
     */
    public static @NotNull String trimToWidthBackwards(final @NotNull GuiFont font,
                                                       final @NotNull String  text,
                                                       final          int     maxWidth)
    {
        final MutableFloat total_width = new MutableFloat();
        final MutableInt   end_index   = new MutableInt(text.length());

        IGuiCharacterVisitor
            .of((i, font2, style, codePoint) ->
            {
                final float width = total_width.addAndGet(FontUtil.measureGlyph(i, font2, codePoint));
    
                if (width > ((float) maxWidth))
                {
                    return false;
                }
    
                end_index.setValue(i);
                return true;
            })
            .visitBackwards(font, text);

        return text.substring(end_index.intValue());
    }
    
    //==================================================================================================================
    /**
     * Takes the given string and wraps it over multiple lines, if the text exceeds {@code maxWidth}.
     * <p>
     * This WILL consume format codes, by that means, they will be ignored during line width calculation. However,
     * they will still be part of the substring-range passed to the consumer, so the string will still have to be
     * formatted afterward.
     * @param baseFont               The base {@link GuiFont} to use if not overridden by style attributes
     * @param text                   The text to wrap
     * @param maxWidth               The maximum width a line can have
     * @param keepTrailingWhitespace Whether whitespace at the end of a line (new line and space) should be kept or
     *                               discarded
     * @param consumer               The consumer that consumes the line with the first parameter being the substring's
     *                               start index (inclusive) and the second the end index (exclusive)
     */
    public static void wrapLines(final @NotNull GuiFont                      baseFont,
                                 final @NotNull String                       text,
                                 final          int                          maxWidth,
                                 final          boolean                      keepTrailingWhitespace,
                                 final @NotNull BiConsumer<Integer, Integer> consumer)
    {
        final int strlen = text.length();
        
        for (int i = 0; i < strlen;)
        {
            final LineBreakingVisitor visitor = new LineBreakingVisitor(maxWidth);
            
			if (TextVisitFactory.visitFormatted(text, i, Style.EMPTY, visitor.asStyledVisitor(baseFont)))
            {
				consumer.accept(i, strlen);
				break;
			}
   
			final int  line_end = visitor.getEndingIndex();
			final char end_char = text.charAt(line_end);
			final int  end_i    = ((end_char != '\n' && end_char != ' ') ? line_end : (line_end + 1));
   
			consumer.accept(i, (keepTrailingWhitespace ? end_i : line_end));
			i = end_i;
        }
	}
    
    public static @NotNull List<StringVisitable> wrapLines(final @NotNull GuiFont baseFont,
                                                           final @NotNull String  text,
                                                           final          int     maxWidth)
    {
        final List<StringVisitable> list = new ArrayList<>();
        FontUtil.wrapLines(
            baseFont,
            text,
            maxWidth,
            false,
            ((style, start, end) -> list.add(StringVisitable.styled(text.substring(start, end), style))));
        return list;
    }

    public static void wrapLines(final @NotNull GuiFont                              baseFont,
                                 final @NotNull StringVisitable                      text,
                                 final          int                                  maxWidth,
                                 final @NotNull BiConsumer<StringVisitable, Boolean> lineConsumer)
    {
		final List<StyledString> list = new ArrayList<>();
		
        text.visit(((style, string) ->
        {
			if (!string.isEmpty())
            {
				list.add(new StyledString(string, style));
			}

			return Optional.empty();
		}), Style.EMPTY);
  
		final LineWrappingCollector collector = new LineWrappingCollector(list);
  
		boolean has_more          = true;
		boolean last_line_empty   = false;
		boolean last_line_wrapped = false;

		while (has_more)
        {
			has_more = false;
   
			final LineBreakingVisitor visitor = new LineBreakingVisitor(maxWidth);

			for (final var styled : collector.parts)
            {
				final boolean exceeded = !TextVisitFactory.visitFormatted(styled.literal, 0, styled.style, Style.EMPTY,
                                                                          visitor.asStyledVisitor(baseFont));
    
				if (exceeded)
                {
					final int     end_i         = visitor.getEndingIndex();
					final Style   end_style     = visitor.getEndingStyle();
					final char    end_char      = collector.charAt(end_i);
					final boolean is_new_line   = (end_char == '\n');
					final boolean is_whitespace = (is_new_line || end_char == ' ');
     
					final StringVisitable visitable = collector.collectLine(end_i, (is_whitespace ? 1 : 0), end_style);
					lineConsumer.accept(visitable, last_line_wrapped);
     
					last_line_empty   = is_new_line;
					last_line_wrapped = !is_new_line;
					has_more          = true;
     
					break;
				}

				visitor.addOffset(styled.literal.length());
			}
		}

		final StringVisitable visitable = collector.collectRemainders();
  
		if (visitable != null)
        {
			lineConsumer.accept(visitable, last_line_wrapped);
		}
        else if (last_line_empty)
        {
			lineConsumer.accept(StringVisitable.EMPTY, false);
		}
	}
 
	public static @NotNull List<StringVisitable> wrapLines(final @NotNull GuiFont         baseFont,
                                                           final @NotNull StringVisitable text,
                                                           final          int             maxWidth)
    {
        final List<StringVisitable> list = new ArrayList<>();
		FontUtil.wrapLines(baseFont, text, maxWidth, (part, lastLineWrapped) -> list.add(part));
		return list;
	}

	public static @NotNull List<StringVisitable> wrapLines(final @NotNull GuiFont         baseFont,
                                                           final @NotNull StringVisitable text,
                                                           final          int             maxWidth,
                                                           final @NotNull StringVisitable wrappedLinePrefix)
    {
        final List<StringVisitable> list = new ArrayList<>();
		FontUtil.wrapLines(
            baseFont,
            text,
            maxWidth,
            ((part, lastLineWrapped) -> list.add(lastLineWrapped
                ? StringVisitable.concat(wrappedLinePrefix, part)
                : part)));
        
		return list;
	}
    
    //******************************************************************************************************************
    private FontUtil() {}
}

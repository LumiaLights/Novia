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
import net.minecraft.text.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.CharBuffer;
import java.util.*;



//**********************************************************************************************************************
public class TextLayout
{
    //******************************************************************************************************************
    /**
     * @param glyph     The {@link Glyph} object from the font
     * @param codePoint The code point of the glyph
     * @param width     The width of the glyph
     * @param offsetX   The left-hand offset from the previous glyph, usually extra tracking space
     */
    public record CodePoint(
        @NotNull Glyph glyph,
                 int   codePoint,
                 float width,
                 float offsetX
    ) {}
    
    /**
     * @param codePoints   The list of codepoints for this run
     * @param font         The font used to render the text
     * @param color        The colour used to render the text
     * @param shadowColour The shadow colour used to render the text
     * @param left         The x coordinate on the current line of this run
     * @param baseline     The baseline of the font that this run is using
     */
    public record Run(
        @NotNull  List<CodePoint> codePoints,
        @NotNull  GuiFont         font,
        @Nullable TextColor       color,
        @Nullable Integer         shadowColour,
                  float           left,
                  float           baseline
    ) {}
    
    /**
     * @param runs     The collection of text runs this line contains
     * @param baseline The absolute baseline of where the line's glyphs are positioned around in the entire layout
     * @param ascent   The ascent of the biggest character, upwards from the baseline
     * @param descent  The descent of the biggest character, downwards from the baseline
     * @param leading  The extra space factor between this and the next line
     *                 (where 1.0 means normal spacing, 2.0 means double and so on)
     */
    public record Line(
        @NotNull List<Run> runs,
                 float     baseline,
                 float     ascent,
                 float     descent,
                 float     leading
    ) {}
    
    public enum WordWrap
    {
        /** Do not wrap, discard everything. */
        NO_WRAP,
        
        /** Wrap whenever possible. */
        BREAK_WORD,

        /** Wrap only on spaces or single words that are too long for a line. */
        BREAK_SPACE,
    }
    
    public static class LayoutOptions
    {
        //**************************************************************************************************************
        /** The maximum width of the layout, exceeding characters will be broken on to the next line. */
        float maxWidth = Float.MAX_VALUE;
        
        /** The maximum height of the layout, anything beyond will be discarded. */
        float maxHeight = Float.MAX_VALUE;
        
        /**
         * Additional spacing between lines, if negative, lines will be closer together. This value is a factor where
         * 1.0 means normal spacing and anything else is a multiple of that.
         */
        float lineSpacing = 1f;

        /**
         * If {@link WordWrap#BREAK_WORD} is being used, determines whether a dash should be added at the end of the
         * breakpoint of the word, if a word is broken over multiple lines.
         */
        boolean appendDashToBrokenWord = true;
        
        /**
         * If {@link WordWrap#NO_WRAP} is being used, if this is {@code true}, new lines will be consumed, otherwise
         * new lines will be replaced with spaces.
         */
        boolean consumeNewLine = false;
        
        /** The wrap policy of lines. */
        @NotNull WordWrap wordWrap = WordWrap.BREAK_WORD;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private interface WrapHelper
        extends TextFormat.Visitor
    {
        //**************************************************************************************************************
        @NotNull List<Line> getLines();
    }
    
    private class WordBreakWrapper
        implements WrapHelper
    {
        //**************************************************************************************************************
        private final Deque<Line> lines;
        
        //**************************************************************************************************************
        public WordBreakWrapper(final int prealloc) { this.lines = new ArrayDeque<>(prealloc); }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final @NotNull TextFormat.Node node, final @NotNull CharBuffer content)
        {
            return true;
        }
        
        //==============================================================================================================
        @Override public @NotNull List<Line> getLines() { return new ArrayList<>(this.lines); }
    }
    
    private class SpaceBreakWrapper
        implements WrapHelper
    {
        //**************************************************************************************************************
        private final Deque<Line> lines;
        
        //**************************************************************************************************************
        public SpaceBreakWrapper(final int prealloc) { this.lines = new ArrayDeque<>(prealloc); }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final @NotNull TextFormat.Node node, final @NotNull CharBuffer content)
        {
            return true;
        }
        
        //==============================================================================================================
        @Override public @NotNull List<Line> getLines() { return new ArrayList<>(this.lines); }
    }
    
    private class NoBreakWrapper
        implements WrapHelper
    {
        //**************************************************************************************************************
        private final List<Run> runs;
        
        private boolean first         = true;
        private float   left          = 0;
        private float   maxLineHeight = Integer.MIN_VALUE;
        private float   maxAscent     = Integer.MIN_VALUE;
        private float   maxDescent    = Integer.MIN_VALUE;
        
        //**************************************************************************************************************
        public NoBreakWrapper(final int prealloc) { this.runs = new ArrayList<>(prealloc); }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final @NotNull TextFormat.Node node, final @NotNull CharBuffer content)
        {
            final Deque<CodePoint> code_points = new ArrayDeque<>(content.length());
            final var              it          = content.codePoints().iterator();
            final float            tracking    = TextLayout.getTracking(node.font());
            final float            scale       = node.font().getScale();
            final float            start       = this.left;
            
            boolean quit = false;
            
            while (it.hasNext())
            {
                final int c = it.next();
                
                if (c == '\n' && TextLayout.this.layoutOptions.consumeNewLine)
                {
                    continue;
                }
                
                final Glyph glyph = node.font().getGlyph((c == '\n' ? ' ' : c));
                final float width = ((glyph.getAdvance() + (node.font().isBold() ? glyph.getBoldOffset() : 0f)) * scale);
                final float offset;
                
                if (this.first)
                {
                    this.first = false;
                    offset     = 0f;
                }
                else
                {
                    offset = tracking;
                }
                
                this.left += (offset + width);
                
                if (this.left > TextLayout.this.layoutOptions.maxWidth)
                {
                    quit = true;
                    break;
                }
                
                code_points.add(new CodePoint(glyph, c, width, (tracking)));
            }
            
            final FontMetrics metrics = node.font().getMetrics();
            
            this.maxLineHeight = Math.max(this.maxLineHeight, (metrics.height()  * scale));
            this.maxAscent     = Math.max(this.maxAscent,     (metrics.ascent()  * scale));
            this.maxDescent    = Math.max(this.maxDescent,    (metrics.descent() * scale));
            
            final float baseline = (metrics.baseline() * scale);
            this.runs.add(new Run(new ArrayList<>(code_points), node.font(), node.colour(), node.shadowColour(), start,
                                  baseline));
            
            return !quit;
        }
        
        //==============================================================================================================
        @Override
        public @NotNull List<Line> getLines()
        {
            return new ArrayList<>(List.of(new Line(
                this.runs,
                this.maxAscent,
                this.maxAscent,
                this.maxDescent,
                ( * TextLayout.this.layoutOptions.lineSpacing))));
        }
    }
    
    //******************************************************************************************************************
    private static float getTracking(final @NotNull GuiFont font)
    {
        return (font.getMetrics().height() * font.getScale() * font.getTracking());
    }
    
    //******************************************************************************************************************
    private final List<Line>    lines = new ArrayList<>();
    private final LayoutOptions layoutOptions;
    
    //******************************************************************************************************************
    public TextLayout(final @NotNull TextFormat textFormat, final @NotNull LayoutOptions options)
    {
        this.layoutOptions = options;
        this.computeLayout(textFormat);
    }
    
    //==================================================================================================================
    public void computeLayout(final @NotNull TextFormat textFormat)
    {
        this.lines.clear();
        
        if (textFormat.isEmpty())
        {
            return;
        }
        
        final WrapHelper wrapper = switch (this.layoutOptions.wordWrap)
        {
            case BREAK_WORD  -> new WordBreakWrapper (StringUtils.countMatches(textFormat.getContent(), '\n') + 1);
            case BREAK_SPACE -> new SpaceBreakWrapper(StringUtils.countMatches(textFormat.getContent(), '\n') + 1);
            case NO_WRAP     -> new NoBreakWrapper   (textFormat.getNodeCount());
        };
        
        textFormat.visit(wrapper);
        this.lines.addAll(wrapper.getLines());
    }
}

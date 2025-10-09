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
package xyz.lumialights.novia.api.gui.mixin.client;

import net.minecraft.client.font.BitmapFont;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.client.font.UnihexFont;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.lumialights.novia.api.gui.impl.GlyphMetricsExtension;
import xyz.lumialights.novia.api.gui.impl.TrueTypeFontAccessor;



//**********************************************************************************************************************
@Mixin(Glyph.class)
public interface GlyphMixin
    extends GlyphMetricsExtension
{
    //******************************************************************************************************************
    @Mixin(BitmapFont.BitmapFontGlyph.class)
    abstract class BitmapGlyphMixin
        implements GlyphMetricsExtension
    {
        //**************************************************************************************************************
        @Shadow @Final int   width;
        @Shadow @Final int   height;
        @Shadow @Final int   ascent;
        @Shadow @Final float scaleFactor;
        
        //**************************************************************************************************************
        @Unique public float getOversample() { return (1f / this.scaleFactor); }
        
        //==============================================================================================================
        @Override public float novia$getBaseline() { return this.ascent;                         }
        @Override public float novia$getLeft()     { return 0f;                                  }
        @Override public float novia$getTop()      { return (7.0f - this.ascent);                }
        @Override public float novia$getRight()    { return (this.width / this.getOversample()); }
        
        @Override
        public float novia$getBottom()
        {
            return (this.novia$getTop() + (this.height / this.getOversample()));
        }
    }
    
    @Mixin(TrueTypeFont.TtfGlyph.class)
    abstract class TrueTypeGlyphMixin
        implements GlyphMetricsExtension
    {
        //**************************************************************************************************************
        @Shadow @Final int          width;
        @Shadow @Final int          height;
        @Shadow @Final float        ascent;
        @Shadow @Final float        bearingX;
        @Shadow @Final TrueTypeFont field_2336;
        
        //**************************************************************************************************************
        @Unique public float getOversample() { return ((TrueTypeFontAccessor) this.field_2336).novia$getOversample(); }
        
        //==============================================================================================================
        @Override public float novia$getBaseline() { return this.ascent;          }
        @Override public float novia$getLeft()     { return this.bearingX;        }
        @Override public float novia$getTop()      { return (7.0f - this.ascent); }
        
        @Override
        public float novia$getRight()
        {
            return (this.novia$getLeft() + (this.width / this.getOversample()));
        }
        
        @Override
        public float novia$getBottom()
        {
            return (this.novia$getTop() + (this.height / this.getOversample()));
        }
    }
    
    @Mixin(UnihexFont.UnicodeTextureGlyph.class)
    abstract class UnihexGlyphMixin
        implements GlyphMetricsExtension
    {
        //**************************************************************************************************************
        @Unique private static final float OVERSAMPLE         = 0.5f;
        @Unique private static final int   UNIHEX_HEIGHT      = 16;
        @Unique private static final float OVERSAMPLED_HEIGHT = (UNIHEX_HEIGHT * OVERSAMPLE);
        
        //**************************************************************************************************************
        @Shadow public abstract int width();
        
        //==============================================================================================================
        @Override public float novia$getBaseline() { return 7f;                                           }
        @Override public float novia$getLeft()     { return 0f;                                           }
        @Override public float novia$getTop()      { return 0f;                                           }
        @Override public float novia$getRight()    { return (this.width() * UnihexGlyphMixin.OVERSAMPLE); }
        @Override public float novia$getBottom()   { return UnihexGlyphMixin.OVERSAMPLED_HEIGHT;          }
    }
}

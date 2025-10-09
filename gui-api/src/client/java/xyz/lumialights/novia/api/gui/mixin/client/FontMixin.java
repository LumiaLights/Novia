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

import net.minecraft.client.font.*;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Size_Metrics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.lumialights.novia.api.gui.font.FontMetrics;
import xyz.lumialights.novia.api.gui.impl.FontMetricsExtension;
import xyz.lumialights.novia.api.gui.impl.TrueTypeFontAccessor;

import java.nio.ByteBuffer;
import java.util.Objects;



//**********************************************************************************************************************
@Mixin(Font.class)
public interface FontMixin
    extends FontMetricsExtension
{
    //******************************************************************************************************************
    @Mixin(BitmapFont.class)
    abstract class BitmapFontMixin
        implements FontMetricsExtension
    {
        //**************************************************************************************************************
        @Unique FontMetrics metrics;
        
        //**************************************************************************************************************
        @Inject(method = "<init>", at = @At("TAIL"))
        public void initMetrics(final @NotNull NativeImage                                image,
                                final @NotNull GlyphContainer<BitmapFont.BitmapFontGlyph> glyphs,
                                final @NotNull CallbackInfo                               ci)
        {
            final var it = glyphs.getProvidedGlyphs().intIterator();
            
            if (it.hasNext())
            {
                final BitmapFont.BitmapFontGlyph glyph      = Objects.requireNonNull(glyphs.get(it.nextInt()));
                final float                      oversample = (1f / glyph.scaleFactor());
                this.metrics = new FontMetrics((glyph.height() / oversample), (glyph.ascent() / oversample));
            }
            else
            {
                this.metrics = new FontMetrics(8f, 7f);
            }
        }
        
        //==============================================================================================================
        @Override public @NotNull FontMetrics novia$getMetrics() { return this.metrics; }
    }
    
    @Mixin(TrueTypeFont.class)
    abstract class TrueTypeFontMixin
        implements
            FontMetricsExtension,
            TrueTypeFontAccessor
    {
        //**************************************************************************************************************
        @Unique private FontMetrics metrics;
        
        //==============================================================================================================
        @Shadow @Final float oversample;
        
        //**************************************************************************************************************
        @Inject(method = "<init>", at = @At("TAIL"))
        public void initMetrics(final @NotNull ByteBuffer   buffer,
                                final @NotNull FT_Face      face,
                                final          float        size,
                                final          float        oversample,
                                final          float        shiftX,
                                final          float        shiftY,
                                final @NotNull String       excludedCharacters,
                                final @NotNull CallbackInfo ci)
        {
            final FT_Size_Metrics metrics = Objects.requireNonNull(face.size()).metrics();
            final float           ascent  = ((int) (face.ascender()          * (metrics.y_scale() / 65536.0)) >> 6);
            final float           descent = ((int) Math.abs(face.descender() * (metrics.y_scale() / 65536.0)) >> 6);
            this.metrics = new FontMetrics(((ascent + descent) / this.oversample), (ascent / this.oversample));
        }
        
        //==============================================================================================================
        @Override public @NotNull FontMetrics novia$getMetrics() { return this.metrics; }
        @Override public float novia$getOversample() { return this.oversample; }
    }
    
    @Mixin(UnihexFont.class)
    abstract class UnihexFontMixin
        implements FontMetricsExtension
    {
        //**************************************************************************************************************
        @Unique FontMetrics metrics;
        
        //**************************************************************************************************************
        @Inject(method = "<init>", at = @At("TAIL"))
        public void initMetrics(final @NotNull GlyphContainer<UnihexFont.UnicodeTextureGlyph> glyphs,
                                final @NotNull CallbackInfo                                   ci)
        {
            // doesn't apply to all fonts but... at this point I don't care any longer
            this.metrics = new FontMetrics(8f, 7f);
        }
        
        //==============================================================================================================
        @Override public @NotNull FontMetrics novia$getMetrics() { return this.metrics; }
    }
}

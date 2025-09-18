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
package xyz.lumialights.novia.api.core.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Range;
import org.jetbrains.annotations.NotNull;



//**********************************************************************************************************************
public record NormalisedRange(double min, double max, double step, double skew)
{
    //******************************************************************************************************************
    /** Represents the full {@code double} range (-{@link Double#MAX_VALUE} to {@link Double#MAX_VALUE}). */
    public static final NormalisedRange FULL_RANGE = new NormalisedRange(-Double.MAX_VALUE, Double.MAX_VALUE);
    
    /** Represents the full positive {@code double} range (0 to {@link Double#MAX_VALUE}). */
    public static final NormalisedRange POSITIVE_RANGE = new NormalisedRange(0, Double.MAX_VALUE);
    
    /** Represents the full negative {@code double} range (-{@link Double#MAX_VALUE} to -0). */
    public static final NormalisedRange NEGATIVE_RANGE = new NormalisedRange(-Double.MAX_VALUE, 0);
    
    /** The serialisation coded for this class. */
    public static final Codec<NormalisedRange> CODEC = RecordCodecBuilder.create(instance -> instance
        .group(
            Codec.DOUBLE
                .optionalFieldOf("min", -Double.MAX_VALUE)
                .forGetter(NormalisedRange::min),
            Codec.DOUBLE
                .optionalFieldOf("max", Double.MAX_VALUE)
                .forGetter(NormalisedRange::max),
            Codec.DOUBLE
                .optionalFieldOf("step", 1.0)
                .forGetter(NormalisedRange::step),
            Codec.DOUBLE
                .optionalFieldOf("skew", 1.0)
                .forGetter(NormalisedRange::skew))
        .apply(instance, NormalisedRange::new));
    
    /** The packet coded for this class. */
    public static final PacketCodec<PacketByteBuf, NormalisedRange> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.DOUBLE, NormalisedRange::min,
        PacketCodecs.DOUBLE, NormalisedRange::max,
        PacketCodecs.DOUBLE, NormalisedRange::step,
        PacketCodecs.DOUBLE, NormalisedRange::skew,
        NormalisedRange::new);
    
    //******************************************************************************************************************
    public NormalisedRange
    {
        if (min > max)
        {
            throw new IllegalArgumentException("Minimum %s was bigger than maximum %s".formatted(min, max));
        }
        
        if (step < 0)
        {
            throw new IllegalArgumentException("Step cannot be negative");
        }
        
        if (skew <= 0)
        {
            throw new IllegalArgumentException("Skew cannot be negative and must be greater than zero");
        }
    }
    
    public NormalisedRange(final @NotNull Range<Double> range) { this(range, 0.0, 1.0); }
    
    public NormalisedRange(final double start, final double end) { this(start, end, 0.0, 1.0); }
    
    public NormalisedRange(final @NotNull Range<Double> range, final double step) { this(range, step, 1.0); }
    
    public NormalisedRange(final double start, final double end, final double step) { this(start, end, step, 1.0); }
    
    public NormalisedRange(final @NotNull Range<Double> range, final double step, final double skew)
    {
        this(range.minInclusive(), range.maxInclusive(), step, skew);
    }
    
    //==================================================================================================================
    public double getDistance() { return (this.max - this.min); }
    
    public @NotNull Range<Double> getRange() { return new Range<>(this.min, this.max); }
    
    //==================================================================================================================
    public boolean isInRange(final @NotNull Double value) { return (value >= this.min && value <= this.max); }
    
    //==================================================================================================================
    public double normalise(final double value)
    {
        final double normalised = (double) Math.clamp(((value - this.min) / this.getDistance()), 0.0, 1.0);
        
        if (this.skew == 1.0)
        {
            return normalised;
        }
        
        return (double) Math.pow(normalised, this.skew);
    }
    
    public double denormalise(final double normalised)
    {
        double adjusted = Math.clamp(normalised, 0.0, 1.0);
        
        if (this.skew != 1.0 && adjusted > 0.0)
        {
            adjusted = (float) Math.exp(Math.log(adjusted) / this.skew);
        }
        
        return (this.min + (this.getDistance() * adjusted));
    }
    
    public double clamp(final double value) { return Math.clamp(value, this.min, this.max); }
    
    public double snap(double value)
    {
        if (this.step > 0.0)
        {
            value = (this.min + (this.step * Math.floor((value - this.min) / this.step + 0.5)));
        }
        
        return ((value <= this.min || this.max == this.min) ? this.min : Math.min(value, this.max));
    }
}

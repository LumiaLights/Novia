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
package xyz.lumialights.novia.api.gui.canvas.brush;

import com.mojang.blaze3d.textures.GpuTextureView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.canvas.ICanvasState;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumialights.novia.api.gui.canvas.renderer.IShapedRender;

import java.util.Objects;



//**********************************************************************************************************************
/// Describes the fill that rendered shapes using [IShapedRender] use to fill their vertices. There is three types of
/// brush fills, each of which can be found in [FillType].
public final class Brush
{
    //******************************************************************************************************************
    private FillType       type;
    private Integer        colour;
    private Gradient       gradient;
    private GpuTextureView texture;
    
    //******************************************************************************************************************
    /// Constructs a new solid colour fill brush.
    /// @param colour The solid colour to fill
    public Brush(final int colour)
    {
        this.type     = FillType.SOLID;
        this.colour   = colour;
        this.gradient = null;
        this.texture  = null;
    }
    
    /// Constructs a new solid colour fill brush.
    /// @param colour The solid [Colour] to fill
    public Brush(final @NotNull Colour colour) { this(colour.colour()); }
    
    /// Constructs a new gradient fill brush.
    /// @param gradient The [Gradient] to fill
    public Brush(final @NotNull Gradient gradient)
    {
        this.type     = FillType.GRADIENT;
        this.colour   = null;
        this.gradient = Objects.requireNonNull(gradient, "gradient must not be null");
        this.texture  = null;
    }
    
    /// Constructs a new texture fill brush.
    /// @param texture The [GpuTextureView] to fill
    public Brush(final @NotNull GpuTextureView texture)
    {
        this.type     = FillType.TEXTURED;
        this.colour   = null;
        this.gradient = null;
        this.texture  = Objects.requireNonNull(texture, "texture must not be null");
    }
    
    /// Constructs a new copy of the given `other` [Brush].
    /// @param other The other [Brush]
    public Brush(final @NotNull Brush other)
    {
        this.type     = other.type;
        this.colour   = other.colour;
        this.gradient = other.gradient;
        this.texture  = other.texture;
    }
    
    //==================================================================================================================
    /// Provides the colour if the brush is in solid fill mode, otherwise returns `null`.
    /// @return The current colour
    public @Nullable Integer getColour() { return this.colour; }
    
    /// Provides the [Gradient] if the brush is in gradient fill mode, otherwise returns `null`.
    /// @return The current [Gradient]
    public @Nullable Gradient getGradient() { return this.gradient; }
    
    /// Provides the [GpuTextureView] if the brush is in texture fill mode, otherwise returns `null`.
    /// @return The current [GpuTextureView]
    public @Nullable GpuTextureView getTexture() { return this.texture; }
    
    /// Gets the [FillType] currently used to draw.
    /// @return The [FillType]
    public @NotNull FillType getType() { return this.type; }
    
    //==================================================================================================================
    /// Binds this brush to the given [IShapedRender] and creates a new [ICanvasState.Renderer].
    /// @param renderer The [IShapedRender] to bake
    /// @param opacity  The opacity value to apply
    public void apply(final @NotNull IShapedRender renderer, final float opacity)
    {
        switch (this.getType())
        {
            case SOLID    -> renderer.applySolid    (this.colour,   opacity);
            case GRADIENT -> renderer.applyGradient (this.gradient, opacity);
            case TEXTURED -> renderer.appplyTextured(-1,            opacity);
        }
    }
    
    //==================================================================================================================
    /// Puts the brush into solid fill mode and sets the colour to be used.
    /// @param colour The new colour value
    public void setColour(final int colour)
    {
        this.type     = FillType.SOLID;
        this.colour   = colour;
        this.gradient = null;
        this.texture  = null;
    }
    
    /// Puts the brush into solid fill mode and sets the colour to be used.
    /// @param colour The new [Colour]
    public void setColour(final @NotNull Colour colour) { this.setColour(colour.colour()); }
    
    /// Puts the brush into gradient fill mode and sets the gradient to be used.
    /// @param gradient The new [Gradient]
    public void setGradient(final @NotNull Gradient gradient)
    {
        this.type     = FillType.GRADIENT;
        this.colour   = null;
        this.gradient = Objects.requireNonNull(gradient, "gradient must not be null");
        this.texture  = null;
    }
    
    /// Puts the brush into texture fill mode and sets the texture to be used.
    /// @param texture The new [GpuTextureView]
    public void setTexture(final @NotNull GpuTextureView texture)
    {
        this.type     = FillType.TEXTURED;
        this.colour   = null;
        this.gradient = null;
        this.texture  = Objects.requireNonNull(texture, "texture must not be null");
    }
}

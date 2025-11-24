/**
 * .-----------------. .----------------.  .----------------.  .----------------.  .----------------.
 * | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
 * | | ____  _____  | || |     ____     | || | ____   ____  | || |     _____    | || |      __      | |
 * | ||_   \|_   _| | || |   .'    `.   | || ||_  _| |_  _| | || |    |_   _|   | || |     /  \     | |
 * | |  |   \ | |   | || |  /  .--.  \  | || |  \ \   / /   | || |      | |     | || |    / /\ \    | |
 * | |  | |\ \| |   | || |  | |    | |  | || |   \ \ / /    | || |      | |     | || |   / ____ \   | |
 * | | _| |_\   |_  | || |  \  `--'  /  | || |    \ ' /     | || |     _| |_    | || | _/ /    \ \_ | |
 * | ||_____|\____| | || |   `.____.'   | || |     \_/      | || |    |_____|   | || ||____|  |____|| |
 * | |              | || |              | || |              | || |              | || |              | |
 * | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
 * '----------------'  '----------------'  '----------------'  '----------------'  '----------------'
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2025 LumiaLights
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.lumialights.novia.api.gui.canvas;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.*;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.lumialights.novia.api.core.util.Colour;
import xyz.lumialights.novia.api.gui.ApiDefine;
import xyz.lumialights.novia.api.gui.canvas.brush.Brush;
import xyz.lumialights.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumialights.novia.api.gui.canvas.renderer.IShapedRender;
import xyz.lumialights.novia.api.gui.canvas.renderer.ShapedRectangleRender;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.GuiComponentAttorney;
import xyz.lumialights.novia.api.gui.component.GuiScreen;
import xyz.lumialights.novia.api.gui.component.ScreenLayer;
import xyz.lumialights.novia.api.gui.font.GuiFont;
import xyz.lumialights.novia.api.gui.font.TextFormat;
import xyz.lumialights.novia.api.gui.font.TextLayout;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.geometry.Point;
import xyz.lumialights.novia.api.gui.geometry.Rectangle;
import xyz.lumialights.novia.api.gui.geometry.UvMapping;
import xyz.lumialights.novia.api.gui.impl.GuiRenderStateAccessor;
import xyz.lumialights.novia.api.gui.impl.RotatingCubeMapRendererExtension;
import xyz.lumialights.novia.api.gui.util.DrawUtil;

import java.util.*;
import java.util.function.Function;



//**********************************************************************************************************************
/**
 * The canvas represents a higher level wrapper around {@link DrawContext} that abstracts away some lower level
 * Minecraft drawing details.
 * <p>
 * For each component that gets passed an instance of this class, an internal “component frame” gets pushed onto the
 * frame buffer (not to be confused with video frame buffers), which represent the current portion on the screen the
 * component is drawing to on the screen. Each frame provides data about the component's current render pass, such as
 * its opacity, activity, location, style and its associated {@link Brush} that is used to define how each of the
 * provided drawing calls are rendered.
 * <p>
 * Another useful concept of canvases is stateful operations. Each component's render pass manages an internal
 * state buffer that gets reset after it is done with drawing, states allow temporarily changing how things are rendered
 * such as clipping region (OpenGL calls this scissor), font, brush etc. During a render, a new temporary state can be
 * pushed onto the state buffer that allows, for the duration of the temporary state, to make modifications to the
 * canvas that get reset to the previous state before pushing the state (for more info see {@link Canvas#pushState()}).
 * Every operation in this class that modifies this state is marked as "stateful".
 */
public final class Canvas
{
    //******************************************************************************************************************
    private static class State
    {
        //**************************************************************************************************************
        public final AffineTransform transform;

        public Brush     brush;
        public GuiFont   font;
        public Rectangle clipRect;
        public float     opacity;

        //**************************************************************************************************************
        public State(final @NotNull Rectangle       clipRect,
                     final @NotNull Brush           brush,
                     final @NotNull GuiFont         font,
                     final @NotNull AffineTransform transform,
                     final          float           opacity)
        {
            this.clipRect  = clipRect;
            this.transform = transform;
            this.font      = font;
            this.brush     = brush;
            this.opacity   = opacity;
        }

        public State(final @NotNull State other)
        {
            this(
                new Rectangle(other.clipRect),
                new Brush(other.brush),
                new GuiFont(other.font),
                new AffineTransform(other.transform),
                other.opacity);
        }
    }

    private record Frame(
        @NotNull Rectangle        clientRect,
        @NotNull GuiFont          font,
        @NotNull IGuiTemplate     template,
        @NotNull IPaletteProvider palette,
        @NotNull AffineTransform  transform,
                 float            opacity,
                 boolean          active
    ) {
        //**************************************************************************************************************
        public static @NotNull Frame forComponent(final @NotNull GuiComponent component, final @NotNull Frame parent)
        {
            return new Frame(
                component.getScreenBounds().intersect(parent.clientRect),
                Objects.requireNonNullElse(GuiComponentAttorney.font(component), parent.font),
                Objects.requireNonNullElse(GuiComponentAttorney.template(component), parent.template),
                component,
                (new AffineTransform(parent.transform)).translate(component.getX(), component.getY()),
                (parent.opacity * component.getOpacity()),
                (parent.active && component.hasActiveFlag())
            );
        }

        public static @NotNull Frame forLayer(final @NotNull ScreenLayer layer)
        {
            return new Frame(
                layer.getBounds(),
                layer.getLayerFont(),
                layer.getLayerTemplate(),
                layer.getPalette(),
                new AffineTransform(),
                1.0f,
                true
            );
        }

        //==============================================================================================================
        private @NotNull State createState()
        {
            return new State(
                new Rectangle(this.clientRect),
                this.template.getDefaultBrush().get(),
                new GuiFont(this.font),
                new AffineTransform(this.transform),
                this.opacity);
        }
    }

    @FunctionalInterface
    private interface DrawFunc<T>
    {
        //**************************************************************************************************************
        void draw(@NotNull T text, int x, int y);
    }

    //******************************************************************************************************************
    public static final RenderPipeline.Snippet NOVIA_GUI_SNIPPET;

    public static final RenderPipeline.Snippet NOVIA_POSITION_TEX_COLOR_SNIPPET;

    public static final RenderPipeline NOVIA_GUI;

    public static final RenderPipeline NOVIA_GUI_TEXTURED;
    
    //------------------------------------------------------------------------------------------------------------------
    private static final Identifier INWORLD_MENU_BACKGROUND_TEXTURE;
    
    //==================================================================================================================
    static
    {
        NOVIA_GUI_SNIPPET = RenderPipeline
            .builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader("core/gui")
            .withFragmentShader("core/gui")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .buildSnippet();
        
        NOVIA_POSITION_TEX_COLOR_SNIPPET = RenderPipeline
            .builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .buildSnippet();
        
        NOVIA_GUI = RenderPipelines.register(RenderPipeline
            .builder(NOVIA_GUI_SNIPPET)
            .withLocation(Identifier.of(ApiDefine.API_ID, "pipeline/gui"))
            .build());
        
        NOVIA_GUI_TEXTURED = RenderPipelines.register(RenderPipeline
            .builder(NOVIA_POSITION_TEX_COLOR_SNIPPET)
            .withLocation(Identifier.of(ApiDefine.API_ID, "pipeline/gui_textured"))
            .build());
        
        INWORLD_MENU_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_background.png");
    }

    //******************************************************************************************************************
    /**
     * The absolute screen position of the mouse cursor at the start of the current render pass.
     * <p>
     * Tip: Use {@link Point#toRelativePoint(Point)} with {@link GuiComponent#getScreenPosition()} to get the mouse
     * position relative to the current frame's component.
     */
    public final @NotNull Point mousePos;

    /** The tick delta between each render pass. */
    public final float deltaTime;

    //------------------------------------------------------------------------------------------------------------------
    // only used internally for debugging purposes, this is not exposed to the outside otherwise
    final DrawContext internalContext;

    //------------------------------------------------------------------------------------------------------------------
    private final Deque<Frame>    framebuffer = new ArrayDeque<>(8);
    private final Deque<State>    statebuffer = new ArrayDeque<>(3);
    private final MinecraftClient client      = MinecraftClient.getInstance();
    private final GuiAtlasManager atlas       = MinecraftClient.getInstance().getGuiAtlasManager();
    private final GuiRenderState  renderState;

    private State state = null;
    private Frame frame = null;

    //******************************************************************************************************************
    public Canvas(final @NotNull DrawContext context, final @NotNull Point mousePos, final float deltaTime)
    {
        this.internalContext = context;
        this.renderState     = Objects.requireNonNull(context,  "context must not be null").state;
        this.mousePos        = Objects.requireNonNull(mousePos, "mouse pos must not be null");
        this.deltaTime       = deltaTime;
    }

    //==================================================================================================================
    /**
     * Gets the screen's template.
     * <p>
     * Since the returned template is not a copy, it should never be modified in any way, as this might have
     * unexpected effects on other frames that also inherit this template.
     * @return The {@link IGuiTemplate}
     */
    public @NotNull IGuiTemplate getTemplate() { return this.frame.template; }

    /**
     * Gets the current brush.
     * <p>
     * The brush returned is not a copy but the state's own brush, any changes to it will apply to the current state.
     * @return The {@link Brush}
     */
    public @NotNull Brush getBrush() { return this.state.brush; }

    /**
     * Gets the current font.
     * <p>
     * The font returned is not a copy but the state's own font, any changes to it will apply to the current state.
     * @return The {@link GuiFont}
     */
    public @NotNull GuiFont getFont() { return this.state.font; }

    /**
     * Gets the current frame's opacity.
     * <p>
     * The opacity returned is the product of all opacity levels of all parent frames and the current one,
     * plus the current state's opacity level set with {@link #setOpacity(float)}.
     * @return The opacity
     */
    public float getOpacity() { return this.state.opacity; }

    /**
     * Gets the width of the window scaled to the current gui scale.
     * @return The scaled window width
     */
    public int getScaledWindowWidth() { return this.client.getWindow().getScaledWidth(); }

    /**
     * Gets the height of the window scaled to the current gui scale.
     * @return The scaled window height
     */
    public int getScaledWindowHeight() { return this.client.getWindow().getScaledHeight(); }

    /**
     * Gets a {@link Colour} that was specified for the current frame.
     * <p>
     * This will first look into the palette of the current frame, if no colour was found it will browse
     * the current {@link IGuiTemplate}'s palette and if there was still no colour found,
     * it will return {@link Colour#BLACK}.
     * @param id The {@link ColourId} for the colour
     * @return The {@link Colour}
     * @throws IllegalStateException If no default colour has been registered for the given ID
     */
    public @NotNull Colour findColour(final @NotNull ColourId id)
    {
        return this.frame.palette
            .getColour(id)
            .or(() -> this.getTemplate().getColour(id))
            .orElse(Colour.BLACK);
    }

    /**
     * Gets a copy of the current state's applied transform.
     * <p>
     * Since the returned transform is a copy you cannot directly modify the transform but instead requires you to set
     * it with either {@link #setTransform(AffineTransform)} or {@link #addTransform(AffineTransform)}.
     * @return The {@link AffineTransform}
     */
    public @NotNull AffineTransform getTransform() { return new AffineTransform(this.state.transform);}

    /**
     * Gets a copy of the currently applied clipping region.
     * <p>
     * Since the returned region is a copy you cannot directly modify the region but instead requires you to set
     * it with either {@link #setClippingRegion(Rectangle)} or {@link #setClippingRegion(int, int, int, int)}.
     * @return The current clipping region
     */
    public @NotNull Rectangle getClippingRegion() { return new Rectangle(this.state.clipRect); }
    
    /**
     * Finds a sprite by its given sprite ID (starting in assets folder 'textures/gui/sprites/').
     * @param spriteId The {@link Identifier} of the sprite
     * @return The {@link Sprite} for that ID, or {@link MissingSprite} if no sprite for that ID was found
     */
    public @NotNull Sprite findSprite(final @NotNull Identifier spriteId) { return this.atlas.getSprite(spriteId); }
    
    /**
     * Gets the scaling algorithm for the given sprite.
     * @param sprite The {@link Sprite}
     * @return The {@link Scaling} algorithm
     */
    public @NotNull Scaling getSpriteScaling(final @NotNull Sprite sprite) { return this.atlas.getScaling(sprite); }
    
    /**
     * Gets the scaling algorithm for the given sprite.
     * @param spriteId The {@link Identifier} of the sprite
     * @return The {@link Scaling} algorithm
     */
    public @NotNull Scaling getSpriteScaling(final @NotNull Identifier spriteId)
    {
        return this.getSpriteScaling(this.findSprite(spriteId));
    }
    
    //==================================================================================================================
    /**
     * Gets whether the current frame is active based on all its parent frames.
     * <p>
     * This is useful to avoid {@link GuiComponent#isActive()} calls, as they need to scan the entire parent hierarchy.
     * @return {@code true} if the frame is active
     */
    public boolean isActive() { return this.frame.active; }

    //==================================================================================================================
    /**
     * Gets whether the current clip region contains the given absolute coordinates.
     * @param screenX The absolute left coordinate
     * @param screenY The absolute y coordinate
     * @return {@code true} if the given points lies within the current clipping region
     */
    public boolean clipRegionContains(final int screenX, final int screenY)
    {
        return this.state.clipRect.contains(screenX, screenY);
    }

    /**
     * Gets whether the current clip region contains the given absolute coordinates.
     * @param screenPoint The absolute point
     * @return {@code true} if the given points lies within the current clipping region
     */
    public boolean clipRegionContains(final @NotNull Point screenPoint)
    {
        return screenPoint.transform(this::clipRegionContains);
    }

    /**
     * Gets whether the current clip region intersects with the specified area.
     * @param screenX The absolute left coordinate of the area
     * @param screenY The absolute y coordinate of the area
     * @param width   The width of the area
     * @param height  The height of the area
     * @return {@code true} if the given area intersects with the current clipping region
     */
    public boolean clipRegionIntersects(final int screenX, final int screenY, final int width, final int height)
    {
        return this.state.clipRect.intersects(screenX, screenY, width, height);
    }

    /**
     * Gets whether the current clip region intersects with the specified area.
     * @param screenRect The absolutely positioned area
     * @return {@code true} if the given area intersects with the current clipping region
     */
    public boolean clipRegionIntersects(final @NotNull Rectangle screenRect)
    {
        return screenRect.transform(this::clipRegionIntersects);
    }

    /**
     * Gets whether the current frame's area (the visible component area) contains the given absolute coordinates.
     * @param screenX The absolute left coordinate
     * @param screenY The absolute y coordinate
     * @return {@code true} if the given points lies within the frame area
     */
    public boolean frameContains(final int screenX, final int screenY)
    {
        return this.frame.clientRect.contains(screenX, screenY);
    }

    /**
     * Gets whether the current frame's area (the visible component area) contains the given absolute coordinates.
     * @param screenPoint The absolute point
     * @return {@code true} if the given points lies within the frame area
     */
    public boolean frameContains(final @NotNull Point screenPoint)
    {
        return screenPoint.transform(this::frameContains);
    }

    /**
     * Gets whether the current frame's area (the visible component area) intersects with the specified area.
     * @param screenX The absolute left coordinate of the area
     * @param screenY The absolute y coordinate of the area
     * @param width   The width of the area
     * @param height  The height of the area
     * @return {@code true} if the given area intersects with the current frame area
     */
    public boolean frameIntersects(final int screenX, final int screenY, final int width, final int height)
    {
        return this.frame.clientRect.intersects(screenX, screenY, width, height);
    }

    /**
     * Gets whether the current frame's area (the visible component area) intersects with the specified area.
     * @param screenRect The absolutely positioned area
     * @return {@code true} if the given area intersects with the current frame area
     */
    public boolean frameIntersects(final @NotNull Rectangle screenRect)
    {
        return screenRect.transform(this::frameIntersects);
    }

    //==================================================================================================================
    /**
     * Sets the brush to a solid colour fill.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param colour The new colour
     */
    public void setColour(final int colour) { this.state.brush.setColour(colour); }

    /**
     * Sets the brush to a solid colour fill.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param colour The new colour
     */
    public void setColour(final @NotNull Colour colour) { this.setColour(colour.colour()); }

    /**
     * Sets the brush to a gradient colour fill.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param gradient The {@link Gradient}
     */
    public void setGradient(final @NotNull Gradient gradient) { this.state.brush.setGradient(gradient); }
    
    public void setTexture(final @NotNull GpuTextureView texture) { this.state.brush.setTexture(texture); }
    
    /**
     * Replaces the current brush with the given brush.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param brush The new {@link Brush}
     */
    public void setBrush(final @NotNull Brush brush) { this.state.brush = new Brush(brush); }

    /**
     * Sets the opacity for the canvas. The opacity will be multiplied by the current frame's opacity, by that means,
     * if the current frame has an opacity of {@code 0.5}, setting the opacity to {@code 0.5} will result in an opacity
     * of {@code 0.25}.
     * This operation is stateful (see {@link Canvas}).
     * @param opacity The opacity level
     */
    public void setOpacity(final float opacity) { this.state.opacity = (opacity * this.frame.opacity); }

    /**
     * Sets the current state's font used to draw text on screen.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param font The new font
     */
    public void setFont(final @NotNull GuiFont font) { this.state.font = new GuiFont(font); }

    /**
     * Sets the new clipping region relative to the origin of the current frame (the component being drawn). The
     * clipping region cannot go outside the frame's bounds.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param rectangle The area to apply the clipping region to
     */
    public void setClippingRegion(final @NotNull Rectangle rectangle)
    {
        rectangle.accept(this::setClippingRegion);
    }

    /**
     * Sets the new clipping region relative to the origin of the current frame (the component being drawn). The
     * clipping region cannot go outside the frame's bounds.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param x      The start left coordinate of the clipping region
     * @param y      The start y coordinate of the clipping region
     * @param width  The width of the clipping region
     * @param height The height of the clipping region
     */
    public void setClippingRegion(final int x, final int y, final int width, final int height)
    {
        final Rectangle trans_rect = this.getTransform().applyToVertices(x, y, width, height);
        this.state.clipRect = this.frame.clientRect.intersect(trans_rect);
    }

    /**
     * Sets the current transform, allowing rotating, translating and scaling subsequent drawing calls.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param transform The {@link AffineTransform}
     */
    public void setTransform(final @NotNull AffineTransform transform)
    {
        this.state.transform.set(this.state.transform.set(this.frame.transform).multiply(transform).getMatrix());
    }

    /**
     * Adds a transform to the current transform, allowing rotating, translating and scaling subsequent drawing calls.
     * <p>
     * This operation is stateful (see {@link Canvas}).
     * @param transform The {@link AffineTransform}
     */
    public void addTransform(final @NotNull AffineTransform transform)
    {
        this.state.transform.set(this.state.transform.multiply(transform).getMatrix());
    }

    //==================================================================================================================
    /**
     * Adds a custom render state object to the renderer.
     * @param renderState The {@link ICanvasState}
     */
    @SuppressWarnings("resource")
    public void draw(final @NotNull ICanvasState renderState)
    {
        if (this.getClippingRegion().isEmpty())
        {
            return;
        }
        
        final AffineTransform transform   = this.state.transform;
        final ScreenRect      clip_bounds = this.state.clipRect.toScreenRect();
        final Rectangle       bounds      = Objects.requireNonNullElseGet(
            renderState.bounds(),
            this.frame::clientRect);
        
        final ScreenRect rect;
        {
            final Rectangle temp_bounds = transform.applyToVertices(bounds).intersect(clip_bounds);
            
            if (temp_bounds.isEmpty())
            {
                return;
            }
            
            rect = temp_bounds.toScreenRect();
        }
        
        final TextureSetup   texture  = Objects.requireNonNullElseGet(renderState.texture(), TextureSetup::empty);
        final RenderPipeline pipeline = Objects.requireNonNullElseGet(
            renderState.pipeline(),
            (() -> ((texture.texure0() != null || texture.texure1() != null)
                ? Canvas.NOVIA_GUI_TEXTURED
                : Canvas.NOVIA_GUI)));
        
        final Matrix4f              matrix   = (new Matrix4f()).mul(transform.getMatrix());
        final ICanvasState.Renderer renderer = renderState.renderer();
        
        ((GuiRenderStateAccessor) this.renderState).novia$addState(new SimpleGuiElementRenderState()
        {
            //**********************************************************************************************************
            @Override
            public void setupVertices(final @NotNull VertexConsumer vertices, final float depth)
            {
                renderer.render(vertices, matrix.translate(0f, 0f, depth));
            }
            
            @Override public RenderPipeline      pipeline()     { return pipeline; }
            @Override public TextureSetup        textureSetup() { return texture; }
            @Override public @NotNull ScreenRect scissorArea()  { return clip_bounds; }
            @Override public @NotNull ScreenRect bounds()       { return rect; }
        });
    }
    
    public void drawShaped(final @NotNull IShapedRender renderer, final @NotNull Rectangle bounds)
    {
        this.drawShaped(renderer, bounds, null, null);
    }
    
    public void drawShaped(final @NotNull  IShapedRender  renderer,
                           final @NotNull  Rectangle      bounds,
                           final @Nullable GpuTextureView texture)
    {
        this.drawShaped(renderer, bounds, null, texture);
    }
    
    public void drawShaped(final @NotNull  IShapedRender  renderer,
                           final @NotNull  Rectangle      bounds,
                           final @Nullable RenderPipeline pipeline,
                           final @Nullable GpuTextureView texture)
    {
        final TextureSetup setup = TextureSetup.of(texture, this.getBrush().getTexture());
        this.getBrush().apply(renderer, this.getOpacity());
        this.draw(new ICanvasState.Renderable(renderer, setup, pipeline, bounds));
    }
    
    public void draw(final @NotNull SimpleGuiElementRenderState renderState)
    {
        if (this.getClippingRegion().isEmpty() || renderState.scissorArea() == null || renderState.bounds() == null)
        {
            return;
        }
        
        ((GuiRenderStateAccessor) this.renderState).novia$addState(renderState);
    }

    public void drawItem(final @NotNull ItemGuiElementRenderState state) { this.renderState.addItem(state); }

    //==================================================================================================================
    /**
     * Draws a horizontal line.
     * @param x      The left position of the line
     * @param y      The top y position of the line
     * @param length The length of the line (width)
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawHorizontalLine(final int x, final int y, final int length)
    {
        this.fill(x, y, length, 1);
    }

    /**
     * Draws a horizontal line.
     * @param pos    The top-left start position of the line
     * @param length The length of the line (width)
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawHorizontalLine(final @NotNull Point pos, final int length)
    {
        pos.accept((x, y) -> this.drawHorizontalLine(x, y, length));
    }

    /**
     * Draws a vertical line.
     * @param x      The left position of the line
     * @param y      The top y position of the line
     * @param length The length of the line (height)
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawVerticalLine(final int x, final int y, final int length) { this.fill(x, y, 1, length); }

    /**
     * Draws a vertical line.
     * @param pos    The top-left start position of the line
     * @param length The length of the line (width)
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawVerticalLine(final @NotNull Point pos, final int length)
    {
        pos.accept((x, y) -> this.drawVerticalLine(x, y, length));
    }

    //==================================================================================================================
    /**
     * Draws a transparent black texture on top, giving the sensation of making everything behind it darker.
     * @param x      The left position of the darkened area
     * @param y      The y position of the darkened area
     * @param width  The width of the darkened area
     * @param height The height of the darkened area
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawDarkening(final int x, final int y, final int width, final int height)
    {
        final Identifier texture = (this.client.world == null
            ? Screen.MENU_BACKGROUND_TEXTURE
            : Canvas.INWORLD_MENU_BACKGROUND_TEXTURE);
        this.drawTexture(texture, x, y, width, height, false);
    }

    /**
     * Draws a transparent black texture on top, giving the sensation of making everything behind it darker.
     * @param rect The area to darken
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawDarkening(final @NotNull Rectangle rect) { rect.accept(this::drawDarkening); }

    /**
     * Draws the iconic Minecraft main menu panorama in the specified area.
     * @param x      The left position of the panorama
     * @param y      The y position of the panorama
     * @param width  The width of the panorama
     * @param height The height of the panorama
     * @param rotate Whether the panorama should rotate
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawPanorama(final int x, final int y, final int width, final int height, final boolean rotate)
    {
        final RotatingCubeMapRendererExtension renderer = ((RotatingCubeMapRendererExtension) this.client.gameRenderer
            .getRotatingPanoramaRenderer());
        renderer.novia$renderPositioned(this, x, y, width, height, rotate);
    }

    /**
     * Draws the iconic Minecraft main menu panorama in the specified area.
     * @param rect   The area to draw the panorama in
     * @param rotate Whether the panorama should rotate
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawPanorama(final @NotNull Rectangle rect, final boolean rotate)
    {
        rect.accept((x, y, w, h) -> this.drawPanorama(x, y, w, h, rotate));
    }

    //==================================================================================================================
    /**
     * Fills a rectangle with the currently set brush.
     * @param rect The area to fill
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void fill(final @NotNull Rectangle rect)
    {
        this.drawShaped(new ShapedRectangleRender.Fill(rect), rect);
    }

    /**
     * Fills a rectangle with the currently set brush.
     * @param x      The left position of the rectangle
     * @param y      The y position of the rectangle
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void fill(final int x, final int y, final int width, final int height)
    {
        this.fill(new Rectangle(x, y, width, height));
    }
    
    /** Fills the entire's component area with the currently set brush. */
    public void fill() { this.fill(this.state.clipRect); }

    /**
     * Draws a rectangular outline.
     * @param rect The area to draw a border around
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawRect(final @NotNull Rectangle rect)
    {
        this.drawShaped(new ShapedRectangleRender.Border(rect, 1f), rect);
    }
    
    /**
     * Draws a rectangular outline.
     * @param x      The left position of the rectangle
     * @param y      The y position of the rectangle
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawRect(final int x, final int y, final int width, final int height)
    {
        this.drawRect(new Rectangle(x, y, width, height));
    }
    
    /** Draws a border around the entire component's area with the currently set brush. */
    public void drawRect() { this.drawRect(this.state.clipRect); }

    //==================================================================================================================
    /**
     * Draws the given text on screen with the current {@link GuiFont}.
     * @param text The string to draw
     * @param x    The draw coordinate on the left-axis
     * @param y    The draw coordinate on the y-axis
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawText(final @NotNull String text, final int x, final int y)
    {
        final TextFormat format = new TextFormat();
        format.append(text, this.getFont());
        format.draw(this, x, y);
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont}.
     * @param text The {@link Text} component to draw
     * @param x    The draw coordinate on the left-axis
     * @param y    The draw coordinate on the y-axis
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawText(final @NotNull Text text, final int x, final int y)
    {
        final TextFormat format = new TextFormat();
        format.append(text, this.getFont());
        format.draw(this, x, y);
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont}.
     * @param text The {@link OrderedText} to draw
     * @param x    The top-left coordinate on the left-axis
     * @param y    The top-left coordinate on the y-axis
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawText(final @NotNull OrderedText text, final int x, final int y)
    {
        final TextFormat format = new TextFormat();
        format.append(text, this.getFont());
        format.draw(this, x, y);
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont}.
     * @param text  The string to draw
     * @param point The top-left point
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawText(final @NotNull String text, final @NotNull Point point)
    {
        point.accept((x, y) -> this.drawText(text, x, y));
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont}.
     * @param text  The {@link Text} component to draw
     * @param point The top-left point
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawText(final @NotNull Text text, final @NotNull Point point)
    {
        point.accept((x, y) -> this.drawText(text, x, y));
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont}.
     * @param text  The {@link OrderedText} to draw
     * @param point The top-left point
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawText(final @NotNull OrderedText text, final @NotNull Point point)
    {
        point.accept((x, y) -> this.drawText(text, x, y));
    }
    
    //==================================================================================================================
    /**
     * Draws the given text on screen with the current {@link GuiFont} and aligned to the given region.
     * @param text      The text to draw
     * @param x         The left coordinate of the alignment area
     * @param y         The y coordinate of the alignment area
     * @param width     The width the alignment area
     * @param height    The height of the alignment area
     * @param alignment The {@link Alignment} to use to align the text
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawTextAligned(final @NotNull String    text,
                                final          int       x,
                                final          int       y,
                                final          int       width,
                                final          int       height,
                                final @NotNull Alignment alignment)
    {
        final GuiFont font        = this.getFont();
        final float   text_width  = TextLayout.getTextWidth(font, text);
        final float   text_height = font.getHeight();
        
        final TextFormat format = new TextFormat();
        format.append(text, font);
        alignment.align(x, y, width, height, text_width, text_height).getPosition().accept((tx, ty) ->
            format.draw(this, tx, ty));
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont} and aligned to the given region.
     * @param text      The text to draw
     * @param x         The left coordinate of the alignment area
     * @param y         The y coordinate of the alignment area
     * @param width     The width the alignment area
     * @param height    The height of the alignment area
     * @param alignment The {@link Alignment} to use to align the text
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawTextAligned(final @NotNull Text      text,
                                final          int       x,
                                final          int       y,
                                final          int       width,
                                final          int       height,
                                final @NotNull Alignment alignment)
    {
        final GuiFont font        = this.getFont();
        final float   text_width  = TextLayout.getTextWidth(font, text);
        final float   text_height = font.getHeight();
        
        final TextFormat format = new TextFormat();
        format.append(text, font);
        alignment.align(x, y, width, height, text_width, text_height).getPosition().accept((tx, ty) ->
            format.draw(this, tx, ty));
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont} and aligned to the given region.
     * @param text      The text to draw
     * @param x         The left coordinate of the alignment area
     * @param y         The y coordinate of the alignment area
     * @param width     The width the alignment area
     * @param height    The height of the alignment area
     * @param alignment The {@link Alignment} to use to align the text
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawTextAligned(final @NotNull OrderedText text,
                                final          int         x,
                                final          int         y,
                                final          int         width,
                                final          int         height,
                                final @NotNull Alignment   alignment)
    {
        final GuiFont font        = this.getFont();
        final float   text_width  = TextLayout.getTextWidth(font, text);
        final float   text_height = font.getHeight();
        
        final TextFormat format = new TextFormat();
        format.append(text, font);
        alignment.align(x, y, width, height, text_width, text_height).getPosition().accept((tx, ty) ->
            format.draw(this, tx, ty));
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont} and aligned to the given region.
     * @param text      The text to draw
     * @param area      The {@link Rectangle} to align the text with
     * @param alignment The {@link Alignment} to use to align the text
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawTextAligned(final @NotNull String    text,
                                final @NotNull Rectangle area,
                                final @NotNull Alignment alignment)
    {
        area.accept((x, y, w, h) -> this.drawTextAligned(text, x, y, w, h, alignment));
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont} and aligned to the given region.
     * @param text      The text to draw
     * @param area      The {@link Rectangle} to align the text with
     * @param alignment The {@link Alignment} to use to align the text
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawTextAligned(final @NotNull Text      text,
                                final @NotNull Rectangle area,
                                final @NotNull Alignment alignment)
    {
        area.accept((x, y, w, h) -> this.drawTextAligned(text, x, y, w, h, alignment));
    }

    /**
     * Draws the given text on screen with the current {@link GuiFont} and aligned to the given region.
     * @param text      The text to draw
     * @param area      The {@link Rectangle} to align the text with
     * @param alignment The {@link Alignment} to use to align the text
     * @see Canvas#setBrush(Brush)
     * @see Canvas#getBrush()
     * @see Brush
     */
    public void drawTextAligned(final @NotNull OrderedText text,
                                final @NotNull Rectangle   area,
                                final @NotNull Alignment   alignment)
    {
        area.accept((x, y, w, h) -> this.drawTextAligned(text, x, y, w, h, alignment));
    }
    
    //==================================================================================================================
    public void drawScrollableText(final @NotNull String text,
                                   final          int    x,
                                   final          int    y,
                                   final          int    width,
                                   final          int    height)
    {
        this.drawScrollableText(text, x, y, width, height, ((x * 2 + width) / 2));
    }

    public void drawScrollableText(final @NotNull Text text,
                                   final          int  x,
                                   final          int  y,
                                   final          int  width,
                                   final          int  height)
    {
        this.drawScrollableText(text, x, y, width, height, ((x * 2 + width) / 2));
    }

    public void drawScrollableText(final @NotNull OrderedText text,
                                   final          int         x,
                                   final          int         y,
                                   final          int         width,
                                   final          int         height)
    {
        this.drawScrollableText(text, x, y, width, height, ((x * 2 + width) / 2));
    }

    public void drawScrollableText(final @NotNull String text,
                                   final          int    x,
                                   final          int    y,
                                   final          int    width,
                                   final          int    height,
                                   final          int    centerX)
    {
        this.drawScrollableText(this::drawText, this.getFont()::getTextWidthFitted, text, x, y, width, height, centerX);
    }

    public void drawScrollableText(final @NotNull Text text,
                                   final          int  x,
                                   final          int  y,
                                   final          int  width,
                                   final          int  height,
                                   final          int  centerX)
    {
        this.drawScrollableText(this::drawText, (text1 -> TextLayout.getTextWidthFitted(this.getFont(), text1)), text,
                                x, y, width, height, centerX);
    }

    public void drawScrollableText(final @NotNull OrderedText text,
                                   final          int         x,
                                   final          int         y,
                                   final          int         width,
                                   final          int         height,
                                   final          int         centerX)
    {
        this.drawScrollableText(this::drawText, (text1 -> TextLayout.getTextWidthFitted(this.getFont(), text1)), text,
                                x, y, width, height, centerX);
    }

    public void drawScrollableText(final @NotNull String text, final @NotNull Rectangle rect)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h));
    }

    public void drawScrollableText(final @NotNull Text text, final @NotNull Rectangle rect)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h));
    }

    public void drawScrollableText(final @NotNull OrderedText text, final @NotNull Rectangle rect)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h));
    }

    public void drawScrollableText(final @NotNull String    text,
                                   final @NotNull Rectangle rect,
                                   final          int       centerX)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h, centerX));
    }

    public void drawScrollableText(final @NotNull Text      text,
                                   final @NotNull Rectangle rect,
                                   final          int       centerX)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h, centerX));
    }

    public void drawScrollableText(final @NotNull OrderedText text,
                                   final @NotNull Rectangle   rect,
                                   final          int         centerX)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h, centerX));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> void drawScrollableText(final @NotNull DrawFunc<T>          drawFunc,
                                        final @NotNull Function<T, Integer> widthFunc,
                                        final @NotNull T                    text,
                                        final          int                  x,
                                        final          int                  y,
                                        final          int                  width,
                                        final          int                  height,
                                        final          int                  centerX)
    {
        final int text_width = widthFunc.apply(text);
        final int text_y = ((int) (((y * 2 + height) - this.client.textRenderer.fontHeight) * 0.5));
        final int text_right = (x + width);
        final int draw_width = (text_right - x);

        if (text_width > draw_width) {
            final int overflow = (text_width - width + 1);
            final double time = (Util.getMeasuringTimeMs() / 1000.0);
            final double e = Math.max(overflow * 0.5, 3.0);
            final double f = (Math.sin((Math.PI * 0.5) * Math.cos(Math.TAU * time / e)) * 0.5 + 0.5);
            final int shift = (int) MathHelper.lerp(f, 0.0, overflow);

            this.runWithState(() ->
            {
                this.setClippingRegion(x, y, width, height);
                drawFunc.draw(text, (x - shift), text_y);
            });
        } else {
            final int text_c = (text_width / 2);
            final int text_x = (MathHelper.clamp(centerX, (x + text_c), (text_right - text_c)) - text_c);

            drawFunc.draw(text, text_x, text_y);
        }
    }

    //==================================================================================================================
    /**
     * Draws a region of a sprite on the screen with the scaling being automatically determined by the texture atlas.
     * @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param uv       The {@link UvMapping} texture region
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Identifier spriteId,
                           final          int        x,
                           final          int        y,
                           final          int        width,
                           final          int        height,
                           final @NotNull UvMapping  uv,
                           final          boolean    useBrush)
    {
        final Sprite  sprite  = this.findSprite(spriteId);
        final Scaling scaling = this.getSpriteScaling(sprite);
        this.drawSprite(sprite, scaling, x, y, width, height, uv, useBrush);
    }
    
    /**
     * Draws a region of a sprite on the screen with the scaling being automatically determined by the texture atlas.
     * @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param uv       The {@link UvMapping} texture region
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Identifier spriteId,
                           final @NotNull Rectangle  rect,
                           final @NotNull UvMapping  uv,
                           final          boolean    useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(spriteId, x, y, w, h, uv, useBrush));
    }
    
    /**
     * Draws a sprite on the screen with the scaling being automatically determined by the texture atlas.
     * @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Identifier spriteId,
                           final          int        x,
                           final          int        y,
                           final          int        width,
                           final          int        height,
                           final          boolean    useBrush)
    {
        this.drawSprite(spriteId, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /**
     * Draws a sprite on the screen with the scaling being automatically determined by the texture atlas.
     * @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Identifier spriteId,
                           final @NotNull Rectangle  rect,
                           final          boolean    useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(spriteId, x, y, w, h, useBrush));
    }
    
    /**
     * Draws a given region of a sprite with the given scaling algorithm.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The scaling algorithm
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param uv       The {@link UvMapping} texture region
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Sprite    sprite,
                           final @NotNull Scaling   scaling,
                           final          int       x,
                           final          int       y,
                           final          int       width,
                           final          int       height,
                           final @NotNull UvMapping uv,
                           final          boolean   useBrush)
    {
        switch (scaling)
        {
            case Scaling.Stretch   s -> this.drawSpriteStretched (sprite,    x, y, width, height, uv, useBrush);
            case Scaling.NineSlice n -> this.drawSpriteNineSliced(sprite, n, x, y, width, height, uv, useBrush);
            case Scaling.Tile      t -> this.drawSpriteTiled     (sprite, t, x, y, width, height, uv, useBrush);

            default -> throw new IllegalStateException("Unexpected scaling: " + scaling);
        }
    }
    
    /**
     * Draws a sprite with the given scaling algorithm.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The scaling algorithm
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Sprite    sprite,
                           final @NotNull Scaling   scaling,
                           final @NotNull Rectangle rect,
                           final @NotNull UvMapping uv,
                           final          boolean   useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(sprite, scaling, x, y, w, h, uv, useBrush));
    }
    
    /**
     * Draws a sprite with the given scaling algorithm.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The scaling algorithm
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Sprite  sprite,
                           final @NotNull Scaling scaling,
                           final          int     x,
                           final          int     y,
                           final          int     width,
                           final          int     height,
                           final          boolean useBrush)
    {
        this.drawSprite(sprite, scaling, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /**
     * Draws a sprite with the given scaling algorithm.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The scaling algorithm
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSprite(final @NotNull Sprite    sprite,
                           final @NotNull Scaling   scaling,
                           final @NotNull Rectangle rect,
                           final          boolean   useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(sprite, scaling, x, y, w, h, UvMapping.FULL, useBrush));
    }
    
    /**
     * Draws a given region of a sprite stretched to the given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param uv       The {@link UvMapping} texture region
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteStretched(final @NotNull Sprite    sprite,
                                    final          int       x,
                                    final          int       y,
                                    final          int       width,
                                    final          int       height,
                                    final @NotNull UvMapping uv,
                                    final          boolean   useBrush)
    {
        if (width > 0 && height > 0)
        {
            this.drawTexturedQuadSprite(sprite, x, y, (x + width), (y + height), uv.minU(), uv.minV(), uv.maxU(),
                                        uv.maxV(), useBrush);
        }
    }
    
    /**
     * Draws a given region of a sprite stretched to the given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param uv       The {@link UvMapping} texture region
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteStretched(final @NotNull Sprite    sprite,
                                    final @NotNull Rectangle rect,
                                    final @NotNull UvMapping uv,
                                    final          boolean   useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteStretched(sprite, x, y, w, h, uv, useBrush));
    }
    
    /**
     * Draws an entire sprite stretched to the given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteStretched(final @NotNull Sprite  sprite,
                                    final          int     x,
                                    final          int     y,
                                    final          int     width,
                                    final          int     height,
                                    final          boolean useBrush)
    {
        this.drawSpriteStretched(sprite, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /**
     * Draws an entire sprite stretched to the given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteStretched(final @NotNull Sprite sprite, final @NotNull Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteStretched(sprite, x, y, w, h, useBrush));
    }

    /**
     * Draws a region of a sprite on the screen that is tiled and repeated to fill its given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.Tile} scaling
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param uv       The {@link UvMapping} texture region to use as tile, this is the part that will be repeated
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteTiled(final @NotNull Sprite       sprite,
                                final @NotNull Scaling.Tile scaling,
                                final          int          x,
                                final          int          y,
                                final          int          width,
                                final          int          height,
                                final @NotNull UvMapping    uv,
                                final          boolean      useBrush)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }

        final int tile_u      = (int) (scaling.width()   * uv.minU());
        final int tile_v      = (int) (scaling.height()  * uv.minV());
        final int tile_width  = (int) ((scaling.width()  * uv.maxU()) - tile_u);
        final int tile_height = (int) ((scaling.height() * uv.maxV()) - tile_v);

        this.drawSpriteTiled(sprite, x, y, width, height, tile_u, tile_v, tile_width, tile_height, tile_width,
                             tile_height, useBrush);
    }
    
    /**
     * Draws a region of a sprite on the screen that is tiled and repeated to fill its given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.Tile} scaling
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param uv       The {@link UvMapping} texture region to use as tile, this is the part that will be repeated
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteTiled(final @NotNull Sprite       sprite,
                                final @NotNull Scaling.Tile scaling,
                                final @NotNull Rectangle    rect,
                                final @NotNull UvMapping    uv,
                                final          boolean      useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteTiled(sprite, scaling, x, y, w, h, uv, useBrush));
    }
    
    /**
     * Draws a sprite on the screen that is tiled and repeated to fill its given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.Tile} scaling
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteTiled(final @NotNull Sprite       sprite,
                                final @NotNull Scaling.Tile scaling,
                                final          int          x,
                                final          int          y,
                                final          int          width,
                                final          int          height,
                                final          boolean      useBrush)
    {
        this.drawSpriteTiled(sprite, x, y, width, height, 0, 0, scaling.width(), scaling.height(), scaling.width(),
                             scaling.height(), useBrush);
    }
    
    /**
     * Draws a sprite on the screen that is tiled and repeated to fill its given area.
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.Tile} scaling
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteTiled(final @NotNull Sprite       sprite,
                                final @NotNull Scaling.Tile scaling,
                                final @NotNull Rectangle    rect,
                                final          boolean      useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteTiled(sprite, scaling, x, y, w, h, useBrush));
    }
    
    /**
     * Draws a region of a sprite on the screen that is 9-sliced
     * (see <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice scaling</a>).
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.NineSlice} scaling
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param uv       The {@link UvMapping} texture region to use as 9-slice, this is the part that will be repeated
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteNineSliced(final @NotNull Sprite            sprite,
                                     final @NotNull Scaling.NineSlice scaling,
                                     final          int               x,
                                     final          int               y,
                                     final          int               width,
                                     final          int               height,
                                     final @NotNull UvMapping         uv,
                                     final          boolean           useBrush)
    {
        final Scaling.NineSlice.Border border = scaling.border();
        final int                      left   = Math.min(border.left(),   (width  / 2));
        final int                      top    = Math.min(border.top(),    (height / 2));
        final int                      right  = Math.min(border.right(),  (width  / 2));
        final int                      bottom = Math.min(border.bottom(), (height / 2));
        
        final int texture_u      = (int) (scaling.width()  * uv.minU());
        final int texture_v      = (int) (scaling.height() * uv.minV());
        final int texture_width  = (int) (scaling.width()  * uv.maxU() - texture_u);
        final int texture_height = (int) (scaling.height() * uv.maxV() - texture_v);
        
        if (width == texture_width && height == texture_height)
        {
            // Full sprite
            this.drawSpriteRegion(sprite, x, y, width, height, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);
        }
        else if (height == texture_height)
        {
            final int right_edge = (texture_width - right);
            
            // Left edge
            this.drawSpriteRegion(sprite, x, y, left, height, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);
            
            // Middle edge
            this.drawInnerSprite(scaling, sprite, (x + left), y, (width - right - left), height, (texture_u + left),
                                 texture_v, texture_width, texture_height, (right_edge - left), texture_height,
                                 useBrush);

            // Right edge
            this.drawSpriteRegion(sprite, (x + width - right), y, right, height, (texture_u + right_edge), texture_v,
                                  texture_width, texture_height, useBrush);
        }
        else if (width == texture_width)
        {
            final int bottom_edge = (texture_height - bottom);
            
            // Top edge
            this.drawSpriteRegion(sprite, x, y, width, top, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);

            // Middle edge
            this.drawInnerSprite(scaling, sprite, x, (y + top), width, (height - bottom - top), texture_u,
                                 (texture_v + top), texture_width, texture_height, texture_width, (bottom_edge - top),
                                 useBrush);

            // Bottom edge
            this.drawSpriteRegion(sprite, x, (y + height - bottom), width, bottom, texture_u, (texture_v + bottom_edge),
                                  texture_width, texture_height, useBrush);
        }
        else
        {
            final int draw_x      = (x + left);
            final int draw_x2     = (x + width - right);
            final int draw_y      = (y + top);
            final int draw_width  = (width - right - left);
            final int draw_height = (height - bottom - top);
            final int tile_width  = (texture_width - right - left);
            final int tile_height = (texture_height - bottom - top);
            final int text_u      = (y + height - bottom);
            final int text_u2     = (texture_width - right);
            final int text_v      = (texture_height - bottom);

            // Top-left corner
            this.drawSpriteRegion(sprite, x, y, left, top, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);

            // Top edge
            this.drawInnerSprite(scaling, sprite, draw_x, y, draw_width, top, (texture_u + left), texture_v,
                                 texture_width, texture_height, tile_width, top, useBrush);

            // Top-right corner
            this.drawSpriteRegion(sprite, draw_x2, y, right, top, (texture_u + text_u2), texture_v, texture_width,
                                  texture_height, useBrush);

            // Bottom-left corner
            this.drawSpriteRegion(sprite, x, text_u, left, bottom, texture_u, (texture_v + text_v), texture_width,
                                  texture_height, useBrush);

            // Bottom edge
            this.drawInnerSprite(scaling, sprite, draw_x, text_u, draw_width, bottom, (texture_u + left),
                                 (texture_v + text_v), texture_width, texture_height, tile_width, bottom, useBrush);

            // Bottom-right corner
            this.drawSpriteRegion(sprite, draw_x2, text_u, right, bottom, (texture_u + text_u2), (texture_v + text_v),
                                  texture_width, texture_height, useBrush);

            // Left edge
            this.drawInnerSprite(scaling, sprite, x, draw_y, left, draw_height, texture_u, (texture_v + top),
                                 texture_width, texture_height, left, tile_height, useBrush);

            // Center
            this.drawInnerSprite(scaling, sprite, draw_x, draw_y, draw_width, draw_height, (texture_u + left),
                                 (texture_v + top), texture_width, texture_height, tile_width, tile_height, useBrush);

            // Right edge
            this.drawInnerSprite(scaling, sprite, draw_x2, draw_y, right, draw_height, (texture_u + text_u2),
                                 (texture_v + top), texture_width, texture_height, right, tile_height, useBrush);
        }
    }
    
    /**
     * Draws a region of a sprite on the screen that is 9-sliced
     * (see <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice scaling</a>).
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.NineSlice} scaling
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param uv       The {@link UvMapping} texture region to use as 9-slice, this is the part that will be repeated
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    public void drawSpriteNineSliced(final @NotNull Sprite            sprite,
                                     final @NotNull Scaling.NineSlice scaling,
                                     final @NotNull Rectangle         rect,
                                     final @NotNull UvMapping         uv,
                                     final          boolean           useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteNineSliced(sprite, scaling, x, y, w, h, uv, useBrush));
    }
    
    /**
     * Draws a sprite on the screen that is 9-sliced
     * (see <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice scaling</a>).
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.NineSlice} scaling
     * @param x        The left coordinate
     * @param y        The y coordinate
     * @param width    The width of the area to draw
     * @param height   The height of the area to draw
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    private void drawSpriteNineSliced(final @NotNull Sprite            sprite,
                                      final @NotNull Scaling.NineSlice scaling,
                                      final          int               x,
                                      final          int               y,
                                      final          int               width,
                                      final          int               height,
                                      final          boolean           useBrush)
    {
        this.drawSpriteNineSliced(sprite, scaling, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /**
     * Draws a sprite on the screen that is 9-sliced
     * (see <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice scaling</a>).
     * @param sprite   The sprite to draw (can be fetched from the texture atlas)
     * @param scaling  The {@link Scaling.NineSlice} scaling
     * @param rect     The {@link Rectangle} to draw the texture in
     * @param useBrush Whether to use the currently set brush to apply to the texture
     */
    private void drawSpriteNineSliced(final @NotNull Sprite            sprite,
                                      final @NotNull Scaling.NineSlice scaling,
                                      final @NotNull Rectangle         rect,
                                      final          boolean           useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteNineSliced(sprite, scaling, x, y, w, h, useBrush));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void drawSpriteRegion(final @NotNull Sprite sprite,
                                  final int x, final int y, final int width, final int height,
                                  final int u, final int v, final int textureWidth, final int textureHeight,
                                  final boolean useBrush)
    {
        if (width != 0 && height != 0)
        {
            this.drawTexturedQuadSprite(sprite, x, y, (x + width), (y + height), (u / (float) textureWidth),
                                        (v / (float) textureHeight), ((u + width) / (float) textureWidth),
                                        ((v + height) / (float) textureHeight), useBrush);
        }
    }
    
    private void drawSpriteTiled(final @NotNull Sprite       sprite,
                                 final          int          x,
                                 final          int          y,
                                 final          int          width,
                                 final          int          height,
                                 final          int          u,
                                 final          int          v,
                                 final          int          textureWidth,
                                 final          int          textureHeight,
                                 final          int          tileWidth,
                                 final          int          tileHeight,
                                 final          boolean      useBrush)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }

        if (tileWidth <= 0 || tileHeight <= 0)
        {
            throw new IllegalArgumentException(
                "Tiled sprite texture size must be positive, got %sx%s".formatted(tileWidth, tileHeight));
        }

        for (int ix = 0; ix < width; ix += tileWidth)
        {
            final int draw_width = Math.min(tileWidth, (width - ix));
            final int draw_x     = (x + ix);

            for (int iy = 0; iy < height; iy += tileHeight)
            {
                final int draw_height = Math.min(tileHeight, (height - iy));
                final int draw_y      = (y + iy);

                this.drawSpriteRegion(sprite, draw_x, draw_y, draw_width, draw_height,
                                      u, v, textureWidth, textureHeight,
                                      useBrush);
            }
        }
    }

    private void drawInnerSprite(final @NotNull Scaling.NineSlice nineSlice, final @NotNull Sprite sprite,
                                 final int x, final int y, final int width, final int height,
                                 final int u, final int v, final int textureWidth, final int textureHeight,
                                 final int tileWidth, final int tileHeight, final boolean useBrush)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }

        if (nineSlice.stretchInner())
        {
            this.drawTexturedQuadSprite(sprite, x, y, (x + width), (y + height), (u / (float) textureWidth),
                                        (v / (float) textureHeight), ((u + tileWidth) / (float) textureWidth),
                                        ((v + tileHeight) / (float) textureHeight), useBrush);
        }
        else
        {
            this.drawSpriteTiled(sprite, x, y, width, height, u, v, textureWidth, textureHeight, tileWidth, tileHeight,
                                 useBrush);
        }
    }
    
    private void drawTexturedQuadSprite(final @NotNull Sprite sprite,
                                        final int x1, final int y1, final int x2, final int y2,
                                        final float u1, final float v1, final float u2, final float v2,
                                        final boolean useBrush)
    {
        this.drawTexturedQuad(
            sprite.getAtlasId(),
            x1, y1, x2, y2,
            sprite.getFrameU(u1), sprite.getFrameV(v1), sprite.getFrameU(u2), sprite.getFrameV(v2),
            useBrush);
    }
    
    //==================================================================================================================
    /**
     * Draws a region of a texture on the screen as-is and stretched to the target area.
     * @param textureId The id of the texture inside the assets' root folder
     * @param x         The left coordinate
     * @param y         The y coordinate
     * @param width     The width of the area to draw
     * @param height    The height of the area to draw
     * @param uv        The {@link UvMapping} texture region to use as 9-slice, this is the part that will be repeated
     * @param useBrush  Whether to use the currently set brush to apply to the texture
     */
    public void drawTexture(final @NotNull Identifier textureId,
                            final          int        x,
                            final          int        y,
                            final          int        width,
                            final          int        height,
                            final @NotNull UvMapping  uv,
                            final          boolean    useBrush)
    {
        this.drawTexturedQuad(textureId, x, y, (x + width), (y + height), uv.minU(), uv.minV(), uv.maxU(), uv.maxV(),
                              useBrush);
    }

    /**
     * Draws a region of a texture on the screen as-is and stretched to the target area.
     * @param textureId The id of the texture inside the assets' root folder
     * @param rect      The {@link Rectangle} to draw the texture in
     * @param uv        The {@link UvMapping} texture region to use as 9-slice, this is the part that will be repeated
     * @param useBrush  Whether to use the currently set brush to apply to the texture
     */
    public void drawTexture(final @NotNull Identifier textureId,
                            final @NotNull Rectangle  rect,
                            final @NotNull UvMapping  uv,
                            final          boolean    useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawTexture(textureId, x, y, w, h, uv, useBrush));
    }

    /**
     * Draws a texture on the screen as-is and stretched to the target area.
     * @param textureId The id of the texture inside the assets' root folder
     * @param x         The left coordinate
     * @param y         The y coordinate
     * @param width     The width of the area to draw
     * @param height    The height of the area to draw
     * @param useBrush  Whether to use the currently set brush to apply to the texture
     */
    public void drawTexture(final @NotNull Identifier textureId,
                            final          int        x,
                            final          int        y,
                            final          int        width,
                            final          int        height,
                            final          boolean    useBrush)
    {
        this.drawTexture(textureId, x, y, width, height, UvMapping.FULL, useBrush);
    }

    /**
     * Draws a texture on the screen as-is and stretched to the target area.
     * @param textureId The id of the texture inside the assets' root folder
     * @param rect      The {@link Rectangle} to draw the texture in
     * @param useBrush  Whether to use the currently set brush to apply to the texture
     */
    public void drawTexture(final @NotNull Identifier textureId, final @NotNull Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawTexture(textureId, x, y, w, h, useBrush));
    }

    //------------------------------------------------------------------------------------------------------------------
    private void drawTexturedQuad(final @NotNull Identifier textureId,
                                  final int x1, final int y1, final int x2, final int y2,
                                  final float u1, final float v1, final float u2, final float v2,
                                  final boolean useBrush)
    {
        final GpuTextureView texture = this.client.getTextureManager().getTexture(textureId).getGlTextureView();
        this.drawTexturedQuad(texture, x1, y1, x2, y2, u1, v1, u2, v2, useBrush);
    }

    private void drawTexturedQuad(final @NotNull GpuTextureView texture,
                                  final int x1, final int y1, final int x2, final int y2,
                                  final float u1, final float v1, final float u2, final float v2,
                                  final boolean useBrush)
    {
        final Rectangle bounds = Rectangle.fromPoints(x1, y1, x2, y2);
        
        if (!useBrush)
        {
            final int colour = Colour.WHITE.withOpacityRel(this.getOpacity()).colour();
            this.draw(new ICanvasState.Renderable(
                ((vertices, matrix) -> DrawUtil.drawTexturedRect(
                    matrix, vertices, 0f,
                    x1, y1, x2, y2,
                    u1, v1, u2, v2,
                    colour, colour, colour, colour)),
                TextureSetup.withoutGlTexture(texture),
                null,
                bounds
            ));
            
            return;
        }

        this.drawShaped(new ShapedRectangleRender.Texture(x1, y1, x2, y2, u1, v1, u2, v2), bounds, texture);
    }

    //==================================================================================================================
    /**
     * Creates a new temporary state for the current canvas frame that remembers the state prior to this call and
     * reverts to it upon calling {@link #popState()}.
     * <p>
     * This is useful when needing to do transforms and clipping regions or when a brush needs to be remembered for
     * later.
     * <p>
     * There is also {@link #runWithState(Runnable)} which hides pushing and popping behind a callback so that this
     * does not have to be manually managed.
     */
    public void pushState()
    {
        this.statebuffer.addLast(this.state);
        this.state = new State(this.state);
    }

    /**
     * Pops the current frame's state and reverts to the state prior to {@link #pushState()}.
     * <p>
     * Do note that this does not need to be called if you don't need to revert to a previous state, however, if this is
     * the case, a temporary state would not be needed to begin with.
     * @throws NoSuchElementException If no {@link #pushState()} has happened prior to this call
     */
    public void popState() { this.state = this.statebuffer.removeLast(); }

    /**
     * Pops all pushed states and initialises the default state of the current frame.
     * <p>
     * This can be used to revert the frame state to the default.
     */
    public void resetState()
    {
        this.statebuffer.clear();
        this.state = this.frame.createState();
    }

    /**
     * Automatically pushes and pops a new state context and executes the given action between the push and pop.
     * <p>
     * This means, any brush, matrix and clipping updates applied during the given action are reverted to before the
     * action was invoked after it finished.
     * <p>
     * This should be preferred whenever possible as it takes away the responsibility for managing a state switch, hence
     * no manual intervention is needed.
     * <p>
     * One drawback of this is that sometimes IDEs might not be able to hotswap if the runnable changes and
     * hence a complete reload becomes necessary.
     * @param action The action to run during the current temporary state
     */
    public void runWithState(final @NotNull Runnable action)
    {
        this.pushState();
        action.run();
        this.popState();
    }

    //==================================================================================================================
    void pushFrame(final @NotNull GuiComponent component)
    {
        Objects.requireNonNull(component, "component must not be null");
        this.framebuffer.addLast(this.frame);
        this.frame = Frame.forComponent(component, this.frame);
        this.resetState();
    }

    void popFrame() { this.frame = this.framebuffer.removeLast(); }

    void setLayer(final @NotNull ScreenLayer layer)
    {
        this.framebuffer.clear();
        this.frame = Frame.forLayer(layer);
        this.resetState();

        this.renderState.createNewRootLayer();
    }

    void initFramebuffer(final @NotNull GuiScreen screen)
    {
        this.frame = new Frame(
            screen.getScreenBounds(),
            screen.getFont(),
            screen.getTemplate(),
            screen,
            new AffineTransform(),
            1.0f,
            true);
        this.state = this.frame.createState();
    }
}

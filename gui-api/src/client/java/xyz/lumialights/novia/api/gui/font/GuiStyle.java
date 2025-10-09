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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.impl.StyleAccessor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;



//**********************************************************************************************************************
public class GuiStyle
    extends Style
{
    //******************************************************************************************************************
    public static final GuiStyle EMPTY = new GuiStyle(null, null, null, null, null, null, null, null, null, null, null,
                                                      null, null, null);

    //==================================================================================================================
    public static final MapCodec<GuiStyle> MAP_CODEC;
    public static final Codec<GuiStyle> CODEC;
    public static final PacketCodec<RegistryByteBuf, GuiStyle> PACKET_CODEC;

    //==================================================================================================================
    static {
        MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance
            .group(
                TextColor.CODEC
                    .optionalFieldOf("color")
                    .forGetter((style) -> Optional.ofNullable(style.getColor())),
                net.minecraft.util.dynamic.Codecs.ARGB
                    .optionalFieldOf("shadow_color")
                    .forGetter((style) -> Optional.ofNullable(style.getShadowColor())),
                Codec.BOOL
                    .optionalFieldOf("bold")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isBold())),
                Codec.BOOL
                    .optionalFieldOf("italic")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isItalic())),
                Codec.BOOL
                    .optionalFieldOf("underlined")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isUnderlined())),
                Codec.BOOL
                    .optionalFieldOf("strikethrough")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isStrikethrough())),
                Codec.BOOL
                    .optionalFieldOf("obfuscated")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isObfuscated())),
                ClickEvent.CODEC
                    .optionalFieldOf("click_event")
                    .forGetter((style) -> Optional.ofNullable(style.getClickEvent())),
                HoverEvent.CODEC
                    .optionalFieldOf("hover_event")
                    .forGetter((style) -> Optional.ofNullable(style.getHoverEvent())),
                Codec.STRING
                    .optionalFieldOf("insertion")
                    .forGetter((style) -> Optional.ofNullable(style.getInsertion())),
                Identifier.CODEC
                    .optionalFieldOf("font")
                    .forGetter((style) -> Optional.ofNullable(style.getFont())),
                Codec.BOOL
                    .optionalFieldOf("shaded")
                    .forGetter((style) -> Optional.ofNullable(style.shaded)),
                Codec.FLOAT
                    .optionalFieldOf("size")
                    .forGetter((style) -> Optional.ofNullable(style.getSize())),
                Codec.FLOAT
                    .optionalFieldOf("kerning")
                    .forGetter((style) -> Optional.ofNullable(style.getKerning())))
            .apply(instance, GuiStyle::of));
        CODEC        = MAP_CODEC.codec();
        PACKET_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);
    }

    //******************************************************************************************************************
    private static <T> @NotNull GuiStyle with(final @NotNull  GuiStyle newStyle,
                                              final @Nullable T        oldAttribute,
                                              final @Nullable T        newAttribute)
    {
        return ((oldAttribute != null && newAttribute == null && newStyle.equals(GuiStyle.EMPTY))
            ? GuiStyle.EMPTY
            : newStyle);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static @NotNull GuiStyle of(final @NotNull Optional<TextColor>  color,
                                        final @NotNull Optional<Integer>    shadowColor,
                                        final @NotNull Optional<Boolean>    bold,
                                        final @NotNull Optional<Boolean>    italic,
                                        final @NotNull Optional<Boolean>    underlined,
                                        final @NotNull Optional<Boolean>    strikethrough,
                                        final @NotNull Optional<Boolean>    obfuscated,
                                        final @NotNull Optional<ClickEvent> clickEvent,
                                        final @NotNull Optional<HoverEvent> hoverEvent,
                                        final @NotNull Optional<String>     insertion,
                                        final @NotNull Optional<Identifier> font,
                                        final @NotNull Optional<Boolean>    shaded,
                                        final @NotNull Optional<Float>      size,
                                        final @NotNull Optional<Float>      kerning)
    {
        final GuiStyle style = new GuiStyle(
            color        .orElse(null),
            shadowColor  .orElse(null),
            bold         .orElse(null),
            italic       .orElse(null),
            underlined   .orElse(null),
            strikethrough.orElse(null),
            obfuscated   .orElse(null),
            clickEvent   .orElse(null),
            hoverEvent   .orElse(null),
            insertion    .orElse(null),
            font         .orElse(null),
            shaded       .orElse(null),
            size         .orElse(null),
            kerning      .orElse(null));
        return (style.equals(GuiStyle.EMPTY) ? GuiStyle.EMPTY : style);
    }

    //******************************************************************************************************************
    final @Nullable Boolean shaded;
    final @Nullable Float   size;
    final @Nullable Float   kerning;

    //******************************************************************************************************************
    public GuiStyle(final @Nullable TextColor  color,
                    final @Nullable Integer    shadowColor,
                    final @Nullable Boolean    bold,
                    final @Nullable Boolean    italic,
                    final @Nullable Boolean    underlined,
                    final @Nullable Boolean    strikethrough,
                    final @Nullable Boolean    obfuscated,
                    final @Nullable ClickEvent clickEvent,
                    final @Nullable HoverEvent hoverEvent,
                    final @Nullable String     insertion,
                    final @Nullable Identifier font,
                    final @Nullable Boolean    shaded,
                    final @Nullable Float      size,
                    final @Nullable Float      kerning)
    {
        super(color, shadowColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent,
              insertion, font);

        this.shaded  = shaded;
        this.size    = size;
        this.kerning = kerning;
    }

    public GuiStyle(final @NotNull Style style, final @Nullable Boolean shaded, final @Nullable Float size,
                    final @Nullable Float kerning)
    {
        this((StyleAccessor) style, style, shaded, size, kerning);
    }

    //------------------------------------------------------------------------------------------------------------------
    private GuiStyle(final @NotNull StyleAccessor styleAcc, final @NotNull Style style, final @Nullable Boolean shaded,
                     final @Nullable Float size, final @Nullable Float kerning)
    {
        super(
            style   .getColor(),
            style   .getShadowColor(),
            styleAcc.novia$isBold(),
            styleAcc.novia$isItalic(),
            styleAcc.novia$isUnderlined(),
            styleAcc.novia$isStrikethrough(),
            styleAcc.novia$isObfuscated(),
            style   .getClickEvent(),
            style   .getHoverEvent(),
            style   .getInsertion(),
            styleAcc.novia$getFontId()
        );
        this.shaded  = shaded;
        this.size    = size;
        this.kerning = kerning;
    }

    //==================================================================================================================
    /**
     * Gets the size scale.
     * @return The size
     */
    public @Nullable Float getSize() { return this.size; }

    /**
     * Gets the kerning factor.
     * @return The kerning factor
     */
    public @Nullable Float getKerning() { return this.kerning; }

    //==================================================================================================================
    public boolean isShaded() { return (this.shaded == Boolean.TRUE); }
    
    @Override public boolean isEmpty() { return (this == GuiStyle.EMPTY); }
    
    //==================================================================================================================
    public @NotNull GuiStyle withColor(final @Nullable TextColor color)
    {
        return new GuiStyle(super.withColor(color), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withColor(final @Nullable Formatting color)
    {
        return (this.withColor(color != null ? TextColor.fromFormatting(color) : null));
    }

    public @NotNull GuiStyle withColor(final int rgbColor) { return this.withColor(TextColor.fromRgb(rgbColor)); }

    public @NotNull GuiStyle withShadowColor(final int shadowColor)
    {
        return new GuiStyle(super.withShadowColor(shadowColor), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withBold(final @Nullable Boolean bold)
    {
        return new GuiStyle(super.withBold(bold), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withItalic(final @Nullable Boolean italic)
    {
        return new GuiStyle(super.withItalic(italic), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withUnderline(final @Nullable Boolean underline)
    {
        return new GuiStyle(super.withUnderline(underline), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withStrikethrough(final @Nullable Boolean strikethrough)
    {
        return new GuiStyle(super.withStrikethrough(strikethrough), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withObfuscated(final @Nullable Boolean obfuscated)
    {
        return new GuiStyle(super.withObfuscated(obfuscated), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withClickEvent(final @Nullable ClickEvent clickEvent)
    {
        return new GuiStyle(super.withClickEvent(clickEvent), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withHoverEvent(final @Nullable HoverEvent hoverEvent)
    {
        return new GuiStyle(super.withHoverEvent(hoverEvent), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withInsertion(final @Nullable String insertion)
    {
        return new GuiStyle(super.withInsertion(insertion), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withFont(final @Nullable Identifier font)
    {
        return new GuiStyle(super.withFont(font), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withShadow(final @Nullable Boolean shadow)
    {
        final StyleAccessor style_acc = (StyleAccessor) this;
        return (!Objects.equals(this.shaded, shadow)
            ? GuiStyle.with(
                new GuiStyle(this.getColor(), this.getShadowColor(), style_acc.novia$isBold(),
                             style_acc.novia$isItalic(), style_acc.novia$isUnderlined(),
                             style_acc.novia$isStrikethrough(), style_acc.novia$isObfuscated(), this.getClickEvent(),
                             this.getHoverEvent(), this.getInsertion(), this.getFont(), shadow, this.size,
                             this.kerning),
                this.shaded, shadow)
            : this);
    }

    public @NotNull GuiStyle withSize(final @Nullable Float size)
    {
        final StyleAccessor style_acc = (StyleAccessor) this;
        return (!Objects.equals(this.size, size)
            ? GuiStyle.with(
                new GuiStyle(this.getColor(), this.getShadowColor(), style_acc.novia$isBold(),
                             style_acc.novia$isItalic(), style_acc.novia$isUnderlined(),
                             style_acc.novia$isStrikethrough(), style_acc.novia$isObfuscated(), this.getClickEvent(),
                             this.getHoverEvent(), this.getInsertion(), this.getFont(), this.shaded, size,
                             this.kerning),
                this.size, size)
            : this);
    }

    public @NotNull GuiStyle withKerning(final @Nullable Float kerning)
    {
        final StyleAccessor style_acc = (StyleAccessor) this;
        return (!Objects.equals(this.kerning, kerning)
            ? GuiStyle.with(
                new GuiStyle(this.getColor(), this.getShadowColor(), style_acc.novia$isBold(),
                             style_acc.novia$isItalic(), style_acc.novia$isUnderlined(),
                             style_acc.novia$isStrikethrough(), style_acc.novia$isObfuscated(), this.getClickEvent(),
                             this.getHoverEvent(), this.getInsertion(), this.getFont(), this.shaded, this.size,
                             kerning),
                this.kerning, kerning)
            : this);
    }

    public @NotNull GuiStyle withFormatting(final @NotNull Formatting formatting)
    {
        if (formatting == Formatting.RESET) return GuiStyle.EMPTY;
        return new GuiStyle(super.withFormatting(formatting), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withExclusiveFormatting(final @NotNull Formatting formatting)
    {
        if (formatting == Formatting.RESET) return GuiStyle.EMPTY;
        return new GuiStyle(super.withExclusiveFormatting(formatting), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withFormatting(final @NotNull Formatting @NotNull ...formattings)
    {
        if (Arrays.stream(formattings).anyMatch(e -> e == Formatting.RESET)) return GuiStyle.EMPTY;
        return new GuiStyle(super.withFormatting(formattings), this.shaded, this.size, this.kerning);
    }

    public @NotNull GuiStyle withParent(final @NotNull GuiStyle parent)
    {
        if (this == GuiStyle.EMPTY)
        {
            return parent;
        }

        final StyleAccessor accessor        = (StyleAccessor) this;
        final StyleAccessor parent_accessor = (StyleAccessor) parent;
        
        return (parent != GuiStyle.EMPTY
            ? new GuiStyle(
                this.getOrParent(Style        ::getColor,              this,     parent),
                this.getOrParent(Style        ::getShadowColor,        this,     parent),
                this.getOrParent(StyleAccessor::novia$isBold,          accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isItalic,        accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isUnderlined,    accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isStrikethrough, accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isObfuscated,    accessor, parent_accessor),
                this.getOrParent(Style        ::getClickEvent,         this,     parent),
                this.getOrParent(Style        ::getHoverEvent,         this,     parent),
                this.getOrParent(Style        ::getInsertion,          this,     parent),
                this.getOrParent(StyleAccessor::novia$getFontId,       accessor, parent_accessor),
                this.getOrParent(GuiStyle     ::isShaded,              this,     parent),
                this.getOrParent(GuiStyle     ::getSize,               this,     parent),
                this.getOrParent(GuiStyle     ::getKerning,            this,     parent))
            : this);
    }

    @Override
    public @NotNull GuiStyle withParent(final @NotNull Style parent)
    {
        if (this == GuiStyle.EMPTY)
        {
            return new GuiStyle(parent, null, null, null);
        }

        final Boolean shadow;
        final Float   size;
        final Float   kerning;
        
        if (parent instanceof GuiStyle g_style)
        {
            shadow  = this.getOrParent(GuiStyle::isShaded,   this, g_style);
            size    = this.getOrParent(GuiStyle::getSize,    this, g_style);
            kerning = this.getOrParent(GuiStyle::getKerning, this, g_style);
        }
        else
        {
            shadow  = this.shaded;
            size    = this.size;
            kerning = this.kerning;
        }
        
        final StyleAccessor accessor        = (StyleAccessor) this;
        final StyleAccessor parent_accessor = (StyleAccessor) parent;
        
        return (!parent.isEmpty()
            ? new GuiStyle(
                this.getOrParent(Style        ::getColor,              this,     parent),
                this.getOrParent(Style        ::getShadowColor,        this,     parent),
                this.getOrParent(StyleAccessor::novia$isBold,          accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isItalic,        accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isUnderlined,    accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isStrikethrough, accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isObfuscated,    accessor, parent_accessor),
                this.getOrParent(Style        ::getClickEvent,         this,     parent),
                this.getOrParent(Style        ::getHoverEvent,         this,     parent),
                this.getOrParent(Style        ::getInsertion,          this,     parent),
                this.getOrParent(StyleAccessor::novia$getFontId,       accessor, parent_accessor),
                shadow,
                size,
                kerning)
            : this);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T, O> @Nullable T getOrParent(final @NotNull Function<O, T> getter, final @NotNull O primary,
                                           final @NotNull O parent)
    {
        final T first = getter.apply(primary);
        return (first != null ? first : getter.apply(parent));
    }

    //==================================================================================================================
    @Override
    public String toString()
    {
        final BiFunction<String, Object, String> appender = ((id, in) -> "," + id + '=' + in);
        final String                             string   = super.toString();

        return (
            string.substring(0, (string.length() - 1))
            + appender.apply("shaded",  this.shaded)
            + appender.apply("size",    this.size)
            + appender.apply("kerning", this.kerning)
            + "}"
        );
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof GuiStyle other)) return false;

        return (
            super.equals(o)
            && Objects.equals(this.shaded,  other.shaded)
            && Objects.equals(this.size,    other.size)
            && Objects.equals(this.kerning, other.kerning)
        );
    }

    @Override public int hashCode() { return Objects.hash(super.hashCode(), this.shaded, this.size, this.kerning); }

}

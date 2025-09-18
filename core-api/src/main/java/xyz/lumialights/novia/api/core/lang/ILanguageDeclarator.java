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
package xyz.lumialights.novia.api.core.lang;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;



//**********************************************************************************************************************
public interface ILanguageDeclarator<E extends Enum<E> & ILanguageDeclarator<E>>
    extends
        Text,
        ITranslationHolder
{
    //******************************************************************************************************************
    /**
     * Gets the translatable object of the current declarator.
     * @return The translation key
     */
    @NotNull TranslatableTextContent getTranslatable();
    
    @Override default TextContent     getContent()       { return this.getTranslatable(); }
    @Override default Style           getStyle()         { return Style.EMPTY; }
    @Override default List<Text>      getSiblings()      { return List.of(); }
    @Override default @NotNull String toTranslationKey() { return this.getTranslatable().getKey(); }
    
    //==================================================================================================================
    default @NotNull  MutableText asMutableText() { return MutableText.of(this.getTranslatable()); }
    @Override default OrderedText asOrderedText() { return asMutableText().asOrderedText(); }
    
    //==================================================================================================================
    default @NotNull MutableText styled(@NotNull final UnaryOperator<Style> styleUpdater)
    {
        return asMutableText().styled(styleUpdater);
    }

    default @NotNull MutableText fillStyle(@NotNull final Style styleOverride)
    {
        return asMutableText().fillStyle(styleOverride);
    }

    default @NotNull MutableText formatted(@NotNull final Formatting @NotNull ...formattings)
    {
        return asMutableText().formatted(formattings);
    }

    default @NotNull MutableText formatted(@NotNull final Formatting formatting)
    {
        return asMutableText().formatted(formatting);
    }

    default @NotNull MutableText withColor(final int color)
    {
        return asMutableText().withColor(color);
    }
    
    default @NotNull MutableText appended(@NotNull final String text)
    {
        return asMutableText().append(text);
    }

    default @NotNull MutableText appended(@NotNull final Text text)
    {
        return asMutableText().append(text);
    }
    
    //==================================================================================================================
    /**
     * Gets the translation component for the current translation key with the given late-bound arguments.
     * @param args The arguments to bind to this translation component
     * @return The translatable text component
     */
    default @NotNull MutableText withArgs(@NotNull final Object ...args)
    {
        return Text.translatable(this.getTranslatable().getKey(), args);
    }
    
    /**
     * Gets the translation component for the current translation key with the given fallback.
     * @param fallback The fallback string
     * @return The translatable text component
     */
    default @NotNull MutableText withFallback(@NotNull final String fallback)
    {
        return Text.translatable(this.getTranslatable().getKey(), fallback);
    }
    
    /**
     * Gets the translation component for the current translation key with the given fallback and late-bound arguments.
     * @param fallback The fallback string
     * @param args The arguments to bind to this translation component
     * @return The translatable text component
     */
    default @NotNull MutableText withFallback(@NotNull final String fallback, @NotNull final Object ...args)
    {
        return Text.translatableWithFallback(this.getTranslatable().getKey(), fallback, args);
    }
    
    //==================================================================================================================
    @SuppressWarnings("unchecked")
    default @NotNull LanguageMap.Mapped<E> mapped(@NotNull final String          defaultTranslation,
                                                  @NotNull final String @NotNull ...translations)
    {
        return new LanguageMap.Mapped<>((E) this, defaultTranslation, List.of(translations));
    }
}

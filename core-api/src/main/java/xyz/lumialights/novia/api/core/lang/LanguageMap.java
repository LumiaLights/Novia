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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class LanguageMap<E extends Enum<E> & ILanguageDeclarator<E>>
    extends HashMap<String, EnumMap<E, String>>
{
    //******************************************************************************************************************
    public record Mapped<E extends Enum<E> & ILanguageDeclarator<E>>(@NotNull E            constant,
                                                                     @NotNull String       defaultTranslation,
                                                                     @NotNull List<String> translations)
    {}
    
    //******************************************************************************************************************
    public static <E extends Enum<E> & ILanguageDeclarator<E>> @NotNull LanguageMap<E> create(
        @NotNull final Class<E> enumClass)
    {
        return new LanguageMap<>(enumClass);
    }
    
    //******************************************************************************************************************
    private final Class<E>                         enumClass;
    private final Object2ObjectMap<String, String> lang2LangFallbacks = new Object2ObjectOpenHashMap<>();
    private final Map<String, Map<String, String>> rawTranslations    = new Object2ObjectOpenHashMap<>();
    
    //******************************************************************************************************************
    private LanguageMap(final Class<E> enumClass)
    {
        Objects.requireNonNull(enumClass, "enum class must not be null");
        this.enumClass = enumClass;
    }
    
    //==================================================================================================================
    public @Nullable String getTranslation(@NotNull final String langCode, @NotNull final E translationKey)
    {
        Objects.requireNonNull(langCode,       "lang code must not be null");
        Objects.requireNonNull(translationKey, "translation key must not be null");
        
        final EnumMap<E, String> translations = this.get(langCode);
        return (translations != null ? translations.get(translationKey) : null);
    }
    
    //==================================================================================================================
    public void defaultsTo(@NotNull final String defaultLanguage, @NotNull final String ...targetLanguages)
    {
        if (!this.containsKey(defaultLanguage))
        {
            throw new IllegalArgumentException("default language '%s' is not registered".formatted(defaultLanguage));
        }
        
        for (final var target : targetLanguages)
        {
            if (target.equals(defaultLanguage))
            {
                throw new IllegalArgumentException("defaulted language code must not refer to itself");
            }
            
            this.lang2LangFallbacks.put(target, defaultLanguage);
            this.computeIfAbsent(target, (k -> new EnumMap<>(this.enumClass)));
        }
    }
    
    //==================================================================================================================
    public void addTranslation(@NotNull final String langCode,
                               @NotNull final E      translationKey,
                               @NotNull final String translation)
    {
        Objects.requireNonNull(langCode,       "lang code must not be null");
        Objects.requireNonNull(translationKey, "translation key must not be null");
        Objects.requireNonNull(translation,    "translation string must not be null");
        
        final EnumMap<E, String> translation_map = this.computeIfAbsent(langCode, (e -> new EnumMap<>(this.enumClass)));
        
        if (translation_map.containsKey(translationKey))
        {
            throw new TranslationRegisterException(String.format(
                "duplicate translation key '%s' for language '%s'",
                translationKey, langCode));
        }
        
        translation_map.put(translationKey, translation);
    }
    
    public void addAllTranslations(@NotNull final String langCode, @NotNull final Map<E, String> translations)
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation map must not be null");
        translations.forEach((key, val) -> this.addTranslation(langCode, key, val));
    }
    
    @SafeVarargs
    public final void addAllTranslations(@NotNull final String                                     langCode,
                                         @NotNull final Pair<@NotNull E, @NotNull String> @NotNull ...translations)
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation list must not be null");
        
        Arrays
            .stream(translations)
            .forEach(pair -> this.addTranslation(langCode, pair.first(), pair.second()));
    }
    
    @SafeVarargs
    public final void fromMapped(@NotNull final List<String>       langCodes,
                                 @NotNull final Mapped<E> @NotNull ...translations)
    {
        this.addAllTranslations(
            Language.DEFAULT_LANGUAGE,
            Arrays.stream(translations).collect(Collectors.toMap(Mapped::constant, Mapped::defaultTranslation)));
        
        for (int i = 0; i < langCodes.size(); i++)
        {
            final EnumMap<E, String> translation_map = this.computeIfAbsent(
                langCodes.get(i),
                (e -> new EnumMap<>(this.enumClass)));
            
            for (final var mapped : translations)
            {
                if (translation_map.containsKey(mapped.constant()))
                {
                    throw new TranslationRegisterException(
                        "duplicate key '%s'".formatted(mapped.constant().toTranslationKey()));
                }
                
                final String translation = (mapped.translations.size() > i ? mapped.translations.get(i) : null);
                translation_map.put(mapped.constant, (translation != null ? translation : mapped.defaultTranslation));
            }
        }
    }
    
    public void addRawTranslation(@NotNull final String langCode,
                                  @NotNull final String translationKey,
                                  @NotNull final String translation)
    {
        Objects.requireNonNull(langCode,       "lang code must not be null");
        Objects.requireNonNull(translationKey, "translation key must not be null");
        Objects.requireNonNull(translation,    "translation string must not be null");
        
        final Map<String, String> raw_translation_map = this.rawTranslations.computeIfAbsent(
            langCode,
            (e -> new HashMap<>()));
        
        if (raw_translation_map.containsKey(translationKey))
        {
            throw new TranslationRegisterException(
                "duplicate translation key '%s' for language '%s'".formatted(translationKey, langCode));
        }
        
        if (Arrays.stream(this.enumClass.getEnumConstants()).anyMatch(e -> e.toTranslationKey().equals(translationKey)))
        {
            throw new TranslationRegisterException(
                "invalid raw translation key '%s', already specified as translation declaration in %s"
                    .formatted(translationKey, this.enumClass.getSimpleName()));
        }
        
        raw_translation_map.put(translationKey, translation);
    }
    
    public void addAllRawTranslations(@NotNull final String langCode, @NotNull final Map<String, String> translations)
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation map must not be null");
        translations.forEach((key, val) -> this.addRawTranslation(langCode, key, val));
    }
    
    @SafeVarargs
    public final void addAllRawTranslations(
        @NotNull final String                                          langCode,
        @NotNull final Pair<@NotNull String, @NotNull String> @NotNull ...translations
    )
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation list must not be null");
        
        Arrays
            .stream(translations)
            .forEach(pair -> this.addRawTranslation(langCode, pair.first(), pair.second()));
    }
    
    @SafeVarargs
    public final <E2 extends Enum<E2> & ILanguageDeclarator<E2>> void fromMappedDeclarator(
        @NotNull final List<String>        langCodes,
        @NotNull final Mapped<E2> @NotNull ...translations)
    {
        this.addAllRawTranslations(
            Language.DEFAULT_LANGUAGE,
            Arrays.stream(translations).collect(Collectors.toMap(
                (t -> t.constant.toTranslationKey()),
                Mapped::defaultTranslation)));
        
        for (int i = 0; i < langCodes.size(); i++)
        {
            final Map<String, String> translation_map = this.rawTranslations.computeIfAbsent(
                langCodes.get(i),
                (e -> new HashMap<>()));
            
            for (final var mapped : translations)
            {
                final String key = mapped.constant.toTranslationKey();
                
                if (translation_map.containsKey(key))
                {
                    throw new TranslationRegisterException("duplicate key '%s'".formatted(key));
                }
                
                final String translation = (mapped.translations.size() > i ? mapped.translations.get(i) : null);
                translation_map.put(key, (translation != null ? translation : mapped.defaultTranslation));
            }
        }
    }
    
    //==================================================================================================================
    public @NotNull Stream<Pair<@NotNull String, @NotNull String>> streamLanguage(@NotNull final String langCode)
    {
        if (!this.containsKey(langCode))
        {
            throw new IllegalArgumentException("lang code '%s' is not registered".formatted(langCode));
        }
        
        final EnumMap<E, String>  translation_map = this.get(langCode);
        final EnumMap<E, String>  defaults_map    = (this.lang2LangFallbacks.containsKey(langCode)
            ? this.get(this.lang2LangFallbacks.get(langCode))
            : null);
        final Map<String, String> empty_map       = new HashMap<>();
        
        return Stream.concat(
            Arrays
                .stream(this.enumClass.getEnumConstants())
                .flatMap(constant ->
                {
                    final String translation = translation_map.get(constant);
                    
                    if (translation == null)
                    {
                        if (defaults_map != null)
                        {
                            final String defaulted = defaults_map.get(constant);
                            
                            if (defaulted != null)
                            {
                                return Stream.of(Pair.of(constant.toTranslationKey(), defaulted));
                            }
                        }
                        
                        return Stream.empty();
                    }
                    
                    return Stream.of(Pair.of(constant.toTranslationKey(), translation_map.get(constant)));
                }),
            this.rawTranslations
                .getOrDefault(langCode, empty_map)
                .entrySet()
                .stream()
                .map(Pair::of));
    }
}

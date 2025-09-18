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
package xyz.lumialights.novia.api.core.data.lang;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.lang.LanguageMap;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;


//**********************************************************************************************************************
public class NoviaLanguageProvider
    implements DataProvider
{
    //******************************************************************************************************************
    public static FabricDataGenerator.Pack.@NotNull RegistryDependentFactory<NoviaLanguageProvider> factory(
        @NotNull final LanguageMap<?> languageMap)
    {
        return ((output, registry) -> new NoviaLanguageProvider(output, languageMap, registry));
    }
    
    //******************************************************************************************************************
    protected final FabricDataOutput dataOutput;
    
    //------------------------------------------------------------------------------------------------------------------
    private final LanguageMap<?>                                   languageMap;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup;
    
    //==================================================================================================================
    public NoviaLanguageProvider(@NotNull final FabricDataOutput                                 dataOutput,
                                 @NotNull final LanguageMap<?>                                   languageMap,
                                 @NotNull final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup)
    {
        Objects.requireNonNull(dataOutput,     "data output must not be null");
        Objects.requireNonNull(languageMap,    "language map must not be null");
        Objects.requireNonNull(registryLookup, "registry lookup must not be null");
        
        this.dataOutput     = dataOutput;
        this.languageMap    = languageMap;
        this.registryLookup = registryLookup;
    }

    //==================================================================================================================
    public @NotNull String getName()
    {
        return "Languages [%s]".formatted(String.join(", ", this.languageMap.keySet()));
    }
    
    //==================================================================================================================
    public @NotNull CompletableFuture<?> run(final DataWriter writer)
    {
        if (this.languageMap.isEmpty())
        {
            return CompletableFuture.completedFuture(null);
        }
        
        return this.registryLookup.thenCompose((lookup) ->
        {
            final List<CompletableFuture<?>> futures = new ArrayList<>();
            
            for (final var lang_code : this.languageMap.keySet())
            {
                final JsonObject lang_entry_obj = new JsonObject();
                this.languageMap
                    .streamLanguage(lang_code)
                    .forEach(entry -> lang_entry_obj.addProperty(entry.first(), entry.second()));
                
                futures.add(DataProvider.writeToPath(
                    writer,
                    lang_entry_obj,
                    this.getLangFilePath(lang_code)));
            }
            
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    //------------------------------------------------------------------------------------------------------------------
    private @NotNull Path getLangFilePath(@NotNull final String code)
    {
        return this.dataOutput
            .getResolver(DataOutput.OutputType.RESOURCE_PACK, "lang")
            .resolveJson(Identifier.of(this.dataOutput.getModId(), code));
    }
}

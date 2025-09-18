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
package xyz.lumialights.novia.api.config.schema;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.registry.ConfigRegistry;
import xyz.lumialights.novia.api.config.schema.document.SchemaDocument;
import xyz.lumialights.novia.api.config.event.ConfigRegistryEvent;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.core.serialisation.dynops.DynamicOpsSerialisers;
import xyz.lumialights.novia.api.core.serialisation.dynops.IDynamicOpsSerialiser;
import xyz.lumialights.novia.api.core.util.FragmentId;
import xyz.lumialights.novia.api.core.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class SchemaStore
{
    //******************************************************************************************************************
    private static final Pattern SCHEMA_FILE_PATTERN_PATTERN = Pattern.compile("^([\\-_a-z0-9/.]+)\\.schema\\.json$");
    
    //******************************************************************************************************************
    private static @NotNull List<Pair<Identifier, URL>> findSchemas(@NotNull final String modId)
    {
        return FabricLoader.getInstance()
            .getModContainer(modId)
            .flatMap(container -> container.findPath("resources/schemas"))
            .<List<Pair<Identifier, URL>>>map(path ->
            {
                try (final Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, ((file, attrs) -> true)))
                {
                    return stream
                        .flatMap(spath ->
                        {
                            try
                            {
                                final Matcher m = SCHEMA_FILE_PATTERN_PATTERN.matcher(spath.getFileName().toString());
                                
                                if (m.matches())
                                {
                                    final Path   parent   = spath.getParent();
                                    final String rel_path = (parent.equals(path)
                                        ? ""
                                        : parent.relativize(path).toString());
                                    
                                    String id_path = (rel_path + m.group(1));
                                    
                                    if (id_path.startsWith("/"))
                                    {
                                        id_path = id_path.substring(1);
                                    }
                                    
                                    return Stream.of(Pair.of(Identifier.of(modId, id_path), spath.toUri().toURL()));
                                }
                            }
                            catch (final MalformedURLException ex)
                            {
                                Novia.LOGGER.debug(ex);
                            }
                            
                            return Stream.empty();
                        })
                        .toList();
                }
                catch (final IOException ex)
                {
                    Novia.LOGGER.debug("Problem getting schemas for mod '{}'", modId, ex);
                    return List.of();
                }
            })
            .orElse(List.of());
    }
    
    //******************************************************************************************************************
    private final Map<Identifier, SchemaDocument> store      = new HashMap<>();
    private final Set<String>                     namespaces = new HashSet<>();
    private final ConfigRegistry                  registry;
    
    //******************************************************************************************************************
    public SchemaStore(@NotNull final ConfigRegistry registry)
    {
        this.registry = registry;
        
        ConfigRegistryEvent.FREEZING.register(reg ->
        {
            if (this.registry == reg)
            {
                this.store.clear();
            }
        });
    }
    
    //==================================================================================================================
    public @Nullable SchemaDocument getSchema(@NotNull final Identifier schemaId)
    {
        return this.store.get(schemaId);
    }
    
    public @Nullable SchemaDocument loadAndGetSchema(@NotNull final Identifier schemaId)
    {
        this.loadSchemas(schemaId.getNamespace());
        return this.store.get(schemaId);
    }
    
    //==================================================================================================================
    public @Nullable IPropertySchema resolve(@NotNull final FragmentId id)
    {
        final SchemaDocument doc = this.store.get(id.id());
        
        if (doc == null)
        {
            return null;
        }
        
        return doc.findTagged(id.fragment()).orElse(null);
    }
    
    //==================================================================================================================
    public void loadSchemas(@NotNull final String modId)
    {
        if (this.namespaces.contains(modId))
        {
            return;
        }
        
        this.store.putAll(findSchemas(modId)
            .stream()
            .flatMap(p ->
            {
                try (final InputStream input = p.second().openStream())
                {
                    if (input != null)
                    {
                        final IDynamicOpsSerialiser<?> serialiser = DynamicOpsSerialisers
                            .get(SchemaDocument.DOCUMENT_OPS);
                        Objects.requireNonNull(serialiser, "Supplied non-registered serialiser");
                        
                        return serialiser.decode(input, SchemaDocument.CODEC).mapOrElse(
                            (doc -> Stream.of(p.withSecond(doc))),
                            (res -> Stream.empty()));
                    }
                    else
                    {
                        Novia.LOGGER.debug("No schema document found for namespace '{}'", modId);
                    }
                }
                catch (final IOException ex)
                {
                    Novia.LOGGER.debug("Could not load schema from schema document '{}'", p.second().getPath(), ex);
                }
                
                return Stream.empty();
            })
            .collect(Collectors.toMap(Pair::first, Pair::second)));
        this.namespaces.add(modId);
    }
}

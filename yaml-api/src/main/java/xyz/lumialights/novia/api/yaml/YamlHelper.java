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
package xyz.lumialights.novia.api.yaml;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.nodes.Node;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;



//**********************************************************************************************************************
public class YamlHelper
{
    //******************************************************************************************************************
    public static @Nullable Node parse(@NotNull final Reader reader, @NotNull final LoadSettings settings)
        throws IOException
    {
        return parse(IOUtils.toString(reader), settings);
    }

    public static @Nullable Node parse(@NotNull final InputStream stream, @NotNull final LoadSettings settings)
        throws IOException
    {
        return parse(IOUtils.toString(stream, StandardCharsets.UTF_8), settings);
    }

    public static @Nullable Node parse(@NotNull final Path file, @NotNull final LoadSettings settings)
        throws IOException
    {
        return parse(Files.readString(file), settings);
    }

    public static @Nullable Node parse(@NotNull final String text, @NotNull final LoadSettings settings)
    {
        final Compose compose = new Compose(settings);
        return compose.composeString(text).orElse(null);
    }
    
    //==================================================================================================================
    public static void serialise(@NotNull final Writer       writer,
                                 @NotNull final Node         node,
                                 @NotNull final DumpSettings settings)
        throws IOException
    {
        writer.write(serialiseToString(node, settings));
    }

    public static void serialise(@NotNull final OutputStream stream,
                                 @NotNull final Node         node,
                                 @NotNull final DumpSettings settings)
        throws IOException
    {
        IOUtils.write(serialiseToString(node, settings), stream, StandardCharsets.UTF_8);
    }

    public static void serialise(@NotNull final Path file,
                                 @NotNull final Node node,
                                 @NotNull final DumpSettings settings)
        throws IOException
    {
        Files.writeString(file, serialiseToString(node, settings));
    }

    public static @NotNull String serialiseToString(@NotNull final Node node, @NotNull final DumpSettings settings)
    {
        final Dump dump = new Dump(settings);
        
        final StringBuilder result = new StringBuilder();
        dump.dumpNode(node, stringBuilder(result));
        
        return result.toString();
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static @NotNull StreamDataWriter stringBuilder(@NotNull final StringBuilder writer)
    {
        return new StreamDataWriter()
        {
            //**********************************************************************************************************
            @Override
            public void write(final String str)
            {
                writer.append(str);
            }

            @Override
            public void write(final String str, int off, int len)
            {
                writer.append(str, off, (off + len));
            }

            //==========================================================================================================
            @Override
            public void flush() {}
        };
    }
}

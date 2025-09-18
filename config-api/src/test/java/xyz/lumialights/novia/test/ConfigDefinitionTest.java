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
package xyz.lumialights.novia.test;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.lumialights.novia.api.config.document.ConfigDocumentParseException;
import xyz.lumialights.novia.api.config.schema.document.SchemaDocument;

import java.io.IOException;
import java.net.URL;



//**********************************************************************************************************************
public class ConfigDefinitionTest
{
    //******************************************************************************************************************
    private static final String[] POINTERS = {
        "/exact/property",
        "/exact/another/property",
        "/someRootProperty",
        "/",
        "/pattern/hello/test",
        "/pattern/world/test",
        "/pattern/!/test"
    };
    
    //******************************************************************************************************************
    private static @NotNull SchemaDocument getSchema()
    {
        final URL url = ConfigDefinitionTest.class.getClassLoader().getResource("config.novia-config-api.json");
        
        if (url == null)
        {
            throw new RuntimeException("Could not find config api json file");
        }
        
        try
        {
            return ConfigDocument.parseSchemaDocument(url.openStream(), "test");
        }
        catch (final ConfigDocumentParseException | IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    //******************************************************************************************************************
    private static SchemaDocument SCHEMA;
    
    //******************************************************************************************************************
    @BeforeAll
    public static void parsing()
    {
        SCHEMA = getSchema();
    }
    
    //==================================================================================================================
    @Test
    public void lookup()
    {
    
    }
}

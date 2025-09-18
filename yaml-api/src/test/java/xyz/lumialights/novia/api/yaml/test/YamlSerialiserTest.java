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
package xyz.lumialights.novia.api.yaml.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.nodes.Node;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.yaml.YamlOps;
import xyz.lumialights.novia.api.yaml.YamlOpsSerialiser;

import java.util.*;



//**********************************************************************************************************************
public class YamlSerialiserTest
{
    //******************************************************************************************************************
    private final Value intValue    = new Value(0);
    private final Value floatValue  = new Value(0.00001f);
    private final Value stringValue = new Value("420");
    private final Value boolValue   = new Value(true);

    private final Value mapValue = new Value(Maps.newLinkedHashMap(ImmutableMap.of(
        new Value("boolean"), new Value(true),

        new Value("map"), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value("string"), new Value("hello"),
            new Value("null"), new Value(),

            new Value("list"), new Value(List.of(
                new Value("number"),
                new Value(1)))))))));

    private final Value listValue = new Value(List.of(
        new Value(true),
        new Value("hello"),
        new Value("map"),
        new Value(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value("null"), new Value(),

            new Value("list"), new Value(List.of(
                new Value("number"),
                new Value(1))))))));

    private final Value nullValue = new Value();
    
    private final Value all = new Value(ImmutableMap.of(
        new Value("int"),    intValue,
        new Value("float"),  floatValue,
        new Value("string"), stringValue,
        new Value("bool"),   boolValue,
        new Value("map"),    mapValue,
        new Value("list"),   listValue,
        new Value("null"),   nullValue));
    
    //******************************************************************************************************************
    @Test
    void serialise()
    {
        final DataResult<Node> node = Value.CODEC.encodeStart(YamlOps.INSTANCE, this.all);
        Assertions.assertTrue(node.isSuccess(), () -> node.error().orElseThrow().message());
        
        final String output = YamlOpsSerialiser.INSTANCE.serialiseString(node.getOrThrow());
        System.out.println(output);
    }
}

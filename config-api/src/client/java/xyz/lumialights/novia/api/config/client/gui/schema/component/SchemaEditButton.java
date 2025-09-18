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
package xyz.lumialights.novia.api.config.client.gui.schema.component;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.config.client.gui.ConfigScreen;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumialights.novia.api.gui.component.provided.NVSimpleButton;

import java.util.*;



//**********************************************************************************************************************
public class SchemaEditButton
    extends SchemaButton
{
    //******************************************************************************************************************
    private final PropertyId            id;
    private final IStatefulComponent<?> component;
    private final ConfigScreen          screen;
    
    //******************************************************************************************************************
    private static void openScreen(final @NotNull NVSimpleButton button)
    {
        final SchemaEditButton button2 = (SchemaEditButton) button;
        button2.screen.showEditScreen(
            button2.id,
            Text.literal(Objects.requireNonNull(button2.id.pointer().getName())),
            button2.component);
    }
    
    //******************************************************************************************************************
    public SchemaEditButton(final @NotNull PropertyId            propertyId,
                            final @NotNull IStatefulComponent<?> component,
                            final @NotNull ConfigScreen          screen)
    {
        super(SchemaEditButton::openScreen);
        
        this.id        = propertyId;
        this.component = component;
        this.screen    = screen;
    }
    
    //==================================================================================================================
    @Override public @NotNull Value getValue() { return this.component.getValue(); }
    
    //==================================================================================================================
    @Override public void setValue(final @NotNull Value value) { this.component.setValue(value); }
}

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

import com.google.common.collect.Sets;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.client.ConfigApiLangClient;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumialights.novia.api.gui.component.provided.NVSimpleButton;
import xyz.lumialights.novia.api.gui.event.GuiEventArgs;
import xyz.lumialights.novia.api.gui.event.GuiEventHandler;

import java.util.*;



//**********************************************************************************************************************
public abstract class SchemaButton
    extends NVSimpleButton
    implements IStatefulComponent<SchemaButton>
{
    //******************************************************************************************************************
    private final Set<GuiEventHandler<GuiEventArgs>> listeners = Sets.newIdentityHashSet();
    
    private boolean muted = false;
    
    //******************************************************************************************************************
    public SchemaButton(final @NotNull Text text) { super(text, ConfigApiLangClient.CONFIG_EDIT_BUTTON_TEXT); }
    public SchemaButton() { this(ConfigApiLangClient.CONFIG_EDIT_BUTTON_TEXT); }
    
    //==================================================================================================================
    @Override
    public void addChangeListener(final @NotNull GuiEventHandler<GuiEventArgs> handler)
    {
        this.listeners.add(handler);
    }
    
    @Override
    public void removeChangeListener(final @NotNull GuiEventHandler<GuiEventArgs> handler)
    {
        this.listeners.remove(handler);
    }
    
    @Override public void mute()   { this.muted = true;  }
    @Override public void unmute() { this.muted = false; }
    
    //==================================================================================================================
    protected void notifyChangeListeners()
    {
        if (this.muted)
        {
            return;
        }
        
        this.listeners.forEach(handler -> handler.handle(this, GuiEventArgs.EMPTY));
    }
}

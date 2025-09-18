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
package xyz.lumialights.novia.api.gui.component.input;

import net.minecraft.client.Keyboard;

import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.gui.component.GuiComponent;

import java.util.*;



//**********************************************************************************************************************
public final class KeyEvent
    extends AbstractInputEvent<KeyEvent>
{
    //******************************************************************************************************************
    /** The keyboard device that has triggered this event. */
    public final @NotNull Keyboard device = MinecraftClient.getInstance().keyboard;
    
    /** Either the key code of the key, which has been pressed, or the character that has been typed. */
    public final int input;
    
    /** The modifiers that have been pressed when this event has been triggered. */
    public final int modifiers;
    
    /** The scan code of the key, which has been pressed, or empty. */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final @NotNull Optional<Integer> scanCode;
    
    //******************************************************************************************************************
    public KeyEvent(final @NotNull GuiComponent source, final int input, final int scanCode, final int modifiers)
    {
        super(source);
    
        this.input     = input;
        this.modifiers = modifiers;
        this.scanCode  = Optional.of(scanCode);
    }
    
    public KeyEvent(final @NotNull GuiComponent source, final char character, final int modifiers)
    {
        super(source);
        
        this.input     = character;
        this.modifiers = modifiers;
        this.scanCode  = Optional.empty();
    }
    
    //==================================================================================================================
    /**
     * Gets the character represented by {@link #input}.
     * <p>
     * If this is not part of a char input event, using this might overflow the original input.
     * @return A character
     */
    public char character() { return (char) this.input; }
}

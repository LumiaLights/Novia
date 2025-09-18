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
package xyz.lumialights.novia.api.gui.component.provided;

import com.mojang.serialization.Codec;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.gui.GuiApiId;
import xyz.lumialights.novia.api.gui.component.ComponentUtil;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.IComponentNavigator;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;
import xyz.lumialights.novia.api.gui.property.GuiProperty;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;


//**********************************************************************************************************************
/**
 * An abstract base class for widgets that can be pressed.
 * <p>
 * Buttons using this base class will be rendered with the default Minecraft button texture as background,
 * custom implementations can implement this class to render custom things on top of the button texture
 * (e.g. {@link NVSimpleButton} for buttons providing rendering text on top).
 */
public abstract class NVAbstractButton<S extends NVAbstractButton<S>>
    extends GuiComponent
{
    //******************************************************************************************************************
    /** The interface used to listen to button click actions. */
    @FunctionalInterface
    public interface ActionListener<S extends NVAbstractButton<S>> extends Consumer<S> {}
    
    //******************************************************************************************************************
    /** See {@link NVAbstractButton#playClickSound}. */
    public static final boolean DEFAULT_SHOULD_PLAY_SOUND = true;
    
    //******************************************************************************************************************
    /** Describes whether the iconic click sound should play upon clicking the button. */
    public final GuiProperty.NonNull<Boolean> playClickSound;
    
    //------------------------------------------------------------------------------------------------------------------
    private ActionListener<S> action;
    
    //******************************************************************************************************************
    public NVAbstractButton(final @NotNull ActionListener<S> action, final @NotNull Text message)
    {
        super(message);
        
        this.playClickSound = GuiProperty.nonNull(NVAbstractButton.DEFAULT_SHOULD_PLAY_SOUND);
        this.action         = Objects.requireNonNull(action, "action must not be null");
        
        this.setWantsFocus(true);
    }
    
    //==================================================================================================================
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    public @NotNull ActionListener<S> getActionListener() { return this.action; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public @NotNull Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.ABSTRACT_BUTTON_PLAY_SOUND,
                this.playClickSound,
                Codec.BOOL));
    }
    
    //==================================================================================================================
    public void setActionListener(final @NotNull ActionListener<S> action)
    {
        this.action = Objects.requireNonNull(action, "action must not be null");
    }
    
    //==================================================================================================================
    /** Explicitly executes the button's associated click handler. */
    @SuppressWarnings("unchecked")
    public void makePress()
    {
        if (this.playClickSound.get())
        {
            ComponentUtil.playClickSound();
        }
        
        this.onPress();
        this.action.accept((S) this);
    }
    
    //==================================================================================================================
    @Override
    protected boolean onMouseDown(final @NotNull MouseEvent e)
    {
        if (!this.isActive() || !e.isLeftButtonDown())
        {
            return false;
        }
        
        this.makePress();
        return true;
    }
    
    @Override
    protected boolean onKeyDown(final @NotNull KeyEvent e)
    {
        if (this.isActive() && KeyCodes.isToggle(e.input))
        {
            this.makePress();
			return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    /** Can be overridden to let child implementations also listen to click events internally. */
    protected void onPress() {}
}

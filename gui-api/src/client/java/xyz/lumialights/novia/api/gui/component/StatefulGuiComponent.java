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
package xyz.lumialights.novia.api.gui.component;

import com.google.common.collect.Sets;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;

import java.util.*;



//**********************************************************************************************************************
/**
 * A {@link GuiComponent} that provides (usually) mutable state, which can be set and read.
 * <p>
 * This class is not a requirement for components that handle state but is recommended for API compatibility.
 *
 * @see GuiComponent
 * @see IStatefulComponent
 */
public abstract class StatefulGuiComponent<Self extends GuiComponent & IStatefulComponent<Self>>
    extends GuiComponent
    implements IStatefulComponent<Self>
{
    //******************************************************************************************************************
    private final Set<ChangeListener<Self>> listeners = Sets.newIdentityHashSet();
    
    private boolean muted = false;
    
    //******************************************************************************************************************
    /**
     * Constructs a new stateful GUI component.
     * @param message The component's initial message
     */
    public StatefulGuiComponent(@NotNull final Text message) { super(message); }
    
    /** Constructs a new stateful GUI component with an empty message. */
    public StatefulGuiComponent() {}
    
    //==================================================================================================================
    /**
     * Gets whether this component's change listeners are muted.
     * @return {@code true} if this component is muted
     */
    public final boolean isMuted() { return this.muted; }
    
    //==================================================================================================================
    @Override
    public final void addChangeListener(final @NotNull ChangeListener<Self> listener)
    {
        Objects.requireNonNull(listener, "listener must not be null");
        this.listeners.add(listener);
    }
    
    @Override
    public final void removeChangeListener(final @NotNull ChangeListener<Self> listener)
    {
        Objects.requireNonNull(listener, "listener must not be null");
        this.listeners.remove(listener);
    }
    
    //==================================================================================================================
    @Override public final void mute()   { this.muted = true; }
    @Override public final void unmute() { this.muted = false; }
    
    //==================================================================================================================
    /**
     * Triggers change notifications for all change listeners attached to this component.
     * <p>
     * If this component is currently muted, this doesn't do anything.
     */
    @SuppressWarnings("unchecked")
    protected final void notifyChangeListeners()
    {
        if (this.muted)
        {
            return;
        }
        
        this.listeners.forEach(listener -> listener.onChange((Self) this));
    }
}

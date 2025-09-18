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
package xyz.lumialights.novia.api.gui.component.integration;

import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.serialisation.IValueConvertible;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.gui.component.GuiComponent;


//**********************************************************************************************************************
/**
 * A base class used for {@link GuiComponent} objects to handle state in an API stable way. All implementations
 * provide listeners that track changes and the {@link IValueConvertible} for interoperability with {@link Value}
 * objects.
 */
public interface IStatefulComponent<Self extends GuiComponent & IStatefulComponent<Self>>
    extends IValueConvertible
{
    //******************************************************************************************************************
    
    /** A functional interface for listening to state changed in a stateful component. */
    @FunctionalInterface
    interface ChangeListener<Self extends GuiComponent & IStatefulComponent<Self>>
    {
        //**************************************************************************************************************
        void onChange(@NotNull Self component);
    }
    
    //******************************************************************************************************************
    /**
     * Adds a change listener to the internal listener queue that can be triggered upon change notifications.
     * <p>
     * Listeners are compared by identity, meaning no two listeners of the same instance can be added or removed.
     *
     * @param listener The listener instance
     */
    void addChangeListener(@NotNull ChangeListener<Self> listener);
    
    /**
     * Removes a change listener to the internal listener queue that can be triggered upon change notifications.
     * <p>
     * Listeners are compared by identity, meaning no two listeners of the same instance can be added or removed.
     *
     * @param listener The listener instance
     */
    void removeChangeListener(@NotNull ChangeListener<Self> listener);
    
    //==================================================================================================================
    /**
     * Disables change notifications for the component.
     * <p>
     * When muting a component, the caller of this method has to make sure to always unmute this component after
     * the necessary changes have been made; otherwise the component is at risk of never notifying its
     * listeners any more.
     */
    void mute();
    
    /** Re-enables change notifications for the component. */
    void unmute();
}

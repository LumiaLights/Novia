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

import net.minecraft.client.input.KeyCodes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.serialisation.IValueConvertible;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.gui.ApiDefine;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumialights.novia.api.gui.component.ComponentUtil;
import xyz.lumialights.novia.api.gui.component.IComponentNavigator;
import xyz.lumialights.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumialights.novia.api.gui.component.input.KeyEvent;
import xyz.lumialights.novia.api.gui.component.input.MouseEvent;



//**********************************************************************************************************************
/**
 * A clickable box that can be checked or unchecked on input events; this is a boolean component that can have one of
 * two values: {@code true} or {@code false}.
 * <p>
 * This component listens to enter and space keyboard input events to toggle its current state if it is focused.
 * <p>
 * This is a stateful GUI component, the value it contains can be converted to and from {@link Value}.
 * (for more details, see {@link #getValue()}, {@link #setValue(Value)} and {@link IValueConvertible})
 */
public class NVCheckBox
    extends StatefulGuiComponent<NVCheckBox>
{
    //******************************************************************************************************************
    public record CheckboxTexture(
        @NotNull Identifier activeChecked,
        @NotNull Identifier activeUnchecked,
        @NotNull Identifier focusedChecked,
        @NotNull Identifier focusedUnchecked,
        @NotNull Identifier inactiveChecked,
        @NotNull Identifier inactiveUnchecked
    )
    {
        //**************************************************************************************************************
        public @NotNull Identifier get(final boolean checked, final boolean focused, final boolean active)
        {
            if (active)
            {
                if (focused)
                {
                    return (checked ? this.focusedChecked : this.focusedUnchecked);
                }
                
                return (checked ? this.activeChecked : this.activeUnchecked);
            }
            
            return (checked ? this.inactiveChecked : this.inactiveUnchecked);
        }
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        void nvCheckBoxDrawBackground(@NotNull Canvas canvas, @NotNull NVCheckBox checkBox);
        void nvCheckBoxDrawCheckMark(@NotNull Canvas canvas, @NotNull NVCheckBox checkBox);
    }
    
    //******************************************************************************************************************
    /** The textures used to render the checkbox. */
    public static final CheckboxTexture TEXTURES = new CheckboxTexture(
        Identifier.of(ApiDefine.API_ID, "widget/checkbox_checked"),
        Identifier.of(ApiDefine.API_ID, "widget/checkbox_unchecked"),
        Identifier.of(ApiDefine.API_ID, "widget/checkbox_checked_focused"),
        Identifier.of(ApiDefine.API_ID, "widget/checkbox_unchecked_focused"),
        Identifier.of(ApiDefine.API_ID, "widget/checkbox_inactive"),
        Identifier.of(ApiDefine.API_ID, "widget/checkbox_inactive"));
    
    //******************************************************************************************************************
    private boolean checked;
    
    //******************************************************************************************************************
    /**
     * Constructs a new checkbox with the given initial state and component message.
     * @param checked {@code true} if the checkbox should be checked, {@code false} otherwise
     * @param message The component message
     */
    public NVCheckBox(final boolean checked, final @NotNull Text message)
    {
        super(message);
        
        this.checked = checked;
        this.setWantsFocus(true);
    }
    
    /**
     * Constructs a new checkbox with the given initial state.
     * @param checked {@code true} if the checkbox should be checked, {@code false} otherwise
     */
    public NVCheckBox(final boolean checked) { this(checked, ScreenTexts.EMPTY); }
    
    /** Constructs a new unchecked checkbox. */
    public NVCheckBox() { this(false); }
    
    //==================================================================================================================
    /**
     * Gets the state of this checkbox as a boolean {@link Value}.
     * @return The boolean {@link Value}
     */
    @Override public @NotNull Value getValue() { return new Value(this.checked); }
    
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    //==================================================================================================================
    /**
     * Whether this checkbox is currently checked.
     * @return {@code true} if it is checked
     */
    public boolean isChecked() { return this.checked; }
    
    //==================================================================================================================
    /**
     * Sets whether the checkbox should be checked, does nothing if it already has the given state.
     * @param checked {@code true} if it should be checked
     */
    public void setChecked(final boolean checked)
    {
        if (this.checked != checked)
        {
            this.checked = checked;
            this.notifyChangeListeners();
        }
    }
    
    /** Toggles the state of the checkbox to the opposite of its current value. */
    public void toggle()
    {
        this.checked = !this.checked;
        this.notifyChangeListeners();
    }
    
    /**
     * Sets the value of this checkbox as a {@link Value} object. If the value is not a boolean value, this does
     * nothing.
     * @param value The new boolean {@link Value}
     */
    @Override
    public void setValue(final @NotNull Value value)
    {
        if (!value.isBoolean())
        {
            return;
        }
        
        this.setChecked(value.getBoolean());
    }
    
    //==================================================================================================================
    @Override
    protected boolean onMouseDown(final @NotNull MouseEvent e)
    {
        if (this.isActive())
        {
            this.toggle();
            ComponentUtil.playClickSound();

            return true;
        }
        
        return false;
    }
    
    @Override
    protected boolean onKeyDown(final @NotNull KeyEvent e)
    {
        if (this.isActive() && KeyCodes.isToggle(e.input))
        {
            this.toggle();
            return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    @Override
    protected void draw(final @NotNull Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvCheckBoxDrawBackground(canvas, this);
        template.nvCheckBoxDrawCheckMark(canvas, this);
    }
}

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
package xyz.lumialights.novia.api.core.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



//**********************************************************************************************************************
/** Provides some helper utilities regarding enchantments. */
public abstract class NoviaEnchantmentHelper
{
    //******************************************************************************************************************
    /**
     * Returns the registry entry for an enchantment registry key found on an item stack.
     * @param key   The enchantment registry key
     * @param stack The item stack to search
     * @return The enchantment registry entry, or null if not found or the item stack was null
     */
    public static @Nullable RegistryEntry<Enchantment> forKey(@NotNull  final RegistryKey<Enchantment> key,
                                                              @Nullable final ItemStack                stack)
    {
        if (stack != null)
        {
            return stack.getEnchantments()
                .getEnchantments()
                .stream()
                .filter(e -> e.matchesKey(key))
                .findFirst()
                .orElse(null);
        }

        return null;
    }

    /**
     * Gets the current level of an enchantment on an item stack by the given enchantment registry key.
     * @param key   The enchantment registry key
     * @param stack The item stack to search
     * @return The current enchantment level or 0 if the enchantment was not found or the item stack was null
     */
    public static int getLevel(@NotNull final RegistryKey<Enchantment> key, @Nullable final ItemStack stack)
    {
        return getLevel(key, stack, 0);
    }

    /**
     * Gets the current level of an enchantment on an item stack by the given enchantment registry key.
     * @param key          The enchantment registry key
     * @param stack        The item stack to search
     * @param defaultValue The default value to return if the enchantment level could not be gathered
     * @return The current enchantment level or the given default value if the enchantment was not found
     *         or the item stack was null
     */
    public static int getLevel(@NotNull  final RegistryKey<Enchantment> key,
                               @Nullable final ItemStack                stack,
                                         final int                      defaultValue)
    {
        final RegistryEntry<Enchantment> entry = forKey(key, stack);
        return (entry != null ? stack.getEnchantments().getLevel(entry) : defaultValue);
    }
}

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
package xyz.lumialights.novia.api.core.lang;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.*;



//**********************************************************************************************************************
public interface ITranslationHolder
{
    //******************************************************************************************************************
    static @NotNull String of(@NotNull final ITranslationHolder holder)
    {
        return holder.toTranslationKey();
    }
    
    static @NotNull String of(@NotNull final Item item)
    {
        return item.getTranslationKey();
    }
    
    static @NotNull String of(@NotNull final Block block)
    {
        return block.getTranslationKey();
    }
    
    static @NotNull String of(final RegistryKey<ItemGroup> registryKey)
    {
        final ItemGroup   group   = Registries.ITEM_GROUP.getValueOrThrow(registryKey);
        final TextContent content = group.getDisplayName().getContent();
        
        if (content instanceof TranslatableTextContent translatableTextContent)
        {
            return translatableTextContent.getKey();
        }
        else
        {
            throw new UnsupportedOperationException(
                "ItemGroup (%s) is not translatable".formatted(group.getDisplayName().getString()));
        }
    }

    static @NotNull String of(@NotNull final EntityType<?> entityType)
    {
        return entityType.getTranslationKey();
    }

    static @NotNull String ofEnchantment(@NotNull final RegistryKey<Enchantment> enchantment)
    {
        return Util.createTranslationKey("enchantment", enchantment.getValue());
    }

    static @NotNull String of(@NotNull final RegistryEntry<EntityAttribute> entityAttribute)
    {
        return entityAttribute.value().getTranslationKey();
    }

    static @NotNull String of(@NotNull final StatType<?> statType)
    {
        return ("stat_type." + Objects.requireNonNull(Registries.STAT_TYPE.getId(statType)).toTranslationKey());
    }
    
    static @NotNull String of(@NotNull final Stat<Identifier> stat)
    {
        return ("stat." + Objects.requireNonNull(stat.getValue()).toTranslationKey());
    }

    static @NotNull String of(@NotNull final StatusEffect statusEffect)
    {
        return statusEffect.getTranslationKey();
    }

    static @NotNull String of(@NotNull final Identifier identifier)
    {
        return identifier.toTranslationKey();
    }

    static @NotNull String of(@NotNull final TagKey<?> tagKey)
    {
        return tagKey.getTranslationKey();
    }
    
    //******************************************************************************************************************
    @NotNull String toTranslationKey();
}

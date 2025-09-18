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
package xyz.lumialights.novia.test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.*;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.*;
import xyz.lumialights.novia.api.core.registry.IRegistryObjectMatcher;
import xyz.lumialights.novia.api.core.registry.MatcherSyntaxException;
import xyz.lumialights.novia.api.core.registry.RegistryMatcher;

import java.util.*;
import java.util.regex.Pattern;



//**********************************************************************************************************************
public class RegistryMatcherTest
{
    //******************************************************************************************************************
    @Test
    public void parsing()
    {
        final String          exact_expression = "!some_id|/[a-z0-9]+/|*_wildc?rd|#/id/|#wildcar*:id|#minecraft:simple";
        final RegistryMatcher reg_matcher      = RegistryMatcher.compile(exact_expression);
        
        final List<IRegistryObjectMatcher> match_list = reg_matcher.matchers();
        Assertions.assertEquals(6, match_list.size());
        Assertions.assertTrue(reg_matcher.negated());
        
        Assertions.assertEquals(
            "minecraft:some_id",
            Assertions.assertInstanceOf(RegistryMatcher.ExactMatcher.class, match_list.get(0)).id());
        
        final Pattern pattern = Assertions
            .assertInstanceOf(RegistryMatcher.RegexMatcher.class, match_list.get(1))
            .pattern();
        
        Assertions.assertTrue (pattern.matcher("dawdj893dadd9039").matches());
        Assertions.assertFalse(pattern.matcher("dawdj893dAdd9039").matches());
        Assertions.assertFalse(pattern.matcher("")                .matches());
        
        final RegistryMatcher.WildcardMatcher w_matcher = Assertions
            .assertInstanceOf(RegistryMatcher.WildcardMatcher.class, match_list.get(2));
        Assertions.assertTrue (w_matcher.matchesId(Identifier.of("some_wildcard")));
        Assertions.assertTrue (w_matcher.matchesId(Identifier.of("another_wildcard")));
        Assertions.assertTrue (w_matcher.matchesId(Identifier.of("_wildcard")));
        Assertions.assertTrue (w_matcher.matchesId(Identifier.of("wd_wildcfrd")));
        Assertions.assertFalse(w_matcher.matchesId(Identifier.of("somewildcard")));
        Assertions.assertFalse(w_matcher.matchesId(Identifier.of("some_")));
        Assertions.assertFalse(w_matcher.matchesId(Identifier.of("")));
        Assertions.assertFalse(w_matcher.matchesId(Identifier.of("wd_wildcadrd")));
        Assertions.assertFalse(w_matcher.matchesId(Identifier.of("wdd_wildcrd")));

        final RegistryMatcher.TagMatcher tag_1 =
            Assertions.assertInstanceOf(RegistryMatcher.TagMatcher.class, match_list.get(3));
        Assertions.assertInstanceOf(RegistryMatcher.RegexMatcher.class, tag_1.method());
        
        final RegistryMatcher.TagMatcher tag_2 =
            Assertions.assertInstanceOf(RegistryMatcher.TagMatcher.class, match_list.get(4));
        Assertions.assertInstanceOf(RegistryMatcher.WildcardMatcher.class, tag_2.method());

        final RegistryMatcher.TagMatcher tag_3 =
            Assertions.assertInstanceOf(RegistryMatcher.TagMatcher.class, match_list.get(5));
        Assertions.assertInstanceOf(RegistryMatcher.ExactMatcher.class, tag_3.method());
    }
    
    @Test
    public void testSyntax()
    {
        // Cannot start expression with pipe character
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("|some_expression"));
        
        // Cannot start expression with pipe character
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile(" |some_expression"));
        
        // Cannot start expression with pipe character
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("    |some_expression"));
        
        // Cannot end expression with pipe character
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_expression|"));
        
        // Cannot end expression with pipe character
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_expression| "));
        
        // Cannot end expression with pipe character
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_expression|     "));
        
        // Unterminated regex expression
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("/.*|some_expression"));
        
        // Empty match expression
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_id||some_expression"));
        
        // Empty match expression
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_id| |some_expression"));
        
        // Empty match expression
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_id|     |some_expression"));
        
        // Regex terminator escaping
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("/.*/[a-z]+/"));
        final RegistryMatcher.RegexMatcher pattern = (RegistryMatcher.RegexMatcher) Assertions.assertDoesNotThrow(
            () -> RegistryMatcher.compile("/.*\\/[a-z]+/")).matchers().getFirst();
        Assertions.assertEquals(".*/[a-z]+", pattern.pattern().pattern());
        
        // Name method expression starting with '/' becomes an unterminated RegEx method expression
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("/some_expression"));
        // Name method expression starting with '/' must be escaped with '@' to avoid interpretation as RegEx pattern
        final RegistryMatcher.ExactMatcher exact = Assertions.assertInstanceOf(
            RegistryMatcher.ExactMatcher.class,
            Assertions.assertDoesNotThrow(
                () -> RegistryMatcher.compile("@/some_expression")).matchers().getFirst());
        Assertions.assertEquals("minecraft:/some_expression", exact.id());
        
        // Invalid spaces inside name ID
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_  expression"));
        
        // Negation only works for the whole match expression, not single ones
        Assertions.assertThrowsExactly(
            MatcherSyntaxException.class,
            () -> RegistryMatcher.compile("some_expression|!another_expression"));
        
        // It's completely fine to split the expression across multiple lines too
        final RegistryMatcher reg_matcher = Assertions.assertDoesNotThrow(
            () -> RegistryMatcher.compile("""
        !
            stone
            | /minecraft:.+_(pick)?axe/
            | #minecraft:all_signs
            | #/minecraft:.*_armor/
            | #*_hoe
        """));
        
        Assertions.assertEquals(5, reg_matcher.matchers().size());
        Assertions.assertTrue(reg_matcher.negated());
        
        final RegistryMatcher.ExactMatcher match1 = Assertions.assertInstanceOf(
            RegistryMatcher.ExactMatcher.class,
            reg_matcher.matchers().get(0));
        Assertions.assertEquals("minecraft:stone", match1.id());
        
        final RegistryMatcher.RegexMatcher match2 = Assertions.assertInstanceOf(
            RegistryMatcher.RegexMatcher.class,
            reg_matcher.matchers().get(1));
        Assertions.assertEquals("minecraft:.+_(pick)?axe", match2.pattern().pattern());
        
        final RegistryMatcher.TagMatcher match3 = Assertions.assertInstanceOf(
            RegistryMatcher.TagMatcher.class,
            reg_matcher.matchers().get(2));
        final RegistryMatcher.ExactMatcher match3_exact = Assertions.assertInstanceOf(
            RegistryMatcher.ExactMatcher.class,
            match3.method());
        Assertions.assertEquals("minecraft:all_signs", match3_exact.id());
        
        final RegistryMatcher.TagMatcher match4 = Assertions.assertInstanceOf(
            RegistryMatcher.TagMatcher.class,
            reg_matcher.matchers().get(3));
        final RegistryMatcher.RegexMatcher match4_regex = Assertions.assertInstanceOf(
            RegistryMatcher.RegexMatcher.class,
            match4.method());
        Assertions.assertEquals("minecraft:.*_armor", match4_regex.pattern().pattern());
        
        final RegistryMatcher.TagMatcher match5 = Assertions.assertInstanceOf(
            RegistryMatcher.TagMatcher.class,
            reg_matcher.matchers().get(4));
        final RegistryMatcher.WildcardMatcher match5_wildcard = Assertions.assertInstanceOf(
            RegistryMatcher.WildcardMatcher.class,
            match5.method());
        Assertions.assertEquals("minecraft:*_hoe", match5_wildcard.wildcardPattern());
    }
    
    @Nested
    public class RegistryTests
    {
        //**************************************************************************************************************
        @SuppressWarnings("deprecation")
        @BeforeAll
        public static void prepare()
        {
            SharedConstants.createGameVersion();
            Bootstrap.initialize();
            Registries.bootstrap();
            
            Registries.BLOCK.startTagReload(new TagGroupLoader.RegistryTags<>(
                RegistryKeys.BLOCK,
                Map.of(
                    BlockTags.ALL_SIGNS, List.of(
                        Blocks.ACACIA_SIGN  .getRegistryEntry(),
                        Blocks.BAMBOO_SIGN  .getRegistryEntry(),
                        Blocks.BIRCH_SIGN   .getRegistryEntry(),
                        Blocks.CHERRY_SIGN  .getRegistryEntry(),
                        Blocks.SPRUCE_SIGN  .getRegistryEntry(),
                        Blocks.CRIMSON_SIGN .getRegistryEntry(),
                        Blocks.DARK_OAK_SIGN.getRegistryEntry(),
                        Blocks.JUNGLE_SIGN  .getRegistryEntry(),
                        Blocks.OAK_SIGN     .getRegistryEntry(),
                        Blocks.MANGROVE_SIGN.getRegistryEntry(),
                        Blocks.WARPED_SIGN  .getRegistryEntry(),
                        Blocks.PALE_OAK_SIGN.getRegistryEntry(),
                        
                        Blocks.ACACIA_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.BAMBOO_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.BIRCH_HANGING_SIGN   .getRegistryEntry(),
                        Blocks.CHERRY_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.SPRUCE_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.CRIMSON_HANGING_SIGN .getRegistryEntry(),
                        Blocks.DARK_OAK_HANGING_SIGN.getRegistryEntry(),
                        Blocks.JUNGLE_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.OAK_HANGING_SIGN     .getRegistryEntry(),
                        Blocks.MANGROVE_HANGING_SIGN.getRegistryEntry(),
                        Blocks.WARPED_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.PALE_OAK_HANGING_SIGN.getRegistryEntry(),
                        
                        Blocks.ACACIA_WALL_SIGN  .getRegistryEntry(),
                        Blocks.BAMBOO_WALL_SIGN  .getRegistryEntry(),
                        Blocks.BIRCH_WALL_SIGN   .getRegistryEntry(),
                        Blocks.CHERRY_WALL_SIGN  .getRegistryEntry(),
                        Blocks.SPRUCE_WALL_SIGN  .getRegistryEntry(),
                        Blocks.CRIMSON_WALL_SIGN .getRegistryEntry(),
                        Blocks.DARK_OAK_WALL_SIGN.getRegistryEntry(),
                        Blocks.JUNGLE_WALL_SIGN  .getRegistryEntry(),
                        Blocks.OAK_WALL_SIGN     .getRegistryEntry(),
                        Blocks.MANGROVE_WALL_SIGN.getRegistryEntry(),
                        Blocks.WARPED_WALL_SIGN  .getRegistryEntry(),
                        Blocks.PALE_OAK_WALL_SIGN.getRegistryEntry(),
                        
                        Blocks.ACACIA_WALL_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.BAMBOO_WALL_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.BIRCH_WALL_HANGING_SIGN   .getRegistryEntry(),
                        Blocks.CHERRY_WALL_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.SPRUCE_WALL_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.CRIMSON_WALL_HANGING_SIGN .getRegistryEntry(),
                        Blocks.DARK_OAK_WALL_HANGING_SIGN.getRegistryEntry(),
                        Blocks.JUNGLE_WALL_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.OAK_WALL_HANGING_SIGN     .getRegistryEntry(),
                        Blocks.MANGROVE_WALL_HANGING_SIGN.getRegistryEntry(),
                        Blocks.WARPED_WALL_HANGING_SIGN  .getRegistryEntry(),
                        Blocks.PALE_OAK_WALL_HANGING_SIGN.getRegistryEntry()),
                    
                    BlockTags.CRIMSON_STEMS, List.of(
                        Blocks.CRIMSON_HYPHAE         .getRegistryEntry(),
                        Blocks.CRIMSON_STEM           .getRegistryEntry(),
                        Blocks.STRIPPED_CRIMSON_HYPHAE.getRegistryEntry(),
                        Blocks.STRIPPED_CRIMSON_STEM  .getRegistryEntry()),
                    
                    BlockTags.WARPED_STEMS, List.of(
                        Blocks.WARPED_HYPHAE         .getRegistryEntry(),
                        Blocks.WARPED_STEM           .getRegistryEntry(),
                        Blocks.STRIPPED_WARPED_HYPHAE.getRegistryEntry(),
                        Blocks.STRIPPED_WARPED_STEM  .getRegistryEntry()))))
                .apply();
            
            Registries.ITEM.startTagReload(new TagGroupLoader.RegistryTags<>(
                RegistryKeys.ITEM,
                Map.of(
                    ItemTags.FOOT_ARMOR, List.of(
                        Items.LEATHER_BOOTS  .getRegistryEntry(),
                        Items.CHAINMAIL_BOOTS.getRegistryEntry(),
                        Items.IRON_BOOTS     .getRegistryEntry(),
                        Items.DIAMOND_BOOTS  .getRegistryEntry(),
                        Items.GOLDEN_BOOTS   .getRegistryEntry(),
                        Items.NETHERITE_BOOTS.getRegistryEntry()),
                    
                    ItemTags.LEG_ARMOR, List.of(
                        Items.LEATHER_LEGGINGS  .getRegistryEntry(),
                        Items.CHAINMAIL_LEGGINGS.getRegistryEntry(),
                        Items.IRON_LEGGINGS     .getRegistryEntry(),
                        Items.DIAMOND_LEGGINGS  .getRegistryEntry(),
                        Items.GOLDEN_LEGGINGS   .getRegistryEntry(),
                        Items.NETHERITE_LEGGINGS.getRegistryEntry()),
                    
                    ItemTags.CHEST_ARMOR, List.of(
                        Items.LEATHER_CHESTPLATE  .getRegistryEntry(),
                        Items.CHAINMAIL_CHESTPLATE.getRegistryEntry(),
                        Items.IRON_CHESTPLATE     .getRegistryEntry(),
                        Items.DIAMOND_CHESTPLATE  .getRegistryEntry(),
                        Items.GOLDEN_CHESTPLATE   .getRegistryEntry(),
                        Items.NETHERITE_CHESTPLATE.getRegistryEntry()),
                    
                    ItemTags.HEAD_ARMOR, List.of(
                        Items.LEATHER_HELMET  .getRegistryEntry(),
                        Items.CHAINMAIL_HELMET.getRegistryEntry(),
                        Items.IRON_HELMET     .getRegistryEntry(),
                        Items.DIAMOND_HELMET  .getRegistryEntry(),
                        Items.GOLDEN_HELMET   .getRegistryEntry(),
                        Items.NETHERITE_HELMET.getRegistryEntry(),
                        Items.TURTLE_HELMET   .getRegistryEntry()))))
                .apply();
        }
        
        //==============================================================================================================
        @Test
        public void idMatching()
        {
            // Without namespace
            {
                final String          expression = "stone";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.ExactMatcher.class, matcher.matchers().getFirst());
                
                Assertions.assertTrue(matcher.matches(Blocks.STONE));
                Registries.BLOCK
                    .streamEntries()
                    .filter(block -> (block.value() != Blocks.STONE))
                    .forEach(block -> Assertions.assertFalse(matcher.matches(block)));
            }
            
            // With namespace
            {
                final String          expression = "minecraft:stone";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.ExactMatcher.class, matcher.matchers().getFirst());
                
                Assertions.assertTrue(matcher.matches(Blocks.STONE));
                Registries.BLOCK
                    .streamEntries()
                    .filter(block -> (block.value() != Blocks.STONE))
                    .forEach(block -> Assertions.assertFalse(matcher.matches(block)));
            }
            
            // Regex
            {
                final String          expression = "/.*_(pick)?axe/";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.RegexMatcher.class, matcher.matchers().getFirst());
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> (item.value() instanceof PickaxeItem || item.value() instanceof AxeItem))
                    .forEach(item ->
                        Assertions.assertTrue(matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' is neither a pickaxe nor an axe")));
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> !(item.value() instanceof PickaxeItem || item.value() instanceof AxeItem))
                    .forEach(item -> Assertions.assertFalse(
                        matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' was either a pickaxe or an axe")));
            }
            
            // Wildcard without namespace
            {
                final String          expression = "minecraft:*_pickaxe";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.WildcardMatcher.class, matcher.matchers().getFirst());
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> (item.value() instanceof PickaxeItem))
                    .forEach(item -> Assertions.assertTrue(
                        matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' is not a pickaxe")));
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> !(item.value() instanceof PickaxeItem))
                    .forEach(item -> Assertions.assertFalse(
                        matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' was a pickaxe")));
            }
            
            // Negative exact match
            {
                final String          expression = "!minecraft:diamond_pickaxe";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.ExactMatcher.class, matcher.matchers().getFirst());
                Assertions.assertTrue(matcher.negated());
                
                Assertions.assertTrue (matcher.matches(Items.WOODEN_PICKAXE));
                Assertions.assertTrue (matcher.matches(Items.STONE_PICKAXE));
                Assertions.assertTrue (matcher.matches(Items.GOLDEN_PICKAXE));
                Assertions.assertTrue (matcher.matches(Items.IRON_PICKAXE));
                Assertions.assertTrue (matcher.matches(Items.NETHERITE_PICKAXE));
                Assertions.assertFalse(matcher.matches(Items.DIAMOND_PICKAXE));
            }
            
            // Negative wildcard match
            {
                final String          expression = "!minecraft:*stone";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.WildcardMatcher.class, matcher.matchers().getFirst());
                Assertions.assertTrue(matcher.negated());
                
                Assertions.assertTrue(matcher.matches(Blocks.STONE_SLAB));
                Assertions.assertTrue(matcher.matches(Blocks.REDSTONE_TORCH));
                Assertions.assertTrue(matcher.matches(Blocks.REDSTONE_WIRE));
                Assertions.assertTrue(matcher.matches(Blocks.STONE_BUTTON));
                
                Assertions.assertFalse(matcher.matches(Blocks.STONE));
                Assertions.assertFalse(matcher.matches(Blocks.GLOWSTONE));
                Assertions.assertFalse(matcher.matches(Blocks.END_STONE));
                Assertions.assertFalse(matcher.matches(Blocks.POINTED_DRIPSTONE));
                Assertions.assertFalse(matcher.matches(Blocks.COBBLESTONE));
                Assertions.assertFalse(matcher.matches(Blocks.MOSSY_COBBLESTONE));
                Assertions.assertFalse(matcher.matches(Blocks.SANDSTONE));
            }
            
            // Negative regex match
            {
                final String          expression = "!/minecraft:.+n_shovel/";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.RegexMatcher.class, matcher.matchers().getFirst());
                Assertions.assertTrue(matcher.negated());
                
                Assertions.assertTrue(matcher.matches(Items.STONE_SHOVEL));
                Assertions.assertTrue(matcher.matches(Items.DIAMOND_SHOVEL));
                Assertions.assertTrue(matcher.matches(Items.NETHERITE_SHOVEL));
                Assertions.assertTrue(matcher.matches(Items.STONE_HOE));
                Assertions.assertTrue(matcher.matches(Items.DIAMOND_PICKAXE));
                Assertions.assertTrue(matcher.matches(Items.NETHERITE_AXE));
                
                Assertions.assertFalse(matcher.matches(Items.WOODEN_SHOVEL));
                Assertions.assertFalse(matcher.matches(Items.GOLDEN_SHOVEL));
                Assertions.assertFalse(matcher.matches(Items.IRON_SHOVEL));
            }
            
            // Multi matching
            {
                final String          expression = "minecraft:*_*axe|/minecraft:.+_shovel/|wooden_hoe|stone_hoe|golden_hoe|iron_hoe|diamond_hoe|netherite_hoe";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(8, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.WildcardMatcher.class, matcher.matchers().get(0));
                Assertions.assertInstanceOf(RegistryMatcher.RegexMatcher   .class, matcher.matchers().get(1));
                matcher.matchers().stream().skip(2).forEach(m ->
                    Assertions.assertInstanceOf(RegistryMatcher.ExactMatcher.class, m));
                    
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> (item.value() instanceof PickaxeItem
                                  || item.value() instanceof AxeItem
                                  || item.value() instanceof HoeItem
                                  || item.value() instanceof ShovelItem))
                    .forEach(item -> Assertions.assertTrue(
                        matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' did not match '" + expression + "'")));
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> !(item.value() instanceof PickaxeItem
                                   || item.value() instanceof AxeItem
                                   || item.value() instanceof HoeItem
                                   || item.value() instanceof ShovelItem))
                    .forEach(item -> Assertions.assertFalse(
                        matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' did match '" + expression + "'")));
            }
            
            // Multi negative matching
            {
                final String          expression = "!minecraft:*_*axe|/minecraft:.+_shovel/|wooden_hoe|stone_hoe|golden_hoe|iron_hoe|diamond_hoe|netherite_hoe";
                final RegistryMatcher matcher    = RegistryMatcher.compile(expression);
                
                Assertions.assertEquals(8, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.WildcardMatcher.class, matcher.matchers().get(0));
                Assertions.assertInstanceOf(RegistryMatcher.RegexMatcher   .class, matcher.matchers().get(1));
                matcher.matchers().stream().skip(2).forEach(m ->
                    Assertions.assertInstanceOf(RegistryMatcher.ExactMatcher.class, m));
                
                Assertions.assertTrue(matcher.negated());
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> (item.value() instanceof PickaxeItem
                                  || item.value() instanceof AxeItem
                                  || item.value() instanceof HoeItem
                                  || item.value() instanceof ShovelItem))
                    .forEach(item -> Assertions.assertFalse(
                        matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' did match '" + expression + "'")));
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> !(item.value() instanceof PickaxeItem
                                   || item.value() instanceof AxeItem
                                   || item.value() instanceof HoeItem
                                   || item.value() instanceof ShovelItem))
                    .forEach(item -> Assertions.assertTrue(
                        matcher.matches(item),
                        (() -> "Item '" + item.getIdAsString() + "' did not match '" + expression + "'")));
            }
        }
        
        @Test
        public void tagMatching()
        {
            // Testing block tag expression
            {
                // Test with namespace
                final String          tag_expression = "#minecraft:all_signs";
                final RegistryMatcher matcher        = Assertions.assertDoesNotThrow(() ->
                    RegistryMatcher.compile(tag_expression));
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.TagMatcher.class, matcher.matchers().getFirst());
                
                Registries.BLOCK
                    .streamEntries()
                    .filter(block -> (block.value() instanceof AbstractSignBlock))
                    .forEach(block -> Assertions.assertTrue(
                        matcher.matches(block),
                        (() -> "Block '" + block.getIdAsString() + "' is not a '" + tag_expression + "'")));
                
                Registries.BLOCK
                    .streamEntries()
                    .filter(block -> !(block.value() instanceof AbstractSignBlock))
                    .forEach(block -> Assertions.assertFalse(
                        matcher.matches(block),
                        (() -> "Block '" + block.getIdAsString() + "' did match '" + tag_expression + "'")));
            }
            
            // Testing item tag expression
            {
                // Test without vanilla namespace and with cascading
                final String          tag_expression = "#head_armor|#foot_armor|#leg_armor|#chest_armor";
                final RegistryMatcher matcher        = Assertions.assertDoesNotThrow(() ->
                    RegistryMatcher.compile(tag_expression));
                
                Assertions.assertEquals(4, matcher.matchers().size());
                matcher.matchers().forEach(m -> Assertions.assertInstanceOf(
                    RegistryMatcher.TagMatcher.class,
                    m,
                    () -> "Matcher was not of type TagMatcher but instead '" + m.getClass().getCanonicalName() + '\''));
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> (item.value() instanceof ArmorItem))
                    .forEach(item -> Assertions.assertTrue(
                        matcher.matches(item.value().getDefaultStack()),
                        (() -> "Item '" + item.getIdAsString() + "' is not a '" + tag_expression + "'")));
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> !(item.value() instanceof ArmorItem))
                    .forEach(item -> Assertions.assertFalse(
                        matcher.matches(item.value().getDefaultStack()),
                        (() -> "Item '" + item.getIdAsString() + "' was a '" + tag_expression + "'")));
            }
            
            // Testing item tag again but with regex instead
            {
                // Test without vanilla namespace and with cascading
                final String          tag_expression = "#/minecraft:(head|foot|leg)_armor/|#chest_armor";
                final RegistryMatcher matcher        = Assertions.assertDoesNotThrow(() ->
                    RegistryMatcher.compile(tag_expression));
                
                Assertions.assertEquals(2, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.TagMatcher.class, matcher.matchers().getFirst());
                Assertions.assertInstanceOf(
                    RegistryMatcher.RegexMatcher.class,
                    ((RegistryMatcher.TagMatcher) matcher.matchers().getFirst()).method());
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> (item.value() instanceof ArmorItem))
                    .forEach(item -> Assertions.assertTrue(
                        matcher.matches(item.value().getDefaultStack()),
                        (() -> "Item '" + item.getIdAsString() + "' is not a '" + tag_expression + "'")));
                
                Registries.ITEM
                    .streamEntries()
                    .filter(item -> !(item.value() instanceof ArmorItem))
                    .forEach(item -> Assertions.assertFalse(
                        matcher.matches(item.value().getDefaultStack()),
                        (() -> "Item '" + item.getIdAsString() + "' was a '" + tag_expression + "'")));
            }
            
            // test wildcard tag matcher
            {
                final String          tag_expression = "#*_stems";
                final RegistryMatcher matcher        = Assertions.assertDoesNotThrow(() ->
                    RegistryMatcher.compile(tag_expression));
                
                Assertions.assertEquals(1, matcher.matchers().size());
                Assertions.assertInstanceOf(RegistryMatcher.TagMatcher.class, matcher.matchers().getFirst());
                Assertions.assertInstanceOf(
                    RegistryMatcher.WildcardMatcher.class,
                    ((RegistryMatcher.TagMatcher) matcher.matchers().getFirst()).method());
                
                Assertions.assertTrue(matcher.matches(Blocks.CRIMSON_HYPHAE));
                Assertions.assertTrue(matcher.matches(Blocks.CRIMSON_STEM));
                Assertions.assertTrue(matcher.matches(Blocks.STRIPPED_CRIMSON_STEM));
                Assertions.assertTrue(matcher.matches(Blocks.STRIPPED_CRIMSON_HYPHAE));
                Assertions.assertTrue(matcher.matches(Blocks.WARPED_HYPHAE));
                Assertions.assertTrue(matcher.matches(Blocks.WARPED_STEM));
                Assertions.assertTrue(matcher.matches(Blocks.STRIPPED_WARPED_STEM));
                Assertions.assertTrue(matcher.matches(Blocks.STRIPPED_WARPED_HYPHAE));
                
                Assertions.assertFalse(matcher.matches(Blocks.MELON_STEM));
                Assertions.assertFalse(matcher.matches(Blocks.PUMPKIN_STEM));
                Assertions.assertFalse(matcher.matches(Blocks.MUSHROOM_STEM));
                Assertions.assertFalse(matcher.matches(Blocks.OAK_LOG));
                Assertions.assertFalse(matcher.matches(Blocks.STRIPPED_OAK_LOG));
                Assertions.assertFalse(matcher.matches(Blocks.STONE));
                Assertions.assertFalse(matcher.matches(Blocks.DIRT));
            }
        }
    }
}

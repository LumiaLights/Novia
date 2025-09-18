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
package xyz.lumialights.novia.api.core.registry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.World;
import org.apache.commons.io.FilenameUtils;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.core.block.BlockContext;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
/**
 * Provides a set of matching tools that can be applied to blocks, items, entities or anything else that has a provided
 * registry.
 * <p>
 * A match expression is a string that resembles a certain aspect about a matchable object, like the ID of such an
 * object.
 * <p>
 * Match expressions can be cascaded with the pipe character '|' where the given object must match only one of the
 * subexpressions.
 * Additionally, subexpressions may begin with an exclamation mark '!' to indicate that the result is inverted and
 * will only evaluate true if the subexpression did not match.
 * <p>
 * There is 4 types of matchers provided by this library:
 * <table>
 *     <tr>
 *         <th style="white-space:nowrap">Matcher Type</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td>Tag</td>
 *         <td>Matches a tag ID to the given object's registered tags. (Tag match expressions start with a '#')</td>
 *     </tr>
 *     <tr>
 *         <td>RegEx</td>
 *         <td>
 *             Matches a given RegEx pattern to the object's registry ID.
 *             (Starts and ends with '/', end can be omitted if it is the last subexpression)
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>Wildcard</td>
 *         <td>
 *             Matches a given wildcard pattern to the object's registry ID.
 *             A wildcard pattern is a normal string where the character '?' indicates to match any single character
 *             and '*' to match any one to infinite characters.
 *             (see {@link FilenameUtils#wildcardMatch(String, String)})
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>Exact</td>
 *         <td>Matches an object only if the test and the object's ID are exactly the same.</td>
 *     </tr>
 * </table>
 * <p>
 * Be aware that RegEx matchers can only stand by themselves, that means they cannot be cascaded and only be one expression.
 * <p>
 * This is heavily inspired by Denizen's
 * <a href="https://meta.denizenscript.com/Docs/Languages/Advanced%20Object%20Matching">Advanced Object Matching</a>
 * system.
 */
public record RegistryMatcher(@NotNull List<IRegistryObjectMatcher> matchers, boolean negated)
{
    //******************************************************************************************************************
    public interface IMethodMatcher
        extends IRegistryObjectMatcher
    {
        //**************************************************************************************************************
        @Override
        default <E> boolean match(@NotNull final RegistryEntry<E> entry)
        {
            return matchesId(getId(entry));
        }
        
        boolean matchesId(@NotNull Identifier id);
    }
    
    //==================================================================================================================
    public record ExactMatcher(@NotNull String id)
        implements IMethodMatcher
    {
        //**************************************************************************************************************
        @Language("RegExp")
        public static final String  ID_PATTERN_STR = "^([_\\-a-z0-9.]+:)?[_\\-a-z0-9/.]+$";
        public static final Pattern ID_PATTERN     = Pattern.compile(ID_PATTERN_STR);
        
        //**************************************************************************************************************
        @Override
        public boolean matchesId(@NotNull final Identifier id)
        {
            return id.toString().equals(this.id);
        }
    }
    
    public record RegexMatcher(@NotNull Pattern pattern)
        implements IMethodMatcher
    {
        //**************************************************************************************************************
        @Override
        public boolean matchesId(@NotNull final Identifier id)
        {
            return this.pattern.matcher(id.toString()).matches();
        }
    }
    
    public record WildcardMatcher(@NotNull String wildcardPattern)
        implements IMethodMatcher
    {
        //**************************************************************************************************************
        @Language("RegExp")
        public static final String  WILDCARD_PATTERN_STR = "^([_\\-a-z0-9.?*]+:)?[_\\-a-z0-9/.?*]+$";
        public static final Pattern WILDCARD_PATTERN     = Pattern.compile(WILDCARD_PATTERN_STR);
        
        //**************************************************************************************************************
        @Override
        public boolean matchesId(@NotNull final Identifier id)
        {
            return FilenameUtils.wildcardMatch(id.toString(), this.wildcardPattern);
        }
    }
    
    public record TagMatcher(@NotNull IMethodMatcher method)
        implements IRegistryObjectMatcher
    {
        //**************************************************************************************************************
        @Override
        public <E> boolean match(@NotNull final RegistryEntry<E> entry)
        {
            return entry.streamTags().anyMatch(tag -> this.method.matchesId(tag.id()));
        }
    }
    
    //******************************************************************************************************************
    /**
     * Parses a match expression from a list of sub-expressions and constructs a new {@link RegistryMatcher}.
     * @param subExpressions A list of match sub-expressions
     * @param negated        Whether the matcher is negated (see {@link RegistryMatcher#negated()})
     * @return The new {@link RegistryMatcher} object
     * @throws MatcherSyntaxException If the expression contained a syntax error
     */
    public static @NotNull RegistryMatcher compile(@NotNull final List<String> subExpressions,
                                                            final boolean      negated)
    {
        final List<IRegistryObjectMatcher> result = new ArrayList<>(8);

        for (int i = 0; i < subExpressions.size(); i++)
        {
            final String expression = subExpressions.get(i);
            
            if (expression.isEmpty())
            {
                throw new InvalidIdentifierException("Empty sub-expression at index " + i);
            }
            
            final CharacterIterator it = new StringCharacterIterator(expression);
            skipWhitespace(it);
            
            if (it.current() == '#')
            {
                it.next();
                result.add(new TagMatcher(parseMethod(it, (c -> false))));
            }
            else
            {
                result.add(parseMethod(it, (c -> false)));
            }
        }
        
        return new RegistryMatcher(result, negated);
    }
    
    /**
     * Parses a match expression and constructs a new {@link RegistryMatcher}.
     * @param expression The match expression
     * @return The new {@link RegistryMatcher} object
     * @throws MatcherSyntaxException If the expression contained a syntax error
     */
    public static @NotNull RegistryMatcher compile(@NotNull final String expression)
    {
        if (expression.isEmpty())
        {
            throw new MatcherSyntaxException("Empty match expression");
        }
        
        if (expression.charAt(0) == '|')
        {
            throw new MatcherSyntaxException("Empty match expression at 1");
        }
        
        if (expression.endsWith("|"))
        {
            throw new MatcherSyntaxException("Empty match expression at " + expression.length());
        }
        
        final List<IRegistryObjectMatcher> result = new ArrayList<>(8);
        final CharacterIterator            it     = new StringCharacterIterator(expression);
        final boolean                      negated;
        
        // Match expression is negated
        if (it.first() == '!')
        {
            negated = true;
            
            if (it.next() == CharacterIterator.DONE)
            {
                throw new MatcherSyntaxException("Empty match expression at " + (it.getIndex() + 1));
            }
        }
        else
        {
            negated = false;
        }
        
        do
        {
            final char c = skipWhitespace(it);
            result.add(switch (c)
            {
                // Starting expression with expression terminators indicates an empty expression
                case '|', CharacterIterator.DONE ->
                    throw new MatcherSyntaxException("Empty sub-expression at " + (it.getIndex() + 1));
                
                // We are dealing with a tag expression
                case '#' ->
                {
                    it.next();
                    yield new TagMatcher(parseMethod(it, charEquals('|')));
                }
                
                // The remaining types are registry ID expressions
                default -> parseMethod(it, charEquals('|'));
            });
        }
        while (it.next() != CharacterIterator.DONE);
        
        return new RegistryMatcher(result, negated);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static @NotNull IMethodMatcher parseMethod(@NotNull final CharacterIterator            it,
                                                       @NotNull final Function<Character, Boolean> terminator)
    {
        char c = it.current();
        
        // Method cannot start with an expression terminator
        if (c == CharacterIterator.DONE || terminator.apply(c))
        {
            throw new MatcherSyntaxException("Empty match sub-expression at " + (it.getIndex() + 1));
        }
        
        final IMethodMatcher method = switch (c)
        {
            // '/' indicates that we are dealing with a RegEx expression
            case '/':
            {
                final char n = it.next();
                
                if (n == CharacterIterator.DONE || terminator.apply(n))
                {
                    throw new MatcherSyntaxException("Empty RegEx pattern at " + (it.getIndex() + 1));
                }
                
                @Language("RegExp")
                final String content = readRegexExpression(it);
                
                try
                {
                    yield new RegexMatcher(Pattern.compile(content));
                }
                catch (final PatternSyntaxException ex)
                {
                    throw new MatcherSyntaxException("Invalid RegEx pattern", ex);
                }
            }
            
            // We are dealing with a name expression,
            // optionally indicated by a '@' (necessary if the expression starts with a '/')
            case '@':
            {
                c = it.next();
                
                if (c == CharacterIterator.DONE || terminator.apply(c))
                {
                    throw new MatcherSyntaxException("Empty name pattern at " + (it.getIndex() + 1));
                }
            } // FALLTHROUGH
            default:
            {
                final StringBuilder result = new StringBuilder(64);
                
                do
                {
                    result.append(c);
                    c = it.next();
                }
                while (!(c == CharacterIterator.DONE || terminator.apply(c) || Character.isWhitespace(c)));
                
                final String  content             = result.toString();
                final boolean is_wildcard_pattern = (content.indexOf('*') > -1 || content.indexOf('?') > -1);
                
                final Matcher matcher =
                    (is_wildcard_pattern
                        ? WildcardMatcher.WILDCARD_PATTERN
                        : ExactMatcher   .ID_PATTERN)
                    .matcher(content);
                
                if (!matcher.matches())
                {
                    throw new MatcherSyntaxException(String.format(
                        "Invalid name '%s' pattern at %s, not a valid Minecraft (wildcard) ID expression",
                        content, (it.getIndex() + 1)));
                }
                
                @Subst("minecraft:test")
                final String id_pattern = (matcher.group(1) != null ? content : "minecraft:" + content);
                yield (is_wildcard_pattern ? new WildcardMatcher(id_pattern) : new ExactMatcher(id_pattern));
            }
        };
        
        final char end = skipWhitespace(it);
        
        if (end != CharacterIterator.DONE && !terminator.apply(end))
        {
            throw new MatcherSyntaxException(String.format(
                "Unexpected '%c' token in method expression at %d, expected match expression terminator",
                end, (it.getIndex() + 1)));
        }
        
        return method;
    }
    
    private static char skipWhitespace(@NotNull final CharacterIterator it)
    {
        for (char c = it.current(); c != CharacterIterator.DONE; c = it.next())
        {
            if (!Character.isWhitespace(c))
            {
                return c;
            }
        }
        
        return CharacterIterator.DONE;
    }
    
    private static @NotNull String readRegexExpression(@NotNull final CharacterIterator it)
    {
        final StringBuilder result = new StringBuilder(64);
        
        for (char c = it.current();; c = it.next())
        {
            if (c == CharacterIterator.DONE)
            {
                throw new MatcherSyntaxException("Unterminated RegEx escape sequence at " + (it.getIndex() + 1));
            }
            
            if (c == '\\')
            {
                c = it.next();
                
                if (c == CharacterIterator.DONE)
                {
                    throw new MatcherSyntaxException("Unterminated RegEx escape sequence at " + (it.getIndex() + 1));
                }
                
                if (c != '/')
                {
                    result.append('\\');
                }
            }
            else if (c == '/')
            {
                it.next();
                break;
            }
            
            result.append(c);
        }
        
        return result.toString();
    }
    
    private static Function<Character, Boolean> charEquals(final char c)
    {
        return (oc -> (c == oc));
    }
    
    //******************************************************************************************************************
    /**
     * Matches against a {@link BlockState}.
     * @param state The {@link BlockState} to match
     * @return True if the matcher matched
     */
    public boolean matches(@NotNull final BlockState state)
    {
        return matches(state.getRegistryEntry());
    }
    
    /**
     * Matches against an {@link ItemStack}.
     * @param stack The {@link ItemStack} to match
     * @return True if the matcher matched
     */
    public boolean matches(@NotNull final ItemStack stack)
    {
        return matches(stack.getRegistryEntry());
    }
    
    /**
     * Matches against a {@link Block}.
     * @param block The {@link Block} to match
     * @return True if the matcher matched
     */
    public boolean matches(@NotNull final Block block)
    {
        return matches(block.getDefaultState().getRegistryEntry());
    }
    
    /**
     * Matches against a {@link Block}.
     * @param block The {@link Block} to match
     * @return True if the matcher matched
     */
    public boolean matches(@NotNull final BlockContext block)
    {
        return matches(block.state().getRegistryEntry());
    }
    
    /**
     * Matches against an {@link Item}.
     * @param item The {@link Item} to match
     * @return True if the matcher matched
     */
    public boolean matches(@NotNull final Item item)
    {
        return matches(item.getDefaultStack().getRegistryEntry());
    }
    
    /**
     * Matches against a static registry {@link RegistryKey}.
     * <p>
     * Please note that this will only match against registry keys from static registries. Dynamic registry contents,
     * such as for enchantments, will fail; for these cases it is advisable to use
     * {@link RegistryMatcher#matches(World, RegistryKey)} instead.
     * <p>
     * If no registry for the given {@link RegistryKey} could be found, or the registry has no object with the key,
     * this will return {@code false}.
     * @param key The registry key to lookup
     * @return True if the matcher matched
     * @param <E> The object type the registry holds
     */
    public <E> boolean matches(@NotNull final RegistryKey<E> key)
    {
        @SuppressWarnings("unchecked")
        final Registry<E> registry = (Registry<E>) Registries.REGISTRIES.get(key.getRegistry());
        return (registry != null && matches(registry, key));
    }
    
    /**
     * Matches against a {@link RegistryKey} in the given registry.
     * <p>
     * If the given registry does not contain such a {@link RegistryKey}, this will return {@code false}.
     * @param key The registry key to lookup
     * @return {@code true} if the registry contains the given {@link RegistryKey} and the matcher matched
     * @param <E> The object type the registry holds
     */
    public <E> boolean matches(@NotNull final Registry<E> registry, @NotNull final RegistryKey<E> key)
    {
        final Optional<RegistryEntry.Reference<E>> entry = registry.getEntry(key.getValue());
        return (entry.isPresent() && matches(entry.get()));
    }
    
    /**
     * Matches against a {@link RegistryEntry}.
     * @param entry The {@link RegistryEntry} to match against
     * @return True if the {@link RegistryEntry} matched the matcher
     * @param <E> The object type the registry holds
     */
    public <E> boolean matches(@NotNull final RegistryEntry<E> entry)
    {
        return (!this.negated
            ? this.matchers.stream().anyMatch (m -> m.match(entry))
            : this.matchers.stream().noneMatch(m -> m.match(entry)));
    }
    
    /**
     * Matches against a dynamic registry {@link RegistryKey}.
     * <p>
     * Please note that this will only match against registry keys from dynamic registries. Static registry contents,
     * such as for blocks, items etc., will fail; for these cases it is advisable to use
     * {@link RegistryMatcher#matches(RegistryKey)} instead.
     * <p>
     * If no registry for the given {@link RegistryKey} could be found, or the registry has no object with the key,
     * this will return {@code false}.
     * @param world The current world holding the dynamic registries
     * @param key   The registry key to lookup
     * @return True if the matcher matched
     * @param <E> The object type the registry holds
     */
    public <E> boolean matches(@NotNull final World world, @NotNull final RegistryKey<E> key)
    {
        final Optional<Registry<E>> registry = world.getRegistryManager().getOptional(key.getRegistryRef());
        return (registry.isPresent() && matches(registry.orElseThrow(), key));
    }
    
    //==================================================================================================================
    public <E> @NotNull Stream<RegistryEntry.Reference<E>> streamRegistryEntries(@NotNull final Registry<E> registry)
    {
        return registry
            .streamEntries()
            .filter(this::matches);
    }
    
    public <E> @NotNull Stream<RegistryEntry.Reference<E>> streamDynamicRegistryEntries(
        @NotNull final World world,
        @NotNull final RegistryKey<Registry<E>> registryKey)
    {
        final Optional<Registry<E>> registry = world.getRegistryManager().getOptional(registryKey);
        return (registry.isPresent() ? streamRegistryEntries(registry.orElseThrow()) : Stream.empty());
    }
    
    //==================================================================================================================
    public <E> @NotNull List<E> getRegistryEntries(@NotNull final Registry<E> registry)
    {
        return streamRegistryEntries(registry)
            .map(RegistryEntry.Reference::value)
            .collect(Collectors.toList());
    }
    
    public <E> @NotNull List<E> getDynamicRegistryEntries(@NotNull final World                    world,
                                                          @NotNull final RegistryKey<Registry<E>> registryKey)
    {
        return streamDynamicRegistryEntries(world, registryKey)
            .map(RegistryEntry.Reference::value)
            .collect(Collectors.toList());
    }
}

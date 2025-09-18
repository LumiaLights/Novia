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
package xyz.lumialights.novia.api.core.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.lang3.ArrayUtils;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.lang.ITranslationHolder;

import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public class JsonPointer
    implements ITranslationHolder
{
    //******************************************************************************************************************
    /** The RegEx pattern to parse JSON pointer segments. (where capture group 1 is the current segment's content) */
    @Language("RegExp")
    public static final String POINTER_PATTERN = "/([^/]*)";
    
    /** The RegEx pattern to evaluate a full non-capturing JSON pointer expression. */
    @Language("RegExp")
    public static final String FULL_PATTERN = "^(?:/[^/]*)*$";
    
    /** Represents a pointer to the JSON root value. (by that means, a JSON pointer with a value of "") */
    public static final JsonPointer ROOT = new JsonPointer(new String[0]);
    
    /** Provides a packet codec for a JsonPointer object. */
    public static final PacketCodec<ByteBuf, JsonPointer> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, JsonPointer::toString,
        JsonPointer::compile);
    
    public static final Codec<JsonPointer> CODEC = Codec.STRING
        .xmap(JsonPointer::compile, JsonPointer::toString);

    //******************************************************************************************************************
    /**
     * Tries to compile a JSON pointer string to a new instance of the {@link JsonPointer} class.
     * <p>
     * If the pointer string was invalid, which really only happens if the string length is over 0 and it doesn't start
     * with a '/', this returns {@code null}.
     * @param pointer The pointer string
     * @return The new {@link JsonPointer} or null if the pointer string was invalid.
     */
    public static @Nullable JsonPointer compile(@Pattern(FULL_PATTERN) @NotNull final String pointer)
    {
        Objects.requireNonNull(pointer, "pointer must not be null");
        
        if (pointer.isEmpty())              return ROOT;
        if (!pointer.matches(FULL_PATTERN)) return null;
        
        return new JsonPointer(Arrays
            .stream(pointer.split("/", -1))
            .skip(1)
            .map(JsonPointer::unescape)
            .toArray(String[]::new));
    }

    public static @NotNull String escape(@NotNull final String string)
    {
        Objects.requireNonNull(string, "string must not be null");
        return string.replaceAll("~", "~0").replaceAll("/", "~1");
    }

    public static @NotNull String unescape(@NotNull final String string)
    {
        Objects.requireNonNull(string, "string must not be null");
        return string.replaceAll("~1", "/").replaceAll("~0", "~");
    }

    //******************************************************************************************************************
    private final String[] paths;
    private final String   pointer;

    //******************************************************************************************************************
    private JsonPointer(@NotNull final String[] paths)
    {
        this.paths   = paths;
        this.pointer = (paths.length > 0
            ? ('/' + Arrays.stream(paths).map(JsonPointer::escape).collect(Collectors.joining("/")))
            : "");
    }
    
    private JsonPointer(@NotNull final String[] paths, @NotNull final String pointer)
    {
        this.paths   = paths;
        this.pointer = pointer;
    }

    //==================================================================================================================
    /**
     * Gets a copy of the segment array this pointer comprises. (starting from root and ending at deepest key)
     * @return The segments array
     */
    public @NotNull String[] getPaths() { return this.paths.clone(); }
    
    /**
     * Gets the number of segments this pointer comprises.
     * @return The number of segments
     */
    public int getDepth() { return this.paths.length; }
    
    /**
     * Gets the value of the last (current) segment.
     * If the current pointer is the root pointer, this will return {@code null}.
     * @return The current segment name
     */
    public @Nullable String getName()
    {
        return (!isRoot()
            ? this.paths[this.paths.length - 1]
            : null);
    }
    
    /**
     * Gets the parent JSON pointer of the last (current) segment.
     * If the current pointer is the root pointer, this will return {@code null}.
     * @return The parent {@link JsonPointer} object
     */
    public @Nullable JsonPointer getParent()
    {
        if (isRoot())               return null;
        if (this.paths.length == 1) return ROOT;

        return new JsonPointer(Arrays.copyOfRange(this.paths, 0, (this.paths.length - 1)));
    }
    
    /**
     * Gets a pointer to a neighbouring key.
     * If this is the root pointer, returns null.
     * @param name Name of the sibling
     * @return The sibling pointer
     */
    public @Nullable JsonPointer getSibling(@NotNull final String name)
    {
        if (name.equals(getName())) return this;
        
        final JsonPointer parent = getParent();
        return (parent != null ? parent.getChild(name) : null);
    }
    
    /**
     * Gets a pointer to a direct child of the current pointer.
     * @param name The name of the child
     * @return The child pointer
     */
    public @NotNull JsonPointer getChild(@NotNull final String name)
    {
        return new JsonPointer(ArrayUtils.add(this.paths, name), (this.pointer + '/' + escape(name)));
    }

    //==================================================================================================================
    /**
     * Determines whether the current JSON pointer points to the root value of a JSON document.
     * @return {@code true} if the pointer points to the root value
     */
    public boolean isRoot() { return (this == ROOT); }

    //==================================================================================================================
    /**
     * Parses the given pointer string, combines it with the current pointer and returns a new pointer.
     * <p>
     * If the given pointer string was invalid, null will be returned.
     * @param pointer The pointer to resolve
     * @return The new JSON pointer or null
     */
    public @Nullable JsonPointer resolve(@Pattern(FULL_PATTERN) @NotNull final String pointer)
    {
        if (pointer.isEmpty()) return this;
        
        @Subst("")
        final String      subst = pointer;
        final JsonPointer temp  = JsonPointer.compile(subst);
        
        return (temp != null
            ? new JsonPointer(ArrayUtils.addAll(this.paths, temp.paths), (this.pointer + temp.pointer))
            : null);
    }
    
    //==================================================================================================================
    public @NotNull String toTranslationKey()
    {
        return String.join(".", this.paths);
    }
    
    //==================================================================================================================
    @Override
    public String toString() { return this.pointer; }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof JsonPointer other)) return false;
        return this.pointer.equals(other.pointer);
    }

    @Override
    public int hashCode() { return this.pointer.hashCode(); }
}

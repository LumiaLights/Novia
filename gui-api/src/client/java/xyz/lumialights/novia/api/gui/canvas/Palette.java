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
package xyz.lumialights.novia.api.gui.canvas;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.core.util.Colour;

import java.util.*;
import java.util.stream.Collectors;


//**********************************************************************************************************************
/** This class describes a map of {@link Colour} objects mapped to a reserved {@link ColourId}. */
public class Palette
{
    //******************************************************************************************************************
    private final Int2ObjectMap<Colour> colours;
    
    //******************************************************************************************************************
    /**
     * Constructs a new colour map object from the given colours and with an optional default colour.
     * @param colourMap     The colours to initialise the colour map with
     * @param defaultColour The default value to be returned when no colour for a given ID has been found
     */
    public Palette(final @NotNull Map<ColourId, Colour> colourMap, final @Nullable Colour defaultColour)
    {
        this.colours = new Int2ObjectArrayMap<>(colourMap
            .entrySet()
            .stream()
            .collect(ImmutableMap.toImmutableMap((e -> e.getKey().id), Map.Entry::getValue)));
        this.colours.defaultReturnValue(defaultColour);
    }
    
    /**
     * Constructs a new colour map object with the optional default colour.
     * @param defaultColour The default value to be returned when no colour for a given ID has been found
     */
    public Palette(final @Nullable Colour defaultColour)
    {
        this(Map.of(), defaultColour);
    }

    /** Constructs a new empty palette. */
    public Palette() { this(Map.of(), null); }
    
    //==================================================================================================================
    /**
     * Gets the colour for the given ID.
     * @param id The {@link ColourId}
     * @return The {@link Colour}, or if not found an empty {@link Optional} or the default colour if one was set
     */
    public final @NotNull Optional<Colour> getColour(final @NotNull ColourId id)
    {
        return Optional.ofNullable(this.colours.get(id.id));
    }
    
    /**
     * Gets the colour integer value for the given ID.
     * @param id The {@link ColourId}
     * @return The colour integer value, or if not found an empty {@link Optional} or the default colour if one was set
     */
    public final @NotNull Optional<Integer> getColourValue(final @NotNull ColourId id)
    {
        return this.getColour(id).map(Colour::colour);
    }
    
    /** {@return the number of colours in the map} */
    public final int getColourCount() { return this.colours.size(); }
    
    //==================================================================================================================
    /**
     * Gets whether there is any colour in the map.
     * @return {@code true} if there is at least one colour currently set
     */
    public final boolean hasColours() { return !this.colours.isEmpty(); }
    
    //==================================================================================================================
    /** {@return the ID and colour entry set} */
    public final @NotNull Set<Int2ObjectMap.Entry<Colour>> entrySet() { return this.colours.int2ObjectEntrySet(); }

    /** {@return the ID set} */
    public final @NotNull IntSet idSet() { return this.colours.keySet(); }

    /** {@return the colour list} */
    public final @NotNull Collection<Colour> colours() { return this.colours.values(); }
    
    //==================================================================================================================
    /**
     * Sets the colour for the given ID, or removes the colour if {@code null} is given.
     * @param id     The ID of the colour to set or unset
     * @param colour The colour to set or {@code null} to unset the colour
     * @return The previous colour associated with the ID, or null (or the default) if there was no colour set for that
     *         ID
     */
    public final @Nullable Colour setColour(final @NotNull ColourId id, final @Nullable Colour colour)
    {
        if (colour == null)
        {
            return this.colours.remove(id.id);
        }
        else
        {
            return this.colours.put(id.id, colour);
        }
    }

    /**
     * Sets the colour for the given ID if not already set for this palette.
     * @param id     The ID of the colour to set or unset
     * @param colour The colour to set or {@code null} to unset the colour
     * @return The previous colour associated with the ID, or null (or the default) if there was no colour set for that
     *         ID
     */
    public final @Nullable Colour setColourIfAbsent(final @NotNull ColourId id, final @Nullable Colour colour)
    {
        return this.colours.putIfAbsent(id.id, colour);
    }
    
    /**
     * Sets the same colour for a range of colour IDs.
     * @param ids    The range of colour IDs
     * @param colour The colour to set each one to
     */
    public final void setColours(final @NotNull Collection<ColourId> ids, final @Nullable Colour colour)
    {
        ids.forEach(id -> this.setColour(id, colour));
    }
    
    /**
     * Sets all the given colour IDs in the palette to the given colour values.
     * @param colours The colours to initialise
     */
    public final void setAllColours(final @NotNull Map<ColourId, Colour> colours)
    {
        this.colours.putAll(colours
            .entrySet()
            .stream()
            .collect(Collectors.toMap((e -> e.getKey().id), Map.Entry::getValue)));
    }
    
    //==================================================================================================================
    /** Clears the entire map. */
    public final void clear() { this.colours.clear(); }
}

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
package xyz.lumialights.novia.api.config.client.document;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.PropertyId;

import java.util.*;



//**********************************************************************************************************************
public record ScreenEntry(@NotNull ScreenEntry.Type                                type,
                          @NotNull Either<ScreenEntry.Property, ScreenEntry.Group> object)
{
    //******************************************************************************************************************
    public enum Type
        implements StringIdentifiable
    {
        GROUP,
        PROPERTY,
        ;
        
        //**************************************************************************************************************
        public static final Codec<Type> CODEC = StringIdentifiable.createCodec(Type::values);
        
        //**************************************************************************************************************
        @Override public String asString() { return this.name().toLowerCase(); }
    }
    
    //==================================================================================================================
    public record Property(@NotNull PropertyId               id,
                           @NotNull Optional<Text>           title,
                           @NotNull Optional<Text>           description,
                           @NotNull Optional<ComponentStyle> style)
    {
        //**************************************************************************************************************
        public static final MapCodec<Property> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                PropertyId.CODEC
                    .fieldOf("property")
                    .forGetter(Property::id),
                TextCodecs.CODEC
                    .optionalFieldOf("title")
                    .forGetter(Property::title),
                TextCodecs.CODEC
                    .optionalFieldOf("description")
                    .forGetter(Property::description),
                ComponentStyle.CODEC
                    .optionalFieldOf("style")
                    .forGetter(Property::style))
            .apply(instance, Property::new));
        
        //**************************************************************************************************************
        public Property(final @NotNull PropertyId id)
        {
            this(id, Optional.empty(), Optional.empty(), Optional.empty());
        }
        
        //==============================================================================================================
        public boolean isBasic()
        {
            return (this.description.isEmpty() && this.title.isEmpty() && this.style.isEmpty());
        }
    }
    
    public record Group(@NotNull List<Property> properties, @NotNull Text title)
    {
        //**************************************************************************************************************
        public static final MapCodec<Group> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                Codec
                    .withAlternative(
                        PropertyId.CODEC
                            .flatXmap(
                                (id   -> DataResult.success(new Property(id))),
                                (prop -> (prop.isBasic() ? DataResult.success(prop.id) : DataResult.error(() -> "")))),
                        Property.MAP_CODEC.codec())
                    .listOf()
                    .fieldOf("properties")
                    .forGetter(Group::properties),
                TextCodecs.CODEC
                    .fieldOf("title")
                    .forGetter(Group::title))
            .apply(instance, Group::new));
    }
    
    //******************************************************************************************************************
    public static final Codec<ScreenEntry> CODEC = Codec.withAlternative(
        PropertyId.CODEC
            .flatXmap(
                (id -> DataResult.success(new ScreenEntry(Type.PROPERTY, Either.left(new Property(id))))),
                (e  ->
                {
                    if (e.type == Type.PROPERTY)
                    {
                        final Property property = e.object.left().orElseThrow();
                        
                        if (property.isBasic())
                        {
                            return DataResult.success(property.id());
                        }
                    }
                    
                    return DataResult.error(() -> "");
                })),
        Type.CODEC.dispatch(
            ScreenEntry::type,
            (type -> (switch (type)
            {
                case GROUP -> Group.MAP_CODEC
                    .xmap((g -> new ScreenEntry(type, Either.right(g))), (e -> e.object.right().orElseThrow()));
                case PROPERTY -> Property.MAP_CODEC
                    .xmap((p -> new ScreenEntry(type, Either.left(p))), (e -> e.object.left().orElseThrow()));
            }))));
}

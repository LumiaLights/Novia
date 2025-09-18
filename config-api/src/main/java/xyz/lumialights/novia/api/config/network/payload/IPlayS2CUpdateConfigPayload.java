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
package xyz.lumialights.novia.api.config.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.ConfigApiIds;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.core.util.Pair;

import java.util.List;



//**********************************************************************************************************************
public sealed interface IPlayS2CUpdateConfigPayload
    extends CustomPayload
    permits
        IPlayS2CUpdateConfigPayload.Single,
        IPlayS2CUpdateConfigPayload.Bulk
{
    //******************************************************************************************************************
    PacketCodec<ByteBuf, Pair<JsonPointer, Value>> UPDATE_CODEC = Pair
        .getPacketCodec(
            JsonPointer.PACKET_CODEC,
            Value      .PACKET_CODEC);
    
    //******************************************************************************************************************
    record Single(@NotNull Identifier id, @NotNull Pair<JsonPointer, Value> update)
        implements IPlayS2CUpdateConfigPayload
    {
        //**************************************************************************************************************
        public static final Id<Single>                         ID;
        public static final PacketCodec<PacketByteBuf, Single> CODEC;
        
        //==============================================================================================================
        static
        {
            ID    = new Id<>(ConfigApiIds.Payload.PLAY_S2C_CONFIG_SINGLE);
            CODEC = PacketCodec
                .tuple(
                    Identifier.PACKET_CODEC, Single::id,
                    IPlayS2CUpdateConfigPayload.UPDATE_CODEC, Single::update,
                    Single::new);
        }
        
        //**************************************************************************************************************
        @Override
        public @NotNull List<Pair<JsonPointer, Value>> getUpdates() { return List.of(update); }
        
        @Override
        public @NotNull Identifier getProviderId() { return this.id; }
        
        //==============================================================================================================
        @Override
        public Id<Single> getId()
        {
            return ID;
        }
    }

    record Bulk(@NotNull Identifier id, @NotNull List<Pair<JsonPointer, Value>> updates)
        implements IPlayS2CUpdateConfigPayload
    {
        //**************************************************************************************************************
        public static final Id<Bulk>                         ID;
        public static final PacketCodec<PacketByteBuf, Bulk> CODEC;

        //==============================================================================================================
        static
        {
            ID    = new Id<>(ConfigApiIds.Payload.PLAY_S2C_CONFIG_BULK);
            CODEC = PacketCodec
                .tuple(
                    Identifier.PACKET_CODEC, Bulk::id,
                    IPlayS2CUpdateConfigPayload.UPDATE_CODEC.collect(PacketCodecs.toList()), Bulk::updates,
                    Bulk::new);
        }

        //**************************************************************************************************************
        @Override
        public @NotNull List<Pair<JsonPointer, Value>> getUpdates() { return this.updates; }
        
        @Override
        public @NotNull Identifier getProviderId() { return this.id; }
        
        //==============================================================================================================
        @Override
        public Id<Bulk> getId() { return ID; }
    }
    
    //******************************************************************************************************************
    @NotNull List<Pair<JsonPointer, Value>> getUpdates();
    @NotNull Identifier                     getProviderId();
}

package com.chara.some_interesting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ToggleFavoritePayload(int index) implements CustomPacketPayload {

    public static final Type<ToggleFavoritePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "toggle_favorite"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleFavoritePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ToggleFavoritePayload::index,
                    ToggleFavoritePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

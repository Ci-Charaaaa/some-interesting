package com.chara.some_interesting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelectBoundItemPayload(int index) implements CustomPacketPayload {

    public static final Type<SelectBoundItemPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "select_bound_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectBoundItemPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SelectBoundItemPayload::index,
                    SelectBoundItemPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

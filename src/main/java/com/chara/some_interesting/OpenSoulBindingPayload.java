package com.chara.some_interesting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenSoulBindingPayload() implements CustomPacketPayload {

    public static final Type<OpenSoulBindingPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "open_soul_binding"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSoulBindingPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> {}, buf -> new OpenSoulBindingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

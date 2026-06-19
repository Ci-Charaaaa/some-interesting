package com.chara.some_interesting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record BoundItemsSyncPayload(List<ItemStack> items) implements CustomPacketPayload {

    public static final Type<BoundItemsSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "bound_items_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoundItemsSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC, BoundItemsSyncPayload::items,
                    BoundItemsSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

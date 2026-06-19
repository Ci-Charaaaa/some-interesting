package com.chara.some_interesting;

import com.chara.some_interesting.Menu.SoulBindingMenu;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModNetworking {

    private static final Set<String> shownIntro = new HashSet<>();

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(BoundItemsSyncPayload.TYPE, BoundItemsSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(OpenSoulBindingPayload.TYPE, OpenSoulBindingPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectBoundItemPayload.TYPE, SelectBoundItemPayload.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> BoundItemStorage.get().load(server));
        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> BoundItemStorage.get().save(server));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> BoundItemStorage.get().save(server));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            BoundItemStorage.get().clear();
            shownIntro.clear();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            syncBoundItems(player);
        });

        ServerPlayNetworking.registerGlobalReceiver(OpenSoulBindingPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();

            String key = player.getScoreboardName();
            if (shownIntro.add(key)) {
                player.sendSystemMessage(Component.translatable("screen.some-interesting.intro.title"));
                player.sendSystemMessage(Component.translatable("screen.some-interesting.intro.bind"));
                player.sendSystemMessage(Component.translatable("screen.some-interesting.intro.select"));
                player.sendSystemMessage(Component.translatable("screen.some-interesting.intro.craft"));
                player.sendSystemMessage(Component.translatable("screen.some-interesting.intro.result"));
            }

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new SoulBindingMenu(id, inv),
                    Component.translatable("screen.some-interesting.blank")
            ));
        });

        ServerPlayNetworking.registerGlobalReceiver(SelectBoundItemPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof SoulBindingMenu menu) {
                List<net.minecraft.world.item.ItemStack> items = BoundItemStorage.get().getBoundItems(player);
                int index = payload.index();
                String key = BoundItemStorage.getPlayerKey(player);
                if (index >= 0 && index < items.size()) {
                    if (BoundItemStorage.get().isOnCooldown(key, index)) {
                        long remaining = BoundItemStorage.get().getRemainingCooldownMs(key, index);
                        long hours = remaining / 3600000;
                        long minutes = (remaining % 3600000) / 60000;
                        player.sendSystemMessage(Component.translatable(
                                "screen.some-interesting.cooldown", hours, minutes));
                    }
                    menu.setSelectedBoundItem(items.get(index), index, key);
                } else {
                    menu.setSelectedBoundItem(net.minecraft.world.item.ItemStack.EMPTY, -1, key);
                }
            }
        });
    }

    public static void syncBoundItems(ServerPlayer player) {
        List<net.minecraft.world.item.ItemStack> items = BoundItemStorage.get().getBoundItems(player);
        ServerPlayNetworking.send(player, new BoundItemsSyncPayload(items));
    }
}

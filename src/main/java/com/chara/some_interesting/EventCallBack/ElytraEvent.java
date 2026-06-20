package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.ElytraEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

public class ElytraEvent {

    private static final Map<UUID, Integer> flightTicks = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (!chest.is(Items.ELYTRA)) continue;

                if (!player.isFallFlying()) {
                    flightTicks.remove(player.getUUID());
                    continue;
                }

                int ticks = flightTicks.merge(player.getUUID(), 1, Integer::sum);
                if (ticks % 20 != 0) continue;

                var cfg = ModConfig.get().elytra;
                ElytraEnhanceComponent comp = chest.getOrDefault(
                        ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                        new ElytraEnhanceComponent(0, false, false, false));

                int normal = comp.normal_count() + 1;
                boolean ad = comp.is_adept();
                boolean sy = comp.is_synchronized();
                boolean so = comp.is_soulbound();
                int md = chest.getOrDefault(DataComponents.MAX_DAMAGE, 0);

                chest.set(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                        new ElytraEnhanceComponent(normal, ad, sy, so));

                boolean na = !ad && normal >= cfg.adeptThreshold;
                boolean ns = !sy && normal >= cfg.syncThreshold;
                boolean nl = !so && normal >= cfg.soulThreshold;
                if (na || ns || nl) {
                    String name = get_name(chest);

                    if (nl) {
                        chest.set(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                                new ElytraEnhanceComponent(normal, true, true, true));
                        chest.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.soulDurability));
                        chest.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "elytra", "soulbound", name, "max_level", (int)(md * cfg.soulDurability), cfg.soulSpeedBonus * 100 + "%");
                    } else if (ns) {
                        chest.set(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                                new ElytraEnhanceComponent(normal, ad, true, so));
                        chest.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.syncDurability));
                        chest.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "elytra", "synchronized", name, (int)(md * cfg.syncDurability), cfg.syncSpeedBonus * 100 + "%");
                    } else {
                        chest.set(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                                new ElytraEnhanceComponent(normal, true, sy, so));
                        chest.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.adeptDurability));
                        chest.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "elytra", "adept", name, (int)(md * cfg.adeptDurability), cfg.adeptSpeedBonus * 100 + "%");
                    }
                }
            }
        });
    }
}

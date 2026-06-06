package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.ElytraEnhanceComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

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

                // 累计飞行 tick
                int ticks = flightTicks.merge(player.getUUID(), 1, Integer::sum);
                if (ticks % 20 != 0) continue; // 每秒计一次

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

                // 升级判定
                boolean na = !ad && normal >= 120;
                boolean ns = !sy && normal >= 480;
                boolean nl = !so && normal >= 960;
                if (na || ns || nl) {
                    // 升级（异步 event 中不能直接播 sound，这里已经是 server 线程）
                    String name = get_name(chest);

                    if (nl) {
                        chest.set(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                                new ElytraEnhanceComponent(normal, true, true, true));
                        chest.set(DataComponents.MAX_DAMAGE, (int)(md * 1.8));
                        chest.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "elytra", "soulbound", name, "max_level", (int)(md * 1.8));
                    } else if (ns) {
                        chest.set(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                                new ElytraEnhanceComponent(normal, ad, true, so));
                        chest.set(DataComponents.MAX_DAMAGE, (int)(md * 1.5));
                        chest.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "elytra", "synchronized", name, (int)(md * 1.5));
                    } else {
                        chest.set(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                                new ElytraEnhanceComponent(normal, true, sy, so));
                        chest.set(DataComponents.MAX_DAMAGE, (int)(md * 1.2));
                        chest.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "elytra", "adept", name, (int)(md * 1.2));
                    }
                }

                // 速度加成已移至 ElytraSpeedMixin（降低空气阻力）
            }
        });
    }
}

package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.BowEnhanceComponent;
import com.chara.some_interesting.component.CrossbowEnhanceComponent;
import com.chara.some_interesting.component.TridentEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

public class RangedEvent {

    public static void initialize() {

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide()) {
                ItemStack heldstack = player.getItemInHand(hand);
                if (heldstack.is(Items.TRIDENT)) {
                    var cfg = ModConfig.get().trident;
                    TridentEnhanceComponent comp = heldstack.getOrDefault(
                            TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                            new TridentEnhanceComponent(0, 0, false, false, false));
                    int normal = comp.normal_count();
                    int newSuper = comp.super_count() + 1;
                    boolean ad = comp.is_adept();
                    boolean sy = comp.is_synchronized();
                    boolean so = comp.is_soulbound();
                    int md = heldstack.getOrDefault(DataComponents.MAX_DAMAGE, 0);

                    heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                            new TridentEnhanceComponent(normal, newSuper, ad, sy, so));

                    boolean na = !ad && (normal >= cfg.adeptNormal && newSuper >= cfg.adeptSuper);
                    boolean ns = !sy && (normal >= cfg.syncNormal && newSuper >= cfg.syncSuper);
                    boolean nl = !so && (normal >= cfg.soulNormal && newSuper >= cfg.soulSuper);

                    if (na || ns || nl) {
                        out_sound(world, player);
                        String name = get_name(heldstack);

                        if (nl) {
                            heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                                    new TridentEnhanceComponent(normal, newSuper, true, true, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.soulDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);
                            upgrade_text(player, "trident", "soulbound", name, "max_level", (int)(md * cfg.soulDurability), cfg.soulDamageBonus * 100 + "%");
                        } else if (ns) {
                            heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                                    new TridentEnhanceComponent(normal, newSuper, ad, true, so));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.syncDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);
                            upgrade_text(player, "trident", "synchronized", name, (int)(md * cfg.syncDurability), cfg.syncDamageBonus * 100 + "%");
                        } else if (na) {
                            heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                                    new TridentEnhanceComponent(normal, newSuper, true, sy, so));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.adeptDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);
                            upgrade_text(player, "trident", "adept", name, (int)(md * cfg.adeptDurability), cfg.adeptDamageBonus * 100 + "%");
                        }
                    }
                }

                if (heldstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(heldstack)) {
                    CrossbowEnhanceComponent comp = heldstack.getOrDefault(
                            CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                            new CrossbowEnhanceComponent(0, false, false, false));
                    int normal_count = comp.normal_count();
                    boolean is_adept = comp.is_adept();
                    boolean is_synchronized = comp.is_synchronized();
                    boolean is_soulbound = comp.is_soulbound();
                    int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE, 0);

                    heldstack.set(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                            new CrossbowEnhanceComponent(++normal_count, is_adept, is_synchronized, is_soulbound));

                    processCrossbowUpgrade(player, heldstack, normal_count, is_adept, is_synchronized, is_soulbound, max_damage);
                }

            }
            return InteractionResult.PASS;
        });
    }

    public static void processBowUpgrade(Player player, ItemStack heldstack,
                                          int normal_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound,
                                          int max_damage) {
        var cfg = ModConfig.get().bow;
        Level world = player.level();
        if (normal_count < cfg.adeptThreshold) {
        } else if (normal_count < cfg.syncThreshold) {
            if (!is_adept) {
                is_adept = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "bow", "adept", name, max_damage, cfg.adeptArrowBonus);

                heldstack.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                        new BowEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.adeptDurability));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else if (normal_count < cfg.soulThreshold) {
            if (!is_synchronized) {
                is_synchronized = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "bow", "synchronized", name, max_damage, cfg.syncArrowBonus);

                heldstack.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                        new BowEnhanceComponent(normal_count, is_adept, true, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.syncDurability));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else {
            if (!is_soulbound) {
                is_soulbound = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "bow", "soulbound", name, "max_level", max_damage, cfg.soulArrowBonus);

                heldstack.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                        new BowEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.soulDurability));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        }
    }

    public static void processCrossbowUpgrade(Player player, ItemStack heldstack,
                                               int normal_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound,
                                               int max_damage) {
        var cfg = ModConfig.get().crossbow;
        Level world = player.level();
        if (normal_count < cfg.adeptThreshold) {
        } else if (normal_count < cfg.syncThreshold) {
            if (!is_adept) {
                is_adept = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "crossbow", "adept", name, max_damage, cfg.adeptArrowBonus);

                heldstack.set(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                        new CrossbowEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.adeptDurability));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else if (normal_count < cfg.soulThreshold) {
            if (!is_synchronized) {
                is_synchronized = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "crossbow", "synchronized", name, max_damage, cfg.syncArrowBonus);

                heldstack.set(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                        new CrossbowEnhanceComponent(normal_count, is_adept, true, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.syncDurability));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else {
            if (!is_soulbound) {
                is_soulbound = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "crossbow", "soulbound", name, "max_level", max_damage, cfg.soulArrowBonus);

                heldstack.set(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                        new CrossbowEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.soulDurability));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        }
    }
}

package com.chara.test1.EventCallBack;

import com.chara.test1.component.BowEnhanceComponent;
import com.chara.test1.component.CrossbowEnhanceComponent;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static com.chara.test1.EventCallBack.AttackEvent.*;

public class RangedEvent {

    public static void initialize() {

        //弩射击计数，弓射击计数因调用物品栈会打断动画，逻辑移至注入方法中
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide()) {
                ItemStack heldstack = player.getItemInHand(hand);
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

    //弓升级处理
    public static void processBowUpgrade(Player player, ItemStack heldstack,
                                          int normal_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound,
                                          int max_damage) {
        Level world = player.level();
        if (normal_count <= 60) {
            //do nothing
        } else if (normal_count <= 180) {
            if (!is_adept) {
                is_adept = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "bow", "adept", name, max_damage);

                heldstack.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                        new BowEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.2));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else if (normal_count <= 500) {
            if (!is_synchronized) {
                is_synchronized = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "bow", "synchronized", name, max_damage);

                heldstack.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                        new BowEnhanceComponent(normal_count, is_adept, true, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.5));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else {
            if (!is_soulbound) {
                is_soulbound = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "bow", "soulbound", name, "max_level", max_damage);

                heldstack.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                        new BowEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.8));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        }
    }

    //弩升级处理
    public static void processCrossbowUpgrade(Player player, ItemStack heldstack,
                                               int normal_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound,
                                               int max_damage) {
        Level world = player.level();
        if (normal_count <= 60) {
            //do nothing
        } else if (normal_count <= 180) {
            if (!is_adept) {
                is_adept = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "crossbow", "adept", name, max_damage);

                heldstack.set(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                        new CrossbowEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.2));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else if (normal_count <= 540) {
            if (!is_synchronized) {
                is_synchronized = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "crossbow", "synchronized", name, max_damage);

                heldstack.set(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                        new CrossbowEnhanceComponent(normal_count, is_adept, true, is_soulbound));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.5));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        } else {
            if (!is_soulbound) {
                is_soulbound = true;
                out_sound(world, player);
                String name = get_name(heldstack);
                upgrade_text(player, "crossbow", "soulbound", name, "max_level", max_damage);

                heldstack.set(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                        new CrossbowEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.8));
                heldstack.set(DataComponents.REPAIR_COST, 0);
            }
        }
    }
}

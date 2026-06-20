package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.ShearsEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

public class ShearsEvent {

    private static boolean isShearsMineable(BlockState state) {
        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.WOOL)
                || state.is(Blocks.COBWEB)
                || state.is(Blocks.VINE)
                || state.is(Blocks.GLOW_LICHEN)
                || state.is(Blocks.CAVE_VINES)
                || state.is(Blocks.CAVE_VINES_PLANT)
                || state.is(Blocks.DRIED_KELP_BLOCK)
                || state.is(Blocks.TRIPWIRE);
    }

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (world.isClientSide()) return;
            ItemStack held = player.getMainHandItem();
            if (!held.is(Items.SHEARS) || !isShearsMineable(state)) return;
            checkAndUpgrade(player, held, world);
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            if (!held.is(Items.SHEARS)) return InteractionResult.PASS;
            if (!(entity instanceof LivingEntity)) return InteractionResult.PASS;

            boolean canShear = entity instanceof Sheep
                    || entity instanceof MushroomCow
                    || entity instanceof SnowGolem;
            if (!canShear) return InteractionResult.PASS;

            checkAndUpgrade(player, held, world);
            return InteractionResult.PASS;
        });
    }

    private static void checkAndUpgrade(Player player, ItemStack heldstack, Level world) {
        var cfg = ModConfig.get().shears;
        ShearsEnhanceComponent comp = heldstack.getOrDefault(
                ShearsEnhanceComponent.SHEARS_PROFICIENCY_COMPONENT,
                new ShearsEnhanceComponent(0, false, false, false));

        int normal = comp.normal_count() + 1;
        boolean ad = comp.is_adept();
        boolean sy = comp.is_synchronized();
        boolean so = comp.is_soulbound();
        int md = heldstack.getOrDefault(DataComponents.MAX_DAMAGE, 0);

        heldstack.set(ShearsEnhanceComponent.SHEARS_PROFICIENCY_COMPONENT,
                new ShearsEnhanceComponent(normal, ad, sy, so));

        boolean na = !ad && normal >= cfg.adeptThreshold;
        boolean ns = !sy && normal >= cfg.syncThreshold;
        boolean nl = !so && normal >= cfg.soulThreshold;
        if (!na && !ns && !nl) return;

        if (!(world instanceof ServerLevel serverLevel)) return;
        out_sound(serverLevel, player);
        String name = get_name(heldstack);

        if (nl) {
            heldstack.set(ShearsEnhanceComponent.SHEARS_PROFICIENCY_COMPONENT,
                    new ShearsEnhanceComponent(normal, true, true, true));
            heldstack.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.soulDurability));
            heldstack.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "shears", "soulbound", name, "max_level", (int)(md * cfg.soulDurability));
        } else if (ns) {
            heldstack.set(ShearsEnhanceComponent.SHEARS_PROFICIENCY_COMPONENT,
                    new ShearsEnhanceComponent(normal, ad, true, so));
            heldstack.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.syncDurability));
            heldstack.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "shears", "synchronized", name, (int)(md * cfg.syncDurability));
        } else {
            heldstack.set(ShearsEnhanceComponent.SHEARS_PROFICIENCY_COMPONENT,
                    new ShearsEnhanceComponent(normal, true, sy, so));
            heldstack.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.adeptDurability));
            heldstack.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "shears", "adept", name, (int)(md * cfg.adeptDurability));
        }
    }
}

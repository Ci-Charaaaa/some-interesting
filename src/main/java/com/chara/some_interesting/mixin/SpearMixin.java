package com.chara.some_interesting.mixin;

import com.chara.some_interesting.component.SpearEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

@Mixin(Item.class)
public class SpearMixin {

    @Inject(at = @At("HEAD"), method = "postHurtEnemy")
    private void testmod$onSpearPostHit(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (!(attacker instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!stack.is(ItemTags.SPEARS)) return;

        SpearEnhanceComponent comp = stack.getOrDefault(
                SpearEnhanceComponent.SPEAR_PROFICIENCY_COMPONENT,
                new SpearEnhanceComponent(0, false, false, false));
        int normal = comp.normal_count() + 1;
        boolean ad = comp.is_adept();
        boolean sy = comp.is_synchronized();
        boolean so = comp.is_soulbound();
        int md = stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);

        stack.set(SpearEnhanceComponent.SPEAR_PROFICIENCY_COMPONENT,
                new SpearEnhanceComponent(normal, ad, sy, so));

        boolean na = !ad && normal >= ModConfig.get().spear.adeptThreshold;
        boolean ns = !sy && normal >= ModConfig.get().spear.syncThreshold;
        boolean nl = !so && normal >= ModConfig.get().spear.soulThreshold;
        if (!na && !ns && !nl) return;

        out_sound(player.level(), player);
        String name = get_name(stack);

        if (nl) {
            stack.set(SpearEnhanceComponent.SPEAR_PROFICIENCY_COMPONENT,
                    new SpearEnhanceComponent(normal, true, true, true));
            stack.set(DataComponents.MAX_DAMAGE, (int)(md * ModConfig.get().spear.soulDurability));
            stack.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "spear", "soulbound", name, "max_level", (int)(md * ModConfig.get().spear.soulDurability));
        } else if (ns) {
            stack.set(SpearEnhanceComponent.SPEAR_PROFICIENCY_COMPONENT,
                    new SpearEnhanceComponent(normal, ad, true, so));
            stack.set(DataComponents.MAX_DAMAGE, (int)(md * ModConfig.get().spear.syncDurability));
            stack.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "spear", "synchronized", name, (int)(md * ModConfig.get().spear.syncDurability));
        } else {
            stack.set(SpearEnhanceComponent.SPEAR_PROFICIENCY_COMPONENT,
                    new SpearEnhanceComponent(normal, true, sy, so));
            stack.set(DataComponents.MAX_DAMAGE, (int)(md * ModConfig.get().spear.adeptDurability));
            stack.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "spear", "adept", name, (int)(md * ModConfig.get().spear.adeptDurability));
        }
    }
}

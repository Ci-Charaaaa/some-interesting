package com.chara.some_interesting.mixin;

import com.chara.some_interesting.component.FishingRodEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

@Mixin(FishingHook.class)
public class FishingMixin {

    @Shadow
    private int nibble;
    @Shadow
    private int luck;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
    private void testmod$onConstruct(Player player, Level level, int luck, int lureSpeed, CallbackInfo ci) {
        net.minecraft.world.item.ItemStack rod = player.getMainHandItem();
        if (!rod.is(Items.FISHING_ROD)) return;

        FishingRodEnhanceComponent comp = rod.getOrDefault(
                FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                new FishingRodEnhanceComponent(0, false, false, false));

        int penalty = 0;
        if (comp.is_soulbound()) penalty = ModConfig.get().fishing.soulLurePenalty;
        else if (comp.is_synchronized()) penalty = ModConfig.get().fishing.syncLurePenalty;
        else if (comp.is_adept()) penalty = ModConfig.get().fishing.adeptLurePenalty;

        if (penalty != 0) {
            ((FishingHookAccessor) this).setLureSpeed(Math.max(0, lureSpeed + penalty));
        }
    }

    @Inject(at = @At("HEAD"), method = "retrieve")
    private void testmod$onRetrieve(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook)(Object)this;
        if (!(hook.getPlayerOwner() instanceof Player player)) return;

        ItemStack rod = player.getMainHandItem();
        if (!rod.is(Items.FISHING_ROD)) return;

        FishingRodEnhanceComponent comp = rod.getOrDefault(
                FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                new FishingRodEnhanceComponent(0, false, false, false));
        int normal = comp.normal_count() + 1;
        boolean ad = comp.is_adept();
        boolean sy = comp.is_synchronized();
        boolean so = comp.is_soulbound();
        int md = rod.getOrDefault(DataComponents.MAX_DAMAGE, 0);

        int luckBonus = 0;
        if (so) luckBonus = ModConfig.get().fishing.soulLuckBonus;
        else if (sy) luckBonus = ModConfig.get().fishing.syncLuckBonus;
        else if (ad) luckBonus = ModConfig.get().fishing.adeptLuckBonus;
        if (luckBonus > 0) {
            ((FishingHookAccessor) hook).setLuck(this.luck + luckBonus);
        }

        rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                new FishingRodEnhanceComponent(normal, ad, sy, so));

        boolean na = !ad && normal >= ModConfig.get().fishing.adeptThreshold;
        boolean ns = !sy && normal >= ModConfig.get().fishing.syncThreshold;
        boolean nl = !so && normal >= ModConfig.get().fishing.soulThreshold;
        if (!na && !ns && !nl) return;

        if (!(hook.level() instanceof ServerLevel serverLevel)) return;
        out_sound(serverLevel, player);
        String name = get_name(rod);

        if (nl) {
            rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                    new FishingRodEnhanceComponent(normal, true, true, true));
            rod.set(DataComponents.MAX_DAMAGE, (int)(md * ModConfig.get().fishing.soulDurability));
            rod.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "fishing", "soulbound", name, "max_level", (int)(md * ModConfig.get().fishing.soulDurability), ModConfig.get().fishing.soulLuckBonus, ModConfig.get().fishing.soulLurePenalty);
        } else if (ns) {
            rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                    new FishingRodEnhanceComponent(normal, ad, true, so));
            rod.set(DataComponents.MAX_DAMAGE, (int)(md * ModConfig.get().fishing.syncDurability));
            rod.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "fishing", "synchronized", name, (int)(md * ModConfig.get().fishing.syncDurability), ModConfig.get().fishing.syncLuckBonus, ModConfig.get().fishing.syncLurePenalty);
        } else {
            rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                    new FishingRodEnhanceComponent(normal, true, sy, so));
            rod.set(DataComponents.MAX_DAMAGE, (int)(md * ModConfig.get().fishing.adeptDurability));
            rod.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "fishing", "adept", name, (int)(md * ModConfig.get().fishing.adeptDurability), ModConfig.get().fishing.adeptLuckBonus, ModConfig.get().fishing.adeptLurePenalty);
        }
    }
}

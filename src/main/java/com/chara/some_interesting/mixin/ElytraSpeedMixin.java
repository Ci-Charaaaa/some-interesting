package com.chara.some_interesting.mixin;

import com.chara.some_interesting.component.ElytraEnhanceComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ElytraSpeedMixin {

    @Inject(at = @At("TAIL"), method = "updateFallFlyingMovement", cancellable = true)
    private void testmod$modifyElytraSpeed(Vec3 velocity, CallbackInfoReturnable<Vec3> cir) {
        if (!(((LivingEntity)(Object)this) instanceof Player player)) return;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(Items.ELYTRA)) return;

        ElytraEnhanceComponent comp = chest.getOrDefault(
                ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT,
                new ElytraEnhanceComponent(0, false, false, false));

        double boost = 0;
        if (comp.is_soulbound()) boost = 0.03;
        else if (comp.is_synchronized()) boost = 0.02;
        else if (comp.is_adept()) boost = 0.01;

        if (boost > 0) {
            Vec3 original = cir.getReturnValue();
            // 缩放速度以抵消部分空气阻力 → 火箭推力更持久，极限速度更高
            cir.setReturnValue(original.scale(1.0 + boost));
        }
    }
}

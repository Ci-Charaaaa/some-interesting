package com.chara.some_interesting.mixin;

import com.chara.some_interesting.EventCallBack.RangedEvent;
import com.chara.some_interesting.component.BowEnhanceComponent;
import com.chara.some_interesting.component.CrossbowEnhanceComponent;
import com.chara.some_interesting.component.TridentEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

@Mixin(Projectile.class)
public class ArrowDamageMixin {

    @Inject(
            at = @At("TAIL"),
            method = "applyOnProjectileSpawned"
    )
    private void testmod$onProjectileSpawned(ServerLevel level, ItemStack stack, CallbackInfo ci) {
        if (!((Projectile)(Object)this instanceof AbstractArrow arrow)) {
            return;
        }

        Entity owner = arrow.getOwner();
        if (!(owner instanceof Player player)) {
            return;
        }

        double multiplier = 1.0;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.is(Items.BOW)) {
            BowEnhanceComponent comp = weapon.getOrDefault(
                    BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                    new BowEnhanceComponent(0, false, false, false));
            int normal_count = comp.normal_count();
            boolean is_adept = comp.is_adept();
            boolean is_synchronized = comp.is_synchronized();
            boolean is_soulbound = comp.is_soulbound();
            int max_damage = weapon.getOrDefault(DataComponents.MAX_DAMAGE, 0);

            weapon.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                    new BowEnhanceComponent(++normal_count, is_adept, is_synchronized, is_soulbound));

            RangedEvent.processBowUpgrade(player, weapon, normal_count, is_adept, is_synchronized, is_soulbound, max_damage);

            BowEnhanceComponent updated = weapon.getOrDefault(
                    BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                    new BowEnhanceComponent(0, false, false, false));
            if (updated.is_soulbound()) {
                multiplier = ModConfig.get().bow.soulArrowBonus;
            } else if (updated.is_synchronized()) {
                multiplier = ModConfig.get().bow.syncArrowBonus;
            } else if (updated.is_adept()) {
                multiplier = ModConfig.get().bow.adeptArrowBonus;
            }
        }

        if (weapon.is(Items.CROSSBOW)) {
            CrossbowEnhanceComponent comp = weapon.getOrDefault(
                    CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                    new CrossbowEnhanceComponent(0, false, false, false));
            if (comp.is_soulbound()) {
                multiplier = ModConfig.get().crossbow.soulArrowBonus;
            } else if (comp.is_synchronized()) {
                multiplier = ModConfig.get().crossbow.syncArrowBonus;
            } else if (comp.is_adept()) {
                multiplier = ModConfig.get().crossbow.adeptArrowBonus;
            }
        }

        if (stack.is(Items.TRIDENT)) {
            TridentEnhanceComponent comp = stack.getOrDefault(
                    TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                    new TridentEnhanceComponent(0, 0, false, false, false));
            if (comp.is_soulbound()) {
                multiplier = ModConfig.get().trident.soulThrownBonus;
            } else if (comp.is_synchronized()) {
                multiplier = ModConfig.get().trident.syncThrownBonus;
            } else if (comp.is_adept()) {
                multiplier = ModConfig.get().trident.adeptThrownBonus;
            }
        }

        if (multiplier != 1.0) {
            double originalDamage = ((ArrowDamageAccessor) arrow).testmod$getBaseDamage();
            ((ArrowDamageAccessor) arrow).testmod$setBaseDamage(originalDamage * multiplier);
        }
    }
}

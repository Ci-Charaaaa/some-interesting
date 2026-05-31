package com.chara.some_interesting.mixin;

import com.chara.some_interesting.EventCallBack.RangedEvent;
import com.chara.some_interesting.component.BowEnhanceComponent;
import com.chara.some_interesting.component.CrossbowEnhanceComponent;
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

/**
 * 弓/弩熟练度 Mixin
 * 切入点：Projectile.applyOnProjectileSpawned(ServerLevel, ItemStack)
 * - 这是箭矢/弹射物生成到世界后的统一回调
 * - 此时箭已离弦，蓄力动画已完成，修改物品栈不会造成抖动
 * - 在此处完成计数、升级判定、伤害倍率修改三项工作
 */
@Mixin(Projectile.class)
public class ArrowDamageMixin {

    @Inject(
            at = @At("TAIL"),
            method = "applyOnProjectileSpawned"
    )
    private void SomeInteresting$onProjectileSpawned(ServerLevel level, ItemStack stack, CallbackInfo ci) {
        //只处理箭矢类弹射物
        if (!((Projectile)(Object)this instanceof AbstractArrow arrow)) {
            return;
        }

        //获取射出者
        Entity owner = arrow.getOwner();
        if (!(owner instanceof Player player)) {
            return;
        }

        //主手武器
        ItemStack weapon = player.getMainHandItem();
        double multiplier = 1.0;

        //=================== 弓 ===================
        if (weapon.is(Items.BOW)) {
            BowEnhanceComponent comp = weapon.getOrDefault(
                    BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                    new BowEnhanceComponent(0, false, false, false));
            int normal_count = comp.normal_count();
            boolean is_adept = comp.is_adept();
            boolean is_synchronized = comp.is_synchronized();
            boolean is_soulbound = comp.is_soulbound();
            int max_damage = weapon.getOrDefault(DataComponents.MAX_DAMAGE, 0);

            //计数 +1
            weapon.set(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                    new BowEnhanceComponent(++normal_count, is_adept, is_synchronized, is_soulbound));

            //升级判定
            RangedEvent.processBowUpgrade(player, weapon, normal_count, is_adept, is_synchronized, is_soulbound, max_damage);

            //重新读取以获取升级后的阶段
            BowEnhanceComponent updated = weapon.getOrDefault(
                    BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT,
                    new BowEnhanceComponent(0, false, false, false));
            if (updated.is_soulbound()) {
                multiplier = 1.8;
            } else if (updated.is_synchronized()) {
                multiplier = 1.5;
            } else if (updated.is_adept()) {
                multiplier = 1.2;
            }
        }

        //=================== 弩 ===================
        if (weapon.is(Items.CROSSBOW)) {
            CrossbowEnhanceComponent comp = weapon.getOrDefault(
                    CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT,
                    new CrossbowEnhanceComponent(0, false, false, false));
            if (comp.is_soulbound()) {
                multiplier = 1.8;
            } else if (comp.is_synchronized()) {
                multiplier = 1.5;
            } else if (comp.is_adept()) {
                multiplier = 1.2;
            }
        }

        //应用伤害倍率（通过 accessor 读取当前基础伤害后乘以倍率）
        if (multiplier != 1.0) {
            double originalDamage = ((ArrowDamageAccessor) arrow).SomeInteresting$getBaseDamage();
            ((ArrowDamageAccessor) arrow).SomeInteresting$setBaseDamage(originalDamage * multiplier);
        }
    }
}

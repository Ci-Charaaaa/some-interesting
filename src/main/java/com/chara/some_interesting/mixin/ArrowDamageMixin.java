package com.chara.some_interesting.mixin;

import com.chara.some_interesting.EventCallBack.RangedEvent;
import com.chara.some_interesting.component.BowEnhanceComponent;
import com.chara.some_interesting.component.CrossbowEnhanceComponent;
import com.chara.some_interesting.component.TridentEnhanceComponent;
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

/**
 * 弓/弩/三叉戟投掷熟练度 Mixin
 *
 * 切入点：Projectile.applyOnProjectileSpawned(ServerLevel, ItemStack)
 * 这是所有弹射物生成到世界后的统一回调（包括通过 Projectile.spawnProjectileFromRotation 创建的三叉戟）。
 * stack 参数是生成弹射物时消耗的物品栈（对三叉戟来说就是投掷出去的那把三叉戟）。
 *
 * 弓/弩用 player.getMainHandItem() 是因为它们不计入消耗，
 * 三叉戟在生存模式下主手已空，只能用 stack 参数。
 */
@Mixin(Projectile.class)
public class ArrowDamageMixin {

    @Inject(
            at = @At("TAIL"),
            method = "applyOnProjectileSpawned"
    )
    private void testmod$onProjectileSpawned(ServerLevel level, ItemStack stack, CallbackInfo ci) {
        //只处理箭矢类弹射物（包括 ThrownTrident）
        if (!((Projectile)(Object)this instanceof AbstractArrow arrow)) {
            return;
        }

        //获取射出者
        Entity owner = arrow.getOwner();
        if (!(owner instanceof Player player)) {
            return;
        }

        double multiplier = 1.0;

        //=================== 弓 ===================
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

        //=================== 三叉戟投掷（仅伤害倍率，计数由 UseItemCallback 处理）===================
        if (stack.is(Items.TRIDENT)) {
            TridentEnhanceComponent comp = stack.getOrDefault(
                    TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                    new TridentEnhanceComponent(0, 0, false, false, false));
            if (comp.is_soulbound()) {
                multiplier = 1.8;
            } else if (comp.is_synchronized()) {
                multiplier = 1.5;
            } else if (comp.is_adept()) {
                multiplier = 1.2;
            }
        }

        //应用伤害倍率
        if (multiplier != 1.0) {
            double originalDamage = ((ArrowDamageAccessor) arrow).testmod$getBaseDamage();
            ((ArrowDamageAccessor) arrow).testmod$setBaseDamage(originalDamage * multiplier);
        }
    }
}

package com.chara.test1.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 访问 AbstractArrow 的私有字段 baseDamage，
 * 用于读取当前基础伤害后再乘以熟练度倍率。
 */
@Mixin(AbstractArrow.class)
public interface ArrowDamageAccessor {

    @Accessor("baseDamage")
    double testmod$getBaseDamage();

    @Accessor("baseDamage")
    void testmod$setBaseDamage(double damage);
}

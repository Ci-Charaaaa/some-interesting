package com.chara.some_interesting.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface FishingHookAccessor {

    @Accessor("luck")
    @Mutable
    void setLuck(int luck);

    @Accessor("lureSpeed")
    @Mutable
    void setLureSpeed(int lureSpeed);
}

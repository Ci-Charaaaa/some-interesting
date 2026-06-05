package com.chara.some_interesting.mixin;

import com.chara.some_interesting.component.FishingRodEnhanceComponent;
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

/**
 * 钓鱼竿熟练度：
 * - 每次收竿计数 +1
 * - 升级后：钓鱼时长延长（lureSpeed 降低），宝藏概率提升（luck 增加）
 */
@Mixin(FishingHook.class)
public class FishingMixin {

    @Shadow
    private int nibble;
    @Shadow
    private int luck;

    // ==================== 构造时调整 lureSpeed（延长钓鱼等待时间） ====================
    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
    private void testmod$onConstruct(Player player, Level level, int luck, int lureSpeed, CallbackInfo ci) {
        net.minecraft.world.item.ItemStack rod = player.getMainHandItem();
        if (!rod.is(Items.FISHING_ROD)) return;

        FishingRodEnhanceComponent comp = rod.getOrDefault(
                FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                new FishingRodEnhanceComponent(0, false, false, false));

        // 根据熟练度降低 lureSpeed（越高等待越久）
        int penalty = 0;
        if (comp.is_soulbound()) penalty = 3;
        else if (comp.is_synchronized()) penalty = 2;
        else if (comp.is_adept()) penalty = 1;

        if (penalty > 0) {
            ((FishingHookAccessor) this).setLureSpeed(Math.max(0, lureSpeed - penalty));
        }
    }

    // ==================== 收竿时计数 + 升级 + 增加 luck（宝藏概率提升） ====================
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

        // 增加 luck 以提升宝藏概率
        int luckBonus = 0;
        if (so) luckBonus = 3;
        else if (sy) luckBonus = 2;
        else if (ad) luckBonus = 1;
        if (luckBonus > 0) {
            ((FishingHookAccessor) hook).setLuck(this.luck + luckBonus);
        }

        rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                new FishingRodEnhanceComponent(normal, ad, sy, so));

        //升级判定
        boolean na = !ad && normal >= 60;
        boolean ns = !sy && normal >= 180;
        boolean nl = !so && normal >= 500;
        if (!na && !ns && !nl) return;

        //播放升级音效和文本
        if (!(hook.level() instanceof ServerLevel serverLevel)) return;
        out_sound(serverLevel, player);
        String name = get_name(rod);

        if (nl) {
            rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                    new FishingRodEnhanceComponent(normal, true, true, true));
            rod.set(DataComponents.MAX_DAMAGE, (int)(md * 1.8));
            rod.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "fishing", "soulbound", name, "max_level", (int)(md * 1.8));
        } else if (ns) {
            rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                    new FishingRodEnhanceComponent(normal, ad, true, so));
            rod.set(DataComponents.MAX_DAMAGE, (int)(md * 1.5));
            rod.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "fishing", "synchronized", name, (int)(md * 1.5));
        } else {
            rod.set(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT,
                    new FishingRodEnhanceComponent(normal, true, sy, so));
            rod.set(DataComponents.MAX_DAMAGE, (int)(md * 1.2));
            rod.set(DataComponents.REPAIR_COST, 0);
            upgrade_text(player, "fishing", "adept", name, (int)(md * 1.2));
        }
    }
}

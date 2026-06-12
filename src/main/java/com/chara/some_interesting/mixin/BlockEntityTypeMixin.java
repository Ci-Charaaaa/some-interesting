package com.chara.some_interesting.mixin;

import com.chara.some_interesting.ModBlockEntities.UpgradeForgeTableEntity;
import com.chara.some_interesting.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Set;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerModTypes(CallbackInfo ci) {
        try {
            Class<?> supplierCls = Class.forName(
                    "net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object supplier = Proxy.newProxyInstance(
                    supplierCls.getClassLoader(),
                    new Class<?>[]{supplierCls},
                    (p, m, a) -> new UpgradeForgeTableEntity((BlockPos) a[0], (BlockState) a[1]));

            // 直接调私有构造器，跳过 register() → 无 datafixer 调用
            Constructor ctor = BlockEntityType.class.getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            BlockEntityType type = (BlockEntityType) ctor.newInstance(supplier, Set.of(ModBlocks.UPGRADE_FORGE_TABLE));

            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath("some-interesting", "upgrade_forge_table"),
                    type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

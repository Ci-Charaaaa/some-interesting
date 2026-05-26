package com.chara.test1.component;

import com.chara.test1.TestMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record PickaxeEnhanceComponent(int normal_excavate_count,int rare_excavate_count,boolean is_adept,boolean is_synchronized,boolean is_soulbound) implements TooltipProvider {

    //给启动类调用的初始化方法
    public static void initialize(){
        //注册数据组件
        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE,PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        if (is_soulbound) {
            tooltip.accept(Component.translatable("item.test-mod.soulbound").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.test-mod.normal_excavate_count.info", this.normal_excavate_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.rare_excavate_count.info", this.rare_excavate_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_synchronized) {
            tooltip.accept(Component.translatable("item.test-mod.synchronized").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.test-mod.normal_excavate_count.info", this.normal_excavate_count, 500).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.rare_excavate_count.info", this.rare_excavate_count, 15).withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_adept) {
            tooltip.accept(Component.translatable("item.test-mod.adept").withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.test-mod.normal_excavate_count.info", this.normal_excavate_count, 180).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.rare_excavate_count.info", this.rare_excavate_count, 3).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.translatable("item.test-mod.familiarizing").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.normal_excavate_count.info", this.normal_excavate_count, 60).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.rare_excavate_count.info", this.rare_excavate_count, 0).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    //定义匹配的codec
    public static final Codec<PickaxeEnhanceComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("normal_excavate_count",0).forGetter(PickaxeEnhanceComponent::normal_excavate_count),
                Codec.INT.optionalFieldOf("rare_excavate_count", 0).forGetter(PickaxeEnhanceComponent::rare_excavate_count),
                Codec.BOOL.optionalFieldOf("adept", false).forGetter(PickaxeEnhanceComponent::is_adept),
                Codec.BOOL.optionalFieldOf("synchronized", false).forGetter(PickaxeEnhanceComponent::is_synchronized),
                Codec.BOOL.optionalFieldOf("soulbound", false).forGetter(PickaxeEnhanceComponent::is_soulbound)
        ).apply(builder, PickaxeEnhanceComponent::new);
    });

    //自定义一个储存自定义类型数据的数据组件
    public static final DataComponentType<PickaxeEnhanceComponent> PICKAXE_PROFICIENCY_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(TestMod.MOD_ID, "pickaxe_proficiency_component"),
            DataComponentType.<PickaxeEnhanceComponent>builder().persistent(PickaxeEnhanceComponent.CODEC).build()
    );
}

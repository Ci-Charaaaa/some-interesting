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

public record SwordsEnhanceComponent(int normal_count, int super_count, boolean is_synchronized, boolean is_soulbound) implements TooltipProvider {

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        if (is_soulbound) {
            tooltip.accept(Component.translatable("item.test-mod.soulbound").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.test-mod.normal_count.info", this.normal_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.super_count.info", this.super_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_synchronized) {
            tooltip.accept(Component.translatable("item.test-mod.synchronized").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.test-mod.normal_count.info", this.normal_count, 300).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.super_count.info", this.super_count, 60).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.translatable("item.test-mod.familiarizing").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.normal_count.info", this.normal_count, 30).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.super_count.info", this.super_count, 6).withStyle(ChatFormatting.DARK_GRAY));
        }
    }


    //给启动类调用的初始化方法
    public static void initialize(){
        //注册这个数据组件
        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT);
    }

    //声明一个自定义的Codec实例（Codec：一个用于转化字段的类）
    public static final Codec<SwordsEnhanceComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("normal_count",0).forGetter(SwordsEnhanceComponent::normal_count),
                Codec.INT.optionalFieldOf("super_count", 0).forGetter(SwordsEnhanceComponent::super_count),
                Codec.BOOL.optionalFieldOf("synchronized", false).forGetter(SwordsEnhanceComponent::is_synchronized),
                Codec.BOOL.optionalFieldOf("soulbound", false).forGetter(SwordsEnhanceComponent::is_soulbound)
        ).apply(builder, SwordsEnhanceComponent::new);
    });

    //自定义一个储存自定义类型数据的数据组件
    public static final DataComponentType<SwordsEnhanceComponent> SWORDS_PROFICIENCY_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(TestMod.MOD_ID, "swords_proficiency_component"),
            DataComponentType.<SwordsEnhanceComponent>builder().persistent(SwordsEnhanceComponent.CODEC).build()
    );


}

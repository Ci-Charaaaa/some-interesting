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

public record AxeEnhanceComponent(int normal_count, int super_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound) implements TooltipProvider {

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        if (is_soulbound) {
            tooltip.accept(Component.translatable("item.test-mod.soulbound").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.test-mod.axe_normal_count.info", this.normal_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.axe_super_count.info", this.super_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_synchronized) {
            tooltip.accept(Component.translatable("item.test-mod.synchronized").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.test-mod.axe_normal_count.info", this.normal_count, 400).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.axe_super_count.info", this.super_count, 40).withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_adept) {
            tooltip.accept(Component.translatable("item.test-mod.adept").withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.test-mod.axe_normal_count.info", this.normal_count, 120).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.axe_super_count.info", this.super_count, 12).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.translatable("item.test-mod.familiarizing").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.axe_normal_count.info", this.normal_count, 30).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.test-mod.axe_super_count.info", this.super_count, 3).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void initialize(){
        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT);
    }

    public static final Codec<AxeEnhanceComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("normal_count",0).forGetter(AxeEnhanceComponent::normal_count),
                Codec.INT.optionalFieldOf("super_count", 0).forGetter(AxeEnhanceComponent::super_count),
                Codec.BOOL.optionalFieldOf("adept", false).forGetter(AxeEnhanceComponent::is_adept),
                Codec.BOOL.optionalFieldOf("synchronized", false).forGetter(AxeEnhanceComponent::is_synchronized),
                Codec.BOOL.optionalFieldOf("soulbound", false).forGetter(AxeEnhanceComponent::is_soulbound)
        ).apply(builder, AxeEnhanceComponent::new);
    });

    public static final DataComponentType<AxeEnhanceComponent> AXE_PROFICIENCY_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(TestMod.MOD_ID, "axe_proficiency_component"),
            DataComponentType.<AxeEnhanceComponent>builder().persistent(AxeEnhanceComponent.CODEC).build()
    );
}

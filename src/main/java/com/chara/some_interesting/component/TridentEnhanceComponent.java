package com.chara.some_interesting.component;

import com.chara.some_interesting.SomeInteresting;
import com.chara.some_interesting.config.ModConfig;
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

public record TridentEnhanceComponent(int normal_count, int super_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound) implements TooltipProvider {

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        var cfg = ModConfig.get().trident;
        if (is_soulbound) {
            tooltip.accept(Component.translatable("item.some-interesting.soulbound").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.some-interesting.trident_normal_count.info", this.normal_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.trident_super_count.info", this.super_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_synchronized) {
            tooltip.accept(Component.translatable("item.some-interesting.synchronized").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.some-interesting.trident_normal_count.info", this.normal_count, cfg.soulNormal).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.trident_super_count.info", this.super_count, cfg.soulSuper).withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_adept) {
            tooltip.accept(Component.translatable("item.some-interesting.adept").withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.some-interesting.trident_normal_count.info", this.normal_count, cfg.syncNormal).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.trident_super_count.info", this.super_count, cfg.syncSuper).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.translatable("item.some-interesting.familiarizing").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.trident_normal_count.info", this.normal_count, cfg.adeptNormal).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.trident_super_count.info", this.super_count, cfg.adeptSuper).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void initialize(){
        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT);
    }

    public static final Codec<TridentEnhanceComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("normal_count",0).forGetter(TridentEnhanceComponent::normal_count),
                Codec.INT.optionalFieldOf("super_count", 0).forGetter(TridentEnhanceComponent::super_count),
                Codec.BOOL.optionalFieldOf("adept", false).forGetter(TridentEnhanceComponent::is_adept),
                Codec.BOOL.optionalFieldOf("synchronized", false).forGetter(TridentEnhanceComponent::is_synchronized),
                Codec.BOOL.optionalFieldOf("soulbound", false).forGetter(TridentEnhanceComponent::is_soulbound)
        ).apply(builder, TridentEnhanceComponent::new);
    });

    public static final DataComponentType<TridentEnhanceComponent> TRIDENT_PROFICIENCY_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "trident_proficiency_component"),
            DataComponentType.<TridentEnhanceComponent>builder().persistent(TridentEnhanceComponent.CODEC).build()
    );
}

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

public record ShearsEnhanceComponent(int normal_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound) implements TooltipProvider {

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        var cfg = ModConfig.get().shears;
        if (is_soulbound) {
            tooltip.accept(Component.translatable("item.some-interesting.soulbound").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.some-interesting.shears_normal_count.info", this.normal_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_synchronized) {
            tooltip.accept(Component.translatable("item.some-interesting.synchronized").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.some-interesting.shears_normal_count.info", this.normal_count, cfg.soulThreshold).withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_adept) {
            tooltip.accept(Component.translatable("item.some-interesting.adept").withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.some-interesting.shears_normal_count.info", this.normal_count, cfg.syncThreshold).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.translatable("item.some-interesting.familiarizing").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.shears_normal_count.info", this.normal_count, cfg.adeptThreshold).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void initialize(){
        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, ShearsEnhanceComponent.SHEARS_PROFICIENCY_COMPONENT);
    }

    public static final Codec<ShearsEnhanceComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("normal_count",0).forGetter(ShearsEnhanceComponent::normal_count),
                Codec.BOOL.optionalFieldOf("adept", false).forGetter(ShearsEnhanceComponent::is_adept),
                Codec.BOOL.optionalFieldOf("synchronized", false).forGetter(ShearsEnhanceComponent::is_synchronized),
                Codec.BOOL.optionalFieldOf("soulbound", false).forGetter(ShearsEnhanceComponent::is_soulbound)
        ).apply(builder, ShearsEnhanceComponent::new);
    });

    public static final DataComponentType<ShearsEnhanceComponent> SHEARS_PROFICIENCY_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "shears_proficiency_component"),
            DataComponentType.<ShearsEnhanceComponent>builder().persistent(ShearsEnhanceComponent.CODEC).build()
    );
}

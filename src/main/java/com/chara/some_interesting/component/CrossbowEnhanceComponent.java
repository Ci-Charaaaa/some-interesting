package com.chara.some_interesting.component;

import com.chara.some_interesting.SomeInteresting;
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

public record CrossbowEnhanceComponent(int normal_count, boolean is_adept, boolean is_synchronized, boolean is_soulbound) implements TooltipProvider {

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        if (is_soulbound) {
            tooltip.accept(Component.translatable("item.some-interesting.soulbound").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.some-interesting.crossbow_normal_count.info", this.normal_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.crossbow.damage_bonus", "+80%").withStyle(ChatFormatting.GREEN));
        } else if (is_synchronized) {
            tooltip.accept(Component.translatable("item.some-interesting.synchronized").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.some-interesting.crossbow_normal_count.info", this.normal_count, 540).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.crossbow.damage_bonus", "+50%").withStyle(ChatFormatting.GREEN));
        } else if (is_adept) {
            tooltip.accept(Component.translatable("item.some-interesting.adept").withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.some-interesting.crossbow_normal_count.info", this.normal_count, 180).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.crossbow.damage_bonus", "+20%").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.accept(Component.translatable("item.some-interesting.familiarizing").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.crossbow_normal_count.info", this.normal_count, 60).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void initialize(){
        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT);
    }

    public static final Codec<CrossbowEnhanceComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("normal_count",0).forGetter(CrossbowEnhanceComponent::normal_count),
                Codec.BOOL.optionalFieldOf("adept", false).forGetter(CrossbowEnhanceComponent::is_adept),
                Codec.BOOL.optionalFieldOf("synchronized", false).forGetter(CrossbowEnhanceComponent::is_synchronized),
                Codec.BOOL.optionalFieldOf("soulbound", false).forGetter(CrossbowEnhanceComponent::is_soulbound)
        ).apply(builder, CrossbowEnhanceComponent::new);
    });

    public static final DataComponentType<CrossbowEnhanceComponent> CROSSBOW_PROFICIENCY_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "crossbow_proficiency_component"),
            DataComponentType.<CrossbowEnhanceComponent>builder().persistent(CrossbowEnhanceComponent.CODEC).build()
    );
}

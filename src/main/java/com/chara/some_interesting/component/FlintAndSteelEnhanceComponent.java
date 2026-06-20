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

public record FlintAndSteelEnhanceComponent(int normal_count, boolean has_lit_portal, boolean is_adept, boolean is_synchronized, boolean is_soulbound) implements TooltipProvider {

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        var cfg = ModConfig.get().flint;
        String missionStatus = "§7" + (has_lit_portal ? "1" : "0") + "§8/§71";
        if (is_soulbound) {
            tooltip.accept(Component.translatable("item.some-interesting.soulbound").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.some-interesting.fs_normal_count.info", this.normal_count, "MAX").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.fs.mission", missionStatus).withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_synchronized) {
            tooltip.accept(Component.translatable("item.some-interesting.synchronized").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.some-interesting.fs_normal_count.info", this.normal_count, cfg.soulThreshold).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.fs.mission", missionStatus).withStyle(ChatFormatting.DARK_GRAY));
        } else if (is_adept) {
            tooltip.accept(Component.translatable("item.some-interesting.adept").withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.some-interesting.fs_normal_count.info", this.normal_count, cfg.syncThreshold).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.fs.mission", missionStatus).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.translatable("item.some-interesting.familiarizing").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.fs_normal_count.info", this.normal_count, cfg.adeptThreshold).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable("item.some-interesting.fs.mission", missionStatus).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void initialize(){
        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT);
    }

    public static final Codec<FlintAndSteelEnhanceComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("normal_count",0).forGetter(FlintAndSteelEnhanceComponent::normal_count),
                Codec.BOOL.optionalFieldOf("has_lit_portal", false).forGetter(FlintAndSteelEnhanceComponent::has_lit_portal),
                Codec.BOOL.optionalFieldOf("adept", false).forGetter(FlintAndSteelEnhanceComponent::is_adept),
                Codec.BOOL.optionalFieldOf("synchronized", false).forGetter(FlintAndSteelEnhanceComponent::is_synchronized),
                Codec.BOOL.optionalFieldOf("soulbound", false).forGetter(FlintAndSteelEnhanceComponent::is_soulbound)
        ).apply(builder, FlintAndSteelEnhanceComponent::new);
    });

    public static final DataComponentType<FlintAndSteelEnhanceComponent> FS_PROFICIENCY_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "fs_proficiency_component"),
            DataComponentType.<FlintAndSteelEnhanceComponent>builder().persistent(FlintAndSteelEnhanceComponent.CODEC).build()
    );
}

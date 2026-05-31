package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.ArmorEnhanceComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

public class ArmorEvent {

    private static final Map<UUID, Float> lastHealth = new HashMap<>();
    private static final Identifier ARMOR_PRO = Identifier.fromNamespaceAndPath("some-interesting", "armor_protection");
    private static final Identifier ARMOR_TGH = Identifier.fromNamespaceAndPath("some-interesting", "armor_toughness");
    private static final int TH_ADEPT = 200;
    private static final int TH_SYNC  = 800;
    private static final int TH_SOUL  = 2000;

    public static void initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                lastHealth.remove(handler.getPlayer().getUUID()));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                float current = player.getHealth();
                Float prev = lastHealth.get(player.getUUID());
                if (prev != null && current < prev) {
                    hurt(player);
                }
                lastHealth.put(player.getUUID(), current);
            }
        });
    }

    private static void hurt(ServerPlayer player) {
        Level world = player.level();
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : armorSlots) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isEmpty()) continue;

            ArmorEnhanceComponent comp = armor.getOrDefault(
                    ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                    new ArmorEnhanceComponent(0, false, false, false));

            int dt = comp.damage_taken() + 1;
            boolean ad = comp.is_adept();
            boolean sy = comp.is_synchronized();
            boolean so = comp.is_soulbound();
            int baseMaxDura = armor.getOrDefault(DataComponents.MAX_DAMAGE, 0);

            armor.set(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                    new ArmorEnhanceComponent(dt, ad, sy, so));

            boolean needAdept = !ad && dt >= TH_ADEPT;
            boolean needSync  = !sy && dt >= TH_SYNC;
            boolean needSoul  = !so && dt >= TH_SOUL;

            if (!needAdept && !needSync && !needSoul) continue;

            out_sound(world, player);
            String name = get_name(armor);

            if (needSoul) {
                armor.set(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                        new ArmorEnhanceComponent(dt, true, true, true));
                armor.set(DataComponents.MAX_DAMAGE, (int)(baseMaxDura * 1.8));
                armor.set(DataComponents.REPAIR_COST, 0);
                applyModifier(armor, ARMOR_PRO, 4, false);
                applyModifier(armor, ARMOR_TGH, 2, true);
                upgrade_text(player, "armor", "soulbound", name, "max_level", (int)(baseMaxDura * 1.8));
            } else if (needSync) {
                armor.set(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                        new ArmorEnhanceComponent(dt, true, true, false));
                armor.set(DataComponents.MAX_DAMAGE, (int)(baseMaxDura * 1.5));
                armor.set(DataComponents.REPAIR_COST, 0);
                applyModifier(armor, ARMOR_PRO, 2, false);
                applyModifier(armor, ARMOR_TGH, 1, true);
                upgrade_text(player, "armor", "synchronized", name, (int)(baseMaxDura * 1.5));
            } else if (needAdept) {
                armor.set(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                        new ArmorEnhanceComponent(dt, true, false, false));
                armor.set(DataComponents.MAX_DAMAGE, (int)(baseMaxDura * 1.2));
                armor.set(DataComponents.REPAIR_COST, 0);
                applyModifier(armor, ARMOR_PRO, 1, false);
                upgrade_text(player, "armor", "adept", name, (int)(baseMaxDura * 1.2));
            }
        }
    }

    private static void applyModifier(ItemStack armor, Identifier id, int value, boolean isTough) {
        ItemAttributeModifiers current = armor.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            if (!entry.modifier().id().equals(id)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        builder.add(isTough ? Attributes.ARMOR_TOUGHNESS : Attributes.ARMOR,
                new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.ARMOR);

        armor.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
}

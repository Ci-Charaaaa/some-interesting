package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.ArmorEnhanceComponent;
import com.chara.some_interesting.component.ShieldEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

public class ArmorEvent {

    private static final Map<UUID, Float> lastHealth = new HashMap<>();
    private static final Map<UUID, Integer> shieldDura = new HashMap<>();
    private static final Identifier ARMOR_PRO = Identifier.fromNamespaceAndPath("some-interesting", "armor_protection");
    private static final Identifier ARMOR_TGH = Identifier.fromNamespaceAndPath("some-interesting", "armor_toughness");
    private static final Identifier SHIELD_KNOCK = Identifier.fromNamespaceAndPath("some-interesting", "shield_knockback");

    public static void initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uid = handler.getPlayer().getUUID();
            lastHealth.remove(uid);
            shieldDura.remove(uid);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uid = player.getUUID();

                float current = player.getHealth();
                Float prev = lastHealth.get(uid);
                if (prev != null && current < prev) {
                    armorUpgrade(player, player.level());
                }
                lastHealth.put(uid, current);

                ItemStack shield = player.isBlocking() && player.getOffhandItem().is(Items.SHIELD)
                        ? player.getOffhandItem()
                        : player.isBlocking() && player.getMainHandItem().is(Items.SHIELD)
                        ? player.getMainHandItem() : null;
                if (shield != null) {
                    int curDura = shield.getOrDefault(DataComponents.DAMAGE, 0);
                    Integer prevDura = shieldDura.get(uid);
                    if (prevDura != null && curDura > prevDura) {
                        shieldUpgrade(player, player.level(), shield);
                    }
                    shieldDura.put(uid, curDura);
                } else {
                    shieldDura.remove(uid);
                }
            }
        });
    }

    private static void shieldUpgrade(ServerPlayer player, Level world, ItemStack shield) {
        var cfg = ModConfig.get().shield;
        ShieldEnhanceComponent comp = shield.getOrDefault(
                ShieldEnhanceComponent.SHIELD_PROFICIENCY_COMPONENT,
                new ShieldEnhanceComponent(0, false, false, false));
        int dt = comp.normal_count() + 1;
        boolean ad = comp.is_adept();
        boolean sy = comp.is_synchronized();
        boolean so = comp.is_soulbound();
        int md = shield.getOrDefault(DataComponents.MAX_DAMAGE, 0);

        shield.set(ShieldEnhanceComponent.SHIELD_PROFICIENCY_COMPONENT,
                new ShieldEnhanceComponent(dt, ad, sy, so));

        boolean na = !ad && dt >= cfg.adeptThreshold;
        boolean ns = !sy && dt >= cfg.syncThreshold;
        boolean nl = !so && dt >= cfg.soulThreshold;
        if (!na && !ns && !nl) return;

        out_sound(world, player);
        String name = get_name(shield);

        if (nl) {
            shield.set(ShieldEnhanceComponent.SHIELD_PROFICIENCY_COMPONENT,
                    new ShieldEnhanceComponent(dt, true, true, true));
            shield.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.soulDurability));
            shield.set(DataComponents.REPAIR_COST, 0);
            applyShieldModifier(shield, SHIELD_KNOCK, cfg.soulKnockbackResist);
            upgrade_text(player, "shield", "soulbound", name, "max_level", (int)(md * cfg.soulDurability), cfg.soulKnockbackResist);
        } else if (ns) {
            shield.set(ShieldEnhanceComponent.SHIELD_PROFICIENCY_COMPONENT,
                    new ShieldEnhanceComponent(dt, true, true, false));
            shield.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.syncDurability));
            shield.set(DataComponents.REPAIR_COST, 0);
            applyShieldModifier(shield, SHIELD_KNOCK, cfg.syncKnockbackResist);
            upgrade_text(player, "shield", "synchronized", name, (int)(md * cfg.syncDurability), cfg.syncKnockbackResist);
        } else if (na) {
            shield.set(ShieldEnhanceComponent.SHIELD_PROFICIENCY_COMPONENT,
                    new ShieldEnhanceComponent(dt, true, false, false));
            shield.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.adeptDurability));
            shield.set(DataComponents.REPAIR_COST, 0);
            applyShieldModifier(shield, SHIELD_KNOCK, cfg.adeptKnockbackResist);
            upgrade_text(player, "shield", "adept", name, (int)(md * cfg.adeptDurability), cfg.adeptKnockbackResist);
        }
    }

    private static void applyShieldModifier(ItemStack shield, Identifier id, double knockback) {
        ItemAttributeModifiers current = shield.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            if (!entry.modifier().id().equals(id)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        builder.add(Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(id, knockback, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.ANY);
        shield.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void armorUpgrade(ServerPlayer player, Level world) {
        var cfg = ModConfig.get().armor;
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

            boolean needAdept = !ad && dt >= cfg.adeptThreshold;
            boolean needSync  = !sy && dt >= cfg.syncThreshold;
            boolean needSoul  = !so && dt >= cfg.soulThreshold;

            if (!needAdept && !needSync && !needSoul) continue;

            out_sound(world, player);
            String name = get_name(armor);

            if (needSoul) {
                armor.set(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                        new ArmorEnhanceComponent(dt, true, true, true));
                armor.set(DataComponents.MAX_DAMAGE, (int)(baseMaxDura * cfg.soulDurability));
                armor.set(DataComponents.REPAIR_COST, 0);
                applyArmorModifiers(armor, cfg.soulArmorBonus, cfg.soulToughnessBonus);
                upgrade_text(player, "armor", "soulbound", name, "max_level", (int)(baseMaxDura * cfg.soulDurability), cfg.soulArmorBonus * 100 + "%");
            } else if (needSync) {
                armor.set(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                        new ArmorEnhanceComponent(dt, true, true, false));
                armor.set(DataComponents.MAX_DAMAGE, (int)(baseMaxDura * cfg.syncDurability));
                armor.set(DataComponents.REPAIR_COST, 0);
                applyArmorModifiers(armor, cfg.syncArmorBonus, cfg.syncToughnessBonus);
                upgrade_text(player, "armor", "synchronized", name, (int)(baseMaxDura * cfg.syncDurability), cfg.syncArmorBonus * 100 + "%");
            } else if (needAdept) {
                armor.set(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT,
                        new ArmorEnhanceComponent(dt, true, false, false));
                armor.set(DataComponents.MAX_DAMAGE, (int)(baseMaxDura * cfg.adeptDurability));
                armor.set(DataComponents.REPAIR_COST, 0);
                applyArmorModifiers(armor, cfg.adeptArmorBonus, cfg.adeptToughnessBonus);
                upgrade_text(player, "armor", "adept", name, (int)(baseMaxDura * cfg.adeptDurability), cfg.adeptArmorBonus * 100 + "%");
            }
        }
    }

    private static void applyArmorModifiers(ItemStack armor, double armorBonus, double toughnessBonus) {
        ItemAttributeModifiers current = armor.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            if (!entry.modifier().id().equals(ARMOR_PRO) && !entry.modifier().id().equals(ARMOR_TGH)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        builder.add(Attributes.ARMOR,
                new AttributeModifier(ARMOR_PRO, armorBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.ARMOR);
        builder.add(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(ARMOR_TGH, toughnessBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.ARMOR);

        armor.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
}

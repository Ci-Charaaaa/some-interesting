package com.chara.some_interesting.client;

import com.chara.some_interesting.config.ModConfig;
import com.chara.some_interesting.config.ModConfigLoader;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {

    private static Component t(String key) {
        return Component.translatable("config.some-interesting." + key);
    }

    public static Screen create(Screen parent) {
        ModConfig cfg = ModConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(t("title"))
                .setSavingRunnable(ModConfigLoader::save);
        ConfigEntryBuilder e = builder.entryBuilder();

        swordCategory(builder, e, cfg.sword);
        maceCategory(builder, e, cfg.mace);
        axeCategory(builder, e, cfg.axe);
        tridentCategory(builder, e, cfg.trident);
        pickaxeCategory(builder, e, cfg.pickaxe);
        singleThresholdCategory(builder, e, t("shovel"), cfg.shovel);
        hoeCategory(builder, e, cfg.hoe);
        singleBowCategory(builder, e, t("bow"), cfg.bow);
        crossbowCategory(builder, e, cfg.crossbow);
        armorCategory(builder, e, cfg.armor);
        shieldCategory(builder, e, cfg.shield);
        elytraCategory(builder, e, cfg.elytra);
        fishingCategory(builder, e, cfg.fishing);
        durabilityOnlyCategory(builder, e, t("shears"), cfg.shears);
        flintCategory(builder, e, cfg.flint);
        spearCategory(builder, e, cfg.spear);

        return builder.build();
    }

    private static void swordCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.SwordConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("sword"));
        cat.addEntry(e.startIntField(t("adept_normal"), c.adeptNormal).setDefaultValue(30).setSaveConsumer(v -> c.adeptNormal = v).build());
        cat.addEntry(e.startIntField(t("adept_super"), c.adeptSuper).setDefaultValue(6).setSaveConsumer(v -> c.adeptSuper = v).build());
        cat.addEntry(e.startIntField(t("sync_normal"), c.syncNormal).setDefaultValue(180).setSaveConsumer(v -> c.syncNormal = v).build());
        cat.addEntry(e.startIntField(t("sync_super"), c.syncSuper).setDefaultValue(20).setSaveConsumer(v -> c.syncSuper = v).build());
        cat.addEntry(e.startIntField(t("soul_normal"), c.soulNormal).setDefaultValue(500).setSaveConsumer(v -> c.soulNormal = v).build());
        cat.addEntry(e.startIntField(t("soul_super"), c.soulSuper).setDefaultValue(80).setSaveConsumer(v -> c.soulSuper = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_damage"), c.adeptDamageBonus).setDefaultValue(0.2).setSaveConsumer(v -> c.adeptDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_damage"), c.syncDamageBonus).setDefaultValue(0.5).setSaveConsumer(v -> c.syncDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_damage"), c.soulDamageBonus).setDefaultValue(0.8).setSaveConsumer(v -> c.soulDamageBonus = v).build());
    }

    private static void maceCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.MaceConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("mace"));
        cat.addEntry(e.startIntField(t("adept_normal"), c.adeptNormal).setDefaultValue(18).setSaveConsumer(v -> c.adeptNormal = v).build());
        cat.addEntry(e.startIntField(t("adept_super"), c.adeptSuper).setDefaultValue(6).setSaveConsumer(v -> c.adeptSuper = v).build());
        cat.addEntry(e.startIntField(t("sync_normal"), c.syncNormal).setDefaultValue(36).setSaveConsumer(v -> c.syncNormal = v).build());
        cat.addEntry(e.startIntField(t("sync_super"), c.syncSuper).setDefaultValue(30).setSaveConsumer(v -> c.syncSuper = v).build());
        cat.addEntry(e.startIntField(t("soul_normal"), c.soulNormal).setDefaultValue(72).setSaveConsumer(v -> c.soulNormal = v).build());
        cat.addEntry(e.startIntField(t("soul_super"), c.soulSuper).setDefaultValue(90).setSaveConsumer(v -> c.soulSuper = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_damage"), c.adeptDamageBonus).setDefaultValue(0.2).setSaveConsumer(v -> c.adeptDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_damage"), c.syncDamageBonus).setDefaultValue(0.5).setSaveConsumer(v -> c.syncDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_damage"), c.soulDamageBonus).setDefaultValue(0.8).setSaveConsumer(v -> c.soulDamageBonus = v).build());
    }

    private static void axeCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.AxeConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("axe"));
        cat.addEntry(e.startIntField(t("adept_normal"), c.adeptNormal).setDefaultValue(30).setSaveConsumer(v -> c.adeptNormal = v).build());
        cat.addEntry(e.startIntField(t("adept_super"), c.adeptSuper).setDefaultValue(3).setSaveConsumer(v -> c.adeptSuper = v).build());
        cat.addEntry(e.startIntField(t("sync_normal"), c.syncNormal).setDefaultValue(120).setSaveConsumer(v -> c.syncNormal = v).build());
        cat.addEntry(e.startIntField(t("sync_super"), c.syncSuper).setDefaultValue(12).setSaveConsumer(v -> c.syncSuper = v).build());
        cat.addEntry(e.startIntField(t("soul_normal"), c.soulNormal).setDefaultValue(400).setSaveConsumer(v -> c.soulNormal = v).build());
        cat.addEntry(e.startIntField(t("soul_super"), c.soulSuper).setDefaultValue(40).setSaveConsumer(v -> c.soulSuper = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_damage"), c.adeptDamageBonus).setDefaultValue(0.1).setSaveConsumer(v -> c.adeptDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_damage"), c.syncDamageBonus).setDefaultValue(0.25).setSaveConsumer(v -> c.syncDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_damage"), c.soulDamageBonus).setDefaultValue(0.4).setSaveConsumer(v -> c.soulDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("adept_mining"), c.adeptMiningBonus).setDefaultValue(1.0).setSaveConsumer(v -> c.adeptMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_mining"), c.syncMiningBonus).setDefaultValue(2.0).setSaveConsumer(v -> c.syncMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_mining"), c.soulMiningBonus).setDefaultValue(4.0).setSaveConsumer(v -> c.soulMiningBonus = v).build());
    }

    private static void tridentCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.TridentConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("trident"));
        cat.addEntry(e.startIntField(t("adept_normal"), c.adeptNormal).setDefaultValue(30).setSaveConsumer(v -> c.adeptNormal = v).build());
        cat.addEntry(e.startIntField(t("adept_super"), c.adeptSuper).setDefaultValue(6).setSaveConsumer(v -> c.adeptSuper = v).build());
        cat.addEntry(e.startIntField(t("sync_normal"), c.syncNormal).setDefaultValue(150).setSaveConsumer(v -> c.syncNormal = v).build());
        cat.addEntry(e.startIntField(t("sync_super"), c.syncSuper).setDefaultValue(20).setSaveConsumer(v -> c.syncSuper = v).build());
        cat.addEntry(e.startIntField(t("soul_normal"), c.soulNormal).setDefaultValue(450).setSaveConsumer(v -> c.soulNormal = v).build());
        cat.addEntry(e.startIntField(t("soul_super"), c.soulSuper).setDefaultValue(80).setSaveConsumer(v -> c.soulSuper = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_damage"), c.adeptDamageBonus).setDefaultValue(0.2).setSaveConsumer(v -> c.adeptDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_damage"), c.syncDamageBonus).setDefaultValue(0.5).setSaveConsumer(v -> c.syncDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_damage"), c.soulDamageBonus).setDefaultValue(0.8).setSaveConsumer(v -> c.soulDamageBonus = v).build());
        cat.addEntry(e.startDoubleField(t("adept_thrown"), c.adeptThrownBonus).setDefaultValue(0.2).setSaveConsumer(v -> c.adeptThrownBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_thrown"), c.syncThrownBonus).setDefaultValue(0.5).setSaveConsumer(v -> c.syncThrownBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_thrown"), c.soulThrownBonus).setDefaultValue(0.8).setSaveConsumer(v -> c.soulThrownBonus = v).build());
    }

    private static void pickaxeCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.PickaxeConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("pickaxe"));
        cat.addEntry(e.startIntField(t("adept_normal"), c.adeptNormal).setDefaultValue(60).setSaveConsumer(v -> c.adeptNormal = v).build());
        cat.addEntry(e.startIntField(t("adept_rare"), c.adeptRare).setDefaultValue(0).setSaveConsumer(v -> c.adeptRare = v).build());
        cat.addEntry(e.startIntField(t("sync_normal"), c.syncNormal).setDefaultValue(500).setSaveConsumer(v -> c.syncNormal = v).build());
        cat.addEntry(e.startIntField(t("sync_rare"), c.syncRare).setDefaultValue(3).setSaveConsumer(v -> c.syncRare = v).build());
        cat.addEntry(e.startIntField(t("soul_normal"), c.soulNormal).setDefaultValue(1800).setSaveConsumer(v -> c.soulNormal = v).build());
        cat.addEntry(e.startIntField(t("soul_rare"), c.soulRare).setDefaultValue(18).setSaveConsumer(v -> c.soulRare = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_mining"), c.adeptMiningBonus).setDefaultValue(2.0).setSaveConsumer(v -> c.adeptMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_mining"), c.syncMiningBonus).setDefaultValue(4.0).setSaveConsumer(v -> c.syncMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_mining"), c.soulMiningBonus).setDefaultValue(8.0).setSaveConsumer(v -> c.soulMiningBonus = v).build());
    }

    private static void singleThresholdCategory(ConfigBuilder b, ConfigEntryBuilder e, Component name, ModConfig.ShovelConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(name);
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(300).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(1200).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_mining"), c.adeptMiningBonus).setDefaultValue(2.0).setSaveConsumer(v -> c.adeptMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_mining"), c.syncMiningBonus).setDefaultValue(4.0).setSaveConsumer(v -> c.syncMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_mining"), c.soulMiningBonus).setDefaultValue(8.0).setSaveConsumer(v -> c.soulMiningBonus = v).build());
    }

    private static void hoeCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.HoeConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("hoe"));
        cat.addEntry(e.startIntField(t("adept_mining_thresh"), c.adeptMining).setDefaultValue(60).setSaveConsumer(v -> c.adeptMining = v).build());
        cat.addEntry(e.startIntField(t("sync_mining_thresh"), c.syncMining).setDefaultValue(180).setSaveConsumer(v -> c.syncMining = v).build());
        cat.addEntry(e.startIntField(t("soul_mining_thresh"), c.soulMining).setDefaultValue(540).setSaveConsumer(v -> c.soulMining = v).build());
        cat.addEntry(e.startIntField(t("adept_tilling"), c.adeptTilling).setDefaultValue(60).setSaveConsumer(v -> c.adeptTilling = v).build());
        cat.addEntry(e.startIntField(t("sync_tilling"), c.syncTilling).setDefaultValue(180).setSaveConsumer(v -> c.syncTilling = v).build());
        cat.addEntry(e.startIntField(t("soul_tilling"), c.soulTilling).setDefaultValue(600).setSaveConsumer(v -> c.soulTilling = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_mining"), c.adeptMiningBonus).setDefaultValue(2.0).setSaveConsumer(v -> c.adeptMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_mining"), c.syncMiningBonus).setDefaultValue(4.0).setSaveConsumer(v -> c.syncMiningBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_mining"), c.soulMiningBonus).setDefaultValue(8.0).setSaveConsumer(v -> c.soulMiningBonus = v).build());
    }

    private static void singleBowCategory(ConfigBuilder b, ConfigEntryBuilder e, Component name, ModConfig.BowConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(name);
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(180).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(500).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_arrow"), c.adeptArrowBonus).setDefaultValue(1.2).setSaveConsumer(v -> c.adeptArrowBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_arrow"), c.syncArrowBonus).setDefaultValue(1.5).setSaveConsumer(v -> c.syncArrowBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_arrow"), c.soulArrowBonus).setDefaultValue(1.8).setSaveConsumer(v -> c.soulArrowBonus = v).build());
    }

    private static void armorCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.ArmorConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("armor"));
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(200).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(800).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(2000).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_armor"), c.adeptArmorBonus).setDefaultValue(0.2).setSaveConsumer(v -> c.adeptArmorBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_armor"), c.syncArmorBonus).setDefaultValue(0.4).setSaveConsumer(v -> c.syncArmorBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_armor"), c.soulArmorBonus).setDefaultValue(0.6).setSaveConsumer(v -> c.soulArmorBonus = v).build());
        cat.addEntry(e.startDoubleField(t("adept_toughness"), c.adeptToughnessBonus).setDefaultValue(0.0).setSaveConsumer(v -> c.adeptToughnessBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_toughness"), c.syncToughnessBonus).setDefaultValue(0.1).setSaveConsumer(v -> c.syncToughnessBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_toughness"), c.soulToughnessBonus).setDefaultValue(0.2).setSaveConsumer(v -> c.soulToughnessBonus = v).build());
    }

    private static void shieldCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.ShieldConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("shield"));
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(200).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(500).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_kb_resist"), c.adeptKnockbackResist).setDefaultValue(0.1).setSaveConsumer(v -> c.adeptKnockbackResist = v).build());
        cat.addEntry(e.startDoubleField(t("sync_kb_resist"), c.syncKnockbackResist).setDefaultValue(0.2).setSaveConsumer(v -> c.syncKnockbackResist = v).build());
        cat.addEntry(e.startDoubleField(t("soul_kb_resist"), c.soulKnockbackResist).setDefaultValue(0.4).setSaveConsumer(v -> c.soulKnockbackResist = v).build());
    }

    private static void elytraCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.ElytraConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("elytra"));
        cat.addEntry(e.startIntField(t("adept_threshold_sec"), c.adeptThreshold).setDefaultValue(120).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold_sec"), c.syncThreshold).setDefaultValue(480).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold_sec"), c.soulThreshold).setDefaultValue(960).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_speed"), c.adeptSpeedBonus).setDefaultValue(0.01).setSaveConsumer(v -> c.adeptSpeedBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_speed"), c.syncSpeedBonus).setDefaultValue(0.02).setSaveConsumer(v -> c.syncSpeedBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_speed"), c.soulSpeedBonus).setDefaultValue(0.03).setSaveConsumer(v -> c.soulSpeedBonus = v).build());
    }

    private static void fishingCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.FishingConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("fishing"));
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(180).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(500).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startIntField(t("adept_luck"), c.adeptLuckBonus).setDefaultValue(1).setSaveConsumer(v -> c.adeptLuckBonus = v).build());
        cat.addEntry(e.startIntField(t("sync_luck"), c.syncLuckBonus).setDefaultValue(2).setSaveConsumer(v -> c.syncLuckBonus = v).build());
        cat.addEntry(e.startIntField(t("soul_luck"), c.soulLuckBonus).setDefaultValue(3).setSaveConsumer(v -> c.soulLuckBonus = v).build());
        cat.addEntry(e.startIntField(t("adept_lure"), c.adeptLurePenalty).setDefaultValue(-1).setSaveConsumer(v -> c.adeptLurePenalty = v).build());
        cat.addEntry(e.startIntField(t("sync_lure"), c.syncLurePenalty).setDefaultValue(-2).setSaveConsumer(v -> c.syncLurePenalty = v).build());
        cat.addEntry(e.startIntField(t("soul_lure"), c.soulLurePenalty).setDefaultValue(-3).setSaveConsumer(v -> c.soulLurePenalty = v).build());
    }

    private static void durabilityOnlyCategory(ConfigBuilder b, ConfigEntryBuilder e, Component name, ModConfig.ShearsConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(name);
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(180).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(500).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
    }

    private static void flintCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.FlintConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("flint"));
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(180).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(500).setSaveConsumer(v -> c.soulThreshold = v).build());
        cat.addEntry(e.startBooleanToggle(t("soul_requires_creeper"), c.soulRequiresCreeper).setDefaultValue(true).setSaveConsumer(v -> c.soulRequiresCreeper = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
    }

    private static void crossbowCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.CrossbowConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("crossbow"));
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(180).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(540).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
        cat.addEntry(e.startDoubleField(t("adept_arrow"), c.adeptArrowBonus).setDefaultValue(1.2).setSaveConsumer(v -> c.adeptArrowBonus = v).build());
        cat.addEntry(e.startDoubleField(t("sync_arrow"), c.syncArrowBonus).setDefaultValue(1.5).setSaveConsumer(v -> c.syncArrowBonus = v).build());
        cat.addEntry(e.startDoubleField(t("soul_arrow"), c.soulArrowBonus).setDefaultValue(1.8).setSaveConsumer(v -> c.soulArrowBonus = v).build());
    }

    private static void spearCategory(ConfigBuilder b, ConfigEntryBuilder e, ModConfig.SpearConfig c) {
        ConfigCategory cat = b.getOrCreateCategory(t("spear"));
        cat.addEntry(e.startIntField(t("adept_threshold"), c.adeptThreshold).setDefaultValue(60).setSaveConsumer(v -> c.adeptThreshold = v).build());
        cat.addEntry(e.startIntField(t("sync_threshold"), c.syncThreshold).setDefaultValue(180).setSaveConsumer(v -> c.syncThreshold = v).build());
        cat.addEntry(e.startIntField(t("soul_threshold"), c.soulThreshold).setDefaultValue(500).setSaveConsumer(v -> c.soulThreshold = v).build());
        addDurability(cat, e, c.adeptDurability, c.syncDurability, c.soulDurability, 1.2, 1.5, 1.8, v -> c.adeptDurability = v, v -> c.syncDurability = v, v -> c.soulDurability = v);
    }

    private static void addDurability(ConfigCategory cat, ConfigEntryBuilder e,
            double adept, double sync, double soul,
            double defAdept, double defSync, double defSoul,
            java.util.function.DoubleConsumer saveAdept,
            java.util.function.DoubleConsumer saveSync,
            java.util.function.DoubleConsumer saveSoul) {
        cat.addEntry(e.startDoubleField(t("adept_durability"), adept).setDefaultValue(defAdept).setSaveConsumer(saveAdept::accept).build());
        cat.addEntry(e.startDoubleField(t("sync_durability"), sync).setDefaultValue(defSync).setSaveConsumer(saveSync::accept).build());
        cat.addEntry(e.startDoubleField(t("soul_durability"), soul).setDefaultValue(defSoul).setSaveConsumer(saveSoul::accept).build());
    }

}

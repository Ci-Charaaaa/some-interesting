package com.chara.some_interesting.config;

public class ModConfig {

    private static ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void set(ModConfig config) {
        INSTANCE = config;
    }

    public String _readme_1 = "=== Some Interesting 模组配置文件 / Mod Configuration File ===";
    public String _readme_2 = "修改后需重启游戏生效 / Restart game after modification";
    public String _readme_3 = "文件位置 / File location: .minecraft/config/some-interesting.json";
    public String _readme_4 = "--- 字段说明 / Field Guide ---";
    public String _readme_5 = "Threshold(阈值): 达到此数值时触发升级 / Triggers upgrade when reached";
    public String _readme_6 = "Durability(耐久): 乘数, 1.2=耐久+20%, 1.5=+50%, 1.8=+80%";
    public String _readme_7 = "DamageBonus(伤害): 百分比, 0.2=伤害+20% / Percentage, 0.2=+20% damage";
    public String _readme_8 = "MiningBonus(挖掘): 平值加成, 2=效率+2 / Flat value, 2=+2 efficiency";
    public String _readme_9 = "ArrowBonus(箭矢): 伤害乘数, 1.2=箭矢伤害x1.2 / Multiplier, 1.2=x1.2 arrow dmg";
    public String _readme_10 = "adept=粗通, sync=精通, soul=灵魂相通 / adept→synchronized→soulbound";

    public SwordConfig sword = new SwordConfig();
    public MaceConfig mace = new MaceConfig();
    public AxeConfig axe = new AxeConfig();
    public TridentConfig trident = new TridentConfig();
    public PickaxeConfig pickaxe = new PickaxeConfig();
    public ShovelConfig shovel = new ShovelConfig();
    public HoeConfig hoe = new HoeConfig();
    public BowConfig bow = new BowConfig();
    public CrossbowConfig crossbow = new CrossbowConfig();
    public ArmorConfig armor = new ArmorConfig();
    public ShieldConfig shield = new ShieldConfig();
    public ElytraConfig elytra = new ElytraConfig();
    public FishingConfig fishing = new FishingConfig();
    public ShearsConfig shears = new ShearsConfig();
    public FlintConfig flint = new FlintConfig();
    public SpearConfig spear = new SpearConfig();

    public static class SwordConfig {
        public int adeptNormal = 30;
        public int adeptSuper = 6;
        public int syncNormal = 180;
        public int syncSuper = 20;
        public int soulNormal = 500;
        public int soulSuper = 80;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptDamageBonus = 0.2;
        public double syncDamageBonus = 0.5;
        public double soulDamageBonus = 0.8;
    }

    public static class MaceConfig {
        public int adeptNormal = 18;
        public int adeptSuper = 6;
        public int syncNormal = 36;
        public int syncSuper = 30;
        public int soulNormal = 72;
        public int soulSuper = 90;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptDamageBonus = 0.2;
        public double syncDamageBonus = 0.5;
        public double soulDamageBonus = 0.8;
    }

    public static class AxeConfig {
        public int adeptNormal = 30;
        public int adeptSuper = 3;
        public int syncNormal = 120;
        public int syncSuper = 12;
        public int soulNormal = 400;
        public int soulSuper = 40;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptDamageBonus = 0.1;
        public double syncDamageBonus = 0.25;
        public double soulDamageBonus = 0.4;

        public double adeptMiningBonus = 1.0;
        public double syncMiningBonus = 2.0;
        public double soulMiningBonus = 4.0;
    }

    public static class TridentConfig {
        public int adeptNormal = 30;
        public int adeptSuper = 6;
        public int syncNormal = 150;
        public int syncSuper = 20;
        public int soulNormal = 450;
        public int soulSuper = 80;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptDamageBonus = 0.2;
        public double syncDamageBonus = 0.5;
        public double soulDamageBonus = 0.8;

        public double adeptThrownBonus = 0.2;
        public double syncThrownBonus = 0.5;
        public double soulThrownBonus = 0.8;
    }

    public static class PickaxeConfig {
        public int adeptNormal = 60;
        public int adeptRare = 0;
        public int syncNormal = 500;
        public int syncRare = 3;
        public int soulNormal = 1800;
        public int soulRare = 18;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptMiningBonus = 2.0;
        public double syncMiningBonus = 4.0;
        public double soulMiningBonus = 8.0;
    }

    public static class ShovelConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 300;
        public int soulThreshold = 1200;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptMiningBonus = 2.0;
        public double syncMiningBonus = 4.0;
        public double soulMiningBonus = 8.0;
    }

    public static class HoeConfig {
        public int adeptMining = 60;
        public int syncMining = 180;
        public int soulMining = 540;

        public int adeptTilling = 60;
        public int syncTilling = 180;
        public int soulTilling = 600;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptMiningBonus = 2.0;
        public double syncMiningBonus = 4.0;
        public double soulMiningBonus = 8.0;
    }

    public static class BowConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 180;
        public int soulThreshold = 500;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptArrowBonus = 1.2;
        public double syncArrowBonus = 1.5;
        public double soulArrowBonus = 1.8;
    }

    public static class CrossbowConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 180;
        public int soulThreshold = 540;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptArrowBonus = 1.2;
        public double syncArrowBonus = 1.5;
        public double soulArrowBonus = 1.8;
    }

    public static class ArmorConfig {
        public int adeptThreshold = 200;
        public int syncThreshold = 800;
        public int soulThreshold = 2000;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptArmorBonus = 0.2;
        public double syncArmorBonus = 0.4;
        public double soulArmorBonus = 0.6;

        public double adeptToughnessBonus = 0.0;
        public double syncToughnessBonus = 0.1;
        public double soulToughnessBonus = 0.2;
    }

    public static class ShieldConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 200;
        public int soulThreshold = 500;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptKnockbackResist = 0.1;
        public double syncKnockbackResist = 0.2;
        public double soulKnockbackResist = 0.4;
    }

    public static class ElytraConfig {
        public int adeptThreshold = 120;
        public int syncThreshold = 480;
        public int soulThreshold = 960;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public double adeptSpeedBonus = 0.01;
        public double syncSpeedBonus = 0.02;
        public double soulSpeedBonus = 0.03;
    }

    public static class FishingConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 180;
        public int soulThreshold = 500;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;

        public int adeptLurePenalty = -1;
        public int syncLurePenalty = -2;
        public int soulLurePenalty = -3;

        public int adeptLuckBonus = 1;
        public int syncLuckBonus = 2;
        public int soulLuckBonus = 3;
    }

    public static class ShearsConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 180;
        public int soulThreshold = 500;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;
    }

    public static class FlintConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 180;
        public int soulThreshold = 500;
        public boolean soulRequiresCreeper = true;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;
    }

    public static class SpearConfig {
        public int adeptThreshold = 60;
        public int syncThreshold = 180;
        public int soulThreshold = 500;

        public double adeptDurability = 1.2;
        public double syncDurability = 1.5;
        public double soulDurability = 1.8;
    }
}

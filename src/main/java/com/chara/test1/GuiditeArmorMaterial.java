package com.chara.test1;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.*;

import java.util.Map;

import static com.chara.test1.ModItems.register;

public class GuiditeArmorMaterial {


    //注册物品：盔甲材料
    public static final ResourceKey<EquipmentAsset> EMERALD_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(TestMod.MOD_ID, "emerald"));

    //设置盔甲的基础耐久属性，钻石为33，下界合金为37
    public static final int EMERALD_BASE_DURABILITY = 18;

    //创建具体护甲实例
    public static final ArmorMaterial EMERALD_INSTANCE = new ArmorMaterial(
            EMERALD_BASE_DURABILITY,
            Map.of(//各部位护甲的护甲值枚举
                ArmorType.HELMET,3,
                ArmorType.CHESTPLATE,6,
                ArmorType.LEGGINGS,4,
                ArmorType.BOOTS,2
            ),
            //盔甲附魔性，钻10，铁9
            12,
            //穿戴时的音效
            SoundEvents.ARMOR_EQUIP_IRON,
            //盔甲韧性
            0.75f,
            //击退抗性
            0.0f,
            //修复使用的材料所在的标签
            ModItems.EMERADL_TAG,
            //注册的键
            EMERALD_ARMOR_MATERIAL_KEY
    );


    //创建具体部位的盔甲物品
    public static final Item EMERALD_HELMET = register(
            "emerald_helmet",
            Item::new,
            new Item.Properties().humanoidArmor(GuiditeArmorMaterial.EMERALD_INSTANCE, ArmorType.HELMET)
                    .durability(ArmorType.HELMET.getDurability(GuiditeArmorMaterial.EMERALD_BASE_DURABILITY))
    );

    public static final Item EMERALD_CHESTPLATE = register(
            "emerald_chestplate",
            Item::new,
            new Item.Properties().humanoidArmor(GuiditeArmorMaterial.EMERALD_INSTANCE, ArmorType.CHESTPLATE)
                    .durability(ArmorType.CHESTPLATE.getDurability(GuiditeArmorMaterial.EMERALD_BASE_DURABILITY))
    );

    public static final Item EMERALD_LEGGINGS = register(
            "emerald_leggings",
            Item::new,
            new Item.Properties().humanoidArmor(GuiditeArmorMaterial.EMERALD_INSTANCE, ArmorType.LEGGINGS)
                    .durability(ArmorType.LEGGINGS.getDurability(GuiditeArmorMaterial.EMERALD_BASE_DURABILITY))
    );

    public static final Item EMERALD_BOOTS = register(
            "emerald_boots",
            Item::new,
            new Item.Properties().humanoidArmor(GuiditeArmorMaterial.EMERALD_INSTANCE, ArmorType.BOOTS)
                    .durability(ArmorType.BOOTS.getDurability(GuiditeArmorMaterial.EMERALD_BASE_DURABILITY))
    );








    public static final int tem(){

        int i = ArmorMaterials.DIAMOND.durability();
        System.out.println(i);


        return 0 ;
    }



}

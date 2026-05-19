package com.chara.test1;

import com.chara.test1.component.ModComponent;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.datafix.fixes.MobEffectIdFix;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;
import java.util.function.Function;

public class ModItems{

    public static void initialize(){
        //执行具体的自定义标签页的注册逻辑，物品的注册不需要是因为注册方法是静态的，加载时直接自动编译内部的注册逻辑
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,CUSTOM_CREATIVE_TAB_KEY,CUSTOM_CREATIVE_TAB);
    }

    //创造一个自己的创造模式物品标签页的键，用于后面注册
    public static final ResourceKey<CreativeModeTab> CUSTOM_CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(TestMod.MOD_ID,
                    "creative_tab")
    );

    //用于具体的创建mod的创造模式物品标签页
    public static final CreativeModeTab CUSTOM_CREATIVE_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.EMERALD_SWORD)) //这里是给标签页的贴图设置为指定物品/方块的贴图
            .title(Component.translatable("creativeTab.test-mod")) //这里是指定标签页在本地显示文本名称时使用的key，用这个key去对应真正的name
            .displayItems((params, output) -> {

                //添加物品的地方，此处添加一套绿宝石工具
                output.accept(ModItems.EMERALD_SWORD);
                output.accept(ModItems.EMERALD_PICKAXE);
                output.accept(ModItems.USELESS_ITEM);
                output.accept(ModItems.EMERALD_SHOVEL);
                output.accept(ModItems.EMERALD_HOE);
                output.accept(ModItems.EMERALD_AXE);
                //一套绿宝石盔甲
                output.accept(GuiditeArmorMaterial.EMERALD_HELMET);
                output.accept(GuiditeArmorMaterial.EMERALD_CHESTPLATE);
                output.accept(GuiditeArmorMaterial.EMERALD_LEGGINGS);
                output.accept(GuiditeArmorMaterial.EMERALD_BOOTS);

                //添加一个食物
                output.accept(ModItems.JUMP_APPLE);

                //同理添加方块
//                // The tab builder also accepts Blocks
//                output.accept(ModBlocks.CONDENSED_DIRT);
//                output.accept(ModBlocks.CONDENSED_OAK_LOG);

                // 这里是描述如何添加特殊的，处理过（处理包括特殊的文本描述，自带附魔等等）的物品/方块，到标签页里
                ItemStack stack = new ItemStack(Items.EMERALD);
                stack.set(DataComponents.ITEM_NAME, Component.literal("The emerald"));
                stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Make the emerald great again"))));
                output.accept(stack);
            })
            .build();




    //创建一个标签，这里是一个叫做“emerald_repair_tag”的，用来提供绿宝石等级的修复材料的标签（TAG）
    public static final TagKey<Item> EMERADL_TAG = TagKey.create(BuiltInRegistries.ITEM.key(),Identifier.fromNamespaceAndPath(TestMod.MOD_ID,"emerald_repair_tag"));

    //创建“工具等级”，使得程序会根据这个工具等级来给相应的工具进行赋值
    public static final ToolMaterial EMERALD_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,  //挖掘等级
            333,                                //耐久
            9F,                                //挖掘速度，铁6，钻8，金12
            2.5F,                               //基础攻击力
            16,                                 //附魔亲和力，铁14，木15，钻10，金22
            EMERADL_TAG                         //修复材料所在的标签，mc使用标签对修复材料进行分类
    );

    //可以给食物添加有特殊效果的组件
    public static final Consumable JUMP_APPLE_CONSUMABLE_CONTENT = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 6 * 20,1),1.0f))
            .build();

    //给与食物的基本属性的组件
    public static final FoodProperties JUMP_APPLE_CONTENT = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.3f)
            .build();


    //这是全局声明一个新的物品实例，Item::new是Item的构造器，
    //其传参和返回值与Function<Item.properties,T>一致，所以直接把构造器当接口实现用了，
    public static final Item USELESS_ITEM = register("useless_item",Item::new,new Item.Properties());

    //创建一个食物物品，后面属性使用food，food()传入直接新建的foodProper，也可以自定义两个组件，一个基础组件，一个特殊效果组件传入
    public static final Item JUMP_APPLE = register("jump_apple",Item::new,new Item.Properties().food(JUMP_APPLE_CONTENT,JUMP_APPLE_CONSUMABLE_CONTENT));

    //这是工具类物品的实例创建
    public static final Item EMERALD_PICKAXE = register("emerald_pickaxe",Item::new,new Item.Properties().pickaxe(EMERALD_MATERIAL,1f,-2.8f));
    public static final Item EMERALD_SWORD = register("emerald_sword",Item::new,new Item.Properties().sword(EMERALD_MATERIAL,3.5f,-2.4f));
    public static final Item EMERALD_SHOVEL = register("emerald_shovel",Item::new,new Item.Properties().shovel(EMERALD_MATERIAL,1f,-2.8f));
    public static final Item EMERALD_HOE = register("emerald_hoe",Item::new,new Item.Properties().hoe(EMERALD_MATERIAL,1f,-2f));
    public static final Item EMERALD_AXE = register("emerald_axe",Item::new,new Item.Properties().axe(EMERALD_MATERIAL,5f,-3f));




    public static <T extends Item> T register( String name, Function<Item.Properties,T> itemFactory
            , Item.Properties settings){


        //创造注册表关联的item的键，用于注册表识别
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TestMod.MOD_ID,name));

        //使用item的类成员properties的setid方法，把刚才声明的键给设置成一个物品的id，等于创建物品的实例
        T item = itemFactory.apply(settings.setId(itemKey));


        //把物品注册到表里面去,BuiltInRegistries是一个静态容器类，是小注册表的注册表，也就是总目录
        Registry.register(BuiltInRegistries.ITEM,itemKey,item);



        return item;
    }

}

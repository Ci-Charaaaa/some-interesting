package com.chara.some_interesting;

import com.chara.some_interesting.EventCallBack.AttackEvent;
import com.chara.some_interesting.EventCallBack.ArmorEvent;
import com.chara.some_interesting.EventCallBack.BreakEvent;
import com.chara.some_interesting.EventCallBack.RangedEvent;
import com.chara.some_interesting.EventCallBack.RightEvent;
import com.chara.some_interesting.EventCallBack.ShearsEvent;
import com.chara.some_interesting.EventCallBack.ElytraEvent;
import com.chara.some_interesting.component.*;
import com.chara.some_interesting.config.ModConfigLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SomeInteresting implements ModInitializer {
	public static final String MOD_ID = "some-interesting";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		ModConfigLoader.load();
		ModItems.initialize();
		GuiditeArmorMaterial.initialize();
		ModBlocks.initialize();

		AttackEvent.initialize();
		RangedEvent.initialize();
		BreakEvent.initialize();
		RightEvent.initialize();
		ArmorEvent.initialize();
		ShearsEvent.initialize();
		ElytraEvent.initialize();

		ModNetworking.initialize();

		PickaxeEnhanceComponent.initialize();
		SwordsEnhanceComponent.initialize();
		AxeEnhanceComponent.initialize();
		ShovelEnhanceComponent.initialize();
		HoeEnhanceComponent.initialize();
		BowEnhanceComponent.initialize();
		CrossbowEnhanceComponent.initialize();
		ArmorEnhanceComponent.initialize();
		ShieldEnhanceComponent.initialize();
		TridentEnhanceComponent.initialize();
		MaceEnhanceComponent.initialize();
		SpearEnhanceComponent.initialize();
		FishingRodEnhanceComponent.initialize();
		ShearsEnhanceComponent.initialize();
		FlintAndSteelEnhanceComponent.initialize();
		ElytraEnhanceComponent.initialize();


		//对原版的树叶添加食物的属性
		//橡树
		Food_in(Items.OAK_LEAVES);
		//云杉
		Food_in(Items.SPRUCE_LEAVES);
		//白桦
		Food_in(Items.BIRCH_LEAVES);
		//丛林
		Food_in(Items.JUNGLE_LEAVES);
		//樱花
		Food_in(Items.CHERRY_LEAVES);
		//红树
		Food_in(Items.MANGROVE_LEAVES);
		//深色橡木
		Food_in(Items.DARK_OAK_LEAVES);
		//苍白橡木
		Food_in(Items.PALE_OAK_LEAVES);
		//杜鹃树叶
		Food_in(Items.AZALEA_LEAVES);
		//盛开的杜鹃树叶
		Food_in(Items.FLOWERING_AZALEA_LEAVES);

		//金合欢树叶添加食用后中毒的效果，单独处理
		DefaultItemComponentEvents.MODIFY.register(context -> {
			context.modify(Items.ACACIA_LEAVES, builder -> {
				builder.set(DataComponents.FOOD, new FoodProperties(1, 0.4f, false));
				//截至到这一行，前面的内容与下方通用方法的逻辑相同，不再赘述
				//下方则展示了如何操作“特殊效果”，首先是特殊效果实例不再引用default默认实例
				//而是引用了builder来新建一个效果实例
				builder.set(DataComponents.CONSUMABLE, Consumable.builder()
						.consumeSeconds(1.6f)            //此处是进食时长，填入了默认的1.6s
						.animation(ItemUseAnimation.EAT) //这里为进食添加动画，同样使用了默认的EAT动画
						.sound(SoundEvents.GENERIC_EAT)  //这里是声誉，同上
						.hasConsumeParticles(true)       //这里是是否有食用时的碎片粒子效果，同上
						//此处则为“特殊效果”，先生成一个”应用状态影响的消耗品状态“实例
						.onConsume(new ApplyStatusEffectsConsumeEffect(
								//对这个消耗品状态进行def，此处为选择生物类影响：中毒，
								//持续事件200ticks（20ticks为一秒），等级加值为0（也就是默认的一级），
								//增强倍数为1.0.也就是不增强
								//最后.build，生成实例
								new MobEffectInstance(MobEffects.POISON,200,0),1.0f))
						.build());
			});
		});
	}

	//通用的给物品添加食物tag的方法
	public static void Food_in(Item leaves ){

		//DefaultItemComponentEvents是默认物品时间类，引用Modify来表示我要修改一个默认的事件
		//并register把这个修改注册到表里面，context则是“上下文”，用来提供你到底要对事件做什么的容器和载体
		DefaultItemComponentEvents.MODIFY.register(context -> {
			//这里提供好一个context，再次通过modify对这个容器增加/修改/删除内容
			//两个传参分别指定对什么物品操作，是什么操作，此处builder是新建操作...
			context.modify(leaves, builder -> {
				//此处引用食物组件对象，new一个新的食物类实例，并填入需要的值
				builder.set(DataComponents.FOOD, new FoodProperties(1, 0.4f, false));
				//这里则是引用食物组件的特殊效果对象，此处是直接引用了特殊效果的默认实例，表示没有特殊效果
				builder.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
			});
		});

	}
}
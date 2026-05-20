package com.chara.test1;

import com.chara.test1.component.EnhanceComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMod implements ModInitializer {
	public static final String MOD_ID = "test-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		ModItems.initialize();
		EnhanceComponent.initialize();
		GuiditeArmorMaterial.tem();

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult)->{

			//定义一个标识符用于修改攻击数据组件
			Identifier PROFICIENCY_BONUS_ID = Identifier.fromNamespaceAndPath("test-mod", "proficiency_damage");
			// 确保逻辑在服务端运行，且攻击的目标是个生物
			if (!world.isClientSide() && entity instanceof LivingEntity target) {

				//获取玩家手上的物品
				ItemStack heldstack = player.getMainHandItem();

				// 检查玩家手里的物品
				if (player.getItemInHand(hand).is(ItemTags.SWORDS)) {
					//获取高级数据组件的记录类和组件本身
					EnhanceComponent heldComponent = heldstack.getOrDefault(
							EnhanceComponent.TURE_PROFICIENCY_COMPONENT,
							new EnhanceComponent(0, 0,false,false));

					//获取两个具体的值
					int normal_count = heldComponent.normal_count();
					int super_count = heldComponent.super_count();
					boolean is_synchronized = heldComponent.is_synchronized();
					boolean is_soulbound = heldComponent.is_soulbound();

					//检测是否满足暴击条件
					boolean isCrit = player.fallDistance > 0.0F
							&& !player.onGround()
							&& !player.isInWater()
							&& !player.hasEffect(MobEffects.BLINDNESS)
							&& !player.isPassenger();

					//如果满足，则对super_count加1
					if (isCrit){
						heldstack.set(EnhanceComponent.TURE_PROFICIENCY_COMPONENT, new EnhanceComponent(normal_count, ++super_count,is_synchronized,is_soulbound));
					}

					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					if(normal_count <= 30 || super_count <= 6){
						//do nothing
					}else if (normal_count <= 300 || super_count <= 60){

						//如果发现是否精通为假，改成真的，并播放升级音效和输出文本
						if(!is_synchronized){

							is_synchronized = true;

							world.playSound(
									null,
									player.getX(), player.getY(), player.getZ(),
									net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
									net.minecraft.sounds.SoundSource.PLAYERS,
									1.0F, // 音量
									1.0F  // 音调
							);

					//获取物品名字（优先取自定义名，没有则用默认显示名）
					String name = heldstack.getOrDefault(DataComponents.CUSTOM_NAME,
							heldstack.getOrDefault(DataComponents.ITEM_NAME,
									Component.literal("???"))).getString();

					//输出文本
					player.sendSystemMessage(Component.translatable("item.test-mod.be.synchronized.text.info",name));
					player.sendSystemMessage(Component.translatable("item.test-mod.be.synchronized.repair_reset"));
					player.sendSystemMessage(Component.translatable("item.test-mod.be.synchronized.durability_up",String.valueOf((int)(max_damage*1.2))));
					player.sendSystemMessage(Component.translatable("item.test-mod.be.synchronized.damage_up"));
					player.sendSystemMessage(Component.translatable("item.test-mod.be.synchronized.next_goal",String.valueOf(300),String.valueOf(60)));

					//修改为真防止反复触发
					heldstack.set(EnhanceComponent.TURE_PROFICIENCY_COMPONENT,new EnhanceComponent(normal_count,super_count,true,is_soulbound));

					heldstack.set(DataComponents.MAX_DAMAGE,(int)(max_damage*1.2));
					heldstack.set(DataComponents.REPAIR_COST,0);

				}

				target.hurt(player.damageSources().magic(),3.0f);

					}else{

						//如果发现是否灵魂相通为假，改成真的，并播放升级音效和输出文本
						if(!is_soulbound){

							world.playSound(
									null,
									player.getX(), player.getY(), player.getZ(),
									net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
									net.minecraft.sounds.SoundSource.PLAYERS,
									1.0F, // 音量
									1.0F  // 音调
							);

							//获取物品名字
							String name = heldstack.getOrDefault(DataComponents.CUSTOM_NAME,
									heldstack.getOrDefault(DataComponents.ITEM_NAME,
											Component.literal("???"))).getString();

							//输出文本
							player.sendSystemMessage(Component.translatable("item.test-mod.be.soulbound.text.info",name).withStyle(ChatFormatting.GOLD));
							player.sendSystemMessage(Component.translatable("item.test-mod.be.soulbound.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.be.soulbound.durability_up",String.valueOf((int)(max_damage*1.8))));
							player.sendSystemMessage(Component.translatable("item.test-mod.be.soulbound.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.be.soulbound.max_level"));

							//修改为真防止反复触发
							heldstack.set(EnhanceComponent.TURE_PROFICIENCY_COMPONENT,new EnhanceComponent(normal_count,super_count,is_synchronized,true));

							heldstack.set(DataComponents.MAX_DAMAGE,(int)(max_damage*1.8));
							heldstack.set(DataComponents.REPAIR_COST,0);
						}

						target.hurt(player.damageSources().magic(),6.0f);


					}

					//每次自增普通攻击次数的值
					heldstack.set(EnhanceComponent.TURE_PROFICIENCY_COMPONENT, new EnhanceComponent(++normal_count, super_count,is_synchronized,is_soulbound));

				}
			}

			return InteractionResult.PASS;
		});



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

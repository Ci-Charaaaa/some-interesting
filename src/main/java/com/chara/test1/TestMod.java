package com.chara.test1;

import com.chara.test1.component.AxeEnhanceComponent;
import com.chara.test1.component.PickaxeEnhanceComponent;
import com.chara.test1.component.HoeEnhanceComponent;
import com.chara.test1.component.ShovelEnhanceComponent;
import com.chara.test1.component.SwordsEnhanceComponent;
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

public class TestMod implements ModInitializer {
	public static final String MOD_ID = "test-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		ModItems.initialize();
		GuiditeArmorMaterial.initialize();

		PickaxeEnhanceComponent.initialize();
		SwordsEnhanceComponent.initialize();
		AxeEnhanceComponent.initialize();
		ShovelEnhanceComponent.initialize();
		HoeEnhanceComponent.initialize();

		//镐使用的回调方法，侦测破坏方块，并实现相关逻辑
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			//定义一个标识符用于修改挖掘数据组件
			Identifier MINING_SPEED_ID = Identifier.fromNamespaceAndPath("test-mod", "mining_speed_id");
			//判断是否是客户端以及被挖掘的方块是否是镐可挖的方块类
			if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_PICKAXE)){

				ItemStack heldstack = player.getMainHandItem();
				//判断玩家手上是不是镐
				if(heldstack.is(ItemTags.PICKAXES)){
					//获取玩家手上的物品数据组件
					PickaxeEnhanceComponent pickaxeEnhanceComponent = heldstack.getOrDefault(
							PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,
							new PickaxeEnhanceComponent(0,0,false,false,false));

					int normal_mined_count = pickaxeEnhanceComponent.normal_excavate_count();
					int rare_mined_count = pickaxeEnhanceComponent.rare_excavate_count();
					boolean is_adept = pickaxeEnhanceComponent.is_adept();
					boolean is_synchronized = pickaxeEnhanceComponent.is_synchronized();
					boolean is_soulbound = pickaxeEnhanceComponent.is_soulbound();

					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					//如果挖的是稀有矿石，则稀有矿石挖掘数加1
					if (state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES) || state.is(ConventionalBlockTags.NETHERITE_SCRAP_ORES)){
						heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,++rare_mined_count,is_adept,is_synchronized,is_soulbound));
					}else{
						//普通挖掘自增一
						heldstack.set(
								PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,
								new PickaxeEnhanceComponent(++normal_mined_count,rare_mined_count,is_adept,is_synchronized,is_soulbound));

					}

					if(normal_mined_count <= 60 || rare_mined_count < 0){
						//do nothing
					}else if (normal_mined_count <= 180 || rare_mined_count <= 3){
						if (!is_adept){
							//防止反复触发
							is_adept = true;
							heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,rare_mined_count,true,is_synchronized,is_soulbound));

							//播放音效
							out_sound(world,player);
							//获取物品名字（优先取自定义名，没有则用默认显示名）
							String name = get_name(heldstack);

							//粗通文本
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.adept.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.adept.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.adept.durability_up", String.valueOf((int) (max_damage * 1.1))));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.adept.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.adept.next_goal", String.valueOf(150), String.valueOf(3)));

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//依旧修饰符
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//依旧需要覆盖，使所以build一个用
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							//依旧FOR循环加入数据时，过滤掉倍粗通时修改的属性
							for (ItemAttributeModifiers.Entry entry : current.modifiers()){
								if (!entry.modifier().id().equals(MINING_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							//依旧把东西一一输入
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(MINING_SPEED_ID, 2,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else if (normal_mined_count <= 500 || rare_mined_count <= 15){

						//判断是否已经精通
						if(!is_synchronized){

							//防止反复触发
							is_synchronized = true;
							heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,rare_mined_count,is_adept,true,is_soulbound));

							//播放音效
							out_sound(world,player);
							//获取物品名字（优先取自定义名，没有则用默认显示名）
							String name = get_name(heldstack);

							//输出文本
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.synchronized.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.synchronized.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.synchronized.durability_up", String.valueOf((int) (max_damage * 1.5))));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.synchronized.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.synchronized.next_goal", String.valueOf(500), String.valueOf(15)));

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//依旧修饰符
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//依旧需要覆盖，使所以build一个用
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							//依旧遍历读取所有数据
							current.modifiers().forEach(entry ->
									builder.add(entry.attribute(), entry.modifier(), entry.slot()));
							//依旧把东西一一输入
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(MINING_SPEED_ID, 4,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else {
						//判断是否灵魂相通
						if (!is_soulbound){

							//防止反复触发
							is_soulbound = true;
							heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,rare_mined_count,is_adept,is_synchronized,true));

							//播放音效
							out_sound(world,player);
							//获取物品名字（优先取自定义名，没有则用默认显示名）
							String name = get_name(heldstack);

							//输出文本
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.soulbound.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.soulbound.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.soulbound.durability_up", String.valueOf((int) (max_damage * 1.8))));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.soulbound.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.pickaxe.soulbound.max_level"));

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.8));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//依旧修饰符
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//依旧需要覆盖，使所以build一个用
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

							//依旧FOR循环加入数据时，过滤掉倍精通时修改的属性
							for (ItemAttributeModifiers.Entry entry : current.modifiers()){
								if (!entry.modifier().id().equals(MINING_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}

							//依旧把东西一一输入
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(MINING_SPEED_ID, 8,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
		});

		//剑逻辑
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
					SwordsEnhanceComponent heldComponent = heldstack.getOrDefault(
							SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT,
							new SwordsEnhanceComponent(0, 0,false,false,false));

					//获取两个具体的值
					int normal_count = heldComponent.normal_count();
					int super_count = heldComponent.super_count();
					boolean is_adept = heldComponent.is_adept();
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
						heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(normal_count, ++super_count,is_adept,is_synchronized,is_soulbound));
					}else{
						//每次自增普通攻击次数的值
						heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(++normal_count, super_count,is_adept,is_synchronized,is_soulbound));
					}

					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					if(normal_count <= 30 || super_count <= 6){
						//do nothing
					} else if (normal_count <= 180 || super_count <= 20) {
						if (!is_adept){
							//修改为真防止反复触发
							is_adept = true;
							heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(normal_count, super_count, true,is_synchronized, is_soulbound));
							//播放音效
							out_sound(world,player);
							//获取物品名字（优先取自定义名，没有则用默认显示名）
							String name = get_name(heldstack);

							//输出文本
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.adept.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.adept.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.adept.durability_up", String.valueOf((int) (max_damage * 1.2))));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.adept.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.adept.next_goal", String.valueOf(180), String.valueOf(20)));

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//获取这把剑当前拥有的所有属性修饰符--属性修饰符，通过这类modifier对剑的属性进行修改
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//组件算固有属性，不能动态修改，需要调用builder重新生成一个覆盖原有的
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							//复制原有的所有修饰符--新生成的那个只有要修改的地方与原本不同，
							//但是其他属性要保持不变，所以得先获取一下原有的
							current.modifiers().forEach(entry ->
									builder.add(entry.attribute(), entry.modifier(), entry.slot()));
							//追加精通加成--也就是要修改的部分
							builder.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(PROFICIENCY_BONUS_ID, 0.2,
											//选择 加上原有值*xx倍 的修改方式
											AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					} else if (normal_count <= 500 || super_count <= 80) {
						//如果发现是否精通为假，改成真的，并播放升级音效和输出文本
						if (!is_synchronized) {
							is_synchronized = true;
							//修改为真防止反复触发
							heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(normal_count, super_count, is_adept,true, is_soulbound));
							//播放音效
							out_sound(world,player);
							//获取物品名字（优先取自定义名，没有则用默认显示名）
							String name = get_name(heldstack);

							//输出文本
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.synchronized.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.synchronized.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.synchronized.durability_up", String.valueOf((int) (max_damage * 1.5))));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.synchronized.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.synchronized.next_goal", String.valueOf(500), String.valueOf(80)));

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//获取这把剑当前拥有的所有属性修饰符--属性修饰符，通过这类modifier对剑的属性进行修改
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//组件算固有属性，不能动态修改，需要调用builder重新生成一个覆盖原有的
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							//复制原有的所有修饰符--新生成的那个只有要修改的地方与原本不同，
							//通过if来判断过滤之前粗通修改的数值，重新从0加
							for(ItemAttributeModifiers.Entry entry : current.modifiers() ){
								if (!entry.modifier().id().equals(PROFICIENCY_BONUS_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							//追加精通加成--也就是要修改的部分
							builder.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(PROFICIENCY_BONUS_ID, 0.5,
											//选择 加上原有值*xx倍 的修改方式
											AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else{
						//如果发现是否灵魂相通为假，改成真的，并播放升级音效和输出文本
						if(!is_soulbound){
							is_soulbound = true;
							//修改为真防止反复触发
							heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT,new SwordsEnhanceComponent(normal_count,super_count,is_adept,is_synchronized,true));
							//播放音效
							out_sound(world,player);
							//获取物品名字
							String name = get_name(heldstack);

							//输出文本
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.soulbound.text.info",name).withStyle(ChatFormatting.GOLD));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.soulbound.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.soulbound.durability_up",String.valueOf((int)(max_damage*1.8))));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.soulbound.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.swords.soulbound.max_level"));

							heldstack.set(DataComponents.MAX_DAMAGE,(int)(max_damage*1.8));
							heldstack.set(DataComponents.REPAIR_COST,0);

							//获取这把剑当前拥有的所有属性修饰符--属性修饰符，通过这类modifier对剑的属性进行修改
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//组件算固有属性，不能动态修改，需要调用builder重新生成一个覆盖原有的
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							//复制原有的所有修饰符--新生成的那个只有要修改的地方与原本不同，
							//但是其他属性要保持不变，所以得先获取一下原有的
							//通过if来判断过滤之前精通修改的数值，重新从0加
							for(ItemAttributeModifiers.Entry entry : current.modifiers() ){
								if (!entry.modifier().id().equals(PROFICIENCY_BONUS_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							//追加灵魂相通加成--也就是要修改的部分
							builder.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(PROFICIENCY_BONUS_ID, 0.8,
											//选择 加上原有值*xx倍 的修改方式
											AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
			return InteractionResult.PASS;
		});

		//斧逻辑，斧头既要改效率又要改攻击，所有注册两个
		Identifier AXE_SPEED_ID = Identifier.fromNamespaceAndPath("test-mod", "axe_speed_id");
		Identifier AXE_DAMAGE_ID = Identifier.fromNamespaceAndPath("test-mod", "axe_damage_id");

		//斧暴击
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			//依旧判断是不是客户端和生物
			if(!world.isClientSide() && entity instanceof LivingEntity){
				ItemStack heldstack = player.getMainHandItem();
				//依旧判断是不是斧头
				if(heldstack.is(ItemTags.AXES)){
					//依旧获取数据组件
					AxeEnhanceComponent comp = heldstack.getOrDefault(
							AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
							new AxeEnhanceComponent(0,0,false,false,false));
					//依旧拿值
					int normal_count = comp.normal_count();
					int super_count = comp.super_count();
					boolean is_adept = comp.is_adept();
					boolean is_synchronized = comp.is_synchronized();
					boolean is_soulbound = comp.is_soulbound();

					//依旧暴击
					boolean isCrit = player.fallDistance > 0.0F
							&& !player.onGround()
							&& !player.isInWater()
							&& !player.hasEffect(MobEffects.BLINDNESS)
							&& !player.isPassenger();

					if(isCrit){
						heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
								new AxeEnhanceComponent(normal_count, ++super_count, is_adept, is_synchronized, is_soulbound));
					}else{
						heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
								new AxeEnhanceComponent(++normal_count, super_count, is_adept, is_synchronized, is_soulbound));
					}
				}
			}
			return InteractionResult.PASS;
		});

		//斧砍伐
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_AXE)){
				ItemStack heldstack = player.getMainHandItem();
				if(heldstack.is(ItemTags.AXES)){
					AxeEnhanceComponent comp = heldstack.getOrDefault(
							AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
							new AxeEnhanceComponent(0,0,false,false,false));

					int normal_count = comp.normal_count();
					int super_count = comp.super_count();
					boolean is_adept = comp.is_adept();
					boolean is_synchronized = comp.is_synchronized();
					boolean is_soulbound = comp.is_soulbound();
					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					//砍树也加
					heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
							new AxeEnhanceComponent(++normal_count, super_count, is_adept, is_synchronized, is_soulbound));

					if(normal_count <= 30 || super_count <= 3){
						//do nothing
					}else if(normal_count <= 120 || super_count <= 12){
						//粗通
						if(!is_adept){
							is_adept = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.adept.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.adept.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.adept.durability_up", String.valueOf((int)(max_damage * 1.2))));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.adept.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.adept.next_goal", String.valueOf(400), String.valueOf(40)));

							heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
									new AxeEnhanceComponent(normal_count, super_count, true, is_synchronized, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							current.modifiers().forEach(entry ->
									builder.add(entry.attribute(), entry.modifier(), entry.slot()));
							builder.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(AXE_DAMAGE_ID, 0.1,
											AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
									EquipmentSlotGroup.MAINHAND);
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(AXE_SPEED_ID, 1,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else if(normal_count <= 400 || super_count <= 40){
						//默契
						if(!is_synchronized){
							is_synchronized = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.synchronized.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.synchronized.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.synchronized.durability_up", String.valueOf((int)(max_damage * 1.5))));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.synchronized.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.synchronized.next_goal", String.valueOf(400), String.valueOf(40)));

							heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
									new AxeEnhanceComponent(normal_count, super_count, is_adept, true, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for(ItemAttributeModifiers.Entry entry : current.modifiers()){
								if(!entry.modifier().id().equals(AXE_DAMAGE_ID) && !entry.modifier().id().equals(AXE_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(AXE_DAMAGE_ID, 0.25,
											AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
									EquipmentSlotGroup.MAINHAND);
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(AXE_SPEED_ID, 2,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else{
						//灵魂相通
						if(!is_soulbound){
							is_soulbound = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.soulbound.text.info", name).withStyle(ChatFormatting.GOLD));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.soulbound.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.soulbound.durability_up", String.valueOf((int)(max_damage * 1.8))));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.soulbound.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.axe.soulbound.max_level"));

							heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
									new AxeEnhanceComponent(normal_count, super_count, is_adept, is_synchronized, true));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.8));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for(ItemAttributeModifiers.Entry entry : current.modifiers()){
								if(!entry.modifier().id().equals(AXE_DAMAGE_ID) && !entry.modifier().id().equals(AXE_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(AXE_DAMAGE_ID, 0.4,
											AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
									EquipmentSlotGroup.MAINHAND);
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(AXE_SPEED_ID, 4,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
		});

		//铲逻辑
		Identifier SHOVEL_SPEED_ID = Identifier.fromNamespaceAndPath("test-mod", "shovel_speed_id");

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_SHOVEL) ){
				ItemStack heldstack = player.getMainHandItem();
				if(heldstack.is(ItemTags.SHOVELS)){
					ShovelEnhanceComponent comp = heldstack.getOrDefault(
							ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
							new ShovelEnhanceComponent(0,false,false,false));
					int normal_count = comp.normal_count();
					boolean is_adept = comp.is_adept();
					boolean is_synchronized = comp.is_synchronized();
					boolean is_soulbound = comp.is_soulbound();
					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
							new ShovelEnhanceComponent(++normal_count, is_adept, is_synchronized, is_soulbound));

					if(normal_count <= 60){
						//do nothing
					}else if(normal_count <= 180){
						//粗通
						if(!is_adept){
							is_adept = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.adept.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.adept.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.adept.durability_up", String.valueOf((int)(max_damage * 1.2))));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.adept.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.adept.next_goal", String.valueOf(150)));

							heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
									new ShovelEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							current.modifiers().forEach(entry ->
									builder.add(entry.attribute(), entry.modifier(), entry.slot()));
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(SHOVEL_SPEED_ID, 2,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else if(normal_count <= 600){
						//默契
						if(!is_synchronized){
							is_synchronized = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.synchronized.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.synchronized.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.synchronized.durability_up", String.valueOf((int)(max_damage * 1.5))));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.synchronized.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.synchronized.next_goal", String.valueOf(150)));

							heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
									new ShovelEnhanceComponent(normal_count, is_adept, true, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for(ItemAttributeModifiers.Entry entry : current.modifiers()){
								if(!entry.modifier().id().equals(SHOVEL_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(SHOVEL_SPEED_ID, 4,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else{
						//灵魂相通
						if(!is_soulbound){
							is_soulbound = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.soulbound.text.info", name).withStyle(ChatFormatting.GOLD));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.soulbound.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.soulbound.durability_up", String.valueOf((int)(max_damage * 1.8))));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.soulbound.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.shovel.soulbound.max_level"));

							heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
									new ShovelEnhanceComponent(normal_count, is_adept, is_synchronized, true));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.8));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for(ItemAttributeModifiers.Entry entry : current.modifiers()){
								if(!entry.modifier().id().equals(SHOVEL_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(SHOVEL_SPEED_ID, 8,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
		});

		//锄逻辑
		Identifier HOE_SPEED_ID = Identifier.fromNamespaceAndPath("test-mod", "hoe_speed_id");

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_HOE) ){
				ItemStack heldstack = player.getMainHandItem();
				if(heldstack.is(ItemTags.HOES)){
					HoeEnhanceComponent comp = heldstack.getOrDefault(
							HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
							new HoeEnhanceComponent(0,false,false,false));
					int normal_count = comp.normal_count();
					boolean is_adept = comp.is_adept();
					boolean is_synchronized = comp.is_synchronized();
					boolean is_soulbound = comp.is_soulbound();
					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
							new HoeEnhanceComponent(++normal_count, is_adept, is_synchronized, is_soulbound));

					if(normal_count <= 60){
						//do nothing
					}else if(normal_count <= 180){
						//粗通
						if(!is_adept){
							is_adept = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.durability_up", String.valueOf((int)(max_damage * 1.2))));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.next_goal", String.valueOf(150)));

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							current.modifiers().forEach(entry ->
									builder.add(entry.attribute(), entry.modifier(), entry.slot()));
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(HOE_SPEED_ID, 2,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else if(normal_count <= 600){
						//默契
						if(!is_synchronized){
							is_synchronized = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.durability_up", String.valueOf((int)(max_damage * 1.5))));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.next_goal", String.valueOf(150)));

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, true, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for(ItemAttributeModifiers.Entry entry : current.modifiers()){
								if(!entry.modifier().id().equals(HOE_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(HOE_SPEED_ID, 4,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else{
						//灵魂相通
						if(!is_soulbound){
							is_soulbound = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.text.info", name).withStyle(ChatFormatting.GOLD));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.durability_up", String.valueOf((int)(max_damage * 1.8))));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.max_level"));

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, is_synchronized, true));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.8));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for(ItemAttributeModifiers.Entry entry : current.modifiers()){
								if(!entry.modifier().id().equals(HOE_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(HOE_SPEED_ID, 8,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
		});

		//锄耕地（右键计数）
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			ItemStack heldstack = player.getItemInHand(hand);
			if(!world.isClientSide() && heldstack.is(ItemTags.HOES)){

				//通过hitResult获取方块信息
				BlockPos hitPos = hitResult.getBlockPos();
				BlockState hitState = world.getBlockState(hitPos);
				Block hitBlock = hitState.getBlock();
				//检测是不是可耕耘方块
				if(hitBlock == Blocks.DIRT || hitBlock == Blocks.GRASS_BLOCK ||
						hitBlock == Blocks.PODZOL || hitBlock == Blocks.COARSE_DIRT ||
						hitBlock == Blocks.ROOTED_DIRT ){
					HoeEnhanceComponent comp = heldstack.getOrDefault(
							HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
							new HoeEnhanceComponent(0,false,false,false));

					int normal_count = comp.normal_count();
					boolean is_adept = comp.is_adept();
					boolean is_synchronized = comp.is_synchronized();
					boolean is_soulbound = comp.is_soulbound();
					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
							new HoeEnhanceComponent(++normal_count, is_adept, is_synchronized, is_soulbound));

					if(normal_count <= 60){
						//do nothing
					}else if(normal_count <= 180){
						//粗通
						if(!is_adept){
							is_adept = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.durability_up", String.valueOf((int)(max_damage * 1.2))));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.adept.next_goal", String.valueOf(150)));

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							current.modifiers().forEach(entry ->
									builder.add(entry.attribute(), entry.modifier(), entry.slot()));
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(HOE_SPEED_ID, 2,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else if(normal_count <= 600){
						//默契
						if(!is_synchronized){
							is_synchronized = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.text.info", name));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.durability_up", String.valueOf((int)(max_damage * 1.5))));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.synchronized.next_goal", String.valueOf(150)));

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, true, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for(ItemAttributeModifiers.Entry entry : current.modifiers()){
								if(!entry.modifier().id().equals(HOE_SPEED_ID)){
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(HOE_SPEED_ID, 4,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else {
						//灵魂相通
						if (!is_soulbound) {
							is_soulbound = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.text.info", name).withStyle(ChatFormatting.GOLD));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.repair_reset"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.durability_up", String.valueOf((int) (max_damage * 1.8))));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.damage_up"));
							player.sendSystemMessage(Component.translatable("item.test-mod.hoe.soulbound.max_level"));

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, is_synchronized, true));
							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.8));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
								if (!entry.modifier().id().equals(HOE_SPEED_ID)) {
									builder.add(entry.attribute(), entry.modifier(), entry.slot());
								}
							}
							builder.add(Attributes.MINING_EFFICIENCY,
									new AttributeModifier(HOE_SPEED_ID, 8,
											AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
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

	public String get_name(ItemStack heldstack){
		return heldstack.getOrDefault(DataComponents.CUSTOM_NAME,
				heldstack.getOrDefault(DataComponents.ITEM_NAME,
						Component.literal("???"))).getString();
	}

	public void out_sound(Level world, Player player){
		//播放升级音效
		world.playSound(
				null,
				player.getX(), player.getY(), player.getZ(),
				net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
				net.minecraft.sounds.SoundSource.PLAYERS,
				1.0F, // 音量
				1.0F  // 音调
		);
	}



}
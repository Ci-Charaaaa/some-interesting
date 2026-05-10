package com.chara.test1;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
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
		GuiditeArmorMaterial.tem();



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
				builder.set(DataComponents.CONSUMABLE, Consumable.builder()
						.consumeSeconds(1.6f)
						.animation(ItemUseAnimation.EAT)
						.sound(SoundEvents.GENERIC_EAT)
						.hasConsumeParticles(true)
						.onConsume(new ApplyStatusEffectsConsumeEffect(
								new MobEffectInstance(MobEffects.POISON,200,0),1.0f))
						.build());
			});
		});
	}

	public static void Food_in(Item leaves ){

		DefaultItemComponentEvents.MODIFY.register(context -> {
			context.modify(leaves, builder -> {
				builder.set(DataComponents.FOOD, new FoodProperties(1, 0.4f, false));
				builder.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
			});
		});

	}


}

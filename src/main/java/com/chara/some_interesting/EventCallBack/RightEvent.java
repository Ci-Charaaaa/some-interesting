package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.FlintAndSteelEnhanceComponent;
import com.chara.some_interesting.component.HoeEnhanceComponent;
import com.chara.some_interesting.component.ShovelEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

public class RightEvent {


    public static void initialize(){

        Identifier HOE_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "hoe_speed_id");

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			ItemStack heldstack = player.getItemInHand(hand);
			if(!world.isClientSide() && heldstack.is(ItemTags.HOES)){

				BlockPos hitPos = hitResult.getBlockPos();
				BlockState hitState = world.getBlockState(hitPos);
				Block hitBlock = hitState.getBlock();
				if(hitBlock == Blocks.DIRT || hitBlock == Blocks.GRASS_BLOCK ||
						hitBlock == Blocks.PODZOL || hitBlock == Blocks.COARSE_DIRT ||
						hitBlock == Blocks.ROOTED_DIRT ){
					var cfg = ModConfig.get().hoe;
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

				if(normal_count < cfg.adeptTilling){
				}else if(normal_count < cfg.syncTilling){
						if(!is_adept){
							is_adept = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							upgrade_text(player,"hoe","adept",name,max_damage, cfg.adeptMiningBonus);

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.adeptDurability));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							copy_original_data(current,builder,HOE_SPEED_ID);

							Attack_damage_add(builder,HOE_SPEED_ID,cfg.adeptMiningBonus,heldstack,false);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else if(normal_count < cfg.soulTilling){
						if(!is_synchronized){
							is_synchronized = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							upgrade_text(player,"hoe","synchronized",name,max_damage, cfg.syncMiningBonus);

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, true, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.syncDurability));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							copy_original_data(current,builder,HOE_SPEED_ID);

							Attack_damage_add(builder,HOE_SPEED_ID,cfg.syncMiningBonus,heldstack,false);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else {
						if (!is_soulbound) {
							is_soulbound = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							upgrade_text(player,"hoe","soulbound",name,"max_level",max_damage, cfg.soulMiningBonus);

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, is_synchronized, true));
							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.soulDurability));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							copy_original_data(current,builder,HOE_SPEED_ID);

							Attack_damage_add(builder,HOE_SPEED_ID,cfg.soulMiningBonus,heldstack,false);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
			return InteractionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			ItemStack held = player.getItemInHand(hand);
			if (world.isClientSide() || !held.is(Items.FLINT_AND_STEEL)) {
				return InteractionResult.PASS;
			}

			var cfg = ModConfig.get().flint;
			FlintAndSteelEnhanceComponent comp = held.getOrDefault(
					FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT,
					new FlintAndSteelEnhanceComponent(0, false, false, false, false));

			int normal = comp.normal_count() + 1;
			boolean ad = comp.is_adept();
			boolean sy = comp.is_synchronized();
			boolean so = comp.is_soulbound();
			boolean creeper = comp.has_lit_portal();
			int md = held.getOrDefault(DataComponents.MAX_DAMAGE, 0);

			held.set(FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT,
					new FlintAndSteelEnhanceComponent(normal, creeper, ad, sy, so));

			boolean na = !ad && normal >= cfg.adeptThreshold;
			boolean ns = !sy && normal >= cfg.syncThreshold;
			boolean nl = !so && normal >= cfg.soulThreshold && (!cfg.soulRequiresCreeper || creeper);
			if (!na && !ns && !nl) return InteractionResult.PASS;

			if (!(world instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
			out_sound(serverLevel, player);
			String name = get_name(held);

			if (nl) {
				held.set(FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT,
						new FlintAndSteelEnhanceComponent(normal, creeper, true, true, true));
				held.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.soulDurability));
				held.set(DataComponents.REPAIR_COST, 0);
				upgrade_text(player, "fs", "soulbound", name, "max_level", (int)(md * cfg.soulDurability));
			} else if (ns) {
				held.set(FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT,
						new FlintAndSteelEnhanceComponent(normal, creeper, ad, true, so));
				held.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.syncDurability));
				held.set(DataComponents.REPAIR_COST, 0);
				upgrade_text(player, "fs", "synchronized", name, (int)(md * cfg.syncDurability));
				if (!creeper) {
					player.sendSystemMessage(Component.translatable("item.some-interesting.fs.synchronized.creeper_hint"));
				}
			} else {
				held.set(FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT,
						new FlintAndSteelEnhanceComponent(normal, creeper, true, sy, so));
				held.set(DataComponents.MAX_DAMAGE, (int)(md * cfg.adeptDurability));
				held.set(DataComponents.REPAIR_COST, 0);
				upgrade_text(player, "fs", "adept", name, (int)(md * cfg.adeptDurability));
			}

			return InteractionResult.PASS;
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide()) return InteractionResult.PASS;
			ItemStack held = player.getItemInHand(hand);
			if (!held.is(Items.FLINT_AND_STEEL)) return InteractionResult.PASS;
			if (!(entity instanceof Creeper creeper)) return InteractionResult.PASS;

			creeper.ignite();

			FlintAndSteelEnhanceComponent comp = held.getOrDefault(
					FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT,
					new FlintAndSteelEnhanceComponent(0, false, false, false, false));
			held.set(FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT,
					new FlintAndSteelEnhanceComponent(comp.normal_count(), true,
							comp.is_adept(), comp.is_synchronized(), comp.is_soulbound()));

			held.hurtAndBreak(1, player, hand);
			return InteractionResult.SUCCESS;
		});
    }
}

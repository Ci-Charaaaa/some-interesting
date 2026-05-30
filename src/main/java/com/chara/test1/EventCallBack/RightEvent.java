package com.chara.test1.EventCallBack;

import com.chara.test1.component.HoeEnhanceComponent;
import com.chara.test1.component.ShovelEnhanceComponent;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.chara.test1.EventCallBack.AttackEvent.*;

public class RightEvent {


    public static void initialize(){

        //锄修改符
        Identifier HOE_SPEED_ID = Identifier.fromNamespaceAndPath("test-mod", "hoe_speed_id");


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
							upgrade_text(player,"hoe","adept",name,max_damage);

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							copy_original_data(current,builder,HOE_SPEED_ID);

							Attack_damage_add(builder,HOE_SPEED_ID,2,heldstack,false);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else if(normal_count <= 600){
						//默契
						if(!is_synchronized){
							is_synchronized = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							upgrade_text(player,"hoe","synchronized",name,max_damage);

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, true, is_soulbound));
							heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							copy_original_data(current,builder,HOE_SPEED_ID);

							Attack_damage_add(builder,HOE_SPEED_ID,4,heldstack,false);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else {
						//灵魂相通
						if (!is_soulbound) {
							is_soulbound = true;
							out_sound(world, player);
							String name = get_name(heldstack);
							upgrade_text(player,"hoe","soulbound",name,"max_level",max_damage);

							heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
									new HoeEnhanceComponent(normal_count, is_adept, is_synchronized, true));
							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.8));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
							copy_original_data(current,builder,HOE_SPEED_ID);

							Attack_damage_add(builder,HOE_SPEED_ID,8,heldstack,false);
							heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
			return InteractionResult.PASS;
		});
    }
}

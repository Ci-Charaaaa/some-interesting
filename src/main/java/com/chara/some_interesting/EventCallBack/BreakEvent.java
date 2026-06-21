package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.AxeEnhanceComponent;
import com.chara.some_interesting.component.HoeEnhanceComponent;
import com.chara.some_interesting.component.PickaxeEnhanceComponent;
import com.chara.some_interesting.component.ShovelEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import static com.chara.some_interesting.EventCallBack.AttackEvent.*;

public class BreakEvent {


    public static void initialize(){

        Identifier MINING_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "mining_speed_id");
        Identifier HOE_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "hoe_speed_id");
        Identifier AXE_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "axe_speed_id");
        Identifier AXE_DAMAGE_ID = Identifier.fromNamespaceAndPath("some-interesting", "axe_damage_id");
        Identifier SHOVEL_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "shovel_speed_id");

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_PICKAXE)){

				ItemStack heldstack = player.getMainHandItem();
				if(heldstack.is(ItemTags.PICKAXES)){
					var cfg = ModConfig.get().pickaxe;
					PickaxeEnhanceComponent pickaxeEnhanceComponent = heldstack.getOrDefault(
							PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,
							new PickaxeEnhanceComponent(0,0,false,false,false));

					int normal_mined_count = pickaxeEnhanceComponent.normal_excavate_count();
					int rare_mined_count = pickaxeEnhanceComponent.rare_excavate_count();
					boolean is_adept = pickaxeEnhanceComponent.is_adept();
					boolean is_synchronized = pickaxeEnhanceComponent.is_synchronized();
					boolean is_soulbound = pickaxeEnhanceComponent.is_soulbound();

					int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

					if (state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES) || state.is(ConventionalBlockTags.NETHERITE_SCRAP_ORES)){
						heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,++rare_mined_count,is_adept,is_synchronized,is_soulbound));
					}

					heldstack.set(
							PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,
							new PickaxeEnhanceComponent(++normal_mined_count,rare_mined_count,is_adept,is_synchronized,is_soulbound));


				if(normal_mined_count < cfg.adeptNormal || rare_mined_count < cfg.adeptRare){
				}else if (normal_mined_count < cfg.syncNormal || rare_mined_count < cfg.syncRare){
						if (!is_adept){
							is_adept = true;
							heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,rare_mined_count,true,is_synchronized,is_soulbound));

							out_sound(world,player);
							String name = get_name(heldstack);
                            upgrade_text(player,"pickaxe","adept",name,max_damage, cfg.adeptMiningBonus);

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.adeptDurability));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

                            copy_original_data(current,builder,MINING_SPEED_ID);
                            Attack_damage_add(builder,MINING_SPEED_ID,cfg.adeptMiningBonus,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
				}else if (normal_mined_count < cfg.soulNormal || rare_mined_count < cfg.soulRare){

					if(!is_synchronized){

							is_synchronized = true;
							heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,rare_mined_count,is_adept,true,is_soulbound));

							out_sound(world,player);
							String name = get_name(heldstack);
                            upgrade_text(player,"pickaxe","synchronized",name,max_damage, cfg.syncMiningBonus);

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.syncDurability));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

                            copy_original_data(current,builder,MINING_SPEED_ID);
                            Attack_damage_add(builder,MINING_SPEED_ID,cfg.syncMiningBonus,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}else {
						if (!is_soulbound){

							is_soulbound = true;
							heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,rare_mined_count,is_adept,is_synchronized,true));

							out_sound(world,player);
							String name = get_name(heldstack);
                            upgrade_text(player,"pickaxe","soulbound",name,"max_level",max_damage, cfg.soulMiningBonus);

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.soulDurability));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

                            copy_original_data(current,builder,MINING_SPEED_ID);

                            Attack_damage_add(builder,MINING_SPEED_ID,cfg.soulMiningBonus,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
		});

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_AXE)){
                ItemStack heldstack = player.getMainHandItem();
                if(heldstack.is(ItemTags.AXES)){
                    var cfg = ModConfig.get().axe;
                    AxeEnhanceComponent comp = heldstack.getOrDefault(
                            AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                            new AxeEnhanceComponent(0,0,false,false,false));

                    int normal_count = comp.normal_count();
                    int super_count = comp.super_count();
                    boolean is_adept = comp.is_adept();
                    boolean is_synchronized = comp.is_synchronized();
                    boolean is_soulbound = comp.is_soulbound();
                    int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

                    heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                            new AxeEnhanceComponent(++normal_count, super_count, is_adept, is_synchronized, is_soulbound));

                    if(normal_count < cfg.adeptNormal || super_count < cfg.adeptSuper){
                    }else if(normal_count < cfg.syncNormal || super_count < cfg.syncSuper){
                        if(!is_adept){
                            is_adept = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"axe","adept",name,max_damage, cfg.adeptMiningBonus);

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, true, is_synchronized, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.adeptDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

                            copy_original_data(current,builder,AXE_DAMAGE_ID,AXE_SPEED_ID);

                            Attack_damage_add(builder,AXE_DAMAGE_ID,cfg.adeptDamageBonus,heldstack,true);
                            Attack_damage_add(builder,AXE_SPEED_ID,cfg.adeptMiningBonus,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else if(normal_count < cfg.soulNormal || super_count < cfg.soulSuper){
                        if(!is_synchronized){
                            is_synchronized = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"axe","synchronized",name,max_damage, cfg.syncMiningBonus);

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, is_adept, true, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.syncDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,AXE_DAMAGE_ID,AXE_SPEED_ID);

                            Attack_damage_add(builder,AXE_DAMAGE_ID,cfg.syncDamageBonus,heldstack,true);
                            Attack_damage_add(builder,AXE_SPEED_ID,cfg.syncMiningBonus,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else{
                        if(!is_soulbound){
                            is_soulbound = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"axe","soulbound",name,"max_level",max_damage, cfg.soulMiningBonus);

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, is_adept, is_synchronized, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.soulDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,AXE_DAMAGE_ID,AXE_SPEED_ID);

                            Attack_damage_add(builder,AXE_DAMAGE_ID,cfg.soulDamageBonus,heldstack,true);
                            Attack_damage_add(builder,AXE_SPEED_ID,cfg.soulMiningBonus,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
        });


        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_SHOVEL) ){
                ItemStack heldstack = player.getMainHandItem();
                if(heldstack.is(ItemTags.SHOVELS)){
                    var cfg = ModConfig.get().shovel;
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

                    if(normal_count < cfg.adeptThreshold){
                    }else if(normal_count < cfg.syncThreshold){
                        if(!is_adept){
                            is_adept = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"shovel","adept",name,max_damage, cfg.adeptMiningBonus);

                            heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
                                    new ShovelEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.adeptDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,SHOVEL_SPEED_ID);

                            Attack_damage_add(builder,SHOVEL_SPEED_ID,cfg.adeptMiningBonus,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else if(normal_count < cfg.soulThreshold){
                        if(!is_synchronized){
                            is_synchronized = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"shovel","synchronized",name,max_damage, cfg.syncMiningBonus);

                            heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
                                    new ShovelEnhanceComponent(normal_count, is_adept, true, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.syncDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,SHOVEL_SPEED_ID);

                            Attack_damage_add(builder,SHOVEL_SPEED_ID,cfg.syncMiningBonus,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else{
                        if(!is_soulbound){
                            is_soulbound = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"shovel","soulbound",name,"max_level",max_damage, cfg.soulMiningBonus);

                            heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
                                    new ShovelEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.soulDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,SHOVEL_SPEED_ID);

                            Attack_damage_add(builder,SHOVEL_SPEED_ID,cfg.soulMiningBonus,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if(!world.isClientSide() && state.is(BlockTags.MINEABLE_WITH_HOE) ){
                ItemStack heldstack = player.getMainHandItem();
                if(heldstack.is(ItemTags.HOES)){
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

                    if(normal_count < cfg.adeptMining){
                    }else if(normal_count < cfg.syncMining){
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
                    }else if(normal_count < cfg.soulMining){
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
                    }else{
                        if(!is_soulbound){
                            is_soulbound = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"hoe","soulbound",name,"max_level",max_damage, cfg.soulMiningBonus);

                            heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
                                    new HoeEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.soulDurability));
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
        });
    }
}

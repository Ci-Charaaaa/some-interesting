package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.AxeEnhanceComponent;
import com.chara.some_interesting.component.HoeEnhanceComponent;
import com.chara.some_interesting.component.PickaxeEnhanceComponent;
import com.chara.some_interesting.component.ShovelEnhanceComponent;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

        //镐修改符
        Identifier MINING_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "mining_speed_id");
        //锄修改符
        Identifier HOE_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "hoe_speed_id");
        //斧修改符，斧头既要改效率又要改攻击，所有注册两个
        Identifier AXE_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "axe_speed_id");
        Identifier AXE_DAMAGE_ID = Identifier.fromNamespaceAndPath("some-interesting", "axe_damage_id");
        //铲逻辑
        Identifier SHOVEL_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "shovel_speed_id");



        //镐使用的回调方法，侦测破坏方块，并实现相关逻辑
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
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
					}

					//普通挖掘自增一
					heldstack.set(
							PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,
							new PickaxeEnhanceComponent(++normal_mined_count,rare_mined_count,is_adept,is_synchronized,is_soulbound));


					if(normal_mined_count <= 60 || rare_mined_count < 0){
						//do nothing
					}else if (normal_mined_count <= 500 || rare_mined_count <= 3){
						if (!is_adept){
							//防止反复触发
							is_adept = true;
							heldstack.set(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT,new PickaxeEnhanceComponent(normal_mined_count,rare_mined_count,true,is_synchronized,is_soulbound));

							//播放音效
							out_sound(world,player);
							//获取物品名字（优先取自定义名，没有则用默认显示名）
							String name = get_name(heldstack);
							//粗通文本
                            upgrade_text(player,"pickaxe","adept",name,max_damage);

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.2));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//依旧修饰符
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//依旧需要覆盖，使所以build一个用
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

							//依旧FOR循环加入数据时，过滤掉倍粗通时修改的属性
                            copy_original_data(current,builder,MINING_SPEED_ID);
							//依旧把东西一一输入
                            Attack_damage_add(builder,MINING_SPEED_ID,2,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
					}else if (normal_mined_count <= 1800 || rare_mined_count <= 18){

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
                            upgrade_text(player,"pickaxe","synchronized",name,max_damage);

							heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.5));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//依旧修饰符
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//依旧需要覆盖，使所以build一个用
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

							//依旧FOR循环加入数据时，过滤掉倍粗通时修改的属性
                            copy_original_data(current,builder,MINING_SPEED_ID);
							//依旧把东西一一输入
                            Attack_damage_add(builder,MINING_SPEED_ID,4,heldstack,false);
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
                            upgrade_text(player,"pickaxe","soulbound",name,"max_level",max_damage);

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.8));
							heldstack.set(DataComponents.REPAIR_COST, 0);

							//依旧修饰符
							ItemAttributeModifiers current = heldstack.getOrDefault(
									DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
							//依旧需要覆盖，使所以build一个用
							ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

							//依旧FOR循环加入数据时，过滤掉倍精通时修改的属性
                            copy_original_data(current,builder,MINING_SPEED_ID);

							//依旧把东西一一输入
                            Attack_damage_add(builder,MINING_SPEED_ID,8,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
						}
					}
				}
			}
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
                            upgrade_text(player,"axe","adept",name,max_damage);

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, true, is_synchronized, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.2));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

                            copy_original_data(current,builder,AXE_DAMAGE_ID,AXE_SPEED_ID);

                            Attack_damage_add(builder,AXE_DAMAGE_ID,0.1,heldstack,true);
                            Attack_damage_add(builder,AXE_SPEED_ID,2,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else if(normal_count <= 400 || super_count <= 40){
                        //默契
                        if(!is_synchronized){
                            is_synchronized = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"axe","synchronized",name,max_damage);

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, is_adept, true, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.5));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,AXE_DAMAGE_ID,AXE_SPEED_ID);

                            Attack_damage_add(builder,AXE_DAMAGE_ID,0.25,heldstack,true);
                            Attack_damage_add(builder,AXE_SPEED_ID,4,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else{
                        //灵魂相通
                        if(!is_soulbound){
                            is_soulbound = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"axe","soulbound",name,"max_level",max_damage);

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, is_adept, is_synchronized, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.8));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,AXE_DAMAGE_ID,AXE_SPEED_ID);

                            Attack_damage_add(builder,AXE_DAMAGE_ID,0.4,heldstack,true);
                            Attack_damage_add(builder,AXE_SPEED_ID,4,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
        });


        //铲破坏
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
                    }else if(normal_count <= 300){
                        //粗通
                        if(!is_adept){
                            is_adept = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"shovel","adept",name,max_damage);

                            heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
                                    new ShovelEnhanceComponent(normal_count, true, is_synchronized, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.2));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,SHOVEL_SPEED_ID);

                            Attack_damage_add(builder,SHOVEL_SPEED_ID,2,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else if(normal_count <= 1200){
                        //默契
                        if(!is_synchronized){
                            is_synchronized = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"shovel","synchronized",name,max_damage);

                            heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
                                    new ShovelEnhanceComponent(normal_count, is_adept, true, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.5));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,SHOVEL_SPEED_ID);

                            Attack_damage_add(builder,SHOVEL_SPEED_ID,4,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else{
                        //灵魂相通
                        if(!is_soulbound){
                            is_soulbound = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"shovel","soulbound",name,"max_level",max_damage);

                            heldstack.set(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT,
                                    new ShovelEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.8));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,SHOVEL_SPEED_ID);

                            Attack_damage_add(builder,SHOVEL_SPEED_ID,8,heldstack,false);

                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
        });

        //锄破坏方块
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
                    }else if(normal_count <= 540){
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
                    }else{
                        //灵魂相通
                        if(!is_soulbound){
                            is_soulbound = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"hoe","soulbound",name,"max_level",max_damage);

                            heldstack.set(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT,
                                    new HoeEnhanceComponent(normal_count, is_adept, is_synchronized, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * 1.8));
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
        });
    }
}
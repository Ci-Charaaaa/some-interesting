package com.chara.test1.EventCallBack;

import com.chara.test1.component.AxeEnhanceComponent;
import com.chara.test1.component.SwordsEnhanceComponent;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

public class AttackEvent {

    public static void initialize(){

        //定义一个标识符用于修改攻击数据组件
        Identifier PROFICIENCY_BONUS_ID = Identifier.fromNamespaceAndPath("test-mod", "proficiency_damage");
        //斧逻辑，斧头既要改效率又要改攻击，所有注册两个
        Identifier AXE_SPEED_ID = Identifier.fromNamespaceAndPath("test-mod", "axe_speed_id");
        Identifier AXE_DAMAGE_ID = Identifier.fromNamespaceAndPath("test-mod", "axe_damage_id");


        //剑攻击逻辑
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult)->{
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
                    boolean isCrit = is_Crit(player);

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
                            upgrade_text(player,"swords","adept",name,max_damage);

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.2));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            //获取这把剑当前拥有的所有属性修饰符--属性修饰符，通过这类modifier对剑的属性进行修改
                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            //组件算固有属性，不能动态修改，需要调用builder重新生成一个覆盖原有的
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            //复制原有的所有修饰符--新生成的那个只有要修改的地方与原本不同，
                            //但是其他属性要保持不变，所以得先获取一下原有的
                            copy_original_data(current,builder,PROFICIENCY_BONUS_ID);
                            //追加精通加成--也就是要修改的部分
                            Attack_damage_add(builder,PROFICIENCY_BONUS_ID,0.2,heldstack,true);
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
                            upgrade_text(player,"swords","synchronized",name,max_damage);

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * 1.5));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            //获取这把剑当前拥有的所有属性修饰符--属性修饰符，通过这类modifier对剑的属性进行修改
                            ItemAttributeModifiers current = heldstack.getOrDefault(
                                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            //组件算固有属性，不能动态修改，需要调用builder重新生成一个覆盖原有的
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            //复制原有的所有修饰符--新生成的那个只有要修改的地方与原本不同，
                            //通过if来判断过滤之前粗通修改的数值，重新从0加
                            copy_original_data(current,builder,PROFICIENCY_BONUS_ID);
                            //追加精通加成--也就是要修改的部分
                            Attack_damage_add(builder,PROFICIENCY_BONUS_ID,0.5,heldstack,true);
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
                            upgrade_text(player,"swords","soulbound",name,"max_level",max_damage);

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
                            copy_original_data(current,builder,PROFICIENCY_BONUS_ID);
                            //追加灵魂相通加成--也就是要修改的部分
                            Attack_damage_add(builder,PROFICIENCY_BONUS_ID,0.8,heldstack,true);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

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
                    int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

                    //依旧暴击
                    boolean isCrit = is_Crit(player);

                    if(isCrit){
                        heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                new AxeEnhanceComponent(normal_count, ++super_count, is_adept, is_synchronized, is_soulbound));
                    }else{
                        heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                new AxeEnhanceComponent(++normal_count, super_count, is_adept, is_synchronized, is_soulbound));
                    }


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
                            Attack_damage_add(builder,AXE_DAMAGE_ID,1,heldstack,false);
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
                            Attack_damage_add(builder,AXE_DAMAGE_ID,2,heldstack,false);
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
                            Attack_damage_add(builder,AXE_DAMAGE_ID,4,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }

                }
            }
            return InteractionResult.PASS;
        });

    }

    public static String get_name(ItemStack heldstack){
        return heldstack.getOrDefault(DataComponents.CUSTOM_NAME,
                heldstack.getOrDefault(DataComponents.ITEM_NAME,
                        Component.literal("???"))).getString();
    }

    public static void out_sound(Level world, Player player){
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

    public static void upgrade_text(Player player,String type,String level,String name,String max_level,int max_damage){
        upgrade_text(player, type, level, name, max_damage);
        player.sendSystemMessage(Component.translatable("item.test-mod." + type + "." + level +"." + max_level));
    }

    public static void upgrade_text(Player player,String type,String level,String name,int max_damage){
        player.sendSystemMessage(Component.translatable("item.test-mod." + type + "." + level +".text.info", name).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("item.test-mod." + type + "." + level +".repair_reset"));
        player.sendSystemMessage(Component.translatable("item.test-mod." + type + "." + level +".durability_up", String.valueOf((int)(max_damage * 1.8))));
        player.sendSystemMessage(Component.translatable("item.test-mod." + type + "." + level +".damage_up"));
    }

    public static boolean is_Crit(Player player){
        return player.fallDistance > 0.0F
                && !player.onGround()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.isPassenger();
    }

    public static void Attack_damage_add(ItemAttributeModifiers.Builder builder,Identifier id,double multiple_value,ItemStack heldstack,boolean is_multiplied){
        if (is_multiplied){
            builder.add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(id, multiple_value,
                            //选择 加上原有值*xx倍 的修改方式
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    EquipmentSlotGroup.MAINHAND);
            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        }else{
            builder.add(Attributes.MINING_EFFICIENCY,
                    new AttributeModifier(id, multiple_value,
                            //选择 加上原有值*xx倍 的修改方式
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        }
    }

    //通过if来判断过滤之前修改过的数值，重新加
    public static void copy_original_data(ItemAttributeModifiers current,ItemAttributeModifiers.Builder builder,Identifier id1,Identifier id2){
        for(ItemAttributeModifiers.Entry entry : current.modifiers()){
            if(!entry.modifier().id().equals(id1) && !entry.modifier().id().equals(id2)){
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
    }

    public static void copy_original_data(ItemAttributeModifiers current,ItemAttributeModifiers.Builder builder,Identifier id1){
        for(ItemAttributeModifiers.Entry entry : current.modifiers()){
            if(!entry.modifier().id().equals(id1)){
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
    }

}



package com.chara.some_interesting.EventCallBack;

import com.chara.some_interesting.component.AxeEnhanceComponent;
import com.chara.some_interesting.component.MaceEnhanceComponent;
import com.chara.some_interesting.component.SwordsEnhanceComponent;
import com.chara.some_interesting.component.TridentEnhanceComponent;
import com.chara.some_interesting.config.ModConfig;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public class AttackEvent {

    public static final Identifier PROFICIENCY_BONUS_ID = Identifier.fromNamespaceAndPath("some-interesting", "proficiency_damage");
    public static final Identifier AXE_SPEED_ID = Identifier.fromNamespaceAndPath("some-interesting", "axe_speed_id");
    public static final Identifier AXE_DAMAGE_ID = Identifier.fromNamespaceAndPath("some-interesting", "axe_damage_id");
    public static final Identifier TRIDENT_DAMAGE_ID = Identifier.fromNamespaceAndPath("some-interesting", "trident_damage");
    public static final Identifier MACE_DAMAGE_ID = Identifier.fromNamespaceAndPath("some-interesting","mace_damage_id");

    public static void initialize(){

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult)->{
            if (!world.isClientSide() && entity instanceof LivingEntity target) {
                ItemStack heldstack = player.getMainHandItem();

                if (player.getItemInHand(hand).is(ItemTags.SWORDS)) {
                    var cfg = ModConfig.get().sword;
                    SwordsEnhanceComponent heldComponent = heldstack.getOrDefault(
                            SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT,
                            new SwordsEnhanceComponent(0, 0,false,false,false));

                    int normal_count = heldComponent.normal_count();
                    int super_count = heldComponent.super_count();
                    boolean is_adept = heldComponent.is_adept();
                    boolean is_synchronized = heldComponent.is_synchronized();
                    boolean is_soulbound = heldComponent.is_soulbound();

                    boolean isCrit = is_Crit(player);
                    if (isCrit){
                        heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(normal_count, ++super_count,is_adept,is_synchronized,is_soulbound));
                    }else{
                        heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(++normal_count, super_count,is_adept,is_synchronized,is_soulbound));
                    }

                    int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

                    if(normal_count < cfg.adeptNormal || super_count < cfg.adeptSuper){
                    } else if (normal_count < cfg.syncNormal || super_count < cfg.syncSuper) {
                        if (!is_adept){
                            is_adept = true;
                            heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(normal_count, super_count, true,is_synchronized, is_soulbound));
                            out_sound(world,player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"swords","adept",name,max_damage, cfg.adeptDamageBonus * 100 + "%");

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.adeptDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,PROFICIENCY_BONUS_ID);
                            Attack_damage_add(builder,PROFICIENCY_BONUS_ID,cfg.adeptDamageBonus,heldstack,true);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    } else if (normal_count < cfg.soulNormal || super_count < cfg.soulSuper) {
                        if (!is_synchronized) {
                            is_synchronized = true;
                            heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT, new SwordsEnhanceComponent(normal_count, super_count, is_adept,true, is_soulbound));
                            out_sound(world,player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"swords","synchronized",name,max_damage, cfg.syncDamageBonus * 100 + "%");

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.syncDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,PROFICIENCY_BONUS_ID);
                            Attack_damage_add(builder,PROFICIENCY_BONUS_ID,cfg.syncDamageBonus,heldstack,true);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else{
                        if(!is_soulbound){
                            is_soulbound = true;
                            heldstack.set(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT,new SwordsEnhanceComponent(normal_count,super_count,is_adept,is_synchronized,true));
                            out_sound(world,player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"swords","soulbound",name,"max_level",max_damage, cfg.soulDamageBonus * 100 + "%");

                            heldstack.set(DataComponents.MAX_DAMAGE,(int)(max_damage * cfg.soulDurability));
                            heldstack.set(DataComponents.REPAIR_COST,0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,PROFICIENCY_BONUS_ID);
                            Attack_damage_add(builder,PROFICIENCY_BONUS_ID,cfg.soulDamageBonus,heldstack,true);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult)->{
            if (!world.isClientSide() && entity instanceof LivingEntity target) {
                ItemStack heldstack = player.getMainHandItem();

                if (player.getItemInHand(hand).is(Items.MACE)) {
                    var cfg = ModConfig.get().mace;
                    MaceEnhanceComponent heldComponent = heldstack.getOrDefault(
                            MaceEnhanceComponent.MACE_PROFICIENCY_COMPONENT,
                            new MaceEnhanceComponent(0, 0,false,false,false));

                    int normal_count = heldComponent.normal_count();
                    int super_count = heldComponent.super_count();
                    boolean is_adept = heldComponent.is_adept();
                    boolean is_synchronized = heldComponent.is_synchronized();
                    boolean is_soulbound = heldComponent.is_soulbound();

                    boolean isCrit = is_Crit(player);
                    if (isCrit){
                        heldstack.set(MaceEnhanceComponent.MACE_PROFICIENCY_COMPONENT, new MaceEnhanceComponent(normal_count, ++super_count,is_adept,is_synchronized,is_soulbound));
                    }else{
                        heldstack.set(MaceEnhanceComponent.MACE_PROFICIENCY_COMPONENT, new MaceEnhanceComponent(++normal_count, super_count,is_adept,is_synchronized,is_soulbound));
                    }

                    int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE,0);

                    if(normal_count < cfg.adeptNormal || super_count < cfg.adeptSuper){
                    } else if (normal_count < cfg.syncNormal || super_count < cfg.syncSuper) {
                        if (!is_adept){
                            is_adept = true;
                            heldstack.set(MaceEnhanceComponent.MACE_PROFICIENCY_COMPONENT, new MaceEnhanceComponent(normal_count, super_count, true,is_synchronized, is_soulbound));
                            out_sound(world,player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"mace","adept",name,max_damage, cfg.adeptDamageBonus * 100 + "%");

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.adeptDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,MACE_DAMAGE_ID);
                            Attack_damage_add(builder,MACE_DAMAGE_ID,cfg.adeptDamageBonus,heldstack,true);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    } else if (normal_count < cfg.soulNormal || super_count < cfg.soulSuper) {
                        if (!is_synchronized) {
                            is_synchronized = true;
                            heldstack.set(MaceEnhanceComponent.MACE_PROFICIENCY_COMPONENT, new MaceEnhanceComponent(normal_count, super_count, is_adept,true, is_soulbound));
                            out_sound(world,player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"mace","synchronized",name,max_damage, cfg.syncDamageBonus * 100 + "%");

                            heldstack.set(DataComponents.MAX_DAMAGE, (int) (max_damage * cfg.syncDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,MACE_DAMAGE_ID);
                            Attack_damage_add(builder,MACE_DAMAGE_ID,cfg.syncDamageBonus,heldstack,true);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }else{
                        if(!is_soulbound){
                            is_soulbound = true;
                            heldstack.set(MaceEnhanceComponent.MACE_PROFICIENCY_COMPONENT,new MaceEnhanceComponent(normal_count,super_count,is_adept,is_synchronized,true));
                            out_sound(world,player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"mace","soulbound",name,"max_level",max_damage, cfg.soulDamageBonus * 100 + "%");

                            heldstack.set(DataComponents.MAX_DAMAGE,(int)(max_damage * cfg.soulDurability));
                            heldstack.set(DataComponents.REPAIR_COST,0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,MACE_DAMAGE_ID);
                            Attack_damage_add(builder,MACE_DAMAGE_ID,cfg.soulDamageBonus,heldstack,true);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if(!world.isClientSide() && entity instanceof LivingEntity){
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

                    boolean isCrit = is_Crit(player);
                    if(isCrit){
                        heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                new AxeEnhanceComponent(normal_count, ++super_count, is_adept, is_synchronized, is_soulbound));
                    }else{
                        heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                new AxeEnhanceComponent(++normal_count, super_count, is_adept, is_synchronized, is_soulbound));
                    }

                    if(normal_count < cfg.adeptNormal || super_count < cfg.adeptSuper){
                    }else if(normal_count < cfg.syncNormal || super_count < cfg.syncSuper){
                        if(!is_adept){
                            is_adept = true;
                            out_sound(world, player);
                            String name = get_name(heldstack);
                            upgrade_text(player,"axe","adept",name,max_damage, cfg.adeptDamageBonus * 100 + "%");

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, true, is_synchronized, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.adeptDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
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
                            upgrade_text(player,"axe","synchronized",name,max_damage, cfg.syncDamageBonus * 100 + "%");

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, is_adept, true, is_soulbound));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.syncDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
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
                            upgrade_text(player,"axe","soulbound",name,"max_level",max_damage, cfg.soulDamageBonus * 100 + "%");

                            heldstack.set(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT,
                                    new AxeEnhanceComponent(normal_count, super_count, is_adept, is_synchronized, true));
                            heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.soulDurability));
                            heldstack.set(DataComponents.REPAIR_COST, 0);

                            ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            copy_original_data(current,builder,AXE_DAMAGE_ID,AXE_SPEED_ID);

                            Attack_damage_add(builder,AXE_DAMAGE_ID,cfg.soulDamageBonus,heldstack,true);
                            Attack_damage_add(builder,AXE_SPEED_ID,cfg.soulMiningBonus,heldstack,false);
                            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if(!world.isClientSide() && entity instanceof LivingEntity target){
                ItemStack heldstack = player.getMainHandItem();
                if(heldstack.is(Items.TRIDENT)){
                    var cfg = ModConfig.get().trident;
                    TridentEnhanceComponent comp = heldstack.getOrDefault(
                            TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                            new TridentEnhanceComponent(0, 0, false, false, false));
                    int normal = comp.normal_count();
                    int superCnt = comp.super_count();
                    boolean is_adept = comp.is_adept();
                    boolean is_synchronized = comp.is_synchronized();
                    boolean is_soulbound = comp.is_soulbound();
                    int max_damage = heldstack.getOrDefault(DataComponents.MAX_DAMAGE, 0);

                    heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                            new TridentEnhanceComponent(++normal, superCnt, is_adept, is_synchronized, is_soulbound));

                    int newNormal = normal + 1;
                    boolean na = !is_adept && newNormal >= cfg.adeptNormal && superCnt >= cfg.adeptSuper;
                    boolean ns = !is_synchronized && newNormal >= cfg.syncNormal && superCnt >= cfg.syncSuper;
                    boolean nl = !is_soulbound && newNormal >= cfg.soulNormal && superCnt >= cfg.soulSuper;

                    if (!na && !ns && !nl) return InteractionResult.PASS;

                    out_sound(world, player);
                    String name = get_name(heldstack);

                    if (nl) {
                        heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                                new TridentEnhanceComponent(newNormal, superCnt, true, true, true));
                        heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.soulDurability));
                        heldstack.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "trident", "soulbound", name, "max_level", (int)(max_damage * cfg.soulDurability), cfg.soulDamageBonus * 100 + "%");
                    } else if (ns) {
                        heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                                new TridentEnhanceComponent(newNormal, superCnt, is_adept, true, is_soulbound));
                        heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.syncDurability));
                        heldstack.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "trident", "synchronized", name, (int)(max_damage * cfg.syncDurability), cfg.syncDamageBonus * 100 + "%");
                    } else {
                        heldstack.set(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT,
                                new TridentEnhanceComponent(newNormal, superCnt, true, is_synchronized, is_soulbound));
                        heldstack.set(DataComponents.MAX_DAMAGE, (int)(max_damage * cfg.adeptDurability));
                        heldstack.set(DataComponents.REPAIR_COST, 0);
                        upgrade_text(player, "trident", "adept", name, (int)(max_damage * cfg.adeptDurability), cfg.adeptDamageBonus * 100 + "%");
                    }

                    ItemAttributeModifiers current = heldstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                    ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                    copy_original_data(current, builder, TRIDENT_DAMAGE_ID);

                    double dmgMul = nl ? cfg.soulDamageBonus : ns ? cfg.syncDamageBonus : cfg.adeptDamageBonus;
                    builder.add(Attributes.ATTACK_DAMAGE,
                            new AttributeModifier(TRIDENT_DAMAGE_ID, dmgMul,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                            EquipmentSlotGroup.MAINHAND);
                    heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
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
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void upgrade_text(Player player,String type,String level,String name,String max_level,int max_damage,Object... bonusArgs){
        upgrade_text(player, type, level, name, max_damage, bonusArgs);
        player.sendSystemMessage(Component.translatable("item.some-interesting." + type + "." + level +"." + max_level));
    }

    public static void upgrade_text(Player player,String type,String level,String name,int max_damage,Object... bonusArgs){
        player.sendSystemMessage(Component.translatable("item.some-interesting." + type + "." + level +".text.info", name).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("item.some-interesting." + type + "." + level +".repair_reset"));
        player.sendSystemMessage(Component.translatable("item.some-interesting." + type + "." + level +".durability_up", "\u00a7a" + max_damage));
        Object[] colored = new Object[bonusArgs.length];
        for (int i = 0; i < bonusArgs.length; i++) colored[i] = "\u00a7a" + bonusArgs[i];
        player.sendSystemMessage(Component.translatable("item.some-interesting." + type + "." + level +".damage_up", colored));
        grantAdvancement(player, level);
    }

    private static void grantAdvancement(Player player, String level) {
        if (!(player instanceof ServerPlayer sp)) return;
        String adv = switch (level) {
            case "adept" -> "proficiency/adept";
            case "synchronized" -> "proficiency/synchronized";
            case "soulbound" -> "proficiency/soulbound";
            default -> null;
        };
        if (adv == null) return;
        AdvancementHolder holder = ((net.minecraft.server.level.ServerLevel) sp.level()).getServer()
                .getAdvancements().get(Identifier.fromNamespaceAndPath("some-interesting", adv));
        if (holder != null) sp.getAdvancements().award(holder, "impossible");
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
                    new AttributeModifier(id, multiple_value, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    EquipmentSlotGroup.MAINHAND);
            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        }else{
            builder.add(Attributes.MINING_EFFICIENCY,
                    new AttributeModifier(id, multiple_value, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
            heldstack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        }
    }

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

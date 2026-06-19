package com.chara.some_interesting;

import com.chara.some_interesting.component.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BindingStoneItem extends Item {

    public BindingStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack offHandStack = player.getOffhandItem();
        if (offHandStack.isEmpty()) {
            if (!world.isClientSide()) {
                player.sendSystemMessage(Component.translatable("item.some-interesting.binding_stone.fail_empty"));
            }
            return InteractionResult.FAIL;
        }

        if (!isSoulbound(offHandStack)) {
            if (!world.isClientSide()) {
                player.sendSystemMessage(Component.translatable("item.some-interesting.binding_stone.fail_not_soulbound"));
            }
            return InteractionResult.FAIL;
        }

        if (!world.isClientSide()) {
            String itemName = offHandStack.getHoverName().getString();
            player.sendSystemMessage(Component.translatable("item.some-interesting.binding_stone.success", itemName));

            player.getMainHandItem().shrink(1);

            world.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }

    public static boolean isSoulbound(ItemStack stack) {
        SwordsEnhanceComponent swords = stack.get(SwordsEnhanceComponent.SWORDS_PROFICIENCY_COMPONENT);
        if (swords != null && swords.is_soulbound()) return true;

        PickaxeEnhanceComponent pickaxe = stack.get(PickaxeEnhanceComponent.PICKAXE_PROFICIENCY_COMPONENT);
        if (pickaxe != null && pickaxe.is_soulbound()) return true;

        AxeEnhanceComponent axe = stack.get(AxeEnhanceComponent.AXE_PROFICIENCY_COMPONENT);
        if (axe != null && axe.is_soulbound()) return true;

        ShovelEnhanceComponent shovel = stack.get(ShovelEnhanceComponent.SHOVEL_PROFICIENCY_COMPONENT);
        if (shovel != null && shovel.is_soulbound()) return true;

        HoeEnhanceComponent hoe = stack.get(HoeEnhanceComponent.HOE_PROFICIENCY_COMPONENT);
        if (hoe != null && hoe.is_soulbound()) return true;

        BowEnhanceComponent bow = stack.get(BowEnhanceComponent.BOW_PROFICIENCY_COMPONENT);
        if (bow != null && bow.is_soulbound()) return true;

        CrossbowEnhanceComponent crossbow = stack.get(CrossbowEnhanceComponent.CROSSBOW_PROFICIENCY_COMPONENT);
        if (crossbow != null && crossbow.is_soulbound()) return true;

        ArmorEnhanceComponent armor = stack.get(ArmorEnhanceComponent.ARMOR_PROFICIENCY_COMPONENT);
        if (armor != null && armor.is_soulbound()) return true;

        ShieldEnhanceComponent shield = stack.get(ShieldEnhanceComponent.SHIELD_PROFICIENCY_COMPONENT);
        if (shield != null && shield.is_soulbound()) return true;

        TridentEnhanceComponent trident = stack.get(TridentEnhanceComponent.TRIDENT_PROFICIENCY_COMPONENT);
        if (trident != null && trident.is_soulbound()) return true;

        MaceEnhanceComponent mace = stack.get(MaceEnhanceComponent.MACE_PROFICIENCY_COMPONENT);
        if (mace != null && mace.is_soulbound()) return true;

        SpearEnhanceComponent spear = stack.get(SpearEnhanceComponent.SPEAR_PROFICIENCY_COMPONENT);
        if (spear != null && spear.is_soulbound()) return true;

        FishingRodEnhanceComponent fishing = stack.get(FishingRodEnhanceComponent.FISHING_PROFICIENCY_COMPONENT);
        if (fishing != null && fishing.is_soulbound()) return true;

        ShearsEnhanceComponent shears = stack.get(ShearsEnhanceComponent.SHEARS_PROFICIENCY_COMPONENT);
        if (shears != null && shears.is_soulbound()) return true;

        FlintAndSteelEnhanceComponent fs = stack.get(FlintAndSteelEnhanceComponent.FS_PROFICIENCY_COMPONENT);
        if (fs != null && fs.is_soulbound()) return true;

        ElytraEnhanceComponent elytra = stack.get(ElytraEnhanceComponent.ELYTRA_PROFICIENCY_COMPONENT);
        if (elytra != null && elytra.is_soulbound()) return true;

        return false;
    }
}

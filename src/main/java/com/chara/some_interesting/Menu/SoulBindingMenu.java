package com.chara.some_interesting.Menu;

import com.chara.some_interesting.BoundItemStorage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SoulBindingMenu extends AbstractContainerMenu {

    private static final int GRID_SIZE = 9;
    private static final int RESULT_IDX = 9;
    private static final int INV_START = 10;
    private static final int INV_END = INV_START + 36;

    private static final int GRID_X = 190, GRID_Y = 18;
    private static final int RESULT_X = 270, RESULT_Y = 36;
    private static final int SLOT = 18;
    private static final int INV_X = 89, INV_Y = 148, HOTBAR_Y = 206;

    private final TransientCraftingContainer craftSlots;
    private final ResultContainer resultSlots = new ResultContainer();
    private final Player player;
    private ItemStack selectedBoundItem = ItemStack.EMPTY;
    private int selectedIndex = -1;
    private String playerKey = "";

    public SoulBindingMenu(int containerId, Inventory playerInventory) {
        super(ModMenuType.SOUL_BINDING_MENU_TYPE, containerId);
        this.player = playerInventory.player;
        this.craftSlots = new TransientCraftingContainer(this, 3, 3);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(craftSlots, col + row * 3,
                        GRID_X + col * SLOT, GRID_Y + row * SLOT));
            }
        }

        this.addSlot(new ResultSlot(player, craftSlots, resultSlots, 0, RESULT_X, RESULT_Y) {
            @Override
            public void onTake(Player p, ItemStack stack) {
                if (!playerKey.isEmpty() && selectedIndex >= 0) {
                    BoundItemStorage.get().setCooldown(playerKey, selectedIndex);
                }
                super.onTake(p, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        INV_X + col * SLOT, INV_Y + row * SLOT));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, INV_X + col * SLOT, HOTBAR_Y));
        }
    }

    public void setSelectedBoundItem(ItemStack stack, int index, String playerKey) {
        this.selectedBoundItem = stack.copy();
        this.selectedIndex = index;
        this.playerKey = playerKey;
        slotsChanged(craftSlots);
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != craftSlots) {
            super.slotsChanged(container);
            return;
        }

        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (selectedBoundItem.isEmpty()
                || BoundItemStorage.get().isOnCooldown(playerKey, selectedIndex)) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            resultSlots.setRecipeUsed(null);
            return;
        }

        CraftingInput input = CraftingInput.of(3, 3, craftSlots.getItems());
        Optional<RecipeHolder<CraftingRecipe>> recipe =
                serverLevel.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);

        if (recipe.isPresent()) {
            ItemStack recipeResult = recipe.get().value().assemble(input);
            if (recipeResult.is(selectedBoundItem.getItem())) {
                ItemStack output = selectedBoundItem.copy();
                output.set(DataComponents.DAMAGE, 0);
                resultSlots.setItem(0, output);
                resultSlots.setRecipeUsed(recipe.get());
                return;
            }
        }
        resultSlots.setItem(0, ItemStack.EMPTY);
        resultSlots.setRecipeUsed(null);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == RESULT_IDX) {
            if (!moveItemStackTo(stack, INV_START, INV_END, true))
                return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (index >= INV_START) {
            if (!moveItemStackTo(stack, 0, GRID_SIZE, false))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, INV_START, INV_END, false))
                return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, craftSlots);
    }
}

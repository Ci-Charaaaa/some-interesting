package com.chara.some_interesting.Menu;

import com.chara.some_interesting.ModBlockEntities.UpgradeForgeTableEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class UpgradeForgeTableMenu extends AbstractContainerMenu {

    // ====== 配方表 ======
    private static final List<ForgeRecipe> RECIPES = List.of(
            // 木 → 石
            sword(Items.WOODEN_SWORD, Items.COBBLESTONE, Items.STONE_SWORD),
            // 石 → 金/铜/铁
            sword(Items.STONE_SWORD, Items.GOLD_INGOT, Items.GOLDEN_SWORD),
            sword(Items.STONE_SWORD, Items.COPPER_INGOT, Items.COPPER_SWORD),
            sword(Items.STONE_SWORD, Items.IRON_INGOT, Items.IRON_SWORD),
            // 金 → 铜
            sword(Items.GOLDEN_SWORD, Items.COPPER_INGOT, Items.COPPER_SWORD),
            // 铜 → 铁
            sword(Items.COPPER_SWORD, Items.IRON_INGOT, Items.IRON_SWORD),
            // 铁 → 钻
            sword(Items.IRON_SWORD, Items.DIAMOND, Items.DIAMOND_SWORD),
            // 钻 → 下界合金
            sword(Items.DIAMOND_SWORD, Items.NETHERITE_INGOT, Items.NETHERITE_SWORD)
    );

    private static ForgeRecipe sword(Item from, Item material, Item to) {
        return new ForgeRecipe(
                t -> t.is(from),
                List.of(
                        new ForgeRecipe.GridPos(1, i -> i.is(material)),
                        new ForgeRecipe.GridPos(4, i -> i.is(material))
                ),
                to
        );
    }

    // ====== 槽位索引 ======
    private static final int MATERIAL_START = 0;
    private static final int MATERIAL_END   = 9;
    private static final int TOOL_SLOT_IDX   = 9;
    private static final int RESULT_SLOT_IDX  = 10;
    private static final int CONTAINER_SIZE   = 11;
    private static final int INVENTORY_START = CONTAINER_SIZE;
    private static final int INVENTORY_END   = INVENTORY_START + 3 * 9 + 9;

    // ====== 坐标 ======
    private static final int TOOL_X = 8, TOOL_Y = 34;
    private static final int MAT_X = 30, MAT_Y = 17;
    private static final int SLOT = 18;
    private static final int RESULT_X = 124, RESULT_Y = 35;
    private static final int INV_X = 8, INV_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Container container;

    public UpgradeForgeTableMenu(final int containerId, final Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(CONTAINER_SIZE));
    }

    public UpgradeForgeTableMenu(final int containerId, final Inventory inventory, final Container container) {
        super(ModMenuType.UPGRADE_FORGE_TABLE_MENU_TYPE, containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        container.startOpen(inventory.player);

        // 材料 3×3（变更时刷新配方）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int idx = col + row * 3;
                int sx = MAT_X + col * SLOT, sy = MAT_Y + row * SLOT;
                this.addSlot(new Slot(container, idx, sx, sy) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        UpgradeForgeTableMenu.this.slotsChanged(container);
                    }
                });
            }
        }

        // 工具槽（变更时刷新配方）
        this.addSlot(new Slot(container, TOOL_SLOT_IDX, TOOL_X, TOOL_Y) {
            @Override
            public void setChanged() {
                super.setChanged();
                UpgradeForgeTableMenu.this.slotsChanged(container);
            }
        });

        // 结果槽
        this.addSlot(new Slot(container, RESULT_SLOT_IDX, RESULT_X, RESULT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void onTake(Player player, ItemStack resultStack) {
                super.onTake(player, resultStack);
                for (int i = MATERIAL_START; i < MATERIAL_END; i++) {
                    ItemStack mat = container.getItem(i);
                    if (!mat.isEmpty()) mat.shrink(1);
                }
                container.getItem(TOOL_SLOT_IDX).shrink(1);
                UpgradeForgeTableMenu.this.slotsChanged(container);
            }
        });

        // 玩家主背包 3×9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9,
                        INV_X + col * SLOT, INV_Y + row * SLOT));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, INV_X + col * SLOT, HOTBAR_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex == RESULT_SLOT_IDX) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true))
                return ItemStack.EMPTY;
            slot.onQuickCraft(original, stack);
        }
        else if (slotIndex < INVENTORY_START) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false))
                return ItemStack.EMPTY;
        }
        else {
            if (!moveItemStackTo(stack, TOOL_SLOT_IDX, TOOL_SLOT_IDX + 1, false)) {
                if (!moveItemStackTo(stack, MATERIAL_START, MATERIAL_END, false))
                    return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    // ====== 配方更新 ======
    @Override
    public void slotsChanged(Container inv) {
        if (inv != container) { super.slotsChanged(inv); return; }
        updateResult();
    }

    private List<ItemStack> getGridItems() {
        List<ItemStack> items = new ArrayList<>(9);
        for (int i = MATERIAL_START; i < MATERIAL_END; i++) {
            items.add(container.getItem(i));
        }
        return items;
    }

    private void updateResult() {
        ItemStack tool = container.getItem(TOOL_SLOT_IDX);
        if (tool.isEmpty()) {
            container.setItem(RESULT_SLOT_IDX, ItemStack.EMPTY);
            return;
        }

        List<ItemStack> grid = getGridItems();
        for (ForgeRecipe recipe : RECIPES) {
            if (recipe.matches(tool, grid)) {
                container.setItem(RESULT_SLOT_IDX, recipe.buildResult(tool));
                return;
            }
        }
        container.setItem(RESULT_SLOT_IDX, ItemStack.EMPTY);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}

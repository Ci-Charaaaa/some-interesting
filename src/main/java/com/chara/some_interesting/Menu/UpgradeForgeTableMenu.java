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

import static net.minecraft.world.item.Items.*;

public class UpgradeForgeTableMenu extends AbstractContainerMenu {

    // 各工具的材料槽位（3×3 索引: row0=0,1,2  row1=3,4,5  row2=6,7,8）
    // 剑: 中中+上中    镐: 上左+上中+上右  斧: 上左+上中+中左  铲: 上中  锄: 上左+上中
    private static final int[] SWORD_SLOTS  = {1, 4};
    private static final int[] PICKAXE_SLOTS = {0, 1, 2};
    private static final int[] AXE_SLOTS    = {0, 1, 3};
    private static final int[] SHOVEL_SLOTS = {1};
    private static final int[] HOE_SLOTS    = {0, 1};
    // 长矛: 上右放矿物，中中+下左放木棍
    private static final int[] SPEAR_SLOTS  = {2};

    // 各装备的材料槽位（匹配合成表）
    private static final int[] HELMET_SLOTS    = {0, 1, 2, 3, 5};
    // 头盔/靴子下半部分配方（中排+下排）
    private static final int[] HELMET_BOTTOM   = {3, 4, 5, 6, 8};
    private static final int[] CHESTPLATE_SLOTS = {0, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] LEGGINGS_SLOTS   = {0, 1, 2, 3, 5, 6, 8};
    private static final int[] BOOTS_SLOTS      = {0, 2, 3, 5};
    private static final int[] BOOTS_BOTTOM     = {3, 5, 6, 8};

    // ====== 配方表 ======
    private static final List<ForgeRecipe> RECIPES = createRecipes();

    private static List<ForgeRecipe> createRecipes() {
        List<ForgeRecipe> list = new ArrayList<>();
        addToolLine(list, SWORD_SLOTS, WOODEN_SWORD,COBBLESTONE, STONE_SWORD, GOLD_INGOT, GOLDEN_SWORD,
                COPPER_INGOT, COPPER_SWORD, IRON_INGOT, IRON_SWORD, DIAMOND, DIAMOND_SWORD, NETHERITE_INGOT, NETHERITE_SWORD);
        addToolLine(list, PICKAXE_SLOTS, WOODEN_PICKAXE,COBBLESTONE, STONE_PICKAXE, GOLD_INGOT, GOLDEN_PICKAXE,
                COPPER_INGOT, COPPER_PICKAXE, IRON_INGOT, IRON_PICKAXE, DIAMOND, DIAMOND_PICKAXE, NETHERITE_INGOT, NETHERITE_PICKAXE);
        addToolLine(list, AXE_SLOTS, WOODEN_AXE,COBBLESTONE, STONE_AXE, GOLD_INGOT, GOLDEN_AXE,
                COPPER_INGOT, COPPER_AXE, IRON_INGOT, IRON_AXE, DIAMOND, DIAMOND_AXE, NETHERITE_INGOT, NETHERITE_AXE);
        addToolLine(list, SHOVEL_SLOTS, WOODEN_SHOVEL,COBBLESTONE, STONE_SHOVEL, GOLD_INGOT, GOLDEN_SHOVEL,
                COPPER_INGOT, COPPER_SHOVEL, IRON_INGOT, IRON_SHOVEL, DIAMOND, DIAMOND_SHOVEL, NETHERITE_INGOT, NETHERITE_SHOVEL);
        addToolLine(list, HOE_SLOTS, WOODEN_HOE,COBBLESTONE, STONE_HOE, GOLD_INGOT, GOLDEN_HOE,
                COPPER_INGOT, COPPER_HOE, IRON_INGOT, IRON_HOE, DIAMOND, DIAMOND_HOE, NETHERITE_INGOT, NETHERITE_HOE);
        // 长矛（上右放矿物）
        addToolLine(list, SPEAR_SLOTS, WOODEN_SPEAR,COBBLESTONE, STONE_SPEAR, GOLD_INGOT, GOLDEN_SPEAR,
                COPPER_INGOT, COPPER_SPEAR, IRON_INGOT, IRON_SPEAR, DIAMOND, DIAMOND_SPEAR, NETHERITE_INGOT, NETHERITE_SPEAR);
        // 盔甲
        addArmorLine(list, HELMET_SLOTS, LEATHER_HELMET, COPPER_HELMET, IRON_HELMET, GOLDEN_HELMET, DIAMOND_HELMET, NETHERITE_HELMET);
        addArmorLine(list, HELMET_BOTTOM, LEATHER_HELMET, COPPER_HELMET, IRON_HELMET, GOLDEN_HELMET, DIAMOND_HELMET, NETHERITE_HELMET);
        addArmorLine(list, CHESTPLATE_SLOTS, LEATHER_CHESTPLATE, COPPER_CHESTPLATE, IRON_CHESTPLATE, GOLDEN_CHESTPLATE, DIAMOND_CHESTPLATE, NETHERITE_CHESTPLATE);
        addArmorLine(list, LEGGINGS_SLOTS, LEATHER_LEGGINGS, COPPER_LEGGINGS, IRON_LEGGINGS, GOLDEN_LEGGINGS, DIAMOND_LEGGINGS, NETHERITE_LEGGINGS);
        addArmorLine(list, BOOTS_SLOTS, LEATHER_BOOTS, COPPER_BOOTS, IRON_BOOTS, GOLDEN_BOOTS, DIAMOND_BOOTS, NETHERITE_BOOTS);
        addArmorLine(list, BOOTS_BOTTOM, LEATHER_BOOTS, COPPER_BOOTS, IRON_BOOTS, GOLDEN_BOOTS, DIAMOND_BOOTS, NETHERITE_BOOTS);
        return List.copyOf(list);
    }

    private static void addArmorLine(List<ForgeRecipe> list, int[] slots,
            Item leather, Item copper, Item iron, Item golden, Item diamond, Item netherite) {
        // 皮革 → 金/铜/铁
        list.add(forge(slots, leather, GOLD_INGOT, golden));
        list.add(forge(slots, leather, COPPER_INGOT, copper));
        list.add(forge(slots, leather, IRON_INGOT, iron));
        // 金 → 铜
        list.add(forge(slots, golden, COPPER_INGOT, copper));
        // 铜 → 铁
        list.add(forge(slots, copper, IRON_INGOT, iron));
        // 铁 → 钻
        list.add(forge(slots, iron, DIAMOND, diamond));
        // 钻 → 下界合金
        list.add(forge(slots, diamond, NETHERITE_INGOT, netherite));
    }

    private static void addToolLine(List<ForgeRecipe> list, int[] slots,
            Item wood, Item cobble, Item stone,
            Item goldIngot, Item golden,
            Item copperIngot, Item copper,
            Item ironIngot, Item iron,
            Item diamond, Item diamondTool,
            Item netherite, Item netheriteTool) {
        list.add(forge(slots, wood, cobble, stone));
        list.add(forge(slots, stone, goldIngot, golden));
        list.add(forge(slots, stone, copperIngot, copper));
        list.add(forge(slots, stone, ironIngot, iron));
        list.add(forge(slots, golden, copperIngot, copper));
        list.add(forge(slots, copper, ironIngot, iron));
        list.add(forge(slots, iron, diamond, diamondTool));
        list.add(forge(slots, diamondTool, netherite, netheriteTool));
    }

    private static ForgeRecipe forge(int[] slots, Item from, Item material, Item to) {
        List<ForgeRecipe.GridPos> pattern = new ArrayList<>();
        for (int s : slots) {
            pattern.add(new ForgeRecipe.GridPos(s, i -> i.is(material)));
        }
        return new ForgeRecipe(t -> t.is(from), pattern, to);
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
                // 消耗所有材料（每个非空格子 -1）
                for (int i = MATERIAL_START; i < MATERIAL_END; i++) {
                    ItemStack mat = container.getItem(i);
                    if (!mat.isEmpty()) {
                        mat.shrink(1);
                        if (mat.isEmpty()) container.setItem(i, ItemStack.EMPTY);
                    }
                }
                // 消耗工具
                ItemStack tool = container.getItem(TOOL_SLOT_IDX);
                if (!tool.isEmpty()) {
                    tool.shrink(1);
                    if (tool.isEmpty()) container.setItem(TOOL_SLOT_IDX, ItemStack.EMPTY);
                }
                // 清空结果槽并刷新
                container.setItem(RESULT_SLOT_IDX, ItemStack.EMPTY);
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
            // 消耗材料 + 工具（同 onTake）
            for (int i = MATERIAL_START; i < MATERIAL_END; i++) {
                ItemStack mat = container.getItem(i);
                if (!mat.isEmpty()) {
                    mat.shrink(1);
                    if (mat.isEmpty()) container.setItem(i, ItemStack.EMPTY);
                }
            }
            ItemStack tool = container.getItem(TOOL_SLOT_IDX);
            if (!tool.isEmpty()) {
                tool.shrink(1);
                if (tool.isEmpty()) container.setItem(TOOL_SLOT_IDX, ItemStack.EMPTY);
            }
            slot.onQuickCraft(original, stack);
            slotsChanged(container);
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

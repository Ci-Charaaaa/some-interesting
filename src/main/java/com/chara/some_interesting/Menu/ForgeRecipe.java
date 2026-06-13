package com.chara.some_interesting.Menu;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record ForgeRecipe(
        Predicate<ItemStack> toolPredicate,
        List<GridPos> grid,
        Item outputItem
) {
    public record GridPos(int gridIndex, Predicate<ItemStack> ingredient) {}

    public boolean matches(ItemStack tool, List<ItemStack> gridItems) {
        if (!toolPredicate.test(tool)) return false;

        //要求的所有材料必须在规定位置上
        for (GridPos pos : grid) {
            ItemStack s = gridItems.get(pos.gridIndex);
            if (s.isEmpty() || !pos.ingredient.test(s)) return false;
        }

        //非材料槽位必须为空
        Set<Integer> used = grid.stream().map(GridPos::gridIndex).collect(Collectors.toSet());
        for (int i = 0; i < 9; i++) {
            if (!used.contains(i) && !gridItems.get(i).isEmpty())
                return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public ItemStack buildResult(ItemStack tool) {
        ItemStack result = new ItemStack(outputItem);

        Component defaultName = tool.getPrototype().get(DataComponents.ITEM_NAME);

        for (TypedDataComponent<?> c : tool.getComponents()) {
            DataComponentType<?> type = c.type();
            if (type == DataComponents.DAMAGE
                    || type == DataComponents.MAX_DAMAGE
                    || type == DataComponents.ATTRIBUTE_MODIFIERS
                    || type == DataComponents.TOOL
                    || type == DataComponents.ITEM_MODEL)
                continue;

            if (type == DataComponents.ITEM_NAME) {
                Component name = (Component) c.value();
                if (name == null || name.equals(defaultName)) continue;
            }

            result.set((DataComponentType<Object>) type, c.value());
        }
        return result;
    }
}

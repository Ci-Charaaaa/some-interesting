package com.chara.some_interesting.Menu;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public record ForgeRecipe(
        Predicate<ItemStack> toolPredicate,
        List<GridPos> grid,
        Item outputItem
) {
    public record GridPos(int gridIndex, Predicate<ItemStack> ingredient) {}

    public boolean matches(ItemStack tool, List<ItemStack> gridItems) {
        if (!toolPredicate.test(tool)) return false;
        for (GridPos pos : grid) {
            ItemStack s = gridItems.get(pos.gridIndex);
            if (s.isEmpty() || !pos.ingredient.test(s)) return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public ItemStack buildResult(ItemStack tool) {
        ItemStack result = new ItemStack(outputItem);

        // 工具的原型默认组件——用来判断 ITEM_NAME 是否为玩家自定义名
        Component defaultName = tool.getPrototype().get(DataComponents.ITEM_NAME);

        for (TypedDataComponent<?> c : tool.getComponents()) {
            DataComponentType<?> type = c.type();
            // 跳过基础属性组件
            if (type == DataComponents.DAMAGE
                    || type == DataComponents.MAX_DAMAGE
                    || type == DataComponents.ATTRIBUTE_MODIFIERS
                    || type == DataComponents.TOOL
                    || type == DataComponents.ITEM_MODEL)
                continue;

            // ITEM_NAME：只有被玩家改过名才保留，否则用升级后物品的默认名
            if (type == DataComponents.ITEM_NAME) {
                Component name = (Component) c.value();
                if (name == null || name.equals(defaultName)) continue;
            }

            result.set((DataComponentType<Object>) type, c.value());
        }
        return result;
    }
}

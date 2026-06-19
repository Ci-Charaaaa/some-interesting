package com.chara.some_interesting.client;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClientBoundItemData {

    private static List<ItemStack> boundItems = new ArrayList<>();

    public static void setBoundItems(List<ItemStack> items) {
        boundItems = new ArrayList<>(items);
    }

    public static List<ItemStack> getBoundItems() {
        return boundItems;
    }
}

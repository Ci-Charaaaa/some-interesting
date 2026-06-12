package com.chara.some_interesting.Menu;

import com.chara.some_interesting.SomeInteresting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ModMenuType {
    public static final MenuType<UpgradeForgeTableMenu> UPGRADE_FORGE_TABLE_MENU_TYPE = register("upgrade_forge_table", UpgradeForgeTableMenu::new);

    public static <T extends AbstractContainerMenu> MenuType<T> register(
            String name,
            MenuType.MenuSupplier<T> constructor
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, name);
        return Registry.register(BuiltInRegistries.MENU, id, new MenuType<>(constructor, FeatureFlagSet.of()));
    }
}

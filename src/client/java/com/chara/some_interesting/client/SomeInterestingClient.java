package com.chara.some_interesting.client;

import com.chara.some_interesting.Menu.ModMenuType;
import com.chara.some_interesting.Screens.UpgradeForgeTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class SomeInterestingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuType.UPGRADE_FORGE_TABLE_MENU_TYPE, UpgradeForgeTableScreen::new);
    }
}

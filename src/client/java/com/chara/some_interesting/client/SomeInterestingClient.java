package com.chara.some_interesting.client;

import com.chara.some_interesting.BoundItemsSyncPayload;
import com.chara.some_interesting.Menu.ModMenuType;
import com.chara.some_interesting.Screens.SoulBindingScreen;
import com.chara.some_interesting.Screens.UpgradeForgeTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public class SomeInterestingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuType.UPGRADE_FORGE_TABLE_MENU_TYPE, UpgradeForgeTableScreen::new);
        MenuScreens.register(ModMenuType.SOUL_BINDING_MENU_TYPE, SoulBindingScreen::new);
        ModKeyBindings.initialize();

        ClientPlayNetworking.registerGlobalReceiver(BoundItemsSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> ClientBoundItemData.setBoundItems(payload.items()));
        });
    }
}

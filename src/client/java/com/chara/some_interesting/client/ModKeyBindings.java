package com.chara.some_interesting.client;

import com.chara.some_interesting.OpenSoulBindingPayload;
import com.chara.some_interesting.SomeInteresting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    private static final KeyMapping.Category MOD_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "keybinds")
    );

    private static final KeyMapping OPEN_BLANK_SCREEN = new KeyMapping(
            "key.some-interesting.open_blank_screen",
            GLFW.GLFW_KEY_K,
            MOD_CATEGORY
    );

    public static KeyMapping getOpenBlankScreenKey() {
        return OPEN_BLANK_SCREEN;
    }

    public static void initialize() {
        KeyMappingHelper.registerKeyMapping(OPEN_BLANK_SCREEN);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_BLANK_SCREEN.consumeClick()) {
                if (client.screen == null && client.getConnection() != null) {
                    ClientPlayNetworking.send(new OpenSoulBindingPayload());
                }
            }
        });
    }
}

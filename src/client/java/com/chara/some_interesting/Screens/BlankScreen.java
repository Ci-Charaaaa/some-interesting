package com.chara.some_interesting.Screens;

import com.chara.some_interesting.client.ModKeyBindings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class BlankScreen extends Screen {

    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 150;

    public BlankScreen() {
        super(Component.translatable("screen.some-interesting.blank"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (ModKeyBindings.getOpenBlankScreenKey().matches(keyEvent)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xC0101010);
        graphics.fill(x, y, x + PANEL_WIDTH, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y + PANEL_HEIGHT - 1, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.fill(x + PANEL_WIDTH - 1, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.centeredText(this.font, this.title, this.width / 2, y + 6, 0xFFFFFF);
    }
}

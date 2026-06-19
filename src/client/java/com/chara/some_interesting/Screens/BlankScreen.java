package com.chara.some_interesting.Screens;

import com.chara.some_interesting.client.ClientBoundItemData;
import com.chara.some_interesting.client.ModKeyBindings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BlankScreen extends Screen {

    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 200;
    private static final int ITEM_ROW_HEIGHT = 20;
    private static final int PADDING = 8;

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
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xC0101010);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, 0xFFFFFFFF);
        graphics.fill(panelX, panelY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.fill(panelX, panelY, panelX + 1, panelY + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFFFFF);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        graphics.centeredText(this.font, this.title, this.width / 2, panelY + 6, 0xFFFFFFFF);

        List<ItemStack> items = ClientBoundItemData.getBoundItems();
        int startY = panelY + 20;
        int maxItems = (PANEL_HEIGHT - 28) / ITEM_ROW_HEIGHT;

        if (items.isEmpty()) {
            graphics.centeredText(this.font,
                    Component.translatable("screen.some-interesting.blank.empty"),
                    this.width / 2, panelY + PANEL_HEIGHT / 2 - 4, 0xFF888888);
            return;
        }

        for (int i = 0; i < Math.min(items.size(), maxItems); i++) {
            ItemStack stack = items.get(i);
            int itemX = panelX + PADDING;
            int itemY = startY + i * ITEM_ROW_HEIGHT;

            graphics.fill(itemX - 1, itemY - 1, itemX + 17, itemY + 17, 0x40FFFFFF);

            graphics.item(stack, itemX, itemY);
            graphics.itemDecorations(this.font, stack, itemX, itemY);

            graphics.text(this.font, stack.getHoverName(), itemX + 22, itemY + 4, 0xFFFFFFFF);

            if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
                graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
            }
        }

        if (items.size() > maxItems) {
            graphics.centeredText(this.font,
                    Component.literal("... +" + (items.size() - maxItems)),
                    this.width / 2, startY + maxItems * ITEM_ROW_HEIGHT + 2, 0xFF888888);
        }
    }
}

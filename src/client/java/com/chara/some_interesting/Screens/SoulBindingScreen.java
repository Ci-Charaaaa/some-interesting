package com.chara.some_interesting.Screens;

import com.chara.some_interesting.Menu.SoulBindingMenu;
import com.chara.some_interesting.SelectBoundItemPayload;
import com.chara.some_interesting.client.ClientBoundItemData;
import com.chara.some_interesting.client.ModKeyBindings;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SoulBindingScreen extends AbstractContainerScreen<SoulBindingMenu> {

    private static final int ITEM_ROW_HEIGHT = 20;
    private static final int SLOT = 18;
    private static final int GRID_X = 190, GRID_Y = 18;
    private static final int RESULT_X = 270, RESULT_Y = 36;
    private static final int INV_X = 89, INV_Y = 148, HOTBAR_Y = 206;

    private int selectedIndex = -1;

    public SoulBindingScreen(SoulBindingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 340, 230);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = INV_X;
        this.inventoryLabelY = 136;
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
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        List<ItemStack> items = ClientBoundItemData.getBoundItems();
        int maxItems = Math.min(items.size(), 5);
        int startY = this.topPos + 20;

        for (int i = 0; i < maxItems; i++) {
            int itemX = this.leftPos + 8;
            int itemY = startY + i * ITEM_ROW_HEIGHT;
            if (event.x() >= itemX && event.x() < itemX + 170
                    && event.y() >= itemY && event.y() < itemY + 18) {
                selectedIndex = i;
                ClientPlayNetworking.send(new SelectBoundItemPayload(i));
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    private void drawSlotBg(GuiGraphicsExtractor g, int sx, int sy) {
        g.fill(sx, sy, sx + SLOT, sy + SLOT, 0xFF1A1A1A);
        g.fill(sx, sy, sx + SLOT, sy + 1, 0xFF505050);
        g.fill(sx, sy + SLOT - 1, sx + SLOT, sy + SLOT, 0xFF505050);
        g.fill(sx, sy, sx + 1, sy + SLOT, 0xFF505050);
        g.fill(sx + SLOT - 1, sy, sx + SLOT, sy + SLOT, 0xFF505050);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        graphics.fill(x, y, x + w, y + h, 0xC0101010);
        graphics.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);

        graphics.fill(x + 180, y + 1, x + 181, y + 132, 0x60FFFFFF);
        graphics.fill(x + 1, y + 132, x + w - 1, y + 133, 0x60FFFFFF);
        graphics.fill(x + INV_X - 2, y + 200, x + INV_X + 9 * SLOT + 2, y + 201, 0x40FFFFFF);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawSlotBg(graphics, x + GRID_X + col * SLOT, y + GRID_Y + row * SLOT);
            }
        }
        drawSlotBg(graphics, x + RESULT_X, y + RESULT_Y);

        int arrowX = x + 248;
        int arrowY = y + 38;
        graphics.fill(arrowX, arrowY, arrowX + 14, arrowY + 1, 0xFFAAAAAA);
        graphics.fill(arrowX, arrowY + 1, arrowX + 14, arrowY + 2, 0xFFAAAAAA);
        graphics.fill(arrowX + 10, arrowY - 3, arrowX + 14, arrowY + 5, 0xFFAAAAAA);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBg(graphics, x + INV_X + col * SLOT, y + INV_Y + row * SLOT);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotBg(graphics, x + INV_X + col * SLOT, y + HOTBAR_Y);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int x = this.leftPos;
        int y = this.topPos;

        graphics.text(this.font, Component.translatable("screen.some-interesting.blank"),
                x + 8, y + 6, 0xFFFFFFFF);
        graphics.text(this.font, Component.translatable("screen.some-interesting.crafting"),
                x + 190, y + 6, 0xFFFFFFFF);
        graphics.text(this.font, Component.translatable("container.inventory"),
                x + INV_X, y + 136, 0xFFAAAAAA);

        List<ItemStack> items = ClientBoundItemData.getBoundItems();
        int startY = y + 20;
        int maxItems = 5;

        if (items.isEmpty()) {
            graphics.text(this.font,
                    Component.translatable("screen.some-interesting.blank.empty"),
                    x + 30, y + 70, 0xFF888888);
        } else {
            for (int i = 0; i < Math.min(items.size(), maxItems); i++) {
                ItemStack stack = items.get(i);
                int itemX = x + 8;
                int itemY = startY + i * ITEM_ROW_HEIGHT;

                if (i == selectedIndex) {
                    graphics.fill(itemX - 2, itemY - 2, itemX + 170, itemY + 18, 0x40FFD700);
                    graphics.fill(itemX - 2, itemY - 2, itemX + 170, itemY - 1, 0xFFFFD700);
                    graphics.fill(itemX - 2, itemY + 17, itemX + 170, itemY + 18, 0xFFFFD700);
                    graphics.fill(itemX - 2, itemY - 2, itemX - 1, itemY + 18, 0xFFFFD700);
                    graphics.fill(itemX + 169, itemY - 2, itemX + 170, itemY + 18, 0xFFFFD700);
                } else {
                    graphics.fill(itemX - 1, itemY - 1, itemX + 17, itemY + 17, 0x40FFFFFF);
                }

                graphics.item(stack, itemX, itemY);
                graphics.itemDecorations(this.font, stack, itemX, itemY);
                graphics.text(this.font, stack.getHoverName(), itemX + 22, itemY + 4, 0xFFFFFFFF);

                if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
                    graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
                }
            }

            if (items.size() > maxItems) {
                graphics.text(this.font,
                        Component.literal("... +" + (items.size() - maxItems)),
                        x + 60, startY + maxItems * ITEM_ROW_HEIGHT + 2, 0xFF888888);
            }
        }

        if (selectedIndex == -1 && !items.isEmpty()) {
            graphics.text(this.font,
                    Component.translatable("screen.some-interesting.select_hint"),
                    x + 190, y + 80, 0xFF888888);
        }
    }
}

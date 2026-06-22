package com.chara.some_interesting.Screens;

import com.chara.some_interesting.BindingStoneItem;
import com.chara.some_interesting.Menu.SoulBindingMenu;
import com.chara.some_interesting.SelectBoundItemPayload;
import com.chara.some_interesting.ToggleFavoritePayload;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SoulBindingScreen extends AbstractContainerScreen<SoulBindingMenu> {

    private static final int ITEM_ROW_HEIGHT = 28;
    private static final int SLOT = 18;
    private static final int GRID_X = 190, GRID_Y = 18;
    private static final int RESULT_X = 270, RESULT_Y = 36;
    private static final int INV_X = 89, INV_Y = 148, HOTBAR_Y = 206;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int selectedIndex = -1;
    private int scrollOffset = 0;

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

    private int[] buildDisplayOrder(List<ItemStack> items) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) indices.add(i);
        indices.sort((a, b) -> {
            boolean fa = items.get(a).getOrDefault(BindingStoneItem.FAVORITED, false);
            boolean fb = items.get(b).getOrDefault(BindingStoneItem.FAVORITED, false);
            if (fa && !fb) return -1;
            if (!fa && fb) return 1;
            return Integer.compare(a, b);
        });
        int[] order = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) order[i] = indices.get(i);
        return order;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        List<ItemStack> items = ClientBoundItemData.getBoundItems();
        if (items.isEmpty()) return super.mouseClicked(event, bl);
        int[] order = buildDisplayOrder(items);
        int scrollMax = Math.max(0, order.length - 4);
        int visibleCount = Math.min(order.length - scrollOffset, 4);
        int startY = this.topPos + 20;

        for (int i = 0; i < visibleCount; i++) {
            int originalIndex = order[scrollOffset + i];
            int itemX = this.leftPos + 8;
            int itemY = startY + i * ITEM_ROW_HEIGHT;
            if (event.x() >= itemX && event.x() < itemX + 170
                    && event.y() >= itemY && event.y() < itemY + ITEM_ROW_HEIGHT - 2) {
                if (event.button() == 1) {
                    ClientPlayNetworking.send(new ToggleFavoritePayload(originalIndex));
                    return true;
                }
                selectedIndex = originalIndex;
                ClientPlayNetworking.send(new SelectBoundItemPayload(originalIndex));
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelLeft = this.leftPos;
        int panelRight = this.leftPos + 180;
        int panelTop = this.topPos;
        int panelBottom = this.topPos + 132;

        if (mouseX >= panelLeft && mouseX < panelRight && mouseY >= panelTop && mouseY < panelBottom) {
            int maxOffset = Math.max(0, ClientBoundItemData.getBoundItems().size() - 4);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) verticalAmount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void drawSlotBg(GuiGraphicsExtractor g, int sx, int sy) {
        g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF1A1A1A);
        g.fill(sx - 1, sy - 1, sx + 17, sy, 0xFF505050);
        g.fill(sx - 1, sy + 16, sx + 17, sy + 17, 0xFF505050);
        g.fill(sx - 1, sy - 1, sx, sy + 17, 0xFF505050);
        g.fill(sx + 16, sy - 1, sx + 17, sy + 17, 0xFF505050);
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
        int arrowY = y + 44;
        graphics.fill(arrowX, arrowY, arrowX + 8, arrowY + 2, 0xFFAAAAAA);
        graphics.fill(arrowX + 7, arrowY - 3, arrowX + 9, arrowY + 5, 0xFFAAAAAA);
        graphics.fill(arrowX + 9, arrowY - 2, arrowX + 11, arrowY + 4, 0xFFAAAAAA);
        graphics.fill(arrowX + 11, arrowY - 1, arrowX + 13, arrowY + 3, 0xFFAAAAAA);
        graphics.fill(arrowX + 13, arrowY, arrowX + 14, arrowY + 2, 0xFFAAAAAA);

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
        int[] order = buildDisplayOrder(items);
        int startY = y + 20;
        int maxVisible = 4;

        if (items.isEmpty()) {
            graphics.text(this.font,
                    Component.translatable("screen.some-interesting.blank.empty"),
                    x + 30, y + 70, 0xFF888888);
        } else {
            int scrollMax = Math.max(0, order.length - maxVisible);
            int visibleCount = Math.min(order.length - scrollOffset, maxVisible);

            for (int i = 0; i < visibleCount; i++) {
                int originalIndex = order[scrollOffset + i];
                ItemStack stack = items.get(originalIndex);
                int itemX = x + 8;
                int itemY = startY + i * ITEM_ROW_HEIGHT;

                if (originalIndex == selectedIndex) {
                    graphics.fill(itemX - 2, itemY - 2, itemX + 170, itemY + 24, 0x40FFD700);
                    graphics.fill(itemX - 2, itemY - 2, itemX + 170, itemY - 1, 0xFFFFD700);
                    graphics.fill(itemX - 2, itemY + 23, itemX + 170, itemY + 24, 0xFFFFD700);
                    graphics.fill(itemX - 2, itemY - 2, itemX - 1, itemY + 24, 0xFFFFD700);
                    graphics.fill(itemX + 169, itemY - 2, itemX + 170, itemY + 24, 0xFFFFD700);
                } else {
                    graphics.fill(itemX - 1, itemY - 1, itemX + 17, itemY + 17, 0x40FFFFFF);
                }

                graphics.item(stack, itemX, itemY);
                graphics.itemDecorations(this.font, stack, itemX, itemY);

                boolean fav = stack.getOrDefault(BindingStoneItem.FAVORITED, false);
                int textX = itemX + 22;
                if (fav) {
                    graphics.text(this.font, Component.literal("\u2605"), itemX + 22, itemY + 1, 0xFFFFD700);
                    textX += 10;
                }
                graphics.text(this.font, stack.getHoverName(), textX, itemY + 1, 0xFFFFFFFF);

                long bindTime = stack.getOrDefault(BindingStoneItem.BIND_TIME, 0L);
                if (bindTime > 0) {
                    LocalDateTime dt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(bindTime), ZoneId.systemDefault());
                    String timeStr = dt.format(TIME_FMT);
                    graphics.text(this.font, Component.literal(timeStr),
                            itemX + 22, itemY + 13, 0xFF666666);
                }

                if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
                    graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
                }
            }

            if (order.length > maxVisible) {
                int end = Math.min(scrollOffset + maxVisible, order.length);
                String pageInfo = (scrollOffset + 1) + "-" + end + " / " + order.length;
                graphics.text(this.font, Component.literal(pageInfo),
                        x + 8, y + 124, 0xFF888888);

                if (scrollOffset > 0) {
                    graphics.text(this.font, Component.literal("\u25B2"),
                            x + 168, y + 18, 0xFF888888);
                }
                if (scrollOffset + maxVisible < order.length) {
                    graphics.text(this.font, Component.literal("\u25BC"),
                            x + 168, y + 120, 0xFF888888);
                }
            }
        }

        if (selectedIndex == -1 && !items.isEmpty()) {
            graphics.text(this.font,
                    Component.translatable("screen.some-interesting.select_hint"),
                    x + 190, y + 80, 0xFF888888);
        }
    }
}

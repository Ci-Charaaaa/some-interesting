package com.chara.some_interesting.Screens;

import com.chara.some_interesting.Menu.UpgradeForgeTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class UpgradeForgeTableScreen extends AbstractContainerScreen<UpgradeForgeTableMenu> {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final int TOOL_X = 8, TOOL_Y = 34;
    private static final int SLOT_SIZE = 18;

    public UpgradeForgeTableScreen(UpgradeForgeTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 28;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.leftPos, this.topPos,
                0.0f, 0.0f,
                this.imageWidth, this.imageHeight,
                256, 256);

        // 绘制工具格的深色背景 + 边框（模拟 Slot 立体凹陷效果）
        int x = this.leftPos + TOOL_X;
        int y = this.topPos + TOOL_Y;
        int w = SLOT_SIZE, h = SLOT_SIZE;

        // 背景
        graphics.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        // 上/左边框（暗色——凹陷区域顶部的阴影）
        graphics.fill(x, y, x + w, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + h, 0xFF373737);
        // 下/右边框（亮色——凹陷区域底边的反光）
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);
    }
}

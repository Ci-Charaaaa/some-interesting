package com.chara.some_interesting.ModBlockEntities;

import com.chara.some_interesting.Menu.UpgradeForgeTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class UpgradeForgeTableEntity extends BlockEntity implements ImplementedContainer, MenuProvider {

    public static final int MATERIAL_SLOT_START = 0;
    public static final int MATERIAL_SLOT_END = 8;
    public static final int TOOL_SLOT = 9;
    public static final int RESULT_SLOT = 10;
    public static final int TOTAL_SLOTS = 11;

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    @NonNull
    public Component getDisplayName() {
        return Component.translatable("block.some-interesting.upgrade_forge_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new UpgradeForgeTableMenu(containerId, inventory, this);
    }

    public UpgradeForgeTableEntity(BlockPos pos, BlockState state) {
        super(UPGRADE_FORGE_TABLE_ENTITY, pos, state);
    }

    @SuppressWarnings("unchecked")
    public static final BlockEntityType<UpgradeForgeTableEntity> UPGRADE_FORGE_TABLE_ENTITY =
            (BlockEntityType<UpgradeForgeTableEntity>) (Object) BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("some-interesting", "upgrade_forge_table"));

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output, this.items);
        super.saveAdditional(output);
    }
}

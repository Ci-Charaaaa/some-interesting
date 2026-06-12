package com.chara.some_interesting;

import com.chara.some_interesting.Blocks.UpgradeForgeTableBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class ModBlocks {

    public static void initialize() {}

    public static final Block UPGRADE_FORGE_TABLE = register(
            "upgrade_forge_table",
            UpgradeForgeTableBlock::new,
            BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1, 4),
            true);




    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties, boolean shouldRegisterItem) {
        //给方块创建一个注册用的键
        ResourceKey<Block> blockKey = keyOfBlock(name);
        //用工厂给方块造一个空白实例
        Block block = blockFactory.apply(properties.setId(blockKey));

        //有时存在不希望能成为物品的方块
        if (shouldRegisterItem) {
            //给item注册一个注册键
            ResourceKey<Item> itemKey = keyOfItem(name);
            //实例化带有item的方块
            BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        }
        //无item的方块
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        // 添加方块的所有 state 到 BLOCK_STATE_REGISTRY（网络同步用）
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            Block.BLOCK_STATE_REGISTRY.add(state);
        }

        return block;
    }

    private static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, name));
    }

    private static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, name));
    }




}

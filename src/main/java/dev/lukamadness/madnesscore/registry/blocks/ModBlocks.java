package dev.lukamadness.madnesscore.registry.blocks;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.blocks.blockclass.TailoringTableBlock;
import dev.lukamadness.madnesscore.registry.blocks.itemclass.TailoringTableItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block TAILORING_TABLE = register(
            "tailoring_table",
            new TailoringTableBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.OAK_TAN)
                            .strength(2.5F)
                            .sounds(BlockSoundGroup.WOOD)
                            .requiresTool()
                            .nonOpaque()
            )
    );

    private static Block registerBlockWithCustomItem(String name, Block block) {
        Item item;
        try {
            item = (Item) Class.forName("dev.lukamadness.madnesscore.client.registry.blockclass.TailoringTableItemClient")
                    .getDeclaredConstructor(Block.class, Item.Settings.class)
                    .newInstance(block, new Item.Settings());
        } catch (Exception e) {
            e.printStackTrace();
            item = new TailoringTableItem(block, new Item.Settings());
        }

        Registry.register(Registries.ITEM, Identifier.of(MadnessCore.MOD_ID, name), item);
        Registry.register(Registries.BLOCK, Identifier.of(MadnessCore.MOD_ID, name), block);

        return block;
    }

    private static Block register(String name, Block block) {
        Registry.register(Registries.BLOCK, Identifier.of(MadnessCore.MOD_ID, name), block);

        Registry.register(
                Registries.ITEM,
                Identifier.of(MadnessCore.MOD_ID, name),
                new BlockItem(block, new Item.Settings())
        );

        return block;
    }

    public static void initialize() {
        MadnessCore.LOGGER.info("Registering Mod Blocks");
    }
}

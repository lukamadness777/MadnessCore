package dev.lukamadness.madnesscore.registry;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.blocks.ModBlocks;
import dev.lukamadness.madnesscore.registry.items.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup MADNESS_CORE = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(MadnessCore.MOD_ID, "madness_core"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.MADNESS_CORE))
                    .displayName(Text.translatable("itemGroup.madnesscore.madness_core"))
                    .entries((context, entries) -> {
                        entries.add(ModBlocks.TAILORING_TABLE);

                        entries.add(ModItems.WHITE_FABRIC);
                        entries.add(ModItems.BLACK_FABRIC);
                        entries.add(ModItems.GRAY_FABRIC);
                        entries.add(ModItems.LIGHT_GRAY_FABRIC);
                        entries.add(ModItems.BROWN_FABRIC);
                        entries.add(ModItems.RED_FABRIC);
                        entries.add(ModItems.ORANGE_FABRIC);
                        entries.add(ModItems.YELLOW_FABRIC);
                        entries.add(ModItems.LIME_FABRIC);
                        entries.add(ModItems.GREEN_FABRIC);
                        entries.add(ModItems.CYAN_FABRIC);
                        entries.add(ModItems.LIGHT_BLUE_FABRIC);
                        entries.add(ModItems.BLUE_FABRIC);
                        entries.add(ModItems.PURPLE_FABRIC);
                        entries.add(ModItems.MAGENTA_FABRIC);
                        entries.add(ModItems.PINK_FABRIC);

                        entries.add(ModItems.DYEABLE_SHIRT);
                        entries.add(ModItems.DYEABLE_JACKET);
                        entries.add(ModItems.DYEABLE_LEGGINS);
                        entries.add(ModItems.FORMAL_SUIT);
                        entries.add(ModItems.FORMAL_SUIT_BOOTS);
                    })
                    .build()
    );

    public static void initialize() {
        MadnessCore.LOGGER.info("Registering Item Groups");
    }
}
package dev.lukamadness.madnesscore.registry.blocks;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.blocks.blockclass.TailoringTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<TailoringTableBlockEntity> TAILORING_TABLE =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(MadnessCore.MOD_ID, "tailoring_table"),
                    FabricBlockEntityTypeBuilder.create(
                            TailoringTableBlockEntity::new,
                            ModBlocks.TAILORING_TABLE
                    ).build()
            );

    public static void initialize() {
        MadnessCore.LOGGER.info("Registering Block Entities");
    }
}
package dev.lukamadness.madnesscore.client.render.model;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.blocks.blockclass.TailoringTableBlock;
import dev.lukamadness.madnesscore.registry.blocks.blockclass.TailoringTableBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class TailoringTableModel extends GeoModel<TailoringTableBlockEntity> {
    @Override
    public Identifier getModelResource(TailoringTableBlockEntity entity) {
        return Identifier.of(MadnessCore.MOD_ID, "geo/block/tailoring_table.geo.json");
    }

    @Override
    public Identifier getTextureResource(TailoringTableBlockEntity entity) {
        return Identifier.of(MadnessCore.MOD_ID, "textures/block/tailoring_table.png");
    }

    @Override
    public Identifier getAnimationResource(TailoringTableBlockEntity entity) {
        return Identifier.of(MadnessCore.MOD_ID, "animations/tailoring_table.animation.json");
    }
}
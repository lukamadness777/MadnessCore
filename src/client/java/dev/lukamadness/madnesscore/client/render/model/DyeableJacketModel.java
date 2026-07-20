package dev.lukamadness.madnesscore.client.render.model;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableJacketItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DyeableJacketModel extends GeoModel<DyeableJacketItem> {
    @Override
    public Identifier getModelResource(DyeableJacketItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "geo/armor/dyeable_jacket.geo.json");
    }

    @Override
    public Identifier getTextureResource(DyeableJacketItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "textures/armor/dyeable_jacket.png");
    }

    @Override
    public Identifier getAnimationResource(DyeableJacketItem animatable) {
        return null;
    }
}

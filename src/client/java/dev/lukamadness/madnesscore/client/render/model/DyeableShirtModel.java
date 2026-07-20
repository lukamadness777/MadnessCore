package dev.lukamadness.madnesscore.client.render.model;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableShirtItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DyeableShirtModel extends GeoModel<DyeableShirtItem> {
    @Override
    public Identifier getModelResource(DyeableShirtItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "geo/armor/dyeable_shirt.geo.json");
    }

    @Override
    public Identifier getTextureResource(DyeableShirtItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "textures/armor/dyeable_shirt.png");
    }

    @Override
    public Identifier getAnimationResource(DyeableShirtItem animatable) {
        return null;
    }
}

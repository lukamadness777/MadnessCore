package dev.lukamadness.madnesscore.client.render.model;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableLegginsItem;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableShirtItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DyeableLegginsModel extends GeoModel<DyeableLegginsItem> {
    @Override
    public Identifier getModelResource(DyeableLegginsItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "geo/armor/dyeable_leggins.geo.json");
    }

    @Override
    public Identifier getTextureResource(DyeableLegginsItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "textures/armor/dyeable_shirt.png");
    }

    @Override
    public Identifier getAnimationResource(DyeableLegginsItem animatable) {
        return null;
    }
}


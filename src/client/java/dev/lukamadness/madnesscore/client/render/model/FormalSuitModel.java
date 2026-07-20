package dev.lukamadness.madnesscore.client.render.model;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FormalSuitModel extends GeoModel<FormalSuitItem> {

    @Override
    public Identifier getModelResource(FormalSuitItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "geo/armor/formal_suit_chest.geo.json");
    }

    @Override
    public Identifier getTextureResource(FormalSuitItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "textures/armor/formal_suit.png");
    }

    @Override
    public Identifier getAnimationResource(FormalSuitItem animatable) {
        return null;
    }
}
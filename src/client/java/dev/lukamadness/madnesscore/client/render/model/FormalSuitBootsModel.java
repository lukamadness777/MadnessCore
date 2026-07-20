package dev.lukamadness.madnesscore.client.render.model;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitBootsItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FormalSuitBootsModel extends GeoModel<FormalSuitBootsItem> {
    @Override
    public Identifier getModelResource(FormalSuitBootsItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "geo/armor/formal_suit_boots.geo.json");
    }

    @Override
    public Identifier getTextureResource(FormalSuitBootsItem animatable) {
        return Identifier.of(MadnessCore.MOD_ID, "textures/armor/formal_suit.png");
    }

    @Override
    public Identifier getAnimationResource(FormalSuitBootsItem animatable) {
        return null;
    }
}

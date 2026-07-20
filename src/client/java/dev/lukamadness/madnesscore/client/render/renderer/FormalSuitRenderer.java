package dev.lukamadness.madnesscore.client.render.renderer;

import dev.lukamadness.madnesscore.client.render.model.FormalSuitModel;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitItem;
import dev.lukamadness.madnesscore.util.DataComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DyeableGeoArmorRenderer;
import software.bernie.geckolib.util.Color;

public class FormalSuitRenderer extends DyeableGeoArmorRenderer<FormalSuitItem> {

    public FormalSuitRenderer() {
        super(new FormalSuitModel());
    }

    @Override
    public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        this.setVisible(true);

        if (this.currentStack != null) {
            Boolean tieVisible = this.currentStack.get(DataComponents.TIE_VISIBLE);
            if (tieVisible != null && !tieVisible) {
                GeoBone tieBone = this.model.getBone("dyeableTie").orElse(null);
                if (tieBone != null) {
                    tieBone.setHidden(true);
                }
            }
        }
    }

    @Override
    protected boolean isBoneDyeable(GeoBone bone) {
        return bone.getName().startsWith("dyeable");
    }

    @Override
    protected @NotNull Color getColorForBone(GeoBone bone) {
        DataComponents.FormalColors colors = this.currentStack != null
                ? this.currentStack.get(DataComponents.FORMAL_COLORS)
                : null;

        if (colors == null) {
            colors = DataComponents.FormalColors.DEFAULT;
        }

        int rgb = getColorForBoneName(bone.getName(), colors);
        return new Color(0xFF000000 | rgb);
    }

    private int getColorForBoneName(String boneName, DataComponents.FormalColors colors) {
        if (boneName.contains("Tie")) {
            return colors.tieColor();
        } else if (boneName.contains("Shirt")) {
            return colors.shirtColor();
        } else if (boneName.contains("Suit")) {
            return colors.suitColor();
        }

        return 0xFFFFFF;
    }

    public void setCurrentStack(ItemStack stack) {
        this.currentStack = stack;
    }

    public void setCurrentEntity(LivingEntity entity) {
        this.currentEntity = entity;
    }
}
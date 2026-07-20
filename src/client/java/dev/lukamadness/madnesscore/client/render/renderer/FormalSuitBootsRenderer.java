package dev.lukamadness.madnesscore.client.render.renderer;

import dev.lukamadness.madnesscore.client.render.model.FormalSuitBootsModel;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitBootsItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DyeableGeoArmorRenderer;
import software.bernie.geckolib.util.Color;

public class FormalSuitBootsRenderer extends DyeableGeoArmorRenderer<FormalSuitBootsItem> {
    public FormalSuitBootsRenderer() {
        super(new FormalSuitBootsModel());
    }

    @Override
    public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        this.setVisible(true);
    }

    @Override
    protected boolean isBoneDyeable(GeoBone bone) {
        String name = bone.getName();
        return name.contains("dyeable") || name.contains("Suit");
    }

    @Override
    protected @NotNull Color getColorForBone(GeoBone bone) {
        if (this.currentStack != null) {
            DyedColorComponent comp = this.currentStack.get(DataComponentTypes.DYED_COLOR);

            if (comp != null) {
                return new Color(0xFF000000 | comp.rgb());
            }
        }

        return new Color(0xFF000000 | 0x1E1E1E);
    }

    public void setCurrentStack(ItemStack stack) {
        this.currentStack = stack;
    }

    public void setCurrentEntity(LivingEntity entity) {
        this.currentEntity = entity;
    }
}

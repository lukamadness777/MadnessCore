package dev.lukamadness.madnesscore.client.render.renderer;

import dev.lukamadness.madnesscore.client.render.model.DyeableLegginsModel;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableLegginsItem;
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

public class DyeableLegginsRenderer extends DyeableGeoArmorRenderer<DyeableLegginsItem> {
    public DyeableLegginsRenderer() {
        super(new DyeableLegginsModel());
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

        return !name.equals("logoLayer")
                && !name.equals("someOtherLayer");
    }

    @Override
    protected @NotNull Color getColorForBone(GeoBone bone) {
        if (bone.getName().equals("logoLayer")) {
            return Color.WHITE;
        }

        if (this.currentStack != null) {
            DyedColorComponent comp = this.currentStack.get(DataComponentTypes.DYED_COLOR);

            if (comp != null) {
                return new Color(0xFF000000 | comp.rgb());
            }
        }

        return Color.WHITE;
    }

    public void setCurrentStack(ItemStack stack) {
        this.currentStack = stack;
    }

    public void setCurrentEntity(LivingEntity entity) {
        this.currentEntity = entity;
    }
}

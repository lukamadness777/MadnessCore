package dev.lukamadness.madnesscore.client.mixin;

import dev.lukamadness.madnesscore.registry.items.armorclass.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(
            method = "setModelPose",
            at = @At("TAIL")
    )
    private void madnesscore$hideBodyLayersWhenShirtEquipped(
            AbstractClientPlayerEntity player,
            CallbackInfo ci
    ) {
        PlayerEntityRenderer self = (PlayerEntityRenderer)(Object)this;
        PlayerEntityModel<?> model = (PlayerEntityModel<?>) self.getModel();

        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack legsStack = player.getEquippedStack(EquipmentSlot.LEGS);

        if (chestStack.getItem() instanceof DyeableShirtItem
                ||chestStack.getItem() instanceof FormalSuitBootsItem) {
            model.jacket.visible      = false;
            model.leftSleeve.visible  = false;
            model.rightSleeve.visible = false;
        }
        else if (legsStack.getItem() instanceof DyeableLegginsItem
                || legsStack.getItem() instanceof FormalSuitBootsItem) {
            model.leftPants.visible  = false;
            model.rightPants.visible = false;
        }
    }
}
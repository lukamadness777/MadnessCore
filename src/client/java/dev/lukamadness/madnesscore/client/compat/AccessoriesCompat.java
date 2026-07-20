package dev.lukamadness.madnesscore.client.compat;

import dev.lukamadness.madnesscore.client.render.renderer.DyeableJacketRenderer;
import dev.lukamadness.madnesscore.registry.items.ModItems;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableJacketItem;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AccessoriesCompat {

    public static void register() {
        register(ModItems.DYEABLE_JACKET, DyeableJacketRenderer::new, EquipmentSlot.CHEST,
                (r, s) -> r.getTextureLocation((DyeableJacketItem) s.getItem()));
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <R extends GeoArmorRenderer<?>> void register(
            Item modItem,
            Supplier<R> factory,
            EquipmentSlot slot,
            BiFunction<R, ItemStack, Identifier> setupAndTexture,
            Predicate<LivingEntity> extraGuard
    ) {
        AccessoriesRendererRegistry.registerRenderer(modItem, () -> new AccessoryRenderer() {
            private R renderer;

            @Override
            public <M extends LivingEntity> void render(
                    ItemStack stack,
                    SlotReference reference,
                    MatrixStack matrices,
                    EntityModel<M> contextModel,
                    VertexConsumerProvider multiBufferSource,
                    int light,
                    float limbSwing,
                    float limbSwingAmount,
                    float partialTicks,
                    float ageInTicks,
                    float netHeadYaw,
                    float headPitch
            ) {
                LivingEntity entity = reference.entity();
                if (entity == null) return;
                if (renderer == null) renderer = factory.get();
                if (!(contextModel instanceof BipedEntityModel<?> bipedModel)) return;
                if (extraGuard != null && !extraGuard.test(entity)) return;

                renderer.prepForRender(entity, stack, slot, bipedModel,
                        multiBufferSource, partialTicks, limbSwing, limbSwingAmount, netHeadYaw, headPitch);

                if (entity instanceof AbstractClientPlayerEntity
                        && contextModel instanceof PlayerEntityModel<?> playerModel) {
                    AccessoryRenderer.followBodyRotations(entity,
                            (BipedEntityModel<LivingEntity>) (BipedEntityModel<?>) playerModel);
                }

                Identifier texture = setupAndTexture.apply(renderer, stack);
                renderer.render(matrices,
                        multiBufferSource.getBuffer(RenderLayer.getEntityCutoutNoCull(texture)),
                        light, OverlayTexture.DEFAULT_UV);
            }
        });
    }

    private static <R extends GeoArmorRenderer<?>> void register(
            Item modItem, Supplier<R> factory, EquipmentSlot slot,
            BiFunction<R, ItemStack, Identifier> setupAndTexture) {
        register(modItem, factory, slot, setupAndTexture, null);
    }


}

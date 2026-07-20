package dev.lukamadness.madnesscore.client.render.renderer;

import dev.lukamadness.madnesscore.client.render.model.TailoringTableModel;
import dev.lukamadness.madnesscore.registry.blocks.blockclass.TailoringTableBlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TailoringTableRenderer extends GeoBlockRenderer<TailoringTableBlockEntity> {
    public TailoringTableRenderer() {
        super(new TailoringTableModel());
    }

    @Override
    public void render(TailoringTableBlockEntity entity, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        int skyLight   = entity.getWorld().getLightLevel(net.minecraft.world.LightType.SKY, entity.getPos().up());
        int blockLight = entity.getWorld().getLightLevel(net.minecraft.world.LightType.BLOCK, entity.getPos().up());
        int light = LightmapTextureManager.pack(blockLight, skyLight);
        super.render(entity, partialTick, poseStack, bufferSource, light, packedOverlay);
    }
}
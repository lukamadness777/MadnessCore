package dev.lukamadness.madnesscore.network;

import dev.lukamadness.madnesscore.tailoring.TailoringRecipeManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ModNetworking {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(TailoringRecipeListPayload.ID, TailoringRecipeListPayload.CODEC);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(TailoringRecipeManager.getInstance());

        PayloadTypeRegistry.playC2S().register(AppearanceConfigPayload.ID, AppearanceConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AppearanceConfigPayload.ID, AppearanceConfigPayload.CODEC);
    }
}
package dev.lukamadness.madnesscore.client.network;

import dev.lukamadness.madnesscore.client.screen.TailoringTableScreen;
import dev.lukamadness.madnesscore.network.TailoringRecipeListPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ModNetworkingClient {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(TailoringRecipeListPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (MinecraftClient.getInstance().currentScreen instanceof TailoringTableScreen screen) {
                        screen.setRecipes(payload.recipes());
                    }
                }));
    }
}
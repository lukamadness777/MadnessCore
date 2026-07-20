package dev.lukamadness.madnesscore.client.network;

import dev.lukamadness.madnesscore.client.config.MadnessCoreConfig;
import dev.lukamadness.madnesscore.network.AppearanceConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class AppearanceClientNetworking {

    private AppearanceClientNetworking() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(AppearanceConfigPayload.ID, (payload, context) ->
                AppearanceCache.put(payload.owner(), payload)
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sendToServer());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AppearanceCache.clear());
    }

    public static void sendToServer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (!ClientPlayNetworking.canSend(AppearanceConfigPayload.ID)) return;

        AppearanceConfigPayload payload = AppearanceConfigPayloadHelper.fromConfig(
                client.player.getUuid(), MadnessCoreConfig.get());

        ClientPlayNetworking.send(payload);
    }
}
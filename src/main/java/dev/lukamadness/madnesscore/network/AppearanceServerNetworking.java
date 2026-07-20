package dev.lukamadness.madnesscore.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class AppearanceServerNetworking {

    private AppearanceServerNetworking() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(AppearanceConfigPayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();

            AppearanceConfigPayload verified = new AppearanceConfigPayload(
                    sender.getUuid(),
                    payload.skinColor(),
                    payload.eyeOffsetX(), payload.eyeOffsetY(), payload.eyeWidth(), payload.eyeHeight(), payload.eyeColor(),
                    payload.hairType(),
                    payload.hairColor(),
                    payload.hairPixels(),
                    payload.eyePixels()
            );

            AppearanceStore.put(sender.getUuid(), verified);

            context.server().execute(() -> {
                for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(player, verified);
                }
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (AppearanceConfigPayload existing : AppearanceStore.getAll().values()) {
                if (existing.owner().equals(handler.getPlayer().getUuid())) continue;
                ServerPlayNetworking.send(handler.getPlayer(), existing);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                AppearanceStore.remove(handler.getPlayer().getUuid()));
    }
}
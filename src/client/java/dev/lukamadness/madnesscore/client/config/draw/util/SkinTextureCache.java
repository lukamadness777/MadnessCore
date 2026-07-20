package dev.lukamadness.madnesscore.client.config.draw.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

public final class SkinTextureCache {

    private static volatile Identifier fetchedSkinTexture = null;
    private static boolean fetchInFlight = false;

    private SkinTextureCache() {}

    public static Identifier get() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player instanceof AbstractClientPlayerEntity clientPlayer) {
            return clientPlayer.getSkinTextures().texture();
        }

        fetchAsyncIfNeeded(client);

        Identifier fetched = fetchedSkinTexture;
        return (fetched != null) ? fetched : DefaultSkinHelper.getTexture();
    }

    private static synchronized void fetchAsyncIfNeeded(MinecraftClient client) {
        if (fetchedSkinTexture != null || fetchInFlight) return;
        fetchInFlight = true;
        try {
            client.getSkinProvider().fetchSkinTextures(client.getGameProfile())
                    .thenAcceptAsync(skinTextures -> {
                        fetchedSkinTexture = skinTextures.texture();
                        fetchInFlight = false;
                    }, client)
                    .exceptionally(ex -> {
                        fetchInFlight = false;
                        return null;
                    });
        } catch (Exception e) {
            fetchInFlight = false;
        }
    }
}
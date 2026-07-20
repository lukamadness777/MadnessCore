package dev.lukamadness.madnesscore.client.network;

import dev.lukamadness.madnesscore.client.config.MadnessCoreConfig;
import dev.lukamadness.madnesscore.client.config.draw.util.SkinRegion;
import dev.lukamadness.madnesscore.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.network.util.PixelPacking;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class AppearanceConfigPayloadHelper {

    private AppearanceConfigPayloadHelper() {}

    public static AppearanceConfigPayload fromConfig(UUID owner, MadnessCoreConfig cfg) {
        return new AppearanceConfigPayload(
                owner,
                cfg.skinColor,
                cfg.eyeOffsetX, cfg.eyeOffsetY, cfg.eyeWidth, cfg.eyeHeight, cfg.eyeColor,
                cfg.hairType.ordinal(),
                cfg.hairColor,
                packMap(cfg.hairPixels),
                packMap(cfg.eyePixels)
        );
    }

    private static Map<String, byte[]> packMap(Map<String, boolean[][]> source) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        for (Map.Entry<String, boolean[][]> entry : source.entrySet()) {
            out.put(entry.getKey(), PixelPacking.pack(entry.getValue()));
        }
        return out;
    }

    public static Map<String, boolean[][]> unpackHairPixels(AppearanceConfigPayload payload) {
        return unpackMap(payload.hairPixels());
    }

    public static Map<String, boolean[][]> unpackEyePixels(AppearanceConfigPayload payload) {
        return unpackMap(payload.eyePixels());
    }

    private static Map<String, boolean[][]> unpackMap(Map<String, byte[]> source) {
        Map<String, boolean[][]> out = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : source.entrySet()) {
            SkinRegion region = SkinRegion.byKey(entry.getKey());
            if (region == null) continue;
            out.put(entry.getKey(), PixelPacking.unpack(entry.getValue(), region.width, region.height));
        }
        return out;
    }
}
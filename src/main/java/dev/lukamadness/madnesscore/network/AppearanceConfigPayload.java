package dev.lukamadness.madnesscore.network;

import dev.lukamadness.madnesscore.MadnessCore;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record AppearanceConfigPayload(
        UUID owner,
        int skinColor,
        int eyeOffsetX,
        int eyeOffsetY,
        int eyeWidth,
        int eyeHeight,
        int eyeColor,
        int hairType,
        int hairColor,
        Map<String, byte[]> hairPixels,
        Map<String, byte[]> eyePixels
) implements CustomPayload {

    public static final Id<AppearanceConfigPayload> ID =
            new Id<>(Identifier.of(MadnessCore.MOD_ID, "appearance_config"));

    public static final PacketCodec<RegistryByteBuf, AppearanceConfigPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeUuid(payload.owner());
                buf.writeInt(payload.skinColor());
                buf.writeInt(payload.eyeOffsetX());
                buf.writeInt(payload.eyeOffsetY());
                buf.writeInt(payload.eyeWidth());
                buf.writeInt(payload.eyeHeight());
                buf.writeInt(payload.eyeColor());
                buf.writeInt(payload.hairType());
                buf.writeInt(payload.hairColor());
                writePixelMap(buf, payload.hairPixels());
                writePixelMap(buf, payload.eyePixels());
            },
            buf -> new AppearanceConfigPayload(
                    buf.readUuid(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    readPixelMap(buf),
                    readPixelMap(buf)
            )
    );

    private static void writePixelMap(RegistryByteBuf buf, Map<String, byte[]> map) {
        buf.writeVarInt(map.size());
        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeByteArray(entry.getValue());
        }
    }

    private static Map<String, byte[]> readPixelMap(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, byte[]> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readString();
            byte[] value = buf.readByteArray();
            map.put(key, value);
        }
        return map;
    }

    @Override
    public Id<AppearanceConfigPayload> getId() { return ID; }
}
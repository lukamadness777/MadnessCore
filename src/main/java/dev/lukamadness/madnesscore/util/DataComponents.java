package dev.lukamadness.madnesscore.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukamadness.madnesscore.MadnessCore;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DataComponents {

    public static final ComponentType<FormalColors> FORMAL_COLORS = register(
            "formal_colors",
            builder -> builder
                    .codec(FormalColors.CODEC)
                    .packetCodec(FormalColors.PACKET_CODEC)
    );

    public static final ComponentType<Boolean> TIE_VISIBLE = register(
            "tie_visible",
            builder -> builder
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL)
    );

    private static <T> ComponentType<T> register(
            String name,
            java.util.function.UnaryOperator<ComponentType.Builder<T>> builderOperator
    ) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.of(MadnessCore.MOD_ID, name),
                builderOperator.apply(ComponentType.builder()).build()
        );
    }

    public static void registerComponents() {
    }

    public record FormalColors(
            int suitColor,
            int tieColor,
            int shirtColor
    ) {
        public static final FormalColors DEFAULT = new FormalColors(0x1E1E1E, 0x890E10, 0xFFFFFF);

        public static final Codec<FormalColors> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.fieldOf("suit_color").forGetter(FormalColors::suitColor),
                        Codec.INT.fieldOf("tie_color").forGetter(FormalColors::tieColor),
                        Codec.INT.fieldOf("shirt_color").forGetter(FormalColors::shirtColor)
                ).apply(instance, FormalColors::new));

        public static final PacketCodec<RegistryByteBuf, FormalColors> PACKET_CODEC =
                PacketCodec.tuple(
                        PacketCodecs.VAR_INT, FormalColors::suitColor,
                        PacketCodecs.VAR_INT, FormalColors::tieColor,
                        PacketCodecs.VAR_INT, FormalColors::shirtColor,
                        FormalColors::new
                );

        public FormalColors withSuitColor(int color) {
            return new FormalColors(color, tieColor, shirtColor);
        }

        public FormalColors withTieColor(int color) {
            return new FormalColors(suitColor, color, shirtColor);
        }

        public FormalColors withShirtColor(int color) {
            return new FormalColors(suitColor, tieColor, color);
        }
    }
}
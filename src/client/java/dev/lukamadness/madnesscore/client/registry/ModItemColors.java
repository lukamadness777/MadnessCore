package dev.lukamadness.madnesscore.client.registry;

import dev.lukamadness.madnesscore.registry.items.ModItems;
import dev.lukamadness.madnesscore.util.DataComponents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.DataComponentTypes;

public class ModItemColors {

    public static void register() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            DyedColorComponent comp = stack.get(DataComponentTypes.DYED_COLOR);
            return comp != null ? (comp.rgb() | 0xFF000000) : -1;
        }, ModItems.DYEABLE_SHIRT);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            DyedColorComponent comp = stack.get(DataComponentTypes.DYED_COLOR);
            return comp != null ? (comp.rgb() | 0xFF000000) : -1;
        }, ModItems.DYEABLE_JACKET);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex != 1) return -1;
            DyedColorComponent comp = stack.get(DataComponentTypes.DYED_COLOR);
            return comp != null ? (comp.rgb() | 0xFF000000) : -1;
        }, ModItems.DYEABLE_LEGGINS);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            DataComponents.FormalColors colors = stack.get(DataComponents.FORMAL_COLORS);
            if (colors == null) colors = DataComponents.FormalColors.DEFAULT;

            return switch (tintIndex) {
                case 0 -> colors.suitColor() | 0xFF000000;
                case 1 -> colors.shirtColor() | 0xFF000000;
                case 2 -> {
                    Boolean tieVisible = stack.get(DataComponents.TIE_VISIBLE);
                    if (tieVisible != null && !tieVisible) {
                        yield colors.shirtColor() | 0xFF000000;
                    }
                    yield colors.tieColor() | 0xFF000000;
                }
                default -> -1;
            };
        }, ModItems.FORMAL_SUIT);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex != 1) return -1;
            DyedColorComponent comp = stack.get(DataComponentTypes.DYED_COLOR);
            if (comp == null) {
                return 0x1E1E1E | 0xFF000000;
            }
            return comp.rgb() | 0xFF000000;
        }, ModItems.FORMAL_SUIT_BOOTS);
    }
}
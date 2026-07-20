package dev.lukamadness.madnesscore.helpers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class DyeMixUtil {

    private DyeMixUtil() {}

    public static int mixDyeColor(ItemStack base, List<DyeItem> dyes) {
        int r = 0, g = 0, b = 0, count = 0;

        DyedColorComponent existing = base.get(DataComponentTypes.DYED_COLOR);
        if (existing != null) {
            int c = existing.rgb();
            r += (c >> 16) & 0xFF;
            g += (c >> 8) & 0xFF;
            b += c & 0xFF;
            count++;
        }

        for (DyeItem dye : dyes) {
            int c = dye.getColor().getEntityColor();
            r += (c >> 16) & 0xFF;
            g += (c >> 8) & 0xFF;
            b += c & 0xFF;
            count++;
        }

        return ((r / count) << 16) | ((g / count) << 8) | (b / count);
    }
}
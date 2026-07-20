package dev.lukamadness.madnesscore.client.registry.armorclass;

import dev.lukamadness.madnesscore.client.render.renderer.FormalSuitRenderer;
import dev.lukamadness.madnesscore.registry.items.ModArmorMaterials;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitItem;
import dev.lukamadness.madnesscore.util.DataComponents;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.List;
import java.util.function.Consumer;

public class FormalSuitItemClient extends FormalSuitItem {
    public FormalSuitItemClient() {
        super(ModArmorMaterials.CLOTHES_MATERIAL, new Settings().maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        if (stack.get(DataComponents.FORMAL_COLORS) == null) {
            stack.set(DataComponents.FORMAL_COLORS, DataComponents.FormalColors.DEFAULT);
        }
        return stack;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> BipedEntityModel<?> getGeoArmorRenderer(
                    @Nullable T livingEntity,
                    ItemStack itemStack,
                    @Nullable EquipmentSlot equipmentSlot,
                    @Nullable BipedEntityModel<T> original
            ) {
                if (this.renderer == null)
                    this.renderer = new FormalSuitRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(
                Text.translatable("tooltip.madnesscore.dyeable_tip").formatted(Formatting.DARK_GRAY)
        );

        Boolean tieVisible = stack.get(DataComponents.TIE_VISIBLE);
        if (tieVisible != null) {
            String visibilityKey = tieVisible ? "tooltip.madnesscore.show" : "tooltip.madnesscore.hide";
            MutableText tieVisibilityText = Text.translatable("tooltip.madnesscore.tie_show")
                    .formatted(Formatting.GRAY)
                    .append(Text.translatable(visibilityKey));
            tooltip.add(tieVisibilityText);
        }

        DataComponents.FormalColors colors = stack.get(DataComponents.FORMAL_COLORS);
        if (colors != null) {
            int suitRgb = colors.suitColor();
            int tieRgb = colors.tieColor();
            int shirtRgb = colors.shirtColor();

            String suitHex = String.format("#%06X", suitRgb);
            String tieHex = String.format("#%06X", tieRgb);
            String shirtHex = String.format("#%06X", shirtRgb);

            MutableText suitText = Text.translatable("tooltip.madnesscore.suit_color")
                    .formatted(Formatting.GRAY)
                    .append(
                            Text.literal(suitHex)
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(suitRgb)))
                    );

            MutableText tieText = Text.translatable("tooltip.madnesscore.tie_color")
                    .formatted(Formatting.GRAY)
                    .append(
                            Text.literal(tieHex)
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(tieRgb)))
                    );

            MutableText shirtText = Text.translatable("tooltip.madnesscore.shirt_color")
                    .formatted(Formatting.GRAY)
                    .append(
                            Text.literal(shirtHex)
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(shirtRgb)))
                    );

            tooltip.add(suitText);
            tooltip.add(tieText);
            tooltip.add(shirtText);
        }

        super.appendTooltip(stack, context, tooltip, type);
    }
}
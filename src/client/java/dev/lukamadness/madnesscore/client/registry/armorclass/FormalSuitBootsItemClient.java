package dev.lukamadness.madnesscore.client.registry.armorclass;

import dev.lukamadness.madnesscore.client.render.renderer.FormalSuitBootsRenderer;
import dev.lukamadness.madnesscore.registry.items.ModArmorMaterials;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitBootsItem;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
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

public class FormalSuitBootsItemClient extends FormalSuitBootsItem {
    public FormalSuitBootsItemClient() {
        super(ModArmorMaterials.BOOTS_MATERIAL, new Settings().maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        if (stack.get(DataComponentTypes.DYED_COLOR) == null) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E1E1E, false));
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
                    this.renderer = new FormalSuitBootsRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {

        tooltip.add(
                Text.translatable("tooltip.madnesscore.dyeable_tip").formatted(Formatting.DARK_GRAY)
        );

        DyedColorComponent comp = stack.get(DataComponentTypes.DYED_COLOR);
        if (comp != null) {
            int rgb = comp.rgb();
            String hex = String.format("#%06X", rgb);

            MutableText text = Text.translatable("tooltip.madnesscore.suit_color")
                    .formatted(Formatting.GRAY)
                    .append(
                            Text.literal(hex)
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)))
                    );

            tooltip.add(text);
        }

        super.appendTooltip(stack, context, tooltip, type);
    }
}
package dev.lukamadness.madnesscore.registry.items.armorclass;

import dev.lukamadness.madnesscore.util.DataComponents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FormalSuitItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FormalSuitItem(RegistryEntry<ArmorMaterial> material, Item.Settings settings) {
        super(material, ArmorItem.Type.CHESTPLATE, settings);
    }

    public static int getSuitColor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.FORMAL_COLORS, DataComponents.FormalColors.DEFAULT).suitColor();
    }

    public static int getTieColor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.FORMAL_COLORS, DataComponents.FormalColors.DEFAULT).tieColor();
    }

    public static int getShirtColor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.FORMAL_COLORS, DataComponents.FormalColors.DEFAULT).shirtColor();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
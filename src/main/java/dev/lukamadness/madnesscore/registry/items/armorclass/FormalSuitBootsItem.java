package dev.lukamadness.madnesscore.registry.items.armorclass;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FormalSuitBootsItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FormalSuitBootsItem(RegistryEntry<ArmorMaterial> material, Item.Settings settings) {
        super(material, Type.BOOTS, settings);
    }

    public static int getColor(ItemStack stack) {
        DyedColorComponent comp = stack.get(DataComponentTypes.DYED_COLOR);
        return comp != null ? comp.rgb() : 0x1E1E1E;
    }

    public void setColor(ItemStack stack, int color) {
        stack.set(
                DataComponentTypes.DYED_COLOR,
                new DyedColorComponent(color, false)
        );
    }

    public void removeColor(ItemStack stack) {
        stack.remove(DataComponentTypes.DYED_COLOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {return cache;}
}
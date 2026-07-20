package dev.lukamadness.madnesscore.registry.items;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.items.armorclass.*;
import dev.lukamadness.madnesscore.util.DataComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.function.Supplier;

public class ModItems {

    public static final Item MADNESS_CORE = register("madness_core", new Item(new Item.Settings().maxCount(1)));

    public static final Item WHITE_FABRIC = register("white_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item BLACK_FABRIC = register("black_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item BLUE_FABRIC = register("blue_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item BROWN_FABRIC = register("brown_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item CYAN_FABRIC = register("cyan_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item GRAY_FABRIC = register("gray_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item GREEN_FABRIC = register("green_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item LIGHT_BLUE_FABRIC = register("light_blue_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item LIGHT_GRAY_FABRIC = register("light_gray_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item LIME_FABRIC = register("lime_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item MAGENTA_FABRIC = register("magenta_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item ORANGE_FABRIC = register("orange_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item PINK_FABRIC = register("pink_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item PURPLE_FABRIC = register("purple_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item RED_FABRIC = register("red_fabric", new Item(new Item.Settings().maxCount(16)));
    public static final Item YELLOW_FABRIC = register("yellow_fabric", new Item(new Item.Settings().maxCount(16)));

    public static final Item DYEABLE_SHIRT = registerItem("dyeable_shirt", tryClientClass(
            "dev.lukamadness.madnesscore.client.registry.armorclass.DyeableShirtItemClient",
            () -> new DyeableShirtItem(ModArmorMaterials.CLOTHES_MATERIAL,
                    new Item.Settings().maxCount(1)
                            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFFFFFF, false)))));

    public static final Item DYEABLE_LEGGINS = registerItem("dyeable_leggins", tryClientClass(
            "dev.lukamadness.madnesscore.client.registry.armorclass.DyeableLegginsItemClient",
            () -> new DyeableLegginsItem(ModArmorMaterials.CLOTHES_MATERIAL,
                    new Item.Settings().maxCount(1)
                            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFFFFFF, false)))));

    public static final Item DYEABLE_JACKET = registerItem("dyeable_jacket", tryClientClass(
            "dev.lukamadness.madnesscore.client.registry.armorclass.DyeableJacketItemClient",
            () -> new DyeableJacketItem(ModArmorMaterials.CLOTHES_MATERIAL,
                    new Item.Settings().maxCount(1)
                            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFFFFFF, false)))));

    public static final Item FORMAL_SUIT = registerItem("formal_suit", tryClientClass(
            "dev.lukamadness.madnesscore.client.registry.armorclass.FormalSuitItemClient",
            () -> new FormalSuitItem(ModArmorMaterials.CLOTHES_MATERIAL,
                    new Item.Settings()
                            .maxCount(1)
                            .component(DataComponents.FORMAL_COLORS, DataComponents.FormalColors.DEFAULT))));

    public static final Item FORMAL_SUIT_BOOTS = registerItem("formal_suit_boots", tryClientClass(
            "dev.lukamadness.madnesscore.client.registry.armorclass.FormalSuitBootsItemClient",
            () -> new FormalSuitBootsItem(ModArmorMaterials.BOOTS_MATERIAL,
                    new Item.Settings().maxCount(1))));

    @SuppressWarnings("unchecked")
    private static <T extends Item> T tryClientClass(String clientClassName, Supplier<T> fallback, Object... args) {
        try {
            Class<?>[] types = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class[]::new);

            for (var constructor : Class.forName(clientClassName).getDeclaredConstructors()) {
                if (constructor.getParameterCount() == args.length) {
                    return (T) constructor.newInstance(args);
                }
            }
        } catch (Exception e) {
            MadnessCore.LOGGER.error("Failed to load client class '{}'", clientClassName, e);
        }
        return fallback.get();
    }

    private static Item registerItem(String itemId, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MadnessCore.MOD_ID, itemId), item);
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MadnessCore.MOD_ID, name), item);
    }

    public static void initialize() {
        MadnessCore.LOGGER.info("Registering Items");
    }
}
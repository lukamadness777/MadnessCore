package dev.lukamadness.madnesscore.registry.items;

import dev.lukamadness.madnesscore.MadnessCore;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ModArmorMaterials {
    public static final RegistryEntry<ArmorMaterial> CLOTHES_MATERIAL = register(
            "clothes",
            Map.of(ArmorItem.Type.CHESTPLATE, 1),
            5,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            0f, 0f,
            Ingredient.EMPTY
    );

    public static final RegistryEntry<ArmorMaterial> BOOTS_MATERIAL = register(
            "clothes",
            Map.of(ArmorItem.Type.BOOTS, 1),
            5,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            0f, 0f,
            Ingredient.EMPTY
    );

    private static RegistryEntry<ArmorMaterial> register(
            String id,
            Map<ArmorItem.Type, Integer> defense,
            int enchantability,
            RegistryEntry<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Ingredient repairIngredient
    ) {
        EnumMap<ArmorItem.Type, Integer> defenseMap = new EnumMap<>(defense);

        return Registry.registerReference(
                Registries.ARMOR_MATERIAL,
                Identifier.of(MadnessCore.MOD_ID, id),
                new ArmorMaterial(defenseMap, enchantability, equipSound, () -> repairIngredient, List.of(), toughness, knockbackResistance)
        );
    }

    public static void register() {

    }
}

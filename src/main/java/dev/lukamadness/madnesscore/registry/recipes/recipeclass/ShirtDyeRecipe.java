package dev.lukamadness.madnesscore.registry.recipes.recipeclass;

import dev.lukamadness.madnesscore.helpers.DyeMixUtil;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableJacketItem;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableLegginsItem;
import dev.lukamadness.madnesscore.registry.items.armorclass.DyeableShirtItem;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitBootsItem;
import dev.lukamadness.madnesscore.registry.recipes.ModRecipes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ShirtDyeRecipe extends SpecialCraftingRecipe {

    public ShirtDyeRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    private static boolean isShirt(Item item) {
        return item instanceof DyeableShirtItem
                || item instanceof FormalSuitBootsItem
                || item instanceof DyeableJacketItem
                || item instanceof DyeableLegginsItem;
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean foundShirt = false;
        boolean foundDye = false;

        for (ItemStack stack : input.getStacks()) {

            if (stack.isEmpty()) continue;

            if (isShirt(stack.getItem())) {
                if (foundShirt) return false;
                foundShirt = true;
            }
            else if (stack.getItem() instanceof DyeItem) {
                foundDye = true;
            }
            else {
                return false;
            }
        }

        return foundShirt && foundDye;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {

        ItemStack shirt = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (ItemStack stack : input.getStacks()) {

            if (stack.isEmpty()) continue;

            if (isShirt(stack.getItem())) {
                shirt = stack.copy();
            }
            else if (stack.getItem() instanceof DyeItem dye) {
                dyes.add(dye);
            }
        }

        if (shirt.isEmpty())
            return ItemStack.EMPTY;

        if (!dyes.isEmpty()) {
            shirt.set(
                    DataComponentTypes.DYED_COLOR,
                    new DyedColorComponent(
                            DyeMixUtil.mixDyeColor(shirt, dyes),
                            false
                    )
            );
        }

        return shirt;
    }


    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHIRT_DYE;
    }
}

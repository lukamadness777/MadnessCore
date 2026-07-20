package dev.lukamadness.madnesscore.registry.recipes;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.recipes.recipeclass.FormalItemDyeRecipe;
import dev.lukamadness.madnesscore.registry.recipes.recipeclass.ShirtDyeRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {
    public static final RecipeSerializer<ShirtDyeRecipe> SHIRT_DYE =
            Registry.register(
                    Registries.RECIPE_SERIALIZER,
                    Identifier.of(MadnessCore.MOD_ID, "shirt_dye"),
                    new SpecialRecipeSerializer<>(ShirtDyeRecipe::new)
            );

    public static final RecipeSerializer<FormalItemDyeRecipe> FORMAL_DYE =
            Registry.register(
                    Registries.RECIPE_SERIALIZER,
                    Identifier.of(MadnessCore.MOD_ID, "formal_suit_dye"),
                    new SpecialRecipeSerializer<>(FormalItemDyeRecipe::new)
            );

    public static void registerRecipes() {

    }
}

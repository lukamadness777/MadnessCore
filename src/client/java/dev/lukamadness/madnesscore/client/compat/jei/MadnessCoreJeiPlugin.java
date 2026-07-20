package dev.lukamadness.madnesscore.client.compat.jei;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.blocks.ModBlocks;
import dev.lukamadness.madnesscore.tailoring.TailoringRecipeManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@JeiPlugin
public class MadnessCoreJeiPlugin implements IModPlugin {

    private static final Identifier PLUGIN_UID = Identifier.of(MadnessCore.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new TailoringRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                TailoringRecipeCategory.RECIPE_TYPE,
                TailoringRecipeManager.getInstance().getRecipes()
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TAILORING_TABLE.asItem()), TailoringRecipeCategory.RECIPE_TYPE);
    }
}
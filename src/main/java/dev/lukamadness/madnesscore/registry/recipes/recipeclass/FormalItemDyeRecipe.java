package dev.lukamadness.madnesscore.registry.recipes.recipeclass;

import dev.lukamadness.madnesscore.helpers.DyeMixUtil;
import dev.lukamadness.madnesscore.registry.items.armorclass.FormalSuitItem;
import dev.lukamadness.madnesscore.registry.recipes.ModRecipes;
import dev.lukamadness.madnesscore.util.DataComponents;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FormalItemDyeRecipe extends SpecialCraftingRecipe {

    public FormalItemDyeRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean foundFormal = false;
        boolean foundDye = false;
        boolean foundString = false;

        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof FormalSuitItem) {
                if (foundFormal) return false;
                foundFormal = true;
            } else if (stack.getItem() instanceof DyeItem) {
                foundDye = true;
            } else if (stack.getItem() == Items.PAPER) {
                continue;
            } else if (stack.getItem() == Items.STRING) {
                foundString = true;
            } else {
                return false;
            }
        }

        return foundFormal && (foundDye || foundString);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack formal = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();
        boolean hasPaper = false;
        boolean hasString = false;

        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof FormalSuitItem) {
                formal = stack.copy();
            } else if (stack.getItem() instanceof DyeItem dye) {
                dyes.add(dye);
            } else if (stack.getItem() == Items.PAPER) {
                hasPaper = true;
            } else if (stack.getItem() == Items.STRING) {
                hasString = true;
            }
        }

        if (formal.isEmpty()) {
            return ItemStack.EMPTY;
        }

        DataComponents.FormalColors colors = formal.get(DataComponents.FORMAL_COLORS);
        if (colors == null) {
            colors = DataComponents.FormalColors.DEFAULT;
        }

        if (hasString) {
            Boolean tieVisible = formal.get(DataComponents.TIE_VISIBLE);
            boolean newVisibility = !(tieVisible == null || tieVisible);
            formal.set(DataComponents.TIE_VISIBLE, newVisibility);
        }

        if (!dyes.isEmpty()) {
            int mixedColor = DyeMixUtil.mixDyeColor(formal, dyes);

            if (hasPaper) {
                colors = colors.withSuitColor(mixedColor);
            } else {
                colors = hasString ? colors.withTieColor(mixedColor) : colors.withShirtColor(mixedColor);
            }
        }

        formal.set(DataComponents.FORMAL_COLORS, colors);
        return formal;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORMAL_DYE;
    }
}
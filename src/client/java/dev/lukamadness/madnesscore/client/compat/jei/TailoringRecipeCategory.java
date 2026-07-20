package dev.lukamadness.madnesscore.client.compat.jei;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.blocks.ModBlocks;
import dev.lukamadness.madnesscore.tailoring.FabricRequirement;
import dev.lukamadness.madnesscore.tailoring.TailoringRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class TailoringRecipeCategory implements IRecipeCategory<TailoringRecipe> {

    public static final Identifier UID = Identifier.of(MadnessCore.MOD_ID, "tailoring");
    public static final RecipeType<TailoringRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, TailoringRecipe.class);

    private static final int WIDTH = 66;
    private static final int HEIGHT = 36;

    private static final int INPUT_SLOT_COUNT = 4;
    private static final int[][] INPUT_SLOT_POSITIONS = {
            {0, 0}, {18, 0}, {0, 18}, {18, 18}
    };
    private static final int OUTPUT_SLOT_X = 48;
    private static final int OUTPUT_SLOT_Y = 9;

    private final IDrawable icon;
    private final IDrawable slot;

    public TailoringRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(getCatalystStack());
        this.slot = guiHelper.getSlotDrawable();
    }

    private static ItemStack getCatalystStack() {
        return new ItemStack(ModBlocks.TAILORING_TABLE.asItem());
    }

    @Override
    public RecipeType<TailoringRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("madnesscore.jei.category.tailoring");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(TailoringRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext drawContext, double mouseX, double mouseY) {
        // El drawable de slot es 18x18 y el item se renderiza con 1px de inset,
        // así que lo dibujamos 1px arriba/izquierda de la posición del addSlot().
        for (int[] pos : INPUT_SLOT_POSITIONS) {
            slot.draw(drawContext, pos[0] - 1, pos[1] - 1);
        }
        slot.draw(drawContext, OUTPUT_SLOT_X - 1, OUTPUT_SLOT_Y - 1);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TailoringRecipe recipe, IFocusGroup focuses) {
        List<FabricRequirement> requirements = recipe.requirements();

        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            if (i >= requirements.size()) {
                continue;
            }
            FabricRequirement requirement = requirements.get(i);
            int[] pos = INPUT_SLOT_POSITIONS[i];

            builder.addSlot(RecipeIngredientRole.INPUT, pos[0], pos[1])
                    .addItemStack(new ItemStack(requirement.item(), requirement.count()));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y)
                .addItemStack(recipe.output());
    }
}
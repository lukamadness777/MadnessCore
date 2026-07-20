package dev.lukamadness.madnesscore.tailoring;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TailoringRecipe(Identifier id, List<FabricRequirement> requirements, ItemStack output) {

    public boolean matches(List<ItemStack> inputStacks) {
        Map<Item, Integer> provided = new HashMap<>();
        for (ItemStack stack : inputStacks) {
            if (!stack.isEmpty()) {
                provided.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        for (FabricRequirement req : requirements) {
            if (provided.getOrDefault(req.item(), 0) < req.count()) {
                return false;
            }
        }
        return true;
    }
}
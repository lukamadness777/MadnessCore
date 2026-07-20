package dev.lukamadness.madnesscore.tailoring;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lukamadness.madnesscore.MadnessCore;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TailoringRecipeManager implements SimpleSynchronousResourceReloadListener {

    private static final TailoringRecipeManager INSTANCE = new TailoringRecipeManager();
    private static final Gson GSON = new Gson();

    private List<TailoringRecipe> recipes = List.of();

    public static TailoringRecipeManager getInstance() {
        return INSTANCE;
    }

    public List<TailoringRecipe> getRecipes() {
        return recipes;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(MadnessCore.MOD_ID, "tailoring_recipes");
    }

    @Override
    public void reload(ResourceManager manager) {
        List<TailoringRecipe> loaded = new ArrayList<>();

        Map<Identifier, Resource> found = manager.findResources("tailoring_recipes",
                path -> path.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : found.entrySet()) {
            Identifier fileId = entry.getKey();
            try (BufferedReader reader = entry.getValue().getReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                loaded.add(parseRecipe(fileId, json));
            } catch (Exception e) {
                MadnessCore.LOGGER.error("No se pudo cargar la receta de tailoring '{}'", fileId, e);
            }
        }

        this.recipes = List.copyOf(loaded);
        MadnessCore.LOGGER.info("Cargadas {} recetas de tailoring", this.recipes.size());
    }

    private TailoringRecipe parseRecipe(Identifier fileId, JsonObject json) {
        JsonObject outputJson = json.getAsJsonObject("output");
        Item outputItem = Registries.ITEM.get(Identifier.of(outputJson.get("item").getAsString()));
        int outputCount = outputJson.has("count") ? outputJson.get("count").getAsInt() : 1;
        ItemStack output = new ItemStack(outputItem, outputCount);

        List<FabricRequirement> requirements = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("ingredients")) {
            JsonObject ingredientJson = element.getAsJsonObject();
            Item item = Registries.ITEM.get(Identifier.of(ingredientJson.get("item").getAsString()));
            int count = ingredientJson.has("count") ? ingredientJson.get("count").getAsInt() : 1;
            requirements.add(new FabricRequirement(item, count));
        }

        return new TailoringRecipe(fileId, requirements, output);
    }
}
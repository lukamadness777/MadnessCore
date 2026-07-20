package dev.lukamadness.madnesscore.network;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.tailoring.FabricRequirement;
import dev.lukamadness.madnesscore.tailoring.TailoringRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record TailoringRecipeListPayload(List<RecipeEntry> recipes) implements CustomPayload {

    public static final CustomPayload.Id<TailoringRecipeListPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MadnessCore.MOD_ID, "tailoring_recipe_list"));

    public static final PacketCodec<PacketByteBuf, TailoringRecipeListPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeVarInt(payload.recipes.size());
                for (RecipeEntry entry : payload.recipes) {
                    buf.writeIdentifier(entry.id());
                    buf.writeIdentifier(Registries.ITEM.getId(entry.output().getItem()));
                    buf.writeVarInt(entry.output().getCount());
                    buf.writeVarInt(entry.requirements().size());
                    for (FabricRequirement req : entry.requirements()) {
                        buf.writeIdentifier(Registries.ITEM.getId(req.item()));
                        buf.writeVarInt(req.count());
                    }
                }
            },
            buf -> {
                int size = buf.readVarInt();
                List<RecipeEntry> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    Identifier id = buf.readIdentifier();
                    Item outputItem = Registries.ITEM.get(buf.readIdentifier());
                    int outputCount = buf.readVarInt();

                    int reqCount = buf.readVarInt();
                    List<FabricRequirement> requirements = new ArrayList<>(reqCount);
                    for (int j = 0; j < reqCount; j++) {
                        Item reqItem = Registries.ITEM.get(buf.readIdentifier());
                        int count = buf.readVarInt();
                        requirements.add(new FabricRequirement(reqItem, count));
                    }

                    list.add(new RecipeEntry(id, new ItemStack(outputItem, outputCount), requirements));
                }
                return new TailoringRecipeListPayload(list);
            }
    );

    public static TailoringRecipeListPayload of(List<TailoringRecipe> recipes) {
        List<RecipeEntry> entries = new ArrayList<>(recipes.size());
        for (TailoringRecipe recipe : recipes) {
            entries.add(new RecipeEntry(recipe.id(), recipe.output(), recipe.requirements()));
        }
        return new TailoringRecipeListPayload(entries);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record RecipeEntry(Identifier id, ItemStack output, List<FabricRequirement> requirements) {
    }
}
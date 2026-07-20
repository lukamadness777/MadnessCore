package dev.lukamadness.madnesscore.example;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.api.BloodlineRegistry;
import dev.lukamadness.madnesscore.api.FamilyRegistry;
import dev.lukamadness.madnesscore.api.Species;
import dev.lukamadness.madnesscore.api.SpeciesRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ExampleContent {

    private ExampleContent() {}

    public static void register() {
        Species vampire = SpeciesRegistry.register(MadnessCore.MOD_ID, "vampire", Text.literal("§4Vampire"));

        BloodlineRegistry.register(Identifier.of(MadnessCore.MOD_ID, "elder_vampire"),
                vampire.id(), "§4Elder Vampire", 5);
        BloodlineRegistry.register(Identifier.of(MadnessCore.MOD_ID, "fledgling_vampire"),
                vampire.id(), "§cFledgling Vampire", 20);

        BloodlineRegistry.register(Identifier.of(MadnessCore.MOD_ID, "hunter"),
                SpeciesRegistry.HUMAN_ID, "§eHunter", 10);
        BloodlineRegistry.register(Identifier.of(MadnessCore.MOD_ID, "mage"),
                SpeciesRegistry.HUMAN_ID, "§dMage", 8);
        BloodlineRegistry.register(Identifier.of(MadnessCore.MOD_ID, "commoner"),
                SpeciesRegistry.HUMAN_ID, "§fCommoner", 30);

        FamilyRegistry.registerForAll(Identifier.of(MadnessCore.MOD_ID, "cursed"), "§8Cursed", 1);
    }
}
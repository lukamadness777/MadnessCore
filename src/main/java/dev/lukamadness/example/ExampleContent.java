// dev/lukamadness/madnesscore/example/ExampleContent.java
package dev.lukamadness.madnesscore.example;

import dev.lukamadness.madnesscore.api.BloodlineRegistry;
import dev.lukamadness.madnesscore.api.FamilyRegistry;
import dev.lukamadness.madnesscore.api.Species;
import dev.lukamadness.madnesscore.api.SpeciesRegistry;
import net.minecraft.util.Identifier;

public final class ExampleContent {

    private ExampleContent() {}

    public static void register() {
        Species vampire = SpeciesRegistry.register("madnesscore", "vampire", "§4Vampire");

        BloodlineRegistry.register(Identifier.of("madnesscore", "elder_vampire"),
                vampire.id(), "§4Elder Vampire", 5);      // más raro
        BloodlineRegistry.register(Identifier.of("madnesscore", "fledgling_vampire"),
                vampire.id(), "§cFledgling Vampire", 20); // más común

        BloodlineRegistry.register(Identifier.of("madnesscore", "hunter"),
                SpeciesRegistry.HUMAN_ID, "§eHunter", 10);
        BloodlineRegistry.register(Identifier.of("madnesscore", "mage"),
                SpeciesRegistry.HUMAN_ID, "§dMage", 8);
        BloodlineRegistry.register(Identifier.of("madnesscore", "commoner"),
                SpeciesRegistry.HUMAN_ID, "§fCommoner", 30);

        BloodlineRegistry.register(Identifier.of("madnesscore", "eldian"),
                SpeciesRegistry.HUMAN_ID, "§bEldian", 8);

        // ── Familias: un tercer nivel debajo de un bloodline ────────────────
        // Ejemplo: dentro del bloodline "ackerman", dos ramas de familia.
        FamilyRegistry.register(Identifier.of("madnesscore", "ackerman"),
                Identifier.of("madnesscore", "eldian"), "§4(Ackerman)", 5);
        FamilyRegistry.register(Identifier.of("madnesscore", "royal"),
                Identifier.of("madnesscore", "eldian"), "§6(Royal Blood)", 5);

        // Familia que aplica a CUALQUIER bloodline (ej: un rasgo transversal).
        FamilyRegistry.registerForAll(Identifier.of("madnesscore", "cursed"), "§8Maldito", 1);
    }
}
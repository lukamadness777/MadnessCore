package dev.lukamadness.madnesscore.api;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record Bloodline(Identifier id, Identifier speciesId, Text displayName, double weight) {

    public boolean appliesTo(Identifier speciesId) {
        return this.speciesId == null || this.speciesId.equals(speciesId);
    }

    public boolean isAll() {
        return speciesId == null;
    }
}
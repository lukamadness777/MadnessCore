package dev.lukamadness.madnesscore.api;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record Family(Identifier id, Identifier bloodlineId, Text displayName, double weight, boolean hidden) {

    public boolean appliesTo(Identifier bloodlineId) {
        return this.bloodlineId == null || this.bloodlineId.equals(bloodlineId);
    }

    public boolean isAll() {
        return bloodlineId == null;
    }
}
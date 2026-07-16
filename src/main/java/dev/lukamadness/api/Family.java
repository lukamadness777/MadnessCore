// dev/lukamadness/madnesscore/api/Family.java
package dev.lukamadness.madnesscore.api;

import net.minecraft.util.Identifier;

/**
 * Un tercer nivel debajo de Bloodline (ej: "Ackerman", "Royal" dentro de un bloodline "eldian").
 * bloodlineId == null significa que la familia aplica a CUALQUIER bloodline (o incluso a "sin bloodline").
 */
public record Family(Identifier id, Identifier bloodlineId, String displayName, double weight) {

    public boolean appliesTo(Identifier bloodlineId) {
        return this.bloodlineId == null || this.bloodlineId.equals(bloodlineId);
    }

    public boolean isAll() {
        return bloodlineId == null;
    }
}
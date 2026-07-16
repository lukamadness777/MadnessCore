package dev.lukamadness.madnesscore.api;

import net.minecraft.util.Identifier;

/**
 * Una raza/especie jugable (ej: "human", "eldian", "marleyan").
 * Se registra mediante {@link SpeciesRegistry#register(Identifier, String, double)}.
 *
 * @param weight peso relativo usado al rollear la especie de un jugador nuevo.
 *               No es un porcentaje fijo: se normaliza contra la suma de todos los
 *               weights registrados. Ej: Human(1) + Vampire(1) = 50%/50%.
 *               Si agregas Elf(2), pasa a ser Human 25% / Vampire 25% / Elf 50%.
 *               weight <= 0 significa "nunca se rollea, solo asignable por comando/API".
 */
public record Species(Identifier id, String displayName, double weight) {
}
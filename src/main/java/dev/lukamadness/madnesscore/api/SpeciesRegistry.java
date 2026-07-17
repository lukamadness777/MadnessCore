package dev.lukamadness.madnesscore.api;

import dev.lukamadness.madnesscore.MadnessCore;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro público de especies. "human" existe siempre por defecto (weight = 1).
 * Otros mods llaman a {@link #register} en su ModInitializer para añadir las suyas.
 */
public final class SpeciesRegistry {

    private SpeciesRegistry() {}

    private static final Map<Identifier, Species> SPECIES = new LinkedHashMap<>();

    public static final Identifier HUMAN_ID = Identifier.of(MadnessCore.MOD_ID, "human");
    public static final Species HUMAN = register(HUMAN_ID, Text.translatable("madnesscore.species.human"), 1.0);

    /** weight = 1.0 por defecto (misma chance relativa que Human). */
    public static Species register(Identifier id, Text displayName) {
        return register(id, displayName, 1.0);
    }

    public static Species register(Identifier id, Text displayName, double weight) {
        if (SPECIES.containsKey(id)) {
            throw new IllegalStateException("Species already registered: " + id);
        }
        Species species = new Species(id, displayName, weight);
        SPECIES.put(id, species);
        return species;
    }

    public static Species register(String namespace, String path, Text displayName) {
        return register(Identifier.of(namespace, path), displayName, 1.0);
    }

    public static Species register(String namespace, String path, Text displayName, double weight) {
        return register(Identifier.of(namespace, path), displayName, weight);
    }

    public static boolean exists(Identifier id) {
        return SPECIES.containsKey(id);
    }

    public static Species get(Identifier id) {
        return SPECIES.get(id);
    }

    public static Species getOrHuman(Identifier id) {
        return SPECIES.getOrDefault(id, HUMAN);
    }

    public static Map<Identifier, Species> getAll() {
        return Collections.unmodifiableMap(SPECIES);
    }

    /**
     * Cambia el weight de una especie ya registrada (útil para reajustar Human,
     * que se registra automáticamente con weight=1 al cargar la clase).
     * Ej: {@code SpeciesRegistry.setWeight(SpeciesRegistry.HUMAN_ID, 3.0)} para
     * hacerlo 3 veces más probable que una especie con weight=1.
     */
    public static void setWeight(Identifier id, double newWeight) {
        Species current = SPECIES.get(id);
        if (current == null) {
            throw new IllegalStateException("Cannot set weight, species not registered: " + id);
        }
        SPECIES.put(id, new Species(current.id(), current.displayName(), newWeight));
    }

    /**
     * Rollea una especie al azar, ponderada por weight entre TODAS las registradas
     * (con weight > 0). Si no hay ninguna elegible, cae en Human.
     */
    public static Identifier rollAny() {
        List<Species> candidates = new ArrayList<>();
        double totalWeight = 0;
        for (Species s : SPECIES.values()) {
            if (s.weight() > 0) {
                candidates.add(s);
                totalWeight += s.weight();
            }
        }
        if (candidates.isEmpty() || totalWeight <= 0) {
            return HUMAN_ID;
        }
        double roll = Math.random() * totalWeight;
        double cumulative = 0;
        for (Species s : candidates) {
            cumulative += s.weight();
            if (roll < cumulative) {
                return s.id();
            }
        }
        return candidates.get(candidates.size() - 1).id();
    }
}
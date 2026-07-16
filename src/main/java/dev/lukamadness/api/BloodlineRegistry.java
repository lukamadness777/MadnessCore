package dev.lukamadness.madnesscore.api;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BloodlineRegistry {

    private BloodlineRegistry() {}

    private static final Map<Identifier, Bloodline> BLOODLINES = new LinkedHashMap<>();

    public static Bloodline register(Identifier id, Identifier speciesId, String displayName, double weight) {
        if (BLOODLINES.containsKey(id)) {
            throw new IllegalStateException("Bloodline already registered: " + id);
        }
        if (speciesId != null && !SpeciesRegistry.exists(speciesId)) {
            throw new IllegalStateException("Cannot register bloodline '" + id + "' for unknown species: " + speciesId);
        }
        Bloodline bloodline = new Bloodline(id, speciesId, displayName, weight);
        BLOODLINES.put(id, bloodline);
        return bloodline;
    }

    public static Bloodline registerForAll(Identifier id, String displayName, double weight) {
        return register(id, null, displayName, weight);
    }

    public static Bloodline get(Identifier id) {
        return BLOODLINES.get(id);
    }

    public static boolean exists(Identifier id) {
        return BLOODLINES.containsKey(id);
    }

    public static Map<Identifier, Bloodline> getAll() {
        return Collections.unmodifiableMap(BLOODLINES);
    }

    public static List<Bloodline> getForSpecies(Identifier speciesId) {
        List<Bloodline> result = new ArrayList<>();
        for (Bloodline b : BLOODLINES.values()) {
            if (b.appliesTo(speciesId)) {
                result.add(b);
            }
        }
        return result;
    }

    public static Bloodline rollFor(Identifier speciesId) {
        List<Bloodline> candidates = new ArrayList<>();
        double totalWeight = 0;
        for (Bloodline b : getForSpecies(speciesId)) {
            if (b.weight() > 0) {
                candidates.add(b);
                totalWeight += b.weight();
            }
        }
        if (candidates.isEmpty() || totalWeight <= 0) {
            return null;
        }
        double roll = Math.random() * totalWeight;
        double cumulative = 0;
        for (Bloodline b : candidates) {
            cumulative += b.weight();
            if (roll < cumulative) {
                return b;
            }
        }
        return candidates.get(candidates.size() - 1);
    }
}
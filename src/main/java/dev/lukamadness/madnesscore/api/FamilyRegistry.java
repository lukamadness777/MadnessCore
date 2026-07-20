package dev.lukamadness.madnesscore.api;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FamilyRegistry {

    private FamilyRegistry() {}

    private static final Map<Identifier, Family> FAMILIES = new LinkedHashMap<>();

    public static Family register(Identifier id, Identifier bloodlineId, Text displayName, double weight) {
        return register(id, bloodlineId, displayName, weight, false);
    }

    public static Family register(Identifier id, Identifier bloodlineId, Text displayName, double weight, boolean hidden) {
        if (FAMILIES.containsKey(id)) {
            throw new IllegalStateException("Family already registered: " + id);
        }
        if (bloodlineId != null && !BloodlineRegistry.exists(bloodlineId)) {
            throw new IllegalStateException("Cannot register family '" + id + "' for unknown bloodline: " + bloodlineId);
        }
        Family family = new Family(id, bloodlineId, displayName, weight, hidden);
        FAMILIES.put(id, family);
        return family;
    }

    public static Family register(Identifier id, Identifier bloodlineId, String displayName, double weight) {
        return register(id, bloodlineId, Text.literal(displayName), weight, false);
    }

    public static Family register(Identifier id, Identifier bloodlineId, String displayName, double weight, boolean hidden) {
        return register(id, bloodlineId, Text.literal(displayName), weight, hidden);
    }

    public static Family registerForAll(Identifier id, Text displayName, double weight) {
        return register(id, null, displayName, weight, false);
    }

    public static Family registerForAll(Identifier id, Text displayName, double weight, boolean hidden) {
        return register(id, null, displayName, weight, hidden);
    }

    public static Family registerForAll(Identifier id, String displayName, double weight) {
        return register(id, null, Text.literal(displayName), weight, false);
    }

    public static Family registerForAll(Identifier id, String displayName, double weight, boolean hidden) {
        return register(id, null, Text.literal(displayName), weight, hidden);
    }

    public static Family get(Identifier id) {
        return FAMILIES.get(id);
    }

    public static boolean exists(Identifier id) {
        return FAMILIES.containsKey(id);
    }

    public static Map<Identifier, Family> getAll() {
        return Collections.unmodifiableMap(FAMILIES);
    }

    public static List<Family> getForBloodline(Identifier bloodlineId) {
        List<Family> result = new ArrayList<>();
        for (Family f : FAMILIES.values()) {
            if (f.appliesTo(bloodlineId)) {
                result.add(f);
            }
        }
        return result;
    }

    public static Family rollFor(Identifier bloodlineId) {
        List<Family> candidates = new ArrayList<>();
        double totalWeight = 0;
        for (Family f : getForBloodline(bloodlineId)) {
            if (f.weight() > 0) {
                candidates.add(f);
                totalWeight += f.weight();
            }
        }
        if (candidates.isEmpty() || totalWeight <= 0) {
            return null;
        }
        double roll = Math.random() * totalWeight;
        double cumulative = 0;
        for (Family f : candidates) {
            cumulative += f.weight();
            if (roll < cumulative) {
                return f;
            }
        }
        return candidates.get(candidates.size() - 1);
    }
}
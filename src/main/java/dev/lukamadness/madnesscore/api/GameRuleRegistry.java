package dev.lukamadness.madnesscore.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GameRuleRegistry {

    private GameRuleRegistry() {}

    private static final Map<String, GameRule> RULES = new LinkedHashMap<>();

    public static final GameRule REROLL_SPECIES_ON_DEATH = register("rerollSpecieDeath", false);
    public static final GameRule REROLL_BLOODLINE_ON_DEATH = register("rerollBloodlineDeath", false);
    public static final GameRule REROLL_FAMILY_ON_DEATH = register("rerollFamilyDeath", false);

    public static GameRule register(String key, boolean defaultValue) {
        if (RULES.containsKey(key)) {
            throw new IllegalStateException("Game rule already registered: " + key);
        }
        GameRule rule = new GameRule(key, defaultValue);
        RULES.put(key, rule);
        return rule;
    }

    public static boolean exists(String key) {
        return RULES.containsKey(key);
    }

    public static GameRule get(String key) {
        return RULES.get(key);
    }

    public static Map<String, GameRule> getAll() {
        return Collections.unmodifiableMap(RULES);
    }
}
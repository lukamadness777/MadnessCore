package dev.lukamadness.madnesscore.data;

import dev.lukamadness.madnesscore.api.GameRule;
import dev.lukamadness.madnesscore.api.GameRuleRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class GameRuleData extends PersistentState {

    public static final String ID = "madnesscore_gamerules";

    /** Solo guarda overrides; si una key no está acá, se usa el defaultValue del registro. */
    private final Map<String, Boolean> overrides = new HashMap<>();

    public boolean get(GameRule rule) {
        return overrides.getOrDefault(rule.key(), rule.defaultValue());
    }

    public boolean get(String key) {
        GameRule rule = GameRuleRegistry.get(key);
        boolean fallback = rule != null && rule.defaultValue();
        return overrides.getOrDefault(key, fallback);
    }

    public void set(String key, boolean value) {
        overrides.put(key, value);
        markDirty();
    }

    // ── NBT ──────────────────────────────────────────────────────────────

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound map = new NbtCompound();
        overrides.forEach(map::putBoolean);
        nbt.put("rules", map);
        return nbt;
    }

    public static GameRuleData readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        GameRuleData state = new GameRuleData();
        NbtCompound map = nbt.getCompound("rules");
        for (String key : map.getKeys()) {
            state.overrides.put(key, map.getBoolean(key));
        }
        return state;
    }

    private static final Type<GameRuleData> TYPE = new Type<>(
            GameRuleData::new,
            GameRuleData::readNbt,
            null
    );

    public static GameRuleData get(MinecraftServer server) {
        PersistentStateManager manager = server
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager();
        return manager.getOrCreate(TYPE, ID);
    }
}
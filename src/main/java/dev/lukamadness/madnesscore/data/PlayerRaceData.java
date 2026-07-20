package dev.lukamadness.madnesscore.data;

import dev.lukamadness.madnesscore.api.SpeciesRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerRaceData extends PersistentState {

    public static final String ID = "madnesscore_players";

    public record PlayerEntry(Identifier speciesId, Identifier bloodlineId, Identifier familyId) {
        public PlayerEntry withSpecies(Identifier newSpecies) {
            return new PlayerEntry(newSpecies, bloodlineId, familyId);
        }
        public PlayerEntry withBloodline(Identifier newBloodline) {
            return new PlayerEntry(speciesId, newBloodline, familyId);
        }
        public PlayerEntry withFamily(Identifier newFamily) {
            return new PlayerEntry(speciesId, bloodlineId, newFamily);
        }
    }

    private final Map<UUID, PlayerEntry> players = new HashMap<>();

    public Map<UUID, PlayerEntry> getAll() {
        return Collections.unmodifiableMap(players);
    }

    public boolean has(UUID uuid) {
        return players.containsKey(uuid);
    }

    public PlayerEntry get(UUID uuid) {
        return players.getOrDefault(uuid, new PlayerEntry(SpeciesRegistry.HUMAN_ID, null, null));
    }

    public void set(UUID uuid, PlayerEntry entry) {
        players.put(uuid, entry);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound map = new NbtCompound();
        players.forEach((uuid, entry) -> {
            NbtCompound data = new NbtCompound();
            data.putString("species", entry.speciesId().toString());
            if (entry.bloodlineId() != null) {
                data.putString("bloodline", entry.bloodlineId().toString());
            }
            if (entry.familyId() != null) {
                data.putString("family", entry.familyId().toString());
            }
            map.put(uuid.toString(), data);
        });
        nbt.put("players", map);
        return nbt;
    }

    public static PlayerRaceData readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        PlayerRaceData state = new PlayerRaceData();
        NbtCompound map = nbt.getCompound("players");
        for (String key : map.getKeys()) {
            NbtCompound data = map.getCompound(key);
            Identifier species = Identifier.tryParse(data.getString("species"));
            if (species == null) species = SpeciesRegistry.HUMAN_ID;
            Identifier bloodline = data.contains("bloodline")
                    ? Identifier.tryParse(data.getString("bloodline"))
                    : null;
            Identifier family = data.contains("family")
                    ? Identifier.tryParse(data.getString("family"))
                    : null;
            state.players.put(UUID.fromString(key), new PlayerEntry(species, bloodline, family));
        }
        return state;
    }

    private static final Type<PlayerRaceData> TYPE = new Type<>(
            PlayerRaceData::new,
            PlayerRaceData::readNbt,
            null
    );

    public static PlayerRaceData get(MinecraftServer server) {
        PersistentStateManager manager = server
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager();
        return manager.getOrCreate(TYPE, ID);
    }
}
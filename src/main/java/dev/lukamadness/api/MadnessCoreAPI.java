// dev/lukamadness/madnesscore/api/MadnessCoreAPI.java
package dev.lukamadness.madnesscore.api;

import dev.lukamadness.madnesscore.data.PlayerRaceData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * Punto de entrada público de MadnessCore. Otros mods solo deberían necesitar
 * esta clase junto con {@link SpeciesRegistry} / {@link BloodlineRegistry} / {@link FamilyRegistry}.
 */
public final class MadnessCoreAPI {

    private MadnessCoreAPI() {}

    // ── Species ──────────────────────────────────────────────────────────

    public static Species getSpecies(MinecraftServer server, UUID uuid) {
        Identifier id = PlayerRaceData.get(server).get(uuid).speciesId();
        return SpeciesRegistry.getOrHuman(id);
    }

    public static boolean isSpecies(MinecraftServer server, UUID uuid, Identifier speciesId) {
        return getSpecies(server, uuid).id().equals(speciesId);
    }

    /**
     * Cambia la especie del jugador. Si el bloodline actual ya no aplica a la nueva especie,
     * se limpia automáticamente (y con él, la familia, ya que dependía de ese bloodline).
     * No rollea nada nuevo — para eso usar {@link #setSpeciesAndReroll}.
     */
    public static void setSpecies(MinecraftServer server, UUID uuid, Identifier speciesId) {
        if (!SpeciesRegistry.exists(speciesId)) {
            throw new IllegalArgumentException("Unknown species: " + speciesId);
        }
        PlayerRaceData data = PlayerRaceData.get(server);
        PlayerRaceData.PlayerEntry current = data.get(uuid);
        PlayerRaceData.PlayerEntry updated = current.withSpecies(speciesId);

        Bloodline currentBloodline = current.bloodlineId() == null ? null : BloodlineRegistry.get(current.bloodlineId());
        if (currentBloodline != null && !currentBloodline.appliesTo(speciesId)) {
            updated = updated.withBloodline(null).withFamily(null);
        }
        data.set(uuid, updated);
    }

    /** Cambia la especie Y rerollea bloodline + familia para la nueva especie (usado por /madnesscore species set). */
    public static void setSpeciesAndReroll(MinecraftServer server, UUID uuid, Identifier speciesId) {
        setSpecies(server, uuid, speciesId);
        rollBloodline(server, uuid);
        rollFamily(server, uuid);
    }

    // ── Bloodline ────────────────────────────────────────────────────────

    /** Devuelve null si el jugador no tiene bloodline actualmente. */
    public static Bloodline getBloodline(MinecraftServer server, UUID uuid) {
        Identifier bloodlineId = PlayerRaceData.get(server).get(uuid).bloodlineId();
        return bloodlineId == null ? null : BloodlineRegistry.get(bloodlineId);
    }

    public static boolean hasBloodline(MinecraftServer server, UUID uuid) {
        return getBloodline(server, uuid) != null;
    }

    public static boolean isBloodline(MinecraftServer server, UUID uuid, Identifier bloodlineId) {
        Bloodline current = getBloodline(server, uuid);
        return current != null && current.id().equals(bloodlineId);
    }

    /**
     * Pasa bloodlineId == null para limpiar el bloodline del jugador.
     * Si la familia actual ya no aplica al nuevo bloodline, se limpia automáticamente.
     */
    public static void setBloodline(MinecraftServer server, UUID uuid, Identifier bloodlineId) {
        if (bloodlineId != null) {
            Bloodline bloodline = BloodlineRegistry.get(bloodlineId);
            if (bloodline == null) {
                throw new IllegalArgumentException("Unknown bloodline: " + bloodlineId);
            }
            Identifier currentSpecies = PlayerRaceData.get(server).get(uuid).speciesId();
            if (!bloodline.appliesTo(currentSpecies)) {
                throw new IllegalArgumentException("Bloodline '" + bloodlineId
                        + "' is not available for species '" + currentSpecies + "'");
            }
        }
        PlayerRaceData data = PlayerRaceData.get(server);
        PlayerRaceData.PlayerEntry current = data.get(uuid);
        PlayerRaceData.PlayerEntry updated = current.withBloodline(bloodlineId);

        Family currentFamily = current.familyId() == null ? null : FamilyRegistry.get(current.familyId());
        if (currentFamily != null && !currentFamily.appliesTo(bloodlineId)) {
            updated = updated.withFamily(null);
        }
        data.set(uuid, updated);
    }

    /** Rollea un bloodline ponderado para la especie actual del jugador (puede quedar en null). */
    public static Bloodline rollBloodline(MinecraftServer server, UUID uuid) {
        Identifier speciesId = PlayerRaceData.get(server).get(uuid).speciesId();
        Bloodline rolled = BloodlineRegistry.rollFor(speciesId);
        setBloodline(server, uuid, rolled == null ? null : rolled.id());
        return rolled;
    }

    // ── Family ───────────────────────────────────────────────────────────

    /** Devuelve null si el jugador no tiene familia actualmente (el default es "none"). */
    public static Family getFamily(MinecraftServer server, UUID uuid) {
        Identifier familyId = PlayerRaceData.get(server).get(uuid).familyId();
        return familyId == null ? null : FamilyRegistry.get(familyId);
    }

    public static boolean hasFamily(MinecraftServer server, UUID uuid) {
        return getFamily(server, uuid) != null;
    }

    public static boolean isFamily(MinecraftServer server, UUID uuid, Identifier familyId) {
        Family current = getFamily(server, uuid);
        return current != null && current.id().equals(familyId);
    }

    /** Pasa familyId == null para limpiar la familia del jugador. */
    public static void setFamily(MinecraftServer server, UUID uuid, Identifier familyId) {
        if (familyId != null) {
            Family family = FamilyRegistry.get(familyId);
            if (family == null) {
                throw new IllegalArgumentException("Unknown family: " + familyId);
            }
            Identifier currentBloodline = PlayerRaceData.get(server).get(uuid).bloodlineId();
            if (!family.appliesTo(currentBloodline)) {
                throw new IllegalArgumentException("Family '" + familyId
                        + "' is not available for bloodline '" + currentBloodline + "'");
            }
        }
        PlayerRaceData data = PlayerRaceData.get(server);
        PlayerRaceData.PlayerEntry current = data.get(uuid);
        data.set(uuid, current.withFamily(familyId));
    }

    /** Rollea una familia ponderada para el bloodline actual del jugador (puede quedar en null). */
    public static Family rollFamily(MinecraftServer server, UUID uuid) {
        Identifier bloodlineId = PlayerRaceData.get(server).get(uuid).bloodlineId();
        Family rolled = FamilyRegistry.rollFor(bloodlineId);
        setFamily(server, uuid, rolled == null ? null : rolled.id());
        return rolled;
    }

    // ── Combinados ───────────────────────────────────────────────────────

    /** Snapshot conveniente de especie + bloodline + familia de un jugador. */
    public record RaceProfile(Species species, Bloodline bloodline, Family family) {
        public String describe() {
            return "Species: " + species.displayName()
                    + ", Bloodline: " + (bloodline != null ? bloodline.displayName() : "None")
                    + ", Family: " + (family != null ? family.displayName() : "None");
        }
    }

    public static RaceProfile getProfile(MinecraftServer server, UUID uuid) {
        return new RaceProfile(getSpecies(server, uuid), getBloodline(server, uuid), getFamily(server, uuid));
    }

    /**
     * Se llama una vez por jugador en su primer join:
     * 1) rollea la especie (ponderada entre todas las registradas)
     * 2) rollea un bloodline para esa especie
     * 3) rollea una familia para ese bloodline.
     * Si el jugador ya tiene datos guardados, no toca nada y devuelve {@code null}.
     */
    public static PlayerRaceData.PlayerEntry ensureInitialized(MinecraftServer server, UUID uuid) {
        PlayerRaceData data = PlayerRaceData.get(server);
        if (data.has(uuid)) {
            return null;
        }
        Identifier rolledSpecies = SpeciesRegistry.rollAny();
        data.set(uuid, new PlayerRaceData.PlayerEntry(rolledSpecies, null, null));
        rollBloodline(server, uuid);
        rollFamily(server, uuid);
        return data.get(uuid);
    }
}
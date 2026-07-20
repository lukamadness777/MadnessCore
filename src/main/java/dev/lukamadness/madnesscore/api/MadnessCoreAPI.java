package dev.lukamadness.madnesscore.api;

import dev.lukamadness.madnesscore.data.PlayerRaceData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.UUID;

public final class MadnessCoreAPI {

    private MadnessCoreAPI() {}

    public static Species getSpecies(MinecraftServer server, UUID uuid) {
        Identifier id = PlayerRaceData.get(server).get(uuid).speciesId();
        return SpeciesRegistry.getOrHuman(id);
    }

    public static boolean isSpecies(MinecraftServer server, UUID uuid, Identifier speciesId) {
        return getSpecies(server, uuid).id().equals(speciesId);
    }

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

    public static void setSpeciesAndReroll(MinecraftServer server, UUID uuid, Identifier speciesId) {
        setSpecies(server, uuid, speciesId);
        rollBloodline(server, uuid);
        rollFamily(server, uuid);
    }

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

    public static Bloodline rollBloodline(MinecraftServer server, UUID uuid) {
        Identifier speciesId = PlayerRaceData.get(server).get(uuid).speciesId();
        Bloodline rolled = BloodlineRegistry.rollFor(speciesId);
        setBloodline(server, uuid, rolled == null ? null : rolled.id());
        return rolled;
    }

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

    public static Family rollFamily(MinecraftServer server, UUID uuid) {
        Identifier bloodlineId = PlayerRaceData.get(server).get(uuid).bloodlineId();
        Family rolled = FamilyRegistry.rollFor(bloodlineId);
        setFamily(server, uuid, rolled == null ? null : rolled.id());
        return rolled;
    }

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
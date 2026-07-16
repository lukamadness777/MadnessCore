// dev/lukamadness/madnesscore/event/PlayerRespawnHandler.java
package dev.lukamadness.madnesscore.event;

import dev.lukamadness.madnesscore.api.Bloodline;
import dev.lukamadness.madnesscore.api.Family;
import dev.lukamadness.madnesscore.api.GameRuleRegistry;
import dev.lukamadness.madnesscore.api.MadnessCoreAPI;
import dev.lukamadness.madnesscore.api.Species;
import dev.lukamadness.madnesscore.api.SpeciesRegistry;
import dev.lukamadness.madnesscore.data.GameRuleData;
import dev.lukamadness.madnesscore.util.MadnessLang;
import dev.lukamadness.madnesscore.util.RaceText;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public final class PlayerRespawnHandler {

    private PlayerRespawnHandler() {}

    public static void register() {
        // alive == true significa que el jugador viejo seguía vivo (ej: volvió del End) -> no es una muerte real.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;

            MinecraftServer server = newPlayer.getServer();
            if (server == null) return;

            GameRuleData rules = GameRuleData.get(server);
            boolean rerollSpecies = rules.get(GameRuleRegistry.REROLL_SPECIES_ON_DEATH);
            boolean rerollBloodline = rules.get(GameRuleRegistry.REROLL_BLOODLINE_ON_DEATH);
            boolean rerollFamily = rules.get(GameRuleRegistry.REROLL_FAMILY_ON_DEATH);
            if (!rerollSpecies && !rerollBloodline && !rerollFamily) return;

            UUID uuid = newPlayer.getUuid();
            Species oldSpecies = MadnessCoreAPI.getSpecies(server, uuid);
            Bloodline oldBloodline = MadnessCoreAPI.getBloodline(server, uuid);
            Family oldFamily = MadnessCoreAPI.getFamily(server, uuid);

            if (rerollSpecies) {
                Identifier newSpeciesId = SpeciesRegistry.rollAny();
                MadnessCoreAPI.setSpecies(server, uuid, newSpeciesId);
                // setSpecies ya limpia bloodline/familia si dejaron de aplicar.
            }
            if (rerollBloodline) {
                MadnessCoreAPI.rollBloodline(server, uuid);
                // rollBloodline -> setBloodline ya limpia la familia si dejó de aplicar.
            }
            if (rerollFamily) {
                MadnessCoreAPI.rollFamily(server, uuid);
            }

            MadnessCoreAPI.RaceProfile profile = MadnessCoreAPI.getProfile(server, uuid);
            boolean speciesChanged = !profile.species().id().equals(oldSpecies.id());
            boolean bloodlineChanged = !Objects.equals(profile.bloodline(), oldBloodline);
            boolean familyChanged = !Objects.equals(profile.family(), oldFamily);
            if (!speciesChanged && !bloodlineChanged && !familyChanged) return;

            newPlayer.sendMessage(Text.translatable(MadnessLang.PLAYER_REBORN_AS, RaceText.identity(profile)), false);
        });
    }
}
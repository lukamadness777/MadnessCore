package dev.lukamadness.madnesscore.event;

import dev.lukamadness.madnesscore.api.Bloodline;
import dev.lukamadness.madnesscore.api.GameRuleRegistry;
import dev.lukamadness.madnesscore.api.MadnessCoreAPI;
import dev.lukamadness.madnesscore.api.Species;
import dev.lukamadness.madnesscore.api.SpeciesRegistry;
import dev.lukamadness.madnesscore.data.GameRuleData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public final class PlayerDeathHandler {

    private PlayerDeathHandler() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;

            MinecraftServer server = player.getServer();
            if (server == null) return;

            GameRuleData rules = GameRuleData.get(server);
            boolean rerollSpecies = rules.get(GameRuleRegistry.REROLL_SPECIES_ON_DEATH);
            boolean rerollBloodline = rules.get(GameRuleRegistry.REROLL_BLOODLINE_ON_DEATH);
            if (!rerollSpecies && !rerollBloodline) return;

            UUID uuid = player.getUuid();
            Species oldSpecies = MadnessCoreAPI.getSpecies(server, uuid);
            Bloodline oldBloodline = MadnessCoreAPI.getBloodline(server, uuid);

            if (rerollSpecies) {
                Identifier newSpeciesId = SpeciesRegistry.rollAny();
                MadnessCoreAPI.setSpecies(server, uuid, newSpeciesId);
            }

            if (rerollBloodline) {
                // rollea según la especie actual (ya cambiada arriba si correspondía)
                MadnessCoreAPI.rollBloodline(server, uuid);
            } else if (rerollSpecies) {
                // la especie cambió pero el bloodline no se rerollea: si el bloodline
                // viejo ya no aplica a la especie nueva, hay que limpiarlo igual.
                Bloodline current = MadnessCoreAPI.getBloodline(server, uuid);
                Species newSpecies = MadnessCoreAPI.getSpecies(server, uuid);
                if (current != null && !current.appliesTo(newSpecies.id())) {
                    MadnessCoreAPI.setBloodline(server, uuid, null);
                }
            }

            Species finalSpecies = MadnessCoreAPI.getSpecies(server, uuid);
            Bloodline finalBloodline = MadnessCoreAPI.getBloodline(server, uuid);

            boolean speciesChanged = !finalSpecies.id().equals(oldSpecies.id());
            boolean bloodlineChanged = !Objects.equals(finalBloodline, oldBloodline);
            if (!speciesChanged && !bloodlineChanged) return;

            String message = "§7You were reborn as: " + finalSpecies.displayName()
                    + (finalBloodline != null ? " §7(" + finalBloodline.displayName() + "§7)" : "");
            player.sendMessage(Text.literal(message), false);
        });
    }
}
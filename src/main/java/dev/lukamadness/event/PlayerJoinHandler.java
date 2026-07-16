// dev/lukamadness/madnesscore/event/PlayerJoinHandler.java
package dev.lukamadness.madnesscore.event;

import dev.lukamadness.madnesscore.api.MadnessCoreAPI;
import dev.lukamadness.madnesscore.data.PlayerRaceData;
import dev.lukamadness.madnesscore.util.MadnessLang;
import dev.lukamadness.madnesscore.util.RaceText;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class PlayerJoinHandler {

    private PlayerJoinHandler() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // Devuelve null si el jugador ya tenía datos (no es su primer join) -> no mandamos mensaje.
            PlayerRaceData.PlayerEntry assigned = MadnessCoreAPI.ensureInitialized(server, player.getUuid());
            if (assigned == null) return;

            MadnessCoreAPI.RaceProfile profile = MadnessCoreAPI.getProfile(server, player.getUuid());
            player.sendMessage(Text.translatable(MadnessLang.PLAYER_BORN_AS, RaceText.identity(profile)), false);
        });
    }
}
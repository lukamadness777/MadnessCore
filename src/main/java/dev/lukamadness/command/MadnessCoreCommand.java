// dev/lukamadness/madnesscore/command/MadnessCoreCommand.java
package dev.lukamadness.madnesscore.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.lukamadness.madnesscore.api.Bloodline;
import dev.lukamadness.madnesscore.api.BloodlineRegistry;
import dev.lukamadness.madnesscore.api.Family;
import dev.lukamadness.madnesscore.api.FamilyRegistry;
import dev.lukamadness.madnesscore.api.GameRule;
import dev.lukamadness.madnesscore.api.GameRuleRegistry;
import dev.lukamadness.madnesscore.api.MadnessCoreAPI;
import dev.lukamadness.madnesscore.api.Species;
import dev.lukamadness.madnesscore.api.SpeciesRegistry;
import dev.lukamadness.madnesscore.data.GameRuleData;
import dev.lukamadness.madnesscore.data.PlayerRaceData;
import dev.lukamadness.madnesscore.util.MadnessLang;
import dev.lukamadness.madnesscore.util.RaceText;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MadnessCoreCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(buildRoot()));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildRoot() {
        return CommandManager.literal("madnesscore")
                .then(buildSpecies())
                .then(buildBloodline())
                .then(buildFamily())
                .then(buildGamerule())
                .then(buildCheck());
    }

    // ── Resolución nombre-mostrado -> objeto registrado ─────────────────────
    // Los comandos ahora reciben el displayName (sin colores, case-insensitive),
    // no el Identifier crudo. Internamente seguimos usando Identifier siempre.

    private static Species resolveSpecies(String input) {
        if (input == null) return null;
        String needle = RaceText.stripColor(input).trim();
        for (Species s : SpeciesRegistry.getAll().values()) {
            if (RaceText.stripColor(s.displayName()).equalsIgnoreCase(needle)) return s;
        }
        return null;
    }

    private static Bloodline resolveBloodline(String input, Identifier speciesFilter) {
        if (input == null) return null;
        String needle = RaceText.stripColor(input).trim();
        Collection<Bloodline> pool = speciesFilter == null
                ? BloodlineRegistry.getAll().values()
                : BloodlineRegistry.getForSpecies(speciesFilter);
        for (Bloodline b : pool) {
            if (RaceText.stripColor(b.displayName()).equalsIgnoreCase(needle)) return b;
        }
        return null;
    }

    private static Family resolveFamily(String input, Identifier bloodlineFilter) {
        if (input == null) return null;
        String needle = RaceText.stripColor(input).trim();
        for (Family f : FamilyRegistry.getForBloodline(bloodlineFilter)) {
            if (RaceText.stripColor(f.displayName()).equalsIgnoreCase(needle)) return f;
        }
        return null;
    }

    // ── /madnesscore species list|set ───────────────────────────────────────

    private static LiteralArgumentBuilder<ServerCommandSource> buildSpecies() {
        return CommandManager.literal("species")
                .then(CommandManager.literal("list")
                        .executes(ctx -> executeSpeciesList(ctx.getSource())))
                .then(CommandManager.literal("set")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("species", StringArgumentType.string())
                                        .suggests(MadnessCoreCommand::suggestSpeciesNames)
                                        .executes(ctx -> executeSpeciesChain(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                StringArgumentType.getString(ctx, "species"), null, null))
                                        .then(CommandManager.argument("bloodline", StringArgumentType.string())
                                                .suggests(MadnessCoreCommand::suggestBloodlineNamesForChain)
                                                .executes(ctx -> executeSpeciesChain(ctx.getSource(),
                                                        EntityArgumentType.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "species"),
                                                        StringArgumentType.getString(ctx, "bloodline"), null))
                                                .then(CommandManager.argument("family", StringArgumentType.string())
                                                        .suggests(MadnessCoreCommand::suggestFamilyNamesForChain)
                                                        .executes(ctx -> executeSpeciesChain(ctx.getSource(),
                                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                                StringArgumentType.getString(ctx, "species"),
                                                                StringArgumentType.getString(ctx, "bloodline"),
                                                                StringArgumentType.getString(ctx, "family"))))))));
    }

    private static CompletableFuture<Suggestions> suggestSpeciesNames(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        for (Species s : SpeciesRegistry.getAll().values()) {
            builder.suggest(RaceText.stripColor(s.displayName()));
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestBloodlineNamesForChain(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Species species = resolveSpecies(StringArgumentType.getString(ctx, "species"));
        Collection<Bloodline> pool = species == null
                ? BloodlineRegistry.getAll().values()
                : BloodlineRegistry.getForSpecies(species.id());
        for (Bloodline b : pool) {
            builder.suggest(RaceText.stripColor(b.displayName()));
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestFamilyNamesForChain(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Species species = resolveSpecies(StringArgumentType.getString(ctx, "species"));
        Bloodline bloodline = resolveBloodline(StringArgumentType.getString(ctx, "bloodline"),
                species != null ? species.id() : null);
        for (Family f : FamilyRegistry.getForBloodline(bloodline != null ? bloodline.id() : null)) {
            builder.suggest(RaceText.stripColor(f.displayName()));
        }
        return builder.buildFuture();
    }

    private static int executeSpeciesList(ServerCommandSource source) {
        Map<Identifier, Species> all = SpeciesRegistry.getAll();
        double totalWeight = all.values().stream().mapToDouble(Species::weight).filter(w -> w > 0).sum();

        StringBuilder sb = new StringBuilder("=== Species (" + all.size() + ") ===");
        for (Species species : all.values()) {
            String chance = species.weight() > 0 && totalWeight > 0
                    ? String.format(" - %.1f%%", (species.weight() / totalWeight) * 100)
                    : " - no rolleable";
            sb.append("\n- ").append(species.displayName()).append(" §8(").append(species.id()).append("§8)")
                    .append(" weight=").append(species.weight()).append(chance);
        }
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return all.size();
    }

    /**
     * /madnesscore species set <targets> <species> [<bloodline> [<family>]]
     * Lo que NO especifiques se rerollea automáticamente para esa especie/bloodline.
     */
    private static int executeSpeciesChain(ServerCommandSource source, Collection<ServerPlayerEntity> targets,
                                           String speciesName, String bloodlineName, String familyName) {
        Species species = resolveSpecies(speciesName);
        if (species == null) {
            source.sendError(Text.translatable(MadnessLang.SPECIES_ERROR_NOT_FOUND, speciesName));
            return 0;
        }

        Bloodline explicitBloodline = null;
        if (bloodlineName != null) {
            explicitBloodline = resolveBloodline(bloodlineName, species.id());
            if (explicitBloodline == null) {
                source.sendError(Text.translatable(MadnessLang.BLOODLINE_ERROR_NOT_FOUND, bloodlineName));
                return 0;
            }
        }

        Family explicitFamily = null;
        if (familyName != null) {
            Identifier bloodlineScope = explicitBloodline != null ? explicitBloodline.id() : null;
            explicitFamily = resolveFamily(familyName, bloodlineScope);
            if (explicitFamily == null) {
                source.sendError(Text.translatable(MadnessLang.FAMILY_ERROR_NOT_FOUND, familyName));
                return 0;
            }
        }

        MinecraftServer server = source.getServer();
        for (ServerPlayerEntity player : targets) {
            UUID uuid = player.getUuid();
            MadnessCoreAPI.setSpecies(server, uuid, species.id());

            if (explicitBloodline != null) {
                MadnessCoreAPI.setBloodline(server, uuid, explicitBloodline.id());
            } else {
                MadnessCoreAPI.rollBloodline(server, uuid);
            }

            if (explicitFamily != null) {
                MadnessCoreAPI.setFamily(server, uuid, explicitFamily.id());
            } else {
                MadnessCoreAPI.rollFamily(server, uuid);
            }

            MadnessCoreAPI.RaceProfile profile = MadnessCoreAPI.getProfile(server, uuid);
            player.sendMessage(Text.translatable(MadnessLang.PLAYER_IDENTITY_CHANGED, RaceText.identity(profile)), false);
        }

        int count = targets.size();
        source.sendFeedback(() -> Text.translatable(MadnessLang.SPECIES_SET_SUCCESS, count, species.displayName()), false);
        return count;
    }

    // ── /madnesscore bloodline list|set|count ───────────────────────────────

    private static LiteralArgumentBuilder<ServerCommandSource> buildBloodline() {
        return CommandManager.literal("bloodline")
                .then(CommandManager.literal("list")
                        .executes(ctx -> executeBloodlineList(ctx.getSource(), null))
                        .then(CommandManager.argument("species", StringArgumentType.string())
                                .suggests(MadnessCoreCommand::suggestSpeciesNames)
                                .executes(ctx -> executeBloodlineList(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "species")))))
                .then(CommandManager.literal("set")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("bloodline", StringArgumentType.string())
                                        .suggests(MadnessCoreCommand::suggestBloodlinesForTargets)
                                        .executes(ctx -> executeBloodlineSet(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                StringArgumentType.getString(ctx, "bloodline"))))
                                .then(CommandManager.literal("none")
                                        .executes(ctx -> executeBloodlineClear(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"))))))
                .then(CommandManager.literal("count")
                        .executes(ctx -> executeBloodlineCount(ctx.getSource())));
    }

    /** Solo sugiere bloodlines que apliquen a la especie ACTUAL del primer target. */
    private static CompletableFuture<Suggestions> suggestBloodlinesForTargets(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Bloodline> pool = BloodlineRegistry.getAll().values();
        try {
            Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(ctx, "targets");
            if (!targets.isEmpty()) {
                MinecraftServer server = ctx.getSource().getServer();
                Identifier speciesId = MadnessCoreAPI.getSpecies(server, targets.iterator().next().getUuid()).id();
                pool = BloodlineRegistry.getForSpecies(speciesId);
            }
        } catch (CommandSyntaxException ignored) {
            // "targets" todavía no resuelve a nada válido -> mostramos todos como fallback
        }
        for (Bloodline b : pool) {
            builder.suggest(RaceText.stripColor(b.displayName()));
        }
        return builder.buildFuture();
    }

    private static int executeBloodlineList(ServerCommandSource source, String speciesName) {
        Identifier speciesFilter = null;
        if (speciesName != null) {
            Species species = resolveSpecies(speciesName);
            if (species == null) {
                source.sendError(Text.translatable(MadnessLang.SPECIES_ERROR_NOT_FOUND, speciesName));
                return 0;
            }
            speciesFilter = species.id();
        }

        Collection<Bloodline> bloodlines = speciesFilter == null
                ? BloodlineRegistry.getAll().values()
                : BloodlineRegistry.getForSpecies(speciesFilter);

        StringBuilder sb = new StringBuilder("=== Bloodlines (" + bloodlines.size() + ") ===");
        for (Bloodline b : bloodlines) {
            String scope = b.isAll() ? "All" : b.speciesId().toString();
            sb.append("\n- ").append(b.displayName()).append(" §8(").append(b.id()).append("§8)")
                    .append(" [").append(scope).append("]");
        }
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return bloodlines.size();
    }

    private static int executeBloodlineSet(ServerCommandSource source, Collection<ServerPlayerEntity> targets, String bloodlineName) {
        Bloodline bloodline = resolveBloodline(bloodlineName, null);
        if (bloodline == null) {
            source.sendError(Text.translatable(MadnessLang.BLOODLINE_ERROR_NOT_FOUND, bloodlineName));
            return 0;
        }
        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerPlayerEntity player : targets) {
            try {
                MadnessCoreAPI.setBloodline(server, player.getUuid(), bloodline.id());
                player.sendMessage(Text.translatable(MadnessLang.PLAYER_BLOODLINE_CHANGED,
                        Text.literal(bloodline.displayName())), false);
                count++;
            } catch (IllegalArgumentException e) {
                source.sendError(Text.literal(player.getName().getString() + ": " + e.getMessage()));
            }
        }
        int finalCount = count;
        source.sendFeedback(() -> Text.translatable(MadnessLang.BLOODLINE_SET_SUCCESS, finalCount, bloodline.displayName()), false);
        return count;
    }

    private static int executeBloodlineClear(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        MinecraftServer server = source.getServer();
        for (ServerPlayerEntity player : targets) {
            MadnessCoreAPI.setBloodline(server, player.getUuid(), null);
            player.sendMessage(Text.translatable(MadnessLang.PLAYER_BLOODLINE_CLEARED), false);
        }
        int count = targets.size();
        source.sendFeedback(() -> Text.translatable(MadnessLang.BLOODLINE_CLEARED_SUCCESS, count), false);
        return count;
    }

    private static int executeBloodlineCount(ServerCommandSource source) {
        PlayerRaceData data = PlayerRaceData.get(source.getServer());
        Map<Identifier, Integer> counts = new LinkedHashMap<>();
        for (PlayerRaceData.PlayerEntry entry : data.getAll().values()) {
            Identifier key = entry.bloodlineId() == null ? Identifier.of("madnesscore", "none") : entry.bloodlineId();
            counts.merge(key, 1, Integer::sum);
        }
        StringBuilder sb = new StringBuilder("=== Bloodline Statistics ===");
        counts.forEach((id, n) -> {
            Bloodline b = BloodlineRegistry.get(id);
            sb.append("\n").append(b != null ? b.displayName() : "None").append(": ").append(n);
        });
        sb.append("\nTotal Players: ").append(data.getAll().size());
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return data.getAll().size();
    }

    // ── /madnesscore family list|set|count ──────────────────────────────────

    private static LiteralArgumentBuilder<ServerCommandSource> buildFamily() {
        return CommandManager.literal("family")
                .then(CommandManager.literal("list")
                        .executes(ctx -> executeFamilyList(ctx.getSource(), null))
                        .then(CommandManager.argument("bloodline", StringArgumentType.string())
                                .suggests(MadnessCoreCommand::suggestAllBloodlineNames)
                                .executes(ctx -> executeFamilyList(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "bloodline")))))
                .then(CommandManager.literal("set")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("family", StringArgumentType.string())
                                        .suggests(MadnessCoreCommand::suggestFamiliesForTargets)
                                        .executes(ctx -> executeFamilySet(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                StringArgumentType.getString(ctx, "family"))))
                                .then(CommandManager.literal("none")
                                        .executes(ctx -> executeFamilyClear(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"))))))
                .then(CommandManager.literal("count")
                        .executes(ctx -> executeFamilyCount(ctx.getSource())));
    }

    private static CompletableFuture<Suggestions> suggestAllBloodlineNames(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        for (Bloodline b : BloodlineRegistry.getAll().values()) {
            builder.suggest(RaceText.stripColor(b.displayName()));
        }
        return builder.buildFuture();
    }

    /** Solo sugiere familias que apliquen al bloodline ACTUAL del primer target. */
    private static CompletableFuture<Suggestions> suggestFamiliesForTargets(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Identifier bloodlineFilter = null;
        boolean filtered = false;
        try {
            Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(ctx, "targets");
            if (!targets.isEmpty()) {
                MinecraftServer server = ctx.getSource().getServer();
                Bloodline current = MadnessCoreAPI.getBloodline(server, targets.iterator().next().getUuid());
                bloodlineFilter = current == null ? null : current.id();
                filtered = true;
            }
        } catch (CommandSyntaxException ignored) {
            // "targets" todavía no resuelve a nada válido -> mostramos todos como fallback
        }
        Collection<Family> pool = filtered
                ? FamilyRegistry.getForBloodline(bloodlineFilter)
                : FamilyRegistry.getAll().values();
        for (Family f : pool) {
            builder.suggest(RaceText.stripColor(f.displayName()));
        }
        return builder.buildFuture();
    }

    private static int executeFamilyList(ServerCommandSource source, String bloodlineName) {
        Identifier bloodlineFilter = null;
        if (bloodlineName != null) {
            Bloodline bloodline = resolveBloodline(bloodlineName, null);
            if (bloodline == null) {
                source.sendError(Text.translatable(MadnessLang.BLOODLINE_ERROR_NOT_FOUND, bloodlineName));
                return 0;
            }
            bloodlineFilter = bloodline.id();
        }

        Collection<Family> families = bloodlineFilter == null
                ? FamilyRegistry.getAll().values()
                : FamilyRegistry.getForBloodline(bloodlineFilter);

        StringBuilder sb = new StringBuilder("=== Families (" + families.size() + ") ===");
        for (Family f : families) {
            String scope = f.isAll() ? "All" : f.bloodlineId().toString();
            sb.append("\n- ").append(f.displayName()).append(" §8(").append(f.id()).append("§8)")
                    .append(" [").append(scope).append("]");
        }
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return families.size();
    }

    private static int executeFamilySet(ServerCommandSource source, Collection<ServerPlayerEntity> targets, String familyName) {
        Family family = resolveFamily(familyName, null);
        if (family == null) {
            source.sendError(Text.translatable(MadnessLang.FAMILY_ERROR_NOT_FOUND, familyName));
            return 0;
        }
        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerPlayerEntity player : targets) {
            try {
                MadnessCoreAPI.setFamily(server, player.getUuid(), family.id());
                player.sendMessage(Text.translatable(MadnessLang.PLAYER_FAMILY_CHANGED,
                        Text.literal(family.displayName())), false);
                count++;
            } catch (IllegalArgumentException e) {
                source.sendError(Text.literal(player.getName().getString() + ": " + e.getMessage()));
            }
        }
        int finalCount = count;
        source.sendFeedback(() -> Text.translatable(MadnessLang.FAMILY_SET_SUCCESS, finalCount, family.displayName()), false);
        return count;
    }

    private static int executeFamilyClear(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        MinecraftServer server = source.getServer();
        for (ServerPlayerEntity player : targets) {
            MadnessCoreAPI.setFamily(server, player.getUuid(), null);
            player.sendMessage(Text.translatable(MadnessLang.PLAYER_FAMILY_CLEARED), false);
        }
        int count = targets.size();
        source.sendFeedback(() -> Text.translatable(MadnessLang.FAMILY_CLEARED_SUCCESS, count), false);
        return count;
    }

    private static int executeFamilyCount(ServerCommandSource source) {
        PlayerRaceData data = PlayerRaceData.get(source.getServer());
        Map<Identifier, Integer> counts = new LinkedHashMap<>();
        for (PlayerRaceData.PlayerEntry entry : data.getAll().values()) {
            Identifier key = entry.familyId() == null ? Identifier.of("madnesscore", "none") : entry.familyId();
            counts.merge(key, 1, Integer::sum);
        }
        StringBuilder sb = new StringBuilder("=== Family Statistics ===");
        counts.forEach((id, n) -> {
            Family f = FamilyRegistry.get(id);
            sb.append("\n").append(f != null ? f.displayName() : "None").append(": ").append(n);
        });
        sb.append("\nTotal Players: ").append(data.getAll().size());
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return data.getAll().size();
    }

    // ── /madnesscore gamerule [rule] [true|false] ───────────────────────────

    private static LiteralArgumentBuilder<ServerCommandSource> buildGamerule() {
        return CommandManager.literal("gamerule")
                .executes(ctx -> executeGameruleList(ctx.getSource()))
                .then(CommandManager.argument("rule", StringArgumentType.word())
                        .suggests((ctx, builder) ->
                                net.minecraft.command.CommandSource.suggestMatching(GameRuleRegistry.getAll().keySet(), builder))
                        .executes(ctx -> executeGameruleQuery(ctx.getSource(),
                                StringArgumentType.getString(ctx, "rule")))
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                                .requires(src -> src.hasPermissionLevel(2))
                                .executes(ctx -> executeGameruleSet(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "rule"),
                                        BoolArgumentType.getBool(ctx, "value")))));
    }

    private static int executeGameruleList(ServerCommandSource source) {
        GameRuleData data = GameRuleData.get(source.getServer());
        StringBuilder sb = new StringBuilder("=== MadnessCore Game Rules ===");
        for (GameRule rule : GameRuleRegistry.getAll().values()) {
            sb.append("\n- ").append(rule.key()).append(" = ").append(data.get(rule));
        }
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return GameRuleRegistry.getAll().size();
    }

    private static int executeGameruleQuery(ServerCommandSource source, String key) {
        if (!GameRuleRegistry.exists(key)) {
            source.sendError(Text.translatable(MadnessLang.GAMERULE_UNKNOWN, key));
            return 0;
        }
        boolean value = GameRuleData.get(source.getServer()).get(key);
        source.sendFeedback(() -> Text.translatable(MadnessLang.GAMERULE_VALUE, key, value), false);
        return value ? 1 : 0;
    }

    private static int executeGameruleSet(ServerCommandSource source, String key, boolean value) {
        if (!GameRuleRegistry.exists(key)) {
            source.sendError(Text.translatable(MadnessLang.GAMERULE_UNKNOWN, key));
            return 0;
        }
        GameRuleData.get(source.getServer()).set(key, value);
        source.sendFeedback(() -> Text.translatable(MadnessLang.GAMERULE_SET, key, value), false);
        return value ? 1 : 0;
    }

    // ── /madnesscore check <jugador> ────────────────────────────────────────

    private static LiteralArgumentBuilder<ServerCommandSource> buildCheck() {
        return CommandManager.literal("check")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> executeCheck(ctx.getSource(),
                                EntityArgumentType.getPlayer(ctx, "target"))));
    }

    private static int executeCheck(ServerCommandSource source, ServerPlayerEntity target) {
        MinecraftServer server = source.getServer();
        MadnessCoreAPI.RaceProfile profile = MadnessCoreAPI.getProfile(server, target.getUuid());
        Text identity = RaceText.identity(profile);
        source.sendFeedback(() -> Text.translatable(MadnessLang.CHECK_RESULT, target.getName(), identity), false);
        return 1;
    }
}
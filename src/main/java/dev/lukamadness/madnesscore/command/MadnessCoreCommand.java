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
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
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

    private static Species resolveSpecies(String input) {
        if (input == null) return null;
        String needle = RaceText.stripColor(input).trim();
        for (Species s : SpeciesRegistry.getAll().values()) {
            if (RaceText.stripColor(s.displayName().getString()).equalsIgnoreCase(needle)) return s;
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
            if (RaceText.stripColor(b.displayName().getString()).equalsIgnoreCase(needle)) return b;
        }
        return null;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildSpecies() {
        return CommandManager.literal("species")
                .then(CommandManager.literal("list")
                        .executes(ctx -> executeSpeciesList(ctx.getSource())))
                .then(CommandManager.literal("set")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("species", IdentifierArgumentType.identifier())
                                        .suggests((ctx, builder) ->
                                                CommandSource.suggestIdentifiers(SpeciesRegistry.getAll().keySet(), builder))
                                        .executes(ctx -> executeSpeciesChain(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                IdentifierArgumentType.getIdentifier(ctx, "species"), null, null))
                                        .then(CommandManager.argument("bloodline", IdentifierArgumentType.identifier())
                                                .suggests(MadnessCoreCommand::suggestBloodlinesForSpeciesArg)
                                                .executes(ctx -> executeSpeciesChain(ctx.getSource(),
                                                        EntityArgumentType.getPlayers(ctx, "targets"),
                                                        IdentifierArgumentType.getIdentifier(ctx, "species"),
                                                        IdentifierArgumentType.getIdentifier(ctx, "bloodline"), null))
                                                .then(CommandManager.argument("family", IdentifierArgumentType.identifier())
                                                        .suggests(MadnessCoreCommand::suggestFamiliesForBloodlineArgInSpeciesChain)
                                                        .executes(ctx -> executeSpeciesChain(ctx.getSource(),
                                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                                IdentifierArgumentType.getIdentifier(ctx, "species"),
                                                                IdentifierArgumentType.getIdentifier(ctx, "bloodline"),
                                                                IdentifierArgumentType.getIdentifier(ctx, "family"))))))));
    }

    private static CompletableFuture<Suggestions> suggestBloodlinesForSpeciesArg(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Identifier> allowed;
        try {
            Identifier speciesId = IdentifierArgumentType.getIdentifier(ctx, "species");
            allowed = BloodlineRegistry.getForSpecies(speciesId).stream().map(Bloodline::id).toList();
        } catch (IllegalArgumentException e) {
            allowed = BloodlineRegistry.getAll().keySet();
        }
        return CommandSource.suggestIdentifiers(allowed, builder);
    }

    private static CompletableFuture<Suggestions> suggestFamiliesForBloodlineArgInSpeciesChain(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Identifier> allowed;
        try {
            Identifier bloodlineId = IdentifierArgumentType.getIdentifier(ctx, "bloodline");
            allowed = FamilyRegistry.getForBloodline(bloodlineId).stream().map(Family::id).toList();
        } catch (IllegalArgumentException e) {
            allowed = FamilyRegistry.getAll().keySet();
        }
        return CommandSource.suggestIdentifiers(allowed, builder);
    }

    private static int executeSpeciesList(ServerCommandSource source) {
        Map<Identifier, Species> all = SpeciesRegistry.getAll();
        double totalWeight = all.values().stream().mapToDouble(Species::weight).filter(w -> w > 0).sum();

        StringBuilder sb = new StringBuilder("=== Species (" + all.size() + ") ===");
        for (Species species : all.values()) {
            String chance = species.weight() > 0 && totalWeight > 0
                    ? String.format(" - %.1f%%", (species.weight() / totalWeight) * 100)
                    : " - no rolleable";
            sb.append("\n- ").append(species.displayName().getString()).append(chance);
        }
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return all.size();
    }

    private static int executeSpeciesChain(ServerCommandSource source, Collection<ServerPlayerEntity> targets,
                                           Identifier speciesId, Identifier bloodlineId, Identifier familyId) {
        if (!SpeciesRegistry.exists(speciesId)) {
            source.sendError(Text.translatable(MadnessLang.SPECIES_ERROR_NOT_FOUND, speciesId.toString()));
            return 0;
        }
        Species species = SpeciesRegistry.get(speciesId);

        if (bloodlineId != null && !BloodlineRegistry.exists(bloodlineId)) {
            source.sendError(Text.translatable(MadnessLang.BLOODLINE_ERROR_NOT_FOUND, bloodlineId.toString()));
            return 0;
        }
        if (familyId != null && !FamilyRegistry.exists(familyId)) {
            source.sendError(Text.translatable(MadnessLang.FAMILY_ERROR_NOT_FOUND, familyId.toString()));
            return 0;
        }

        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerPlayerEntity player : targets) {
            UUID uuid = player.getUuid();
            try {
                MadnessCoreAPI.setSpecies(server, uuid, speciesId);

                if (bloodlineId != null) {
                    MadnessCoreAPI.setBloodline(server, uuid, bloodlineId);
                } else {
                    MadnessCoreAPI.rollBloodline(server, uuid);
                }

                if (familyId != null) {
                    MadnessCoreAPI.setFamily(server, uuid, familyId);
                } else {
                    MadnessCoreAPI.rollFamily(server, uuid);
                }

                MadnessCoreAPI.RaceProfile profile = MadnessCoreAPI.getProfile(server, uuid);
                player.sendMessage(Text.translatable(MadnessLang.PLAYER_IDENTITY_CHANGED, RaceText.identity(profile)), false);
                count++;
            } catch (IllegalArgumentException e) {
                source.sendError(Text.literal(player.getName().getString() + ": " + e.getMessage()));
            }
        }

        int finalCount = count;
        source.sendFeedback(() -> Text.translatable(MadnessLang.SPECIES_SET_SUCCESS, finalCount, species.displayName()), false);
        return count;
    }

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
                                .then(CommandManager.argument("bloodline", IdentifierArgumentType.identifier())
                                        .suggests(MadnessCoreCommand::suggestBloodlinesForTargets)
                                        .executes(ctx -> executeBloodlineChain(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                IdentifierArgumentType.getIdentifier(ctx, "bloodline"), null))
                                        .then(CommandManager.argument("family", IdentifierArgumentType.identifier())
                                                .suggests(MadnessCoreCommand::suggestFamiliesForBloodlineArgInBloodlineChain)
                                                .executes(ctx -> executeBloodlineChain(ctx.getSource(),
                                                        EntityArgumentType.getPlayers(ctx, "targets"),
                                                        IdentifierArgumentType.getIdentifier(ctx, "bloodline"),
                                                        IdentifierArgumentType.getIdentifier(ctx, "family")))))
                                .then(CommandManager.literal("none")
                                        .executes(ctx -> executeBloodlineClear(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"))))))
                .then(CommandManager.literal("count")
                        .executes(ctx -> executeBloodlineCount(ctx.getSource())));
    }

    private static CompletableFuture<Suggestions> suggestSpeciesNames(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        for (Species s : SpeciesRegistry.getAll().values()) {
            builder.suggest(RaceText.stripColor(s.displayName().getString()));
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestBloodlinesForTargets(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Identifier> allowed = BloodlineRegistry.getAll().keySet();
        try {
            Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(ctx, "targets");
            if (!targets.isEmpty()) {
                MinecraftServer server = ctx.getSource().getServer();
                Identifier speciesId = MadnessCoreAPI.getSpecies(server, targets.iterator().next().getUuid()).id();
                allowed = BloodlineRegistry.getForSpecies(speciesId).stream().map(Bloodline::id).toList();
            }
        } catch (CommandSyntaxException ignored) {
        }
        return CommandSource.suggestIdentifiers(allowed, builder);
    }

    private static CompletableFuture<Suggestions> suggestFamiliesForBloodlineArgInBloodlineChain(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Identifier> allowed;
        try {
            Identifier bloodlineId = IdentifierArgumentType.getIdentifier(ctx, "bloodline");
            allowed = FamilyRegistry.getForBloodline(bloodlineId).stream().map(Family::id).toList();
        } catch (IllegalArgumentException e) {
            allowed = FamilyRegistry.getAll().keySet();
        }
        return CommandSource.suggestIdentifiers(allowed, builder);
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
            sb.append("\n- ").append(b.displayName().getString());
        }
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return bloodlines.size();
    }

    private static int executeBloodlineChain(ServerCommandSource source, Collection<ServerPlayerEntity> targets,
                                             Identifier bloodlineId, Identifier familyId) {
        if (!BloodlineRegistry.exists(bloodlineId)) {
            source.sendError(Text.translatable(MadnessLang.BLOODLINE_ERROR_NOT_FOUND, bloodlineId.toString()));
            return 0;
        }
        Bloodline bloodline = BloodlineRegistry.get(bloodlineId);

        if (familyId != null && !FamilyRegistry.exists(familyId)) {
            source.sendError(Text.translatable(MadnessLang.FAMILY_ERROR_NOT_FOUND, familyId.toString()));
            return 0;
        }

        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerPlayerEntity player : targets) {
            UUID uuid = player.getUuid();
            try {
                MadnessCoreAPI.setBloodline(server, uuid, bloodlineId);

                if (familyId != null) {
                    MadnessCoreAPI.setFamily(server, uuid, familyId);
                } else {
                    MadnessCoreAPI.rollFamily(server, uuid);
                }

                MadnessCoreAPI.RaceProfile profile = MadnessCoreAPI.getProfile(server, uuid);
                player.sendMessage(Text.translatable(MadnessLang.PLAYER_IDENTITY_CHANGED, RaceText.identity(profile)), false);
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
            sb.append(b != null ? b.displayName().getString() : "None").append(": ").append(n);
        });
        sb.append("\nTotal Players: ").append(data.getAll().size());
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return data.getAll().size();
    }

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
                                .then(CommandManager.argument("family", IdentifierArgumentType.identifier())
                                        .suggests(MadnessCoreCommand::suggestFamiliesForTargets)
                                        .executes(ctx -> executeFamilySet(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"),
                                                IdentifierArgumentType.getIdentifier(ctx, "family"))))
                                .then(CommandManager.literal("none")
                                        .executes(ctx -> executeFamilyClear(ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets"))))))
                .then(CommandManager.literal("count")
                        .executes(ctx -> executeFamilyCount(ctx.getSource())));
    }

    private static CompletableFuture<Suggestions> suggestAllBloodlineNames(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        for (Bloodline b : BloodlineRegistry.getAll().values()) {
            builder.suggest(RaceText.stripColor(b.displayName().getString()));
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestFamiliesForTargets(
            CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Identifier> allowed = FamilyRegistry.getAll().keySet();
        try {
            Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(ctx, "targets");
            if (!targets.isEmpty()) {
                MinecraftServer server = ctx.getSource().getServer();
                Bloodline current = MadnessCoreAPI.getBloodline(server, targets.iterator().next().getUuid());
                Identifier bloodlineId = current == null ? null : current.id();
                allowed = FamilyRegistry.getForBloodline(bloodlineId).stream().map(Family::id).toList();
            }
        } catch (CommandSyntaxException ignored) {
        }
        return CommandSource.suggestIdentifiers(allowed, builder);
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
            sb.append("\n- ").append(f.displayName().getString());
        }
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return families.size();
    }

    private static int executeFamilySet(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier familyId) {
        if (!FamilyRegistry.exists(familyId)) {
            source.sendError(Text.translatable(MadnessLang.FAMILY_ERROR_NOT_FOUND, familyId.toString()));
            return 0;
        }
        Family family = FamilyRegistry.get(familyId);
        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerPlayerEntity player : targets) {
            try {
                MadnessCoreAPI.setFamily(server, player.getUuid(), familyId);
                player.sendMessage(Text.translatable(MadnessLang.PLAYER_FAMILY_CHANGED, family.displayName()), false);
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
            sb.append(f != null ? f.displayName().getString() : "None").append(": ").append(n);
        });
        sb.append("\nTotal Players: ").append(data.getAll().size());
        String output = sb.toString();
        source.sendFeedback(() -> Text.literal(output), false);
        return data.getAll().size();
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildGamerule() {
        return CommandManager.literal("gamerule")
                .executes(ctx -> executeGameruleList(ctx.getSource()))
                .then(CommandManager.argument("rule", StringArgumentType.word())
                        .suggests((ctx, builder) ->
                                CommandSource.suggestMatching(GameRuleRegistry.getAll().keySet(), builder))
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
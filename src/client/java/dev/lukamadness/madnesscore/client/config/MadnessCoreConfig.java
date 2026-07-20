package dev.lukamadness.madnesscore.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.lukamadness.madnesscore.client.config.draw.util.SkinRegion;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class MadnessCoreConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("madnesscoreconfig.json");

    private static MadnessCoreConfig INSTANCE = new MadnessCoreConfig();

    public enum HairType {
        BALD("madnesscore.hairtype.bald"),
        SHORT("madnesscore.hairtype.short"),
        MEDIUM("madnesscore.hairtype.medium"),
        LONG("madnesscore.hairtype.long");

        private final String translationKey;
        HairType(String translationKey) { this.translationKey = translationKey; }

        public net.minecraft.text.Text label() { return net.minecraft.text.Text.translatable(translationKey); }

        public HairType next() {
            HairType[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    public int skinColor = 0xFFFFFF;

    public int eyeOffsetX = 5;
    public int eyeOffsetY = 3;
    public int eyeWidth   = 2;
    public int eyeHeight  = 1;
    public int eyeColor   = 0x000000;
    public Map<String, boolean[][]> eyePixels = new LinkedHashMap<>();

    public HairType hairType = HairType.SHORT;
    public int hairColor = 0x3B2412;
    public Map<String, boolean[][]> hairPixels = new LinkedHashMap<>();

    public boolean[][] getPixels(SkinRegion region) {
        return hairPixels.computeIfAbsent(region.key(),
                k -> new boolean[region.height][region.width]);
    }

    public void setPixels(SkinRegion region, boolean[][] pixels) {
        hairPixels.put(region.key(), pixels);
    }

    public boolean[][] getEyePixels(SkinRegion region) {
        return eyePixels.computeIfAbsent(region.key(),
                k -> new boolean[region.height][region.width]);
    }

    public void setEyePixels(SkinRegion region, boolean[][] pixels) {
        eyePixels.put(region.key(), pixels);
    }

    public static MadnessCoreConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            MadnessCoreConfig loaded = GSON.fromJson(reader, MadnessCoreConfig.class);
            INSTANCE = (loaded != null) ? loaded : new MadnessCoreConfig();
            INSTANCE.ensureDefaults();
        } catch (IOException | com.google.gson.JsonParseException e) {
            backupBrokenConfig();
            INSTANCE = new MadnessCoreConfig();
            save();
        }
    }

    private static void backupBrokenConfig() {
        try {
            Path backup = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".broken-" + System.currentTimeMillis());
            Files.move(CONFIG_PATH, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        dev.lukamadness.madnesscore.client.network.AppearanceClientNetworking.sendToServer();
    }

    private void ensureDefaults() {
        if (hairType == null) {
            hairType = HairType.SHORT;
        }
        if (hairPixels == null) {
            hairPixels = new LinkedHashMap<>();
        }
        if (eyePixels == null) {
            eyePixels = new LinkedHashMap<>();
        }

        for (SkinRegion region : SkinRegion.all()) {
            boolean[][] hairP = hairPixels.get(region.key());
            if (!isValid(hairP, region)) {
                hairPixels.put(region.key(), new boolean[region.height][region.width]);
            }

            boolean[][] eyeP = eyePixels.get(region.key());
            if (!isValid(eyeP, region)) {
                eyePixels.put(region.key(), new boolean[region.height][region.width]);
            }
        }
    }

    private static boolean isValid(boolean[][] pixels, SkinRegion region) {
        if (pixels == null || pixels.length != region.height) return false;
        for (boolean[] row : pixels) {
            if (row == null || row.length != region.width) return false;
        }
        return true;
    }

    public static class EyePreviewWidget extends ClickableWidget {

        public static final int PIXEL_SIZE = 12;
        public static final int HEAD_UV    = 8;
        public static final int HEAD_PX    = HEAD_UV * PIXEL_SIZE;
        public static final int LABEL_H    = 12;
        public static final int MAX_EYE_SIZE = 3;

        private volatile Identifier fetchedSkinTexture = null;

        public EyePreviewWidget(int x, int y, MadnessCoreConfig cfg) {
            super(x, y, HEAD_PX, LABEL_H + HEAD_PX, Text.empty());
            fetchSessionSkinAsync();
        }

        private void fetchSessionSkinAsync() {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) return;

            try {
                client.getSkinProvider().fetchSkinTextures(client.getGameProfile())
                        .thenAcceptAsync(skinTextures -> fetchedSkinTexture = skinTextures.texture(), client)
                        .exceptionally(ex -> null);
            } catch (Exception e) {
            }
        }

        @Override
        protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            int headY = getY() + LABEL_H;

            ctx.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.translatable("madnesscore.config.preview.your_skin"), getX(), getY(), 0xFFAAAAAA, false);

            Identifier skinTexture = getPlayerSkinTexture();

            ctx.drawTexture(skinTexture, getX(), headY, HEAD_PX, HEAD_PX, 8f, 8f, 8, 8, 64, 64);
            ctx.drawTexture(skinTexture, getX(), headY, HEAD_PX, HEAD_PX, 40f, 8f, 8, 8, 64, 64);

            ctx.drawBorder(getX(), headY, HEAD_PX, HEAD_PX, 0xFF888888);
        }

        private Identifier getPlayerSkinTexture() {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player instanceof AbstractClientPlayerEntity clientPlayer) {
                return clientPlayer.getSkinTextures().texture();
            }
            Identifier fetched = fetchedSkinTexture;
            return (fetched != null) ? fetched : DefaultSkinHelper.getTexture();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {}

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }
}
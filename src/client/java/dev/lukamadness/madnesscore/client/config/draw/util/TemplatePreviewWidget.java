package dev.lukamadness.madnesscore.client.config.draw.util;

import dev.lukamadness.madnesscore.client.config.MadnessCoreConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class TemplatePreviewWidget extends ClickableWidget {

    public static final int MAX_PREVIEW_SCALE = 3;
    private static final int MIN_PREVIEW_SCALE = 1;

    private static final int BASE_PIXEL_SIZE = MadnessCoreConfig.EyePreviewWidget.PIXEL_SIZE;
    public static final int HEAD_UV = MadnessCoreConfig.EyePreviewWidget.HEAD_UV;
    public static final int LABEL_H = MadnessCoreConfig.EyePreviewWidget.LABEL_H;

    private final MadnessCoreConfig cfg;
    private final int pixelSize;
    private final int headPx;

    public TemplatePreviewWidget(int rightEdgeX, int topY, MadnessCoreConfig cfg, int maxAvailableWidth) {
        super(rightEdgeX - HEAD_UV * computePixelSize(maxAvailableWidth), topY,
                HEAD_UV * computePixelSize(maxAvailableWidth),
                LABEL_H + HEAD_UV * computePixelSize(maxAvailableWidth),
                Text.empty());
        this.cfg = cfg;
        this.pixelSize = computePixelSize(maxAvailableWidth);
        this.headPx = HEAD_UV * this.pixelSize;
    }

    private static int computePixelSize(int maxAvailableWidth) {
        int maxScaleThatFits = maxAvailableWidth / (HEAD_UV * BASE_PIXEL_SIZE);
        int effectiveScale = Math.max(MIN_PREVIEW_SCALE, Math.min(MAX_PREVIEW_SCALE, maxScaleThatFits));
        return BASE_PIXEL_SIZE * effectiveScale;
    }

    public int getHeadPx() {
        return headPx;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int headY = getY() + LABEL_H;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text label = Text.translatable("madnesscore.config.preview.template");
        int textWidth = textRenderer.getWidth(label);
        int headRightX = getX() + headPx;

        ctx.drawText(textRenderer, label, headRightX - textWidth, getY(), 0xFFAAAAAA, false);

        drawHead(ctx, getX(), headY);
    }

    private void drawHead(DrawContext ctx, int ox, int oy) {
        int baseColor = 0xFF000000 | (cfg.skinColor & 0xFFFFFF);
        ctx.fill(ox, oy, ox + headPx, oy + headPx, baseColor);
        ctx.drawBorder(ox, oy, headPx, headPx, 0xFF888888);

        int clampedOffsetX = clamp(cfg.eyeOffsetX, 0, HEAD_UV - cfg.eyeWidth);
        int eyeRow = clamp(HEAD_UV - cfg.eyeOffsetY - cfg.eyeHeight, 0, HEAD_UV - cfg.eyeHeight);

        int eyeColRight = HEAD_UV - clampedOffsetX - cfg.eyeWidth;
        int eyeColLeft  = clampedOffsetX;

        drawEyeRect(ctx, ox, oy, eyeColRight, eyeRow, cfg.eyeWidth, cfg.eyeHeight);
        drawEyeRect(ctx, ox, oy, eyeColLeft, eyeRow, cfg.eyeWidth, cfg.eyeHeight);

        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT),
                cfg.getEyePixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT)),
                cfg.eyeColor);
        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT),
                cfg.getEyePixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT)),
                cfg.eyeColor);

        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT),
                cfg.getPixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT)),
                cfg.hairColor);
        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT),
                cfg.getPixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT)),
                cfg.hairColor);
    }

    private void drawPixelPart(DrawContext ctx, int ox, int oy, SkinRegion region, boolean[][] pixels, int color) {
        int fillColor = 0xFF000000 | (color & 0xFFFFFF);
        for (int row = 0; row < region.height; row++) {
            for (int col = 0; col < region.width; col++) {
                if (!pixels[row][col]) continue;
                int px = ox + col * pixelSize;
                int py = oy + row * pixelSize;
                ctx.fill(px, py, px + pixelSize, py + pixelSize, fillColor);
            }
        }
    }

    private void drawEyeRect(DrawContext ctx, int ox, int oy, int col, int row, int width, int height) {
        int eyeScreenX = ox + col * pixelSize;
        int eyeScreenY = oy + row * pixelSize;
        int eyeW = width * pixelSize;
        int eyeH = height * pixelSize;

        ctx.fill(eyeScreenX, eyeScreenY, eyeScreenX + eyeW, eyeScreenY + eyeH, 0xFFFFFFFF);
        ctx.drawBorder(eyeScreenX, eyeScreenY, eyeW, eyeH, 0xFFFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {}

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
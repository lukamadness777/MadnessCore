package dev.lukamadness.madnesscore.client.config.draw.skin;

import dev.lukamadness.madnesscore.client.config.MadnessCoreConfig;
import dev.lukamadness.madnesscore.client.config.draw.util.SkinRegion;
import dev.lukamadness.madnesscore.client.config.draw.util.SkinTextureCache;
import dev.lukamadness.madnesscore.client.config.draw.hair.HairDrawScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SkinPartSelectScreen extends Screen {

    private static final int TEXTURE_SIZE = 64;
    private static final int TOP_MARGIN = 40;
    private static final int BOTTOM_MARGIN = 50;
    private static final int SIDE_MARGIN = 20;
    private static final int MIN_SCALE = 2;
    private static final int MAX_SCALE = 8;

    private final Screen parent;
    private final MadnessCoreConfig cfg;
    private final SkinRegion currentRegion;

    private int scale;
    private int canvasSize;
    private int canvasX, canvasY;
    private SkinRegion hovered;

    public SkinPartSelectScreen(Screen parent, MadnessCoreConfig cfg, SkinRegion currentRegion) {
        super(Text.translatable("madnesscore.config.partselect.title"));
        this.parent = parent;
        this.cfg = cfg;
        this.currentRegion = currentRegion;
    }

    @Override
    protected void init() {
        this.clearChildren();

        int availableWidth = this.width - SIDE_MARGIN * 2;
        int availableHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        int maxScaleThatFits = Math.min(availableWidth, availableHeight) / TEXTURE_SIZE;
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, maxScaleThatFits));

        canvasSize = TEXTURE_SIZE * scale;
        canvasX = (this.width - canvasSize) / 2;
        canvasY = TOP_MARGIN;

        int cancelY = Math.min(canvasY + canvasSize + 14, this.height - 26);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("madnesscore.config.partselect.cancel"), b ->
                        this.client.setScreen(new HairDrawScreen(parent, cfg, currentRegion)))
                .dimensions(this.width / 2 - 50, cancelY, 100, 20)
                .build());


        this.addDrawable(this::renderCanvasAndRegions);
    }

    private void renderCanvasAndRegions(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("madnesscore.config.partselect.instructions"),
                this.width / 2, 24, 0xA0A0A0);

        Identifier skin = SkinTextureCache.get();
        ctx.drawTexture(skin, canvasX, canvasY, canvasSize, canvasSize,
                0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        ctx.drawBorder(canvasX - 1, canvasY - 1, canvasSize + 2, canvasSize + 2, 0xFFAAAAAA);

        hovered = regionAt(mouseX, mouseY);

        for (SkinRegion region : SkinRegion.all()) {
            int rx = canvasX + region.u * scale;
            int ry = canvasY + region.v * scale;
            int rw = region.width * scale;
            int rh = region.height * scale;

            boolean isCurrent = region == currentRegion;
            boolean isHovered = region == hovered;

            int borderColor = isCurrent ? 0xFF55FF55 : (isHovered ? 0xFFFFFF55 : 0x80FFFFFF);
            if (isHovered) {
                ctx.fill(rx, ry, rx + rw, ry + rh, 0x40FFFFFF);
            }
            ctx.drawBorder(rx, ry, rw, rh, borderColor);
        }

        if (hovered != null) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    hovered.label(),
                    this.width / 2, canvasY + canvasSize + 4, 0xFFFFFF);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world != null) {
            this.renderInGameBackground(ctx);
        } else {
            this.renderPanoramaBackground(ctx, delta);
        }
        this.renderDarkening(ctx);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private SkinRegion regionAt(double mouseX, double mouseY) {
        if (mouseX < canvasX || mouseY < canvasY) return null;
        int px = (int) ((mouseX - canvasX) / scale);
        int py = (int) ((mouseY - canvasY) / scale);
        if (px < 0 || px >= TEXTURE_SIZE || py < 0 || py >= TEXTURE_SIZE) return null;

        for (SkinRegion region : SkinRegion.all()) {
            if (px >= region.u && px < region.u + region.width
                    && py >= region.v && py < region.v + region.height) {
                return region;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        SkinRegion clicked = regionAt(mouseX, mouseY);
        if (clicked != null) {
            this.client.setScreen(new HairDrawScreen(parent, cfg, clicked));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(new HairDrawScreen(parent, cfg, currentRegion));
        }
    }
}
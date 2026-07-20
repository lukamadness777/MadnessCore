package dev.lukamadness.madnesscore.client.config.menu;

import dev.lukamadness.madnesscore.client.config.*;
import dev.lukamadness.madnesscore.client.config.draw.eye.EyeDrawScreen;
import dev.lukamadness.madnesscore.client.config.draw.hair.HairDrawScreen;
import dev.lukamadness.madnesscore.client.config.draw.util.SkinRegion;
import dev.lukamadness.madnesscore.client.config.draw.util.TemplatePreviewWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class CharacterOffsetsScreen extends Screen {

    private static final int MARGIN_X = 20;
    private static final int HEADER_Y_START = 32;
    private static final int HEADER_WIDTH = 200;
    private static final int HEADER_HEIGHT = 20;
    private static final int SECTION_GAP = 6;

    private static final int SLIDER_WIDTH = 200;
    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_GAP = 4;
    private static final int PREVIEW_GAP = 10;

    private static final int CONTENT_BOTTOM_MARGIN = 40;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLL_STEP = 16;
    private boolean draggingScrollbar = false;
    private int scrollbarTrackX, scrollbarThumbY, scrollbarThumbHeight, scrollbarTrackHeight;

    private final Screen parent;
    private final MadnessCoreConfig cfg;

    private boolean hairExpanded = false;
    private boolean eyesExpanded = false;
    private boolean skinExpanded = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int contentTop, contentBottom, totalContentHeight;

    private boolean colorPickerOpen = false;
    private boolean eyedropperActive = false;
    private IntConsumer colorConfirmCallback;

    private static final int SB_SIZE = 130;
    private static final int HUE_W = 18;
    private static final int POPUP_GAP = 8;
    private static final int PREVIEW_H = 22;
    private static final int PICK_BTN_H = 18;
    private float pickerHue, pickerSaturation, pickerBrightness;
    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private int popupX, popupY;

    public CharacterOffsetsScreen(Screen parent, MadnessCoreConfig cfg) {
        super(Text.translatable("madnesscore.config.tab.character_offsets"));
        this.parent = parent;
        this.cfg = cfg;
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private int computeContentHeight() {
        int y = HEADER_Y_START;

        y += MadnessCoreConfig.EyePreviewWidget.LABEL_H + MadnessCoreConfig.EyePreviewWidget.HEAD_PX + PREVIEW_GAP;

        y += HEADER_HEIGHT + SECTION_GAP;
        if (hairExpanded) {
            y += (HEADER_HEIGHT + SECTION_GAP) * 3;
        }

        y += HEADER_HEIGHT + SECTION_GAP;
        if (eyesExpanded) {
            y += (SLIDER_HEIGHT + SLIDER_GAP) * 4;
            y += HEADER_HEIGHT + SECTION_GAP;
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        y += HEADER_HEIGHT + SECTION_GAP;
        if (skinExpanded) y += HEADER_HEIGHT + SECTION_GAP;

        return y - HEADER_Y_START;
    }

    private void computeScrollbarGeometry() {
        scrollbarTrackX = MARGIN_X + HEADER_WIDTH + 10;
        scrollbarTrackHeight = contentBottom - contentTop;
        if (scrollbarTrackHeight <= 0 || totalContentHeight <= 0) {
            scrollbarThumbHeight = 0;
            scrollbarThumbY = contentTop;
            return;
        }
        scrollbarThumbHeight = clampInt(
                (int) ((float) scrollbarTrackHeight / totalContentHeight * scrollbarTrackHeight),
                16, scrollbarTrackHeight);
        scrollbarThumbY = contentTop + (maxScroll == 0 ? 0
                : (int) ((float) scrollOffset / maxScroll * (scrollbarTrackHeight - scrollbarThumbHeight)));
    }

    private void rebuildWidgets() {
        this.clearChildren();

        contentTop = HEADER_Y_START;
        contentBottom = this.height - CONTENT_BOTTOM_MARGIN;
        int viewportHeight = Math.max(0, contentBottom - contentTop);

        totalContentHeight = computeContentHeight();
        maxScroll = Math.max(0, totalContentHeight - viewportHeight);
        scrollOffset = clampInt(scrollOffset, 0, maxScroll);
        computeScrollbarGeometry();

        int y = HEADER_Y_START;

        int yourSkinPreviewX = MARGIN_X + (SLIDER_WIDTH - MadnessCoreConfig.EyePreviewWidget.HEAD_PX) / 2;
        MadnessCoreConfig.EyePreviewWidget yourSkinPreview = new MadnessCoreConfig.EyePreviewWidget(yourSkinPreviewX, y - scrollOffset, cfg);
        this.addDrawableChild(yourSkinPreview);
        y += yourSkinPreview.getHeight() + PREVIEW_GAP;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("madnesscore.config.section.hair", hairExpanded ? "▲" : "▼"), b -> {
                            hairExpanded = !hairExpanded;
                            rebuildWidgets();
                        })
                .dimensions(MARGIN_X, y - scrollOffset, HEADER_WIDTH, HEADER_HEIGHT)
                .build());
        y += HEADER_HEIGHT + SECTION_GAP;

        if (hairExpanded) {
            this.addDrawableChild(ButtonWidget.builder(
                            Text.translatable("madnesscore.config.hair.type", cfg.hairType.label()), b -> {
                                cfg.hairType = cfg.hairType.next();
                                MadnessCoreConfig.save();
                                rebuildWidgets();
                            })
                    .dimensions(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT)
                    .build());
            y += HEADER_HEIGHT + SECTION_GAP;

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("madnesscore.config.hair.color"), b ->
                            openColorPicker(cfg.hairColor, color -> { cfg.hairColor = color; MadnessCoreConfig.save(); }))
                    .dimensions(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT)
                    .build());
            y += HEADER_HEIGHT + SECTION_GAP;

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("madnesscore.config.hair.draw"), b -> openHairDrawScreen())
                    .dimensions(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT)
                    .build());
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("madnesscore.config.section.eyes", eyesExpanded ? "▲" : "▼"), b -> {
                            eyesExpanded = !eyesExpanded;
                            rebuildWidgets();
                        })
                .dimensions(MARGIN_X, y - scrollOffset, HEADER_WIDTH, HEADER_HEIGHT)
                .build());
        y += HEADER_HEIGHT + SECTION_GAP;

        if (eyesExpanded) {
            this.addDrawableChild(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.offset_x", 0, MadnessCoreConfig.EyePreviewWidget.HEAD_UV - 1,
                    () -> cfg.eyeOffsetX, v -> cfg.eyeOffsetX = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addDrawableChild(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.offset_y", 0, MadnessCoreConfig.EyePreviewWidget.HEAD_UV - 1,
                    () -> cfg.eyeOffsetY, v -> cfg.eyeOffsetY = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addDrawableChild(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.width", 1, MadnessCoreConfig.EyePreviewWidget.MAX_EYE_SIZE,
                    () -> cfg.eyeWidth, v -> cfg.eyeWidth = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addDrawableChild(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.height", 1, MadnessCoreConfig.EyePreviewWidget.MAX_EYE_SIZE,
                    () -> cfg.eyeHeight, v -> cfg.eyeHeight = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("madnesscore.config.eye.color"), b ->
                            openColorPicker(cfg.eyeColor, color -> { cfg.eyeColor = color; MadnessCoreConfig.save(); }))
                    .dimensions(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT)
                    .build());
            y += HEADER_HEIGHT + SECTION_GAP;

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("madnesscore.config.eye.draw"), b -> openEyeDrawScreen())
                    .dimensions(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT)
                    .build());
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("madnesscore.config.section.skin", skinExpanded ? "▲" : "▼"), b -> {
                            skinExpanded = !skinExpanded;
                            rebuildWidgets();
                        })
                .dimensions(MARGIN_X, y - scrollOffset, HEADER_WIDTH, HEADER_HEIGHT)
                .build());
        y += HEADER_HEIGHT + SECTION_GAP;

        if (skinExpanded) {
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("madnesscore.config.skin.color"), b ->
                            openColorPicker(cfg.skinColor, color -> { cfg.skinColor = color; MadnessCoreConfig.save(); }))
                    .dimensions(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT)
                    .build());
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        int previewGap = 16;
        int leftContentRightEdge = MARGIN_X + HEADER_WIDTH + 10 + SCROLLBAR_WIDTH;
        int maxPreviewWidth = this.width - leftContentRightEdge - previewGap - MARGIN_X;

        this.addDrawableChild(new TemplatePreviewWidget(
                this.width - MARGIN_X, HEADER_Y_START, cfg, maxPreviewWidth));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> this.close())
                .dimensions(this.width - 8 - 100, this.height - 8 - 20, 100, 20)
                .build());
    }

    private void openHairDrawScreen() {
        if (this.client == null) return;
        this.client.setScreen(new HairDrawScreen(this, cfg, SkinRegion.defaultRegion()));
    }

    private void openEyeDrawScreen() {
        if (this.client == null) return;
        this.client.setScreen(new EyeDrawScreen(this, cfg, SkinRegion.defaultRegion()));
    }

    private static int clampInt(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (colorPickerOpen) return true;
        scrollOffset -= (int) (verticalAmount * SCROLL_STEP);
        rebuildWidgets();
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);

        if (maxScroll > 0 && !(colorPickerOpen && !eyedropperActive)) {
            drawScrollbar(ctx);
        }

        if (colorPickerOpen && eyedropperActive) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("madnesscore.config.colorpicker.eyedropper_hint"),
                    this.width / 2, 8, 0xFFFFFF00);
        } else if (colorPickerOpen) {
            renderColorPickerPopup(ctx);
        }
    }

    private void drawScrollbar(DrawContext ctx) {
        if (scrollbarTrackHeight <= 0 || totalContentHeight <= 0) return;
        ctx.fill(scrollbarTrackX, contentTop, scrollbarTrackX + SCROLLBAR_WIDTH, contentBottom, 0x40FFFFFF);
        ctx.fill(scrollbarTrackX, scrollbarThumbY, scrollbarTrackX + SCROLLBAR_WIDTH,
                scrollbarThumbY + scrollbarThumbHeight, 0xFFAAAAAA);
    }

    private boolean mouseInScrollbarThumb(double mx, double my) {
        if (scrollbarThumbHeight <= 0) return false;
        return mx >= scrollbarTrackX && mx < scrollbarTrackX + SCROLLBAR_WIDTH
                && my >= scrollbarThumbY && my < scrollbarThumbY + scrollbarThumbHeight;
    }

    private void updateScrollFromDrag(double my) {
        if (scrollbarTrackHeight <= scrollbarThumbHeight) return;
        double usableTrack = scrollbarTrackHeight - scrollbarThumbHeight;
        double relative = (my - contentTop - scrollbarThumbHeight / 2.0) / usableTrack;
        scrollOffset = clampInt((int) Math.round(relative * maxScroll), 0, maxScroll);
        rebuildWidgets();
    }

    private void openColorPicker(int initialColor, IntConsumer onConfirm) {
        int color = initialColor & 0xFFFFFF;
        float[] hsb = rgbToHsb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
        pickerHue = hsb[0];
        pickerSaturation = hsb[1];
        pickerBrightness = hsb[2];
        colorConfirmCallback = onConfirm;

        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int totalH = SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP + PICK_BTN_H + POPUP_GAP + 20;
        popupX = (this.width - totalW) / 2;
        popupY = (this.height - totalH) / 2;

        eyedropperActive = false;
        colorPickerOpen = true;
    }

    private void renderColorPickerPopup(DrawContext ctx) {
        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int totalH = SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP + PICK_BTN_H + POPUP_GAP + 20;

        ctx.fill(0, 0, this.width, this.height, 0x80000000);
        ctx.fill(popupX - 6, popupY - 6, popupX + totalW + 6, popupY + totalH + 6, 0xF0202020);
        ctx.drawBorder(popupX - 6, popupY - 6, totalW + 12, totalH + 12, 0xFFFFFFFF);

        drawSBSquare(ctx, popupX, popupY);
        int cursorX = popupX + (int) (pickerSaturation * (SB_SIZE - 1));
        int cursorY = popupY + (int) ((1f - pickerBrightness) * (SB_SIZE - 1));
        ctx.fill(cursorX - 2, cursorY - 2, cursorX + 3, cursorY + 3, 0xFFFFFFFF);
        ctx.fill(cursorX - 1, cursorY - 1, cursorX + 2, cursorY + 2, 0xFF000000);

        int hueX = popupX + SB_SIZE + POPUP_GAP;
        drawHueBar(ctx, hueX, popupY);
        int hueCursorY = popupY + (int) (pickerHue * (SB_SIZE - 1));
        ctx.fill(hueX - 2, hueCursorY - 1, hueX + HUE_W + 2, hueCursorY + 2, 0xFFFFFFFF);
        ctx.fill(hueX - 1, hueCursorY, hueX + HUE_W + 1, hueCursorY + 1, 0xFF000000);

        int previewY = popupY + SB_SIZE + POPUP_GAP;
        int currentColor = hsbToRgb(pickerHue, pickerSaturation, pickerBrightness);
        ctx.fill(popupX, previewY, popupX + totalW, previewY + PREVIEW_H, 0xFF000000 | currentColor);
        ctx.drawBorder(popupX, previewY, totalW, PREVIEW_H, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("#" + String.format("%06X", currentColor)),
                popupX + totalW / 2, previewY + (PREVIEW_H - 8) / 2, 0xFFFFFFFF);

        int pickBtnY = previewY + PREVIEW_H + POPUP_GAP;
        drawPopupButton(ctx, popupX, pickBtnY, totalW, PICK_BTN_H,
                Text.translatable("madnesscore.config.colorpicker.pick").getString(), mouseInPickButton());

        int btnY = pickBtnY + PICK_BTN_H + POPUP_GAP;
        drawPopupButton(ctx, popupX, btnY, 60, 18,
                Text.translatable("madnesscore.config.colorpicker.done").getString(), mouseInPopupDoneButton());
        drawPopupButton(ctx, popupX + totalW - 60, btnY, 60, 18,
                Text.translatable("madnesscore.config.colorpicker.cancel").getString(), mouseInPopupCancelButton());
    }

    private void drawSBSquare(DrawContext ctx, int x, int y) {
        for (int px = 0; px < SB_SIZE; px++) {
            float s = px / (float) (SB_SIZE - 1);
            for (int py = 0; py < SB_SIZE; py++) {
                float b = 1f - py / (float) (SB_SIZE - 1);
                int color = hsbToRgb(pickerHue, s, b);
                ctx.fill(x + px, y + py, x + px + 1, y + py + 1, 0xFF000000 | color);
            }
        }
    }

    private void drawHueBar(DrawContext ctx, int x, int y) {
        for (int py = 0; py < SB_SIZE; py++) {
            float h = py / (float) (SB_SIZE - 1);
            ctx.fill(x, y + py, x + HUE_W, y + py + 1, 0xFF000000 | hsbToRgb(h, 1f, 1f));
        }
    }

    private void drawPopupButton(DrawContext ctx, int x, int y, int w, int h, String label, boolean hovered) {
        ctx.fill(x, y, x + w, y + h, hovered ? 0xFF505050 : 0xFF383838);
        ctx.drawBorder(x, y, w, h, 0xFFAAAAAA);
        ctx.drawCenteredTextWithShadow(this.textRenderer, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFF);
    }

    private boolean mouseInPopupDoneButton() {
        return lastMouseX >= popupX && lastMouseX < popupX + 60
                && lastMouseY >= popupDoneY() && lastMouseY < popupDoneY() + 18;
    }

    private boolean mouseInPopupCancelButton() {
        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int cx = popupX + totalW - 60;
        return lastMouseX >= cx && lastMouseX < cx + 60
                && lastMouseY >= popupDoneY() && lastMouseY < popupDoneY() + 18;
    }

    private boolean mouseInPickButton() {
        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int pickBtnY = popupY + SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP;
        return lastMouseX >= popupX && lastMouseX < popupX + totalW
                && lastMouseY >= pickBtnY && lastMouseY < pickBtnY + PICK_BTN_H;
    }

    private int popupDoneY() {
        return popupY + SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP + PICK_BTN_H + POPUP_GAP;
    }

    private double lastMouseX, lastMouseY;

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        lastMouseX = mx;
        lastMouseY = my;

        if (colorPickerOpen && eyedropperActive) {
            int sampled = sampleColorAt(mx, my);
            float[] hsb = rgbToHsb((sampled >> 16) & 0xFF, (sampled >> 8) & 0xFF, sampled & 0xFF);
            pickerHue = hsb[0];
            pickerSaturation = hsb[1];
            pickerBrightness = hsb[2];
            eyedropperActive = false;
            return true;
        }

        if (colorPickerOpen) {
            if (mx >= popupX && mx < popupX + SB_SIZE && my >= popupY && my < popupY + SB_SIZE) {
                draggingSB = true;
                updateSB(mx, my);
                return true;
            }
            int hueX = popupX + SB_SIZE + POPUP_GAP;
            if (mx >= hueX && mx < hueX + HUE_W && my >= popupY && my < popupY + SB_SIZE) {
                draggingHue = true;
                updateHue(my);
                return true;
            }
            if (mouseInPickButton()) {
                eyedropperActive = true;
                return true;
            }
            if (mouseInPopupDoneButton()) {
                int finalColor = hsbToRgb(pickerHue, pickerSaturation, pickerBrightness);
                colorPickerOpen = false;
                if (colorConfirmCallback != null) colorConfirmCallback.accept(finalColor);
                return true;
            }
            if (mouseInPopupCancelButton()) {
                colorPickerOpen = false;
                return true;
            }
            return true;
        }

        if (maxScroll > 0 && mouseInScrollbarThumb(mx, my)) {
            draggingScrollbar = true;
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        lastMouseX = mx;
        lastMouseY = my;

        if (colorPickerOpen && !eyedropperActive) {
            if (draggingSB) { updateSB(mx, my); return true; }
            if (draggingHue) { updateHue(my); return true; }
            return true;
        }
        if (colorPickerOpen) return true;

        if (draggingScrollbar) {
            updateScrollFromDrag(my);
            return true;
        }

        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (colorPickerOpen) {
            draggingSB = false;
            draggingHue = false;
            return true;
        }
        draggingScrollbar = false;
        return super.mouseReleased(mx, my, button);
    }

    private void updateSB(double mx, double my) {
        pickerSaturation = clamp01((float) (mx - popupX) / (SB_SIZE - 1));
        pickerBrightness = clamp01(1f - (float) (my - popupY) / (SB_SIZE - 1));
    }

    private void updateHue(double my) {
        pickerHue = clamp01((float) (my - popupY) / (SB_SIZE - 1));
    }

    private int sampleColorAt(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        try (NativeImage image = ScreenshotRecorder.takeScreenshot(client.getFramebuffer())) {
            double scale = client.getWindow().getScaleFactor();
            int px = (int) Math.round(mouseX * scale);
            int py = (int) Math.round(mouseY * scale);
            px = Math.max(0, Math.min(image.getWidth() - 1, px));
            py = Math.max(0, Math.min(image.getHeight() - 1, py));

            int abgr = image.getColor(px, py);
            int r = abgr & 0xFF;
            int g = (abgr >> 8) & 0xFF;
            int b = (abgr >> 16) & 0xFF;
            return (r << 16) | (g << 8) | b;
        } catch (Exception e) {
            return 0xFFFFFF;
        }
    }

    @Override
    public void close() {
        MadnessCoreConfig.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int hsbToRgb(float h, float s, float b) {
        if (s == 0f) {
            int v = (int) (b * 255);
            return (v << 16) | (v << 8) | v;
        }
        float sector = h * 6f;
        int i = (int) sector;
        float f = sector - i;
        float p = b * (1f - s);
        float q = b * (1f - s * f);
        float t = b * (1f - s * (1f - f));
        float r, g, bl;
        switch (i % 6) {
            case 0 -> { r = b; g = t; bl = p; }
            case 1 -> { r = q; g = b; bl = p; }
            case 2 -> { r = p; g = b; bl = t; }
            case 3 -> { r = p; g = q; bl = b; }
            case 4 -> { r = t; g = p; bl = b; }
            default -> { r = b; g = p; bl = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (bl * 255);
    }

    private static float[] rgbToHsb(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        float brightness = max;
        float saturation = max == 0 ? 0 : delta / max;
        float hue = 0;
        if (delta != 0) {
            if (max == rf) hue = (gf - bf) / delta % 6;
            else if (max == gf) hue = (bf - rf) / delta + 2;
            else hue = (rf - gf) / delta + 4;
            hue /= 6f;
            if (hue < 0) hue += 1f;
        }
        return new float[]{hue, saturation, brightness};
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static class IntSliderWidget extends SliderWidget {
        private final String translationKey;
        private final int min, max;
        private final IntConsumer onChange;

        IntSliderWidget(int x, int y, int width, int height, String translationKey, int min, int max,
                        IntSupplier getter, IntConsumer onChange) {
            super(x, y, width, height, Text.empty(), normalize(getter.getAsInt(), min, max));
            this.translationKey = translationKey;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            updateMessage();
        }

        private static double normalize(int value, int min, int max) {
            return max == min ? 0 : (value - min) / (double) (max - min);
        }

        @Override
        protected void updateMessage() {
            int current = min + (int) Math.round(this.value * (max - min));
            setMessage(Text.translatable(translationKey, current));
        }

        @Override
        protected void applyValue() {
            int current = min + (int) Math.round(this.value * (max - min));
            onChange.accept(current);
        }
    }
}
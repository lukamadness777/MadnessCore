package dev.lukamadness.madnesscore.client.config;

import dev.lukamadness.madnesscore.client.config.menu.CharacterOffsetsScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MadnessCoreConfigScreen extends Screen {

    private static final int TAB_BUTTON_HEIGHT = 20;
    private static final int TAB_BUTTON_PADDING = 20;
    private static final int TAB_BUTTON_GAP = 4;
    private static final int TAB_BAR_Y = 24;
    private static final int CONTENT_TOP_MARGIN = 12;

    private final Screen parent;
    private final List<TabDefinition> tabs = new ArrayList<>();
    private int selectedTab = 0;

    protected MadnessCoreConfigScreen(Screen parent) {
        super(Text.translatable("madnesscore.config.title"));
        this.parent = parent;

        this.tabs.add(new TabDefinition(Text.translatable("madnesscore.config.tab.general"),
                ctx -> renderPlaceholder(ctx, Text.translatable("madnesscore.config.general.placeholder")),
                null));

        this.tabs.add(new TabDefinition(Text.translatable("madnesscore.config.tab.character_offsets"),
                ctx -> renderPlaceholder(ctx, Text.translatable("madnesscore.config.offsets.placeholder")),
                this::openCharacterOffsetsScreen));
    }

    public static Screen create(Screen parent) {
        return new MadnessCoreConfigScreen(parent);
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        this.clearChildren();

        int[] widths = new int[tabs.size()];
        int totalWidth = 0;
        for (int i = 0; i < tabs.size(); i++) {
            int textWidth = this.textRenderer.getWidth(tabs.get(i).title());
            widths[i] = Math.max(60, textWidth + TAB_BUTTON_PADDING);
            totalWidth += widths[i];
        }
        totalWidth += TAB_BUTTON_GAP * Math.max(0, tabs.size() - 1);

        int startX = (this.width - totalWidth) / 2;
        int x = startX;

        for (int i = 0; i < tabs.size(); i++) {
            int index = i;
            int buttonWidth = widths[i];

            ButtonWidget button = ButtonWidget.builder(tabs.get(i).title(), b -> onTabClicked(index))
                    .dimensions(x, TAB_BAR_Y, buttonWidth, TAB_BUTTON_HEIGHT)
                    .build();
            button.active = index != selectedTab;
            this.addDrawableChild(button);

            x += buttonWidth + TAB_BUTTON_GAP;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> this.close())
                .dimensions(this.width - 8 - 100, this.height - 8 - 20, 100, 20)
                .build());
    }

    private void onTabClicked(int index) {
        TabDefinition tab = tabs.get(index);
        if (tab.onOpenScreen() != null) {
            tab.onOpenScreen().run();
            return;
        }
        selectTab(index);
    }

    private void openCharacterOffsetsScreen() {
        if (this.client == null) return;
        MadnessCoreConfig cfg = MadnessCoreConfig.get();
        this.client.setScreen(new CharacterOffsetsScreen(this, cfg));
    }

    private void selectTab(int index) {
        if (index == this.selectedTab) return;
        this.selectedTab = index;
        rebuildWidgets();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);

        int contentTop = TAB_BAR_Y + TAB_BUTTON_HEIGHT + CONTENT_TOP_MARGIN;
        tabs.get(selectedTab).contentRenderer().render(new TabRenderContext(context, contentTop));
    }

    private void renderPlaceholder(TabRenderContext ctx, Text message) {
        ctx.context().drawCenteredTextWithShadow(
                this.textRenderer, message, this.width / 2, ctx.contentTop() + 10, 0xA0A0A0);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private record TabDefinition(Text title, TabContentRenderer contentRenderer, Runnable onOpenScreen) {}

    private record TabRenderContext(DrawContext context, int contentTop) {}

    @FunctionalInterface
    private interface TabContentRenderer {
        void render(TabRenderContext ctx);
    }
}
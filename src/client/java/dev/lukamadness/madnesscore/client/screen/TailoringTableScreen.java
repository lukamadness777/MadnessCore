package dev.lukamadness.madnesscore.client.screen;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.network.TailoringRecipeListPayload;
import dev.lukamadness.madnesscore.registry.screenhandler.screenhandlerclass.TailoringTableScreenHandler;
import dev.lukamadness.madnesscore.tailoring.FabricRequirement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TailoringTableScreen extends HandledScreen<TailoringTableScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of(MadnessCore.MOD_ID, "textures/gui/container/tailoring_table.png");

    private static final Identifier PATTERN =
            Identifier.of(MadnessCore.MOD_ID, "container/tailor/pattern");
    private static final Identifier PATTERN_HIGHLIGHTED =
            Identifier.of(MadnessCore.MOD_ID, "container/tailor/pattern_highlighted");
    private static final Identifier PATTERN_SELECTED =
            Identifier.of(MadnessCore.MOD_ID, "container/tailor/pattern_selected");
    private static final Identifier SCROLLER =
            Identifier.of(MadnessCore.MOD_ID, "container/tailor/scroller");
    private static final Identifier SCROLLER_DISABLED =
            Identifier.of(MadnessCore.MOD_ID, "container/tailor/scroller_disabled");

    private static final int INPUT_SLOT_COUNT = 4;

    private static final int LIST_X = 60;
    private static final int LIST_Y = 17;
    private static final int LIST_WIDTH = 56;
    private static final int LIST_HEIGHT = 52;
    private static final int LIST_COLUMNS = 4;
    private static final int ICON_SIZE = 14;
    private static final int SCROLL_STEP = ICON_SIZE / 2;

    private static final int SCROLLBAR_X = LIST_X + LIST_WIDTH + 4;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;

    private List<TailoringRecipeListPayload.RecipeEntry> allRecipes = List.of();
    private List<Integer> visibleIndices = List.of();

    private int selectedIndex = -1;
    private float scrollOffset = 0f;
    private boolean draggingScrollbar = false;
    private int dragOffsetY = 0;

    public TailoringTableScreen(TailoringTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    public void setRecipes(List<TailoringRecipeListPayload.RecipeEntry> recipes) {
        this.allRecipes = recipes;
    }

    private List<Integer> computeVisibleIndices() {
        Map<Item, Integer> provided = new HashMap<>();
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            ItemStack stack = this.handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                provided.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        List<Integer> visible = new ArrayList<>();
        for (int i = 0; i < allRecipes.size(); i++) {
            if (isCraftable(allRecipes.get(i), provided)) {
                visible.add(i);
            }
        }
        return visible;
    }

    private boolean isCraftable(TailoringRecipeListPayload.RecipeEntry recipe, Map<Item, Integer> provided) {
        for (FabricRequirement req : recipe.requirements()) {
            if (provided.getOrDefault(req.item(), 0) < req.count()) {
                return false;
            }
        }
        return true;
    }

    private int getTotalRows() {
        return (int) Math.ceil(visibleIndices.size() / (double) LIST_COLUMNS);
    }

    private float getMaxScrollPixels() {
        return Math.max(0, getTotalRows() * ICON_SIZE - LIST_HEIGHT);
    }

    private int getThumbY() {
        float maxScroll = getMaxScrollPixels();
        if (maxScroll <= 0) {
            return LIST_Y;
        }
        float progress = scrollOffset / maxScroll;
        return LIST_Y + Math.round(progress * (LIST_HEIGHT - SCROLLBAR_HEIGHT));
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        this.visibleIndices = computeVisibleIndices();
        this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0f, getMaxScrollPixels());

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        int pixelOffset = Math.round(scrollOffset);

        int firstRow = Math.max(0, pixelOffset / ICON_SIZE);
        int lastRow = Math.min(getTotalRows(), (pixelOffset + LIST_HEIGHT) / ICON_SIZE + 1);

        context.enableScissor(x + LIST_X, y + LIST_Y, x + LIST_X + LIST_WIDTH, y + LIST_Y + LIST_HEIGHT);
        for (int row = firstRow; row < lastRow; row++) {
            for (int col = 0; col < LIST_COLUMNS; col++) {
                int slot = row * LIST_COLUMNS + col;
                if (slot >= visibleIndices.size()) {
                    continue;
                }
                int recipeIndex = visibleIndices.get(slot);

                int slotX = LIST_X + col * ICON_SIZE;
                int slotY = LIST_Y + row * ICON_SIZE - pixelOffset;

                Identifier sprite;
                if (recipeIndex == selectedIndex) {
                    sprite = PATTERN_SELECTED;
                } else if (isPointWithinBounds(slotX, slotY, ICON_SIZE, ICON_SIZE, mouseX, mouseY)
                        && isPointWithinBounds(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
                    sprite = PATTERN_HIGHLIGHTED;
                } else {
                    sprite = PATTERN;
                }

                context.drawGuiTexture(sprite, x + slotX, y + slotY, ICON_SIZE, ICON_SIZE);

                float scale = ICON_SIZE / 16f;
                context.getMatrices().push();
                context.getMatrices().translate(x + slotX, y + slotY, 0);
                context.getMatrices().scale(scale, scale, 1f);
                context.drawItem(allRecipes.get(recipeIndex).output(), 0, 0);
                context.getMatrices().pop();
            }
        }
        context.disableScissor();

        boolean scrollable = getMaxScrollPixels() > 0;
        Identifier scrollbarSprite = scrollable ? SCROLLER : SCROLLER_DISABLED;
        context.drawGuiTexture(scrollbarSprite, x + SCROLLBAR_X, y + getThumbY(), SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        if (!isPointWithinBounds(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            return;
        }

        int pixelOffset = Math.round(scrollOffset);
        int row = (mouseY - LIST_Y + pixelOffset) / ICON_SIZE;
        int col = (mouseX - LIST_X) / ICON_SIZE;
        int slot = row * LIST_COLUMNS + col;

        if (col >= 0 && col < LIST_COLUMNS && slot >= 0 && slot < visibleIndices.size()) {
            int recipeIndex = visibleIndices.get(slot);
            context.drawItemTooltip(this.textRenderer, allRecipes.get(recipeIndex).output(), mouseX + x, mouseY + y);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        if (getMaxScrollPixels() > 0
                && isPointWithinBounds(SCROLLBAR_X, LIST_Y, SCROLLBAR_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            this.draggingScrollbar = true;

            int currentThumbY = getThumbY();
            double relativeMouseY = mouseY - y;

            if (relativeMouseY >= currentThumbY && relativeMouseY < currentThumbY + SCROLLBAR_HEIGHT) {
                this.dragOffsetY = (int) (relativeMouseY - currentThumbY);
            } else {
                this.dragOffsetY = SCROLLBAR_HEIGHT / 2;
                updateScrollFromMouse(relativeMouseY - dragOffsetY);
            }
            return true;
        }

        if (isPointWithinBounds(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            double relX = mouseX - x;
            double relY = mouseY - y;
            int pixelOffset = Math.round(scrollOffset);
            int row = (int) ((relY - LIST_Y + pixelOffset) / ICON_SIZE);
            int col = (int) ((relX - LIST_X) / ICON_SIZE);
            int slot = row * LIST_COLUMNS + col;

            if (col >= 0 && col < LIST_COLUMNS && slot >= 0 && slot < visibleIndices.size()) {
                int recipeIndex = visibleIndices.get(slot);
                this.selectedIndex = recipeIndex;
                ClientPlayNetworkHandler networkHandler = this.client.getNetworkHandler();
                if (networkHandler != null) {
                    networkHandler.sendPacket(new ButtonClickC2SPacket(this.handler.syncId, recipeIndex));
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            int y = (height - backgroundHeight) / 2;
            updateScrollFromMouse(mouseY - y - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float maxScroll = getMaxScrollPixels();
        if (maxScroll > 0) {
            scrollOffset = MathHelper.clamp(scrollOffset - (float) (verticalAmount * SCROLL_STEP), 0f, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void updateScrollFromMouse(double relativeY) {
        float maxScroll = getMaxScrollPixels();
        if (maxScroll <= 0) {
            return;
        }
        float progress = (float) ((relativeY - LIST_Y) / (double) (LIST_HEIGHT - SCROLLBAR_HEIGHT));
        scrollOffset = MathHelper.clamp(progress, 0f, 1f) * maxScroll;
    }
}
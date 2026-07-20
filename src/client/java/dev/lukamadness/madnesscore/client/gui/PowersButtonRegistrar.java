package dev.lukamadness.madnesscore.client.gui;

import dev.lukamadness.madnesscore.client.mixin.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class PowersButtonRegistrar {

    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_MARGIN = 4;

    private PowersButtonRegistrar() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen) {
                addPowersButton(screen);
            }
        });
    }

    private static void addPowersButton(Screen screen) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        int guiLeft = accessor.madnesscore$getGuiLeft();
        int guiTop = accessor.madnesscore$getGuiTop();
        int backgroundWidth = accessor.madnesscore$getBackgroundWidth();

        int buttonX = guiLeft + backgroundWidth + BUTTON_MARGIN;
        int buttonY = guiTop + BUTTON_MARGIN;

        ButtonWidget powersButton = ButtonWidget.builder(Text.empty(), button -> {
                })
                .dimensions(buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE)
                .tooltip(Tooltip.of(Text.translatable("gui.beyondthesea.powers")))
                .build();

        Screens.getButtons(screen).add(powersButton);
    }
}
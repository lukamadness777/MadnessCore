package dev.lukamadness.madnesscore.registry.screenhandler;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.screenhandler.screenhandlerclass.TailoringTableScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    public static final ScreenHandlerType<TailoringTableScreenHandler> TAILORING_TABLE =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(MadnessCore.MOD_ID, "tailoring_table"),
                    new ScreenHandlerType<>(TailoringTableScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
            );

    public static void register() {
    }
}
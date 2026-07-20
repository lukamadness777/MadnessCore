package dev.lukamadness.madnesscore.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lukamadness.madnesscore.client.config.MadnessCoreConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuCompat implements ModMenuApi, ClientModInitializer {

    @Override
    public void onInitializeClient() {}

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MadnessCoreConfigScreen::create;
    }
}
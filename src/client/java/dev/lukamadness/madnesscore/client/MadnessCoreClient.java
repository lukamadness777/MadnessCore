package dev.lukamadness.madnesscore.client;

import dev.lukamadness.madnesscore.client.compat.AccessoriesCompat;
import dev.lukamadness.madnesscore.client.config.MadnessCoreConfig;
import dev.lukamadness.madnesscore.client.network.AppearanceClientNetworking;
import dev.lukamadness.madnesscore.client.network.ModNetworkingClient;
import dev.lukamadness.madnesscore.client.registry.ModItemColors;
import dev.lukamadness.madnesscore.client.render.renderer.TailoringTableRenderer;
import dev.lukamadness.madnesscore.client.screen.TailoringTableScreen;
import dev.lukamadness.madnesscore.registry.blocks.ModBlockEntities;
import dev.lukamadness.madnesscore.registry.screenhandler.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class MadnessCoreClient implements ClientModInitializer {
	private void registerBlockEntityRenderers() {
		BlockEntityRendererFactories.register(ModBlockEntities.TAILORING_TABLE, ctx -> new TailoringTableRenderer());
	}

	private void registerCompat() {
		if (FabricLoader.getInstance().isModLoaded("accessories")) {
			AccessoriesCompat.register();
		}
	}

	@Override
	public void onInitializeClient() {
		ModItemColors.register();
		ModNetworkingClient.register();

		HandledScreens.register(ModScreenHandlers.TAILORING_TABLE, TailoringTableScreen::new);
		registerBlockEntityRenderers();

		registerCompat();
		MadnessCoreConfig.load();

		AppearanceClientNetworking.register();
	}
}
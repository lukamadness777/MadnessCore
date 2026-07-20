package dev.lukamadness.madnesscore;

import dev.lukamadness.madnesscore.command.MadnessCoreCommand;
import dev.lukamadness.madnesscore.event.PlayerJoinHandler;
import dev.lukamadness.madnesscore.event.PlayerRespawnHandler;
import dev.lukamadness.madnesscore.example.ExampleContent;

import dev.lukamadness.madnesscore.network.AppearanceServerNetworking;
import dev.lukamadness.madnesscore.network.ModNetworking;
import dev.lukamadness.madnesscore.registry.ModItemGroups;
import dev.lukamadness.madnesscore.registry.blocks.ModBlocks;
import dev.lukamadness.madnesscore.registry.items.ModItems;
import dev.lukamadness.madnesscore.registry.recipes.ModRecipes;
import dev.lukamadness.madnesscore.util.DataComponents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MadnessCore implements ModInitializer {
	public static final String MOD_ID = "madnesscore";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void registryMod() {
		ModItems.initialize();
		ModBlocks.initialize();
		ModItemGroups.initialize();
		ModRecipes.registerRecipes();
		ModNetworking.register();
		DataComponents.registerComponents();

		AppearanceServerNetworking.register();
	}

		@Override
	public void onInitialize() {
		MadnessCoreCommand.register();
		PlayerJoinHandler.register();
		PlayerRespawnHandler.register();

		registryMod();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}

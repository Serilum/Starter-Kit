package com.natamus.starterkit;

import com.mojang.brigadier.ParseResults;
import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.fabric.callbacks.CollectiveCommandEvents;
import com.natamus.starterkit.cmds.CommandStarterkit;
import com.natamus.starterkit.events.StarterServerEvents;
import com.natamus.starterkit.util.Reference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ModFabric implements ModInitializer {
	
	@Override
	public void onInitialize() {
		setGlobalConstants();
		ModCommon.init();

		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadEvents() {
		ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer minecraftServer) -> {
			StarterServerEvents.onServerStarting(minecraftServer);
		});

		ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerLevel world) -> {
			StarterServerEvents.onSpawn(world, entity);
		});

		CollectiveCommandEvents.ON_COMMAND_PARSE.register((String string, ParseResults<CommandSourceStack> parse) -> {
			StarterServerEvents.onCommand(string, parse);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CommandStarterkit.register(dispatcher);
		});
	}

	private static void setGlobalConstants() {

	}
}

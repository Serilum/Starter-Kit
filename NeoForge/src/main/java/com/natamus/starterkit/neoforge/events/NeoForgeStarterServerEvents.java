package com.natamus.starterkit.neoforge.events;

import com.natamus.starterkit.cmds.CommandStarterkit;
import com.natamus.starterkit.events.StarterServerEvents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber
public class NeoForgeStarterServerEvents {
	@SubscribeEvent
	public static void onServerStarted(ServerStartingEvent e) {
		StarterServerEvents.onServerStarting(e.getServer());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onSpawn(EntityJoinLevelEvent e) {
		StarterServerEvents.onSpawn(e.getLevel(), e.getEntity());
	}
	
	@SubscribeEvent
	public static void onCommand(CommandEvent e) {
		StarterServerEvents.onCommand("", e.getParseResults());
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandStarterkit.register(e.getDispatcher());
	}
}

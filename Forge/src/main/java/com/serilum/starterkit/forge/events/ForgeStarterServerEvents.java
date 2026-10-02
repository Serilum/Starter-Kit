package com.serilum.starterkit.forge.events;

import com.serilum.starterkit.cmds.CommandStarterkit;
import com.serilum.starterkit.events.StarterServerEvents;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandles;

public class ForgeStarterServerEvents {
	public static void registerEventsInBus() {
		BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgeStarterServerEvents.class);
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartingEvent e) {
		StarterServerEvents.onServerStarting(e.getServer());
	}

	@SubscribeEvent(priority = Priority.LOWEST)
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

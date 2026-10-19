package com.serilum.starterkit.neoforge.events;

import com.serilum.starterkit.cmds.CommandStarterkit;
import com.serilum.starterkit.events.StarterServerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

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
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
		if (e.getEntity() instanceof ServerPlayer serverPlayer) {
			StarterServerEvents.onRespawn(serverPlayer, e.isEndConquered());
		}
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

package com.natamus.starterkit.forge.events;

import com.natamus.starterkit.cmds.CommandStarterkit;
import com.natamus.starterkit.events.StarterServerEvents;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeStarterServerEvents {
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

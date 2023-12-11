package com.natamus.starterkit.neoforge.events;

import com.natamus.starterkit.cmds.CommandStarterkit;
import com.natamus.starterkit.events.FirstSpawnEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class NeoForgeFirstSpawnEvent {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandStarterkit.register(e.getDispatcher());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onSpawn(EntityJoinLevelEvent e) {
		FirstSpawnEvent.onSpawn(e.getLevel(), e.getEntity());
	}
	
	@SubscribeEvent
	public static void onCommand(CommandEvent e) {
		FirstSpawnEvent.onCommand("", e.getParseResults());
	}
}

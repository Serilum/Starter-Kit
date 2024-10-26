package com.natamus.starterkit.forge.events;

import com.natamus.starterkit.cmds.CommandStarterkit;
import com.natamus.starterkit.events.StarterServerEvents;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeStarterServerEvents {
	@SubscribeEvent
	public void onServerStarted(ServerStartingEvent e) {
		StarterServerEvents.onServerStarting(e.getServer());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent e) {
		StarterServerEvents.onSpawn(e.getLevel(), e.getEntity());
	}
	
	@SubscribeEvent
	public void onCommand(CommandEvent e) {
		StarterServerEvents.onCommand("", e.getParseResults());
	}

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent e) {
    	CommandStarterkit.register(e.getDispatcher());
    }
}

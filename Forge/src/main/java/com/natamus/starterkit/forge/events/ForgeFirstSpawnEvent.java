package com.natamus.starterkit.forge.events;

import com.natamus.starterkit.cmds.CommandStarterkit;
import com.natamus.starterkit.events.FirstSpawnEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeFirstSpawnEvent {
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent e) {
    	CommandStarterkit.register(e.getDispatcher());
    }

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinWorldEvent e) {
		FirstSpawnEvent.onSpawn(e.getWorld(), e.getEntity());
	}
	
	@SubscribeEvent
	public void onCommand(CommandEvent e) {
		FirstSpawnEvent.onCommand("", e.getParseResults());
	}
}

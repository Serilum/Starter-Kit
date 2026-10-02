package com.serilum.starterkit.neoforge.events;

import com.serilum.starterkit.events.StarterClientEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class NeoForgeStarterClientEvents {
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre e) {
		StarterClientEvents.onClientTick();
	}
}

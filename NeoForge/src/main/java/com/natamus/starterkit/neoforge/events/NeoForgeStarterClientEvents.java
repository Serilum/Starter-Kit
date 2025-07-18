package com.natamus.starterkit.neoforge.events;

import com.natamus.starterkit.events.StarterClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class NeoForgeStarterClientEvents {
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre e) {
		StarterClientEvents.onClientTick();
	}
}

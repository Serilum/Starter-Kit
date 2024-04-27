package com.natamus.starterkit.neoforge.events;

import com.natamus.starterkit.events.StarterClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class NeoForgeStarterClientEvents {
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent e) {
		StarterClientEvents.onClientTick();
	}
}

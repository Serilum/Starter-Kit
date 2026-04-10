package com.natamus.starterkit.forge.events;

import com.natamus.starterkit.events.StarterClientEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public class ForgeStarterClientEvents {
	public static void registerEventsInBus() {
		// BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgeStarterClientEvents.class);

		TickEvent.ClientTickEvent.Pre.BUS.addListener(ForgeStarterClientEvents::onClientTick);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent.Pre e) {
		StarterClientEvents.onClientTick();
	}
}

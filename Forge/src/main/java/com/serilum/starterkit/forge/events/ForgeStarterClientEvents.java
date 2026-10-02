package com.serilum.starterkit.forge.events;

import com.serilum.starterkit.events.StarterClientEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeStarterClientEvents {
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent e) {
		if (!e.phase.equals(TickEvent.Phase.START)) {
			return;
		}

		StarterClientEvents.onClientTick();
	}
}

package com.natamus.starterkit.forge.events;

import com.natamus.starterkit.events.StarterClientEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ForgeStarterClientEvents {
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent e) {
		if (!e.phase.equals(TickEvent.Phase.START)) {
			return;
		}

		StarterClientEvents.onClientTick();
	}
}

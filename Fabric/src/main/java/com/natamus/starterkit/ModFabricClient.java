package com.natamus.starterkit;

import com.natamus.starterkit.events.StarterClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class ModFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModCommon.registerPackets();

		registerEvents();
	}
	
	private void registerEvents() {
		ClientTickEvents.START_CLIENT_TICK.register((Minecraft client) -> {
			StarterClientEvents.onClientTick();
		});
	}
}

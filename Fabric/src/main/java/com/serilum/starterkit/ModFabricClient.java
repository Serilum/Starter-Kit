package com.serilum.starterkit;

import com.serilum.starterkit.events.StarterClientEvents;
import net.fabricmc.api.ClientModInitializer;
import com.serilum.starterkit.util.Reference;
import com.natamus.collective.check.ShouldLoadCheck;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class ModFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		ModCommon.registerPackets();

		registerEvents();
	}
	
	private void registerEvents() {
		ClientTickEvents.START_CLIENT_TICK.register((Minecraft client) -> {
			StarterClientEvents.onClientTick();
		});
	}
}

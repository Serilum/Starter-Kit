package com.natamus.starterkit;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.check.ShouldLoadCheck;
import com.natamus.starterkit.neoforge.config.IntegrateNeoForgeConfig;
import com.natamus.starterkit.neoforge.events.NeoForgeStarterClientEvents;
import com.natamus.starterkit.neoforge.events.NeoForgeStarterServerEvents;
import com.natamus.starterkit.util.Reference;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Reference.MOD_ID)
public class ModNeoForge {
	
	public ModNeoForge(IEventBus modEventBus) {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		modEventBus.addListener(this::loadComplete);

		setGlobalConstants();
		ModCommon.init();

		IntegrateNeoForgeConfig.registerScreen(ModLoadingContext.get());

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadComplete(final FMLLoadCompleteEvent event) {
		if (FMLEnvironment.dist.equals(Dist.CLIENT)) {
			NeoForge.EVENT_BUS.register(NeoForgeStarterClientEvents.class);
		}

		NeoForge.EVENT_BUS.register(NeoForgeStarterServerEvents.class);
	}

	private static void setGlobalConstants() {

	}
}
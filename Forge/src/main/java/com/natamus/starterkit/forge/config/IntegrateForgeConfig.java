package com.natamus.starterkit.forge.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.starterkit.util.Reference;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class IntegrateForgeConfig {
	public static void registerScreen(ModLoadingContext modLoadingContext) {
		modLoadingContext.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> {
			return new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> {
				return DuskConfig.DuskConfigScreen.getScreen(screen, Reference.MOD_ID);
			});
		});
	}
}

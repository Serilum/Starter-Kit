package com.natamus.starterkit.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.starterkit.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

	@Entry public static boolean enablePlayerMustBeNearSpawnForKitCheck = true;
	@Entry public static boolean addExistingItemsAfterKitSet = true;
	@Entry public static boolean enableFTBIslandCreateCompatibility = true;

	public static void initConfig() {
		configMetaData.put("enablePlayerMustBeNearSpawnForKitCheck", Arrays.asList(
			"If a player that hasn't logged in before must be near spawn to be given a kit. This can be useful for installing the mod on existing worlds."
		));
		configMetaData.put("addExistingItemsAfterKitSet", Arrays.asList(
			"Whether items that existed in the inventory, such as books added by other mods, should be added back to the inventory after the kit was set. If disabled, they'll be removed. You can still manually set them via the kit."
		));
		configMetaData.put("enableFTBIslandCreateCompatibility", Arrays.asList(
			"Whether the starter kit should be re-set after the '/ftbteamislands create' command from FTB Team Islands. Does nothing when it's not installed."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}
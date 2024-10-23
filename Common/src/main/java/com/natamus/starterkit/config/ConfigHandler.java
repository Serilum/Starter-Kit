package com.natamus.starterkit.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.starterkit.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

	@Entry public static boolean randomizeMultipleKitsToggle = true;
	@Entry public static boolean addExistingItemsAfterKitSet = true;
	@Entry public static boolean usePotionEffectsInStarterKit = true;
	@Entry public static boolean formatKitNames = true;

	@Entry public static String chooseKitText = "%s, you can choose a starter kit!";

	@Entry public static boolean announcePlayerKitChoiceInDedicatedServer = true;

	@Entry public static boolean enableFTBIslandCreateCompatibility = true;

	public static void initConfig() {
		configMetaData.put("randomizeMultipleKitsToggle", Arrays.asList(
			"When multiple starter kits are added via /starterkit add, there are two ways to distribute them. With this enabled, one is chosen at random. When disabled, players can choose one on first join."
		));
		configMetaData.put("addExistingItemsAfterKitSet", Arrays.asList(
			"Whether items that existed in the inventory, such as books added by other mods, should be added back to the inventory after the kit was set. If disabled, they'll be removed. You can still manually set them via the kit."
		));
		configMetaData.put("usePotionEffectsInStarterKit", Arrays.asList(
			"If potion/mob effect functionality should be enabled. This means that when creating a kit via /sk (add/set), it also saves the active effects the player has. And when handing out the starter kits, it adds the effects to new players."
		));
		configMetaData.put("formatKitNames", Arrays.asList(
			"If kit names should be formatted. Each word will be capitalized."
		));

		configMetaData.put("chooseKitScreenHeader", Arrays.asList(
			"The text used above the kit choice screen, or if the mod is not installed in the chat. %s will be replaced with the player's name."
		));

		configMetaData.put("announcePlayerKitChoiceInDedicatedServer", Arrays.asList(
			"Whether an announcement should be broadcasted to the server whenever a new player makes a kit choice. 'randomizeMultipleKitsToggle' must be disabled, and there must be at least 2 starter kits available."
		));

		configMetaData.put("enableFTBIslandCreateCompatibility", Arrays.asList(
			"Whether the starter kit should be re-set after the '/ftbteamislands create' command from FTB Team Islands. Does nothing when it's not installed."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}
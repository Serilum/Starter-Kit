package com.serilum.starterkit.config;

import com.natamus.collective.config.DuskConfig;
import com.serilum.starterkit.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

	@Entry public static boolean randomizeMultipleKitsToggle = true;
	@Entry public static String randomKitWeights = "";
	@Entry public static boolean giveStarterKitOnFirstJoin = true;
	@Entry public static boolean giveStarterKitOnRespawn = false;
	@Entry public static boolean addExistingItemsAfterKitSet = true;
	@Entry public static boolean usePotionEffectsInStarterKit = true;
	@Entry public static boolean formatKitNames = true;

	@Entry public static String chooseKitText = "%s, you can choose a starter kit!";
	@Entry public static boolean lockKitChoiceScreen = false;

	@Entry public static boolean announcePlayerKitChoiceInDedicatedServer = true;

	@Entry public static boolean enableFTBIslandCreateCompatibility = true;

	public static void initConfig() {
		configMetaData.put("randomizeMultipleKitsToggle", Arrays.asList(
			"When multiple starter kits are added via /starterkit add, there are two ways to distribute them. With this enabled, one is chosen at random. When disabled, players can choose one on first join."
		));
		configMetaData.put("randomKitWeights", Arrays.asList(
			"How likely each kit is to be picked when kits are given randomly, as comma-separated kit_name:weight pairs. Kits that are not listed have a weight of 1. For example 'warrior:5,wizard:1' makes the warrior kit 5 times as likely as the wizard kit."
		));
		configMetaData.put("giveStarterKitOnFirstJoin", Arrays.asList(
			"Whether new players receive (or get to choose) a starter kit when they join for the first time. When disabled, kits are only handed out via '/sk give <player>', which opens the kit choice screen when players can choose. Useful for an NPC or command block that hands out kits."
		));
		configMetaData.put("giveStarterKitOnRespawn", Arrays.asList(
			"Whether players receive the last starter kit they got again when they respawn after dying. Does nothing when the keepInventory gamerule is enabled."
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

		configMetaData.put("chooseKitText", Arrays.asList(
			"The text used above the kit choice screen, or if the mod is not installed in the chat. %s will be replaced with the player's name. Can also be a translation key from a resource pack, just like kit names and kit descriptions."
		));

		configMetaData.put("lockKitChoiceScreen", Arrays.asList(
			"Whether the kit choice screen stays open until a kit is chosen. When enabled, the inventory key and Escape do not close it. This is read from each player's own config, so a modpack sets it in the config it ships."
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
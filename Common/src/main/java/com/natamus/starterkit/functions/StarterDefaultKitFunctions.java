package com.natamus.starterkit.functions;

import com.natamus.starterkit.util.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StarterDefaultKitFunctions {
	public static void createDefaultKits(boolean fromRetro) throws IOException {
		boolean createDefaultKit = !fromRetro;

		String defaultKitRootPath = Util.configKitPath;
		if (fromRetro) {
			if (new File(defaultKitRootPath + File.separator + "starterkit.txt").isFile()) {
				defaultKitRootPath = Util.configInactiveKitPath;
				createDefaultKit = true;
			}
		}

		if (createDefaultKit) {
			PrintWriter defaultKitWriter = new PrintWriter(defaultKitRootPath + File.separator + "Default.txt", StandardCharsets.UTF_8);
			defaultKitWriter.print(getDefaultKitGearString());
			defaultKitWriter.close();
		}

		PrintWriter archerKitWriter = new PrintWriter(Util.configInactiveKitPath + File.separator + "Archer.txt", StandardCharsets.UTF_8);
		PrintWriter lumberjackKitWriter = new PrintWriter(Util.configInactiveKitPath + File.separator + "Lumberjack.txt", StandardCharsets.UTF_8);
		PrintWriter witchKitWriter = new PrintWriter(Util.configInactiveKitPath + File.separator + "Witch.txt", StandardCharsets.UTF_8);

		archerKitWriter.print(getArcherKitGearString());
		lumberjackKitWriter.print(getLumberjackKitGearString());
		witchKitWriter.print(getWitchKitGearString());

		archerKitWriter.close();
		lumberjackKitWriter.close();
		witchKitWriter.close();

		createDefaultKitDescriptions(fromRetro);
	}

	public static void createDefaultKitDescriptions(boolean fromRetro) throws IOException {
		PrintWriter defaultDescWriter = new PrintWriter(Util.configDescriptionPath + File.separator + "Default.txt", StandardCharsets.UTF_8);
		PrintWriter archerDescWriter = new PrintWriter(Util.configDescriptionPath + File.separator + "Archer.txt", StandardCharsets.UTF_8);
		PrintWriter lumberjackDescWriter = new PrintWriter(Util.configDescriptionPath + File.separator + "Lumberjack.txt", StandardCharsets.UTF_8);
		PrintWriter witchDescWriter = new PrintWriter(Util.configDescriptionPath + File.separator + "Witch.txt", StandardCharsets.UTF_8);

		defaultDescWriter.print("Worn by many adventurers over the years. A good pair of boots, the ability to defend yourself and enough food for a few days.");
		archerDescWriter.print("Survive longer by defeating your enemies from a distance.");
		lumberjackDescWriter.print("There's no need to punch a tree, use an axe instead!");
		witchDescWriter.print("Has a good mix of potions, useful in many scenarios.");

		defaultDescWriter.close();
		archerDescWriter.close();
		lumberjackDescWriter.close();
		witchDescWriter.close();
	}

	public static String getDefaultKitGearString() {
		StringBuilder gearString = new StringBuilder();

		gearString.append("'head' : '',").append(System.lineSeparator());
		gearString.append("'chest' : '',").append(System.lineSeparator());
		gearString.append("'legs' : '',").append(System.lineSeparator());
		gearString.append("'feet' : '{count:1,id:\"minecraft:leather_boots\"}',").append(System.lineSeparator());
		gearString.append("'offhand' : '{count:1,id:\"minecraft:shield\"}',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{count:1,id:\"minecraft:wooden_sword\"}";
			}
			else if (i == 1) {
				itemString = "{count:9,id:\"minecraft:bread\"}";
			}

			gearString.append(i).append(" : '").append(itemString).append("',").append(System.lineSeparator());
		}

		gearString.append("'effects' : '',").append(System.lineSeparator());

		return gearString.toString();
	}

	public static String getArcherKitGearString() {
		StringBuilder gearString = new StringBuilder();

		gearString.append("'head' : '{count:1,id:\"minecraft:leather_helmet\"}',").append(System.lineSeparator());
		gearString.append("'chest' : '',").append(System.lineSeparator());
		gearString.append("'legs' : '',").append(System.lineSeparator());
		gearString.append("'feet' : '{count:1,id:\"minecraft:leather_boots\"}',").append(System.lineSeparator());
		gearString.append("'offhand' : '',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{count:1,id:\"minecraft:bow\",components:{\"minecraft:enchantments\":{levels:{\"minecraft:power\":1}}}}";
			}
			else if (i == 7) {
				itemString = "{count:6,id:\"minecraft:baked_potato\"}";
			}
			else if (i == 8) {
				itemString = "{count:32,id:\"minecraft:arrow\"}";
			}

			gearString.append(i).append(" : '").append(itemString).append("',").append(System.lineSeparator());
		}

		gearString.append("'effects' : '',").append(System.lineSeparator());

		return gearString.toString();
	}

	public static String getLumberjackKitGearString() {
		StringBuilder gearString = new StringBuilder();

		gearString.append("'head' : '',").append(System.lineSeparator());
		gearString.append("'chest' : '',").append(System.lineSeparator());
		gearString.append("'legs' : '',").append(System.lineSeparator());
		gearString.append("'feet' : '{count:1,id:\"minecraft:iron_boots\"}',").append(System.lineSeparator());
		gearString.append("'offhand' : '{count:4,id:\"minecraft:oak_sapling\"}',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{count:1,id:\"minecraft:iron_axe\",components:{\"minecraft:enchantments\":{levels:{\"minecraft:efficiency\":1}}}}";
			}
			else if (i == 8) {
				itemString = "{count:8,id:\"minecraft:cooked_beef\"}";
			}

			gearString.append(i).append(" : '").append(itemString).append("',").append(System.lineSeparator());
		}

		gearString.append("'effects' : '',").append(System.lineSeparator());

		return gearString.toString();
	}

	public static String getWitchKitGearString() {
		StringBuilder gearString = new StringBuilder();

		gearString.append("'head' : '',").append(System.lineSeparator());
		gearString.append("'chest' : '',").append(System.lineSeparator());
		gearString.append("'legs' : '',").append(System.lineSeparator());
		gearString.append("'feet' : '{count:1,id:\"minecraft:golden_boots\"}',").append(System.lineSeparator());
		gearString.append("'offhand' : '',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{count:1,id:\"minecraft:stick\"}";
			}
			else if (i == 1) {
				itemString = "{count:16,id:\"minecraft:apple\"}";
			}
			else if (i == 3) {
				itemString = "{count:1,id:\"minecraft:splash_potion\",components:{\"minecraft:potion_contents\":{potion:\"minecraft:strong_swiftness\"}}}";
			}
			else if (i == 4) {
				itemString = "{count:1,id:\"minecraft:splash_potion\",components:{\"minecraft:potion_contents\":{potion:\"minecraft:strong_healing\"}}}";
			}
			else if (i == 6) {
				itemString = "{count:1,id:\"minecraft:splash_potion\",components:{\"minecraft:potion_contents\":{potion:\"minecraft:strong_poison\"}}}";
			}
			else if (i == 7) {
				itemString = "{count:1,id:\"minecraft:splash_potion\",components:{\"minecraft:potion_contents\":{potion:\"minecraft:strong_harming\"}}}";
			}
			else if (i == 8) {
				itemString = "{count:1,id:\"minecraft:splash_potion\",components:{\"minecraft:potion_contents\":{potion:\"minecraft:strong_harming\"}}}";
			}

			gearString.append(i).append(" : '").append(itemString).append("',").append(System.lineSeparator());
		}

		gearString.append("'effects' : '',").append(System.lineSeparator());

		return gearString.toString();
	}
}

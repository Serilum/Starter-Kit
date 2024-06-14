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
		gearString.append("'feet' : '{Count:1b,id:\"minecraft:leather_boots\",tag:{Damage:0}}',").append(System.lineSeparator());
		gearString.append("'offhand' : '{Count:1b,id:\"minecraft:shield\",tag:{Damage:0}}',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{Count:1b,id:\"minecraft:wooden_sword\",tag:{Damage:0}}";
			}
			else if (i == 1) {
				itemString = "{Count:9b,id:\"minecraft:bread\"}";
			}

			gearString.append(i).append(" : '").append(itemString).append("',").append(System.lineSeparator());
		}

		gearString.append("'effects' : '',").append(System.lineSeparator());

		return gearString.toString();
	}

	public static String getArcherKitGearString() {
		StringBuilder gearString = new StringBuilder();

		gearString.append("'head' : '{Count:1b,id:\"minecraft:leather_helmet\",tag:{Damage:0}}',").append(System.lineSeparator());
		gearString.append("'chest' : '',").append(System.lineSeparator());
		gearString.append("'legs' : '',").append(System.lineSeparator());
		gearString.append("'feet' : '{Count:1b,id:\"minecraft:leather_boots\",tag:{Damage:0}}',").append(System.lineSeparator());
		gearString.append("'offhand' : '',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{Count:1b,id:\"minecraft:bow\",tag:{Damage:0,Enchantments:[{id:\"minecraft:power\",lvl:1s}],RepairCost:1}}";
			}
			else if (i == 7) {
				itemString = "{Count:6b,id:\"minecraft:baked_potato\"}";
			}
			else if (i == 8) {
				itemString = "{Count:32b,id:\"minecraft:arrow\"}";
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
		gearString.append("'feet' : '{Count:1b,id:\"minecraft:iron_boots\",tag:{Damage:0}}',").append(System.lineSeparator());
		gearString.append("'offhand' : '{Count:4b,id:\"minecraft:oak_sapling\"}',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{Count:1b,id:\"minecraft:iron_axe\",tag:{Damage:0,Enchantments:[{id:\"minecraft:efficiency\",lvl:1s}],RepairCost:1}}";
			}
			else if (i == 8) {
				itemString = "{Count:8b,id:\"minecraft:cooked_beef\"}";
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
		gearString.append("'feet' : '{Count:1b,id:\"minecraft:golden_boots\",tag:{Damage:0}}',").append(System.lineSeparator());
		gearString.append("'offhand' : '',").append(System.lineSeparator());

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String itemString = "";

			if (i == 0) {
				itemString = "{Count:1b,id:\"minecraft:stick\"}";
			}
			else if (i == 1) {
				itemString = "{Count:16b,id:\"minecraft:apple\"}";
			}
			else if (i == 3) {
				itemString = "{Count:1b,id:\"minecraft:splash_potion\",tag:{Potion:\"minecraft:strong_swiftness\"}}";
			}
			else if (i == 4) {
				itemString = "{Count:1b,id:\"minecraft:splash_potion\",tag:{Potion:\"minecraft:strong_healing\"}}";
			}
			else if (i == 6) {
				itemString = "{Count:1b,id:\"minecraft:splash_potion\",tag:{Potion:\"minecraft:strong_poison\"}}";
			}
			else if (i == 7) {
				itemString = "{Count:1b,id:\"minecraft:splash_potion\",tag:{Potion:\"minecraft:strong_harming\"}}";
			}
			else if (i == 8) {
				itemString = "{Count:1b,id:\"minecraft:splash_potion\",tag:{Potion:\"minecraft:strong_harming\"}}";
			}

			gearString.append(i).append(" : '").append(itemString).append("',").append(System.lineSeparator());
		}

		gearString.append("'effects' : '',").append(System.lineSeparator());

		return gearString.toString();
	}
}

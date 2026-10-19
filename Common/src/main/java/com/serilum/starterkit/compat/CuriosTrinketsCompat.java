package com.serilum.starterkit.compat;

import com.natamus.collective.functions.GearFunctions;
import com.natamus.collective.functions.ItemFunctions;
import com.natamus.collective.functions.NumberFunctions;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CuriosTrinketsCompat {
	private static final String curiosSlotPrefix = "curios:";
	private static final String trinketsSlotPrefix = "trinkets:";

	public static boolean isModSlotLine(String line) {
		String strippedLine = line.strip();
		return strippedLine.startsWith("'" + curiosSlotPrefix) || strippedLine.startsWith("'" + trinketsSlotPrefix);
	}

	public static String getGearStringWithoutModSlots(String kitFileContent) {
		return joinKitFileLines(kitFileContent, false);
	}

	public static String getModSlotGearString(String kitFileContent) {
		return joinKitFileLines(kitFileContent, true);
	}

	private static String joinKitFileLines(String kitFileContent, boolean modSlotLines) {
		StringBuilder gearString = new StringBuilder();
		for (String line : kitFileContent.split("\\R")) {
			if (isModSlotLine(line) != modSlotLines) {
				continue;
			}

			if (!gearString.isEmpty()) {
				gearString.append("\n");
			}
			gearString.append(line);
		}

		return gearString.toString();
	}

	public static String getModSlotGearStringFromPlayer(Player player) {
		return getModSlotGearStringFromPlayerTag(player.saveWithoutId(new CompoundTag()));
	}

	public static String getModSlotGearStringFromPlayerTag(CompoundTag playerTag) {
		List<String> modSlotLines = new ArrayList<>();

		ListTag curiosEntries = playerTag.getCompound("ForgeCaps").getCompound("curios:inventory").getList("Curios", Tag.TAG_COMPOUND);
		for (int i = 0; i < curiosEntries.size(); i++) {
			CompoundTag curiosEntry = curiosEntries.getCompound(i);
			String curioSlot = curiosEntry.getString("Identifier");

			ListTag curioStacks = curiosEntry.getCompound("StacksHandler").getCompound("Stacks").getList("Items", Tag.TAG_COMPOUND);
			for (int stackIndex = 0; stackIndex < curioStacks.size(); stackIndex++) {
				CompoundTag itemTag = curioStacks.getCompound(stackIndex);

				ItemStack itemStack = ItemStack.of(itemTag);
				if (!curioSlot.isEmpty() && !itemStack.isEmpty()) {
					modSlotLines.add(getModSlotLine(curiosSlotPrefix + curioSlot + ":" + itemTag.getInt("Slot"), itemStack));
				}
			}
		}

		CompoundTag trinketGroups = playerTag.getCompound("cardinal_components").getCompound("trinkets:trinkets");
		for (String trinketGroup : trinketGroups.getAllKeys()) {
			CompoundTag trinketSlots = trinketGroups.getCompound(trinketGroup);
			for (String trinketSlot : trinketSlots.getAllKeys()) {
				ListTag trinketItems = trinketSlots.getCompound(trinketSlot).getList("Items", Tag.TAG_COMPOUND);
				for (int offset = 0; offset < trinketItems.size(); offset++) {
					ItemStack itemStack = ItemStack.of(trinketItems.getCompound(offset));
					if (!itemStack.isEmpty()) {
						modSlotLines.add(getModSlotLine(trinketsSlotPrefix + trinketGroup + "/" + trinketSlot + ":" + offset, itemStack));
					}
				}
			}
		}

		return String.join("\n", modSlotLines);
	}

	public static String getModSlotLine(String slotKey, ItemStack itemStack) {
		String itemData = ItemFunctions.getNBTStringFromItemStack(itemStack).replaceAll("[\\r\\n]", "|n").replace("'", "‵");
		return "'" + slotKey + "' : '" + itemData + "',";
	}

	public static List<ItemStack> getModSlotItems(String modSlotGearString) {
		List<ItemStack> modSlotItems = new ArrayList<>();
		if (modSlotGearString.isEmpty()) {
			return modSlotItems;
		}

		for (ItemStack itemStack : GearFunctions.getHashMapFromGearString(modSlotGearString).values()) {
			if (!itemStack.isEmpty()) {
				modSlotItems.add(itemStack);
			}
		}

		return modSlotItems;
	}

	public static List<String> getModSlotCommands(String modSlotGearString) {
		List<String> modSlotCommands = new ArrayList<>();
		if (modSlotGearString.isEmpty()) {
			return modSlotCommands;
		}

		HashMap<String, ItemStack> modSlotItems = GearFunctions.getHashMapFromGearString(modSlotGearString);
		for (String slotKey : modSlotItems.keySet()) {
			ItemStack itemStack = modSlotItems.get(slotKey);
			if (itemStack.isEmpty()) {
				continue;
			}

			String modSlotCommand = getModSlotCommand(slotKey, getItemArgument(itemStack), itemStack.getCount());
			if (!modSlotCommand.isEmpty()) {
				modSlotCommands.add(modSlotCommand);
			}
		}

		return modSlotCommands;
	}

	private static String getModSlotCommand(String slotKey, String itemArgument, int count) {
		int indexSeparator = slotKey.lastIndexOf(":");
		if (indexSeparator < 0) {
			return "";
		}

		String slotIndex = slotKey.substring(indexSeparator + 1);
		if (!NumberFunctions.isNumeric(slotIndex)) {
			return "";
		}

		if (slotKey.startsWith(curiosSlotPrefix)) {
			String curioSlot = slotKey.substring(curiosSlotPrefix.length(), indexSeparator);
			return "curios replace " + curioSlot + " " + slotIndex + " @s with " + itemArgument + " " + count;
		}

		if (slotKey.startsWith(trinketsSlotPrefix)) {
			String[] groupAndSlot = slotKey.substring(trinketsSlotPrefix.length(), indexSeparator).split("/");
			if (groupAndSlot.length != 2) {
				return "";
			}

			return "trinkets set " + groupAndSlot[0] + " " + groupAndSlot[1] + " " + slotIndex + " " + itemArgument; // Trinkets' own count argument throws, so a trinket is always placed as one item.
		}

		return "";
	}

	public static String getItemArgument(ItemStack itemStack) {
		return new ItemInput(itemStack.getItemHolder(), itemStack.getTag()).serialize();
	}
}

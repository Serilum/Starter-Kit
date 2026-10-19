package com.serilum.starterkit.compat;

import com.natamus.collective.functions.GearFunctions;
import com.natamus.collective.functions.ItemFunctions;
import com.natamus.collective.functions.NumberFunctions;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
		Level level = player.level();

		TagValueOutput playerOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		player.saveWithoutId(playerOutput);

		return getModSlotGearStringFromPlayerTag(level, playerOutput.buildResult());
	}

	public static String getModSlotGearStringFromPlayerTag(Level level, CompoundTag playerTag) {
		List<String> modSlotLines = new ArrayList<>();

		ListTag curiosEntries = playerTag.getCompoundOrEmpty("neoforge:attachments").getCompoundOrEmpty("curios:inventory").getListOrEmpty("Curios");
		for (int i = 0; i < curiosEntries.size(); i++) {
			CompoundTag curiosEntry = curiosEntries.getCompoundOrEmpty(i);
			for (String curioSlot : curiosEntry.keySet()) {
				ListTag curioStacks = curiosEntry.getCompoundOrEmpty(curioSlot).getCompoundOrEmpty("Stacks").getListOrEmpty("stacks");
				for (int slotIndex = 0; slotIndex < curioStacks.size(); slotIndex++) {
					ItemStack itemStack = parseItemStack(level, curioStacks.getCompoundOrEmpty(slotIndex));
					if (!itemStack.isEmpty()) {
						modSlotLines.add(getModSlotLine(level, curiosSlotPrefix + curioSlot + ":" + slotIndex, itemStack));
					}
				}
			}
		}

		return String.join("\n", modSlotLines);
	}

	private static ItemStack parseItemStack(Level level, CompoundTag itemTag) {
		if (itemTag.isEmpty()) {
			return ItemStack.EMPTY;
		}

		Optional<ItemStack> itemStack = ItemStack.CODEC.parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), itemTag).result();
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		return itemStack.get();
	}

	public static String getModSlotLine(Level level, String slotKey, ItemStack itemStack) {
		String itemData = ItemFunctions.getNBTStringFromItemStack(level, itemStack).replaceAll("[\\r\\n]", "|n").replace("'", "‵");
		return "'" + slotKey + "' : '" + itemData + "',";
	}

	public static List<ItemStack> getModSlotItems(Level level, String modSlotGearString) {
		List<ItemStack> modSlotItems = new ArrayList<>();
		if (modSlotGearString.isEmpty()) {
			return modSlotItems;
		}

		for (ItemStack itemStack : GearFunctions.getHashMapFromGearString(level, modSlotGearString).values()) {
			if (!itemStack.isEmpty()) {
				modSlotItems.add(itemStack);
			}
		}

		return modSlotItems;
	}

	public static List<String> getModSlotCommands(Level level, String modSlotGearString) {
		List<String> modSlotCommands = new ArrayList<>();
		if (modSlotGearString.isEmpty()) {
			return modSlotCommands;
		}

		HashMap<String, ItemStack> modSlotItems = GearFunctions.getHashMapFromGearString(level, modSlotGearString);
		for (String slotKey : modSlotItems.keySet()) {
			ItemStack itemStack = modSlotItems.get(slotKey);
			if (itemStack.isEmpty()) {
				continue;
			}

			String modSlotCommand = getModSlotCommand(slotKey, getItemArgument(level, itemStack), itemStack.getCount());
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

	public static String getItemArgument(Level level, ItemStack itemStack) {
		StringBuilder itemArgument = new StringBuilder(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());

		RegistryOps<Tag> registryOps = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		DataComponentPatch.SplitResult componentChanges = itemStack.getComponentsPatch().split();

		List<String> componentArguments = new ArrayList<>();
		for (TypedDataComponent<?> component : componentChanges.added()) {
			Identifier componentKey = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
			Optional<Tag> encodedComponent = component.encodeValue(registryOps).result();
			if (componentKey == null || encodedComponent.isEmpty()) {
				continue;
			}

			componentArguments.add(componentKey + "=" + encodedComponent.get());
		}

		for (DataComponentType<?> removedComponentType : componentChanges.removed()) {
			Identifier componentKey = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(removedComponentType);
			if (componentKey != null) {
				componentArguments.add("!" + componentKey);
			}
		}

		if (!componentArguments.isEmpty()) {
			itemArgument.append("[").append(String.join(",", componentArguments)).append("]");
		}

		return itemArgument.toString();
	}
}

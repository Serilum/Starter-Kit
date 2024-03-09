package com.natamus.starterkit.functions;

import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.*;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.data.Constants;
import com.natamus.starterkit.data.Variables;
import com.natamus.starterkit.networking.packets.ToClientAskIfModIsInstalledPacket;
import com.natamus.starterkit.networking.packets.ToClientSelectFirstSlotPacket;
import com.natamus.starterkit.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class StarterGearFunctions {
	public static void initStarterKitHandle(Level level, Player player, @Nullable Player commandPlayer) {
		initStarterKitHandle(level, player, commandPlayer, "");
	}
	public static void initStarterKitHandle(Level level, Player player, @Nullable Player commandPlayer, String kitName) {
		final UUID uuid = player.getUUID();

		TaskFunctions.enqueueCollectiveTask(level.getServer(), () -> {
			if (StarterCheckFunctions.shouldPlayerReceiveStarterKit(level, player) || commandPlayer != null) {
				if (ConfigHandler.usePotionEffectsInStarterKit) {
					player.removeAllEffects();
				}

				if (!ConfigHandler.randomizeMultipleKitsToggle && Variables.starterGearEntries.size() > 1 && kitName.equals("")) {
					Dispatcher.sendToClient(new ToClientAskIfModIsInstalledPacket(), (ServerPlayer) player);

					TaskFunctions.enqueueCollectiveTask(level.getServer(), () -> {
						if (!Variables.playersWithModInstalledOnClient.contains(uuid)) {
							StarterGearFunctions.chooseOrGiveStarterKit(player, commandPlayer, kitName);
						}
					}, 100);
				}
				else {
					StarterGearFunctions.giveStarterKit(player, commandPlayer, kitName);
				}
			}
		}, 10);
	}
	public static void chooseOrGiveStarterKit(Player player, @Nullable Player commandPlayer, String kitName) {
		int kitCount = Variables.starterGearEntries.size();
		if (kitCount == 0) {
			return;
		}

		if (!ConfigHandler.randomizeMultipleKitsToggle && kitCount > 1 && kitName.equals("")) {
			chooseStarterKitViaCommands(player);
			return;
		}

		giveStarterKit(player, commandPlayer, kitName);
	}

	public static void chooseStarterKitViaCommands(Player player) {
		Level level = player.level;
		if (level.isClientSide) {
			return;
		}

		MessageFunctions.sendMessage(player, Component.literal(ConfigHandler.chooseKitText.replace("%s", player.getName().getString())).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD), true);
		MessageFunctions.sendMessage(player, "  /sk choose <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendMessage(player, "  /sk info <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendMessage(player, " Available kits: " + StringUtils.join(getActiveKitNames(), ", "), ChatFormatting.GRAY, true);
	}

	public static String giveStarterKit(Player player, @Nullable Player commandPlayer) {
		return giveStarterKit(player, commandPlayer, "");
	}
	public static String giveStarterKit(Player player, @Nullable Player commandPlayer, String kitName) {
		if (Variables.starterGearEntries.isEmpty()) {
			return null;
		}

		if (!Variables.starterGearEntries.containsKey(kitName)) {
			kitName = "";
		}

		String kitGearString = "";
		if (kitName.equals("")) {
			String[] allKitNames = Variables.starterGearEntries.keySet().toArray(new String[0]);
			String randomKitName = allKitNames[GlobalVariables.random.nextInt(allKitNames.length)];

			if (!Variables.starterGearEntries.containsKey(randomKitName)) {
				Constants.logger.warn(Constants.logPrefix + "Unable to find a starter kit to give with the name '" + randomKitName + "'.");
				return null;
			}

			kitGearString = Variables.starterGearEntries.get(randomKitName);
			kitName = randomKitName;
		}
		else {
			kitGearString = Variables.starterGearEntries.get(kitName);
		}

		if (kitGearString.equals("")) {
			Constants.logger.warn(Constants.logPrefix + "Unable to find a starter kit to give.");
			return null;
		}

		List<ItemStack> toAddAfter = new ArrayList<ItemStack>();
		if (ConfigHandler.addExistingItemsAfterKitSet) {
			Inventory inv = player.getInventory();
			boolean isempty = true;
			for (int i=0; i < 36; i++) {
				ItemStack itemStack = inv.getItem(i);
				if (!itemStack.isEmpty()) {
					toAddAfter.add(itemStack.copy());
				}
			}
		}

		GearFunctions.setPlayerGearFromGearString(player, kitGearString, ConfigHandler.usePotionEffectsInStarterKit);

		if (toAddAfter.size() > 0) {
			for (ItemStack itemStackToAdd : toAddAfter) {
				ItemFunctions.giveOrDropItemStack(player, itemStackToAdd);
			}
		}

		if (commandPlayer != null) {
			MessageFunctions.sendMessage(commandPlayer, player.getName().getString() + " has been given the '" + Util.formatKitName(kitName) + "' starter kit!", ChatFormatting.DARK_GREEN, true);
		}

		StarterCheckFunctions.addPlayerToTrackingMap(player);

		Dispatcher.sendToClient(new ToClientSelectFirstSlotPacket(), (ServerPlayer)player);
		return kitName;
	}

	public static String createStarterKitFile(Player player, String kitName, boolean adding) {
		String playerGearString = GearFunctions.getGearStringFromPlayer(player, ConfigHandler.usePotionEffectsInStarterKit);

		if (!adding) {
			moveAllKitsToInactive();
		}

		if (kitName.equals("")) {
			File[] files = Util.configKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
			kitName = "Kit_" + (files.length+1);
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(Util.configKitPath + File.separator + kitName + ".txt", StandardCharsets.UTF_8);
			writer.print(playerGearString);
		}
		catch (IOException ex) {
			Constants.logger.warn(Constants.logPrefix + "Something went wrong while writing the new starter kit file.");
			ex.printStackTrace();
		}

		if (writer != null) {
			writer.close();
		}

		processKitFiles();
		return kitName;
	}

	public static boolean moveKitToInactive(String kitName) {
		String kitPath = Util.configKitPath + File.separator + kitName + ".txt";
		File kit = new File(kitPath);
		if (!kit.isFile()) {
			return false;
		}

		String destinationPath = Util.configInactiveKitDir.getAbsolutePath() + File.separator + kitName + ".txt";

		boolean movedCorrectly = moveKit(kitPath, destinationPath);

		processKitFiles();
		return movedCorrectly;
	}
	public static void moveAllKitsToInactive() {
		File[] files = Util.configKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files ) {
			String filePath = Util.configKitDir.getAbsolutePath() + File.separator + file.getName();
			String destinationPath = Util.configInactiveKitDir.getAbsolutePath() + File.separator + file.getName();

			moveKit(filePath, destinationPath);
		}

		processKitFiles();
	}

	public static boolean moveKitToActive(String kitName) {
		String kitPath = Util.configInactiveKitPath + File.separator + kitName + ".txt";
		File kit = new File(kitPath);
		if (!kit.isFile()) {
			return false;
		}

		String destinationPath = Util.configKitDir.getAbsolutePath() + File.separator + kitName + ".txt";

		boolean movedCorrectly = moveKit(kitPath, destinationPath);

		processKitFiles();
		return movedCorrectly;
	}
	public static void moveAllKitsToActive() {
		File[] files = Util.configInactiveKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files ) {
			String filePath = Util.configInactiveKitDir.getAbsolutePath() + File.separator + file.getName();
			String destinationPath = Util.configKitDir.getAbsolutePath() + File.separator + file.getName();

			moveKit(filePath, destinationPath);
		}

		processKitFiles();
	}

	private static boolean moveKit(String filePath, String destinationPath) {
		File destinationFile = new File(destinationPath);
		while (destinationFile.exists()) {
			destinationPath = destinationPath.replace(".txt", "_.txt");
			destinationFile = new File(destinationPath);
		}

		try {
			Files.move(Paths.get(filePath), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			return false;
		}

		return true;
	}

	public static void processKitFiles() {
		Variables.starterGearEntries = new HashMap<String, String>();

		try {
			File[] files = Util.configKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
			if (files.length == 0 && !(new File(Util.configInactiveKitPath + File.separator + "Default.txt").isFile())) {
				StarterDefaultKitFunctions.createDefaultKits(false);

				files = Util.configKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
			}

			// Check for incorrect file names
			boolean renamedFiles = false;
			for (File file : files) {
				String fileName = file.getName();
				if (fileName.contains(" ")) {
					Files.move(Paths.get(file.getAbsolutePath()), Paths.get(file.getAbsolutePath().replace(fileName, fileName.replace(" ", "_"))), StandardCopyOption.REPLACE_EXISTING);
					renamedFiles = true;
				}
			}

			if (renamedFiles) {
				files = Util.configKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
			}

			for (File file : files) {
				String kitName = file.getName().replace(".txt", "");
				String gearString = Files.readString(Paths.get(file.getAbsolutePath()));
				Variables.starterGearEntries.put(kitName, gearString);

				File matchingKitDescriptionFile = new File(Util.configDescriptionPath + File.separator + kitName + ".txt");
				if (!matchingKitDescriptionFile.isFile()) {
					PrintWriter descriptionWriter = new PrintWriter(Util.configDescriptionPath + File.separator + kitName + ".txt", StandardCharsets.UTF_8);

					descriptionWriter.print("You can edit this description in ./config/starterkit/description/" + kitName + ".txt!");

					descriptionWriter.close();
				}
			}

			processKitDescriptionFiles();
		}
		catch (Exception ex) {
			Constants.logger.warn(Constants.logPrefix + "Unable to process the starter kit files.");
			ex.printStackTrace();
		}
	}

	private static void processKitDescriptionFiles() throws IOException {
		Variables.starterKitDescriptions = new HashMap<String, String>();

		File[] files = Util.configDescriptionDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files) {
			String kitName = file.getName().replace(".txt", "");
			String kitDescription = Files.readString(Paths.get(file.getAbsolutePath()));
			Variables.starterKitDescriptions.put(kitName, kitDescription);
		}
	}

	public static List<String> getActiveKitNames() {
		return getActiveKitNames(Variables.starterGearEntries, false);
	}
	public static List<String> getActiveKitNames(boolean includeAll) {
		return getActiveKitNames(Variables.starterGearEntries, includeAll);
	}
	public static List<String> getActiveKitNames(HashMap<String, String> entryMap) {
		return getActiveKitNames(entryMap, false);
	}
	public static List<String> getActiveKitNames(HashMap<String, String> entryMap, boolean includeAll) {
		List<String> kitNames = new ArrayList<String>(entryMap.keySet());

		if (includeAll) {
			kitNames.add("_all");
		}

		Collections.sort(kitNames);

		return kitNames;
	}

	public static List<String> getInactiveKitNames() {
		return getInactiveKitNames(false);
	}
	public static List<String> getInactiveKitNames(boolean includeAll) {
		List<String> inactiveKitNames = new ArrayList<String>();

		File[] files = Util.configInactiveKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files) {
			String inactiveKitName = file.getName().replace(".txt", "");
			inactiveKitNames.add(inactiveKitName);
		}

		if (includeAll) {
			inactiveKitNames.add("_all");
		}

		Collections.sort(inactiveKitNames);

		return inactiveKitNames;
	}

	public static int showKitInformation(Player player, String kitName) {
		String kitDescription = "";
		if (Variables.starterKitDescriptions.containsKey(kitName)) {
			kitDescription = Variables.starterKitDescriptions.get(kitName);

			if (kitDescription.contains("/config/starterkit/description/")) {
				kitDescription = "N/A";
			}
		}

		MessageFunctions.sendMessage(player, Component.literal("Name: ").withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(Util.formatKitName(kitName)).withStyle(ChatFormatting.GRAY)), true);
		MessageFunctions.sendMessage(player, Component.literal("Description: ").withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(kitDescription).withStyle(ChatFormatting.GRAY)));

		StringBuilder kitItems = new StringBuilder();
		if (Variables.starterGearEntries.containsKey(kitName)) {
			List<ItemStack> kitItemList = GearFunctions.getItemStackListFromGearString(Variables.starterGearEntries.get(kitName));

			for (ItemStack itemStack : kitItemList) {
				if (itemStack.isEmpty()) {
					continue;
				}

				if (!kitItems.toString().equals("")) {
					kitItems.append(", ");
				}

				String count = itemStack.getCount() + " ";
				if (count.equals("1 ")) {
					count = "";
				}

				kitItems.append(count).append(itemStack.getDisplayName().getString().replace("[", "").replace("]", "").toLowerCase());
			}
		}

		MessageFunctions.sendMessage(player, Component.literal("Items: ").withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(kitItems.toString()).withStyle(ChatFormatting.GRAY)));
		return 1;
	}
}

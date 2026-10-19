package com.serilum.starterkit.functions;

import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.GearFunctions;
import com.natamus.collective.functions.ItemFunctions;
import com.natamus.collective.functions.MessageFunctions;
import com.natamus.collective.functions.NumberFunctions;
import com.natamus.collective.functions.TaskFunctions;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.serilum.starterkit.compat.CuriosTrinketsCompat;
import com.serilum.starterkit.config.ConfigHandler;
import com.serilum.starterkit.data.Constants;
import com.serilum.starterkit.data.Variables;
import com.serilum.starterkit.networking.packets.ToClientAskIfModIsInstalledPacket;
import com.serilum.starterkit.networking.packets.ToClientSelectFirstSlotPacket;
import com.serilum.starterkit.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
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
	public static void initStarterKitHandle(Level level, Player player, @Nullable CommandSourceStack commandSource) {
		initStarterKitHandle(level, player, commandSource, "");
	}
	public static void initStarterKitHandle(Level level, Player player, @Nullable CommandSourceStack commandSource, String kitName) {
		final UUID uuid = player.getUUID();

		TaskFunctions.enqueueCollectiveServerTask(level.getServer(), () -> {
			if (player.isRemoved()) {
				return;
			}

			if (StarterCheckFunctions.shouldPlayerReceiveStarterKit(level, player) || commandSource != null) {
				if (!ConfigHandler.randomizeMultipleKitsToggle && Variables.starterGearEntries.size() > 1 && kitName.isEmpty()) {
					Variables.playersWithModInstalledOnClient.remove(uuid);
					Dispatcher.sendToClient(new ToClientAskIfModIsInstalledPacket(), (ServerPlayer) player);

					TaskFunctions.enqueueCollectiveServerTask(level.getServer(), () -> {
						if (!Variables.playersWithModInstalledOnClient.contains(uuid)) {
							chooseOrGiveStarterKit(player, commandSource, kitName);
						}
					}, 100);
				}
				else {
					giveStarterKit(player, commandSource, kitName);
				}
			}
		}, 10);
	}
	public static void chooseOrGiveStarterKit(Player player, @Nullable CommandSourceStack commandSource, String kitName) {
		int kitCount = Variables.starterGearEntries.size();
		if (kitCount == 0) {
			return;
		}

		if (!ConfigHandler.randomizeMultipleKitsToggle && kitCount > 1 && kitName.isEmpty()) {
			chooseStarterKitViaCommands(player);
			return;
		}

		giveStarterKit(player, commandSource, kitName);
	}

	public static void chooseStarterKitViaCommands(Player player) {
		Level level = player.level();
		if (level.isClientSide) {
			return;
		}

		MessageFunctions.sendMessage(player, Util.getCustomTextComponent(ConfigHandler.chooseKitText, player.getName().getString()).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD), true);
		MessageFunctions.sendMessage(player, "  /sk choose <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendMessage(player, "  /sk info <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendTranslatableMessage(player, " ", "collective.starterkit.message.availablekits", true, ChatFormatting.GRAY, StringUtils.join(getActiveKitNames(), ", "));
	}

	public static String giveStarterKit(Player player, @Nullable CommandSourceStack commandSource) {
		return giveStarterKit(player, commandSource, "");
	}
	public static String giveStarterKit(Player player, @Nullable CommandSourceStack commandSource, String kitName) {
		if (Variables.starterGearEntries.isEmpty() || player.isRemoved()) {
			return null;
		}

		if (!Variables.starterGearEntries.containsKey(kitName)) {
			kitName = "";
		}

		String kitGearString = "";
		if (kitName.isEmpty()) {
			String randomKitName = getWeightedRandomKitName();

			if (!Variables.starterGearEntries.containsKey(randomKitName)) {
				Constants.logger.warn(Constants.logPrefix + "Unable to find a starter kit to give with the name '{}'.", randomKitName);
				return null;
			}

			kitGearString = Variables.starterGearEntries.get(randomKitName);
			kitName = randomKitName;
		}
		else {
			kitGearString = Variables.starterGearEntries.get(kitName);
		}

		if (kitGearString.isEmpty()) {
			Constants.logger.warn(Constants.logPrefix + "Unable to find a starter kit to give.");
			return null;
		}

		List<ItemStack> toAddAfter = new ArrayList<>();
		if (ConfigHandler.addExistingItemsAfterKitSet) {
			Inventory inv = player.getInventory();
			for (int i=0; i < 36; i++) {
				ItemStack itemStack = inv.getItem(i);
				if (!itemStack.isEmpty()) {
					toAddAfter.add(itemStack.copy());
				}
			}

			for (EquipmentSlot slot : Constants.equipmentSlots) {
				ItemStack itemStack = player.getItemBySlot(slot);
				if (!itemStack.isEmpty()) {
					toAddAfter.add(itemStack.copy());
				}
			}
		}

		if (!toAddAfter.isEmpty()) {
			player.getInventory().clearContent();
		}

		GearFunctions.setPlayerGearFromGearString(player, kitGearString, ConfigHandler.usePotionEffectsInStarterKit);

		if (!toAddAfter.isEmpty()) {
			for (ItemStack itemStackToAdd : toAddAfter) {
				ItemFunctions.giveOrDropItemStack(player, itemStackToAdd);
			}
		}

		if (commandSource != null) {
			MessageFunctions.sendTranslatableMessage(commandSource, "collective.starterkit.message.givenstarterkit", true, ChatFormatting.DARK_GREEN, player.getName().getString(), Util.formatKitName(kitName));
		}

		StarterCheckFunctions.addPlayerToTrackingMap(player);
		rememberReceivedKit(player, kitName);

		ServerPlayer serverPlayer = (ServerPlayer)player;
		serverPlayer.inventoryMenu.sendAllDataToRemote();

		runKitCommands(serverPlayer, kitName);
		runCommandsAtPlayer(serverPlayer, CuriosTrinketsCompat.getModSlotCommands(Variables.starterKitModSlotGear.getOrDefault(kitName, "")));

		Dispatcher.sendToClient(new ToClientSelectFirstSlotPacket(), serverPlayer);
		return kitName;
	}

	public static String createStarterKitFile(Player player, String kitName, boolean adding) {
		String playerGearString = GearFunctions.getGearStringFromPlayer(player, ConfigHandler.usePotionEffectsInStarterKit);

		String modSlotGearString = CuriosTrinketsCompat.getModSlotGearStringFromPlayer(player);
		if (!modSlotGearString.isEmpty()) {
			playerGearString += "\n" + modSlotGearString;
		}

		if (!adding) {
			moveAllKitsToInactive();
		}

		if (kitName.isEmpty()) {
			int kitNumber = 1;
			while (new File(Util.configKitDir, "Kit_" + kitNumber + ".txt").exists() || new File(Util.configInactiveKitDir, "Kit_" + kitNumber + ".txt").exists()) {
				kitNumber += 1;
			}

			kitName = "Kit_" + kitNumber;
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
		Variables.starterGearEntries = new HashMap<>();
		Variables.starterKitModSlotGear = new HashMap<>();

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
				String kitFileContent = Files.readString(Paths.get(file.getAbsolutePath()));
				Variables.starterGearEntries.put(kitName, CuriosTrinketsCompat.getGearStringWithoutModSlots(kitFileContent));

				String modSlotGearString = CuriosTrinketsCompat.getModSlotGearString(kitFileContent);
				if (!modSlotGearString.isEmpty()) {
					Variables.starterKitModSlotGear.put(kitName, modSlotGearString);
				}

				File matchingKitDescriptionFile = new File(Util.configDescriptionPath + File.separator + kitName + ".txt");
				if (!matchingKitDescriptionFile.isFile()) {
					PrintWriter descriptionWriter = new PrintWriter(Util.configDescriptionPath + File.separator + kitName + ".txt", StandardCharsets.UTF_8);

					descriptionWriter.print("You can edit this description in ./config/serilum/starterkit/descriptions/" + kitName + ".txt!");

					descriptionWriter.close();
				}

				File matchingKitCommandFile = new File(Util.configCommandPath + File.separator + kitName + ".txt");
				if (!matchingKitCommandFile.isFile()) {
					PrintWriter commandWriter = new PrintWriter(Util.configCommandPath + File.separator + kitName + ".txt", StandardCharsets.UTF_8);

					commandWriter.println("# Commands that run when a player receives the '" + kitName + "' kit, one per line. Lines starting with # are ignored.");
					commandWriter.println("# They run as the server at the player's position. @s is the player, and %s is replaced with the player's name.");
					commandWriter.print("# For example: tag @s add " + kitName.toLowerCase());

					commandWriter.close();
				}
			}

			processKitDescriptionFiles();
			processKitCommandFiles();
		}
		catch (Exception ex) {
			Constants.logger.warn(Constants.logPrefix + "Unable to process the starter kit files.");
			ex.printStackTrace();
		}
	}

	private static void processKitDescriptionFiles() throws IOException {
		Variables.starterKitDescriptions = new HashMap<>();

		File[] files = Util.configDescriptionDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files) {
			String kitName = file.getName().replace(".txt", "");
			String kitDescription = Files.readString(Paths.get(file.getAbsolutePath()));
			Variables.starterKitDescriptions.put(kitName.toLowerCase(), kitDescription);
		}
	}

	private static void processKitCommandFiles() throws IOException {
		Variables.starterKitCommands = new HashMap<>();

		File[] files = Util.configCommandDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files) {
			String kitName = file.getName().replace(".txt", "");

			List<String> kitCommands = new ArrayList<>();
			for (String line : Files.readAllLines(Paths.get(file.getAbsolutePath()))) {
				String kitCommand = line.strip();
				if (kitCommand.isEmpty() || kitCommand.startsWith("#")) {
					continue;
				}

				kitCommands.add(kitCommand);
			}

			Variables.starterKitCommands.put(kitName.toLowerCase(), kitCommands);
		}
	}

	public static String getWeightedRandomKitName() {
		List<String> kitNames = getActiveKitNames();
		HashMap<String, Integer> kitWeights = getConfiguredKitWeights();

		int totalWeight = 0;
		for (String kitName : kitNames) {
			totalWeight += kitWeights.getOrDefault(kitName, 1);
		}

		if (totalWeight <= 0) {
			return kitNames.get(GlobalVariables.random.nextInt(kitNames.size()));
		}

		int roll = GlobalVariables.random.nextInt(totalWeight);
		for (String kitName : kitNames) {
			roll -= kitWeights.getOrDefault(kitName, 1);
			if (roll < 0) {
				return kitName;
			}
		}

		return kitNames.get(0);
	}

	private static HashMap<String, Integer> getConfiguredKitWeights() {
		HashMap<String, Integer> kitWeights = new HashMap<>();

		for (String rawKitWeight : ConfigHandler.randomKitWeights.split(",")) {
			String[] kitWeightParts = rawKitWeight.split(":");
			if (kitWeightParts.length != 2) {
				continue;
			}

			String weightString = kitWeightParts[1].strip();
			if (!NumberFunctions.isNumeric(weightString)) {
				continue;
			}

			String kitName = Util.findCorrectKitNameFromInput(kitWeightParts[0].strip());
			long kitWeight = Math.max(0, Math.min(1000000, (long)Double.parseDouble(weightString)));
			kitWeights.put(kitName, (int)kitWeight);
		}

		return kitWeights;
	}

	private static void rememberReceivedKit(Player player, String kitName) {
		for (String tag : new ArrayList<>(player.getTags())) {
			if (tag.startsWith(Constants.receivedKitTagPrefix)) {
				player.removeTag(tag);
			}
		}

		player.addTag(Constants.receivedKitTagPrefix + kitName);
	}

	public static String getLastReceivedKitName(Player player) {
		for (String tag : player.getTags()) {
			if (tag.startsWith(Constants.receivedKitTagPrefix)) {
				return tag.substring(Constants.receivedKitTagPrefix.length());
			}
		}

		return "";
	}

	private static void runKitCommands(ServerPlayer serverPlayer, String kitName) {
		List<String> kitCommands = Variables.starterKitCommands.get(kitName.toLowerCase());
		if (kitCommands == null) {
			return;
		}

		String playerName = serverPlayer.getName().getString();

		List<String> playerKitCommands = new ArrayList<>();
		for (String kitCommand : kitCommands) {
			playerKitCommands.add(kitCommand.replace("%s", playerName));
		}

		runCommandsAtPlayer(serverPlayer, playerKitCommands);
	}

	private static void runCommandsAtPlayer(ServerPlayer serverPlayer, List<String> commands) {
		if (commands.isEmpty()) {
			return;
		}

		ServerLevel serverLevel = serverPlayer.serverLevel();
		MinecraftServer minecraftServer = serverLevel.getServer();
		CommandSourceStack commandSource = minecraftServer.createCommandSourceStack().withEntity(serverPlayer).withPosition(serverPlayer.position()).withRotation(serverPlayer.getRotationVector()).withLevel(serverLevel).withSuppressedOutput();

		for (String command : commands) {
			minecraftServer.getCommands().performPrefixedCommand(commandSource, command);
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
		List<String> kitNames = new ArrayList<>(entryMap.keySet());

		if (includeAll) {
			kitNames.add("_all");
		}

		Util.sortKitNames(kitNames);

		return kitNames;
	}

	public static List<String> getInactiveKitNames() {
		return getInactiveKitNames(false);
	}
	public static List<String> getInactiveKitNames(boolean includeAll) {
		List<String> inactiveKitNames = new ArrayList<>();

		File[] files = Util.configInactiveKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files) {
			String inactiveKitName = file.getName().replace(".txt", "");
			inactiveKitNames.add(inactiveKitName);
		}

		if (includeAll) {
			inactiveKitNames.add("_all");
		}

		Util.sortKitNames(inactiveKitNames);

		return inactiveKitNames;
	}

	public static int showKitInformation(Level level, CommandSourceStack targetSource, Player targetPlayer, String kitName) {
		String kitDescription = "";
		if (Variables.starterKitDescriptions.containsKey(kitName.toLowerCase())) {
			kitDescription = Variables.starterKitDescriptions.get(kitName.toLowerCase());

			if (kitDescription.contains("/starterkit/description")) {
				kitDescription = "N/A";
			}
		}

		MutableComponent nameComponent = Component.literal("Name: ").withStyle(ChatFormatting.DARK_GREEN).append(Util.getKitNameComponent(kitName).withStyle(ChatFormatting.GRAY));
		MutableComponent descriptionComponent = Component.literal("Description: ").withStyle(ChatFormatting.DARK_GREEN).append(Util.getCustomTextComponent(kitDescription).withStyle(ChatFormatting.GRAY));

		if (targetSource != null) {
			MessageFunctions.sendMessage(targetSource, nameComponent, true);
			MessageFunctions.sendMessage(targetSource, descriptionComponent);
		}
		if (targetPlayer != null) {
			MessageFunctions.sendMessage(targetPlayer, nameComponent, true);
			MessageFunctions.sendMessage(targetPlayer, descriptionComponent);
		}

		StringBuilder kitItems = new StringBuilder();
		if (Variables.starterGearEntries.containsKey(kitName)) {
			List<ItemStack> kitItemList = GearFunctions.getItemStackListFromGearString(Variables.starterGearEntries.get(kitName));
			kitItemList.addAll(CuriosTrinketsCompat.getModSlotItems(Variables.starterKitModSlotGear.getOrDefault(kitName, "")));

			for (ItemStack itemStack : kitItemList) {
				if (itemStack.isEmpty()) {
					continue;
				}

				if (!kitItems.toString().isEmpty()) {
					kitItems.append(", ");
				}

				String count = itemStack.getCount() + " ";
				if (count.equals("1 ")) {
					count = "";
				}

				kitItems.append(count).append(itemStack.getDisplayName().getString().replace("[", "").replace("]", "").toLowerCase());
			}
		}

		MutableComponent itemComponent = Component.literal("Items: ").withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(kitItems.toString()).withStyle(ChatFormatting.GRAY));

		if (targetSource != null) {
			MessageFunctions.sendMessage(targetSource, itemComponent);
		}
		if (targetPlayer != null) {
			MessageFunctions.sendMessage(targetPlayer, itemComponent);
		}
		return 1;
	}
}

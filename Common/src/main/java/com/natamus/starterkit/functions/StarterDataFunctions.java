package com.natamus.starterkit.functions;

import com.natamus.collective.functions.GearFunctions;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.starterkit.data.Constants;
import com.natamus.starterkit.data.Variables;
import com.natamus.starterkit.util.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class StarterDataFunctions {
	public static void init(MinecraftServer minecraftServer) {
		setupTrackingMap();
		initDataFolders(minecraftServer);

		try {
			readTrackingJsonFile(minecraftServer);
		}
		catch (Exception ex) {
			Constants.logger.warn(Constants.logPrefix + "Something went wrong while parsing the tracking json file.");
			writeTrackingMapToJsonFile(minecraftServer);
		}

		try {
			StarterDataFunctions.processExistingTrackingData(minecraftServer);
		}
		catch (NullPointerException ex) {
			Constants.logger.warn(Constants.logPrefix + "Unable to process existing tracking data.");
		}
	}

	private static void setupTrackingMap() {
		Variables.trackingMap.put("singleplayer", new HashMap<String, Boolean>());
		Variables.trackingMap.put("multiplayer", new HashMap<String, Boolean>());
	}

	public static void initConfigFolders() {
		if (!Util.rootConfigDir.isDirectory()) { Util.rootConfigDir.mkdirs(); }
		if (!Util.configKitDir.isDirectory()) { Util.configKitDir.mkdirs(); }
		if (!Util.configInactiveKitDir.isDirectory()) { Util.configInactiveKitDir.mkdirs(); }
		if (!Util.configDescriptionDir.isDirectory()) { Util.configDescriptionDir.mkdirs(); }

		if ((new File(Util.rootConfigPath + File.separator + "starterkit.txt")).isFile()) { // old file structure exists
			try {
				processRetroConfig();
			}
			catch (IOException ex) {
				Constants.logger.warn(Constants.logPrefix + "Unable to convert the old config format.");
			}
		}
	}
	private static void initDataFolders(MinecraftServer minecraftServer) {
		if (!Util.getRootDataDir(minecraftServer).isDirectory()) { Util.getRootDataDir(minecraftServer).mkdirs(); }
	}

	public static void readTrackingJsonFile(MinecraftServer minecraftServer) throws Exception {
		String trackingJsonFilePath = Util.getRootDataDir(minecraftServer) + File.separator + "tracking.json";
		File trackingJsonFile = new File(trackingJsonFilePath);

		if (!trackingJsonFile.isFile()) {
			writeTrackingMapToJsonFile(minecraftServer);
			return;
		}

		String rawJson = Files.readString(Paths.get(trackingJsonFilePath));

		Map<String, Map<String, Boolean>> jsonMap = Constants.gson.fromJson(rawJson, Constants.gsonTrackingMapType);
		for (String environment : jsonMap.keySet()) {
			HashMap<String, Boolean> trackingData = new HashMap<String, Boolean>();
			for (String key : jsonMap.get(environment).keySet()) {
				trackingData.put(key, jsonMap.get(environment).get(key));
			}
			Variables.trackingMap.put(environment, trackingData);
		}
	}

	public static void processExistingTrackingData(MinecraftServer minecraftServer) throws NullPointerException {
		String playerDataFolder = WorldFunctions.getWorldPath(minecraftServer) + File.separator + "playerdata";
		File playerDataDir = new File(playerDataFolder);

		boolean updatedTracking = false;

		File[] files = playerDataDir.listFiles((File pathname) -> pathname.getName().endsWith(".dat"));
		for (File f : files) {
			String fileName = f.getName();
			String rawUUID = fileName.replace(".dat", "");

			if (!Variables.trackingMap.containsKey(rawUUID)) {
				Variables.trackingMap.get("multiplayer").put(rawUUID, true);
				updatedTracking = true;
			}
		}

		if (updatedTracking) {
			writeTrackingMapToJsonFile(minecraftServer);
		}
	}

	public static void resetTrackingForPlayer(Player player) {
		Level level = player.level();
		if (level.isClientSide) {
			return;
		}

		MinecraftServer minecraftServer = level.getServer();
		if (!minecraftServer.isDedicatedServer()) {
			Variables.trackingMap.get("singleplayer").replaceAll((r, v) -> false);
		}

		String rawUUID = player.getStringUUID();
		Variables.trackingMap.get("multiplayer").put(rawUUID, false);

		writeTrackingMapToJsonFile(minecraftServer);
	}

	public static void resetTrackingMap(MinecraftServer minecraftServer) {
		Variables.trackingMap.get("singleplayer").replaceAll((r, v) -> false);
		Variables.trackingMap.get("multiplayer").replaceAll((r, v) -> false);

		writeTrackingMapToJsonFile(minecraftServer);
	}

	public static void writeTrackingMapToJsonFile(MinecraftServer minecraftServer) {
		String trackingJsonFilePath = Util.getRootDataDir(minecraftServer) + File.separator + "tracking.json";
		File trackingJsonFile = new File(trackingJsonFilePath);

		String rawJson = Constants.gsonPretty.toJson(Variables.trackingMap);

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(trackingJsonFilePath, StandardCharsets.UTF_8);
			writer.print(rawJson);
		}
		catch (IOException ex) {
			Constants.logger.warn(Constants.logPrefix + "Unable to write kit tracking map to json data file.");
		}

		if (writer != null) {
			writer.close();
		}
	}

	@SuppressWarnings("DuplicateExpressions")
	private static void processRetroConfig() throws IOException {
		Constants.logger.info(Constants.logPrefix + "Old file structure detected. Converting it automatically.");

		File[] files = Util.rootConfigDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files ) {
			String filePath = Util.rootConfigDir + File.separator + file.getName();
			String fileContent = Files.readString(Paths.get(filePath));
			if (!fileContent.contains("'effects'")) {
				fileContent += "'effects' : ''," + System.lineSeparator();
			}

			fileContent = GearFunctions.sortGearString(fileContent);

			PrintWriter fileWriter = new PrintWriter(filePath, StandardCharsets.UTF_8);
			fileWriter.print(fileContent.strip());
			fileWriter.close();
		}

		String activeKitSourcePath = Util.rootConfigDir.getAbsolutePath() + File.separator + "starterkit.txt";
		String activeKitDestinationPath = Util.configKitDir.getAbsolutePath() + File.separator + "Default.txt";

		String defaultGearString = StarterDefaultKitFunctions.getDefaultKitGearString();
		String activeKitGearString = Files.readString(Paths.get(activeKitSourcePath));

		if (!activeKitGearString.equals("") && !defaultGearString.equals(activeKitGearString)) { // custom starter kit
			activeKitDestinationPath = Util.configKitDir.getAbsolutePath() + File.separator + "starterkit.txt";
		}

		Files.move(Paths.get(activeKitSourcePath), Paths.get(activeKitDestinationPath), StandardCopyOption.REPLACE_EXISTING);

		files = Util.rootConfigDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files ) {
			String inactiveKitSourcePath = Util.rootConfigDir.getAbsolutePath() + File.separator + file.getName();
			String inactiveKitDestinationPath = Util.configInactiveKitDir.getAbsolutePath() + File.separator + file.getName();

			Files.move(Paths.get(inactiveKitSourcePath), Paths.get(inactiveKitDestinationPath), StandardCopyOption.REPLACE_EXISTING);
		}

		StarterDefaultKitFunctions.createDefaultKits(true);

		Constants.logger.info(Constants.logPrefix + "The old file structure has successfully been converted!");
	}
}

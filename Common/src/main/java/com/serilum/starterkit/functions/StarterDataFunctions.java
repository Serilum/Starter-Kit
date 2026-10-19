package com.serilum.starterkit.functions;

import com.natamus.collective.functions.GearFunctions;
import com.natamus.collective.functions.WorldFunctions;
import com.serilum.starterkit.data.Constants;
import com.serilum.starterkit.data.Variables;
import com.serilum.starterkit.util.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class StarterDataFunctions {
	public static void init(MinecraftServer minecraftServer) {
		setupTrackingMap();
		initDataFolders(minecraftServer);

		boolean isNewTrackingFile = !getTrackingJsonFile(minecraftServer).isFile();

		try {
			readTrackingJsonFile(minecraftServer);
		}
		catch (Exception ex) {
			Constants.logger.warn(Constants.logPrefix + "Something went wrong while parsing the tracking json file. A copy of it is kept as tracking.json.broken.");
			keepBrokenTrackingJsonFile(minecraftServer);
			writeTrackingMapToJsonFile(minecraftServer);
			isNewTrackingFile = true;
		}

		if (!isNewTrackingFile) {
			return;
		}

		try {
			StarterDataFunctions.processExistingTrackingData(minecraftServer);
		}
		catch (NullPointerException ex) {
			Constants.logger.warn(Constants.logPrefix + "Unable to process existing tracking data.");
		}
	}

	private static File getTrackingJsonFile(MinecraftServer minecraftServer) {
		return new File(Util.getRootDataDir(minecraftServer), "tracking.json");
	}

	private static void keepBrokenTrackingJsonFile(MinecraftServer minecraftServer) {
		Path trackingJsonPath = getTrackingJsonFile(minecraftServer).toPath();

		try {
			Files.copy(trackingJsonPath, trackingJsonPath.resolveSibling("tracking.json.broken"), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ignored) { }
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
		if (!Util.configCommandDir.isDirectory()) { Util.configCommandDir.mkdirs(); }

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
		File trackingJsonFile = getTrackingJsonFile(minecraftServer);

		if (!trackingJsonFile.isFile()) {
			writeTrackingMapToJsonFile(minecraftServer);
			return;
		}

		String rawJson = Files.readString(trackingJsonFile.toPath());

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
		File playerDataDir = minecraftServer.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();

		boolean updatedTracking = false;

		File[] files = playerDataDir.listFiles((File pathname) -> pathname.getName().endsWith(".dat"));
		for (File f : files) {
			String fileName = f.getName();
			String rawUUID = fileName.replace(".dat", "");

			if (!Variables.trackingMap.get("multiplayer").containsKey(rawUUID)) {
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
		if (StarterCheckFunctions.isSingleplayerOwner(minecraftServer, player)) {
			Variables.trackingMap.get("singleplayer").put(WorldFunctions.getWorldFolderName(minecraftServer), false);
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
		String rawJson = Constants.gsonPretty.toJson(Variables.trackingMap);

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(getTrackingJsonFile(minecraftServer), StandardCharsets.UTF_8);
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

		if (!activeKitGearString.equals("") && !GearFunctions.areTwoGearStringsEqual(activeKitGearString, defaultGearString)) { // custom starter kit
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

package com.natamus.starterkit.util;

import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.data.Variables;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public class Util {
	public static final String rootConfigPath = DataFunctions.getConfigDirectory() + File.separator + "starterkit";
	public static final String configKitPath = rootConfigPath + File.separator + "kits";
	public static final String configInactiveKitPath = configKitPath + File.separator + "inactive";
	public static final String configDescriptionPath = rootConfigPath + File.separator + "descriptions";
	public static String getRootDataPath(MinecraftServer minecraftServer) {
		return WorldFunctions.getWorldPath(minecraftServer) + File.separator + "data" + File.separator + "starterkit";
	}

	public static final File rootConfigDir = new File(rootConfigPath);
	public static final File configKitDir = new File(configKitPath);
	public static final File configInactiveKitDir = new File(configInactiveKitPath);
	public static final File configDescriptionDir = new File(configDescriptionPath);
	public static File getRootDataDir(MinecraftServer minecraftServer) {
		return new File(getRootDataPath(minecraftServer));
	}

	public static String formatKitName(String rawKitName) {
		if (!ConfigHandler.formatKitNames) {
			return rawKitName;
		}

		String kitName = rawKitName.replace("_", " ");
		if (ConfigHandler.formatKitNames) {
			kitName = StringFunctions.capitalizeEveryWord(kitName);
		}

		return kitName;
	}
	public static String simplifyKitName(String rawKitName) {
		return rawKitName.replace(" ", "_").strip();
	}

	public static String findCorrectKitNameFromInput(String rawKitName) {
		rawKitName = simplifyKitName(rawKitName);

		for (String kitName : Variables.starterGearEntries.keySet()) {
			if (kitName.equalsIgnoreCase(rawKitName)) {
				return kitName;
			}
		}

		File[] files = Util.configInactiveKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files) {
			String fileName = file.getName();
			String kitName = fileName.replace(".txt", "");

			if (kitName.equalsIgnoreCase(rawKitName)) {
				return kitName;
			}
		}

		return rawKitName;
	}

	public static String removeLastChar(String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		return s.substring(0, s.length()-1);
	}

	private boolean listHaveEqualObjects(List<?> listA, List<?> listB) {
		return new HashSet<>(listA).equals(new HashSet<>(listB));
	}
}
package com.serilum.starterkit.util;

import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.collective.functions.WorldFunctions;
import com.serilum.starterkit.config.ConfigHandler;
import com.serilum.starterkit.data.Variables;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	public static final String rootConfigPath = DataFunctions.getConfigDirectory() + File.separator + "starterkit";
	public static final String configKitPath = rootConfigPath + File.separator + "kits";
	public static final String configInactiveKitPath = configKitPath + File.separator + "inactive";
	public static final String configDescriptionPath = rootConfigPath + File.separator + "descriptions";
	public static final String configCommandPath = rootConfigPath + File.separator + "commands";
	public static String getRootDataPath(MinecraftServer minecraftServer) {
		return WorldFunctions.getWorldPath(minecraftServer) + File.separator + "data" + File.separator + "starterkit";
	}

	public static final File rootConfigDir = new File(rootConfigPath);
	public static final File configKitDir = new File(configKitPath);
	public static final File configInactiveKitDir = new File(configInactiveKitPath);
	public static final File configDescriptionDir = new File(configDescriptionPath);
	public static final File configCommandDir = new File(configCommandPath);

	private static final Pattern kitOrderPattern = Pattern.compile("^(\\d{1,6})_.+");
	public static File getRootDataDir(MinecraftServer minecraftServer) {
		return new File(getRootDataPath(minecraftServer));
	}

	public static String formatKitName(String rawKitName) {
		if (!ConfigHandler.formatKitNames) {
			return rawKitName;
		}

		String kitName = removeKitOrderPrefix(rawKitName).replace("_", " ");
		if (ConfigHandler.formatKitNames) {
			kitName = StringFunctions.capitalizeEveryWord(kitName);
		}

		return kitName;
	}
	public static int getKitOrderNumber(String rawKitName) {
		Matcher orderMatcher = kitOrderPattern.matcher(rawKitName);
		if (!orderMatcher.matches()) {
			return Integer.MAX_VALUE;
		}

		return Integer.parseInt(orderMatcher.group(1));
	}
	public static String removeKitOrderPrefix(String rawKitName) {
		if (getKitOrderNumber(rawKitName) == Integer.MAX_VALUE) {
			return rawKitName;
		}

		return rawKitName.substring(rawKitName.indexOf("_") + 1);
	}
	public static void sortKitNames(List<String> kitNames) {
		kitNames.sort(Comparator.comparingInt(Util::getKitOrderNumber).thenComparing(Comparator.naturalOrder()));
	}
	public static MutableComponent getKitNameComponent(String rawKitName) {
		return Component.translatableWithFallback(rawKitName, formatKitName(rawKitName));
	}
	public static MutableComponent getCustomTextComponent(String text, Object... args) {
		String fallbackText = text;
		for (Object arg : args) {
			fallbackText = fallbackText.replace("%s", String.valueOf(arg));
		}

		return Component.translatableWithFallback(text.strip(), fallbackText.replace("%", "%%"), args);
	}
	public static String simplifyKitName(String rawKitName) {
		return rawKitName.replace(" ", "_").replace("/", "_").replace("\\", "_").replace("..", "_").strip();
	}

	public static String findCorrectKitNameFromInput(String rawKitName) {
		rawKitName = simplifyKitName(rawKitName);

		for (String kitName : Variables.starterGearEntries.keySet()) {
			if (kitName.equalsIgnoreCase(rawKitName)) {
				return kitName;
			}
		}

		for (String kitName : Variables.starterGearEntries.keySet()) {
			if (removeKitOrderPrefix(kitName).equalsIgnoreCase(rawKitName)) {
				return kitName;
			}
		}

		File[] files = Util.configInactiveKitDir.listFiles((File pathname) -> pathname.getName().endsWith(".txt"));
		for (File file : files) {
			String fileName = file.getName();
			String kitName = fileName.replace(".txt", "");

			if (kitName.equalsIgnoreCase(rawKitName) || removeKitOrderPrefix(kitName).equalsIgnoreCase(rawKitName)) {
				return kitName;
			}
		}

		return rawKitName;
	}

	public static String removeLastChar(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		return s.substring(0, s.length()-1);
	}

	private boolean listHaveEqualObjects(List<?> listA, List<?> listB) {
		return new HashSet<>(listA).equals(new HashSet<>(listB));
	}
}
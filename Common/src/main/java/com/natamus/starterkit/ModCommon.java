package com.natamus.starterkit;

import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.util.Util;

public class ModCommon {

	public static void init() {
		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		try {
			Util.getOrCreateGearConfig(true);
		} catch (Exception ignored) {
			System.out.println("[Starter Kit] Unable to get or create the gear config file.");
		}
	}
}
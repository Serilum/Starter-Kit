package com.natamus.starterkit;

import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.functions.StarterDataFunctions;
import com.natamus.starterkit.functions.StarterGearFunctions;
import com.natamus.starterkit.networking.PacketRegistration;

public class ModCommon {

	public static void init() {
		ConfigHandler.initConfig();

		registerPackets();

		load();
	}

	private static void load() {
		StarterDataFunctions.initConfigFolders();
		StarterGearFunctions.processKitFiles();
	}

	public static void registerPackets() {
		new PacketRegistration().init();
	}
}
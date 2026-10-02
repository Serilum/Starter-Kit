package com.serilum.starterkit;

import com.serilum.starterkit.config.ConfigHandler;
import com.serilum.starterkit.functions.StarterDataFunctions;
import com.serilum.starterkit.functions.StarterGearFunctions;
import com.serilum.starterkit.networking.PacketRegistration;

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
package com.natamus.starterkit.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Variables {
	public static HashMap<String, String> starterGearEntries = new HashMap<>();
	public static HashMap<String, String> starterKitDescriptions = new HashMap<>();

	public static HashMap<String, HashMap<String, Boolean>> trackingMap = new HashMap<>();
	public static List<UUID> playersWithModInstalledOnClient = new ArrayList<>();
}

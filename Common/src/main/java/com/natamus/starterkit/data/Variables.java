package com.natamus.starterkit.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Variables {
	public static HashMap<String, String> starterGearEntries = new HashMap<String, String>();
	public static HashMap<String, String> starterKitDescriptions = new HashMap<String, String>();

	public static HashMap<String, HashMap<String, Boolean>> trackingMap = new HashMap<String, HashMap<String, Boolean>>();
	public static List<UUID> playersWithModInstalledOnClient = new ArrayList<UUID>();
}

package com.natamus.starterkit.functions;

import com.natamus.collective.functions.WorldFunctions;
import com.natamus.starterkit.data.Variables;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StarterCheckFunctions {
	public static boolean shouldPlayerReceiveStarterKit(Player player) {
		return shouldPlayerReceiveStarterKit(player.level, player);
	}
	public static boolean shouldPlayerReceiveStarterKit(Level level, Player player) {
		if (level.isClientSide) {
			return false;
		}

		MinecraftServer minecraftServer = level.getServer();
		if (!minecraftServer.isDedicatedServer()) {
			String levelName = WorldFunctions.getWorldFolderName(minecraftServer);
			if (Variables.trackingMap.get("singleplayer").containsKey(levelName)) {
				return !Variables.trackingMap.get("singleplayer").get(levelName);
			}
		}
		else {
			String rawUUID = player.getStringUUID();
			if (Variables.trackingMap.get("multiplayer").containsKey(rawUUID)) {
				return !Variables.trackingMap.get("multiplayer").get(rawUUID);
			}
		}

		return true;
	}

	public static void addPlayerToTrackingMap(Player player) {
		addPlayerToTrackingMap(player.level, player);
	}
	public static void addPlayerToTrackingMap(Level level, Player player) {
		if (level.isClientSide) {
			return;
		}

		MinecraftServer minecraftServer = level.getServer();
		if (!minecraftServer.isDedicatedServer()) {
			String levelName = WorldFunctions.getWorldFolderName(minecraftServer);

			Variables.trackingMap.get("singleplayer").put(levelName, true);
		}
		else {
			String rawUUID = player.getStringUUID();

			Variables.trackingMap.get("multiplayer").put(rawUUID, true);
		}

		StarterDataFunctions.writeTrackingMapToJsonFile(minecraftServer);
	}
}

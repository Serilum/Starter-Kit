package com.serilum.starterkit.functions;

import com.natamus.collective.functions.WorldFunctions;
import com.serilum.starterkit.config.ConfigHandler;
import com.serilum.starterkit.data.Variables;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StarterCheckFunctions {
	public static boolean shouldPlayerReceiveStarterKit(Player player) {
		return shouldPlayerReceiveStarterKit(player.level(), player);
	}
	public static boolean shouldPlayerReceiveStarterKit(Level level, Player player) {
		if (level.isClientSide()) {
			return false;
		}

		Boolean receivedStarterKit = getTrackedReceivedValue(level.getServer(), player);
		if (receivedStarterKit == null) {
			return ConfigHandler.giveStarterKitOnFirstJoin;
		}

		return !receivedStarterKit;
	}

	public static boolean hasReceivedStarterKit(Player player) {
		Level level = player.level();
		if (level.isClientSide()) {
			return false;
		}

		return Boolean.TRUE.equals(getTrackedReceivedValue(level.getServer(), player));
	}

	private static Boolean getTrackedReceivedValue(MinecraftServer minecraftServer, Player player) {
		if (isSingleplayerOwner(minecraftServer, player)) {
			return Variables.trackingMap.get("singleplayer").get(WorldFunctions.getWorldFolderName(minecraftServer));
		}

		return Variables.trackingMap.get("multiplayer").get(player.getStringUUID());
	}

	public static boolean isSingleplayerOwner(MinecraftServer minecraftServer, Player player) {
		if (minecraftServer.isDedicatedServer()) {
			return false;
		}

		return minecraftServer.isSingleplayerOwner(player.getGameProfile());
	}

	public static void addPlayerToTrackingMap(Player player) {
		addPlayerToTrackingMap(player.level(), player);
	}
	public static void addPlayerToTrackingMap(Level level, Player player) {
		if (level.isClientSide()) {
			return;
		}

		MinecraftServer minecraftServer = level.getServer();
		String rawUUID = player.getStringUUID();
		Variables.trackingMap.get("multiplayer").put(rawUUID, true);

		if (isSingleplayerOwner(minecraftServer, player)) {
			String levelName = WorldFunctions.getWorldFolderName(minecraftServer);
			Variables.trackingMap.get("singleplayer").put(levelName, true);
		}

		StarterDataFunctions.writeTrackingMapToJsonFile(minecraftServer);
	}
}
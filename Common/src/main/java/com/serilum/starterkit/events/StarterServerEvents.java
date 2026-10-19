package com.serilum.starterkit.events;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.natamus.collective.functions.TaskFunctions;
import com.serilum.starterkit.config.ConfigHandler;
import com.serilum.starterkit.data.Variables;
import com.serilum.starterkit.functions.StarterCheckFunctions;
import com.serilum.starterkit.functions.StarterDataFunctions;
import com.serilum.starterkit.functions.StarterGearFunctions;
import com.serilum.starterkit.util.Reference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

public class StarterServerEvents {
	public static void onServerStarting(MinecraftServer minecraftServer) {
		StarterDataFunctions.init(minecraftServer);
	}

	public static void onSpawn(Level level, Entity entity) {
		if (level.isClientSide()) {
			return;
		}
		
		if (!(entity instanceof Player player)) {
			return;
		}

		if (player.entityTags().contains("collective.firstJoin." + Reference.MOD_ID)) { // Version < 5.5 backwards compatibility
			return;
		}

		if (!ConfigHandler.giveStarterKitOnFirstJoin) {
			return;
		}

		StarterGearFunctions.initStarterKitHandle(level, player, null);
	}

	public static void onRespawn(ServerPlayer serverPlayer, boolean returnedFromEnd) {
		if (!ConfigHandler.giveStarterKitOnRespawn || returnedFromEnd) {
			return;
		}

		ServerLevel serverLevel = serverPlayer.level();
		if (serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}

		if (ConfigHandler.giveStarterKitOnFirstJoin && StarterCheckFunctions.shouldPlayerReceiveStarterKit(serverPlayer)) {
			return;
		}

		String kitName = StarterGearFunctions.getLastReceivedKitName(serverPlayer);
		if (Variables.starterGearEntries.containsKey(kitName)) {
			TaskFunctions.enqueueCollectiveServerTask(serverLevel.getServer(), () -> {
				StarterGearFunctions.giveStarterKit(serverPlayer, null, kitName);
			}, 10);
			return;
		}

		if (!StarterCheckFunctions.hasReceivedStarterKit(serverPlayer)) {
			return;
		}

		StarterDataFunctions.resetTrackingForPlayer(serverPlayer);
		if (!ConfigHandler.giveStarterKitOnFirstJoin) {
			StarterGearFunctions.initStarterKitHandle(serverLevel, serverPlayer, null);
		}
	}

	public static void onCommand(String string, ParseResults<CommandSourceStack> parse) {
		if (!ConfigHandler.enableFTBIslandCreateCompatibility) {
			return;
		}
		
		CommandContextBuilder<CommandSourceStack> context = parse.getContext();
		Command<CommandSourceStack> command = context.getCommand();
		if (command == null) {
			return;
		}
		
		String cmdstr = command.toString().toLowerCase();
		if (cmdstr.contains("ftbteamislands.commands.createislandcommand")) {
			CommandSourceStack source = context.getSource();
			
			Entity sourceentity = source.getEntity();
			if (sourceentity instanceof Player player) {
				Level level = player.level();

				if (!level.isClientSide()) {
					StarterGearFunctions.initStarterKitHandle(level, player, null);
				}
			}
		}
	}
}

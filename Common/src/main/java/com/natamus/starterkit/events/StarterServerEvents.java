package com.natamus.starterkit.events;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.functions.StarterDataFunctions;
import com.natamus.starterkit.functions.StarterGearFunctions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StarterServerEvents {
	public static void onServerStarting(MinecraftServer minecraftServer) {
		StarterDataFunctions.init(minecraftServer);
	}

	public static void onSpawn(Level level, Entity entity) {
		if (level.isClientSide) {
			return;
		}
		
		if (!(entity instanceof Player)) {
			return;
		}

		StarterGearFunctions.initStarterKitHandle(level, (Player)entity, null);
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
			if (sourceentity instanceof Player) {
				Player player = (Player)sourceentity;
				Level level = player.level();

				if (!level.isClientSide) {
					StarterGearFunctions.initStarterKitHandle(level, player, null);
				}
			}
		}
	}
}

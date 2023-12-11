package com.natamus.starterkit.events;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.natamus.collective.functions.PlayerFunctions;
import com.natamus.collective.functions.TaskFunctions;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.util.Reference;
import com.natamus.starterkit.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FirstSpawnEvent {
	public static void onSpawn(Level world, Entity entity) {
		if (world.isClientSide) {
			return;
		}
		
		if (!(entity instanceof Player)) {
			return;
		}

		final Player player = (Player)entity;
		TaskFunctions.enqueueCollectiveTask(world.getServer(), () -> {
			if (PlayerFunctions.isJoiningWorldForTheFirstTime(player, Reference.MOD_ID, false)) {
				Util.setStarterKit(player);
			}
		}, 5);
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
					TaskFunctions.enqueueCollectiveTask(level.getServer(), () -> {
						Util.setStarterKit(player);
					}, 40);
				}
			}
		}
	}
}

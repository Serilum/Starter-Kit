package com.natamus.starterkit.cmds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.natamus.collective.functions.MessageFunctions;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.data.Constants;
import com.natamus.starterkit.data.Variables;
import com.natamus.starterkit.functions.StarterCheckFunctions;
import com.natamus.starterkit.functions.StarterCommandFunctions;
import com.natamus.starterkit.functions.StarterDataFunctions;
import com.natamus.starterkit.functions.StarterGearFunctions;
import com.natamus.starterkit.util.Reference;
import com.natamus.starterkit.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class CommandStarterkit {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		for (String commandPrefix : Constants.commandPrefixes) {
			dispatcher.register(Commands.literal(commandPrefix)
				.executes((command) -> {
					return showCommandHelp(command.getSource());
				})
				.then(Commands.literal("help")
				.executes((command) -> {
					return showCommandHelp(command.getSource());
				}))

				.then(Commands.literal("info")
				.then(Commands.argument("kit_name", StringArgumentType.string()).suggests(StarterCommandFunctions.activeKitSuggestions)
				.executes((command) -> {
					CommandSourceStack source = command.getSource();

					String kitName = Util.findCorrectKitNameFromInput(StringArgumentType.getString(command, "kit_name"));

					return StarterGearFunctions.showKitInformation(source.getLevel(), source, null, kitName);
				})))
				.then(Commands.literal("info")
				.then(Commands.argument("kit_name", StringArgumentType.string()).suggests(StarterCommandFunctions.activeKitSuggestions)
				.then(Commands.argument("target", EntityArgument.player())
				.executes((command) -> {
					Player targetPlayer = EntityArgument.getPlayer(command, "target");
					String kitName = Util.findCorrectKitNameFromInput(StringArgumentType.getString(command, "kit_name"));

					return StarterGearFunctions.showKitInformation(targetPlayer.level(), null, targetPlayer, kitName);
				}))))

				.then(Commands.literal("choose")
				.then(Commands.argument("kit_name", StringArgumentType.string()).suggests(StarterCommandFunctions.activeKitSuggestions)
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!source.isPlayer()) {
						MessageFunctions.sendMessage(source, "This command can only be ran as a player.", ChatFormatting.RED);
						return 0;
					}

					Player player = source.getPlayer();

					if (!StarterCheckFunctions.shouldPlayerReceiveStarterKit(player)) {
						MessageFunctions.sendMessage(source, "You are not eligible for a starter kit.", ChatFormatting.RED);
						return 0;
					}

					String kitName = Util.findCorrectKitNameFromInput(StringArgumentType.getString(command, "kit_name"));
					if (!Variables.starterGearEntries.containsKey(kitName)) {
						MessageFunctions.sendMessage(source, "The starter kit '" + kitName + "' does not exist.", ChatFormatting.RED);
						return 0;
					}

					String actualKitName = StarterGearFunctions.giveStarterKit(player, null, kitName);
					if (actualKitName == null) {
						MessageFunctions.sendMessage(source, "Something went wrong while choosing your starter kit.", ChatFormatting.RED);
						return 0;
					}

					MessageFunctions.sendMessage(player, "You have been given the '" + Util.formatKitName(actualKitName) + "' starter kit.", ChatFormatting.DARK_GREEN, true);
					return 1;
				})))

				.then(Commands.literal("add")
				.executes((command) -> {
					return processCommand(command.getSource(), "", true);
				}))
				.then(Commands.literal("add")
				.then(Commands.argument("kit_name", StringArgumentType.string())
				.executes((command) -> {
					return processCommand(command.getSource(), StringArgumentType.getString(command, "kit_name"), true);
				})))

				.then(Commands.literal("set")
				.executes((command) -> {
					return processCommand(command.getSource(), "", false);
				}))
				.then(Commands.literal("set")
				.then(Commands.argument("kit_name", StringArgumentType.string())
				.executes((command) -> {
					return processCommand(command.getSource(), StringArgumentType.getString(command, "kit_name"), false);
				})))

				.then(Commands.literal("deactivate")
				.then(Commands.argument("kit_name", StringArgumentType.string()).suggests(StarterCommandFunctions.activeKitSuggestionsWithAll)
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					String kitNameInput = StringArgumentType.getString(command, "kit_name");
					if (kitNameInput.equalsIgnoreCase("_all")) {
						StarterGearFunctions.moveAllKitsToInactive();
						MessageFunctions.sendMessage(source, "Moved all kits to the inactive folder.", ChatFormatting.DARK_GREEN, true);
						return 1;
					}

					String kitName = Util.findCorrectKitNameFromInput(kitNameInput);

					if (!StarterGearFunctions.moveKitToInactive(kitName)) {
						MessageFunctions.sendMessage(source, "Unable to move the '" + kitName + "' kit to the inactive folder.", ChatFormatting.RED, true);
						return 0;
					}

					MessageFunctions.sendMessage(source, "The kit '" + kitName + "' has been moved to the inactive folder.", ChatFormatting.DARK_GREEN, true);
					return 1;
				})))

				.then(Commands.literal("activate")
				.then(Commands.argument("kit_name", StringArgumentType.string()).suggests(StarterCommandFunctions.inactiveKitSuggestionsWithAll)
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					String kitNameInput = StringArgumentType.getString(command, "kit_name");
					if (kitNameInput.equalsIgnoreCase("_all")) {
						StarterGearFunctions.moveAllKitsToActive();
						MessageFunctions.sendMessage(source, "Moved all kits to the active folder.", ChatFormatting.DARK_GREEN, true);
						return 1;
					}

					String kitName = Util.findCorrectKitNameFromInput(kitNameInput);

					if (!StarterGearFunctions.moveKitToActive(kitName)) {
						MessageFunctions.sendMessage(source, "Unable to move the '" + kitName + "' kit to the active folder.", ChatFormatting.RED, true);
						return 0;
					}

					MessageFunctions.sendMessage(source, "The kit '" + kitName + "' has been moved to the active folder.", ChatFormatting.DARK_GREEN, true);
					return 1;
				})))

				.then(Commands.literal("give")
				.then(Commands.argument("target", EntityArgument.player())
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					Level level = source.getLevel();
					if (level.isClientSide) {
						return 1;
					}

					Player targetPlayer = EntityArgument.getPlayer(command, "target");

					StarterDataFunctions.resetTrackingForPlayer(targetPlayer);
					StarterGearFunctions.initStarterKitHandle(level, targetPlayer, source);

					if (ConfigHandler.randomizeMultipleKitsToggle && Variables.starterGearEntries.size() > 1) {
						MessageFunctions.sendMessage(source, targetPlayer.getName().getString() + " has been given the choice for a new starter kit!", ChatFormatting.DARK_GREEN, true);
					}
					return 1;
				})))
				.then(Commands.literal("give")
				.then(Commands.argument("target", EntityArgument.player())
				.then(Commands.argument("kit_name", StringArgumentType.string()).suggests(StarterCommandFunctions.activeKitSuggestions)
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					Level level = source.getLevel();
					if (level.isClientSide) {
						return 1;
					}

					Player targetPlayer = EntityArgument.getPlayer(command, "target");
					String kitName = Util.findCorrectKitNameFromInput(StringArgumentType.getString(command, "kit_name"));

					StarterDataFunctions.resetTrackingForPlayer(targetPlayer);
					StarterGearFunctions.initStarterKitHandle(level, targetPlayer, source, kitName);

					if (ConfigHandler.randomizeMultipleKitsToggle && Variables.starterGearEntries.size() > 1) {
						MessageFunctions.sendMessage(source, targetPlayer.getName().getString() + " has been given the choice for a new starter kit!", ChatFormatting.DARK_GREEN, true);
					}
					return 1;
				}))))

				.then(Commands.literal("list")
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					MessageFunctions.sendMessage(source, Component.literal("The current ").withStyle(ChatFormatting.DARK_GREEN).append(Component.literal("active").withStyle(ChatFormatting.GOLD).append(Component.literal(" starter kits are:").withStyle(ChatFormatting.DARK_GREEN))), true);

					List<String> activeKitNames = StarterGearFunctions.getActiveKitNames();
					int activeKitcount = activeKitNames.size();
					if (activeKitcount > 0) {
						for (String kitName : activeKitNames) {
							MessageFunctions.sendMessage(source, " " + kitName, ChatFormatting.GRAY);
						}
					}
					else {
						MessageFunctions.sendMessage(source, " N/A", ChatFormatting.RED);
					}

					if (ConfigHandler.randomizeMultipleKitsToggle) {
						MessageFunctions.sendMessage(source, "One kit is given randomly on join.", ChatFormatting.DARK_GREEN, true);
					}
					else {
						MessageFunctions.sendMessage(source, "Players can choose one of the kits on join.", ChatFormatting.DARK_GREEN, true);
						if (activeKitcount < 2) {
							if (activeKitcount == 0) {
								MessageFunctions.sendMessage(source, " > If 2 more kits are added.", ChatFormatting.DARK_GRAY);
							}
							else {
								MessageFunctions.sendMessage(source, " > If 1 more kit is added. For now all players will receive the '" + activeKitNames.getFirst() + "' kit without a choice screen.", ChatFormatting.DARK_GRAY);
							}
						}
					}

					return 1;
				}))

				.then(Commands.literal("list")
				.then(Commands.literal("inactive")
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					MessageFunctions.sendMessage(source, Component.literal("The current ").withStyle(ChatFormatting.DARK_GREEN).append(Component.literal("inactive").withStyle(ChatFormatting.RED).append(Component.literal(" starter kits are:").withStyle(ChatFormatting.DARK_GREEN))), true);

					List<String> inactiveKitNames = StarterGearFunctions.getInactiveKitNames();
					if (inactiveKitNames.size() > 0) {
						for (String kitName : inactiveKitNames) {
							MessageFunctions.sendMessage(source, " " + kitName, ChatFormatting.GRAY);
						}
					}
					else {
						MessageFunctions.sendMessage(source, " N/A", ChatFormatting.RED);
					}

					return 1;
				})))

				.then(Commands.literal("reload")
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					StarterGearFunctions.processKitFiles();

					MessageFunctions.sendMessage(command.getSource(), "All active kits have been reloaded.", ChatFormatting.DARK_GREEN, true);
					sendKitCount(source, false);
					return 1;
				}))

				.then(Commands.literal("reset")
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					Level level = source.getLevel();
					if (level.isClientSide) {
						return 0;
					}

					StarterDataFunctions.resetTrackingMap(level.getServer());

					MessageFunctions.sendMessage(source, "Starter Kit tracking data has been reset. All players will now again receive a kit on join.", ChatFormatting.DARK_GREEN, true);
					return 1;
				}))
			);
		}
	}

	private static boolean permissionCheck(CommandSourceStack source) {
		if (!source.hasPermission(2)) {
			MessageFunctions.sendMessage(source, "You do not have permission to use that command.", ChatFormatting.RED);
			return false;
		}
		return true;
	}

	private static int showCommandHelp(CommandSourceStack source) {
		if (source.hasPermission(2)) {
			MessageFunctions.sendMessage(source, Component.literal(Reference.NAME + " Admin Usage:").withStyle(ChatFormatting.GOLD), true);
			MessageFunctions.sendMessage(source, " /sk add (kit_name)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Adds your current inventory to the active kits.", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk set (kit_name)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Sets your current inventory as the only active kit.", ChatFormatting.GRAY);

			MessageFunctions.sendMessage(source, " /sk give <player> (kit_name)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Give a player a random (or specific) starter kit.", ChatFormatting.GRAY);

			MessageFunctions.sendMessage(source, " /sk activate <kit_name>/_all", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Moves kit_name or all kits from inactive to active.", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk deactivate <kit_name>/_all", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Moves kit_name or all kits from active to inactive.", ChatFormatting.GRAY);

			MessageFunctions.sendMessage(source, " /sk list (inactive)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Lists all active (or inactive) kits.", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk reset", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Resets all tracking. Players receive another kit on join.", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk reload", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Reloads starter kit changes from the config folder.", ChatFormatting.GRAY);
		}

		MessageFunctions.sendMessage(source, Component.literal(Reference.NAME + " User Usage:").withStyle(ChatFormatting.GOLD), true);
		MessageFunctions.sendMessage(source, " /sk choose <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendMessage(source, "     Allows players to choose a starter kit via commands. This is only needed when Starter Kit is not installed on the client and the server enabled kit choices.", ChatFormatting.GRAY);
		MessageFunctions.sendMessage(source, " /sk info <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendMessage(source, "     Shows the description and items of kit_name.", ChatFormatting.GRAY);
		return 1;
	}

	private static int processCommand(CommandSourceStack source, String kitName, boolean adding) {
		if (!permissionCheck(source)) { return 0; }

		if (!source.isPlayer()) {
			MessageFunctions.sendMessage(source, "This command can only be ran as a player.", ChatFormatting.RED);
			return 0;
		}

		Player player = source.getPlayer();

		kitName = Util.findCorrectKitNameFromInput(kitName);

		String wording = "adding";
		String worded = "added";
		String cmd = "add";
		if (!adding) {
			wording = "setting";
			worded = "set";
			cmd = "set";
		}

		String actualKitName = StarterGearFunctions.createStarterKitFile(player, kitName, adding);

		if (actualKitName.equals("")) {
			MessageFunctions.sendMessage(source, "Something went wrong while " + wording + " a new starter kit.", ChatFormatting.RED);
			return 0;
		}

		MessageFunctions.sendMessage(source, "The starter kit '" + Util.formatKitName(actualKitName) + "' was " + worded + "!", ChatFormatting.DARK_GREEN, true);

		if (!kitName.equals(actualKitName)) {
			MessageFunctions.sendMessage(source, " If you'd like to specify the kit name, use:", ChatFormatting.GRAY, true);
			MessageFunctions.sendMessage(source, "     /sk " + cmd + " <kit_name>", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " ", ChatFormatting.GRAY);
		}

		sendKitCount(source, true);
		return 1;
	}

	private static void sendKitCount(CommandSourceStack source, boolean now) {
		int kitCount = Variables.starterGearEntries.size();

		String isare = "are";
		String s = "s";
		if (kitCount == 1) {
			isare = "is";
			s = "";
		}

		String nowword = "now ";
		if (!now) {
			nowword = "";
		}

		MessageFunctions.sendMessage(source, " There " + isare + " " + nowword + kitCount + " starter kit" + s + " available.", ChatFormatting.DARK_GRAY);
	}
}

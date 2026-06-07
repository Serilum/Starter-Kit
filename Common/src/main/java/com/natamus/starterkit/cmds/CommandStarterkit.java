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
						MessageFunctions.sendTranslatableMessage(source, "collective.shared.message.playeronly", ChatFormatting.RED);
						return 0;
					}

					Player player = source.getPlayer();

					if (!StarterCheckFunctions.shouldPlayerReceiveStarterKit(player)) {
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.eligiblestarterkit", ChatFormatting.RED);
						return 0;
					}

					String kitName = Util.findCorrectKitNameFromInput(StringArgumentType.getString(command, "kit_name"));
					if (!Variables.starterGearEntries.containsKey(kitName)) {
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.starterkitexist", ChatFormatting.RED, kitName);
						return 0;
					}

					String actualKitName = StarterGearFunctions.giveStarterKit(player, null, kitName);
					if (actualKitName == null) {
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.somethingwentwrongwhilechoosing", ChatFormatting.RED);
						return 0;
					}

					MessageFunctions.sendTranslatableMessage(player, "collective.starterkit.message.receivedstarterkit", true, ChatFormatting.DARK_GREEN, Util.formatKitName(actualKitName));
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
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.movedkitsinactive", true, ChatFormatting.DARK_GREEN);
						return 1;
					}

					String kitName = Util.findCorrectKitNameFromInput(kitNameInput);

					if (!StarterGearFunctions.moveKitToInactive(kitName)) {
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.unablemovekitinactive", true, ChatFormatting.RED, kitName);
						return 0;
					}

					MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.kitmovedinactive", true, ChatFormatting.DARK_GREEN, kitName);
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
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.movedkitsactive", true, ChatFormatting.DARK_GREEN);
						return 1;
					}

					String kitName = Util.findCorrectKitNameFromInput(kitNameInput);

					if (!StarterGearFunctions.moveKitToActive(kitName)) {
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.unablemovekitactive", true, ChatFormatting.RED, kitName);
						return 0;
					}

					MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.kitmovedactive", true, ChatFormatting.DARK_GREEN, kitName);
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
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.givenchoicestarter", true, ChatFormatting.DARK_GREEN, targetPlayer.getName().getString());
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
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.givenchoicestarter", true, ChatFormatting.DARK_GREEN, targetPlayer.getName().getString());
					}
					return 1;
				}))))

				.then(Commands.literal("list")
				.executes((command) -> {
					CommandSourceStack source = command.getSource();
					if (!permissionCheck(source)) { return 0; }

					MessageFunctions.sendMessage(source, Component.translatable("collective.starterkit.message.currentactivestarter").withStyle(ChatFormatting.DARK_GREEN), true);

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
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.kitgivenrandomly", true, ChatFormatting.DARK_GREEN);
					}
					else {
						MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.playerschoosekits", true, ChatFormatting.DARK_GREEN);
						if (activeKitcount < 2) {
							if (activeKitcount == 0) {
								MessageFunctions.sendTranslatableMessage(source, " ", "collective.starterkit.message.if2more", ChatFormatting.DARK_GRAY);
							}
							else {
								MessageFunctions.sendTranslatableMessage(source, " ", "collective.starterkit.message.if1more", ChatFormatting.DARK_GRAY, activeKitNames.getFirst());
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

					MessageFunctions.sendMessage(source, Component.translatable("collective.starterkit.message.currentinactivestarter").withStyle(ChatFormatting.DARK_GREEN), true);

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

					MessageFunctions.sendTranslatableMessage(command.getSource(), "collective.starterkit.message.activekitsreloaded", true, ChatFormatting.DARK_GREEN);
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

					MessageFunctions.sendTranslatableMessage(source, "collective.starterkit.message.trackingdatareset", true, ChatFormatting.DARK_GREEN, Reference.NAME);
					return 1;
				}))
			);
		}
	}

	private static boolean permissionCheck(CommandSourceStack source) {
		if (!source.hasPermission(2)) {
			MessageFunctions.sendTranslatableMessage(source, "collective.shared.message.nopermission", ChatFormatting.RED);
			return false;
		}
		return true;
	}

	private static int showCommandHelp(CommandSourceStack source) {
		if (source.hasPermission(2)) {
			MessageFunctions.sendMessage(source, Component.translatable("collective.shared.message.adminusage", Reference.NAME).withStyle(ChatFormatting.GOLD), true);
			MessageFunctions.sendMessage(source, " /sk add (kit_name)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.addscurrentinventory", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk set (kit_name)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.setscurrentinventory", ChatFormatting.GRAY);

			MessageFunctions.sendMessage(source, " /sk give <player> (kit_name)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.giveplayerrandom", ChatFormatting.GRAY);

			MessageFunctions.sendMessage(source, " /sk activate <kit_name>/_all", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.moveskitnamekitsfrom", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk deactivate <kit_name>/_all", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.moveskitnamekits", ChatFormatting.GRAY);

			MessageFunctions.sendMessage(source, " /sk list (inactive)", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.listsactiveinactive", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk reset", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.resetstrackingplayers", ChatFormatting.GRAY);
			MessageFunctions.sendMessage(source, " /sk reload", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.reloadsstarterkit", ChatFormatting.GRAY);
		}

		MessageFunctions.sendMessage(source, Component.translatable("collective.starterkit.message.userusage", Reference.NAME).withStyle(ChatFormatting.GOLD), true);
		MessageFunctions.sendMessage(source, " /sk choose <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.allowsplayerschoose", ChatFormatting.GRAY, Reference.NAME);
		MessageFunctions.sendMessage(source, " /sk info <kit_name>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendTranslatableMessage(source, "     ", "collective.starterkit.message.showsdescriptionitems", ChatFormatting.GRAY);
		return 1;
	}

	private static int processCommand(CommandSourceStack source, String kitName, boolean adding) {
		if (!permissionCheck(source)) { return 0; }

		if (!source.isPlayer()) {
			MessageFunctions.sendTranslatableMessage(source, "collective.shared.message.playeronly", ChatFormatting.RED);
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
			MessageFunctions.sendTranslatableMessage(source, adding ? "collective.starterkit.message.somethingwentwrongadding" : "collective.starterkit.message.somethingwentwrongsetting", ChatFormatting.RED);
			return 0;
		}

		MessageFunctions.sendTranslatableMessage(source, adding ? "collective.starterkit.message.starterkitadded" : "collective.starterkit.message.starterkitset", true, ChatFormatting.DARK_GREEN, Util.formatKitName(actualKitName));

		if (!kitName.equals(actualKitName)) {
			MessageFunctions.sendTranslatableMessage(source, " ", "collective.starterkit.message.ifdlike", true, ChatFormatting.GRAY);
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

		MessageFunctions.sendTranslatableMessage(source, " ", now ? "collective.starterkit.message.kitcountnow" : "collective.starterkit.message.kitcount", ChatFormatting.DARK_GRAY, kitCount);
	}
}

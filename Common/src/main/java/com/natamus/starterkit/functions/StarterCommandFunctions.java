package com.natamus.starterkit.functions;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class StarterCommandFunctions {
	public static final SuggestionProvider<CommandSourceStack> activeKitSuggestions = (context, builder) -> SharedSuggestionProvider.suggest(
		StarterGearFunctions.getActiveKitNames(), builder,
        value -> value.replace(" ", "_").toLowerCase(),
        value -> Component.literal(value.replace(" ", "_").toLowerCase())
    );

	public static final SuggestionProvider<CommandSourceStack> inactiveKitSuggestions = (context, builder) -> SharedSuggestionProvider.suggest(
		StarterGearFunctions.getInactiveKitNames(), builder,
        value -> value.replace(" ", "_").toLowerCase(),
        value -> Component.literal(value.replace(" ", "_").toLowerCase())
    );

	public static final SuggestionProvider<CommandSourceStack> activeKitSuggestionsWithAll = (context, builder) -> SharedSuggestionProvider.suggest(
		StarterGearFunctions.getActiveKitNames(true), builder,
        value -> value.replace(" ", "_").toLowerCase(),
        value -> Component.literal(value.replace(" ", "_").toLowerCase())
    );

	public static final SuggestionProvider<CommandSourceStack> inactiveKitSuggestionsWithAll = (context, builder) -> SharedSuggestionProvider.suggest(
		StarterGearFunctions.getInactiveKitNames(true), builder,
        value -> value.replace(" ", "_").toLowerCase(),
        value -> Component.literal(value.replace(" ", "_").toLowerCase())
    );
}

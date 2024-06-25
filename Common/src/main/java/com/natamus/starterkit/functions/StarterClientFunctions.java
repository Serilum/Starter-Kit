package com.natamus.starterkit.functions;

import com.natamus.collective.functions.GearFunctions;
import com.natamus.starterkit.data.Constants;
import com.natamus.starterkit.data.ConstantsClient;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.inventory.StarterKitInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StarterClientFunctions {
	public static void showInitialChooseKitScreen() {
		showInitialChooseKitScreen(ConstantsClient.mc.player);
	}
	public static void showInitialChooseKitScreen(Player player) {
		if (!cacheLocalPlayerEquipment()) {
			Constants.logger.warn(Constants.logPrefix + "Unable to cache the local player's prior equipment.");
			return;
		}

		removeLocalPlayerEquipment();
		setCachedStarterKitInventory(player, 0);

		if (VariablesClient.cachedStarterKitInventory == null) {
			Constants.logger.warn(Constants.logPrefix + "Cannot initiate StarterKitInventoryScreen, the cached starter kit inventory is null.");
            return;
		}

		ConstantsClient.mc.setScreen(new StarterKitInventoryScreen(player));
	}

	public static void cycleChooseKitScreen(Player player, boolean next) {
		if (player == null) {
			Constants.logger.warn(Constants.logPrefix + "Unable to cycle the choose kit screen, player is null.");
			return;
		}

		List<String> activeKitNames = StarterGearFunctions.getActiveKitNames(VariablesClient.cachedStarterGearEntries);

		int kitIndex = 0;
		for (String activeKitName : activeKitNames) {
			if (activeKitName.equals(VariablesClient.cachedStarterKitName)) {
				break;
			}

			kitIndex += 1;
		}

		if (next) {
			kitIndex += 1;
		}
		else {
			kitIndex -= 1;
		}

		if (kitIndex < 0) {
			kitIndex = activeKitNames.size()-1;
		}
		else if (kitIndex >= activeKitNames.size()) {
			kitIndex = 0;
		}

		removeLocalPlayerEquipment();
		setCachedStarterKitInventory(player, kitIndex);

		if (VariablesClient.cachedStarterKitInventory == null) {
			Constants.logger.warn(Constants.logPrefix + "Cannot cycle StarterKitInventoryScreen, the cached starter kit inventory is null.");
            return;
		}

		ConstantsClient.mc.setScreen(new StarterKitInventoryScreen(player));
	}

	public static void setCachedStarterKitInventory(Player player, int kitIndex) {
		Inventory inventory = new Inventory(player);

		if (VariablesClient.cachedStarterGearEntries.size() == 0) {
			Constants.logger.warn(Constants.logPrefix + "Unable to show the choose kit screen, starter gear entries are empty.");
			return;
		}
		else if (kitIndex >= VariablesClient.cachedStarterGearEntries.size()) {
			Constants.logger.warn(Constants.logPrefix + "Unable to show the choose kit screen, kit index higher than the cached starter gear entries.");
			return;
		}
		
		String kitName = StarterGearFunctions.getActiveKitNames(VariablesClient.cachedStarterGearEntries).get(kitIndex);
		String gearString = VariablesClient.cachedStarterGearEntries.get(kitName);

		if (gearString == null) {
			Constants.logger.warn(Constants.logPrefix + "Unable to show the choose kit screen, gearString is null from kit name '" + kitName + "' and kit index " + kitIndex + ".");
			return;
		}

		GearFunctions.setInventoryFromGearString(inventory, gearString);

		VariablesClient.cachedStarterKitName = kitName;
		VariablesClient.cachedStarterKitInventory = inventory;
		VariablesClient.cachedStarterKitEffects = GearFunctions.getEffectsFromGearString(player, gearString);
	}

	public static boolean cacheLocalPlayerEquipment() {
		LocalPlayer localPlayer = ConstantsClient.mc.player;
		if (localPlayer == null) {
			return false;
		}

		return cacheLocalPlayerEquipment(localPlayer);
	}
	public static boolean cacheLocalPlayerEquipment(Player player) {
		VariablesClient.priorPlayerEquipment = new HashMap<EquipmentSlot, ItemStack>();
		VariablesClient.priorPlayerHotbar = new ArrayList<ItemStack>();

		player.getInventory().selected = 0;
		for (EquipmentSlot equipmentSlot : Constants.equipmentSlots) {
			VariablesClient.priorPlayerEquipment.put(equipmentSlot, player.getItemBySlot(equipmentSlot).copy());
		}

		for (int i = 0; i < 9; i++) {
			VariablesClient.priorPlayerHotbar.add(player.getInventory().getItem(i).copy());
		}

		return true;
	}

	public static boolean removeLocalPlayerEquipment() {
		LocalPlayer localPlayer = ConstantsClient.mc.player;
		if (localPlayer == null) {
			return false;
		}

		return removeLocalPlayerEquipment(localPlayer);
	}
	public static boolean removeLocalPlayerEquipment(Player player) {
		ItemStack airStack = new ItemStack(Items.AIR, 1);

		for (EquipmentSlot equipmentSlot : Constants.equipmentSlots) {
			player.setItemSlot(equipmentSlot, airStack.copy());
		}

		for (int i = 0; i < 9; i++) {
			player.getInventory().setItem(i, airStack.copy());
		}

		return true;
	}

	public static boolean setPriorLocalPlayerEquipment() {
		LocalPlayer localPlayer = ConstantsClient.mc.player;
		if (localPlayer == null) {
			return false;
		}

		return setPriorLocalPlayerEquipment(localPlayer);
	}
	public static boolean setPriorLocalPlayerEquipment(Player player) {
		if (VariablesClient.priorPlayerEquipment == null || VariablesClient.priorPlayerHotbar == null) {
			return false;
		}

		player.getInventory().selected = 0;
		for (EquipmentSlot equipmentSlot : Constants.equipmentSlots) {
			ItemStack slotStack = VariablesClient.priorPlayerEquipment.get(equipmentSlot);

			if (slotStack != null) {
				player.setItemSlot(equipmentSlot, slotStack.copy());
			}
		}

		for (int i = 0; i < 9; i++) {
			player.getInventory().setItem(i, VariablesClient.priorPlayerHotbar.get(i).copy());
		}

		ConstantsClient.mc.options.hideGui = false;
		return true;
	}

	public static void clearStarterKitClientCache() {
		VariablesClient.cachedStarterKitName = "";
		VariablesClient.cachedStarterKitInventory = null;
		VariablesClient.cachedStarterKitEffects = new ArrayList<MobEffectInstance>();

		clearPriorEquipmentCache();
	}

	public static void clearPriorEquipmentCache() {
		VariablesClient.priorPlayerEquipment = null;
		VariablesClient.priorPlayerHotbar = null;

		ConstantsClient.mc.options.hideGui = false;
	}

	public static void selectFirstSlot() {
		Player player = ConstantsClient.mc.player;
		if (player != null) {
			player.getInventory().selected = 0;
		}
	}
}

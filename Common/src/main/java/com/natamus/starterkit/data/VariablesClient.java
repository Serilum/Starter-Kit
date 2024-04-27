package com.natamus.starterkit.data;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VariablesClient {
	public static HashMap<String, String> cachedStarterGearEntries = new HashMap<String, String>();
	public static HashMap<String, String> cachedStarterKitDescriptions = new HashMap<String, String>();


	public static String cachedStarterKitName = "";
	public static Inventory cachedStarterKitInventory = null;
	public static List<MobEffectInstance> cachedStarterKitEffects = new ArrayList<MobEffectInstance>();

	public static HashMap<EquipmentSlot, ItemStack> priorPlayerEquipment = null;
	public static List<ItemStack> priorPlayerHotbar = null;

	public static boolean waitingForAnnouncement = false;
	public static boolean openChooseKitScreen = false;
}

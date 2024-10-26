package com.natamus.starterkit.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import com.natamus.starterkit.util.Reference;
import net.minecraft.world.entity.EquipmentSlot;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Constants {
	public static final Logger logger = LogUtils.getLogger();
	public static final String logPrefix = "[" + Reference.NAME + "] ";

	public static final Gson gson = new Gson();
	public static final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
	public static final Type gsonTrackingMapType = new TypeToken<Map<String, Map<String, Boolean>>>(){}.getType();

	public static List<EquipmentSlot> equipmentSlots = new ArrayList<EquipmentSlot>(Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND));
	public static List<String> commandPrefixes = new ArrayList<String>(Arrays.asList("starterkit", "sk"));
}

package com.natamus.starterkit.inventory;

import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

public record SnapbackData(ItemStack item, Vector2i start, Vector2i end, long time) {}
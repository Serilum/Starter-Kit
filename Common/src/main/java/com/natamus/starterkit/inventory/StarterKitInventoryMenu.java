package com.natamus.starterkit.inventory;

import com.mojang.datafixers.util.Pair;
import com.natamus.starterkit.data.VariablesClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class StarterKitInventoryMenu extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    public static final int CONTAINER_ID = 0;
    public static final int RESULT_SLOT = 0;
    public static final int CRAFT_SLOT_START = 0;
    public static final int CRAFT_SLOT_END = 0;
    public static final int ARMOR_SLOT_START = 5;
    public static final int ARMOR_SLOT_END = 9;
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 36;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 45;
    public static final int SHIELD_SLOT = 45;
    public static final ResourceLocation BLOCK_ATLAS = ResourceLocation.parse("textures/atlas/blocks.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = ResourceLocation.parse("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = ResourceLocation.parse("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = ResourceLocation.parse("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = ResourceLocation.parse("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = ResourceLocation.parse("item/empty_armor_slot_shield");
    static final ResourceLocation[] TEXTURE_EMPTY_SLOTS;
    private static final EquipmentSlot[] SLOT_IDS;
    public final boolean active;
    private final Player owner;

    public StarterKitInventoryMenu(Player player) {
        super(null, 0);

        int i;
        int j;
        this.active = true;
        this.owner = player;

        if (VariablesClient.cachedStarterKitInventory == null) {
            return;
        }

        for (i = 0; i < 4; ++i) {
            final EquipmentSlot equipmentSlot = SLOT_IDS[i];
            this.addSlot(new Slot(VariablesClient.cachedStarterKitInventory, 39 - i, 8, 8 + i * 18) {
                public void setByPlayer(@NotNull ItemStack newStack, @NotNull ItemStack oldStack) {
                    StarterKitInventoryMenu.onEquipItem(owner, equipmentSlot, newStack, oldStack);
                    super.setByPlayer(newStack, oldStack);
                }

                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }

                public boolean mayPickup(@NotNull Player player) {
                    ItemStack itemStack = this.getItem();
                    return false;
                }

                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(StarterKitInventoryMenu.BLOCK_ATLAS, StarterKitInventoryMenu.TEXTURE_EMPTY_SLOTS[equipmentSlot.getIndex()]);
                }
            });
        }

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(VariablesClient.cachedStarterKitInventory, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(VariablesClient.cachedStarterKitInventory, i, 8 + i * 18, 142));
        }

        this.addSlot(new Slot(VariablesClient.cachedStarterKitInventory, 40, 77, 62) {
            public void setByPlayer(@NotNull ItemStack newStack, @NotNull ItemStack oldStack) {
                StarterKitInventoryMenu.onEquipItem(owner, EquipmentSlot.OFFHAND, newStack, oldStack);
                super.setByPlayer(newStack, oldStack);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(StarterKitInventoryMenu.BLOCK_ATLAS, StarterKitInventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    static void onEquipItem(Player player, EquipmentSlot slot, ItemStack newItem, ItemStack oldItem) {
        player.onEquipItem(slot, oldItem, newItem);
    }

    public static boolean isHotbarSlot(int index) {
        return index >= 36 && index < 45 || index == 45;
    }

    public void fillCraftSlotsStackedContents(@NotNull StackedContents itemHelper) { }

    public void clearCraftingContent() { }

    @Override
    public boolean recipeMatches(@NotNull RecipeHolder recipeHolder) {
        return false;
    }

    public void slotsChanged(@NotNull Container container) {

    }

    public void removed(@NotNull Player player) {
        super.removed(player);
    }

    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    public boolean canTakeItemForPickAll(@NotNull ItemStack stack, @NotNull Slot slot) {
        return false;
    }

    public int getResultSlotIndex() {
        return 0;
    }

    public int getGridWidth() {
        return 0;
    }

    public int getGridHeight() {
        return 0;
    }

    public int getSize() {
        return 5;
    }

    public CraftingContainer getCraftSlots() {
        return null;
    }

    public @NotNull RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public boolean shouldMoveToInventory(int slotIndex) {
        return slotIndex != this.getResultSlotIndex();
    }

    static {
        TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
        SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    }
}
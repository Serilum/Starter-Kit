package com.natamus.starterkit.inventory;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.natamus.starterkit.data.Constants;
import com.natamus.starterkit.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class StarterKitAbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {
    public static final ResourceLocation INVENTORY_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/inventory.png");
    private static final float SNAPBACK_SPEED = 100.0F;
    private static final int QUICKDROP_DELAY = 500;
    public static final int SLOT_ITEM_BLIT_OFFSET = 100;
    private static final int HOVER_ITEM_BLIT_OFFSET = 200;
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int titleLabelX;
    protected int titleLabelY;
    protected int inventoryLabelX;
    protected int inventoryLabelY;
    protected T menu;
    protected Component playerInventoryTitle;
    @Nullable
    protected Slot hoveredSlot;
    @Nullable
    private Slot clickedSlot;
    @Nullable
    private Slot snapbackEnd;
    @Nullable
    private Slot quickdropSlot;
    @Nullable
    private Slot lastClickSlot;
    protected int leftPos;
    protected int topPos;
    private boolean isSplittingStack;
    private ItemStack draggingItem;
    private int snapbackStartX;
    private int snapbackStartY;
    private long snapbackTime;
    private ItemStack snapbackItem;
    private long quickdropTime;
    protected Set<Slot> quickCraftSlots;
    protected boolean isQuickCrafting;
    private int quickCraftingType;
    private int quickCraftingButton;
    private boolean skipNextRelease;
    private int quickCraftingRemainder;
    private long lastClickTime;
    private int lastClickButton;
    private boolean doubleclick;
    private ItemStack lastQuickMoved;

    public StarterKitAbstractContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(title);

        if (playerInventory == null) {
            Constants.logger.warn(Constants.logPrefix + "Cannot initiate StarterKitAbstractContainerScreen, the playerInventory is null.");
            return;
        }

        this.draggingItem = ItemStack.EMPTY;
        this.snapbackItem = ItemStack.EMPTY;
        this.quickCraftSlots = Sets.newHashSet();
        this.lastQuickMoved = ItemStack.EMPTY;
        this.menu = menu;
        this.playerInventoryTitle = playerInventory.getDisplayName();
        this.skipNextRelease = true;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.disableDepthTest();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)i, (float)j, 0.0F);
        this.hoveredSlot = null;

        if (this.menu == null) {
            return;
        }

        int l;
        int m;
        for(int k = 0; k < this.menu.slots.size(); ++k) {
            Slot slot = this.menu.slots.get(k);
            if (slot.isActive()) {
                this.renderSlot(guiGraphics, slot);
            }

            if (this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
                this.hoveredSlot = slot;
                l = slot.x;
                m = slot.y;
                if (this.hoveredSlot.isHighlightable()) {
                    renderSlotHighlight(guiGraphics, l, m, 0);
                }
            }
        }

        this.renderLabels(guiGraphics, mouseX, mouseY);
        ItemStack itemStack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (!itemStack.isEmpty()) {
            int n = 1;
            l = this.draggingItem.isEmpty() ? 8 : 16;
            String string = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemStack = itemStack.copyWithCount(Mth.ceil((float)itemStack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemStack = itemStack.copyWithCount(this.quickCraftingRemainder);
                if (itemStack.isEmpty()) {
                    string = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(guiGraphics, itemStack, mouseX - i - 8, mouseY - j - l, string);
        }

        if (!this.snapbackItem.isEmpty()) {
            float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            l = this.snapbackEnd.x - this.snapbackStartX;
            m = this.snapbackEnd.y - this.snapbackStartY;
            int o = this.snapbackStartX + (int)((float)l * f);
            int p = this.snapbackStartY + (int)((float)m * f);
            this.renderFloatingItem(guiGraphics, this.snapbackItem, o, p, null);
        }

        guiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
    }

    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    public static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 16, -2130706433, -2130706433, blitOffset);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemStack = this.hoveredSlot.getItem();
            guiGraphics.renderTooltip(this.font, this.getTooltipFromContainerItem(itemStack), itemStack.getTooltipImage(), x, y);
        }

    }

    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        return getTooltipFromItem(this.minecraft, stack);
    }

    private void renderFloatingItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 232.0F);
        guiGraphics.renderItem(stack, x, y);
        guiGraphics.renderItemDecorations(this.font, stack, x, y - (this.draggingItem.isEmpty() ? 0 : 8), text);
        guiGraphics.pose().popPose();
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    protected abstract void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);

    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        int i = slot.x;
        int j = slot.y;
        ItemStack itemStack = slot.getItem();
        boolean bl = false;
        boolean bl2 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemStack2 = this.menu.getCarried();
        String string = null;
        int k;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemStack2.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack2, true) && this.menu.canDragTo(slot)) {
                bl = true;
                k = Math.min(itemStack2.getMaxStackSize(), slot.getMaxStackSize(itemStack2));
                int l = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int m = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack2) + l;
                if (m > k) {
                    m = k;
                    String var10000 = ChatFormatting.YELLOW.toString();
                    string = var10000 + k;
                }

                itemStack = itemStack2.copyWithCount(m);
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        if (itemStack.isEmpty() && slot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureAtlasSprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                guiGraphics.blit(i, j, 0, 16, 16, textureAtlasSprite);
                bl2 = true;
            }
        }

        if (!bl2) {
            if (bl) {
                guiGraphics.fill(i, j, i + 16, j + 16, -2130706433);
            }

            k = slot.x + slot.y * this.imageWidth;
            if (slot.isFake()) {
                guiGraphics.renderFakeItem(itemStack, i, j, k);
            } else {
                guiGraphics.renderItem(itemStack, i, j, k);
            }

            guiGraphics.renderItemDecorations(this.font, itemStack, i, j, string);
        }

        guiGraphics.pose().popPose();
    }

    @SuppressWarnings("rawtypes")
    private void recalculateQuickCraftRemaining() {
        ItemStack itemStack = this.menu.getCarried();
        if (!itemStack.isEmpty() && this.isQuickCrafting) {
            if (this.quickCraftingType == 2) {
                this.quickCraftingRemainder = itemStack.getMaxStackSize();
            } else {
                this.quickCraftingRemainder = itemStack.getCount();

                int i;
                int k;
                for(Iterator var2 = this.quickCraftSlots.iterator(); var2.hasNext(); this.quickCraftingRemainder -= k - i) {
                    Slot slot = (Slot)var2.next();
                    ItemStack itemStack2 = slot.getItem();
                    i = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
                    int j = Math.min(itemStack.getMaxStackSize(), slot.getMaxStackSize(itemStack));
                    k = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack) + i, j);
                }

            }
        }
    }

    @Nullable
    private Slot findSlot(double mouseX, double mouseY) {
        for(int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            if (this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
                return slot;
            }
        }

        return null;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            boolean bl = this.minecraft.options.keyPickItem.matchesMouse(button) && this.minecraft.gameMode.hasInfiniteItems();
            Slot slot = this.findSlot(mouseX, mouseY);
            long l = Util.getMillis();
            this.doubleclick = this.lastClickSlot == slot && l - this.lastClickTime < 250L && this.lastClickButton == button;
            this.skipNextRelease = false;
            if (button != 0 && button != 1 && !bl) {
                this.checkHotbarMouseClicked(button);
            } else {
                int i = this.leftPos;
                int j = this.topPos;
                boolean bl2 = this.hasClickedOutside(mouseX, mouseY, i, j, button);
                int k = -1;
                if (slot != null) {
                    k = slot.index;
                }

                if (bl2) {
                    k = -999;
                }

                if (this.minecraft.options.touchscreen().get() && bl2 && this.menu.getCarried().isEmpty()) {
                    this.onClose();
                    return true;
                }

                if (k != -1) {
                    if (this.minecraft.options.touchscreen().get()) {
                        if (slot != null && slot.hasItem()) {
                            this.clickedSlot = slot;
                            this.draggingItem = ItemStack.EMPTY;
                            this.isSplittingStack = button == 1;
                        } else {
                            this.clickedSlot = null;
                        }
                    } else if (!this.isQuickCrafting) {
                        if (this.menu.getCarried().isEmpty()) {
                            if (bl) {
                                this.slotClicked(slot, k, button, ClickType.CLONE);
                            } else {
                                boolean bl3 = k != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                                ClickType clickType = ClickType.PICKUP;
                                if (bl3) {
                                    this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                                    clickType = ClickType.QUICK_MOVE;
                                } else if (k == -999) {
                                    clickType = ClickType.THROW;
                                }

                                this.slotClicked(slot, k, button, clickType);
                            }

                            this.skipNextRelease = true;
                        } else {
                            this.isQuickCrafting = true;
                            this.quickCraftingButton = button;
                            this.quickCraftSlots.clear();
                            if (button == 0) {
                                this.quickCraftingType = 0;
                            } else if (button == 1) {
                                this.quickCraftingType = 1;
                            } else if (bl) {
                                this.quickCraftingType = 2;
                            }
                        }
                    }
                }
            }

            this.lastClickSlot = slot;
            this.lastClickTime = l;
            this.lastClickButton = button;
            return true;
        }
    }

    private void checkHotbarMouseClicked(int keyCode) {
        if (this.hoveredSlot != null && this.menu.getCarried().isEmpty()) {
            if (this.minecraft.options.keySwapOffhand.matchesMouse(keyCode)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return;
            }

            for(int i = 0; i < 9; ++i) {
                if (this.minecraft.options.keyHotbarSlots[i].matchesMouse(keyCode)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
                }
            }
        }

    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return mouseX < (double)guiLeft || mouseY < (double)guiTop || mouseX >= (double)(guiLeft + this.imageWidth) || mouseY >= (double)(guiTop + this.imageHeight);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        Slot slot = this.findSlot(mouseX, mouseY);
        ItemStack itemStack = this.menu.getCarried();
        if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
            if (button == 0 || button == 1) {
                if (this.draggingItem.isEmpty()) {
                    if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
                        this.draggingItem = this.clickedSlot.getItem().copy();
                    }
                } else if (this.draggingItem.getCount() > 1 && slot != null && AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false)) {
                    long l = Util.getMillis();
                    if (this.quickdropSlot == slot) {
                        if (l - this.quickdropTime > 500L) {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.quickdropTime = l + 750L;
                            this.draggingItem.shrink(1);
                        }
                    } else {
                        this.quickdropSlot = slot;
                        this.quickdropTime = l;
                    }
                }
            }
        } else if (this.isQuickCrafting && slot != null && !itemStack.isEmpty() && (itemStack.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2) && AbstractContainerMenu.canItemQuickReplace(slot, itemStack, true) && slot.mayPlace(itemStack) && this.menu.canDragTo(slot)) {
            this.quickCraftSlots.add(slot);
            this.recalculateQuickCraftRemaining();
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Slot slot = this.findSlot(mouseX, mouseY);
        int i = this.leftPos;
        int j = this.topPos;
        boolean bl = this.hasClickedOutside(mouseX, mouseY, i, j, button);
        int k = -1;
        if (slot != null) {
            k = slot.index;
        }

        if (bl) {
            k = -999;
        }

        Slot slot2;
        Iterator var13;
        if (this.doubleclick && slot != null && button == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
            if (hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    var13 = this.menu.slots.iterator();

                    while(var13.hasNext()) {
                        slot2 = (Slot)var13.next();
                        if (slot2 != null && slot2.mayPickup(this.minecraft.player) && slot2.hasItem() && slot2.container == slot.container && AbstractContainerMenu.canItemQuickReplace(slot2, this.lastQuickMoved, true)) {
                            this.slotClicked(slot2, slot2.index, button, ClickType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(slot, k, button, ClickType.PICKUP_ALL);
            }

            this.doubleclick = false;
            this.lastClickTime = 0L;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != button) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }

            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }

            boolean bl2;
            if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
                if (button == 0 || button == 1) {
                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }

                    bl2 = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
                    if (k != -1 && !this.draggingItem.isEmpty() && bl2) {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, button, ClickType.PICKUP);
                        this.slotClicked(slot, k, 0, ClickType.PICKUP);
                        if (this.menu.getCarried().isEmpty()) {
                            this.snapbackItem = ItemStack.EMPTY;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, button, ClickType.PICKUP);
                            this.snapbackStartX = Mth.floor(mouseX - (double)i);
                            this.snapbackStartY = Mth.floor(mouseY - (double)j);
                            this.snapbackEnd = this.clickedSlot;
                            this.snapbackItem = this.draggingItem;
                            this.snapbackTime = Util.getMillis();
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackStartX = Mth.floor(mouseX - (double)i);
                        this.snapbackStartY = Mth.floor(mouseY - (double)j);
                        this.snapbackEnd = this.clickedSlot;
                        this.snapbackItem = this.draggingItem;
                        this.snapbackTime = Util.getMillis();
                    }

                    this.clearDraggingState();
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);
                var13 = this.quickCraftSlots.iterator();

                while(var13.hasNext()) {
                    slot2 = (Slot)var13.next();
                    this.slotClicked(slot2, slot2.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }

                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            } else if (!this.menu.getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.matchesMouse(button)) {
                    this.slotClicked(slot, k, button, ClickType.CLONE);
                } else {
                    bl2 = k != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                    if (bl2) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }

                    this.slotClicked(slot, k, button, bl2 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
        }

        if (this.menu.getCarried().isEmpty()) {
            this.lastClickTime = 0L;
        }

        this.isQuickCrafting = false;
        return true;
    }

    public void clearDraggingState() {
        this.draggingItem = ItemStack.EMPTY;
        this.clickedSlot = null;
    }

    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    @SuppressWarnings("SameParameterValue")
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        mouseX -= i;
        mouseY -= j;
        return mouseX >= (double)(x - 1) && mouseX < (double)(x + width + 1) && mouseY >= (double)(y - 1) && mouseY < (double)(y + height + 1);
    }

    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot != null) {
            slotId = slot.index;
        }

        this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, slotId, mouseButton, type, this.minecraft.player);
    }

    protected void handleSlotStateChanged(int slotId, int containerId, boolean newState) {
        this.minecraft.gameMode.handleSlotStateChanged(slotId, containerId, newState);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        } else {
            this.checkHotbarKeyPressed(keyCode, scanCode);
            if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                if (this.minecraft.options.keyPickItem.matches(keyCode, scanCode)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
                } else if (this.minecraft.options.keyDrop.matches(keyCode, scanCode)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
                }
            }

            return true;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
            if (this.minecraft.options.keySwapOffhand.matches(keyCode, scanCode)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return true;
            }

            for(int i = 0; i < 9; ++i) {
                if (this.minecraft.options.keyHotbarSlots[i].matches(keyCode, scanCode)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
                    return true;
                }
            }
        }

        return false;
    }

    public void removed() {
        if (this.minecraft.player != null) {
            this.menu.removed(this.minecraft.player);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public final void tick() {
        super.tick();
        if (this.minecraft.player.isAlive() && !this.minecraft.player.isRemoved()) {
            this.containerTick();
        } else {
            this.minecraft.player.closeContainer();
        }

    }

    protected void containerTick() {
    }

    public @NotNull T getMenu() {
        return this.menu;
    }

    public void onClose() {
        this.minecraft.player.closeContainer();
        super.onClose();
    }
}

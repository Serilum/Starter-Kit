package com.natamus.starterkit.inventory;

import com.google.common.collect.Sets;
import com.natamus.starterkit.data.Constants;
import com.natamus.starterkit.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class StarterKitAbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {
    public static final Identifier INVENTORY_LOCATION = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/container/inventory.png");
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");
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

    @SuppressWarnings("FieldCanBeLocal")
    private SnapbackData snapbackData;

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

    @SuppressWarnings("UnusedAssignment")
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)i, (float)j);
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
                    this.renderSlotHighlightBack(guiGraphics);
                    this.renderSlotHighlightFront(guiGraphics);
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

        guiGraphics.pose().popMatrix();
    }

    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    private void renderSlotHighlightBack(GuiGraphics $$0) {
        if (this.hoveredSlot != null && this.hoveredSlot.isHighlightable()) {
            $$0.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, this.hoveredSlot.x - 4, this.hoveredSlot.y - 4, 24, 24);
        }

    }

    private void renderSlotHighlightFront(GuiGraphics $$0) {
        if (this.hoveredSlot != null && this.hoveredSlot.isHighlightable()) {
            $$0.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, this.hoveredSlot.x - 4, this.hoveredSlot.y - 4, 24, 24);
        }

    }

    protected void renderTooltip(GuiGraphics guiGraphics, int i, int j) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack $$3 = this.hoveredSlot.getItem();
            if (this.menu.getCarried().isEmpty() || this.showTooltipWithItemInHand($$3)) {
                guiGraphics.setTooltipForNextFrame(this.font, this.getTooltipFromContainerItem($$3), $$3.getTooltipImage(), i, j, (Identifier)$$3.get(DataComponents.TOOLTIP_STYLE));
            }

        }
    }

    private boolean showTooltipWithItemInHand(ItemStack $$0) {
        return (Boolean)$$0.getTooltipImage().map(ClientTooltipComponent::create).map(ClientTooltipComponent::showTooltipWithItemInHand).orElse(false);
    }

    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        return getTooltipFromItem(this.minecraft, stack);
    }

    private void renderFloatingItem(GuiGraphics $$0, ItemStack $$1, int $$2, int $$3, @Nullable String $$4) {
        $$0.renderItem($$1, $$2, $$3);
        $$0.renderItemDecorations(this.font, $$1, $$2, $$3 - (this.draggingItem.isEmpty() ? 0 : 8), $$4);
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    protected abstract void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);

    protected void renderSlot(GuiGraphics $$0, Slot $$1) {
        int $$2 = $$1.x;
        int $$3 = $$1.y;
        ItemStack $$4 = $$1.getItem();
        boolean $$5 = false;
        boolean $$6 = $$1 == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack $$7 = this.menu.getCarried();
        String $$8 = null;
        if ($$1 == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !$$4.isEmpty()) {
            $$4 = $$4.copyWithCount($$4.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains($$1) && !$$7.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace($$1, $$7, true) && this.menu.canDragTo($$1)) {
                $$5 = true;
                int $$9 = Math.min($$7.getMaxStackSize(), $$1.getMaxStackSize($$7));
                int $$10 = $$1.getItem().isEmpty() ? 0 : $$1.getItem().getCount();
                int $$11 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, $$7) + $$10;
                if ($$11 > $$9) {
                    $$11 = $$9;
                    String var10000 = ChatFormatting.YELLOW.toString();
                    $$8 = var10000 + $$9;
                }

                $$4 = $$7.copyWithCount($$11);
            } else {
                this.quickCraftSlots.remove($$1);
                this.recalculateQuickCraftRemaining();
            }
        }

        if ($$4.isEmpty() && $$1.isActive()) {
            Identifier $$12 = $$1.getNoItemIcon();
            if ($$12 != null) {
                $$0.blitSprite(RenderPipelines.GUI_TEXTURED, $$12, $$2, $$3, 16, 16);
                $$6 = true;
            }
        }

        if (!$$6) {
            if ($$5) {
                $$0.fill($$2, $$3, $$2 + 16, $$3 + 16, -2130706433);
            }

            int $$13 = $$1.x + $$1.y * this.imageWidth;
            if ($$1.isFake()) {
                $$0.renderFakeItem($$4, $$2, $$3, $$13);
            } else {
                $$0.renderItem($$4, $$2, $$3, $$13);
            }

            $$0.renderItemDecorations(this.font, $$4, $$2, $$3, $$8);
        }

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

    @SuppressWarnings("IfStatementWithIdenticalBranches")
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean $$1) {
        if (super.mouseClicked(mouseButtonEvent, $$1)) {
            return true;
        } else {
            boolean $$2 = this.minecraft.options.keyPickItem.matchesMouse(mouseButtonEvent) && this.minecraft.player.hasInfiniteMaterials();
            Slot $$3 = this.getHoveredSlot(mouseButtonEvent.x(), mouseButtonEvent.y());
            this.doubleclick = this.lastClickSlot == $$3 && $$1;
            this.skipNextRelease = false;
            if (mouseButtonEvent.button() != 0 && mouseButtonEvent.button() != 1 && !$$2) {
                this.checkHotbarMouseClicked(mouseButtonEvent);
            } else {
                int $$4 = this.leftPos;
                int $$5 = this.topPos;
                boolean $$6 = this.hasClickedOutside(mouseButtonEvent.x(), mouseButtonEvent.y(), $$4, $$5);
                int $$7 = -1;
                if ($$3 != null) {
                    $$7 = $$3.index;
                }

                if ($$6) {
                    $$7 = -999;
                }

                if ((Boolean)this.minecraft.options.touchscreen().get() && $$6 && this.menu.getCarried().isEmpty()) {
                    this.onClose();
                    return true;
                }

                if ($$7 != -1) {
                    if ((Boolean)this.minecraft.options.touchscreen().get()) {
                        if ($$3 != null && $$3.hasItem()) {
                            this.clickedSlot = $$3;
                            this.draggingItem = ItemStack.EMPTY;
                            this.isSplittingStack = mouseButtonEvent.button() == 1;
                        } else {
                            this.clickedSlot = null;
                        }
                    } else if (!this.isQuickCrafting) {
                        if (this.menu.getCarried().isEmpty()) {
                            if ($$2) {
                                this.slotClicked($$3, $$7, mouseButtonEvent.button(), ClickType.CLONE);
                            } else {
                                boolean $$8 = $$7 != -999 && mouseButtonEvent.hasShiftDown();
                                ClickType $$9 = ClickType.PICKUP;
                                if ($$8) {
                                    this.lastQuickMoved = $$3 != null && $$3.hasItem() ? $$3.getItem().copy() : ItemStack.EMPTY;
                                    $$9 = ClickType.QUICK_MOVE;
                                } else if ($$7 == -999) {
                                    $$9 = ClickType.THROW;
                                }

                                this.slotClicked($$3, $$7, mouseButtonEvent.button(), $$9);
                            }

                            this.skipNextRelease = true;
                        } else {
                            this.isQuickCrafting = true;
                            this.quickCraftingButton = mouseButtonEvent.button();
                            this.quickCraftSlots.clear();
                            if (mouseButtonEvent.button() == 0) {
                                this.quickCraftingType = 0;
                            } else if (mouseButtonEvent.button() == 1) {
                                this.quickCraftingType = 1;
                            } else if ($$2) {
                                this.quickCraftingType = 2;
                            }
                        }
                    }
                }
            }

            this.lastClickSlot = $$3;
            return true;
        }
    }

    protected boolean hasClickedOutside(double $$0, double $$1, int $$2, int $$3) {
        return $$0 < (double)$$2 || $$1 < (double)$$3 || $$0 >= (double)($$2 + this.imageWidth) || $$1 >= (double)($$3 + this.imageHeight);
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

    public boolean mouseReleased(MouseButtonEvent $$0) {
        Slot $$1 = this.getHoveredSlot($$0.x(), $$0.y());
        int $$2 = this.leftPos;
        int $$3 = this.topPos;
        boolean $$4 = this.hasClickedOutside($$0.x(), $$0.y(), $$2, $$3);
        int $$5 = -1;
        if ($$1 != null) {
            $$5 = $$1.index;
        }

        if ($$4) {
            $$5 = -999;
        }

        if (this.doubleclick && $$1 != null && $$0.button() == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, $$1)) {
            if ($$0.hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for(Slot $$6 : this.menu.slots) {
                        if ($$6 != null && $$6.mayPickup(this.minecraft.player) && $$6.hasItem() && $$6.container == $$1.container && AbstractContainerMenu.canItemQuickReplace($$6, this.lastQuickMoved, true)) {
                            this.slotClicked($$6, $$6.index, $$0.button(), ClickType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked($$1, $$5, $$0.button(), ClickType.PICKUP_ALL);
            }

            this.doubleclick = false;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != $$0.button()) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }

            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }

            if (this.clickedSlot != null && (Boolean)this.minecraft.options.touchscreen().get()) {
                if ($$0.button() == 0 || $$0.button() == 1) {
                    if (this.draggingItem.isEmpty() && $$1 != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }

                    boolean $$7 = AbstractContainerMenu.canItemQuickReplace($$1, this.draggingItem, false);
                    if ($$5 != -1 && !this.draggingItem.isEmpty() && $$7) {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, $$0.button(), ClickType.PICKUP);
                        this.slotClicked($$1, $$5, 0, ClickType.PICKUP);
                        if (this.menu.getCarried().isEmpty()) {
                            this.snapbackData = null;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, $$0.button(), ClickType.PICKUP);
                            this.snapbackData = new SnapbackData(this.draggingItem, new Vector2i((int)$$0.x(), (int)$$0.y()), new Vector2i(this.clickedSlot.x + $$2, this.clickedSlot.y + $$3), Util.getMillis());
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackData = new SnapbackData(this.draggingItem, new Vector2i((int)$$0.x(), (int)$$0.y()), new Vector2i(this.clickedSlot.x + $$2, this.clickedSlot.y + $$3), Util.getMillis());
                    }

                    this.clearDraggingState();
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.slotClicked((Slot)null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

                for(Slot $$8 : this.quickCraftSlots) {
                    this.slotClicked($$8, $$8.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }

                this.slotClicked((Slot)null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            } else if (!this.menu.getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.matchesMouse($$0)) {
                    this.slotClicked($$1, $$5, $$0.button(), ClickType.CLONE);
                } else {
                    boolean $$9 = $$5 != -999 && $$0.hasShiftDown();
                    if ($$9) {
                        this.lastQuickMoved = $$1 != null && $$1.hasItem() ? $$1.getItem().copy() : ItemStack.EMPTY;
                    }

                    this.slotClicked($$1, $$5, $$0.button(), $$9 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
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

    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        if (super.keyPressed(keyEvent)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(keyEvent)) {
            this.onClose();
            return true;
        } else {
            this.checkHotbarKeyPressed(keyEvent);
            if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                if (this.minecraft.options.keyPickItem.matches(keyEvent)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
                } else if (this.minecraft.options.keyDrop.matches(keyEvent)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, keyEvent.hasControlDown() ? 1 : 0, ClickType.THROW);
                }
            }

            return true;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected boolean checkHotbarKeyPressed(KeyEvent $$0) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
            if (this.minecraft.options.keySwapOffhand.matches($$0)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return true;
            }

            for(int $$1 = 0; $$1 < 9; ++$$1) {
                if (this.minecraft.options.keyHotbarSlots[$$1].matches($$0)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, $$1, ClickType.SWAP);
                    return true;
                }
            }
        }

        return false;
    }

    private void checkHotbarMouseClicked(MouseButtonEvent mouseButtonEvent) {
        if (this.hoveredSlot != null && this.menu.getCarried().isEmpty()) {
            if (this.minecraft.options.keySwapOffhand.matchesMouse(mouseButtonEvent)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return;
            }

            for(int $$1 = 0; $$1 < 9; ++$$1) {
                if (this.minecraft.options.keyHotbarSlots[$$1].matchesMouse(mouseButtonEvent)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, $$1, ClickType.SWAP);
                }
            }
        }

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

    @Nullable
    private Slot getHoveredSlot(double $$0, double $$1) {
        for(Slot $$2 : this.menu.slots) {
            if ($$2.isActive() && this.isHovering($$2, $$0, $$1)) {
                return $$2;
            }
        }

        return null;
    }
}

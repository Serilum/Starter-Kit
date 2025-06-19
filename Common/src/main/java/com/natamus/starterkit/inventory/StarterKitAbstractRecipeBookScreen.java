package com.natamus.starterkit.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public abstract class StarterKitAbstractRecipeBookScreen<T extends RecipeBookMenu> extends StarterKitAbstractContainerScreen<T> implements RecipeUpdateListener {
    private final RecipeBookComponent<?> recipeBookComponent;
    private boolean widthTooNarrow;

    public StarterKitAbstractRecipeBookScreen(T $$0, RecipeBookComponent<?> $$1, Inventory $$2, Component $$3) {
        super($$0, $$2, $$3);
        this.recipeBookComponent = $$1;
    }

    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.initButton();
    }

    protected abstract ScreenPosition getRecipeBookButtonPosition();

    private void initButton() {
        ScreenPosition $$0 = this.getRecipeBookButtonPosition();
        this.addRenderableWidget(new ImageButton($$0.x(), $$0.y(), 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, ($$0x) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ScreenPosition $$1 = this.getRecipeBookButtonPosition();
            $$0x.setPosition($$1.x(), $$1.y());
            this.onRecipeBookButtonClick();
        }));
        this.addWidget(this.recipeBookComponent);
    }

    protected void onRecipeBookButtonClick() {
    }

    public void render(GuiGraphics $$0, int $$1, int $$2, float $$3) {

    }

    protected void renderSlots(GuiGraphics $$0) {

    }

    protected boolean isBiggerResultSlot() {
        return true;
    }

    public boolean charTyped(char $$0, int $$1) {
        return this.recipeBookComponent.charTyped($$0, $$1) ? true : super.charTyped($$0, $$1);
    }

    public boolean keyPressed(int $$0, int $$1, int $$2) {
        return this.recipeBookComponent.keyPressed($$0, $$1, $$2) ? true : super.keyPressed($$0, $$1, $$2);
    }

    public boolean mouseClicked(double $$0, double $$1, int $$2) {
        if (this.recipeBookComponent.mouseClicked($$0, $$1, $$2)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked($$0, $$1, $$2);
        }
    }

    protected boolean isHovering(int $$0, int $$1, int $$2, int $$3, double $$4, double $$5) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering($$0, $$1, $$2, $$3, $$4, $$5);
    }

    protected boolean hasClickedOutside(double $$0, double $$1, int $$2, int $$3, int $$4) {
        boolean $$5 = $$0 < (double)$$2 || $$1 < (double)$$3 || $$0 >= (double)($$2 + this.imageWidth) || $$1 >= (double)($$3 + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside($$0, $$1, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, $$4) && $$5;
    }

    protected void slotClicked(Slot $$0, int $$1, int $$2, ClickType $$3) {
        super.slotClicked($$0, $$1, $$2, $$3);
        this.recipeBookComponent.slotClicked($$0);
    }

    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    public void fillGhostRecipe(RecipeDisplay $$0) {
        this.recipeBookComponent.fillGhostRecipe($$0);
    }
}

package com.natamus.starterkit.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.data.ConstantsClient;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.functions.StarterClientFunctions;
import com.natamus.starterkit.networking.packets.ToServerSendKitChoicePacket;
import com.natamus.starterkit.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class StarterKitInventoryScreen extends StarterKitEffectRenderingInventoryScreen<StarterKitInventoryMenu> {
    private float xMouse;
    private float yMouse;
    private boolean widthTooNarrow;
    private boolean buttonClicked;


	public StarterKitInventoryScreen(Player player) {
        super(new StarterKitInventoryMenu(player), VariablesClient.cachedStarterKitInventory, Component.literal(Util.formatKitName(VariablesClient.cachedStarterKitName)).withStyle(ChatFormatting.BOLD));

        this.minecraft = Minecraft.getInstance();

        if (player == null) {
            return;
        }

        Inventory kitInventory = VariablesClient.cachedStarterKitInventory;
        if (kitInventory == null) {
            return;
        }

        NonNullList<ItemStack> itemList = kitInventory.items;
        NonNullList<ItemStack> armourList = kitInventory.armor;
        NonNullList<ItemStack> offhandList = kitInventory.offhand;

        player.getInventory().selected = 0;
        player.setItemSlot(EquipmentSlot.HEAD, armourList.get(3));
        player.setItemSlot(EquipmentSlot.CHEST, armourList.get(2));
        player.setItemSlot(EquipmentSlot.LEGS, armourList.get(1));
        player.setItemSlot(EquipmentSlot.FEET, armourList.get(0));
        player.setItemSlot(EquipmentSlot.MAINHAND, itemList.get(0));
        player.setItemSlot(EquipmentSlot.OFFHAND, offhandList.get(0));

        ConstantsClient.mc.options.hideGui = true;
    }

    public void containerTick() {
    }

    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;

        setupButtons();
    }

    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 104, this.height / 2 - 22);
    }

    private void setupButtons() {
		Button previousKitButton = Button.builder(Component.literal(" < "), (button) -> {
            StarterClientFunctions.cycleChooseKitScreen(this.minecraft.player, false);
		}).bounds(this.width/2 + 13, this.height/2 - 21, 30, 16).build();

		Button nextKitButton = Button.builder(Component.literal(" > "), (button) -> {
			StarterClientFunctions.cycleChooseKitScreen(this.minecraft.player, true);
		}).bounds(this.width/2 + 47, this.height/2 - 21, 30, 16).build();

		Button chooseKitButton = Button.builder(Component.literal("Choose Starter Kit"), (button) -> {
            StarterClientFunctions.clearPriorEquipmentCache();
			Dispatcher.sendToServer(new ToServerSendKitChoicePacket(VariablesClient.cachedStarterKitName));
            ConstantsClient.mc.setScreen(null);
		}).bounds(this.width/2 - 60, this.height/2 + 88, 120, 20).build();

		this.addRenderableWidget(previousKitButton);
        this.addRenderableWidget(nextKitButton);
        this.addRenderableWidget(chooseKitButton);
    }

    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String playerName = "";
        if (this.minecraft.player != null) {
            playerName = this.minecraft.player.getName().getString();
        }

        // Header
        String headerString = ConfigHandler.chooseKitText.replace("%s", playerName);
        if (playerName.equals("")) {
            headerString = headerString.replace(", p", "P");
        }

        Component headerComponent = Component.literal(headerString).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
        int headerWidth = this.font.width(headerComponent);
        guiGraphics.drawString(this.font, headerComponent, -((headerWidth - this.imageWidth)/2), -16, 0, true);

        // Kit Name
        String fullKitName = Util.formatKitName(VariablesClient.cachedStarterKitName);
        String kitName = fullKitName;
        while (this.font.width(kitName) > 88) {
            kitName = Util.removeLastChar(kitName);
        }

        if (!fullKitName.equalsIgnoreCase(kitName)) {
            kitName += "...";
        }

        guiGraphics.drawString(this.font, kitName, 79, 7, 4210752, false);

        // Kit Description
        if (VariablesClient.cachedStarterKitDescriptions.containsKey(VariablesClient.cachedStarterKitName.toLowerCase())) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();

            String kitDescription = VariablesClient.cachedStarterKitDescriptions.get(VariablesClient.cachedStarterKitName.toLowerCase());
            Component descriptionComponent = Component.literal(kitDescription);

            poseStack.scale(0.66F, 0.66F, 0.66F);

            int y = 28;
            List<FormattedCharSequence> descriptionLines = ComponentRenderUtils.wrapComponents(descriptionComponent, 136, this.minecraft.font);
            for (FormattedCharSequence line : descriptionLines) {
                int x = 120;
                if (y > 28) {
                    x -= 2;
                }

                if (y > 70 && descriptionLines.size() > 6) {
                    guiGraphics.drawString(this.font, "...", x + 132, y, 4210752, false);
                }

                guiGraphics.drawString(this.font, line, x, y, 4210752, false);
                y += 10;

                if (y > 80) {
                    break;
                }
            }

            poseStack.popPose();
        }
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(RenderType::guiTextured, INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, this.xMouse, this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int scale, float yOffset, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float)(x1 + x2) / 2.0F;
        float g = (float)(y1 + y2) / 2.0F;
        guiGraphics.enableScissor(x1, y1, x2, y2);
        float h = (float)Math.atan((f - mouseX) / 40.0F);
        float i = (float)Math.atan((g - mouseY) / 40.0F);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(i * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);
        float j = entity.yBodyRot;
        float k = entity.getYRot();
        float l = entity.getXRot();
        float m = entity.yHeadRotO;
        float n = entity.yHeadRot;
        entity.yBodyRot = 180.0F + h * 20.0F;
        entity.setYRot(180.0F + h * 40.0F);
        entity.setXRot(-i * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + yOffset, 0.0F);
        renderEntityInInventory(guiGraphics, f, g, scale, vector3f, quaternionf, quaternionf2, entity);
        entity.yBodyRot = j;
        entity.setYRot(k);
        entity.setXRot(l);
        entity.yHeadRotO = m;
        entity.yHeadRot = n;
        guiGraphics.disableScissor();
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, float x, float y, int scale, Vector3f translate, Quaternionf pose, @Nullable Quaternionf cameraOrientation, LivingEntity entity) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 50.0);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().translate(translate.x, translate.y, translate.z);
        guiGraphics.pose().mulPose(pose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            cameraOrientation.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(cameraOrientation);
        }

        entityRenderDispatcher.setRenderShadow(false);
        guiGraphics.drawSpecial((multiBufferSource) -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 1.0F, guiGraphics.pose(), multiBufferSource, 15728880);
        });
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return (!this.widthTooNarrow) && super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

	protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {

    }

    public void recipesUpdated() {

    }
}

package com.natamus.starterkit.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.data.ConstantsClient;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.functions.StarterClientFunctions;
import com.natamus.starterkit.networking.packets.ToServerSendKitChoicePacket;
import com.natamus.starterkit.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
import com.mojang.math.Vector3f;

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

    private void setupButtons() {
		Button previousKitButton = new Button(this.width/2 + 13, this.height/2 - 21, 30, 16, Component.literal(" < "), (button) -> {
            StarterClientFunctions.cycleChooseKitScreen(this.minecraft.player, false);
		});

		Button nextKitButton = new Button(this.width/2 + 47, this.height/2 - 21, 30, 16, Component.literal(" > "), (button) -> {
			StarterClientFunctions.cycleChooseKitScreen(this.minecraft.player, true);
		});

		Button chooseKitButton = new Button(this.width/2 - 60, this.height/2 + 88, 120, 20, Component.literal("Choose Starter Kit"), (button) -> {
            StarterClientFunctions.clearPriorEquipmentCache();
			Dispatcher.sendToServer(new ToServerSendKitChoicePacket(VariablesClient.cachedStarterKitName));
            ConstantsClient.mc.setScreen(null);
		});

		this.addRenderableWidget(previousKitButton);
        this.addRenderableWidget(nextKitButton);
        this.addRenderableWidget(chooseKitButton);
    }

    protected void renderLabels(PoseStack poseStack, int i, int j) {
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
        this.font.draw(poseStack, headerComponent, -((float) (headerWidth - this.imageWidth) /2), -16, 0);

        // Kit Name
        String fullKitName = Util.formatKitName(VariablesClient.cachedStarterKitName);
        String kitName = fullKitName;
        while (this.font.width(kitName) > 88) {
            kitName = Util.removeLastChar(kitName);
        }

        if (!fullKitName.equalsIgnoreCase(kitName)) {
            kitName += "...";
        }

        this.font.draw(poseStack, kitName, 79, 7, 4210752);

        // Kit Description
        if (VariablesClient.cachedStarterKitDescriptions.containsKey(VariablesClient.cachedStarterKitName)) {
            poseStack.pushPose();

            String kitDescription = VariablesClient.cachedStarterKitDescriptions.get(VariablesClient.cachedStarterKitName);
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
                    this.font.draw(poseStack, "...", x + 132, y, 4210752);
                }

                this.font.draw(poseStack, line, x, y, 4210752);
                y += 10;

                if (y > 80) {
                    break;
                }
            }

            poseStack.popPose();
        }
    }

    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);

        this.renderTooltip(poseStack, i, j);
        this.xMouse = (float)i;
        this.yMouse = (float)j;
    }

    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int k = this.leftPos;
        int l = this.topPos;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventory(k + 51, l + 75, 30, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    @SuppressWarnings("deprecation")
    public static void renderEntityInInventory(int i, int j, int k, float f, float g, LivingEntity livingEntity) {
        float h = (float)Math.atan(f / 40.0F);
        float l = (float)Math.atan(g / 40.0F);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(i, j, 1050.0);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack2 = new PoseStack();
        poseStack2.translate(0.0, 0.0, 1000.0);
        poseStack2.scale((float)k, (float)k, (float)k);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion2 = Vector3f.XP.rotationDegrees(l * 20.0F);
        quaternion.mul(quaternion2);
        poseStack2.mulPose(quaternion);
        float m = livingEntity.yBodyRot;
        float n = livingEntity.getYRot();
        float o = livingEntity.getXRot();
        float p = livingEntity.yHeadRotO;
        float q = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + h * 20.0F;
        livingEntity.setYRot(180.0F + h * 40.0F);
        livingEntity.setXRot(-l * 20.0F);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion2.conj();
        entityRenderDispatcher.overrideCameraOrientation(quaternion2);
        entityRenderDispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, poseStack2, bufferSource, 15728880);
        });
        bufferSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        livingEntity.yBodyRot = m;
        livingEntity.setYRot(n);
        livingEntity.setXRot(o);
        livingEntity.yHeadRotO = p;
        livingEntity.yHeadRot = q;
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
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

package com.natamus.starterkit.inventory;

import com.natamus.collective.functions.GearFunctions;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.data.ConstantsClient;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.functions.StarterClientFunctions;
import com.natamus.starterkit.networking.packets.ToServerSendKitChoicePacket;
import com.natamus.starterkit.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;

public class StarterKitInventoryScreen extends StarterKitAbstractContainerScreen<StarterKitInventoryMenu> {
    private float xMouse;
    private float yMouse;
    private boolean widthTooNarrow;
    private boolean buttonClicked;


	public StarterKitInventoryScreen(Player player) {
        super(new StarterKitInventoryMenu(player), VariablesClient.cachedStarterKitInventory, Component.literal(Util.formatKitName(VariablesClient.cachedStarterKitName)).withStyle(ChatFormatting.BOLD));

        if (player == null) {
            return;
        }

        Inventory kitInventory = VariablesClient.cachedStarterKitInventory;
        if (kitInventory == null) {
            return;
        }

        NonNullList<ItemStack> itemList = kitInventory.getNonEquipmentItems();

        EntityEquipment entityEquipment = GearFunctions.getEntityEquipment(kitInventory);
        List<ItemStack> armourList = Arrays.asList(entityEquipment.get(EquipmentSlot.HEAD), entityEquipment.get(EquipmentSlot.CHEST), entityEquipment.get(EquipmentSlot.LEGS), entityEquipment.get(EquipmentSlot.FEET)); // kitInventory.armor;
        List<ItemStack> offhandList = Arrays.asList(entityEquipment.get(EquipmentSlot.OFFHAND));

        player.getInventory().setSelectedSlot(0);
        player.setItemSlot(EquipmentSlot.HEAD, armourList.get(0));
        player.setItemSlot(EquipmentSlot.CHEST, armourList.get(1));
        player.setItemSlot(EquipmentSlot.LEGS, armourList.get(2));
        player.setItemSlot(EquipmentSlot.FEET, armourList.get(3));
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

    protected void renderLabels(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        String playerName = "";
        if (this.minecraft.player != null) {
            playerName = this.minecraft.player.getName().getString();
        }

        // Header
        String headerString = ConfigHandler.chooseKitText.replace("%s", playerName);
        if (playerName.isEmpty()) {
            headerString = headerString.replace(", p", "P");
        }

        Component headerComponent = Component.literal(headerString).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
        int headerWidth = this.font.width(headerComponent);
        guiGraphics.text(this.font, headerComponent, -((headerWidth - this.imageWidth)/2), -16, 0xFFFFFFFF, true);

        // Kit Name
        String fullKitName = Util.formatKitName(VariablesClient.cachedStarterKitName);
        String kitName = fullKitName;
        while (this.font.width(kitName) > 88) {
            kitName = Util.removeLastChar(kitName);
        }

        if (!fullKitName.equalsIgnoreCase(kitName)) {
            kitName += "...";
        }

        guiGraphics.text(this.font, kitName, 79, 7, 0xFF323200, false);

        // Kit Description
        if (VariablesClient.cachedStarterKitDescriptions.containsKey(VariablesClient.cachedStarterKitName.toLowerCase())) {
            Matrix3x2fStack matrixStack = guiGraphics.pose();
            matrixStack.pushMatrix();

            String kitDescription = VariablesClient.cachedStarterKitDescriptions.get(VariablesClient.cachedStarterKitName.toLowerCase());
            Component descriptionComponent = Component.literal(kitDescription);

            matrixStack.scale(0.66F, 0.66F);

            int y = 28;
            List<FormattedCharSequence> descriptionLines = ComponentRenderUtils.wrapComponents(descriptionComponent, 136, this.minecraft.font);
            for (FormattedCharSequence line : descriptionLines) {
                int x = 120;
                if (y > 28) {
                    x -= 2;
                }

                if (y > 70 && descriptionLines.size() > 6) {
                    guiGraphics.text(this.font, "...", x + 132, y, 0xFF323200, false);
                }

                guiGraphics.text(this.font, line, x, y, 0xFF323200, false);
                y += 10;

                if (y > 80) {
                    break;
                }
            }

            matrixStack.popMatrix();
        }
    }

    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }

    protected void renderBg(GuiGraphicsExtractor $$0, float $$1, int $$2, int $$3) {
        int $$4 = this.leftPos;
        int $$5 = this.topPos;
        $$0.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_LOCATION, $$4, $$5, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        renderEntityInInventoryFollowsMouse($$0, $$4 + 26, $$5 + 8, $$4 + 75, $$5 + 78, 30, 0.0625F, this.xMouse, this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphicsExtractor $$0, int $$1, int $$2, int $$3, int $$4, int $$5, float $$6, float $$7, float $$8, LivingEntity $$9) {
        float $$10 = (float)($$1 + $$3) / 2.0F;
        float $$11 = (float)($$2 + $$4) / 2.0F;
        $$0.enableScissor($$1, $$2, $$3, $$4);
        float $$12 = (float)Math.atan((double)(($$10 - $$7) / 40.0F));
        float $$13 = (float)Math.atan((double)(($$11 - $$8) / 40.0F));
        Quaternionf $$14 = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf $$15 = (new Quaternionf()).rotateX($$13 * 20.0F * ((float)Math.PI / 180F));
        $$14.mul($$15);
        float $$16 = $$9.yBodyRot;
        float $$17 = $$9.getYRot();
        float $$18 = $$9.getXRot();
        float $$19 = $$9.yHeadRotO;
        float $$20 = $$9.yHeadRot;
        $$9.yBodyRot = 180.0F + $$12 * 20.0F;
        $$9.setYRot(180.0F + $$12 * 40.0F);
        $$9.setXRot(-$$13 * 20.0F);
        $$9.yHeadRot = $$9.getYRot();
        $$9.yHeadRotO = $$9.getYRot();
        float $$21 = $$9.getScale();
        Vector3f $$22 = new Vector3f(0.0F, $$9.getBbHeight() / 2.0F + $$6 * $$21, 0.0F);
        float $$23 = (float)$$5 / $$21;
        renderEntityInInventory($$0, $$1, $$2, $$3, $$4, $$23, $$22, $$14, $$15, $$9);
        $$9.yBodyRot = $$16;
        $$9.setYRot($$17);
        $$9.setXRot($$18);
        $$9.yHeadRotO = $$19;
        $$9.yHeadRot = $$20;
        $$0.disableScissor();
    }

    public static void renderEntityInInventory(GuiGraphicsExtractor $$0, int $$1, int $$2, int $$3, int $$4, float $$5, Vector3f $$6, Quaternionf $$7, @Nullable Quaternionf $$8, LivingEntity $$9) {
        EntityRenderDispatcher $$10 = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> $$11 = $$10.getRenderer($$9);
        EntityRenderState $$12 = $$11.createRenderState($$9, 1.0F);
        $$0.entity($$12, $$5, $$6, $$7, $$8, $$1, $$2, $$3, $$4);
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(mouseButtonEvent);
        }
    }

	protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ContainerInput containerInput) {

    }

    public void recipesUpdated() {

    }
}

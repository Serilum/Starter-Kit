package com.natamus.starterkit.inventory;

import com.google.common.collect.Ordering;
import com.natamus.starterkit.data.VariablesClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class StarterKitEffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends StarterKitAbstractContainerScreen<T> {
    private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE = ResourceLocation.parse("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE = ResourceLocation.parse("container/inventory/effect_background_small");

    public StarterKitEffectRenderingInventoryScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderEffects(guiGraphics, mouseX, mouseY);
    }

    public boolean canSeeEffects() {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;
        return j >= 32;
    }

    @SuppressWarnings("rawtypes")
    private void renderEffects(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;

        if (!VariablesClient.cachedStarterKitEffects.isEmpty() && j >= 32) {
            boolean bl = j >= 120;
            int k = 33;
            if (VariablesClient.cachedStarterKitEffects.size() > 5) {
                k = 132 / (VariablesClient.cachedStarterKitEffects.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(VariablesClient.cachedStarterKitEffects);
            this.renderBackgrounds(guiGraphics, i, k, iterable, bl);
            this.renderIcons(guiGraphics, i, k, iterable, bl);
            if (bl) {
                this.renderLabels(guiGraphics, i, k, iterable);
            } else if (mouseX >= i && mouseX <= i + 33) {
                int l = this.topPos;
                MobEffectInstance mobEffectInstance = null;

                for(Iterator var12 = iterable.iterator(); var12.hasNext(); l += k) {
                    MobEffectInstance mobEffectInstance2 = (MobEffectInstance)var12.next();
                    if (mouseY >= l && mouseY <= l + k) {
                        mobEffectInstance = mobEffectInstance2;
                    }
                }

                if (mobEffectInstance != null) {
                    List<Component> list = List.of(this.getEffectName(mobEffectInstance), MobEffectUtil.formatDuration(mobEffectInstance, 1.0F, this.minecraft.level.tickRateManager().tickrate()));
                    guiGraphics.renderTooltip(this.font, list, Optional.empty(), mouseX, mouseY);
                }
            }

        }
    }

    @SuppressWarnings("rawtypes")
    private void renderBackgrounds(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall) {
        int i = this.topPos;

        for(Iterator var7 = effects.iterator(); var7.hasNext(); i += yOffset) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)var7.next();
            if (isSmall) {
                guiGraphics.blitSprite(EFFECT_BACKGROUND_LARGE_SPRITE, renderX, i, 120, 32);
            } else {
                guiGraphics.blitSprite(EFFECT_BACKGROUND_SMALL_SPRITE, renderX, i, 32, 32);
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private void renderIcons(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall) {
        MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
        int i = this.topPos;

        for(Iterator var8 = effects.iterator(); var8.hasNext(); i += yOffset) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)var8.next();
            Holder<MobEffect> mobEffectHolder = mobEffectInstance.getEffect();
            TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffectHolder);
            guiGraphics.blit(renderX + (isSmall ? 6 : 7), i + 7, 0, 18, 18, textureAtlasSprite);
        }

    }

    @SuppressWarnings("rawtypes")
    private void renderLabels(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects) {
        int i = this.topPos;

        for(Iterator var6 = effects.iterator(); var6.hasNext(); i += yOffset) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)var6.next();
            Component component = this.getEffectName(mobEffectInstance);
            guiGraphics.drawString(this.font, component, renderX + 10 + 18, i + 6, 16777215);
            Component component2 = MobEffectUtil.formatDuration(mobEffectInstance, 1.0F, this.minecraft.level.tickRateManager().tickrate());
            guiGraphics.drawString(this.font, component2, renderX + 10 + 18, i + 6 + 10, 8355711);
        }

    }

    private Component getEffectName(MobEffectInstance effect) {
        MutableComponent mutableComponent = effect.getEffect().value().getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            MutableComponent var10000 = mutableComponent.append(CommonComponents.SPACE);
            int var10001 = effect.getAmplifier();
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        }

        return mutableComponent;
    }
}
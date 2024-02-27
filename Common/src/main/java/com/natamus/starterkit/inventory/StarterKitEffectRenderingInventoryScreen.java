package com.natamus.starterkit.inventory;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.natamus.starterkit.data.VariablesClient;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    public StarterKitEffectRenderingInventoryScreen(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        this.renderEffects(poseStack, i, j);
    }

    public boolean canSeeEffects() {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;
        return j >= 32;
    }

    @SuppressWarnings("rawtypes")
    private void renderEffects(PoseStack poseStack, int i, int j) {
        int k = this.leftPos + this.imageWidth + 2;
        int l = this.width - k;
        if (!VariablesClient.cachedStarterKitEffects.isEmpty() && l >= 32) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            boolean bl = l >= 120;
            int m = 33;
            if (VariablesClient.cachedStarterKitEffects.size() > 5) {
                m = 132 / (VariablesClient.cachedStarterKitEffects.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(VariablesClient.cachedStarterKitEffects);
            this.renderBackgrounds(poseStack, k, m, iterable, bl);
            this.renderIcons(poseStack, k, m, iterable, bl);
            if (bl) {
                this.renderLabels(poseStack, k, m, iterable);
            } else if (i >= k && i <= k + 33) {
                int n = this.topPos;
                MobEffectInstance mobEffectInstance = null;

                for(Iterator var12 = iterable.iterator(); var12.hasNext(); n += m) {
                    MobEffectInstance mobEffectInstance2 = (MobEffectInstance)var12.next();
                    if (j >= n && j <= n + m) {
                        mobEffectInstance = mobEffectInstance2;
                    }
                }

                if (mobEffectInstance != null) {
                    List<Component> list = List.of(this.getEffectName(mobEffectInstance), Component.literal(MobEffectUtil.formatDuration(mobEffectInstance, 1.0F)));
                    this.renderTooltip(poseStack, list, Optional.empty(), i, j);
                }
            }

        }
    }

    @SuppressWarnings("rawtypes")
    private void renderBackgrounds(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable, boolean bl) {
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int k = this.topPos;

        for(Iterator var7 = iterable.iterator(); var7.hasNext(); k += j) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)var7.next();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (bl) {
                this.blit(poseStack, i, k, 0, 166, 120, 32);
            } else {
                this.blit(poseStack, i, k, 0, 198, 32, 32);
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private void renderIcons(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable, boolean bl) {
        MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
        int k = this.topPos;

        for(Iterator var8 = iterable.iterator(); var8.hasNext(); k += j) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)var8.next();
            MobEffect mobEffect = mobEffectInstance.getEffect();
            TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
            RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
            blit(poseStack, i + (bl ? 6 : 7), k + 7, this.getBlitOffset(), 18, 18, textureAtlasSprite);
        }

    }

    @SuppressWarnings("rawtypes")
    private void renderLabels(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable) {
        int k = this.topPos;

        for(Iterator var6 = iterable.iterator(); var6.hasNext(); k += j) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)var6.next();
            Component component = this.getEffectName(mobEffectInstance);
            this.font.drawShadow(poseStack, component, (float)(i + 10 + 18), (float)(k + 6), 16777215);
            String string = MobEffectUtil.formatDuration(mobEffectInstance, 1.0F);
            this.font.drawShadow(poseStack, string, (float)(i + 10 + 18), (float)(k + 6 + 10), 8355711);
        }

    }

    private Component getEffectName(MobEffectInstance mobEffectInstance) {
        MutableComponent mutableComponent = mobEffectInstance.getEffect().getDisplayName().copy();
        if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
            MutableComponent var10000 = mutableComponent.append(" ");
            int var10001 = mobEffectInstance.getAmplifier();
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        }

        return mutableComponent;
    }
}

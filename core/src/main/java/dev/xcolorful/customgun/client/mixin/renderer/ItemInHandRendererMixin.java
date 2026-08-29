/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.event.render.BeforeRenderHandEvent;
import dev.xcolorful.customgun.client.api.renderer.KeepingItemRenderer;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin implements KeepingItemRenderer {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private ItemStack mainHandItem;

    private ItemStack cgc$keepItem;
    private long cgc$keepTimeMs;
    private long cgc$keepTimestamp;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    public void cgc$onBeforeRenderHand(float frameInterp, PoseStack poseStack,
                                   SubmitNodeCollector nodeCollector,
                                   LocalPlayer player, int lightCoords,
                                   CallbackInfo ci) {
        CustomGun.getEventPoster().postCustomEvent(new BeforeRenderHandEvent(poseStack));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void cgc$cancelEquippedProgress(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (cgc$keepItem != null) {
            // 检查keep时长
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - cgc$keepTimestamp < cgc$keepTimeMs) {
                // 没超过需要keep的时长，保持keep
                this.mainHandHeight = 1.0f;
                this.oMainHandHeight = 1.0f;
                this.mainHandItem = cgc$keepItem;
                return;
            }
        }

        ItemStack gunItem = mc.player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun != null) {
            this.mainHandHeight = 1.0f;
            this.oMainHandHeight = 1.0f;
            this.mainHandItem = gunItem;
        }
    }

    // --------KeepItemRenderer--------

    @Override
    public void cgc$keep(ItemStack itemStack, long timeMs) {
        long time = System.currentTimeMillis() - this.cgc$keepTimestamp;
        if (time < this.cgc$keepTimeMs) {
            return;
        }
        this.cgc$keepTimeMs = timeMs;
        this.cgc$keepTimestamp = System.currentTimeMillis();
        this.cgc$keepItem = itemStack;
        this.mainHandItem = itemStack;
    }

    @Override
    public ItemStack cgc$getCurrentItem() {
        if (Minecraft.getInstance().player == null) {
            return this.mainHandItem;
        }

        if (this.cgc$keepItem != null) {
            long time = System.currentTimeMillis() - this.cgc$keepTimestamp;
            if (time < this.cgc$keepTimeMs) {
                return this.cgc$keepItem;
            }

            this.cgc$keepItem = null;
        }
        return this.mainHandItem;
    }
}

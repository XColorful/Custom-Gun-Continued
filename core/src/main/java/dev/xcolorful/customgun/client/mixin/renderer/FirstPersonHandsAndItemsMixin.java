/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.mixin.renderer;

import dev.xcolorful.customgun.client.api.renderer.KeepingItemRenderer;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/*
为了跨版本提前知道26.3neoforge的移植方式，添加此类作为占位符
 */
/**
 * <ul>
 *     <li>26.2 的 {@code net.minecraft.client.renderer.ItemInHandRenderer} 在 26.3 被{@code FirstPersonHandsAndItems}（玩家持有的手部/物品状态）+ FirstPersonHandsAndItemsRenderer（渲染器）取代</li>
 *     <li>原本 {@link ItemInHandRendererMixin} 注入的字段与 {@code tick()} 都搬到了这个状态类上</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.3")
@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin implements KeepingItemRenderer {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private ItemStack mainHandItem;

    private ItemStack cgc$keepItem;
    private long cgc$keepTimeMs;
    private long cgc$keepTimestamp;

    @Inject(method = "tick", at = @At("HEAD"))
    public void cgc$cancelEquippedProgress(LocalPlayer player,
                                           CallbackInfo ci) {
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

        ItemStack gunItem = player.getMainHandItem();
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

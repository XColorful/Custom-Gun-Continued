/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.mixin.gui;

import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.client.api.entity.LocalShooterProperty;

@Mixin(AbstractButton.class)
public class AbstractButtonMixin {

    /**
     * 记录点击按钮的时间，后续方便给予射击冷却，防止点击按钮后误触开火
     */
    @Inject(method = "onClick(DD)V", at = @At("HEAD"))
    public void onClickHead(double mouseX, double mouseY,
                            CallbackInfo ci) {
        LocalShooterProperty.clientClickButtonTimestamp = System.currentTimeMillis();
    }
}

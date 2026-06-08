/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.neoforgeclient.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiao.customgun.CustomGun;
import xiao.customgun.core.item.ammo.AmmoItem;

@Mixin(AmmoItem.class)
public class AmmoItemMixin {

    // 我 注 入 我 自 己 (悲
    @ModifyReturnValue(
            method = "test(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("RETURN"),
            remap = false
    )
    private int modify(int original) {
        CustomGun.LOGGER.info("ModifyReturnValue OK");
        return 999;
    }
}

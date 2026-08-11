/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.compat.playeranimator;

import dev.xcolorful.customgun.client.api.event.IAddClientReloadListenerEvent;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import net.minecraft.world.entity.LivingEntity;

public class PlayerAnimatorCompat {

    public static void init() {
        // mixin注入点
    }

    public static void registerReloadListener(IAddClientReloadListenerEvent event) {
        // mixin注入点
    }


    public static void stopAllAnimation(LivingEntity livingEntity) {
        // mixin注入点
    }


    /**
     * @return 是否接管
     */
    public static boolean playAnimation(LivingEntity livingEntity,
                                     GunDisplayInstance display,
                                     float limbSwingAmount) {
        // mixin注入点
        return false;
    }

    // --------Deprecated--------

    /**
     * 检测逻辑合并至 {@link #playAnimation}
     */
    @Deprecated(forRemoval = true)
    public static boolean hasPlayerAnimator3rd(LivingEntity livingEntity, GunDisplayInstance display) {
        return false;
    }
}

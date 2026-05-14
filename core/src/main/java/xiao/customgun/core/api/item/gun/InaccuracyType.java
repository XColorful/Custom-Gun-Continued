/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.core.api.entity.ShootState;

import java.util.Map;

/**
 * 移步至 {@link ShootState}
 */
@Deprecated(forRemoval = true)
public enum InaccuracyType {
    NONE;
    public static InaccuracyType getInaccuracyType(LivingEntity livingEntity) {
        return null;
    }
    public static Map<InaccuracyType, Float> getDefaultInaccuracy() {
        return null;
    }
    private static boolean isMove(LivingEntity livingEntity) {
        return false;
    }
}

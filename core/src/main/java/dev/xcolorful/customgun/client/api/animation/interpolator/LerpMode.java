/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.interpolator;

import dev.xcolorful.customgun.core.api.animation.interpolator.LerpModeTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum LerpMode implements ResourceTag.ConstantTag {
    /**
     * 线性插值
     * <ul>
     *     <li>按固定比例在两个值之间进行平滑过渡</li>
     * </ul>
     */
    LINEAR(LerpModeTag.LINEAR),
    /**
     * 球面线性插值
     * <ul>
     *     <li>通过球面路径计算旋转过渡</li>
     * </ul>
     */
    SPHERICAL_LINEAR(LerpModeTag.SPHERICAL_LINEAR),
    /**
     * Catmull-Rom 样条插值
     * <ul>
     *     <li>通过样条曲线生成平滑过渡效果</li>
     * </ul>
     */
    CATMULL_ROM(LerpModeTag.CATMULL_ROM),
    /**
     * 球面四元数样条插值
     * <ul>
     *     <li>通过球面四元数样条实现旋转平滑过渡</li>
     * </ul>
     */
    SPHERICAL_SQUAD(LerpModeTag.SPHERICAL_SQUAD);

    public final String tagName;
    public final String constantName;
    LerpMode(String name) {
        this.tagName = name;
        this.constantName = name.toUpperCase(Locale.ENGLISH);
    }
    @Override public String getTagName() {
        return this.tagName;
    }
    @Override public String getConstantName() {
        return this.constantName;
    }

    private static final Map<String, LerpMode> LERP_MODES = new HashMap<>();

    static {
        for (LerpMode mode : values()) {
            LERP_MODES.put(mode.tagName, mode);
            LERP_MODES.put(mode.constantName, mode);
        }
    }

    public static @Nullable LerpMode fromString(String name) {
        return name != null ? LERP_MODES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.tagName;
    }
}

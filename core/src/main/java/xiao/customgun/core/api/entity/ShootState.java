/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum ShootState implements ResourceTag.CategoryTag {
    /**
     * 站立不动
     */
    STAND(ShootStateTag.STAND),
    /**
     * 跑打
     */
    MOVE(ShootStateTag.MOVE),
    /**
     * 潜行 (半蹲)
     */
    SNEAK(ShootStateTag.SNEAK),
    /**
     * 趴姿 (游泳)
     */
    PRONE(ShootStateTag.PRONE, ShootStateTag.PRONE_OLD1),
    /**
     * 瞄准状态
     */
    AIM(ShootStateTag.AIM),
    /**
     * 悬空
     */
    LEVITATE(ShootStateTag.LEVITATE);

    public final String stateName;
    private final String stateNameOld;
    ShootState(String name) {
        this(name, null);
    }
    ShootState(String name, String nameOld) {
        this.stateName = name;
        this.stateNameOld = nameOld;
    }

    @Override public String getTagName() {
        return this.stateName;
    }
    @Override public String getCategoryName() {
        return this.stateName;
    }

    private static final Map<String, ShootState> SHOOT_STATES = new HashMap<>();

    static {
        for (ShootState state : values()) {
            SHOOT_STATES.put(state.stateName, state);
            if (state.stateNameOld != null) SHOOT_STATES.put(state.stateNameOld, state);
        }
    }

    public static @Nullable ShootState fromString(String name) {
        return name != null ? SHOOT_STATES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.stateName;
    }
}
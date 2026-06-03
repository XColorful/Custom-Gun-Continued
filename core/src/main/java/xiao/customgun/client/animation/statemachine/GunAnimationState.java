/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.animation.statemachine;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.animation.statemachine.GunAnimationStateTag;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum GunAnimationState implements ResourceTag.ConstantTag {
    INPUT_BOLT(GunAnimationStateTag.INPUT_BOLT),
    INPUT_DRAW(GunAnimationStateTag.INPUT_DRAW),
    INPUT_PUT_AWAY(GunAnimationStateTag.INPUT_PUT_AWAY),
    INPUT_FIRE_SELECT(GunAnimationStateTag.INPUT_FIRE_SELECT),
    INPUT_INSPECT(GunAnimationStateTag.INPUT_INSPECT),
    INPUT_BAYONET_MUZZLE(GunAnimationStateTag.INPUT_BAYONET_MUZZLE),
    INPUT_BAYONET_STOCK(GunAnimationStateTag.INPUT_BAYONET_STOCK),
    INPUT_BAYONET_PUSH(GunAnimationStateTag.INPUT_BAYONET_PUSH),
    INPUT_RELOAD(GunAnimationStateTag.INPUT_RELOAD),
    INPUT_CANCEL_RELOAD(GunAnimationStateTag.INPUT_CANCEL_RELOAD),
    INPUT_SHOOT(GunAnimationStateTag.INPUT_SHOOT),
    INPUT_WALK(GunAnimationStateTag.INPUT_WALK),
    INPUT_RUN(GunAnimationStateTag.INPUT_RUN),
    INPUT_IDLE(GunAnimationStateTag.INPUT_IDLE);

    public final String typeName;
    GunAnimationState(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getConstantName() {
        return this.typeName;
    }

    private static final Map<String, GunAnimationState> ANIMATION_STATES = new HashMap<>();

    static {
        for (GunAnimationState state : values()) {
            ANIMATION_STATES.put(state.typeName, state);
        }
    }

    public static @Nullable GunAnimationState fromString(String name) {
        return name != null ? ANIMATION_STATES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
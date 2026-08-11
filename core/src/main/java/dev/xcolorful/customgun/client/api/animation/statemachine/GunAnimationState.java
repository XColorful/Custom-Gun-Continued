/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.statemachine;

import dev.xcolorful.customgun.core.api.animation.statemachine.GunAnimationStateTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum GunAnimationState implements ResourceTag.ConstantTag {
    INPUT_BOLT(GunAnimationStateTag.INPUT_BOLT, null),
    INPUT_DRAW(GunAnimationStateTag.INPUT_DRAW, null),
    INPUT_PUT_AWAY(GunAnimationStateTag.INPUT_PUT_AWAY, null),
    INPUT_SWITCH_FIRE_MODE(GunAnimationStateTag.INPUT_SWITCH_FIRE_MODE, GunAnimationStateTag.INPUT_SWITCH_FIRE_MODE_OLD1),
    INPUT_INSPECT(GunAnimationStateTag.INPUT_INSPECT, null),
    INPUT_BAYONET_MUZZLE(GunAnimationStateTag.INPUT_BAYONET_MUZZLE, null),
    INPUT_BAYONET_STOCK(GunAnimationStateTag.INPUT_BAYONET_STOCK, null),
    INPUT_BAYONET_PUSH(GunAnimationStateTag.INPUT_BAYONET_PUSH, null),
    INPUT_RELOAD(GunAnimationStateTag.INPUT_RELOAD, null),
    INPUT_CANCEL_RELOAD(GunAnimationStateTag.INPUT_CANCEL_RELOAD, null),
    INPUT_SHOOT(GunAnimationStateTag.INPUT_SHOOT, null),
    INPUT_WALK(GunAnimationStateTag.INPUT_WALK, null),
    INPUT_RUN(GunAnimationStateTag.INPUT_RUN, null),
    INPUT_IDLE(GunAnimationStateTag.INPUT_IDLE, null);

    public final String typeName;
    public final String typeNameOld;
    GunAnimationState(String name, String nameOld) {
        this.typeName = name;
        this.typeNameOld = nameOld;
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
            if (state.typeNameOld != null) ANIMATION_STATES.put(state.typeNameOld, state);
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
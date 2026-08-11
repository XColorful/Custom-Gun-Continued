/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.controller;

import dev.xcolorful.customgun.client.api.animation.AnimationPlayType;

public class AnimPlan {
    public String animationName;
    public AnimationPlayType playType;
    public float transitionTimeS;

    public AnimPlan(String animationName, AnimationPlayType playType, float transitionTimeS) {
        this.animationName = animationName;
        this.playType = playType;
        this.transitionTimeS = transitionTimeS;
    }
}
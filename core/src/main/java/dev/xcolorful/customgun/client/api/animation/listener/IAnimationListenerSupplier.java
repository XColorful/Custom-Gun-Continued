/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.listener;

import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;

import javax.annotation.Nullable;

public interface IAnimationListenerSupplier {

    @Nullable
    IAnimationListener supplyListeners(String nodeName,
                                       AnimationChannelType type);
}

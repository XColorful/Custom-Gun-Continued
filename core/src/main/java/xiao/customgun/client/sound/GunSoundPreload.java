/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.sound;

import xiao.customgun.client.api.sound.gun.GunSoundType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GunSoundPreload {
    public static final List<String> DEFAULT_PRELOAD_NAMES = Arrays.stream(GunSoundType.values())
            .filter(type -> type.preload)
            .map(GunSoundType::getTagName)
            .collect(Collectors.toList());

    private GunSoundPreload() {
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.assets.display.GunDisplay;

import java.util.Map;

/**
 * 经过处理和校验的枪械显示数据
 */
public class GunDisplayInstance {
    private GunDisplay gunDisplay;
    private Map<GunSoundType, ResourceLocation> sounds = Maps.newHashMap();

    public @Nullable ResourceLocation getSounds(GunSoundType gunSoundType) {
        return sounds.get(gunSoundType);
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.instance.assets;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.assets.display.GunDisplay;
import xiao.customgun.core.resource.instance.PojoInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * 经过处理和校验的枪械显示数据
 */
public final class GunDisplayInstance extends PojoInstance<GunDisplay> {

    private Map<GunSoundType, Identifier> sounds = new HashMap<>();

    private GunDisplayInstance(@NotNull GunDisplay pojo) {
        super(pojo);
    }

    public static @Nullable GunDisplayInstance fromPojo(GunDisplay pojo) {
        if (pojo == null) return null;
        GunDisplayInstance instance = new GunDisplayInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        return true;
    }

    public @Nullable Identifier getSounds(GunSoundType gunSoundType) {
        return sounds.get(gunSoundType);
    }
}

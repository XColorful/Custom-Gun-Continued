/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.renderer.item;

import net.minecraft.client.Minecraft;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;

public class AnimateGeoItemRenderer {

    public void triggerDraw() {
        GunDisplayInstance gunDisplayInstance = null;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.DRAW_SOUND),
                Minecraft.getInstance().player);
    }

    public void triggerPutAway() {
        GunDisplayInstance gunDisplayInstance = null;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.PUT_AWAY_SOUND),
                Minecraft.getInstance().player);
    }
}

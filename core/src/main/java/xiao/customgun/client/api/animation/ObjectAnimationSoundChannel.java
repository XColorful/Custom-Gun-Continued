/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.animation;

import net.minecraft.world.entity.Entity;
import xiao.customgun.client.sound.SoundPlayManager;

// TODO
public class ObjectAnimationSoundChannel {

    public void playSound(double fromTimeS, double toTimeS, Entity entity, int soundDistance, float volume, float pitch) {
        // TODO

        SoundPlayManager.get().playAnimationSound(null,
                volume, pitch,
                entity, soundDistance);
    }
}

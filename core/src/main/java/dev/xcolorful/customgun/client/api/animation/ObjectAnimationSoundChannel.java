/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation;

import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import net.minecraft.world.entity.Entity;

// TODO
public class ObjectAnimationSoundChannel {

    public void playSound(double fromTimeS, double toTimeS, Entity entity, int soundDistance, float volume, float pitch) {
        // TODO

        SoundPlayManager.get().playAnimationSound(null,
                volume, pitch,
                entity, soundDistance);
    }
}

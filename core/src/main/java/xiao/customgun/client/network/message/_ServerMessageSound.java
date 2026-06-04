/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.config.SoundConfig;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.client.util.ClientWorldUtils;
import xiao.customgun.core.network.message.ServerMessageSound;

@ApiStatus.Internal
public class _ServerMessageSound {

    public static void playSound(ServerMessageSound message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        LivingEntity livingEntity = ClientWorldUtils.getLivingEntityById(level, message.entityId());
        if (livingEntity == null) return;

        var gunLocation = message.gunId();
        var gunDisplayLocation = message.gunDisplayId();
        GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunDisplayLocation, gunLocation);
        if (gunDisplayInstance == null) return;

        GunSoundType soundType = GunSoundType.fromString(message.soundName());
        var soundLocation = gunDisplayInstance.getGunSound(soundType);
        if (soundLocation == null) return;

//        switch (soundType) {
//            case SHOOT_SOUND, SHOOT_3P_SOUND -> {
//                SoundPlayManager.get().playClientSound(soundLocation,
//                        message.volume(), message.pitch(),
//                        livingEntity, false,
//                        message.distance(),
//                        true, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get());
//            }
//            default -> {
//                SoundPlayManager.get().playClientSound(soundLocation,
//                        message.volume(), message.pitch(),
//                        livingEntity, false,
//                        message.distance(),
//                        true, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get());
//            }
//        }
        // ↑豪到我了，就为了一个没实装的mono(单声道)?
        SoundPlayManager.get().playClientSound(soundLocation,
                message.volume(), message.pitch(),
                livingEntity, false,
                message.distance(),
                true, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get());
    }
}

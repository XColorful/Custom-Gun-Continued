/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.resource.assets.display;

import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.display.SoundTag;

public enum SoundType implements ResourceTag {
    // 射击
    SHOOT_SOUND(SoundTag.SHOOT_SOUND, true),
    SHOOT_3P_SOUND(SoundTag.SHOOT_3P_SOUND, true),
    SILENCE_SOUND(SoundTag.SILENCE_SOUND, true),
    SILENCE_3P_SOUND(SoundTag.SILENCE_3P_SOUND, true),
    // 近战
    MELEE_BAYONET(SoundTag.MELEE_BAYONET, false),
    MELEE_PUSH(SoundTag.MELEE_PUSH, false),
    MELEE_STOCK(SoundTag.MELEE_STOCK, false),
    // 动作
    DRY_FIRE_SOUND(SoundTag.DRY_FIRE_SOUND, true),
    RELOAD_EMPTY_SOUND(SoundTag.RELOAD_EMPTY_SOUND, true),
    RELOAD_TACTICAL_SOUND(SoundTag.RELOAD_TACTICAL_SOUND, true),
    INSPECT_EMPTY_SOUND(SoundTag.INSPECT_EMPTY_SOUND, true),
    INSPECT_SOUND(SoundTag.INSPECT_SOUND, true),
    DRAW_SOUND(SoundTag.DRAW_SOUND, true),
    PUT_AWAY_SOUND(SoundTag.PUT_AWAY_SOUND, true),
    BOLT_SOUND(SoundTag.BOLT_SOUND, true),
    FIRE_SELECT(SoundTag.FIRE_SELECT, true),
    // 反馈
    HEAD_HIT_SOUND(SoundTag.HEAD_HIT_SOUND, false),
    FLESH_HIT_SOUND(SoundTag.FLESH_HIT_SOUND, false),
    KILL_SOUND(SoundTag.KILL_SOUND, false),
    // 配件
    UNINSTALL_SOUND(SoundTag.UNINSTALL_SOUND, false),
    INSTALL_SOUND(SoundTag.INSTALL_SOUND, false);

    public final String soundTag;
    public final boolean preload;

    SoundType(String soundTag, boolean preload) {
        this.soundTag = soundTag;
        this.preload = preload;
    }

    @Override public String getTagName() {
        return soundTag;
    }

    public String getSoundName() {
        return name();
    }
}
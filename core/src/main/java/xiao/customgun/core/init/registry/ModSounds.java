/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.init.registry;

import net.minecraft.sounds.SoundEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;

public class ModSounds {
    public static final IRegistrar<SoundEvent> SOUNDS =
            CustomGun.getRegistrarFactory().createSounds(CustomGun.MOD_ID);

    // 类加载顺序会保证在 ↑调用SOUNDS 前执行
    public static final IRegistryObject<SoundEvent> GUN = SOUNDS.register("gun", () ->
            SoundEvent.createVariableRangeEvent(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "gun"))));
    public static final IRegistryObject<SoundEvent> TARGET_HIT = SOUNDS.register("target_block_hit", () ->
            SoundEvent.createVariableRangeEvent(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "target_block_hit"))));
}
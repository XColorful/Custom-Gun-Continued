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


    /**
     * TODO 等mixin做完之后给每个 {@link xiao.customgun.client.api.sound.gun.GunSoundType} 都做一个注册类型，用ResourceLocation path前缀检测来生成对应的声音事件
     */
    public static final IRegistryObject<SoundEvent> GUN = SOUNDS.register("gun", () ->
            SoundEvent.createVariableRangeEvent(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "gun"))));
    public static final IRegistryObject<SoundEvent> TARGET_HIT = SOUNDS.register("target_block_hit", () ->
            SoundEvent.createVariableRangeEvent(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "target_block_hit"))));
}
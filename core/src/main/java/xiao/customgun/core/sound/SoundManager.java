/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.core.api.sound.attachment.AttachmentSoundTypeTag;
import xiao.customgun.core.api.sound.gun.GunSoundTypeTag;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.network.message.ServerMessageSound;
import xiao.customgun.core.util.SendUtils;
import xiao.customgun.core.util.WorldUtils;

public class SoundManager {
    /**
     * 射击音效，自己能听见
     */
    public static final String SHOOT_SOUND = GunSoundTypeTag.SHOOT_SOUND;
    /**
     * 其他玩家听到的枪声
     */
    public static final String SHOOT_3P_SOUND = GunSoundTypeTag.SHOOT_3P_SOUND;
    /**
     * 消音器音效
     */
    public static final String SILENCE_SOUND = GunSoundTypeTag.SILENCE_SOUND;
    /**
     * 其他玩家听到的消音器枪声
     */
    public static final String SILENCE_3P_SOUND = GunSoundTypeTag.SILENCE_3P_SOUND;
    /**
     * 近战刺刀音效
     */
    public static final String MELEE_BAYONET = GunSoundTypeTag.MELEE_BAYONET;
    /**
     * 近战推人音效
     */
    public static final String MELEE_PUSH = GunSoundTypeTag.MELEE_PUSH;
    /**
     * 近战枪拖砸人音效
     */
    public static final String MELEE_STOCK = GunSoundTypeTag.MELEE_STOCK;
    /**
     * 没有子弹时，空击的声音
     */
    public static final String DRY_FIRE_SOUND = GunSoundTypeTag.DRY_FIRE_SOUND;
    /**
     * 空仓换弹声音
     */
    public static final String RELOAD_EMPTY_SOUND = GunSoundTypeTag.RELOAD_EMPTY_SOUND;
    /**
     * 战术换弹声音
     */
    public static final String RELOAD_TACTICAL_SOUND = GunSoundTypeTag.RELOAD_TACTICAL_SOUND;
    /**
     * 空仓检视声音
     */
    public static final String INSPECT_EMPTY_SOUND = GunSoundTypeTag.INSPECT_EMPTY_SOUND;
    /**
     * 普通检视声音
     */
    public static final String INSPECT_SOUND = GunSoundTypeTag.INSPECT_SOUND;
    /**
     * 切枪切入声音
     */
    public static final String DRAW_SOUND = GunSoundTypeTag.DRAW_SOUND;
    /**
     * 切枪切出的声音
     */
    public static final String PUT_AWAY_SOUND = GunSoundTypeTag.PUT_AWAY_SOUND;
    /**
     * 拉栓声音
     */
    public static final String BOLT_SOUND = GunSoundTypeTag.BOLT_SOUND;
    /**
     * 切换开关模式的声音
     */
    public static final String FIRE_SELECT = GunSoundTypeTag.SWITCH_FIRE_MODE;
    /**
     * 爆头击中声音
     */
    public static final String HEAD_HIT_SOUND = GunSoundTypeTag.HEAD_HIT_SOUND;
    /**
     * 普通击中声音
     */
    public static final String FLESH_HIT_SOUND = GunSoundTypeTag.FLESH_HIT_SOUND;
    /**
     * 击杀的声音
     */
    public static final String KILL_SOUND = GunSoundTypeTag.KILL_SOUND;
    /**
     * 卸载配件的声音，用于配件的
     */
    public static final String UNINSTALL_SOUND = AttachmentSoundTypeTag.UNINSTALL_SOUND;
    /**
     * 装载配件的声音，用于配件的
     */
    public static final String INSTALL_SOUND = AttachmentSoundTypeTag.INSTALL_SOUND;

    public static void sendSoundToNearby(LivingEntity sourceEntity, int distance,
                                         Identifier gunId, Identifier gunDisplayId,
                                         String soundName, float volume, float pitch) {
        if (PlannedRefactor.ON_SEND_SOUND_MESSAGE) return;
        if (sourceEntity.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = sourceEntity.blockPosition();
            ServerMessageSound soundMessage = new ServerMessageSound(sourceEntity.getId(), gunId, gunDisplayId, soundName, volume, pitch, distance);
            serverLevel.getChunkSource().chunkMap.getPlayers(WorldUtils.chunkPos(pos), false).stream()
                    .filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < distance * distance)
                    .filter(p -> p.getId() != sourceEntity.getId())
                    .forEach(p -> SendUtils.sendMessageToPlayer(p, soundMessage));
        }
    }
}
/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterReloadEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.network.message.ClientMessagePlayerCancelReload;
import xiao.customgun.core.network.message.ClientMessagePlayerReloadGun;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterReload extends LocalShooterAspect {

    public LocalShooterReload(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void cancelReload() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        ReloadState reloadState = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter).cgc$getSynReloadState();
        if (!reloadState.getStateType().isReloading()) return;

        SendUtils.sendMessageToServer(new ClientMessagePlayerCancelReload());
        this.doCancelReload(gunDisplayInstance);
    }
    private void doCancelReload(GunDisplayInstance gunDisplayInstance) {
        // TODO GunDisplayInstance AnimationStateMachine
    }

    public void reload() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        if (clientGunIndexInstance == null) return;
        GunData gunData = clientGunIndexInstance.getGunData();
        if (gunData == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        // 检查状态锁
        if (this.localShooterProperty.clientStateLock) return;

        if (
                // 检查是否为背包直读
                iGun.useInventoryAmmo(gunItem)
                // 射击后冷却100ms
                || System.currentTimeMillis() - this.localShooterProperty.clientShootTimestamp < RELOAD_COOLDOWN_MS
        ) return;

        // 检查弹药
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        if (iLivingShooter.cgc$needCheckAmmo() && !iGun.canReload(iGun, gunItem, iLivingShooter, this.localShooter)) return;

        // 锁上状态锁
        this.localShooterProperty.lockState(operator -> operator.cgc$getSynReloadState().getStateType().isReloading());
        this.localShooterProperty.chargeProgress = 0f;

        // 触发换弹事件
        if (CustomGun.getEventPoster().postCustomEvent(new ShooterReloadEvent(McLogicalSide.CLIENT,
                iLivingShooter, this.localShooter, iGun, gunItem))) {
            return;
        }

        // 发包通知服务器
        SendUtils.sendMessageToServer(new ClientMessagePlayerReloadGun());
        // 执行客户端 reload 相关内容
        this.doReload(iGun, gunItem, gunDisplayInstance, gunData, gunItem);
    }
    private void doReload(IGun iGun, ItemStack gunItem, GunDisplayInstance gunDisplayInstance, GunData gunData, ItemStack mainHandItem) {
        if (false) {
            return;
        }

        BoltType boltType = gunData.getBoltType();
        boolean hasAmmo = boltType == BoltType.OPEN_BOLT ? iGun.getMagAmmoCount(gunItem) > 0
                : iGun.hasBarrelAmmo(gunItem);
        // 触发 reload，停止播放声音
        SoundPlayManager.get().stopCurrentSound();
        var soundLocation = !hasAmmo ? GunSoundType.RELOAD_EMPTY_SOUND : GunSoundType.RELOAD_TACTICAL_SOUND;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(soundLocation),
                this.localShooter);
        // TODO AnimationStateMachine
    }
}

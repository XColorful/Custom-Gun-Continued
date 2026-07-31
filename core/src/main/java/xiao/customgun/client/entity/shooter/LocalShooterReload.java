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
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.entity.shooter.LivingShooterReload;
import xiao.customgun.core.network.message.ClientMessagePlayerCancelReload;
import xiao.customgun.core.network.message.ClientMessagePlayerReloadGun;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterReload extends LocalShooterAspect {

    public LocalShooterReload(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    /**
     * 对齐{@link LivingShooterReload#reload()}
     */
    public void reload() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if ( // 2.1 检查状态锁
                this.localShooterProperty.clientStateLock
        ) return;
        else if ( // 2.2
                // 射击后冷却100ms
                System.currentTimeMillis() - this.localShooterProperty.clientShootTimestamp < RELOAD_COOLDOWN_MS
        ) return;

        { // 3. IGunRuntime操作结果 -> Shooter状态
            ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
            boolean canReload = iGun.canReload(iGun, gunItem, iLivingShooter, this.localShooter);
            if (!canReload) {
                return;
            }
        } { // 3.1锁上状态锁
            this.localShooterProperty.lockState(_iLivingShooter -> _iLivingShooter.cgc$getSynReloadState().getStateType().isReloading());
            this.localShooterProperty.chargeProgress = 0f;
        }

        // 发包通知服务器
        SendUtils.sendMessageToServer(new ClientMessagePlayerReloadGun());

        // 执行客户端 reload 相关内容
        this._doReload(iGun, gunItem, gunItem);
    }
    private void _doReload(IGun iGun, ItemStack gunItem, ItemStack mainHandItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        GunData gunData = gunIndexInstance.getGunData();

        if (false) {
            return;
        }

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

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

    public void cancelReload() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        else if ( // 2.2
                !ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter).cgc$getSynReloadState()
                        .getStateType().isReloading()
        ) return;

        // 3. IGunRuntime操作结果 -> Shooter状态
        // 暂无

        SendUtils.sendMessageToServer(new ClientMessagePlayerCancelReload());

        this.doCancelReload(gunItem);
    }
    private void doCancelReload(ItemStack gunItem) {
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;
        // TODO GunDisplayInstance AnimationStateMachine
    }
}

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
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.entity.shooter.ISynGunState;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.network.message.ClientMessagePlayerBoltGun;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterBolt extends LocalShooterAspect {

    public LocalShooterBolt(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void bolt() {
        // 检查状态锁
        if (this.localShooterProperty.clientStateLock) return;

        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        // 缓存近的判断前置
        if (
                // 检查是否在拉栓
                this.localShooterProperty.isBolting
                // 检查是否有弹药在枪膛内
                || iGun.hasBarrelAmmo(gunItem)
        ) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        if (clientGunIndexInstance == null) return;

        GunData gunData = clientGunIndexInstance.getGunData();
        if (gunData == null) return;

        // 检查 bolt 类型是否是 manual action
        BoltType boltType = gunData.getBoltType();
        if (boltType != BoltType.MANUAL_ACTION) return;

        // 判断没有子弹的条件 (背包直读且包内没子弹 / 非背包直读且弹匣子弹数 < 1)
        boolean useInventoryAmmo = iGun.useInventoryAmmo(gunItem); // 是否为背包直读
        boolean hasAmmo = useInventoryAmmo ? !iGun.hasInventoryAmmo(this.localShooter, gunItem)
                : iGun.getMagAmmoCount(gunItem) < 1;
        if (hasAmmo) return;

        // 锁上状态锁
        this.localShooterProperty.lockState(ISynGunState::cgc$getSynIsBolting);
        this.localShooterProperty.isBolting = true;

        SendUtils.sendMessageToServer(new ClientMessagePlayerBoltGun());
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance != null) {
            var soundLocation = gunDisplayInstance.getGunSound(GunSoundType.BOLT_SOUND);
            SoundPlayManager.get().playGunSound(soundLocation, this.localShooter);
            // TODO AnimationStateMachine trigger INPUT_BOLT
        }
    }

    public void tickAutoBolt() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.localShooterProperty.isBolting = false;
            return;
        }

        this.bolt();

        if (this.localShooterProperty.isBolting) {
            // 对于客户端来说，膛内弹药被填入的状态同步到客户端的瞬间，bolt 过程才算完全结束
            if (iGun.hasBarrelAmmo(gunItem)) {
                this.localShooterProperty.isBolting = false;
            }
        }
    }
}

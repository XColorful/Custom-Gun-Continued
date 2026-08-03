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
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import xiao.customgun.core.network.message.ClientMessagePlayerSwitchFireMode;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterSwitchFireMode extends LocalShooterAspect {

    public LocalShooterSwitchFireMode(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void switchFireMode() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if ( // 2.1 检查状态锁
                this.localShooterProperty.clientStateLock
        ) return;

        // 3. IGunRuntime操作结果 -> Shooter状态
        boolean success = iGun.switchFireMode(null, iGun, gunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter), this.localShooter);
        if (!success) return;

        SendUtils.sendMessageToServer(new ClientMessagePlayerSwitchFireMode());

        // 刷新配件缓存
        ShooterGunModifierManager.postChangeEvent(this.localShooter, gunItem);

        this._doSwitchFireMode(iGun, gunItem);
    }
    private void _doSwitchFireMode(IGun iGun, ItemStack gunItem) {
        // 播放音效
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.SWITCH_FIRE_MODE),
                this.localShooter);
        // TODO AnimationStateMachine
    }
}

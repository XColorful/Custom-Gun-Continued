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
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterSwitchFireModeEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.item.gun.GunPropertyManager;
import xiao.customgun.core.network.message.ClientMessagePlayerFireSelect;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterSwitchFireMode extends LocalShooterAspect {

    public LocalShooterSwitchFireMode(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void switchFireMode() {
        // 检查状态锁
        if (this.localShooterProperty.clientStateLock) return;

        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        if (CustomGun.getEventPoster().postCustomEvent(new ShooterSwitchFireModeEvent(McLogicalSide.CLIENT,
                iLivingShooter, this.localShooter, iGun, gunItem))) {
            return;
        }
        SendUtils.sendMessageToServer(new ClientMessagePlayerFireSelect());

        // 播放音效
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.FIRE_SELECT),
                this.localShooter);
        // 客户端切换开火模式
        iGun.switchFireMode(null, gunItem);
        GunPropertyManager.postChangeEvent(this.localShooter, gunItem);
        // TODO AnimationStateMachine
    }
}

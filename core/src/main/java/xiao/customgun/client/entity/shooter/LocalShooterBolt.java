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
import xiao.customgun.core.api.entity.shooter.ISynGunState;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.entity.shooter.LivingShooterBolt;
import xiao.customgun.core.network.message.ClientMessagePlayerBoltGun;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterBolt extends LocalShooterAspect {

    public LocalShooterBolt(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    /**
     * 对齐{@link LivingShooterBolt#bolt()}
     */
    public void bolt() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if (
                // 检查状态锁
                this.localShooterProperty.clientStateLock
                // 检查是否在拉栓
                || this.localShooterProperty.isBolting
        ) return;

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

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.entity.shooter;

import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.entity.shooter.ISynGunState;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterBolt;
import dev.xcolorful.customgun.core.network.message.ClientMessagePlayerBoltGun;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class LocalShooterBolt extends LocalShooterAspect {

    public LocalShooterBolt(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    /**
     * 对齐{@link LivingShooterBolt#bolt()}
     */
    public void bolt() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if ( // 2.1 检查状态锁
                this.localShooterProperty.clientStateLock) return;
        else if ( // 2.2 检查状态
                // 检查是否在拉栓
                this.localShooterProperty.isBolting
        ) return;

        { // 3. IGunRuntime操作结果 -> Shooter状态
            this.localShooterProperty.isBolting = true;
        } { // 3.1 锁上状态锁
            this.localShooterProperty.lockState(ISynGunState::cgc$getSynIsBolting);
        }

        SendUtils.sendMessageToServer(new ClientMessagePlayerBoltGun());
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance != null) {
            var soundLocation = gunDisplayInstance.getGunSound(GunSoundType.BOLT_SOUND);
            SoundPlayManager.get().playGunSound(soundLocation, this.localShooter);
            // TODO AnimationStateMachine trigger INPUT_BOLT
        }
    }

    /**
     * // TODO 加一个手动拉栓的按键
     * 能拉栓的时候自动拉栓
     * 避免多加一个按键 或者 嵌入到shoot的逻辑里
     */
    public void tickAutoBolt() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.localShooterProperty.isBolting = false;
            return;
        }

        // TODO ↓这个太简单粗暴了
        this.bolt();

        if (this.localShooterProperty.isBolting) {
            // 对于客户端来说，膛内弹药被填入的状态同步到客户端的瞬间，bolt 过程才算完全结束
            if (iGun.hasBarrelAmmo(gunItem)) {
                this.localShooterProperty.isBolting = false;
            }
        }
    }
}

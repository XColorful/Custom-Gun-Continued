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
import xiao.customgun.client.animation.statemachine.GunAnimationState;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.gun.attack.IGunAttackRuntime;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.network.message.ClientMessagePlayerMelee;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterMelee extends LocalShooterAspect {

    public LocalShooterMelee(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void prepareMelee() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if ( // 2.1 检查状态锁
                this.localShooterProperty.clientStateLock
        ) return;

        @Nullable IGunAttackRuntime.MeleePreparation meleePreparation;
        { // 3. IGunRuntime操作结果 -> Shooter状态
            ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
            meleePreparation = iGun.prepareMelee(iGun, gunItem, iLivingShooter, this.localShooter);
            if (meleePreparation == null) {
                return;
            }
        } { // 3.1 锁上状态锁
            this.localShooterProperty.lockState(_iLivingShooter -> _iLivingShooter.cgc$getSynMeleeCooldown() > 0);
        }

        switch (meleePreparation.meleeType()) {
            case BAYONET -> _doMelee(iGun, gunItem, GunSoundType.MELEE_BAYONET, GunAnimationState.INPUT_BAYONET_MUZZLE);
            case STOCK -> _doMelee(iGun, gunItem, GunSoundType.MELEE_STOCK, GunAnimationState.INPUT_BAYONET_STOCK);
            case PUSH -> _doMelee(iGun, gunItem, GunSoundType.MELEE_PUSH, GunAnimationState.INPUT_BAYONET_PUSH);
            // 增加类型使此处强制编译不通过
        }
    }
    private void _doMelee(IGun iGun, ItemStack gunItem,
                          GunSoundType gunSoundType, GunAnimationState gunAnimationState) {
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(gunSoundType),
                this.localShooter);
        // 发送执行近战的数据包，通知服务器
        SendUtils.sendMessageToServer(new ClientMessagePlayerMelee());
        // 动画状态机转移状态
        // TODO GunDisplayInstance AnimationStateMachine
    }
}

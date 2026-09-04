/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.entity.shooter;

import dev.xcolorful.customgun.client.animation.statemachine.GunAnimStateContext;
import dev.xcolorful.customgun.client.animation.statemachine.LuaAnimStateMachine;
import dev.xcolorful.customgun.client.api.animation.statemachine.GunAnimationState;
import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import dev.xcolorful.customgun.core.network.message.ClientMessagePlayerSwitchFireMode;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class LocalShooterSwitchFireMode extends LocalShooterAspect {

    public LocalShooterSwitchFireMode(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void switchFireMode() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
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

        // 动画状态机转移状态
        LuaAnimStateMachine<GunAnimStateContext> animStateMachine = gunDisplayInstance.getAnimStateMachine();
        this._triggerSwitchFireMode(animStateMachine);
    }
    /**
     * <ul>
     *     同时触发新旧两种输入来兼容旧脚本
     *     <li>两者是同一逻辑输入的别名，对只识别其中一种的脚本而言，另一种输入只会走到 transition 的 default 分支并返回 nil</li>
     *     <li>要是有脚本会刻意记录无效调用（有副作用），已经拆成单独的函数方便 Mixin 了 (需要额外维护当前是哪个状态机脚本）</li>
     * </ul>
     */
    private void _triggerSwitchFireMode(LuaAnimStateMachine<GunAnimStateContext> animStateMachine) {
        animStateMachine.trigger(GunAnimationState.INPUT_SWITCH_FIRE_MODE.typeNameOld);
        animStateMachine.trigger(GunAnimationState.INPUT_SWITCH_FIRE_MODE.getConstantName());
    }
}

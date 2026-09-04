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
import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class LocalShooterInspect extends LocalShooterAspect {

    public LocalShooterInspect(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void inspect() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            @Nullable IAnimateGeoItemRenderer<?, ?> renderer = IAnimateGeoItem.cgc$getCustomRenderer(gunItem);
            if (renderer != null) {
                renderer.triggerAnimation(gunItem, GunAnimationState.INPUT_INSPECT.getConstantName());
            }
            return;
        }

        // 检查状态锁
        if (this.localShooterProperty.clientStateLock) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;
        GunData gunData = gunIndexInstance.getGunData();

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        BoltType boltType = gunData.getBoltType();
        boolean hasInspectAmmo = boltType.useBarrelAmmo() ? iGun.hasBarrelAmmo(gunItem)
                : iGun.getMagAmmoCount(gunItem) > 0;

        // 触发 inspect，停止播放声音
        SoundPlayManager.get().stopMainTrackSound();
        var soundLocation = gunDisplayInstance.getGunSound(!hasInspectAmmo ? GunSoundType.INSPECT_EMPTY_SOUND : GunSoundType.INSPECT_SOUND);
        SoundPlayManager.get().playGunSound(soundLocation,
                this.localShooter);

        LuaAnimStateMachine<GunAnimStateContext> animStateMachine = gunDisplayInstance.getAnimStateMachine();
        animStateMachine.trigger(GunAnimationState.INPUT_INSPECT.getConstantName());
    }
}

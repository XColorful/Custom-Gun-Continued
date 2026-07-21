/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.network.message.ClientMessagePlayerProne;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterProne extends LocalShooterAspect {

    private static final int COOLDOWN_TICKS = 10;
    private boolean isProne = false;
    private int crawCooldownTicks = 0;

    public LocalShooterProne(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public boolean isProne() {
        return this.isProne;
    }

    public void prone(boolean isProne) {
        if (proneNotAvailable()) {
            this.isProne = false;
            return;
        }

        // 冷却时间没到
        if (this.crawCooldownTicks > 0) return;
        else this.crawCooldownTicks = COOLDOWN_TICKS;

        this.isProne = isProne;
        SendUtils.sendMessageToServer(new ClientMessagePlayerProne(isProne));
    }

    public void tickProne() {
        if (this.crawCooldownTicks > 0) this.crawCooldownTicks--;

        if (proneNotAvailable()) {
            this.isProne = false;
            return;
        }

        this.setPronePose();
    }

    private boolean proneNotAvailable() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            return false;
        }

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        if (clientGunIndexInstance == null) {
            return false;
        }

        GunData gunData = clientGunIndexInstance.getGunData();
        if (gunData == null
                || !gunData.getEnableProne() // 不允许趴下的武器
                || !this.localShooter.onGround() // 悬空
                || this.localShooter.isSwimming() // 游泳
                || this.localShooter.isSpectator() // 旁观模式
        ) {
            return false;
        }

        return true;
    }

    private void setPronePose() {
        if (this.isProne) {
            this.localShooter.setForcedPose(Pose.SWIMMING);
        } else {
            this.localShooter.setForcedPose(null);
        }
    }
}

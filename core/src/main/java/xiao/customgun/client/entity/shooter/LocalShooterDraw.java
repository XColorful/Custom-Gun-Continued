/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;

public final class LocalShooterDraw extends LocalShooterAspect {

    /**
     * 原版这个字段就没true过
     */
    private boolean readyToDraw = false;

    public LocalShooterDraw(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }


    public void draw(ItemStack lastItem) {
        // TODO
    }

    private void doPutAway() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        GunDisplayInstance gunDisplayInstance = null;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.PUT_AWAY_SOUND),
                localPlayer);
    }

    public boolean isReadyToDraw() {
        return this.readyToDraw;
    }
    public void setReadyToDraw(boolean readyToDraw) {
        this.readyToDraw = readyToDraw;
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;

public class AnimateGeoItemRenderer {

    public void triggerDraw() {
        GunDisplayInstance gunDisplayInstance = null;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.DRAW_SOUND),
                Minecraft.getInstance().player);
    }

    public void triggerPutAway() {
        GunDisplayInstance gunDisplayInstance = null;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.PUT_AWAY_SOUND),
                Minecraft.getInstance().player);
    }

    /**
     * 渲染第一人称，暂时只用于玩家
     */
    public void renderFirstPerson(LocalPlayer player, ItemStack stack,
                                  ItemDisplayContext ctx,
                                  PoseStack poseStack,
                                  MultiBufferSource bufferSource,
                                  int light, float partialTick) {
    }
}

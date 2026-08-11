/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.shooter;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.gun._SurroundDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.util.InventoryUtils;
import dev.xcolorful.customgun.core.util.MathUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class HumanoidOffhandRender {

    public static <S extends ArmedEntityRenderState> void renderGun(S renderState,
                                                                    PoseStack matrixStack,
                                                                    SubmitNodeCollector submitNodeCollector,
                                                                    int lightCoords) {
        renderOffhandGun(matrixStack, buffer, lightCoords, entity);
        renderHotbarGun(matrixStack, buffer, lightCoords, entity);
    }

    private static void renderOffhandGun(PoseStack matrixStack,
                                         MultiBufferSource buffer,
                                         int lightCoords,
                                         LivingEntity entity) {
        ItemStack gunItem = entity.getOffhandItem();
        if (gunItem.isEmpty()) return;

        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        GunDisplay gunDisplay = gunDisplayInstance.getPojo();
        _SurroundDisplay surroundDisplay = gunDisplay.getSurroundDisplayByOffhand();
        _renderGunItem(matrixStack, buffer, lightCoords, entity, gunItem, surroundDisplay);
    }

    private static void renderHotbarGun(PoseStack matrixStack,
                                        MultiBufferSource buffer,
                                        int lightCoords,
                                        LivingEntity entity) {
        if (!(entity instanceof Player player)) return;

        Inventory inventory = player.getInventory();
        int selected = InventoryUtils.getSelectedSlot(inventory);
        for (int i = 0; i < 9; i++) {
            if (i == selected) continue;

            ItemStack gunItem = inventory.getItem(i);
            if (gunItem.isEmpty()) continue;

            @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
            if (iGun == null) continue;

            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            if (gunDisplayInstance == null) continue;

            @Nullable Int2ObjectArrayMap<_SurroundDisplay> surroundDisplayList = gunDisplayInstance.getSurroundDisplayByHotbar();
            if (surroundDisplayList == null || surroundDisplayList.isEmpty()) continue;

            if (!surroundDisplayList.containsKey(i)) continue;

            _SurroundDisplay surroundDisplay = surroundDisplayList.get(i);
            _renderGunItem(matrixStack, buffer, lightCoords, entity, gunItem, surroundDisplay);
        }
    }

    private static void _renderGunItem(PoseStack matrixStack,
                                       MultiBufferSource buffer,
                                       int lightCoords,
                                       LivingEntity entity,
                                       ItemStack gunItem,
                                       _SurroundDisplay surroundDisplay) {
        float[] pos = surroundDisplay.getPos();
        float[] rotate = surroundDisplay.getRotate();
        float[] scale = surroundDisplay.getScale();

        matrixStack.pushPose(); {
            matrixStack.translate(-pos[0] / 16f, 1.5 - pos[1] / 16f, pos[2] / 16f);
            matrixStack.scale(-scale[0], -scale[1], scale[2]);
            Quaternionf rotation = new Quaternionf();
            MathUtil.Quaternion.set(rotation, (float) Math.toRadians(rotate[0]), (float) Math.toRadians(rotate[1]), (float) Math.toRadians(rotate[2]));
            matrixStack.mulPose(rotation);

            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            renderer.renderStatic(gunItem, ItemDisplayContext.FIXED, lightCoords, OverlayTexture.NO_OVERLAY, matrixStack, buffer, entity.level(), entity.getId());
        }
        matrixStack.popPose();
    }
}

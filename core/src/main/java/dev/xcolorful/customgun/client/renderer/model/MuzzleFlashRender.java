/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.oculus.OculusCompat;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.model.bedrock.SlotModel;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.gun._MuzzleFlashDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.gun.FireSoundType;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._MuzzleModifierData;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MuzzleFlashRender implements IModelComponentRenderer {
    public static class State {
        /**
         * 当前是否渲染自身的枪口火焰
         */
        public static boolean isSelf = false;
        private static long lastShootTimestamp = -1;
        private static boolean muzzleFlashStartMark = false;
        private static float muzzleFlashRandomRotate = 0;
        private static Matrix3f muzzleFlashNormal = new Matrix3f();
        private static Matrix4f muzzleFlashPose = new Matrix4f();
    }
    private static final SlotModel MUZZLE_FLASH_MODEL = new SlotModel(true);
    /**
     * 枪口火焰显示时长
     */
    private static final long TIME_RANGE_MS = 50;


    private final GunModelObject gunModelObject;

    public MuzzleFlashRender(GunModelObject gunModelObject) {
        this.gunModelObject = gunModelObject;
    }

    public static void onShoot() {
        // 记录开火时间戳
        State.lastShootTimestamp = System.currentTimeMillis();
        // 记录枪口火焰启动标记
        State.muzzleFlashStartMark = true;
        // 随机给予枪口火焰的旋转
        State.muzzleFlashRandomRotate = (float) (Math.random() * 360);
    }

    // --------IModelComponentRenderer--------

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        if (!State.isSelf) return;

        if (OculusCompat.isRenderShadow()) return;

        final long currentTimeMillis = System.currentTimeMillis() - State.lastShootTimestamp;
        if (currentTimeMillis > TIME_RANGE) return;

        @Nullable _MuzzleFlashDisplay muzzleFlashDisplay;
        { // 获取枪口火焰数据
            ItemStack gunItem = this.gunModelObject.getCurrentGunItem();
            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            if (gunDisplayInstance == null) return;

            GunDisplay gunDisplay = gunDisplayInstance.getPojo();
            muzzleFlashDisplay = gunDisplay.getMuzzleFlashDisplay();
            if (muzzleFlashDisplay == null) return;
        }

        ItemStack muzzleItem = this.gunModelObject.getCurrentAttachmentItem().get(AttachmentCategory.MUZZLE);
        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(muzzleItem);
        if (iAttachment == null) {
            renderMuzzleFlash(poseStack, currentTimeMillis, gunModelObject, muzzleFlashDisplay);
            return;
        }

        { // 检查是否是隐藏火焰
            var attachmentLocation = iAttachment.getAttachmentLocation(muzzleItem);
            @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(attachmentLocation);
            if (attachmentIndexInstance == null) return;

            AttachmentData attachmentData = attachmentIndexInstance.getAttachmentData();
            @Nullable _MuzzleModifierData muzzleModifierData = attachmentData.getMuzzleModifier();
            if (muzzleModifierData == null) return;

            FireSoundType fireSoundType = muzzleModifierData.getFireSoundType();
            switch (fireSoundType) {
                case NORMAL -> {}
                case SILENCED, MUTED -> { // 消音或无声武器
                    return;
                }
                // 增加类型使此处强制编译不通过
            }
        }

        this.renderMuzzleFlash(poseStack, currentTimeMillis, gunModelObject, muzzleFlashDisplay);
    }

    private void renderMuzzleFlash(PoseStack poseStack,
                                          long currentTimeMillis,
                                          GunModelObject gunModelObject,
                                          _MuzzleFlashDisplay muzzleFlashDisplay) {
        if (State.muzzleFlashStartMark) {
            State.muzzleFlashNormal = new Matrix3f(poseStack.last().normal());
            State.muzzleFlashPose = new Matrix4f(poseStack.last().pose());
        }
        gunModelObject.delegateRender((_poseStack, _vertexConsumer, _transformType, light, overlay) ->
                this.doRender(light, overlay, currentTimeMillis, muzzleFlashDisplay));
    }
    private void doRender(int light, int overlay, long currentTimeMillis, _MuzzleFlashDisplay muzzleFlashDisplay) {
        if (State.muzzleFlashNormal == null || State.muzzleFlashPose == null) return;

        float scale = 0.5f * muzzleFlashDisplay.getTextureScale();
        float scaleTime = TIME_RANGE / 2.0f;
        scale = currentTimeMillis < scaleTime ? (scale * (currentTimeMillis / scaleTime)) : scale;
        State.muzzleFlashStartMark = false;
        MultiBufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // 推送到指定位置
        PoseStack poseStack2 = new PoseStack();
        poseStack2.last().normal().mul(State.muzzleFlashNormal);
        poseStack2.last().pose().mul(State.muzzleFlashPose);

        // 先渲染一遍半透明背景
        poseStack2.pushPose(); {
            poseStack2.scale(scale, scale, scale);
            poseStack2.mulPose(Axis.ZP.rotationDegrees(State.muzzleFlashRandomRotate));
            poseStack2.translate(0, -1, 0);
            RenderType renderTypeBg = RenderType.entityTranslucent(muzzleFlashDisplay.getTextureLocation());
            MUZZLE_FLASH_MODEL.renderToBuffer(poseStack2, multiBufferSource.getBuffer(renderTypeBg), light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack2.popPose();

        // 然后渲染发光效果
        poseStack2.pushPose(); {
            poseStack2.scale(scale / 2, scale / 2, scale / 2);
            poseStack2.mulPose(Axis.ZP.rotationDegrees(State.muzzleFlashRandomRotate));
            poseStack2.translate(0, -0.9, 0);
            RenderType renderTypeLight = RenderType.energySwirl(muzzleFlashDisplay.getTextureLocation(), 1, 1);
            MUZZLE_FLASH_MODEL.renderToBuffer(poseStack2, multiBufferSource.getBuffer(renderTypeLight), light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack2.popPose();
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private static final long TIME_RANGE = TIME_RANGE_MS;
}

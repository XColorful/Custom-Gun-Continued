/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.minecraft.BlockEntityWithoutLevelRenderer;
import dev.xcolorful.customgun.client.model.AmmoModelObject;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.model.bedrock.SlotModel;
import dev.xcolorful.customgun.client.resource.assets.display.AmmoDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._ModelTransform;
import dev.xcolorful.customgun.client.resource.assets.display._ModelTransformScale;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class AmmoItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final SlotModel SLOT_AMMO_MODEL = new SlotModel();

    public AmmoItemRenderer() {
        this(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
    public AmmoItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }


    private void _applyPositioningTransform(PoseStack poseStack,
                                            ItemDisplayContext transformType,
                                            AmmoModelObject modelObject,
                                            @Nullable _ModelTransformScale scale) {
        switch (transformType) {
            case FIXED -> _applyPositioningNodeTransform(poseStack, scale != null ? scale.getFixedScale() : null, modelObject.getFixedOriginPath());
            case GROUND -> _applyPositioningNodeTransform(poseStack, scale != null ? scale.getGroundScale() : null, modelObject.getGroundOriginPath());
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND ->
                    _applyPositioningNodeTransform(poseStack, scale != null ? scale.getThirdPersonScale() : null, modelObject.getThirdPersonHandOriginPath());
        }
    }
    private static void _applyPositioningNodeTransform(PoseStack poseStack,
                                                       @Nullable float[] scale,
                                                       List<BedrockPart> nodePath) {
        if (nodePath == null) return;

        if (scale == null) {
            scale = new float[]{1, 1, 1};
        }

        // 应用定位组的反向位移、旋转，使定位组的位置就是渲染中心
        poseStack.translate(0, 1.5, 0);
        for (int i = nodePath.size() - 1; i >= 0; i--) {
            BedrockPart t = nodePath.get(i);
            poseStack.mulPose(Axis.XN.rotation(t.xRot));
            poseStack.mulPose(Axis.YN.rotation(t.yRot));
            poseStack.mulPose(Axis.ZN.rotation(t.zRot));
            if (t.getParent() != null) {
                poseStack.translate(-t.x * scale[0] / 16.0F, -t.y * scale[1] / 16.0F, -t.z * scale[2] / 16.0F);
            } else {
                poseStack.translate(-t.x * scale[0] / 16.0F, (1.5F - t.y / 16.0F) * scale[1], -t.z * scale[2] / 16.0F);
            }
        }
        poseStack.translate(0, -1.5, 0);
    }

    private void _applyScaleTransform(PoseStack poseStack,
                                      ItemDisplayContext transformType,
                                      @Nullable _ModelTransformScale scale) {
        if (scale == null) {
            return;
        }
        float[] vector3f = null;
        switch (transformType) {
            case FIXED -> vector3f = scale.getFixedScale();
            case GROUND -> vector3f = scale.getGroundScale();
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> vector3f = scale.getThirdPersonScale();
        }

        if (vector3f != null) {
            poseStack.translate(0, 1.5, 0);
            poseStack.scale(vector3f[0], vector3f[1], vector3f[2]);
            poseStack.translate(0, -1.5, 0);
        }
    }

    // --------BlockEntityWithoutLevelRenderer--------

    @Override
    public void renderByItem(@NotNull ItemStack ammoItem,
                             @NotNull ItemDisplayContext transformType,
                             @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource pBuffer,
                             int pPackedLight, int pPackedOverlay) {
        @Nullable IAmmo iAmmo = IAmmoGetter.fromItemStack(ammoItem);
        if (iAmmo == null) return;

        var ammoLocation = iAmmo.getAmmoLocation(ammoItem);
        @Nullable ClientAmmoIndexInstance ammoIndexInstance = ClientResourceApi.getClientAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance != null) {
            // 先获取 3D 模型，如果为空，统一使用 GUI 渲染
            @Nullable AmmoModelObject ammoModel = ammoIndexInstance.getAmmoModel();
            AmmoDisplay ammoDisplay = ammoIndexInstance.getAmmoDisplay();
            @Nullable var modelTextureLocation = ammoDisplay.getTextureLocation();

            poseStack.pushPose(); {
                if (transformType == ItemDisplayContext.GUI || ammoModel == null || modelTextureLocation == null) {
                    // GUI 特殊渲染
                    poseStack.translate(0.5, 1.5, 0.5);
                    poseStack.mulPose(Axis.ZN.rotationDegrees(180));

                    @Nullable var slotTextureLocation = ammoDisplay.getSlotTextureLocation();
                    if (slotTextureLocation == null) slotTextureLocation = ClientRenderUtils.getMissingTextureLocation();

                    VertexConsumer buffer = pBuffer.getBuffer(ClientRenderUtils.RenderType_.entityTranslucent(slotTextureLocation));
                    SLOT_AMMO_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    @Nullable _ModelTransform modelTransform = ammoDisplay.getModelTransform();

                    // 移动到模型原点
                    poseStack.translate(0.5, 2, 0.5);
                    // 反转模型
                    poseStack.scale(-1, -1, 1);
                    // 应用定位组的变换（位移和旋转，不包括缩放）
                    _applyPositioningTransform(poseStack, transformType, ammoModel, modelTransform != null ? modelTransform.getScale() : null);
                    // 应用 display 数据中的缩放
                    _applyScaleTransform(poseStack, transformType, modelTransform != null ? modelTransform.getScale() : null);

                    // 渲染子弹盒模型
                    RenderType renderType = ClientRenderUtils.RenderType_.entityCutout(modelTextureLocation);
                    ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector_(pBuffer);
                    try {
                        ammoModel.render(poseStack, transformType, renderType, pPackedLight, pPackedOverlay);
                    } finally {
                        ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector_(null);
                    }
                }
            }
            poseStack.popPose();
        } else {
            poseStack.pushPose(); {
                // 没有这个 ammoID，渲染个错误材质提醒别人
                poseStack.translate(0.5, 1.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));

                {
                    VertexConsumer buffer = pBuffer.getBuffer(ClientRenderUtils.RenderType_.entityTranslucent(ClientRenderUtils.getMissingTextureLocation()));
                    SLOT_AMMO_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
            poseStack.popPose();
        }
    }
}

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
import dev.xcolorful.customgun.client.model.AttachmentModelObject;
import dev.xcolorful.customgun.client.model.bedrock.SlotModel;
import dev.xcolorful.customgun.client.renderer.model.AttachmentRender;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LodDisplay;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderDistance;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttachmentItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final SlotModel SLOT_ATTACHMENT_MODEL = new SlotModel();

    public AttachmentItemRenderer() {
        this(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
    public AttachmentItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    /**
     * 低模替换逻辑同 {@link AttachmentRender#renderAttachment}
     */
    private void _renderDefaultAttachment(@NotNull PoseStack poseStack,
                                          @NotNull MultiBufferSource pBuffer,
                                          @NotNull ItemDisplayContext transformType,
                                          int pPackedLight, int pPackedOverlay,
                                          ClientAttachmentIndexInstance clientAttachmentIndexInstance) {
        @Nullable AttachmentModelObject attachmentModelObject = clientAttachmentIndexInstance.getAttachmentModel();
        AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();

        @Nullable var attachmentTextureLocation = attachmentDisplay.getTextureLocation();
        if (attachmentModelObject != null && attachmentTextureLocation != null) {
            // 渲染模型

            { // 低模替换
                // 调用低模
                @Nullable AttachmentModelObject attachmentModelLod = clientAttachmentIndexInstance.getAttachmentModelLod();
                if (attachmentModelLod != null // 有低模
                        && !transformType.firstPerson() // 不是第一人称
                        && ClientRenderDistance.shouldRenderLod(poseStack)) { // 在低模渲染范围
                    @Nullable _LodDisplay lodDisplay = attachmentDisplay.getLodDisplay();
                    if (lodDisplay != null) {
                        attachmentModelObject = attachmentModelLod;
                        attachmentTextureLocation = lodDisplay.getTextureLocation();
                    }
                }
            }
            if (attachmentTextureLocation == null) attachmentTextureLocation = ClientRenderUtils.getMissingTextureLocation();

            RenderType renderType = ClientRenderUtils.RenderType_.entityCutout(attachmentTextureLocation);
            ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector_(pBuffer);
            try {
                attachmentModelObject.render(poseStack, transformType, renderType, pPackedLight, pPackedOverlay, null, null);
            } finally {
                ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector_(null);
            }
        } else {
            // 渲染 GUI
            poseStack.translate(0, 0.5, 0);

            // 展示框里显示正常
            if (transformType == ItemDisplayContext.FIXED) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }

            @Nullable var slotTexture = attachmentDisplay.getSlotTextureLocation();
            if (slotTexture == null) slotTexture = ClientRenderUtils.getMissingTextureLocation();

            {
                VertexConsumer buffer = pBuffer.getBuffer(ClientRenderUtils.RenderType_.entityTranslucent(slotTexture));
                SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    // --------BlockEntityWithoutLevelRenderer--------

    @Override
    public void renderByItem(@NotNull ItemStack attachmentItem,
                             @NotNull ItemDisplayContext transformType,
                             @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource pBuffer,
                             int pPackedLight, int pPackedOverlay) {
        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return;

        var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
        @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
        if (clientAttachmentIndexInstance != null) {
            poseStack.pushPose(); {
                // GUI 特殊渲染
                if (transformType == ItemDisplayContext.GUI) {
                    poseStack.translate(0.5, 1.5, 0.5);
                    poseStack.mulPose(Axis.ZN.rotationDegrees(180));

                    AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
                    @Nullable var slotTexture = attachmentDisplay.getSlotTextureLocation();
                    if (slotTexture == null) slotTexture = ClientRenderUtils.getMissingTextureLocation();

                    {
                        VertexConsumer buffer = pBuffer.getBuffer(ClientRenderUtils.RenderType_.entityTranslucent(slotTexture));
                        SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
                    }
                } else {
                    poseStack.translate(0.5, 2, 0.5);

                    // 反转模型
                    poseStack.scale(-1, -1, 1);
                    if (transformType == ItemDisplayContext.FIXED) {
                        poseStack.mulPose(Axis.YN.rotationDegrees(90f));
                    }

                    this._renderDefaultAttachment(poseStack, pBuffer, transformType, pPackedLight, pPackedOverlay, clientAttachmentIndexInstance);
                }
            }
            poseStack.popPose();
        } else {
            poseStack.pushPose(); {
                // 没有这个 attachmentLocation，渲染黑紫材质以提醒
                poseStack.translate(0.5, 1.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));

                {
                    VertexConsumer buffer = pBuffer.getBuffer(ClientRenderUtils.RenderType_.entityTranslucent(ClientRenderUtils.getMissingTextureLocation()));
                    SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
            poseStack.popPose();
        }
    }
}

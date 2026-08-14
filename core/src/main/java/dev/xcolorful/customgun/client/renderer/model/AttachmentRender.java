/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.model.AttachmentModelObject;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.renderer.item.AttachmentItemRenderer;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LodDisplay;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderDistance;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class AttachmentRender implements IModelComponentRenderer {

    private final GunModelObject gunModelObject;
    private final AttachmentCategory category;

    public AttachmentRender(GunModelObject gunModelObject, AttachmentCategory category) {
        this.gunModelObject = gunModelObject;
        this.category = category;
    }

    public static void renderAttachment(PoseStack poseStack,
                                        ItemDisplayContext transformType,
                                        int light, int overlay,
                                        ItemStack gunItem,
                                        IAttachment iAttachment,
                                        ItemStack attachmentItem) {
        poseStack.translate(0, -1.5, 0);

        var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
        @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
        if (clientAttachmentIndexInstance == null) {
            // 没有对应的 attachmentIndex，渲染黑紫材质以提醒
            Minecraft mc = Minecraft.getInstance();
            MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(ClientRenderUtils.RenderType_.entityTranslucent(ClientRenderUtils.getMissingTextureLocation()));
            AttachmentItemRenderer.SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
            return;
        }

        AttachmentModelObject attachmentModelObject = clientAttachmentIndexInstance.getAttachmentModel();
        AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
        @Nullable var attachmentTextureLocation = attachmentDisplay.getTextureLocation();

        // 这里是枪械里的配件渲染，没有模型材质就不渲染
        if (attachmentModelObject == null || attachmentTextureLocation == null) return;

        { // 低模替换
            @Nullable AttachmentModelObject attachmentModelLod = clientAttachmentIndexInstance.getAttachmentModelLod();
            if (attachmentModelLod != null // 有低模
                    && !transformType.firstPerson() // 不是第一人称
                    && ClientRenderDistance.shouldRenderLod(poseStack)) { // 在低模渲染范围
                @Nullable _LodDisplay lodDisplay = attachmentDisplay.getLodDisplay();
                if (lodDisplay != null) {
                    // 有低模display
                    attachmentModelObject = attachmentModelLod;
                    attachmentTextureLocation = lodDisplay.getTextureLocation();
                }
            }
        }
        if (attachmentTextureLocation == null) attachmentTextureLocation = ClientRenderUtils.getMissingTextureLocation();

        RenderType renderType = RenderType.entityCutout(attachmentTextureLocation);
        attachmentModelObject.render(poseStack, transformType, renderType, light, overlay, gunItem, attachmentItem);
    }

    // --------IModelComponentRenderer--------

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        ItemStack attachmentItem = this.gunModelObject.getCurrentAttachmentItem().get(this.category);
        if (attachmentItem == null || attachmentItem.isEmpty()) return;

        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return;

        Matrix3f normal = new Matrix3f(poseStack.last().normal());
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        // 和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
        this.gunModelObject.delegateRender((poseStack1, vertexBuffer1, transformType1, _light, _overlay) -> {
            PoseStack _poseStack = new PoseStack();
            _poseStack.last().normal().mul(normal);
            _poseStack.last().pose().mul(pose);

            // 渲染配件
            renderAttachment(_poseStack, transformType, light, overlay, this.gunModelObject.getCurrentGunItem(), iAttachment, attachmentItem);
        });
    }
}

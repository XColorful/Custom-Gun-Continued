/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.ar.GunModelAR;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.AttachmentRender;
import dev.xcolorful.customgun.client.renderer.model.BeamRender;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.attachment.MagazineCategory;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class _GunModelRender {

    private static AttachmentCategory[] ATTACHMENT_CATEGORIES = AttachmentCategory.values();

    static {
        if (PlannedRefactor.CACHE_ITEM_STACK_FOR_RENDER) {}
    }

    /**
     * TODO 渲染时每帧都这么拼命地读NBT？1.21.1+读操作还有额外的copy开销怎么办？
     */
    protected static void render(GunModelObject _this,
                                 PoseStack matrixStack,
                                 ItemDisplayContext transformType,
                                 RenderType renderType,
                                 int light, int overlay,
                                 ItemStack gunItem) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        { // 刷新状态
            _this.currentGunItem = gunItem;
            _this.currentMagazineCategory = MagazineCategory.NONE;
            _this.adapterToRender.clear();
        }

        // 更新配件物品的缓存，以供渲染使用
        for (int i = 0; i < ATTACHMENT_CATEGORIES.length; i++) {
            AttachmentCategory category = ATTACHMENT_CATEGORIES[i];
            if (category == AttachmentCategory.NONE) continue;

            @NotNull ItemStack attachmentItem = iGun.getAttachment(gunItem, category);
            if (attachmentItem.isEmpty()) {
                attachmentItem = iGun.getBuiltinAttachment(gunItem, category);
            }

            _this.currentAttachmentItem.put(category, attachmentItem);
            @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
            if (iAttachment == null) continue;

            var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
            @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
            if (clientAttachmentIndexInstance == null) continue;

            // 添加需要渲染的转接口
            AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
            @Nullable String adapterNodeName = attachmentDisplay.getAdapterNodeName();
            if (adapterNodeName != null) {
                _this.adapterToRender.add(adapterNodeName);
            }

            switch (category) {
                case MAGAZINE -> {
                    // 读取弹匣类别，为弹匣渲染做准备
                    @Nullable AttachmentData attachmentData = clientAttachmentIndexInstance.getAttachmentData();
                    if (attachmentData != null) _this.currentMagazineCategory = attachmentData.getMagazineCategory();
                    if (_this.currentMagazineCategory == null) _this.currentMagazineCategory = MagazineCategory.NONE;
                }
                case SCOPE -> {
                    // 读取瞄具 Mount 的渲染需求
                    _this.renderMount = attachmentDisplay.getShowMount();
                }
            }
        }

        { // 渲染激光
            if (_this.laserBeamPaths != null) {
                BeamRender.render(matrixStack, transformType, _this.laserBeamPaths, gunItem);
            }
        }

        if (GunModelAR.render(matrixStack, transformType, renderType, light, overlay, gunItem)) {
            return;
        }

        { // 渲染
            renderScope(_this, matrixStack, transformType, renderType, light, overlay, gunItem);

            ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            _this.super_render(matrixStack, transformType, renderType, light, overlay);
            ClientRenderHelper.disableItemEntityStencilTest();
            _GunModelRender._clearStencilBuffer();
        }
    }

    protected static void renderScope(GunModelObject _this,
                                      PoseStack matrixStack,
                                      ItemDisplayContext transformType,
                                      RenderType renderType,
                                      int light, int overlay,
                                      ItemStack gunItem) {
        ItemStack attachmentItem = _this.currentAttachmentItem.get(AttachmentCategory.SCOPE);
        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (_this.scopePosPath == null || iAttachment == null || attachmentItem.isEmpty()) return;

        matrixStack.pushPose(); {
            for (int i = 0; i < _this.scopePosPath.size(); i++) {
                BedrockPart bedrockPart = _this.scopePosPath.get(i);
                bedrockPart.translate_rotate_scale(matrixStack);
            }
            AttachmentRender.renderAttachment(matrixStack, transformType, light, overlay, gunItem, iAttachment, attachmentItem);
        }
        matrixStack.popPose();

        var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
        @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
        if (clientAttachmentIndexInstance == null) return;

        AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
        boolean enableScope = attachmentDisplay.getEnableScope();
        if (!enableScope) return;

        // 开启模板测试，因为镜内不渲染枪体
        if (enableScope & attachmentDisplay.getEnableSight()) {
            // 组合镜
            ClientRenderHelper.enableItemEntityStencilTest();
            ClientRenderHelper.GL._stencilFunc(GL11.GL_GREATER, 127, 0xFF);
        } else {
            // 长筒镜
            ClientRenderHelper.enableItemEntityStencilTest();
            ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        }
    }

    // --------Compat--------
    // 跨版本适配层

    private static void _clearStencilBuffer() {
        // [1.20.1, 1.21.6)
//        ClientRenderHelper.GL._clear(GL11.GL_STENCIL_BUFFER_BIT);

        // [1.21.6, )
//        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
//        if (target.useStencil && target.getDepthTexture() != null) {
//            RenderSystem.getDevice().createCommandEncoder().clearStencilTexture(target.getDepthTexture(), 0);
//        }
    }
}

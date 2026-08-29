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
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.ar.GunModelAR;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.AttachmentRender;
import dev.xcolorful.customgun.client.renderer.model.BeamRender;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.attachment.MagazineCategory;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.List;

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
            _renderScope(_this, matrixStack, transformType, renderType, light, overlay, gunItem);

            _renderHand(_this, matrixStack, transformType, renderType, light, overlay, gunItem);

            ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            _this.super_render(matrixStack, transformType, renderType, light, overlay);
            ClientRenderHelper.disableItemEntityStencilTest();
            _GunModelRender._clearStencilBuffer();
        }
    }

    private static void _renderScope(GunModelObject _this,
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

    /**
     * <ul>
     *     26.2 延迟渲染下
     *     <li>手臂与非瞄具配件必须在 submit 阶段提交（flush 阶段 collector 已被清空）</li>
     *     <li>这里与 {@link #_renderScope} 一样，在 submit 阶段沿定位组路径计算位姿并提交</li>
     * </ul>
     */
    @ApiStatus.AvailableSince("26.2")
    private static void _renderHand(GunModelObject _this,
                                    PoseStack matrixStack,
                                    ItemDisplayContext transformType,
                                    RenderType renderType,
                                    int light, int overlay,
                                    ItemStack gunItem) {
        // [26.2, )
        if (true) return; // 让IDE保留下面的引用关系
        _renderAttachments(_this, matrixStack, transformType, light, overlay, gunItem);
        if (transformType.firstPerson() && _this.getRenderHand()) {
            _renderHandInternal(_this, matrixStack, light, HumanoidArm.LEFT);
            _renderHandInternal(_this, matrixStack, light, HumanoidArm.RIGHT);
        }
    }
    /**
     * 在 submit 阶段渲染非瞄具配件（laser/grip/muzzle/magazine 等），沿各自的定位组路径提交。
     */
    @ApiStatus.AvailableSince("26.2")
    private static void _renderAttachments(GunModelObject _this,
                                           PoseStack matrixStack,
                                           ItemDisplayContext transformType,
                                           int light, int overlay,
                                           ItemStack gunItem) {
        for (int i = 0; i < ATTACHMENT_CATEGORIES.length; i++) {
            AttachmentCategory category = ATTACHMENT_CATEGORIES[i];
            if (category == AttachmentCategory.NONE || category == AttachmentCategory.SCOPE) continue;

            ItemStack attachmentItem = _this.currentAttachmentItem.get(category);
            @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
            if (iAttachment == null || attachmentItem.isEmpty()) continue;

            @Nullable List<BedrockPart> posPath = _this.getAttachmentPosPath(category);
            if (posPath == null) continue;

            matrixStack.pushPose(); {
                for (int j = 0; j < posPath.size(); j++) {
                    posPath.get(j).translate_rotate_scale(matrixStack);
                }
                AttachmentRender.renderAttachment(matrixStack, transformType, light, overlay, gunItem, iAttachment, attachmentItem);
            }
            matrixStack.popPose();
        }
    }
    /**
     * 在 submit 阶段渲染第一人称手臂，沿手部定位组路径提交。
     */
    @ApiStatus.AvailableSince("26.2")
    private static void _renderHandInternal(GunModelObject _this,
                                            PoseStack matrixStack,
                                            int light,
                                            HumanoidArm hand) {
        @Nullable List<BedrockPart> handPath = hand == HumanoidArm.LEFT ? _this.getLeftHandPosPath() : _this.getRightHandPosPath();
        if (handPath == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        matrixStack.pushPose(); {
            for (int i = 0; i < handPath.size(); i++) {
                handPath.get(i).translate_rotate_scale(matrixStack);
            }
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180f));
            ClientRenderHelper.renderFirstPersonArm(minecraft.player, hand, matrixStack, light);
        }
        matrixStack.popPose();
    }

    // --------Compat--------
    // 跨版本适配层

    private static void _clearStencilBuffer() {
        // [1.20.1, 1.21.6)
//        ClientRenderHelper.GL._clear(GL11.GL_STENCIL_BUFFER_BIT);

        // [1.21.6, )
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        if (target.useStencil && target.getDepthTexture() != null) {
            RenderSystem.getDevice().createCommandEncoder().clearStencilTexture(target.getDepthTexture(), 0);
        }
    }
}

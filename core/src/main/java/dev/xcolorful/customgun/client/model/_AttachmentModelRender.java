/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.vertex.*;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.compat.ar.AttachmentModelAR;
import dev.xcolorful.customgun.client.compat.oculus.OculusCompat;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.BeamRender;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * 专门把{@link AttachmentModelObject}渲染实现抽到单独的类，避免影响阅读视野
 */
public class _AttachmentModelRender {

    protected static void render(AttachmentModelObject _this,
                                 PoseStack matrixStack,
                                 ItemDisplayContext transformType,
                                 RenderType renderType,
                                 int light, int overlay,
                                 @Nullable ItemStack gunItem, @Nullable ItemStack attachmentItem) {
        _this.currentGunItem = gunItem;
        _this.attachmentItem = attachmentItem;

        { // 渲染倍镜
            if (transformType.firstPerson()) {
                // 第一人称渲染
                if (_this.enableScope & _this.enableSight) {
                    renderBoth(_this, matrixStack, transformType, renderType, light, overlay); // 参数不写成多行是因为 函数签名已经能体现diff & 方便直接看全貌
                } else if (_this.enableScope) {
                    renderScope(_this, matrixStack, transformType, renderType, light, overlay);
                } else if (_this.enableSight) {
                    renderSight(_this, matrixStack, transformType, renderType, light, overlay);
                }
            } else {
                // 渲染镜身
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.scopeBodyPath);
                // 渲染目镜上的环
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.ocularRingPath);
            }
        }


        { // 渲染模型和激光
            boolean renderModelBeforeBeam = _this.enableScope | _this.enableSight; // 瞄准镜 -> 先渲染模型，再渲染激光
            if (renderModelBeforeBeam) {
                _this.super_render(matrixStack, transformType, renderType, light, overlay);
            }

            // 渲染激光
            for (int i = 0; i < _this.laserBeamPaths.size(); i++) {
                List<BedrockPart> laserBeamPath = _this.laserBeamPaths.get(i);
                BeamRender.render(matrixStack, transformType, laserBeamPath, attachmentItem);
            }

            if (!renderModelBeforeBeam) {
                _this.super_render(matrixStack, transformType, renderType, light, overlay);
            }
        }
    }

    /**
     * 同时渲染scope和sight遮罩
     */
    private static void renderBoth(AttachmentModelObject _this,
                                   PoseStack matrixStack,
                                   ItemDisplayContext transformType,
                                   RenderType renderType,
                                   int light, int overlay) {
        if (AttachmentModelAR.renderBoth(_this, matrixStack, transformType, renderType, light, overlay)) return;

        ClientRenderHelper.enableItemEntityStencilTest();

        // 清空模板缓冲区、准备绘制模板缓冲
        _AttachmentModelRender._clearStencilBuffer();
        if (_this.ocularRingPath != null) {
            ClientRenderHelper.GL._stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

            // 渲染目镜外环
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.ocularRingPath);
        }

        // 渲染目镜以写入模板桓冲值 (暂时只渲染 ocular_scope)
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, true);

        // 渲染镜身
        if (_this.scopeBodyPath != null) {
            ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.scopeBodyPath);
        }

        // 渲染目镜以写入模板桓冲值 (渲染其他的目镜)
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 渲染目镜遮罩和划分
        renderOcularAndDivision(_this, matrixStack, transformType, renderType, light, overlay, true);

        // 关闭模板缓冲
        ClientRenderHelper.GL._stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        ClientRenderHelper.disableItemEntityStencilTest();

        // 渲染其他部分
        _this.super_render(matrixStack, transformType, renderType, light, overlay);
    }
    /**
     * 只渲染scope遮罩
     */
    private static void renderScope(AttachmentModelObject _this,
                                    PoseStack matrixStack,
                                    ItemDisplayContext transformType,
                                    RenderType renderType,
                                    int light, int overlay) {
        if (AttachmentModelAR.renderScope(_this, matrixStack, transformType, renderType, light, overlay)) return;

        ClientRenderHelper.enableItemEntityStencilTest();

        // 清空模板缓冲区、准备绘制模板缓冲
        _AttachmentModelRender._clearStencilBuffer();

        // 渲染目镜外环
        if (_this.ocularRingPath != null) {
            ClientRenderHelper.GL._stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.ocularRingPath);
        }

        // 渲染目镜以写入模板桓冲值
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 渲染镜身
        if (_this.scopeBodyPath != null) {
            ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.scopeBodyPath);
        }

        // 渲染目镜遮罩和划分
        renderOcularAndDivision(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 关闭模板缓冲
        ClientRenderHelper.GL._stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        ClientRenderHelper.disableItemEntityStencilTest();

        // 渲染其他部分
        _this.super_render(matrixStack, transformType, renderType, light, overlay);
    }
    /**
     * 只渲染sight遮罩
     */
    private static void renderSight(AttachmentModelObject _this,
                                    PoseStack matrixStack,
                                    ItemDisplayContext transformType,
                                    RenderType renderType,
                                    int light, int overlay) {
        if (AttachmentModelAR.renderSight(_this, matrixStack, transformType, renderType, light, overlay)) return;

        ClientRenderHelper.enableItemEntityStencilTest();

        // 清空模板缓冲区、准备绘制模板缓冲
        _AttachmentModelRender._clearStencilBuffer();

        // 渲染目镜以写入模板桓冲值
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 渲染准心 (划分?)
        renderDivisionOnly(_this, matrixStack, transformType, renderType, light, overlay);

        // 关闭模板缓冲
        ClientRenderHelper.GL._stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        ClientRenderHelper.disableItemEntityStencilTest();

        // 渲染其他部分
        if (_this.scopeBodyPath != null) {
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.scopeBodyPath);
        }
        _this.super_render(matrixStack, transformType, renderType, light, overlay);
    }

    /**
     * 单独渲染模型的一部分
     */
    private static void renderModelPart(AttachmentModelObject _this,
                                        PoseStack poseStack,
                                        ItemDisplayContext transformType,
                                        RenderType renderType,
                                        int light, int overlay,
                                        @Nullable List<BedrockPart> path) {
        if (path == null) return;

        @Nullable Object collector = ClientRenderHelper.FirstPersonArmHelper.getFirstPersonArmCollector();
        if (collector == null) return;

        poseStack.pushPose(); {
            // 只对最后一个节点之前的节点施加变换；最后一个节点的变换由下面的 part.render() 内部施加，
            // 否则末端节点（scope_body/ocular/ocular_ring 等）的平移旋转会被应用两次
            for (int i = 0; i < path.size() - 1; i++) {
                path.get(i).translate_rotate_scale(poseStack);
            }
            BedrockPart part = path.get(path.size() - 1);
            {
                part.visible = true; {
                    Minecraft mc = Minecraft.getInstance();

                    MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                    VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
                    part.render(poseStack,
                            transformType,
                            vertexConsumer,
                            light, overlay);

                    if (!OculusCompat.endBatch(bufferSource)) {
                        bufferSource.endBatch();
                    }
                }
                part.visible = false;
            }
        }
        poseStack.popPose();
    }

    private static void renderOcularStencil(AttachmentModelObject _this,
                                            PoseStack matrixStack,
                                            ItemDisplayContext transformType,
                                            RenderType renderType,
                                            int light, int overlay,
                                            boolean enableScope) {
        if (_this.divisionOcularEntries.isEmpty()) return;

        ClientRenderHelper.GL._colorMask(false, false, false, false);
        ClientRenderHelper.GL._depthMask(false);
        ClientRenderHelper.GL._stencilMask(0xFF);
        ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        // 绘制目镜
        for (int i = _this.divisionOcularEntries.size() - 1; i >= 0; i--) {
            AttachmentModelObject._Division_Ocular_Entry entry = _this.divisionOcularEntries.get(i);
            @Nullable List<BedrockPart> ocularNodePath = entry.getOcularNodePath();
            if (ocularNodePath == null) continue; // 倒序，所以不break

            if (enableScope == entry.getEnableScope()) {
                ClientRenderHelper.GL._stencilFunc(GL11.GL_GREATER, i + 1, 0xFF);
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, ocularNodePath);
            }
        }

        // 恢复渲染状态
        ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        ClientRenderHelper.GL._depthMask(true);
        ClientRenderHelper.GL._colorMask(true, true, true, true);
    }

    private static void renderOcularAndDivision(AttachmentModelObject _this,
                                                PoseStack matrixStack,
                                                ItemDisplayContext transformType,
                                                RenderType renderType,
                                                int light, int overlay,
                                                boolean selective) {
        if (_this.divisionOcularEntries.isEmpty() || _this.divisionOcularEntries.get(0).getOcularNodePath() == null) return;

        @Nullable Object collector = ClientRenderHelper.FirstPersonArmHelper.getFirstPersonArmCollector();
        if (collector == null) return;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder;
        builder = tesselator.getBuilder();

        // 准备渲染圆形模板层
        ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INVERT);
        ClientRenderHelper.GL._colorMask(false, false, false, false);
        ClientRenderHelper.GL._depthMask(false);
        float rad = 80 * _this.getScopeViewRadiusModifier(); // 80是一个随便找的大小合适的数值
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            float partialTick = ClientRenderUtils.getRenderFrameTime();
            rad *= ILocalShooterGetter.fromLocalPlayer(player).cgc$getRenderAimingProgress(partialTick);
        }
        final float finalRad = rad;

        // 遍历 divisionOcularEntries
        for (int i = 0; i < _this.divisionOcularEntries.size(); i++) {
            AttachmentModelObject._Division_Ocular_Entry entry = _this.divisionOcularEntries.get(i);
            @Nullable List<BedrockPart> ocularNodePath = entry.getOcularNodePath();
            if (ocularNodePath == null) break; // 正序遍历，所以直接结束

            if (selective && !entry.getEnableScope()) {
                continue;
            }

            ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
            Vector3f ocularCenter = getBedrockPartCenter(matrixStack, ocularNodePath);
            float centerX = ocularCenter.x() * 16 * 90;
            float centerY = ocularCenter.y() * 16 * 90;

            {
                builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                builder.vertex( // matrixStack.last(),
                        centerX, centerY, -90.0f)
                        .color(255, 255, 255, 255)
                        .endVertex();
                for (int j = 0; j <= 90; j++) {
                    float angle = (float) j * ((float) Math.PI * 2F) / 90.0F;
                    float sin = Mth.sin(angle);
                    float cos = Mth.cos(angle);
                    builder.vertex( // matrixStack.last(),
                            centerX + cos * finalRad, centerY + sin * finalRad, -90.0f)
                            .color(255, 255, 255, 255)
                            .endVertex();
                }
                BufferUploader.drawWithShader(builder.end());
            }
        }

        ClientRenderHelper.GL._depthMask(true);
        ClientRenderHelper.GL._colorMask(true, true, true, true);
        ClientRenderHelper.GL._stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        // 遍历 divisionOcularEntries
        for (int i = 0; i < _this.divisionOcularEntries.size(); i++) {
            AttachmentModelObject._Division_Ocular_Entry entry = _this.divisionOcularEntries.get(i);
            @Nullable List<BedrockPart> ocularNodePath = entry.getOcularNodePath();
            @Nullable List<BedrockPart> divisionNodePath = entry.getDivisionNodePath();
            if (ocularNodePath == null || divisionNodePath == null) break; // 正序遍历，等价于 i < ocularNodePaths.size() && i < divisionNodePaths.size()

            if (i > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("Index of oculus is out of range for 127");
            }
            if (selective && !entry.getEnableScope()) {
                ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, divisionNodePath);
            } else {
                // 渲染目镜黑色遮罩
                ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, ocularNodePath);
                // 渲染准心 (划分?)
                int b = ~(i+1) & 0xFF;
                ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, b, 0xFF);
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, divisionNodePath);
            }
        }
    }
    private static void renderDivisionOnly(AttachmentModelObject _this,
                                           PoseStack matrixStack,
                                           ItemDisplayContext transformType,
                                           RenderType renderType,
                                           int light, int overlay) {
        if (_this.divisionOcularEntries.isEmpty() || _this.divisionOcularEntries.get(0).getDivisionNodePath() == null) return;

        ClientRenderHelper.GL._disableDepthTest();
        for (int i = 0; i < _this.divisionOcularEntries.size(); i++) {
            AttachmentModelObject._Division_Ocular_Entry entry = _this.divisionOcularEntries.get(i);
            @Nullable List<BedrockPart> divisionNodePath = entry.getDivisionNodePath();
            if (divisionNodePath == null) break; // 正序遍历，所以直接结束

            ClientRenderHelper.GL._stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, divisionNodePath);
        }
        ClientRenderHelper.GL._enableDepthTest();
    }

    // public仅用于链接
    @ApiStatus.Internal
    public static Vector3f getBedrockPartCenter(PoseStack poseStack, @NotNull List<BedrockPart> path) {
        Vector3f result;
        poseStack.pushPose(); {
//            poseStack.last().pose().mulLocal(ClientRenderHelper.GL._getModelViewMatrix()); // 加速渲染的兼容，正常应该不需要

            for (int i = 0; i < path.size(); i++) {
                BedrockPart part = path.get(i);
                part.translate_rotate_scale(poseStack);
            }
            Matrix4f matrix = poseStack.last().pose();
            result = new Vector3f(matrix.m30(), matrix.m31(), matrix.m32());
        }
        poseStack.popPose();
        return result;
    }
    
    private static void _clearStencilBuffer() {
        ClientRenderHelper.GL.glClearStencil(0);
        ClientRenderHelper.GL._clear(GL11.GL_STENCIL_BUFFER_BIT);
    }
}

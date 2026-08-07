/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.compat.ar.AttachmentModelAR;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.BeamRender;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
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
                                 ItemStack gunItem, @Nullable ItemStack attachmentItem) {
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
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.scopeBodyPath);
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
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        if (_this.ocularRingPath != null) {
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

            // 渲染目镜外环
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.ocularRingPath);
        }

        // 渲染目镜以写入模板桓冲值 (暂时只渲染 ocular_scope)
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, true);

        // 渲染镜身
        if (_this.scopeBodyPath != null) {
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.scopeBodyPath);
        }

        // 渲染目镜以写入模板桓冲值 (渲染其他的目镜)
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 渲染目镜遮罩和划分
        renderOcularAndDivision(_this, matrixStack, transformType, renderType, light, overlay, true);

        // 关闭模板缓冲
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
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
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);

        // 渲染目镜外环
        if (_this.ocularRingPath != null) {
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.ocularRingPath);
        }

        // 渲染目镜以写入模板桓冲值
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 渲染镜身
        if (_this.scopeBodyPath != null) {
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, _this.scopeBodyPath);
        }

        // 渲染目镜遮罩和划分
        renderOcularAndDivision(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 关闭模板缓冲
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
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
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);

        // 渲染目镜以写入模板桓冲值
        renderOcularStencil(_this, matrixStack, transformType, renderType, light, overlay, false);

        // 渲染准心 (划分?)
        renderDivisionOnly(_this, matrixStack, transformType, renderType, light, overlay);

        // 关闭模板缓冲
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
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

        poseStack.pushPose(); {
            for (int i = 0; i < path.size(); i++) {
                path.get(i).translate_rotate_scale(poseStack);
            }
            BedrockPart part = path.get(path.size() - 1);
            part.visible = true; {
                Minecraft mc = Minecraft.getInstance();

                MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
                part.render(poseStack,
                        transformType,
                        vertexConsumer,
                        light, overlay);

                // TODO OculusCompat
            }
            part.visible = false;
        }
        poseStack.popPose();
    }

    private static void renderOcularStencil(AttachmentModelObject _this,
                                            PoseStack matrixStack,
                                            ItemDisplayContext transformType,
                                            RenderType renderType,
                                            int light, int overlay,
                                            boolean enableScope) {
        if (_this.ocularRingPath.isEmpty()) return;

        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        // 绘制目镜
        for (int i = _this.divisionOcularEntries.size() - 1; i >= 0; i--) {
            AttachmentModelObject._Division_Ocular_Entry entry = _this.divisionOcularEntries.get(i);
            @Nullable List<BedrockPart> ocularNodePath = entry.getOcularNodePath();
            if (ocularNodePath == null) continue; // 倒序，所以不break

            if (enableScope == entry.getEnableScope()) {
                RenderSystem.stencilFunc(GL11.GL_GREATER, i + 1, 0xFF);
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, ocularNodePath);
            }
        }

        // 恢复渲染状态
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
    }

    private static void renderOcularAndDivision(AttachmentModelObject _this,
                                                PoseStack matrixStack,
                                                ItemDisplayContext transformType,
                                                RenderType renderType,
                                                int light, int overlay,
                                                boolean selective) {
        if (_this.divisionOcularEntries.isEmpty() || _this.divisionOcularEntries.get(0).getOcularNodePath() == null) return;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        // 准备渲染圆形模板层
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INVERT);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        float rad = 80 * _this.getScopeViewRadiusModifier(); // 80是一个随便找的大小合适的数值
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            rad *= ILocalShooterGetter.fromLocalPlayer(player).cgc$getRenderAimingProgress(Minecraft.getInstance().getFrameTime());
        }

        // 遍历 divisionOcularEntries
        for (int i = 0; i < _this.divisionOcularEntries.size(); i++) {
            AttachmentModelObject._Division_Ocular_Entry entry = _this.divisionOcularEntries.get(i);
            @Nullable List<BedrockPart> ocularNodePath = entry.getOcularNodePath();
            if (ocularNodePath == null) break; // 正序遍历，所以直接结束

            if (selective && !entry.getEnableScope()) {
                continue;
            }

            RenderSystem.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
            Vector3f ocularCenter = getBedrockPartCenter(matrixStack, ocularNodePath);
            float centerX = ocularCenter.x() * 16 * 90;
            float centerY = ocularCenter.y() * 16 * 90;
            builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(centerX, centerY, -90.0D).color(255, 255, 255, 255).endVertex();
            for (int j = 0; j <= 90; j++) {
                float angle = (float) j * ((float) Math.PI * 2F) / 90.0F;
                float sin = Mth.sin(angle);
                float cos = Mth.cos(angle);
                builder.vertex(centerX + cos * rad, centerY + sin * rad, -90.0D).color(255, 255, 255, 255).endVertex();
            }
            BufferUploader.drawWithShader(builder.end());
        }

        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

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
                RenderSystem.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, divisionNodePath);
            } else {
                // 渲染目镜黑色遮罩
                RenderSystem.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, ocularNodePath);
                // 渲染准心 (划分?)
                int b = ~(i+1) & 0xFF;
                RenderSystem.stencilFunc(GL11.GL_EQUAL, b, 0xFF);
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

        RenderSystem.disableDepthTest();
        for (int i = 0; i < _this.divisionOcularEntries.size(); i++) {
            AttachmentModelObject._Division_Ocular_Entry entry = _this.divisionOcularEntries.get(i);
            @Nullable List<BedrockPart> divisionNodePath = entry.getDivisionNodePath();
            if (divisionNodePath == null) break; // 正序遍历，所以直接结束

            RenderSystem.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
            renderModelPart(_this, matrixStack, transformType, renderType, light, overlay, divisionNodePath);
        }
        RenderSystem.enableDepthTest();
    }

    private static Vector3f getBedrockPartCenter(PoseStack poseStack, @NotNull List<BedrockPart> path) {
        Vector3f result;
        poseStack.pushPose(); {
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
}

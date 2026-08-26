/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xcolorful.customgun.client.compat.ar.ARCompat;
import dev.xcolorful.customgun.client.compat.optifine.OptifineCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class ClientRenderHelper {

    public static void blit(PoseStack poseStack, float x, float y, float uOffset, float vOffset, float pWidth, float height, float textureWidth, float textureHeight) {
        blit(poseStack, x, y, pWidth, height, uOffset, vOffset, pWidth, height, textureWidth, textureHeight);
    }

    private static void blit(PoseStack poseStack, float x, float y, float pWidth, float height, float uOffset, float vOffset, float uWidth, float vHeight, float textureWidth, float textureHeight) {
        innerBlit(poseStack, x, x + pWidth, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    private static void innerBlit(PoseStack poseStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight) {
        innerBlit(poseStack.last().pose(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / textureWidth, (uOffset + uWidth) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight);
    }

    private static void innerBlit(Matrix4f matrix, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix, x1, y2, blitOffset).setUv(minU, maxV);
        bufferbuilder.addVertex(matrix, x2, y2, blitOffset).setUv(maxU, maxV);
        bufferbuilder.addVertex(matrix, x2, y1, blitOffset).setUv(maxU, minV);
        bufferbuilder.addVertex(matrix, x1, y1, blitOffset).setUv(minU, minV);
        BufferUploader.draw(bufferbuilder.buildOrThrow());
    }

    public static void enableItemEntityStencilTest() {
        RenderSystem.assertOnRenderThread();

        boolean handled = OptifineCompat.onEnableItemEntityStencilTest();
        if (!handled) {
            Minecraft mc = Minecraft.getInstance();
            ClientRenderUtils.getMainRenderTarget(mc).enableStencil();
        }

        GL11.glEnable(GL11.GL_STENCIL_TEST);
    }

    public static void disableItemEntityStencilTest() {
        RenderSystem.assertOnRenderThread();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack matrixStack, int combinedLight) {
        @Nullable Object collector = FirstPersonArmHelper.FIRST_PERSON_ARM_COLLECTOR.get();
        if (collector == null) return;

        if (player == null) return;

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        PlayerRenderer renderer = (PlayerRenderer) renderManager.getRenderer(player);
        MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        // int oldId = RenderSystem.getShaderTexture(0);
        // RenderSystem.setShaderTexture(0, ClientRenderUtils.getSkinTextureLocation(player));

        ARCompat.setRenderingLevel();

        var skinLocation = ClientRenderUtils.getSkinTextureLocation(player);
        boolean isSleeveVisible;
        if (hand == HumanoidArm.RIGHT) {
            isSleeveVisible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);

            renderer.renderRightHand(matrixStack,
                    buffer,
                    combinedLight,
                    player);
        } else {
            isSleeveVisible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);

            renderer.renderLeftHand(matrixStack,
                    buffer,
                    combinedLight,
                    player);
        }

        ARCompat.resetRenderingLevel();

        // RenderSystem.setShaderTexture(0, oldId);
    }

    public static class GL {

        public static void _stencilFunc(int func, int ref, int readMask) {
            RenderSystem.stencilFunc(func, ref, readMask);
        }
        public static void _stencilOp(int stencilFail, int depthFail, int pass) {
            RenderSystem.stencilOp(stencilFail, depthFail, pass);
        }

        public static void _colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
            RenderSystem.colorMask(red, green, blue, alpha);
        }
        public static void _depthMask(boolean flag) {
            RenderSystem.depthMask(flag);
        }
        public static void _stencilMask(int mask) {
            RenderSystem.stencilMask(mask);
        }

        public static void _clear(int mask) {
            // 1.20.1-1.21.1
            RenderSystem.clear(mask, Minecraft.ON_OSX);

            // 1.21.4
//          RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT);

            // 1.21.6
//          GlStateManager._clear(mask);
        }
        public static void glClearStencil(int s) {
            RenderSystem.clearStencil(s);
        }

        public static void _disableDepthTest() {
            RenderSystem.disableDepthTest();
        }
        public static void _enableDepthTest() {
            RenderSystem.enableDepthTest();
        }

        public static void _enableBlend() {
            RenderSystem.enableBlend();
        }
        public static void _disableBlend() {
            RenderSystem.disableBlend();
        }

        /**
         * 原版准心的反色效果，使在各种画面上都可见
         * @see net.minecraft.client.gui.Gui#renderCrosshair
         */
        public static void _enableCrosshair() {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                    GlStateManager.DestFactor.ONE_MINUS_DST_COLOR,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
        }
        public static void _disableCrosshair() {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    @ApiStatus.AvailableSince("1.21.10")
    public static class FirstPersonArmHelper {

        private static final ThreadLocal<Object> FIRST_PERSON_ARM_COLLECTOR = new ThreadLocal<>();

        public static void setFirstPersonArmCollector(Object collector) {
            if (collector == null) {
                FIRST_PERSON_ARM_COLLECTOR.remove();
            } else {
                FIRST_PERSON_ARM_COLLECTOR.set(collector);
            }
        }

        /**
         * 获取当前渲染使用的{@code SubmitNodeCollector}，未设置时返回{@code null}
         * <br>
         * {@code MultiBufferSource}在26.2被移除，模型渲染统一经由该收集器提交
         * <br>
         * 在26.2以前的版本用于占位，如果低版本渲染异常可使其始终返回非null来规避
         */
        @ApiStatus.AvailableSince("26.2")
        public static @Nullable Object getFirstPersonArmCollector() {
            if (disableCollectorCheck) return _dummy;
            var collector = FIRST_PERSON_ARM_COLLECTOR.get();
            return collector != null ? collector : dummyCollector.get();
        }

        public static boolean disableCollectorCheck = true; // 26.2起不再使用
        private static final @NotNull Object _dummy = new Object();
        private static final ThreadLocal<Object> dummyCollector = new ThreadLocal<>();
        /**
         * 该重载用于[1.21.10, 26.2)占位用
         */
        public static void setFirstPersonArmCollector_(Object collector) {
            if (collector == null) {
                dummyCollector.remove();
            } else {
                dummyCollector.set(collector);
            }
        }
    }
}

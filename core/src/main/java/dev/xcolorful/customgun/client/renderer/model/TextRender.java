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
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.ModelObject;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderManager;
import dev.xcolorful.customgun.core.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Supplier;

public class TextRender implements IModelComponentRenderer {

    private final _ModelNodeTextDisplay modelNodeTextDisplay;
    /**
     * 文字节点从模型根节点开始的完整路径
     */
    private final List<BedrockPart> path;
    private final Supplier<@Nullable ItemStack> gunItem;

    public TextRender(_ModelNodeTextDisplay modelNodeTextDisplay, List<BedrockPart> path, Supplier<@Nullable ItemStack> gunItem) {
        this.modelNodeTextDisplay = modelNodeTextDisplay;
        this.path = path;
        this.gunItem = gunItem;
    }

    /**
     * 文字改由 {@link #submit} 提交
     * <br>
     * 这里保留功能渲染器占位，沿用原语义：文字节点自身的方块与子节点不渲染
     */
    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
    }

    /**
     * 提交文字
     * <ul>
     *     <li>26.2 起文字必须在 submit 阶段提交</li>
     *     <li>[1.20.1, 26.2) {@link ModelObject#render} 提交时机不同</li>
     * </ul>
     */
    public void submit(Object collector, // SubmitNodeCollector collector,
                       PoseStack poseStack,
                       ItemDisplayContext transformType) {
        if (!transformType.firstPerson()) return;

        String textLang = this.modelNodeTextDisplay.getTextLang();
        IPlaceholderManager placeholderManager = CustomGun.getPlaceholderManager();

        String text = Language.getInstance().getOrDefault(textLang);
        String parsed = placeholderManager.parse(text, this.gunItem.get());
        if (parsed.isEmpty()) return;

        poseStack.pushPose(); {
            for (int i = 0; i < this.path.size(); i++) {
                this.path.get(i).translate_rotate_scale(poseStack);
            }

            ClientRenderHelper.rotate(poseStack, Axis.ZP.rotationDegrees(180f));
            Matrix3f normal = new Matrix3f(poseStack.last().normal());
            Matrix4f pose = new Matrix4f(poseStack.last().pose());

            Font font = Minecraft.getInstance().font;
            boolean shadow = this.modelNodeTextDisplay.getEnableTextShadow();
            int color = ColorUtils.ARGB32.opaque(this.modelNodeTextDisplay.getTextColor());
            float scale = this.modelNodeTextDisplay.getTextScale();
            int packLight = ClientRenderUtils.LightTexture_.pack(this.modelNodeTextDisplay.getTextLight(), this.modelNodeTextDisplay.getTextLight());
            int width = font.width(parsed);
            int xOffset = (int) (width * this.modelNodeTextDisplay.getXOffsetScale());

            PoseStack poseStack2 = new PoseStack();
            poseStack2.last().normal().mul(normal);
            poseStack2.last().pose().mul(pose);
            poseStack2.scale(2 / 300f * scale, -2 / 300f * scale, -2 / 300f);

            { // 提交文字
                // [1.20.1, 26.2)
//                MultiBufferSource.BufferSource bufferSource = ClientRenderHelper.GL.getModelBufferSource();
//                font.drawInBatch(parsed,
//                        -xOffset,
//                        -font.lineHeight / 2f,
//                        color,
//                        shadow,
//                        poseStack2.last().pose(),
//                        bufferSource,
//                        Font.DisplayMode.NORMAL,
//                        0,
//                        packLight);
//                bufferSource.endBatch();

                // [26.2, )
                ((SubmitNodeCollector) collector).submitText(poseStack2,
                        -xOffset,
                        -font.lineHeight / 2f,
                        Component.literal(parsed).getVisualOrderText(),
                        shadow,
                        Font.DisplayMode.NORMAL,
                        packLight,
                        color,
                        0,
                        0);
            }
        } poseStack.popPose();
    }
}

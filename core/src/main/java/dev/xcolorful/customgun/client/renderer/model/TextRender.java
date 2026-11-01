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
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import dev.xcolorful.customgun.client.model.ModelObject;
import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderManager;
import dev.xcolorful.customgun.core.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class TextRender implements IModelComponentRenderer {

    private final ModelObject modelObject;
    private final _ModelNodeTextDisplay modelNodeTextDisplay;
    private final Supplier<@Nullable ItemStack> gunItem;

    public TextRender(ModelObject modelObject, _ModelNodeTextDisplay modelNodeTextDisplay, Supplier<@Nullable ItemStack> gunItem) {
        this.modelObject = modelObject;
        this.modelNodeTextDisplay = modelNodeTextDisplay;
        this.gunItem = gunItem;
    }

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        if (!transformType.firstPerson()) return;

        String textLang = this.modelNodeTextDisplay.getTextLang();
        IPlaceholderManager placeholderManager = CustomGun.getPlaceholderManager();

        String text = Language.getInstance().getOrDefault(textLang);
        String parsed = placeholderManager.parse(text, this.gunItem.get());
        if (parsed.isEmpty()) return;

        ClientRenderHelper.rotate(poseStack, Axis.ZP.rotationDegrees(180f));
        Matrix3f normal = new Matrix3f(poseStack.last().normal());
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        // 和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
        this.modelObject.delegateRender((_poseStack, _vertexBuffer, _transformType, _light, _overlay) -> {
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
                MultiBufferSource.BufferSource bufferSource = ClientRenderHelper.GL.getModelBufferSource();
                font.drawInBatch(parsed,
                        -xOffset,
                        -font.lineHeight / 2f,
                        color,
                        shadow,
                        poseStack2.last().pose(),
                        bufferSource,
                        Font.DisplayMode.NORMAL,
                        0,
                        packLight);
                bufferSource.endBatch();

                // [26.2, )
                /**
                 * 文字不再走 submitText
                 * <ul>
                 *     <li>提交进手部 pass 的文字会被 Iris 派到光影包的手部程序</li>
                 *     <li>而文字顶点格式没有 Normal，表现为发黑与拖影</li>
                 *     <li>改为排队：由 {@link TextRenderOverlay} 在手部几何画完之后用 {@link ClientRenderRegistry} 的管线重画</li>
                 * </ul>
                 */
//                TextRenderOverlay.queue(poseStack2.last().pose(),
//                        poseStack2.last().normal(),
//                        -xOffset,
//                        -font.lineHeight / 2f,
//                        parsed,
//                        shadow,
//                        packLight,
//                        color);
            }
        }
        );
    }
}

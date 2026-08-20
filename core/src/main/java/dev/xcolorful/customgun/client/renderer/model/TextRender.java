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
import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TextRender implements IModelComponentRenderer {

    private final ModelObject modelObject;
    private final _ModelNodeTextDisplay modelNodeTextDisplay;
    private final ItemStack gunItem;

    public TextRender(ModelObject modelObject, _ModelNodeTextDisplay modelNodeTextDisplay, ItemStack gunItem) {
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
        String parsed = placeholderManager.parse(text, this.gunItem);
        if (parsed.isEmpty()) return;

        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        Matrix3f normal = new Matrix3f(poseStack.last().normal());
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        // 和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
        this.modelObject.delegateRender((_poseStack, _vertexBuffer, _transformType, _light, _overlay) -> {
            @Nullable SubmitNodeCollector collector = ClientRenderHelper.FirstPersonArmHelper.getFirstPersonArmCollector();
            if (collector == null) return;

            Font font = Minecraft.getInstance().font;
            boolean shadow = this.modelNodeTextDisplay.getEnableTextShadow();
            int color = this.modelNodeTextDisplay.getTextColor();
            float scale = this.modelNodeTextDisplay.getTextScale();
            int packLight = ClientRenderUtils.LightTexture_.pack(this.modelNodeTextDisplay.getTextLight(), this.modelNodeTextDisplay.getTextLight());
            int width = font.width(text);
            int xOffset = (int) (width * this.modelNodeTextDisplay.getXOffsetScale());

            PoseStack poseStack2 = new PoseStack();
            poseStack2.last().normal().mul(normal);
            poseStack2.last().pose().mul(pose);
            poseStack2.scale(2 / 300f * scale, -2 / 300f * scale, -2 / 300f);

            collector.submitText(poseStack2,
                    -xOffset,
                    -font.lineHeight / 2f,
                    Component.literal(text).getVisualOrderText(),
                    shadow,
                    Font.DisplayMode.NORMAL,
                    packLight,
                    color,
                    0,
                    0);
        });
    }
}

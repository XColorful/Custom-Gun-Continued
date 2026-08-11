/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;

/**
 * 原模组的{@link BedrockPart}没有继承{@link IBedrockCube}，所以抽一个接口
 */
public interface IBedrockCubeCompile {

    void compile(PoseStack.Pose pose,
                 VertexConsumer consumer,
                 int light, int overlay,
                 float red, float green, float blue, float alpha);
}

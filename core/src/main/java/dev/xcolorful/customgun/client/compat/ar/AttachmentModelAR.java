/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.compat.ar;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.model.AttachmentModelObject;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;

public class AttachmentModelAR {

    /**
     * 同时渲染scope和sight遮罩
     * @return 是否接管渲染
     */
    public static boolean renderBoth(AttachmentModelObject _this,
                                     PoseStack matrixStack,
                                     ItemDisplayContext transformType,
                                     RenderType renderType,
                                     int light, int overlay) {
        // mixin注入点
        return false;
    }
    /**
     * 只渲染scope遮罩
     */
    public static boolean renderScope(AttachmentModelObject _this,
                                   PoseStack matrixStack,
                                   ItemDisplayContext transformType,
                                   RenderType renderType,
                                   int light, int overlay) {
        // mixin注入点
        return false;
    }
    /**
     * 只渲染sight遮罩
     */
    public static boolean renderSight(AttachmentModelObject _this,
                                   PoseStack matrixStack,
                                   ItemDisplayContext transformType,
                                   RenderType renderType,
                                   int light, int overlay) {
        // mixin注入点
        return false;
    }

    // --------Special--------

    /**
     * 很喜欢用神秘小数字吗？
     * <br>
     * Herobrine: 问了吗？
     * <br>
     * 再问添加一万个魔法数字
     */
    @ApiStatus.Internal public static final int _943 = -943;
}

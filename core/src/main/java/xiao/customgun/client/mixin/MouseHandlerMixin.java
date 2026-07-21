/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiao.customgun.client.config.ZoomConfig;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.util.MathUtil;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    public void adjustSensitivity(LocalPlayer player, double yaw, double pitch, Operation<Void> original) {
        ItemStack gunItem = player.getMainHandItem();
        IGun iGun= IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            original.call(player, yaw, pitch);
            return;
        }

//        @NotNull var scopeLocation = iGun.getAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
//        if (scopeLocation.equals(ResourceTag.NULL_LOCATION)) {
//            scopeLocation = iGun.getBuiltinAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
//        }
        /*
         * ↑(源码)在不同地方都重新默写，用的还不是同一接口
         * 恭喜达成成就：🐟的记忆
         */
        float scopeZoomScale = iGun.getScopeZoomScale(gunItem);

        float progress = ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$getSynAimingProgress();
        // 开镜灵敏度系数
        double sensitivityMultiplier = ZoomConfig.ZOOM_SENSITIVITY_BASE_MULTIPLIER.get();
        sensitivityMultiplier = 1 + (sensitivityMultiplier - 1) * progress;
        // 两种状态下的 fov 计算
        double originalFov = Minecraft.getInstance().options.fov().get();
        double currentFov = MathUtil.magnificationToFov(1 + (scopeZoomScale - 1) * progress, originalFov);
        // 荧幕距离系数，MC 和 COD 一样使用 MDV 标准，默认为 MDV133（系数为 1.33）
        double coefficient = ZoomConfig.SCREEN_DISTANCE_COEFFICIENT.get();
        double denominator = MathUtil.zoomSensitivityRatio(currentFov, originalFov, coefficient) * sensitivityMultiplier;
        // 最终结果
        double finalYaw = yaw * denominator;
        double finalPitch = cgc$calculatePronePitch(player, pitch, denominator);
        original.call(player, finalYaw, finalPitch);
    }

    private static double cgc$calculatePronePitch(LocalPlayer player, double pitch, double denominator) {
        double finalPitch = pitch * denominator;
        // 对趴姿限制 pitch 范围
        if (!player.isSwimming() && player.getPose() == Pose.SWIMMING) {
            // 仰角正负是反的
            float playerPitch = -player.getXRot();

//            // 如果玩家上仰超过 25 度，不允许上
//            if (playerPitch > 45) {
//                finalPitch = Math.max(finalPitch, 0);
//            }
//            // 下俯超过 25 度，不允许下
//            if (playerPitch < -30) {
//                finalPitch = Math.min(finalPitch, 0);
//            }
            /*
             * ↑(源码)请修改数字的时候顺带改注释, 别不看不写, 更别留下错误的注释
             */
            // [下仰20°, 上仰25°]
            if (playerPitch > 25) finalPitch = Math.max(finalPitch, 0);
            else if (playerPitch < -20) finalPitch = Math.min(finalPitch, 0);
        }
        return finalPitch;
    }
}

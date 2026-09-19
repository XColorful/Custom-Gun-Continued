package dev.xcolorful.customgun.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.xcolorful.customgun.client.config.ZoomConfig;
import dev.xcolorful.customgun.client.entity.shooter.LocalShooterAspect;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterProne;
import dev.xcolorful.customgun.core.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    /**
     * {@code net.minecraft.world.entity.Entity#turn} 把入参乘以 0.15F 得到角度增量
     * 俯仰角限制要按角度夹取，因此需要在本单位与角度之间换算
     */
    private static final float TURN_DEGREES_PER_UNIT = 0.15F;

    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    public void cgc$adjustSensitivity(LocalPlayer player, double yaw, double pitch, Operation<Void> original) {
        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun= IGunGetter.fromItemStack(gunItem);
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
        if (!player.isSwimming() && player.getPose() == LivingShooterProne.PRONE_POSE) {
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
            /*
             * 必须按绝对值夹取，只钳制增量的话，已经落在范围外的角度永远回不来：
             * turnPlayer 每帧都会调一次 turn（鼠标静止时增量为 0），此时钳制增量的结果是 0，不会把越界的角度拉回来
             * 场景一：趴下时手持非枪械物品把视角抬到范围外，再切到枪械
             * 场景二：趴下连续射击，被后坐力顶出范围
             */
            // 仰角正负是反的
            float upPitch = -player.getXRot();
            float upDelta = (float) -finalPitch * TURN_DEGREES_PER_UNIT;
            float upClamped = Mth.clamp(upPitch + upDelta, LocalShooterAspect.PRONE_PITCH_MIN, LocalShooterAspect.PRONE_PITCH_MAX);
            // 夹取在角度空间做，结果再换算回 turn() 的入参单位
            // 走 turn() 而不是 setXRot()，是为了让 xRotO 与 xRot 一起被更新，否则相机插值会一直偏
            finalPitch = -(upClamped - upPitch) / TURN_DEGREES_PER_UNIT;
        }
        return finalPitch;
    }
}

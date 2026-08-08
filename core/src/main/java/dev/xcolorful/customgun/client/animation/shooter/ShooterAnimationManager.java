/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.animation.shooter.animator.DefaultShooterAnimator;
import dev.xcolorful.customgun.client.api.animation.shooter.IShooterAnimationManager;
import dev.xcolorful.customgun.client.api.animation.shooter.IShooterAnimator;
import dev.xcolorful.customgun.client.api.item.gun.IShooterAnimationCategory;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.playeranimator.PlayerAnimatorCompat;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.util.ClassUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 玩家第三人称动画管理器
 */
public class ShooterAnimationManager implements IShooterAnimationManager {
    public static final ShooterAnimationManager INSTANCE = new ShooterAnimationManager();

    final ClassUtils.ArrayMap<String, IShooterAnimator> animators = new ClassUtils.ArrayMap<>((iShooterAnimator -> iShooterAnimator.getAnimationCategory().getName()));

    protected ShooterAnimationManager() {
    }

    public static void init(McSide mcSide) {
        INSTANCE.registerAnimator(DefaultShooterAnimator.INSTANCE);
    }

    // --------IShooterAnimationManager--------

    @Override
    public boolean registerAnimator(IShooterAnimator animator) {
        String category = animator.getAnimationCategory().getCategoryName();
        if (this.animators.containsKey(category)) {
            IShooterAnimator previous = this.animators.put(category, animator);
            this.animators.put(category, animator);
            CustomGun.LOGGER.debug("ShooterAnimationManager: registered animator {} for category {} (replaced {})", animator.getAnimatorName(), category, previous.getAnimatorName());
        } else {
            this.animators.put(category, animator);
            CustomGun.LOGGER.debug("ShooterAnimationManager: registered animator {} for category {}", animator.getAnimatorName(), category);
        }
        return true;
    }

    @Override
    public @Nullable IShooterAnimator getAnimator(IShooterAnimationCategory animationCategory) {
        return this.animators.mapGet(animationCategory.getName());
    }

    /**
     * 检查流程模板同{@link dev.xcolorful.customgun.core.entity.shooter}
     */
    @Override
    public void setRotationAnglesHead(LivingEntity entityIn,
                                      ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                                      float limbSwingAmount) {
        // 游戏暂停时不进行动画计算，否则会 StackOverflow
        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        // 1. 手持枪械检查
        ItemStack gunItem = entityIn.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            PlayerAnimatorCompat.stopAllAnimation(entityIn);
            return;
        }

        if ( // 2.2 检查状态
                // 攀爬
                entityIn.onClimbable()
                // 游泳
                || entityIn.isSwimming()
                // 鞘翅飞行
                || entityIn.getPose() == Pose.FALL_FLYING
                // 睡觉
                || entityIn.getPose() == Pose.SLEEPING
        ) {
            PlayerAnimatorCompat.stopAllAnimation(entityIn);
            return;
        }

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        if (PlayerAnimatorCompat.playAnimation(entityIn, gunDisplayInstance, limbSwingAmount)) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(entityIn);
        this.playVanillaAnimation(head, body, leftArm, rightArm, iLivingShooter, entityIn, gunDisplayInstance);
    }


    private void playVanillaAnimation(ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                                      ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                      GunDisplayInstance gunDisplayInstance) {
        GunDisplay gunDisplay = gunDisplayInstance.getPojo();
        IShooterAnimationCategory shooterAnimationCategory = gunDisplay.getShooterAnimationCategory();
        @Nullable IShooterAnimator animator = this.getAnimator(shooterAnimationCategory);
        if (animator == null) return;

        float aimingProgress = iLivingShooter.cgc$getSynAimingProgress();
        if (aimingProgress <= 0) {
            animator.animateShooter(head, body, leftArm, rightArm, iLivingShooter, livingShooter, gunDisplayInstance);
        } else {
            animator.animateShooterAiming(head, body, leftArm, rightArm, iLivingShooter, livingShooter, gunDisplayInstance);
        }
    }
}

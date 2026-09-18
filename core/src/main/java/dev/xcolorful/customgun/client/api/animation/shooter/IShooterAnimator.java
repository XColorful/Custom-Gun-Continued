package dev.xcolorful.customgun.client.api.animation.shooter;

import dev.xcolorful.customgun.client.api.item.gun.IShooterAnimationCategory;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public interface IShooterAnimator {

    /**
     * @return 实例自身的标识，与动画无关
     */
    String getAnimatorName();

    /**
     * @return 注册的动画类型
     */
    IShooterAnimationCategory getAnimationCategory();

    /**
     * 射手生物第三人称动画
     */
    void animateShooter(ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                        ILivingShooter iLivingShooter, LivingEntity livingShooter,
                        GunDisplayInstance gunDisplayInstance);

    /**
     * 射手生物第三人称瞄准动画
     */
    void animateShooterAiming(ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                              ILivingShooter iLivingShooter, LivingEntity livingShooter,
                              GunDisplayInstance gunDisplayInstance);
}

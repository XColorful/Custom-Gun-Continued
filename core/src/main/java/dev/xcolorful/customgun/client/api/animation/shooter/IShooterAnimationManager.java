package dev.xcolorful.customgun.client.api.animation.shooter;

import dev.xcolorful.customgun.client.api.item.gun.IShooterAnimationCategory;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface IShooterAnimationManager {

    boolean registerAnimator(IShooterAnimator animator);

    void setRotationAnglesHead(LivingEntity entityIn,
                               ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                               float limbSwingAmount);

    @Nullable IShooterAnimator getAnimator(IShooterAnimationCategory animationCategory);
}

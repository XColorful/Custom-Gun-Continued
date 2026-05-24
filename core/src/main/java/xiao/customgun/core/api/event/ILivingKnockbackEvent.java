package xiao.customgun.core.api.event;

import net.minecraft.world.entity.LivingEntity;

public interface ILivingKnockbackEvent {

    LivingEntity getEntity();

    float getKnockbackStrength();
    double getRatioX();
    double getRatioZ();

    float getOriginalKnockbackStrength();
    double getOriginalRatioX();
    double getOriginalRatioZ();

    void setKnockbackStrength(float strength);
    void setRatioX(double ratioX);
    void setRatioZ(double ratioZ);
}

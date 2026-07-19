package xiao.customgun.core.api.event.shooter;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ILivingShooter;

public interface ILivingShooterEvent {

    @Nullable ILivingShooter getILivingShooter();
    @Nullable LivingEntity getLivingShooter();

    // --------Deprecated--------

    @Deprecated default LivingEntity getEntity() {
        return this.getLivingShooter();
    }
}

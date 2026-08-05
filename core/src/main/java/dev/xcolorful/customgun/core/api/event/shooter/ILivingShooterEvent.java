package dev.xcolorful.customgun.core.api.event.shooter;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface ILivingShooterEvent {

    @Nullable ILivingShooter getILivingShooter();
    @Nullable LivingEntity getLivingShooter();

    // --------Deprecated--------

    @Deprecated default LivingEntity getEntity() {
        return this.getLivingShooter();
    }
}

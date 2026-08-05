package dev.xcolorful.customgun.core.api.event.projectile;

import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import org.jetbrains.annotations.Nullable;

public interface IBulletVictimEntityEvent {

    @Nullable IBulletVictimEntity getIBulletVictimEntity();
}

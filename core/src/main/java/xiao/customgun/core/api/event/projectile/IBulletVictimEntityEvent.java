package xiao.customgun.core.api.event.projectile;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IBulletVictimEntity;

public interface IBulletVictimEntityEvent {

    @Nullable IBulletVictimEntity getIBulletVictimEntity();
}

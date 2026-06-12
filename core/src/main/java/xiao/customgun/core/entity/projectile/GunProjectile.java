package xiao.customgun.core.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import xiao.customgun.core.api.entity.IGunProjectile;

public class GunProjectile extends Projectile implements IGunProjectile {

    public GunProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
    }
}

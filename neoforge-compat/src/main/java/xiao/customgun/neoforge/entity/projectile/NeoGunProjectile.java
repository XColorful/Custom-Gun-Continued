package xiao.customgun.neoforge.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import xiao.customgun.core.entity.projectile.GunProjectile;

public class NeoGunProjectile extends GunProjectile {

    public NeoGunProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
}

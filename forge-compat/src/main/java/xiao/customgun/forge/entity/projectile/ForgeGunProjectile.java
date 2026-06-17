package xiao.customgun.forge.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import xiao.customgun.core.entity.projectile.GunProjectile;

public class ForgeGunProjectile extends GunProjectile {

    public ForgeGunProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
}

package dev.xcolorful.customgun.forge.entity.projectile;

import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class ForgeGunProjectile extends GunProjectile {

    public ForgeGunProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
}

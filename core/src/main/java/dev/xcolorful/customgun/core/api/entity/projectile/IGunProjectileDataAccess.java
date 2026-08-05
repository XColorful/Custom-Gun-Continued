package dev.xcolorful.customgun.core.api.entity.projectile;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGunProjectileDataAccess extends IGunProjectileNBTAccess, IGunProjectileStateAccess {

    @Nullable String getManagerGroupTag(Entity gunProjectile);
    void setManagerGroupTag(Entity gunProjectile, String managerGroupTag);

    /**
     * 获取枪械ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getGunLocation(Entity gunProjectile);
    void setGunLocation(Entity gunProjectile, Identifier gunLocation);
    /**
     * 获取NBT指定的GunDisplay，如无则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getGunDisplayLocation(Entity gunProjectile);
    void setGunDisplayLocation(Entity gunProjectile, Identifier gunDisplayLocation);
    /**
     * 获取子弹ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getAmmoLocation(Entity gunProjectile);
    void setAmmoLocation(Entity gunProjectile, Identifier ammoLocation);

    boolean hasExtraDataTag(Entity gunProjectile);
    @Nullable CompoundTag getExtraDataTag(Entity gunProjectile);
    void setExtraDataTag(Entity gunProjectile, CompoundTag extraDataTag);

    @Nullable _ExplosionData getExplosionData(Entity gunProjectile);
    void setExplosionData(Entity gunProjectile, _ExplosionData explosionData);
}

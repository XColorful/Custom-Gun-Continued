package xiao.customgun.core.api.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IGunProjectileDataAccess extends IGunProjectileNBTAccess, IGunProjectileStateAccess {

    @Nullable String getManagerGroupTag(Entity gunProjectile);
    void setManagerGroupTag(Entity gunProjectile, String managerGroupTag);

    /**
     * 获取枪械ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getGunLocation(Entity gunProjectile);
    void setGunLocation(Entity gunProjectile, ResourceLocation gunLocation);
    /**
     * 获取NBT指定的GunDisplay，如无则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getGunDisplayLocation(Entity gunProjectile);
    void setGunDisplayLocation(Entity gunProjectile, ResourceLocation gunDisplayLocation);
    /**
     * 获取子弹ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getAmmoLocation(Entity gunProjectile);
    void setAmmoLocation(Entity gunProjectile, ResourceLocation ammoLocation);

    boolean hasExtraDataTag(Entity gunProjectile);
    @Nullable CompoundTag getExtraDataTag(Entity gunProjectile);
    void setExtraDataTag(Entity gunProjectile, CompoundTag extraDataTag);
}

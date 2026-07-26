package xiao.customgun.core.api.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.GunProjectileProperty;
import xiao.customgun.core.api.entity.projectile.IGunProjectileDataAccess;
import xiao.customgun.core.api.entity.projectile.IGunProjectileStateAccess;
import xiao.customgun.core.api.projectile.effect.IProjectileEffectRuntime;
import xiao.customgun.core.api.projectile.impact.IProjectileImpactRuntime;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import xiao.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;

import java.util.List;

public interface IProjectileRuntime extends
        IProjectileEffectRuntime,
        IProjectileImpactRuntime,
        IProjectilePhysicsRuntime,
        IProjectileProcessRuntime {

    /**
     * {@link GunProjectileProperty} {@link IGunProjectileDataAccess}
     */
    final class DataCache {
        public @Nullable String managerGroupTag;
        public @Nullable Identifier gunLocation;
        public @Nullable Identifier gunDisplayLocation;
        public @Nullable Identifier ammoLocation;
        public @Nullable CompoundTag extraDataTag;
        public DataCache() {
        }
    }
    /**
     * {@link GunProjectileProperty} {@link IGunProjectileStateAccess}
     */
    final class StateCache {
        public Vec3 shootPos;
        public float armorIgnorePercent = 0;
        public float headshotMultiplier = 1;
        public List<_DistanceDamageData> damageCalculation;
        public int lifetimeTicks = 0;
        public float bulletSpeed = 0; // TODO 原模组逻辑是直接在shootFromRotation里setDeltaMovement
        public float gravity = 0;
        public float friction = 0;
        public int pierce = 0;
        public boolean isTracer = false;
        public boolean fireAspect = false;
        public int fireAspectSeconds = 1;
        public float knockbackStrength = 0;
        public @Nullable CompoundTag extraStateTag;
        public @Nullable _ExplosionData explosionData;
        public StateCache() {
        }
    }
}

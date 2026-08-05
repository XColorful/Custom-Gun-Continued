/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.event.projectile;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import dev.xcolorful.customgun.core.event.EventDispatcher;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 枪射物{@link IGunProjectile} 命中 实体{@link Entity} 事件
 */
public class ProjectileHitEntityEvent extends GunProjectileEvent implements ILogicalSideOnly, IBulletVictimEntityEvent {

    protected final McLogicalSide logicalSide;

    public final @NotNull Context context;

    protected @NotNull IProjectilePhysicsRuntime.EntityHitResult entityHitResult;
    protected @Nullable IBulletVictimEntity iBulletVictimEntity;

    /**
     * 暂时不使用 (依赖默认值填充)
     */
    @Deprecated
    public ProjectileHitEntityEvent(McLogicalSide logicalSide,
                                    @Nullable IGunProjectile iGunProjectile, @Nullable Entity gunProjectile,
                                    @NotNull IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                    @Nullable IBulletVictimEntity iBulletVictimEntity) {
        this(logicalSide, new Context(),
                iGunProjectile, gunProjectile,
                entityHitResult,
                iBulletVictimEntity);
        this.buildDefaultContext();
    }
    public ProjectileHitEntityEvent(McLogicalSide logicalSide, @NotNull Context context,
                                    @Nullable IGunProjectile iGunProjectile, @Nullable Entity gunProjectile,
                                    @NotNull IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                    @Nullable IBulletVictimEntity iBulletVictimEntity) {
        super(iGunProjectile, gunProjectile);
        this.logicalSide = logicalSide;
        this.context = context;
        this.entityHitResult = entityHitResult;
        this.iBulletVictimEntity = iBulletVictimEntity;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.PROJECTILE_HIT_ENTITY_EVENT;
    }

    @Override
    public final McLogicalSide getLogicalSide() {
        return this.logicalSide;
    }

    public @NotNull IProjectilePhysicsRuntime.EntityHitResult getEntityHitResult() {
        return this.entityHitResult;
    }
    public @Nullable IBulletVictimEntity getIBulletVictimEntity() {
        return this.iBulletVictimEntity;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        if (this.logicalSide.isClient()) return null;

        Entity victimEntity = this.getVictimEntity();
        if (!(victimEntity.level() instanceof ServerLevel serverLevel)) return null;

        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                victimEntity.position(),
                victimEntity.getRotationVector(),
                serverLevel,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                serverLevel.getServer(),
                victimEntity
        );
    }

    @Override public String getTextName() {
        return this.entityHitResult.entity().getName().getString();
    }
    @Override public Component getDisplayName() {
        return this.entityHitResult.entity().getDisplayName();
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ProjectileHitEntityEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }

    public static class Context {
        @Nullable Entity victimEntity;
        @Nullable Entity causingEntity;
        Identifier gunLocation;
        float baseDamage = 0;
        @Nullable DamageSource bulletDamage;
        @Nullable DamageSource piercerDamage;
        boolean headshot = false;
        float headshotMultiplier = 1;

        // --------Getter & Setter--------

        public @Nullable Entity getVictimEntity() {
            return this.victimEntity;
        }
        public @Nullable Entity getCausingEntity() {
            return this.causingEntity;
        }
        public Identifier getGunLocation() {
            return this.gunLocation;
        }
        public float getBaseDamage() {
            return this.baseDamage;
        }
        public @Nullable DamageSource getBulletDamage() {
            return this.bulletDamage;
        }
        public @Nullable DamageSource getPiercerDamage() {
            return this.piercerDamage;
        }
        public boolean isHeadshot() {
            return this.headshot;
        }
        public float getHeadshotMultiplier() {
            return this.headshotMultiplier;
        }

        public void setVictimEntity(@Nullable Entity victimEntity) {
            this.victimEntity = victimEntity;
        }
        public void setCausingEntity(@Nullable Entity causingEntity) {
            this.causingEntity = causingEntity;
        }
        public void setGunLocation(Identifier gunLocation) {
            this.gunLocation = gunLocation;
        }
        public void setBaseDamage(float baseDamage) {
            this.baseDamage = baseDamage;
        }
        public void setBulletDamage(@Nullable DamageSource bulletDamage) {
            this.bulletDamage = bulletDamage;
        }
        public void setPiercerDamage(@Nullable DamageSource piercerDamage) {
            this.piercerDamage = piercerDamage;
        }
        public void setHeadshot(boolean headshot) {
            this.headshot = headshot;
        }
        public void setHeadshotMultiplier(float headshotMultiplier) {
            this.headshotMultiplier = headshotMultiplier;
        }

        // --------Deprecated--------

        @Deprecated public @Nullable Entity getHurtEntity() {
            return this.getVictimEntity();
        }
        @Deprecated public float getBaseAmount() {
            return this.getBaseDamage();
        }

        @Deprecated public void setHurtEntity(@Nullable Entity hurtEntity) {
            this.setVictimEntity(hurtEntity);
        }
    }

    // --------便利方法--------

    public Entity getVictimEntity() {
        return this.getEntityHitResult().entity();
    }

    @Deprecated
    @ApiStatus.Internal
    public void buildDefaultContext() {
        context.victimEntity = this.getVictimEntity();
        context.causingEntity = this.getGunProjectile();
        if (context.causingEntity instanceof Projectile projectile) {
            @Nullable Entity livingShooter = projectile.getOwner();
            if (livingShooter != null) context.causingEntity = livingShooter;
        }
        context.gunLocation = this.getGunLocation();
        // TODO damage

        context.headshot = this.entityHitResult.headshot();
        // TODO headshotMultiplier
    }
}

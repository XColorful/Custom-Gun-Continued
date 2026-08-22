/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.entity;

import dev.xcolorful.customgun.core.api.entity.hitbox.IEntityHitboxHistoryGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.data.data.gun._InaccuracyData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

// TODO 改成mask的形式，让pojo多个数据充当modifier
public enum ShootState implements ResourceTag.CategoryTag {
    /**
     * 站立不动
     */
    STAND(ShootStateTag.STAND),
    /**
     * 跑打
     */
    MOVE(ShootStateTag.MOVE),
    /**
     * 骑射
     */
    RIDE(ShootStateTag.RIDE),
    /**
     * 潜行 (半蹲)
     */
    SNEAK(ShootStateTag.SNEAK),
    /**
     * 趴姿 (游泳)
     */
    PRONE(ShootStateTag.PRONE, ShootStateTag.PRONE_OLD1),
    /**
     * 瞄准状态
     */
    AIM(ShootStateTag.AIM),
    /**
     * 悬空
     */
    LEVITATE(ShootStateTag.LEVITATE);

    public final String stateName;
    private final String stateNameOld;
    ShootState(String name) {
        this(name, null);
    }
    ShootState(String name, String nameOld) {
        this.stateName = name;
        this.stateNameOld = nameOld;
    }

    @Override public String getTagName() {
        return this.stateName;
    }
    @Override public String getCategoryName() {
        return this.stateName;
    }

    private static final Map<String, ShootState> SHOOT_STATES = new HashMap<>();

    static {
        for (ShootState state : values()) {
            SHOOT_STATES.put(state.stateName, state);
            if (state.stateNameOld != null) SHOOT_STATES.put(state.stateNameOld, state);
        }
    }

    public static @Nullable ShootState fromString(String name) {
        return name != null ? SHOOT_STATES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.stateName;
    }

    public float getInaccuracy(_InaccuracyData pojo) {
        return switch (this) {
            case STAND -> pojo.getStand();
            case MOVE -> pojo.getMove();
            case RIDE -> pojo.getRide();
            case SNEAK -> pojo.getSneak();
            case PRONE -> pojo.getProne();
            case AIM -> pojo.getAim();
            case LEVITATE -> pojo.getLevitate();
            // 增加类型使此处强制编译不通过
        };
    }

    public static ShootState fromLivingShooter(@Nullable LivingEntity livingShooter) {
        if (livingShooter == null) return ShootState.LEVITATE;
        return fromLivingShooter(ILivingShooterGetter.cgc$fromLivingEntity(livingShooter), livingShooter);
    }
    public static ShootState fromLivingShooter(@NotNull ILivingShooter iLivingShooter, @NotNull LivingEntity livingShooter) {
        // 1. 骑乘 (优先于悬空)
        if (livingShooter.isPassenger() || livingShooter.isVehicle()) {
            return ShootState.RIDE;
        }
        // 2. 悬空
        if (!livingShooter.onGround() || livingShooter.isSwimming()) {
            return ShootState.LEVITATE;
        }

        // 3. 瞄准 (这个跟移动的优先级有争议)
        if (iLivingShooter.cgc$getSynAimingProgress() >= 1.0f) {
            return ShootState.AIM;
        }
        // 4. 移动
        @Nullable IEntityHitboxHistory entityHitboxHistory = IEntityHitboxHistoryGetter.cgc$fromEntity(livingShooter);
        @Nullable Vec3 velocity = entityHitboxHistory != null ? entityHitboxHistory.cgc$getHistoryVelocity(0) : null;
        if (velocity != null && velocity.length() > 0.05f
//                || livingShooter.walkDist - livingShooter.walkDistO > 0.05f // 这两个字段在1.21.4被移除
        ) {
            return ShootState.MOVE;
        }

        // 5. 潜行
        if (livingShooter.getPose() == Pose.CROUCHING) {
            return ShootState.SNEAK;
        }
        // 6. 趴姿
        if (livingShooter.isVisuallySwimming()) { // 任意模组的swim姿势均可，用活版门也算
            return ShootState.PRONE;
        }

        // 7. 站姿
        return ShootState.STAND;
    }

    public static EnumSet<ShootState> of(@Nullable LivingEntity livingShooter) {
        if (livingShooter == null) return EnumSet.noneOf(ShootState.class);
        return of(ILivingShooterGetter.cgc$fromLivingEntity(livingShooter), livingShooter);
    }
    public static EnumSet<ShootState> of(@NotNull ILivingShooter iLivingShooter, @NotNull LivingEntity livingShooter) {
        EnumSet<ShootState> set = EnumSet.noneOf(ShootState.class);

        boolean isStanding = true;
        // 1. 骑乘 (优先于悬空)
        if (livingShooter.isPassenger() || livingShooter.isVehicle()) {
            set.add(ShootState.RIDE);
            isStanding = false;
        }
        // 2. 悬空
        if (!livingShooter.onGround() || livingShooter.isSwimming()) {
            set.add(ShootState.LEVITATE);
            isStanding = false;
        }
        // 3. 瞄准
        if (iLivingShooter.cgc$getSynAimingProgress() >= 1.0f) {
            set.add(ShootState.AIM);
        }
        // 4. 移动
        @Nullable IEntityHitboxHistory entityHitboxHistory = IEntityHitboxHistoryGetter.cgc$fromEntity(livingShooter);
        @Nullable Vec3 velocity = entityHitboxHistory != null ? entityHitboxHistory.cgc$getHistoryVelocity(0) : null;
        if (velocity != null && velocity.length() > 0.05f) {
            set.add(ShootState.MOVE);
            isStanding = false;
        }
        // 5. 潜行
        if (livingShooter.isCrouching()) {
            set.add(ShootState.SNEAK);
            isStanding = false;
        }
        // 6. 趴姿
        if (livingShooter.isVisuallySwimming()) {
            set.add(ShootState.PRONE);
            isStanding = false;
        }
        // 7. 站姿
        if (isStanding) {
            set.add(ShootState.STAND);
        }
        return set;
    }

    // --------Deprecated--------

    @Deprecated public boolean isAim() {
        return this == AIM;
    }
}
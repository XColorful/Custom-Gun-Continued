/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.entity.sync.Serializers;
import xiao.customgun.core.entity.sync.SyncedClassKey;
import xiao.customgun.core.entity.sync.SyncedDataKey;
import xiao.customgun.core.entity.sync.SyncedEntityData;

public class LivingShooterSyncKey {

    /**
     * 缓存字段，避免每次都重新拿
     * 其他模组不应该在模组主类初始化时调用
     */
    public static final IMcRegistry mcRegistry = CustomGun.getMcRegistry();

    public static final SyncedDataKey<LivingEntity, Long> SHOOT_COOL_DOWN_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.LONG)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "shoot_cool_down")))
            .defaultValueSupplier(() -> -1L)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Long> MELEE_COOL_DOWN_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.LONG)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "melee_cool_down")))
            .defaultValueSupplier(() -> -1L)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, ReloadState> RELOAD_STATE_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.RELOAD_STATE)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "reload_state")))
            .defaultValueSupplier(ReloadState::new)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Float> AIMING_PROGRESS_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.FLOAT)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "aiming_progress")))
            .defaultValueSupplier(() -> 0f)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Long> DRAW_COOL_DOWN_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.LONG)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "draw_cool_down")))
            .defaultValueSupplier(() -> -1L)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> IS_AIMING_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "is_aiming")))
            .defaultValueSupplier(() -> false)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Float> SPRINT_TIME_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.FLOAT)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "sprint_time")))
            .defaultValueSupplier(() -> 0f)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> IS_BOLTING_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "is_bolting")))
            .defaultValueSupplier(() -> false)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static void registerAll() {
        registerSyncData(SHOOT_COOL_DOWN_KEY);
        registerSyncData(MELEE_COOL_DOWN_KEY);
        registerSyncData(RELOAD_STATE_KEY);
        registerSyncData(AIMING_PROGRESS_KEY);
        registerSyncData(DRAW_COOL_DOWN_KEY);
        registerSyncData(IS_AIMING_KEY);
        registerSyncData(SPRINT_TIME_KEY);
        registerSyncData(IS_BOLTING_KEY);
    }

    private static void registerSyncData(SyncedDataKey<? extends Entity, ?> dataKey) {
        SyncedEntityData.instance().registerDataKey(dataKey);
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.sync;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.entity.sync.core.Serializers;
import xiao.customgun.core.entity.sync.core.SyncedClassKey;
import xiao.customgun.core.entity.sync.core.SyncedDataKey;
import xiao.customgun.core.entity.sync.core.SyncedEntityData;

public class ModSyncedEntityData {

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

    public static final SyncedDataKey<LivingEntity, ReloadState> RELOAD_STATE_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, ModSerializers.RELOAD_STATE)
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

    public static final SyncedDataKey<LivingEntity, Integer> THROWABLE_USE_TICK = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.INTEGER)
            .id(mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "throwable_using")))
            .defaultValueSupplier(() -> -1)
            .syncMode(SyncedDataKey.SyncMode.SELF_ONLY)
            .build();

    public static void registerAll() {
        registerEntityData(SHOOT_COOL_DOWN_KEY);
        registerEntityData(MELEE_COOL_DOWN_KEY);
        registerEntityData(RELOAD_STATE_KEY);
        registerEntityData(AIMING_PROGRESS_KEY);
        registerEntityData(DRAW_COOL_DOWN_KEY);
        registerEntityData(IS_AIMING_KEY);
        registerEntityData(SPRINT_TIME_KEY);
        registerEntityData(IS_BOLTING_KEY);
        registerEntityData(THROWABLE_USE_TICK);
    }

    private static void registerEntityData(SyncedDataKey<? extends Entity, ?> dataKey) {
        SyncedEntityData.instance().registerDataKey(dataKey);
    }
}

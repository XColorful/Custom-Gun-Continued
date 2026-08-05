/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.config;

import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.config.ModConfigTag;

import java.util.Collections;
import java.util.List;

public class SyncConfig {

    public static void init(IModConfigSpecBuilder builder) {
        interactKey(builder);
        base_multiplier(builder);
        misc(builder);
    }

    // 交互键的判断是在客户端执行的，但是需要服务端来控制
    public static IModConfigSpec<List<String>> INTERACT_KEY_WHITELIST_BLOCKS;
    public static IModConfigSpec<List<String>> INTERACT_KEY_WHITELIST_ENTITIES;
    public static IModConfigSpec<List<String>> INTERACT_KEY_BLACKLIST_BLOCKS;
    public static IModConfigSpec<List<String>> INTERACT_KEY_BLACKLIST_ENTITIES;

    private static void interactKey(IModConfigSpecBuilder builder) {
        builder.startBuild(ModConfigTag.interactKey_path);

        builder.addComment(ModConfigTag.interactKeyWhitelistBlocks_comment);
        INTERACT_KEY_WHITELIST_BLOCKS = builder.addConfig(ModConfigTag.interactKeyWhitelistBlocks_path, Collections.emptyList());

        builder.addComment(ModConfigTag.interactKeyWhitelistEntities_comment);
        INTERACT_KEY_WHITELIST_ENTITIES = builder.addConfig(ModConfigTag.interactKeyWhitelistEntities_path, Collections.emptyList());

        builder.addComment(ModConfigTag.interactKeyBlacklistBlocks_comment);
        INTERACT_KEY_BLACKLIST_BLOCKS = builder.addConfig(ModConfigTag.interactKeyBlacklistBlocks_path, Collections.emptyList());

        builder.addComment(ModConfigTag.interactKeyBlacklistEntities_comment);
        INTERACT_KEY_BLACKLIST_ENTITIES = builder.addConfig(ModConfigTag.interactKeyBlacklistEntities_path, Collections.emptyList());

        builder.finishBuild();
    }

    // 三个全局系数，用于客户端枪械文本提示，需要同步
    public static IModConfigSpec<Double> DAMAGE_BASE_MULTIPLIER;
    public static IModConfigSpec<Double> ARMOR_IGNORE_BASE_MULTIPLIER;
    public static IModConfigSpec<Double> HEAD_SHOT_BASE_MULTIPLIER;
    public static IModConfigSpec<Double> WEIGHT_SPEED_MULTIPLIER;

    private static void base_multiplier(IModConfigSpecBuilder builder) {
        builder.startBuild(ModConfigTag.baseMultiplier_path);

        builder.addComment(ModConfigTag.damageBaseMultiplier_comment);
        DAMAGE_BASE_MULTIPLIER = builder.addConfig(ModConfigTag.damageBaseMultiplier_path, 1.0, 0.0, Double.MAX_VALUE);

        builder.addComment(ModConfigTag.armorIgnoreBaseMultiplier_comment);
        ARMOR_IGNORE_BASE_MULTIPLIER = builder.addConfig(ModConfigTag.armorIgnoreBaseMultiplier_path, 1.0, 0.0, Double.MAX_VALUE);

        builder.addComment(ModConfigTag.headShotBaseMultiplier_comment);
        HEAD_SHOT_BASE_MULTIPLIER = builder.addConfig(ModConfigTag.headShotBaseMultiplier_path, 1.0, 0.0, Double.MAX_VALUE);

        builder.addComment(ModConfigTag.weightSpeedMultiplier_comment);
        WEIGHT_SPEED_MULTIPLIER = builder.addConfig(ModConfigTag.weightSpeedMultiplier_path, 0.015, -1.0, Double.MAX_VALUE);

        builder.finishBuild();
    }

    public static IModConfigSpec<List<String>> HEAD_SHOT_AABB; // 需要同步到客户端，方便客户端 debug 显示碰撞箱
    public static IModConfigSpec<Integer> AMMO_BOX_STACK_SIZE; // 子弹盒存储上限需要客户端显示支持
    public static IModConfigSpec<List<List<String>>> CLIENT_GUN_PACK_DOWNLOAD_URLS; // 客户端需要下载的枪械包
    public static IModConfigSpec<Boolean> ENABLE_PRONE; // 禁用趴下战术动作
    public static IModConfigSpec<Boolean> ENABLE_TABLE_FILTER;
    public static IModConfigSpec<Boolean> SERVER_SHOOT_NETWORK_V;
    public static IModConfigSpec<Boolean> SERVER_SHOOT_COOLDOWN_V;

    private static void misc(IModConfigSpecBuilder builder) {
        builder.startBuild(ModConfigTag.misc_path);

        builder.addComments(ModConfigTag.headShotAABB_comment);
        HEAD_SHOT_AABB = builder.addConfig(ModConfigTag.headShotAABB_path, Collections.emptyList());

        builder.addComment(ModConfigTag.ammoBoxStackSize_comment);
        AMMO_BOX_STACK_SIZE = builder.addConfig(ModConfigTag.ammoBoxStackSize_path, 3, 1, Integer.MAX_VALUE);

        builder.addComment(ModConfigTag.clientGunPackDownloadUrls_comment);
        CLIENT_GUN_PACK_DOWNLOAD_URLS = builder.addConfig(ModConfigTag.clientGunPackDownloadUrls_path, Collections.emptyList());

        builder.addComment(ModConfigTag.enableProne_comment);
        ENABLE_PRONE = builder.addConfig(ModConfigTag.enableProne_path, true);

        builder.addComment(ModConfigTag.enableTableFilter_comment);
        ENABLE_TABLE_FILTER = builder.addConfig(ModConfigTag.enableTableFilter_path, true);

        builder.addComment(ModConfigTag.serverShootNetworkV_comment);
        SERVER_SHOOT_NETWORK_V = builder.addConfig(ModConfigTag.serverShootNetworkV_path, true);

        builder.addComments(ModConfigTag.serverShootCooldownV_comment);
        SERVER_SHOOT_COOLDOWN_V = builder.addConfig(ModConfigTag.serverShootCooldownV_path, true);

        builder.finishBuild();
    }
}
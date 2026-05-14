/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.config.SyncConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadAABBData {
    private static final Map<ResourceLocation, AABB> AABB_CHECK = new HashMap<>();
    // 解析格式：minecraft:herobrine [-0.225, 1.35, -0.225, 0.225, 1.8, 0.225]
    // 生物ID + 碰撞箱
    private static final Pattern REG = Pattern.compile("^([a-z0-9_.-]+:[a-z0-9/._-]+)\s*?\\[([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),*?\s*?]");

    public static void reloadHeadAABB() {
        AABB_CHECK.clear();
        for (String entry : SyncConfig.HEAD_SHOT_AABB.get()) {
            addHeadAABB(entry);
        }
    }

    @ApiStatus.Internal
    public static @Nullable AABB addHeadAABB(String entry) {
        Matcher matcher = REG.matcher(entry);
        if (matcher.find()) {
            var id = CustomGun.getMcRegistry().createResourceLocation(matcher.group(1));
            double x1 = Double.parseDouble(matcher.group(2));
            double y1 = Double.parseDouble(matcher.group(3));
            double z1 = Double.parseDouble(matcher.group(4));
            double x2 = Double.parseDouble(matcher.group(5));
            double y2 = Double.parseDouble(matcher.group(6));
            double z2 = Double.parseDouble(matcher.group(7));
            AABB aabb = new AABB(x1, y1, z1, x2, y2, z2);
            AABB_CHECK.put(id, aabb);
            return aabb;
        }
        return null;
    }

    public static @Nullable AABB getHeadAABB(ResourceLocation id) {
        return AABB_CHECK.get(id);
    }
}

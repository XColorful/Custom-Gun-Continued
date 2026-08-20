/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.gui.overlay;

/**
 * 放在overlay manager不合适，但是不管是作为过渡还是搁置，也就烂这了呗
 * 事件本身不维护信息
 * 原模组逻辑隐含了“调用一次”对应“增加一次”，即扩展模组手动更新会破坏状态
 * 定制功能应该手动监听，而不是调用这个老接口
 */
public interface OverlayStateAccessor {

    // --------Crosshair--------

    void setHitTimestamp(long currentTimeMillis);
    void setKillTimestamp(long currentTimeMillis);
    void setHeadshotTimestamp(long currentTimeMillis);

    long getHitTimestamp();
    long getKillTimestamp();
    long getHeadshotTimestamp();

    // --------Deprecated--------

    @Deprecated(forRemoval = true) default void markHitTimestamp(long currentTimeMillis) {
        setHitTimestamp(currentTimeMillis);
    }
    @Deprecated(forRemoval = true) default void markKillTimestamp(long currentTimeMillis) {
        setKillTimestamp(currentTimeMillis);
    }
    @Deprecated(forRemoval = true) default void markHeadShotTimestamp(long currentTimeMillis) {
        setHeadshotTimestamp(currentTimeMillis);
    }
}

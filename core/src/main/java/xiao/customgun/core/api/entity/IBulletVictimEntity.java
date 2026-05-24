/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity;

public interface IBulletVictimEntity {

    /**
     * 重置击退效果，实体此时恢复正常原版击退逻辑
     */
    void cgc$resetKnockBackStrength();

    /**
     * 获取击退强度，负数表示使用原版击退
     */
    float cgc$getKnockBackStrength();

    /**
     * 设置击退强度
     */
    void cgc$setKnockBackStrength(float strength);
}

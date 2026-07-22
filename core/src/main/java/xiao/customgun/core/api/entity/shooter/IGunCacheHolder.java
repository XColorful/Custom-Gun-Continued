/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity.shooter;

import org.jetbrains.annotations.Nullable;

public interface IGunCacheHolder {

    /**
     * 更新枪械的配件属性修改值
     * <p>
     * 通过将配件修改的属性值缓存在实体上，避免频繁的计算，提升性能
     *
     * @param propertyCache 更新完的配件属性修改值
     */
    void cgc$updateGunPropertyCache(ShooterGunPropertyCache propertyCache);

    /**
     * 获取配件属性修改值缓存
     *
     * @return 绝大部分情况下，这个数值都不可能为 null
     */
    @Nullable ShooterGunPropertyCache cgc$getGunPropertyCache();
}

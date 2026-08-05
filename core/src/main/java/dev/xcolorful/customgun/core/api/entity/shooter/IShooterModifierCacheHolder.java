/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import org.jetbrains.annotations.Nullable;

public interface IShooterModifierCacheHolder {

    /**
     * 更新枪械的配件属性修改值
     * <p>
     * 通过将配件修改的属性值缓存在实体上，避免频繁的计算，提升性能
     *
     * @param modifierCache 更新完的配件属性修改值
     */
    void cgc$updateGunModifierCache(ShooterGunModifierCache modifierCache);

    /**
     * 获取配件属性修改值缓存
     *
     * @return 绝大部分情况下，这个数值都不可能为 null
     */
    @Nullable ShooterGunModifierCache cgc$getGunModifierCache();
}

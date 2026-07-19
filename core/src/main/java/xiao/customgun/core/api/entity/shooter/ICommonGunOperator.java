/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity.shooter;

public interface ICommonGunOperator {

    // --------行为动作--------

    void cgc$crawl(boolean isCrawl);

    // --------枪械操作--------

    void cgc$switchFireMode();
    void cgc$aim(boolean isAim);
    void cgc$melee();
    void cgc$bolt();
    void cgc$reload();

    // --------Deprecated--------

    @Deprecated default void cgc$fireSelect() {
        this.cgc$switchFireMode();
    }
}

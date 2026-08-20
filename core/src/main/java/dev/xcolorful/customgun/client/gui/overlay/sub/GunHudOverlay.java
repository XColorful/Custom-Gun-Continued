/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.overlay.sub;

import dev.xcolorful.customgun.core.developer.PlannedRefactor;

/**
 * 可以放扩展模组，(反正各作者最终都会自制)做不同类型的枪械HUD，主模组卸下包袱
 * 如果没有一个默认HUD的话，就只能背包里看弹药数，而且看不到开火模式
 * overlay很多，可以改成manager的形式，主模组提供注册入口，而且都是aboveall的话，跟直接监听gui event没啥区别
 * 目前还是搁置
 */
@Deprecated
public class GunHudOverlay {
    static {
        /**
         * 预计枪械hud做成：
         * 渲染的数据放在主线程更新，hud类开放并发容器给主线程
         * 但是枪的逻辑目前还没统一，依旧到处拉屎，这是很恶心的代码味道
         */
        if (PlannedRefactor.UNIFY_GUN_API) {}
    }
}

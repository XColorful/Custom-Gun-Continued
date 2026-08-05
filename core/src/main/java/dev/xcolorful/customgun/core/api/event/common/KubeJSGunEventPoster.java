/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.event.common;

import dev.xcolorful.customgun.core.api.event.CustomEvent;

/**
 * KubeJS的兼容会改成以下模式:
 * <ol>
 *     <li>单独的模组</li>
 *     <li>对所有事件的公共父类{@link CustomEvent}mixin本接口的替代物</li>
 *     <li>维护注册列表，使得可以通过 配置文件/指令 动态 监听/取消监听 某个事件 (同BattleRoyale的函数API指令)</li>
 * </ol>
 * 避免了原实现方式的以下问题:
 * <ol>
 *     <li>事件创建不代表已经post</li>
 *     <li>不需要对每个事件类都注入接口(缺少新的公共父类，只有Forge的Event)</li>
 * </ol>
 */
@Deprecated(forRemoval = true)
public interface KubeJSGunEventPoster {
}

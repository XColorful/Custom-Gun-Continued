/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.event.EventDispatcher;

public interface ICustomEvent extends IEvent {

    default EventType getType() {
        return null;
    }
    CustomEventType getEventType();

    default Class<? extends ICustomEvent> getCustomEventClass() {
        return this.getClass();
    }

    /**
     * 优化掉 Post 自定义事件时的 Hash get 开销，方便 JIT 内联
     * 具体事件类只需返回一个静态 EventDispatcher 即可
     * 不提供默认实现理应能辅助单态内联，但测试下来似乎没多大提升
     * 具体类从 {@link CustomGun#getEventPoster()} {@link ICustomEventPoster#getEventDispatcher} 获取一个 static final 引用即可
     */
    @ApiStatus.Internal
    @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher();
}

/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.api.event.ICustomEventPoster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventPoster implements ICustomEventPoster {

    private static class EventPosterHolder {
        private static final EventPoster INSTANCE = new EventPoster();
    }

    public static ICustomEventPoster get() {
        return EventPosterHolder.INSTANCE;
    }

    protected EventPoster() {}

    // 统一管理 EventDispatcher
    // 使得没有创建事件实例时也能获取，且事件类的静态 EventDispatcher 也从这统一获取
    // ConcurrentHashMap 比全局锁要好
    private final Map<Class<? extends ICustomEvent>, EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType>> eventDispatchers = new ConcurrentHashMap<>();
    @ApiStatus.Internal public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher(Class<? extends ICustomEvent> eventClass) {
        return eventDispatchers.computeIfAbsent(eventClass, k -> new EventDispatcher<>());
    }

    @Deprecated(forRemoval = false)
    public static boolean postEvent(ICustomEvent customEvent) {
        return CustomGun.getEventPoster().postCustomEvent(customEvent);
    }
    // 事件发布入口
    public boolean postCustomEvent(ICustomEvent customEvent) {
        // 传入固定结构的 Lambda 以确保 dispatch 内部的 invoker 保持单态，从而触发 JIT 优化
        customEvent.getEventDispatcher().dispatch(
                customEvent,
                (handler, event) -> handler.handleEvent(event.getEventType(), event)
        );
        return customEvent.isCanceled();
    }
}

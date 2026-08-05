/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.event.IEventRegister;

import java.util.function.Consumer;

public class ClientDelayedEvent<T> implements IEventHandler {

    @Override public String getEventHandlerName() {
        return String.format("ClientDelayedEvent:n(%s)%s", this.ticksLeft, this.description);
    }

    private final Consumer<T> task;
    private final T parameter;
    private int ticksLeft;
    private final String description;

    /**
     * 创建一个延迟事件，并立即注册到事件总线
     * @param task      要执行的函数
     * @param parameter 传递给函数的参数
     * @param delay     延迟的tick数
     * @param description 写入log的说明
     */
    public ClientDelayedEvent(Consumer<T> task, T parameter, int delay, String description) {
        this.task = task;
        this.parameter = parameter;
        this.ticksLeft = delay;
        this.description = description;
        IEventRegister eventRegister = CustomGun.getEventRegister();
        eventRegister.register(this, EventType.CLIENT_TICK_EVENT);
    }

    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.CLIENT_TICK_EVENT) {
            onClientTick((IClientTickEvent) event);
        } else {
            CustomGun.LOGGER.warn("{} received wrong event type: {}", getEventHandlerName(), eventType);
        }
    }

    /**
     * 事件监听器：在每个服务器tick结束时触发
     */
    private void onClientTick(IClientTickEvent event) {
        if (--this.ticksLeft == 0) {
            run();
            CustomGun.LOGGER.debug("Process {}", this.ticksLeft);
        }
        if (this.ticksLeft <= 0) {
            IEventRegister eventRegister = CustomGun.getEventRegister();
            eventRegister.unregister(this, EventType.SERVER_TICK_EVENT);
        }
    }

    /**
     * 执行任务，不手动try
     */
    private void run() {
        this.task.accept(this.parameter);
    }
}
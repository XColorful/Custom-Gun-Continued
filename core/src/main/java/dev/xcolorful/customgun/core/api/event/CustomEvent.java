/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.event;

public abstract class CustomEvent implements ICustomEvent {

    private boolean isCanceled = false;

    public CustomEvent() {
    }

    @Override
    public CustomEventType getEventType() {
        return CustomEventType.CUSTOM_EVENT;
    }

    @Override
    public boolean isCanceled() {
        return this.isCanceled;
    }

    @Override
    public void setCanceled(boolean cancel) {
        if (isCancelable()) {
            this.isCanceled = cancel;
        }
    }

    @Override
    public Object getEvent() {
        return this;
    }
}

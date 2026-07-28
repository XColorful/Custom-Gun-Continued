/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.util;

import xiao.customgun.core.api.event.CycledEvent;

import java.util.function.BooleanSupplier;

/**
 * Go to {@link CycledEvent}
 */
@Deprecated(forRemoval = true)
public class CycleTaskHelper {

    public static void addCycleTask(BooleanSupplier task, long periodMs, int cycles) {
        CycledEvent.create(task, 0, periodMs, cycles <= 0 ? Integer.MAX_VALUE : cycles); // 每tick执行一次，可以持续2147483647 (3.4年)
    }

    public static void addCycleTask(BooleanSupplier task, long delayMs, long periodMs, int cycles) {
        CycledEvent.create(task, delayMs, periodMs, cycles <= 0 ? Integer.MAX_VALUE : cycles);
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.statemachine;

public class TrackArrayMismatchException extends RuntimeException {

    public TrackArrayMismatchException(String msg) {
        super(msg);
    }

    public TrackArrayMismatchException(){
        super();
    }
}

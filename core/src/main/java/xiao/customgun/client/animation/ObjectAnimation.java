/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.animation;

// TODO
public class ObjectAnimation {

    public enum PlayType {
        /**
         * 播放一次，停留在最后一帧
         */
        PLAY_ONCE_HOLD,
        /**
         * 播放一次后停止
         */
        PLAY_ONCE_STOP,
        /**
         * 循环播放
         */
        LOOP
    }
}

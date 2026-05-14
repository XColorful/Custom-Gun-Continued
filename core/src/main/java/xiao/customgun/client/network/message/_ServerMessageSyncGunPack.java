/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message;

import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.network.message.ServerMessageSyncGunPack;

@ApiStatus.Internal
public class _ServerMessageSyncGunPack {

    public static void doSync(ServerMessageSyncGunPack message, boolean remoteConnection) {
        if (remoteConnection) {
            // TODO CommonAssetsManager
        }
        // TODO CommonNetworkCache
        // 通知客户端重新构建ClientIndex
        // TODO ClientIndexManager
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message;

import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.CustomGun;
import xiao.customgun.client.resource._AssetsInstanceManager;
import xiao.customgun.client.resource.network.SyncDataCache;
import xiao.customgun.core.network.message.ServerMessageSyncGunPack;
import xiao.customgun.core.resource._AllDataManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@ApiStatus.Internal
public class _ServerMessageSyncGunPack {
    private static final AtomicLong TICKET_COUNTER = new AtomicLong(0);
    private static volatile long lastActiveTicket = -1;
    private static volatile long lastCompletedTicket = -1;

    public static void doSync(ServerMessageSyncGunPack message, Consumer<Runnable> handler, boolean remoteConnection) {
        if (remoteConnection) {
            _AllDataManager.clearInstance();
        }

        final long currentTicket = TICKET_COUNTER.incrementAndGet();
        lastActiveTicket = currentTicket;
        // 扔到后台线程池去解析
        CompletableFuture.supplyAsync(() -> {
            if (currentTicket < lastActiveTicket) {
                return null; // 有新包送达，老包直接提前放弃
            }

            // 纯Pojo解析可以放网络线程异步处理
            long t0 = System.nanoTime();
            var parsed = SyncDataCache.INSTANCE.rebuildFromNetworkAsync(message.cache());
            long t1 = System.nanoTime();
            CustomGun.LOGGER.debug("ServerMessageSyncGunPack: rebuildFromNetworkAsync: {} ms", (t1 - t0) / 1_000_000.0);
            return parsed;
        }, ForkJoinPool.commonPool()).thenAcceptAsync((parsedResult) -> {
            // 此时已由handler::accept切回主线程排队队列中

            if (parsedResult == null
                    || currentTicket < lastActiveTicket
                    || currentTicket <= lastCompletedTicket) {
                return; // 过时校验不通过，拒绝合流与刷新
            }

            // 在主线程完成全套字段引用的替换
            long t2 = System.nanoTime();
            SyncDataCache.INSTANCE.setParseResult(parsedResult);
            long t3 = System.nanoTime();
            CustomGun.LOGGER.debug("ServerMessageSyncGunPack: setParseResult: {} ms", (t3 - t2) / 1_000_000.0);

            // 通知客户端重新构建PojoInstance (主线程)
            lastCompletedTicket = currentTicket;
            long t4 = System.nanoTime();
            _AssetsInstanceManager.reload();
            long t5 = System.nanoTime();
            CustomGun.LOGGER.debug("ServerMessageSyncGunPack: reload AssetsInstanceManager: {} ms", (t5 - t4) / 1_000_000.0);
        }, handler::accept);
    }
}

package dev.xcolorful.customgun.core.util;

import net.minecraft.server.packs.resources.PreparableReloadListener;

public class ResourceUtils {

    /**
     * 仅用于在 {@link dev.xcolorful.customgun.core.api.event.IAddServerReloadListenerEvent} (或客户端事件) 提供跨版本封装
     */
    public static PreparableReloadListener asyncReloadListener(Runnable action) {
        // [1.20.1, 1.21.4)
        // reload(PreparationBarrier, ResourceManager, ProfilerFiller, ProfilerFiller, Executor, Executor)
//        return (barrier, resourceManager, preparationProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
//            return barrier
//                    .wait(Void.TYPE)
//                    .thenRunAsync(action, gameExecutor);
//        };

        // [1.21.4, 1.21.10)
        // reload(PreparationBarrier, ResourceManager, Executor, Executor)
        return (barrier, resourceManager, backgroundExecutor, gameExecutor) -> {
            return barrier
                    .wait(Void.TYPE)
                    .thenRunAsync(action, gameExecutor);
        };

        // [1.21.10, )
        // reload(SharedState, Executor, PreparationBarrier, Executor)
//        return (sharedState, backgroundExecutor, barrier, gameExecutor) -> {
//            return barrier.wait("")
//                    .thenRunAsync(action, gameExecutor);
//        };
    }
}

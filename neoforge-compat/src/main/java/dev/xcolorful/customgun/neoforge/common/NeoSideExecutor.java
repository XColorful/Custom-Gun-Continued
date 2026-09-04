package dev.xcolorful.customgun.neoforge.common;

import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.common.McSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.EffectiveSide;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class NeoSideExecutor implements ISideExecutor {

    @Override
    public void executeOn(McSide dist, Supplier<Runnable> toRun) {
        if (FMLEnvironment.dist == McSideHelper.convert(dist)) {
            toRun.get().run();
        }
    }

    @Override
    public void executeOnIsolated(McSide dist, Supplier<ISideExecutor.SideRunnable> toRun) {
        if (FMLEnvironment.dist == McSideHelper.convert(dist)) {
            toRun.get().run();
        }
    }

    @Override
    public McLogicalSide getLogicalSide() {
        /*
        EffectiveSide.get() 对不在 SidedThreadGroup 里的线程（如 资源reload 的完成线程）一律返回 CLIENT
        专用服务端可以用物理 side 兜底
        单人游戏仍然存在无法区分服务端逻辑的问题
         */
        if (dev.xcolorful.customgun.CustomGun.getMcSide().isServerSide()) return McLogicalSide.SERVER;

        return McSideHelper.convert(EffectiveSide.get());
    }

    @Override
    public Executor getMainThreadExecutor(McLogicalSide side) {
        return null;
    }
}

package xiao.customgun.neoforge.common;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import xiao.customgun.core.api.common.ISideExecutor;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.common.McSide;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class NeoSideExecutor implements ISideExecutor {

    @Override
    public void unsafeRunWhenOn(McSide dist, Supplier<Runnable> toRun) {
        if (FMLEnvironment.dist == McSideHelper.convert(dist)) {
            toRun.get().run();
        }
    }

    @Override
    public void safeRunWhenOn(McSide dist, Supplier<ISideExecutor.SideRunnable> toRun) {
        if (FMLEnvironment.dist == McSideHelper.convert(dist)) {
            toRun.get().run();
        }
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McSideHelper.convert(EffectiveSide.get());
    }

    @Override
    public Executor getMainThreadExecutor(McLogicalSide side) {
        return LogicalSidedProvider.WORKQUEUE.get(McSideHelper.convert(side));
    }
}

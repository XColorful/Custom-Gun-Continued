package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.Nullable;

public interface IComputeFovEvent extends IEvent {

    GameRenderer getGameRenderer();
    Camera getCamera();
    double getPartialTick();

    float getFOV();
    /**
     * 改用低精度的 {@link IComputeFovEvent#getFOV()}
     */
    @Deprecated(since = "1.21.4")
    default double getFOVDouble() {
        return getFOV();
    }
    void setFOV(float fov);


    /**
     * 获取当前 FOV 是否来源于玩家设置中的 FOV 值
     * <ul>
     *     该信息用于区分普通玩家视角 FOV 与特殊 FOV 场景
     *     <li>比如只修改玩家正常视角的 FOV，而不影响望远镜、特殊相机等固定 FOV 场景</li>
     *     <li>自26.2起不再有效，使用方应避免使用该接口作为唯一判断条件</li>
     * </ul>
     */
    @Deprecated(since = "26.2")
    default @Nullable Boolean useConfiguredFov() {
        return null;
    }
}

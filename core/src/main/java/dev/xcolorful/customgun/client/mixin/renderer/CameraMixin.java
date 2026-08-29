package dev.xcolorful.customgun.client.mixin.renderer;

import dev.xcolorful.customgun.client.renderer.item.gun.GunCameraHelper;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
为了跨版本提前知道26.1neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("26.1.x")
//@Mixin(Camera.class)
public class CameraMixin {

    /**
     * <ul>
     *     26.2 起
     *     <li>{@code ViewportEvent.ComputeFov} 不再携带 {@code usedConfiguredFov()}</li>
     *     <li>世界 FOV（{@code calculateFov}）与手部/HUD FOV（{@code calculateHudFov}）都走同一事件</li>
     *     <li>这里在两者入口标记 {@link GunCameraHelper.State#isLevelFov}，供 {@link GunCameraHelper} 区分</li>
     * </ul>
     */
    private void cgc$markLevelFov(float partialTicks,
                                  CallbackInfoReturnable<Float> cir) {
        GunCameraHelper.State.isLevelFov = true;
    }
    private void cgc$markHudFov(float partialTicks,
                                CallbackInfoReturnable<Float> cir) {
        GunCameraHelper.State.isLevelFov = false;
    }
}

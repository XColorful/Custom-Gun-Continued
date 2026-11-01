package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.event.render.ItemInHandBobEvent;
import dev.xcolorful.customgun.client.api.event.render.LevelBobEvent;
import dev.xcolorful.customgun.client.renderer.item.gun.GunCameraHelper;
import dev.xcolorful.customgun.client.renderer.model.TextRenderOverlay;
import dev.xcolorful.customgun.client.renderer.victim.GunHurtBobTweak;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /*
    对不存在的方法shadow会导致整个类的mixin失效
    不需要的方法就不要去shadow
     */
//    @Deprecated(since = "26.2")
//    @Shadow
//    public abstract Minecraft getMinecraft();

//    @Shadow
//    public abstract void render(DeltaTracker deltaTracker,
//                                boolean pRenderLevel);

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    public void cgc$onBobHurt( // CameraRenderState cameraState
                              PoseStack poseStack,
                              float pPartialTicks, CallbackInfo ci) {
        // 取消受伤导致的视角摇晃
        if (Minecraft.getInstance().getCameraEntity() instanceof LocalPlayer player && !player.isDeadOrDying()) {
            if (GunHurtBobTweak.onHurtBobTweak(player, poseStack, pPartialTicks)) {
                ci.cancel();
                return;
            }
        }

        // 触发其他事件
        boolean cancel; {
            if (GunCameraHelper.State.renderItemInHand()) {
                cancel = CustomGun.getEventPoster().postCustomEvent(new ItemInHandBobEvent.Hurt());
            } else {
                cancel = CustomGun.getEventPoster().postCustomEvent(new LevelBobEvent.Hurt());
            }
        }

        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void cgc$onBobView( // CameraRenderState cameraState
                              PoseStack poseStack,
                              float pPartialTicks, CallbackInfo ci) {
        boolean cancel; {
            if (GunCameraHelper.State.renderItemInHand()) {
                cancel = CustomGun.getEventPoster().postCustomEvent(new ItemInHandBobEvent.View());
            } else {
                cancel = CustomGun.getEventPoster().postCustomEvent(new LevelBobEvent.View());
            }
        }

        if (cancel) {
            ci.cancel();
        }
    }

    /**
     * <ul>
     *     1.21.6起:
     *     <li>手部 FOV 在 {@code renderItemInHand} 之外计算 ({@code render} 里先 {@code getFov(camera, f, false)} 再 {@code renderItemInHand}）</li>
     *     <li>导致下面 renderItemInHand 的 HEAD/RETURN 注入无法覆盖手部 FOV 计算时刻</li>
     *     <li>因此改回用 {@code getFov} 的 {@code useFovSetting} 参数区分 world（true）与 hand（false）</li>
     *     <li>这是 1.21.6 唯一能覆盖两个 FOV 计算点的位置</li>
     * </ul>
     * @deprecated 自26.1.x，改用{@link CameraMixin}
     */
    @Deprecated(since = "26.1.x")
    @Inject(method = "getFov", at = @At("HEAD"))
    public void cgc$switchRenderType(Camera pActiveRenderInfo,
                                     float pPartialTicks,
                                     boolean pUseFOVSetting,
                                     CallbackInfoReturnable<Double> cir) {
        GunCameraHelper.State.isLevelFov = pUseFOVSetting;
    }

    /**
     * 文字覆盖层的两个刷新点，见 {@link TextRenderOverlay}。
     * <ul>
     *     <li>光影下 Iris 把手部 pass 搬进了 {@code LevelRenderer#render}：在它返回之后刷新
     *     （此时手部已画完，且还没到 vanilla 的 {@code clearDepthTexture}，主深度里留着目镜几何）</li>
     *     <li>无光影时手部在 {@code renderLevel} 内部靠后才画：等它画完再刷新</li>
     *     <li>两处都调用，队列为空时只是一次判空；两次刷新不会重复画同一批文字</li>
     * </ul>
     */
    @ApiStatus.AvailableSince("26.2")
//    @Inject(
//            method = "renderLevel",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
//                    shift = At.Shift.AFTER
//            )
//    )
    private void cgc$flushTextOverlayAfterLevel(CallbackInfo ci) {
        TextRenderOverlay.flush();
    }

    @ApiStatus.AvailableSince("26.2")
//    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void cgc$flushTextOverlayAtEnd(CallbackInfo ci) {
        TextRenderOverlay.flush();
    }

//    @Deprecated(since = "1.21.6")
//    @Inject(method = "renderItemInHand", at = @At("HEAD"))
//    private void cgc$beforeRenderItemInHand(Camera camera,
//                                            float partialTick,
//                                            Matrix4f projectionMatrix,
//                                            CallbackInfo ci) {
//        GunCameraHelper.State.renderItemInHand = true;
//    }
//    @Deprecated(since = "1.21.6")
//    @Inject(method = "renderItemInHand", at = @At("RETURN"))
//    private void cgc$afterRenderItemInHand(Camera camera,
//                                           float partialTick,
//                                           Matrix4f projectionMatrix,
//                                           CallbackInfo ci) {
//        GunCameraHelper.State.renderItemInHand = false;
//    }
}

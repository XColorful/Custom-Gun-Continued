package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.event.render.BeforeRenderHandEvent;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
为了跨版本提前知道26.3neoforge的移植方式，添加此类作为占位符
 */
/**
 * <ul>
 *     <li>26.2 的 {@code ItemInHandRenderer.submitHandsWithItems} 在 26.3 迁到了{@code FirstPersonHandsAndItemsRenderer#submitHandsWithItems}</li>
 *     <li>玩家/光照参数被 {@code PlayerRenderState} + {@code FirstPersonHandsAndItemsRenderState} 取代</li>
 *     <li>见 {@link FirstPersonHandsAndItemsMixin}</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.3")
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class FirstPersonHandsAndItemsRendererMixin {

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    public void cgc$onBeforeRenderHand(float partialTicks, PoseStack poseStack,
                                       SubmitNodeCollector nodeCollector,
                                       PlayerRenderState playerState,
                                       FirstPersonHandsAndItemsRenderState state,
                                       CallbackInfo ci) {
        CustomGun.getEventPoster().postCustomEvent(new BeforeRenderHandEvent(poseStack));
    }
}

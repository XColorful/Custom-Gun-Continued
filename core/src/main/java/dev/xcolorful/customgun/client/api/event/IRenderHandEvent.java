package dev.xcolorful.customgun.client.api.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRenderHandEvent extends IEvent {

    PoseStack getPoseStack();

    @Deprecated(since = "1.21.10")
    @Nullable Object getMultiBufferSource();
    @ApiStatus.AvailableSince("1.21.10")
    @Nullable SubmitNodeCollector getSubmitNodeCollector();

    /**
     * @return 1.21.10以前返回{@code MultiBufferSource}，1.21.10及以后返回{@code SubmitNodeCollector}
     */
    @NotNull SubmitNodeCollector getMultiBufferSource_SubmitNodeCollector();

    int getPackedLight();
    float getPartialTick();
    float getInterpolatedPitch();
    float getSwingProgress();
    float getEquipProgress();

    InteractionHand getHand();
    ItemStack getItemStack();
}

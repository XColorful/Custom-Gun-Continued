package dev.xcolorful.customgun.neoforgeclient.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.api.event.IRenderHandEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoRenderHandEvent extends NeoEvent implements IRenderHandEvent {

    protected RenderHandEvent renderHandEvent;

    public NeoRenderHandEvent(Event event) {
        super(event);
        if (event instanceof RenderHandEvent eventIn) {
            this.renderHandEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderHandEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.RENDER_HAND_EVENT;
    }

    @Override public PoseStack getPoseStack() {
        return renderHandEvent.getPoseStack();
    }

    @Override public @Nullable MultiBufferSource getMultiBufferSource() {
        return renderHandEvent.getMultiBufferSource();
    }
    @Override public @Nullable Object getSubmitNodeCollector() {
        return null;
    }

    @Override public @NotNull MultiBufferSource getMultiBufferSource_SubmitNodeCollector() {
        return renderHandEvent.getMultiBufferSource();
    }

    @Override public int getPackedLight() {
        return renderHandEvent.getPackedLight();
    }
    @Override public float getPartialTick() {
        return renderHandEvent.getPartialTick();
    }
    @Override public float getInterpolatedPitch() {
        return renderHandEvent.getInterpolatedPitch();
    }
    @Override public float getSwingProgress() {
        return renderHandEvent.getSwingProgress();
    }
    @Override public float getEquipProgress() {
        return renderHandEvent.getEquipProgress();
    }

    @Override public InteractionHand getHand() {
        return renderHandEvent.getHand();
    }
    @Override public ItemStack getItemStack() {
        return renderHandEvent.getItemStack();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoRenderHandEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}

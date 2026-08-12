package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IComputeFovEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeComputeFovEvent extends ForgeEvent implements IComputeFovEvent {

    protected ViewportEvent.ComputeFov computeFov;

    public ForgeComputeFovEvent(Event event) {
        super(event);
        if (event instanceof ViewportEvent.ComputeFov eventIn) {
            this.computeFov = eventIn;
        } else {
            throw new RuntimeException("Expected ComputeFov but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.COMPUTE_FOV_EVENT;
    }

    @Override public GameRenderer getGameRenderer() {
        return computeFov.getRenderer();
    }
    @Override public Camera getCamera() {
        return computeFov.getCamera();
    }
    @Override public double getPartialTick() {
        return computeFov.getPartialTick();
    }

    @Override public float getFOV() {
        return (float) computeFov.getFOV();
    }
    @Override public double getFOVDouble() {
        return computeFov.getFOV();
    }
    @Override public void setFOV(float fov) {
        computeFov.setFOV(fov);
    }
    @Override public Boolean useConfiguredFov() {
        return computeFov.usedConfiguredFov();
    }


    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeComputeFovEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}

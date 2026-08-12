package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IComputeCameraAnglesEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jetbrains.annotations.Nullable;

public class NeoComputeCameraAnglesEvent extends NeoEvent implements IComputeCameraAnglesEvent {

    protected ViewportEvent.ComputeCameraAngles computeCameraAngles;

    public NeoComputeCameraAnglesEvent(Event event) {
        super(event);
        if (event instanceof ViewportEvent.ComputeCameraAngles eventIn) {
            this.computeCameraAngles = eventIn;
        } else {
            throw new RuntimeException("Expected ComputeCameraAngles but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.COMPUTE_CAMERA_ANGLES_EVENT;
    }

    @Override public GameRenderer getGameRenderer() {
        return computeCameraAngles.getRenderer();
    }
    @Override public Camera getCamera() {
        return computeCameraAngles.getCamera();
    }
    @Override public double getPartialTick() {
        return computeCameraAngles.getPartialTick();
    }

    @Override public float getYaw() {
        return computeCameraAngles.getYaw();
    }
    @Override public float getPitch() {
        return computeCameraAngles.getPitch();
    }
    @Override public float getRoll() {
        return computeCameraAngles.getRoll();
    }

    @Override public void setYaw(float yaw) {
        computeCameraAngles.setYaw(yaw);
    }
    @Override public void setPitch(float pitch) {
        computeCameraAngles.setPitch(pitch);
    }
    @Override public void setRoll(float roll) {
        computeCameraAngles.setRoll(roll);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoComputeCameraAnglesEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}

package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

public interface IComputeCameraAnglesEvent extends IEvent {

    GameRenderer getGameRenderer();
    Camera getCamera();
    double getPartialTick();

    float getYaw();
    float getPitch();
    float getRoll();

    void setYaw(float yaw);
    void setPitch(float pitch);
    void setRoll(float roll);
}

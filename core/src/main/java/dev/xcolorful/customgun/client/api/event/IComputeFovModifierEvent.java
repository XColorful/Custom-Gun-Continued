package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.OptionInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

public interface IComputeFovModifierEvent extends IEvent {

    Player getPlayer();

    float getFovModifier();

    /**
     * 在1.21.4之前，返回的是实时读取{@link OptionInstance#get()}的值
     * <br>
     * 即{@code Minecraft.getInstance().options.fovEffectScale().get().floatValue()}
     */
    @ApiStatus.AvailableSince("1.21.4")
    float getFovScale();

    float getNewFovModifier();

    void setNewFovModifier(float newFovModifier);
}

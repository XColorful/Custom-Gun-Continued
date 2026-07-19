package xiao.customgun.client.api.event.render;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.CustomEvent;

public abstract class RenderBobEvent extends CustomEvent {

    public RenderBobEvent() {
    }
    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }
}

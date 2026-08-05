package dev.xcolorful.customgun.client.api.event.render;

import dev.xcolorful.customgun.core.api.event.CustomEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

public abstract class RenderBobEvent extends CustomEvent {

    public RenderBobEvent() {
    }
    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }
}

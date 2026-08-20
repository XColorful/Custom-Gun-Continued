package dev.xcolorful.customgun.client.api.gui.tooltip;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTooltipView {

    public @Nullable Component pojoLocation;
    public @Nullable Component category;
    public @Nullable Component packInfo;
    public BaseTooltipView() {
    }
}

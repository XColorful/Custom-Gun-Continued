package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IItemTooltipEvent extends IEvent {

    @Nullable Player getPlayer();

    @NotNull ItemStack getItemStack();

    @NotNull List<Component> getToolTip();

    @NotNull TooltipFlag getFlags();
}

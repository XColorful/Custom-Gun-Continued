package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IAddClientReloadListenerEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeAddClientReloadListenerEvent extends ForgeEvent implements IAddClientReloadListenerEvent {

    protected RegisterClientReloadListenersEvent registerClientReloadListenersEvent;

    public ForgeAddClientReloadListenerEvent(Event event) {
        super(event);
        if (event instanceof RegisterClientReloadListenersEvent eventIn) {
            this.registerClientReloadListenersEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RegisterClientReloadListenersEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.ADD_CLIENT_RELOAD_LISTENER_EVENT;
    }

    @Override
    public void addListener(ResourceLocation registryKey, PreparableReloadListener listener) {
        this.registerClientReloadListenersEvent.registerReloadListener(listener);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeAddClientReloadListEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
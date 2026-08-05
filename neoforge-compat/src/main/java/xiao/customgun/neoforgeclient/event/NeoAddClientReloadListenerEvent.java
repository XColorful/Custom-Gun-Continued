package xiao.customgun.neoforgeclient.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.event.IAddClientReloadListenerEvent;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.neoforge.event.NeoEvent;

public class NeoAddClientReloadListenerEvent extends NeoEvent implements IAddClientReloadListenerEvent {

    protected RegisterClientReloadListenersEvent registerClientReloadListenersEvent;

    public NeoAddClientReloadListenerEvent(Event event) {
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
        return "NeoAddClientReloadListenerEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
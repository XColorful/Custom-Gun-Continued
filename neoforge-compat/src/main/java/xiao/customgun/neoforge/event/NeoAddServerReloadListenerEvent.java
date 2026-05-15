package xiao.customgun.neoforge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IAddServerReloadListenerEvent;

public class NeoAddServerReloadListenerEvent extends NeoEvent implements IAddServerReloadListenerEvent {

    protected AddServerReloadListenersEvent addServerReloadListenersEvent;

    public NeoAddServerReloadListenerEvent(Event event) {
        super(event);
        if (event instanceof AddServerReloadListenersEvent eventIn) {
            this.addServerReloadListenersEvent = eventIn;
        } else {
            throw new RuntimeException("Expected AddServerReloadListenersEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.ADD_SERVER_RELOAD_LISTENER_EVENT;
    }

    @Override
    public void addListener(ResourceLocation registryKey, PreparableReloadListener listener) {
        this.addServerReloadListenersEvent.addListener(registryKey, listener);
    }

    @Override
    public ReloadableServerResources getServerResources() {
        return this.addServerReloadListenersEvent.getServerResources();
    }

    @Override
    public RegistryAccess getRegistryAccess() {
        return this.addServerReloadListenersEvent.getRegistryAccess();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoAddServerReloadListenerEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
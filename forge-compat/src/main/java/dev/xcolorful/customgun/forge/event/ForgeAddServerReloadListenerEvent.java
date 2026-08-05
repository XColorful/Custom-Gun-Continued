package dev.xcolorful.customgun.forge.event;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IAddServerReloadListenerEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeAddServerReloadListenerEvent extends ForgeEvent implements IAddServerReloadListenerEvent {

    protected AddReloadListenerEvent addReloadListenerEvent;

    public ForgeAddServerReloadListenerEvent(Event event) {
        super(event);
        if (event instanceof AddReloadListenerEvent eventIn) {
            this.addReloadListenerEvent = eventIn;
        } else {
            throw new RuntimeException("Expected AddReloadListenerEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.ADD_SERVER_RELOAD_LISTENER_EVENT;
    }

    @Override
    public void addListener(ResourceLocation registryKey, PreparableReloadListener listener) {
        this.addReloadListenerEvent.addListener(listener);
    }

    @Override
    public ReloadableServerResources getServerResources() {
        return this.addReloadListenerEvent.getServerResources();
    }

    @Override
    public RegistryAccess getRegistryAccess() {
        return this.addReloadListenerEvent.getRegistryAccess();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeAddServerReloadListenerEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
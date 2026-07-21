package xiao.customgun.neoforge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IServerTickEvent;
import xiao.customgun.core.api.minecraft.CommandLevel;

public class NeoPrepareServerTickEvent extends NeoEvent implements IServerTickEvent {

    protected ServerTickEvent.Pre serverTickEvent;

    public NeoPrepareServerTickEvent(Event event) {
        super(event);
        if (event instanceof ServerTickEvent.Pre eventIn) {
            this.serverTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected ServerTickEvent.Pre but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_SERVER_TICK_EVENT;
    }

    @Override
    public MinecraftServer getServer() {
        return serverTickEvent.getServer();
    }

    @Override
    public @NotNull CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        MinecraftServer server = this.getServer();
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                Vec3.ZERO,
                Vec2.ZERO,
                server.overworld(),
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                server,
                null
        );
    }

    @Override public String getTextName() {
        return "NeoPrepareServerTickEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
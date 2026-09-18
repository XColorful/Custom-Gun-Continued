package dev.xcolorful.customgun.core.api.event;

import net.minecraft.server.MinecraftServer;

public interface IServerTickEvent extends IEvent {

    MinecraftServer getServer();
}

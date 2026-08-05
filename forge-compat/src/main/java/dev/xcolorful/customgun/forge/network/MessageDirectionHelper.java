package dev.xcolorful.customgun.forge.network;

import dev.xcolorful.customgun.core.api.network.MessageDirection;
import net.minecraftforge.network.NetworkDirection;

public class MessageDirectionHelper {

    public static MessageDirection convert(NetworkDirection<?> direction) {
        return direction == NetworkDirection.PLAY_TO_CLIENT
                ? MessageDirection.SERVER_TO_CLIENT
                : MessageDirection.CLIENT_TO_SERVER;
    }

    public static NetworkDirection<?> convert(MessageDirection direction) {
        return direction == MessageDirection.SERVER_TO_CLIENT
                ? NetworkDirection.PLAY_TO_CLIENT
                : NetworkDirection.PLAY_TO_SERVER;
    }
}

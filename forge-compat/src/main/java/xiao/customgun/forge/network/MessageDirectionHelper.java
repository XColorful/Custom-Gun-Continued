package xiao.customgun.forge.network;

import net.minecraftforge.network.NetworkDirection;
import xiao.customgun.core.api.network.MessageDirection;

public class MessageDirectionHelper {

    public static MessageDirection convert(NetworkDirection direction) {
        return direction == NetworkDirection.PLAY_TO_CLIENT
                ? MessageDirection.SERVER_TO_CLIENT
                : MessageDirection.CLIENT_TO_SERVER;
    }

    public static NetworkDirection convert(MessageDirection direction) {
        return direction == MessageDirection.SERVER_TO_CLIENT
                ? NetworkDirection.PLAY_TO_CLIENT
                : NetworkDirection.PLAY_TO_SERVER;
    }
}

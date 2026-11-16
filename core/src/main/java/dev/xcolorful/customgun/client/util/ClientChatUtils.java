package dev.xcolorful.customgun.client.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ClientChatUtils {

    // --------LocalPlayer--------

    /**
     * 向本地玩家发送普通文本消息
     * @param player 接收消息的 LocalPlayer 对象
     * @param message 要发送的字符串消息
     */
    public static void sendMessageToPlayer(@NotNull LocalPlayer player, String message) {
        // [1.20.1, 26.1.x)
//        player.displayClientMessage(Component.literal(message), false);

        // [26.1.x, )
        player.sendSystemMessage(Component.literal(message));
    }
    /**
     * 向本地玩家发送可翻译的文本消息
     * @param player 接收消息的 LocalPlayer 对象
     * @param translationKey 翻译键
     * @param args 翻译参数
     */
    public static void sendTranslatableMessageToPlayer(@NotNull LocalPlayer player, String translationKey, Object... args) {
        Component translatableComponent = Component.translatable(translationKey, args);
        // [1.20.1, 26.1.x)
//        player.displayClientMessage(translatableComponent, false);

        // [26.1.x, )
        player.sendSystemMessage(translatableComponent);
    }
    public static void sendComponentMessageToPlayer(@NotNull LocalPlayer player, Component component) {
//        player.displayClientMessage(component, false);

        // [26.1.x, )
        player.sendSystemMessage(component);
    }
    /**
     * 向本地玩家发送可点击的 Minecraft 组件消息
     * @param player 接收消息的 LocalPlayer 对象
     * @param clickableComponent 要发送的可点击的 Minecraft Component 对象
     */
    public static void sendClickableMessageToPlayer(@NotNull LocalPlayer player, Component clickableComponent) {
//        player.displayClientMessage(clickableComponent, false);

        // [26.1.x, )
        player.sendSystemMessage(clickableComponent);
    }

    public static void sendActionBarToPlayer(@NotNull LocalPlayer player, Component actionBar) {
        // [1.20.1, 26.1.x)
//        player.displayClientMessage(actionBar, true);

        // [26.1.x, )
        player.sendOverlayMessage(actionBar);
    }
}

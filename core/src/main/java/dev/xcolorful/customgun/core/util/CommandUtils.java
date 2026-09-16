package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class CommandUtils {

    /**
     * @param commandLevel 请使用 {@link CommandLevel}，并注意隐藏返回值类型 (或者用 {@code var})
     * @return 新的 CommandSourceStack
     */
    public static CommandSourceStack sourceStack(CommandSource source,
                                                 Vec3 position,
                                                 Vec2 rotation,
                                                 ServerLevel level,
                                                 int commandLevel,
                                                 String textName,
                                                 Component displayName,
                                                 MinecraftServer server,
                                                 @Nullable Entity entity) {
        if (entity != null) {
            return new CommandSourceStack( // _SourceStack.entity(
                    source,
                    position,
                    rotation,
                    level,
                    commandLevel,
                    textName, // 26.3移除
                    displayName, // 26.3移除
                    server,
                    entity
            );
        } else {
            return new CommandSourceStack( // _SourceStack.name(
                    source,
                    position,
                    rotation,
                    level,
                    commandLevel,
                    textName, // 26.3移除
                    displayName,
                    server
                    , null // 26.3移除
            );
        }
    }

    /**
     * 外部调用请使用 {@link CommandUtils#sourceStack}
     * <ul>
     *     保留1.20.1-26.2参数写法的原因:
     *     <li>已经有大量使用，可以查找替换{@code return new CommandSourceStack(} -> {@code CommandUtils.sourceStack(}</li>
     *     <li>即使用了Access Transformer，26.3的参数也对不上，额外抽一个AT接口（到平台层实现）会很迷</li>
     * </ul>
     * 即最终选择的做法是：全局查找替换 + one line import
     */
    @ApiStatus.AvailableSince("26.3")
    @ApiStatus.Internal
    public static class _SourceStack {

        /**
         * Entity version
         */
        public static CommandSourceStack entity(CommandSource source,
                                                Vec3 position,
                                                Vec2 rotation,
                                                ServerLevel level,
                                                int commandLevel,
                                                MinecraftServer server,
                                                Entity entity) {
            return new CommandSourceStack(
                    source,
                    position,
                    rotation,
                    level,
                    commandLevel,
                    "", // 26.3移除
                    Component.empty(), // 26.3移除
                    server,
                    entity
            );
        }

        public static CommandSourceStack name(CommandSource source,
                                              Vec3 position,
                                              Vec2 rotation,
                                              ServerLevel level,
                                              int commandLevel,
                                              Component name,
                                              MinecraftServer server) {
            return new CommandSourceStack(
                    source,
                    position,
                    rotation,
                    level,
                    commandLevel,
                    "", // 26.3移除
                    name,
                    server
                    , null // 26.3移除
            );
        }
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xcolorful.customgun.core.config.SyncConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static dev.xcolorful.customgun.core.command.CommandArg.CONFIG;
import static dev.xcolorful.customgun.core.command.CommandArg.STATE;

public class ConfigCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> config = Commands.literal(CONFIG);
        for (ConfigKey key : ConfigKey.values()) {
            config.then(Commands.literal(key.name())
                    .then(Commands.argument(STATE, BoolArgumentType.bool())
                            .executes(context -> setConfig(context, key))));
        }
        return config;
    }

    private static int setConfig(CommandContext<CommandSourceStack> context, ConfigKey key) {
        boolean state = BoolArgumentType.getBool(context, STATE);

        if (key == null) {
            return 0;
        }
        switch (key) {
            case defaultTableLimit -> SyncConfig.ENABLE_TABLE_FILTER.set(state);
            case serverShootNetworkCheck -> SyncConfig.SERVER_SHOOT_NETWORK_V.set(state);
            case serverShootCooldownCheck -> SyncConfig.SERVER_SHOOT_COOLDOWN_V.set(state);
        }
        context.getSource().sendSystemMessage(Component.translatable(key.lang + "." + (state ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

    public enum ConfigKey {
        defaultTableLimit("commands.tacz.config.default_table_limit"),
        serverShootNetworkCheck("commands.tacz.config.server_shoot_network_check"),
        serverShootCooldownCheck("commands.tacz.config.server_shoot_cooldown_check"),
        ;

        public final String lang;
        ConfigKey(String lang) {
            this.lang = lang;
        }
    }
}
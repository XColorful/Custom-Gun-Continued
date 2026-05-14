/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.command.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static xiao.customgun.core.command.CommandArg.AMOUNT;
import static xiao.customgun.core.command.CommandArg.DUMMY;
import static xiao.customgun.core.command.CommandArg.ENTITY;

public class DummyAmmoCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(DUMMY)
                .then(Commands.argument(ENTITY, EntityArgument.entities())
                        .then(Commands.argument(AMOUNT, IntegerArgumentType.integer(0))
                                .executes(DummyAmmoCommand::setDummy)));
    }

    private static int setDummy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entities = EntityArgument.getEntities(context, ENTITY);
        int cnt = 0;
        int amount = IntegerArgumentType.getInteger(context, AMOUNT);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                ItemStack stack = living.getMainHandItem();
                // TODO IGun
            }
        }
        return cnt;
    }
}
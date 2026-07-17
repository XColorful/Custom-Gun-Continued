/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.command.sub;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;

import static xiao.customgun.core.command.CommandArg.*;

public class AttachmentLockCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(ATTACHMENT_LOCK)
                .then(Commands.argument(ENTITY, EntityArgument.entities())
                        .then(Commands.argument(GUN_ATTACHMENT_LOCK, BoolArgumentType.bool())
                                .executes(AttachmentLockCommand::setAttachmentLock)));
    }

    private static int setAttachmentLock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entities = EntityArgument.getEntities(context, ENTITY);
        int cnt = 0;
        boolean locked = BoolArgumentType.getBool(context, GUN_ATTACHMENT_LOCK);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                ItemStack gunItem = living.getMainHandItem();
                IGun iGun = IGunGetter.fromItemStack(gunItem);
                if (iGun != null) {
                    iGun.setAttachmentLock(gunItem, locked);
                    cnt++;
                }
            }
        }
        return cnt;
    }
}
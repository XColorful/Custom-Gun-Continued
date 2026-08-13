package dev.xcolorful.customgun.client.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import static dev.xcolorful.customgun.client.command.ClientCommandArg.DEBUG;
import static dev.xcolorful.customgun.client.resource.assets.SoundManager.MOD_SOUNDS;
import static dev.xcolorful.customgun.client.resource.assets.SoundManager.MOD_SOUNDS_OLD1;

public class _DebugCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> getClient() {
        return Commands.literal(DEBUG)
                .then(Commands.literal("clientMixinTest")
                        .then(Commands.literal("IAnimateGeoItem")
                                .executes(_DebugCommand::testIAnimateGeoItemMixin)))
                .then(Commands.literal("testGetSound")
                        .then(Commands.argument("rl", StringArgumentType.string())
                                .executes(_DebugCommand::testGetSound)
                        )
                );
    }
    private static int testGetSound(CommandContext<CommandSourceStack> context) {
        var rl = CustomGun.getMcRegistry().createResourceLocation(StringArgumentType.getString(context, "rl"));
        var soundManager = Minecraft.getInstance().getSoundManager();
        boolean loaded = soundManager.getSoundEvent(rl) != null; // 实际拿不到 (不是原版注册)

        var resourceManager = Minecraft.getInstance().getResourceManager();
        var rl1 = MOD_SOUNDS.idToFile(rl);
        var rl2 = MOD_SOUNDS_OLD1.idToFile(rl);
        boolean location1 = resourceManager.getResource(rl1).isPresent();
        boolean location2 = resourceManager.getResource(rl2).isPresent();
        context.getSource().sendSuccess(() -> Component.literal(String.format("rl: %s, loaded %s, location1 %s, location2 %s", rl, loaded, location1, location2)), false);
        return Command.SINGLE_SUCCESS;
    }
    private static int testIAnimateGeoItemMixin(CommandContext<CommandSourceStack> context) {
        GunItem gunItem = ModItems.GUN.get();
        @Nullable IAnimateGeoItem iAnimateGeoItem = IAnimateGeoItem.cgc$fromItem(gunItem);
        if (iAnimateGeoItem != null) {
            context.getSource().sendSuccess(() -> Component.literal("GunItem is IAnimateGeoItem"), false);
            IAnimateGeoItemRenderer<?, ?> renderer = iAnimateGeoItem.cgc$getCustomRenderer();
            context.getSource().sendSuccess(() -> Component.literal("IAnimateGeoItem.cgc$getCustomRenderer() is " + (renderer != null ? renderer.getClass().getName() : "null")), false);
        } else {
            context.getSource().sendFailure(Component.literal("GunItem is not IAnimateGeoItem"));
        }
        return Command.SINGLE_SUCCESS;
    }
}

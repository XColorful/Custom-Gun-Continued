/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.command.sub;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.entity.IClientGunProjectile;
import xiao.customgun.client.resource._AllAssetsManager;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.entity.projectile.GunProjectile;
import xiao.customgun.core.init.registry.ModEntities;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.item.ammo.AmmoItem;
import xiao.customgun.core.resource._AllDataManager;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.GunIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static xiao.customgun.core.command.CommandArg.DEBUG;
import static xiao.customgun.core.command.CommandArg.ENABLE;

public class DebugCommand {
    public static boolean DEBUG_VALUE = false;


    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(DEBUG)
                .then(Commands.literal("testIO")
                        .then(Commands.argument("lenient", BoolArgumentType.bool())
                                .executes(DebugCommand::testIO)))
                .then(Commands.literal("testGunData")
                        .then(Commands.argument("rl", StringArgumentType.string())
                                .executes(DebugCommand::testGunData)))
                .then(Commands.literal("testAllData")
                        .then(Commands.argument("indent", IntegerArgumentType.integer())
                                .then(Commands.argument("path", StringArgumentType.string())
                                        .executes(DebugCommand::testAllData))))
                .then(Commands.literal("mixinTest")
                        .then(Commands.literal("ILivingShooter")
                                .executes(DebugCommand::testLivingShooterMixin))
                        .then(Commands.literal("GunProjectile")
                                .executes(DebugCommand::testGunProjectileMixin)))
                .then(Commands.literal("testForgeMixin").executes(DebugCommand::testForgeMixin))
                .then(Commands.argument(ENABLE, BoolArgumentType.bool())
                        .executes(DebugCommand::setValue));
    }

    private static int setValue(CommandContext<CommandSourceStack> context) {
        DEBUG_VALUE = BoolArgumentType.getBool(context, ENABLE);
        if (context.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            if (DEBUG_VALUE) {
                serverPlayer.sendSystemMessage(Component.literal("TacZ Debug Mode is Turn On"));
            } else {
                serverPlayer.sendSystemMessage(Component.literal("TacZ Debug Mode is Turn Off"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public static final String TEST_INPUT = "./test_input.json";
    public static final String TEST_OUTPUT = "./test_output.json";
    private static int testIO(CommandContext<CommandSourceStack> context) {
        doTestIO(context.getSource(), BoolArgumentType.getBool(context, "lenient"));
        return Command.SINGLE_SUCCESS;
    }
    private static void doTestIO(CommandSourceStack source, boolean lenient) {
        Path inputPath = Paths.get(TEST_INPUT);
        Path outputPath = Paths.get(TEST_OUTPUT);
        if (!Files.exists(inputPath)) {
            source.sendFailure(Component.literal("IO Test Failed: Input file not found at " + inputPath.toAbsolutePath()));
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(lenient);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("    ");
            testAction(jsonReader, jsonWriter);
            jsonWriter.flush();
            source.sendSuccess(() -> Component.literal("IO Test Success! Saved to: " + TEST_OUTPUT), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("IO Exception: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private static void testAction(JsonReader reader, JsonWriter writer) throws IOException {
        GunIndex pojo = GunIndex.fromJson(reader);
        if (pojo != null) {
            GunIndex.toJson(writer, pojo);
        }
    }
    private static int testGunData(CommandContext<CommandSourceStack> context) {
        doTestGunData(context.getSource(), StringArgumentType.getString(context, "rl"));
        return Command.SINGLE_SUCCESS;
    }
    private static void doTestGunData(CommandSourceStack source, String rlString) {
        var rl = CustomGun.getMcRegistry().createResourceLocation(rlString);
        var allManager = _AllDataManager.getCurrent();
        if (allManager == null) {
            source.sendFailure(Component.literal("AllDataManager is null"));
            return;
        }
        var pojoManager = allManager.gunDataManager;
        if (pojoManager == null) {
            source.sendFailure(Component.literal("DataManager is null"));
            return;
        }
        var pojo = pojoManager.getPojo(rl);
        if (pojo == null) {
            source.sendFailure(Component.literal("Failed to find data for: " + rlString));
            return;
        }
        Path outputPath = Paths.get(TEST_OUTPUT);
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("    ");
            testDataAction(jsonWriter, pojo);
            jsonWriter.flush();
            source.sendSuccess(() -> Component.literal("Data Test Success! Saved to: " + TEST_OUTPUT), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Exception: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    private static void testDataAction(JsonWriter writer, GunData pojo) throws IOException {
        GunData.toJson(writer, pojo);
    }

    /**
     * 写到 ./{path}/data/{namespace}/
     * ./{path}/assets/{namespace}/
     */
    private static int testAllData(CommandContext<CommandSourceStack> context) {
        String path = StringArgumentType.getString(context, "path");
        _AllDataManager allManager = _AllDataManager.getCurrent();
        if (allManager != null) {
            CommandSourceStack source = context.getSource();
            try {
                String indent = "\t".repeat(Math.max(0, IntegerArgumentType.getInteger(context, "indent")));
                testManager(indent, path, allManager.gunpackMetaManager);
                testManager(indent, path, allManager.gunDataManager);
                testManager(indent, path, allManager.attachmentDataManager);
                testManager(indent, path, allManager.blockDataManager);
                testManager(indent, path, allManager.gunIndexManager);
                testManager(indent, path, allManager.attachmentIndexManager);
                testManager(indent, path, allManager.ammoIndexManager);
                testManager(indent, path, allManager.blockIndexManager);
                testManager(indent, path, allManager.attachmentTagManager);
                testManager(indent, path, allManager.gunAttachmentDataManager);
                testManager(indent, path, allManager.recipeFilterDataManager);
                if (CustomGun.getMcSide().isClientSide()) {
                    _AllAssetsManager allAssetsManager = _AllAssetsManager.INSTANCE;
                    testManager(indent, path, allAssetsManager.gunpackInfoManager);
                    testManager(indent, path, allAssetsManager.bedrockAnimationManager);
                    testManager(indent, path, allAssetsManager.gltfAnimationManager);
                    testManager(indent, path, allAssetsManager.gunDisplayManager);
                    testManager(indent, path, allAssetsManager.attachmentDisplayManager);
                    testManager(indent, path, allAssetsManager.ammoDisplayManager);
                    testManager(indent, path, allAssetsManager.blockDisplayManager);
                    testManager(indent, path, allAssetsManager.bedrockModelManager);
                    testManager(indent, path, allAssetsManager.playerAnimationManager);
                }

                source.sendSuccess(() -> Component.literal("All data successfully exported to ./" + path), true);
            } catch (Exception e) {
                CustomGun.LOGGER.debug("Exception: ", e);
                source.sendFailure(Component.literal("Export failed: " + e));
            }
        } else {
            context.getSource().sendFailure(Component.literal("AllDataManager is null."));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static void testManager(String indent, String basePath, @Nullable ResourcePojoManager<?> pojoManager) {
        if (pojoManager == null) return;
        Map<ResourceLocation, ? extends ResourcePojo<?>> allPojo = pojoManager.getAllPojo();
        if (allPojo == null || allPojo.isEmpty()) return;

        String typeDir = pojoManager.getPackType().getDirectory();
        var converters = pojoManager.getFileToIdConverters();
        if (converters == null || converters.isEmpty()) return;
        var converter = converters.get(0);

        for (Map.Entry<ResourceLocation, ? extends ResourcePojo<?>> entry : allPojo.entrySet()) {
            var rl = entry.getKey();
            ResourcePojo<?> pojo = entry.getValue();
            if (pojo == null) continue;

            var fileRl = converter.idToFile(rl);
            Path outputFile = Paths.get(basePath, typeDir, fileRl.getNamespace(), fileRl.getPath());
            Path outputDir = outputFile.getParent();

            try {
                if (outputDir != null) {
                    Files.createDirectories(outputDir);
                }
                try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                    JsonWriter jsonWriter = new JsonWriter(writer);
                    jsonWriter.setIndent(indent);
                    pojo.toJson(jsonWriter);
                    jsonWriter.flush();
                }
            } catch (IOException e) {
                CustomGun.LOGGER.error("Failed to export pojo [{}] to {}", rl, outputFile, e);
            }
        }
    }

    private static int testLivingShooterMixin(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayer();
        if (player instanceof ILivingShooter livingShooter) {
            source.sendSuccess(() -> Component.literal(player.getName() + " is ILivingShooter"), false);
        } else {
            source.sendFailure(Component.literal(player.getName() + " is not a ILivingShooter!"));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int testGunProjectileMixin(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        GunProjectile gunProjectile = new GunProjectile(ModEntities.GUN_PROJECTILE.get(), source.getLevel());
        if (gunProjectile instanceof IClientGunProjectile iClientProjectile) {
            source.sendSuccess(() -> Component.literal("GunProjectile is IClientGunProjectile"), false);
        } else {
            source.sendFailure(Component.literal("GunProjectile is not IClientGunProjectile"));
        }
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("deprecation")
    private static int testForgeMixin(CommandContext<CommandSourceStack> context) {
        Item item = ModItems.AMMO.get();
        context.getSource().sendSuccess(() -> Component.literal("" + ((AmmoItem)item).test(ItemStack.EMPTY)), false);
        return 1;
    }
}
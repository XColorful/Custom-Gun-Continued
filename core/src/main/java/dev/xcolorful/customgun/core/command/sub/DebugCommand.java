/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.command.sub;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.IClientGunProjectile;
import dev.xcolorful.customgun.client.resource._AllAssetsManager;
import dev.xcolorful.customgun.core.api.entity.IEntityHitboxHistory;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.hitbox.IEntityHitboxHistoryGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import dev.xcolorful.customgun.core.init.registry.ModEntities;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.resource._AllDataManager;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static dev.xcolorful.customgun.core.command.CommandArg.DEBUG;
import static dev.xcolorful.customgun.core.command.CommandArg.ENABLE;

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
                                .executes(DebugCommand::testGunProjectileMixin))
                        .then(Commands.literal("IEntityHitboxHistory")
                                .executes(DebugCommand::testEntityHitboxHistory)))
                .then(Commands.literal("testAllRecipes")
                        .executes(DebugCommand::testAllRecipes))
                .then(Commands.literal("showMagAmmo")
                        .executes(DebugCommand::showMagAmmo))
                .then(Commands.literal("setMagAmmo")
                        .then(Commands.argument("ammo", IntegerArgumentType.integer(0))
                                .executes(DebugCommand::setMagAmmo)))
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
    private static int testEntityHitboxHistory(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayer();
        IEntityHitboxHistory entityHitboxHistory = IEntityHitboxHistoryGetter.cgc$fromEntity(player);
        if (entityHitboxHistory != null) {
            source.sendSuccess(() -> Component.literal("GunProjectile is IEntityHitboxHistory"), false);
            for (int i = 0; i < 20; i++) {
                @Nullable AABB aabb = entityHitboxHistory.cgc$getHistoryHitbox(i);
                CustomGun.LOGGER.debug("entityHitboxHistory.cgc$getHistoryHitbox({}): {}", i, aabb);
            }
        } else {
            source.sendFailure(Component.literal("Player is not IEntityHitboxHistory"));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int testAllRecipes(CommandContext<CommandSourceStack> context) {
        int cnt = 1;
        for (var entry : ResourceApi.getAllTableRecipe().entrySet()) {
            var tableRecipe = entry.getValue();
            CustomGun.LOGGER.debug("testAllRecipes {}: rl: {}, result: {}", cnt++, entry.getKey(), tableRecipe.getResultItem().getHoverName().getString());
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int showMagAmmo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayer();
        if (player == null) return 0;

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return 0;

        MutableComponent component = Component.empty(); {
            component.append(gunItem.getDisplayName());
            int magAmmoLimit = iGun.getMagAmmoLimit(gunItem);

            int barrelAmmoCount = iGun.getBarrelAmmoCount(gunItem);
            int magAmmoCount = iGun.getMagAmmoCount(gunItem);
            component.append(String.format(" %s %s/%s", barrelAmmoCount, magAmmoCount, magAmmoLimit));
        }
        source.sendSuccess(() -> component, false);
        return Command.SINGLE_SUCCESS;
    }
    private static int setMagAmmo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayer();
        if (player == null) return 0;

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return 0;

        MutableComponent component = Component.empty(); {
            component.append(gunItem.getDisplayName());
            int magAmmoLimit = iGun.getMagAmmoLimit(gunItem);

            int barrelAmmoCount = iGun.getBarrelAmmoCount(gunItem);
            int magAmmoCount = iGun.getMagAmmoCount(gunItem);
            component.append(String.format(" %s %s/%s", barrelAmmoCount, magAmmoCount, magAmmoLimit));

            int ammo = IntegerArgumentType.getInteger(context, "ammo");
            iGun.setMagAmmoCount(gunItem, ammo);
            magAmmoCount = iGun.getMagAmmoCount(gunItem);
            component.append(String.format(" -> %s/%s", magAmmoCount, magAmmoLimit));
        }
        source.sendSuccess(() -> component, false);
        return Command.SINGLE_SUCCESS;
    }
}
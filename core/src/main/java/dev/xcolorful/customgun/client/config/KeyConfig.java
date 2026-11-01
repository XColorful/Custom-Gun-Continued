package dev.xcolorful.customgun.client.config;

import dev.xcolorful.customgun.core.api.config.ClientModConfigTag;
import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;

public class KeyConfig {
    public static IModConfigSpec<Boolean> HOLD_TO_AIM;
    public static IModConfigSpec<Boolean> AIM_SWITCH_TO_FIRST_PERSON;
    public static IModConfigSpec<Boolean> REVERT_AIM_PERSPECTIVE;
    public static IModConfigSpec<Boolean> HOLD_TO_PRONE;
    public static IModConfigSpec<Boolean> HOLD_SHOOT_PREVENT_BOLT;
    public static IModConfigSpec<Boolean> AUTO_RELOAD;
    public static IModConfigSpec<Boolean> SHOOT_TO_RELOAD;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.key_path);

        builder.addComment(ClientModConfigTag.holdToAim_comment);
        HOLD_TO_AIM = builder.addConfig(ClientModConfigTag.holdToAim_path, false);

        builder.addComment(ClientModConfigTag.aimSwitchToFirstPerson_comment);
        AIM_SWITCH_TO_FIRST_PERSON = builder.addConfig(ClientModConfigTag.aimSwitchToFirstPerson_path, true);

        builder.addComment(ClientModConfigTag.revertAimPerspective_comment);
        REVERT_AIM_PERSPECTIVE = builder.addConfig(ClientModConfigTag.revertAimPerspective_path, true);

        builder.addComment(ClientModConfigTag.holdToProne_comment);
        HOLD_TO_PRONE = builder.addConfig(ClientModConfigTag.holdToProne_path, false);

        builder.addComment(ClientModConfigTag.holdShootPreventBolt_comment);
        HOLD_SHOOT_PREVENT_BOLT = builder.addConfig(ClientModConfigTag.holdShootPreventBolt_path, true);

        builder.addComment(ClientModConfigTag.autoReload_comment);
        AUTO_RELOAD = builder.addConfig(ClientModConfigTag.autoReload_path, false);

        builder.addComment(ClientModConfigTag.shootToReload_comment);
        SHOOT_TO_RELOAD = builder.addConfig(ClientModConfigTag.shootToReload_path, true);

        builder.finishBuild();
    }
}
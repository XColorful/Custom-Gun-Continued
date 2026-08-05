/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.config;

import dev.xcolorful.customgun.core.developer.bug.Herobrine;

public class ModConfigTag {

    // GunConfig
    public static final String gun_path = "gun";

    public static final String defaultGunFireSoundDistance_path = "DefaultGunFireSoundDistance";
    public static final String defaultGunFireSoundDistance_comment = "The default fire sound range (block)";

    public static final String defaultGunSilenceSoundDistance_path = "DefaultGunSilenceSoundDistance";
    public static final String defaultGunSilenceSoundDistance_comment = "The silencer default fire sound range (block)";

    public static final String defaultGunOtherSoundDistance_path = "DefaultGunOtherSoundDistance";
    public static final String defaultGunOtherSoundDistance_comment = "The range (block) of other gun sound, reloading sound etc.";

    public static final String creativePlayerConsumeAmmo_path = "CreativePlayerConsumeAmmo";
    public static final String creativePlayerConsumeAmmo_comment = "Whether or not the player will consume ammo in creative mode";

    public static final String autoReloadWhenRespawn_path = "AutoReloadWhenRespawn";
    public static final String autoReloadWhenRespawn_comment = "Auto reload all the guns in player inventory, useful for pvp servers";

    // AmmoConfig
    public static final String ammo_path = "ammo";

    public static final String explosiveAmmoDestroysBlock_path = "ExplosiveAmmoDestroysBlock";
    public static final String explosiveAmmoDestroysBlock_comment = "Warning: Ammo with explosive properties can break blocks";

    public static final String explosiveAmmoFire_path = "ExplosiveAmmoFire";
    public static final String explosiveAmmoFire_comment = "Warning: Ammo with explosive properties can set the surroundings on fire";

    public static final String explosiveAmmoKnockBack_path = "ExplosiveAmmoKnockBack";
    public static final String explosiveAmmoKnockBack_comment = "Ammo with explosive properties can add knockback effect";

    public static final String explosiveAmmoVisibleDistance_path = "ExplosiveAmmoVisibleDistance";
    public static final String explosiveAmmoVisibleDistance_comment = "The distance at which the explosion effect can be seen";

    public static final String passThroughBlocks_path = "PassThroughBlocks";
    public static final String passThroughBlocks_comment = "Those blocks that the ammo can pass through";

    public static final String destroyGlass_path = "DestroyGlass";
    public static final String destroyGlass_comment = "Whether a ammo can break the glass";

    public static final String igniteBlock_path = "IgniteBlock";
    public static final String igniteBlock_comment = "Whether a ammo can ignite the block";

    public static final String igniteEntity_path = "IgniteEntity";
    public static final String igniteEntity_comment = "Whether a ammo can ignite the entity";

    public static final String globalBulletSpeedModifier_path = "GlobalBulletSpeedModifier";
    public static final String[] globalBulletSpeedModifier_comment = new String[]{
            "Global bullet speed modifier, the init speed of the bullet will be multiplied by this value.",
            "This is to compensate the side effects introduced while fixing the shooter variable input issue"
    };

    // OtherConfig
    public static final String other_path = "other";

    public static final String targetSoundDistance_path = "TargetSoundDistance";
    public static final String targetSoundDistance_comment = "The farthest sound distance of the target, including minecarts type";

    public static final String serverHitboxOffset_path = "ServerHitboxOffset";
    public static final String serverHitboxOffset_comment = "DEV: Server hitbox offset (If the hitbox is ahead, fill in a negative number)";

    public static final String serverHitboxLatencyFix_path = "ServerHitboxLatencyFix";
    public static final String serverHitboxLatencyFix_comment = "Server hitbox latency fix";

    public static final String serverHitboxLatencyMaxSaveMs_path = "ServerHitboxLatencyMaxSaveMs";
    public static final String serverHitboxLatencyMaxSaveMs_comment = "The maximum latency (in milliseconds) for the server hitbox latency fix saved";

    // SyncConfig

    // interact_key
    public static final String interactKey_path = "interact_key";

    public static final String interactKeyWhitelistBlocks_path = "InteractKeyWhitelistBlocks";
    public static final String interactKeyWhitelistBlocks_comment = "These whitelist blocks can be interacted with when the interact key is pressed";

    public static final String interactKeyWhitelistEntities_path = "InteractKeyWhitelistEntities";
    public static final String interactKeyWhitelistEntities_comment = "These whitelist entities can be interacted with when the interact key is pressed";

    public static final String interactKeyBlacklistBlocks_path = "InteractKeyBlacklistBlocks";
    public static final String interactKeyBlacklistBlocks_comment = "These blacklist blocks can be interacted with when the interact key is pressed";

    public static final String interactKeyBlacklistEntities_path = "InteractKeyBlacklistEntities";
    public static final String interactKeyBlacklistEntities_comment = "These blacklist entities can be interacted with when the interact key is pressed";

    // base_multiplier
    public static final String baseMultiplier_path = "base_multiplier";

    public static final String damageBaseMultiplier_path = "DamageBaseMultiplier";
    public static final String damageBaseMultiplier_comment = "All base damage number is multiplied by this factor";

    public static final String armorIgnoreBaseMultiplier_path = "ArmorIgnoreBaseMultiplier";
    public static final String armorIgnoreBaseMultiplier_comment = "All armor ignore damage number is multiplied by this factor";

    public static final String headShotBaseMultiplier_path = "HeadShotBaseMultiplier";
    public static final String headShotBaseMultiplier_comment = "All head shot damage number is multiplied by this factor";

    public static final String weightSpeedMultiplier_path = "WeightSpeedMultiplier";
    public static final String weightSpeedMultiplier_comment = "The movement speed will decrease per kg of weight. 0.015 means 1.5% speed decrease per kg. Set a negative value to disable this feature";

    // misc
    public static final String misc_path = "misc";

    public static final String headShotAABB_path = "HeadShotAABB";
    public static final String[] headShotAABB_comment = new String[]{
            "The entity's head hitbox during the headshot",
            "Format: minecraft:" + Herobrine.herobrine + " [-0.225, 1.35, -0.225, 0.225, 1.8, 0.225]"
    };

    public static final String ammoBoxStackSize_path = "AmmoBoxStackSize";
    public static final String ammoBoxStackSize_comment = "The maximum stack size of ammo that the ammo box can hold";

    public static final String clientGunPackDownloadUrls_path = "ClientGunPackDownloadUrls";
    public static final String clientGunPackDownloadUrls_comment = "Deprecated. Use vanilla server resource pack";

    public static final String enableProne_path = "EnableProne"; public static final String enableProne_path_OLD1 = "EnableCrawl";
    public static final String enableProne_comment = "Whether or not players are allowed to use the prone feature";

    public static final String enableTableFilter_path = "EnableDefaultGunSmithTableFilter";
    public static final String enableTableFilter_comment = "Enable the recipe limit of default gunsmith table or not";

    public static final String serverShootNetworkV_path = "ServerShootNetworkCheck";
    public static final String serverShootNetworkV_comment = "[Debug Option] Do server-side network check while shooting or not";

    public static final String serverShootCooldownV_path = "ServerShootCooldownCheck";
    public static final String[] serverShootCooldownV_comment = new String[]{
            "[Debug Option] Do server-side shoot cooldown check or not.",
            "WARNING: Close this will disable the shoot cooldown check in server-side at all,",
            "which may lead to potential for cheating.",
            "Only consider to close this when you can't shoot at all sometimes."
    };

    private ModConfigTag() {}
}

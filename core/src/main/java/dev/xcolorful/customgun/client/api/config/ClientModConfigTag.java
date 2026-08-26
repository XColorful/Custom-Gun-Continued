/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.config;

public class ClientModConfigTag {

    // KeyConfig
    public static final String key_path = "key";

    public static final String holdToAim_path = "HoldToAim";
    public static final String holdToAim_comment = "True if you want to hold the right mouse button to aim";

    public static final String holdToProne_path = "HoldToProne"; public static final String holdToProne_path_OLD1 = "HoldToCrawl";
    public static final String holdToProne_comment = "True if you want to hold the prone button to prone";

    public static final String autoReload_path = "AutoReload";
    public static final String autoReload_comment = "Try to reload automatically when the gun is empty";


    // RenderConfig
    public static final String render_path = "render";

    public static final String enableLaserFadeOut_path = "EnableLaserFadeOut";
    public static final String enableLaserFadeOut_comment = "Whether or not apply fadeout effect on the laser beam. Close this may improve laser performance under some shaders.";

    public static final String gunLodRenderDistance_path = "GunLodRenderDistance";
    public static final String gunLodRenderDistance_comment = "How far to display the lod model, 0 means always display";

    public static final String disableGunTilting_path = "DisableGunTilting";
    public static final String disableGunTilting_comment = "Whether to disable gun tilting while crouching";

    public static final String bulletHoleParticleLife_path = "BulletHoleParticleLife";
    public static final String bulletHoleParticleLife_comment = "The existence time of bullet hole particles, in tick";

    public static final String bulletHoleParticleFadeThreshold_path = "BulletHoleParticleFadeThreshold";
    public static final String bulletHoleParticleFadeThreshold_comment = "The threshold for fading out when rendering bullet hole particles";

    public static final String replaceVanillaCrosshair_path = "ReplaceVanillaCrosshair";
    public static final String replaceVanillaCrosshair_comment = "Whether to replace the vanilla crosshair when holding a gun";

    public static final String crosshairType_path = "CrosshairType";
    public static final String crosshairType_comment = "The crosshair when holding a gun";

    public static final String hitMarkerStartPosition_path = "HitMarketStartPosition";
    public static final String hitMarkerStartPosition_comment = "The starting position of the hit marker";

    public static final String headShotDebugHitbox_path = "HeadShotDebugHitbox";
    public static final String headShotDebugHitbox_comment = "Whether or not to display the head shot's hitbox";

    public static final String gunHUDEnable_path = "GunHUDEnable";
    public static final String gunHUDEnable_comment = "Whether or not to display the gun's HUD";

    public static final String killAmountEnable_path = "KillAmountEnable";
    public static final String killAmountEnable_comment = "Whether or not to display the kill amount";

    public static final String killAmountDurationSecond_path = "KillAmountDurationSecond";
    public static final String killAmountDurationSecond_comment = "The duration of the kill amount, in second";

    public static final String targetRenderDistance_path = "TargetRenderDistance";
    public static final String targetRenderDistance_comment = "The farthest render distance of the target, including minecarts type";

    public static final String enableFirstPersonBulletTracer_path = "EnableFirstPersonBulletTracer";
    public static final String enableFirstPersonBulletTracer_comment = "Whether or not to render first person bullet trail";

    public static final String disableInteractHudText_path = "DisableInteractHudText";
    public static final String disableInteractHudText_comment = "Disable the interact hud text in center of the screen";

    public static final String autoSelectGunSmithTableFilter_path = "AutoSelectGunSmithTableFilter";
    public static final String autoSelectGunSmithTableFilter_comment = "Whether or not to automatically select the gun smith table's held item filter when opening it with a gun, attachment or ammo in main hand";

    public static final String damageCounterResetTime_path = "DamageCounterResetTime";
    public static final String damageCounterResetTime_comment = "Max time the damage counter will reset";

    public static final String disableMovementAttributeFov_path = "DisableMovementAttributeFov";
    public static final String disableMovementAttributeFov_comment = "Disable the FOV effect from the movement speed attribute while holding a gun";

    public static final String appendResourceLocationInTooltip_path = "AppendResourceLocationInTooltip";
    public static final String appendResourceLocationInTooltip_comment = "Whether to append resource location after gunpack information in tooltips";

    public static final String blockEntityTranslucent_path = "EnableBlockEntityTranslucent";
    public static final String blockEntityTranslucent_comment = "Enable translucent while render block entity or not. Enable this option will result in ADDITIONAL PERFORMANCE OVERHEAD.";


    // ResourceConfig
    public static final String resource_path = "resource";

    public static final String enableLazyClientAssetLoad_path = "EnableLazyClientAssetLoad";
    public static final String[] enableLazyClientAssetLoad_comment = new String[]{
            "Build heavy TACZ client assets such as models and animation state machines on demand.",
            "Inventory items are pre-warmed in the background when possible.",
            "If a render needs an asset before warmup finishes, the render thread will wait for it once."
    };

    // SoundConfig
    public static final String sound_path = "sound";

    public static final String hitSoundConcurrencyLimit_path = "HitSoundConcurrencyLimit";
    public static final String hitSoundConcurrencyLimit_comment = "Max active hit marker sounds for the same entity and sound id. 0 disables this limit.";

    public static final String defaultSoundConcurrencyLimit_path = "DefaultSoundConcurrencyLimit";
    public static final String defaultSoundConcurrencyLimit_comment = "Max active normal gun sounds for the same entity and sound id. 0 disables this limit.";

    public static final String highFrequencySoundConcurrencyLimit_path = "HighFrequencySoundConcurrencyLimit";
    public static final String highFrequencySoundConcurrencyLimit_comment = "Max active high-frequency gun sounds, such as shooting and animation keyframe sounds, for the same entity and sound id. 0 disables this limit.";

    public static final String firstPersonAnimationSoundTracking_path = "FirstPersonAnimationSoundTracking";
    public static final String firstPersonAnimationSoundTracking_comment = "Use a non-relative entity-tracking world sound source for first-person animation keyframe sounds. This can improve compatibility with physical sound mods, but may introduce slight stereo drift while moving.";

    // ZoomConfig
    public static final String zoom_path = "Zoom";

    public static final String screenDistanceCoefficient_path = "ScreenDistanceCoefficient";
    public static final String screenDistanceCoefficient_comment = "Screen distance coefficient for zoom, using MDV standard, default is MDV133";

    public static final String zoomSensitivityBaseMultiplier_path = "ZoomSensitivityBaseMultiplier";
    public static final String zoomSensitivityBaseMultiplier_comment = "Zoom sensitivity is multiplied by this factor";

    private ClientModConfigTag() {}
}

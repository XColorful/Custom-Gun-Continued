/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.IClientTickEvent;
import xiao.customgun.client.api.resource.assets.AssetsFolderType;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.config.SoundConfig;
import xiao.customgun.client.resource.GunDisplayInstance;
import xiao.customgun.client.util.ClientWorldUtils;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.item.IAttachment;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.config.GunConfig;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.init.registry.ModSounds;
import xiao.customgun.core.network.message.ServerMessageSound;
import xiao.customgun.core.resource.data.data.GunData;

import java.util.*;

public class SoundPlayManager implements IEventHandler {
    private static class SoundPlayManagerHolder {
        private static final SoundPlayManager INSTANCE = new SoundPlayManager();
    }
    public static SoundPlayManager get() {
        return SoundPlayManagerHolder.INSTANCE;
    }
    protected SoundPlayManager() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.CLIENT_TICK_EVENT) {
            onClientTick((IClientTickEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    public static final FileToIdConverter MOD_SOUND_LISTER = new FileToIdConverter(
            AssetsFolderType.MOD_SOUNDS.getFolderName(),
            FileExtensionType.OGG.getExtensionNameWithDot());

    private record SoundKey(int entityId, ResourceLocation soundId) {}
    private record TrackedGunSound(GunSoundInstance instance, UUID entityUuid) {}
    private static final Map<SoundKey, ArrayDeque<TrackedGunSound>> TRACKED_GUN_SOUNDS = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> SOUND_RESOURCE_EXISTS_CACHE = new HashMap<>();
    private static final Set<ResourceLocation> MISSING_SOUND_WARNED = new HashSet<>();

    private static int soundCleanupTickCounter = 0;

    /**
     * 用于阻止连发时，反复播放 DryFire 音效
     */
    private static boolean DRY_SOUND_TRACK = true;

    /**
     * 临时缓存，用于停止播放的
     */
    private static GunSoundInstance tmpSoundInstance = null;

    public static @Nullable GunSoundInstance playClientSound(Entity entity,
                                                   @Nullable ResourceLocation name,
                                                   float volume, float pitch, int distance, boolean mono) {
        boolean relative = ClientWorldUtils.isLocalPlayer(entity);
        return playClientSound(entity, name, volume, pitch, distance, mono, SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get(), !relative, relative);
    }
    private static @Nullable GunSoundInstance playClientSound(Entity entity,
                                                    @Nullable ResourceLocation name,
                                                    float volume, float pitch, int distance, boolean mono, int concurrencyLimit, boolean trackEntity, boolean relative) {
        Minecraft minecraft = Minecraft.getInstance();
        if (name == null || !hasSoundResource(minecraft, name)) {
            return null;
        }
        if (concurrencyLimit > 0) {
            limitConcurrentGunSound(minecraft, entity.getId(), name, concurrencyLimit);
        }
        GunSoundInstance instance = trackEntity
                ? new EntityTrackingGunSoundInstance(ModSounds.GUN.get(), SoundSource.PLAYERS, volume, pitch, entity, distance, name, mono)
                : new GunSoundInstance(ModSounds.GUN.get(), SoundSource.PLAYERS, volume, pitch, entity, distance, name, mono, relative);
        minecraft.getSoundManager().play(instance);
        if (concurrencyLimit > 0) {
            trackGunSound(entity.getId(), entity.getUUID(), name, instance);
        }
        return instance;
    }
    public static @Nullable GunSoundInstance playClientSound(Entity entity,
                                                   @Nullable ResourceLocation name,
                                                   float volume, float pitch, int distance) {
        return playClientSound(entity, name, volume, pitch, distance, false);
    }

    public static @Nullable GunSoundInstance playAnimationSound(Entity entity,
                                                      @Nullable ResourceLocation name,
                                                      float volume, float pitch, int distance) {
        return playClientSound(entity, name, volume, pitch, distance, false, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(), false, ClientWorldUtils.isLocalPlayer(entity));
    }

    public static void stopPlayGunSound() {
        if (tmpSoundInstance != null) {
            tmpSoundInstance.setStop();
        }
    }
    public static void stopPlayGunSound(GunDisplayInstance gunDisplayIndex, GunSoundType animationName) {
        if (tmpSoundInstance != null) {
            if (tmpSoundInstance.getRegistryName() != null && tmpSoundInstance.getRegistryName().equals(gunDisplayIndex.getSounds(animationName))) {
                tmpSoundInstance.setStop();
            }
        }
    }

    public static void playerRefitSound(ItemStack attachmentItem, LocalPlayer player, String soundName) {
        // TODO IAttachment
//        IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
        IAttachment iAttachment = null;
        if (iAttachment == null) {
            return;
        }
//        var attachmentId = iAttachment.getAttachmentId(attachmentItem);
        // TODO TimelessAPI
    }

    public static void playShootSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex, GunData gunData) {
        if (PlannedRefactor.ON_MAGIC_CLIENT_SOUND_VOLUME) return;
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.SHOOT_SOUND), 0.8f, 0.9f + entity.getRandom().nextFloat() * 0.125f, // 随机音高应该抽到 GunDisplay 里?
                (int) (GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get() * gunData.getFireSoundData().getNormalMultiplier()), false,
                SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(), false, ClientWorldUtils.isLocalPlayer(entity));
    }

    public static void playSilenceSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex, GunData gunData) {
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.SILENCE_SOUND), 0.6f, 0.9f + entity.getRandom().nextFloat() * 0.125f,
                (int) (GunConfig.DEFAULT_GUN_SILENCE_SOUND_DISTANCE.get() * gunData.getFireSoundData().getSilencedMultiplier()), false,
                SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(), false, ClientWorldUtils.isLocalPlayer(entity));
    }

    public static void playDryFireSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        if (!DRY_SOUND_TRACK) return;
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.DRY_FIRE_SOUND), 1.0f, 0.9f + entity.getRandom().nextFloat() * 0.125f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
        DRY_SOUND_TRACK = false;
    }
    /**
     * 只有松开鼠标时，才会重置
     */
    public static void resetDryFireSound() {
        DRY_SOUND_TRACK = true;
    }

    /**
     * TODO 抽个 switch case 出来?
     */
    public static void playReloadSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex, boolean noAmmo) {
        if (noAmmo) {
            tmpSoundInstance = playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.RELOAD_EMPTY_SOUND), 1.0f, 1.0f,
                    GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
        } else {
            tmpSoundInstance = playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.RELOAD_TACTICAL_SOUND), 1.0f, 1.0f,
                    GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
        }
    }
    public static void playInspectSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex, boolean noAmmo) {
        if (noAmmo) {
            tmpSoundInstance = playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.INSPECT_EMPTY_SOUND), 1.0f, 1.0f,
                    GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
        } else {
            tmpSoundInstance = playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.INSPECT_SOUND), 1.0f, 1.0f,
                    GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
        }
    }
    public static void playBoltSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        tmpSoundInstance = playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.BOLT_SOUND), 1.0f, 1.0f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }
    public static void playDrawSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        tmpSoundInstance = playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.DRAW_SOUND), 1.0f, 1.0f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }
    public static void playPutAwaySound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        tmpSoundInstance = playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.PUT_AWAY_SOUND), 1.0f, 1.0f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }
    public static void playFireSelectSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.FIRE_SELECT), 1.0f, 1.0f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }
    public static void playMeleeBayonetSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.MELEE_BAYONET), 1.0f, 0.9f + entity.getRandom().nextFloat() * 0.125f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }
    public static void playMeleePushSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.MELEE_PUSH), 1.0f, 0.9f + entity.getRandom().nextFloat() * 0.125f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }
    public static void playMeleeStockSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.MELEE_STOCK), 1.0f, 0.9f + entity.getRandom().nextFloat() * 0.125f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }
    public static void playHeadHitSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        boolean relative = ClientWorldUtils.isLocalPlayer(entity);
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.HEAD_HIT_SOUND), 1.0f, 1.0f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(), false, SoundConfig.HIT_SOUND_CONCURRENCY_LIMIT.get(), !relative, relative);
    }
    public static void playFleshHitSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        boolean relative = ClientWorldUtils.isLocalPlayer(entity);
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.FLESH_HIT_SOUND), 1.0f, 1.0f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(), false, SoundConfig.HIT_SOUND_CONCURRENCY_LIMIT.get(), !relative, relative);
    }
    public static void playKillSound(LivingEntity entity, GunDisplayInstance gunDisplayIndex) {
        playClientSound(entity, gunDisplayIndex.getSounds(GunSoundType.KILL_SOUND), 1.0f, 1.0f,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }

    public static void playMessageSound(ServerMessageSound message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(message.entityId()) instanceof LivingEntity livingEntity)) {
            return;
        }
        var gunId = message.gunId();
        var gunDisplayId = message.gunDisplayId();
        // TODO TimelessAPI
    }

    public static void onClientTick(IClientTickEvent event) {
        // IClientTickEvent 本身已经是只在 Phase.END 触发

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            cleanupInvalidEntitySounds(minecraft);
            return;
        }
        soundCleanupTickCounter++;
        if (soundCleanupTickCounter % 5 == 0) {
            cleanupInvalidEntitySounds(minecraft);
        }
    }

    public static void clearSoundResourceCache() {
        SOUND_RESOURCE_EXISTS_CACHE.clear();
        MISSING_SOUND_WARNED.clear();
    }

    private static void limitConcurrentGunSound(Minecraft minecraft, int entityId, ResourceLocation soundId, int limit) {
        SoundKey key = new SoundKey(entityId, soundId);
        ArrayDeque<TrackedGunSound> sounds = TRACKED_GUN_SOUNDS.get(key);
        if (sounds == null) {
            return;
        }
        int activeForKey = 0;
        Iterator<TrackedGunSound> iterator = sounds.iterator();
        while (iterator.hasNext()) {
            TrackedGunSound tracked = iterator.next();
            if (minecraft.getSoundManager().isActive(tracked.instance())) {
                activeForKey++;
            } else {
                iterator.remove();
            }
        }
        if (sounds.isEmpty()) {
            TRACKED_GUN_SOUNDS.remove(key);
        }

        int toStop = activeForKey - limit + 1;
        if (toStop <= 0) {
            return;
        }

        int stopped = 0;
        iterator = sounds.iterator();
        while (iterator.hasNext() && stopped < toStop) {
            TrackedGunSound tracked = iterator.next();
            if (minecraft.getSoundManager().isActive(tracked.instance())) {
                tracked.instance().setStop();
                iterator.remove();
                stopped++;
            }
        }
        if (sounds.isEmpty()) {
            TRACKED_GUN_SOUNDS.remove(key);
        }
    }

    private static void trackGunSound(int entityId, UUID entityUuid, ResourceLocation soundId, GunSoundInstance instance) {
        SoundKey key = new SoundKey(entityId, soundId);
        TRACKED_GUN_SOUNDS.computeIfAbsent(key, ignored -> new ArrayDeque<>()).addLast(new TrackedGunSound(instance, entityUuid));
    }

    private static void cleanupInvalidEntitySounds(Minecraft minecraft) {
        if (minecraft.level == null) {
            stopAndClearTrackedSounds();
            return;
        }
        Iterator<Map.Entry<SoundKey, ArrayDeque<TrackedGunSound>>> entryIterator = TRACKED_GUN_SOUNDS.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<SoundKey, ArrayDeque<TrackedGunSound>> entry = entryIterator.next();
            Iterator<TrackedGunSound> soundIterator = entry.getValue().iterator();
            while (soundIterator.hasNext()) {
                TrackedGunSound tracked = soundIterator.next();
                if (!minecraft.getSoundManager().isActive(tracked.instance())) {
                    soundIterator.remove();
                    continue;
                }
                Entity owner = minecraft.level.getEntity(entry.getKey().entityId());
                if (isInvalidSoundOwner(owner, tracked.entityUuid())) {
                    tracked.instance().setStop();
                    soundIterator.remove();
                }
            }
            if (entry.getValue().isEmpty()) {
                entryIterator.remove();
            }
        }
    }

    private static void stopAndClearTrackedSounds() {
        for (ArrayDeque<TrackedGunSound> sounds : TRACKED_GUN_SOUNDS.values()) {
            for (TrackedGunSound tracked : sounds) {
                tracked.instance().setStop();
            }
        }
        TRACKED_GUN_SOUNDS.clear();
    }

    private static boolean isInvalidSoundOwner(@Nullable Entity entity, UUID entityUuid) {
        return entity == null
                || !entity.getUUID().equals(entityUuid)
                || entity.isRemoved()
                || entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying();
    }

    private static boolean hasSoundResource(Minecraft minecraft, ResourceLocation soundId) {
        boolean exists = SOUND_RESOURCE_EXISTS_CACHE.computeIfAbsent(soundId, id -> {
            ResourceLocation soundPath = MOD_SOUND_LISTER.idToFile(id);
            return minecraft.getResourceManager().getResource(soundPath).isPresent();
        });
        if (!exists && MISSING_SOUND_WARNED.add(soundId)) {
            ResourceLocation soundPath = MOD_SOUND_LISTER.idToFile(soundId);
            CustomGun.LOGGER.warn("[TACZ Sound] Missing gun sound resource, skipped. sound={}, path={}", soundId, soundPath);
        }
        return exists;
    }
}

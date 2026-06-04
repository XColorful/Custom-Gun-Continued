/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.IClientTickEvent;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.attachment.AttachmentSoundType;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.config.SoundConfig;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import xiao.customgun.client.util.ClientWorldUtils;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.item.IAttachment;
import xiao.customgun.core.api.item.attachment.IAttachmentGetter;
import xiao.customgun.core.config.GunConfig;
import xiao.customgun.core.developer.bug.Herobrine;
import xiao.customgun.core.init.registry.ModSounds;

import java.util.*;

public final class SoundPlayManager implements IEventHandler {
    private static class SoundPlayManagerHolder {
        private static final SoundPlayManager INSTANCE = new SoundPlayManager();
    }
    public static SoundPlayManager get() {
        return SoundPlayManagerHolder.INSTANCE;
    }
    private SoundPlayManager() {}
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

    private final Map<_EntitySoundEntry, ArrayDeque<_TrackedSoundInstance>> TRACKED_SOUND_INSTANCE = new HashMap<>();
    private final Map<Identifier, Identifier> SOUND_PATH_CACHE = new HashMap<>();

    private int soundCleanupTickCounter = 0;

    /**
     * 用于阻止连发时，反复播放 DryFire 音效
     */
    private boolean ALLOW_DRY_FIRE = true;

    /**
     * 临时缓存，用于停止播放的
     */
    private ResourceSoundInstance currentSoundInstance = null;

    public @Nullable ResourceSoundInstance playClientSound(@Nullable Identifier soundLocation,
                                                            float volume, float pitch,
                                                            @NotNull Entity entity, boolean relative,
                                                            float soundDistance,
                                                            boolean trackEntity, int concurrencyLimit) {
        var soundPath = this.getSoundPath(soundLocation);
        if (soundPath == null) return null;

        Minecraft minecraft = Minecraft.getInstance();
        if (concurrencyLimit > 0) this.limitConcurrentGunSound(minecraft, entity.getId(), soundLocation, concurrencyLimit);

        ResourceSoundInstance soundInstance = trackEntity
                ? new EntityTrackingSoundInstance(ModSounds.GUN.get(), SoundSource.PLAYERS, RandomSource.create(Herobrine.HIM), soundLocation, soundPath, volume, pitch, entity, false, soundDistance)
                : new ResourceSoundInstance(ModSounds.GUN.get(), SoundSource.PLAYERS, RandomSource.create(Herobrine.HIM), soundLocation, soundPath, volume, pitch, entity, relative, soundDistance);
        minecraft.getSoundManager().play(soundInstance);

        if (concurrencyLimit > 0) trackGunSound(entity.getId(), entity.getUUID(), soundLocation, soundInstance);

        return soundInstance;
    }

    public void stopCurrentSound() {
        if (currentSoundInstance != null) currentSoundInstance.setStop();
    }
    public void stopCurrentSound(GunDisplayInstance gunDisplayIndex, GunSoundType animationName) {
        if (currentSoundInstance == null) return;
        var soundLocation = currentSoundInstance.getSoundLocation();
        if (soundLocation != null && soundLocation.equals(gunDisplayIndex.getGunSound(animationName))) {
            currentSoundInstance.setStop();
        }
    }

    public void clearCacheOnReload() {
        SOUND_PATH_CACHE.clear();
        this.stopAndClearTrackedSounds();
    }

    public boolean isAllowDryFire() {
        return ALLOW_DRY_FIRE;
    }
    public void resetDryFireSound() {
        ALLOW_DRY_FIRE = true;
    }

    public void onClientTick(IClientTickEvent event) {
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

    private void limitConcurrentGunSound(Minecraft minecraft, int entityId, Identifier soundId, int limit) {
        _EntitySoundEntry key = new _EntitySoundEntry(entityId, soundId);
        ArrayDeque<_TrackedSoundInstance> sounds = TRACKED_SOUND_INSTANCE.get(key);
        if (sounds == null) {
            return;
        }
        int activeForKey = 0;
        Iterator<_TrackedSoundInstance> iterator = sounds.iterator();
        while (iterator.hasNext()) {
            _TrackedSoundInstance tracked = iterator.next();
            if (minecraft.getSoundManager().isActive(tracked.soundInstance())) {
                activeForKey++;
            } else {
                iterator.remove();
            }
        }
        if (sounds.isEmpty()) {
            TRACKED_SOUND_INSTANCE.remove(key);
        }

        int toStop = activeForKey - limit + 1;
        if (toStop <= 0) {
            return;
        }

        int stopped = 0;
        iterator = sounds.iterator();
        while (iterator.hasNext() && stopped < toStop) {
            _TrackedSoundInstance tracked = iterator.next();
            if (minecraft.getSoundManager().isActive(tracked.soundInstance())) {
                tracked.soundInstance().setStop();
                iterator.remove();
                stopped++;
            }
        }
        if (sounds.isEmpty()) {
            TRACKED_SOUND_INSTANCE.remove(key);
        }
    }

    private void trackGunSound(int entityId, UUID entityUuid, Identifier soundId, ResourceSoundInstance instance) {
        _EntitySoundEntry key = new _EntitySoundEntry(entityId, soundId);
        TRACKED_SOUND_INSTANCE.computeIfAbsent(key, ignored -> new ArrayDeque<>()).addLast(new _TrackedSoundInstance(instance, entityUuid));
    }

    private void cleanupInvalidEntitySounds(Minecraft minecraft) {
        if (minecraft.level == null) {
            this.stopAndClearTrackedSounds();
            return;
        }
        Iterator<Map.Entry<_EntitySoundEntry, ArrayDeque<_TrackedSoundInstance>>> entryIterator = TRACKED_SOUND_INSTANCE.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<_EntitySoundEntry, ArrayDeque<_TrackedSoundInstance>> entry = entryIterator.next();
            Iterator<_TrackedSoundInstance> soundIterator = entry.getValue().iterator();
            while (soundIterator.hasNext()) {
                _TrackedSoundInstance tracked = soundIterator.next();
                if (!minecraft.getSoundManager().isActive(tracked.soundInstance())) {
                    soundIterator.remove();
                    continue;
                }
                Entity owner = ClientWorldUtils.getEntityById(minecraft.level, entry.getKey().entityId());
                if (this.isInvalidSoundOwner(owner, tracked.entityUUID())) {
                    tracked.soundInstance().setStop();
                    soundIterator.remove();
                }
            }
            if (entry.getValue().isEmpty()) {
                entryIterator.remove();
            }
        }
    }

    private void stopAndClearTrackedSounds() {
        for (ArrayDeque<_TrackedSoundInstance> sounds : TRACKED_SOUND_INSTANCE.values()) {
            for (_TrackedSoundInstance tracked : sounds) {
                tracked.soundInstance().setStop();
            }
        }
        TRACKED_SOUND_INSTANCE.clear();
    }

    private boolean isInvalidSoundOwner(@Nullable Entity entity, UUID entityUuid) {
        return entity == null
                || !entity.getUUID().equals(entityUuid)
                || entity.isRemoved()
                || entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying();
    }

    private Identifier getSoundPath(Identifier soundLocation) {
        var pathLocation = SOUND_PATH_CACHE.get(soundLocation);
        if (pathLocation != null) return pathLocation;
        pathLocation = ResourceSoundInstance.getPathLocation(soundLocation);
        if (pathLocation != null) SOUND_PATH_CACHE.put(soundLocation, pathLocation);
        if (pathLocation == null) CustomGun.LOGGER.warn("Failed to get soundPath from soundLocation {}", soundLocation);
        return pathLocation;
    }

    // --------便利方法--------

    public @Nullable ResourceSoundInstance playAnimationSound(@Nullable Identifier soundLocation,
                                                              float volume, float pitch,
                                                              @NotNull Entity entity,
                                                              float soundDistance) {
        boolean trackEntity = !ClientWorldUtils.isLocalPlayer(entity) || SoundConfig.FIRST_PERSON_ANIMATION_SOUND_TRACKING.get();
        return this.playClientSound(soundLocation,
                volume, pitch,
                entity, !trackEntity,
                soundDistance, trackEntity, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get());
    }

    public void playerRefitSound(ItemStack attachmentItem, LivingEntity player, AttachmentSoundType soundType) {
        IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return;
        ClientAttachmentIndexInstance pojoInstance = ClientResourceApi.getClientAttachmentIndexInstance(iAttachment.getAttachmentLocation(attachmentItem));
        if (pojoInstance == null) return;

        var soundLocation = pojoInstance.getAttachmentDisplay().getAttachmentSounds().get(soundType);
        if (soundLocation != null) this.playClientSound(soundLocation,
                1.0f, 1.0f,
                player, false,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(), true, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get());
    }

    public void playGunSound(@Nullable Identifier soundLocation,
                             float volume,
                             @NotNull LivingEntity localPlayer,
                             float soundDistance,
                             boolean trackEntity) {
        this.playClientSound(soundLocation,
                volume, 0.9f + localPlayer.getRandom().nextFloat() * 0.125f,
                localPlayer, false,
                soundDistance,
                trackEntity, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get());
    }
    public void playGunSound(@Nullable Identifier soundLocation,
                             @NotNull LivingEntity localPlayer) {
        this.playClientSound(soundLocation,
                1.0f, 1.0f,
                localPlayer, false,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                true, SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get());
    }

    // --------Deprecated--------

    // --------“大便类”--------

    private record _EntitySoundEntry(int entityId, Identifier soundLocation) {
    }
    private record _TrackedSoundInstance(ResourceSoundInstance soundInstance, UUID entityUUID) {
    }
}

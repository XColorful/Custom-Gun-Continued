/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.forge.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xiao.customgun.CustomGun;
import xiao.customgun.core.entity.sync.SyncedEntityData;
import xiao.customgun.forge.CustomGunForge;
import xiao.customgun.forge.minecraft.capability.SyncDataCapabilityProvider;

/**
 * 1.20.4+ NeoForge 不兼容
 * 作为处理这种情况的示范
 */
@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class _LivingShooterSyncData {

    public static final ResourceLocation capabilityRl = CustomGunForge.mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "sync_entity_data"));
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (SyncedEntityData.instance().hasSyncedDataKey(event.getObject())) {
            SyncDataCapabilityProvider provider = new SyncDataCapabilityProvider();
            event.addCapability(capabilityRl, provider);
            // Don't add invalidate to server player since it's persistent
            if (!(event.getObject() instanceof ServerPlayer)) {
                event.addListener(provider::invalidate);
            }
        }
    }
}

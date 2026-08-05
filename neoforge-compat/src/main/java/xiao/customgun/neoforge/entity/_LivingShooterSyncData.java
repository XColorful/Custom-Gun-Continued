/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.neoforge.entity;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.entity.sync.SyncedEntityData;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import dev.xcolorful.customgun.neoforge.minecraft.capability.SyncDataCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;

/**
 * 1.20.4+ NeoForge 不兼容
 * 作为处理这种情况的示范
 */
@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class _LivingShooterSyncData {

    public static final ResourceLocation capabilityRl = CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "sync_entity_data"));
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

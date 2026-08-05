/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforge.event;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ITagsUpdatedEvent;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.jetbrains.annotations.Nullable;

public class NeoTagsUpdatedEvent extends NeoEvent implements ITagsUpdatedEvent {

    protected TagsUpdatedEvent tagsUpdatedEvent;

    public NeoTagsUpdatedEvent(Event event) {
        super(event);
        if (event instanceof TagsUpdatedEvent eventIn) {
            this.tagsUpdatedEvent = eventIn;
        } else {
            throw new RuntimeException("Expected TagsUpdatedEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.TAGS_UPDATED_EVENT;
    }

    @Override
    public dev.xcolorful.customgun.core.api.common.McLogicalSide getLogicalSide() {
        return CustomGunNeoforge.sideExecutor.getLogicalSide();
    }

    @Override
    public HolderLookup.Provider getLookupProvider() {
        return tagsUpdatedEvent.getRegistryAccess();
    }

    @Override
    public @Nullable RegistryAccess getRegistryAccess() {
        return tagsUpdatedEvent.getRegistryAccess();
    }

    @Override
    public UpdateCause getUpdateCause() {
        return switch (tagsUpdatedEvent.getUpdateCause()) {
            case SERVER_DATA_LOAD -> UpdateCause.SERVER_DATA_LOAD;
            case CLIENT_PACKET_RECEIVED -> UpdateCause.CLIENT_PACKET_RECEIVED;
        };
    }

    @Override
    public boolean shouldUpdateStaticData() {
        return tagsUpdatedEvent.shouldUpdateStaticData();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoTagsUpdatedEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
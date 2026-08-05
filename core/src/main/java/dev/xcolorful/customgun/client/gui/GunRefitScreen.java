/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui;

import dev.xcolorful.customgun.client.api.sound.attachment.AttachmentSoundType;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

// TODO
public class GunRefitScreen extends Screen {

    public GunRefitScreen() {
        super(Component.literal("Gun Refit Screen"));
        // TODO
    }

    @Override
    public void init() {
        if (PlannedRefactor.ON_CREATE_SCREEN) return;
    }

    private void addInventoryAttachmentButtons() {
        LocalPlayer player = getMinecraft().player;
        // TODO
        SoundPlayManager.get().playerRefitSound(null, player, AttachmentSoundType.INSTALL_SOUND);
    }

    private void addAttachmentTypeButtons() {
        LocalPlayer player = getMinecraft().player;
        // TODO
        SoundPlayManager.get().playerRefitSound(null, player, AttachmentSoundType.UNINSTALL_SOUND);
    }
}

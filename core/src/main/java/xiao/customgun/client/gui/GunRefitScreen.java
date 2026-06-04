/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import xiao.customgun.client.api.sound.attachment.AttachmentSoundType;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.developer.PlannedRefactor;

// TODO
public class GunRefitScreen extends Screen {

    protected GunRefitScreen() {
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

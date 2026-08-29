/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model;

import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.ShellRender;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

public interface IGunModelObjectRenderer extends IGunModelObjectRender {

    // --------Getter & Setter--------

    EnumMap<AttachmentCategory, ItemStack> getCurrentAttachmentItem();
    ItemStack getCurrentGunItem();
    @Nullable BedrockPart getAdditionalMagazineNode();
    @Nullable List<BedrockPart> getIronSightPath();
    @Nullable List<BedrockPart> getIdleSightPath();
    @Nullable List<BedrockPart> getThirdPersonHandOriginPath();
    @Nullable List<BedrockPart> getFixedOriginPath();
    @Nullable List<BedrockPart> getGroundOriginPath();
    @Nullable List<BedrockPart> getMuzzleFlashPosPath();
    @Nullable List<BedrockPart> getScopePosPath();
    @ApiStatus.AvailableSince("26.2")
    @Nullable List<BedrockPart> getLeftHandPosPath();
    @ApiStatus.AvailableSince("26.2")
    @Nullable List<BedrockPart> getRightHandPosPath();
    @ApiStatus.AvailableSince("26.2")
    @Nullable List<BedrockPart> getAttachmentPosPath(AttachmentCategory category);
    @Nullable List<BedrockPart> getRefitAttachmentViewPath(AttachmentCategory type);
    @Nullable ShellRender getShellRender(int index);
    @Nullable BedrockPart getRootNode();
    boolean getRenderHand();

    void setRenderHand(boolean renderHand);
}

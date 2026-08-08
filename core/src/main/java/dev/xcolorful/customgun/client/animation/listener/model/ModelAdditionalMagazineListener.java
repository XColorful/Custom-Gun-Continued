/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.model;

import dev.xcolorful.customgun.client.api.animation.ObjectAnimationChannel;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.client.model.GunModelObject;

public class ModelAdditionalMagazineListener implements IAnimationListener {

    private final IAnimationListener listener;
    private final GunModelObject model;

    public ModelAdditionalMagazineListener(IAnimationListener listener, GunModelObject model) {
        this.listener = listener;
        this.model = model;
    }
    @Override
    public ObjectAnimationChannel.ChannelType getType() {
        return this.listener.getType();
    }

    @Override
    public void update(float[] values, boolean blend) {
        listener.update(values, blend);
        if (model.getAdditionalMagazineNode() != null) {
            model.getAdditionalMagazineNode().visible = true;
        }
    }

    @Override
    public float[] initialValue() {
        return listener.initialValue();
    }
}

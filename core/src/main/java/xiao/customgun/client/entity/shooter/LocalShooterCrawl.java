/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.entity.LocalShooterProperty;

public final class LocalShooterCrawl extends LocalShooterAspect {

    private boolean isCrawling = false;

    public LocalShooterCrawl(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }


    public void crawl(boolean isCrawl) {
        // TODO
    }

    public void tickCrawl() {
        // TODO
    }

    public boolean isCrawling() {
        return this.isCrawling;
    }
}

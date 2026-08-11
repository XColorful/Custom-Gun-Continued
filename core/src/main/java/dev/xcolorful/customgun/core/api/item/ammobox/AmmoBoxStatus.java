/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.ammobox;

import dev.xcolorful.customgun.core.api.item.IAmmoBox;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum AmmoBoxStatus {
    /*
    子 网 掩 码
     */

    // 00000000 . 00000000 . 00000000 . 00000001
    FULL(1, 0), // 有子弹

    // 00000000 . 00000000 . 00000000 . 00000010
    INFINITE_FEED(1, 1),

    // 00000000 . 00000000 . 00000000 . 00000100
    ALMIGHTY_AMMO(1, 2),

    // 00000000 . 00000000 . 00000000 . 11111000
    BOX_LEVEL(0b11111, 3); // 32种

    private final int bitWidthMask; // 纯净的位宽掩码（如 0b11111）
    private final int offset; // 偏移量
    private final int mask; // 自动算好的完整网络掩码（如 0b11111 << 3）
    AmmoBoxStatus(int bitWidthMask, int offset) {
        this.bitWidthMask = bitWidthMask;
        this.offset = offset;
        this.mask = bitWidthMask << offset;
    }
    public int getBitWidthMask() {
        return this.bitWidthMask;
    }
    public int getOffset() {
        return this.offset;
    }
    public int getMask() {
        return this.mask;
    }

    public static class StatusMask {
        private int mask;
        protected StatusMask(int mask) {
            this.mask = mask;
        }
        public static @NotNull StatusMask fromAmmoBox(ItemStack ammoItem) {
            StatusMask statusMask = new StatusMask(0);
            @Nullable IAmmoBox iAmmoBox = IAmmoBoxGetter.fromItemStack(ammoItem);
            if (iAmmoBox == null) return statusMask;

            statusMask.setMask(FULL, iAmmoBox.getAmmoCount(ammoItem) > 0 ? 1 : 0);
            statusMask.setMask(INFINITE_FEED, iAmmoBox.hasInfiniteFeed(ammoItem) ? 1 : 0);
            statusMask.setMask(ALMIGHTY_AMMO, iAmmoBox.isAlmightyAmmo(ammoItem) ? 1 : 0);
            statusMask.setMask(BOX_LEVEL, iAmmoBox.getBoxLevel(ammoItem));

            return statusMask;
        }

        // 覆写掩码
        public void setMask(AmmoBoxStatus maskType, int value) {
            this.mask &= ~maskType.getMask(); // 清除选中网段
            int maskedValue = (value & maskType.getBitWidthMask()); // 限制在选中网段范围内
            this.mask |= (maskedValue << maskType.getOffset());
        }

        public int toMask() {
            return this.mask;
        }
    }
}

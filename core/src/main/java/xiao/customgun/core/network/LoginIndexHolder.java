/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network;

import java.util.function.IntSupplier;

/**
 * <p>Author: MrCrayfish</p>
 * <p>Open source at <a href="https://github.com/MrCrayfish/Framework">Github</a> under LGPL License.</p>
 */
public abstract class LoginIndexHolder implements IntSupplier {
    private int loginIndex;

    public int getLoginIndex() {
        return this.loginIndex;
    }

    public void setLoginIndex(final int loginIndex) {
        this.loginIndex = loginIndex;
    }

    @Override
    public int getAsInt() {
        return this.getLoginIndex();
    }
}

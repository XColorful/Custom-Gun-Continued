/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.client;

import xiao.customgun.CustomGun;
import xiao.customgun.client.event.custom.ClientEventHandlers;
import xiao.customgun.client.init.ClientModConfig;

public class CustomGunClient {

    protected static boolean initialized;

    public static void init() {
        if (initialized) return;

        ClientModConfig.init();

        ClientEventHandlers.registerAll(CustomGun.getEventRegister());
        initialized = true;
    }
}

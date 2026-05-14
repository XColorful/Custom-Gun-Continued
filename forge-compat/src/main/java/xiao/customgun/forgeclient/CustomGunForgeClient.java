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

package xiao.customgun.forgeclient;

import xiao.customgun.client.CustomGunClient;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.forgeclient.event.ForgeClientEventRegister;

public class CustomGunForgeClient {

    protected static boolean initialized;
    public static IEventRegister eventRegister;

    public static void init() {
        if (initialized) return;
        CustomGunForgeClient.eventRegister = new ForgeClientEventRegister();
        CustomGunClient.init();
        initialized = true;
    }
}

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

package xiao.customgun.neoforgeclient;

import xiao.customgun.client.CustomGunClient;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.neoforgeclient.event.NeoClientEventRegister;

public class CustomGunNeoforgeClient {

    protected static boolean initialized;
    public static IEventRegister eventRegister;

    public static void init() {
        if (initialized) return;
        CustomGunNeoforgeClient.eventRegister = new NeoClientEventRegister();
        CustomGunClient.init();
        initialized = true;
    }
}

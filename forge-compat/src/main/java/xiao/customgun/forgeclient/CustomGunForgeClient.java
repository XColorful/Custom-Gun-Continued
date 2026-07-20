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
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.forgeclient.event.ForgeClientEventRegister;
import xiao.customgun.forgeclient.input.ForgeKeyMapping;

public class CustomGunForgeClient {

    protected static boolean initialized;
    public static IEventRegister eventRegister;
    public static IKeyMapping.Creator keyMappingCreator;

    public static void init() {
        if (initialized) return;
        CustomGunForgeClient.eventRegister = new ForgeClientEventRegister();
        CustomGunForgeClient.keyMappingCreator = new ForgeKeyMapping.Creator();

        CustomGunClient.init(CustomGunForgeClient.keyMappingCreator);
        initialized = true;
    }
}

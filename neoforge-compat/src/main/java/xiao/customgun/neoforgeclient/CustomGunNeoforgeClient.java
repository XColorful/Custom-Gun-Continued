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
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.minecraft.access.IClientAccessTransformer;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.neoforgeclient.event.NeoClientEventRegister;
import xiao.customgun.neoforgeclient.input.NeoKeyMapping;
import xiao.customgun.neoforgeclient.minecraft.access.NeoClientAccessTransformer;

public class CustomGunNeoforgeClient {

    protected static boolean initialized;
    public static IEventRegister eventRegister;
    public static IKeyMapping.Creator keyMappingCreator;
    public static IClientAccessTransformer accessTransformer;

    public static void init() {
        if (initialized) return;
        CustomGunNeoforgeClient.eventRegister = new NeoClientEventRegister();
        CustomGunNeoforgeClient.keyMappingCreator = new NeoKeyMapping.Creator();
        CustomGunNeoforgeClient.accessTransformer = new NeoClientAccessTransformer();

        CustomGunClient.init(CustomGunNeoforgeClient.keyMappingCreator,
                CustomGunNeoforgeClient.accessTransformer);
        initialized = true;
    }
}

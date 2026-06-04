/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.command;

import xiao.customgun.CustomGun;

public class CommandArg {

    public static final String MOD_ID = CustomGun.MOD_ID;
    public static final String MOD_ID_SHORT = CustomGun.MOD_ID_SHORT;

    public static final String ENTITY = "target";
    public static final String ENABLE = "enable";

    // AttachmentLockCommand
    public static final String ATTACHMENT_LOCK = "attachment_lock";
    public static final String GUN_ATTACHMENT_LOCK = "AttachmentLock";

    // ConfigCommand
    public static final String CONFIG = "config";
    public static final String KEY = "key"; // 把 Forge的Enum 用 literal 代替了所以用不上
    public static final String STATE = "state";

    // ConvertCommand
    public static final String CONVERT = "convert";

    // DebugCommand
    public static final String DEBUG = "debug";

    // DummyAmmoCommand
    public static final String DUMMY = "dummy";
    public static final String AMOUNT = "dummyAmount";

    // HideTooltipPartCommand
    public static final String HIDE_TOOLTIP_PART = "hide_tooltip_part";
    public static final String MASK = "mask";

    // ListPackCommand

    // OverwriteCommand
    public static final String OVERWRITE = "overwrite";

    // ReloadCommand
    public static final String RELOAD = "reload";
}

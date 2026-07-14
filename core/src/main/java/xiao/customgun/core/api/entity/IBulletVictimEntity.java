/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity;

import xiao.customgun.core.api.entity.victim.IBulletVictimEntityImpact;
import xiao.customgun.core.api.entity.victim.IBulletVictimKnockback;

/*
文档译名: 受弹实体 (XiaoColorful译); 否决译名: 受弹物(没指明Entity), 受击实体(范围超出Bullet), 子弹承受者(太啰嗦), 中弹实体(跟一般会出现的描述混淆)
 */
public interface IBulletVictimEntity extends IBulletVictimEntityImpact, IBulletVictimKnockback {
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity;

import xiao.customgun.core.api.entity.shooter.IShooterModifierCacheHolder;
import xiao.customgun.core.api.entity.shooter.IGunOperator;
import xiao.customgun.core.api.entity.shooter.IShooterState;
import xiao.customgun.core.api.entity.shooter.ISynGunState;

/*
文档译名: 射手生物 (XiaoColorful译); 否决译名: 射击生物(跟动词混淆), 射击者(范围超出了接口默认的LivingEntity)
 */
public interface ILivingShooter extends IGunOperator, IShooterState, ISynGunState, IShooterModifierCacheHolder {

    /**
     * 初始化枪械操作的各个数据，如换弹冷却、开火冷却等。
     */
    void cgc$initLivingShooter();

    ShooterProperty cgc$getShooterProperty();

    /**
     * 曳光弹计数器自增 1，并根据传入的曳光弹间隔计算当前子弹是否为曳光弹。
     * @param tracerCountInterval 曳光弹间隔
     * @return 是否为曳光弹
     */
    boolean cgc$nextBulletIsTracer(int tracerCountInterval);
}

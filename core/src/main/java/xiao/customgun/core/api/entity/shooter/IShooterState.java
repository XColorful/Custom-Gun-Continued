/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity.shooter;

public interface IShooterState extends IShooterLatency {

    /**
     * 服务端，该操作者是否受弹药数影响
     *
     * @return 如果为 false，那么开火时不会检查弹药，无论是玩家背包内还是枪械内的
     */
    boolean cgc$needCheckAmmo();

    /**
     * 服务端，开火是否消耗弹药
     *
     * @return 如果为 false，那么开火不会消耗枪械弹药
     */
    boolean cgc$consumesAmmoOrNot();

    /**
     * 根据情况返回玩家应当处于的冲刺状态，在玩家切换冲刺状态的时候调用。
     * 这里的逻辑应该严格与客户端端对应，如果不对应，会出现客户端表现和服务端不符的情况。
     * （例如客户端的视觉效果是玩家在冲刺，而服务端玩家实际上没有冲刺）
     * TODO
     */
    boolean cgc$getProcessedSprintStatus(boolean sprint);
}

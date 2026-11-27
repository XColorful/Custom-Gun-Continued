package dev.xcolorful.customgun.core.api.entity.shooter;

import org.jetbrains.annotations.ApiStatus;

public interface ICommonGunOperator {

    // --------行为动作--------

    void cgc$prone(boolean isProne);

    // --------枪械操作--------

    void cgc$switchFireMode();
    void cgc$aim(boolean isAim);
    void cgc$melee();
    void cgc$bolt();
    void cgc$reload();
    /**
     * 临时创建，具体接口设计待定
     */
    @ApiStatus.Internal
    boolean cgc$canRefit();

    // --------Deprecated--------

    @Deprecated default void cgc$fireSelect() {
        this.cgc$switchFireMode();
    }
}

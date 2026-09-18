package dev.xcolorful.customgun.core.api.entity.shooter;

public interface ICommonGunOperator {

    // --------行为动作--------

    void cgc$prone(boolean isProne);

    // --------枪械操作--------

    void cgc$switchFireMode();
    void cgc$aim(boolean isAim);
    void cgc$melee();
    void cgc$bolt();
    void cgc$reload();

    // --------Deprecated--------

    @Deprecated default void cgc$fireSelect() {
        this.cgc$switchFireMode();
    }
}

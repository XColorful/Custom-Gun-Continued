package xiao.customgun.api.common;

import xiao.customgun.CustomGun;

public interface ISideOnly {

    default boolean clientSideOnly() {
        return false;
    }
    default boolean serverSideOnly() {
        return false;
    }
    default boolean inProperSide() {
        return inProperSide(CustomGun.getMcSide());
    }
    default boolean inProperSide(McSide mcSide) {
        if (clientSideOnly() && mcSide == McSide.DEDICATED_SERVER) {
            return false;
        } else return !serverSideOnly() || mcSide != McSide.CLIENT;
    }
}

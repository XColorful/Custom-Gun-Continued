package xiao.customgun.core.developer;

public class PlannedRefactor {

    // 发声音包的逻辑，如果不整点活那干嘛不走原版playsound?
    public static final boolean ON_SEND_SOUND_MESSAGE = false;
    // 抽离客户端播放声音时的神秘数字(参数)
    public static final boolean ON_MAGIC_CLIENT_SOUND_VOLUME = false;
    // 背包满时吐出物品
    public static final boolean ON_DROP_ITEM_ENTITY_INSTEAD = false;
    // 子弹消耗
    public static final boolean ON_CONSUME_AMMO = false;
    // 创造模式子弹限制
    public static final boolean ON_CREATIVE_NO_AMMO_CHECK = false;
    // 瞄准时是否允许冲刺
    public static final boolean SPRINT_ON_AIMING = false;
    // 装弹时是否允许冲刺
    public static final boolean SPRINT_ON_RELOADING = false;
}

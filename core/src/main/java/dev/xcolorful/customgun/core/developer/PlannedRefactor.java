package dev.xcolorful.customgun.core.developer;

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
    // 是否启用向后兼容 (之后移到Forge config里)
    public static final boolean ENABLE_BACK_COMPATIBILITY = true;
    // Screen解耦 (Manager管理/post事件)
    public static final boolean ON_CREATE_SCREEN = false;
    // 将Assets pojo的data移到Data
    public static final boolean MOVE_ASSETS_TO_DATA = true;
    // 粒子大小
    public static final float PARTICLE_SIZE = 0.05f;
    // 生成粒子(事件钩子)
    public static final boolean ON_SPAWN_BULLET_PARTICLE = false;
    // 子弹盒颜色
    public static final int MAGIC_AMMO_BOX_COLOR = 0x727d6b;
    // onProjectileTick (onGameTick)
    public static final boolean ON_PROJECTILE_TICK_EVENT = false;
    // onProjectileTickFinish (onGameTickFinish)
    public static final boolean ON_PROJECTILE_TICK_FINISH_EVENT = false;
    // 添加config来做一般Entity的击退 (扩展模组就应该直接注入IBulletVictimEntity而无需这个重构)
    public static final boolean ON_NON_BULLET_VICTIM_HIT = true;
    // scope数据(dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay)从资源包移到数据包里，并且服务端可能还要加开镜后的散布减小
    public static final boolean MOVE_SCOPE_VIEW_INDEX_TO_CORE = false;
    // 专门处理刺刀范围
    public static final boolean SPECIAL_MELEE_RANGE_CALCULATION = false;
    // 获取上膛子弹数 (写进GunData里)
    public static final int GET_MAX_BARREL_AMMO = 1;
    // 缓存100个ItemStack，渲染的时候直接hash get (equal改成引用) 来查找，物品状态更新和过期清理都挂在client tick，如有必要可以上并发
    public static final boolean CACHE_ITEM_STACK_FOR_RENDER = false;
    // 统一枪械api，避免到处拉屎
    public static final boolean UNIFY_GUN_API = false;
    // 添加机制来在开火时/手动输入按键时才拉栓
    public static final boolean AUTO_BOLT = true;
}


# 缓存系统 — CGC 重构版

> `ShooterGunPropertyCache` 的架构设计、生命周期、与 `GunPropertyManager` 的配合关系。

## 命名语义

`ShooterGunPropertyCache` 的名称明确了三个事实：
- **Shooter** — 绑定在射手（`ILivingShooter`）上
- **Gun** — 缓存的是枪械属性
- **Property** — 缓存内容是被配件修改过的属性值（不仅仅是配件的原始数据）

与 TaCZ 的 `AttachmentCacheProperty` 相比，消除了 "Attachment" 引起的歧义（TaCZ 的缓存是枪械属性缓存，不是配件数据本身的缓存）。

## 实体接口架构

```
                    ILivingShooter
                    ├── IGunOperator (枪械操作: draw/shoot/aim...)
                    ├── IShooterState (弹药检查/冲刺状态)
                    ├── ISynGunState (同步状态查询)
                    └── IGunCacheHolder (缓存存取) ← 本体系关注
                            ├── cgc$updateGunPropertyCache(ShooterGunPropertyCache)
                            └── cgc$getGunPropertyCache() → @Nullable ShooterGunPropertyCache
```

### IGunCacheHolder

```java
public interface IGunCacheHolder {
    void cgc$updateGunPropertyCache(ShooterGunPropertyCache propertyCache);
    @Nullable ShooterGunPropertyCache cgc$getGunPropertyCache();
}
```

**接口隔离原则**：缓存存取被独立在 `IGunCacheHolder` 接口中。消费缓存值的代码只需依赖此接口，不需要知道 `IGunOperator` 的全部方法。

### 实现：LivingEntityMixin

```java
// LivingEntityMixin 实现
@Override public void cgc$updateGunPropertyCache(ShooterGunPropertyCache propertyCache) {
    this.cgc$shooterProperty.shooterGunModifierCache = propertyCache;
}
@Override public @Nullable ShooterGunPropertyCache cgc$getGunPropertyCache() {
    return this.cgc$shooterProperty.shooterGunModifierCache;
}
```

缓存在 `ShooterProperty.shooterGunModifierCache` 字段中按实体存储。

## ShooterProperty 的缓存字段

```java
public class ShooterProperty {
    // ... 其他字段 ...
    
    /**
     * 配件修改过的各种属性缓存
     */
    @Nullable
    public ShooterGunPropertyCache shooterGunModifierCache = null;
}
```

注意 `resetProperty()` 方法**不会**清除 `shooterGunModifierCache`，这意味着缓存可以跨状态重置保留。缓存只在明确更新时被替换。

## GunPropertyManager — 缓存编排

`xiao.customgun.core.entity.shooter.modifier.ShooterGunModifierManager`

这是 CGC 缓存体系的管理器（对应 TaCZ 的 `AttachmentPropertyManager`）。

### 当前实现

```java
public class GunPropertyManager {
    public static void postChangeEvent(LivingEntity livingShooter) {
        postChangeEvent(livingShooter, livingShooter.getMainHandItem());
    }

    public static void postChangeEvent(LivingEntity livingShooter, ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        // 1. 计算缓存值
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter);
        ShooterGunPropertyCache gunPropertyCache = updateShooterGunPropertyCache(gunIndexInstance, iGun, gunItem);

        // 2. 触发事件
        CustomGun.getEventPoster().postCustomEvent(new ShooterGunPropertyCacheEvent(
            CustomGun.getSideExecutor().getLogicalSide(),
            iLivingShooter, livingShooter,
            iGun, gunItem,
            gunPropertyCache));

        // 3. 写入缓存
        iLivingShooter.cgc$updateGunPropertyCache(gunPropertyCache);
    }

    private static ShooterGunPropertyCache updateShooterGunPropertyCache(
            GunIndexInstance gunIndexInstance, IGun iGun, ItemStack gunItem) {
        ShooterGunPropertyCache gunPropertyCache = new ShooterGunPropertyCache();
        GunData gunData = gunIndexInstance.getGunData();
        // TODO 原 ChangeGunPropertyEvent — 计算逻辑待实现
        return gunPropertyCache;
    }
}
```

### 管线设计

```mermaid
sequenceDiagram
    participant Draw as LivingShooterDraw
    participant GPM as GunPropertyManager
    participant SGPC as ShooterGunPropertyCache
    participant Event as ShooterGunPropertyCacheEvent
    participant SP as ShooterProperty

    Draw->>GPM: postChangeEvent(shooter, gunItem)

    Note over GPM: 1. 验证枪械有效性
    GPM->>GPM: IGunGetter.fromItemStack()
    GPM->>GPM: ResourceApi.getGunIndexInstance()

    Note over GPM: 2. 计算缓存
    GPM->>SGPC: new ShooterGunPropertyCache()
    GPM->>GPM: updateShooterGunPropertyCache(...)
    Note over GPM: TODO: 遍历 AttachmentModifierType<br/>读取 AttachmentData + GunData<br/>计算最终缓存值

    Note over GPM: 3. 触发事件
    GPM->>Event: new ShooterGunPropertyCacheEvent(...)
    GPM->>Event: CustomGun.getEventPoster().postCustomEvent()
    Note over Event: 监听器可在此修改 cache

    Note over GPM: 4. 写入缓存
    GPM->>SP: cgc$updateGunPropertyCache()
    SP-->>GPM: shooterProperty.shooterGunModifierCache = cache
```

### 待实现的 TODO

`updateShooterGunPropertyCache()` 中标注了 `// TODO 原 ChangeGunPropertyEvent`。需要实现的内容对应 TaCZ 的：

1. **初始化**：遍历所有 `AttachmentModifierType`，从 `GunData` 读取枪械的默认属性值创建初始缓存
2. **收集配件数据**：遍历枪上安装的所有配件，收集每个 `AttachmentModifierType` 对应的修改数据
3. **计算**：对每个 modifier，将默认值 + 所有配件的修改值组合计算写入最终缓存
4. **Lua 脚本更新**：对 `allCacheModifiableByScript()` 中的属性，调用脚本引擎更新
5. **脚本数据管理**：`ShooterProperty.scriptData` 字段用于缓存 Lua 脚本的数据

## 缓存的生命周期

### 创建/更新时机

缓存在以下时机创建或更新：
1. **实体初始化**：`LivingEntityMixin.cgc$initLivingShooter()` → `GunPropertyManager.postChangeEvent()`
2. **切枪**：`LivingShooterDraw.draw()` → `GunPropertyManager.postChangeEvent()`
3. **脚本触发**：Lua 脚本调用的更新路径

### 不更新时机

`ShooterProperty.resetProperty()` **不重置** `shooterGunModifierCache`，理由：
- 缓存与枪械物品相关，不是射击状态相关
- 重置射击状态（如换弹后）不需要重新计算配件属性
- 只有在切枪/换枪时才需要刷新缓存

## ShooterGunPropertyCache 类设计（待实现）

当前 `ShooterGunPropertyCache.java` 是一个空类。根据 TaCZ 原版分析和 CGC 架构，待实现的字段应包含：

```java
// 预期设计（示意）
public class ShooterGunPropertyCache {
    // 每个 AttachmentModifierType 对应的缓存值
    // 不是简单的 Map<String, CacheValue>，而是强类型字段：
    // - 简单数值型（_SimpleModifierData）：共享加数/百分比/唯一乘数 + 脚本函数
    // - 复合型：各自处理（如爆炸有多个子属性、后坐力有 pitch/yaw）
    
    // 值类型不限于 Float — 见 AttachmentModifierType.dataType
}
```

**设计约束**：
1. 不能是简单的 `Map<String, Float>` — 不是所有缓存值都是 `Float` 类型
2. 需要能存储各乘区的中间结果（用于后坐力的 `ParameterizedCache` 等价物）
3. 需要支持 Lua 脚本的 `function` 字段执行

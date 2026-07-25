
# 迁移对照 — TaCZ → CGC Modifier 体系

> TaCZ 原版配件修改器体系到 CGC 重构版的逐个类/概念迁移映射。

## 核心类迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `com.tacz.guns.resource.modifier.AttachmentCacheProperty` | `xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache` | 重命名：明确绑定 `ILivingShooter` 生命周期；移到 `api.entity.shooter.modifier` 包 |
| `com.tacz.guns.resource.modifier.AttachmentPropertyManager` | `xiao.customgun.core.entity.shooter.modifier.ShooterGunModifierManager` | 重命名：强调是射手枪械修饰缓存管理器 |
| `com.tacz.guns.entity.shooter.ShooterDataHolder` | `xiao.customgun.core.api.entity.ShooterProperty` | 重命名：精简名称 |
| `com.tacz.guns.api.entity.IGunOperator` | `xiao.customgun.core.api.entity.ILivingShooter` (+ `IShooterModifierCacheHolder` 等) | 拆分为多个细粒度接口 |
| `com.tacz.guns.api.event.common.AttachmentPropertyEvent` | `xiao.customgun.core.api.event.shooter.ShooterGunModifierCacheEvent` | 重命名 + 从 Forge Event 迁移到 CGC CustomEvent |
| `com.tacz.guns.api.modifier.IAttachmentModifier` | `xiao.customgun.core.api.item.attachment.modifier.AttachmentModifierType` (枚举) + `IAttachmentModifier<K,V>`（门面接口） | 接口 → 枚举持有接口引用 |

## 数据类迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `com.tacz.guns.resource.pojo.data.attachment.Modifier` (POJO) | `xiao.customgun.core.resource.data.data.attachment.__ModifierData<T>` (ResourcePojo) | POJO → ResourcePojo 框架；字段重命名 |
| `Modifier.addend` | `__ModifierData.sharedBaseAdd` | 语义明确化 |
| `Modifier.percent` | `__ModifierData.sharedPercentAdd` | 同上 |
| `Modifier.multiplier` | `__ModifierData.uniqueMultiplier` | 同上 |
| `Modifier.function` | `__ModifierData.scriptFunction` | 同上 |
| `com.tacz.guns.resource.pojo.data.attachment.AttachmentData` (Gson POJO) | `xiao.customgun.core.resource.data.data.AttachmentData` (ResourcePojo) | POJO → ResourcePojo；`Map<String, JsonProperty<?>>` → 强类型字段 |

## Modifier 标识体系迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `String` key + `IAttachmentModifier<T,K>` 接口 | `GunModifierType` 枚举（类型标识） + `AttachmentModifierType` 枚举（计算实例） | 字符串 → 编译期安全的枚举 |
| 注册在 `AttachmentPropertyManager.MODIFIERS` Map | `AttachmentModifierType` 枚举常量（附属于 `GunModifierType`） | 集中定义；当前 gun modifiers 被 attachment modifiers 一一对应实现，未来可增加非 attachment 来源（如 ammo modifier） |
| 各 Modifier 内部 `Data` 类的 `@SerializedName` | `GunModifierTypeTag` 常量类 | 标签集中管理 |
| `AttachmentDataTag` | 引用 `GunModifierTypeTag`，OLD1 变体保留于此 | 计划移除，OLD1 迁移至 `AttachmentData.fromJsonReader` 内部 |

## 具体 Modifier 类型迁移

| TaCZ Modifier 类 | CGC 对应类型 | 状态 |
|---|---|---|
| `AdsModifier (ID="ads")` | `AttachmentModifierType.ADS` + `AdsModifier.INSTANCE` | 已迁移（`AdsModifier` + `IAdsModifier`） |
| `AmmoSpeedModifier (ID="ammo_speed")` | `AttachmentModifierType.BULLET_SPEED` + `BulletSpeedModifier.INSTANCE` | 已迁移（`BulletSpeedModifier` + `IAmmoSpeedModifier`） |
| `ArmorIgnoreModifier (ID="armor_ignore")` | `AttachmentModifierType.ARMOR_IGNORE_PERCENT` + `ArmorIgnoreModifier.INSTANCE` | 已迁移（`ArmorIgnoreModifier` + `IArmorIgnoreModifier`） |
| `DamageModifier (ID="damage")` | `AttachmentModifierType.DAMAGE_CALCULATION` + `DamageCalculationModifier.INSTANCE` | 已迁移（`DamageCalculationModifier` + `IDamageCalculationModifier`） |
| `EffectiveRangeModifier (ID="effective_range")` | `AttachmentModifierType.EFFECTIVE_RANGE` + `EffectiveRangeModifier.INSTANCE` | 已迁移（`EffectiveRangeModifier` + `IEffectiveRangeModifier`） |
| `ExplosionModifier (ID="explosion")` | `AttachmentModifierType.BULLET_EXPLOSION` + `BulletExplosionModifier.INSTANCE` | 已迁移（`BulletExplosionModifier` + `IBulletExplosionModifier`，拆分子属性） |
| `ExtraMovementModifier (ID="movement_speed")` | 待定 | 尚未在 CGC 中明确对应 |
| `HeadShotModifier (ID="head_shot")` | `AttachmentModifierType.HEADSHOT_MULTIPLIER` + `HeadshotMultiplierModifier.INSTANCE` | 已迁移（`HeadshotMultiplierModifier` + `IHeadshotMultiplierModifier`） |
| `IgniteModifier (ID="ignite")` | `AttachmentModifierType.FIRE_ASPECT` + `FireAspectModifier.INSTANCE` | 已迁移（`FireAspectModifier` + `IFireAspectModifier`） |
| `InaccuracyModifier (ID="inaccuracy")` | `AttachmentModifierType.{AIM,SNEAK,PRONE,OTHER}_INACCURACY` + 各自 INSTANCE | **拆分为 4 个独立 modifier** |
| `KnockbackModifier (ID="knockback")` | `AttachmentModifierType.KNOCKBACK_STRENGTH` + `KnockbackStrengthModifier.INSTANCE` | 已迁移（`KnockbackStrengthModifier` + `IKnockbackStrengthModifier`） |
| `PierceModifier (ID="pierce")` | `AttachmentModifierType.PIERCE_COUNT` + `PierceCountModifier.INSTANCE` | 已迁移（`PierceCountModifier` + `IPierceCountModifier`） |
| `RecoilModifier (ID="recoil")` | `AttachmentModifierType.RECOIL_DATA` + `RecoilDataModifier.INSTANCE` | 已迁移（`RecoilDataModifier` + `IRecoilDataModifier`，pitch/yaw 拆分） |
| `RpmModifier (ID="rpm")` | `AttachmentModifierType.RPM` + `RpmModifier.INSTANCE` | 已迁移（`RpmModifier` + `IRpmModifier`） |
| `SilenceModifier (ID="silence")` | `AttachmentModifierType.MUZZLE` + `MuzzleModifier.INSTANCE` | 已迁移（`MuzzleModifier` + `IMuzzleModifier`） |
| `WeightModifier (ID="weight_modifier")` | `AttachmentModifierType.WEIGHT` + `WeightModifier.INSTANCE` | 已迁移（`WeightModifier` + `IWeightModifier`） |

#### WEIGHT

`WeightModifier` 已迁移完成——`AttachmentModifierType.WEIGHT` 持有 `WeightModifier.INSTANCE`，实现 `IWeightModifier<AttachmentData>`。`getBase` 由 `IWeightModifier` 的 default 方法提供（`gunData.getWeight()`）。

#### Inaccuracy

TaCZ 的 `InaccuracyModifier` 处理 5 种散布类型（STAND, MOVE, SNEAK, LIE, AIM）。CGC 将其拆分为 4 个独立的 modifier：
- `AIM_INACCURACY` — 瞄准散布
- `SNEAK_INACCURACY` — 潜行散布
- `PRONE_INACCURACY` — 趴下散布
- `OTHER_INACCURACY` — 其余姿态散布（对应 STAND + MOVE）

## 事件体系迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `AttachmentPropertyEvent` (Forge Event) | `ShooterGunModifierCacheEvent` (CustomEvent) | 事件总线变更 |
| `ChangeGunPropertyEvent` (内部处理器) | `ShooterGunModifierManager.updateShooterGunModifierCache()` | 内部逻辑从事件移到管理器 |
| Forge `@SubscribeEvent` | CGC `ICustomEventHandler` 接口 | 监听注册方式变更 |
| `MinecraftForge.EVENT_BUS.post(event)` | `CustomGun.getEventPoster().postCustomEvent(event)` | 事件派发方式变更 |
| KubeJS 桥接 (`KubeJSGunEventPoster`) | CGC 脚本桥接 | 脚本系统变更 |

## 实体绑定迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `IGunOperator.getCacheProperty()` | `IShooterModifierCacheHolder.cgc$getGunModifierCache()` | 接口分离 + 重命名 |
| `IGunOperator.updateCacheProperty()` | `IShooterModifierCacheHolder.cgc$updateGunModifierCache()` | 接口分离 + 重命名 |
| `ShooterDataHolder.cacheProperty` | `ShooterProperty.shooterGunModifierCache` | 字段重命名 |
| `LivingEntityMixin` 实现 `IGunOperator` | `LivingEntityMixin` 实现 `ILivingShooter` (含 `IShooterModifierCacheHolder`) | 接口层次变更 |

## 脚本体系迁移

| TaCZ | CGC |
|---|---|
| `ModernKineticGunScriptAPI.getCachedProperty(String id)` | 待定（脚本 API 尚未迁移） |
| `iGun.modifyProperty(dataHolder, ...)` | 待定 |
| LuaJ `ScriptEngine` 共享实例 | `ThreadLocal<ScriptEngine>`（通过 `ScriptUtils`） |
| `@CacheModifiableByScript` | `IGunModifier.evalByScript`（接口 default 方法） |
| `@ValueModifiableAtRuntime` | 待定 |

## 消费方迁移

| TaCZ 消费者 | CGC 消费者 | 状态 |
|---|---|---|
| `LivingEntitySpeedModifier` | `LivingShooterSpeedModifier` | 连接已就绪，读取为 TODO |
| `LocalPlayerAim` | `LocalShooterAim` | 连接已就绪，读取为 TODO |
| `LivingEntityAim`（服务端） | `LivingShooterAim` | 连接已就绪，读取为 TODO |
| `EntityKineticBullet` | `GunProjectile` | 连接已就绪，已废弃方法中有接口 |
| `GunData.getShootInterval` | `LivingShooterShoot._getShootInterval` | 连接已就绪，读取为 TODO |
| `GunPropertyDiagrams` | 待定 | 改装台 GUI 尚未迁移 |

## 下一个重构步骤

1. **实现 `ShooterGunModifierCache.initAttachmentModifiers()` 的配件遍历**：每个 `AttachmentCategory` 遍历，收集 modifier 数据并计算
2. **解 TODO 所有消费方**：逐个实现从缓存读取具体值的逻辑（通过 `cache.getValue(type, I*Modifier.class)`）
3. **移除 `AttachmentDataTag`**：OLD1 变体迁移到 `AttachmentData.fromJsonReader` 内部
4. **迁移脚本 API**：通过 `IGunModifier.evalByScript` 实现批量脚本修改
5. **迁移改装台 GUI**：实现 `DiagramsData` 的等价物

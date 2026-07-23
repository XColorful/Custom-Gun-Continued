
# 迁移对照 — TaCZ → CGC Modifier 体系

> TaCZ 原版配件修改器体系到 CGC 重构版的逐个类/概念迁移映射。

## 核心类迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `com.tacz.guns.resource.modifier.AttachmentCacheProperty` | `xiao.customgun.core.api.entity.shooter.ShooterGunPropertyCache` | 重命名：明确绑定 `ILivingShooter` 生命周期 |
| `com.tacz.guns.resource.modifier.AttachmentPropertyManager` | `xiao.customgun.core.item.gun.GunPropertyManager` | 重命名：强调是枪械属性管理，移到 `item.gun` 包 |
| `com.tacz.guns.entity.shooter.ShooterDataHolder` | `xiao.customgun.core.api.entity.ShooterProperty` | 重命名：精简名称 |
| `com.tacz.guns.api.entity.IGunOperator` | `xiao.customgun.core.api.entity.ILivingShooter` (+ `IGunCacheHolder` 等) | 拆分为多个细粒度接口 |
| `com.tacz.guns.api.event.common.AttachmentPropertyEvent` | `xiao.customgun.core.api.event.shooter.ShooterGunPropertyCacheEvent` | 重命名 + 从 Forge Event 迁移到 CGC CustomEvent |

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
| `String` key + `IAttachmentModifier<T,K>` 接口 | `AttachmentModifierType` 枚举 | 字符串 → 编译期安全的枚举 |
| 注册在 `AttachmentPropertyManager.MODIFIERS` Map | 枚举常量 + `MODIFIER_TYPES` 查找表 | 集中定义 |
| 各 Modifier 内部 `Data` 类的 `@SerializedName` | `AttachmentModifierTypeTag` 常量类 | 标签集中管理 |

## 具体 Modifier 类型迁移

| TaCZ Modifier 类 | CGC 对应类型 | 状态 |
|---|---|---|
| `AdsModifier (ID="ads")` | `AttachmentModifierType.ADS` + `_SimpleModifierData` | 已迁移数据类，计算逻辑 TODO |
| `AmmoSpeedModifier (ID="ammo_speed")` | `AttachmentModifierType.BULLET_SPEED` + `_SimpleModifierData` | 已迁移数据类 |
| `ArmorIgnoreModifier (ID="armor_ignore")` | `AttachmentModifierType.ARMOR_IGNORE_PERCENT` + `_SimpleModifierData` | 已迁移数据类 |
| `DamageModifier (ID="damage")` | `AttachmentModifierType.DAMAGE_CALCULATION` + `_SimpleModifierData` | 已迁移数据类 |
| `EffectiveRangeModifier (ID="effective_range")` | `AttachmentModifierType.EFFECTIVE_RANGE` + `_SimpleModifierData` | 已迁移数据类 |
| `ExplosionModifier (ID="explosion")` | `AttachmentModifierType.BULLET_EXPLOSION` + `_BulletExplosionModifierData` | 已迁移数据类，拆分为子属性 |
| `ExtraMovementModifier (ID="movement_speed")` | 待定 | 尚未在 CGC 中明确对应 |
| `HeadShotModifier (ID="head_shot")` | `AttachmentModifierType.HEADSHOT_MULTIPLIER` + `_SimpleModifierData` | 已迁移数据类 |
| `IgniteModifier (ID="ignite")` | `AttachmentModifierType.FIRE_ASPECT` + `_FireAspectModifierData` | 已迁移数据类 |
| `InaccuracyModifier (ID="inaccuracy")` | `AttachmentModifierType.{AIM,SNEAK,PRONE,OTHER}_INACCURACY` + `_SimpleModifierData` | **拆分为 4 个独立 modifier** |
| `KnockbackModifier (ID="knockback")` | `AttachmentModifierType.KNOCKBACK_STRENGTH` + `_SimpleModifierData` | 已迁移数据类 |
| `PierceModifier (ID="pierce")` | `AttachmentModifierType.PIERCE_COUNT` + `_SimpleModifierData` | 已迁移数据类 |
| `RecoilModifier (ID="recoil")` | `AttachmentModifierType.RECOIL_DATA` + `_RecoilDataModifierData` | 已迁移数据类，pitch/yaw 拆分为两个 `_SimpleModifierData` |
| `RpmModifier (ID="rpm")` | `AttachmentModifierType.RPM` + `_SimpleModifierData` | 已迁移数据类 |
| `SilenceModifier (ID="silence")` | `AttachmentModifierType.MUZZLE` + `_MuzzleModifierData` | 已迁移数据类，从 pair 简化为 `FireSoundType` 枚举 |
| `WeightModifier (ID="weight_modifier")` | `AttachmentModifierType.WEIGHT` + `Float.class` | 已迁移，不使用 `__ModifierData`（简单 float） |

### 重要拆分：Inaccuracy

TaCZ 的 `InaccuracyModifier` 处理 5 种散布类型（STAND, MOVE, SNEAK, LIE, AIM）。CGC 将其拆分为 4 个独立的 modifier：
- `AIM_INACCURACY` — 瞄准散布
- `SNEAK_INACCURACY` — 潜行散布
- `PRONE_INACCURACY` — 趴下散布
- `OTHER_INACCURACY` — 其余姿态散布（对应 STAND + MOVE）

## 事件体系迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `AttachmentPropertyEvent` (Forge Event) | `ShooterGunPropertyCacheEvent` (CustomEvent) | 事件总线变更 |
| `ChangeGunPropertyEvent` (内部处理器) | `GunPropertyManager.updateShooterGunPropertyCache()` (TODO) | 内部逻辑从事件移到管理器 |
| Forge `@SubscribeEvent` | CGC `ICustomEventHandler` 接口 | 监听注册方式变更 |
| `MinecraftForge.EVENT_BUS.post(event)` | `CustomGun.getEventPoster().postCustomEvent(event)` | 事件派发方式变更 |
| KubeJS 桥接 (`KubeJSGunEventPoster`) | CGC 脚本桥接 | 脚本系统变更 |

## 实体绑定迁移

| TaCZ | CGC | 变更说明 |
|---|---|---|
| `IGunOperator.getCacheProperty()` | `IGunCacheHolder.cgc$getGunPropertyCache()` | 接口分离 |
| `IGunOperator.updateCacheProperty()` | `IGunCacheHolder.cgc$updateGunPropertyCache()` | 接口分离 |
| `ShooterDataHolder.cacheProperty` | `ShooterProperty.shooterGunPropertyCache` | 字段重命名 |
| `LivingEntityMixin` 实现 `IGunOperator` | `LivingEntityMixin` 实现 `ILivingShooter` (含 `IGunCacheHolder`) | 接口层次变更 |

## 脚本体系迁移

| TaCZ | CGC |
|---|---|
| `ModernKineticGunScriptAPI.getCachedProperty(String id)` | 待定（脚本 API 尚未迁移） |
| `iGun.modifyProperty(dataHolder, ...)` | 待定 |
| LuaJ `ScriptEngine` 共享实例 | 待定 |
| `@CacheModifiableByScript` (SOURCE 注解) | 待定 |
| `@ValueModifiableAtRuntime` (SOURCE 注解) | 待定 |

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

1. **填充 `ShooterGunPropertyCache`**：定义字段结构，实现类型安全的缓存存取
2. **实现 `GunPropertyManager.updateShooterGunPropertyCache()`**：替换 `// TODO 原 ChangeGunPropertyEvent`
3. **解 TODO 所有消费方**：逐个实现从缓存读取具体值的逻辑
4. **迁移脚本 API**：确定 Lua 脚本如何读取/修改缓存值
5. **迁移改装台 GUI**：实现 `DiagramsData` 的等价物

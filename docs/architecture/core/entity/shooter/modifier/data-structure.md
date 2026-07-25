
# JSON 数据结构

> CGC 对配件 JSON 数据格式的重构设计：`__ModifierData<T>` 基类体系、`AttachmentData` 强类型字段、JSON 标签常量层级。

## 设计变更概述

| 维度 | 原版 | 重构版 |
|---|---|---|
| 数据存储 | `Map<String, JsonProperty<?>>` + 运行时类型检查 | 每个 modifier 有独立的强类型 nullable 字段 |
| JSON 读取 | Gson `@SerializedName` + 每个 Modifier 的 `readJson()` | `ResourcePojo` 框架的 `fromJsonReader()` + `switch-case` |
| 标签管理 | 散落在各 Modifier 内部 Data 类的 `@SerializedName` | 集中在 `__ModifierDataTag`、`GunModifierTypeTag`、`AttachmentDataTag`（OLD1兼容） |
| 向后兼容 | 各 Modifier 在 `readJson()` 中自行处理 | 在 `fromJsonReader()` 中用 OLD1 标签变体和 `applyBackCompatibility()` 统一处理 |
| 验证 | 无统一机制 | `ResourcePojo.validatePojo()` + `isValid()` 标志 |

## __ModifierData<T> — 基础类

`xiao.customgun.core.resource.data.data.attachment.__ModifierData`

```java
public abstract class __ModifierData<T extends __ModifierData<T>> extends ResourcePojo<T> {
    private float sharedBaseAdd = 0;       // 共享加数（所有配件对此属性的加数求和）
    private float sharedPercentAdd = 0;    // 共享百分比加成（所有配件对此属性的百分比求和）
    private float uniqueMultiplier = 1;    // 唯一乘数（多个配件之间选择最佳值）
    private @Nullable String scriptFunction;  // Lua 脚本函数名
}
```

### 字段语义对比

| CGC 字段名 | 原版对应字段 | 语义说明 |
|---|---|---|
| `sharedBaseAdd` | `Modifier.addend` | **共享加数**：同一属性上所有配件的此值累加。语义与 TaCZ 一致 |
| `sharedPercentAdd` | `Modifier.percent` | **共享百分比加成**：同一属性上所有配件的此值累加。语义与 TaCZ 一致 |
| `uniqueMultiplier` | `Modifier.multiplier` | **唯一乘数**：同一属性上多个配件间可能只选最优值（不是简单连乘）。命名反映此意图 |
| `scriptFunction` | `Modifier.function` | Lua 脚本函数。语义与 TaCZ 一致 |

**设计意图**：`shared` vs `unique` 前缀明确区分了两种计算模式——shared 值在所有配件间累加，unique 值在多个配件间选优。这与 TaCZ 原版的 `addend`/`percent`（共享）和 `multiplier`（唯一）对应，但命名更直观。

### JSON 标签体系

`__ModifierDataTag` 定义了序列化标签：

```java
public class __ModifierDataTag {
    public static final String SHARED_BASE_ADD = "shared_base_add";
    public static final String SHARED_BASE_ADD_OLD1 = "addend";          // 旧版兼容

    public static final String SHARED_PERCENT_ADD = "shared_percent_add";
    public static final String SHARED_PERCENT_ADD_OLD1 = "percent";  // 旧版兼容

    public static final String UNIQUE_MULTIPLIER = "unique_multiplier";
    public static final String UNIQUE_MULTIPLIER_OLD1 = "multiplier"; // 旧版兼容

    public static final String SCRIPT_FUNCTION = "script_function";
    public static final String SCRIPT_FUNCTION_OLD1 = "function";     // 旧版兼容
}
```

每个标签都有对应的 `_OLD1` 变体，在 `fromJsonReader()` 中同时接受新旧两种 JSON 键名。

## _SimpleModifierData — 数值型修改器

`xiao.customgun.core.resource.data.data.attachment._SimpleModifierData`

直接继承 `__ModifierData<_SimpleModifierData>`，不添加额外字段。是最常用的修改器数据类型。

使用的 modifier（所有只涉及数值加减乘除的）：
ADS、DamageCalculation、HeadshotMultiplier、ArmorIgnorePercent、BulletSpeed、PierceCount、KnockbackStrength、RPM、EffectiveRange、AimInaccuracy、SneakInaccuracy、ProneInaccuracy、OtherInaccuracy

## _FireAspectModifierData — 点燃修改器

`xiao.customgun.core.resource.data.data.attachment._FireAspectModifierData`

**不继承** `__ModifierData`，而是独立继承 `ResourcePojo`。因为点燃是布尔语义，不适用数值计算框架。

```java
public class _FireAspectModifierData extends ResourcePojo<_FireAspectModifierData> {
    private boolean igniteEntity = false;
    private boolean igniteBlock = false;
}
```

JSON 标签（`_FireAspectModifierDataTag`）：
- `IGNITE_ENTITY = "ignite_entity"`, OLD1 = `"entity"`
- `IGNITE_BLOCK = "ignite_block"`, OLD1 = `"block"`

## _BulletExplosionModifierData — 爆炸修改器

`xiao.customgun.core.resource.data.data.attachment._BulletExplosionModifierData`

**不继承** `__ModifierData`，因为爆炸有多个子属性，每种子属性可能各自使用数值计算。

```java
public class _BulletExplosionModifierData extends ResourcePojo<_BulletExplosionModifierData> {
    private boolean enableExplode = false;
    private @Nullable _SimpleModifierData explodeDamageModifier;
    private @Nullable _SimpleModifierData explodeScaleModifier;
    private @Nullable _SimpleModifierData maxDelaySecondsModifier;
    private boolean enableKnockback = false;
    private boolean enableWorldDestruction = false;
}
```

设计特点：内嵌了三个 `_SimpleModifierData` 实例，它们各自使用 `sharedBaseAdd`/`sharedPercentAdd`/`uniqueMultiplier`/`scriptFunction` 进行数值计算。爆炸开关（`enableExplode`、`enableKnockback`、`enableWorldDestruction`）是布尔值，使用 OR 语义。

## _RecoilDataModifierData — 后坐力修改器

`xiao.customgun.core.resource.data.data.attachment._RecoilDataModifierData`

```java
public class _RecoilDataModifierData extends ResourcePojo<_RecoilDataModifierData> {
    private @Nullable _SimpleModifierData pitchRecoilModifier;
    private @Nullable _SimpleModifierData yawRecoilModifier;
}
```

Pitch（垂直）和 Yaw（水平）各有独立的 `_SimpleModifierData`。需要独立计算因为后坐力需要保留各乘区原始值用于后续动态缩放。

## _MuzzleModifierData — 枪口修改器

`xiao.customgun.core.resource.data.data.attachment._MuzzleModifierData`

```java
public class _MuzzleModifierData extends ResourcePojo<_MuzzleModifierData> {
    private FireSoundType fireSoundType;
}
```

`FireSoundType` 是枚举（非数值），决定开火音效类型（如是否使用消音音效）。

## _MeleeModifierData — 近战修改器

`xiao.customgun.core.resource.data.data.attachment._MeleeModifierData`

```java
public class _MeleeModifierData extends ResourcePojo<_MeleeModifierData> {
    private float meleeDamage;
    private float meleeDistance;
    private float rangeAngle;
    private float damageDelaySeconds;
    private float baseCooldown;
    private float knockbackStrength;
    private List<_TargetEffectData> targetEffect;
}
```

七个直接字段，都是基本类型或简单 POJO。无嵌套 `__ModifierData`。

## AttachmentData — 强类型配件总数据

`xiao.customgun.core.resource.data.data.AttachmentData`

CGC 的重构版 `AttachmentData` 不再使用 `Map<String, JsonProperty<?>>`，而是用**强类型的 nullable 字段**：

```java
public final class AttachmentData extends ResourcePojo<AttachmentData> {
    // 瞄准速度
    private @Nullable _SimpleModifierData adsModifier;

    // 子弹属性（8个独立字段）
    private @Nullable _SimpleModifierData armorIgnorePercentModifier;
    private @Nullable _SimpleModifierData headshotMultiplierModifier;
    private @Nullable _SimpleModifierData damageCalculationModifier;
    private @Nullable _SimpleModifierData bulletSpeedModifier;
    private @Nullable _SimpleModifierData pierceCountModifier;
    private @Nullable _FireAspectModifierData fireAspectModifier;
    private @Nullable _SimpleModifierData knockbackStrengthModifier;
    private @Nullable _BulletExplosionModifierData bulletExplosionModifier;

    // 枪械属性（7个独立字段）
    private @Nullable _SimpleModifierData rpmModifier;
    private @Nullable _RecoilDataModifierData recoilDataModifier;
    private @Nullable _SimpleModifierData effectiveRangeModifier;
    private @Nullable _SimpleModifierData weightModifier;
    private @Nullable _MuzzleModifierData muzzleModifier;
    private @Nullable _SimpleModifierData aimInaccuracyModifier;
    private @Nullable _SimpleModifierData sneakInaccuracyModifier;
    private @Nullable _SimpleModifierData proneInaccuracyModifier;
    private @Nullable _SimpleModifierData otherInaccuracyModifier;

    // 近战 + 弹匣
    private @Nullable _MeleeModifierData meleeModifier;
    private @Nullable MagazineCategory magazineCategory;
}
```

### JSON 反序列化

使用 `ResourcePojo` 框架的流式解析（`fromJsonReader`），而非 Gson 反射：

```java
protected AttachmentData fromJsonReader(JsonReader reader) throws IOException {
    AttachmentData pojo = new AttachmentData();
    reader.beginObject();
    while (reader.hasNext()) {
        String key = reader.nextName();
        switch (key) {
            case AttachmentDataTag.ADS -> pojo.adsModifier = JsonUtils.read(reader, _SimpleModifierData::fromJson);
            case AttachmentDataTag.ARMOR_IGNORE_PERCENT,
                 AttachmentDataTag.ARMOR_IGNORE_PERCENT_OLD1 ->
                     pojo.armorIgnorePercentModifier = JsonUtils.read(reader, _SimpleModifierData::fromJson);
            // ... 其他字段类似处理
            default -> reader.skipValue();
        }
    }
    reader.endObject();
    return pojo;
}
```

**关键设计**：
- 每个 `case` 可以匹配多个标签（新名 + OLD1），实现向后兼容
- `switch` 语句配合 `ResourcePojo` 流式解析，性能优于 Gson 反射
- 未知键自动跳过，不受 JSON 字段顺序影响
- 每个字段的 `@Nullable` 表示该配件可能不提供此修改

### 验证

`validatePojo()` 方法在数据加载后执行，检查所有嵌套对象的有效性：

```java
protected void validatePojo() {
    if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();
    // 验证所有嵌套的 ResourcePojo 子对象
    if (this.fireAspectModifier != null) this.fireAspectModifier.validate();
    // ...
    boolean isValid = /* 所有嵌套对象都有效 */;
    this.setValid(isValid);
}
```

`applyBackCompatibility()` 在验证前执行，使用 `ENABLE_BACK_COMPATIBILITY` 开关控制旧包兼容处理。

## GunModifierTypeTag — 标签层级

`xiao.customgun.core.api.item.gun.modifier.GunModifierTypeTag`

标签常量的体系结构：

```
GunModifierTypeTag
├── ADS = "ads"
├── HEADSHOT_MULTIPLIER  → _BulletSkillDataTag.HEADSHOT_MULTIPLIER
├── ARMOR_IGNORE_PERCENT → _BulletSkillDataTag.ARMOR_IGNORE_PERCENT
├── DAMAGE_CALCULATION   → _BulletSkillDataTag.DAMAGE_CALCULATION
├── BULLET_SPEED         → _BulletDataTag.BULLET_SPEED
├── PIERCE_COUNT         → _BulletDataTag.PIERCE_COUNT
├── FIRE_ASPECT          → _BulletDataTag.FIRE_ASPECT
├── KNOCKBACK_STRENGTH   → _BulletDataTag.KNOCKBACK_STRENGTH
├── BULLET_EXPLOSION     → _BulletDataTag.BULLET_EXPLOSION
├── RPM                  → GunDataTag.RPM
├── RECOIL_DATA          → GunDataTag.RECOIL_DATA
├── EFFECTIVE_RANGE = "effect_range"
├── WEIGHT = "weight"
├── MUZZLE = "muzzle"
├── AIM_INACCURACY = "aim_inaccuracy"
├── SNEAK_INACCURACY = "sneak_inaccuracy"
├── PRONE_INACCURACY = "prone_inaccuracy"
├── OTHER_INACCURACY = "other_inaccuracy"
├── MELEE = "melee"
└── MAGAZINE_CATEGORY = "magazine_category"
```

**设计特点**：
- 标签定义可以引用其他数据类的标签常量（如 `_BulletSkillDataTag`），减少重复定义
- `GunModifierTypeTag` 是标签值的**权威来源**——`AttachmentDataTag` 的所有新格式标签直接引用 `GunModifierTypeTag` 同名字段
- `AttachmentDataTag` 中的 OLD1 变体是 JSON 向后兼容的唯一职责保留，计划在后续重构中移除

### 标签管理与 Modifier 标识的解耦

标签是纯数据标识符：Modifier 的类型标识由 `GunModifierType` 枚举提供，计算实例由 `AttachmentModifierType` 枚举持有。三者解耦。

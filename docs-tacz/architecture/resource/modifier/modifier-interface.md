[English](#English)

# IAttachmentModifier 接口与具体实现

> `IAttachmentModifier<T, K>` 接口是 Modifier 体系的核心契约，包含 JSON 解析、缓存初始化、计算和 UI 数据四个职责。

### 接口定义

`com.tacz.guns.api.modifier.IAttachmentModifier`

```java
public interface IAttachmentModifier<T, K> {
    String getId();                                               // 唯一标识符
    default String getOptionalFields() { return ""; }             // 旧版 JSON 字段名（兼容）

    JsonProperty<T> readJson(String json);                        // 从 JSON 读取并转为中间类型 T
    CacheValue<K> initCache(ItemStack gunItem, GunData gunData);  // 从枪械默认数据初始化缓存
    void eval(List<T> modifiedValues, CacheValue<K> cache);       // 用所有配件的值计算最终缓存

    @OnlyIn(Dist.CLIENT)
    default List<DiagramsData> getPropertyDiagramsData(...);      // 改装台属性图数据
    @OnlyIn(Dist.CLIENT)
    default int getDiagramsDataSize() { return 0; }               // 属性条数量
}
```

#### 泛型参数

- **T** — JSON 读取后的中间数据类型。大部分为 `Modifier`，少数为自定义类型（如 `Pair<Modifier, Boolean>`）
- **K** — 最终缓存值的类型。可以是 `Float`, `Integer`, `LinkedList<DistanceDamagePair>`, `Map<InaccuracyType, Float>` 等任意类型

#### 四个核心方法

| 方法 | 调用时机 | 职责 |
|---|---|---|
| `readJson` | 数据包加载（反序列化阶段） | 把 JSON 字符串解析为 `JsonProperty<T>`，处理向后兼容 |
| `initCache` | 每次切枪/缓存刷新时 | 从 `GunData` 读取枪械默认值作为缓存的初始值 |
| `eval` | 收集完所有配件的修改数据后 | 将 `initCache` 的默认值 + 所有配件的 `T` 值汇总计算，写入 `CacheValue<K>` |
| `getPropertyDiagramsData` | 客户端改装台 GUI | 返回属性条数据（默认值百分比、修改值百分比、文本等） |

## 具体修改器一览

### 分类总览

| 分类 | Modifier | ID | T (JSON读取类型) | K (缓存值类型) | 默认值来源 |
|---|---|---|---|---|---|
| **瞄准** | AdsModifier | `"ads"` | `Modifier` | `Float` | `gunData.getAimTime()` |
| **伤害** | DamageModifier | `"damage"` | `Modifier` | `LinkedList<DistanceDamagePair>` | ExtraDamage + FireModeAdjust + SyncConfig 乘子 |
| **伤害** | HeadShotModifier | `"head_shot"` | `Modifier` | `Float` | ExtraDamage.headShotMultiplier + FireModeAdjust + SyncConfig |
| **伤害** | ArmorIgnoreModifier | `"armor_ignore"` | `Modifier` | `Float` | ExtraDamage.armorIgnore + FireModeAdjust + SyncConfig |
| **弹道** | AmmoSpeedModifier | `"ammo_speed"` | `Modifier` | `Float` | BulletData.speed + FireModeAdjust |
| **弹道** | EffectiveRangeModifier | `"effective_range"` | `Modifier` | `Float` | ExtraDamage 第一段距离 或 Integer.MAX_VALUE |
| **弹道** | PierceModifier | `"pierce"` | `Modifier` | `Integer` | BulletData.pierce |
| **弹道** | KnockbackModifier | `"knockback"` | `Modifier` | `Float` | BulletData.knockback + FireModeAdjust |
| **弹道** | ExplosionModifier | `"explosion"` | `ExplosionModifierValue` | `ExplosionData` | BulletData.explosionData |
| **弹道** | IgniteModifier | `"ignite"` | `Ignite` | `Ignite` | BulletData.ignite |
| **枪械** | RpmModifier | `"rpm"` | `Modifier` | `Integer` | GunData.roundsPerMinute(fireMode) |
| **枪械** | RecoilModifier | `"recoil"` | `Pair<Modifier, Modifier>` | `ParameterizedCachePair<Float, Float>` | GunData.recoil 的 pitch/yaw 最大 keyframe 值 |
| **枪械** | InaccuracyModifier | `"inaccuracy"` | `Map<InaccuracyType, Modifier>` | `Map<InaccuracyType, Float>` | GunData.inaccuracy + FireModeAdjust |
| **枪械** | WeightModifier | `"weight_modifier"` | `Modifier` | `Float` | GunData.getWeight() |
| **枪械** | SilenceModifier | `"silence"` | `Pair<Modifier, Boolean>` | `Pair<Integer, Boolean>` | GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE, false |
| **机动** | ExtraMovementModifier | `"movement_speed"` | `MoveSpeed` | `MoveSpeed` | GunData.getMoveSpeed() |

### 典型实现模式

大部分数值型修改器遵循相同的模式（以 `AdsModifier` 为例）：

```java
// 1. readJson — Gson 反序列化 + 处理旧格式
public JsonProperty<Modifier> readJson(String json) {
    Data data = CommonAssetsManager.GSON.fromJson(json, Data.class);
    Modifier ads = data.getAds();
    if (ads == null) {  // 兼容旧格式
        ads = new Modifier();
        ads.setAddend(data.getAdsAddendTime());
    }
    return new AdsJsonProperty(ads);
}

// 2. initCache — 从 GunData 读取默认值
public CacheValue<Float> initCache(ItemStack gunItem, GunData gunData) {
    return new CacheValue<>(gunData.getAimTime());
}

// 3. eval — 输入一组配件的 Modifier 列表，用 AttachmentPropertyManager.eval 计算最终值
public void eval(List<Modifier> modifiers, CacheValue<Float> cache) {
    double eval = AttachmentPropertyManager.eval(modifiers, cache.getValue());
    cache.setValue((float) eval);
}

// 4. 内嵌 Data 类 — 对应 JSON 结构
public static class Data {
    @SerializedName("ads")
    private Modifier ads;
    @Deprecated
    @SerializedName("ads_addend")
    private float adsAddendTime = 0;
}

// 5. 内嵌 JsonProperty 子类 — 生成 Tooltip 文本
public static class AdsJsonProperty extends JsonProperty<Modifier> {
    public void initComponents() {
        double eval = AttachmentPropertyManager.eval(value, 0.2);
        if (eval > 0.2) { /* 红色：变慢 */ }
        else if (eval < 0.2) { /* 绿色：变快 */ }
    }
}
```

### 特殊实现模式

#### ExplosionModifier — 复合布尔值聚合

ExplosionModifier 处理爆炸的多个子属性（半径、伤害、击退、破坏方块、延迟）。`eval` 中使用布尔 OR 语义聚合 `explode` 字段：只要任一配件启用爆炸，整个枪就启用爆炸。

```java
boolean explode = cacheValue.isExplode() || AttachmentPropertyManager.eval(explodeValues, false);
if (!explode) return; // 没有爆炸就不用计算后续
```

#### RecoilModifier — 参数化缓存

后坐力使用 `ParameterizedCache` 而非直接计算浮点值，目的是保留各乘区（addend, percent, multiplier）的原始值，以便后续通过 `eval(input)` 动态计算（例如结合瞄准进度动态缩放后坐力）。

#### InaccuracyModifier — 按类型分组的 Map

散布数据是一个 `Map<InaccuracyType, Float>`，每种射击姿态（站立、移动、潜行、趴下、瞄准）有独立的散布值。`eval` 阶段按类型分组处理配件修改。

## DiagramsData — 改装台属性图

每次客户端打开改装台 GUI 时，会遍历所有注册的修改器并调用 `getPropertyDiagramsData`。返回的 `DiagramsData` record 包含：

```java
record DiagramsData(
    double defaultPercent,     // 默认值在显示刻度上的百分比
    double modifierPercent,    // 修改量在显示刻度上的百分比
    Number modifier,           // 修改量数值（用于判断正负）
    String titleKey,           // 属性名语言键
    String positivelyString,   // 正向修改时显示的字符串
    String negativeString,     // 负向修改时显示的字符串
    String defaultString,      // 无修改时显示的字符串
    boolean positivelyBetter   // true=越大越好(绿色), false=越小越好
)
```

每个修改器可以返回多个 `DiagramsData`（例如 InaccuracyModifier 返回 4 条，RecoilModifier 返回 2 条）。

# English

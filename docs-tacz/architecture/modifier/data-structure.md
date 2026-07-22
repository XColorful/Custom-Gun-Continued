
# JSON 数据结构与格式要求

> 配件 JSON 文件的完整格式规范，包含 `Modifier`、`AttachmentData` 和各类具体修改器数据格式。

## 文件位置

配件数据定义在数据包的 `data/<namespace>/attachments/data/` 目录下，文件名既是配件 ID。

## Modifier — 通用修饰器 POJO

`com.tacz.guns.resource.pojo.data.attachment.Modifier`

每个修改器字段都可以使用 `Modifier` 结构表达数值变化：

```jsonc
{
  "addend": 0.0,      // 加数 — 在基础值上直接累加
  "percent": 0.0,     // 百分比加成 — 最终乘以 (1 + percent)
  "multiplier": 1.0,  // 乘数 — 最终乘以 multiplier
  "function": null    // Lua 自定义函数 — 用 Lua 表达式进行最终处理
}
```

### 计算顺序

所有配件对同一属性的修改通过 `AttachmentPropertyManager.eval()` 统一计算：

```
result = (defaultValue + Σaddend) × max(Σ(1+percent), 0) × Π(max(multiplier, 0))
```

然后对每个配件的 `function` 字段逐一执行 Lua 表达式进行二次处理。

### 字段含义

| 字段 | JSON Key | 类型 | 默认值 | 说明 |
|---|---|---|---|---|
| addend | `addend` | double | 0 | 直接加减值。例如伤害+2 |
| percent | `percent` | double | 0 | 百分比增减。例如后坐力-20% |
| multiplier | `multiplier` | double | 1 | 倍率乘算。例如倍率0.5表示减半 |
| function | `function` | String? | null | Lua 表达式，变量 x=当前值, r=原始默认值, y=返回结果 |

### Lua function 执行

```
// Lua 脚本环境
x = value        // 当前已计算的值（addend/percent/multiplier 之后）
r = defaultValue  // 默认值
// 执行 function 字符串表达式
y = 计算结果      // 输出必须赋值给 y
```

## AttachmentData — 配件总数据 POJO

`com.tacz.guns.resource.pojo.data.attachment.AttachmentData`

```java
public class AttachmentData {
    // 核心：由 JSON 反序列化过程中通过 IAttachmentModifier.readJson 填充
    private Map<String, JsonProperty<?>> modifier = Maps.newHashMap();

    // 直接字段
    private float weight = 0;
    private int extendedMagLevel = 0;

    @Nullable
    private MeleeData meleeData = null;
}
```

### JSON 反序列化流程

配件 JSON 的反序列化不是在 `AttachmentData` 中完成的，而是由 `CommonAttachmentIndexSerializer` 负责：

1. Gson 把 JSON 解析为 `JsonObject`
2. 遍历 `AttachmentPropertyManager.MODIFIERS` 注册表中的每个 `IAttachmentModifier`
3. 对每个修改器，用其 `getId()` 在 `JsonObject` 中查找对应 key
4. 如存在，调用 `IAttachmentModifier.readJson(json)` 得到 `JsonProperty<T>`
5. 存入 `AttachmentData.modifier` Map（key = modifier id, value = JsonProperty）
6. 同时处理 `getOptionalFields()` 返回的旧版本字段名

### JsonProperty<T> — 中间类型

`com.tacz.guns.api.modifier.JsonProperty`

```java
public abstract class JsonProperty<T> {
    @Nullable private T value;
    protected List<Component> components = Lists.newArrayList();

    public abstract void initComponents(); // 生成配件 Tooltip 的描述文本
}
```

每个修改器定义自己的 `JsonProperty` 子类，重写 `initComponents()` 来生成配件的 tooltip 提示文本。

## 具体修改器的 JSON 格式

### 数值型修改器（使用 Modifier）

大部分修改器直接使用 `Modifier` 作为数据格式。以下修改器的 JSON 字段名和结构一致：

| Modifier | JSON Key | 示例 |
|---|---|---|
| AdsModifier | `ads` | `{"ads": {"addend": -0.1}}` |
| AmmoSpeedModifier | `ammo_speed` | `{"ammo_speed": {"percent": 0.15}}` |
| ArmorIgnoreModifier | `armor_ignore` | `{"armor_ignore": {"addend": 0.2}}` |
| DamageModifier | `damage` | `{"damage": {"multiplier": 1.2}}` |
| EffectiveRangeModifier | `effective_range` | `{"effective_range": {"percent": 0.3}}` |
| HeadShotModifier | `head_shot` | `{"head_shot": {"multiplier": 1.5}}` |
| KnockbackModifier | `knockback` | `{"knockback": {"addend": 0.5}}` |
| PierceModifier | `pierce` | `{"pierce": {"addend": 1}}` |
| RpmModifier | `rpm` | `{"rpm": {"percent": 0.1}}` |

### 复合型修改器

部分修改器需要不止一个 `Modifier`，使用自定义的数据类：

#### SilenceModifier（消音器）

```jsonc
{
  "silence": {
    "distance": {"percent": -0.5},   // Modifier — 声音传播距离修正
    "use_silence_sound": true        // boolean — 是否使用消音音效
  }
}
```

#### ExplosionModifier（爆炸）

```jsonc
{
  "explosion": {
    "explode": true,              // boolean — 必须显式设为 true 才启用爆炸
    "radius": {"addend": 1.0},    // Modifier
    "damage": {"multiplier": 0.8},// Modifier
    "knockback": true,            // boolean
    "destroy_block": false,       // boolean
    "delay": {"addend": -0.5}     // Modifier — 爆炸延迟（秒）
  }
}
```

#### IgniteModifier（点燃）

```jsonc
{
  "ignite": {
    "ignite_entity": true,        // boolean
    "ignite_block": false         // boolean
  }
}
```

#### InaccuracyModifier（散布）

```jsonc
{
  "inaccuracy": {"percent": 0.2},        // Modifier — 影响 STAND/MOVE 类型
  "aim_inaccuracy": {"percent": -0.1},   // Modifier — 仅影响 AIM 类型
  "sneak_inaccuracy": {"percent": -0.05},// Modifier — 仅影响 SNEAK 类型
  "lie_inaccuracy": {"percent": -0.1}    // Modifier — 仅影响 LIE 类型
}
```

Inaccuracy 有 5 种类型（`InaccuracyType` 枚举）：`STAND`, `MOVE`, `SNEAK`, `LIE`, `AIM`。
- `inaccuracy` 覆盖除 AIM、SNEAK、LIE 之外的所有类型
- 各特定字段覆盖对应类型

#### RecoilModifier（后坐力）

```jsonc
{
  "recoil": {
    "pitch": {"percent": -0.2},   // Modifier — 垂直后坐力 (Pitch)
    "yaw": {"percent": -0.15}     // Modifier — 水平后坐力 (Yaw)
  }
}
```

注意：后坐力使用 `ParameterizedCachePair` 而非直接计算，以保留各乘区原始值用于后续动态计算。

#### WeightModifier（重量）

```jsonc
{
  "weight_modifier": {"addend": 0.5}   // Modifier
}
```

#### ExtraMovementModifier（移动速度修正）

```jsonc
{
  "movement_speed": {
    "base_multiplier": 0.05,     // float — 基础移动速度加成
    "aim_multiplier": -0.2,      // float — 瞄准时移速加成
    "reload_multiplier": -0.1    // float — 换弹时移速加成
  }
}
```

此修改器的 T 和 K 同为 `MoveSpeed`，不使用 `Modifier` 格式。

### 特殊字段（直接字段）

以下字段不属于 modifier Map，是 `AttachmentData` 的直接字段：

| JSON Key | Java 类型 | 说明 |
|---|---|---|
| `weight` | float (direct) | 兼容旧版本的重量字段（已弃用，改用 `weight_modifier`） |
| `extended_mag_level` | int | 扩容弹匣等级（1-3） |
| `melee` | MeleeData | 近战数据（刺刀属性） |

## 向后兼容

`IAttachmentModifier.getOptionalFields()` 返回旧版 JSON 字段名。当新格式字段不存在时，反序列化器会尝试读取旧格式字段并转换为新格式。

常见旧版字段映射：

| 旧字段 | 新字段 | 变更说明 |
|---|---|---|
| `ads_addend` (float) | `ads` (Modifier) | 简单加数升级为完整 Modifier |
| `inaccuracy_addend` (float) | `inaccuracy` (Modifier) | 同上 |
| `weight` (float, 在modifier节内) | `weight_modifier` (Modifier) | 同上 |
| `recoil_modifier.pitch/yaw` (float) | `recoil.pitch/yaw` (Modifier) | 简单百分比升级为 Modifier |

`AttachmentData` 不再包含旧版字段的直接反序列化逻辑（这部分由各 Modifier 处理），但保留 `weight` 和 `extended_mag_level` 的直接读取以兼容未迁移的旧包。

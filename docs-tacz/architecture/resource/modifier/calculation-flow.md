[English](#English)

# Modifier 计算流程

> TaCZ 原版中 `IAttachmentModifier` 与 `GunData`、`AttachmentData` 之间的数据流。本文专注于缓存计算过程本身，不涉及消费方。

### 核心数据关系

```mermaid
graph LR
    subgraph "枪械定义"
        GD["GunData<br/>（枪械的JSON）<br/>aimTime, weight,<br/>roundsPerMinute,<br/>inaccuracy, recoil,<br/>bulletData..."]
    end

    subgraph "配件定义"
        AD["AttachmentData<br/>（配件的JSON）<br/>Map&lt;String, JsonProperty&lt;?&gt;&gt; modifier<br/>+ weight, extendedMagLevel, meleeData"]
    end

    subgraph "Modifier 接口"
        MOD["IAttachmentModifier&lt;T, K&gt;"]
    end

    GD -->|"initCache: 读 base 值"| MOD
    AD -->|"modifier.get(id): 读修改数据"| MOD
    MOD -->|"eval: base + 所有 modifiers → 缓存"| CACHE["CacheValue&lt;K&gt;"]
```

**关键点**：`IAttachmentModifier` 的 `initCache` 方法签名包含 `GunData`，因为枪械的 **base 值**（配件修改前的默认值）来自 `GunData`。`eval` 方法则消费从各配件 `AttachmentData` 收集来的修改值列表。

### 计算两步走

TaCZ 的 `IAttachmentModifier<T, K>` 把计算拆成两步：

| 步骤 | 方法 | 输入 | 输出 | 调用次数 |
|---|---|---|---|---|
| 第一步 | `initCache(gunItem, gunData)` | `GunData`（枪械 base 值） | `CacheValue<K>`（初始缓存，包含默认值） | 每个 Modifier 一次 |
| 第二步 | `eval(List<T>, CacheValue<K>)` | 从各配件 `AttachmentData` 收集来的 `List<T>` | 更新 `CacheValue<K>` 为最终值 | 每个 Modifier 一次 |

**为什么需要两步？** 因为枪械自身的 base 值（来自 `GunData`）和配件修改值（来自 `AttachmentData`）来自不同的数据源，且 base 值只需要读一次。

### 完整计算序列

```mermaid
sequenceDiagram
    participant GD as GunData
    participant MOD as IAttachmentModifier
    participant AD1 as AttachmentData<br/>（配件1）
    participant AD2 as AttachmentData<br/>（配件2）
    participant AD3 as AttachmentData<br/>（配件N）

    Note over MOD: === 第一步：读取 base 值 ===
    MOD->>GD: initCache(gunItem, gunData)
    Note over GD: 例如 AdsModifier:<br/>gunData.getAimTime() → 0.2f

    Note over MOD: === 第二步：收集修改值 ===
    MOD->>AD1: modifier.get("ads").getValue()
    AD1-->>MOD: Modifier(addend=-0.05)
    MOD->>AD2: modifier.get("ads").getValue()
    AD2-->>MOD: Modifier(percent=-0.1)
    MOD->>AD3: modifier.get("ads").getValue()
    AD3-->>MOD: Modifier(multiplier=0.9)

    Note over MOD: === 第三步：计算最终值 ===
    Note over MOD: eval(List<Modifier>, CacheValue<Float>)
    Note over MOD: base=0.2, 配件1 addend=-0.05<br/>配件2 percent=-0.1, 配件3 multiplier=0.9<br/>→ (0.2-0.05)*(1-0.1)*0.9 = 0.1215
```

### GunData 的角色：base 值提供者

`GunData` 在计算管线中只扮演一个角色：**提供每个属性的 base 值（默认值）**。

| Modifier | GunData 中读取的 base 值 | 说明 |
|---|---|---|
| AdsModifier | `gunData.getAimTime()` | 瞄准所需时间（秒） |
| RpmModifier | `gunData.getRoundsPerMinute(fireMode)` | 射速（含射火模式调整） |
| DamageModifier | `BulletData.extraDamage.damageAdjust` + `FireModeAdjust` + `SyncConfig 乘子` | 伤害衰减曲线 |
| WeightModifier | `gunData.getWeight()` | 枪械重量 |
| RecoilModifier | `gunData.getRecoil()` | 后坐力关键帧 |
| InaccuracyModifier | `gunData.getInaccuracy()` + `FireModeAdjust` | 散布值 |
| SilenceModifier | `GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE` | 声音距离（来自配置文件，非 GunData） |
| ArmorIgnoreModifier | `BulletData.extraDamage.armorIgnore` + `FireModeAdjust` + `SyncConfig` | 穿甲值 |
| HeadShotModifier | `BulletData.extraDamage.headShotMultiplier` + `FireModeAdjust` + `SyncConfig` | 爆头倍率 |

可以看到 **base 值并不总是来自 GunData 本身**——有些来自 `BulletData`（GunData 的子数据），有些来自 `GunConfig`（全局配置文件）。`initCache` 方法签名的 `GunData gunData` 参数实际上是一个**获取所有枪械静态数据的入口点**。

### 设计中存在的问题

在 TaCZ 原版中，`IAttachmentModifier` 接口承担了过多职责：

1. **JSON 解析**（`readJson`）— 这个职责属于数据包加载阶段
2. **Base 值获取**（`initCache`）— 依赖 `GunData`，与 modifier 计算无关
3. **计算**（`eval`）— 参数是 `List<T>`（配件数据），这才是 modifier 的核心
4. **UI**（`getPropertyDiagramsData`）— 纯客户端职责

**核心问题**：`eval(List<T>, CacheValue<K>)` 的输入是配件修改值（来自 `AttachmentData`），但 `initCache` 的输入是 `GunData`。这两个数据源是解耦的——modifier 计算本身只关心"配件提供了什么修改数据"和"base 值是多少"，并不需要知道 base 值来自 `GunData`。

这意味着重构时可以将"获取 base 值"的职责从 modifier 接口中移出，由外部的管理器负责：
- 管理器从 `GunData` 获取 base 值
- 管理器遍历配件、调用 `IAttachmentModifier.getModifier(AttachmentData)` 获取修改数据
- 管理器调用 `IAttachmentModifier.eval(modifiers, base)` 完成计算

# English

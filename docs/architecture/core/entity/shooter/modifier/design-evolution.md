[English](#English)

# Modifier 接口设计演进

> 从 TaCZ `IAttachmentModifier<T, K>` 到 CGC 最终方案的设计推演过程。

## 起点：原版胖接口的问题

```mermaid
flowchart TB
    MI["IAttachmentModifier<br/>四个职责混在一起"]
    MI --> READ["readJson<br/>JSON 解析"]
    MI --> INIT["initCache<br/>从 GunData 获取 base 值"]
    MI --> EVAL["eval<br/>消费 AttachmentData 修改值"]
    MI --> UI["getPropertyDiagramsData<br/>UI 数据"]
```

核心问题：`initCache` 和 `eval` 的数据源不同（`GunData` vs `AttachmentData`），但都耦合在同一个接口中。

## 候选方案

### 分离 initCache：modifier 只依赖 AttachmentData

```mermaid
flowchart LR
    GM["管理器"]
    GD["GunData"]
    MOD["IAttachmentModifier"]
    AD["AttachmentData"]

    GD -->|"直接读取 base"| GM
    AD -->|"getModifier"| MOD
    GM -->|"传入 base"| MOD
    MOD -->|"eval"| RESULT["计算结果"]
```

管理器用 switch-case 硬编码每个 modifier 对应 `GunData` 的哪个字段，并将 base 值传入 `eval`。modifier 接口只保留 `getModifier(AttachmentData)` 和 `eval(Collection<K>, V base)`。

**否决原因**：运行时硬编码丢失了类型安全——"ADS modifier 对应 `gunData.getAimTime()`" 的正确性完全由程序员保证，编译器无法参与验证。

### IGunModifier 含 GunData 初值获取：用接口继承

```mermaid
flowchart LR
    subgraph "IGunModifier"
        GM_B["getModifier"]
        GB["getBase"]
        EV_B["eval"]
    end
    GD["GunData"] -->|"getBase"| GB
    AD["AttachmentData"] -->|"getModifier"| GM_B
    IAM["IAttachmentModifier<br/>直接继承 IGunModifier"]
```

`IGunModifier` 在 `IItemModifier` 的基础上增加 `getBase` 方法，作为新的上界。`IAttachmentModifier` 不再继承 `IItemModifier`，改为继承 `IGunModifier`——这样调用方拿到 `IAttachmentModifier` 时也能直接调用 `getBase`。`getModifier`（从 AttachmentData 取）和 `getBase`（从 GunData 取）语义上服务于不同场景却耦合在同一个接口中。具体类仍然要写所有三个方法，没有实现职责分离。

### 枚举持有具体类：两类 modifier 通过接口对应

```mermaid
flowchart LR
    subgraph "gun modifier"
        GMT["GunModifierType<br/>枚举"]
        GM["IGunModifier<br/>具体实现类"]
    end
    subgraph "attachment modifier"
        AMT["AttachmentModifierType<br/>枚举"]
        AM["IAttachmentModifier<br/>具体实现类"]
    end
    GMT -->|"持有实例"| GM
    AMT -->|"持有实例"| AM
    AM -->|"实现接口<br/>完成泛型对应"| GM
    AMT -->|"modifierType 字段"| GMT
```

枪械 modifier 枚举和附件 modifier 枚举各自持有对应的具体实现类。附件 modifier 的类通过实现枪械 modifier 的接口来完成泛型对应。两个枚举之间通过 `modifierType` 字段建立联系——每个 `AttachmentModifierType` 常量指向对应的 `GunModifierType`。

**否决原因**：枚举本身不存泛型，编译器无法通过枚举来保证两个 modifier 类的泛型参数一致。类型安全的验证只能推迟到运行时——区别于最终方案，最终方案在缓存 get/set 时通过 `getValue(IGunModifierHolder, Class<? extends IGunModifier>)` 强制接口泛型匹配，编译期就能捕获类型错误。

### 缓存内 switch-case 赋初值

```mermaid
flowchart LR
    CACHE["ShooterGunModifierCache"]
    GD["GunData"]
    SWITCH["switch-case 硬编码<br/>ADS → getAimTime<br/>RPM → getRpm..."]
    MOD["IAttachmentModifier"]

    GD --> SWITCH
    SWITCH -->|"设置 base"| CACHE
    MOD -->|"getModifier + eval"| CACHE
```

base 值获取全在 `ShooterGunModifierCache` 里用 switch-case 实现，modifier 接口只保留 `getModifier` + `eval`。

**否决原因**：解耦力度最大，但类型安全损失也最大——编译器完全无法参与验证每个 modifier 与 `GunData` 字段的对应关系。

## 最终方案：胖接口拆分 + 接口 default 代理

CGC 项目中已有三处使用同样的拆分手法：
- `IGunOperator` → `ILivingShooter` 组合多个子接口
- `IGun` → `IGunDataAccess` 族，用 default 方法统一实现
- `GunManager` 对外提供门面，内部由子 Manager 实现

Modifier 体系与此一脉相承：`readJson` 移到 `ResourcePojo.fromJsonReader`，`initCache` 变成 `IGunModifier.getBase`（独立接口），UI 由客户端单独处理，`eval` + 新增 `getModifier` 组成 `IItemModifier` 核心。

### 菱形继承链与泛型匹配

```mermaid
flowchart LR
    IIM["IItemModifier<br/>getModifier + eval"]
    IGM["IGunModifier<br/>+ getBase"]
    I_STAR["I*Modifier<br/>（如 IAdsModifier）<br/>K 和 V 在此固定<br/>default getBase 提供实现"]
    IAM["IAttachmentModifier<br/>对外门面<br/>T 固定为 AttachmentData"]
    BASE["AttachmentModifier 抽象基类<br/>提供 evalSimpleModifierData"]
    CONCRETE["*Modifier 具体类<br/>只写 getModifier + eval<br/>getBase 由 default 继承"]

    IIM --> IGM
    IGM --> I_STAR
    I_STAR --> IAM
    IGM --> IAM
    IIM --> IAM
    IAM -.-> BASE
    I_STAR -.-> CONCRETE
    BASE --> CONCRETE
```

菱形三条路径在 Java 编译器层面强制泛型一致性——如果 `IAdsModifier` 的 `V` 是 `Float` 而 `AttachmentModifier` 的 `V` 是 `Integer`，编译器直接拒绝。具体类只写 `getModifier` 和 `eval`，`getBase` 的职责通过接口 default 方法代理给了 `IAdsModifier`。新增 modifier 只需继承抽象基类 + 实现对应的 `I*Modifier` 接口即可。

## 与 TaCZ 的逐项对比

| 维度 | TaCZ | CGC 最终方案 |
|---|---|---|
| 接口数 | 1 个胖接口（4 方法） | 4 层接口（每层 1-2 方法） |
| base 值获取 | 具体类实现 `initCache` | 接口 `default getBase`，一次定义全继承 |
| 类型安全 | 字符串 ID → 运行时 cast | 泛型 → 编译期检查 + 接口 clash 强制匹配 |
| 具体类职责 | 4 方法全部自己写 | 只写 `getModifier` + `eval` |

TaCZ 的 `GunProperties`（`GunProperty<T>` record + 常量列表）在 CGC 中被拆分：属性标识对应 `GunModifierType.typeName`，缓存类型对应 `I*Modifier` 的泛型参数 `V`（编译期确定类型）。

# English

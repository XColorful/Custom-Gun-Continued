[English](#English)

# Modifier 接口设计演进

> 从 TaCZ 到 CGC 最终方案的设计推演过程。

## 起点：原版胖接口的问题

```mermaid
flowchart TB
    MI["IAttachmentModifier<br/>四个职责混在一起"]
    MI --> READ["readJson<br/>JSON 解析"]
    MI --> INIT["initCache<br/>从 GunData 获取 base 值"]
    MI --> EVAL["eval<br/>消费 AttachmentData 修改值"]
    MI --> UI["getPropertyDiagramsData<br/>UI 数据"]
```

核心问题：`initCache`和`eval`的数据源不同（`GunData` vs `AttachmentData`），但都耦合在同一个接口中。

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

管理器用 switch-case 硬编码每个 modifier 对应`GunData`的哪个字段，并将 base 值传入`eval`。modifier 接口只保留`getModifier`和`eval`。

否决原因：运行时硬编码丢失了类型安全——哪个 modifier 对应`GunData`的哪个字段完全由程序员保证，编译器无法参与验证。

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

`IGunModifier`在`IItemModifier`的基础上增加`getBase`方法，作为新的上界。`IAttachmentModifier`不再继承`IItemModifier`，改为继承`IGunModifier`——这样调用方拿到`IAttachmentModifier`时也能直接调用`getBase`。`getModifier`（从`AttachmentData`取）和`getBase`（从`GunData`取）语义上服务于不同场景却耦合在同一个接口中。具体类仍然要写所有三个方法，没有实现职责分离。

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

枪械 modifier 枚举和附件 modifier 枚举各自持有对应的具体实现类。附件 modifier 的类通过实现枪械 modifier 的接口来完成泛型对应。两个枚举之间通过`modifierType`字段建立联系。

否决原因：枚举本身不存泛型，编译器无法通过枚举来保证两个 modifier 类的泛型参数一致。类型安全的验证只能推迟到运行时——区别于最终方案，最终方案在缓存读写时通过强制接口泛型匹配，编译期就能捕获类型错误。

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

base 值获取全在缓存里用 switch-case 实现，modifier 接口只保留`getModifier`加`eval`。

否决原因：解耦力度最大，但类型安全损失也最大——编译器完全无法参与验证每个 modifier 与`GunData`字段的对应关系。

## 最终方案：胖接口拆分加接口 default 代理

CGC 项目中已有三处使用同样的拆分手法：

- `IGunOperator` → `ILivingShooter`组合多个子接口
- `IGun` → `IGunDataAccess`族，用 default 方法统一实现
- `GunManager`对外提供门面，内部由子 Manager 实现

Modifier 体系与此一脉相承：`readJson`移到`ResourcePojo.fromJsonReader`，`initCache`变成`getBase`（独立接口），UI 由客户端单独处理，`eval`加新增`getModifier`组成`IItemModifier`核心。

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

菱形三条路径在 Java 编译器层面强制泛型一致性——如果`IAdsModifier`的`V`是`Float`而`AttachmentModifier`的`V`是`Integer`，编译器直接拒绝。具体类只写`getModifier`和`eval`，`getBase`的职责通过接口 default 方法代理给了`IAdsModifier`。新增 modifier 只需继承抽象基类并实现对应的子接口即可。

## 与 TaCZ 的逐项对比

|维度|TaCZ|CGC 最终方案|
|---|---|---|
|接口数|1 个胖接口（4 方法）|4 层接口（每层 1 到 2 方法）|
|base 值获取|具体类实现`initCache`|接口 `default getBase`，一次定义全继承|
|类型安全|字符串 ID → 运行时 cast|泛型 → 编译期检查加接口 clash 强制匹配|
|具体类职责|4 方法全部自己写|只写`getModifier`加`eval`|

TaCZ 的`GunProperties`（record 加常量列表）在 CGC 中被拆分：属性标识对应`typeName`，缓存类型对应泛型参数`V`（编译期确定类型）。

# English

> The design progression from TaCZ to the final CGC solution.

## Starting Point: Problems with the Original Fat Interface

```mermaid
flowchart TB
    MI["IAttachmentModifier<br/>Four responsibilities mixed together"]
    MI --> READ["readJson<br/>JSON parsing"]
    MI --> INIT["initCache<br/>Get base value from GunData"]
    MI --> EVAL["eval<br/>Consume AttachmentData modifier values"]
    MI --> UI["getPropertyDiagramsData<br/>UI data"]
```

Core problem: `initCache` and `eval` depend on different data sources (`GunData` vs `AttachmentData`) yet are coupled in the same interface.

## Candidate Solutions

### Separate `initCache`: modifier depends only on `AttachmentData`

```mermaid
flowchart LR
    GM["Manager"]
    GD["GunData"]
    MOD["IAttachmentModifier"]
    AD["AttachmentData"]

    GD -->|"Read base directly"| GM
    AD -->|"getModifier"| MOD
    GM -->|"Pass base in"| MOD
    MOD -->|"eval"| RESULT["Result"]
```

The manager uses `switch-case` to hardcode which `GunData` field each modifier corresponds to, and passes the base value into `eval`. The modifier interface only retains `getModifier` and `eval`.

Rejection reason: runtime hardcoding loses type safety—which modifier corresponds to which `GunData` field is entirely guaranteed by the programmer; the compiler cannot participate in verification.

### IGunModifier includes GunData initial value retrieval: use interface inheritance

```mermaid
flowchart LR
    subgraph "IGunModifier"
        GM_B["getModifier"]
        GB["getBase"]
        EV_B["eval"]
    end
    GD["GunData"] -->|"getBase"| GB
    AD["AttachmentData"] -->|"getModifier"| GM_B
    IAM["IAttachmentModifier<br/>Directly inherits IGunModifier"]
```

`IGunModifier` adds `getBase` on top of `IItemModifier` as the new upper bound. `IAttachmentModifier` no longer inherits `IItemModifier` but inherits `IGunModifier` instead—so callers who have an `IAttachmentModifier` can also directly call `getBase`. `getModifier` (reads from `AttachmentData`) and `getBase` (reads from `GunData`) serve semantically different scenarios yet are coupled in the same interface. Concrete classes still must write all three methods; no responsibility separation is achieved.

### Enums holding concrete classes: two types of modifiers matched through interfaces

```mermaid
flowchart LR
    subgraph "gun modifier"
        GMT["GunModifierType<br/>Enum"]
        GM["IGunModifier<br/>Concrete implementation"]
    end
    subgraph "attachment modifier"
        AMT["AttachmentModifierType<br/>Enum"]
        AM["IAttachmentModifier<br/>Concrete implementation"]
    end
    GMT -->|"Holds instance"| GM
    AMT -->|"Holds instance"| AM
    AM -->|"Implements interface<br/>completing generic match"| GM
    AMT -->|"modifierType field"| GMT
```

The gun modifier enum and attachment modifier enum each hold their corresponding concrete implementation classes. The attachment modifier class completes the generic match by implementing the gun modifier class's interface. The two enums are connected through the `modifierType` field.

Rejection reason: enums themselves do not carry generics; the compiler cannot use enums to guarantee that the generic parameters of the two modifier classes are consistent. Type safety verification can only be deferred to runtime—unlike the final solution, which enforces interface generic matching at cache read/write time, catching type errors at compile time.

### Switch-case base value assignment in the cache

```mermaid
flowchart LR
    CACHE["ShooterGunModifierCache"]
    GD["GunData"]
    SWITCH["switch-case hardcoded<br/>ADS → getAimTime<br/>RPM → getRpm..."]
    MOD["IAttachmentModifier"]

    GD --> SWITCH
    SWITCH -->|"Set base"| CACHE
    MOD -->|"getModifier + eval"| CACHE
```

All base value retrieval is implemented with switch-case in the cache, and the modifier interface only retains `getModifier` plus `eval`.

Rejection reason: achieves the greatest decoupling but also loses the most type safety—the compiler cannot participate in verifying the correspondence between each modifier and `GunData` field.

## Final Solution: Fat Interface Split plus Interface default Delegation

Three places in the CGC project already use the same splitting technique:

- `IGunOperator` → `ILivingShooter` composing multiple sub-interfaces
- `IGun` → `IGunDataAccess` family, unified implementation via default methods
- `GunManager` providing a facade externally, internally implemented by sub-Managers

The modifier system follows the same approach: `readJson` moved to `ResourcePojo.fromJsonReader`, `initCache` became `getBase` (separate interface), UI handled separately by the client, and `eval` plus the new `getModifier` form the core of `IItemModifier`.

### Diamond Inheritance and Generic Matching

```mermaid
flowchart LR
    IIM["IItemModifier<br/>getModifier + eval"]
    IGM["IGunModifier<br/>+ getBase"]
    I_STAR["I*Modifier<br/>(e.g. IAdsModifier)<br/>K and V fixed here<br/>default getBase provides impl"]
    IAM["IAttachmentModifier<br/>External facade<br/>T fixed to AttachmentData"]
    BASE["AttachmentModifier abstract base<br/>Provides evalSimpleModifierData"]
    CONCRETE["*Modifier concrete class<br/>Only writes getModifier + eval<br/>getBase inherited from default"]

    IIM --> IGM
    IGM --> I_STAR
    I_STAR --> IAM
    IGM --> IAM
    IIM --> IAM
    IAM -.-> BASE
    I_STAR -.-> CONCRETE
    BASE --> CONCRETE
```

The three diamond paths enforce generic consistency at the Java compiler level—if one path has `V` as `Float` and another has `V` as `Integer`, the compiler rejects the code. Concrete classes only write `getModifier` and `eval`; the `getBase` responsibility is delegated through interface default methods. Adding a new modifier only requires extending the abstract base class and implementing the corresponding sub-interface.

## Side-by-Side Comparison with TaCZ

|Dimension|TaCZ|CGC Final Solution|
|---|---|---|
|Interface count|1 fat interface (4 methods)|4 interface layers (1 to 2 methods each)|
|Base value retrieval|Concrete class implements `initCache`|Interface `default getBase`, defined once, inherited by all|
|Type safety|`String` ID → runtime cast|Generics → compile-time checking plus interface clash enforcement|
|Concrete class responsibility|All 4 methods self-written|Only writes `getModifier` plus `eval`|

TaCZ's `GunProperties` (record plus constant list) is split in CGC: property identity maps to `typeName`, and cache type maps to generic parameter `V` (compile-time determined type).

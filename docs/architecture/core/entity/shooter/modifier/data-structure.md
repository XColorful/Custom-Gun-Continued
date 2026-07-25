[English](#English)

# JSON 数据结构

> CGC 对配件 JSON 数据格式的重构设计：修改器数据基类体系、强类型字段、JSON 标签常量层级。

## 设计变更

|维度|原版|CGC 重构版|
|---|---|---|
|数据存储|`Map` 加运行时类型检查|每个 modifier 有独立的强类型 nullable 字段|
|JSON 读取|Gson 注解加每个 Modifier 自行解析|`ResourcePojo` 框架的 `fromJsonReader`|
|标签管理|散落在各 Modifier 内部|集中在对应的 Tag 常量类|

## 修改器数据基类

所有 modifier 数据共享四个字段，语义直接绑定到字段名：

- sharedBaseAdd：共享加数，所有配件对该属性的附加值累加
- sharedPercentAdd：共享百分比，所有配件对该属性的百分比加成累加
- uniqueMultiplier：唯一乘数，取最后安装配件的值（不累加）
- scriptFunction：可选的 Lua 表达式，在数值计算后执行二次处理

大部分 modifier 直接沿用这四个字段。少数需要额外字段的 modifier（如火属性、爆炸、后坐力、枪口、近战）扩展各自的数据子类。

计算由统一的方法处理，先按公式计算再逐 modifier 执行 `scriptFunction`。

## 强类型配件总数据

CGC 不再使用 `Map` 存储 modifier，而是每个 modifier 字段独立声明为 nullable 类型。JSON 反序列化时通过 switch-case 匹配 key 并将其解析为对应的强类型字段，每个字段暴露 getter 供枚举常量通过接口方法访问。

# English

> CGC's refactored design for attachment JSON data: modifier data base class hierarchy, strongly-typed fields, and JSON tag constant tiering.

## Design Changes

|Dimension|Original|CGC Refactored|
|---|---|---|
|Data storage|`Map` with runtime type checking|Each modifier has its own strongly-typed nullable field|
|JSON reading|Gson annotations plus per-Modifier parsing|`ResourcePojo` framework's `fromJsonReader`|
|Tag management|Scattered across each Modifier's internals|Centralized in the corresponding Tag constant classes|

## Modifier Data Base Class

All modifier data shares four fields with semantics bound directly to the field names:

- sharedBaseAdd: shared additive value, accumulated across all attachments for the property
- sharedPercentAdd: shared percentage bonus, accumulated across all attachments for the property
- uniqueMultiplier: unique multiplier, takes the value of the last-installed attachment (not accumulated)
- scriptFunction: optional Lua expression, executed after numeric computation for secondary processing

Most modifiers use these four fields directly. A few modifiers that need extra fields (fire aspect, explosion, recoil, muzzle, melee) extend their own data subclasses.

Computation is handled by a unified method: the formula is applied first, then `scriptFunction` is executed per modifier.

## Strongly-Typed Attachment Data

CGC no longer uses a `Map` to store modifiers. Instead, each modifier field is independently declared as a nullable type. During JSON deserialization, a switch-case matches keys and parses them into the corresponding strongly-typed fields, with each field exposing a getter for enum constants to access through interface methods.

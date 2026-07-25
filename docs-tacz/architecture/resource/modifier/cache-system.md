[English](#English)

# 缓存系统

> 配件属性修改值的运行时缓存，绑定在实体数据上，随切枪事件刷新。

## 数据模型

每个 modifier 的缓存值类型各异：`Float`（如瞄准速度、重量）、`Integer`（如射速、穿透数）、`LinkedList`（伤害衰减曲线）、`Map`（散布值）、自定义对象（爆炸参数）等，通过泛型包装类持有。

缓存对象内部用两个 Map 存储数据——一个存最终计算结果（以 modifier ID 为键），一个存配件修改值的中间列表（计算后清除）。

## 计算流水线

数值型修改器的算术引擎按固定顺序计算：`(defaultValue + Σaddend) × max(Σ(1+percent), 0) × Π(max(multiplier, 0))`，然后对每个配件的 Lua 表达式逐一执行二次处理。

整个缓存刷新分三个阶段：

```mermaid
flowchart LR
    A[阶段一<br/>从 GunData 初始化<br/>每个属性的默认值] --> B[阶段二<br/>遍历枪上所有配件<br/>按 modifier ID 分类<br/>收集修改数据]
    B --> C[阶段三<br/>对每个属性调用 eval<br/>计算最终值并清除中间数据]
```

- 阶段一：遍历所有已注册 modifier，从 GunData 读取每个属性的默认值作为缓存初始值
- 阶段二：遍历枪上安装的所有配件（按槽位），将各配件的 Modifier 数据按 ID 分类收集
- 阶段三：对每个有修改的属性，将默认值加所有收集的修改值传入 eval 计算最终结果

## 缓存的生命周期

缓存在切枪时创建并刷新。事件流如下：

```mermaid
sequenceDiagram
    participant Draw as 切枪逻辑
    participant APM as 管理器
    participant ACP as 缓存对象
    participant Event as 事件
    participant Script as Lua 脚本
    participant Mixin as 实体 Mixin

    Draw->>APM: postChangeEvent(shooter, gunItem)
    APM->>ACP: 创建缓存对象
    APM->>Event: 创建并派发事件
    Event->>Event: KubeJS 事件派发
    Event->>ACP: eval(gunItem, gunData)
    Event->>Event: Forge 事件总线派发（第三方可修改缓存）
    APM->>Script: Lua 脚本修改（限定属性子集）
    APM->>Mixin: 写入实体缓存字段
```

触发时机：实体初始化和切枪时，由管理器统一编排。缓存写入实体后，消费方通过实体接口获取缓存对象，再按 ID 读取具体属性值。

# English

> Runtime cache for attachment property modifiers, bound to entity data and refreshed on gun draw events.

## Data Model

Each modifier's cached value has a different type: `Float` (e.g. aim speed, weight), `Integer` (e.g. fire rate, pierce count), `LinkedList` (damage falloff curve), `Map` (inaccuracy values), custom objects (explosion parameters), etc., wrapped in a generic holder class.

The cache object internally uses two Maps: one for final computation results (keyed by modifier ID), and one for intermediate modifier data lists (cleared after computation).

## Computation Pipeline

The arithmetic engine follows a fixed formula: `(defaultValue + Σaddend) × max(Σ(1+percent), 0) × Π(max(multiplier, 0))`, then executes each attachment's Lua expression for secondary processing.

The cache refresh proceeds in three phases:

```mermaid
flowchart LR
    A[Phase 1<br/>Initialize defaults<br/>from GunData] --> B[Phase 2<br/>Iterate all attachments<br/>collect modifier data<br/>by modifier ID]
    B --> C[Phase 3<br/>Call eval per property<br/>compute final value<br/>clear intermediate data]
```

- Phase 1: Iterate all registered modifiers, read each property's default value from GunData as the initial cache value
- Phase 2: Iterate all attachments on the gun (by slot), collect each attachment's modifier data classified by ID
- Phase 3: For each modified property, pass the default value plus all collected modifier values to eval for final computation

## Cache Lifecycle

The cache is created and refreshed on gun draw. The event flow:

```mermaid
sequenceDiagram
    participant Draw as Gun Draw Logic
    participant APM as Manager
    participant ACP as Cache Object
    participant Event as Event
    participant Script as Lua Script
    participant Mixin as Entity Mixin

    Draw->>APM: postChangeEvent(shooter, gunItem)
    APM->>ACP: Create cache object
    APM->>Event: Create and dispatch event
    Event->>Event: KubeJS event dispatch
    Event->>ACP: eval(gunItem, gunData)
    Event->>Event: Forge event bus dispatch (third-party can modify cache)
    APM->>Script: Lua script modification (limited property subset)
    APM->>Mixin: Write cache to entity field
```

Trigger timing: entity initialization and gun draw, both orchestrated by the manager. After the cache is written, consumers access the cache object through the entity interface and read specific property values by modifier ID.

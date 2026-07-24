
# TaCZ Attachment Modifier Architecture

> 配件属性修改器体系的完整文档。覆盖 JSON 数据格式 → 读取解析 → 缓存计算 → 实体绑定 → 事件通知 → 脚本交互 → UI 消费的完整链路。

## 体系总览

```mermaid
graph TB
    subgraph "JSON 数据包层"
        JSON["配件 JSON 文件<br/>data/&lt;namespace&gt;/attachments/data/"]
        MOD["Modifier (POJO)<br/>addend / percent / multiplier / function"]
        AD["AttachmentData (POJO)<br/>Map&lt;String, JsonProperty&lt;?&gt;&gt; modifier"]
    end

    subgraph "Modifier 接口层"
        AM["IAttachmentModifier&lt;T, K&gt;<br/>readJson / initCache / eval / getDiagrams"]
        APM["AttachmentPropertyManager<br/>注册表 + 算术引擎 + LuaJ evaluator"]
    end

    subgraph "缓存层"
        ACP["AttachmentCacheProperty<br/>Map&lt;String, CacheValue&gt; + Map&lt;String, List&gt;"]
        CV["CacheValue&lt;T&gt;<br/>包装任意缓存值类型"]
    end

    subgraph "实体绑定层"
        IGO["IGunOperator (Mixin on LivingEntity)<br/>updateCacheProperty / getCacheProperty"]
        SDH["ShooterDataHolder<br/>cacheProperty 字段"]
        LEDG["LivingEntityDrawGun.draw()<br/>触发缓存刷新"]
    end

    subgraph "事件层"
        APE["AttachmentPropertyEvent<br/>Forge 事件"]
        CGP["ChangeGunPropertyEvent<br/>调用 AttachmentCacheProperty.eval()"]
    end

    subgraph "脚本交互层"
        API["ModernKineticGunScriptAPI<br/>getCachedProperty / modifyProperty"]
        LUA["LuaJ 脚本引擎"]
    end

    subgraph "消费方"
        SHOOT["射速（RPM）"]
        AIM["瞄准时间"]
        DAMAGE["伤害计算"]
        SPEED["移动速度"]
        RECOIL["后坐力"]
        SPREAD["散布"]
        TOOLTIP["配件提示框"]
        GUI["改装台属性图"]
    end

    JSON -->|"解析"| AD
    AD -->|"getModifier()"| AM
    AM -->|"registerModifier()"| APM
    APM -->|"eval() 计算"| ACP
    ACP -->|"存入"| SDH
    LEDG -->|"draw() 时调用"| APM
    APM -->|"postChangeEvent()"| APE
    APE -->|"触发"| CGP
    CGP -->|".eval()"| ACP
    IGO -->|"getCacheProperty()"| ACP
    API -->|"Lua 读取/修改"| ACP
    SHOOT -->|"读取 RPM 缓存"| ACP
    AIM -->|"读取 ADS 缓存"| ACP
    DAMAGE -->|"读取 Damage 缓存"| ACP
    SPEED -->|"读取 Weight/MoveSpeed 缓存"| ACP
    RECOIL -->|"读取 Recoil 缓存"| ACP
    SPREAD -->|"读取 Inaccuracy 缓存"| ACP
    TOOLTIP -->|"JsonProperty.initComponents()"| AM
    GUI -->|"getPropertyDiagramsData()"| AM

    style JSON fill:#e1f5fe
    style MOD fill:#e1f5fe
    style AD fill:#e1f5fe
    style AM fill:#fff3e0
    style APM fill:#fff3e0
    style ACP fill:#f3e5f5
    style CV fill:#f3e5f5
    style IGO fill:#e8f5e9
    style SDH fill:#e8f5e9
    style APE fill:#fce4ec
    style CGP fill:#fce4ec
    style API fill:#fff9c4
```

## 体系概要

本体系解决的核心问题：**配件的 JSON 数据如何影响枪械的运行时属性**。

一把枪可能安装多个配件（瞄准镜、消音器、握把、弹匣等），每个配件都可能修改枪械的多种属性。为高效计算最终属性值并避免重复计算，系统设计了一套「读取-缓存-计算-消费」管道。

### 关键设计特征

1. **字符串键 + 泛型接口**：每个 Modifier 由字符串 ID 标识（如 `"ads"`, `"damage"`），通过 `IAttachmentModifier<T, K>` 接口统一 JSON 读取、初始化和计算行为
2. **实体绑定缓存**：计算结果缓存在 `ShooterDataHolder` 上（每个 LivingEntity 一个实例），仅在切枪时重新计算
3. **Forge 事件扩展**：通过 `AttachmentPropertyEvent` 事件允许外部模组修改缓存值
4. **LuaJ 脚本集成**：缓存值可在 Lua 脚本中被读取和修改
5. **离线计算路径**：`AttachmentDataUtils` 提供不依赖实体缓存的直接计算方式（用于工具提示等场景）

### 文档导航

| 文档 | 内容 |
|---|---|
| [JSON 数据结构与格式要求](./data-structure.md) | `Modifier`、`AttachmentData` 的 JSON schema，向后兼容规则 |
| [IAttachmentModifier 接口](./modifier-interface.md) | 接口契约、17 个具体修改器详解 |
| [缓存系统](./cache-system.md) | `AttachmentCacheProperty` 生命周期、计算流水线、LuaJ 集成 |
| [管理器与事件系统](./manager-and-event.md) | `AttachmentPropertyManager` 注册/计算引擎，事件流，实体绑定 |
| [Modifier 计算流程](./calculation-flow.md) | `GunData`、`AttachmentData`、`IAttachmentModifier` 三者间的数据流与计算过程 |
| [消费方汇总](./consumer-sites.md) | 所有读取缓存值的调用位置和场景 |

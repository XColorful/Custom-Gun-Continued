[English](#English)

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

### AttachmentData — 强类型配件总数据

`xiao.customgun.core.resource.data.data.AttachmentData`

CGC 的重构版 `AttachmentData` 不再使用 `Map<String, JsonProperty<?>>`，而是用**强类型的 nullable 字段**：

```java
public final class AttachmentData extends ResourcePojo<AttachmentData> {
    // 瞄准速度
    private @Nullable _SimpleModifierData adsModifier;

    // 子弹属性
    private @Nullable _SimpleModifierData armorIgnorePercentModifier;
    private @Nullable _SimpleModifierData headshotMultiplierModifier;
    private @Nullable _SimpleModifierData damageCalculationModifier;
    
    // ...
}
```

# English


# Modifier 计算流程 — CGC 重构版

> 重构后 `IItemModifier<T, K, V>` → `IGunModifier<T, K, V>` → `IAttachmentModifier<K, V>` → `AttachmentModifier` 的计算管线设计。

## 接口层次

```
IItemModifier<T, K, V>            (api.item)
    ├── getModifier(T pojo) → K
    └── eval(Collection<K>, V base) → V
          ↑
IAttachmentModifier<K, V>         (api.item.attachment.modifier)
  (extends IItemModifier<AttachmentData, K, V>)
          ↑
AttachmentModifier<K, V>          (item.attachment.modifier)
  (abstract class, 提供 evalSimpleModifierData 等通用实现)
          ↑
AdsModifier                        (item.attachment.modifier)
  (concrete: getModifier → AttachmentData::getAdsModifier,
            eval → evalSimpleModifierData)
```

## 设计原则

原版的 `IAttachmentModifier<T, K>` 接口有四个职责（JSON解析、base值读取、计算、UI），且方法签名混合了两个数据源：

```java
// 原版: initCache 依赖 GunData（base值来源）
CacheValue<K> initCache(ItemStack gunItem, GunData gunData);

// 原版: eval 消费 AttachmentData 中收集的修改值
void eval(List<T> modifiedValues, CacheValue<K> cache);
```

`initCache` 取 `GunData` 参数获取 base 值，但 base 值的获取实际上不属于 modifier 的职责——它是管理器的职责。

### CGC 的分离

```java
// CGC: getModifier — 只从 AttachmentData 读修改数据
@Nullable K getModifier(@NotNull T pojo);

// CGC: eval — base 值由外部（管理器）传入
V eval(Collection<K> modifiers, V base);
```

**关键区别**：
- `getModifier` **只依赖** `AttachmentData`——从配件定义中提取修改值
- `eval` 的 `base` 参数由外部调用者提供——调用者（`ShooterGunModifierManager`）负责从 `GunData` 获取 base 值
- modifier 本身不再知道 `GunData` 的存在

## 计算流程设计

```mermaid
sequenceDiagram
    participant SGMM as ShooterGunModifierManager
    participant GD as GunData
    participant IAM as IAttachmentModifier
    participant AD as AttachmentData（各配件）

    Note over SGMM: === 第一步：获取 base 值 ===
    SGMM->>GD: 读取各个属性的 base 值
    Note over GD: rpm = gunData.getRpm()<br/>aimTime = gunData.getAimTime()<br/>weight = gunData.getWeight()<br/>...

    Note over SGMM: === 第二步：收集配件modifier ===
    loop 遍历枪上安装的每个配件
        SGMM->>IAM: type.getModifier().getModifier(attachmentData)
        IAM->>AD: 读取对应的 modifier 字段
        AD-->>IAM: K (如 _SimpleModifierData, _FireAspectModifierData...)
        IAM-->>SGMM: K
    end

    Note over SGMM: === 第三步：计算 ===
    loop 每个 AttachmentModifierType
        SGMM->>IAM: type.getModifier().eval(modifiers, base)
        Note over IAM: AttachmentModifier.evalSimpleModifierData:<br/>ΣsharedBaseAdd, ΣsharedPercentAdd,<br/>ΠuniqueMultiplier, 执行 scriptFunction
        IAM-->>SGMM: 计算结果 V
    end
```

### 关键变化：base 值的地位

在 CGC 重构中，base 值**不再通过 modifier 接口从 `GunData` 获取**，而是：

1. **管理器**（`ShooterGunModifierManager`）负责从 `GunData` 提取 base 值
2. **管理器**负责遍历 `AttachmentModifierType` 枚举来编排计算
3. `IAttachmentModifier.eval(Collection<K>, V base)` 的 `base` 是外部传入的——modifier 只是执行纯计算

这意味着同一个 modifier 实例**可以用于不同的 base 值**（例如不同的枪械、不同的射火模式），modifier 本身是个无状态的纯计算单元。

## 泛型参数含义

```java
IItemModifier<T extends ResourcePojo<T>, K, V>
```

| 参数 | 绑定到 AttachmentData 时 | 含义 |
|---|---|---|
| `T` | `AttachmentData` | 数据源类型（POJO） |
| `K` | `_SimpleModifierData`, `_FireAspectModifierData`, 等 | 从 POJO 读取的**中间类型**（原始修改数据） |
| `V` | `Float`, `Integer`, 复合类型 | **值类型**（base 和计算结果） |

对于 `IAttachmentModifier<K, V>`（`T` 已绑定为 `AttachmentData`）：

```java
public interface IAttachmentModifier<K, V> extends IItemModifier<AttachmentData, K, V> {
}
```

### 具体例子

`AdsModifier` 实现了 `IAttachmentModifier<_SimpleModifierData, Float>`：

```java
public final class AdsModifier extends AttachmentModifier<_SimpleModifierData, Float> {
    // K = _SimpleModifierData → 从 AttachmentData 读取的中间类型
    public _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getAdsModifier();  // 取配件的 ads 字段
    }

    // V = Float → 值和计算结果都是 float
    public Float eval(Collection<_SimpleModifierData> modifiers, Float base) {
        return evalSimpleModifierData(modifiers, base);
        // base 来自 GunData.getAimTime()，由管理器传入
    }
}
```

## AttachmentModifier 基类的通用计算

`AttachmentModifier<K, V>` 抽象类提供了 `evalSimpleModifierData` 静态方法，处理最常见的 `_SimpleModifierData` 类型计算：

```java
public static Float evalSimpleModifierData(Collection<_SimpleModifierData> modifiers, Float base) {
    float sharedBaseAdd = 0;
    float sharedPercentAdd = 0;
    float uniqueMultiplier = 1;
    for (_SimpleModifierData modifier : modifiers) {
        sharedBaseAdd += modifier.getSharedBaseAdd();
        sharedPercentAdd += modifier.getSharedPercentAdd();
        uniqueMultiplier *= modifier.getUniqueMultiplier();
    }
    float value = (base + sharedBaseAdd) * (1 + sharedPercentAdd) * uniqueMultiplier;
    for (_SimpleModifierData modifier : modifiers) {
        value = IGunModifier.evalSimpleModifierDataByScript(base, value, modifier.getScriptFunction());
    }
    return value;
}
```

**公式**：`result = (base + ΣsharedBaseAdd) * (1 + ΣsharedPercentAdd) * ΠuniqueMultiplier`，然后逐个执行 `scriptFunction`。

脚本调用已迁移到 `IGunModifier.evalSimpleModifierDataByScript`（静态工具方法）和 `IGunModifier.evalByScript`（子接口可重写）。`ScriptUtils.eval` 使用线程隔离的 LuaJ 引擎。

## AttachmentModifierType 枚举的角色变化

重构后，`AttachmentModifierType` 枚举**直接持有 `IAttachmentModifier` 实例**，并且每个常量**附属于**一个 `GunModifierType`：

```java
public enum AttachmentModifierType implements ResourceTag.CategoryTag, IGunModifierType {
    ADS(GunModifierType.ADS, AdsModifier.INSTANCE),
    // ... 全部 20 个常量均已迁移完成
}
```

`ADS` 是第一已迁移完成的常量——构造函数直接引用 `AdsModifier.INSTANCE` 和 `GunModifierType.ADS`。所有常量均已迁移完毕。

## 计算的完整路径

```
切枪事件触发
    → ShooterGunModifierManager.postChangeEvent(shooter, gunItem)
    → ShooterGunModifierCache.of(gunIndexInstance, iGun, gunItem)
        → 遍历 AttachmentModifierType.values():
            → modifier.getBase(iGun, gunItem, gunData)
            → 存入 Map<GunModifierType, Object>
    → 触发 ShooterGunModifierCacheEvent (事件可修改缓存)
    → 写入 ShooterProperty.shooterGunModifierCache
```

## 关键差异

| 维度 | TaCZ | CGC |
|---|---|---|
| 接口声明 | `IAttachmentModifier<T, K>`（4个方法） | `IItemModifier<T, K, V>` → `IGunModifier<T, K, V>` → `IAttachmentModifier<K, V>` |
| JSON 解析 | `readJson(String)` 在 Modifier 上 | `AttachmentData.fromJsonReader(JsonReader)` |
| Base 值获取 | `initCache(gunItem, gunData)` 在 Modifier 上 | `IGunModifier.getBase`（由 `I*Modifier` 子接口 default 实现） |
| 计算 | `eval(List<T>, CacheValue<K>)` | `eval(Collection<K>, V base) → V`（纯函数） |
| Lua 脚本 | 全局 static ScriptEngine | `ThreadLocal<ScriptEngine>`，通过 `IGunModifier.evalByScript` |
| 缓存读写 | `cacheProperty.getCache("ads")` | `cache.getValue(type, IAdsModifier.class)` + 泛型推断 |

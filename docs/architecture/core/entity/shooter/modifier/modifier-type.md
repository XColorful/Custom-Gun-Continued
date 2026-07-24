
# AttachmentModifierType 枚举

> CGC 用编译期类型安全的枚举替代 TaCZ 运行时字符串键来标识和访问配件修改器。枚举直接持有 `IAttachmentModifier` 实例。

## 设计动机

### TaCZ 的问题

```java
// TaCZ 原版：字符串键 + 运行时类型转换
Map<String, IAttachmentModifier<?, ?>> MODIFIERS = Maps.newLinkedHashMap();
MODIFIERS.put("ads", new AdsModifier());
// ...
cacheProperty.<Float>getCache("ads");  // 字符串拼写错误直到运行时才发现
```

问题：
1. **无编译期检查**：`"ads"` 拼错为 `"asd"` 不会报错
2. **类型不安全**：`getCache("ads")` 返回 `Object`，需要手动 `<Float>` 强制转换
3. **注册分散**：每个 Modifier 的注册散落在 `AttachmentPropertyManager.registerModifier()` 中
4. **元数据散落**：修改器的数据类型（T、K）只在接口实现中定义，无法集中查询

### CGC 的方案

```java
// CGC：枚举直接持有 IAttachmentModifier 实例
public enum AttachmentModifierType implements ResourceTag.CategoryTag {
    ADS(AttachmentModifierTypeTag.ADS, AdsModifier.INSTANCE),
    // ... 其他常量类似
}
```

优势：
1. **编译期检查**：枚举常量禁止拼写错误
2. **集中定义**：所有 modifier 的 typeName、modifier 实例在一个位置
3. **自文档化**：枚举本身就是完整的 modifier 目录
4. **IDE 友好**：Find Usages、Rename Refactoring 等 IDE 功能都能正常工作
5. **枚举持有接口**：`ADS` 已迁移完成——枚举直接持有 `IAttachmentModifier` 实例，计算逻辑委托给该实例

## 枚举结构

```java
public enum AttachmentModifierType implements ResourceTag.CategoryTag {
    // 已迁移：枚举持有 IAttachmentModifier 实例
    ADS(AttachmentModifierTypeTag.ADS, AdsModifier.INSTANCE),

    // 未迁移：仍使用 (dataType, getter) 形式（待后续迁移）
    DAMAGE_CALCULATION(AttachmentModifierTypeTag.DAMAGE_CALCULATION,
            _SimpleModifierData.class, AttachmentData::getDamageCalculationModifier),
    // ...
}
```

### 两个核心成员

当前枚举有两种构造函数：

| 形式 | 构造函数 | 使用常量 |
|---|---|---|
| **接口实例**（新） | `(String name, IAttachmentModifier<T, V> modifier)` | `ADS` |
| **字段组合**（旧，待迁移） | `(String name, Class<T> dataType, Function<AttachmentData, T> getter)` | 其余 19 个常量 |

```java
// 新形式
public final String typeName;
public final IAttachmentModifier<?, ?> modifier;

// 旧形式（待迁移）
public final String typeName;
public final Class<?> dataType;
public final Function<AttachmentData, ?> getter;
```

```java
public IAttachmentModifier<?, ?> getModifier() {
    return this.modifier;
}
```

### 枚举常量完整列表

| 枚举常量 | typeName | 数据来源 | 迁移状态 |
|---|---|---|---|
| `ADS` | `"ads"` | `AdsModifier.INSTANCE` | **已迁移** |
| `DAMAGE_CALCULATION` | (tag) | `_SimpleModifierData` | `getDamageCalculationModifier` | **已迁移**（`DamageCalculationModifier` + `IDamageCalculationModifier`） |
| `HEADSHOT_MULTIPLIER` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `ARMOR_IGNORE_PERCENT` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `BULLET_SPEED` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `PIERCE_COUNT` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `FIRE_ASPECT` | (tag) | `_FireAspectModifierData` + getter | 待迁移 |
| `KNOCKBACK_STRENGTH` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `BULLET_EXPLOSION` | (tag) | `_BulletExplosionModifierData` + getter | 待迁移 |
| `RPM` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `RECOIL_DATA` | (tag) | `_RecoilDataModifierData` + getter | 待迁移 |
| `EFFECTIVE_RANGE` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `WEIGHT` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `MUZZLE` | (tag) | `_MuzzleModifierData` + getter | 待迁移 |
| `AIM_INACCURACY` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `SNEAK_INACCURACY` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `PRONE_INACCURACY` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `OTHER_INACCURACY` | (tag) | `_SimpleModifierData` + getter | 待迁移 |
| `MELEE` | (tag) | `_MeleeModifierData` + getter | 待迁移 |
| `MAGAZINE_CATEGORY` | (tag) | `MagazineCategory.class` + getter | 待迁移 |

## 接口层次

```
IItemModifier<T, K, V>                    泛型修饰接口
    ├── getModifier(T pojo) → K          从数据源获取修改值
    └── eval(Collection<K>, V base) → V   计算
          ↑
IAttachmentModifier<K, V>                 绑定 T=AttachmentData
          ↑
AttachmentModifier<K, V> (abstract)       通用计算实现（evalSimpleModifierData）
          ↑
AdsModifier                               具体类
```

## ResourceTag.CategoryTag 实现

```java
@Override public String getTagName() { return this.typeName; }
@Override public String getCategoryName() { return this.typeName; }
```

这使得枚举可以与 `ResourceTag` 体系集成，用于配件标签的分类管理。

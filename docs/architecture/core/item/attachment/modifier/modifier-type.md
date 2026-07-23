
# AttachmentModifierType 枚举

> CGC 用编译期类型安全的枚举替代 TaCZ 运行时字符串键来标识和访问配件修改器。

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
// CGC：枚举 + 强类型 getter
public enum AttachmentModifierType {
    ADS(AttachmentModifierTypeTag.ADS,
        _SimpleModifierData.class, AttachmentData::getAdsModifier),
    // ...

    public final String typeName;
    public final Class<?> dataType;
    public final Function<AttachmentData, ?> getter;
}

// 类型安全访问
_SimpleModifierData adsData = AttachmentModifierType.ADS.get(attachmentData, _SimpleModifierData.class);
```

优势：
1. **编译期检查**：枚举常量禁止拼写错误
2. **集中定义**：所有 modifier 的 typeName、dataType、getter 在一个位置
3. **自文档化**：枚举本身就是完整的 modifier 目录
4. **IDE 友好**：Find Usages、Rename Refactoring 等 IDE 功能都能正常工作

## 枚举结构

```java
public enum AttachmentModifierType implements ResourceTag.CategoryTag {
    ADS(AttachmentModifierTypeTag.ADS,
        _SimpleModifierData.class, AttachmentData::getAdsModifier),

    DAMAGE_CALCULATION(AttachmentModifierTypeTag.DAMAGE_CALCULATION,
        _SimpleModifierData.class, AttachmentData::getDamageCalculationModifier),
    // ... 共 20 个枚举常量
}
```

### 三个核心成员

每个枚举常量的构造函数接受三个参数：

```java
<T> AttachmentModifierType(String name, Class<T> dataType, Function<AttachmentData, T> getter)
```

| 参数 | 类型 | 含义 |
|---|---|---|
| `name` (→ `typeName`) | `String` | Modifier 标识名，也是 JSON 标签值 |
| `dataType` | `Class<?>` | 此修改器在 `AttachmentData` 中对应字段的数据类型 |
| `getter` | `Function<AttachmentData, ?>` | 从 `AttachmentData` 实例获取此修改器数据的方法引用 |

### 枚举常量完整列表

| 枚举常量 | typeName | dataType | getter |
|---|---|---|---|
| `ADS` | `"ads"` | `_SimpleModifierData` | `getAdsModifier` |
| `DAMAGE_CALCULATION` | (tag) | `_SimpleModifierData` | `getDamageCalculationModifier` |
| `HEADSHOT_MULTIPLIER` | (tag) | `_SimpleModifierData` | `getHeadshotMultiplierModifier` |
| `ARMOR_IGNORE_PERCENT` | (tag) | `_SimpleModifierData` | `getArmorIgnorePercentModifier` |
| `BULLET_SPEED` | (tag) | `_SimpleModifierData` | `getBulletSpeedModifier` |
| `PIERCE_COUNT` | (tag) | `_SimpleModifierData` | `getPierceCountModifier` |
| `FIRE_ASPECT` | (tag) | `_FireAspectModifierData` | `getFireAspectModifier` |
| `KNOCKBACK_STRENGTH` | (tag) | `_SimpleModifierData` | `getKnockbackStrengthModifier` |
| `BULLET_EXPLOSION` | (tag) | `_BulletExplosionModifierData` | `getBulletExplosionModifier` |
| `RPM` | (tag) | `_SimpleModifierData` | `getRpmModifier` |
| `RECOIL_DATA` | (tag) | `_RecoilDataModifierData` | `getRecoilDataModifier` |
| `EFFECTIVE_RANGE` | (tag) | `_SimpleModifierData` | `getEffectiveRangeModifier` |
| `WEIGHT` | (tag) | **`Float.class`** | `getWeight` |
| `MUZZLE` | (tag) | `_MuzzleModifierData` | `getMuzzleModifier` |
| `AIM_INACCURACY` | (tag) | `_SimpleModifierData` | `getAimInaccuracyModifier` |
| `SNEAK_INACCURACY` | (tag) | `_SimpleModifierData` | `getSneakInaccuracyModifier` |
| `PRONE_INACCURACY` | (tag) | `_SimpleModifierData` | `getProneInaccuracyModifier` |
| `OTHER_INACCURACY` | (tag) | `_SimpleModifierData` | `getOtherInaccuracyModifier` |
| `MELEE` | (tag) | `_MeleeModifierData` | `getMeleeModifier` |
| `MAGAZINE_CATEGORY` | (tag) | `MagazineCategory.class` | `getMagazineCategory` |

注意 `WEIGHT` 的 `dataType` 是 `Float.class`（普通 Java 类型），而非 `__ModifierData` 子类。这是唯一不使用独立 Modifier 数据类的字段。**含义：** 重量是一个简单附加值，不参与 sharedBaseAdd/sharedPercentAdd/uniqueMultiplier 的组合计算。

### 类型安全取值方法

```java
public @Nullable <T> T get(AttachmentData data, Class<T> clazz) {
    if (!clazz.isAssignableFrom(this.dataType)) {
        throw new IllegalArgumentException("Invalid modifier data type: " + clazz.getName());
    }
    Object value = getter.apply(data);
    return value != null ? clazz.cast(value) : null;
}
```

调用时：
```java
_SimpleModifierData adsData = AttachmentModifierType.ADS.get(attachmentData, _SimpleModifierData.class);
// 编译期保证 dataType 匹配 — 不会出现 ClassCastException
```

### 字符串查找

保留兼容 TaCZ 旧代码的字符串查找：

```java
private static final Map<String, AttachmentModifierType> MODIFIER_TYPES = new HashMap<>();

static {
    for (AttachmentModifierType type : values()) {
        MODIFIER_TYPES.put(type.typeName, type);
    }
}

public static @Nullable AttachmentModifierType fromString(String name) {
    return name != null ? MODIFIER_TYPES.get(name) : null;
}
```

## 未来方向：接口化

第 74 行的 TODO：

```java
// TODO ? 构造函数参数改成接口类，接口类负责定义泛型、getter/setter、::new
```

**当前问题**：枚举的三个参数（String name, Class<?> dataType, Function<...> getter）通过字段直接传递，没有接口约束。

**计划方向**：抽象出一个 `ModifierDataType<T>` 接口：

```java
// 构想中的接口
public interface ModifierDataType<T> {
    Class<T> getDataType();
    T createDefaultInstance();    // ::new
    T getFrom(AttachmentData data);  // getter
    void setTo(AttachmentData data, T value);  // setter（目前枚举中没有）
}
```

枚举常量的构造函数参数从三个独立字段变为一个接口实例：

```java
// 构想中的用法
ADS(AttachmentModifierTypeTag.ADS, new ModifierDataType<_SimpleModifierData>() {
    public Class<_SimpleModifierData> getDataType() { return _SimpleModifierData.class; }
    public _SimpleModifierData createDefaultInstance() { return new _SimpleModifierData(); }
    public _SimpleModifierData getFrom(AttachmentData data) { return data.getAdsModifier(); }
    public void setTo(AttachmentData data, _SimpleModifierData v) { data.setAdsModifier(v); }
})
```

**优势**：
- 新增 modifier 类型只需实现接口，无需修改枚举（但要新增枚举常量）
- setter 能力使得枚举可用于修改 AttachmentData（目前只能读）
- 接口可以由外部实现提供，支持扩展点

## ResourceTag.CategoryTag 实现

`AttachmentModifierType` 实现了 `ResourceTag.CategoryTag` 接口：

```java
@Override public String getTagName() { return this.typeName; }
@Override public String getCategoryName() { return this.typeName; }
```

这使得枚举可以与 `ResourceTag` 体系集成，用于配件标签的分类管理（如允许安装的配件类别）。

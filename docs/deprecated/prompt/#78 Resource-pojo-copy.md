> #78 提示词

# 

```
读git工作区，现在按照相同的方式，把CGC所有ResourcePojo批量补上copy
- copy只在最终子类实现，实现方式同GunIndex
- 所有父类的字段复制都写在父类，子类先调super
- 复制的顺序同成员变量的顺序，且成员变量间有空行（javadoc计入空行）时，copy里也空行
- 子类调super后空一行，最后一个copy和return间不空行（跟switch case的视觉效果一致)
- 对于List等要显式new的，如果List类型不需要ResourcePojo.copy，则new ArrayList直接写在同一行；否则用这种格式写：
例如对于GunData，统一var entry：
Map<FireModeType, _ChargingData> chargingData = new HashMap<>(); {
    for (var entry : this.chargingData.entrySet()) {
        chargingData.put(entry.getKey(), entry.getValue().copy());
    }
}
pojo.setChargingData(chargingData);
对于@Nullable普通类型，仍然直接set，对于List/Map等@Nullable类型，仍然要保留set调用，但多一层if：
@Nullable Map<String, Object> scriptParam = null; {
    if (this.scriptParam != null) {
        scriptParam = new HashMap<>();
        for () {
            // 同上
        }
    }
}
pojo.setScriptParam(scriptParam);
对于List类型，for改用索引(少迭代器开销)
List<FireModeType> fireModeTypes = new ArrayList<>(this.fireModeTypes.size()); {
    for (int i = 0; i < this.fireModeTypes.size(); i++) {
        fireModeTypes.add(this.fireModeTypes.get(i)); // 如果为需要copy的类型则写成.get(i).copy()
    }
}
pojo.setFireModeTypes(fireModeTypes);

如有在以上规则之外的，需要向我报告
```

# 

我先看当前工作区的改动，理解 GunIndex 的 copy 实现方式，再找出所有 ResourcePojo 子类。

已理解 GunIndex 的范式。现在枚举所有 ResourcePojo 子类。

范围比预期大：core + client 共约 70 个具体类、4 个抽象父类。我先确认现状与构建配置。

范围和规则已清楚。我先记录任务，然后逐文件读取实现。

规则基本明确，但有两类字段超出了你给的格式范围，先确认再批量改：

已确认：数组用 null 安全的 clone；@Nullable Pojo 用 `if (… != null) x = this.x.copy();`。继续读取剩余文件。

core 文件已读完。现在读 client 侧。

规则已全部确认。开始写实现，先补抽象父类。

core 侧 42 个具体类已完成。继续 client 侧。

client 侧已完成。先做静态完整性检查，再编译。

静态检查通过：73 个具体类均有 `copy()`，4 个抽象父类有 `copyTo()`。现在编译验证。

编译通过（exit 0）。做最后一遍 diff 抽查。

已完成：73 个具体 Pojo 全部补上 `copy()` / `copyTo()`，4 个抽象父类补上 `copyTo()`；`:core:compileJava` 通过（exit 0）。共修改 78 个 Java 文件。

**按你确认的规则实现**
- 数组：null 安全 `clone()`；`CompoundTag`：`this.itemNbt == null ? null : this.itemNbt.copy()`。
- `@Nullable` 嵌套 Pojo：`@Nullable T x = null; { if (this.x != null) x = this.x.copy(); }`。
- List 索引循环、Map `var entry`、复制分组与字段空行/ javadoc 分组一致，`super.copyTo` 后空行、最后一条与 `return` 紧邻。

**规则之外、需要你确认的处理（已按最小改动实现）**
1. **无 setter 的字段**，无法用 set 形式：
    - [GunDisplay.java](core/src/main/java/dev/xcolorful/customgun/client/resource/assets/display/GunDisplay.java)：`@Deprecated @Nullable String useDefaultAnimation` / `default_animation` 直接 `pojo.field = this.field`。
    - [_BulletData.java](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/gun/_BulletData.java)：`@ApiStatus.Internal bulletSkillDataOld` 直接赋值（null 安全 copy）。
    - [_Cube.java](core/src/main/java/dev/xcolorful/customgun/client/resource/assets/model/bedrock/geometry/bone/_Cube.java)：`hasMirror` 解析状态标志直接赋值。
2. **_SoundEffects.keyframes 是 `final` 字段且无 setter**，只能向 `pojo.keyframes` 直接 `put`，没有走 `set(new Map)` 形式。
3. **`_MovementData movementData` 未标 @Nullable 但实际可为空**（deprecated 可选字段），我按可空处理用了 if 块，避免 `copy()` NPE。
4. **`_GeometryModel.bones` / `_Bone.cubes` 的可空性标在 getter 上**，我按 `@Nullable List` 处理。
5. **非 final 但无子类的类**（`BedrockModel`、`_GeometryModel`、`_Uv`、`_FaceUv`、`_ModelTransformScale`、`_ResultItemData`）直接在其中实现 `copy()`，因为实际就是最终类型。
6. **`RecipeFilterData` 的派生缓存**（`_whitelistLiteral` / `_whitelistPattern` 等）也按成员变量顺序复制了；这些字段在 `validatePojo` 前为 null，对未 validate 的实例调用 `copy()` 会 NPE。

**潜在 NPE 提示**：按“信任 BlockBench 正确格式、不额外判 null”的指示，`_Animation.soundEffects`、`_GeometryModel.description`、`_Cube.uv` 等未注解字段直接 `.copy()`；若实际动画/json 允许缺省这些字段，需要再改为可空处理。

尚未提交，按规则留给你 review。

# 

```
数组都改成这个写法：
int[] extendedMagAmmoSize; {
    extendedMagAmmoSize = this.extendedMagAmmoSize != null ? this.extendedMagAmmoSize.clone() : null;
} pojo.setExtendedMagAmmoSize(extendedMagAmmoSize);
并且把之前写在花括号下面一行的setter改到花括号同一行（用一个空格分隔）
```

# 

用户已把 GunData 改成目标写法。现在批量套用到其余数组与所有块尾 setter。先确认这些块尾 setter 全是本次生成的。

已全部改完并重新编译通过（`:core:compileJava` exit 0，仅剩原有的 deprecation 警告）。

- 29 处数组复制统一为：
```java
int[] extendedMagAmmoSize; {
    extendedMagAmmoSize = this.extendedMagAmmoSize != null ? this.extendedMagAmmoSize.clone() : null;
} pojo.setExtendedMagAmmoSize(extendedMagAmmoSize);
```
含 `float @Nullable []` 形式（如 `scopeZoomScale`、`uv`）。

- 所有块（List / Map / 可空集合 / 可空 Pojo）的 setter 都移到了 `}` 同一行，空格分隔，共 112 处，例如：
```java
        } pojo.setScriptParam(scriptParam);
        } pojo.setBones(bones);
        } pojo.setHeatData(heatData);
```

- 已精确校验：不存在仍留在下一行的 `pojo.setX(...)`；CRLF 换行保持不变；编译通过。

# 

```
现在把IGunModifier，IAttachmentModifier及其子类里手动copy的部分改成统一调用ResourcePojo.copy
```

# 

我先定位 `IGunModifier`、`IAttachmentModifier` 及其实现，找出其中手动 copy 的代码。

找到 5 处手动 copy。现在统一改为调用 `copy()`。

已完成，`:core:compileJava` 通过（exit 0）。

**改为调用 `ResourcePojo.copy()` 的 6 处**
- [IDamageCalculationModifier.java](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/modifier/IDamageCalculationModifier.java)：`entry.copy()` 后覆盖 damage。
- [IRecoilDataModifier.java](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/modifier/IRecoilDataModifier.java)：`getBase` 整体改为 `gunData.getRecoilData().copy()`，删除 `_copyRecoilEntries` 及 3 个已无用 import。
- [IBulletExplosionModifier.java](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/modifier/IBulletExplosionModifier.java)：`return explosion.copy();`。
- [DamageCalculationModifier.java](core/src/main/java/dev/xcolorful/customgun/core/item/attachment/modifier/DamageCalculationModifier.java)：`entry.copy()`。
- [RecoilDataModifier.java](core/src/main/java/dev/xcolorful/customgun/core/item/attachment/modifier/RecoilDataModifier.java)：`entry.copy()`，仍用缩放后的 range 覆盖。
- [MeleeModifier.java](core/src/main/java/dev/xcolorful/customgun/core/item/attachment/modifier/MeleeModifier.java)：`return modifier.copy();`。

**保留未改的 3 处（不是单纯 copy，需你确认）**
1. `IDamageCalculationModifier.getBase` 的 `_DistanceDamageData flat`：源列表为空时合成的默认单段伤害，没有源对象可复制。
2. `RecoilDataModifier.eval` 的 `new _RecoilData()`：构造的是按倍率缩放后的新结果，两个列表都会被覆盖，用 `base.copy()` 反而先复制再丢弃。
3. `IMeleeModifier.getBase`：跨类型映射（`_MeleeData` + `_DefaultMeleeData` → `_MeleeModifierData`），字段并非一一对应：`meleeDistance ← gunBaseLength`、`extraCooldown ← baseCooldown`、`targetEffect` 重置为空，无法用单次 `copy()` 替代。

**行为变化提示**
- `MeleeModifier` 原代码重复 `setMeleeDamage` 且从未设置 `meleeDistance`；改为 `copy()` 后会复制真实的 `meleeDistance`，并且 `targetEffect` 由共享引用变为深拷贝。
- `IRecoilDataModifier.copy()` 会带上源对象的 `valid` 标志（原手写结果默认 false）。

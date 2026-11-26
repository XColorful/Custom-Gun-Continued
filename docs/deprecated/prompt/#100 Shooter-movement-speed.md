> #100 提示词

# 

```
研究下LivingShooterAttribute里UNIQUE_MULTIPLIER是否对最终移速就是直接乘一个数值，_ShootStateSpeed里默认值用0还是1？像游戏目录下\tacz\tacz_default_gun\data\tacz\data\guns\ak47_data.json里_ShootStateSpeed值用0的话，按最终乘算应该会把移速给清零？需要研究原版机制是否对应_ShootStateSpeed里使用的

按CGC的语义预期是：
（基础）x（1+百分比加成）x（独立倍率x独立倍率x……）
shared base add加在基础上
shared percent add加在百分比加成上
unique multiplier为一项独立倍率
```

# 

## 结论速览

1. **`UNIQUE_MULTIPLIER` 不是“把数值直接乘到最终移速上”。** 它映射到原版 [AttributeModifierOperation.java:18](core/src/main/java/dev/xcolorful/customgun/core/api/minecraft/attribute/AttributeModifierOperation.java:18) 的 `AttributeModifier.Operation.values()[2]`，1.20.1 里就是 `MULTIPLY_TOTAL`。该 operation 的 **amount 是增量**：每一步 `total *= (1 + amount)`。
2. 所以 **amount = 0 是 ×1（中性），不会清零**；要清零必须 amount = −1。你担心的“值用 0 会把移速清零”不成立。
3. 多个 `MULTIPLY_TOTAL` 修饰符之间确实各自独立相乘，因此你期望的公式可以精确表达为：
   `(base + Σ sharedBaseAdd) × (1 + Σ sharedPercentAdd) × Π uniqueMultiplier`
4. `_ShootStateSpeed` 是 TaCZ `MoveSpeed` 的原样移植，TaCZ 语义是**增量**语义，**默认 0 是对的**；改成 1 会让所有 TaCZ 数据整体漂移（`base: 0` 的枪直接把玩家移速乘成 0）。
5. 但如果你说的是 CGC 自己 `unique_multiplier` 字段的语义，那是**系数**语义，默认 1，写入属性时必须换算 `amount = 值 − 1`。两套语义不能混在同一个字段上。
6. 另外：**这条链路在当前分支上已经是死代码**，`shoot_state_speed`/`movement_speed` 现在根本不会被解析。

---

## 一、原版 1.20.1 的属性计算机制

`AttributeInstance.calculateValue()`（forge-1.20.1-47.4.23-sources.jar）:

```java
double d0 = baseValue;
for (ADDITION)       d0 += amount;                 // 加在基础上
double d1 = d0;
for (MULTIPLY_BASE)  d1 += d0 * amount;            // = d0 * (1 + Σ amount)
for (MULTIPLY_TOTAL) d1 *= 1.0 + amount;           // 每个修饰符是一次独立乘法
return attribute.sanitizeValue(d1);
```

关键点：

- `MULTIPLY_BASE`：每个 amount 是小数增量（0.1 = +10%），且都以 `d0` 为基数，等价于百分比加成累加。
- `MULTIPLY_TOTAL`：每个修饰符单独乘一次 → 天然就是“独立倍率 × 独立倍率 × …”；但**每个 amount 是相对于 1 的增量**。
- `MOVEMENT_SPEED` 是 `RangedAttribute(默认 0.7, min 0.0, max 1024.0)`，`sanitizeValue` 会 clamp 到 `[0, 1024]`，NaN 变 0。所以只要不是 amount ≤ −1 这种极端值，不会“清零”；清零是 clamp 出来的，不是乘法得出的。

对应到 CGC 的数据语义，映射是完全精确的：

| CGC 数据语义 | 属性操作 | 属性层 amount |
|---|---|---|
| 基础 | base value | — |
| `sharedBaseAdd` | `ADDITION` | 原值 |
| `sharedPercentAdd` | `MULTIPLY_BASE` | 原值（0 = 无加成） |
| `uniqueMultiplier` | `MULTIPLY_TOTAL` | **值 − 1**（1 = 无修正） |

---

## 二、TaCZ 原版到底怎么用 `_ShootStateSpeed`

对照 `tacz-forge-1.20.1-1.2.0-all.jar` 的字节码（不是猜测）：

`com.tacz.guns.entity.shooter.LivingEntitySpeedModifier.updateSpeedModifier()`：

```java
double value = getTargetSpeed(moveSpeed);
if (modifier 不存在 || modifier.getAmount() != value) {
    instance.removeModifier(EXTRA_SPEED_MODIFIER_UUID);
    instance.addTransientModifier(new AttributeModifier(
        EXTRA_SPEED_MODIFIER_UUID, "Extra Gun Speed Modifier",
        value, AttributeModifier.Operation.MULTIPLY_TOTAL));
}
```

- `getTargetSpeed` 的优先级：**reload > aim > base**，只取其中一个字段，不把多个状态叠加。
- JSON 值**原样**作为 `MULTIPLY_TOTAL` 的 amount，没有任何“乘数 → 增量”的换算。
- `com.tacz.guns.resource.pojo.data.gun.MoveSpeed` 的三个字段默认值都是 `0.0F`；`GunData.moveSpeed` 默认是 `new MoveSpeed()`（0/0/0）。

所以 TaCZ 自己的注释和代码是一致的（`tacz_default_gun/.../ak47_data.json:214-221`）：

```jsonc
// 额外移速修正，多种情况同时满足时，仅计算最后一个修正
// 调整的属性将作为“最终乘算”类型(MULTIPLY_TOTAL)的属性修饰器添加至实体上
"movement_speed": { "base": 0.0, "aim": -0.2, "reload": -0.15 }
```

- `base: 0.0` → ×(1+0) = ×1，**无修正**
- `aim: -0.2` → ×0.8
- `reload: -0.15` → ×0.85

字段名虽然叫 `*Multiplier`，但语义是增量 —— 这是 TaCZ 命名上的误导点之一。

两个值得注意的 TaCZ 细节：

- 枪 + 所有配件的值先**相加**（`MoveSpeed.of`），再 `Math.max(0, ...)` 钳到 ≥ 0；但 `eval` 只在“至少一个配件提供了 movement_speed”时才执行，所以无配件贡献时枪自身值原样保留（TaCZ 的一个小怪癖，不建议照搬）。
- CGC 的 weight 映射也与 TaCZ 一致：`amount = weight × (−WEIGHT_SPEED_MULTIPLIER)`，走 `MULTIPLY_BASE`（[LivingShooterAttribute.java:62-73](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAttribute.java:62)）。

---

## 三、CGC 当前代码的真实状态

在 `Shooter-movement-speed` 分支上，这段功能整体是停用/废弃的：

- [_ShootStateSpeed.java:25-27](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/gun/_ShootStateSpeed.java:25)：字段默认 `0F/0F/0F`，与 TaCZ 一致。
- [GunData.java:96](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/GunData.java:96)：解析 switch 里**没有** `GunDataTag.SHOOT_STATE_SPEED` 分支 → `shoot_state_speed` / `movement_speed` 现在会落到 `default` 被忽略，`getShootStateSpeed()` 也没有任何调用者。
- [LivingShooterAttribute.java:53](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAttribute.java:53)：`tickShootStateSpeed` 的调用被注释掉；方法体里 `speed = null` 会立即 return；`_getShootStateSpeed` 无论输入什么都返回 0。
- 更关键的是 [LivingShooterAttribute.java:87](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAttribute.java:87)：

```java
if (shootStateSpeed == 1) return; // 没有倍率乘数就跳过
```

这个判断和 `MULTIPLY_TOTAL` 语义是**矛盾的**：中性值是 0，不是 1。如果直接复活这段代码，`1` 会变成 ×2（并跳过挂载），`0` 反而会被当作需要挂载的值。应改为 `== 0`（若坚持 TaCZ 增量语义），或者先做 `amount = value - 1` 再判断 `== 1`（若采用乘数语义）。

---

## 四、默认值用 0 还是 1 —— 按“这个字段代表什么”来定

**A. 继续沿用 TaCZ 的 `movement_speed` / `shoot_state_speed`（兼容字段）：默认 0。**

- 原值直接作为 `MULTIPLY_TOTAL` 的 amount，跳过条件用 `== 0`。
- 理由：所有现存 TaCZ 数据包都按 0 = 无修正编写。改成 1 的实际后果是：`base: 0` 的枪 → amount 1 → ×2；而如果按“乘数 0”理解再换算成 amount −1，则直接 clamp 到 0，玩家不能移动。两种都错。
- 如果只是觉得“倍率字段默认 1 更直观”，可以在 POJO 内部换成乘数表示（默认 1），但在读写 JSON 的边界做换算，不要改变数据文件语义。现在已经停用并在移除列表里，改动代价最小的是保持 0。

**B. 新做 CGC 原生字段（形如 `unique_multiplier` 的乘数语义）：默认 1，amount = 值 − 1。**

- 理由：与 [__ModifierData.java:12-14](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/attachment/__ModifierData.java:12) 的默认值和 [AttachmentModifier.java:11-20](core/src/main/java/dev/xcolorful/customgun/core/item/attachment/modifier/AttachmentModifier.java:11) 的公式保持一致：
  `value = (base + Σ sharedBaseAdd) × (1 + Σ sharedPercentAdd) × Π uniqueMultiplier`
- 多个配件的乘数可以先相乘成一个 `M`，再挂一个 amount = `M - 1` 的修饰符；结果与逐项挂载完全等价（乘法可交换），实现上更省修饰符。

**C. 两者都要：不要复用同一个字段。** legacy 键按 legacy 语义解析，在内部统一成乘数表示时做 `m = 1 + value`，对外挂载时再 `amount = m - 1`。

---

## 五、顺带发现（建议单独确认）

- [data-structure.md:21](docs/architecture/core/entity/shooter/modifier/data-structure.md:21) 写的是“`uniqueMultiplier`：唯一乘数，取最后安装配件的值（不累加）”，但代码 [AttachmentModifier.java:18](core/src/main/java/dev/xcolorful/customgun/core/item/attachment/modifier/AttachmentModifier.java:18) 是 `uniqueMultiplier *= modifier.getUniqueMultiplier()`，即**全部相乘**。TaCZ 原版也是全部相乘（`d7 *= max(0, multiplier)`），所以是文档与代码不一致，代码行为恰好就是你要的“独立倍率相乘”。另外 CGC 少了 TaCZ 对 percent/multiplier 的 ≥ 0 钳制（负乘数会翻转符号），是否需要保留取决于你的设计。
- `AttributeModifierOperation` 里 `SHARED_BASE_ADD / SHARED_PERCENT_ADD / UNIQUE_MULTIPLIER` 是**属性层**的 operation 命名，amount 全是增量；`__ModifierData` 里同名的三个字段是**数据层**的累加/乘数，语义不同。两者的桥接规则就是上表里的 `uniqueMultiplier - 1`，这点值得在文档里写清楚，否则很容易再次踩到“0 是不是清零”的歧义。

# 

```
那么把文档和代码（如AttachentModifier）里对unique multiplier操作错误的都统一成AttributeModifierOperation javadoc（vanilla语义）
需要说明已有功能里哪些受影响
```

# 

已按“原版（vanilla）增量语义、不做向后兼容”完成统一，`.\gradlew.bat :core:compileJava --offline` 编译通过（BUILD SUCCESSFUL in 55s），仓库里没有自动化测试，所以只做了编译与 IntelliJ 检查。

## 一、改了什么

语义统一为：

```
value = (base + Σ sharedBaseAdd) × (1 + Σ sharedPercentAdd) × Π (1 + uniqueMultiplier)
```

三个数值字段全部是**增量**，默认值全部为 `0`。

| 文件 | 改动 |
|---|---|
| [AttributeModifierOperation.java](core/src/main/java/dev/xcolorful/customgun/core/api/minecraft/attribute/AttributeModifierOperation.java) | 补 enum javadoc：三个操作都是增量、默认 0；`UNIQUE_MULTIPLIER` 每个修饰符独立 `× (1 + amount)` |
| [__ModifierData.java](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/attachment/__ModifierData.java) | `uniqueMultiplier` 默认值 `1` → `0`，补字段语义 javadoc |
| [AttachmentModifier.java:18](core/src/main/java/dev/xcolorful/customgun/core/item/attachment/modifier/AttachmentModifier.java:18) | `uniqueMultiplier *= getUniqueMultiplier()` → `uniqueMultiplierFactor *= 1 + getUniqueMultiplier()` |
| [__ModifierDataTag.java:7](core/src/main/java/dev/xcolorful/customgun/core/api/resource/data/data/attachment/__ModifierDataTag.java:7) | 标明旧键 `multiplier` 不再保留系数兼容，也按增量读取 |
| [_ShootStateSpeed.java](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/gun/_ShootStateSpeed.java) | 补注释：三字段是 `MULTIPLY_TOTAL` 的增量，直接作为 amount |
| [LivingShooterAttribute.java:87](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAttribute.java:87) | 中性值判断已由你改为 `== 0`，注释同步为“没有增量修正” |
| [data-structure.md](docs/architecture/core/entity/shooter/modifier/data-structure.md) / [Home.md](docs/architecture/core/entity/shooter/modifier/Home.md) | 修正“取最后安装配件的值（不累加）”的错误描述，补充公式与兼容性说明 |
| [Attachment data.md](docs/wiki/resource/data/cgc_data/Attachment data.md) | 全部示例 `"unique_multiplier": 1.0` → `0.0`，顶部补增量语义说明 |

> 你在 `AttributeModifierOperation` 里保留了 `当值为 0 时，相当于 uniqueMultiplier = 1` 的常量注释。这是指**效果上**等价于乘数 1；数据字段本身现在默认是 0，两者不冲突。

## 二、已有功能里哪些受影响

所有走 `_SimpleModifierData`（`__ModifierData` 三字段）的配件属性都受影响：

| JSON 键 | Modifier | 受影响内容 |
|---|---|---|
| `ads` | AdsModifier | 瞄准速度 |
| `damage_calculation`/`damage` | DamageCalculationModifier | 每个距离段伤害 |
| `headshot_multiplier`/`head_shot` | HeadshotMultiplierModifier | 爆头倍率 |
| `armor_ignore_percent`/`armor_ignore` | ArmorIgnoreModifier | 穿甲比例 |
| `bullet_speed`/`ammo_speed` | BulletSpeedModifier | 弹速 |
| `pierce_count`/`pierce` | PierceCountModifier | 穿透数 |
| `knockback_strength`/`knockback` | KnockbackStrengthModifier | 击退 |
| `bullet_explosion` → `explode_damage`、`explode_scale`、`max_delay_seconds` | BulletExplosionModifier | 爆炸伤害 / 半径 / 延迟 |
| `rpm` | RpmModifier | 射速 |
| `recoil_data`/`recoil` → `pitch_recoil`、`yaw_recoil` | RecoilDataModifier | 后坐力缩放 |
| `effective_range` | EffectiveRangeModifier | 有效射程 |
| `weight`/`weight_modifier` | WeightModifier | 配件重量 → `WEIGHT_SPEED` 属性 → 移速 |
| `aim_inaccuracy` | AimInaccuracyModifier | 瞄准散布 |
| `sneak_inaccuracy` | SneakInaccuracyModifier | 潜行散布 |
| `prone_inaccuracy`/`lie_inaccuracy` | ProneInaccuracyModifier | 趴下散布 |
| `other_inaccuracy`/`inaccuracy` | OtherInaccuracyModifier | 其他散布 |

**不受影响**：`fire_aspect`、`muzzle`、`melee`、`magazine_category`（不使用 `_SimpleModifierData`）；枪械基础数值（`GunData` 里的 rpm、aim_time、recoil 等）；属性层运行时行为（只是 javadoc 变化）；`_ShootStateSpeed`（本来就是增量语义，默认 0）。

## 三、已有数据的行为变化（重点）

旧公式是 `Π multiplier`，新公式是 `Π (1 + multiplier)`。因为不做兼容换算，**所有已经按系数语义写的 `multiplier` 数据都会变**：

| 数据值 | 旧结果 | 新结果 |
|---|---|---|
| `0.5` | ×0.5 | ×1.5 |
| `0.8` | ×0.8 | ×1.8 |
| `1.0` | ×1 | ×2 |
| `1.5` | ×1.5 | ×2.5 |
| `0` | ×0（TACZ 会往下钳到 0） | ×1（无效果） |
| 省略该键 | ×1（默认 1） | ×1（默认 0） |

实测 `E:\Software\Minecraft\1.20.1testtacz\tacz\tacz_default_gun` 下的配件 JSON：

- 共 **209 处** `"multiplier"`，其中 **157 处小于 1**（原本是减益/缩小，例如后坐力 0.8、弹速 0.9）。
- 统一到增量语义后，这 157 处会从“缩小”变成“放大”（0.8 → 1.8），另外 `0` 会从“清零”变成“无效果”，`1` 会从“无效果”变成翻倍。
- 第三方 TaCZ 数据包同理；仓库内没有 CGC 原生 `unique_multiplier` 数据，所以 CGC 自带数据不受这次解析影响。

如果之后要恢复这些数据原本的意图，换算规则是 `新值 = 旧系数 − 1`（旧 `multiplier: 0.8` → 新 `unique_multiplier: -0.2`）。

其他注意点：

- **没有 ≥0 钳制**：TaCZ 原版对每个 `multiplier` 做 `max(0, m)`，新语义按原版属性行为不钳制。旧数据没有负数；若出现 `< -1` 的值，乘数会变成负数。
- **`scriptFunction` 的输入变了**：Lua 脚本拿到的 `x` 现在是 `Π (1 + u)` 之后的值，按旧系数公式写的脚本需要复核。
- **外部 API**：任何外部代码用 `setUniqueMultiplier(1f)` 表示“中性”的地方，现在会变成 ×2，应改为 `0`；仓库内没有这类调用。

改动均未提交，按项目约定留给你 review。

# 

```
你文档对旧数据的说明有问题，这个调整算fix，并且不用在文档里说明这次fix，直接留目前的语义说明即可
对tacz旧multiplier字段的说明也有问题，这个算CGC实现方面没对其Vanilla的bug，不算对旧输入的兼容，我删了，英文相关内容对齐中文
```

# 

已按你的要求清理：

- [data-structure.md](docs/architecture/core/entity/shooter/modifier/data-structure.md)：删掉了英文里的 **Compatibility** 段，英文条目去掉了 `(equivalent to …)` 标注，现在与中文逐条对齐，只保留当前增量语义和公式。
- [Home.md](docs/architecture/core/entity/shooter/modifier/Home.md)：中英文的重命名表恢复为原描述，不再提旧键语义变化。
- [Attachment data.md](docs/wiki/resource/data/cgc_data/Attachment data.md)：顶部说明已由你删除，中英文均无残留；示例值保持 `unique_multiplier: 0.0`。
- `__ModifierDataTag`、`_ShootStateSpeed` 保持你修改后的状态，不再附加旧输入兼容说明。

现在文档只陈述“三个字段是增量、默认 0、唯一乘算按 `Π(1 + value)` 计算”这一当前语义，不再把本次修正描述成兼容性变更。本轮没有改动 Java 代码，也不需要重新编译；改动仍未提交。

# 

```
现在重新检查代码和文档是否有改漏的
```

# 

重新扫了一遍代码和文档，结果如下。

## 发现并修复的遗漏

[LivingShooterAttribute.java:87](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAttribute.java:87) 的中性值判断又变回了 `shootStateSpeed == 1`（注释还是“没有增量修正”），已改回 `== 0`。这行在死代码里（`speed = null` 会先返回），但语义必须和增量模型一致：0 才是“无效果”。

## 已核对、无需再改的部分

**代码**

- `uniqueMultiplier` 的消费方只有 [AttachmentModifier.java:22](core/src/main/java/dev/xcolorful/customgun/core/item/attachment/modifier/AttachmentModifier.java:22) 一处，现在是 `uniqueMultiplier *= 1 + getUniqueMultiplier()`，初值 1，公式与 [AttributeModifierOperation](core/src/main/java/dev/xcolorful/customgun/core/api/minecraft/attribute/AttributeModifierOperation.java) 的 javadoc 一致。
- [__ModifierData.java:18](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/attachment/__ModifierData.java:18) 默认 `0`，javadoc 指向 `AttributeModifierOperation`。
- [_SimpleModifierData.java:25](core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/attachment/_SimpleModifierData.java:25) 新旧键都按增量原样读取，`toJson` 写新键；没有额外的换算或钳制。
- `getSharedBaseAdd` / `getSharedPercentAdd` / `getUniqueMultiplier` 在 `AttachmentModifier` 之外没有调用方；不存在第二套计算或 UI 展示逻辑。
- 所有 16 个 `_SimpleModifierData` 类型（ads、damage、headshot、armor ignore、bullet speed、pierce、knockback、explosion 三个子项、rpm、recoil 两个子项、effective range、weight、四种 inaccuracy）都走同一个 `evalSimpleModifierData`，没有漏网的分支。
- `fire_aspect`、`muzzle`、`melee`、`magazine_category` 不使用该结构，不受影响。
- 仓库内没有任何配件 JSON 数据（`"multiplier"` 只出现在文档和 tag 常量里），所以没有需要迁移的随包数据。
- `:core:compileJava` 重新编译通过（exit 0）。

**文档**

- `data-structure.md` 中英文都只保留增量语义和公式，没有“取最后”“Π multiplier”或兼容性说明。
- `Home.md` 的重命名表已恢复原描述。
- `Attachment data.md` 的示例全部是 `unique_multiplier: 0.0`，顶部说明已由你删除，中英文一致。
- `docs/api/**` 没有涉及这三个字段语义的内容。

## 没有动、但你可能想确认的两处

1. **`docs-tacz/**` 仍是 TaCZ 原版语义**：例如 `docs-tacz/architecture/resource/modifier/data-structure.md` 写的是 `Π max(multiplier, 0)`、`multiplier：乘数，按乘积累乘`。它是对 TaCZ 上游的参考快照，不是 CGC 当前行为，所以我没改。如果你希望它和 CGC 对齐，需要单独说明。
2. **`AttributeModifierOperation` 为 javadoc 引入了 `__ModifierData` / `_ShootStateSpeed` 的 import**：只影响源码/javadoc，不影响字节码，但让 API 层在源码上依赖了实现层。若在意层次，可改成 javadoc 里的全限定名或纯文本。

另外，`GunData` 仍然没有 `shoot_state_speed` / `movement_speed` 的解析分支，整条移速修正链路仍是停用状态——这是当前分支原有的状态，不属于本次语义修正。改动都没提交。

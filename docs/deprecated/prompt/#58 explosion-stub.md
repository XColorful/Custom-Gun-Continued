> #58 提示词

# 

```
- 现在需要临时移植子弹爆炸效果，使dev.xcolorful.customgun.core.projectile.impact.ProjectileImpactManager调用dev.xcolorful.customgun.core.projectile.impact._TempExplode，爆炸相关代码只写在这一个类里，用内部类，不需要post forge事件，非explode时提前返回
- 原模组逻辑在com.tacz.guns.entity.EntityKineticBullet和com.tacz.guns.util.ExplodeUtil
- TaCZ类到CGC的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

目前的代码，游戏目录下.\tacz\包里tacz:rpg7近距离射击也没造成子弹伤害
```

# 

I'll start by reading the current state of the changed files and the reference sources.

Now let me read the TaCZ originals and the migration mapping.

Now let me read the TaCZ bullet's explosion logic.

Let me read the full bullet tick logic in TaCZ.

Now let me explore the CGC projectile architecture to understand available APIs.

Let me check the vanilla `Explosion` API for 1.20.1 via IDEA MCP.

Let me check what the bullet-ignore block tag contains, and how the registry helper is used.

I have the full picture now. Let me write the ported explosion class.

Now let me wire up `ProjectileImpactManager`.

Now the second cause: pierce is never clamped to ≥1, unlike TaCZ (RPG7 and cz75 both ship `"pierce": 0`).

Let me check the edited files for problems via IDEA.

While it compiles, let me review the final state of the impact manager.

Compile finished, but I made edits after it started — let me recompile the final state.

Let me verify the damage data path for a scalar `damage` value like RPG7's.

Let me check how TaCZ's BulletData distinguishes display damage from real damage.

Let me check the history of the pierce check and the damage-list code to understand intent.

Let me assess how many packs would be affected by the missing damage fallback.

Only RPG7 and one unloaded pack file lack a damage list, so the blast radius is tiny. Applying the TaCZ-faithful fallback:

Let me try to empirically verify the data-level diagnosis with a throwaway JVM run.

Empirically confirmed the diagnosis. Let me also check that existing guns are unaffected.

已按 #58 完成移植，并通过编译与数据层实测验证。以下是改动与需要你确认的判断点。

## 1. 爆炸移植（[`_TempExplode.java`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java)）

全部爆炸代码在这一个类里，未发布任何 Forge 事件：

- [`isExplode`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:65) / [`preExplode`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:75) / [`explode`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:96)：非爆炸弹药一律提前 `return`（`getExplosionData` 未启用即 null）
- [`ProjectileExplosion`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:158)：内部类复刻原模组 `util.block.ProjectileExplosion`，含 15 角点视线判定、`getTracedHitbox` 延迟补偿、击退与 `EXPLOSIVE_AMMO_*` 配置门控
- 省略 `onExplosionStart`/`onExplosionDetonate`；保留 `ClientboundExplodePacket`（客户端爆炸音效与击退靠它，原模组也发）
- 引信判定等价改写：原模组的逐 tick 递减计数器换成 `gunProjectile.tickCount`（枪射物不存档，两者每 tick 同步变动 1，已注释说明）

[`ProjectileImpactManager`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java) 四处调用：`preImpactTick`([:77](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:77))、实体命中([:158](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:158))、方块命中([:177](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:177))，并把原来重复的 `onNonBulletVictimHit(BlockHitResult)` 改为与实体版同款地委托 `cgc$onProjectileImpact`（原文有"楼上等楼下完成后删除"的注释）。**顺序改为原模组的"先出伤、再爆炸"**——原来的脚手架是先爆炸，会把 `_ProjectileHit` 整个跳过。

## 2. 顺带修掉的两个 bug（否则 RPG7 拿不到任何伤害）

用真实解析器跑 `rpg7_data.json` 实测确认：`pierce=0`、`damageCalculation` size=0。

- [`:48`](core/src/main/java/dev/xcolorful/customgun/core/entity/projectile/_GunProjectileConstructor.java:48) 穿透数补上 `Math.max(..., 1)`（原模组 `Mth.clamp(pierce, 1, MAX)`）。`pierce=0` 会被 `impactTick` 在第一个 tick 直接 `discard()` —— 火箭原地消失，既无子弹伤害也无爆炸。（cz75 同样受影响）
- [`IDamageCalculationModifier.getBase`](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/modifier/IDamageCalculationModifier.java:31) 补上"没有距离伤害表时回退到子弹原始伤害"，对应原模组 `DamageModifier#initCache` 的 `rawDamage` 分支。RPG7 旧格式只写了 `damage: 20`（无 `damage_adjust`），本条不改则子弹伤害恒为 0。已确认 ak47/cz75 等有伤害表的枪不受影响。

这两处严格说在爆炸任务范围之外，我按"TaCZ 是规格 + 你报的症状"判断必须一起改。**替代位置**：第 2 条也可以放在 `_BulletData.applyBackCompatibility`（数据层转换，覆盖面更广），我选了与原模组同位、更保守的写法，需要的话可以搬。

## 3. 验证状态

- `:core:compileJava` 与 `:forge-compat:compileJava` 均 BUILD SUCCESSFUL（只剩仓库原有 deprecation 警告）
- 数据层已实测：解析 rpg7 → displayDamage=20、pierceCount=0→1、伤害表 empty→回退 20、爆炸 enable/120/3/30/true/true
- **游戏内未验证**：我无法驱动客户端射击。请你测：RPG7 近距离射击应同时出子弹伤害(20)与爆炸伤害、方块被炸毁、有爆炸音效/粒子；另建议顺手确认 cz75 恢复出伤、普通枪械行为无变化

## 4. 需要你决定的判断点

- 爆炸前的"取消无敌时间"我放在 [`ProjectileImpactManager:160`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:160)（原模组写在子弹的 `onHitEntity` 而不是 `ExplodeUtil`），因此 `explode` 保持了你给的签名。若你希望连这行也收进 `_TempExplode`，需要给它加一个 victim 参数。
- 完全自行实现 `cgc$onProjectileImpact` 而不委托默认实现的受弹方块/实体（例如打靶方块若这么做）不会触发爆炸。

未提交，等你 review。

# 

```
onNonBulletVictimHit里先执行setDeltaMovement（63行）是否有影响？
```

# 

Let me verify what that line is actually copying from vanilla before answering.

## 直接回答：当前没有任何可观察影响，因为所有方块命中路径最后都会 `discard`

链路是 [`impactTick:140`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:140) 把实体的 deltaMovement 抄回 `tickContext`，然后 [`ProjectileProcessManager:29`](core/src/main/java/dev/xcolorful/customgun/core/projectile/process/ProjectileProcessManager.java:29) 检查 `isRemoved()` 就 return 了，`physicMove` 根本不会跑。而方块命中最后一定 discard（非爆炸走 [`:175`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:175)，爆炸在 `_TempExplode.explode` 内部 discard）。所以 63 行现在写的是一个立刻被丢弃的值。

顺带两点小结论：爆炸用的是 `blockHitResult.getLocation()`，射线检测在 `physicTick` 已经算完，都不受它影响；对非受弹方块它是执行两次（63 行 + 170 行），两次中间位置没变所以向量相同，只是冗余、不是错误。

## 它唯一会生效的场景

有路径不 discard 时（比如某个受弹方块自己实现 `cgc$onProjectileImpact` 且保留子弹），140 行会把这个"吸附向量"喂给 `physicMove`：子弹瞬移到命中点、再按 ≈0 的速度吃摩擦和重力 → 表现成"钉在方块上然后掉落"。注意这只是半个 `AbstractArrow#onHitBlock`（原版还配套 `setPosRaw` 推回、`inGround`、音效），我们现在只抄了 `setDeltaMovement` 那一句。

## 更值得关注的是顺序

63 行在 `iBulletVictimBlock.cgc$onProjectileImpact(...)` **之前**跑，即在受弹方块自己的逻辑之前就把子弹速度清成了 `hitLoc − pos`。将来若做跳弹/反弹/继续穿透之类的受弹方块，它去读 `getDeltaMovement()` 拿到的就不是入射速度了。这也是我建议只留一处的理由：

- 只想服务"默认逻辑"→ 只保留 [`:170`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:170)，删掉 63 行（与"扩展模组委托默认实现"的语义一致）
- 想让"自己处理命中的受弹方块"也拿到这个位置修正 → 保留 63 行，但把它挪到方块实现之后更稳妥
- 严格对齐 TaCZ → 两处都可以不要：TaCZ 命中方块后直接 discard，`super.onHitBlock` 只是 `BlockState#onProjectileHit`（顺便说，这也正是 TaCZ 触发打靶方块逻辑的地方），并不改速度

## 另外：我上一轮留的注释写错了

`:170` 那句"原版 Projectile#onHitBlock 的位置修正"不准确——`Projectile#onHitBlock` 只调用 `blockstate.onProjectileHit(...)`，`setDeltaMovement(hitLoc - pos)` 实际来自 `AbstractArrow#onHitBlock`。你正在手改这个文件，我就没动它，要改的话我改成 AbstractArrow 的说法。

# 

```
那现在167行这也应该不需要手动setDeltaMovement？是否命中方块的时候射线检测会处理终点，physicMove的时候移动到正确位置？
```

# 

## 射线检测确实修正了终点，但 physicMove 不读那个终点

`ProjectilePhysicsManager` 里确实做了终点修正：

```java
tickContext.endPos = tickContext.startPos.add(tickContext.deltaMovement);   // :72
...
if (blockHitResult.getType() != HitResult.Type.MISS) {
    tickContext.endPos = blockHitResult.getLocation();                      // :102 命中点
}
tickContext.blockHitResult = blockHitResult;
```

但 `endPos` 目前只喂给了射线检测（[`:112`](core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:112) 的实体检测）和命中信息。`physicMove` 第一行是：

```java
Vec3 thisTickMovement = tickContext.deltaMovement;   // ← 只认这个，与 endPos 无关
...
_this.setPos(_this.getX() + x, _this.getY() + y, _this.getZ() + z);
```

而 `impactTick` 末尾又把实体的 deltaMovement 抄回这个字段（[`:140`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:140)）。所以"手动 setDeltaMovement"恰恰是当前**唯一**能让子弹停/落在命中点上的机制——你的前提（physicMove 会自己移动到正确位置）不成立。

## 结论

**167 行可以删，但理由不是"physicMove 会定位"，而是"默认路径一定 discard"**：爆炸路径在 `_TempExplode.explode` 内 discard，非爆炸走 [`:172`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:172) 的 discard，于是 [`ProjectileProcessManager:29`](core/src/main/java/dev/xcolorful/customgun/core/projectile/process/ProjectileProcessManager.java:29) 直接 return，`physicMove` 根本不跑。

删掉之后有一个潜在后果：一旦出现"命中方块但不 discard"的路径（受弹方块自己返回 true 而保留子弹，比如打靶方块选择不摧毁子弹），`physicMove` 会按整段飞行速度把子弹推过墙面一格（穿墙/埋进方块）。这也是 63 行/167 行的速度写入唯一还有意义的地方——反过来说，如果打靶方块本意是让子弹贴靶面，这个写入正好提供该效果，得看你想不想要。

另外删掉 167 行后，`impactTick:140` 就退化成"读回原值"的冗余行（它原本是给 impact 阶段改速度用的入口，配合"爆炸的boolean复用discard"那套设计），留着无害。

## 更彻底的做法（需要你点头）

如果希望"终点修正"真正生效，可以让 `physicMove` 用 `tickContext.endPos.subtract(tickContext.startPos)` 作为本 tick 位移——那样终点修正天然服务于移动，两处 `setDeltaMovement` 都能删，`impactTick:140` 也不必再中介速度。但这是物理层的契约变更（`endPos` 从"射线终点"变成"本 tick 实际位移"），超出本次爆炸移植范围，要不要动由你决定。

# 

```
那63行和169的set都保留，互相不会覆盖？
```

# 

Let me re-check the current dispatch and both call sites before answering.

## 不会覆盖出问题：两次算的是同一个向量

两个表达式完全相同——`blockHitResult.getLocation().subtract(gunProjectile.position())`，而 `Entity.setDeltaMovement` 就是纯赋值（[`Entity:3200`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:169) 那边对应的原版实现是 `this.deltaMovement = p;`，没有 flag、不发包）。两次调用之间也没有任何东西会改变位置：

```
63 行 write  →  65 行委托  →  169 行 write  →  explode（挪不动子弹）→  discard（不动 position）
```

所以第二次是等值重写，等价于一次。

## 但"执行几次"取决于分支（同时更正我上一轮的说法）

[`impactTick`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:126) 是 `iBulletVictimBlock != null ? onBulletVictimHit : onNonBulletVictimHit`：

| 命中对象 | 经过 63 行？ | 经过 169 行？ |
|---|---|---|
| 一般方块 | 是 | 是（65 行委托）→ 共 2 次，冗余 |
| 受弹方块，实现里委托默认 | **否**（走 [:56](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:56)） | 是 → 共 1 次 |
| 受弹方块，自己处理不委托 | 否 | 否 → 0 次 |

**更正**：我上一轮说"63 行会在受弹方块自己的逻辑之前把速度清掉，跳弹类实现会读到错误速度"——这是错的。63 行只存在于非受弹方块那条分支，受弹方块根本不经过它。而且受弹方块的实现里读 `getDeltaMovement()` 拿到的是本 tick 的入射速度（physicMove 是上一 tick 末尾跑的，本 tick 的 physicTick 不改速度），所以那个顾虑不成立。

## 建议

留 169、删 63。因为 169 在"默认实现"里，任何扩展模组或受弹方块调用 `ProjectileImpactManager.cgc$onProjectileImpact(blockHitResult, ...)` 时都应该拿到完整行为；63 只是同一条路径的重复前置。删掉后非受弹方块路径（65 行委托）的行为完全不变。

# 
---

```
PR #58移植到1.20.4，现在处理_TempExplode移植，新增的版本差异不需要新增utils封装，直接在这个类内处理掉
```

# 

I'll start by finding the PR #58 content and the `_TempExplode` class.

_TempExplode already exists in this worktree (merged from core1.20.1). Let me check whether it actually compiles against 1.20.4's API.

Now I have the version-difference errors. Let me inspect the 1.20.4 vanilla APIs.

Root cause is clear: PR #58's `_TempExplode` was written against 1.20.1's API, and 1.20.4 changed `Explosion`, `ClientboundExplodePacket`, `ClipContext`, and `Entity#ignoreExplosion`. Let me fix them inline.

Now the packet construction, `Explosion` super call, `ignoreExplosion`, damage source, and `ClipContext`.

Both files are clean. Let me verify with a real compile.

Compiles cleanly. Let me confirm there are no other modules affected.

`_TempExplode` 在 1.20.4 编译不过的 5 处，全部按你的要求就地改在本类里，没有新增 utils：

| 报错点 | 1.20.1 写法 | 1.20.4 改法 |
|---|---|---|
| [`:147`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:147) `ClientboundExplodePacket` | 6 参构造 | 1.20.2+ 多了 `BlockInteraction` + 大小粒子 + 音效，直接用 `explosion` 的 getter 补齐（与 `ServerLevel` 原版发包一致） |
| [`:177`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:177) `Explosion` 父构造 | 10 参 | 1.20.4 变 13 参，补 `ParticleTypes.EXPLOSION` / `EXPLOSION_EMITTER` / `SoundEvents.GENERIC_EXPLODE`（即原版无参构造的默认值，视觉不变） |
| [`:194`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:194) 伤害源 | `this.getDamageSource()` | 1.20.4 删掉了该 getter（字段转 private）。在构造里存一份 `Explosion.getDefaultDamageSource(level, exploder)` —— 与原版 `damageSources().explosion(this)` 求值完全相同（`exploder` 是 `Projectile`，indirect source 取 owner） |
| [`:259`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:259) `ignoreExplosion()` | 无参 | 1.20.4 需要把 `Explosion` 传进去，传 `this` |
| [`:347`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:347) `ClipContext` | 第 5 参传 `null` | 1.20.4 新增了 `(…, CollisionContext)` 重载，`null` 变成歧义。改传 `CollisionContext.empty()`，等价于 1.20.1 的 `CollisionContext.of(null)` |

`:core:compileJava`、`:forge-compat:compileJava`、`:neoforge-compat:compileJava` 均 BUILD SUCCESSFUL（只剩仓库原有 deprecation 警告）。

未提交，等你 review。游戏内仍未验证——RPG7 近距射击应同时出子弹伤害与爆炸伤害、方块被毁、有爆炸音效/粒子；另外这次动了发包内容和伤害源构造，建议顺手确认爆炸击退与玩家被炸的击退反馈正常。

# 

```
现在继续处理1.21.1forge的移植
```

# 

Now the same port for 1.21.1 Forge. Let me see what breaks there.

The 1.21.1forge copy has only 2 errors left, and the file's mtime is recent. Let me check its state before touching anything.

A commit `ba2f3586b Port #58 to 1.20.4` is already in this branch's history. Let me understand what's already here before editing.

Only `ProtectionEnchantment` remains — removed in 1.21's enchantment overhaul. Let me find where the method moved.

1.21.1 replaced blast protection's knockback dampener with the `EXPLOSION_KNOCKBACK_RESISTANCE` attribute. Let me confirm no helper method remains, then read the call site.

IDEA's analysis for `ProjectileImpactManager` looks like a stale index (it can't even see a same-package class). Let me get the authoritative answer from the compiler.

1.21.1forge 的 `_TempExplode` 改动已完成，但编译验证被你中断了，所以**尚未验证**。

## 已做的改动（[`_TempExplode.java`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:323)）

这个 worktree 里 1.20.4 的那批适配已经在了（commit `ba2f3586b "Port #58 to 1.20.4"`，只存在于 1.21.1forge 分支上），只剩 1 处 1.21.1 差异：

| 报错点 | 1.20.x 写法 | 1.21.1 改法 |
|---|---|---|
| `ProtectionEnchantment` 整个类消失 | `getExplosionKnockbackAfterDampener(livingEntity, damage)` | `damage *= 1.0 - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE)` |

依据是 1.21.1 原版 `Explosion.explode()` 自己就是这么算的（`:282`）。两者数值等价：1.20.x 是 `damage * (1 - 0.15*爆炸保护等级)`，1.21 里爆炸保护改为提供 `+0.15/级` 的该属性，原版再乘 `(1 - 属性值)`。已用 `search_symbol` 确认全库不存在 `getExplosionKnockbackAfterDampener`。

IDEA 对 `_TempExplode.java` 报 0 error。

## 需要你决定的两件事

**1. 编译没有跑完。** 被 kill（exit 137）。要我现在重跑 `:core:compileJava :forge-compat:compileJava` 吗？

**2. 一个我没解释清楚的疑点，需要你这个知道上下文的人判断。** IDE 对 [`ProjectileImpactManager.java`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:75) 报了一堆错，包括「Cannot resolve symbol `_TempExplode`」和「Cannot resolve symbol `_ProjectileHit`」——这两个类就在同一个包里、文件确实存在。同包类解析不了 + `@Override` 全部失效 + `IGunProjectile` 的方法全找不到，这个形态像是 IDEA 索引过期（这个 worktree 刚被 merge 过），而不是真实编译错误。但我没有跑通编译来证实，所以**这不代表代码是好的**。

如果你中断编译是因为它太重/在抢资源，或者你本来就想先手动看看这个 worktree 的历史（那个 `Port #58 to 1.20.4` 提交出现在 1.21.1forge 上、却不在 1.20.4 worktree 上，我按"不覆盖别人工作"处理了，没有动它），告诉我，我按你的意思来。

未提交，等你 review。

# 

```
继续1.21.4
```

# 

Continuing with 1.21.4neoforge. Let me check its state first.

1.21.4 is past the 1.21.2 explosion refactor — `Explosion` is now an interface there. Let me read the real API and check for existing project usage.

Found the decompiled 1.21.4 sources. Let me read the new `Explosion` API.

Now let me extract the 1.21.4 sources to trace who sends the explode packet and what the interface methods are used for.

Key finding: in 1.21.4 `Explosion` is an interface and `ClientboundExplodePacket` is a 4-field record (no block list). Let me read how `ServerLevel.explode` drives it.

Now let me confirm the remaining 1.21.4 API details I'd need.

Now let me read the current state of the 1.21.4 file so I can plan the restructuring precisely.

Now let me verify the exact 1.21.4 signatures I need for the rewrite.

I have the full picture now. Let me read the exact regions I'll edit.

Now I'll make the edits. Starting with imports.

IDEA reports clean. Now the authoritative check — compile.

The failure is environmental, not a compile error — Gradle can't find a JDK 21 toolchain and can't download one. Let me check what's installed.

No JDK 21 is installed (only 8, 17, 25) and the download is blocked — there are interrupted `.zip.part` files, which is likely what the killed compile was stuck on. Let me see if there's a prior compiled output I can check against.

`IDEA has a JDK named "Java21" configured. Let me find its path so I can point Gradle at it.

Found it — IDEA's `Java21` is at `E:/Software/Minecraft/JDK/jdk-21.0.8`. Let me point Gradle at it (per-invocation flag, no repo changes).

`:core:compileJava` BUILD SUCCESSFUL with the JDK 21 path. Now the remaining modules — including finishing the 1.21.1forge verification that was previously interrupted.

1.21.4 passes both modules. The 1.21.1forge failure needs its reason — let me look.

1.21.4 is fully verified. The 1.21.1forge failure is again the toolchain (Gradle 8.12.1 doesn't seem to pick up the flag). Let me check its toolchain detection and the state of my earlier edit there.

Two things to check: whether my earlier 1.21.1forge edit survived, and whether a fresh daemon picks up the toolchain path.

My earlier 1.21.1forge edit survived (committed as `49f6cfe4c Port #58 to 1.21.1`). Let me try passing the toolchain path via the environment instead.

1.21.4 is done and compiles. Here's the summary.

## 1.21.4neoforge — `_TempExplode` 已移植并验证编译

`:core:compileJava` 与 `:neoforge-compat:compileJava` 均 **BUILD SUCCESSFUL**（只剩仓库原有 deprecation 警告）。改动 85+/21-，全在这一个类里。

这个版本不是小修，是结构性差异——1.21.2 起原版把爆炸重写了：

| | 1.20.4 / 1.21.1 | 1.21.4 |
|---|---|---|
| `Explosion` | class（可继承、可只覆写 `explode()`） | **接口**，只有 `ServerExplosion` 一份实现，且 `calculateExplodedPositions` / `hurtEntities` / `interactWithBlocks` / `createFire` 全是 private |
| 方块破坏/掉落 | `finalizeExplosion(true)` 包办 | 没有 `finalizeExplosion` 了，`explode()` 一次做完；掉落合并逻辑变 private |
| 爆炸包 | 11 参构造（含 `toBlow` 方块列表） | **4 字段 record**：`(Vec3 center, Optional<Vec3> knockback, ParticleOptions particle, Holder<SoundEvent> sound)`，方块列表不再下发 |
| 实体伤害 | `entity.hurt(DamageSource, float)` | `hurt` 被删，改 `hurtServer(ServerLevel, DamageSource, float)` |

对应做法：`ProjectileExplosion` 从 `extends Explosion` 改为 **`extends ServerExplosion`**，整体覆写 `explode()`。这样 `level()`/`getHitPlayers()`/`getDamageSource()`/`isSmall()`/`canTriggerBlocks()`/`shouldAffectBlocklikeEntities()` 这些接口实现直接从父类拿到（尤其后两个带 GameRules 逻辑，手写容易错），我只需把原版的方块破坏/掉落合并/火焰三段搬进类内（`interactWithBlocks` + `addOrAppendStack` + `StackCollector` + `createFire`，与原版算法逐行一致）。粒子改成服务端按 `isSmall()` 选一个下发（原版 `ServerLevel` 同款），`clearToBlow()`/`getToBlow()` 随接口消失而删除。

## 两处判断点，需要你确认

1. **我没有加 `entity.onExplosionHit(this.source)`，也保留 `setDeltaMovement(add)` 而非原版 1.21.4 的 `push`。** 理由：保持与 1.20.4/1.21.1 移植行为一致（TaCZ 语义）。副作用：被枪械炸飞的玩家会吃普通摔落伤害，而原版 1.21.4 的爆炸会把这段摔落伤害归因到冲量上（`ServerPlayer.onExplosionHit` 新增机制）。要跟原版对齐的话我加上，只差一行。
2. **继承 `ServerExplosion` 会白拿几个 1.21.4 原版行为**，与 1.20.x 版本会不同：物品实体/盔甲架是否被炸毁现在看 `shouldAffectBlocklikeEntities()`（受 `mobGriefing` 门控），按钮/门/拉杆不会被触发（`canTriggerBlocks()` 恒 false 因为我们的模式只有 KEEP/DESTROY），掉落物按玩家归属走 `getIndirectSourceEntity()`。这些我认为是"对"的（原版行为），但确实是跨版本差异，先告诉你。

游戏内仍未验证——RPG7 近距射击应同时出子弹伤害与爆炸伤害、方块被毁且掉落实体数量正常（合并成 16 一组）、有爆炸音效/粒子；另外建议顺手确认 KEEP 模式（关掉配置里的方块破坏）下不掉落任何物品。

## 1.21.1forge — 我上一轮那处改动已经进仓库了

工作树是干净的：我上一轮改的 `ProtectionEnchantment → Attributes.EXPLOSION_KNOCKBACK_RESISTANCE` 已被提交为 `49f6cfe4c Port #58 to 1.21.1`，内容未变。

**但 1.21.1forge 的 Gradle 编译在这台机器上跑不起来，和代码无关**：

- 这台机器只装了 JDK 8/17/25，没有 21；1.21.1forge 和 1.21.4neoforge 都要求 `JavaLanguageVersion.of(21)`
- JDK 21 实际存在，在 `E:/Software/Minecraft/JDK/jdk-21.0.8`（IDEA 的 `Java21` 就指向它），但不在 Gradle 的自动探测路径里
- 让它下载也失败：`api.foojay.io` → GitHub 的 HEAD 请求不通（`.gradle/jdks/` 里留着下了一半的 `.zip.part`，我猜这就是你上次中断的那次编译卡住的原因）
- 我加的 `-Dorg.gradle.java.installations.paths=...` **对 1.21.4（Gradle 9.2.1）有效，对 1.21.1forge（Gradle 8.12.1）无效**——`javaToolchains` 里根本没列出那个路径，`--no-daemon` 和 `GRADLE_OPTS` 都一样。所以 1.21.4 的编译结果是真的，1.21.1forge 的编译**我没能跑到**

编 1.21.4 用的命令（你可复现）：

```bash
cd D:/Github/Custom-Gun-Continued-1.21.4neoforge && ./gradlew :core:compileJava :neoforge-compat:compileJava -Dorg.gradle.java.installations.paths="E:/Software/Minecraft/JDK/jdk-21.0.8" -Dorg.gradle.java.installations.auto-download=false
```

要解开 1.21.1forge（以及后面的版本），得动仓库外的东西，所以先问你：

- **A**：把 `org.gradle.java.installations.paths=E:/Software/Minecraft/JDK/jdk-21.0.8` 写进全局 `C:/Users/Administrator/.gradle/gradle.properties`（目前不存在该文件）——一次解决所有版本的 CLI 编译
- **B**：把 JDK 21 复制/软链到 `C:/Program Files/Java/`，让 Gradle 自动探测到
- **C**：不用管，1.21.1forge 就在 IDEA 里编（IDEA 已有 Java21，分析也是 0 error）

未提交，等你 review。

# 

```
改成跟MC原版对齐，如果是MC对爆炸机制新增的内容，就用MC原版的
```

# 

收到——按原版对齐。这两个都是 MC 在 1.21.2 新增到爆炸流程里的东西，所以用原版的。先确认 `push` 的签名。

`Entity.push(Vec3)` exists, and I confirmed 1.20.4's vanilla `Explosion` itself uses `setDeltaMovement(...add(...))` — so 1.20.4/1.21.1 are already aligned and only 1.21.4 needs these two.

Confirmed: with our plain default calculator those two hooks are constant (`true` / `1.0F`), so wiring them would be dead code. Now let me re-verify.

Let me check whether 1.21.1's vanilla has the same additions, since the user's principle applies there too.

1.21.1's vanilla **does** call `entity.onExplosionHit(this.source)` (line 299) but uses `setDeltaMovement` (line 291). So 1.21.1 needs one of the two. Let me confirm the API exists there.

1.21.1 needs that one line too (its vanilla calls it). Let me confirm 1.20.4 has no such call and read the 1.21.1 call site.

1.20.4 has no `Entity.onExplosionHit` at all — so it needs nothing. 1.21.1 does. Adding the line there.

The `-Dorg.gradle.java.home` trick works — 1.21.1forge now compiles too. Let me collect the final state of both changes.

Both changes are in and verified. Let me record the environment + API findings in memory for the remaining version ports.

There's already a `local-jdk21-path.md` memory — let me read it and the current index before writing anything.

已按"跟原版对齐、MC 新增的就用原版的"改完，三个分支都重新验证过编译。

## 1.21.4neoforge（两处，你指的那两个）

- `entity.setDeltaMovement(getDeltaMovement().add(...))` → **`entity.push(knockback)`**（1.21.4 原版 `ServerExplosion.hurtEntities` 的写法，多带 `hasImpulse`，玩家被炸飞的位移同步才正常）。顺带把击退向量抽成一个 `Vec3`，`getHitPlayers().put(player, knockback)` 复用同一个对象——原来那个表达式写了两遍。
- 击退之后追加 **`entity.onExplosionHit(this.exploder)`**（原版同一位置调用）。

## 1.21.1forge（一处）

查了 1.21.1 的原版 `Explosion.explode()`：`:299` **确实有** `entity.onExplosionHit(this.source)`，所以加了这一行；但 `:291` 原版用的仍是 `setDeltaMovement(getDeltaMovement().add(vec31))`，所以 `push` **不**改。这个分支 1.21.1 的移植本就是按原版来的，只有这一处漏了。

## 1.20.4（不用改）

1.20.4 根本没有 `Entity.onExplosionHit`（我用 `search_symbol` 查过，只有方块侧的 `BlockBehaviour.onExplosionHit`），而且原版 `Explosion` 就是用 `setDeltaMovement`——已经是原版写法。

## 有一类原版钩子我故意没接，先说明

1.21.2 起 `ExplosionDamageCalculator` 上的 `shouldDamageEntity` / `getEntityDamageAmount` / `getSeenPercent` / `getKnockbackMultiplier`。这些属于 **TaCZ 主动替换掉的伤害算法部分**（PR #58 的核心就是"伤害与半径分离 + 对生物用延迟补偿碰撞箱做 15 角点视线"），不是 MC 新增机制。而且我们传的是 plain `ExplosionDamageCalculator`，它的默认实现是常量：

```java
public boolean shouldDamageEntity(Explosion e, Entity ent) { return true; }
public float getKnockbackMultiplier(Entity ent) { return 1.0F; }
```

接上去就是 `if (true)` 和 `× 1.0F`，纯死代码。**如果你要的其实是"连伤害也算回原版"**，那是把 PR #58 的核心推翻（RPG7 那套 `explodeDamage`/`explodeScale` 分离的数值会失效），这个我不敢替你决定——说一声我再动。

## 编译验证（都过了）

| 分支 | 结果 |
|---|---|
| 1.21.4neoforge | `:core:compileJava` + `:neoforge-compat:compileJava` **BUILD SUCCESSFUL** |
| 1.21.1forge | `:core:compileJava` + `:forge-compat:compileJava` **BUILD SUCCESSFUL** |
| 1.20.4 | 无改动（工作树干净） |

**工具链问题解决了，不用动你的机器**——之前问你的 A/B 都不需要了。原因是 Gradle 8.12.1 不认 `-Dorg.gradle.java.installations.paths`，但让 Gradle 自己跑在 JDK 21 上就两边都成立：

```bash
cd D:/Github/Custom-Gun-Continued-1.21.1forge && ./gradlew :core:compileJava :forge-compat:compileJava -Dorg.gradle.java.home="E:/Software/Minecraft/JDK/jdk-21.0.8"
```

（1.21.4 用同样加 `-Dorg.gradle.java.home` 也行，不必再带 `installations.paths`。）

## 仍未验证 / 待你决定

- **游戏内没验证**：这次动的正是玩家被炸的击退与摔落伤害归因——建议实测"玩家被 RPG7 近距离炸飞"，确认位移同步正常、落地不吃额外摔落伤害；方块破坏/掉落/火焰那几个新抄的原版路径也一并看下。
- 未提交，等你 review。1.21.4 的改动 `90+/25-`，1.21.1forge 是 `+2`。
- 我把"爆炸 API 跨版本事实"和"JDK 21 工具链写法"记进 memory 了，后面的版本（1.21.6 / 26.x）移植可以直接查。

# 

```
1.21.6没问题
1.21.11有问题
1.21.10单独编译文件看有没有问题，环境下的有点慢
```

# 

收到。先确认这几个 worktree 在磁盘上的位置和状态。

Both worktrees already carry a 1.21.4-style port (same file size, clean tree), and 1.21.11 has 4 distinct errors. Let me see if 1.21.10 is identical and locate its vanilla sources.

1.21.10 and 1.21.11 files are byte-identical to my 1.21.4 version — the port was copied without adapting. Let me find the actual API deltas in each version.

Key delta found: in 1.21.11 `Util` moved to `net.minecraft.util.Util`. Let me read the packet + `ServerExplosion` in both versions.

IDEA's "no errors" for 1.21.10 is stale — the identical file can't compile there either, since 1.21.10's packet is also a 7-field record and `explode()` returns `int` in both. Let me read the real bodies.

`explode()` returns the destroyed-block count in both. Let me see how vanilla builds the packet's `blockParticles`/`blockCount`.

Now let me diff 1.21.4's `ServerExplosion`/packet against 1.21.11's to catch every delta, not just the ones IDEA flagged.

Now let me read 1.21.11's `hurtEntities` in full to place the new pieces correctly.

1.21.10 lacks the radius guard (1.21.11 added it) but both have the packet change, `int explode()`, and the projectile-redirect. Let me sanity-check the user's "1.21.6 没问题" against the actual 1.21.6 API, since IDEA gave a false clean result for 1.21.10.

1.21.6 is genuinely fine (4-field packet, `void explode()`, `net.minecraft.Util`) — the user's read was right there. Now let me locate the exact edit points in the 1.21.10/1.21.11 copies.

Making the edits now. First reading both files' current state.

1.21.11 done. Now the same for 1.21.10 — minus the `Util` move and the radius guard (neither exists in 1.21.10).

Edits applied to both. Now the real compile the user asked for — 1.21.10 first, timed.

25s incremental — no need for a hand-rolled single-file compile. Now 1.21.11 plus both compat modules.

The combined run exceeded 10 minutes and moved to the background — so the compat modules are the slow part (the 25s core compile was fine). Let me check interim progress and the IDE view while it runs.

1.21.11 core+compat: **BUILD SUCCESSFUL** (8m18s — the compat module is the slow one). 1.21.10's compat is still running; I'll get notified. Meanwhile let me review the diffs.

Both files now differ by exactly the two version deltas (Util package + radius guard). Let me extend the memory with what I learned about 1.21.6/1.21.10/1.21.11 while the last compile runs.

1.21.11 修好了，1.21.10 也做了同样改动（少两处），并已用真编译验证。

## 1.21.6：你是对的，确实没问题

我对着 1.21.6 的反编译源逐项核过：packet 仍是 4 字段 record、`ServerExplosion.explode()` 仍是 `void`、`Util` 仍在 `net.minecraft.Util` → 1.21.4 的移植原样可用。

## 1.21.11 的报错都是原版 API 变了（不是移植逻辑问题）

| 报错 | 原因 | 改法 |
|---|---|---|
| `Cannot resolve symbol 'Util'` | 1.21.11 把 `net.minecraft.Util` 搬到了 **`net.minecraft.util.Util`** | 改 import |
| packet「Expected 7 arguments but found 4」 | **1.21.10 起**发包又回到 7 字段：`(center, radius, blockCount, knockback, particle, sound, blockParticles)` | 补 3 个参数 |
| `explode()` 返回值类型冲突 | **1.21.10 起** `ServerExplosion.explode()` 由 `void` 改成 **`int`**——返回"被炸方块数"，正是发包要的 `blockCount` | 签名改 `int` + `return toBlow.size()` |
| （连带）`blockParticles` 从哪来 | 原版默认表 `Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES` 是 **private** | 类内照抄一份（`POOF 0.5/1.0` + `SMOKE 1.0/1.0`，与原版逐字一致） |

**1.21.10 与 1.21.11 的差别只有两处**：`Util` 的包没搬、没有半径守卫。两文件现在 diff 出来正好只有这一点，正是原版的差异。

## 按"原版新增就用原版"补的两处

- **被波及的可转向弹射物改认爆炸源为发射者**（1.21.10 起）：`if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile p) p.setOwner(getDamageSource().getEntity());`
- **半径趋近 0 的守卫**（仅 1.21.11 起，原版 `if (!(radius < 1.0E-5F))` 包住整个 `hurtEntities`）：我们用 `List.of()` 等价实现，**这会改变 `explodeScale=0` 的行为**——原移植下半径 0 会对贴脸生物造成满伤（TaCZ 公式 `minDistance*2/radius` 在半径 0 时等于 0 强度），加上守卫后改为完全不出实体伤害。这是原版行为，但确实是个行为变化，你要是不想要说一声。

**没跟着搬的**：1.21.5+ 把方向向量与倍率整段重写（`getEyePosition().subtract(center).normalize()` + `× getSeenPercent × getKnockbackMultiplier`）。理由同之前：那是 TaCZ 用 15 角点延迟补偿视线替换掉的**伤害算法**部分，搬过来会把 TaCZ 的视线补偿逻辑弄丢。只搬了新增**机制**。

## 验证

| 分支 | 方式 | 结果 |
|---|---|---|
| 1.21.10 | `:core:compileJava` | **BUILD SUCCESSFUL in 25s** |
| 1.21.10 `neoforge-compat` | 还在后台跑 | 完了我再报你 |
| 1.21.11 | `:core:compileJava :neoforge-compat:compileJava` | **BUILD SUCCESSFUL in 8m 18s** |
| 1.21.6 | 对照反编译源核对（未编译） | 与 1.21.4 一致，原样可用 |

关于你说的"单独编译文件"——**不需要手工拉 classpath 跑 javac**：`:core:compileJava` 是增量的，25 秒就跑完；慢的是 compat 模块（1.21.11 那份花了 **8 分 18 秒**）。所以验证一个 core 文件只跑 `:core:compileJava` 就够，别带 compat。

```bash
cd D:/Github/Custom-Gun-Continued-1.21.10neoforge && ./gradlew :core:compileJava -Dorg.gradle.java.home="E:/Software/Minecraft/JDK/jdk-21.0.8"
```

## 一个要提醒你的坑

**IDEA 的单文件分析给过假 clean**。1.21.10 那份 `_TempExplode.java` 明明用了 4 参数的 `ClientboundExplodePacket`（当地是 7 参数 record）、`explode()` 返回值也不兼容，`get_file_problems` 却返回 `errors: []`；而 1.21.11 上同一份文件它准准报出全部错误。差别是那个 worktree 的索引新不新鲜。**所以判定"没问题"要走 `:core:compileJava`**，别只看 IDE——你这轮说"1.21.6没问题"恰好是对的，但用同样方法看 1.21.10 就会漏掉。

两处改动（1.21.11 `28+/6-`、1.21.10 `23+/4-`）都在各自的 worktree 里，未提交，等你 review；游戏内仍未验证（重点看爆炸粒子和方块破坏表现，因为这次动了发包内容）。

# 

```
我之后会手动把1.21.10的修改cherry pick到1.21.11，两个版本相同的修改需要保持一致，仅1.21.11新增的修改允许在1.21.11加上，不然我cherry pick要解决冲突
```

# 

明白了——共用改动必须逐字一致，只有该版本独有的改动才单独加，否则 cherry-pick 会冲突。让我用 `git merge-file` 精确模拟一次 cherry-pick（以 1.21.11 的 HEAD 为 base、我改完的两个文件为两侧），看会不会撞。

实测 cherry-pick 会撞 3 处。让我看清每一处冲突到底是什么再决定怎么改。

冲突 1、2 的根因清楚了（`Util` 搬包那行紧挨着共用改动插入的 import，被 git 并进同一区域）。冲突 3 按理不该冲突（1.21.10 侧那行没动），让我看两侧真实的 hunk 结构。

`diff -u` 报了**整个文件一个大 hunk**——这通常是行尾不一致的信号。让我先确认行尾，再把三方统一成同一种行尾重新模拟（否则我的模拟结论本身是错的）。

行尾是罪魁祸首——统一成 LF 后重测，**真实冲突只有 1 处**（不是我刚才说的 3 处，那是 CRLF/LF 混用造成的假象），就在 import 区。让我测两种修法哪种能让 cherry-pick 完全干净。

两种都报 0 冲突，但我要确认"0 冲突"不是把错误的一侧静默吃掉了（尤其 shuffle 那行）。

先纠正我自己上一条的错误结论：**那不是 3 处冲突，是我的模拟把 LF 的 base 和 CRLF 的工作文件混在一起了**。统一行尾后重测，真实情况是——

**cherry-pick 会撞 1 处**，就在 import 区，根因是 1.21.11 独有的 `Util` 搬包：

- 1.21.10 里 `import net.minecraft.Util;` 排在 `net.minecraft.core.BlockPos;` 上一行；共用改动要在 `BlockPos` 后面插入 `ExplosionParticleInfo`
- 1.21.11 把这行删掉、并按排序挪到 `Mth` 之后——而共用改动又要在 `Mth` 后面插入 `WeightedList`

两个"插入点"都紧贴被删的那行，git 把整段并成一个改动区域 → 冲突。（半径守卫那处反而是干净的，只有我们这一侧改了，能自动合。）

我实测了两种放法，都能让 cherry-pick **0 冲突**且合并结果完全正确，代价是样式取舍不同：

收到——你已经 cherry-pick 好了，我只把冲突改完、不提交。

先看清当前状态（文件里现在有 `git stash apply` 留下的冲突标记）。

状态清楚了：1.21.11 的 HEAD 已是 cherry-pick 进来的 `Port #58 to 1.21.10`，工作区是 `UU`（未合并），冲突只有 import 那一处，半径守卫已经带过来了——和我模拟的完全一致。我来解掉它。

冲突只有那一处，栈里也没有残留 stash。解掉它——保留 import（放在排序正确的位置），这样两个版本的差异就只剩你允许的那两处。

冲突解干净了，编译也过了。现在把它标记为已解决（只 `add` 不提交）。

冲突解完了，没提交。

## 做了什么

1.21.11 文件里唯一的冲突（`git stash apply` 留下的 `UU`）就在 import 区，正是我上一条预测的那处。解法：**保留 `import net.minecraft.util.Util;`，放在排序正确的位置**（`Mth` 之后、`random.WeightedList` 之前）——也就是你 stash 里那份的写法，import 顺序不受影响。

现在两个版本的差异**正好是你允许的两处**：

```
20d19   < import net.minecraft.Util;              ← 1.21.10 有、1.21.11 没有
30a30   > import net.minecraft.util.Util;         ← 1.21.11 独有的搬包
272c272 > // 半径守卫 + List.of() 三元           ← 1.21.11 独有的守卫
```

验证：残留标记 0 处；`:core:compileJava` **BUILD SUCCESSFUL in 13s**。

状态：文件已 `git add` 标记为解决，`git status` 从 `UU` 变成 `M `（暂存 5 insertions/2 deletions），**没有提交**——剩下你自己 review 和提交。如果你不想让我动 index，`git restore --staged <file>` 就退回去。

## 两点顺带确认

- **1.21.10 worktree 是干净的**：我的改动已在你的提交 `595bdeada Port #58 to 1.21.10` 里，工作区无残留。
- **stash 栈是空的**，没有留下需要清理的条目。

## 关于我上次问的那个选择题

既然 cherry-pick 已经做完、冲突也解了，我就按"保持 import 排序"来解（这也是你 stash 里的写法）。如果你更想要全限定名那种写法——它的价值是让**下一个版本对**的 cherry-pick 不再撞同一处——说一声我改。

我把这条规矩记进 memory 了：**共用改动必须逐字一致，版本独有的改动（尤其搬 import）不要落在共用改动的同一 hunk 区**，并附了 `git merge-file` 预演冲突的方法（顺带记下那个行尾坑：`git show` 出 LF、工作区是 CRLF，混用会假报成片冲突——我上次误报"3 处冲突"就是踩了这个）。

# 

```
继续26.1.x
```

# 

继续 26.1.x。先看它的状态和版本号。

MC 版本是 **26.1.2**（NeoForge 26.1.2.7-beta），文件已被更新过两次并提交。先看它现在报什么错、以及跟 1.21.11 的差异。

`diff` 说明 26.1.x 的文件与 1.21.11 **逐字相同**——1.21.11 的移植被原样搬了过来。IDEA 只报 1 个错（`EntityType.is` 没了），工具链是 **Java 25**（本机 `C:\Program Files\Java\jdk-25.0.2` 可被自动探测）。让我用 26.1.2 的反编译源把这个类的依赖项全量比一遍，别只信那 1 个错。

路径都一致。现在逐个数 26.1.2 与原 1.21.11 的差异。

只差一处：26.1.2 把 `EntityType.is(TagKey)` 移到了 `Entity` 上（原版现在写 `entity.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)`）。爆炸其余部分（`explode()` 返回 int、7 字段发包、半径守卫、`Util` 路径）都与 1.21.11 一致。

26.1.x `:core:compileJava` **BUILD SUCCESSFUL (2m39s)**，而且不需要任何工具链参数（工具链是 Java 25，本机 `C:\Program Files\Java\jdk-25.0.2` 能被自动探测到）。核对一下改动面。

改动面确认：与 1.21.11 只差一处（`entity.is(...)` + 一行注释）。把 26.1.2 的事实补进 memory。

26.1.x 搞定了。

## 情况

26.1.x 上的 `_TempExplode.java` 与 1.21.11 那份**逐字相同**（1.21.11 的移植被整份搬了过来），而 26.1.2 的爆炸相关 API 我逐个数过——`ServerExplosion`、`Explosion` 接口、`ClientboundExplodePacket`、`ExplosionParticleInfo`、`WeightedList`、`Util` 的**路径与签名全部没变**（反编译源里只有局部变量被重命名）——所以整份移植成立，只差一处：

| 报错 | 原因 | 改法 |
|---|---|---|
| `Cannot resolve method 'is' in 'EntityType'` | 26.1.2 把 `EntityType#is` 挪到了 `Entity` 上 | `entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE)` → **`entity.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)`**（26.1.2 原版 `ServerExplosion` 里也是这么写的） |

改动面：与 1.21.11 只差这一行 + 一行说明注释（`2 insertions, 1 deletion`）。

## 验证

- `:core:compileJava` → **BUILD SUCCESSFUL in 2m 39s**
- **不需要工具链参数**：26.x 分支要 Java 25（1.21.x 是 21），本机 `C:\Program Files\Java\jdk-25.0.2` 能被 Gradle 自动探测到，直接 `./gradlew :core:compileJava` 就行

## 两点说明

- **未提交**，也没 stage（工作区就是 ` M`），等你 review。
- **compat 模块我没跑**：`_TempExplode` 在 core 里，我只改了一个私有内部类方法体里的一行调用，对外零 API 变化，compat 不可能受影响。要跑的话我可以后台跑一次（其他分支的 compat 要 7~8 分钟），你说一声。
- 顺手确认了那条新 memory（「core 用 Forge 编译导致 NeoForge 私有访问崩溃」）**不适用这里**：这个类用到的 `getDamageSource()` / `isSmall()` / `getHitPlayers()` / `getBlockInteraction()` 全是 public，没碰任何 private 成员。

26.1.2 的结论（爆炸 API 同 1.21.11 + `Entity.is` 这一处 + Java 25）我已补进 memory，后面 26.2 / 26.3 可以直接查。

# 

继续 26.1.x。先看它的状态和版本号。

MC 版本是 **26.1.2**（NeoForge 26.1.2.7-beta），文件已被更新过两次并提交。先看它现在报什么错、以及跟 1.21.11 的差异。

`diff` 说明 26.1.x 的文件与 1.21.11 **逐字相同**——1.21.11 的移植被原样搬了过来。IDEA 只报 1 个错（`EntityType.is` 没了），工具链是 **Java 25**（本机 `C:\Program Files\Java\jdk-25.0.2` 可被自动探测）。让我用 26.1.2 的反编译源把这个类的依赖项全量比一遍，别只信那 1 个错。

路径都一致。现在逐个数 26.1.2 与原 1.21.11 的差异。

只差一处：26.1.2 把 `EntityType.is(TagKey)` 移到了 `Entity` 上（原版现在写 `entity.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)`）。爆炸其余部分（`explode()` 返回 int、7 字段发包、半径守卫、`Util` 路径）都与 1.21.11 一致。

26.1.x `:core:compileJava` **BUILD SUCCESSFUL (2m39s)**，而且不需要任何工具链参数（工具链是 Java 25，本机 `C:\Program Files\Java\jdk-25.0.2` 能被自动探测到）。核对一下改动面。

改动面确认：与 1.21.11 只差一处（`entity.is(...)` + 一行注释）。把 26.1.2 的事实补进 memory。

26.1.x 搞定了。

## 情况

26.1.x 上的 `_TempExplode.java` 与 1.21.11 那份**逐字相同**（1.21.11 的移植被整份搬了过来），而 26.1.2 的爆炸相关 API 我逐个数过——`ServerExplosion`、`Explosion` 接口、`ClientboundExplodePacket`、`ExplosionParticleInfo`、`WeightedList`、`Util` 的**路径与签名全部没变**（反编译源里只有局部变量被重命名）——所以整份移植成立，只差一处：

| 报错 | 原因 | 改法 |
|---|---|---|
| `Cannot resolve method 'is' in 'EntityType'` | 26.1.2 把 `EntityType#is` 挪到了 `Entity` 上 | `entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE)` → **`entity.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)`**（26.1.2 原版 `ServerExplosion` 里也是这么写的） |

改动面：与 1.21.11 只差这一行 + 一行说明注释（`2 insertions, 1 deletion`）。

## 验证

- `:core:compileJava` → **BUILD SUCCESSFUL in 2m 39s**
- **不需要工具链参数**：26.x 分支要 Java 25（1.21.x 是 21），本机 `C:\Program Files\Java\jdk-25.0.2` 能被 Gradle 自动探测到，直接 `./gradlew :core:compileJava` 就行

## 两点说明

- **未提交**，也没 stage（工作区就是 ` M`），等你 review。
- **compat 模块我没跑**：`_TempExplode` 在 core 里，我只改了一个私有内部类方法体里的一行调用，对外零 API 变化，compat 不可能受影响。要跑的话我可以后台跑一次（其他分支的 compat 要 7~8 分钟），你说一声。
- 顺手确认了那条新 memory（「core 用 Forge 编译导致 NeoForge 私有访问崩溃」）**不适用这里**：这个类用到的 `getDamageSource()` / `isSmall()` / `getHitPlayers()` / `getBlockInteraction()` 全是 public，没碰任何 private 成员。

26.1.2 的结论（爆炸 API 同 1.21.11 + `Entity.is` 这一处 + Java 25）我已补进 memory，后面 26.2 / 26.3 可以直接查。

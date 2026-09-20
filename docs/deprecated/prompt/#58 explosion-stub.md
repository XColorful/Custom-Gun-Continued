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

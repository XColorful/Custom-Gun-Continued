#
---

```
接着#58 explostion-stub.md，弹孔逻辑还剩多少没移植？
```

# 

我先读取该任务文档和相关记忆。

读取迁移映射和两边的弹孔相关代码之前，我先快速定位范围。

CGC 已有客户端粒子的雏形，我先对比两边的弹孔实现细节。

客户端粒子已存在，关键要看服务端生成路径是否接上。

`ProjectileImpactManager.java:171` 就是缺口所在。我再核实数据层与客户端保真度细节。

## 结论：弹孔只剩**服务端生成点**一处没写，客户端部分早就齐了

### 已经就位的（约 95%）

| 部件 | 位置 | 状态 |
|---|---|---|
| 数据包/Codec/反序列化 | [BulletHoleOption.java](core/src/main/java/dev/xcolorful/customgun/core/particle/BulletHoleOption.java) | 完整 |
| 粒子类型注册 | [ModParticles.java:17](core/src/main/java/dev/xcolorful/customgun/core/init/registry/ModParticles.java:17) | 完整 |
| provider 注册 | [ClientModParticles.java:23](core/src/main/java/dev/xcolorful/customgun/client/init/ClientModParticles.java:23) | 完整 |
| 客户端渲染 | [BulletHoleParticle.java](core/src/main/java/dev/xcolorful/customgun/client/particle/BulletHoleParticle.java) | 渲染/方块纹理采样/UV 抖动/亮度衰减/淡出/附着失效剔除 全部已移植 |
| 配置项 | [RenderConfig.java:21-22](core/src/main/java/dev/xcolorful/customgun/client/config/RenderConfig.java:21) | life + fadeThreshold 都在 |

### 唯一缺口

[ProjectileImpactManager.java:171](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:171)：

```java
// TODO 命中效果 (弹孔/点燃等)
gunProjectile.discard();
```

对应 TaCZ 的 `EntityKineticBullet.java:498-500` 那三行 `serverLevel.sendParticles(bulletHoleOption, hitVec...)`。**没有它，弹孔一次都不会生成**——粒子和渲染器都是死代码。

两个候选落点，需要你定（我不替你猜）：
- **:171** —— TODO 注释所在，只管方块命中，语义最直接
- [ProjectileEffectManager.impactEffect](core/src/main/java/dev/xcolorful/customgun/core/projectile/effect/ProjectileEffectManager.java:22) —— 名字就是"命中效果"的空方法，但它在 `impactTick` 里**实体命中和方块命中都会调用**（[:104](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:104) 和 [:128](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:128)），要自己区分分支

**参数顺序陷阱**：不能照抄 TaCZ。TaCZ 是 `(dir, pos, ammoId, gunId, gunDisplayId)`，CGC 是 `(dir, pos, ammoLocation, gunDisplayLocation, gunLocation)` —— 后两个对调了。取数据用 `iGunProjectile.getAmmoIndexInstanceCache()` / `getGunLocation(gunProjectile)` / `getGunDisplayLocation(gunProjectile)`。

### 客户端保真度差异（3 处，不阻塞）

1. **弹孔没有曳光色染色**。TaCZ 构造器用 `gunDisplayInstance.getTracerColor()`、回退 `ammoDisplay.getTracerColor()`，写进 `rCol/gCol/bCol`；CGC 构造器完全没这段，弹孔是白色贴图。两个 getter 在 CGC 都已存在（[GunDisplayInstance.java:251](core/src/main/java/dev/xcolorful/customgun/client/resource/instance/assets/GunDisplayInstance.java:251)、[AmmoDisplay.java:112](core/src/main/java/dev/xcolorful/customgun/client/resource/assets/display/AmmoDisplay.java:112)）。
2. **`alpha = 0.9f` 缺失**，CGC 用默认 1.0，淡出曲线整体偏亮。
3. TaCZ 构造器里立刻判一次 `shouldRemove()`，CGC 只在 `tick()` 判；差 <1 tick，可忽略。TaCZ 那条 `ModBlocks.TARGET` 判定在 CGC 不适用——**CGC 根本没有打靶方块**（`ModBlocks` 只有 `BULLET_IGNORE_BLOCKS`）。

### 顺带：同一个 TODO 的「点燃」有静默数据 bug

- CGC 全仓库**没有任何 `setSecondsOnFire` 调用**，`fireAspect` 只在解析层存取，从未生效。
- 更麻烦的是 `_BulletData.java:63` 把旧键 `ignite` 当 boolean 读，但老包用的是**对象形式**：`tacz_default_gun/ak47_data.json:81`、cbrg 的 slr/groza/beryl_m762、suffuse 的 ak147 全是 `"ignite": {"entity": false, "block": false}`。`JsonUtils.readBoolean` 遇到 `{` 会走 `skipValue()` **静默返回 false**（[JsonUtils.java:63-68](core/src/main/java/dev/xcolorful/customgun/core/util/JsonUtils.java:63)）——不报错、不打日志。CGC 已有 `_FireAspectModifierData`（`ignite_entity`/`ignite_block`）可直接复用这个模型。

要我先只补弹孔那三行，还是把 ignite 的数据层一起修？

# 

```
- ignite不管
- todo里是调用参数是BlockHitResult, IGunProjectile, Entity的，怎么会在实体命中的时候调用？
- 如果在ProjectileEffectManager#impactEffect里创建弹孔的话，是否能获取到精确位置，以及是否会重复调两次？是否要在onNonBulletVictimHit(BlockHitResult里补一个setDeltaMovement来获取终点信息？
```

# 

我先重读当前代码，确认分支结构和 `TickContext` 里能拿到什么。

我重读了当前代码。先澄清一处指代问题：**TODO 所在的方法和 `impactEffect` 是两个不同的方法**，我上一条的"它"指代不清，抱歉。

- TODO 所在的 `cgc$onProjectileImpact(BlockHitResult, IGunProjectile, Entity)` —— 方块专用，你说得对，实体命中走不到它
- [`ProjectileEffectManager.impactEffect(TickContext, IGunProjectile, Entity)`](core/src/main/java/dev/xcolorful/customgun/core/projectile/effect/ProjectileEffectManager.java:22) —— 签名里**没有** BlockHitResult，而它确实在两个分支都被调用：[`:104`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:104)（实体分支）和 [`128`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:128)（方块分支）

## 你的三个问题

**1. impactEffect 里能拿到精确位置吗？** 能——`tickContext.blockHitResult.getLocation()` 就是精确命中点（[physics:102](core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:102) 已经把 `endPos` 也设成了它），`getDirection()`/`getBlockPos()` 同样可用。但有个信息缺口：**在 impactEffect 里无法区分这次回调是实体还是方块触发的**。`entityHitResults` 和 `blockHitResult` 是同一次 `physicTick` 一起算出来的，子弹穿过实体再打到墙时两者同时非空——如果 impactEffect 无条件建弹孔，会按实体命中在墙上留下一个洞。

**2. 会重复调两次吗？** 会。同 tick 内实体 + 方块都命中 → :104、:128 各一次；跨 tick 穿透实体（pierce > 1）→ 每 tick 一次。所以放在 impactEffect 里必须自己补分支判断，且这个判断拿不到"是谁触发的"这个信号，只能靠"blockHitResult 非 MISS"近似——会把上面那种情况误判。

**3. 要在 `onNonBulletVictimHit(BlockHitResult)` 补 `setDeltaMovement` 吗？** 不需要。`setDeltaMovement` 写的是**实体速度**，给下一 tick 的 `physicMove` 用（[physics:128](core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:128)），不含任何命中点信息。精确终点早在射线检测阶段就算完了，存在 `blockHitResult` 和 `endPos` 里，与速度无关。补第二次只会成为 [`:167`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:167) 之后的第三次冗余写入（:167 本身在默认路径下也是冗余的，因为必然 discard）。

## 建议落在 :171

放 TODO 那处比 impactEffect 更好，有三个结构性理由：

1. **天然只走方块路径** —— 不需要分支，不可能重复调用
2. **爆炸不留弹孔是免费的** —— `_TempExplode.explode` 成功时 [`:169`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:169) 提前 `return true`，根本走不到 :171，正好对上 TaCZ `EntityKineticBullet.java:493-495` 的"爆炸直接结束不留弹孔"。放 impactEffect 则要自己重新推导"刚才是否爆炸了"，做不到
3. **与 :167/:169 的"默认实现"语义一致** —— 弹孔是默认命中行为，扩展模组调用 `cgc$onProjectileImpact` 委托默认实现时应当拿到它

实现只需要 `getAmmoLocation` / `getGunLocation` / `getGunDisplayLocation` 三个已有 getter（都在 [GunProjectileDataAccessor.java:32-53](core/src/main/java/dev/xcolorful/customgun/core/api/entity/projectile/GunProjectileDataAccessor.java:32)），加 `gunProjectile.level() instanceof ServerLevel` 判定。**注意别照抄 TaCZ 的参数顺序**：TaCZ 是 `(dir, pos, ammoId, gunId, gunDisplayId)`，CGC 的 `BulletHoleOption` 是 `(dir, pos, ammoLocation, gunDisplayLocation, gunLocation)`——后两个对调了。

一个附带提醒：受弹方块自己实现 `cgc$onProjectileImpact` 且返回 true 而不 discard 时，因为 :167 把速度清成了 `hitLoc - pos`，子弹会停在命中点附近，下一 tick 可能重复命中同一方块、在同一位置反复生成弹孔。默认路径不受影响。

要我按 :171 这个方案实现吗？

# 

```
171添加弹孔
以及，如果受弹方块自己实现，应该不走模组内置的默认逻辑，不会触发速度清除？
```

# 

先确认两件事：`getAmmoLocation` 的声明位置，以及目前有没有方块实现 `IBulletVictimBlock`。

没有任何方块实现 `IBulletVictimBlock`，且该接口的 `cgc$onProjectileImpact` 是抽象方法（无默认体）——这正好回答你的第二个问题。现在按 :171 实现。

`IGunProjectile` 继承 `IGunProjectileDataAccess`，三个 getter 都可用。编译验证。

`BulletHoleOption` 的文本参数是 `String` 不是 `ResourceLocation`，补上转换。

## 1. 弹孔已加在 [:174-182](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:174)

```java
if (gunProjectile.level() instanceof ServerLevel serverLevel) {
    BulletHoleOption bulletHoleOption = new BulletHoleOption(blockHitResult.getDirection(), blockHitResult.getBlockPos(),
            iGunProjectile.getAmmoLocation(gunProjectile).toString(),
            iGunProjectile.getGunDisplayLocation(gunProjectile).toString(),
            iGunProjectile.getGunLocation(gunProjectile).toString());
    serverLevel.sendParticles(bulletHoleOption, hitPos.x, hitPos.y, hitPos.z, 1, 0, 0, 0, 0);
}
```

位置在爆炸 `return` 之后、`discard()` 之前，所以爆炸不留弹孔的行为自动成立。`BulletHoleOption` 的文本参数要 `String` 不是 `ResourceLocation`（第一次编译报错，已补 `.toString()`），所以没有去改 record 的签名。原 TODO 收窄成 `// TODO 点燃`，你说不管我理解为"不实现"，但保留这一行以免这条待办被静默丢掉——你要是想直接删掉也可以。

`:core` 编译通过（只剩 `AttachmentModifierType` 那两条仓库原有 deprecation 警告）。**未在游戏内验证**，需要你实机确认弹孔出现、贴图朝向/大小正常、约 400 tick 后淡出消失。

## 2. 对，你的判断正确——不委托就不触发速度清除

`setDeltaMovement` 写在**静态**的 [`ProjectileImpactManager.cgc$onProjectileImpact(BlockHitResult, IGunProjectile, Entity)`](core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/ProjectileImpactManager.java:168) 里，它只是"默认逻辑"，不是命中处理的必经之路。`:123` 选路决定了谁会跑：

| 命中对象 | 实际执行 | 经过 `setDeltaMovement` |
|---|---|---|
| 一般方块 | `onNonBulletVictimHit` → **静态默认逻辑** | 是 |
| 受弹方块，实现体里委托默认 | 方块自己的实现 → 再调静态默认逻辑 | 是 |
| 受弹方块，实现体里不委托 | 只有方块自己的实现 | **否** |

[`IBulletVictimBlockImpact.cgc$onProjectileImpact`](core/src/main/java/dev/xcolorful/customgun/core/api/block/victim/IBulletVictimBlockImpact.java:13) 是抽象方法、没有默认体，所以"自己实现"必然是一次显式重写，语义上就是接管——不委托即完全脱离默认逻辑。目前**没有任何方块实现 `IBulletVictimBlock`**（只有一个 `ProjectileHitBlockEvent`），所以现阶段所有方块命中都走默认逻辑，弹孔 100% 会生成。

顺带更正我上一条说错的地方：我当时说"受弹方块自己实现而不 discard 时，:167 会把速度清成 `hitLoc - pos`，子弹停在命中点附近、可能反复生成弹孔"——**这是错的**。:170 在默认逻辑里，不委托的方块根本走不到它，速度不会被清除。实际后果相反：子弹保持入射速度，`physicMove` 会把整 tick 的位移照跑，于是穿过墙面一格。同一个道理，这种方块也不会拿到 :174 的弹孔。

未提交，等你 review。要我接着修客户端那三处保真度差异（曳光色染色 / `alpha = 0.9f` / 构造期剔除时机）吗？

# 

```
继续修客户端三处
```

# 

继续修改客户端。先看现有的曳光色解析与取色器模式，以便复用。

现在修改客户端粒子：补曳光色、`alpha`、构造期剔除。

三处都补完了，[`BulletHoleParticle.java`](core/src/main/java/dev/xcolorful/customgun/client/particle/BulletHoleParticle.java) 编译通过（无新增警告）。

**1. 曳光色染色** — 新增 [`calculateTracerColor()`](core/src/main/java/dev/xcolorful/customgun/client/particle/BulletHoleParticle.java:161)，构造器里写进 `rCol/gCol/bCol`。复用了 CGC 已有的两段 fallback 写法：

- 枪械 display 用 [`getGunDisplayInstance(displayLocation, gunLocation)`](core/src/main/java/dev/xcolorful/customgun/client/api/resource/ClientResourceApi.java:176) —— 两参数重载正好对应 TaCZ `getGunDisplay(displayId, gunId)` 的二级回退，这也是 `BulletHoleOption` 要带 `gunLocation` 的原因
- 取不到就回退子弹 display 的曳光色，与 [`GunProjectileRenderer:141-149`](core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java:141) 同款

**2. `alpha = 0.9f`** — 加在构造器末尾，`render()` 里的淡出曲线基数与 TaCZ 一致了。

**3. 构造期剔除** — `if (this.shouldRemove()) this.remove();` 加在 `quadSize` 之后。TaCZ 那条 `ModBlocks.TARGET` 判定跳过（CGC 没有打靶方块）；首帧就消失的弹孔不再多渲染一 tick。

两处有意的偏差，说明一下：

- **枪械 display 完全缺失时**：TaCZ 因为整个 `ifPresent` 块不执行而保持白色；CGC 这里仍会回退到子弹的曳光色。我按项目内已有写法（枪械 → 子弹 → 默认白）保持一致，只在"包缺 display 资源"这种坏包场景下才有区别。
- **用 `ResourceLocation.tryParse` 而非 `new ResourceLocation`**：粒子选项是跨网络过来的，解析失败返回 null 就走"无颜色"分支，不会在客户端抛异常。

**未在游戏内验证。** 建议实机确认：弹孔带曳光色（用 RPG7 或任何曳光弹看颜色是否对上）、整体比之前暗一点（0.9 vs 1.0）、打到方块边缘时首帧不残留。

未提交，等你 review。

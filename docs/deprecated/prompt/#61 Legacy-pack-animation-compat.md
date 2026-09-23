> #61 提示词

# 

```
最新commit完成了GunDisplay对旧格式默认动画的填充
但是这个动画播放的帧数似乎不高，有卡顿的感觉，比直接枪械reload、shoot等动画都明显帧数低很多
排查一下问题？
```

# 

先看最新提交改了什么。

Now I have the reference upstream (D:\Github\TACZ) that plays the same files. Let me compare its animation implementation with CGC's.

Let me compare the keyframe data of the default animations against the pack's own animations.

Found a promising lead — let me compare the script-context getters (walk distance / aiming progress) between TaCZ and CGC.

Let me verify what partial tick value reaches the animation context, and check the aiming/walk inputs feeding the animation.

Let me verify the 1.20.1 field semantics through IDEA.

Let me check whether modern versions expose an interpolatable distance field, to see if a cross-version fix is possible at all.

Let me look for the test game instance to confirm the legacy script/data actually in use.

Let me quantify the actual per-frame jump caused by the missing interpolation, using the user's own pack data.

排查完成。结论：**新填充的默认动画本身没问题——是这些动画里"走路/奔跑"那一支的播放进度被绑在了一个每 tick 才更新一次的量上**，所以它们每秒只有 ~12–15 个采样点；而 reload/shoot/idle/draw 是挂钟时间驱动，逐帧平滑。

## 机制

1. 旧版状态机脚本每渲染帧用**行走距离**喂动画进度（装在你枪包里的两份脚本都一样）：
    - `tacz/.../default_state_machine.lua:489`：`context:setAnimationProgress(track, (context:getWalkDist() % 2.0) / 2.0, true)`（walk）
    - `:412`：同样一行（run）
2. CGC 的 `getWalkDist()` 取的是**每 tick 才变一次**的原始值：[IClientGunScriptBackCompat.java:176-187](core/src/main/java/dev/xcolorful/customgun/client/api/script/context/IClientGunScriptBackCompat.java:176) → [EntityUtils.java:19-21](core/src/main/java/dev/xcolorful/customgun/core/util/EntityUtils.java:19) `entity.moveDist`，没有任何 partialTick 插值。
3. TaCZ 的同名方法做了逐帧外推：`walkDist + (walkDist - walkDistO) * partialTicks`（`GunAnimationStateContext.java:327-341`）。
4. 反证"这是移植遗漏而非设计"：同类的 ADS 进度 CGC **是**插值的——`getAimingProgress()` 走 `cgc$getRenderAimingProgress(getPartialTicks())`（同文件 :92-96），脚本 :654 那行 `1 - getAimingProgress()` 因此平滑。只有 walkDist 这条链路漏了。

因为 `setAnimationProgress` 每帧把进度**绝对值**写回，而行走距离在一 tick 内不变，结果就是：**一个 tick 内动画被冻住（约 3 帧），到 tick 边界整段跳一次**。

## 量化（用你自己的 cibr 包 `ciba:rifle_default` 数据算的）

| 动画 | 每次 20 Hz 跳变 | 一个循环的采样点 |
|---|---|---|
| `run`（冲刺 5.6 m/s） | **9.6° / 2.07 unit** | 12 个 |
| `walk_forward` | 1.44° / 0.19 unit | 15 个 |
| `walk_backward` / `walk_sideway` | 0.76° | 15 个 |
| `walk_aiming` | 0.54° | 15 个 |

冲刺时每一步近 10° 的瞬跳，肉眼就是"明显帧数低"。

## 为什么这次 commit 才暴露

我扫了你 10 个包的动画文件：每把枪缺的正好是 `idle`/`walk_*`/`run*`/`slide*`/`aim*`/`melee_*` 这批——也就是本次填充进来的那批。之前没有原型，`runAnimation` 是静默 no-op（枪走动时干脆不动，你不会觉得"卡"）；现在填上了，才把这个插值缺口露出来。而 reload/shoot 是包自带的、由 `ObjectAnimationRunner` 用 `System.nanoTime()` 逐帧推进，所以照旧平滑。

顺带核对了整套引擎（`ObjectAnimationRunner`、插值器、`AnimController`、`AnimStateMachine`、`DiscreteTrackArray`、bedrock 转换、监听器）与 TaCZ 逐行等价，`update()` 也确实每渲染帧调用一次——问题不在这条链上。

## 建议的修法（需要你定）

在脚本上下文的那个访问器上补 partialTick 插值（TaCZ 语义），`anchorWalkDist` 要用同一公式。两个要注意的点：

- **1.20.1 上量本身就不同**：TaCZ 用的是水平 `walkDist`，CGC 现在用 `moveDist`，而 1.20.1 的 `moveDist` 是**三维**距离（我核对了 MC 源码：`Entity.java:692-693`），所以掉落/上下台阶也会推进走动动画。
- **跨版本**：`walkDist`/`walkDistO` 在 1.20.1、1.21.1 都在；1.21.4+ 已删除，而各分支现在的 `EntityUtils.getWalkDist` 是直接映射到 `getMoveDist`（我看了 1.21.4 / 1.21.11 / 26.1.x 三个 worktree，都这样），那边没有现成的"上一 tick 距离"可取。要不要为此加兼容包装/自己缓存上一 tick 的距离，按你的规则我不动，等你决定。

## 两点如实说明

- **我没有做实机验证**（跑不起客户端），以上是代码链 + 数据推演。
- 如果你看到的卡顿是**站着不动时**的 idle 摆动也在卡，那说明还有第二个原因——idle 是挂钟驱动、理论上必须平滑；这种情况告诉我，我再查（我会先要一段录像或当时的状态）。

后台扫描回来了，补充两点与本次改动直接相关的事实：

**1. 这段代码目前只在 1.20.1 这个 worktree**

13 个版本 worktree 里只有本仓有 `_reloadLegacyDefaultAnimation`（其余 12 个都还没有这段代码，`default_animation`/`use_default_animation` 的解析也没跟过去）。要不要按你的惯例移植过去，你定。

**2. 没有任何一个 worktree 带默认动画资源文件**

包括本仓在内，13 个 worktree 都没有 `assets/tacz/animations/rifle_default.animation.json` / `pistol_default.animation.json`（`find` 全仓只命中 `GunDisplay.java` 和 #47 那份文档）。你磁盘上的包也没有：`tacz_default_gun/assets/tacz/animations/` 里只有 30 个逐枪文件，`classic_rccrp` 的 `assets/tacz/` 下没有 `animations`。

而 [GunDisplay.java:418-419](core/src/main/java/dev/xcolorful/customgun/client/resource/assets/display/GunDisplay.java:418) 把 `use_default_animation: "rifle"/"pistol"` 硬编码解析到 `tacz:rifle_default` / `tacz:pistol_default`。所以：

- `use_default_animation` 那一批包（cbrg、cburg、origin、rainforest、classicr、tacz_default_gun…）在这个环境里**填不进去**——`legacyDefaultRifleAnimation` 是 null，`getDefaultAnimation()` 返回 null，等于没生效。
- 真正能生效的只有 `default_animation: "ciba:rifle_default"` 这种**显式路径**（cibr 包里自带这 6 个 `ciba:*_default` 文件）。

这决定了你实测的是哪把枪：如果你看到动画播出来的是 cibr 包的枪（k2 / pp19 / g18c 之类），那一切吻合；**如果你看到的是 `use_default_animation` 的枪**，那说明你客户端里另有一份提供 tacz 命名空间动画的资源包（或者 #47 文档里说好的"把两个资源拷进 CGC"这步你已经在游戏侧做了、但没进仓库）——告我一句，我好确认排查的前提没歪。

（无论哪种，上一轮的结论不变：被填进来的 walk/run 那几支是 20 Hz 阶梯，`run` 每步跳 ~9.6°，是卡顿来源；idle/aim/melee/slide/draw 都是挂钟驱动、本来就是逐帧平滑的。）

# 

```
我现在搭好了EntityMixin和utils，看下直接取水平距离是否跟1.20.1的walkDist一致
```

# 

看下你搭的东西。

核对完了：**不一致**，而且按现在这份 mixin 的写法，量到的值实际会恒为 0（不是卡，是不动）。逐条对比 vanilla 1.20.1 的实现（`Entity.java`，我会给出源码 jar 里的行号）：

```java
// Entity.baseTick()  :436
this.walkDistO = this.walkDist;                    // tick 开始时的快照

// Entity.move(MoverType, Vec3)  :680 / :692        ← 唯一的累加点
if (this.getMovementEmission().emitsAnything() && !this.isPassenger()) {
    ...
    this.walkDist += (float) vec3.horizontalDistance() * 0.6F;   // vec3 = 本次 move 碰撞裁剪后的实际位移
    this.moveDist += (float) sqrt(d1²+d2²+d3²) * 0.6F;
}
```

## 五处差异（按影响排序）

1. **测量窗口不含移动（致命）**。你是 `@Inject(method="tick", at=TAIL)`，而 `Entity.tick()` 在 `LivingEntity.tick()` 里只是第 3 行的 `super.tick()`——**移动发生在之后的第 42 行 `aiStep()`**（本地玩家再往后还有 `LocalPlayer.tick` 的 `sendPosition`）。所以 TAIL 时位置跟 HEAD 完全相同，`dx=dz=0`，字段恒为 0 → 脚本 `(h % 2.0)/2.0` 恒为 0，动画被钉在第一帧。
2. **没有累加**。vanilla 的 `walkDist` 是**单调累计**量；你写的是 `cgc$horizontalMoveDist = 本 tick 位移`（赋值而非 `+=`），于是 `h - hOld` 变成"每 tick 位移的变化量"（≈加速度），且 `h` 非单调，取模后会更乱。要跟 vanilla 对齐就得 `+=`，`Old` 存 tick 开始时的累计值快照。
3. **少了 `* 0.6F` 系数**。脚本用 `% 2.0` 归一化，所以少了这个系数会让走动/奔跑动画**快 1/0.6 ≈ 1.67 倍**（你的 2.0 对应走 2 格，vanilla 对应 3.33 格）。
4. **净位移 vs 路径长度**。vanilla 累加的是每次 `move()` **碰撞裁剪后**的位移（一 tick 可能调用多次，沿墙滑行/被推动的路径长度都算），你的是 tick 首末净位移；贴墙斜走、被击退、上下台阶时会偏小。另外 `move()` 返回 `void`，`vec3 = this.collide(...)` 是局部变量，所以 RETURN 注入拿不到它。
5. **缺守卫**。vanilla 只在 `getMovementEmission().emitsAnything() && !isPassenger()` 时累加（并且整块在 `if (this.noPhysics) {...} else {...}` 的 else 里，所以 `noPhysics=true` 的实体也不算）。你这份没有守卫，坐船/坐车/旁观者都会涨，跟 legacy 表现不一致。

**另外一处笔误**（和 mixin 无关，但会让上面全白费）：[IClientGunScriptBackCompat.java:176-187](core/src/main/java/dev/xcolorful/customgun/client/api/script/context/IClientGunScriptBackCompat.java:176) 里 `horizontalMoveDistOld` 也调用了 `getHorizontalMoveDist`，两个数恒等 → 插值项恒为 0，即使 mixin 修好了依旧是 20 Hz 阶梯。这里应该是 `getHorizontalMoveDistOld`（顺带说，`EntityUtils.getHorizontalMoveDistOld` 目前没有任何调用者，是它暴露了这处笔误）。

## 建议：照抄 vanilla 的位置/守卫/系数

```java
// EntityMixin —— 快照放在 baseTick HEAD（与 vanilla 同一位置）
@Inject(method = "baseTick", at = @At("HEAD"))
private void cgc$snapshotWalkDist(CallbackInfo ci) {
    this.cgc$horizontalMoveDistOld = this.cgc$horizontalMoveDist;
}

@Inject(method = "move", at = @At("HEAD"))
private void cgc$beforeMove(MoverType type, Vec3 delta, CallbackInfo ci) {
    this.cgc$moveStartPos = this.position();
}

@Inject(method = "move", at = @At("RETURN"))
private void cgc$afterMove(MoverType type, Vec3 delta, CallbackInfo ci) {
    if (this.noPhysics || this.isPassenger() || !this.getMovementEmission().emitsAnything()) return;
    Vec3 actual = this.position().subtract(this.cgc$moveStartPos);
    this.cgc$horizontalMoveDist += (float) actual.horizontalDistance() * 0.6F;
}
```

需要 `@Shadow public boolean noPhysics;`、`@Shadow public abstract boolean isPassenger();`、`@Shadow protected abstract Entity.MovementEmission getMovementEmission();`（**protected**，1.20.1 :1117 / 1.21.1 :1169 都是 protected，`@Shadow` 可以shadow）。挂在 `move()` 上还有个附带好处：vanilla 就是在这里累加的，子类覆写 `move()` 而不调 `super` 的情况两边行为自动一致。

## 两点版本事实（供你决定要不要保留这条统一路径）

- **1.20.1 与 1.21.1 的 vanilla 本身就有 `walkDist`/`walkDistO`**（我核对了两边源码，结构一样：`:436/:438` 快照、`:692/:691` 累加）。也就是说这两个版本可以直接用 vanilla 字段，mixin 只为 1.21.4+（`walkDist` 已删）而存在——是否保留统一路径是你的取舍。
- 我核实的签名/字段：`move(MoverType, Vec3)` 在 1.20.1 和 1.21.1 上各只有一份（mixin 不会歧义）、`noPhysics` 是 public 字段、`getMovementEmission()` 是 protected。**1.21.4+ 的 `move` 签名与 `getMovementEmission` 是否还在我没法确认**（本地只有 1.20.1/1.21.1 的源码），你在目标版本上过一遍。

我没有改任何文件，也没有实机验证（只做了源码与数据核对）。如果你想让我按上面的方案落到 `EntityMixin` + 修掉那处 `Old` 笔误，说一声。

# 

```
不使用walkDist字段是因为高版本没有这个字段，所以手动复制一份跨版本统一
按以上改注入位置，跟1.20.1的vanilla保持一致
```

# 

先核对两处以确保注入点等价，再改。

改完了，两处。

**1. [EntityMixin.java](core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/EntityMixin.java) 注入点/守卫/系数对齐 vanilla 1.20.1**

| 你的版本 | 现在 |
|---|---|
| `tick` HEAD 记位置 + 存 Old | `baseTick` HEAD 存 Old（对应 vanilla `Entity.baseTick():436` 的 `walkDistO = walkDist`） |
| `tick` TAIL 量首末净位移并**赋值** | `move` HEAD 记位置、`move` RETURN **累加**（对应 vanilla `Entity.move():680-692`） |
| 无守卫、无系数 | `noPhysics` / `isPassenger()` / `getMovementEmission().emitsAnything()` 三个守卫 + `* 0.6F`，与 vanilla 同一份 |

需要 `@Shadow` 的三个成员我按 vanilla 的可见性声明：`noPhysics`（public 字段）、`isPassenger()`（public）、`getMovementEmission()`（**protected**，1.20.1 :1117 / 1.21.1 :1169 都是 protected，声明成 public 会被 mixin 拒掉）。

**2. [IClientGunScriptBackCompat.java:176-195](core/src/main/java/dev/xcolorful/customgun/client/api/script/context/IClientGunScriptBackCompat.java:176) 修掉 `Old` 取错方法**

`anchorWalkDist` 和 `getWalkDist` 里那两处 `horizontalMoveDistOld` 原本也调的 `getHorizontalMoveDist`（两值恒等 → 插值项恒 0，即使 mixin 修好也还是 20 Hz 阶梯），已改成 `getHorizontalMoveDistOld`。插值公式本身保持你写的 TaCZ 形式，没动。

## 等价性核对（1.20.1 vanilla）

- `move()` 全类只有一份（`:612`，`(MoverType, Vec3)`），且位置就在 `:642` 一次性 `setPos(getX()+vec3.x, ...)` 应用 → move 前后的位置差 ≈ vanilla 用的 `vec3`。
- `position()` 在 1.20.1/1.21.1 都直接返回 `this.position` 字段（不插值），所以量出来的是本 tick 真实位移。
- `Entity.tick()` 就是 `baseTick()`（两个版本都一样），所以快照点在 tick 开头、累加在移动时，顺序与 vanilla 一致；`move` 被外部（活塞）调用时两边也一致。

**一处有意的近似**：vanilla 累加的是 `vec3`（`tryCheckInsideBlocks` **之前**的值），我用的是 move 结束时的位置差；实体卡在方块里被顶出去时，前者不计那一点点位移、我会计。玩家常态下两者相同。

## 两点交代

- **风格**：注入方法我改成了 `private` 且去掉了 `@Unique`（对齐 [LivingEntityMixin.java:56](core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:56) 的既有写法，也避免往 `Entity` 上塞 public 成员）；字段和接口实现仍保留 `@Unique`。不同意就说一声，我改回去。
- **验证**：`:core` 经 IDEA 重建通过，只有既有的 deprecation 警告，没有指向这两个文件的新问题。但**注入目标只有在启动游戏时才会被解析**（选错方法/可见性不匹配是启动即崩，不是编译期报错），而且这次的量值正确性也只能在游戏里看出来，所以我没做实测——建议你 `runClient` 走一遍：走动/冲刺时枪身抖动应当消失。另外 `IEntityExtension` 的 javadoc 里还写着"处理 `EntityUtils#getMoveDist` 的版本差异"，现在这个字段的语义已经是 `walkDist` 的替身，要不要顺手改掉由你定。

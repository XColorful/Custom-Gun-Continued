# 

```
PR #59，1.20.4forge正常，1.21.1forge开枪生成粒子后会报错无法解码，退出到主界面，需要修复。

- 日志在D:\Temp-File\debug.log
- 注：1.20.1的代码到26.3仍编译通过，目前认为是1.21.1起引入的问题
```

# 

I'll start by reading the crash log to understand the decode error.

The startup log is long; let me jump straight to the crash.

Found the root cause. Let me look at the repository structure and the particle registration code.

PR #59 is in the Custom-Gun-Continued repo. Let me examine the particle registration code.

PR #59 landed on `core1.20.1` (bullet-hole-skeleton), which added the particle. There's a dedicated `1.21.1forge` worktree. Let me compare the particle registration across branches.

Let me read the full crash stack trace.

Now let me look at `BulletHoleOption` and how the particle actually gets spawned.

Let me examine the Forge registrar implementation that handles particle type registration.

Found it. Let me confirm by reading the Forge mod entry point on both branches.

The recursive grep timed out; let me use the Grep tool instead.

Let me compare how the particle is actually spawned on each branch.

Let me check how the sibling NeoForge branches handle this same registration.

Let me check the history of this particle registration to see if a registration call was ever present.

I have the decompiled MC sources for both versions. Let me compare the packet write path.

Let me search the 1.20.4 tree more thoroughly for any particle registration path.

Let me check whether any branch ever contained a particle registration call.

Let me check whether this machine also has the 1.20.4 instance and its logs, for a direct comparison.

Let me check the explosion stub, which sits on the path before the particle spawn.

The crash is clear. Let me also check the `customgun:null` warnings to see whether they're related.

Now I'll apply the fix. Let me read the file properly first.

Fix applied. Now let me verify it compiles.

While that builds, let me check for other logs that might show a working 1.20.4 run.

The log confirms it: line 569 shows the particle entry was staged but the registry lookup still failed. Let me check whether any other registrar is likewise unregistered.

Let me check whether a built 1.20.4 jar exists locally, which would settle the 1.20.4 question.

PR #59 landed on both branches at the same moment (2026-09-20 23:51); the existing 1.20.4 jar on disk is a Sep 17 build that predates it. Let me check the compile result.

While the build runs, let me verify the client-side provider wiring, since the particle will now actually reach the client for the first time.

Diagnosis is confirmed. Here's the report while the compile finishes.

**Root cause** — the bullet-hole particle type is never registered. `ModParticles.PARTICLE_TYPES` is the only one of the 10 registrars in [CustomGunForge.java:67-74](app://localhost/epitaxy/local_d9f619f8-b807-4a79-aa58-9ede298aa750) that was never handed to the mod event bus, so `customgun:bullet_hole` never lands in `Registries.PARTICLE_TYPE`.

When a round hits a block, [ProjectileImpactManager.java:186](app://localhost/epitaxy/local_d9f619f8-b807-4a79-aa58-9ede298aa750) calls `serverLevel.sendParticles(...)`. Encoding that packet looks the type up by registry id via `ByteBufCodecs.registry` → `IdMap.getIdOrThrow`, which throws `IllegalArgumentException: Can't find id for '...ModParticleType@618fb1'`. The throw happens in the Netty encoder, which kills the connection → back to the main menu.

Evidence from the log:

- Line 569 `Registering Forge object: bullet_hole` — the entry was _staged_ (class init ran), but no `RegisterEvent` listener was ever attached for that register.
- Line 1382 the failing map is `minecraft:particle_type (Stable)`, i.e. frozen with no entry for us.
- All other registrars (items, entities, tabs, recipes ×4, sounds) are registered; particles were simply omitted.

**Fix applied** (one line) in [CustomGunForge.java:75](app://localhost/epitaxy/local_d9f619f8-b807-4a79-aa58-9ede298aa750):

```java
ModSounds.SOUNDS.registerAll(modEventBus);
ModParticles.PARTICLE_TYPES.registerAll(modEventBus);
```

**Correction to the "introduced in 1.21.1" theory** — this is PR #59's bug, not a 1.21.1 API change. 1.20.4's `FriendlyByteBuf.writeId` throws the identical exception when the id is absent, and 1.20.4 has the same missing call. It looked fine because the 1.20.4 jar on disk (`customgun-0.0.16-forge-1.20.4.jar`) is a Sep 17 build, while PR #59 merged into both branches at 2026-09-20 23:51. Rebuild 1.20.4 from current source, shoot a non-explosive round at a block, and it should fail the same way.

Unrelated noise in the log: the `customgun:null` / `AmmoParticle not valid` warnings come from TaCZ pack data missing particle locations ([ClientAmmoIndexInstance.java:107](app://localhost/epitaxy/local_d9f619f8-b807-4a79-aa58-9ede298aa750)) — pre-existing, harmless.

The `:forge-compat:compileJava` run is still going; I'll confirm the build when it lands. Do you want the same one-line fix propagated to `neoforge-compat` in this repo and to the other version branches (1.20.4, 26.x, etc.), or keep this PR scoped to 1.21.1forge?

# 

```
你看下1.20.1，是否1.20.1开始就应该直接在forge侧调registerAll，然后代替掉RegisterParticleProvidersEvent？
```

# 

Let me look at the 1.20.1 worktree to see how particles are wired there.

Let me find where the bullet hole is actually spawned on 1.20.1.

There's a design doc for PR #59. Let me read it.

Let me verify the same crash applies to 1.20.1's packet path, and check on the earlier compile.

Let me confirm Forge's provider-registration API, since that determines whether the event hook must stay.

Important detail found — let me read the client lookup path to be precise.

Verified across all three versions — here's the answer.

## 两个机制管的是两件不同的事，不能互相替代

**`registerAll(modEventBus)`** —— 把 `ParticleType` 实例注册进 `Registries.PARTICLE_TYPE`（`DeferredRegister` + `RegisterEvent`）。它决定：`ClientboundLevelParticlesPacket` 能不能按注册表 id 编码/解码、`/particle customgun:bullet_hole` 能不能被识别、数据包能不能按名字引用这个粒子。

**`RegisterParticleProvidersEvent`** —— 客户端把「类型 → `ParticleProvider`」放进 `ParticleEngine`，决定收到粒子之后画什么。它是 Forge 唯一入口，1.21.1 里这个方法只暴露 `registerSpecial` / `registerSprite` / `registerSpriteSet` 三个（没有裸 `register`），`registerSpecial` 就是普通 provider 的正路，直接转 `ParticleEngine.register`。所以现在写的 `event::registerSpecial` 是对的，**这个 hook 必须留着**。

## 关键发现：客户端的 provider 表是按「注册名」索引的，压根不看注册表

1.20.1 / 1.20.4 / 1.21.1 三份 Forge 补丁后的 `ParticleEngine` 完全一致：

```java
private final Map<ResourceLocation, ParticleProvider<?>> providers = new java.util.HashMap<>();  // 原版是 Map<ParticleType<?>,…>
providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(type), provider);          // 注册
providers.get(BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType()));       // 查询
```

类型没注册时，`getKey` 在写入和查询两侧都返回 `null`，`HashMap` 容忍 null 键 → **provider 照样能命中，客户端其实画得出来**。

这正好解释了为什么 #59 的文档把「粒子类型注册」标成了"完整"（`docs/deprecated/prompt/#59 bullet-hole-skeleton.md:27`）——它只核对了 `ModParticles` 里有字段、有 `PARTICLE_TYPES.register(...)`，没核对这个 `IRegistrar` 有没有挂上总线；provider 那条链路是通的，所以从客户端看不出问题。唯一依赖注册表的就是**网络编码**那一环，也就是崩溃点。

## 「1.20.1 起就该在 forge 侧调 registerAll」—— 对，而且 1.20.1 现在也缺

1.20.1 是同一份代码的同一个缺口：

|证据|1.20.1|
|---|---|
|`CustomGunForge.java:67-74`|同样只 registerAll 了 items/entities/tabs/recipes/sounds，没有 `ModParticles.PARTICLE_TYPES`|
|`ProjectileImpactManager.java:186,190`|同样 `serverLevel.sendParticles(bulletHoleOption, …)`|
|`ClientboundLevelParticlesPacket.java:53`|同样 `writeId(BuiltInRegistries.PARTICLE_TYPE, …)`|
|`FriendlyByteBuf.java:150-157`|id == -1 时抛同一个 `IllegalArgumentException: Can't find id for …`|

所以 **1.20.1 实机开枪打方块也会一样断线**，只是"1.20.1 到 26.3 都编译通过"验证的是编译，不是运行。`git log --all -S "PARTICLE_TYPES.registerAll"` 在所有分支上零命中 —— 也就是每个端口都缺这一行，不是 1.21.1 引入的。

顺便修正上一轮的推断：1.20.4 之所以看着正常，是因为那边跑的是 9 月 17 日、PR #59（9 月 20 日 23:51 合入）之前的 `customgun-0.0.16` 构建，而崩溃的 1.21.1 是 `0.0.17`。

## 结论

- 要在 Forge/NeoForge 侧的 mod 构造里补 `ModParticles.PARTICLE_TYPES.registerAll(modEventBus);`，与其它 9 个注册器并列（1.21.1forge 我已改，diff 就一行）。
- `RegisterParticleProvidersEvent` 不能删也不能被替代——两者缺一不可：少了 `registerAll` 是服务端发包崩（现在的症状），少了 provider 注册则是包正常收发但屏幕上什么都不出现。
- `ClientModParticles` + `ParticleRegisterer` 这层转发是为了让 core 不依赖加载器，符合项目里其它注册的写法，不用动。

`:forge-compat:compileJava` 还在跑（ForgeGradle 首次构建比较慢），跑完我确认。要我把这一行一并补到其它端口吗？即 1.20.1 / 1.20.2 / 1.20.4 / 1.21.1neoforge / 1.21.4 / 1.21.6 / 26.x 各自的 `CustomGunForge`、`CustomGunNeoforge`。

# 
---

```
现在要处理PR #59的问题：
- 1.21.6的弹孔粒子正常，1.21.10起弹孔粒子方片的法线始终跟玩家视角朝向0 0的直线平行
```

# 

I'll look at the bullet hole particle code and how it differs between versions.

The particle code lives in the Custom-Gun-Continued repo. Let me find the bullet hole particle there.

Found it — PR #59 is the bullet-hole skeleton in Custom-Gun-Continued. Let me read the particle code in both the 1.21.6 and 1.21.10 worktrees.

Now let me look at the vanilla 1.21.10 `QuadParticleRenderState.add` to see what basis the quad is built in.

Now I see the render basis changed. Let me check the supporting `direction().getRotation()` helper and `BulletHoleOption`.

Let me check whether the newer branches (1.21.11, 26.x) already fixed this, and read the current file.

Let me check where the bullet hole particle is spawned, to see the surface offset.

Let me verify the JOML conventions empirically rather than by memory.

Let me write a decisive test against the real JOML library.

Confirmed the bug. Let me see the candidate fixes.

The diagnosis is confirmed. Let me check the particle size constant and the target worktree state.

Now let me read the target file properly before editing.

Now I'll apply the fix.

The diff is clean. Let me check the compile result.

Gradle can't find a Java 21 toolchain. Let me look for a local JDK 21.

Compiles cleanly. Let me record the environment/workflow facts I had to discover, then summarize.

修好了。根因不在 `Direction.getRotation()`，而在 1.21.10 的粒子渲染重构。

## 根因

1.21.10 把 `Particle.render(VertexConsumer, Camera, float)` 换成了 `extract(QuadParticleRenderState, Camera, float)`，方片的顶点由 MC 自己生成（[QuadParticleRenderState.java:185](app://localhost/epitaxy/local_55a4b8da-0fd2-4f30-98f3-981d22b8b974)）：

- 方片固定在 **XY 平面**，法线是 `+Z`；
- 1.21.6 及以前自己画的方片在 **XZ 平面**，法线是 `+Y`。

而 `Direction.getRotation()` 的语义是「把 `+Y` 转到该面朝向」（我用 JOML 1.10.8 复刻验证了 6 个方向全部成立）。所以把同一个 quaternion 塞给新的 render state，方片平面整体偏了 90°：朝北墙上的弹孔会变成水平躺在半空、法线朝下（`0,-1,0`）；地板的弹孔会变成竖着的。你看到的现象就是这个。

## 改动

[BulletHoleParticle.java](app://localhost/epitaxy/local_55a4b8da-0fd2-4f30-98f3-981d22b8b974)，两处：

1. 构造时先乘一个 `rotationX(-π/2)`（把 `+Z` 转到 `+Y`），再交给 `QuadParticleRenderState`：
    
    ```java
    this.rotationCache = new Quaternionf(direction.getRotation()).mul(QUAD_NORMAL_FIX);
    ```
    
2. 顺手修了同一处重构留下的第二个问题：原来的 `particleY += 0.005F` 是全局 Y 抬升，只对地板/天花板有用，对墙面完全不起作用（等于没防 z-fight）。改回沿命中面法线外推 `0.005`。这两块是同一个 refactor 一起坏掉的，不修的话墙上的弹孔会重新和方块面打架。

## 验证

用 JOML 1.10.8 跑了复刻测试（`Direction.getRotation` + render state 的顶点顺序），修复后 6 个方向的**方片法线都等于命中面朝向、方片都贴合该面**；修复前 6 个方向全部偏 90°。文件单独编译通过（javac 25，针对上次构建产物拼的 classpath）。

两点限制：`./gradlew` 在这台机器上跑不起来（没有 JDK 21，foojay 下载被墙，只有 8/17/25），所以我没法做完整构建；也没法进游戏实际看效果，视觉上的最后确认还得你来。

## 需要你定

- 同样这段代码在 `1.21.11neoforge`、`26.1.2neoforge`、`26.3neoforge` 三个 worktree 里一字不差，你说的是「1.21.10 起」——要我一并改过去吗？（那需要逐个确认 `Mth.HALF_PI` 等 API 在对应版本还在，我会分别验一遍。）
- 还没提交。要我在这条 `1.21.10neoforge` 分支上建个 commit（PR #59 已经 merge 了，所以这会是新的分支/PR），还是你先自己过一眼 diff？

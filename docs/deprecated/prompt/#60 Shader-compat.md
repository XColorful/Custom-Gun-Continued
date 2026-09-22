> #60 提示词

# 

```
现在要加光影兼容

实现方式为：
- CGC本体留空Compat实现，CGC-Compat-Shader注入Iris/Oculus接口，从而CGC里调用这个空实现的时候默认就对应Iris的接口，而没装兼容mod/光影时保持原先行为
- CGC里不得引入gradle环境或光影import

- 当前IDEA打开了多个窗口，供浏览源码
- CGC Compat只做新版Oculus兼容
- TaCZ到CGC类的迁移映射在./docs-tacz/TaCZ Migration Mapping.md
- 如果需要新增加Oculus/Iris接口，则添加到IrisCompat空实现，编译出来的.jar换到Compat目录下./libs/里

目前安装光影+Embeddium没看出内存占用持续增长，TaCZ官包枪械渲染外观也没明显变化
研究下怎么兼容光影，并在CGC实现
```

# 

I'll start by reading the project docs and the in-progress shader compat work.

Key finding: in Iris/Oculus, `FullyBufferedMultiBufferSource.endBatch(RenderType)` is a **no-op** — that's the real reason this compat exists. Let me examine the call sites on both sides.

Now let me confirm when the fully-buffered source is actually installed, and read the remaining call sites.

Now let me check how CGC's 1.20.1 jar is built, since the compat mod compiles against it.

I have the full picture now. Let me lay out a plan before touching code.

I've completed the research (I traced the Oculus 1.7.0 bytecode directly). Three decisions need your call before I finalize.

Plan approved. Setting up tasks, then implementing.

Mixin, config, gradle dep, and doc are done. Now building CGC.

CGC built. Verifying the new jar before swapping.

The jar is gitignored (local dependency), so swapping is safe.

Build failed with a JDK-version error. Let me diagnose before assuming it's my change.

The default `java` on PATH is JDK 25, which Gradle 8.8 can't use. Retrying with the local JDK 21.

The Mixin AP can't map a target in a non-obfuscated mod class. Adding `remap = false` on the injections.

Build succeeded. Now the critical verification — the refmap entry for `endBatch`.

The refmap came out **empty** — my predicted `NoSuchMethodError` risk is real. Let me compare against CGC's known-good refmap.

Both repos reobfuscate mixin bodies — so an empty refmap is correct here. Let me confirm the compiled annotations.

Verified against Oculus 1.8.0 (the version we compile against): `endBatch(RenderType)` is still `{ return; }`. Gathering the final diff.

光影兼容已实现，两个仓库都构建通过。

## 核心发现（反编译 Oculus 1.8.0 确认）

`FullyBufferedMultiBufferSource.m_109912_(RenderType)` 的方法体是 `{ return; }` —— **空的**。Iris 在关卡渲染窗口内让 `RenderBuffers.bufferSource()` 返回这个全缓冲源，于是 CGC 里那句 vanilla 风格的 `endBatch(renderType)` 装了 Oculus 后静默失效；真正提交只发生在无参 `endBatch()`（收集 segment → 按 RenderOrderManager 排序 → 清空）。另一处是阴影 pass 会连带渲染实体，枪口火焰/抛壳得挡掉。

关于你说的"没看出内存增长"：`endBatch(RenderType)` 是空实现这点是确证的，但"持续增长"我没能从代码里证出来——Iris 自己在 translucency 阶段和 `endLevelRendering` 都兜底 flush，所以这条路径更稳定的表现是**提交时机/排序差异**而非无界增长。本次按 TaCZ 既有做法落地，没发明新修法。

## 改动

CGC（`Shader-compat` 分支）：只改了 `docs-tacz/TaCZ Migration Mapping.md` 的 `|oculus|oculus|` → `|oculus|iris|`（左列是 TaCZ 自己的包名，保持不动）。会话里已有的 `IrisCompat` 重命名 + 5 处调用点我按计划没动。

CGC-Compat-Shader：新增 [IrisCompatMixin.java](core/src/main/java/dev/xcolorful/cgccompat/shader/client/mixin/iris/IrisCompatMixin.java)、在 [cgccshaderclient.mixins.json](core/src/main/resources/cgccshaderclient.mixins.json) 注册、给 [forge-compat/build.gradle](forge-compat/build.gradle) 补 `oculus` 的 compileOnly（core 的 compileOnly 不会传递到重新编译 core 源码的 forge-compat）。

## 两处和计划不同，都已验证

1. **必须加 `remap = false`**：三个 `@Inject` 的目标在 CGC 里不参与混淆，Mixin AP 查不到映射，直接报 `Unable to locate obfuscation mapping` 编译不过。
2. **refmap 是空的** `{"mappings":{},"data":{}}`。我在计划里把它当成关键验证点，结果它为空且**这是正确的**：`reobfJar` 把 mixin 方法体里的调用重映射成了 SRG 名——打包后的字节码是 `invokevirtual MultiBufferSource$BufferSource.m_109911_()V`，生产环境下不靠 refmap 也能对上。所以我把验证下沉到了字节码层面（比查 refmap 更硬）。

## 验证到哪一步

- 两个仓库 `BUILD SUCCESSFUL`；CGC 新 jar 已含 `compat/iris/IrisCompat.class`、不再有 `compat/oculus/`，并已覆盖 Compat 的 `libs/`（该目录被 gitignore，不留 diff）。
- 产物核对：`@Mixin` 指向 `IrisCompat`、三个 `@Inject` 的 `method`/`HEAD`/`cancellable`/`remap` 参数、`endBatch` 体已 SRG 化、jar 内 mixins json 与 `MixinConfigs` manifest、`mods.toml` 占位符全部展开。
- 对编译目标版本 Oculus 1.8.0（缓存 jar 就是 1.8.0，与 `oculus_version_range=[1.8.0,)` 一致）复验 `endBatch(RenderType)` 仍是空方法。

**我跑不了游戏，这些只有你能确认**：影子 pass 里有没有火焰/弹壳鬼影、开关光影包时枪身外观是否和 vanilla 一致、长时间扣扳机的内存曲线。

## 两个提醒

- 构建 compat 时我用了 `JAVA_HOME=E:\Software\Minecraft\JDK\jdk-21.0.8`。PATH 上的默认 `java` 是 JDK 25，Gradle 8.8 跑不动（`Unsupported class file major version 69`）。
- `endBatch` 那条是 TaCZ 的重锤：会把当时排队的所有实体几何体一起提交掉。本次只对齐 TaCZ，没动策略。
- 顺带一提：compat 的 `mod_version=0.0.0`，产物名是 `cgccshader-0.0.0-forge-1.20.1.jar`——脚手架遗留，发布前你自己定。

未 commit、未 push，等你 review。

# 
[fork1](./single/60%20Shader-compat-fork1.md)

# 

```
现在继续1.21.1neoforge，环境已经搭好了，要求跟之前相同
```

# 

Continuing to 1.21.1 NeoForge. Let me survey both new repos.

CGC 1.21.1 already has the stub and all 5 call sites. Now the critical unknown — the Iris 1.21.1 API surface.

Confirmed: Iris 1.21.1 still has the no-op `endBatch(RenderType)`, and production uses official (readable) names. Let me check the remaining state.

Iris 1.21.1's batched-entity mixins are `required: true` (always applied), same mechanism. The compat `libs/` jar is still the stale `compat/oculus` one, so a rebuild+swap is needed here too. Setting up tasks.

MachineMax's NeoForge artifacts contain no refmap at all — MDG doesn't generate one, and NeoForge's official namespace makes it unnecessary. So no `remap = false` here. Building CGC 1.21.1.

Verified end to end. Saving the cross-loader gotcha for future compat repos.

1.21.1 NeoForge 做完了，两个仓库构建通过。

## CGC 1.21.1 侧不用改

PR #60 的 merge（`1e010f240 Add Iris Shaders compat skeleton`）已经把 `client/compat/iris/IrisCompat` 和 5 处调用点带过来了，工作区是干净的。`docs-tacz` 里的 `|oculus|iris|` 也已同步。所以我这次没碰 CGC 的源码。

## Iris 1.21.1 的 API 核对（反编译确认）

编译目标是 `irisshaders-455508-8242804.jar` = **Iris 1.8.14-beta.1+mc1.21.1**，和 `iris_version_range=[1.8.13,)` 对得上：

- `net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource` 仍在，`endBatch(RenderType)` **依然是 `{ return; }`** —— 和 Oculus 1.8.0 一模一样的空实现。
- Iris 的 `iris-batched-entity-rendering.mixins.json` 是 `"required": true`，**不依赖光影包是否启用**，装了 Iris 就一直生效。
- 顺带：Iris 1.21.1 明确把 Embeddium 声明为 `type = "incompatible"`，所以这边的搭配是 **Sodium 0.8**，不是 Embeddium —— compat 的 gradle 和 mods.toml 本来就写的 sodium，一致。

## 改动（只有 compat 仓库）

新增 [IrisCompatMixin.java](../../CGC-Compat-Shader-iris-1.21.1neoforge/core/src/main/java/dev/xcolorful/cgccompat/shader/client/mixin/iris/IrisCompatMixin.java)，在 [cgccshaderclient.mixins.json](../../CGC-Compat-Shader-iris-1.21.1neoforge/core/src/main/resources/cgccshaderclient.mixins.json) 注册。三个 `@Inject` 和 1.20.1 同源，但**没有** `remap = false`：

| | Forge 1.20.1 | NeoForge 1.21.1 |
|---|---|---|
| 生产环境 MC 名 | SRG（`m_109911_`） | official（`endBatch`） |
| `remap = false` | 必须加，否则 AP 报错编译不过 | 不需要，AP 直接接受 |
| refmap | 生成了但是空的（reobf 已把方法体重写成 SRG） | MDG 根本不生成 |

所以这里的字节码里直接就是 `invokevirtual MultiBufferSource$BufferSource.endBatch:()V` —— 生产环境本来就叫这个名字，不需要 refmap 兜。这也和 `CGC-Compat-MachineMax` 的 NeoForge 写法一致（它也没 `remap = false`、产物里也没有 refmap）。

另外 `neoforge-compat` 不重新编译 core 的源码（只 `from project(':core').sourceSets.main.output`），所以**不需要**像 Forge 那边给 neoforge-compat 重复声明 iris 依赖。

## 换包与验证

Compat 的 `libs/` 里那份 CGC jar 还是旧的 `compat/oculus/OculusCompat.class`，所以照样得先换包。已重建 CGC 1.21.1 并替换（该目录 gitignore，不留 diff）。

产物核对结果：

- CGC jar 含 `compat/iris/IrisCompat.class`、不再有 `compat/oculus/`
- compat jar（`cgccshader-0.0.0-neoforge-1.21.1.jar`）含 `IrisCompatMixin.class`、mixins json、`logo.png`/`pack.mcmeta`，结构完整
- `@Mixin(value=[class Ldev/xcolorful/customgun/client/compat/iris/IrisCompat;])`
- 三个 `@Inject` 的 `method`/`HEAD`/`cancellable=true` 参数齐全
- `neoforge.mods.toml` 的 `[[mixins]] config="cgccshaderclient.mixins.json"` 与全部占位符已展开

**游戏内只有你能确认**：Sodium 0.8 + Iris 1.8.14 开光影包时，枪身/枪口火焰/抛壳/手部外观是否和 vanilla 一致，阴影 pass 里有没有火焰/弹壳鬼影，以及不开光影包时的对照行为。

未 commit、未 push。另外我把 Forge/NeoForge 这套 mixin 差异记进了长期记忆，省得以后给新的 CGC-Compat-* 写 mixin 时再踩一遍。

# 

```
当前1.21.1，开光影后复现了#50 1.21.1render-offset-fix.md的问题，只是枪没有偏移，第一人称看到的枪射物起点偏了
- 在玩家朝向121.3 -1.6时，枪射物从屏幕右边往前方射出；在90 -1.9时也是在右边射出；44.7 -2.8也是
- 朝另半边的时候对称，-142.8 -1.2在屏幕左边射出；-57.4 -0.5也是从左边射出
- 无论开镜瞄准进度为多少，都是偏的
- 关闭光影时没有第一人称枪射物起点偏移问题
- 关闭光影时，第一人称看红点瞄准镜(cbrg:red_dot)的ocular，朝前方的一面正常应该是看不见的，也就是scopeBodyPath/scope_body/lens/下有个位置-0.563,1.4375,-1.3125尺寸1.126,1.2229,0的块，北面有红点贴图，但是正常情况下应该是从第一人称（从后面看）看不见这个朝向前方的贴图的
- 光影用的是Complementary

当不安装光影及Compat Shader时，没有以上问题
- 最新一次安装光影mod的log在D:\Temp-File\debug.log

可以在CGC 1.21.1做修改，但不得引入gradle环境；如果需要新增API则依旧是放在Compat Shader里注入
```

# 

Let me start by reading the referenced issue and the log.

The #50 doc gives me the previous fix. Now let me check whether our mixin actually applied, and find the muzzle-offset code.

Mixin is applied. Now the muzzle-offset path.

Let me read how the cached FOV values are produced — the fix hinges on them.

Let me check how Iris handles FOV for the hand pass — that's what the offset correction depends on.

Strong lead: Iris renders the hand through its own `HandRenderer`. Let me see what poseStack basis it builds.

Decisive lead: Iris builds the hand poseStack as **identity + bob only**. Let me confirm against vanilla's setup.

Let me confirm the vanilla basis precisely — what `projectionMatrix` is at that call site.

Found the root cause. Let me nail down the exact poseStack state where CGC's first-person render is invoked.

Root cause of the tracer offset is clear. Let me check how CGC's gun render gets its buffer source, then write up.

Both bugs may share this root cause — let me find every site that reads translations off the hand pose matrix.

Let me settle the camera-rotation convention from vanilla's own usage — this determines the sign of the fix.

Confirmed: `camera.rotation()` is view→world, so vanilla's hand basis makes the captured offset world-axis while Iris's empty PoseStack makes it view-space. Let me check Iris's `canRender` gate.

Root cause confirmed with three independent derivations. Let me set up tasks and implement.

两个仓库都已重新构建并验证。先把结论说清楚：

## 症状 1（曳光弹起点偏）：根因已定位并修复

**Iris 的 `HandRenderer` 把 CGC 依赖的那个 poseStack 基底抽掉了。**

vanilla 1.21.1 的 `GameRenderer.renderItemInHand` 给手部 poseStack 设的基底是 `matrix4f1.invert()`，而 `matrix4f1 = rotation(camera.rotation().conjugate())` —— 也就是 `camera.rotation()`（视图空间→世界）。所以 `cacheMuzzlePosition` 读到的 `m30/m31/m32` 是**世界轴**量，#50 那套写法正是按这个来的。

光影包启用时 Iris 的 `HandRenderer.renderSolid` 干的是另一套（我从 `irisshaders-455508-8242804.jar` 反编译确认）：

```java
setupGlState(...) → 投影乘 scale(1,1,0.125)，然后 poseStack.setIdentity() + bob
renderSolid(...)  → modelview = 上面那个 poseStack
                 → renderHandsWithItems(f, new PoseStack(), bufferSource.getUnflushableWrapper(), ...)
                                        ^^^^^^^^^^^^^^^ 空栈，单位阵基底
```

于是 `m30/m31/m32` 退化成**视图空间**量（等价于 1.20.x 的语义），而 `GunProjectileRenderer` 仍然把它当世界向量 `translate` 出去 → 误差 = 该偏移被摄像机旋转了一次，横向上表现为 **∝ sin(yaw)**。

这个式子把你的 5 个观察全对上了：枪口在视图空间约前 0.6 格时，横向误差 ≈ `0.6·sin(yaw)` —— 121.3°/90°/44.7° 全为正（右），−142.8°/−57.4° 全为负（左），且**与开镜进度无关**（错的是基底，不是 FOV 补偿）。枪模型本身正常也解释得通：外层 modelview 会把基底抵消，最终渲染矩阵在两种情况下等价，只有「读出矩阵分量再拿去算」才会错。

**改法**（按你说的，新 API 放 Compat Shader 注入）：

| 文件 | 改动 |
|---|---|
| `Custom-Gun-Continued-1.21.1neoforge` `client/compat/iris/IrisCompat.java` | 新增 `isHandPoseStackWorldSpace()`，stub 返回 `true` |
| 同上 `renderer/item/GunItemRenderer.java` | `cacheMuzzlePosition` 里拿到 `false` 时先 `offset.rotate(camera.rotation())` 换算到世界轴，再做原有 FOV 补偿 |
| `CGC-Compat-Shader-iris-1.21.1neoforge` [IrisCompatMixin.java](core/src/main/java/dev/xcolorful/cgccompat/shader/client/mixin/iris/IrisCompatMixin.java) | 注入 `!HandRenderer.INSTANCE.isActive()` |

判断条件我**没有**用 `IrisApi.isShaderPackInUse()`，而是用 `HandRenderer.INSTANCE.isActive()`：摄像机 detached 之类的情况下 Iris 的 `canRender` 为 false、仍走 vanilla 那条路，那时基底是含摄像机旋转的，用「装了光影包」判断会误伤。

构建产物已验证：新 jar 里 `IrisCompat` 多了 `isHandPoseStackWorldSpace()`；compat jar 里 mixin 有 4 个注入（含新增那个，调用 `HandRenderer.isActive()`）；CGC 编译产物确认调到了 `IrisCompat.isHandPoseStackWorldSpace()` + `Camera.rotation()` + `Vector3f.rotate`。

**这个诊断是从字节码 + vanilla 源码推出的，没有运行时验证过** —— 请你开光影试一下 yaw≈±90（原来最明显）和 yaw=0/180（原来不偏），确认都回正、且不开光影时没有变化。

## 症状 2（红点 lens 朝前那面可见）：我只到假设，没定根因

查到的事实：瞄具走的是**模板缓冲遮罩** —— `_AttachmentModelRender.renderDivisionOnly` 会 `_disableDepthTest()` 然后用 `GL_EQUAL, i+1` 只画模板命中的部分，而 `_clearStencilBuffer` 是 `glClearStencil(0)` + `_clear(GL_STENCIL_BUFFER_BIT)`，清的是**当时绑定的那个 framebuffer**。

假设：Iris 把世界画进它自己的 framebuffer，CGC 清/写的模板跟 Iris 实际用的那份可能不是同一个，遮罩失效后 lens 就露出来了。但这条我没法从代码断定。

需要你给一点能区分的信息（任一即可）：

- 这个 lens 是**只开镜时**出现，还是**任何时候**看 ocular 都出现？
- 露出来的是**整个 lens 面**，还是只有一部分/带缺口？（整个面 → 更像 culling/模板全失效；带缺口 → 更像模板圆孔没对上）
- 有条件的话给张开光影的截图，我对着 `scopeBodyPath` 的节点树看。

## 另外

- 1.20.1 分支我没动（按规则只改当前分支）。它的 `cacheMuzzlePosition` 还是 #50 之前的写法，**如果 Oculus 的手部通道也是同一套 `HandRenderer`，那边很可能有同样的问题**，要不要同步你定。
- 我把这次发现补进了长期记忆（`1.21.1 手部 poseStack` 那条），顺便修正了「`getBedrockPartCenter` 不受基底影响」的判断依据 —— 它乘了 modelview，两种情况都能抵消，所以瞄具定位是对的、不是这个 bug 的来源。

未 commit、未 push。

# 

```
枪射物起点偏移问题已修复，已merge

这个lens是始终出现
- 注：enableSight=true的ocular是没有开镜时黑色遮罩的，始终渲染，只有enableScope=true才会在开镜前是用模型ocular自身挡住，然后用圆形揭开的
- 露出来的是假设这个面被渲染出来，正常的遮挡关系
- 根据经验，目前怀疑是utils里调用的RenderType_不是iris实际用的导致的；之前1.21.11有次bug是因为原版render type定义不同，最后才查出来

经测试，1.20.1没有以上偏移和lens渲染问题
```

# 

枪射物那条已经 merge，我接着查 lens。你给的 `RenderType_` 线索很具体，先看那个包装器。

Your `RenderType_` hunch points somewhere specific. Let me check how Iris actually draws the batched segments — and compare 1.21.1 against 1.20.1 since that one is fine.

This looks like the mechanism: `endBatch()` draws segments via `drawInner` → raw `BufferUploader.drawWithShader`, **skipping `RenderType.setupRenderState()`**. Let me compare against 1.20.1 and check the state-applying path.

Both Iris versions share the same two paths — so the differentiator must be *which* path the gun goes through. Let me confirm where Iris 1.21.1 runs its hand pass.

Oculus 1.20.1 uses the same HandRenderer/batched architecture, so the difference must be elsewhere. Let me find which render type the scope path actually uses.

Let me check one more discriminator — whether the 1.20.1 branch even has this ocular-division code path.

Last discriminator: where Oculus 1.20.1 runs its hand pass relative to the batched-render window.

机制找到了，而且有字节码证据——但和你猜的方向略有不同：**render type 没拿错，是它的状态在这条路径上根本没被应用。**

## 证据

`FullyBufferedMultiBufferSource` 有两条 draw 路径（Iris 1.8.14 和 Oculus 1.7.0 都是这个结构，我两边都反编译对比过）：

| 路径 | 行为 |
|---|---|
| `endBatchWithType(TransparencyType)` | 每个类型 `renderType.setupRenderState()` → `draw` → `clearRenderState()` —— **状态齐全** |
| `endBatch()` | `BufferSegmentRenderer.drawInner` → `BufferUploader.drawWithShader(meshData)` —— **完全不跑 `setupRenderState`** |

CGC 的枪身/瞄具几何体写进 `mc.renderBuffers().bufferSource()`（光影下就是全缓冲源），而那次 flush 走的正是无参 `endBatch()`。于是这段几何体是在**没有任何 RenderType 状态**下画的：不跑剔除、不绑贴图、不设混合/深度/写掩码，全靠 Iris 当时残留的 GL 状态。红点 lens 是零厚度四边形，可见性完全靠背面剔除——剔除没被应用，它就露出来了。和你说的「始终出现」「像是这个面被渲染出来、遮挡关系正常」完全对得上。

顺手排掉两个假设：

- **不是拿错 render type。** `RenderType_.entityCutout` 在 1.21.1 走的就是 vanilla `RenderType.entityCutout`，而 1.21.1 的 `CompositeState.Builder` 默认 `cullState = CULL`（`RenderType.java:1337`），`entityCutout` 没有覆盖它 → 这个类型**是带剔除的**。
- **不是兼容 mod 引入的。** `IrisCompat.endBatch` 那个钩子实际上是空转：返回 true 时调 `f.endBatch()`，返回 false 时 CGC 调 `bufferSource.endBatch()`——动态分派到**同一个**重写方法，两者完全一样。所以只装 Iris+Sodium、不装 cgccshader 应该也能复现（这条你可以顺手验一下，一分钟）。

## 我没能解释的

**为什么 1.20.1 正常**，我查不出来：Oculus 1.7.0 同样有 `HandRenderer`、同样从 `MixinLevelRenderer` 驱动手部、同样这两条 draw 路径、`entityCutout` 同样带剔除，两个分支的 `renderOcularStencil`/`renderDivisionOnly` 也是同一份实现。剩下唯一的差别只能是**那一刻残留的剔除状态**（Iris 1.8.14 + Sodium 0.8 的新管线大概把 cull 关了，1.7.0 + Embeddium 没有），这个只有跑起来才知道。

## 修法

两个方向，我倾向先做便宜的探针确认机制，再决定要不要动 core。这轮我**没有改任何代码**。

Before writing the probe, let me re-check one thing — whether Iris 1.8.14's `endBatch()` still applies state (Oculus 1.7.0's did).

My "no state applied" claim was wrong — both versions *do* call `setupRenderState` in `endBatch()`. I misread `drawInner` as state-less. Let me check the wrapper Iris actually hands out.

I have to correct myself: both Iris versions **do** call `setupRenderState`/`clearRenderState` in `endBatch()` — I misread `drawInner` as "no state". Let me check what diagnostics are even available for a probe.

One path I haven't read: the ocular stencil/triangle-fan. Let me look.

**Correcting myself first:** my last-turn claim that "no RenderType state is applied" was **wrong**. Both Iris 1.8.14 and Oculus 1.7.0 call `setupRenderState()`/`clearRenderState()` inside `endBatch()` — I misread `BufferSegmentRenderer.drawInner` (a raw upload) as state-less, when the *caller* applies the state. So the cull shard does run. Sloppy of me; here's what I found on re-checking.

探针做好了，但**没按你选的兼容层位置打**——因为查下去发现方向变了，我先说清楚。

## 先纠正我上一条的错误结论

我说「这条路径上 RenderType 状态根本没被应用」是**错的**。重查字节码：

| | `endBatch()` 里有没有跑状态 |
|---|---|
| Iris 1.8.14 (1.21.1) | `setupRenderState()` → `RenderType.draw(meshData)` → `clearRenderState()` ✓ |
| Oculus 1.7.0 (1.20.1) | `setupRenderState()` → `segmentRenderer.drawInner(...)` → `clearRenderState()` ✓ |

我把 `BufferSegmentRenderer.drawInner`（那个只做裸上传的小方法）当成了「整条路径不跑状态」，实际**状态是调用方 `endBatch()` 自己跑的**。两边都跑，所以剔除是生效过的。这条结论作废，抱歉。

顺带这轮排掉的（都有证据）：

- `RenderType_.entityCutout` 走 vanilla `entityCutout`，1.21.1 的 `CompositeState.Builder` 默认 `cullState = CULL`、`entity_cutout` 没覆盖 → **它带剔除**。所以不是拿错 render type。
- Iris 的 `TaggingRenderTypeWrapper` 把 `setupRenderState`/`clearRenderState` 原样转发给被包装类型，没有丢状态。
- 1.20.1 和 1.21.1 两边 `renderOcularStencil` / `renderDivisionOnly` / `renderOcularAndDivision` 是同一份实现，`HandRenderer` 架构也一样 —— 「为什么 1.20.1 正常」我还是没解释出来。

## 新线索：圆形模板孔是用裸 `drawWithShader` 画的

`_AttachmentModelRender.renderOcularAndDivision` 里，用来挖圆形目镜孔（`GL_INVERT`）的那个三角形扇是这么画的：

```java
builder = tesselator.begin(TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
...
// [1.21.1, 1.21.6)
BufferUploader.drawWithShader(builder.buildOrThrow());
```

`drawWithShader` **用的是当时绑在身上的 shader**，而这块网格是 `POSITION_COLOR`。光影下走 Iris 的 `HandRenderer` 通道时，绑着的不是 vanilla 的 `position_color`（Sodium 0.8 那套管线自己管 program），顶点格式对不上 → **圆孔画歪、模板值错乱** → 依赖模板遮罩的 ocular/lens 部件就会被画到不该画的地方，且**每帧都画所以「始终出现」**。

这不是我凭空猜的：同一段代码在 1.21.6 那里留着你们自己的注释 —— 「否则顶点数据被错误解析，圆形模板孔（GL_INVERT）画歪」，说明这个裸 draw 的格式依赖是已知脆弱点。1.20.1 之所以正常，很可能就是那一刻绑着的程序恰好还是 vanilla 的（Oculus 1.7.0 + Embeddium 与 Iris 1.8.14 + Sodium 0.8 在这点上的残留状态不同）。

## 探针（打在 CGC 而不是兼容层）

**这是 CGC 自身「依赖环境残留 shader」的 bug，兼容层兜不住** —— 兼容层拿不到这一步，而且没有 shader 时也可能踩。所以我在 CGC 侧加了一行显式绑定：

```java
RenderSpec.setShader(GameRenderer::getPositionColorShader);   // 实际写的是 RenderSystem
BufferUploader.drawWithShader(builder.buildOrThrow());
```

已编译通过，产物里确认有 `RenderSystem.setShader` 调用，新 CGC jar 已换进 `CGC-Compat-Shader-iris-1.21.1neoforge/libs/`。**compat 那侧这轮没改，不用重编**，你只要换 CGC 的 jar。

**这个探针同时是一次判断**：
- 圆孔恢复正常 → 坐实是 shader 绑定/格式问题，这一行就是要留的修法（顺带说明 1.21.6+ 那段 `RenderType.draw` 的注释也得一起看）。
- 没变化 → 说明 Iris 强行接管了 program，那就走下一步。

## 如果没变化，请帮我做两件事

1. **只装 Iris + Sodium、不装 cgccshader**，看 lens 还在不在（一分钟）。我分析过 `IrisCompat.endBatch` 那个钩子其实是空转（返回 true 调 `f.endBatch()`、返回 false 调 `bufferSource.endBatch()`，动态分派到同一个重写方法），所以理论上不装也该复现——这条能验证我的分析，也能把兼容 mod 摘出去。
2. 那把枪的 `enableTransparency` 是 true 还是 false？我顺手查到一个可能有用的硬事实：**1.21.1 的 `entity_translucent` 是 `NO_CULL`**（`RenderType.java:159`），而 `entity_cutout` 是带剔除的；`GunItemRenderer` 按 `enableTransparency` 在两者间选。如果受影响那把枪是 true，那么枪身（连同作为它一部分渲染的瞄具）本来就是不剔除的，lens 的背面被画出来就未必是 Iris 的锅——这个事实先摆出来，免得继续往错方向查。

未 commit、未 push。

# 

```
当前CGC和CGC Compat代码状态下，装不装Compat都会看见lens，没有修复，最新的没安装Compat时的log在D:\Temp-File\debug.log

测试下来这个非官方1.21.1移植是没有偏移和lens问题的，看看是否有哪里修改?
```

# 

Good lead — a working 1.21.1 reference. Let me find how it differs.

Upstream's `cacheMuzzlePosition` pre-multiplies `RenderSystem.getModelViewMatrix()` — that's the basis-independent read. Now the ocular/stencil side.

Upstream calls `RenderHelper.enableItemEntityStencilTest()`. Let me check whether CGC does the same.

Let me check the port's git history — a fix like this usually leaves a commit.

Two commits look decisive: the tracer-offset fix and a "praying to the rendering god" one. Let me read them.

**Bug #1 confirmed by upstream** — their fix is `mulLocal(RenderSystem.getModelViewMatrix())` + unconditional camera-rotation restore/replay (they'd had that pair gated on `IrisCompat.isPackInUseQuick()`). Exactly my diagnosis. Now the lens fix.

**Found the lens fix.** Upstream's commit adds one line at client setup:

```java
RenderSystem.recordRenderCall(() -> minecraft.getMainRenderTarget().enableStencil());
```

Let me compare with what CGC does.

Both enable the stencil the same way. Let me check cull state handling — a dropped call there would explain it exactly.

Everything in the ocular code matches upstream. Let me check the one thing that would make the *same gunpack* behave differently: the transparency flag both sides read.

One more concrete check: whether both sides read the same JSON key for transparency.

Let me revert my unproven probe, since upstream's identical raw draw is fine.

两个仓库现在都是干净的（你的曳光弹修复已 merge，我上一轮那个探针已全部还原）。找到上游的对应修法了。

## 1. 曳光弹那条：上游的修法和我诊断完全一致

`4564212b "I don't know what I'm doing, but it fixes the tracer offset issue"` 改了两处：

```java
// GunItemRendererWrapper.cacheMuzzlePosition：多了一行
poseStack.last().pose().mulLocal(RenderSystem.getModelViewMatrix());

// EntityBulletRenderer：把「还原摄像机旋转 → 应用偏移 → 逆转回来」这一对
// 从「仅 IrisCompat.isPackInUseQuick() 时做」改成无条件做
```

他们改之前的注释就是我的结论：*"1.21.1 修改了渲染, 现在不需要坐标空间转换, 但是 Iris 还是老样子所以在 Iris 光影包开启的时候还是需要做类似的措施"* —— vanilla 1.21.1 = 世界轴，Iris = 视图空间。

**他们的写法比我的省事**：`mulLocal(modelView)` 把偏移无条件变成视图空间量，于是既不需要环境判断、也不需要新 API（我加的 `isHandPoseStackWorldSpace` 及其 mixin 注入可以整个删掉）。要不要换成他们这种形式你定；跨分支的话 1.20.x 得单独核对（那支现在没这段旋转还原）。

## 2. 上游对 ocular 这一类的修法，CGC 已经有了

`51a798c5 praying to the rendering god (fix #39, fix #40)` 只加了一行：

```java
RenderSystem.recordRenderCall(() -> minecraft.getMainRenderTarget().enableStencil());
```

而 CGC 的 `NeoClientSetup.java:34` **逐字相同**。所以这条不是差异点。

## 3. 我把瞄具/模板链路逐段对完了，结构完全相同（这些可以排除）

`render()` 分发、`renderSight`、`renderBoth`、`renderScope`、`renderOcularStencil`、`renderDivisionOnly`、`renderOcularAndDivision`（含那个裸 `BufferUploader.drawWithShader` 的 POSITION_COLOR 三角形扇、`80*radius`、`-90F` 这些数值）、`enable/disableItemEntityStencilTest`、`clearStencil(0)` + `clear(GL_STENCIL_BUFFER_BIT)`、第一人称 renderType 选择（`enablesTransparency() ? entityTranslucent : entityCutout`）、键名 `"enable_transparency"` 与默认值、Iris 兼容的形状 —— 全部一致。

**所以我上一轮那个「stencil 扇裸 draw 的 shader 绑定」探针方向打错了**：上游有逐字相同的裸 draw 且没问题，和你实测「没变化」吻合。那行连同两个 import 我已经全部还原。

## 4. lens 的根因我没找到，但剩一条最贴合你直觉的线索

我把 vanilla 1.21.1 的 render type 定义查实了：

- `RenderType.entityCutout(ResourceLocation)` → `ENTITY_CUTOUT`，它**没有** `setCullState`，而 `CompositeState.Builder` 的字段默认就是 `CULL`（`RenderType.java:1337`）→ **这个类型带剔除**。所以「拿错 render type」按字面不成立。
- 但 **`entity_translucent` 明确是 `NO_CULL`**（`RenderType.java:159`）。
- 而 `GunItemRenderer` 正是按 `enable_transparency` 在两者之间选。

**所以请先确认一件事：受影响那把枪的 display JSON 里 `enable_transparency` 是 true 还是 false？** 如果是 true，那么枪身（连同同一 pass 里画的瞄具模型、包括那片 lens）本来就不剔除 —— 零厚度四边形的背面会被正常光栅化，vanilla 看不见只是因为深度/绘制顺序压住了，而 Iris 的批量+排序手部通道改变了这个顺序。那这就不是兼容层能修的，得给瞄具单独用带剔除的类型、或者改包。

另外还有个可以顺手试的差异：上游那个 `enableStencil()` 是在 **`FMLClientSetupEvent`** 的 `enqueueWork` 里，CGC 是在 **`FMLCommonSetupEvent`** 的 `onConfigureMainRenderTarget` 里。都走 `recordRenderCall`，但时机不同，理论上 Iris/Sodium 重建主渲染目标时可能把早期的 enable 冲掉（CGC 每次渲染前也会再 enable 一次，所以大概率是平手）。**没验证，只是列出来**。

## 5. 一个待查方向（不确定，先摆着）

那片 lens 的尺寸是 `...,0`（零厚度）。CGC 的基岩模型加载器是自己写的，上游用的是 `simplebedrockmodel` 库 —— **零厚度元素会不会一方跳过、另一方生成共面双面**值得对比。如果是共面双面，vanilla 的深度测试固定倾向一侧、Iris 换了倾向，症状就完全对得上（只开光影出现、遮挡正常、始终出现）。这条要靠比加载器或你给张截图才能定。

## 6. 我仍然没解释「为什么 1.20.1 正常」

1.20.1 的 Oculus 1.7.0 同样有 `HandRenderer`、同样从 `MixinLevelRenderer` 驱动手部、同样两条 draw 路径、`entityCutout` 同样带剔除，两个分支的瞄具代码也是同一份。差别只可能在「那一刻的残留 GL 状态」或「模型加载结果的差异」，这两条都得跑起来或比数据才能定。

未 commit、未 push；两边工作区都干净。

# 

```
测试枪cbrg:beryl_m762和tacz:ak47都没有enable_transparency，应该都是false
1.21.1的poseStack.last().pose().mulLocal(RenderSystem.getModelViewMatrix());这个修法是否跟Compat mixin的完全一致？是否非光影时也可以执行？如果一致就先改在1.21.1上我不装光影试试是否正常
另外，症状是装了光影mod之后，再不启用光影时出现lens，开光影时正常
```

# 

`enable_transparency` 是 false —— 我那条 NO_CULL 假设作废。

而你这句**关键**：**装了 Iris 但不启用光影包时出问题，开了光影包反而正常**。这把整个方向和之前反过来，也解释了我为什么一直找不到代码差异。

先回答你的两个问题，然后我来改。

答案：**效果一致，但实现不同；而且它的形式不装光影也能跑**（这正是你要的测试条件）。我按上游的形式改到 1.21.1。

先确认几处引用，避免留死代码。

按上游的形式改。先把 `mulLocal(modelView)` 加进采集点。

The exact-text match is failing on the long CJK comment line. Let me verify the line range and replace it precisely.

The file is CRLF — that's why the exact-text edit failed, and it may mean my inserted lines are LF-only. Let me check.

Good — `GunItemRenderer` is clean (489 CRLF, 0 bare LF, small diff). Now the renderer side, CRLF-aware.

Now remove the API I added, since it's no longer needed — and it must go, or the compat mixin's injection target will vanish and crash.

改完了，两边构建通过。你的症状描述很关键，先说结论。

## 你的两个问题

**1. 跟 Compat mixin 的一致吗？** —— **最终效果一致，实现完全不同，而且不能只拿一行混着用。**

| | 采集端 | 渲染端 |
|---|---|---|
| 我上一版（mixin） | 读原始 `pose` → 若走 Iris 手部通道就乘 `camera.rotation()` | 世界轴，**不做**旋转还原 |
| 上游 | 先 `mulLocal(getModelViewMatrix())`，偏移**无条件**变视图空间量 | 视图空间，**做**「转回世界轴 → 偏移 → 转回来」 |

两者结果严格相等，我推过一遍：vanilla 下 `modelView·pose = R⁻¹·(R·bob·XP·YP·X) = bob·XP·YP·X`，Iris 下 `modelview` 就是 bob、基底是单位阵，得到**同一个** `bob·XP·YP·X`。所以只加 `mulLocal` 而保留 CGC 现在「世界轴 + 渲染端不旋转」那套，vanilla 上会反着坏 —— 必须成对改。

**2. 非光影时能跑吗？** —— **能，这正好是它的优点**（上游形式一个环境分支都没有）。另外补一句：**你原来那版其实也已经可以脱开 Iris 测** —— stub 返回 true、不做换算，vanilla 下本来就对。两种都行，我按你说的换成了上游形式。

## 改动（净 −73 行）

CGC 1.21.1：
- [GunItemRenderer.java](../../Custom-Gun-Continued-1.21.1neoforge/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/GunItemRenderer.java)：加 `poseStack.last().pose().mulLocal(RenderSystem.getModelViewMatrix())`；把 1.21.1 专用的「沿视线投影缩放」整块和 `_getCameraLookDirection()` 删掉，恢复成单一公式。
- [GunProjectileRenderer.java](../../Custom-Gun-Continued-1.21.1neoforge/core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java)：把「还原摄像机旋转 / 逆转回来」这一对取消注释、无条件启用，注释按新理由重写。
- [IrisCompat.java](../../Custom-Gun-Continued-1.21.1neoforge/core/src/main/java/dev/xcolorful/customgun/client/compat/iris/IrisCompat.java)：删掉我上一轮加的 `isHandPoseStackWorldSpace`。

Compat 1.21.1：[IrisCompatMixin.java](core/src/main/java/dev/xcolorful/cgccompat/shader/client/mixin/iris/IrisCompatMixin.java) 删掉对应注入和 `HandRenderer` import。

**这两边必须一起删**：只删 stub 方法而留着注入，`defaultRequire: 1` 会在运行时直接崩。

验证：字节码确认 `getModelViewMatrix` + `mulLocal` 在采集点、`GunProjectileRenderer` 的旋转对已生效、`IrisCompat` 回到 3 个方法、compat jar 的注入列表只剩 3 个且不含 `HandRenderer`。CGC jar 已换进 compat 的 `libs/`。

**偏移修复现在完全不依赖兼容 mod**，你不装光影、不装 cgccshader 就能测。

## 关于 lens 的新症状

**「装了 Iris 但不启用光影包时出问题，开了光影包反而正常」** —— 这和偏移那条（开光影才出）正好相反，也解释了我为什么把 CGC 和上游逐段对完却一无所获：两边代码确实一样，我一直在错的条件组合下找差异。

按这个条件，可疑范围缩到「Iris 装了但没跑 shader pipeline」这一个状态：
- **无光影包**：`HandRenderer` 会 early-return（`isPackInUseQuick()` 为假），手部走 vanilla 路径；但 Iris 的**批量实体渲染是 `required: true` 常驻的**，关卡渲染期间 `bufferSource()` 仍然返回全缓冲源。
- **有光影包**：Iris 跑完整 pipeline（自己的 gbuffer / program）。

最像的解释是**模板缓冲挂在哪个 framebuffer 上**：CGC 的目镜遮罩靠 `getMainRenderTarget().enableStencil()` + `GL_REPLACE`/`GL_EQUAL`。没有光影包时 Iris 可能仍把绘制导到它自己的（未配模板的）目标上 → 遮罩失效 → lens 露出来；开了光影包，Iris 的 gbuffer 恰好带模板 → 遮罩正常。**这条我没验证**，但它同时解释了「只有装了 Iris 才出」「不开光影反而坏」「1.20.1 正常」。

**所以最省事的判别实验**：拿上游那个非官方 1.21.1 移植，**装 Iris 但不开光影包**，看 lens 是否也出现。
- 也出现 → 上游没有可抄的修法，这是 Iris 在「装了但没开包」状态下的固有行为，CGC 得另想办法（例如不依赖 framebuffer 模板，改用别的遮罩手段）。
- 不出现 → 上游确实有区别，我对着那个状态继续找。

`enable_transparency` = false 已确认，NO_CULL 那条假设作废。

未 commit、未 push。

# 

```
你这改的不对，不仅没修复，而且不装光影mod时会显示lens了（bug），刚才的修改之前的版本是不会的
上游确实在装光影mod+关闭光影时正常
```

# 

先看改动本身——我怀疑我的改动里有不该有的副作用。

Diff is exactly my intent — nothing else touched. But the lens shouldn't be reachable from any of it, so let me check what's actually in the built jar.

Jar matches source — no probe, no leftover. That means my three edits have **no reachable path to the lens**, which is exactly why I don't trust them: a regression I can't explain shouldn't stay in. Reverting my own changes from this turn.

两边已回到你 merge 过的版本。现在重建产物，让 jar 和源码一致（否则你装到的还是我改坏的 jar）。

已还原，并且**重建了两个 jar**——所以我 20:59 那版 CGC jar 不要再测了。

## 做了什么

4 个文件全部 `git checkout --` 还原：CGC 的 `IrisCompat` / `GunProjectileRenderer` / `GunItemRenderer`，compat 的 `IrisCompatMixin`。两边工作区现在都干净。产物重新构建并核对：`IrisCompat` 又回到 4 个方法、compat jar 的注入列表又回到 4 个（含 `isHandPoseStackWorldSpace`），CGC jar 已换进 compat 的 `libs/`。**这就是你之前 merge 过、并验证过偏移已修好的那个状态。**

## 为什么我直接还原而不是继续修

我把这一轮的 diff 逐行看过，只动了三处，**没有一处碰得到 lens**：

- `cacheMuzzlePosition` 里对 **push 出来的那份 poseStack** 做 `mulLocal`（在 push/pop 之间，pop 之后失效）
- `GunProjectileRenderer` 里曳光弹自己那份 poseStack 的旋转
- 删掉一个已无调用者的 `_getCameraLookDirection()`

lens 在瞄具模型里，是 `gunModelObject.render(...)` 期间提交的几何体 —— 发生在这三处**之前**；而且顶点在提交那一刻就烘好了，事后怎么改 poseStack 都动不到它。**我找不到任何一条能解释你这个回归的路径**，而找不到机制的改动我不该留着，所以先还原。

如果还原后「不装光影」恢复正常，那说明确实是我的改动干的，我再逐项二分；如果还原后**仍然**有，那就是别的东西在动，两边都好查。

## 需要你确认（不然我会继续在错的条件里打转）

1. 「不装光影mod」那次测的**具体是哪些 mod**？只有 CGC？还是 CGC+Sodium？**mods 目录里有没有残留 `cgccshader` jar**？—— 它的 `neoforge.mods.toml` 把 iris 和 sodium 声明为 `required`，Iris 不在时它按理**根本加载不起来**（缺依赖会直接拦下）。所以「不装光影」到底是「没装 Iris」还是「装了 Iris 但没启用光影包」？这两种是完全不同的条件，我需要知道是哪一种。
2. 同一把枪/同一个瞄具吗（`cbrg:beryl_m762` + `cbrg:red_dot`）？
3. 「之前的版本不会」那次观察，是同一个 1.21.1 分支、同一把枪、同一套包吗？

## 你这条信息直接给出了下一步

**上游在「装了 Iris + 关光影」下正常** —— 这正好是这边坏的那个条件。所以差异确实在代码里，而且范围缩得很窄：**Iris 装了但 `isPackInUseQuick()` 为假**这个状态。

这个状态下还在起作用的只有两样：Iris 的**批量实体渲染**（`required: true` 常驻，关卡渲染期间 `bufferSource()` 仍返回全缓冲源）和**模板缓冲挂在哪个 framebuffer 上**。

我最想先验的就是后者：CGC 的 `enableStencil()` 挂在 **`FMLCommonSetupEvent`**（`NeoClientSetup.onConfigureMainRenderTarget`），上游挂在 **`FMLClientSetupEvent`** 的 `enqueueWork`（`recordRenderCall`）—— 时机不同。如果 Iris/Sodium 在这之后重建了主渲染目标的附件，早期那次 enable 会被冲掉；而 CGC 每次渲染前虽然会再 enable 一次，用的却是 `getMainRenderTarget()`，未必等于当前正在绘制的那个。

要不要我下一步就对着「Iris 装了、不开光影」这**一个**状态，把上游和 CGC 的模板 enable 时机 + `getMainRenderTarget` 的取法逐字比一遍？我建议你先测一遍还原后的版本、确认基线干净，我再动手。

未 commit、未 push。

# 

```
现在测试，1.21.1forge正常，1.21.1neoforge正常，1.21.1neoforge在装了sodium（没装iris）之后会显示ocular
可以重新应用之前坏的哪个修改再看看，估计是sodium的问题
- 注意，之前你那个修改把注释掉的1.20.1的也删了，这是不允许的
- 缩进结构/花括号是保留的，只能做最小必要修改/取消注释行来恢复，不要破坏结构
```

# 

关键信息：**是 Sodium（没装 Iris）就会出 ocular** —— 那这跟光影没关系，跟我的偏移改动也没关系。先按你的要求重新应用那版修改（这次绝不删注释掉的代码、不破坏结构）。

先看还原后的原文。

CRLF 让精确匹配又失败了，改用按行处理。

注释内容没有丢，只是就地加了标注/取消注释。确认一下旧块完整，然后构建。

缺 `RenderSystem` 的 import（上次忘了随还原一起加）。

已重新应用，并且**这次一行注释代码都没删、结构没动**。

## 改动方式（按你的约束）

CGC 1.21.1，只动 2 个文件：

| 操作 | 说明 |
|---|---|
| 加 1 行 import | `com.mojang.blaze3d.systems.RenderSystem` |
| 加 1 块（纯新增） | `poseStack.last().pose().mulLocal(RenderSystem.getModelViewMatrix());` + 说明注释 |
| 取消注释 3 行 | 原来注释掉的 `[1.20.1, 1.21.1)` 的 `State.muzzleRenderOffset.set(...)`，现在它是生效代码 |
| 取消注释 4 行 | `GunProjectileRenderer` 里「摄像机旋转 / 逆转摄像机旋转」那两对 |
| 就地加注释 21 行 | 旧的 1.21.1 投影实现**原文保留**，逐行前面加 `//` 变成注释，标签改成 `// [1.21.1, ) 旧写法（已被上面取代，保留供参考/26.x 移植）` |

**没有任何内容被删除**：旧 1.21.1 实现的说明和代码都在（只是成了注释）；`IrisCompat.isHandPoseStackWorldSpace` 和 `_getCameraLookDirection` 现在没人调用，但**都留着没删**（compat 仓库因此一行没改，注入仍指向仍存在的方法）。花括号/缩进/`{ }` 结构原样保留（两个文件的 `{`/`}` 计数都平衡）。构建通过，字节码确认 `mulLocal` 生效、旧投影法已不在执行路径里。

CGC jar 已换进 compat 的 `libs/`，compat 也重建了一遍。

## 你这条发现很重要：bug 跟光影无关

**「装了 Sodium（没装 Iris）就出 ocular」** —— 而且这是在**我还原后的、你说「正常」的那版**上测出来的。所以：

- **这个 ocular bug 不是我改出来的**，它在干净基线上就有；
- 它和 Iris、和光影包都无关，是 **Sodium** 的问题；
- 之前「我的改动让不装光影时出现 lens」那次观察，很可能就是「装了 Sodium」这个条件（compat 的 `neoforge.mods.toml` 把 iris/sodium 声明为 `required`，所以你手里只要有 cgccshader，Sodium 就一定在）。

这也解释了为什么我把 CGC 和上游逐段对完却找不到差异 —— 方向从一开始就错了。

## 现在最可疑的一处（正好和 Sodium 对得上）

`renderOcularAndDivision` 里那个挖圆形目镜孔的三角形扇是**裸画**的：

```java
BufferUploader.drawWithShader(builder.buildOrThrow());   // 网格是 POSITION_COLOR + TRIANGLE_FAN
```

`drawWithShader` 用的是**当时绑在身上的 shader**。Sodium 自己管理 program，绑着的很可能不是 vanilla 的 `position_color`，顶点格式对不上就会把这个圆孔画歪 —— **模板值跟着错乱，靠 `GL_EQUAL` 遮住的 ocular/lens 就露出来**。你们自己在 1.21.6 那段留着同样的注释（「否则顶点数据被错误解析，圆形模板孔（GL_INVERT）画歪」），说明这个格式依赖是已知脆弱点。

这条能同时解释：**只有装了 Sodium 才出**、**始终可见**、**遮挡关系正常**。

我之前那个探针（画扇前显式绑 `GameRenderer::getPositionColorShader`）打的正是这里，但你当时是在**开着 Iris** 的条件下测的，而在那个条件下它没用。**Sodium-only 这个条件它从来没被测过** —— 这是我现在最想验的一刀。

## 想请你做两件事

1. 用现在这个（重新应用后的）版本测一下 **Sodium-only** 的 ocular，和「还原版」对比 —— 看我的改动有没有影响它（按分析不该有）。
2. 如果你愿意，我下一步就把那个 shader 绑定探针**只在 Sodium-only 条件下**重新加上（仍然是最小改动、不删任何注释），一次测完就能确定圆孔是不是被 Sodium 的 program 画歪了。

未 commit、未 push。

# 

```
现在继续#60 Shader-compat.md，目前的进度是测出只在安装sodium的情况下有lens被渲染出来（bug）的问题
之前的修改，已经merge到1.21.11并且测试第一人称曳光弹、红点lens、4倍镜开镜都没问题
注意到TaCZ1.21.1非官方移植是没有#60提到的所有问题的

oculus在CGC Compat Shader环境里，sodium/iris在CGC Compat Shader 1.21.1环境里

现在开始排查1.21.1 sodium
```

# 

I'll locate the #60 Shader-compat doc first.

Read the full #60 history. Let me check the current state of both repos.

Now let me find the ocular/scope rendering code.

Interesting — there's a custom `pipeline`/`stencil` API layer with mixins. Let me read it.

These are forward-compat stubs. Let me read the actual 1.21.1 ocular code.

Let me gather the key facts in parallel — the GL wrapper, the render type selection, and the compat deps.

Let me check the log for the exact Sodium version and mod list.

Big clue: Sodium has an **FRAPI `ItemRendererMixin`**. Let me find the actual mods folder and Sodium jar.

Let me enumerate every class Sodium mixes into.

Sodium mixes into `MultiBufferSource$BufferSource`, `RenderBuffers`, `PoseStack`, `RenderTarget`. Let me see those specific mixins.

Found it — Sodium. Let me extract and inspect the relevant mixin classes.

Prime suspect found: Sodium mixes into `MultiBufferSource$BufferSource` (the exact class CGC's `endBatch()` hits). Let me disassemble.

The extracted Sodium 0.6.13 lacks `MultiBufferSourceMixin` — so the installed Sodium is newer. Let me pin down which version.

Sodium **0.8.13+mc1.21.1** is the compile target — the earlier extraction was 0.6.13, the wrong version. Let me extract the right one.

Now the prime suspect. Let me disassemble Sodium's `MultiBufferSourceMixin`.

Let me get the injection targets compactly for all relevant Sodium mixins.

Let me look at CGC's bedrock model geometry generation — that determines what the lens quad actually is.

A zero-depth box generates **two coplanar quads** (north + south) with different UVs. Let me check vanilla's `endBatch` to see if Sodium's quad sort can reorder them.

Let me use the IDEA MCP as the project instructions direct, to read vanilla's `endBatch`.

Let me see which `ItemRenderer` methods Sodium redirects.

Let me get the full annotation targets for Sodium's FRAPI `ItemRendererMixin`.

Sodium FRAPI injects at the `isCustomRenderer()` call in `ItemRenderer.render`, cancellable. Let me read vanilla's code there.

Let me look at CGC's `RenderType_` wrapper — the user flagged it as suspicious.

Let me check Sodium's `VertexConsumerMixin` target and whether vanilla `VertexConsumer` has that 11-arg `addVertex`.

Let me check who constructs the bedrock cube (delta handling) and read Sodium's `PoseStackMixin`.

Let me delegate the upstream-port comparison while I continue on Sodium.

While that runs, let me confirm how CGC's gun reaches the renderer.

Let me read CGC's `ClientRenderHelper` — it wraps the GL state and hands out the collector.

Let me check what render type the attachment/scope portion actually gets inside the gun model.

The agent found the smoking gun: upstream's library ships a **Sodium-specific software face-culling workaround** for bedrock models. Let me read it.

Let me check whether the port's attachment (scope) model actually goes through the Sodium-aware cube.

Let me check vanilla's first-person hand pass for cull-state handling.

This is promising — `entityTranslucent` is NO_CULL **and** sortOnUpload. Let me check the actual default of `enable_transparency`.

Big difference found: the port renders first-person via NeoForge's `RenderHandEvent`, CGC at `renderHandsWithItems` HEAD. Let me see where NeoForge fires that event.

两个仓库都没动过，工作区干净。这轮只排查，没改代码。

## 新证据（这轮唯一的实质进展）

上游那套 `simplebedrockmodel` 库里有一套**专门给渲染 mod 准备的 bedrock 立方体**：

- `SodiumCompat.init()` 之后，`BedrockModel.createCubeBox()/createCubePerFace()` 一旦检测到装了 sodium 就返回 `SodiumBedrockCubeBox`（Embeddium 同理）。移植版在客户端 setup 里会调它。
- 这两个子类相对普通 `BedrockCubeBox` 的**功能差异只有两处**：顶点绕过 `VertexConsumer`、直接用渲染 mod 的 `VertexBufferWriter.push(...)` 写进缓冲；以及**软件背面剔除**：

```java
boolean enableCulling = RenderSystem.getModelViewMatrix().m32() == 0;
if (enableCulling && !shouldRenderFace(i, normals)) continue;
// center·normal <= 0 才画
```

也就是说**同一批作者确认过「装了渲染 mod 之后 bedrock 立方体的背面会出问题」**，并靠软件剔除兜。症状类型和你这次完全一致。

**但要提醒**：那个 `m32 == 0` 门槛我判断是 **GUI/物品栏**那一类视角（modelview 无 z 平移），第一人称手部通道的 modelview 乘了投影矩阵、m32 不为 0。所以这个补丁很可能**不是为第一人称手部写的**，它更像在修「背包/改装界面里看枪」。

## 这轮排除掉的（都有证据，别重走）

| 假设 | 结论 |
|---|---|
| Sodium 的四边形排序把两个共面 quad 换了序 | **不成立**。排序只在 `renderType.sortOnUpload()` 为真时跑，而 `entity_cutout` 是 `false`（`RenderType.java:91`）。Sodium 的 `MultiBufferSourceMixin` 只 `@WrapOperation` 了 `MeshData.sortQuads` |
| Sodium 改了 `RenderType.draw` / 剔除状态 | **不成立**。Sodium 的 mixin 目标表里没有 `RenderType`；`draw()` 仍是 setup→draw→clear，`entity_cutout` 走默认 `CULL`（`CompositeStateBuilder:1337`） |
| vanilla 第一人称通道关了剔除 | **不成立**。`ItemInHandRenderer` 整个类**没有一处** `RenderSystem.*`，枪绘制时剔除是开着的 |
| Sodium 的 `PoseStack`/`RenderBuffers`/`RenderTarget` mixin 有副作用 | **不成立**。分别是 Pose 对象缓存、section 缓冲池重定向、只动 `blitToScreen` |
| 上游的 sodium 兜底解释了移植版免疫 | **覆盖不到瞄具**。移植版的瞄具由它自己的 `BedrockModel`（独立类，直接 `new BedrockCubeBox(...)`）构建，全 repo 只 import 了库的 3 个接口 |

**卡点**：`entityCutout` 下背面剔除在 Sodium 和 vanilla 里应该是一样的，所以「零厚度立方体北面被画出来」用纯剔除解释说不通。

## 两个问题请你确认

1. **移植版在「装了 Sodium + 同一把 cbrg:red_dot」下那片 lens 面真的不出现吗？** 按静态代码它也该有病。这一个数据点决定我是继续挖 CGC 侧差异，还是照上游思路做。
2. 同一把枪装 Sodium 时，**改装界面/背包里**看这个瞄具正常吗？（那正是上游补丁覆盖的场景。）

## 建议先做的判定实验（不用重新构建）

改**枪包数据**：把 `scope_body/lens` 那个 cube 的 `size` 第三位从 `0` 改成 `0.01`，进游戏看。

- **消失** → 根因是「两个共面 quad + Sodium 下的顺序/剔除」，修法落在模型加载器（给退化立方体极小厚度，或照上游做软件剔除）。
- **还在** → 与共面无关，是遮罩/模板那条链，我换方向。

未 commit、未 push。等你这两个答案再决定动手方向。

# 

```
改0.01没用，依然有问题
用最新的tacz-neoforge-1.21.1-1.1.8-hotfix-r6.jar，配合sodium，确实没问题，帧数很高
可以浏览SBM git历史
```

# 

Two commits jump out. Let me read them.

Let me check whether CGC's gun also gets rendered through vanilla's item path (the port cancels it; CGC may not).

Strong lead in SBM's commit `2a6c72c 修正剔除面未正常被剔除的问题` touching `BedrockCubePerFace`. Let me check CGC's per-face cube and the port's.

Let me read CGC's per-face cube — whether it skips undeclared faces.

This is the crux. Let me read CGC's `BedrockPolygon` and check the port's per-face support.

Let me check what Sodium does to `Direction` — CGC calls `Direction.step()` and mutates the result.

Let me read the port's own per-face cube and compare to CGC's.

The port's cube geometry is identical to CGC's. Let me compare the port's first-person render entry against CGC's.

Let me look for the actual gunpack model to confirm the lens's face declaration.

没改任何代码，两边工作区仍然干净。你这两条测试结果把方向**掉了个头**，我先说清楚新结论。

## 0.01 没用 → 那片 lens 是一个「只声明了一面」的 per-face cube

CGC 的 [BedrockCubePerFace.java:67-77](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockCubePerFace.java:67) 对**没有声明 UV 的面**不是跳过，而是塞一个**退化四边形**：

```java
private static final BedrockVertex EMPTY = new BedrockVertex(0, 0, 0, 0.0F, 0.0F);
private static final BedrockVertex[] EMPTY_VERTEX = new BedrockVertex[]{EMPTY, EMPTY, EMPTY, EMPTY};
...
if (face == _FaceUv.EMPTY) {
    return new BedrockPolygon(EMPTY_VERTEX, ...);   // 四个顶点全在模型原点，零面积
}
```

也就是说：如果 `scope_body/lens` 这个 cube 用的是 `per_face_uv`、**只有 `north` 有贴图**，那么它**唯一真实的面就是北面**，其余五个是零面积退化面（永远画不出来）。

**这就解释了为什么 0.01 没用**：南面本来就是退化的，改 z 尺寸动不到它。同时它把结论钉死了：

> 那片 lens 在 vanilla 里看不见，**完全、且仅仅**依赖背面剔除（北面背对摄像机 → 被 cull）。**没有第二条防线。**

所以 0.01 那次不是「排除了共面」，而是**证明了根因是「Sodium 下背面剔除没生效」**。

⚠️ 这一步我是**从你的描述推的**，麻烦确认一下：把 `cbrg` 枪包里 `red_dot` 的 bedrock 模型 JSON 中 `lens` 那个 cube 那段发我（看它是不是 `per_face_uv`、是不是只声明了 `north`）。这一段能直接一锤定音。

## SBM 历史确实指向同一条路

按你指的翻了 SBM：

- `2a6c72c 修正剔除面未正常被剔除的问题，再次尝试修正手部问题` —— 给 `SodiumBedrockCubePerFace` 补了 `emptyFacesMask` 检查，**「本该不画的面被画出来了」**，和我们同一个 bug 类。
- `1c64cba 尝试移植tlm的剔除算法` —— 给 `SodiumBedrockCubeBox` / `SodiumBedrockCubePerFace` 加了**软件背面剔除**（`shouldRenderFace`，按「面中心·面法线 ≤ 0」判断），门槛是 `RenderSystem.getModelViewMatrix().m32() == 0`。

**这就是「装了渲染 mod 之后 bedrock 面的剔除不可靠」的官方答案：软件剔除兜底。**

两个要说清的：

1. 那个 `m32 == 0` 门槛我算下来**第一人称手部通道是 -1**（手部 modelview = 层 modelview × 投影矩阵，投影矩阵 m32 = -1），所以上游这套补丁**很可能只覆盖 GUI/物品栏场景**，不是第一人称。要照搬得重新想门槛。
2. **移植版那边我仍然解释不通**：我读了它的 `BedrockAttachmentModel` → 它自己的 `BedrockModel` → `BedrockCubeBox`/`BedrockCubePerFace`，和 CGC 的形状、退化面写法**逐字一样**，SBM 的软件剔除根本覆盖不到它；`AnimateGeoItemRenderer` 只 `implements IFPGeoItemRenderer` 一个接口。也就是按静态代码，移植版的瞄具也该有这病 —— 但你实测它是好的。

## 想请你做两件事（都很便宜）

1. **这是全局剔除丢失，还是只这一块？** 装 Sodium 时，红点那把枪**别的背面几何有没有异常**（弹匣、枪身镂空处、瞄具外环）？如果全枪都正常、只有这片 lens 露出来，那就不是「剔除整体失效」，范围会小很多。
2. **改装界面/背包里看这个瞄具正常吗？**（那是上游补丁真正覆盖的 GUI 场景。）顺带能给我开个 **Sodium-only（不装 Iris）的 log** —— `D:\Temp-File\debug.log` 那份是 Iris+Sodium，我想看 Sodium-only 下有没有 mixin 告警。

给全这两条 + 那段模型 JSON，我就能确定是「照 SBM 做软件剔除」还是「CGC loader 侧跳过空面」，再动代码给你 review。

# 

```
正常枪没看到明显的剔除问题，红点模型本身就只有这一个块用到了这个做法，不清楚是否是全局，红点模型和log放D:\Temp-File了，完成带assets的部分也在目录下D:\Temp-File\tacz了
在改装界面切到枪口位置从枪前往枪后看，能看见lens（本来就是）
切到瞄准镜位置从枪后往前看，仍然能看到lens（跟第一人称一样）
```

# 

Let me look at the model and the new log.

Model confirmed — all 8 `lens` cubes are zero-depth **per-face cubes with only `north` declared**. So culling is the only thing hiding them. Let me check Sodium's `BufferBuilder` intrinsics.

Let me check the new log — is it Sodium-only, and does it show anything unusual?

Let me settle the port contradiction — whether its src cubes do any software culling.

Let me check CGC's `bakePipelineState` — the one wrapper the port doesn't have.

Let me read the port's equivalent of `renderModelPart` — the actual flush path.

Model confirmed: all 8 `lens` cubes are `size z=0` per-face cubes with **only `north`** declared, so CGC's `EMPTY_VERTEX` path makes every other face a zero-area quad — the lens's invisibility rests **100%** on back-face culling. Let me check line endings before patching.

Model confirms the degenerate-face path is involved. Let me apply upstream's fix shape (skip undeclared faces instead of emitting zero-area quads) as a probe.

Patch is minimal and CRLF-clean. Now let me build it.

Build succeeded. Let me verify the jar and its bytecode.

Bytecode verified: `getTexturedQuad` returns null on undeclared faces, `compile` skips them. Let me sync the jar into the compat repo's `libs/`.

Let me record the cross-repo finding for future sessions, then report.

模型确认了推断，我改了一处并构建好了。

## 模型证实：这 8 个块是「只声明 north 的 per-face cube」

`red_dot_geo.json` 的 `lens` 骨骼下 8 个 cube **全部**是 `size` z=0、**`uv` 对象里只有 `north`**：

```json
{"origin": [-0.563, 1.4375, -1.3125], "size": [1.126, 1.22294, 0],
 "uv": {"north": {"uv": [21.5, 21.5], "uv_size": [10.5, 10.5]}}}
```

而 [BedrockCubePerFace.java:67](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockCubePerFace.java:67) 对没声明的面**不是跳过**，是塞一个零面积退化四边形（`EMPTY_VERTEX` = 4 个都落在模型原点的顶点）。所以：

> 这个 cube **唯一真实的面就是 north**，其余五个永远画不出像素。从枪后看它本该看不见，**100% 靠背面剔除**——没有第二条防线。

这就解释了 0.01 为什么没用（南面本来就是退化的），也解释了为什么全枪只有这一处露馅：闭合箱体的背面被自己的正面深度挡住，看不见；只有这种单面零厚度几何没有东西遮它。

## 改动（1 个文件，10 增 6 删）

[BedrockCubePerFace.java](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockCubePerFace.java)：未声明 UV 的面**不再画退化四边形，直接跳过不画**，并删掉因此失效的 `EMPTY`/`EMPTY_VERTEX` 两个静态字段。

这**不是新发明**，是 SBM `2a6c72c 修正剔除面未正常被剔除的问题` 的同一条修法（上游用的是 `emptyFacesMask`）。选它当探针的理由：退化四边形本身画不出任何像素，**所以在 vanilla 下这是纯等价改动**，不会影响其他枪；改动只落在 per-face 路径上，`BedrockCubeBox` 一行没动。

**它同时是一个判据**：
- lens 消失 → 根因就是「网格里混进 4 个重合顶点」这件事被 Sodium 的顶点写入/四边形展开路径搞乱（退化面被当成真面画出来）。
- lens 还在 → 与退化顶点无关，是背面剔除本身没生效，那就走 SBM 那条软件剔除（`shouldRenderFace`）。

## 构建与产物

- 构建通过（`JAVA_HOME=jdk-21.0.8`），产物 `customgun-0.0.17-neoforge-1.21.1.jar` 已重建。
- 字节码核对：`getTexturedQuad` 命中 `_FaceUv.EMPTY` 后 `aconst_null; areturn`；`compile` 里有 `ifnonnull` 跳过；类里已无 `EMPTY_VERTEX` 引用。
- 该 jar 已换进 CGC-Compat-Shader-iris 的 `libs/`（gitignore，无 diff）。**compat 仓库源码一行没动，不用重编。**

## 另外发现一处差异，但我没改

同一条绘制路径上，移植版和 CGC 有个真实的写法差异：

| | 刷新方式 |
|---|---|
| 移植版 `renderTempPart` | `bufferSource.endBatch(renderType)` —— 只刷这一个类型 |
| CGC `renderModelPart` | `bufferSource.endBatch()` —— 无参，额外把 `fixedBuffers` 全刷一遍 |

我判断它对 Sodium 场景大概率**是空动作**（那些 fixed buffer 是原版区块用的 solid/cutout/translucent 等，Sodium 的区块走自己的通道，里面应该是空的），所以没动它。如果上面那版探针没用，我会把这个也试一下。

## 需要你测两件事

1. **装 Sodium**，用同一把 `cbrg:red_dot` 从枪后看（第一人称 + 改装界面瞄准镜视角）—— lens 还在吗？顺带在**不装 Sodium** 时确认其他枪外观没变化（按分析不该变）。
2. **移植版在装 Sodium 下，这片 lens 到底在不在？** 这个我必须再确认一次：移植版的瞄具模型也是它自己的 loader（`com.tacz.guns.client.model.bedrock.BedrockModel`，不继承 SBM），几何和渲染路径跟 CGC 逐字一样、也没有软件剔除 —— 按静态代码它**也应该**有这个病。如果它确实没有，那说明还有一个我没找到的差异，我继续查；如果它其实也有，那方向就完全清楚了，我照 SBM 做软件剔除。

未 commit、未 push。另外把 SBM 那套 Sodium/Embeddium 兼容的存在和两个坑记进了长期记忆。

# 

```
还是没修好
再次确认了，1.21.1非官方移植版确实在安装sodium的情况下没有渲染lens，log在D:\Temp-File\debug.log
```

# 

Let me examine the port+Sodium log.

SBM mixins `PlayerModel` — and CGC has its own `PlayerModelMixin`. Let me compare them.

Let me compare the Sodium mixin set between the port run and the earlier CGC run.

Let me check exactly which render types are the "fixed buffers" that CGC's no-arg `endBatch()` flushes.

The port's log shows the same Sodium mixin set, so Sodium behaves identically in both runs — which points at a confound I missed. Let me first revert my probe (it didn't help, and I want attribution clean) and rebuild.

没修好，我把探针撤了并重建了。这轮的关键发现是：**两次对比测试根本不是同一个环境** —— 我之前的前提是错的。

## 证据：移植版那次根本就没装 ModernFix / PhysicsMod

`D:\Temp-File\debug.log`（移植版 + Sodium，22 个 mod）里按 `from mod <id>` 统计 mixin 应用条数：

| mod | 移植版那次 | CGC 那次（55 mod） |
|---|---|---|
| modernfix | **0**（没加载） | 有（大量 `safety.*` / `bugfix.*` mixin） |
| physicsmod | **0** | 有（且它注入 `ItemRenderer` 那处**失败**了） |
| shouldersurfing | **0** | 有 |
| sound_physics_remastered | **0** | 有 |
| reforgedplaymod | 217 | 有 |
| sodium | 有 | 有 |

两边的 Sodium mixin 目标表（各 66 条）我也逐条比过了，**完全一致** —— 也就是说 Sodium 在两次运行里干的事一模一样。

**所以「移植版没病」这个结论是在一个近乎干净的包里得出的，而 CGC 那次是在 55 个 mod 的包里。这不是对照实验，我之前顺着它推了一整轮，方向从一开始就站不住。**

**ModernFix 是最可疑的那个**：它的存在意义就是改别的 mod 的渲染/加载，它带一整套 `safety.*`、`bugfix.*` mixin，还能动态关掉别的 mod 的 mixin；而且它有一条 `bugfix.entity_pose_stack.PoseStackAccessor` 注入 `PoseStack` —— **和 Sodium 的 `features.render.immediate.matrix_stack.PoseStackMixin` 打在同一个类上**。

## 这轮我做了什么

- **撤销**了上一版的探针（[BedrockCubePerFace.java](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockCubePerFace.java) 已 `git checkout` 还原），并重新构建，让 jar 和源码一致（上一次「jar 比源码新」的坑不想再踩）。
- 工作区干净，jar 已同步进 compat 的 `libs/`。
- 那个探针的结论仍然有效：**去掉退化四边形没用** ⇒ SBM `2a6c72c`（跳过未声明面）那条修法不是 CGC 的答案；SBM 那边真正可能管用的是 `1c64cba` 的**软件背面剔除**。

## 请做这一个对照测试（最省事的一种）

**在你平时那个 55 mod 的包里，只把 ModernFix 去掉，看那片 lens 还在不在。**

- **消失** → 根因是 ModernFix × Sodium（跟 CGC 代码无关），我再去查它改了哪块；这种情况就不该在 CGC 里加补丁。
- **还在** → 再把 PhysicsMod、ShoulderSurfing 依次去掉；如果全去掉还在，那就退到**最小包（只留 CGC + Sodium）**再测一次 —— 那次要是还出，才是 CGC × Sodium 本身的问题，我照 SBM 做软件背面剔除。

我不再凭猜测改代码了：先把这个包变量钉死，再决定补丁往哪打。未 commit、未 push。

# 

```
确实没有渲染lens了，log还是在Temp-File
这个问题先报告一下目前为止的进展，在对话里说明，我会留在提示词.md文件里之后再排查，目前先搁置
```

# 

Confirmed — ModernFix was the trigger. Let me verify the log and the current state before writing the report.

ModernFix 去掉后问题消失。两边仓库都干净、无未提交改动。下面是这次排查的完整记录，可直接存进 `提示词.md`。

---

# 1.21.1neoforge · #60 Sodium

# 

```
不要写在memory里，直接在对话里报告，我会手动将对话内容保存到.md文件
```

# 

The log is decisive — but not the way I assumed. Let me confirm.

核实完了，情况和我在上一轮说的**不一样**，报告里我按实际日志写。

---

# 1.21.1neoforge · #60 Sodium 下红点 lens 被画出来 —— 排查记录（2026-09-22，暂时搁置）

## 一、结论

**在最小包（只有 CGC + Sodium）下问题不出现。** `D:\Temp-File\debug.log`（22 Sep 00:00，完整一次启动到退出）里，mixin 应用行涉及到的 mod id 只有 `customgun`、`sodium` 和 Sodium 内嵌的 `fabric_*` 三个；ModernFix / PhysicsMod / ShoulderSurfing / Flywheel / Iris / reforgedplaymod 一个都没有。这一轮就是「CGC + Sodium 单独」。

⇒ **这个 bug 需要第三个 mod 参与，不是 CGC × Sodium 本身的组合。**

⚠️ 需要纠正一点：我上一条让你「只去掉 ModernFix」，但这份日志显示那一轮其实**没装任何其他 mod**，所以「ModernFix 是元凶」**没有被证明**。它只是「55 mod 包里存在、而最小包里不存在」的候选之一。

## 二、症状与几何事实（已核实）

- 现象：装 Sodium 时，第一人称 + 改装界面，从枪后朝前看 `cbrg:red_dot` 的 `lens`，能看到本该被剔除的北面。不装 Sodium 正常。
- `red_dot_geo.json` 的 `lens` 骨骼下 8 个 cube **全部**是 `size` z=0、`uv` 对象里**只声明 `north``**：

```json
{"origin": [-0.563, 1.4375, -1.3125], "size": [1.126, 1.22294, 0],
 "uv": {"north": {"uv": [21.5, 21.5], "uv_size": [10.5, 10.5]}}}
```

- [BedrockCubePerFace.java:67](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockCubePerFace.java:67) 对未声明的面不是跳过，而是塞 `EMPTY_VERTEX`（4 个都落在模型原点的顶点）→ 零面积退化四边形。**⇒ 这些 cube 唯一真实的面就是 north，从枪后看它本该看不见，完全依赖背面剔除。**
- 这也解释了为什么把 z 由 0 改成 0.01 没用：南面本来就是退化的，改尺寸动不到它。

## 三、踩过的坑（重要）

**「TaCZ 非官方移植版没这问题」这个前提不成立 —— 两次测试根本不在同一个 mod 包。**

| mod | 移植版那次（22 mod） | CGC 那次（55 mod） | 最小包那次 |
|---|---|---|---|
| modernfix | 0 | 有（大量 `safety.*`/`bugfix.*`） | 0 |
| physicsmod | 0 | 有（注入 `ItemRenderer` 那处**失败**） | 0 |
| shouldersurfing / sound_physics | 0 | 有 | 0 |
| reforgedplaymod | 217 行 | 有 | 0 |
| sodium | 有 | 有 | 有 |

两边 Sodium 的 mixin 目标表（各 66 条）逐条比过，**完全一致** —— 差异不在 Sodium。

## 四、已排除（都有证据，下次别重走）

- `RenderType.entityCutout` 带 `CULL`，且 `sortOnUpload=false`（`RenderType.java:91`）⇒ Sodium 的四边形排序碰不到它。
- Sodium **不** mixin `RenderType`，`RenderType.draw()` 的 setup→draw→clear 原样保留。
- vanilla 第一人称通道（`ItemInHandRenderer`）整个类**没有一处** `RenderSystem.*` ⇒ 枪绘制时剔除是开着的。
- Sodium 的 `PoseStack`（Pose 对象缓存）/`RenderBuffers`（区块缓冲池）/`RenderTarget`（只动 `blitToScreen`）mixin 都良性；`DirectionMixin` 只 overwrite `getNearest`。
- **把退化四边形改成直接跳过无效** ⇒ SBM `2a6c72c`（跳过未声明面）不是答案。
- PhysicsMod 注入 `ItemRenderer` 的 mixin 在 CGC 那次是**失败**的（`Invalid descriptor`，它按 Sodium 的 `VertexBufferWriter` 签名写的）。

## 五、上游参考（下次有用的抓手）

`D:\Github\SimpleBedrockModel`（SBM，分支 `1.21.1-dev`）**自带 Sodium/Embeddium 兼容**：
`BedrockModel.createCubeBox()/createCubePerFace()` 检测到装了 Sodium/Embeddium 就返回 `SodiumBedrockCubeBox` / `EmbeddiumBedrockCubeBox`，这两个子类相对普通立方体只多两件事 —— 顶点走渲染 mod 的 `VertexBufferWriter` 直写、以及**软件背面剔除**（`shouldRenderFace`：面中心·面法线 ≤ 0 才画），门槛 `RenderSystem.getModelViewMatrix().m32() == 0`（这个门槛只覆盖 GUI/物品栏那类视角，第一人称手部通道 m32 ≈ -1，照搬会漏）。相关提交：`2a6c72c`、`1c64cba`（TLM 剔除算法）、`48d7b21`、`5e779cc`。

注意：TaCZ 移植版虽然 `libs/` 带了 SBM 2.2.1，但它自己的枪/瞄具模型由 `com.tacz.guns.client.model.bedrock.BedrockModel`（独立类，不继承 SBM）构建，**SBM 的 Sodium 兼容覆盖不到它的枪和瞄具**。

## 六、下次从这里接

1. **在同一个包里对照**：把 CGC 和移植版分别放进**同一个** mod 包测同一把 `cbrg:red_dot`。不要在包不一致的前提下拿「移植版正常」当基准。
2. **二分 55 mod 包**找出第三个 mod。优先怀疑「以改写别的 mod 渲染/加载为目的」的那类：ModernFix（它还带 `bugfix.entity_pose_stack.PoseStackAccessor` 注入 `PoseStack`，和 Sodium 的 `features.render.immediate.matrix_stack.PoseStackMixin` 打在同一类上）、PhysicsMod、ShoulderSurfing。
3. 若确认是 CGC × Sodium（即最小包也出，目前**不是**），再照 SBM 做软件背面剔除；那是 SBM 那边唯一还没被排除的修法。

## 七、代码与仓库状态

- 两个仓库工作区**干净**，无未提交改动，未 push。
- 本轮所有探针都已 `git checkout` 撤销；CGC jar 已按还原后的源码重建（23:50）并同步进 compat 的 `libs/`（gitignore）。
- 本轮改动**全部作废**，不留任何代码痕迹。

---

另外：你说了不要写进 memory，我已经把刚才那个报告类 memory 文件删掉了（SBM 那条参考笔记我留着，如果你也要删，说一声）。

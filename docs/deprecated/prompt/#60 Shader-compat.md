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

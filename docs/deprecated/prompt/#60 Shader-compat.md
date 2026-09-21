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
现在点开视频设置会崩溃，看下什么问题，日志在D:\Temp-File了
```

> #63 提示词

# 

```
现在需要修一个以前遗留的forge1.21.1的问题：
- 在游戏内用任意方式修改config的时候，会直接崩溃
- 有的时候是String跟Enum的类型转换问题，还有现在这样崩溃
- 崩毁日志和debug.log都放在D:\Temp-File\了

1.20.4及1.21.1neoforge和其他版本都没问题，目前认定是1.21.1forge自身差异
```

# 

I'll look at the crash logs first.

Now let me find the config code in this branch.

Let me read the Forge config implementation and compare with the NeoForge one.

The debug log shows the smoking gun: the Forge file-watcher thread reloads `customgun-client.toml` every ~0.5s forever, and each reload reports "not correct. Correcting". Let me look at the core config definitions.

I have the Forge sources jar. Let me read ForgeConfigSpec from it and the NeoForge equivalent.

Let me read the `ConfigValue` and `ValueSpec` internals.

Now let me check the NeoForge side to see how it handles enums, and look at the 1.20.4 branch.

Let me look at the enum config and how it's consumed.

Let me settle the list-vs-enum question with nightconfig directly.

Let me read Forge's `defineList`/`defineListAllowEmpty` to pick the right list overload.

I now have the full picture, empirically verified. Let me check the branch layout before writing the fix.

Root cause is confirmed and empirically verified. Now the fix.

Now let me compile to verify.

Compiles clean. Now let me prove the fix against the real Forge code with a standalone harness.

Good — `isCorrect = false` proven with real Forge code. Let me fix the classpath to get the rest.

The harness revealed I must test the full load→save→reload lifecycle. Let me write a faithful reproduction of the loop.

The loop reproduction is decisive. Let me also demonstrate the String→Enum cast failure the user sees.

Both crash modes reproduced against real Forge code. Let me review and tighten the final diff.

Let me check whether the project has tests I should run.

No test sources exist in this project, so compile + the standalone harness are the verification. Let me record the platform constraint for future sessions.

修好了，改动只有一处，未提交，等你 review。

## 根因：不是“修改 config”崩，而是配置文件一直在被自我回写

`ForgeConfigSpec.Builder#define(path, defaultValue)` 的默认校验器是
`defaultValue.getClass().isAssignableFrom(实际值的类)`。而 TOML 落盘时：

- 枚举存成字符串 → `CrosshairType = "DEFAULT"`（我用 nightconfig 3.7.4 实测解析结果就是 `java.lang.String`）
- 列表存成 `ArrayList`

所以 `core` 里那两个走泛型 `addConfig(String, T)` 的定义永远不会通过校验：

[RenderConfig.java:46](core/src/main/java/dev/xcolorful/customgun/client/config/RenderConfig.java:46) 的 `CROSSHAIR_TYPE`，以及 [SyncConfig.java:26](core/src/main/java/dev/xcolorful/customgun/core/config/SyncConfig.java:26) / `AmmoConfig` 的 8 个 `Collections.emptyList()`。

于是每次加载 → `isCorrect` 为 false → correction 改内存值 → `AutosaveCommentedFileConfig` 立刻回写文件 → Forge 的 FileWatcher 把这次回写当成“文件变更”重新加载 → 再 correction……**无限读改写循环**。debug.log 里 `customgun-client.toml is not correct. Correcting` 出现 **193 次**、约每 0.5 秒一次，直到 21:07:46.5 崩溃为止（server 20 次，common 1 次）。崩溃栈正是这个文件在同一时刻被池线程和渲染线程同时写：`ForgeModConfigSpec.set` → `AutosaveCommentedFileConfig.set` → `Files.newOutputStream` → “being used by another process”。

你说的另一个 String/Enum 症状也是同一根因：普通 `ConfigValue.getRaw` 就是 `config.getOrElse(...)`，不做类型转换，correction 还没替换掉那个值时 `get()` 就把 TOML 里的 String 当枚举返回。

## 验证（用真实 Forge 1.21.1 的 `ForgeConfigSpec` + nightconfig 3.7.4 跑独立 harness）

| | 5 次模拟 FileWatcher 重载判定不正确 | 文件被回写 | `get()` 返回 |
|---|---|---|---|
| 旧 `define()` | **5 / 5** | **5 / 5** | correction 后才是枚举；未 correction 时 `ClassCastException: String cannot be cast to CrosshairType`（已复现） |
| 新 `defineEnum` + List 校验器 | **0 / 5** | **0** | 直接是枚举（`EnumValue`） |

## 改动

只改了 [ForgeModConfigSpecBuilder.java:36](forge-compat/src/main/java/dev/xcolorful/customgun/forge/config/ForgeModConfigSpecBuilder.java:36)：`addConfig(String, T)` 里枚举走 `Builder#defineEnum`，`List` 走带 `o instanceof List` 校验器的 `define`，其余不变。

- `core` 接口和调用点都没动，平台差异留在 compat 层。
- Boolean/Integer/Double 落盘类型和默认值一致，日志里也确实没有它们的 `Incorrect key`，不碰。
- 列表元素类型没法校验（空列表推不出元素类型，core API 也不带这个信息），和改动前的宽松程度一致。
- 副作用：`CrosshairType` 那一项首次启动会多一行 `#Allowed Values: ...` 注释，因而会有一次 correction + 回写，之后稳定（harness 里首次 `setConfig` 补齐注释后，5 次重载全部干净）。

## 两点要你决定

1. **1.20.4 / 1.21.1neoforge 不是“没问题”，只是不崩**：它们的 `set` 不 autosave、没有强制回写，所以同样的错误定义表现为“枚举/列表值每次启动被静默重置为默认”，而不是崩溃。要不要一起修？
2. 我没法跑游戏验证（崩溃日志里的 `E:\Software\Minecraft\forge1.21.1` 在这台机器上不存在，我只有 server 端实例）。harness 在 `/tmp/probe/`（仓库外，没进 git），需要的话我可以把它挪进仓库当回归测试，或者你自己跑一遍游戏确认日志里不再有 `is not correct. Correcting`。

# 

```
那这专门加一个重载类型是否更合适?只有enum这一个类型会出问题吗?在1.20.1接口补充这个重载，以及看List是否需要重载
```

# 

Good question — let me investigate before changing anything. First the 1.20.1 checkout and its config API.

Noted — session cwd moved to the 1.20.1 checkout; I'll use absolute paths. Let me enumerate every config default type on both branches.

The existing interface already uses same-name overloads for the int/double variants, so let me test whether enum/list overloads actually resolve cleanly before recommending.

Confirmed — same-name overloads resolve cleanly with **zero** call-site changes (my ambiguity worry was wrong). Let me verify the remaining type question and whether 1.20.1 Forge has the same behavior.

Both 1.20.1 and 1.21.1 have the identical `ConfigWatcher` correction loop. Let me check whether NeoForge does too.

Two important findings before I change anything. Let me verify Forge 1.20.1's enum API so the 1.20.1-side fix is correct.

Both findings are important. Let me apply the overload design — first the 1.20.1 core interface (upstream).

The 1.20.1 interface already has two half-written lines from your side — let me check their git state before completing them.

That's your WIP (uncommitted, currently not compiling) — I'll complete it as intended. Here's the plan.

Now let me compile the 1.20.1 branch.

1.20.1 compiles. Now mirror it into the 1.21.1forge worktree and replace my earlier `instanceof` version.

Both compile. Now let me verify the *dispatch* actually lands on the new overloads using the compiled bytecode.

Perfect dispatch confirmed in bytecode on both branches. Let me review the final diffs.

Diffs are byte-identical on both branches (same blob hashes) — good for merge hygiene. Let me inventory which other branches' builders will need the two new methods.

Let me update my memory note to reflect the final design, then report.

是的，重载更合适 —— 我按你的思路补完了，但有两件事和你的预设不一样。

## 先回答两个问题

**1. 只有 enum 出问题吗？不是，List 一样。**

原理是通用 `define(path, defaultValue)` 的校验器 `defaultValue.getClass().isAssignableFrom(实际值.getClass())`，所以凡是 **Java 类型 ≠ TOML 落盘类型** 的默认值都中招。TOML 数据模型里只有两种：Enum→String、List→ArrayList（Map→Table、自定义对象同理，项目里没用到）。

Boolean/Integer/Double 安全，我核对过真实配置文件：`DamageBaseMultiplier = 1.0`、`WeightSpeedMultiplier = 0.015`、`ExplosiveAmmoVisibleDistance = 192`，日志里也确实没有它们的 `Incorrect key`。

**2. List 需要重载，1.20.1 有 7 处**（AmmoConfig 1 + SyncConfig 6）。我写成泛型 `<T> List<T>`，这样 6 个 `List<String>` 和 1 个 `List<List<String>>`（`ClientGunPackDownloadUrls`）都覆盖 —— 你原来那行 `IModConfigSpec<List<>>` 就是想问这个。

## 改动

你已经在 1.20.1 接口里起了两行（`IModConfigSpec<Enum>` / `IModConfigSpec<List<>>`，未提交、当前编不过），我补成完整声明并把两个平台的实现补上：

[IModConfigSpecBuilder.java](core/src/main/java/dev/xcolorful/customgun/core/api/config/IModConfigSpecBuilder.java:17)
```java
<T> IModConfigSpec<T> addConfig(String path, T defaultValue);
<E extends Enum<E>> IModConfigSpec<E> addConfig(String path, E defaultValue);
<T> IModConfigSpec<List<T>> addConfig(String path, List<T> defaultValue);
```

**调用点零改动** —— javac 按实参静态类型分派（我先前担心和 `Object` 重载有歧义，实测不会）。字节码核对结果和源码数量完全对得上：

| | `(String, Enum)` | `(String, List)` | `(String, Object)` |
|---|---|---|---|
| RenderConfig | 1 (CrosshairType) | – | 15 (Boolean) |
| SyncConfig | – | 6 | 5 (Boolean) |
| AmmoConfig | – | 1 | 6 (Boolean) |

两分支（1.20.1 主 checkout + 1.21.1forge）各 2 个文件、4 行实现，均编译通过；两分支内容**逐字节相同（blob 哈希一致）**，合并时不会冲突。1.21.1forge 里我上一版 compat 层的 `instanceof` 分派和 rawcast helper 已删掉，换成这两个 override。

## 一个你没预料到的发现：1.20.1 本身也有这个 bug

我核对了 Forge 47.4.23 的源码和 fmlcore：

- `ForgeConfigSpec.setConfig` 在 correction 后**同样**强制 `fileConfig.save()`（与 1.21.1 逐字相同）
- `ConfigFileTypeHandler$ConfigWatcher` 的 `"Configuration file {} is not correct. Correcting"` / `"Config file {} changed, sending notifies"` 在 1.20.1 与 1.21.1 **逐字相同**

也就是说 1.20.1 的循环条件齐备，这不是接口整洁问题而是同一个缺陷（枚举值每次启动被重置为 DEFAULT、列表值被重置为默认，且文件被反复回写）。NeoForge 的 `acceptConfig` 也 correction+save，但它用 nightconfig 自己的 FileWatcher 且注释写着"正常情况下不该走到这里"；只有你的实测说它不崩，我没有 NeoForge 日志，所以不断言它是否也在刷 `Correcting`。

## 要你决定

其它分支合并 core1.20.1 后**会编译失败**：`IModConfigSpecBuilder` 多了 2 个抽象方法，而 1.20.4 / 1.21.1neoforge 各有 **2 个**实现类（forge-compat + neoforge-compat）要补。NeoForge 侧我确认过有 `defineEnum(String,V)` 和 `define(String,T,Predicate)`，补法一样。

要我现在一并补吗？还有我这看不到的 1.21.4 / 1.21.6 / 1.21.10 / 1.21.11 / 26.x worktree，也建议一起（或至少确认它们各自的 impl 数量）。

另外提一句：改完后首次启动仍可能看到**一次** `is not correct. Correcting` —— 新增 key、注释变更、以及枚举新加的 `#Allowed Values: ...` 行都会触发一次 correction + 回写，之后稳定（harness 里首次 `setConfig` 后连续 5 次重载全部干净）。两个 checkout 都是工作区改动，未提交。

后台那条清单跑完了，和我上面的结论一致，没有新东西：

- `1.20.4`（分支 1.20.4）：`ForgeModConfigSpecBuilder` + `NeoModConfigSpecBuilder` → 2 个实现
- `1.21.1neoforge`（分支 1.21.1neoforge）：同上 → 2 个实现
- `1.21.1forge`：只有 `ForgeModConfigSpecBuilder` → 1 个（已改完）

所以等你的决定：其它分支是现在一起补这两个 override，还是先只保留 1.20.1 + 1.21.1forge 这两处改动。

# 

```
另外，AmmoConfig是否不能改成ArraySet类型？
```

# 

你改了盘上的两个文件，我按当前状态理解。先验证 `Set`/`ArraySet` 这条路能不能走通。

`Set` 直接写不了盘 —— 让我看一下这两个列表现在怎么被消费的，再给结论。

换 cwd 不影响结论。先给答案：

## 不能 —— nightconfig 的 TOML writer 直接拒写 `Set`

我拿 3.7.4 实测（`LinkedHashSet` 就够说明问题，fastutil 的 `ObjectArraySet` 也是 `Set`）：

```
com.electronwill.nightconfig.core.io.WritingException: Unsupported value type: class java.util.LinkedHashSet
    at com.electronwill.nightconfig.toml.ValueWriter.write(ValueWriter.java:63)
    ...
    at AutosaveCommentedFileConfig.set(AutosaveCommentedFileConfig.java:41)
```

而且因为是 `autosave()`（崩溃栈里那个类），这不是"偶尔失败"——**首次写盘就抛异常**。也就是说把 `PASS_THROUGH_BLOCKS` 改成 `Set`/`ArraySet`，比现在的循环崩得更早、更硬。

原因和前一个 bug 是同一根：TOML 没有 set 类型，数组只能落成 `List`（读回是 `ArrayList`）。所以 `Set` 默认值会两头都不成立：

1. 写盘被拒（`ValueWriter` 不认 `Set`）
2. 就算绕过写盘，读回是 `ArrayList` → 校验器不过 → 又是那个 correction 回写循环，且 `get()` 拿 `ArrayList` 当 `Set` 用 → `ClassCastException`

想真正支持 `Set` 就得在 core 再加一个重载 + 平台层做"写盘转 List、读取转 Set"的适配器。我不建议：这会给项目引入第一个「存储类型 ≠ 暴露类型」的特例，而 TOML 落盘本来就是数组，`List` 已经能表达。

## 如果你要的是 O(1) 查找 / 去重，应该在消费端建索引

现有代码其实已经这么干了：

- [InteractFilterData.java:32](core/src/main/java/dev/xcolorful/customgun/client/config/sync/InteractFilterData.java:32) 的 `reloadInteractFilter()` 已经把那 4 个列表一次性转成 `Map<ResourceLocation, Boolean>`，查找是 O(1) —— 那边改 `Set` 没有任何收益。
- [ProjectilePhysicsManager.java:81](core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:81)（和 `_TempExplode.java:353`）是唯一还在线性扫的地方：射线过滤回调里对 `List<String>` 做 `contains`，每个候选方块一次，还每次 `blockRl.toString()` 分配字符串。

所以要做的话是**在这一处**缓存 `Set<String>`（配置本身仍存 `List<String>`，热重载时重建缓存——注意 `AmmoConfig` 是 COMMON，`onReloadingConfig` 目前只处理 SERVER，得顺带加个 COMMON 分支）。但说实话：列表通常是空的或几条，`.get()` 又是缓存过的字段读，收益只在大列表时才有。`_TempExplode` 同理。**除非你确实见过它出现在 profiler 里，我倾向先不动。**

---

顺带两件小事：

1. 你改完 1.20.1 那两个文件后，两分支的同名文件**不再逐字节相同**了（重载排到了范围重载之后、javadoc 改成 `<ul>`）。合并 core1.20.1 → 1.21.1forge 时这两个文件会有小冲突。要我把 1.21.1forge 侧同步成一样的排布和 javadoc 吗？
2. 上一轮那个待决定项还悬着：其它分支（1.20.4 / 1.21.1neoforge 各 2 个实现类，以及我看不到的 1.21.4 / 1.21.6 / 1.21.10 / 1.21.11 / 26.x）合 core1.20.1 后会编译失败，要不要我现在一起补这两个 override。

# 

```
现在要补充ArraySet缓存的话是在config reload的时候同时判断config类型和CusotmGun.getMcSide()来防止担任游戏/专用服务端/多人游戏冲突？
```

# 

好问题 —— 但 `getMcSide()` 这个判断方向可能是反的。让我核对现有的事件接线和消费侧的 side 判断。

核对完了 —— 这个方向是反的，两个判断都会踩坑。`ForgeSideExecutor.getLogicalSide()` 的实现是关键：

```java
    public McLogicalSide getLogicalSide() {
        /*
        EffectiveSide.get() 对不在 SidedThreadGroup 里的线程（如 资源reload 的完成线程）一律返回 CLIENT
        专用服务端可以用物理 side 兜底
        单人游戏仍然存在无法区分服务端逻辑的问题
         */
        if (CustomGun.getMcSide().isServerSide()) return McLogicalSide.SERVER;
        return McSideHelper.convert(EffectiveSide.get());
    }
```

## 为什么两个判断都不能用

**`getMcSide()` 是物理 side（`FMLLoader.getDist()`）**，单机也是 CLIENT。用它判断，单机就被挡掉了 —— 而单机里集成服务端**正是要用这个缓存的那一侧**。

**更关键的是：对 `PASS_THROUGH_BLOCKS` 这种 COMMON 配置，连 logical side 判断也不能加。** 配置的 Loading/Reloading 事件是在 modloading worker 和配置 watcher 池线程上触发的（就是崩溃日志里的 `pool-7-thread-1` / `pool-10-thread-1`），这些线程不在服务端的 `SidedThreadGroup` 里，`EffectiveSide.get()` 一律返回 CLIENT。所以在单机里：

- `ForgeModConfigEvent.onLoadingConfig` 的 `getLogicalSide().isServer()` → **false**（不重建）
- `ForgeClientModConfigEvent.onLoadingConfig` 的 `getLogicalSide().isClient()` → **true**（会进）

把 COMMON 的缓存挂在 server 侧 handler，单机就**永远不会重建**，集成服务端读到空缓存 —— 正好是你担心的那个坑，而且是必然触发。

## 正确的判据是「源配置属于哪种 ModConfigType」，不是 side

| 缓存源 | 落盘位置 | 该怎么接 |
|---|---|---|
| **COMMON**（`AmmoConfig.PASS_THROUGH_BLOCKS`） | `config/customgun-common.toml`，**双方同一个文件、同一个 ModConfig 实例** | 不做 side 判断，**两个 handler 里都重建**；只判 `type == ModConfigType.COMMON`。同一个 JVM 内两个逻辑侧的值本来就相同，单机最多多建一次，无害 |
| **SERVER**（`SyncConfig.*`） | per-world `serverconfig/customgun-server.toml`，服务端特有、靠网络同步给客户端 | 必须挂 client 侧 handler（`InteractFilterData.reloadInteractFilter()` / `HeadAABBData.reloadHeadAABB()` 就是这么做的）—— 客户端那份文件根本不是服务器的；专用服务端侧则由 `getMcSide().isServerSide()` 兜底 |

**多人（连远程服）不构成冲突**：客户端也会加载自己的 `customgun-common.toml`，值可能和服务端不同，但 `PASS_THROUGH_BLOCKS` 只在服务端弹道里消费（[ProjectilePhysicsManager.java:74](core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:74) 已经 `logicalSide.isClient() → return`）。

## 具体接法

`ModConfig.Event.onLoadingConfig/onReloadingConfig(ModConfigType)` 已经把类型传进来了，所以两个 `Event` 类里各加一段（**Loading 和 Reloading 都要**，Forge 只在 reload 时清 `ConfigValue` 缓存，漏了 Reloading 就是脏数据）：

```java
if (modConfigType == ModConfigType.COMMON) {
    ProjectilePhysicsManager.reloadPassThroughBlocks();   // 或放 AmmoConfig 旁边的小 holder
}
```

形参照 `HeadAABBData.reloadHeadAABB()`：静态 `reloadXxx()`，内部 `clear()` + 重新解析。

两个建议：

1. 缓存存 `Set<ResourceLocation>` 而不是 `Set<String>`，消费侧就不用每个候选方块都 `blockRl.toString()`（`InteractFilterData`/`HeadAABBData` 也都是在 reload 时 parse 成 `ResourceLocation` 的，保持一致）。
2. `_TempExplode.rayTraceBlocks(Level,...)`（第 297 行调用处）自身**没有** side 门槛，如果它在客户端也会被走到，客户端会用自己那份值 —— 这是 COMMON 配置的既有语义，不是缓存引入的新问题，但你可能想顺手确认一下。

另外一句提醒：改完前面那个 correction 循环后，COMMON 配置现在只会在人真的改文件时才 reload，所以这条重建路径平时几乎不会跑 —— 收益（把每次候选方块一次 `List.contains` + `toString`）值不值得，还是看你有没有在 profiler 里见到它。

# 

```
common配置是本地独立的？不自动同步？不需要区分单人游戏和多人游戏客户端？给我修改
```

# 

先核实事实，再动手改。

事实核对完了，你的三点里前两点成立（有 Forge 官方注释背书）：

```java
public enum Type {
    /**
     * ... Loaded on both servers and clients.
     * Stored in the global config directory.    <- 各机器自己的 config/ 目录
     * Not synced.                               <- 不同步
     */
    COMMON,
    /** ... Stored in a server/save specific "serverconfig" directory.
     *  Synced to clients during connection.      <- 只有 SERVER 型会同步
     */
    SERVER;
```

所以 COMMON 确实是**本地独立、不自动同步**（项目里 `SyncConfig` 能到客户端是因为它是 SERVER 型，走 Forge 的 `SyncConfigTask`）。多人客户端也不需要区分——它读自己的 common 文件，而 `PASS_THROUGH_BLOCKS` 只在服务端弹道里用。现在开始改。

等一下 —— 1.20.1 上**已经有这个缓存了**（`AmmoConfig.passThroughBlocksCache`，类型 `ClassUtils.ArraySet<String>`），只是这个 worktree 还没合并到。让我先看清已有实现。

1.20.1 上用的是**项目自己的** `ClassUtils.ArraySet`（不是 fastutil），而且 `AmmoConfig` 里有缓存字段和重建方法。让我读完这套已有实现。

关键发现：`reloadPassThroughBlocks()` 在 1.20.1 上**没有任何调用方** —— 所以那边的缓存永远是空的，`PASS_THROUGH_BLOCKS` 实际已失效。这正是你要补的那一环。让我看下两分支 `AmmoConfig` 是否一致，好按最小差异移植。

完全清楚了 —— 这个分支的 `AmmoConfig` 上还留着你自己的 `// TODO 这个得改ArraySet`，1.20.1 已经改完但**没接上重建的触发点**。我按 1.20.1 的既有实现移植过来，并补上触发点。

两分支都编译通过。现在验证整条链路（触发点 → 重建 → 消费）在字节码里真的接上了。

链路在字节码里全通。但还有一个跨分支风险要查：NeoForge 侧的事件门槛会不会把 COMMON 挡掉。

四个事件类（Forge/NeoForge 各两个）门槛一致，而我两个 `Event` 都挂了 —— `getLogicalSide()` 只可能返回 SERVER 或 CLIENT，所以必然命中其中一个，重建实际上是无条件的。看下最终改动：

1.20.1 那边你似乎同时在改（`AmmoConfig`/两个消费侧文件出现在 diff 里，接口和 builder 已经不在）。让我确认没和你的改动打架。

你把 enum/list 那次修复提交成 `c27d06574` 了。1.20.1 里 `AmmoConfig` + 两个消费侧是你自己在改的缓存部分，我只动了那两个 `Event` 文件。读一下你那份 `#63 config-fix.md` 对齐意图。

改完了，两分支都编译通过，未提交。

## 做法：按 1.20.1 的既有实现移植，补上缺失的触发点

**结论先说**：不需要判断 `getMcSide()`，也不需要判断 logical side，只判 `ModConfigType.COMMON`，而且**两个 `Event` 都要挂**。理由就是前面那条：`getLogicalSide()` 只能是 SERVER 或 CLIENT，单机时配置事件线程报 CLIENT —— 只挂 server 侧就恒不重建。

**1.21.1forge（本 worktree）**
- `AmmoConfig`：从 1.20.1 **逐字**移植 `passThroughBlocksCache` + `reloadPassThroughBlocks()`，顺手去掉你自己留的 `// TODO 这个得改ArraySet`
- `ProjectilePhysicsManager`、`_TempExplode`：局部变量改成读缓存
- `ModConfig.Event`、`ClientModConfig.Event`：`COMMON` 时重建（Loading + Reloading 各一处）

**1.20.1（主 checkout）**
- 我只动了那两个 `Event` 文件（同样 4 处重建）。`AmmoConfig` 和两个消费侧是你自己在改的，我没碰。
- 这两个 Event 文件我直接按同一份内容同步过去了 → **两分支逐字相同**，合并不冲突。

## 顺带修掉一个实际故障

1.20.1 上 `reloadPassThroughBlocks()` 之前**没有任何调用方**（我全仓 grep 只有声明本身），所以那个缓存恒为空、`contains` 恒 false —— 也就是 **1.20.1 上 `PASS_THROUGH_BLOCKS` 实际已失效**，配置里写了也没用。现在接上了。

## 验证（字节码级，不只是编译过）

```
AmmoConfig                      passThroughBlocksCache: ClassUtils$ArraySet<String> + reloadPassThroughBlocks()
ProjectilePhysicsManager        1× AmmoConfig.passThroughBlocksCache,  0× PASS_THROUGH_BLOCKS
_TempExplode                    1× AmmoConfig.passThroughBlocksCache,  0× PASS_THROUGH_BLOCKS
ModConfig$Event                 2× reloadPassThroughBlocks  (Loading + Reloading)
ClientModConfig$Event           2× reloadPassThroughBlocks
```
源码里除 `reloadPassThroughBlocks()` 内部那行外，已无 `PASS_THROUGH_BLOCKS.get()`。另核对：四个事件类（Forge/NeoForge 各 2 个）门槛结构一致，所以这套 core 改动在所有 loader 上都会生效。

## 三个待你决定

1. **其它分支会编译失败**（老问题，仍未处理）：接口那 2 个重载要在 1.20.4 / 1.21.1neoforge 等分支的 `ForgeModConfigSpecBuilder` + `NeoModConfigSpecBuilder` 各补 2 个 override。要现在补吗？
2. `reloadPassThroughBlocks()` 用的是 `clear()+addAll()` 原地改，而配置 watcher 线程重建时服务端线程可能正在 `contains` —— `InteractFilterData`/`HeadAABBData` 也是同样写法，我按既有风格没改。想彻底躲开可以改成「建新实例再换 `volatile` 引用」；要改的话建议 1.20.1 一起改，保持两分支同步。
3. 消费侧仍是 `contains(blockRl.toString())`，每个候选方块一次 `toString()` 分配；缓存 `Set<ResourceLocation>` 能省掉，但会偏离 1.20.1 的既有实现，按你的偏好定。

# 

```
客户端本地独立common的话，本地服务端线程和客户端线程是否可能会同时修改这个config从而导致并发问题?
单人游戏的话会触发两次？
```

# 

两个问题都问得准，让我核一下证据再答（尤其是"谁会在运行时写 config"）。

核完了。先把你这两个问题引出的那句不准确注释改掉（"重复重建无害"会让人以为会命中两次）。

两个问题的结论都和我上一轮的措辞相反，注释已改，两分支依然逐字相同。

## 1. 共用同一个 config 实例 —— 前提成立，但没有并发写

前提你说对了：单机下 client 和集成服务端在同一个 JVM，`customgun-common.toml` 只有一份 `ModConfig` / `ForgeConfigSpec` / 可写配置对象，两个逻辑侧访问的是**同一个对象**（日志里也只有一个 watcher、一次 tracking）。

但**没有任何 mod 代码会写 COMMON 配置**。全仓 `set()` 的写入方只有 6 处：

| 写谁 | 谁在写 / 哪个线程 |
|---|---|
| `RenderConfig.REPLACE_VANILLA_CROSSHAIR` | `DefaultCrosshair:185`，客户端 GUI，渲染线程 |
| `RenderConfig.ENABLE_GUN_HUD` | `DefaultGunHud:162`，同上 |
| `RenderConfig.ENABLE_SHOOTER_OPERATION_HUD` | `DefaultShooterOperation:177`，同上 |
| `SyncConfig.ENABLE_TABLE_FILTER` / `SERVER_SHOOT_NETWORK_V` / `SERVER_SHOOT_COOLDOWN_V` | `ConfigCommand:34-36`，`/customgun config`，服务端线程 |

`GunConfig/AmmoConfig/OtherConfig`（COMMON）的 `set()` 调用数是 **0**（forge-compat 侧也是 0）。所以 COMMON 上只有「读 vs 读」和「读 vs Forge 的 correction/reload」，而后者修掉循环后只在你手改文件时发生。

顺带说清两件事：
- CLIENT 配置那 3 个写入方全在渲染线程（同一线程，不并发）；SERVER 配置在单机下服务端是文件实例、客户端是 Forge 同步过来的另一个实例，本来就不是同一个对象 —— 这也正是 `InteractFilterData`/`HeadAABBData` 挂在 clietn 侧 handler 的原因。
- 真正的并发写暴露面是 nightconfig 自身的弱点：`save()` 只拿**读**锁，两个并发 save 会撞 "file is being used"（就是刚修掉那个崩溃的机制）。这是 Forge 的，不是这次改动引入的。
- **我的缓存没给配置加任何写入路径**：`reloadPassThroughBlocks()` 只 `PASS_THROUGH_BLOCKS.get()` 读一次。

唯一的残留不确定性：Cloth 配置界面在 `CGC-Config-Addon` 那个独立仓库里（崩溃栈里的 `ClothKeyConfig` 写的是 `customgun-client.toml`，CLIENT），它有没有也提供 COMMON 的编辑界面我在这边看不到。如果有，那就是"渲染线程写 + watcher 线程 correction"的 Forge 既有暴露。

## 2. 单人游戏**只会触发一次**，不是两次

两个 handler 的门槛互斥：`getLogicalSide()` 只可能返回 SERVER 或 CLIENT（`getMcSide().isServerSide()` 兜底，否则 `EffectiveSide.get()`），而 `isServer()` / `isClient()` 不可能同时为真。同一次事件派发里两个 listener 是在**同一个线程上同步调用**的，`EffectiveSide` 是 ThreadLocal，两次调用读到同一个值 —— 所以每次只有一个能过。

- 专用服务端：物理 side 兜底 → SERVER → 走 `ModConfig.Event`（client handler 是 `Dist.CLIENT`，压根没注册）
- 单机 / 多人客户端：配置事件在 modloading worker 或 watcher 池线程上 → CLIENT → 走 `ClientModConfig.Event`

日志也能印证：整个会话里 `customgun-common.toml` 的 `is not correct. Correcting` 只出现 **1 次**，而不是 2 次。（就算真被调用两次也无害，`reloadPassThroughBlocks()` 是 clear + 重填，幂等的 —— 但那句"重复重建无害"确实容易被读成"会命中两次"，所以我把它改成"每次只会命中一处"了。）

---

待你决定的三件老事没变：其它分支补那 2 个 overload、`clear()+addAll()` 要不要改成换引用（`volatile`）、消费侧要不要改成缓存 `Set<ResourceLocation>` 省掉 `toString()`。

# 

```
reloadPassThroughBlocks是否可能被两个事件并发调用?
```

# 

能，但只在一种组合下，而且**当前实现撞上会真的坏**。

## 哪些调用会并发

先排除掉不会并发的：

- **同一 config 的多次 Reloading 是串行的**。Forge 每个 config 类型只有一个 watcher 线程（日志里 `CLIENT` 的落在 `pool-7-thread-1`、`SERVER` 的落在 `pool-10-thread-1`，`Starting watcher for handler: ConfigFileTypeHandler[类型]` 每类型一次），同一线程上的多次事件依次派发。
- **两个 Event 单例不会同时命中**（上一轮那条：side 门槛互斥），所以单次派发只有一个 handler 调它。
- **不同 config 类型不会混进来**：`COMMON` 分支被 `type == COMMON` 挡着，CLIENT/SERVER 事件不会调它。

唯一可能的并发组合是 **Loading vs Reloading**：

- Loading 跑在 modloading worker 线程上；Reloading 跑在该类型 watcher 线程上。
- watcher 是在 load **过程中**启动的（日志顺序：`Loaded TOML config file` → `Watching ... for changes` → 紧接着 load 期的 correction），而 load 期的 correction 会立刻回写文件。如果这次回写被 watcher 在 Loading 的 handler 还在栈上时轮询到，两个线程就会同时进 `reloadPassThroughBlocks()`。

handler 体只有微秒级（clear + 重填几十个字符串），所以实际撞上的概率极低，但**不是不可能**——尤其你现在这个 0.5s 轮询间隔下，首次 Reloading 就在 Loading 之后约 0.5s，而加载期还有别的活在同一线程上干。

## 撞上会出什么事

`ClassUtils.ArraySet` 是 `ArrayList` + `HashSet` 的组合，`clear()+addAll()` 是**原地改**：

- **重建 vs 重建**：两个线程同时 `add` 同一元素 → `set.add` 双方都返回 true → `list` 里出现重复；或两个 HashSet 的 resize 交错 → 结构损坏。`list` 和 `set` 就此失步，之后 `get(i)`/迭代结果和 `contains` 不再一致。
- **读 vs 重建**：服务端线程正在 `contains`（HashSet 查找）时被另一个线程 `clear()`/`addAll()` → 结果错误，极端情况下 resize 期间自旋。

## 一次修两种并发的写法

```java
    private static volatile ClassUtils.ArraySet<String> passThroughBlocksCache = new ClassUtils.ArraySet<>();

    public static void reloadPassThroughBlocks() {
        ClassUtils.ArraySet<String> rebuilt = new ClassUtils.ArraySet<>();
        rebuilt.addAll(PASS_THROUGH_BLOCKS.get());
        passThroughBlocksCache = rebuilt;   // 建完整体再换引用，读方只会看到完整的一版
    }
```

- 读方永远看到「旧的完整集合」或「新的完整集合」，不会是半成品；
- 两个并发重建各建各的、最后写入者生效，都是完整集合，不会互相踩；
- 安全发布成立：`ArraySet` 内部 `list`/`set` 是 `final`，`volatile` 写的 happens-before 会把构造时的内容一并发布给读方（消费侧只读，没人改它）。
- 代价：字段加 `volatile` + 三行改动，消费侧签名不变。

要不要现在改？如果要，我建议在 **1.20.1 的 `AmmoConfig`（你手上那份 WIP）** 上改，然后同样内容同步到 1.21.1forge —— 和前面一样保持两分支逐字相同。

另外提一句（不一定要做）：`InteractFilterData.reloadInteractFilter()` 和 `HeadAABBData.reloadHeadAABB()` 是同样的 `clear()+fill` 原地改写法，暴露面相同。要一并硬化的话是三个类一起改，但那是既有代码，我按规矩不主动动。

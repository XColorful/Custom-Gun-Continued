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

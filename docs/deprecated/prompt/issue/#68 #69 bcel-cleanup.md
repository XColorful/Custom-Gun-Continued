> #69 提示词

# 

```
读Issue #68 https://github.com/XColorful/Custom-Gun-Continued/issues/68 里对于bcel库移除的提问
- 现在IDEA打开了CGC和TaCZ1.20.1的窗口，目前仅浏览1.20.1（高版本实现一致）
- TaCZ里注释掉的那行里commit写的是修复了jarJar问题，需要检查是否还有必要
- CGC目前没有应用TaCZ里Attachment data里inline计算脚本的预编译，这个应该不属于bcel的问题

研究这个问题，看是否需要修改
```

# 

我先读 Issue #68 和相关记忆文件。

`gh` 不可用，我用 WebFetch 读 issue。

curl 可用。取正文和评论。

Issue 读到了。核心问题三点，我先在 CGC 侧定位相关代码。

CGC 侧：bcel 只在 `compileOnly` 和 Forge 的 `jarJar` 里出现，**没有任何 `LuaJC.install` 调用**。现在去 TaCZ 侧查证那行注释的来历。

关键线索：那个 commit 的目的是「从本地 libs 切到远程依赖以修复 jarJar」，**同时**升级了 luaj 3.0.3→3.0.8-figura、**新增** bcel 依赖、**新增** 那行 `//LuaJC.install`。继续查 LuaJC 是否曾被调用过，以及 luaj 是否真的需要 bcel。

TaCZ 用的是 `LuaC.install`（纯 Java Lua→LuaJ 字节码编译器），而 `//LuaJC.install` 是**从未启用过的残留注释**。现在查 luaj 是否真的依赖 bcel，以及 CGC 侧对应实现。

CGC 同样用 `LuaC.install`，与 TaCZ 一致。现在查 TaCZ 的 inline 脚本处理与 CGC 的对应实现。

TACZ 1.20.1 的 `functionEval` 也是每次 `ScriptEngine.eval`，没有预编译。我查一下 `Modifier` 数据类和其它分支，确认「预编译」具体指什么。

找到关键点：TaCZ 有 `AttachmentScriptManager` 用 `compilePrototype` 做预编译。检查这个文件是否在当前工作区。

现在全貌清楚了。验证 luaj 的 jar 是否真的含 `LuaJC` 以及是否引用 bcel。

只有 `JavaBuilder` 引用 bcel。检查哪些类会牵出 luajc 包（尤其 CGC 用到的 ScriptEngine 路径）。

`LuajContext`（ScriptEngine 路径）引用了 `LuaJC`。先看 CGC 实际是否走这条路。

现在做决定性验证：在**不带 bcel** 的 classpath 下跑 CGC 实际用到的 luaj 路径。

`[4]` 显示没有 bcel 也能加载 LuaJC。继续查 luaj-jse 的 POM 是否把 bcel 声明为传递依赖，并直接测试 `LuaJC.install` 路径。

**关键发现**：`luaj-jse` 的 POM 把 `org.apache.bcel:bcel` 声明为**必需传递依赖**——这正是「jarJar 问题」的来源。继续验证包路径与 LuaJC.install 的实际行为。

单独 `install` 不触发编译。补一个真正用 LuaJC 编译 chunk 的用例，确认 bcel 的唯一触发点。

`LoadState.install` 只设 undumper，我上一个用例漏了 `LuaC.install`。修正后重测。

决定性结论已得到：只有在 `LuaJC.install` 生效时 bcel 才被需要。现在看第二点——CGC 的 inline 脚本路径。

TaCZ 的新实现是「反序列化时编译一次，缓存 `CompiledScript`」。现在确认 CGC 侧的调用链和热路径。

CGC 的热路径确认了：每发子弹都会走 `evalByScript` → `ScriptUtils.eval` → 每次重新编译。做个微基准量化差距。

量化结果：**8.4×**（3.53 µs → 0.42 µs）。看一下 TaCZ 当前（origin/1.20.1）还保不保留 bcel。

构建产物在。直接检查已发布的 jar 里 bcel 的实际体积与位置。

验证完成。检查两个仓库未被我的探查弄脏。

三个问题都已查证完毕。以下是结论，含实测证据。

## 1. bcel：确认可删，且是 luaj-jse 的传递依赖

**它只被一个类需要。** 扫描 `luaj-core` / `luaj-jse` 全部 377 个 class，引用 `org/apache/bcel` 的只有 `org/luaj/vm2/luajc/JavaBuilder.class`，而它只被 `JavaGen` 调用，`JavaGen` 只被 `LuaJC` 调用。

**实测（JDK 17 + luaj 3.0.8-figura，附/不附 bcel 各跑一遍）：**

| 路径 | 无 bcel |
|---|---|
| `globals.load` + `LuaC.install`（CGC 现用） | ✅ 正常 |
| `compilePrototype`（TaCZ 新预编译用的） | ✅ 正常 |
| JSR-223 `ScriptEngine` | ✅ 正常 |
| `LuaJC.install(g)` 后再 load | ❌ `NoClassDefFoundError: org/apache/bcel/generic/Type` |

CGC 全仓 `LuaJC` 出现次数为 **0**，所以最后一行永远不会被执行。

**它为什么会在？** 不是有人主动选的——`luaj-jse` 的 POM 把 `org.apache.bcel:bcel` 声明为**必需依赖**（我拉了 jitpack 的 POM 确认）。你说的那个 commit `e40f4be1`「恢复 remote 依赖以确保 jar in jar 工作正常」正是把 `libs/*.jar` 换成 maven 远程依赖的那次：换完之后 Gradle 开始解析 luaj-jse 的传递依赖，于是 bcel 被显式加进 `jarJar`——这就是 commit message 里「jarJar 问题」的实际内容。**jar-in-jar 的修复（改用远程依赖）仍然必要；bcel 本身不必要。**

**实际代价**（直接查已构建的 `customgun-26.10.26-forge-1.20.1.jar`）：

```
META-INF/jarjar/bcel-6.6.1.jar    695,309 B   ← 占 6,396,048 B 的 10.9%
```
并且它在 `META-INF/jarjar/metadata.json` 里注册为独立库。顺带一提，TaCZ 自己也还留着（origin/1.20.1 仍在 jarJar bcel），所以这不是「TaCZ 有用所以我们也留」的理由。

## 2. 那行 `//LuaJC.install(globals)`：是从未生效的化石，建议删而不是启用

- `git log -S "LuaJC.install"` 跨全部分支：该行自 2024-12-09 引入起就**一直是注释**，从未取消注释过。
- **它现在根本编译不过**：luaj 3.0 把 LuaJC 移到了 `org.luaj.vm2.luajc.LuaJC`，而 TaCZ 的 `ScriptManager` 用的是 `import org.luaj.vm2.lib.jse.*`；我确认 3.0.8-figura 里 `org.luaj.vm2.lib.jse.LuaJC` **不存在**。而且 3.0 里 LuaJC 实现的是 `Globals.Loader`，不是 2.x 的 `Globals.Compiler`，语义已变。
- **性能理由也站不住**：LuaJC 加速的是「编译 chunk → JVM 字节码」，但 CGC 的脚本 chunk 每次 reload 只编译一次，热路径是**反复调用**已编译的闭包——LuaJC 对此毫无帮助。Issue 里「加载 1 次运行 1 万次」的直觉方向对，但药方开错了（真正该做的是下面第 3 点）。

## 3. inline 脚本预编译：确实是缺口，但和 bcel 无关（你说得对）

CGC 现状：`_SimpleModifierData.scriptFunction` 存 String，每次求值都走 [IGunModifier.java:33](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/modifier/IGunModifier.java:33) → [ScriptUtils.java:31](core/src/main/java/dev/xcolorful/customgun/core/util/ScriptUtils.java:31) → `globals.load(source).call()`，**每次都重新编译**。热路径是 [_GunProjectileConstructor.java:97](core/src/main/java/dev/xcolorful/customgun/core/entity/projectile/_GunProjectileConstructor.java:97)（每发子弹求 5 个属性）和 `AttachmentModifier`。

TaCZ 的新实现（`origin/1.20.1` 的 `2df3c3dc`，2026-10-01）是：`AttachmentScriptManager.compile()` 用 `compilePrototype` 编译一次 → `CompiledScript`；`Modifier` 通过 `@JsonAdapter(ModifierTypeAdapterFactory)` **在反序列化时**编译并缓存；`ParameterizedCache` 直接持 `List<CompiledScript>`。

**实测差距（N=100 万，`y = x * r + math.max(x, 1)`）：**

```
每次编译 (CGC 现状) : 3.53 µs/call
预编译一次 (TaCZ 新) : 0.42 µs/call     → 8.4x
```

用的是 `LuaC` / `compilePrototype`（luaj-core 里的纯 Java 编译器），与 bcel 无关。

⚠️ 一个需要你确认的事实差异：**你本地 `D:\Github\TACZ` 工作区 HEAD 是 2026-05-24 的 `b43eb84c`，里面没有 `AttachmentScriptManager`** ——它在 `origin/1.20.1`（2026-10-02）上。你 IDEA 里看的 TaCZ 窗口如果不是这个 worktree，那我们讨论的是同一份代码；如果是，那预编译那部分你在本地是看不到的。另外 `git worktree list` 显示 TACZ 只有这一个 worktree。

## 建议

**A. 删 bcel（低风险，无行为变化）**：删 [core/build.gradle:23](core/build.gradle:23) 的 `compileOnly`、[forge-compat/build.gradle:102](forge-compat/build.gradle:102) 的 `minecraftLibrary(jarJar(luaj_bcel))` 块、[gradle.properties:48-49](gradle.properties:48) 两行；并从 `luaj-jse` 依赖上 `exclude group: 'org.apache.bcel', module: 'bcel'`，免得 Gradle 继续解析它。CGC 源码里没有 bcel import，所以 `compileOnly` 纯属残留。

**B. 补 inline 预编译（中风险，改动架构）**：这动了 [script-and-modifier.md](docs/architecture/core/gun/script/script-and-modifier.md) 里已写明的设计，按 Rule 3 我先问再动。实现上我不建议照抄 TaCZ 的 `synchronized (GLOBALS)`——CGC 的原意是线程隔离，可以「加载时编译一次 `Prototype`，按线程惰性建 `LuaClosure` 绑到该线程的 `LUA_GLOBALS`」，既保留隔离又不需要锁。顺带该文档已与代码不符（文档说 `ThreadLocal<ScriptEngine>`，代码早已改成 `ThreadLocal<Globals>`，`LUAJ_ENGINE` 已 deprecated 且无调用者）。

**C. 那行注释**：CGC 里没有这行，只有 TaCZ 有；建议在 TaCZ 侧删掉（但按你的规则我不动非本任务分支）。

采用 A 方案。先确认 TACZ 工作区现状（我刚被告知那个文件有变动），再读 CGC 的三个 gradle 文件。

TACZ 已更到 `b482eff8`（1.1.8-hotfix2），`AttachmentScriptManager` 现在本地就有了——之前的差异说明作废。现在执行 A。

`gradle.properties` 被外部改回去了（git diff 里没有它），而另外两个文件正常。先读取当前实际内容。

用户自己把那两行注释掉了（保留出处），我按当前状态不动它。现在跑构建验证 bcel 确实从产物中消失。

Gradle 8.8 跑在 Java 25 上（major version 69）。改用 JDK 17。

构建成功。验证新产物里 bcel 是否已消失，并读取两个文件的当前实际内容。

构建成功，bcel 已从产物中消失。记录两条经验，然后总结。

A 方案完成并已验证。

## 改动（3 个文件，未提交）

| 文件 | 改动 |
|---|---|
| [core/build.gradle:23](core/build.gradle:23) | `compileOnly luaj_bcel` 注释掉 |
| [forge-compat/build.gradle:98](forge-compat/build.gradle:98) | 删除 bcel 的 `minecraftLibrary(jarJar(...))`；改为在 `luaj-jse` 上 `exclude org.apache.bcel:bcel` |
| [gradle.properties:48](gradle.properties:48) | `luaj_bcel` / `luaj_bcel_version` 注释掉 |

两处你把我原本的「删除」改成了「注释保留」（`//    compileOnly ...`、`#luaj_bcel=...`），我按你改后的状态保留，没有回退。

## 验证

`JAVA_HOME=/e/Software/Minecraft/JDK/jdk-17 ./gradlew :forge-compat:jarJar` → **BUILD SUCCESSFUL**（1m39s，仅有既存的 `WeightModifier` deprecated 警告）。

产物对比 `customgun-26.10.26-forge-1.20.1.jar`：

```
改动前  6,396,048 B   META-INF/jarjar/bcel-6.6.1.jar 在列
改动后  5,726,603 B   消失，metadata.json 也不再注册 bcel
```

减少 **669,445 B（约 655 KiB / 10.5%）**，jar 内 bcel 相关条目为 0。`luaj-core` / `luaj-jse` / `commons-math3` / `mixinextras-forge` 均正常保留。

> 顺带一个环境坑（已记入记忆）：PATH 上的 `java` 是 25，Gradle 8.8 直接报 `Unsupported class file major version 69`，必须用 JDK 17 跑。

## 未做

- **B（inline `script_function` 预编译）**：按你的选择搁置。结论仍然有效——CGC 每次求值重新编译（3.53 µs），TaCZ 的 `AttachmentScriptManager` 预编译一次（0.42 µs），**8.4×**，且与 bcel 无关。要不要跟进由你定。
- **C（TaCZ 那行 `//LuaJC.install`）**：结论是可以直接删（包路径已迁移到 `org.luaj.vm2.luajc`，按原文 import 根本编译不过），但按你的规则我没动 TACZ 仓库。你已把 TACZ 本地更到 `b482eff8`，`AttachmentScriptManager` 现在本地可见，我之前提到的那个版本差异作废。

按 CLAUDE.md 规则，改动已就位、未提交，等你 review。

# 
---

```
看下最近的#69合并怎么在neoforge-compat/build.gradle同步修改?
```

# 

让我先看看 #69 那个提交改了什么，以及当前 neoforge-compat/build.gradle 的状态。

#69 提交里改了 core、forge-compat、gradle.properties，但没改 neoforge-compat——工作区里那段注释看起来是手动补的。让我看看提交的实际改动内容。

提交里还带了个设计文档，先读它看设计意图。

现在对比清楚了：forge-compat 侧 #69 加了两处改动，而 neoforge-compat 工作区只注释掉了 jarJar 块，缺了 luaj-jse 上的 exclude。先验证 neoforge 侧 bcel 是否仍会被传递进来。

JDK 17 可用。运行依赖报告看 bcel 是否仍在 jarJar 配置里。

jarJar 配置里已经不含 bcel 了。但依赖报告未必展示传递依赖，我用真实产物验证。

那个 jar 是 14:29 构建的，含 bcel。需要确认它是改动前还是改动后构建的。

jar 是改动前（14:29）构建的，所以含 bcel 不能说明问题。先确认 luaj-jse 的元数据里 bcel 是不是传递依赖。

forge 侧的 `commons-lang3` exclude 是 #69 之前就有的，说明 ForgeGradle 的 jarJar 会拉传递依赖；但 neoforge 侧没写任何 exclude，产物里也没有 commons-lang3——所以这边 jarJar 看起来不传递。用真实构建定论。

离线模式缺 Minecraft 资源，改为联网构建（耗时较长）。

网络不可用，无法完整构建。改用 init script 直接检查 jarJar 配置的传递性并解析文件（离线可行）。

决定性结论：NeoGradle 的 `jarJar` 配置是**非传递的**（`isTransitive=false`），解析结果里根本没有 bcel。再确认全仓没有其它活引用。

## 结论：neoforge 这边只需要注释掉那三行，**不需要**像 forge 那样加 `exclude`

`#69`（7b2a8c4dc）只动了 `core/build.gradle`、`forge-compat/build.gradle`、`gradle.properties`，没碰 neoforge-compat。而工作区里 [neoforge-compat/build.gradle:86-88](neoforge-compat/build.gradle:86) 已经被注释掉了（未提交，时间戳 23:33，晚于那条提交）。

我验证了这个同步是**完整且正确**的，差异不是遗漏：

| 模块 | 配置 | 传递性 | 移除 bcel 的做法 |
|---|---|---|---|
| forge-compat | `minecraftLibrary(jarJar(...))` | **传递** | 注释块 **+** 在 luaj-jse 上 `exclude org.apache.bcel:bcel` |
| neoforge-compat | `jarJar(...)` | **非传递** | 只注释块 |

证据：用 init script 打印出 `configurations.jarJar.isTransitive() == false`，且解析结果只有 `luaj-core` / `luaj-jse` / `commons-math3`，没有 bcel。这也解释了为什么 forge 侧早就写着 `exclude group: 'org.apache.commons', module: 'commons-lang3'` 而这种 exclude 在 neoforge 侧从未需要——ForgeGradle 那个包装会拉传递依赖，NeoGradle 的裸 `jarJar` 不会。

`gradle.properties:57-58` 和 `core/build.gradle:23` 是根/共享文件，`#69` 的注释已经同时覆盖两个 loader，无需再改。

## 未完成项（按 Rule 10 明确说明）

**产物验证没做**。`neoforge-compat/build/libs/customgun-26.10.26-neoforge-1.20.2.jar` 是 14:29 构建的（早于改动），里面仍有 `META-INF/jarjar/bcel-6.6.1.jar`。当前机器**没有网络** —— `:neoforge-compat:jarJar` 联网跑挂在 ForgeGradle 插件解析上，`--offline` 跑挂在 Minecraft 资源下载上。所以「bcel 从产物中消失」这一点尚未实测，等有网时补跑一次 `:neoforge-compat:jarJar` 即可。

我没有提交任何改动，等你 review。

（另：要不要在注释块上补一句跟 forge 侧对称的 `// bcel 仅 LuaJC 使用，CGC 未启用 LuaJC`？目前那三行是裸注释，你决定。）

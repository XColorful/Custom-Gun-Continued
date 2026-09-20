> #28 提示词

```
当前工作在Custom-Gun-Continued-1.20.2目录，分支为1.20.2
阅读最新commit(PR)里forge-compat部分的修改，对照着完成neoforge-compat目录下对应的实现
- 命名和写法对照已有的NeoForge跟Forge之间的对应
- 完成后通知我检查
```

# 

```
模组移植到1.20.2，ServerPlayer的latency字段没了，ServerPlayerMixin不能Shadow这个字段

当前IDEA窗口打开了1.20.1和1.20.2，供浏览源码

现在需要解决这个移植问题，看下怎么获取ServerPlayer延迟
```

# 

I'll investigate how `latency` was used in `ServerPlayerMixin` and how to obtain it in 1.20.2. Let me start by locating the relevant files.

The project already has a `ServerGamePacketListenerImplMixin`. Let me verify how latency is exposed in 1.20.2 by inspecting the actual Minecraft source through the IDEA MCP server.

I've confirmed the key fact: in 1.20.2, `latency` moved from `ServerPlayer` to `ServerCommonPacketListenerImpl` (field `private int latency` + getter `public int latency()`). Now let me confirm how to reach it from `ServerPlayer` — the `connection` field's type and access.

Confirmed. In 1.20.2, `ServerPlayer` no longer has `latency`; the value moved to the connection. `ServerPlayer.connection` is `public ServerGamePacketListenerImpl` (line 154), which extends `ServerCommonPacketListenerImpl`, whose `public int latency()` getter (lines 145-147) now holds the value.

The fix is version-local to `ServerPlayerMixin` — shadow `connection` instead of `latency`, and call `.latency()`. Let me apply it.

Now let me verify the final file and check for any compilation issues via the IDE.

修复已完成。以下是根因与改动说明。

## 根因

1.20.2 里 Mojang 做了一次网络层重构，把延迟（ping）从玩家实体移到了连接对象上：

- `ServerPlayer.latency` 字段被删除。
- 延迟现在存放在 `net.minecraft.server.network.ServerCommonPacketListenerImpl`（`ServerGamePacketListenerImpl` 的父类）里：私有字段 `private int latency`，公开 getter `public int latency()`（源码第 145–147 行）。
- 从玩家拿连接走 `ServerPlayer.connection`，类型是 `public ServerGamePacketListenerImpl connection`（`ServerPlayer` 源码第 154 行）。

## 改动

[ServerPlayerMixin.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/ServerPlayerMixin.java:31)

1. 删掉失效的 `@Shadow public int latency;`
2. 改为 shadow 连接字段：

```java
@Shadow
public ServerGamePacketListenerImpl connection;
```

3. `cgc$getShooterLatencyMs()` 改为：

```java
@Override
public int cgc$getShooterLatencyMs() {
    return this.connection.latency();
}
```

并补了 `import net.minecraft.server.network.ServerGamePacketListenerImpl;`。

## 需要你决策的一点（CLAUDE.md 要求报告新平台 API）

这次引入了新的平台 API `ServerPlayer.connection.latency()`。我的判断是**不需要额外加兼容封装**：`IShooterLatency` 接口已经是跨版本抽象，`ServerPlayerMixin` 本身就是每个版本分支各自的实现层，这个改动完全局限在 1.20.2 分支自己的 mixin 内，跨版本共享代码（`IShooterState.getShooterEyePos` 等）只依赖 `cgc$getShooterLatencyMs()`，签名没变。

## 两点提示

- **潜在空指针**：`connection` 理论上可能为 null（极少数假玩家场景）。但 `cgc$getShooterLatencyMs()` 只在服务端真实玩家射击时被调用，1.20.1 的 `latency` 字段也从未判空，行为一致，所以没加判空。如果你希望防御性判空（`connection == null ? 0 : connection.latency()`），告诉我。
- **IDEA 误报**：`get_file_problems` 对 `cgc$getHistoryHitbox` / `cgc$resetHistoryHitbox` / `cgc$getShooterLatencyMs` 报了 3 个 "Method does not override"——这些是接口方法签名完全匹配的误报（`@Override` 注解我并未改动，属预先存在）。本次新增的 shadow 字段和方法调用**没有**报任何错误。

按 CLAUDE.md，我停在改动这一步，未提交，等你 review。

# 
---

```
PR#57的内容，目前1.20.1forge和neoforge都测试没问题，1.20.4forge测试没问题，1.20.4neoforge有生存和创造都不能开枪，表现为本地显示的枪械数值没动，怀疑是本地没通过检测提前取消，或者服务端拦截；需要检查是哪里露移植了还是neoforge的问题；log在D:/Temp-File/debug.log
```

# 

I'll start by reading the log and pulling up PR#57 context.

Now let me examine the log for errors and find what PR#57 contains.

The log is dominated by mixin debug spam. Let me filter for actual gameplay signal and get PR#57 details.

Let me look at the tail of the log where the firing attempt happens, and find PR#57's changes in git.

Found the root cause in the log — a server-side `IllegalAccessError`. Let me inspect the relevant code and access-transformer config.

Root cause is clear. Let me verify how this is wired in build config and whether other branches have the AT entry.

Let me read the failing code path and check whether PR#57 touched it.

This is a pre-existing 1.20.4 defect (commit `7ab98cb14b`, Sept 5), not a PR#57 regression. Let me confirm how the back-port handled access and check what 1.20.4 actually exposes.

The AT file was never updated for this field. Let me check the project's own access-transformer mechanism and the 1.20.1 AT contents.

Let me check for a Forge run directory and logs from the Forge test to compare behavior.

Let me check how `core` is compiled, then verify the current build state.

`core` compiles against **Forge**, not NeoForge — that's likely the crux. Let me verify the field's actual accessibility in each runtime jar.

Mystery solved on the Forge side. Let me confirm the NeoForge runtime and check whether a public accessor exists.

Let me check the vanilla 1.20.4 jar to see if a public accessor exists (which would allow a cleaner fix).

Vanilla 1.20.4 has a **public** accessor. Let me confirm its Mojang-mapped name.

Let me verify the accessor's implementation via bytecode.

Let me check for other occurrences of this class of bug in the log, and verify the 1.20.2 baseline.

Everything is now confirmed. Let me check all callers, then apply the fix.

Single call site. Applying the fix.

Now let me verify it compiles.

While the build runs, let me check existing memory to see where this finding belongs.

Compiles clean. Let me build the NeoForge jar and verify the emitted bytecode uses the public accessor.

Build succeeded. Let me verify the emitted bytecode in the built jar.

The jar still shows `getfield` — the bundled core classes may be stale. Let me check directly.

The bundled core class is correct; I'd inspected a stale jar. Let me verify the freshly built one.

Bytecode confirmed clean. Let me produce the final distributable jar for testing.

Let me record this trap in memory while the build finishes.

Let me verify the final distributable jar.

根因找到了，**不是 PR#57 引入的回归**，也不是 NeoForge 的 bug，是 1.20.4 分支的移植遗漏。

## 直接原因

log 里 `D:/Temp-File/debug.log` 有 204 次同一个异常，每次开火都抛：

```
IllegalAccessError: class dev.xcolorful.customgun.core.util.ServerUtils tried to access
private field net.minecraft.server.MinecraftServer.tickTimesNanos
  at ServerUtils.getTickTimesNanos(ServerUtils.java:11)
  at LivingShooterShoot.isInServerShootCooldown(LivingShooterShoot.java:160)
  at LivingShooterShoot.shoot(LivingShooterShoot.java:94)
  at C2SMessageShooterShoot.handle(...)
```

C2S 开火包在服务端被整个中断 → 没有消耗、没有状态同步、没有射击。这正好解释「生存和创造都开不了枪」——它跟弹药无关，走的是冷却/时间戳校验这条共同路径，也解释了「本地数值没动」。

## 为什么 Forge 好、NeoForge 坏（关键）

`core/build.gradle` 用的是 **ForgeGradle + `net.minecraftforge:forge:1.20.4-49.2.4`**，core 的编译产物再被打进 forge 和 neoforge 两个 jar。所以 core 里对 vanilla 成员的访问是否合法，是按 **Forge 的可见性**判定的。

用 `javap -p` 对比三个 jar 实测：

| | `tickTimesNanos` | `getTickTimesNanos()` |
|---|---|---|
| vanilla 1.20.4 | `private final long[]` | `public long[]` |
| Forge（AT 产物） | **`public final long[]`** | `public` |
| NeoForge 运行时 | `private`（保持原样） | `public` |

Forge 自带的 AT 把那个字段放开了，所以 Forge 编译过、跑也没事；NeoForge 没放开 → 运行时 link 失败。

## 改动

[ServerUtils.java](core/src/main/java/dev/xcolorful/customgun/core/util/ServerUtils.java) 一行：改调 vanilla 已有的 **public** 访问器 `server.getTickTimesNanos()`。已用 `javap -c` 确认它就是个纯 getter（`getfield; areturn`，不 clone、无副作用），行为与原字段读取完全等价。

这样**不需要加 AT**——vanilla 本来就有 public 入口。这跟你之前定的「私有成员优先加 AT」有点出入，因为这里根本不用碰私有成员；如果你更希望走 AT 那条路（两个 AT 文件都补条目），告诉我，我改。

验证：
- `:core:compileJava` 通过
- 两个 jar 都重新构建，`javap` 确认产物里已经是 `invokevirtual getTickTimesNanos`，不再有 `getfield`
- 全 log 里这一类错误只有 `tickTimesNanos` 一个，没有其它同源隐患
- 未引入任何平台 API，无需 compat wrapper；未提交，等你 review

构建产物在 `neoforge-compat/build/libs/customgun-0.0.17-neoforge-1.20.4.jar`（22:11），可以直接拿去测。

关于 PR#57：它的提交（dummy ammo / 统一弹药消耗那批）都没碰这条路径；`isInServerShootCooldown` 和 `ServerUtils` 来自 2026-09-05 的 `05e6afb78`（Port #29）。PR#57 只是把这个问题暴露出来了——之前大概没在 1.20.4neoforge 上真正开过枪。

一个值得记住的推论：**因为 core 是按 Forge 编译的，只在 Forge 上试跑通过不能证明 NeoForge 可用**。我已经把这条陷阱和判据（查 vanilla jar 而不是 Forge 的 AT 产物）记进记忆了。

那三个后台命令只是之前超时的搜索收尾，结果都印证了前面的结论（它们跑在改动之前，所以还显示旧状态）：

- `tickTimesNanos` 字段引用全仓库只有一处：`ServerUtils.java:11` —— 已被改掉，没有别的地方需要动
- `ServerUtils` 只有一个调用方：`LivingShooterShoot.java:160`

没有新的发现，之前给你的结论和改动都成立。等你 review 后决定是否提交，或者要不要改成走 AT 那条路。

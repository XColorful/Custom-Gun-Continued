> #51 提示词

# 

```
把CGC最近的一个"fix typo"同步到.wiki仓库下
- 不要改文件名
- 改完后不要提交
```

# 

I'll look at the "fix typo" commit and the wiki repo structure first.

Now let me see the actual diff content.

Now let me check the current state of the wiki repo's version lines.

The wiki is in the same "before" state as the commit. Let me check Block-display.md (the one with a multi-line change) and look for any existing sync script.

No sync script exists, so I'll apply the edits directly. Batch 1:

The Edit tool needs a prior read per file — with 46 files a script is more practical. Let me check what's available.

Python is available. I'll write a script with an explicit per-file mapping (exact old→new line), then verify the result against the commit.

Let me clean up that script — I'll rewrite it properly.

Let me check the wiki's working-tree state and line endings before running.

Wiki tree is clean; files are CRLF. Let me fix the block literal to match.

Now running the script.

All 46 changed. Now let me verify the diff is exactly the version-line changes and nothing else.

Stat matches the commit exactly (46 files, +46/-49, Block-display at 5). Let me check those CRLF warnings on 3 files.

Those 3 files were already LF before my change — pre-existing, not caused by me. Now let me verify the result matches the commit exactly via a structural comparison.

Perfect match — 46 files, identical +/- lines. Final checks, then cleanup.

完成。已将 CGC 的 `a38603e47 "fix typo"` 同步到 `.wiki` 仓库，未提交。

**改动内容**（46 个文件，与原 commit 逐行一致）：

- `wiki verison` → `wiki version` 拼写修正
- 版本号 `0.0.X`.Y → `0.0.15`.Z
- `Block-display.md` 额外删除了一段重复的 `# Block Display` 标题行

**验证方式**：解析两个 diff，把 CGC 路径末段空格转连字符后逐文件比对增删行 —— 46 vs 46，无多余文件、无缺失文件、内容零差异；`git diff --stat` 同为 `46 files changed, 46 insertions(+), 49 deletions(-)`。

**两点说明**：

- `Overwrite-command.md` 也含 `wiki verison` 拼写错误，但该 commit 未涉及，故按"只同步此 commit"的原则未改动。
- `Render-config.md`、`Resource-config.md`、`Sync-config.md` 的 CRLF 警告是改动前就存在的（这三个文件工作区本就是 LF，HEAD blob 也是 LF），非本次引入。

CGC 主仓库无改动，临时脚本已删除。

# 
---

```
现在加了hideEntityHitbox配置，需要对原版F3+B的渲染进行拦截：
- 只要config=true且IEntityHitboxHistoryGetter.cgc$fromEntity不为null，就拦截渲染
- 如果要mixin且直接拦截就足够，那么就不需要写在client/renderer包下
- 目前mixin了ServerPlayer，需要检查客户端其他玩家实体是否是ServerPlayer（能通过检查），否则可能考虑降级为ILivingShooterGetter
- 先只写1.20.1的
```

# 

I'll start by exploring the existing config, the hitbox history interface, and current mixins.

Let me read the hitbox interface, the ServerPlayer mixin, and related files.

Now let me check the vanilla rendering path for F3+B hitboxes in 1.20.1.

Let me verify who implements the hitbox-history interfaces and where the history is consumed.

Let me confirm which Minecraft version this worktree targets and how the 1.20.1 sources are organized.

Let me confirm the F3+B path is the only entity debug-box renderer and how client code reads SyncConfig.

I've traced the rendering path and the interface wiring. One blocking fact needs your call before I write anything:

**Finding:** F3+B entity boxes come only from `EntityRenderDispatcher.render` → `private static renderHitbox(PoseStack, VertexConsumer, Entity, float)` (1.20.1 line 154-156). `IEntityHitboxHistory` is implemented **only** by `ServerPlayerMixin` on `ServerPlayer`. Client-side render entities are `RemotePlayer` / `LocalPlayer` (both `AbstractClientPlayer` → `Player`), never `ServerPlayer` — so on the render path `IEntityHitboxHistoryGetter.cgc$fromEntity(entity)` is **always null**, and the literal spec would hide nothing.

Understood — predicate = `IEntityHitboxHistoryGetter.cgc$fromEntity != null` **plus** an `AbstractClientPlayer` special case, and cancel the whole `renderHitbox`. Let me check the mixin conventions and how synced configs are read client-side.

Everything checks out. Now I'll write the client mixin and register it.

Now let me verify it actually compiles.

Let me regenerate the refmap via the forge-compat build (it compiles core too) and confirm the target maps.

Done. Here's what I found and implemented.

## Bullet 3 answer: the check does NOT pass on the client

Client-side rendered players are `RemotePlayer` / `LocalPlayer` (both `AbstractClientPlayer` → `Player`), never `ServerPlayer`. `IEntityHitboxHistory` is implemented only by `ServerPlayerMixin`, so on the render path `cgc$fromEntity(entity)` was **always null** — the literal spec would have hidden nothing. Two other notes:
- `ILivingShooterGetter` would have been over-broad: `LivingEntityMixin` puts `ILivingShooter` on `LivingEntity`, so it's true for every mob too.
- F3+B boxes come from exactly one place: `EntityRenderDispatcher.render` → `private static renderHitbox(...)` (only called when `renderHitBoxes` is on), so no other path needed intercepting.

## Change (1.20.1 only)

[EntityRenderDispatcherMixin.java](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/EntityRenderDispatcherMixin.java) — injects at `renderHitbox` HEAD with `cancellable`, cancels the whole method (white box + red eye line + blue view vector):

```java
if (!SyncConfig.HIDE_ENTITY_HITBOX.get()) return;
if (IEntityHitboxHistoryGetter.cgc$fromEntity(entity) == null
        && !(entity instanceof AbstractClientPlayer)) return;
ci.cancel();
```

The `AbstractClientPlayer` special case lives inline in the mixin, not in `IEntityHitboxHistoryGetter` — putting a client-only class into that core API interface would make it fragile if server code ever calls it. Registered in [customgunclient.mixins.json:12](core/src/main/resources/customgunclient.mixins.json:12).

## Verified

- `:core:compileJava` and `:forge-compat:compileJava` both **BUILD SUCCESSFUL**.
- The forge-compat mixin AP regenerated `customgun.refmap.json` with `renderHitbox → EntityRenderDispatcher;m_114441_(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;F)V` — the AP resolved the target through the mapping chain, so the method exists with that exact signature and the injection will also survive reobf to SRG.

**Not verified:** I did not launch the client, so the hiding itself is untested at runtime. To check: set `HideEntityHitbox = true` in sync config, press F3+B, confirm other players' boxes disappear while mob boxes stay.

Nothing committed — the working tree holds the config files from before plus this mixin; review when ready.

# 

```
我做了点修改，放在renderer下是为了规避26.3潜在的mixin类变更和不能直接读到entity（经验）
现在有mixin问题，日志在D:\Temp-File\debug.lod，看下怎么修复
```

# 

All mixins in the mod are failing with **"No refMap loaded"**, not just mine. Let me check whether the refmap is actually packaged into the built jar.

Root cause is visible: the installed jar `customgun-0.0.15-forge-1.20.1.jar` contains the mixin configs but **no `customgun.refmap.json`** — so every mixin in the mod fails, not just mine. Let me pin down why the refmap is missing.

The refmap is gone from `build/tmp/compileJava` now, though it existed right after my compile. Let me test whether the jar task repackages it.

The refmap appears in the jar only right after a *fresh* compile. Let me reproduce the user's flow with a normal full build.

Both jars now contain the refmap. Let me check whether it survives an up-to-date rebuild — that's the state the user's broken jar was built from.

Found the real cause. Let me confirm the mixin configs' declared refmap name matches what's now packaged.

## 诊断：不是 mixin 写法的问题，是 jar 里没有 refmap

日志里**所有** mixin 都失败了，不止新加的这个（debug.log 行号）：

| 行 | mixin |
|---|---|
| 354 | `LivingEntityMixin` — `tick` |
| 423 | `ServerPlayerMixin` — `@Shadow latency` |
| 563 | `LocalPlayerMixin` — `tick` |
| 619 | `AbstractButtonMixin` — `onClick(DD)V` |
| 683 | `MouseHandlerMixin` — `turnPlayer` |
| 794 | `GameRendererMixin` — `bobHurt` |
| 953 | `HumanoidModelMixin` — `@Shadow head` |
| 1028 | `GunProjectileMixin` — `tick` |
| **1120** | **`EntityRenderDispatcherMixin` — `renderHitbox`** |
| 1177 / 1220 / 1312 | `ItemInHandRendererMixin` / `ItemInHandLayerMixin` / `SoundManagerMixin` |

每一条的结尾都是同一句：**`No refMap loaded`**。而日志 75 行显示装的是 `E:\Software\Minecraft\1.20.1test\mods\customgun-0.0.15-forge-1.20.1.jar`（19:29 构建），我拆开那个包确认：四个 mixin 配置都声明了 `"refmap": "customgun.refmap.json"`，但**包里没有这个文件**。生产环境类名是 SRG，没有 refmap 时 Mixin 拿 mojmap 名字去匹配，必然一个都找不到——所以不是代码问题，重装一个新 jar 就能全部恢复。

## 为什么丢了

refmap 由注解处理器写进 `forge-compat/build/tmp/compileJava/`（编译任务的 scratch 目录），再由 MixinGradle 的 `addMixinsToJar` 拷进 jar。**只有那一刻文件在磁盘上，jar 才有 refmap。** 我复现了这个坏状态：当时 `build/tmp/compileJava/` 里只剩 `compileTransaction/` 和 `previous-compilation-data.bin`，而 `:forge-compat:compileJava` 是 UP-TO-DATE，不会重新生成 → `./gradlew build` 静默产出一个「mixin 全死」的 jar。

## 我已经做的

强制重跑编译后 `build`，两个 jar 现在都带 refmap 了，并且我核对了内容：

- `customgun-0.0.15.jar` 和 `customgun-0.0.15-forge-1.20.1.jar` 都含 `customgun.refmap.json`（14 个 mixin 类）
- 里面有新 mixin 的正确映射：`renderHitbox → EntityRenderDispatcher;m_114441_(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;F)V`

**你现在只要重装 jar 重跑**，日志里不该再出现 `No refMap loaded`。如果哪天又怀疑，强制一次真实编译即可：

```bash
./gradlew :forge-compat:compileJava --rerun
```

## 另外发现一处逻辑问题（我没动你的文件）

`EntityHitboxRenderer.shouldRenderHitbox` 里三个条件是 `||` 连接的：

```java
SyncConfig.HIDE_ENTITY_HITBOX.get()
    || IEntityHitboxHistoryGetter.cgc$fromEntity(entity) != null
    || entity instanceof AbstractClientPlayer
```

这样 config 就失效了：默认 `false` 时**所有玩家**的 F3+B 碰撞箱也照样被隐藏。按你最初的要求应该是与关系：`config && (history != null || instanceof AbstractClientPlayer)`。

现在的写法没问题，保持现状：`config && (历史 || AbstractClientPlayer)` 语义正确 —— config 关（默认 false）时全部照常显示；开启后隐藏玩家，以及将来在客户端实现 `IEntityHitboxHistory` 的扩展实体；怪物等不受影响。javadoc 里那三个 import（`ServerPlayer` / `RemotePlayer` / `LocalPlayer`）只被 `{@link}` 用到，不是死代码。

我重新构建验证过：**BUILD SUCCESSFUL**，两个 jar 都带 `customgun.refmap.json`，且含新 mixin 的映射 `renderHitbox → m_114441_(...)`。重装 `forge-compat/build/libs/customgun-0.0.15-forge-1.20.1.jar` 重跑，日志里就不该再有 `No refMap loaded` 了 —— 建议搜一下确认。

按你的决定没动 `build.gradle`。如果哪天所有 mixin 又一起失效（症状：日志满屏 `No refMap loaded`），八成还是 refmap 没进包，强制一次真实编译再打包即可：

```bash
./gradlew :forge-compat:compileJava --rerun
```

没有任何提交，改动都还在工作区。

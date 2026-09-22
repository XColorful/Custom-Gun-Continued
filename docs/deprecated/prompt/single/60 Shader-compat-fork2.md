# 

```
1.21.6的问题先不管（装iris+sodium，开不开光影都会有问题），不开光影时有问题疑似是iris/sodium版本比1.21.10老导致的
1.21.10、1.21.11现在装不装iris和sodium都没问题，但是开光影仍然有问题
目前先不管光影兼容问题，把1.21.10dev.xcolorful.cgccompat.shader.client.mixin.iris.IrisCompatMixin过编译就行，需要检查是否还对应旧行为
```

# 

New repos added, and there are relevant memories from other sessions. Let me read those first.

The loader-deltas memory covers this exactly. Now let me look at what's actually in the two 1.21.10 repos.

The mixin already exists in this repo — let me read it and the build wiring.

The 1.21.10 mixin has a 4th injection (`isHandPoseStackWorldSpace`). Let me check the CGC-side stub and the build wiring, then just build to see the real error.

The stub has a 5th method (`registerRenderPipelines`) the mixin doesn't cover. Let me build to get the real error while I investigate.

**Found the compile blocker**: Iris 1.9.7 has no `batchedentityrendering` package at all. Let me find what replaced it.

Build still running. Meanwhile, let me find where entity batching lives in Iris 1.9.7.

Iris 1.9 dropped its whole batched-entity-rendering module. Let me check the sibling 1.21.11 repo and CGC's own comments for what replaced it.

Sodium's real code is in a nested jarjar jar. Let me look there and confirm Iris no longer touches `RenderBuffers`.

Sodium has a `RenderBuffersMixin`. Let me decompile it to find the modern equivalent.

The comment says Iris _still_ swaps `bufferSource` on 1.21.6+ — so it moved, not vanished. Let me find it.

`MixinLevelRenderer` in Iris 1.9.7 references `RenderBuffers`. Let me decompile it and read the design doc.

A prior session's report (in the 1.21.10 repo docs) claims Iris 1.9.6 **did** have that class at 1.21.6. Let me pin down what 1.21.10 actually has.

Decisive: Iris **1.9.6** had `batchedentityrendering`; **1.9.7 (1.21.10) removed the whole module**. Let me verify the surviving hooks against 1.9.7.

All three remaining hooks verified present in Iris 1.9.7. Let me check the build result and the 1.21.11 jar.

Confirmed the same for 1.21.11 (Iris 1.10.7). Stopping the stale build and applying the fix.

Now building the 1.21.10 compat mod.

The mixin now compiles. Next blocker is a pre-existing NeoForge loader API change in `CgccShaderNeoforge`. Let me look.

Mixin compiles. Verifying the compiled class and checking how CGC 1.21.10 itself handles the loader dist API.

Compiled mixin verified — three injections, no `endBatch`. Let me check the last piece: whether the remaining hooks are actually live on 1.21.10.

Mixin is done and compiling. Let me record the version fact that drove the fix, then report.

Note: `CgccShaderNeoforge.java` is modified in the working tree — that's **not my edit** (looks like you fixed it in IDEA while I worked). Let me verify the repo builds with your fix in place.

BUILD SUCCESSFUL. Verifying the packaged jar.

做完了，而且整个仓库现在能构建通过。

## 为什么原来编译不过

`net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource` 在 **Iris 1.9.7 里整个模块都没了**（1.9.6 还在）。四条独立证据：

- Iris 1.9.7+mc1.21.10 的 jar 里 `grep -i batched` 零命中，manifest 里也没有 `iris-batched-entity-rendering.mixins.json`
- 整个 jar 里**没有任何类 extends `MultiBufferSource$BufferSource`**（1.20.1 的 Oculus 里 `FullyBufferedMultiBufferSource` 就是这么一个类）
- Sodium 侧无关：0.7.3 的 `RenderBuffersMixin` 只替换 `SectionBufferBuilderPool`，不动 `bufferSource()`

所以 **Iris 1.9.7 起 `RenderBuffers.bufferSource()` 就是原版的**，`endBatch(RenderType)` 本来就是真提交 —— 那个钩子的前提消失了。

## 四个注入逐个对旧行为（你问的就是这个）

|注入|1.21.10 上还成立吗|
|---|---|
|`isRenderShadow`|**成立**。`ShadowRenderingState.areShadowsCurrentlyBeingRendered()` 在 1.9.7 里仍在（只是没了 `renderBlockEntities`，我们不用）|
|`isUsingRenderPack`|**成立**。`IrisApi.getInstance()/isShaderPackInUse()` 仍在。但 CGC 里没有任何调用者（和 1.20.1 一样）|
|`endBatch`|**前提没了，已删注入**。删掉后 stub 返回 `false` → 走原版 `endBatch()` 路径，这正是 1.21.10 的旧行为（这份 mixin 在 1.21.10 从来没编译成功过，所以等于回到"没有它"的状态）|
|`isHandPoseStackWorldSpace`|目标 API 仍在（`HandRenderer.INSTANCE/isActive()`），**但 CGC 侧调用点被注释掉了**（`GunItemRenderer.java:358-367` 整块注释），目前是死代码|

另外 `registerRenderPipelines` 这个 stub 从来没被 mixin 覆盖，而且 CGC 侧被 `ClientRenderRegistry.java:84` 的 `if (true) return;` 短路 —— 也是死代码。Iris 1.9.7 里对应的是新 API `IrisApi.assignPipeline(RenderPipeline, IrisProgram)`，要做激光束那两条自定义管线时用这个。

**别把删除照搬到其他仓库**：Iris ≤1.9.6（1.20.1 Oculus / 1.21.1 / 1.21.4 / 1.21.6）都还该保留 `endBatch` 注入。1.21.11 则和 1.21.10 同病（Iris 1.10.7 也没有该类，我核过了）。

## 构建结果

```
BUILD SUCCESSFUL in 2m 37s
```

产物 `cgccshader-0.0.0-neoforge-1.21.10.jar` 已核对：含 `IrisCompatMixin.class`、`cgccshaderclient.mixins.json`，`neoforge.mods.toml` 里 `[[mixins]] config=` 注册到位，`client` 列表是 `iris.IrisCompatMixin`。编译后的 mixin 只有三个 `@Inject`（`isRenderShadow`/`isUsingRenderPack`/`isHandPoseStackWorldSpace`），`@Mixin` 指向 `IrisCompat`。

**一件要交代的事**：`CgccShaderNeoforge.java:14` 在我第一次构建时还报 `non-static method getDist()`，第二次就变成 `FMLLoader.getCurrent().getDist()` 了 —— **这个改动不是我做的**（我一个字没碰那个文件），看时间点应该是你在 IDEA 里改的。所以我没法说它一定对，只能说带着它构建通过了。我自己查到的等价物是 CGC 那边在用的 `FMLEnvironment.getDist()`（`NeoSideExecutor.java:16`），两种写法都能过编译，你选哪个我不管。

CGC 1.21.10 侧**不需要改**：`IrisCompat` 接口没动，`endBatch` 走 false 分支就是原版路径，也没有 jar 要换。

---

我做不到的：跑游戏验证。三个保留的注入只验到"API 与语义仍对应"，实际画面得你测。开光影那个问题按你说的没碰。

未 commit、未 push。要不要我把同样的 `endBatch` 删除也应用到 `CGC-Compat-Shader-iris-1.21.11neoforge`？（那是另一个仓库，按 CLAUDE.md 得你点头。）

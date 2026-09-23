# 

```
现在移植到1.21.6，1.21.1之后的经过先开一个子Agent读#60 Shader-compat后续内容进行提炼，去掉已经绕完的弯子，避免直接读完浪费上下文

目前1.21.1、1.21.4单独iris+sodium是没问题了，目前#60的修复暂不排查同时安装多mod的兼容问题。
1.21.6有问题：
- 不安装sodium和iris时，正常
- 安装sodium+iris：无论开不开光影，都有红点的准心始终渲染（在ocular范围外），4倍镜（enableScope=true）的ocular始终是模型自身的黑色，且准心也是始终渲染
- 单独安装sodium时，正常
- 以上所有情况都没有出现第一人称枪射物起点偏移问题
- 最后一次安装sodium+iris的log放在D:\Temp-File\debug.log
- 目前怀疑是需要像BattleRoyale xiao.battleroyale.client.renderer.CustomRenderType#onRegisterRenderPipelines那样，给dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry#onRegisterRenderPipelines增加注册，放在IrisCompat里mixin注入
```

# 

I'll locate the #60 doc and check the log size first, then delegate the distillation.

The #60 doc is 1112 lines of accumulated debugging history. Delegating the distillation, then I'll explore the 1.21.6 code in parallel.

Agent is running. Meanwhile, let me look at the lead: the two `onRegisterRenderPipelines` implementations.

Distillation is back — and it flags something important: the doc has **nothing** about `onRegisterRenderPipelines`, so that lead is genuinely untested. Let me read the 1.21.6 code.

CGC's 1.21.6 registry **already** calls `IrisCompat.registerRenderPipelines(registrar)`. Let me see BattleRoyale's Iris glue and CGC's stub.

Found the gap: CGC's `IrisCompat.registerRenderPipelines` stub is **not** in the mixin yet. Let me read BattleRoyale's full Iris registration chain.

Let me stop that runaway grep and find whether a newer CGC branch already has this implemented.

Line 348 is the crux: the stencil hole now goes through `RenderType.debugTriangleFan().draw(...)`. Let me read that whole region and compare with the working 1.21.4 branch.

Now the 1.21.4-vs-1.21.6 differential — this is the cheapest high-value comparison.

Strong differential: 1.21.4 uses raw `BufferUploader.drawWithShader`, 1.21.6 switched to `RenderType.debugTriangleFan().draw()`. Let me check the log for evidence.

Log shows no compat-mod configs loaded and zero rendering warnings — so this reproduces with Sodium+Iris+CGC alone. Let me get the 1.21.6 `RenderType.debugTriangleFan` definition from MC sources.

Line 1052 is the key: `RenderType.draw` only enables stencil if `RenderSystem.STENCIL_TEST != null`. Let me see how CGC's stencil helpers set that.

Now the architecture is clear: CGC conveys stencil state via `RenderSystem.STENCIL_TEST` plus **pipeline modifiers**. Let me read the platform `IStencilOperator`.

Now I have the full mechanism. Let me find how NeoForge's stencil/pipeline-modifier state reaches the draw.

Decisive question: does Iris's buffered flush carry the NeoForge stencil test? Let me find the Iris jar.

`drawInner` only does `drawIndexed` on an already-created `RenderPass` — no stencil. Let me check who creates that pass, in `FullyBufferedMultiBufferSource.endBatch`.

Root cause **confirmed from bytecode**. Let me check one more thing — how NeoForge's pipeline modifiers reach the draw — to scope the fix.

Let me settle whether `RenderType.draw` also picks up the pipeline modifiers (colour/depth write masks).

Root cause fully nailed. Let me verify the `IrisPipelines.assignPipeline` API exists in the 1.21.6 Iris so my advice is accurate.

I've traced this to a source-verified root cause. Let me write it up before changing anything.

子 Agent 的提炼已拿到，我也把 1.21.6 这条链路查到了源码级结论。**先说结论：症状的根因和你的猜测不是同一层，`assignPipeline` 修不了它。**

## 已证实的根因

NeoForge 1.21.6 的模板测试传递链是：

```
NeoStencilOperator.applyStencil()
  → RenderSystem.enableStencil(StencilTest) → RenderSystem.STENCIL_TEST = stencilTest
```

我 grep 了整个 NeoForge 1.21.6 源码（7109 个文件），**`STENCIL_TEST` 只有一个读取者**：

```java
// net/minecraft/client/renderer/RenderType.java:1052  CompositeRenderType.draw(MeshData)
if (RenderSystem.STENCIL_TEST != null) {
    renderpass.enableStencilTest(RenderSystem.STENCIL_TEST);
}
```

而 Iris 的批量实体渲染（`iris-batched-entity-rendering.mixins.json` 是 `"required": true`，装了 Iris 就常驻，跟光影包开关无关）把 `RenderBuffers.bufferSource()` 换成 `FullyBufferedMultiBufferSource`。我反编译了它 1.21.6 版的 `endBatch()`：它**自己**建 `RenderPass`——

```
createRenderPass(...) → setPipeline(...) → bindDefaultUniforms(...) → setUniform(...)
  → BufferSegmentRenderer.drawInner(pipeline, pass, segment)   // 内部只有 upload + setVertexBuffer/setIndexBuffer + drawIndexed
  → pass.close()
```

**整条链没有 `enableStencilTest`。**

对照 vanilla（`MultiBufferSource.java:99`）：`BufferSource.endBatch(rt)` → `renderType.draw(meshdata)` → 走上面那个 1052，模板生效。

⇒ CGC 的瞄具/划分模板 pass 全部经 `renderModelPart` 走 `mc.renderBuffers().bufferSource()` + `IrisCompat.endBatch(...)`，**在 Iris 下就是在「模板测试未启用」的状态里画**：

|stencil func|本该|恒真后|
|---|---|---|
|`GL_EQUAL i+1`（目镜黑遮罩）|只画孔外|全屏 → **ocular 只有模型自身黑色**|
|`GL_EQUAL ~(i+1)`（准心）|只画孔内|全屏 → **准心始终渲染**|

而且 `renderOcularStencil` 的 REPLACE **写入**也走同一路径 → 模板值根本没写进去，恒真得更彻底。

**所有观测都对得上**：Sodium-only 走 vanilla `BufferSource.endBatch` → 正常；无 Iris 正常；1.21.4+Iris 正常，因为那版根本没这套 NeoForge 传递（`_stencilFunc` 注释里就写了 1.21.4 移除 `enableMainRenderTarget`，走的是裸 GL 状态，Iris 裸上传照样吃）；开不开光影包都复现。

另外两点：

- **你给的 debug.log 里没有任何 cgccshader 的 mixin config**，只有 `customgun.*` + `sodium` + `iris` ——即这套症状**不装兼容 mod 就复现**，不是我们 mixin 引入的。
- 顺带查清：`_colorMask(false)`/`_depthMask(false)` 推的 `RenderSystem.pushPipelineModifier` **在 Iris 路径上照样生效**——因为 `GlRenderPass.setPipeline:74` 里 `pipeline = RenderSystem.applyPipelineModifiers(pipeline)`。所以丢的**只有模板测试**这一项。

## 和你猜测的对照

`IrisPipelines.assignPipeline(RenderPipeline, ShaderKey)` 在 1.21.6 的 Iris 里确实存在（我核过 1.8/1.9 的 `IrisPipelines.class`），但它解决的是**「这条管线该绑哪个 shader 程序」**，不会把模板测试带过去 —— 照 BattleRoyale 那样注册补不了这个症状。

你的方向在 `IrisCompat.registerRenderPipelines` 这个桩上仍然有价值，但那是给**激光束那两条自定义管线**用的（Iris 不认识的自定义管线需要 assign shader 程序），是独立的另一件事。

## 修法

模板 pass 必须走 `RenderType.draw` 那条路（即不能经 Iris 的延迟缓冲——而且 Iris 的 flush 是**延迟**的，全局 `STENCIL_TEST` 这种「有状态」传递在语义上根本没法穿过它）。

最小改法：**给这类模板通道一个自己的 vanilla `MultiBufferSource.BufferSource`**。Iris 只替换 `RenderBuffers.bufferSource()`，自有实例不受影响，`endBatch` → `RenderType.draw` → 模板立刻回来。

Implementing the chosen fix. Let me check the exact call sites and the `BufferSource` semantics first.

`ByteBufferBuilder` auto-grows, so sizing is safe. Let me read the exact current code.

Multi-line edits work. Let me read the exact stencil call sites.

Now the main edit — dedicated source + a source-taking overload.

Now the five stencil call sites, each anchored on unique context.

The new doc block landed in front of the existing one. Let me fix the ordering.

Diff is clean, CRLF preserved. Now compiling.

Compiles clean. Let me make sure I didn't miss any stencil-masked draw site.

Found more stencil-masked sites — my fix is incomplete. Let me read them.

`_GunModelRender` also enables the stencil for subsequent gun-body draws. Let me see that method and its caller.

This changes the fix shape: the primary culprit is the **gun body** draw (line 122 `super_render`), which runs inside the stencil window. Let me revert and use a global switch instead — smaller and complete.

Now the three edits.

Minimal, surgical diff. Compiling.

My change orphaned a `Minecraft` import in `ModelObject`. Cleaning that up.

修好了，编译通过。**但我上一轮的判断不完整——"ocular 黑色"的主因不是目镜遮罩，是枪体本身。**

## 补充后的根因

`_GunModelRender._renderScope` 在末尾（161/166 行）设置模板：

```java
// 开启模板测试，因为镜内不渲染枪体
if (enableScope & enableSight) { _stencilFunc(GL_GREATER, 127, 0xFF); }  // 组合镜
else                          { _stencilFunc(GL_EQUAL,  0,   0xFF); }  // 长筒镜
```

然后**不画任何东西就返回**，真正的绘制发生在上层 `_GunModelRender.render`：

```java
_renderScope(...);              // 119: 开启模板 + 设 func
_stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);  // 121
_this.super_render(...);        // 122: ← 枪体，走 ModelObject.render → mc.renderBuffers().bufferSource()
_disableItemEntityStencilTest(); // 126
```

所以 `GL_EQUAL 0`（"镜内不渲染枪体"）的**枪体**是在模板窗口内经共享缓冲源画的 → 模板测试被 Iris 丢掉 → 枪体铺满目镜 → 就是你看到的"模型自身的黑色"。

加上我上轮找到的目镜遮罩（`GL_EQUAL i+1`）和准心（`GL_EQUAL ~(i+1)`），三个贡献者都定位到了。`renderBoth`/`renderScope` 里的镜身（114/158 行 `GL_EQUAL 0`）也同理。

## 改动

把"该用哪个缓冲源"做成一个入口，而不是逐点传参：

```java
// ClientRenderHelper.GL
public static MultiBufferSource.BufferSource getModelBufferSource() {
    if (!stencilEnabled) {
        return Minecraft.getInstance().renderBuffers().bufferSource();   // 原样，Iris 批处理照常
    }
    // 模板测试窗口内：自有 vanilla 源 → endBatch → RenderType#draw → 模板测试生效
    ...
}
```

只改两个取源点：`ModelObject.render`（枪体）和 `_AttachmentModelRender.renderModelPart`（目镜/镜身/准心）。**净 +23 行**（`ClientRenderHelper` +25、另两处各 −1）。

选全局开关而不是给 `renderModelPart` 加参数，是因为 `stencilEnabled` 正好就是模板窗口的边界（`enableItemEntityStencilTest`/`disable...` 成对），开关在**每个**调用点自动正确，不用逐点判断该不该传；非模板窗口下行为完全不变，Iris 的批处理不受影响。也顺手删了 `ModelObject` 里因此变成未使用的 `Minecraft` import。

## 验证到哪一步

- `./gradlew compileJava` 两个模块 **BUILD SUCCESSFUL**（唯一 warning 在无关文件 `HSVSliderGroup`）。
- 三个改动文件 CRLF 保持（459/459、267/267、427/427），花括号配平。
- 兼容仓库**不需要改**（`IrisCompat` 接口没动），也没有 jar 要换——你给的那份 log 是 dev 环境跑的，直接 IDE 起即可。

**跑不了游戏，下面只有你能确认**：装 Sodium+Iris 后 4 倍镜开镜（`enableScope`）目镜里应该是场景而不是黑色、准心只在圆形视野内、红点组合镜同理；再关掉光影包对照一次；以及卸下瞄具后枪体**没有**在残留圆形区域被裁掉（这条是 `stencilEnabled` 门控在保的，别被我改坏）。

## 我没动的地方（都不在你报的症状里，但同属这一类，留着作为下一步候选）

- **手部/瞄具文字**（`HandRender`、`TextRender`）——是 `ModelObject.render` 的 delegate，在模板窗口内画，仍走共享源 → 不会被 `GL_EQUAL 0` 挡在目镜外。
- **枪口火焰/抛壳/激光/瞄准具本体**（`MuzzleFlashRender`、`ShellRender`、`BeamRender`、`AttachmentRender`）——同上。
- `IrisCompat.registerRenderPipelines` 仍是空实现（按你选的"另开一轮"），所以激光束那两条自定义管线在光影下还没绑 ShaderKey。

这些都是"换成 `getModelBufferSource()` 一行"的量级；要的话我可以一并推平，但那会改到特效在窗口内的表现，我没法在游戏里验，所以先停在这里等你测完这一版。未 commit、未 push。

# 

```
现在没开光影的时候，准心确实在ocular范围内了，但是ocular里会显示玩家的手
开光影之后仍然始终可见准心，且倍镜的ocular仍然是黑的
只安装sodium的时候都正常，没有ocular显示手的问题
```

# 

Good progress — that confirms the fix works when no shader pack is on. The hand is one of the gaps I listed. But the pack-on case is a separate mechanism; let me check whether Iris's own render targets even have a stencil attachment.

Critical lead: Iris ships a **stencil-buffer integration-test** mixin that _forces_ a depth-stencil format. Let me see its toggle and target.

`RenderTargets` branches on `isCombinedStencil()` — so the "no stencil" hypothesis needs checking. Let me see which format Iris actually picks.

`IrisRenderingPipeline` derives its depth format **from the main target** — so Iris's targets do get stencil. Let me check the other suspect: Iris has a `MixinRenderPass_Stub`.

Now the confirmed hand issue. The arm/overlay draws are in-window too — let me look at them.

Found it — CGC's own comment says the arm must render **inside the stencil window**, but it goes through the Iris-buffered source. Let me see the exact source line and `TextRender`.

The cwd silently flipped to the 1.20.1 repo partway through — so my last compile verified the **wrong project**. Let me redo it properly with absolute paths.

1.20.1 repo is untouched and clean. Now compiling the correct project.

编译通过了——但**先说一件必须交代的事**：上一条命令里的 `./gradlew` 跑在**错误的仓库**上。shell 的工作目录在中途被静默切回了 `D:\Github\Custom-Gun-Continued`（1.20.1 那个 worktree），所以那次 "BUILD SUCCESSFUL" 验的是 1.20.1。同一轮里我还对着 1.20.1 跑过一次 `git checkout -- core/.../_AttachmentModelRender.java`——**那个文件当时是干净的，没有丢任何东西**（1.20.1 仓库现在仍是 clean，我核过了）。已改用绝对路径 + `-p` 重跑，这次 `:core:compileJava` 与 `:neoforge-compat:compileJava` 都真的执行了：

```
BUILD SUCCESSFUL in 13s
```

## 手的问题：已修

`ClientRenderHelper.renderFirstPersonArm` 里你们自己写着这个意图：

```
 * 1.21.10起 AvatarRenderer#renderHand 改成通过 SubmitNodeCollector 延迟提交
 * 手会在模板测试关闭后才渲染，导致开镜时手穿透镜片
 * 这里按 1.20.1-1.21.6 的即时模式直接渲染手臂，使手的渲染仍处于模板测试期间
```

但手臂的顶点取的是 `Minecraft.getInstance().renderBuffers().bufferSource()`（113 行）——即被 Iris 换掉的全缓冲源，于是"立即"只对 vanilla 成立，一到 Iris 就变成延迟 flush，直接掉出模板窗口。**正是你说的"只装 Sodium 正常、装 Iris 目镜里出现手"。**

顺着这条把窗口内的另外两处也换成同一个源：

- `ClientRenderHelper:113`（手臂本体）+ `HandRender:49` 的 flush 换成 `GL.getModelBufferSource()`
- `TextRender:80`（瞄具文字，同属枪械模型的 delegate）同样换掉，并清掉因此未使用的 `Minecraft mc` 局部变量

现在共 7 个文件有改动，其中 `IrisCompat.java`、`ClientRenderRegistry.java` 是你原有的未提交改动，我实际动的是 5 个：`ClientRenderHelper`（+27，源选择器 + 手臂）、`ModelObject`、`_AttachmentModelRender`、`HandRender`、`TextRender`。

## 开光影仍然坏：我查到哪、卡在哪

我怀疑过"Iris 自己的 framebuffer 没有 stencil 附件"，**反编译后这个假设被否掉了**：

```java
// IrisRenderingPipeline
RenderTarget.getDepthTexture().getFormat()
  → GlConst.toGlInternalId(...) → DepthBufferFormat.fromGlEnumOrDefault(...)
  → new RenderTargets(w, h, depthTexture, ver, depthFormat, ...)
```

Iris 的深度格式是**从主 render target 抄的**，而 `RenderTargets` 里还会 `isCombinedStencil()` 分支。你们已经通过 `ConfigureMainRenderTargetEvent.enableStencil()` 让主 target 用了 combined depth-stencil，所以 Iris 的目标**是带 stencil 的**——这条路是通的。

Iris 确实拦截了 pass 链路：`MixinGlCommandEncoder`（`changeFramebuffer` / `tempFBO` / 对 `GlRenderPass` 的 `iris$bypassSetup`、`iris$setupState` / `ignore`、`ignore2`）与 `MixinRenderPass`（加 `iris$setCustomPass`）。**但 `iris$bypassSetup`/`ignore` 到底跳过了哪些 pass 状态，我没追完**——这是目前唯一的嫌疑点，我没法只靠字节码定论。

所以这里我**没有结论，只有嫌疑**。要定下来得打探针，区分两种情况：

|探针结果|含义|
|---|---|
|开光影时 `RenderSystem.STENCIL_TEST == null`|NeoForge 的模板状态根本没设上（CGC 的 enable 路径在光影下走了别的分支）|
|`STENCIL_TEST != null` 但仍恒真|状态有，是 pass/framebuffer 层把它丢了 → 落在 Iris 的拦截上|

探针就是临时在 `renderModelPart` 的绘制点打一行：`GL.stencilEnabled`、`RenderSystem.STENCIL_TEST != null`、`bufferSource.getClass().getSimpleName()`、`bakedRenderType.pipeline()` 的目标与其 depth format，**开/不开光影各跑一次**。要我写这个探针吗？写完你把两次 log 给我，我直接定位。

## 顺带报一下同属"窗口内但仍走共享源"的剩余项

`MuzzleFlashRender`、`ShellRender`、`BeamRender`、`AttachmentRender` 我**没动**——枪口火焰在管口、抛壳在侧下方、激光在开模板前就画了，屏幕位置上都不落在目镜圆里，改了不改看不出区别，而不改至少不会引入新风险。要是你想让"窗口内一律即时"这条规则彻底一致，也是一行一个的量级，说一声我推平。

未 commit、未 push。

# 

```
装iris+sodium关光影下已经不再在ocular里渲染手了，开光影仍然是准心始终可见且倍镜ocular黑色
不装iris和sodium时正常
```

# 

No-pack case confirmed fixed. Let me chase the pack-on mechanism — Iris may be overriding the pass's depth texture.

The logic already proves the test is always-pass under a pack. Let me find MC's precondition for `enableStencilTest` to actually take effect.

`GlCommandEncoder` applies the stored stencil test at draw time (line 969+) — and Iris intercepts exactly that class. Let me read the bypass condition.

Iris's `iris$bypassSetup` returns `true` (cancelling MC's state setup) when the pass has a shader-pack `CustomPass`. Let me confirm the stencil application is inside that bypassed method.

Root cause nailed. One last check that `trySetup` is the only place the stencil is applied.

Let me check which Iris passes actually get a `CustomPass` (the bypass trigger).

Let me stop the static analysis here — two of my pack-on hypotheses are now **disproved**, and the rest needs in-game data. Setting up a probe.

Adding a temporary, clearly-marked probe to `renderModelPart`.

探针已加好并编译通过。先报两个**被证伪**的假设——这轮的价值主要在这里。

## 开光影那条：排除了两个嫌疑点

**假设一（我上轮提的）："Iris 自己的 framebuffer 没有 stencil 附件" —— 证伪。**

```java
// IrisRenderingPipeline：深度格式是从主 target 抄的
RenderTarget.getDepthTexture().getFormat()
  → GlConst.toGlInternalId(...) → DepthBufferFormat.fromGlEnumOrDefault(...)
  → new RenderTargets(w, h, depthTexture, ver, depthFormat, ...)
```

而 `RenderTargets` 里对 `isCombinedStencil()` 有分支。你们已经用 `ConfigureMainRenderTargetEvent.enableStencil()` 让主 target 用上 combined depth-stencil，所以 Iris 的目标**继承了 stencil 附件**。

**假设二（我的首选怀疑）："Iris 的 `iris$bypassSetup` 跳过了模板应用" —— 也证伪。**

先确认了模板应用的**唯一**落点：

```
GlCommandEncoder.java:969-983   // trySetup(GlRenderPass, Collection<String>)
    var stencilTest = renderPass.stencilTest;
    if (stencilTest != null) { _enableStencilTest(); _stencilFunc(...); _stencilOp(...); _stencilMask(...); }
```

全仓库 `renderPass.stencilTest` 只有这一处读（其余都是 `GlRenderPass` 里存取字段、`RenderSystem.STENCIL_TEST` 的赋值）。而 Iris 的 `iris$bypassSetup` 正是在 `trySetup` 的 HEAD 上，条件满足时 `cir.setReturnValue(true)` 直接短路——**看起来完全吻合**。

但它要求 `pass.iris$getCustomPass() != null`，而 `setCustomPass` 的调用者只有**合成/后处理 pass**：

```
CenterDepthSampler / ColorSpaceFragmentConverter / CompositeRenderer / FinalPassRenderer / ShadowCompositeRenderer
```

`HandRenderer`（开光影时承载 CGC 枪械渲染的那个 pass）引用 `CustomPass` 的次数是 **0**。所以枪械那条 pass 不会被绕过，模板**应该**被应用。

⇒ 丢失点在我还没读到的地方，最可疑的是 Iris 在 `trySetup` **之后**插的 `iris$setupState`（对同一个 `trySetup` 的 `@At("RETURN")`，调 `IrisProgram.iris$setupState()`）——MC 刚设好的模板状态可能被它之后的程序/FBO 切换冲掉。**这只是嫌疑，我没定论。**

## 探针（已加，临时）

`_AttachmentModelRender.renderModelPart` 的绘制点加了一行去重日志，`TODO(临时探针 #60)` 标注，定位后我删掉：

```
[cgc-probe] STENCIL_TEST=... src=... rt=... mainUseStencil=... mainDepthFmt=...
```

麻烦你开一次光影跑一下，把 `[cgc-probe]` 那几行发我。它直接二选一：

|结果|结论|下一步|
|---|---|---|
|`STENCIL_TEST=null`|NeoForge 的模板状态在光影下根本没设上（CGC 的 enable 路径分叉了）|**我们能修**，改 CGC|
|`STENCIL_TEST=StencilTest[...]` 且 `src=BufferSource`|CGC 侧完全正确，是 MC/Iris 的 pass 管线把状态丢了|探针搬进兼容仓库，插到 `GlCommandEncoder.trySetup` 与 Iris 的 `iris$setupState` 上定位|

顺带请一起确认两件还没反馈的：**开镜时"镜内不渲染枪体"生效了吗**（目镜里应该没有枪身），以及**卸下瞄具后枪体没有被残留圆形裁掉**。这两条走的正是我改的另外两个模板点，是它们在 vanilla 与光影下都正确的证据。

## 当前状态

`1.21.6neoforge` 工作区 7 个文件（`IrisCompat`/`ClientRenderRegistry` 是你原有的），我实际动 5 个 + 探针。兼容仓库不动，也没有 jar 要换。这次编译用的是绝对路径 `"$R/gradlew" -p "$R"`，不会再踩上次那个工作目录漂移的坑。

未 commit、未 push。

# 

```
现在iris+sodium开光影仍然准心始终渲染，关光影正常
开镜时不渲染枪体已生效，日志在D:\Temp-File\debug.log
```

# 

The probe fired but my dedup key was too coarse — it collapsed all the distinct stencil funcs into one line, and it can't distinguish "my source" from the shared one. Key facts it _did_ give: the stencil **write** (`GREATER/REPLACE ref=1`) is being set, the target is `main_target` with `DEPTH32_STENCIL8`, and Iris's buffered source is in play. Let me tighten the probe.

Let me confirm from that same log whether a shader pack was actually active.

The pack **was** on (`ComplementaryReimagined`). Now the decisive extraction from the existing log — which stencil funcs were ever set.

No `EQUAL` func anywhere in 815 non-null entries — only `ALWAYS/ref=0` and `GREATER/REPLACE/ref=1`. Let me correlate the fields properly.

Only two stencil states exist in the whole pack-on run: the write (`GREATER/REPLACE/ref=1`) and `ALWAYS/ref=0`. **`EQUAL` never appears** — so the mask/reticle read passes never reached a draw. Let me rule out a mapping bug in CGC's own enum first.

CGC's enum mapping is correct (`EQUAL` exists, converts 1:1). So `EQUAL` is genuinely never set under a pack. Let me instrument the request site too, so one round of runs answers everything.

编译通过，两个探针都加好了。**但我要先纠正我自己的一个结论** —— 这份日志把上一条推理推翻了。

## 日志证明了什么

先确认环境：光影**确实开着**（`Using shaderpack: ComplementaryReimagined_r5.8.1.zip`，Iris `1.9.6+mc1.21.8`）。

把 815 条非 null 探针行按字段关联后，**整场只出现过两种模板状态**：

```
498  front pass=REPLACE compare=GREATER | readMask=255 writeMask=255 ref=1
317  front pass=KEEP    compare=ALWAYS  | readMask=255 writeMask=255 ref=0
```

- `REPLACE/GREATER/ref=1` = `renderOcularStencil` 的**写入**（`_stencilOp(KEEP,KEEP,REPLACE)` + `_stencilFunc(GL_GREATER, i+1)`，i=0）
- `ALWAYS/ref=0` = 各处 `_stencilFunc(GL_ALWAYS, 0, 0xFF)`

**`EQUAL` 一次都没有出现。** 我核过 CGC 自己的映射没有 bug：`StencilFunction` 有 `EQUAL`，`of(GL_EQUAL) → EQUAL`，NeoForge 侧 `convert` 也是 1:1。所以只要 `_stencilFunc(GL_EQUAL, …)` 在 `stencilEnabled == true` 时被执行到，日志里必然出现 `compare=EQUAL`。

顺带也确认了两件好事：目标确实是 `main_target` 且深度格式是 `DEPTH32_STENCIL8`（有 stencil 附件），写入那次用的是我的私有 `BufferSource`（说明 `stencilEnabled` 当时为 true）。

⇒ **开光影这个症状不是"模板测试被丢掉"，而是那批 `EQUAL` 遮罩绘制（目镜遮罩 `EQUAL i+1`、准心 `EQUAL ~(i+1)`、镜身 `EQUAL 0`）根本没走到绘制。** 这跟我上一条"MC 应用了、Iris 把它丢了"的结论是两回事——我上一条的推理（含"Iris 的 flush 没有 `enableStencilTest`"那段）本身没错，也解释了关光影那条为什么被我修好，**但它解释不了开光影这条**。我不该把两条并成一条。

## 缺的对照与新增的探针

现在的日志只有开光影一侧。要分清是"请求了但没落下去"还是"压根没请求"，我把探针加到了请求点：

```
[cgc-probe-func] func=EQUAL ref=1 readMask=255 stencilEnabled=true     ← 新增
[cgc-probe] stencilEnabled=true src=BufferSource STENCIL_TEST=…        ← 绘制点，已收紧
```

两个都做了去重（各上限 64 行），不会再像上份日志那样刷 2233 行。旧的绘制点探针有个缺陷也修了：之前用类名区分来源，而共享源在"手部 pass"下也是 `BufferSource`，分不出来；现在直接按 `getModelBufferSource()` 的身份判断 `stencilEnabled`。

**麻烦跑两次**（这份 build 已经编译好了，IDE 直接起），开光影 / 关光影各一次，把两次的 `[cgc-probe]` 和 `[cgc-probe-func]` 行都发我。判读规则：

|开光影时的结果|结论|
|---|---|
|`[cgc-probe-func]` 里有 `func=EQUAL` 但绘制点没有 `compare=EQUAL`|状态设了又被冲掉 → 回到 pass/管线层（我上一条的嫌疑范围）|
|`[cgc-probe-func]` 里**也没有** `EQUAL`|读阶段根本没执行 → 是 CGC 的流程问题，重点查 `divisionOcularEntries` / `getDivisionNodePath()` / 早退分支|

关光影那次作为对照：如果那边有 `EQUAL`、开光影没有，差异就被夹到最小了。

## 已经确定的（不受这次纠正影响）

- **关光影：修好了**，你实测准心回到目镜内、手也不再出现在目镜里。
- **开镜不渲染枪体：生效**。
- 两次实测都证实 `getModelBufferSource()` 这条切换本身是对的。

探针是临时的、带 `TODO(临时探针 #60)`，定位完我整体删掉。未 commit、未 push。

# 

```
开光影时问题还在，不开光影没问题
日志在D:\Temp-File\debug.log
```

# 

This run changes everything — the probe now shows **all five** stencil states. Let me extract them cleanly.

Now it's unambiguous: **all five stencil states are requested _and_ reach draws on the non-buffered source.** Let me sample the actual GL state right after the draw to find where it's lost.

探针换掉了，编译通过。**先纠正我上一条的结论——那是我自己的探针缺陷造成的误读。**

## 上一条错了

上份日志"`EQUAL` 一次都没出现"是**我探针的去重键太粗**导致的假象：旧键只有 `(非空?|来源类名|RenderType|…)`，不含模板函数值；而同一 RenderType 下 `renderOcularStencil`（写入）永远排在读阶段之前，所以每个 RenderType 只记下了**第一个**状态，后面的 `EQUAL` 全被压掉了。我当时在消息里点出了这个缺陷，**却还是拿它下了结论**——这是我的错。

## 这份日志（收紧后的探针）证明：开光影时 CGC 侧完全正确

五个状态**全部被请求**，且**全部真的走到了绘制**，来源都是我的非缓冲源、`stencilEnabled=true`、`STENCIL_TEST` 已填好：

```
[cgc-probe-func] func=ALWAYS  ref=0     ← _stencilFunc(GL_ALWAYS, 0)
[cgc-probe-func] func=GREATER ref=1     ← renderOcularStencil 写入 (KEEP,KEEP,REPLACE)
[cgc-probe-func] func=EQUAL   ref=0     ← renderBoth/renderScope 镜身
[cgc-probe-func] func=EQUAL   ref=1     ← renderOcularAndDivision 目镜遮罩 (i+1)
[cgc-probe-func] func=EQUAL   ref=254   ← 准心 (~(i+1) & 0xFF)
```

对应的 `[cgc-probe]` 绘制点也是这五个，一一对上，`src=BufferSource`、`stencilEnabled=true`。**连 `renderOcularAndDivision` 的两个循环和 `renderDivisionOnly` 都确实执行了**——这正是我上一条说"没执行"的地方。

⇒ **模板状态被正确设置、并正确抵达绘制，但不起作用。** 丢失点在 MC/Iris 的 pass 应用层，**在 CGC 之下**，不是我们这边能靠换缓冲源解决的。

## 新探针：直接量绘制后的真实 GL 状态

在每次遮罩绘制 flush 之后采样两个量（去重、上限 16 行）：

```
[cgc-probe-gl] afterDraw GL_STENCIL_TEST=<bool> stencilAttachmentType=<int>
```

`glIsEnabled(GL_STENCIL_TEST)` 是真实 GL 查询，反映我们这条 pass 实际留下的状态；`glGetFramebufferAttachmentParameteri(..., GL_STENCIL_ATTACHMENT, ...)` 查当前绑定的 FBO 有没有 stencil 附件。三选一：

|结果|结论|归属|
|---|---|---|
|`GL_STENCIL_TEST=false`|pass 从未启用模板测试（顺序/被绕过）|Iris 侧|
|`true` + `stencilAttachmentType=0`|绑的 FBO 没有 stencil 附件（被换成 Iris 自己的 FBO）|Iris 侧|
|`true` + `stencilAttachmentType=5890`|测试确实开在带 stencil 的缓冲上 → 问题是**缓冲内容**（写入/读取落在不同 target，或清空清错了 buffer）|另一条线，另查|

**麻烦再跑两次**（build 已编译好）：开光影 / 关光影，把 `[cgc-probe-gl]` 行发我（另外两族探针留着没删，一起发也行）。关光影那次是必要的对照——预期它应该是 `true` + `5890`；一旦开光影这行不同，差异就被夹到具体那一个量上了。

## 状态

`1.21.6neoforge` 工作区：你的 2 个文件 + 我改的 5 个 + 三族临时探针（都带 `TODO(临时探针 #60)`，定位完整体删除）。兼容仓库不动。日志也从 1.7MB 降到 578KB——有界去重生效了。未 commit、未 push。

# 

```
以防你不能从一个日志里区分状态，我拆成两个日志放在D:\Temp-File\下
关光影没问题，开光影问题还在
```

# 

Two clean logs. Extracting the GL-state probe from both.

**This is the decisive differential:**

|log|GL_STENCIL_TEST|stencil attachment|
|---|---|---|
|pack **off** (works)|true|**5890 = TEXTURE**|
|pack **on** (broken)|true|**0 = NONE**|

Under a pack the test is enabled but the bound framebuffer has **no stencil buffer**. Let me remove the last ambiguity (whether my post-draw sample reads the pass's own FBO) with an in-pass probe.

Adding an in-pass probe (mixin on `GlCommandEncoder.executeDraw`) so the sample is taken while the pass's own FBO is bound.

两个日志的差值是干净的，而且**只剩一个歧义**。先看结果：

|日志|`GL_STENCIL_TEST`|stencil 附件|
|---|---|---|
|关光影（正常）|`true`|**`5890` = `GL_TEXTURE`**|
|开光影（坏）|`true`|**`0` = `GL_NONE`**|

## 这是什么意思

开光影时**模板测试确实被启用了**，但绘制目标的 framebuffer **没有 stencil 附件** → 测试无从比较 → 退化成恒真 → 目镜遮罩铺满（黑）、准心满屏。这和后一份日志里"五个模板状态全部请求、全部抵达绘制、来源正确"完全咬合：**CGC 侧全对，是目的地没有地方存 stencil。**

也解释了为什么换缓冲源救不了这条：状态是对的，问题在收件方。

## 还剩的歧义 + 新探针

我那次采样是在 flush **之后**取的。如果 `renderpass.close()` 会把外层的 FBO 还回来，那读到的是外层 FBO 而不是这条 pass 自己的。关光影那次同一时刻读到 `5890`，说明外层 FBO 也带 stencil——所以歧义不大；但我上次刚因为"明知探针不精确还下结论"翻过车，这次不重复。

所以加了**pass 内采样**：`neoforge-compat` 临时 mixin `pipeline.GlCommandEncoderProbeMixin`，`@Inject(HEAD)` 打在 `GlCommandEncoder.executeDraw(...)`，并用 `RenderSystem.STENCIL_TEST != null` 门控（只在瞄具窗口内采样，不做全帧同步查询）：

```
[cgc-probe-inpass] GL_STENCIL_TEST=… stencilAttachmentType=… (0=NONE, 5890=TEXTURE) fbo=…
```

去重、上限 16 行，已注册进 `customgunneoforgeclient.mixins.json`，编译通过。它在 pass 自己的 FBO 仍绑定时、绘制前一刻采样，顺手把 `fbo` 也带出来——如果开光影时 fbo 号与关光影不同，就坐实了"换了个 framebuffer"。

## 这次结果决定分岔

|pass 内结果|结论|出路|
|---|---|---|
|`attachmentType=0`|坐实：光影下瞄具遮罩**按现设计无法工作**，Iris 往没有 stencil 的 framebuffer 里画|(a) 上游找 Iris（它本来就按主 target 推导深度格式，保留 stencil 方面应该是小改动）；(b) CGC 侧改成不依赖 stencil 的遮罩实现；(c) 记为光影包限制|
|`attachmentType=5890` 但症状仍在|pass 有 stencil，问题在**缓冲内容**（写入/读取落在不同 buffer，或清空清错了目标）|另一条线，下一步探 stencil 缓冲内容|

顺带一个待查线索：Iris 的 `RenderTargets` 里除主深度外还有 `noHand` / `noTranslucents` 两张独立深度纹理，手部通道可能用的是它们——如果 pass 内确认没有 stencil，这两张的创建格式是第一个该看的地方。

**再跑两次**（build 已编译好），把 `[cgc-probe-inpass]` 发我就行（其余两族探针还在，一起发也行）。

关光影那条修复不受影响，仍然成立。未 commit、未 push。

# 

```
日志已放 D:\Temp-File\ 下，两个都跑好了
```

# 

In-pass samples are much more informative. Let me check one thing before reporting — how MC uses `inRenderPass` (Iris modifies reads of it).

## 先撤回上一条结论

pass 内采样直接否掉了它：两份日志在**启用了模板测试**的采样上完全一致——

```
关光影:  GL_STENCIL_TEST=true  stencilAttachmentType=5890  fbo=4
开光影:  GL_STENCIL_TEST=true  stencilAttachmentType=5890  fbo=4
```

同一套 `fbo=4` + stencil 纹理附件。之前那个 `0=NONE` 是**绘制后**采样，`renderpass.close()` 之后 FBO/状态已经走掉了；我当时就标注了这个风险，结果它确实成立。另外顺带查清：`trySetup` 是在 `executeDraw` **内部**（`GlCommandEncoder:753`）调的，所以我打在 HEAD 的采样反映的是**前一次绘制**留下的状态——能用，但不能当成"我们那次"。

## 现在的事实（开光影，坏的那侧）

|环节|状态|
|---|---|
|五个模板状态|全部请求、全部抵达绘制 ✓|
|缓冲源|非缓冲源，`stencilEnabled=true` ✓|
|pass 内|`fbo=4` + stencil 纹理附件 + 测试已启用 ✓|
|绘制目标|`RenderType` 的 CompositeState 是 `main_target`——**和关光影同一个目标** ✓|
|`_clearStencilBuffer`|清的也是 main target 的 depth-stencil ✓ 一致|

⇒ **目的地、状态、来源三项都和能工作的那一侧一样。** 剩下能解释的只有**缓冲内容**或**时序**：光影下瞄具这段和 Iris 自己对 main target 深度/模板的使用（它有 `noHand`、`noTranslucents` 两张独立深度纹理）在同帧内交错，比如清空/覆盖发生在写入与读取之间。

我已经连着两次从有缺陷的探针里下结论了，所以这次先不再猜——接下来这一发探针要能直接看到**时序**。

Starting with cleanup of the two probes whose questions are answered, then adding the ordering probe. Let me read the current state of the scope methods first.

Now the five ordering call sites.

探针换成了时序版，编译通过。同时**删掉了前两个已答完问题的探针**——尤其那个 pass 内 mixin，它每次 draw 都做同步 GL 查询，本身就可能干扰我这次要量的时序，留着是污染源。

## 新探针：`[cgc-probe-order]`

在瞄具模板路径的 7 个点各打一行，`path#N` 按 **`clear`** 递增 —— 所以每个 `N` 就是一次完整的瞄具路径实例：

```
[cgc-probe-order] path#12 clear            ← _clearStencilBuffer
[cgc-probe-order] path#12 write            ← renderOcularStencil 入口
[cgc-probe-order] path#12 invert           ← 圆形模板孔 (GL_INVERT)
[cgc-probe-order] path#12 read-mask        ← 目镜黑遮罩 (EQUAL i+1)
[cgc-probe-order] path#12 read-ret         ← 准心 (EQUAL ~(i+1))
[cgc-probe-order] path#12 read-div / read-divonly   ← 另两条读分支
```

上限 400 行，纯 Java 日志、不碰 GL，不会扰动被观察对象。

要看的就三件事：

1. **一帧里有几个 `path#N`** —— 光影下瞄具路径是不是被跑了两遍（比如实体阶段一遍 + Iris 手部 pass 一遍）。按日志时间戳看：60fps 下相邻 `#N` 若落在同一个 ~16ms 窗内，就是同帧多次。
2. **`clear` 有没有插进 `write` 和 `read-*` 中间** —— 如果有，就是"写入被清掉、读的时候缓冲已经是 0"，正好解释`EQUAL 1`/`EQUAL 254` 全部落空、而 `EQUAL 0`（枪体）反而正常。
3. **顺序有没有被打乱** —— 设计顺序是 `clear → write → invert → read-*`；顺带能看到 `read-mask` 与 `read-ret` 是否成对紧邻。

**再跑两次**（build 已编译好），把 `cgc-probe-order` 那些行发我就行，大概这样取：

```bash
grep "cgc-probe-order" /d/Temp-File/debug.log | head -60
```

如果一帧一个 path，60 行足够看好几帧的时序；我会先看有没有帧内重复和 clear 插队。未 commit、未 push。

# 

```
日志好了
我测试是先开光影，测4倍镜开镜关镜，切到红点开镜关镜
然后关光影，红点开镜关镜，切到4倍镜开镜关镜
之前的日志也是两个倍镜之间切，顺序不定，避免日志不能区分是红点还是倍镜
```

# 

Both hit the 400-line cap. Let me analyze the sequence shape per path in each log.

## 结果：这条也被否掉了

两个日志的**控制流形状完全一样**：

```
开光影:  78 × clear → write → read-divonly
         33 × clear → write → invert → read-mask → read-ret
关光影:  80 × clear → write → invert → read-mask → read-ret
```

顺序正确、**没有任何 `clear` 插进 `write` 与 `read-*` 之间**、没有重复的路径实例。两份日志的差异只是 `read-divonly`（sight 通道）与 `read-mask/read-ret`（scope 通道）的占比不同——那是你在两轮里先测哪把镜的区别，不是光影。

⇒ **CGC 的 Java 层流程在两种情况下是同一套。** 这是我第三次推翻自己的假设，而且三次都是同一类错误（拿探针的输出越过了它能支撑的范围）。我不再继续这种猜法了。

## 顺手发现两个日志本身的问题

1. **探针上限 400 行在 ~1.7–3.4 秒内就打满了**，所以这两份日志只覆盖了你测试最开头的一小段，后面切镜的部分没记进来。下次要探就得改成"每个状态变化只记前 N 次"或按帧汇总，而不是单纯限总行数。
2. 两份日志**都有** `Using shaderpack: ComplementaryReimagined_r5.8.1.zip`，只有你的文件名和你的观察能区分哪次是关光影——Iris 在 UI 里关光影看起来不打"Disabled"之类的日志。以后靠日志自证得换个标记（比如让我们自己的探针打印 `IrisApi.isShaderPackInUse()`）。

## 现在真正剩下的未知

CGC 能看见的面已经查完了：状态设了、来源对了、顺序对了、（至少部分窗口内的 draw）目的地是 `FBO 4` + stencil 纹理。剩下只有两种可能，都**在 MC/Iris 的绘制内部**：

- 那 5 次遮罩绘制当时究竟有没有被应用上模板测试（要逐 draw、不去重、并读 `GlRenderPass.stencilTest` 字段，需要给那个类加临时 accessor）；
- 或者写入与读取看到的**缓冲内容**不同（Iris 有自己的 `noHand`/`noTranslucents` 深度纹理与 `DepthColorStorage`，可能在写入与读取之间把深度/模板拷回去了）。

两者都要继续往 MC/Iris 内部插探针，每轮换你一次实测，而我已经连续两轮拿到过误导性的数据。

Re-adding the in-pass probe, this time with a tag so I can identify each of CGC's five masked draws specifically.

探针装好了，编译通过。这次按你要的做成**定向**的。

## 这次探到了哪一层

`GlCommandEncoder.drawFromBuffers` —— 它是 `executeDraw` 内部**在 `trySetup` 之后**、真正发 GL 绘制调用前的那一层。所以在它 HEAD 采到的 `glIsEnabled(GL_STENCIL_TEST)` 就是**这次绘制实际用的状态**，不再是"前一次留下的"。

同时给 CGC 那几笔遮罩绘制打了标签（临时 `ClientRenderHelper.PROBE_DRAW_TAG`），mixin 只记录带标签的绘制，**每个标签上限 8 条**（不是全局上限），所以这次不会像上次那样被 400 行截断在开头。

```
[cgc-probe-draw] tag=… glStencilTest=… passStencilTest=… fbo=…
```

`tag` 就是那五笔里的哪一笔（`write` / `read-mask` / `read-ret` / `read-div` / `read-divonly`），`passStencilTest` 是直接从 `GlRenderPass` 反射读出来的字段值（反射拿到的是这次绘制所属 pass 自己的意图）。

## 四种读法，每种都直接指向出路

|`passStencilTest`|`glStencilTest`|结论|
|---|---|---|
|`null`|任意|CGC 设的状态**根本没进到 pass**——问题在 `RenderType#draw` 的 `enableStencilTest` 那一步被换了 pass|
|非 null|`false`|状态进了 pass 但**没被应用到 GL** → 就是 `trySetup` 被绕过/顺序问题，Iris 侧|
|非 null|`true`，且 `fbo` 与关光影一致|测试**确实活着**、目标也对 → 剩下的只能是**缓冲内容**（写入没落进去或被 Iris 的深度拷贝覆盖），下一步探写入|
|`<NoSuchFieldException>`|—|环境里字段名不同，我改用 accessor mixin 重来|

尤其看 **`write` 与 `read-mask` 两行是否不同** —— 如果写入那笔就没应用上、而读取那笔应用上了（或反之），那 stencil 缓冲自然是 0，`EQUAL 1`/`EQUAL 254` 全落空。

**再跑两次**（build 已编译好），`cgc-probe-draw` 就几十行：

```bash
grep "cgc-probe-draw" /d/Temp-File/debug.log
```

原来的 `cgc-probe`（绘制点状态）和 `cgc-probe-func`（请求的状态）也还在，作为交叉核对，一起发我也行。未 commit、未 push。

# 

```
日志好了，再看下
```

# 

**Found it — the `fbo` column is the answer:**

|tag|pack OFF|pack ON|
|---|---|---|
|`write`|fbo=4|**fbo=77**|
|`read-mask`|fbo=4|**fbo=77**|
|`read-ret`|fbo=4|**fbo=77**|
|`read-divonly`|fbo=4|**fbo=77**|
|`invert` (圆形模板孔)|fbo=4|**fbo=4**|

Let me add the one missing measurement to make it airtight.

## 定位到了

`fbo` 那一列就是答案：**开光影时，被遮罩的这几笔绘制跑到 Iris 自己的 framebuffer（`fbo=77`）去了，而圆形模板孔那笔还留在 `fbo=4`**（vanilla `debugTriangleFan` 这个 RenderType Iris 不认识，不重定向）。

其余全部正常：`passStencilTest` 与 CGC 请求的值**逐字对上**（`GREATER/REPLACE/ref=1`、`EQUAL/ref=1`、`EQUAL/ref=254`、`INVERT`），`glStencilTest=true`。所以状态、顺序、开关都没问题——**是收件方被换掉了。**

### 这解释了整条链

- **关光影**：写入、遮罩、准心、模板孔全在 `fbo=4`（主 target，`DEPTH32_STENCIL8`）→ 互相看得见 → 正常。
- **开光影**：写入/遮罩/准心在 `fbo=77`，模板孔在 `fbo=4` → 两者**不在同一个缓冲**里；而且上一轮那个"绘制后采样"读到的 `glStencilTest=true` + `stencilAttachment=NONE` 正是 `fbo=77` —— 即**Iris 的那个 framebuffer 没有 stencil 附件**，测试开着也无从比较 → 恒真 → 遮罩铺满、准心满屏。

### 我要为上一条撤回道歉

**我上一条撤回"没有 stencil 附件"这个假设，本身是错的。** 那次我打在 `executeDraw` 的 HEAD，而 `trySetup` 是在它**内部**调的——所以读到的是**前一次绘制**的状态；恰好前一次是落在 `fbo=4` 上的别的几何体，才给出了 `5890`。加了 tag、打到 `drawFromBuffers`（`trySetup` 之后）就立刻现原形了。结论是：**这个假设一直是对的，是我用错了采样点。**

### 差最后一步

现在这版探针已经补上了对**当前绑定 FBO** 的 stencil 附件查询。再跑一次就能把"`fbo=77` 没有 stencil 附件"这句从"强推断"变成"实测"：

```
[cgc-probe-draw] tag=write glStencilTest=true passStencilTest=… fbo=77 stencilAttachment=0 (0=NONE, 5890=TEXTURE)
```

预期：开光影时 `fbo=77 stencilAttachment=0`，关光影时 `fbo=4 stencilAttachment=5890`。若真是这样，这条就完全定案了。

```bash
grep "cgc-probe-draw" /d/Temp-File/debug.log
```

### 如果定案，出路的性质就变了

这**不是** CGC 能靠换缓冲源/改状态解决的问题——是 Iris 把命中 shader-pack 材质的 RenderType 重定向到自己的 gbuffer framebuffer，而那个 framebuffer 不挂 stencil。而且是**两层**问题：

1. 遮罩几何去的 buffer 没有 stencil 附件；
2. 就算有，模板孔画在 `fbo=4`，和几何也不在同一个 buffer —— 孔和遮罩仍然对不上。

所以正路只有两条：**上游找 Iris**（它的 gbuffer 深度格式本来就继承自主 target，保留 stencil 方面并非不可能；而且它有 `DepthBufferFormat.DEPTH_STENCIL` 和那个 stencil 集成测试 mixin），或者**CGC 把瞄具遮罩改成不依赖 stencil**（alpha/几何遮罩）——后者是设计级改动，且会动到模板那套代码。

先把这一发跑完，`stencilAttachment` 一出来我就把定案结论和可投稿的复现材料一起整理给你。未 commit、未 push。

# 

```
日志好了，继续
```

# 

**定案了。** 直接实测的结果：

||`fbo`|`stencilAttachment`|
|---|---|---|
|**关光影** write / mask / ret / invert / clear|`4`|**5890（有 stencil）**|
|**开光影** write / mask / ret / read-divonly / clear|**`77`**|**`0`（无 stencil）**|
|**开光影** invert（圆形模板孔）|`4`|`5890`|

现在清理探针，把工作区还原成只剩真正的修复。先存一份补丁做保险。

Reverted cleanly. Now re-applying just the four real fixes.

探针已全部清理，工作区只剩真正的修复，编译通过，`临时探针` 标记 0 处，无遗留未跟踪文件。

## 定案（实测，非推断）

|环境|被遮罩的绘制|`GL_STENCIL_TEST`|`passStencilTest`|`fbo`|stencil 附件|
|---|---|---|---|---|---|
|**关光影**|write / mask / ret / invert / clear|`true`|与 CGC 请求**逐字一致**|`4`|**TEXTURE**|
|**开光影**|write / mask / ret / read-divonly / clear|`true`|同上，逐字一致|**`77`**|**NONE**|
|**开光影**|invert（圆形模板孔）|`true`|`INVERT`|`4`|TEXTURE|

**开光影时 Iris 把命中材质映射的那些几何体重定向到它自己的 framebuffer（`fbo=77`），那个 framebuffer 不挂 stencil 附件。** 状态传到了 pass、`GL_STENCIL_TEST` 也开着，但没有任何东西可比 → 测试恒真 → 遮罩铺满、准心满屏。

而且是**两层**问题：圆形模板孔走 `debugTriangleFan`（Iris 不认识、不重定向），留在 `fbo=4`——就算 `77` 有 stencil，孔和几何也不在同一个缓冲里，照样对不上。

顺带说明：我中途撤回"没有 stencil 附件"那个假设是错的，根因是采样点问题（打在 `executeDraw` HEAD，而 `trySetup` 在其内部，读到的是前一筆）。这一轮改用 tag + `drawFromBuffers`（`trySetup` 之后）才拿到真值。

## 落地了什么

关闭光影的那条链**修好了并已实测**（准心回到目镜内、手不再出现在目镜里、镜内不渲染枪体）。改动 5 个文件、净 +36 行：

- `ClientRenderHelper.GL.getModelBufferSource()`：`stencilEnabled` 期间用自有非缓冲源，否则保持共享源
- 取源点：`ModelObject.render`（枪体）、`_AttachmentModelRender.renderModelPart`、`renderFirstPersonArm`、`HandRender`、`TextRender`

`getModelBufferSource()` 的 javadoc 里我把光影那条限制也写进去了——避免以后有人以为这个切换能覆盖光影情况而白查一遍。

**未覆盖**：开光影。这一条的结论是"Iris 的重定向目标没有 stencil 附件"，CGC 侧换源/改状态都无效。

## 可投稿给 Iris 的材料（要点）

> **标题**：NeoForge 1.21.6 的 stencil API 在启用光影包时失效（Iris 把几何体重定向到无 stencil 附件的 framebuffer）
> 
> **环境**：MC 1.21.6 / NeoForge 21.6.20-beta / Iris 1.9.6 / Sodium 0.7.2 / ComplementaryReimagined r5.8.1
> 
> **用法**：`RenderSystem.enableStencil(StencilTest)` → `RenderType#draw` → `RenderPass#enableStencilTest`，用 `GL_EQUAL` / `GL_INVERT` 做瞄具遮罩
> 
> **实测**（探针打在 `GlCommandEncoder.drawFromBuffers` HEAD，逐笔带标签）：
> 
> - 关光影：全部遮罩绘制 → `fbo=4`，附件 = TEXTURE
> - 开光影：同样几笔 → `fbo=77`，**附件 = NONE**；而 `debugTriangleFan`（未被材质映射，不重定向）→ `fbo=4`，附件 = TEXTURE
> 
> **诉求**：让重定向后的绘制所用 framebuffer 的深度纹理带上 stencil 方面（Iris 本就从主 target 推导 `DepthBufferFormat`，且有 `DepthBufferFormat.DEPTH_STENCIL` / `isCombinedStencil()` 分支）；否则请明确 stencil API 在光影下不支持。
> 
> **我没查清的**：`fbo=77` 具体用的是 Iris 哪张深度纹理（`RenderTargets` 里除主深度外还有 `noHand` / `noTranslucents`），这条你们一眼能看出来。

## 两件事等你定

1. **上游**：上面的材料可直接投 Iris；也可以先不急。
2. **要不要改实现**：把瞄具遮罩改成不依赖 stencil（alpha/几何遮罩）——那是设计级改动，且会替换掉模板那套代码的一部分，所以我没动。要动的话我倾向先出个方案再落。

安全工作区：改动前我把带探针的完整版本存成了 `/tmp/cgc-1216-with-probes.patch`（300 行），要回看探针代码可以从那儿取。

未 commit、未 push。

# 
[fork1](#fork1)

# 

```
就目前git暂存区的改动，虽然1.21.6才加，但是放在1.20.1-1.21.4是否行为跟之前保持一致？
```

# 

Good question — let me answer it from the actual branches rather than from memory.

`stencilEnabled` exists and is toggled identically on all three older branches — so the gate would work there. Let me check whether the buffer-source API even exists on them.

查了三个分支的源码，答案是**不一致**，而且分两类。

## 那 5 个文件（我的修复）

||1.20.1|1.21.1 / 1.21.4|
|---|---|---|
|能否编译|**不能**：`MultiBufferSource.immediate` 收的是 `BufferBuilder`，那两个分支里根本没有 `ByteBufferBuilder`（我 grep 过，`core`/`forge-compat`/`neoforge-compat` 全都没出现过这两样）|**能**：`ByteBufferBuilder`、`MultiBufferSource.immediate(ByteBufferBuilder)`、`endBatch → renderType.draw` 与 1.21.6 完全一致|
|行为|—|**会变**|

**行为为什么会变（这是问题核心）**：

1. **1.21.6 之前模板是全局裸 GL 状态。** 看旧分支 `_stencilFunc` 的 `[1.20.1, 1.21.6)` 分支就是 `RenderSystem.stencilFunc(...)`，还有 `RenderSystem.colorMask/depthMask`——**全局状态，任何绘制都吃到，跟走哪个缓冲源无关**。所以那条修复的前提在旧分支上**根本不存在**：它们本来就没这个 bug（与"1.21.1/1.21.4 + iris+sodium 实测正常"一致）。
2. **换了源会拆掉现有的兜底。** 那几笔绘制离开 Iris/Oculus 的批处理后，`IrisCompat.endBatch(bufferSource)` 会变成**返回 false**（传进去的已不是全缓冲源），于是那句"顺手把当时排队的所有几何体一起 flush"不再生效 → **其余部件的提交时机与顺序跟着变**。旧分支恰恰是靠这个兜底在工作。

⇒ 不是"多此一举但无害"，是**会改行为，且没必要**。

`stencilEnabled` 那个门控本身在三个旧分支上都存在、也照样会在同样的窗口里被置真（`enableItemEntityStencilTest` 里那行没有版本门），所以一旦照搬，分支是**真的会走进新分支**，不会因为门控而自动变成 no-op。

## 你那两个文件（`IrisCompat.registerRenderPipelines` + `ClientRenderRegistry` 调用）

|分支|情况|
|---|---|
|1.20.1 / 1.21.1|`ClientRenderRegistry` **完全没有** `onRegisterRenderPipelines` 这个方法|
|1.21.4|**已有同名方法，但签名是 `Consumer<Object>`**，分支里自己注了 `// 1.21.4没有RenderPipeline`|
|1.21.6|`Consumer<RenderPipeline>` + 调 `IrisCompat.registerRenderPipelines(registrar)`|

而且 1.21.4 的 `ClientRenderRegistry` 里 `LASER_BEAM_PIPELINE` 那些都还是老的 `RenderType.create(...)` 写法；1.21.5+ 的 pipeline builder（`RenderPipeline.builder/withVertexShader/withLocation`）在旧分支上不存在（1.20.1/1.21.1 的 `RenderPipeline` 是旧类，`builder` 0 命中）。**接不上。**

## 建议

**保持 1.21.6-only，不要移植。** 想让 core 看起来统一的话，符合本项目既有做法的选择是**放同名空壳 + 版本注释**（1.21.4 的 `onRegisterRenderPipelines(Consumer<Object>)` 就是这个套路的样本），而不是把 1.21.6 的实现搬过去——`getModelBufferSource()` 上我也已经标了 `@ApiStatus.AvailableSince("1.21.6")`，这个意图在注解里是明确的。

需要的话我可以按这个套件出一条"1.21.4 加空壳 stub"的改动，但那属于跨分支动作，按 CLAUDE.md 得你点头我才动别的分支。

# 

```
我目前这个写法在1.20.1装不装oculus都正常，看下是否可行
```

# 

Let me look at what you've written on the 1.20.1 branch.

看了改动，也实测了 `./gradlew compileJava`：**1.20.1 上 BUILD SUCCESSFUL**。结论是**可行，而且这是比我建议的更好的写法**。

## 为什么它是逐字等价，而不是"碰巧能用"

```java
// [1.20.1, 1.21.6)
if (true) return Minecraft.getInstance().renderBuffers().bufferSource(); // 让IDE保留下面的引用关系
```

这正是这个项目既有的版本门写法（同一个文件里 `disableItemEntityStencilTest` 就是这么写的，`_renderHand`、`RenderPipeline` 注释也是这个套路），而且我实测确认 javac 接受这种写法。于是 1.20.1 上：

- `getModelBufferSource()` **恒等于** `mc.renderBuffers().bufferSource()`；
- 5 个取源点（`ModelObject`、`_AttachmentModelRender.renderModelPart`、`renderFirstPersonArm`、`HandRender`、`TextRender`）全部退化成改动前的原句；
- `IrisCompat.endBatch(bufferSource)` 拿到的仍是共享源 → 那句兜底照旧生效。

**所以 1.20.1 的"装不装 oculus 都正常"是结构性保证，不是运气。** 同时我也要修正我上一条的适用范围：我说"换源会改行为、别移植"，那个结论的前提是**照搬 1.21.6 的实现**；你用版本门空实现绕开了它，既拿到 core 统一，又零行为差异。这个做法对。

## 更正我上一条的一个错误

我说"1.20.1 / 1.21.1 的 `ClientRenderRegistry` 完全没有 `onRegisterRenderPipelines`"——**这是错的**。三个分支都有，而且是同一个 `@ApiStatus.AvailableSince("1.21.6")` 前向占位（同签名 `Consumer<RenderPipeline>`、方法体注释掉、留着 TODO）：

```java
    public static void onRegisterRenderPipelines(Consumer<RenderPipeline> registrar) {
//        if (true) return; // [1.21.10, )
//        registrar.accept(LaserBeamRenderState.LASER_BEAM_PIPELINE);
//        registrar.accept(LaserBeamRenderState.LASER_BEAM_ENTITY_PIPELINE);
        // TODO IrisShaders register
    }
```

我上次 grep 后面跟了 `head -8`，把匹配截断了，所以没看到。**那部分本来就能接上，你只是把 TODO 填了。** 唯一真正 1.21.5+ 的是 `registrar.accept(LASER_BEAM_PIPELINE)` 里对 pipeline builder 的用法，你在 1.20.1 上注释掉了——处理正确。

## 三个小疵

1. **`ModelObject.java` 有个自赋值笔误**：
    
    ```java
    MultiBufferSource.BufferSource bufferSource = bufferSource = ClientRenderHelper.GL.getModelBufferSource();
    ```
    
    多了个 `bufferSource =` 。能编译（你已实测跑起来），但明显是手滑，删掉多余的 `bufferSource =` 即可。
    
2. **`if (true) return` 那行在 1.21.6+ 分支上必须删掉。** 现在 1.21.6 那边是对的（没这行），但 1.21.10 / 1.21.11 / 26.x 移植时漏删就是**静默失效**——编译照过，修复悄无声息地没了。你注释里已经有 `// [1.20.1, 1.21.6)`，够用，我只是点一下这个坑的性质。
    
3. `stencilBufferSource` 字段在 1.20.1 上未被使用（和 `stencilEnabled` 一样），可接受；`ByteBufferBuilder` 只出现在注释里，不需要 import ✓；`Minecraft` import 在三个文件里都还有实际使用 ✓ 没有孤儿 import。
    

## 我没动这个分支

按 CLAUDE.md「除非显式要求，只改当前任务的分支」，1.20.1 这个 worktree 我一个字没改。那个笔误要不要我顺手修，你点头我就改。

# fork1

```
之前测试的应该都没有装CGC Compat Shader
现在代码状态下测试：
- 不装iris/sodium/Compat，4倍镜ocular里会渲染手，其他都正常
- 在iris/sodium/Compat，开启光影后，持枪（含倍镜）待机的时候手的位置固定在玩家朝向-180 0时的持枪待机时的手，并且红点和4倍镜准心始终可见
- 日志我还是留的之前的，不是新测的，可以看看是否是没装Compat
- 1.21.4不装光影和光影+Compat没问题

看下最近三个commit的修复，疑似把非光影状态下给搞坏了
```

# 

Let me look at the recent commits and the actual state, plus check whether the old logs had Compat.

Worktree is **clean** — my changes were committed. And the old logs had **no Compat** (confirming your suspicion). Let me see exactly what landed.

Worktree clean, and the old logs indeed had no Compat. Let me read the actual current code paths before theorizing.

Two key facts: the fix landed in `527c5a9d4` (with `getModelBufferSource` **disabled** by an `if (true)` short-circuit), and `f0824d720` then **enabled** it. Let me read the actual draw/flush pairing.

Let me review exactly what the fix commit landed, file by file.

The `_stencilFunc` region looks suspicious — the `[1.20.1, 1.21.6)` legacy call may have been uncommented. Let me read the actual current text.

## 三个 commit 的实际情况

|commit|内容|行为影响|
|---|---|---|
|`fea05f3d8`|`IrisCompat` +2 行|无|
|`527c5a9d4`|整个修复（5 个调用点 + `getModelBufferSource`），但里面 `if (true) return 共享源;` **是生效的**，且 `stencilBufferSource = ...` 那行**被注释**掉了|**等于没改**（那个函数恒返回共享源）|
|`f0824d720`|把那两行对调：短路注释掉、赋值放开|**修复从这一刻才真正生效**|

所以"最近三个 commit"里**唯一改变行为的是 `f0824d720`**——它打开了那套切换。你怀疑的方向对：非光影状态坏掉，就是这套切换的后果（之前一直没被测到，因为关闭光影+Iris 的组合修好之后就往前推了，无 iris 的基线没回头复测）。

旧日志确实没装 Compat（`cgccshader` 出现 0 次），这也解释了为什么那轮诊断里 `IrisCompat` 全是空实现。

## 我认为最可疑的一处（代码级，不是猜）

`HandRender:40` 和 `TextRender:61` 都写着同一句设计意图：

```
和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
```

delegate 拿到的 `_vertexBuffer` 就是枪械模型那支 `VertexConsumer` —— 这套设计**故意让手/文字和枪体共用同一个顶点缓冲**（这样它们和枪体在同一个批次、同一个模板状态下被提交）。而我的改动把这三处（`renderFirstPersonArm`、`HandRender`、`TextRender`）换成了**另一个独立的 `MultiBufferSource`**，正好把这层耦合拆掉了。渲染结果、批次边界、以及它们落在哪个模板状态里，全都跟着变。

还有一个结构性脆弱点：`getModelBufferSource()` 是**每次调用都重新求值**的。凡是"写"和"冲"是两次独立调用（`renderFirstPersonArm` 写 / `HandRender` 冲；`TextRender` 同理），只要中间 `stencilEnabled` 翻过一次，写进去的几何就会留在那个源里，被**下一次**某个 `endBatch()` 带着当时的相机矩阵画出来——这正是你报的"手的位置固定在某个朝向"那种症状。而 `renderModelPart` / `ModelObject.render` 是把源**存进局部变量**再自己冲的，那两处天然成对，不会错位。

## 建议

先说清取舍：**手那部分正是"iris 无光影时手不再出现在目镜里"的功臣**，所以整体回退会让那个已修的 case 复发。

Understood — reporting only, no code changes, no memory. Let me verify the state you reverted to so the report is accurate.

确认：HEAD 是 `51ab750f0 Revert "Update ClientRenderHelper.java"`，`getModelBufferSource()` 里的 `if (true) return 共享源;` 重新生效，5 个调用点留着但全部落到共享源上——即**行为回到了修改前**。工作区干净。

以下是留在对话里的报告。

---

# #60 光影兼容 · 1.21.6 阶段报告

## 1. 交付状态

代码回到修改前，未留未提交改动。当前 1.21.6 现象：

|环境|表现|
|---|---|
|无 iris / sodium|正常|
|iris + sodium，**开或不开光影**|准心始终可见 + 倍镜 ocular 为黑色|
|上述再 ± Compat Shader|**无差别**|

即 `527c5a9d4` 那轮修复已被 `51ab750f0` 整体回退，问题回到起点。1.21.1 / 1.21.4 不受影响。

## 2. 唯一一条硬结论（实测，可复用）

**开光影时准心/目镜失灵的根因：Iris 把命中材质映射的几何体重定向到它自己的 framebuffer，那个 framebuffer 没有 stencil 附件；而圆形模板孔留在主 target，两边根本不在同一个缓冲。**

探针打在 `GlCommandEncoder.drawFromBuffers`（`trySetup` **之后**、真正发 GL 绘制前），按绘制打标签：

|环境|绘制|`GL_STENCIL_TEST`|pass 的 StencilTest|`fbo`|stencil 附件|
|---|---|---|---|---|---|
|关光影|write / mask / ret / invert / clear|`true`|与 CGC 请求逐字一致|`4`|**TEXTURE**|
|开光影|write / mask / ret / read-divonly / clear|`true`|逐字一致|**`77`**|**NONE**|
|开光影|invert（`debugTriangleFan`）|`true`|`INVERT`|`4`|TEXTURE|

结论：状态传递、顺序、开关**全部正确**，是收件方被换掉了。所以 CGC 侧换缓冲源/改状态都无效。

**可投稿给 Iris 的要点**：环境 MC 1.21.6 / NeoForge 21.6.20-beta / Iris 1.9.6 / Sodium 0.7.2 / ComplementaryReimagined r5.8.1；用法 `RenderSystem.enableStencil` → `RenderType#draw` → `RenderPass#enableStencilTest`；诉求是让重定向后绘制所用 framebuffer 的深度纹理带上 stencil 方面（Iris 本就从主 target 推导 `DepthBufferFormat`，且有 `DEPTH_STENCIL` / `isCombinedStencil()` 分支），否则请明确该 API 在光影下不支持。我未查清 `fbo=77` 具体用的是哪张深度纹理（`RenderTargets` 里除主深度外还有 `noHand` / `noTranslucents`）。

## 3. 被推翻的假设（别重走）

- ~~Iris 自己的 render target 没有 stencil~~（据 `IrisRenderingPipeline` 从主 target 推导深度格式而否掉）→ **实际是对的**，但判据错：关键不是深度纹理格式，而是**绘制所用 FBO 的附件**。中途我因此撤回了一次正确结论。
- ~~`iris$bypassSetup` 的 `CustomPass` 绕过吃掉了模板应用~~ → `setCustomPass` 只被合成/后处理 pass 使用（`CenterDepthProbe`/`CompositeRenderer`/`FinalPassRenderer`/`ShadowCompositeRenderer`），`HandRenderer` 引用 0 次，不适用。
- ~~`clear` 插进 write 与 read 之间~~、~~一帧内跑多遍、顺序错乱~~ → 时序探针显示控制流与关光影**完全一致**。
- ~~`endBatch(RenderType)` 空实现是开光影症状的原因~~ → 那条对"关光影 + Iris"成立，但对开光影不是。

## 4. 非光影回归的定位（未验完）

三个 commit 里**只有 `f0824d720` 改变了行为**——它把 `getModelBufferSource()` 里那两行对调，让切换真正生效（之前 `if (true)` + 赋值被注释，函数恒返回共享源，等于没改）。`51ab750f0` 又把它退回去了。

两条候选机制，都没验完就回退了：

1. **拆掉了"手/文字与枪械模型共用顶点缓冲"的既定耦合。** `HandRender:40`、`TextRender:61` 都注明"和枪械模型共用顶点缓冲的需要代理到渲染结束后渲染"，delegate 拿到的 `_vertexBuffer` 就是枪体那支 consumer。给它们单独的 `MultiBufferSource` 等于把这层耦合拆了，批次边界与所处模板状态随之改变。
2. **`getModelBufferSource()` 每次调用重新求值**，而 `renderFirstPersonArm`（写）与 `HandRender`（冲）是两次独立调用，`TextRender` 同理。中间只要 `stencilEnabled` 翻过一次，写进去的几何会留在那个源里，被**后续**某个 `endBatch()` 用当时的相机矩阵画出来——这正是"手固定在某个朝向"的签名。`renderModelPart` / `ModelObject.render` 把源存进局部变量再自己冲，那两处天然成对、不会错位。

当时准备好的候选修法是"**显式传源**"（`HandRender`/`TextRender` 取一次源，传给 `renderFirstPersonArm`，并冲同一个对象），以消除第 2 条的脆弱性、同时保留 iris 无光影那条已观测到的改善。已回退，未实施。

另外要说明：**iris 无光影那条的根因从来没定位过**。当时只观测到"手不再出现在目镜里"，没证明机制。

## 5. 下次接手的建议

- **别再给 arm/text 换独立缓冲源。** 与既定设计冲突（见 4.1）。若要继续 iris-无光影那条，方向应该是查**为什么 Iris 的全缓冲源 flush 会丢模板**，以及让手/文字留回与枪体同一批次。
- **开光影那条**只有两条路：上游 Iris（材料见第 2 节），或把瞄具遮罩改成不依赖 stencil（alpha / 几何遮罩）。设计级改动，建议先出方案。
- **探针要避的坑**（我这轮踩了三次）：采样点必须在 `trySetup` **之后**（`GlCommandEncoder.drawFromBuffers`，不是 `executeDraw` HEAD——后者读到的前一筆）；去重键必须**包含状态值**，否则会把不同模板函数压成一条；用 tag 标出被观察的那几笔绘制；上限按 **per-tag** 而不是全局，否则日志会在开头就被截断。
- **测试矩阵**：无 iris / iris-无光影 / iris-有光影 / 各自 ± Compat，共 6 组。旧的两份日志（`D:\Temp-File\debug-iris-sodium-*.log`）**都是没装 Compat 的**（`cgccshader` 出现 0 次），所以之前所有结论都只覆盖"无 Compat"这一列。

## 6. 顺手的记录

- `527c5a9d4` 里新增了 `docs/deprecated/prompt/single/60 Shader-compat-fork1.md`（944 行），是这轮过程的记录。
- 带全部探针的版本我存过一份快照在 `/tmp/cgc-1216-with-probes.patch`（300 行，临时目录，可能已被清理）。
- `IrisCompat.registerRenderPipelines` 那条（激光束自定义管线在光影下绑 ShaderKey）**始终没做**，与本次症状无关，仍是独立待办。

---

需要我把这份报告落到仓库文档里，或者按第 5 节的方向出个具体方案，说一声。

#
---

```
现在继续处理./docs/deprecated/prompt/single/60 Shader-compat-fork1.md的问题

当前：
- CGC Compat Shader 1.21.1和1.21.4代码没区别，都正常工作
- 1.21.6安装sodium没问题，安装iris+sodium后，无论开不开光影，都会始终显示红点/倍镜的准心，切倍镜的ocular始终是模型自身的黑色
- 1.21.16 iris/sodium降回最低支持的1.21.6版本仍然存在该问题，默认直接以最新iris/sodium为准进行兼容
- 1.21.10-26.3，安装iris+sodium，在不开光影的时候正常，开光影时问题仍存在

iris+sodium+关光影时的问题仅在1.21.6存在，允许CGC Compat Shader对iris/sodium进行mixin来实现关光影的情况下正常

iris+sodium+开光影时的问题默认先处理1.21.6，等通过后cherry pick到高版本验证是否需要再适配
- 如果要大改渲染方式，考虑是否能通过封装渲染接口，如新增渲染管线等，让1.20.1-1.21.4使用旧的渲染，1.21.6起解除门控注释来更换渲染的方式实现，从而实现跨版本统一

当前IDEA打开了CGC 1.21.1，1.21.6，26.3，1.20.1，CGC Compat Shader1.21.1，1.21.6，26.3多个窗口供浏览源码
- iris和sodium源码在Compat Shader窗口里浏览
```

# 

我先读取该提示文档。

我先做侦察：确认两个 1.21.6 仓库的当前状态与 Compat Shader 的既有 mixin 结构。

第一条命令的 `cd` 影响了并行执行——我用显式路径重查 CGC 工作区，避免上次那种工作目录漂移。

现在我读 Compat Shader 的构建配置和 CGC 侧的兼容层现状。

现在定位 Iris/Sodium 的 jar，确认要 mixin 的目标类。

类都在。用 IDEA MCP 反编译 Iris 的批处理源，确认 flush 路径。

现在读 CGC 侧的绘制点，确认 flush 语义。

现在核对 MC 1.21.6 的 `RenderPass` 模板 API，确认 mixin 注入点可行。

核对 MC 侧 FBO 的 stencil 附件判定，以及 Iris 在不开光影时把 pass 指向哪个 target。

关键发现：`endBatch` 里 `iris$getPipeline() == null` 时走的是 vanilla `type.draw()`。我需要确认哪条分支在不开光影时生效。

分析完成，根因链已在源码级确认。建立任务清单后实施。

git 报的是整文件 EOL 变化，我先隔离这是不是行尾归一化造成的假象。

我的 sed 在文件末尾多塞了一个 CR。改用只做定点插入的方式重做，保证除新增行外逐字节不变。

编译通过。现在用 javap 核对注入点的字节码描述符是否逐字匹配。

工作目录又漂到了 CGC 仓库，我改用显式路径复查改动。

工作区改动完成且其它 worktree 未受影响。我把这次踩到的环境坑记入记忆。

改动完成、编译通过。以下是这一轮的结论与变更。

## 根因链（源码级确认，非推断）

之前那份文档里"关光影靠换缓冲源修好"的做法要改在兼容层做，但**根因本身和缓冲源无关**，链路是：

1. `iris-batched-entity-rendering.mixins.json` 是 `"required": true`，所以装了 Iris 就**恒定**把 `RenderBuffers.bufferSource()` 换成 `FullyBufferedMultiBufferSource`——**跟开不开光影包无关**（这解释了为什么关光影也坏）。
2. 它的 `endBatch()` 按 `type.iris$getPipeline()` 分叉。`RenderType.CompositeRenderType` 的 `iris$getPipeline()` 返回 `this.renderPipeline`（`MixinRenderType` 字节码），**恒非空**，所以 CGC 的几何体恒走 Iris 那条自建 `RenderPass` 分支，而不是 vanilla 的 `type.draw()`。
3. 那条链（`createRenderPass` → `setPipeline` → `RenderSystem.bindDefaultUniforms` → `BufferSegmentRenderer.drawInner`）**没有 `enableStencilTest`**。而 1.21.6 的 `RenderSystem.STENCIL_TEST` 在 vanilla 侧唯一读取者就是 `RenderType.CompositeRenderType.draw` 的那两行。
4. ⇒ 模板状态设了但到不了绘制：写入（`GREATER/REPLACE`）不生效、读取（`EQUAL i+1` 目镜遮罩 / `EQUAL ~(i+1)` 准心 / `EQUAL 0` 镜身）全部退化成恒真。

顺带确认了两件让修法可行的前提：`_clearStencilBuffer` 清的是**主 target** 的模板，CGC 的 RenderType 默认 `OutputState = MAIN_TARGET`（`RenderStateShard.MAIN_TARGET`），所以两者的目标一致；主 target 有 stencil 附件（CGC 经 `ConfigureMainRenderTargetEvent::enableStencil` 打开）。圆形模板孔走 `RenderType.debugTriangleFan().draw(...)` 的即时路径，本来就读 `STENCIL_TEST`，不受影响。

## 改动（只在 CGC Compat Shader 1.21.6，CGC 本体零改动）

新增 [FullyBufferedMultiBufferSourceMixin.java](app://localhost/epitaxy/local_1fec1fe6-1e34-43b4-9002-70dc89169ecc)：用 MixinExtras `@WrapOperation` 包住 `endBatch` / `endBatchWithType` 里那次 `CommandEncoder.createRenderPass(...)`，把 vanilla 那两行补回去：

```java
StencilTest stencilTest = RenderSystem.STENCIL_TEST;
if (stencilTest != null) pass.enableStencilTest(stencilTest);
```

外加上 `cgccshaderclient.mixins.json` 一行注册。注入点的字节码描述符我用 `javap -c` 逐字核过（两个方法各一处，`invokeinterface CommandEncoder.createRenderPass:(Supplier;GpuTextureView;OptionalInt;GpuTextureView;OptionalDouble)RenderPass;`）。

**为什么这次不会像上轮那样把非光影基线搞坏**：上轮坏在改 `getModelBufferSource()` 的取源点（拆了手/文字与枪体共用顶点缓冲的耦合）。这次 CGC 一个字没动，`getModelBufferSource()` 保持回退后的原样；这个 mixin 只在装了 Iris 时才存在，`RenderSystem.STENCIL_TEST` 为 null（模板窗口之外）时纯 no-op。另外 `_colorMask(false)`/`_depthMask(false)` 那条链本来就活着（`GlRenderPass.setPipeline:74` 会 `applyPipelineModifiers`），缺的只有模板测试这一项，所以补这一行就够。

## 开光影那条：本次**未改**，行为与现在一致

光影包开启时 pass 的目标是 Iris 自己的 framebuffer，深度纹理没有 stencil 方面 → 按 GL 规范"没有 stencil 缓冲等同恒真"，这一行加不加表现一样。所以我没有为它加门控（无谓的分支），这条仍按你的计划留到下一步。

## 需要你实测确认

装 iris+sodium、**不**开光影：

1. 红点/倍镜准心只在圆形视野内（不再满屏）；
2. 4 倍镜开镜后 ocular 里是场景，不是模型自身的黑色；
3. 关镜后枪体没有被残留圆形区域裁掉；
4. 顺带看一眼**第一人称的其他几何体有没有被误裁**（见下面的残余风险）。

## 残余风险（我没法在游戏里验，如实报）

Iris 的 `endBatch()` 是**整批** flush，而 vanilla 的 `endBatch(rt)` 只冲那一个 RenderType。所以 CGC 模板窗口内**第一次** flush 时，若共享源里正好还留着别人排队的几何体，它也会吃到当次的模板状态。实际暴露面很小：CGC 每个绘制点都会触发整批 flush，队列基本是空的；而且带 `ocularRingPath` 的常见路径下窗口内第一笔用的是 `GL_ALWAYS/KEEP`（等于没开）。真正的暴露只有 `ocularRingPath == null` 时窗口内第一笔就是 `REPLACE` 写入这一种。若你测出某个第一人称元素消失/被裁，就是这条，届时我再决定是加"窗口开启前先冲一次"还是别的收口方式。

未 commit、未 push；CGC 1.21.6 与其余 worktree 全干净，编译用绝对路径 `"$R/gradlew" -p "$R"`（这轮 shell cwd 又漂了一次，我已把这条环境坑记进记忆）。

# 

```
已经修复关光影下的问题，现在继续开光影时的问题修复
```

# 

关光影已过，很好。开光影这条要断的是"模板缓冲在哪个 target"，我先把 Iris 侧的深度纹理格式链路查清。

关键还差一环：光影下这些几何体的 pass 用的是哪个 depth 纹理。我并行查两边——Iris 的包装类型指向哪个 target，以及 MC 主 target 的深度格式。

这条比关光影深，我先把一个决定性问题交给子 Agent 静态查清（它会读字节码），同时我自己梳理设计空间。

子 Agent 在查 Iris 内部。我同时从 CGC 侧确认枪械渲染在帧内的位置（这决定"改投递目标"是否可行）。

我已经把关键问题交给了子 Agent（静态追 Iris 在光影下把 CGC 的几何体渲染到哪个 target、那个 depth 有没有 stencil 附件），结果回来后我再给方案。

同时我自己这一侧的结论先补充一点：**1.21.10+ 的"关光影正常、开光影仍坏"正好佐证了机制**——那些版本 Iris 已删掉批处理，CGC 的绘制走 vanilla `RenderType.draw`，也就是模板测试**本来就带上了**，可开光影仍然坏。这说明开光影缺的不是"状态传递"（那一环关光影已经被修掉了），而是**收件方的 framebuffer 没有 stencil 附件**——和上一轮探针在 `fbo=77` 上量到的 `stencil=NONE` 一致。

所以开光影这条只剩两种可能出路，都取决于子 Agent 要回答的那个问题：

1. 若那批几何体本就该落在带 stencil 的 target 上（只是 `endBatch` 挑了别处），那可以在现有 mixin 里再补一步"模板窗口期间改写传给 `createRenderPass` 的 color/depth 视图"——几行的量级；
2. 若它落在一个结构上就没有 stencil 的 gbuffer 上（且那正是枪体获得光影效果所必需的），那 GL 模板这条路在光影下就不成立，需要按你之前提的方向把瞄具遮罩改成不依赖 stencil 的实现（属于设计级改动 + 跨版本门控，我会先出方案再落地）。

等结果。

子 Agent 的结论推翻了我之前的假设，而且指向一个更具体的位置。我要亲自核实两处关键实现。

`addDepthAttachment` 用的是 `GL_DEPTH_ATTACHMENT`(36096)——这很可能就是开光影缺 stencil 的原因。我核实 Iris 的世界 FBO 是否复用主 target 的 depth 纹理。

`addDepthAttachment` 用 `GL_DEPTH_ATTACHMENT`(36096) 挂的是**主 target 的 depth-stencil 纹理**——所以 stencil 方面被丢掉了。我核实覆盖范围（所有 FBO 构造路径）和调用签名。

机制已经闭环：Iris 把**主 target 的 depth-stencil 纹理**用 `GL_DEPTH_ATTACHMENT` 挂到自己的 FBO 上，stencil 方面因此不可见。我确认一下纹理格式 API 后落地第二处修复。

机制确认完毕。现在写第二处 mixin——让 Iris 的 FBO 用 `GL_DEPTH_STENCIL_ATTACHMENT` 挂那张 depth-stencil 纹理。

项目里没有生成 refmap——说明 mixin 注解处理器没跑，`@Local` sugar（依赖 AP）会在运行时失效。我改用纯 Mixin 的写法。

开光影这条的根因找到了，而且是字节码级可验证的一处具体缺陷。已落地第二处 mixin，编译通过。

## 根因（开光影）

上一轮探针量到的 `fbo=77 / stencil=NONE` 和 `fbo=4 / stencil=TEXTURE` 这对差异，现在能完整解释：

**1. 绘制落点由"当前绑定的 FBO"决定，而不是 pass 自己的 FBO。** Iris 的 `MixinGlCommandEncoder` 对 `createRenderPass` 内部的 `_glBindFramebuffer` 做了 `@Redirect`：

```java
if (shadows || ImmediateState.safeToMultiply) { this.tempFBO = fbo; return; }  // 跳过绑定
```

而 `safeToMultiply` 只在 `FullyBufferedMultiBufferSource.endBatch()` 的首尾置真/假——也就是说 **CGC 的瞄具几何体在 Iris 的 flush 窗口内画时，压根没绑到自己那个 pass 的 FBO**，而是落进 Iris 当时已绑定的世界 FBO（colortex0 + 世界深度）。这就是 `fbo=77`。圆形模板孔走 `debugTriangleFan().draw(...)`，在 flush 窗口之外，所以真绑 → 主 target 的 FBO → `fbo=4`。（顺带修正上一轮文档里一处说法：`debugTriangleFan` 也是 `CompositeRenderType`、`iris$getPipeline()` 非 null，区分点不是"pipeline 是否为 null"，而是"在不在 flush 窗口内"。）

**2. 那个世界 FBO 的 depth 附件把 stencil 方面丢掉了。** Iris 的 `RenderTargets` **复用的就是主 target 那张 depth 纹理**（`IrisRenderingPipeline` 取 `MainTarget.getDepthTexture()` 传进去，`createGbufferFramebuffer` / `createColorFramebufferWithDepth` 都走 `addDepthAttachment(currentDepthTexture)`）。而 `GlFramebuffer.addDepthAttachment` 是：

```java
IrisRenderSystem.framebufferTexture2D(fbo, 36160 /*FRAMEBUFFER*/, 36096 /*DEPTH_ATTACHMENT*/, 3553, texId, 0);
```

`36096 = GL_DEPTH_ATTACHMENT`——combined depth-stencil 纹理这么挂，只有一个 depth 方面，stencil 不可见（所以 `GL_STENCIL_ATTACHMENT` 查到 `GL_NONE`）。**我核过 CGC 确实已经让主 target 用上 stencil 格式**，也核过那个 FBO 就是模板孔所在的同一张纹理。

⇒ 开光影坏的完整链条：模板状态传递（上一轮已修）没问题了，但这批几何体的**收件方**既不是主 target（被 Iris 跳过绑定），自己又不暴露 stencil → 测试无从比较 → 遮罩/准心恒真、写入也不生效。

## 改动（仍是 Compat Shader 1.21.6，CGC 本体零改动）

新增 [GlFramebufferDepthStencilMixin.java](app://localhost/epitaxy/local_1fec1fe6-1e34-43b4-9002-70dc89169ecc)：`@Redirect` 掉 `addDepthAttachment` 里那次 `framebufferTexture2D`，当纹理格式 `hasStencilAspect()` 时把附件点从 `GL_DEPTH_ATTACHMENT`(36096) 换成 `GL_DEPTH_STENCIL_ATTACHMENT`(33306)，depth-only 的（如阴影用 `DEPTH32`）保持原样。外加上 mixins.json 一行注册。

关键收益：Iris 的世界 FBO 与主 target **共用同一张深度纹理**，而 stencil 缓冲是纹理的一部分——**于是两者共享同一个模板缓冲**。模板写入、目镜黑遮罩、准心（经 Iris 缓冲源画在世界 FBO）、圆形模板孔（画在主 target FBO）、以及 `_clearStencilBuffer` 清的，全部落在同一个模板缓冲里。

这也比"把整批几何体改投主 target"更合适：枪体保持画在 Iris 的世界 FBO 里，光影效果不受影响。

## 请实测（build 已编译好）

开光影，装 iris+sodium+Compat：

1. 红点/倍镜准心只出现在圆形视野内；
2. 4 倍镜开镜后 ocular 里是场景，不是模型自身的黑色；
3. 关镜后枪体没有被残留圆形区域裁掉。

同时**请再复测一遍关光影**：这处新 mixin 改的是 Iris 自己的 FBO 装配，理论上不开光影时 Iris 不建这些 FBO（`RenderTargets` 只在加载光影包时构造）所以应当是空操作，但我不想只靠推断。

要盯的两点（我无法在游戏里判断）：

- **遮罩现在画进 Iris 的世界颜色缓冲、在 composite 之前**，所以黑色镜圈/准心可能被光影包着色（受光照/雾/色调映射影响）。如果看起来"被打了光"而不是纯黑，那是下一步要调的点，不是本次机制错了。
- 与前一轮同样的整批 flush 副作用：若某个第一人称元素被误裁，报我。

## 状态

未 commit、未 push（pack-off 那轮你已经提交为 `d4483b5`，我这次只新增一个文件 + json 一行，`git status` 里就这两项）。CGC 1.21.6 工作区干净。

顺带记一条踩到的坑：这几个 compat 仓库**不跑 mixin 注解处理器**（`build` 里没有 refmap），所以 MixinExtras 的 sugar 注解 `@Local`/`@Share` 会在运行时静默失效——我原本用 `@Local` 拿方法入参，改成了 `@Inject(HEAD)` 存 `@Unique` 字段。`@WrapOperation` 这类运行期内建的注解不受影响。

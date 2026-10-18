> #72 提示词

# 

```
读./docs-tacz/deprecated/prompt/issue/755 shader compat.md
现在要在CGC完成RenderHandEvent的传递重构，在适当位置加上传递

- 当前打开了1.20.1，1.21.1neoforge，1.21.10neoforge，26.3neoforge，CGC Compat Shader1.20.1供浏览源码
- 1.21.10改用SubmitNodeCollector延迟渲染，1.20.1获取MultiBufferSource也需要用getMultiBufferSource_SubmitNodeCollector接口
- 如果1.21.10或高版本需要SubmitNodeCollector引入的一层缩进，则1.20.1也像已有代码那样加一个代码块（用于减少merge后修改的diff行数/保留该行的commit记录不被覆盖）
- CGC本地的IrisCompat的接口保留，如无必要不应该删除
```

# 

I'll start by reading the issue document specified.

I'll explore the current first-person render path and the `getMultiBufferSource_SubmitNodeCollector` interface across the worktrees.

Now let me read the core first-person render path and event interface in 1.20.1.

Now the renderer implementations — let me compare 1.20.1 with 1.21.10 and 26.3.

Let me inspect `FirstPersonArmHelper` and how the bedrock model obtains its buffer source.

Let me look at the bedrock model render pipeline in both 1.20.1 and 26.3.

Let me examine the 1.21.10 branch's transitional design in detail.

The higher-version docs likely describe the intended design. Let me read them.

Let me read the IrisCompat layers and the functional renderers that write vertices.

I'll locate the Oculus jar to verify the wrapper semantics.

The `#36` prompt doc looks directly relevant. Let me read it.

Let me check the shader-compat docs for prior intent around this refactor.

Let me inspect Oculus's HandRenderer flush timing and CGC's stencil helpers.

The 1.21.10 branch has a detailed buffer-source analysis. Let me read it.

Let me check the remaining direct buffer-source call sites and the BEWLR paths.

Let me compare the 26.3 functional renderers to mirror their block structure.

I now have the full picture. Let me make the edits, starting with the central source selector in `ClientRenderHelper`.

Now let me verify the build. First checking the project modules and JDK.

Build passes. Let me review the full diff.

Let me verify the final code sections and overall status.

已完成 1.20.1 分支的 RenderHandEvent 缓冲源传递重构，`:core:compileJava` 与 `:forge-compat:compileJava` 均通过（exit 0，仅有既有 deprecation 警告）。未提交。

## 改动

**1. 入口侧新增取值口** — [ClientRenderHelper.java:484](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:484)

`FirstPersonArmHelper` 增加 `getFirstPersonArmBufferSource()`，从已有的 `FIRST_PERSON_ARM_COLLECTOR` 线程局部变量里按 `instanceof MultiBufferSource` 取出事件传入的源。1.21.10+ 那里存的是 `SubmitNodeCollector`，会自动返回 `null`，合并后不会误用。

**2. 统一选源点** — [ClientRenderHelper.java:256](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:256)

`GL.getModelBufferSource()` 在 `[1.20.1, 1.21.6)` 分支优先返回第一人称传入的源；没有传入源（第三人称、BEWLR、GUI）时回退到 `renderBuffers().bufferSource()`，行为不变。光影下的传入源就是 Iris/Oculus 的 `UnflushableWrapper`，顶点写进 `HandRenderer` 的私有全缓冲源，由 Iris 在 `renderSolid` 结束时代为 flush，CGC 不再触碰共享源，从根上避开 #755 描述的 `getBuffer → removeReady` 丢 segment。

因为所有模型渲染本来就走 `getModelBufferSource()`，这一处改动自动传递到了：
- [ModelObject.java:191](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java:191)（枪体/配件/弹壳模型）
- [_AttachmentModelRender.java:228](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:228)（瞄具/目镜/镜身/准星）
- [TextRender.java:79](core/src/main/java/dev/xcolorful/customgun/client/renderer/model/TextRender.java:79)
- [HandRender.java:49](core/src/main/java/dev/xcolorful/customgun/client/renderer/model/HandRender.java:49)

**3. 原先绕过选源点、直接取共享源的调用点改为同一入口**
- [ClientRenderHelper.java:120](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:120) 第一人称手臂
- [MuzzleFlashRender.java:142](core/src/main/java/dev/xcolorful/customgun/client/renderer/model/MuzzleFlashRender.java:142) 枪口火焰
- [BeamRender.java:64](core/src/main/java/dev/xcolorful/customgun/client/renderer/model/BeamRender.java:64) 激光束
- [AttachmentRender.java:59](core/src/main/java/dev/xcolorful/customgun/client/renderer/model/AttachmentRender.java:59) 缺失材质提示模型

**4. 结构与合并**：`ModelObject`、`renderModelPart`、`MuzzleFlashRender`、`AttachmentRender`、`renderFirstPersonArm` 中原本就有与高版本 `submitCustomGeometry(..., (pose, vc) -> { ... })` 对齐的裸 `{ }` 块，未新增缩进层级，也没有重排这些行。`_AttachmentModelRender.renderOcularAndDivision` 的圆形模板扇形保持 `BufferUploader` 立即绘制（1.20.1 模板是裸 GL 状态，不能走延迟 source）。`IrisCompat` 全部接口与调用点原样保留。

## 需要你实测的风险（重要）

光影包开启时传入源是 `UnflushableWrapper`，它的 `endBatch()` 是空实现，几何体会延迟到 `renderSolid` 末尾才真正绘制——而那时 CGC 已经执行过 `disableItemEntityStencilTest()` 和 `_clearStencilBuffer()`。因此**带瞄具时的模板遮罩（镜内不画枪体、ocular/准心圆形孔）可能失效**，这是延迟 flush 的固有语义差异。不开光影、装了 Iris 但未启用光影包、以及纯 vanilla 三种情况行为与之前完全一致（事件源就是共享源，仍会在模板窗口内立即 flush）。

如果实测出现开镜遮罩异常，两条后续路线：① 用 `GL.stencilEnabled` 门控，让模板窗口内的几何继续走共享源；② 在 CGC-Compat-Shader 里让 `IrisCompat.endBatch` 识别 `UnflushableWrapper` 并 flush 其内部源。这两条都需要你确认后我再动。

# 

```
目前的1.20.1状态：
- 装了CGC Compat Shader后，无论是Complementary还是Hysteria Shaders，红点瞄准镜准心始终显示，ocular黑色；筒镜准心也是准心显示+ocular黑色，并且开镜的视角转变是生硬的线性的而没有之前的效果
- 不装CGC Compat Shader时，没有触发Hysteria Shaders内存泄漏问题，以上问题还在
- 不装CGC Compat Shader并去掉光影oculus/embeddium后，全部正常，没有开镜线性运动或ocular或准心渲染问题
```

# 

Let me verify Iris's flush semantics before deciding the gating, and check how the aim FOV transition is driven.

This suggests a better fix: give stencil-window geometry its own vanilla immediate buffer source (the 1.21.6 approach, backported). Let me verify the 1.20.1 APIs.

Let me check how the aim FOV transition is computed, to judge whether the buffer source change can affect it.

The regressions are consistent with deferred flush happening after the stencil window closes. I'll keep the event-source passing for non-masked geometry, and give the stencil window its own immediate vanilla buffer source (the design already used on 1.21.6).

Let me review the attachment render flow to ensure the private source is used consistently in nested windows.

找到原因了：问题出在**延迟 flush 跨出了模板窗口**，与缓冲源本身无关。

## 诊断

光影下 `RenderHandEvent` 给的是 Iris 的 `UnflushableWrapper`（内部是 `HandRenderer` 的私有全缓冲源）。它的 `endBatch()` 是空实现，几何体要等 `HandRenderer.renderSolid` 结尾的 `readyUp()+endBatch()` 才真正绘制。而 CGC 在 `_GunModelRender.render` 里先画瞄具/枪体，随后就 `disableItemEntityStencilTest()` + 清空模板缓冲——等 Iris 真正 draw 时，模板测试已关闭、模板值已清空，所以：

- ocular 遮罩 / 准心裁剪全部失效 → 准心常显、ocular 黑色；
- 开镜时圆形遮罩的展开动画也一起失效，视觉上只剩下 FOV 变化 → 表现为生硬的线性过渡。

不装 Compat 也一样，因为传入源来自 `RenderHandEvent` 本身，不依赖 compat mixin；去掉 Oculus 后事件源退化成普通共享源、即时绘制，所以全部正常。

## 修复

模板窗口内不能交给延迟源。按 1.21.6 分支已有的设计，在 [ClientRenderHelper.java:256](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:256) 分两条路：

1. **`stencilEnabled` 期间**：使用 CGC 自有的 `MultiBufferSource.immediate(new BufferBuilder(786432))`。它走 vanilla `RenderType.end` → `setupRenderState/draw/clearRenderState`，在窗口内**立即绘制**，模板状态正确；同时完全不碰 `RenderBuffers.bufferSource()`，所以 #755 的 `getBuffer → removeReady` 丢 segment 也不会发生。
2. **非模板窗口**：继续使用 `RenderHandEvent` 传入的源（光影下即 Iris 私有源），A 弹道、激光、瞄具本体等不需要模板的部分仍然走传递。

另外在 [ClientRenderHelper.java:78](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:78) 的 `disableItemEntityStencilTest()` 里，关闭模板测试**之前**先把自有即时源的残留几何体 flush 掉——`MuzzleFlashRender` 这类委托渲染不会自己 flush，否则会漏到下一帧。

`IrisCompat` 接口与 `FirstPersonRender` 原有的预冲刷都保留；`ModelObject`、`renderModelPart`、`TextRender`、`HandRender` 仍通过同一个 `getModelBufferSource()` 取源，没有新增参数。

`:core:compileJava` + `:forge-compat:compileJava` 通过（exit 0）。请重点复测：Compatible/Hysteria 下红点与筒镜的 ocular/准心、开镜过渡，以及不装 Compat Shader 时的 Hysteria 内存曲线。

# 

```
- FOV问题我用了修改前的依然存在，这个先不管
- 1.20.1的ocular/准心问题已经修好了
- 为防止这个修改在高版本有问题，你先在1.21.1neoforge, 1.21.6neoforge, 1.21.10neoforge, 26.2neoforge, 26.3neoforge都应用这个修改，我看没问题再Merge
```

# 

我会逐个 worktree 检查 `getModelBufferSource`、模板窗口和直接取源调用点，再按各版本 API 落地。

先确认 1.21.10 的 `bakePipelineState` 是否可用，以及 26.2 的等价机制，再决定各版本怎么落地。

1.21.1 already uses `ByteBufferBuilder`; fixing that.

1.21.6 的编译失败是分支自带的无关错误（`ItemInHandLayerMixin` 未修改却引用了不存在的 `itemStack`）。我先确认 1.21.10 的事件是否还能给出 `MultiBufferSource`。

已按版本分别处理。1.20.1 保持上一轮修复不变。

## 1.21.1neoforge — 已应用，编译通过（exit 0）

与 1.20.1 同一套改动（4 个文件，+53/−8）：
- `getModelBufferSource()`：`stencilEnabled` 期间返回自有即时源 `MultiBufferSource.immediate(new ByteBufferBuilder(786432))`；否则优先用事件传入的 `MultiBufferSource`，没有则回退共享源。
- `disableItemEntityStencilTest()` 在 `glDisable(GL_STENCIL_TEST)` 前先 flush 自有源。
- `FirstPersonArmHelper.getFirstPersonArmBufferSource()`；`renderFirstPersonArm`、`MuzzleFlashRender`、`BeamRender`、`AttachmentRender` 改为走 `getModelBufferSource()`。

注意：1.21.1 的 `MultiBufferSource.immediate` 已改成收 `ByteBufferBuilder`（不是 1.20.1 的 `BufferBuilder`），已按实际 API 写。

## 1.21.6neoforge — 已应用，但分支本身编译不过

同样的 4 文件改动，差异只有两处版本适配：
- 即时源用 `ByteBufferBuilder`；
- flush 放在 `RenderSystem.disableStencil()` 之前。

`core` 的编译结果只有 **1 个错误**，且不在本次改动范围内：

```
ItemInHandLayerMixin.java:62: error: cannot find symbol
    || IGunGetter.fromItemStack(itemStack) != null)) {
```

该文件在工作区中未被修改（HEAD 内容），`itemStack` 在 `cgc$renderArmWithItemHead` 的参数里不存在。这是 1.21.6 分支自带的断点，需要你先确认是补这个变量还是回退那段逻辑——要我顺手修的话说一声。

## 1.21.10neoforge — 未应用，原因如下

这里不能照搬：
1. `NeoRenderHandEvent.getMultiBufferSource()` 返回 `null`，事件只给 `SubmitNodeCollector`；`FirstPersonArmHelper` 也是 `ThreadLocal<SubmitNodeCollector>`，没有 `MultiBufferSource` 可传。
2. 模型渲染仍是即时 `MultiBufferSource`。改用 collector 等于把 `ModelObject`/配件/特效全部迁移到 `submitCustomGeometry`（26.2 那套），而 `RenderTypePipelineBake` 在 26.2 之前是空实现——延迟提交无法携带 NeoForge 的模板状态，ocular/准心会重现 1.20.1 刚修好的问题。
3. #755 的泄漏前提（Iris `batchedentityrendering` 的全缓冲源）在 1.21.10 已被 Iris 1.9.7 移除，共享源是普通 `BufferSource`，`endBatch` 即时绘制，模板路径本来正常。

所以这条分支维持现状。如果你确实要在 1.21.10 上完成 collector 传递，那是另一项迁移（同时要解决模板状态烘焙），需要单独评估。

## 26.2neoforge / 26.3neoforge — 无需改动

这两个分支已经是本次重构的“终点形态”：
- `ModelObject.render`、`_AttachmentModelRender`、`BeamRender`、`MuzzleFlashRender`、`AttachmentRender`、`TextRender`、`renderFirstPersonArm` 全部通过 `FirstPersonArmHelper.getFirstPersonArmCollector()` 使用事件提供的 `SubmitNodeCollector`；
- 模板/颜色/深度状态由 `RenderTypePipelineBake` 在提交时烘焙进 `RenderType`，不存在“flush 跨出模板窗口”的问题；
- `getModelBufferSource()` 已无调用者（占位方法）。

因此这两个分支不需要应用这套缓冲源改动。工作区均保持干净，未提交。

# 

```
- 1.20.1-26.1.2各分支复用ClientRenderHelper.GL.getModelBufferSource()的复用我已经merge到各分支（功能不变）
- 26.2测试下来不装CGC Compat Shader确实不会触发OOM问题
- 1.21.6也已经测试通过
- 现在把ClientRenderHelper里暂存的修改都分析出具体适用的版本范围，1.21.10的窗口已经打开
- 如果高版本不能直接应用，那么1.21.10以下的变动需要像已有模式那样标版本范围，尽可能可以靠merge时注释掉来实现不影响高版本
```

# 

先读取磁盘上当前状态的 1.20.1 与 1.21.10 的 `ClientRenderHelper`。

明确了。三处新增逻辑的适用上界都是 1.21.10：事件从 1.21.10 起只给 `SubmitNodeCollector`，且 Iris 1.9.7 已删除批处理，共享源重新变成即时提交，不再需要这层中转。现在把范围标上去。

已完成版本范围标注，并核对了 1.21.10 的机制。1.20.1 编译通过（exit 0）。

## 适用版本范围

| 暂存改动 | 范围 | 上界依据 |
|---|---|---|
| `disableItemEntityStencilTest()` 里窗口关闭前 `GL._flushStencilBufferSource()` | `[1.20.1, 1.21.10)` | 只有低版本会用到自有即时源；1.21.10 起不再需要中转 |
| `getModelBufferSource()` 的 `stencilEnabled` → 自有即时源分支 | `[1.20.1, 1.21.10)` | 同上 |
| `getModelBufferSource()` 的事件源优先分支 | `[1.20.1, 1.21.10)` | 事件只在 1.21.10 以前给 `MultiBufferSource`；之后是 `SubmitNodeCollector` |
| `_flushStencilBufferSource()` 方法体 | `[1.20.1, 1.21.10)` | 同上 |
| `FirstPersonArmHelper.getFirstPersonArmBufferSource()` | `[1.20.1, 1.21.10)` | 1.21.10 起线程变量里是 `SubmitNodeCollector` |
| 即时源构造参数 | 1.20.1 用 `BufferBuilder`；`[1.21.1, 1.21.10)` 用 `ByteBufferBuilder` | 1.21.1 已改成 `ByteBufferBuilder` 版 `MultiBufferSource.immediate` |

`getModelBufferSource()` 里的 `return Minecraft...` 保留在 `// [1.21.10, )` 标记下：它既是 1.21.10+ 的实现，也是低版本“非模板窗口且没有事件源”时的回退，Merge 时不要一起注释掉。

## 为什么 1.21.10 不需要应用

- 1.21.10 的 `NeoRenderHandEvent.getMultiBufferSource()` 返回 `null`，只有 `SubmitNodeCollector`，事件源分支无法成立。
- 1.21.10 的模板测试由 `NeoStencilOperator` 注入 `RenderPipeline`（`RenderSystemMixin` 拦截 `applyPipelineModifiers`），`RenderType.draw` 时才应用；而 Iris 1.9.7 已删除 `batchedentityrendering`，`RenderBuffers.bufferSource()` 不再是全缓冲源，`endBatch` 就是即时提交。因此共享源本身就能正确携带模板状态，不需要自有即时源。
- 另外，1.21.10 的模型渲染仍是即时 `MultiBufferSource`，即使想改用 collector 也要先做 26.2 式迁移，且 `bakePipelineState` 在 26.2 之前是空实现，会重新丢模板——不属于本改动。

所以 1.21.10 保持现状；下次 Merge 时按上面的范围把 `[1.20.1, 1.21.10)` 块注释掉，并把 `getFirstPersonArmBufferSource` 里的 `// return null;` 放开即可。

## 26.2 / 26.3

无需改动：第一人称几何全部经事件提供的 `SubmitNodeCollector` 提交，模板/颜色状态在提交时烘焙进 `RenderType`，`getModelBufferSource()` 已无调用者。你测到“不装 Compat Shader 不 OOM”与这个结构一致。

## 备注

- 同时把 `getModelBufferSource()` 上方 Javadoc 里“模板测试关闭时保持共享源”改成了“优先使用事件传入的源，没有才回退共享源”，避免与新实现矛盾。
- 1.21.6 的 `ItemInHandLayerMixin:62` 无关编译错误仍在（`itemStack` 未定义），未处理。

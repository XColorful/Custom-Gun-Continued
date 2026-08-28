# 

```
## 二、已有架构文档

之前已经专门整理过 CGC 和 TaCZ 的渲染体系文档，可以优先利用这些文档快速建立上下文、定位相关模块，从而减少无意义的代码搜索和 token 消耗。

### CGC 渲染体系

`/docs/architecture/client/render/*.md`

CGC 总体架构：

`/docs/architecture/Home.md`

这些文档主要用于：

* 快速了解相关架构；
* 定位可能涉及的模块；
* 理解模块之间的关系；
* 减少从整个项目重新搜索的成本；
* 帮助确定下一步应该调查哪些代码。

但文档不能作为最终事实依据。

如果文档描述与当前源码不一致，以实际源码为准。

如果现有文档不足以解释问题，需要继续深入代码调查，不要局限于文档已经列出的类。

## 三、源码调查工具

可以使用 IDEA 提供的 MCP：

* 获取类之间的引用关系；
* 获取调用关系；
* 查找方法的调用方和被调用方；
* 根据需要调查 CGC 源码。

如果问题涉及某个行为，需要继续追踪这个行为的完整调用链、状态来源和相关渲染模块。

## 四、本次需要解决的问题

下面的问题描述是本次任务的具体输入。

可以同时存在多个问题。

问题之间可能存在共同根因，也可能完全独立。

不要预设它们一定需要分别修复。

### 问题

目前已经完成1.21.11 PR #36的移植，现在需要完成26.1.x的移植
- 对所有编译不通过的，寻找26.1.x的等价写法并修改

当前IDEA窗口有CGC 1.21.11、26.1.x、26.2，供查阅1.21.11已经完成的写法，以及按需查询某段代码在26.2是否变化或者MC 26.2源码

完成修改并过编译后向我报告，我测试是否正常渲染

## 七、多个问题之间的处理方式

如果本次存在多个问题，可以自行决定调查和解决顺序。

优先考虑：

1. 能够通过源码直接确认根因的问题；
2. 可能影响其他问题的底层问题；
3. 修改成本低且可以快速验证的问题；
4. 需要运行时日志才能确认的问题。

如果发现多个现象实际上由同一个移植问题导致，应优先解决共同根因，而不是针对每个现象分别打补丁。

如果某个问题的调查结果能够解释其他问题，应重新检查相关问题，而不是重复调查。

## 八、修改策略

如果已经能够合理确认问题原因，**优先直接修改 CGC 代码，让我运行测试。**

不要因为无法在当前环境中进入游戏，就停留在理论分析阶段。

对于能够通过源码确认的问题，应直接完成代码修改，然后让我进行实际验证。

修改要求：

* 只修改与当前问题相关的部分；
* 不要顺手重构无关代码；
* 不要修改无关的注释或 Javadoc；
* 不要为了“代码更漂亮”而扩大修改范围；
* 修改后检查相关调用链，确认不会明显破坏其他渲染场景。

## 九、运行时日志与测试策略

如果仅通过源码调查无法可靠确认问题原因，允许添加针对性的运行时日志，并让我进行实际测试。

但是，**日志不是默认步骤，也不是为了获取更多信息而随意添加。**

### 9.1 优先直接完成修改

如果已经能够通过源码确认或高度确定问题原因：

1. 直接完成 CGC 修改；
2. 不要为了“保险”额外添加日志；
3. 告诉我如何运行和测试；
4. 让我先进行一次实际验证；
5. 根据测试结果决定是否需要进一步调查。

这样可以避免在实际上已经可以解决的问题上浪费测试和 token。

### 9.2 无法确认原因时再添加日志

只有在以下情况才应添加日志：

* 存在多个合理的候选原因；
* 静态代码无法确定运行时实际走了哪条路径；
* 无法确定某个状态在运行时的实际值；
* 无法确定某个状态的切换时机；
* 无法确认某个变换实际执行了几次；
* 无法确认 tick / render / partial tick 之间的实际关系；
* 必须通过运行时行为才能区分候选原因。

日志应该针对**具体的未知信息**设计。

不要为了排查一个问题，在大量相关类和方法中泛滥添加日志。

### 9.3 日志必须能够区分候选原因

添加日志之前，应先明确：

* 当前有哪些可能原因；
* 哪一个运行时信息能够区分这些原因；
* 应该在哪里记录这个信息；
* 拿到日志后如何根据不同结果继续处理。

如果无法说明“这个日志拿回来之后能够帮助判断什么”，就不要添加这个日志。

日志应尽量记录：

* 状态变化；
* 状态切换前后值；
* 关键调用阶段；
* 关键时间参数；
* partial tick；
* 动画时间；
* 变换参数；
* 当前渲染场景；
* 当前物品 / 枪械；
* 关键方法是否执行。

只记录与当前问题直接相关的信息。

### 9.4 控制日志量

特别注意渲染相关代码通常处于高频调用路径。

不要直接在每一帧 render 方法中无限输出日志。

优先：

* 只在状态发生变化时输出；
* 只记录进入 / 离开关键状态；
* 对高频值进行采样或限制输出频率；
* 只记录能够区分问题的关键字段；
* 避免输出完整对象；
* 避免输出大量重复的相同日志。

如果确实需要记录连续变化过程，也应该控制输出量。

目标是让日志能够帮助定位问题，而不是制造新的 token 消耗。

### 9.5 给出明确的测试步骤

如果添加了日志，必须同时告诉我如何测试。

测试说明至少需要包含：

1. 启动什么环境；
2. 使用什么操作触发问题；
3. 从什么初始状态开始；
4. 操作执行几次；
5. 每次操作之间是否需要等待；
6. 需要观察什么现象；
7. 需要收集哪些日志；
8. 哪些日志不需要提供。

测试步骤应尽可能短。

优先设计能够一次区分多个候选原因的测试。

不要让我进行大量没有明确目的的重复操作。

### 9.6 日志返回后的处理

当我按照要求提供日志后：

1. 先根据日志确认实际运行时状态；
2. 将日志中的状态与 CGC 源码对应起来；
3. 缩小问题范围；
4. 如果已经能够确认原因，直接修改 CGC；
5. 如果现有日志不足以确认原因，只增加真正缺失的信息；
6. 不要继续要求我提供无法产生额外信息的日志。

如果已经能够确认原因，不要为了获得“更完整”的日志继续让我测试。

## 十、代码修改后的测试

如果已经完成了一个合理的代码修改，即使还存在一定的不确定性，也应优先让我运行一次。

不要在本地无法实际运行游戏的情况下，通过不断阅读代码试图达到绝对确定。

如果修改具备：

* 明确的 CGC 移植差异；
* 合理的根因解释；

则可以先完成修改，让我实际验证。

如果测试成功，可以继续检查是否存在明显的关联问题。

如果测试失败，再基于新的实际现象继续调查。

## 十一、需要回答的核心问题

调查完成后，需要明确回答：
1. CGC 当前是如何实现的？
2. 应该修改哪个位置？
3. 是否存在其他相关代码路径也需要同步考虑？

如果当前无法确认原因，不要强行给出确定结论。

应明确区分：

* 已通过源码确认；
* 根据代码高度推测；
* 必须通过运行时测试确认。

## 十二、验证要求

修改完成后，需要尽可能进行验证。

优先：

* 检查编译错误；
* 检查修改代码的调用关系；
* 检查相关状态和渲染链路；
* 对比修改前后的行为逻辑；
* 如果项目提供了适合的测试，则执行相关测试；
* 如果需要游戏内验证，直接让我进行实际测试。

如果当前环境无法实际进入游戏验证，不要声称已经完成游戏内验证。

如果静态代码已经能够确认修改合理，可以直接完成修改，并告诉我需要如何进行游戏内验证。

如果必须依赖运行时状态才能确认，则按照运行时日志与测试策略进行最小化测试。

## 十三、输出要求

完成调查和修改后，向我汇报：

### 1. 问题原因

简明说明问题真正产生的原因。

### 3. CGC 移植情况

说明 CGC 当前实现与 TaCZ 的对应关系，以及具体存在什么差异。

### 4. 修改内容

说明修改了哪些相关部分，以及修改后的逻辑。

### 5. 验证情况

说明进行了哪些验证。

如果需要我运行测试或提供日志，明确给出下一步操作。

不要输出大量源码。

重点描述机制、调用链、差异、修改原因和验证方法。

## 十四、Git 要求

完成修改和验证后：

**不要 commit。**

不要创建 commit，不要修改已有 commit，不要执行任何形式的提交操作。

保持修改后的工作区状态，最后通知我检查。

## 十五、执行原则

不要仅凭问题描述猜测原因。

不要只搜索与问题描述完全相同的类名或方法名。

应该：

> 先从现象确定涉及的渲染阶段 → 阅读相关架构文档 → 调查 CGC 对应调用链 → 对比状态、时机、变换和调用关系 → 确认根因 → 优先完成代码修改 → 让我进行实际验证 → 必要时增加最小化日志 → 根据测试结果继续修改 → 验证 → 汇报。

如果问题能够通过源码调查和对比解决，优先完成代码修改，不要为了获取运行时信息而让我进行没有必要的测试。

如果确实无法通过静态分析确定原因，允许添加日志，但必须有明确的调查目的，并给出最小化、能够区分候选原因的测试步骤。

如果已经完成了能够合理验证的修改，不要继续无意义地扩大调查范围。

现在开始调查并解决上面的问题。
```

# 

I'll start by building the 26.1.x project to get the list of compilation errors, and simultaneously look at the architecture docs for context.

The build timed out. Let me investigate the git state and understand what needs porting, while also trying to detect compilation errors more directly.

Java 25 is available. Let me kick off a compile in the background to get the ground-truth list of compilation errors.

Only one compilation error so far (in `:core:compileJava`). Let me look at that file and find the 26.1.x equivalent.

I now have a complete understanding of the 26.1.2 render pipeline API change. Let me apply the fix to `PipelineModifier.java`.

The core module now compiles. The remaining errors are in `neoforge-compat`. Let me read the 4 affected files.

Now let me investigate the 26.1.2 API for these files. Let me check the NeoForge stencil classes and the SpecialModelRenderer interface.

I now have a complete understanding. Let me implement the fix. First, the two new files (a display-context tracker and a mixin to capture it), then update the existing files.

Now let me update `StencilFunctionHelper.java` (StencilFunction → CompareOp) and `NeoBEWLR.java`.

Now let me recompile to check for remaining errors.

The build succeeds. Let me verify the final state of the modified files to ensure correctness.

编译已通过。下面是本次 26.1.x 移植的完整报告。

## 1. 问题原因

26.1.x（Minecraft 26.1.2 / NeoForge 26.1.2.7-beta）相比 1.21.11 发生了几处渲染 API 变更，导致 `:core` 与 `:neoforge-compat` 各有文件编译失败：

1. **渲染管线状态合并**：`com.mojang.blaze3d.platform.DepthTestFunction` 被删除，`RenderPipeline.Builder` 上的 `withColorWrite` / `withDepthWrite` / `withDepthTestFunction` 三个方法被合并成 `withColorTargetState(ColorTargetState)` 与 `withDepthStencilState(DepthStencilState)`。
2. **模板函数枚举替换**：NeoForge 的 `net.neoforged.neoforge.client.stencil.StencilFunction` 被删除，`StencilPerFaceTest` 的比较函数参数改为原版的 `com.mojang.blaze3d.platform.CompareOp`。
3. **SpecialModelRenderer 接口变化**：`submit(...)` 移除了 `ItemDisplayContext` 参数；`Unbaked` 由非泛型变为泛型 `Unbaked<T>`（`register` 相应要求 `MapCodec<? extends Unbaked<?>>`）；`getExtents` 只保留 `Consumer<Vector3fc>` 版本（`Set<Vector3f>` 版本移除）。

## 2. CGC 移植情况

CGC 的 `PipelineModifier`（`NO_COLOR_WRITE` / `NO_DEPTH_WRITE` / `NO_DEPTH_TEST`）本质是把 1.21.6 之前 `RenderSystem.colorMask/depthMask/disableDepthTest` 这套 GL 状态开关，封装成“在现有管线上叠加一个状态修改”的管线修改器。26.1.2 只是把管线里原本分散的字段（`writeColor`/`writeAlpha`/`writeDepth`/`depthTestFunction`）折叠成了两个不可变 record，映射如下：

|旧写法（1.21.11）|26.1.2 等价|
|---|---|
|`withColorWrite(false)`|`withColorTargetState(new ColorTargetState(原 blend, ColorTargetState.WRITE_NONE))`|
|`withDepthWrite(false)`|`withDepthStencilState(保留 depthTest/bias，仅 writeDepth=false)`|
|`withDepthTestFunction(NO_DEPTH_TEST)`|`withDepthStencilState(Optional.empty())`|

`NeoBEWLR` 的难点是 `ItemDisplayContext`：26.1.2 的 `submit` 不再接收它，但 CGC 的 `renderByItem` 必须区分 GUI / 第一人称 / 第三人称。我通过 mixin 在 `ItemStackRenderState.submit` 时把 `displayContext`（原版把上下文存在 `ItemStackRenderState.displayContext` 字段里）暂存到 ThreadLocal，再由 `NeoBEWLR` 取回。

## 3. 修改内容

**core（6 处）**

- [PipelineModifier.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/api/minecraft/pipeline/PipelineModifier.java)：按上表改写三个枚举项，`NO_DEPTH_WRITE` 用 `Optional.ofNullable(...).map(...)` 保留原 depthTest/bias 并兼容 null。
- [_SpecialModelRenderer.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/api/renderer/item/_SpecialModelRenderer.java)：`submit` 去掉 `ItemDisplayContext`，删除 `getExtents(Set<Vector3f>)`，与 26.1.2 原版对齐。
- [ItemDisplayContextTracker.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/api/minecraft/item/ItemDisplayContextTracker.java)（新增）：ThreadLocal 保存当前 `ItemDisplayContext`，空时回退 `NONE`。
- [ItemStackRenderStateMixin.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/ItemStackRenderStateMixin.java)（新增）：`@Shadow` 字段 `displayContext`，在 `submit` 头/尾 push/pop。
- [customgunclient.mixins.json](app://localhost/epitaxy/core/src/main/resources/customgunclient.mixins.json)：注册上述 mixin。

**neoforge-compat（2 处）**

- [StencilFunctionHelper.java](app://localhost/epitaxy/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforgeclient/minecraft/stencil/StencilFunctionHelper.java)：`StencilFunction` → `CompareOp`，并按 GL 语义 1:1 映射（`NEVER→NEVER_PASS`、`LESS→LESS_THAN`、`LEQUAL→LESS_THAN_OR_EQUAL`、`GEQUAL→GREATER_THAN_OR_EQUAL`、`ALWAYS→ALWAYS_PASS` 等）。
- [NeoBEWLR.java](app://localhost/epitaxy/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforgeclient/renderer/item/NeoBEWLR.java)：`submit` 改为 26.1.2 签名，内部用 `ItemDisplayContextTracker.current()`；删除 `getExtents(Set)`；`BewlrUnbaked` 改为 `implements SpecialModelRenderer.Unbaked<ItemStack>`，`bake` 返回 `SpecialModelRenderer<ItemStack>`（此改动同时修复了 `NeoSpecialModelRendererRegistry` 的 `MapCodec` 类型不匹配，该文件无需改动）。

## 4. 验证情况

- `./gradlew :core:compileJava :neoforge-compat:compileJava` → **BUILD SUCCESSFUL**（18s）。
- 全源码树已无 `DepthTestFunction` / `withColorWrite` / `StencilFunction` / `getExtents(Set` 等旧 API 残留。
- 仅存一条预存在的 deprecation note（`NeoBEWLR implements @Deprecated _SpecialModelRenderer`），非本次引入。
- 未 commit（按 Git 要求保持工作区状态）。

## 需要你测试确认的点

编译已过，但以下两处依赖运行时行为，请重点验证：

1. **物品渲染的 `ItemDisplayContext`（最关键）**：进入游戏后依次检查
    
    - 背包/工具栏里枪械、配件、弹药的**图标**是否为扁平图标（而非 3D 模型）；
    - **第一人称**持枪、**第三人称**看别人持枪、**掉落物**、**物品展示框**的渲染。
    
    若某个场景渲染错了（例如 GUI 里出现 3D 枪模型），说明 mixin 捕获的上下文不对，我再排查 `ItemStackRenderStateMixin` 的 `@Shadow` 字段/方法描述符。
    
2. **模板/镜片裁剪**：用带镜（ocular）的枪瞄具触发一次模板裁剪，确认 `NO_DEPTH_TEST`/`NO_COLOR_WRITE`/`NO_DEPTH_WRITE` 映射后的模板渲染仍正常（尤其 `NO_DEPTH_TEST` 现在映射为 `Optional.empty()`，语义上会连 depth write 一起关，理论上等价，但建议实测）。
    

如果启动即崩溃（mixin apply 失败），把崩溃日志里 `ItemStackRenderStateMixin` 相关的报错发我即可。

# 

```
经测试：
- 物品GUI渲染正常，提示框渲染正常
- 枪械第三人称渲染正常
- 枪械第一人称渲染正常
- 倍镜的准心只在ocular范围里显示（正常）

需要解决的问题：
- 枪械装红点后，仍然能从ocular里看到本来应该被ocular挡住的部分
- 枪械装4倍镜后，开镜后放大的倍率变得更大
- 第一人称持枪移动时，没有抵消掉原版的移动时物品左右晃动，按照目前1.20.1-1.21.11呈现的效果，移动时第一人称看枪械应该是不动的
```

# 

编译已通过，但渲染行为还有三个问题。我先从架构文档和源码定位这三个问题的相关模块。

I found the root cause of issues 2 and 3: `GunCameraHelper.State.renderItemInHand` is never set in 26.1.x (the `GameRendererMixin` injects that set it are commented out). This breaks both the FOV distinction and the bob/sway cancellation. Let me fix both.

Both issues 2 and 3 share the same root cause, which I've now fixed. Let me compile to verify.

Both issue 2 (scope magnification) and issue 3 (sway) shared the same root cause and are now fixed. Let me do a final review of the changes I made this round.

编译通过。下面是本轮调查与修改的完整报告。

## 一、问题 2（4 倍镜放大倍率变大）与问题 3（第一人称移动晃动未抵消）—— 已修复

这两个问题其实是**同一个根因**：`GunCameraHelper.State.renderItemInHand` 这个标志在 26.1.x 里从未被正确设置。

在 1.21.6–1.21.11，它由 `GameRendererMixin` 注入 `GameRenderer.getFov(camera, partialTicks, useFovSetting)`，用 `useFovSetting` 参数区分「世界 FOV」与「手部 FOV」。但 26.1.2 里 `getFov` 从 `GameRenderer` 移到了 `Camera`（`calculateFov`/`calculateHudFov`），导致原来那处注入被注释掉了，而 `renderItemInHand` 的 HEAD/RETURN 注入里赋值也一起被注释掉了。结果 `renderItemInHand` 恒为 `false`：

- **问题 2（FOV）**：`onComputeFovEvent` 永远走「世界 FOV」分支，把倍镜缩放套到了手部模型 FOV 上，4 倍镜看起来放大过头。
- **问题 3（晃动）**：`bobView`/`bobHurt` 永远发 `LevelBobEvent` 而不是 `ItemInHandBobEvent`，导致 `GunRendererAddon.onItemInHandBobView` 取消原版 view-bob 的逻辑不触发。

修改（2 处）：

1. [GunCameraHelper.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/gun/GunCameraHelper.java:162)：`onComputeFovEvent` 改用 `event.useConfiguredFov()` 区分——26.1.2 里 `ViewportEvent.ComputeFov.usedConfiguredFov()` 正好就是原来的 `useFovSetting`（世界 FOV=true、手部 FOV=false）。
2. [GameRendererMixin.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/GameRendererMixin.java:101)：重新启用 `renderItemInHand` 的 HEAD/RETURN 注入里的 `renderItemInHand = true/false` 赋值（供 bob 区分）。

## 二、问题 1（红点 ocular 遮挡）—— 未定位到根因

我先做了大量排除，确认**不是我这几次移植改动造成的**：

- `StencilFunction → CompareOp` 映射是 1:1 正确的（逐项核对 `GlConst.toGl`：`GL_GREATER→GREATER_THAN`、`GL_EQUAL→EQUAL`、`GL_GEQUAL→GREATER_THAN_OR_EQUAL` 等）。
- `PipelineModifier` 三处映射语义正确（核对 `GlCommandEncoder.applyPipelineState`：`Optional.empty()` → `_disableDepthTest()+_depthMask(false)`；与旧版 `NO_DEPTH_TEST` 的 `_disableDepthTest()+_depthMask(true)` 差异在「深度测试关闭时本就不会写深度」，属于 no-op）。
- 模板渲染相关文件（`_AttachmentModelRender`、`_GunModelRender`、`NeoStencilOperator`、`StencilState`）与 1.21.11 **完全一致**，且你反馈「倍镜准心只在 ocular 范围里显示」正常，说明模板注入链路是通的。

一个值得注意的关联：红点走的是 `renderSight → renderDivisionOnly` 这条路径，而它是**唯一**调用 `_disableDepthTest()`（即 `NO_DEPTH_TEST → Optional.empty()`）的地方；4 倍镜走 `renderScope/renderBoth` 不经过它。我已从源码层面确认 `NO_DEPTH_TEST` 的差异是 no-op，但这条唯一性关联很可疑，可能是 26.1.2 在「深度纹理/stencil 附着」上有更细的运行时差异（例如 `clearStencilTexture` 在 1.21.11 会 `glDrawBuffer(GL_NONE)` 并在结束后 `glDrawBuffer(GL_COLOR_ATTACHMENT0)`+重绑 FBO，26.1.2 去掉了这些，但作用的是颜色缓冲，理论上不影响遮挡）。

## 三、验证情况

- `./gradlew :core:compileJava :neoforge-compat:compileJava` → **BUILD SUCCESSFUL**。
- 未 commit，工作区保持修改状态。

## 四、需要你测试/反馈

1. 先测**问题 2、3**：装 4 倍镜开镜看倍率是否恢复、第一人称移动时枪是否不再跟着原版左右晃。
2. **问题 1** 我需要两点信息来继续定位：
    - 红点这个配件在资源包的 `display` 里是 `enable_scope` 还是 `enable_sight`（还是两者都 true）？这决定它走 `renderSight` 还是 `renderBoth`。
    - 「被 ocular 挡住的部分」具体是**镜身金属外壳**没挡住枪身，还是**镜片内**能看到不该有的枪身部分？最好能截个图或描述一下方向（比如是透过镜片看到枪身、还是镜身外圈处露枪身）。

如果问题 2、3 测完确认已修复而问题 1 仍在，我会针对红点那条 `renderSight` 路径加最小化日志（只记录 stencil/depth 状态切换）来定位。

# 

```
之前你修改完的状态下，仅剩问题1没解决，问题2、3解决了

我刚才调整了跨版本的写法，但是现在改完后出现了问题，目前的异常是：
- 开镜后fov没变，只是视角枪械配件ocular往屏幕中间移动到开镜状态，并且离ocular更近
- 红点瞄准镜是enable_sight=true,enable_scope=false，问题还在

你检查下当前状态以及，git暂存区里的写法，跟刚才你改完时的差异
- 对于不影响因为我手贱改错的部分，如一些我刻意保留的注释，仍然保留
- 由于上下文长度限制，本次对话不研究红点瞄准镜的问题，把状态修复完我验证完就结束
```

# 

I found the bug. The user refactored my FOV fix into a `_isLevelRenderFov` method but inverted the logic. Let me verify the compile state first, then fix.

The compile passes — the user's changes are syntactically valid. The only bug is the inverted `_isLevelRenderFov` logic. Let me fix it.

状态已修复。问题根因与改动如下。

## 根因

你把手动改 FOV 时引入了一个**逻辑取反错误**。

你把我的 `onComputeFovEvent` 里的判断重构成了 `_isLevelRenderFov(event)` 方法，但方法体写成了：

```java
result = Boolean.FALSE.equals(event.useConfiguredFov());
```

`useConfiguredFov()` 在 26.1.2 里 **true = 世界 FOV、false = 手部 FOV**（来自 `Camera.calculateFov`/`calculateHudFov` 的 `useFovSetting` 参数）。而方法名叫 `_isLevelRenderFov`（判断"是否是世界渲染 FOV"），所以 `Boolean.FALSE.equals(...)` 正好把世界/手部判断反了：

- 世界 FOV 事件被当成手部 → 走 `_applyGunModelFovModifying`，倍镜缩放没套到世界视角 → "开镜后 fov 没变"
- 手部 FOV 事件被当成世界 → 走 `_applyScopeMagnification`，倍镜缩放到手部模型 → "ocular 往屏幕中间移动、更近"

## 修改

[GunCameraHelper.java:184](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/gun/GunCameraHelper.java:184) 只改了这一行：

```java
result = Boolean.FALSE.equals(event.useConfiguredFov());
```

→

```java
result = Boolean.TRUE.equals(event.useConfiguredFov());
```

（等价于"useConfiguredFov == true 才是世界 FOV"，与上面 `[1.20.1, 26.1)` 分支里 `!State.renderItemInHand` 的语义一致。）

## 保留了你刻意保留/改动的部分

- `ItemDisplayContextTracker` 移到 `client/api/renderer/item/`，`NeoBEWLR`、`ItemStackRenderStateMixin` 的 import 已指向新位置，未动。
- `_SpecialModelRenderer` 和 `NeoBEWLR` 里重新加回 `getExtents(Set<Vector3f>)`、`// 26.1移除`、`// @Deprecated(since = "1.21.11")` 等注释，均保留。
- `ItemStackRenderStateMixin` 工作区里已恢复成完整可用的 mixin（`@Mixin`/`@Shadow`/`@Inject` 均启用），未动。
- 你的 `_isLevelRenderFov` 方法结构、`// [1.20.1, 26.1)` / `// [26.1, 26.2)` 版本区间注释、以及 `GameRendererMixin` 里 `renderItemInHand` 的 `renderItemInHand = true/false` 赋值，都保留。

## 验证

`./gradlew :core:compileJava` → **BUILD SUCCESSFUL**。未 commit。

你可以直接测开镜 FOV 是否恢复；红点问题本次不展开。

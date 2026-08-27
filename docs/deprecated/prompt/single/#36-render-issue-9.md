# 

```
# CGC / TaCZ 渲染问题调查与修改

## 一、固定上下文

CGC 已经基本完成 TaCZ 渲染体系的移植。

当前需要针对一个或多个具体问题，调查 TaCZ 原始实现与 CGC 移植实现之间的关系，判断 CGC 是否完整、正确地移植了 TaCZ 对应机制，并在确认问题原因后直接修改 CGC 代码。

本次任务涉及两个代码体系：

* TaCZ：原始模组，用于确认原始设计和实现。
* CGC：TaCZ 渲染体系的移植目标，也是最终需要修改的代码。

TaCZ 类迁移到 CGC 后的位置、类名变化以及对应关系，可以通过：

`/docs-tacz/TaCZ Migration Mapping.md`

进行定位。

不要因为 CGC 中已经存在对应实现，就默认其行为与 TaCZ 一致。

需要分别调查 TaCZ 原始源码和 CGC 当前源码，再对两者进行对比。

## 二、已有架构文档

之前已经专门整理过 CGC 和 TaCZ 的渲染体系文档，可以优先利用这些文档快速建立上下文、定位相关模块，从而减少无意义的代码搜索和 token 消耗。

### CGC 渲染体系

`/docs/architecture/client/render/*.md`

CGC 总体架构：

`/docs/architecture/Home.md`

### TaCZ 渲染体系

`/docs-tacz/architecture/client/render/*.md`

TaCZ 总体架构：

`/docs-tacz/architecture/Home.md`

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
* 读取 TaCZ 源码；
* 根据需要调查 CGC 源码。

不需要重新从 GitHub 拉取 TaCZ 源码。

调查 TaCZ 类时，优先通过：

`/docs-tacz/TaCZ Migration Mapping.md`

从 CGC 对应实现反推出 TaCZ 原始类的位置，再继续沿 TaCZ 的调用关系调查。

不要只调查迁移映射中直接对应的类。

如果问题涉及某个行为，需要继续追踪这个行为的完整调用链、状态来源和相关渲染模块。

## 四、本次需要解决的问题

下面的问题描述是本次任务的具体输入。

可以同时存在多个问题。

问题之间可能存在共同根因，也可能完全独立。

不要预设它们一定需要分别修复。

### 问题

本次接着./docs/deprecated/prompt/single/#36-render-issue-8.md完成1.21.4的移植后，现在处理1.21.6的移植

当前git暂存区有NeoBEWLR对getExtents接口的实现，这个是必须实现的，我尚不清楚是干嘛用的，你需要研究一下是否需要往里面添加内容

当前测试如下：
- 物品GUI渲染正常
- 第三人称枪械渲染正常
- 物品提示框渲染正常
- 红点瞄准镜、筒镜的ocular都是黑的，并且4倍镜、8倍镜放大倍数变得很大，开镜视角下ocular在屏幕中占据的面积更大了
- 区别于1.21.4修复前的状况，当前没开镜的时候是没看见准心的，我估计又是模板测试画歪了的问题

当前打开了tacz、CGC1.21.4、CGC1.21.6、CGC26.2的源码，你可以浏览1.21.4跟1.21.6的差异（应该没什么差异，有可能有些地方1.21.6当时为了过编译而注释掉了）
- tacz的源码没有1.21.4，也没有1.21.6，参考价值有限
- 26.2的源码仅用于你需要知道某段代码实现在高版本是否变了

## 五、调查目标

针对上面的问题，需要分别调查 TaCZ 和 CGC。

### 5.1 调查 TaCZ 原始实现

首先确定 TaCZ 原本是如何实现这个行为的。

需要调查：

* 问题涉及的入口；
* 相关调用链；
* 状态来源；
* 状态如何变化；
* 相关渲染阶段；
* 相关变换；
* 第一人称 / 第三人称等场景差异；
* 与动画状态机、渲染器、玩家状态或其他模块的关系；
* 最终导致目标行为的关键机制。

不要只找到“负责这个功能的类”就停止。

需要继续追踪到足以解释**为什么 TaCZ 能够得到正确行为**的程度。

如果 TaCZ 中没有直接对应的单一实现，而是多个模块共同产生该行为，需要说明这些模块如何共同工作。

### 5.2 调查 CGC 移植实现

然后调查 CGC 当前对应实现。

通过：

`/docs-tacz/TaCZ Migration Mapping.md`

以及 CGC 渲染架构文档定位对应代码。

需要确认：

* CGC 当前使用哪些类实现这个行为；
* 调用链是否与 TaCZ 对应；
* 状态来源是否一致；
* 状态更新时机是否一致；
* 渲染阶段是否一致；
* 变换顺序是否一致；
* 第一人称 / 第三人称处理是否一致；
* 是否存在遗漏的 TaCZ 逻辑；
* 是否存在 CGC 特有的逻辑改变了原始行为；
* 是否存在移植过程中类职责、调用关系或状态传递发生变化的情况。

## 六、重点：不要只寻找表面上的代码差异

不要简单地进行：

> TaCZ 有 A → CGC 没有 A → 所以补 A。

需要判断差异是否真的会影响当前问题。

尤其要注意：

* 调用时机；
* 状态生命周期；
* 状态初始化；
* 状态清理；
* 状态切换；
* tick 与 render 的执行顺序；
* partial tick；
* 插值；
* 变换叠加顺序；
* 坐标系；
* 第一人称与第三人称的不同处理；
* 渲染状态与玩家状态之间的同步；
* 动画状态机的状态；
* Mixin 注入位置；
* 事件触发时机；
* renderer 内部状态；
* Minecraft 原版渲染流程对行为的影响。

如果当前问题表现为“快速移动、颠倒、缩放、抖动、减速、插值异常”等现象，需要重点调查是否存在：

* 两次或多次应用同一变换；
* 变换顺序错误；
* 初始值异常；
* 插值起点或终点异常；
* render tick / game tick 状态不同步；
* 状态在切换时没有正确重置；
* 第一帧使用了错误状态；
* 渲染状态残留；
* TaCZ 中存在但 CGC 未移植的过渡状态；
* CGC 中额外执行了 TaCZ 不存在的变换。

不要根据现象直接猜原因，必须通过源码调用链验证。

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

在完成 TaCZ 与 CGC 的对比后，如果已经能够合理确认问题原因，**优先直接修改 CGC 代码，让我运行测试。**

不要因为无法在当前环境中进入游戏，就停留在理论分析阶段。

对于能够通过源码确认的问题，应直接完成代码修改，然后让我进行实际验证。

修改要求：

* 修改应以恢复 TaCZ 原本行为为主要目标；
* 只修改与当前问题相关的部分；
* 不要顺手重构无关代码；
* 不要修改无关的注释或 Javadoc；
* 不要为了“代码更漂亮”而扩大修改范围；
* 如果存在多个可行方案，优先选择与 TaCZ 原始实现语义和执行时机最接近的方案；
* 如果 TaCZ 本身存在特殊的历史实现细节，需要判断其是否是当前行为所必需，而不是机械复制代码；
* 修改后检查相关调用链，确认不会明显破坏其他渲染场景。

## 九、运行时日志与测试策略

如果仅通过源码调查无法可靠确认问题原因，允许添加针对性的运行时日志，并让我进行实际测试。

但是，**日志不是默认步骤，也不是为了获取更多信息而随意添加。**

应优先通过 TaCZ / CGC 源码对比解决问题。

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
2. 将日志中的状态与 TaCZ / CGC 源码对应起来；
3. 缩小问题范围；
4. 如果已经能够确认原因，直接修改 CGC；
5. 如果现有日志不足以确认原因，只增加真正缺失的信息；
6. 不要继续要求我提供无法产生额外信息的日志。

如果已经能够确认原因，不要为了获得“更完整”的日志继续让我测试。

## 十、代码修改后的测试

如果已经完成了一个合理的代码修改，即使还存在一定的不确定性，也应优先让我运行一次。

不要在本地无法实际运行游戏的情况下，通过不断阅读代码试图达到绝对确定。

如果修改具备：

* 明确的 TaCZ 对应实现；
* 明确的 CGC 移植差异；
* 合理的根因解释；

则可以先完成修改，让我实际验证。

然后根据我的测试结果继续处理。

如果测试成功，可以继续检查是否存在明显的关联问题。

如果测试失败，再基于新的实际现象继续调查。

## 十一、需要回答的核心问题

调查完成后，需要明确回答：

1. 这个问题在 TaCZ 原始实现中是如何工作的？
2. TaCZ 中哪些模块共同决定了这个行为？
3. CGC 当前是如何实现的？
4. CGC 与 TaCZ 的对应关系是什么？
5. 两者具体在哪里产生了行为差异？
6. 这个差异是否就是当前问题的原因？
7. 如果不是，真正的原因是什么？
8. 应该修改哪个位置？
9. 修改后为什么能够恢复 TaCZ 原本的行为？
10. 是否存在其他相关代码路径也需要同步考虑？

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

### 2. TaCZ 原始实现

说明 TaCZ 原本如何实现，以及关键调用链和状态关系。

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

不要在没有调查 TaCZ 原始实现之前直接修改 CGC。

不要仅凭问题描述猜测原因。

不要只搜索与问题描述完全相同的类名或方法名。

应该：

> 先从现象确定涉及的渲染阶段 → 阅读相关架构文档 → 通过迁移映射定位 TaCZ → 调查 TaCZ 原始调用链 → 调查 CGC 对应调用链 → 对比状态、时机、变换和调用关系 → 确认根因 → 优先完成代码修改 → 让我进行实际验证 → 必要时增加最小化日志 → 根据测试结果继续修改 → 验证 → 汇报。

如果问题能够通过源码调查和对比解决，优先完成代码修改，不要为了获取运行时信息而让我进行没有必要的测试。

如果确实无法通过静态分析确定原因，允许添加日志，但必须有明确的调查目的，并给出最小化、能够区分候选原因的测试步骤。

如果已经完成了能够合理验证的修改，不要继续无意义地扩大调查范围。

现在开始调查并解决上面的问题。
```

# 

I'll start by establishing context: reading the previous issue's resolution, the migration mapping, and the current staged `NeoBEWLR` file.

I have strong context from the previous issue. Now let me investigate the current 1.21.6 situation. Let me check the actual git state and read the key render architecture docs.

I now have a complete understanding of the root cause. Let me verify one remaining detail — the mod ID and the `CustomGun` constant — before writing the fix.

Now I'll implement the fix. Starting with the imports in `ClientRenderHelper.java`:

Now let me verify the changes compile. Let me run the core compile first:

Both modules compile. Let me do a final review of the key changes to verify logic correctness:

Let me verify there are no remaining raw immediate-mode stencil calls elsewhere in the codebase:

Let me verify that `PipelineModifiers.init()` (which fires `RegisterPipelineModifiersEvent`) is actually invoked during startup:

All changes compile and the fix is complete. Let me provide a final review of the modified `_GunModelRender.java` to confirm coherence:

## 调查与修改完成

### 1. 问题原因

1.21.6 起，Minecraft/Blaze3D 把渲染从「即时模式 GL 状态」切换成了「渲染管线 / RenderPass 模型」。模板测试、颜色/深度写入、深度测试这三类状态不再由 `GlStateManager`（即时调用 `glStencilFunc`/`glColorMask` 等）控制，而是：

- **模板测试** → `RenderSystem.enableStencil(StencilTest)`（neoforge 注入的静态字段 `STENCIL_TEST`），在 `RenderType#draw` 时读入 RenderPass；
- **颜色/深度写入、深度测试** → 烘焙在 `RenderPipeline` 里（`writeColor`/`writeDepth`/`depthTestFunction`），由 `GlCommandEncoder.applyPipelineState` 每次绘制时强制套用。

而 CGC 的瞄具模板渲染（`_AttachmentModelRender` / `_GunModelRender`）此前为过编译，只是把 `RenderSystem.stencilFunc` 改成了 `GlStateManager._stencilFunc` 等**即时模式等价物**。这些即时状态在每次 `renderType.draw()` 时被管线状态覆盖：

- `GlCommandEncoder` 在 `renderPass.stencilTest == null`（即 `STENCIL_TEST` 未设置）时直接 `GlStateManager._disableStencilTest()`，于是模板写入/测试全部失效；
- `applyPipelineState` 每次都把 `colorMask`/`depthMask` 重置回管线的 `writeColor`/`writeDepth`。

结果：目镜模板遮罩没打孔、黑镜片没有按圆形裁切、准心没有被限制在镜片内 —— 也就是你看到的「ocular 全黑、开镜时镜片占屏变大、准心异常」。红点/筒镜/组合镜共用同一套模板链路，所以一起坏。

### 2. TaCZ 原始实现

TaCZ（1.20.1–1.21.1）用的是即时模式：`RenderSystem.stencilFunc/stencilOp/stencilMask` + `glEnable(GL_STENCIL_TEST)`，在 `_AttachmentModelRender.renderScope/renderSight/renderBoth` 里按「写目镜模板 → 用 `GL_INVERT` 在圆形视野处打孔 → 镜身只在 `stencil==0` 处画 → 黑遮罩只在镜片环上画 → 准心只在孔内画」的顺序驱动。这套逻辑（模板值 `i+1`、`~(i+1)`、`GL_GREATER 127` 等）在 CGC 里**原样保留且正确**，本次不需要改动逻辑，只需要把「即时模式状态」翻译成 1.21.6 的管线 API。

### 3. CGC 移植情况

CGC 的模板逻辑与 TaCZ 一致，唯一问题是**状态下发方式**没跟上 1.21.6 的管线模型。`_AttachmentModelRender._clearStencilBuffer` 已经改用了 `RenderSystem.getDevice().createCommandEncoder().clearStencilTexture(...)`（正确），但模板 func/op/mask 与 color/depth 写入仍走即时模式。

### 4. 修改内容

改了三处（外加 `getExtents` 结论），均未 commit：

**① [ClientRenderHelper.java](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java)**（核心改动）
- `GL._stencilFunc/_stencilOp/_stencilMask` 改为累积模板状态，统一经 `RenderSystem.enableStencil(new StencilTest(...))` 下发（`StencilTest` 把 func/op/mask 合并成一个 record，正对应即时模式的三个分离调用）；
- `GL._colorMask/_depthMask/_disableDepthTest/_enableDepthTest` 改为通过 neoforge 的 `PipelineModifier`（`pushPipelineModifier`/`popPipelineModifier`）动态改写管线，恢复「只写模板」「关闭深度测试」等能力；
- `enableItemEntityStencilTest`/`disableItemEntityStencilTest` 改为启用/清除 `STENCIL_TEST`（`RenderSystem.enableStencil`/`disableStencil`），并新增了三个 `PipelineModifier` 常量与实现。

**② [_GunModelRender.java](core/src/main/java/dev/xcolorful/customgun/client/model/_GunModelRender.java)**
- `_clearStencilBuffer()` 原来用旧的 `_stencilMask + glClearStencil + _clear(GL_STENCIL_BUFFER_BIT)`（1.21.6 下既无效、又会因新的 `_stencilMask` 副作用泄漏 `STENCIL_TEST`），改成与 `_AttachmentModelRender` 一致的设备清空 API。

**③ [NeoClientSetup.java](neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforgeclient/init/NeoClientSetup.java)**
- 新增 `RegisterPipelineModifiersEvent` 处理器，注册上面三个 `PipelineModifier`。

**④ `getExtents`（不需要加内容）**
- 它的作用是给物品模型提供包围盒：`ItemEntityRenderer`（掉落物悬浮高度/平铺）和 `GuiItemRenderState`（仅当 `items/*.json` 里 `oversized_in_gui=true` 时才用）。CGC 的 GUI 渲染的是**扁平槽位贴图**（不走 3D 模型缩放），且没开 `oversized_in_gui`；掉落物走 `renderByItem` 自定义渲染，包围盒只影响轻微悬浮高度、不影响绘制。所以留空是正确且无害的——暂存区里的 `getExtents` 空实现保持现状即可。

### 5. 验证情况

- `./gradlew :core:compileJava --offline` → BUILD SUCCESSFUL。
- `./gradlew :neoforge-compat:compileJava --offline` → BUILD SUCCESSFUL。
- 未进入游戏内验证（本环境无法运行游戏）。

### 需要你测试

进游戏后重点看第一人称开镜：

1. 长筒镜（`enableScope=true`）开镜：镜片是否出现**圆形放大画面**（不再全黑），黑色镜框只留在镜片圆外，准心只在圆内。
2. 红点瞄准镜（`enableSight=true`）：红点是否只在 ocular 范围内可见。
3. 组合镜（scope+sight）同理。
4. 「4 倍/8 倍放大过大、镜片占屏过大」是否随模板修复而恢复正常（我判断这是黑镜片没打孔、占满屏幕的视觉后果，而非独立 FOV 问题——`GunCameraHelper` 在 1.21.4 与 1.21.6 无差异）。若修完模板后仍觉得倍率异常，再告诉我，我单独排查 FOV 链路。

### 需要你留意的一个架构决策

本次在 core 层的 `ClientRenderHelper` 直接引入了 neoforge 平台 API：`net.neoforged.neoforge.client.stencil.*` 与 `net.neoforged.neoforge.client.pipeline.PipelineModifier`。原因是 1.21.6 的模板支持是 **neoforge 独有**（原版没有等价的 `StencilTest`/`PipelineModifier`），且 `settings.gradle` 里 `core` 已 `implementation "net.neoforged:neoforge"`、本分支为 neoforge-only。按 CLAUDE.md 约定我上报此平台 API 使用：如果你希望保持 core 完全平台无关，我可以后续把它抽成「core 定义接口 + neoforge-compat 提供实现并回注」的桥接，但当前为最小改动直接放进了这个本就是「跨版本兼容包装层」的 `ClientRenderHelper` 里。

工作区保持未提交状态，等你检查。

# 

```
红点的ocular正常了
筒镜的有问题：
- 放大倍数仍然大于实际倍数

4倍镜/8倍镜开镜后ocular还是黑的，没被遮挡的地方形状描述如下：
- 在屏幕下半块，有一个上边高的等腰梯形
- 等腰梯形中间有根竖线，估计是4倍镜/8倍镜准心的渲染（瞄准线）
- 等腰梯形里有很多右上-左下方向的黑色线条，这些部分是挡住的
- 被挡住的这部分和等腰梯形以外的部分，看起来就像ocular模型本身的黑色
- 在以上ocular黑色以外的一些范围，能看到镜子后面的画面

你继续修改，如果封装会导致不方便的话，直到成功修复前都可以不考虑把neoforge import封装掉，但不要commit
```

# 

The red dot works now, which means my stencil translation is correct. The scope has a different failure. Let me investigate the circular fan (the `GL_INVERT` hole-carving) which is scope-specific. The key difference: it uses `renderType.draw()` directly with a `POSITION_COLOR` mesh.

The red dot works, so my stencil translation is correct. The scope's remaining failure is the circular fan (the `GL_INVERT` hole-carving), which uses a `POSITION_COLOR` mesh but draws it via `renderType` (whose pipeline uses `POSITION_COLOR_TEX`). Let me compare the exact 1.21.4 vs 1.21.6 circular-fan code and check the coordinate/vertex handling.

The 1.21.4 code is identical except `BufferUploader.drawWithShader` (which uses the mesh's own `POSITION_COLOR` format and applies model-view × projection) vs `renderType.draw` (which forces the render type's `POSITION_COLOR_TEX` format). Let me verify how `core/position_color` shader and the debug triangle-fan pipeline handle matrices, and how the first-person PoseStack is set up at the scope render point.

`MATRICES_PROJECTION_SNIPPET` declares both `DynamicTransforms` (model-view) and `Projection`, so `debugTriangleFan()` applies the same model-view × projection as the old `BufferUploader.drawWithShader`, but with the correct `POSITION_COLOR` vertex format. The bug is confirmed: the circular fan passes a `POSITION_COLOR` mesh to `renderType` (whose pipeline is `POSITION_COLOR_TEX`). Let me fix it:

Let me verify the compile and then investigate the "magnification too large" FOV concern (which may be a separate issue from the hole).

The FOV wiring is identical between 1.21.4 and 1.21.6, so "magnification too large" is almost certainly a perception artifact of the garbage hole (which my fix now addresses). Let me verify one remaining detail: that `draw()` handles the `TRIANGLE_FAN` mode's sequential index buffer correctly.

Fix complete and compiles. Here's my report.

## 本次修改：筒镜模板孔的顶点格式错误

### 1. 问题原因

红点瞄准镜已经正常，说明上一轮的模板测试翻译（`RenderSystem.enableStencil(StencilTest)`）是**正确**的。筒镜仍然坏，是因为它多了一个红点没有的步骤——**用三角形扇（`GL_INVERT`）在镜片上打圆形孔**，而这一步在 1.21.4→1.21.6 移植时被改坏了：

1.21.4 里画这个圆形孔用的是 `BufferUploader.drawWithShader(builder.buildOrThrow())`，它会用**网格自身的顶点格式**（`POSITION_COLOR`）并套用 model-view × projection。1.21.6 里为了过编译改成了 `renderType.draw(...)`，但 `renderType` 是 `entityCutout`/`entityTranslucent`，其管线顶点格式是 `POSITION_COLOR_TEX`（带贴图坐标/光照）。于是这个 `POSITION_COLOR` 三角形扇被按错误格式解析，圆形孔画成了乱几何——这就是你看到的「下半个屏幕的等腰梯形、梯形里右上-左下方向的黑色线条」的来源。孔没打好 → 镜片黑色遮罩盖满整个镜片 → ocular 全黑；孔的位置/大小错乱 → 视野显得「放大倍数不对」。

（红点没这步，只画 division 准心，所以正常。）

### 2. 修改内容（一行）

[_AttachmentModelRender.java:334](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java) 的圆形模板层绘制，从 `renderType.draw(builder.buildOrThrow())` 改为：

```java
RenderType.debugTriangleFan().draw(builder.buildOrThrow());
```

`debugTriangleFan()` 正是 1.21.6 原版为「`POSITION_COLOR` + `TRIANGLE_FAN`」准备的 RenderType：它的管线用 `core/position_color` 着色器、`DefaultVertexFormat.POSITION_COLOR`、`TRIANGLE_FAN`，且通过 `MATRICES_PROJECTION_SNIPPET`（声明 `DynamicTransforms` 模型视图矩阵 + `Projection` 投影矩阵）套用和旧 `BufferUploader.drawWithShader` 相同的 model-view × projection。所以它等价于 1.21.4 的行为，只是顶点格式匹配了。

### 3. 关于「放大倍数仍然大于实际倍数」

我对比了 1.21.4 与 1.21.6 的 FOV 链路（`GunCameraHelper`、`NeoComputeFovEvent`、`NeoComputeFovModifierEvent` 及两个 EventManager），**全部无差异**。因此世界 FOV 缩放逻辑本身没有版本差异。我判断「放大倍数过大」是圆孔画歪后的**视觉错觉**（黑镜片盖屏、孔的位置/大小错乱），圆孔修好后应一并恢复。若你测试后仍觉得倍率偏大，我再单独排查 FOV 数值来源（`views_fov`/`zoom_model_fov` 与 `getScopeViewRadiusModifier`）。

### 4. 验证情况

- `./gradlew :core:compileJava --offline` → BUILD SUCCESSFUL。
- `./gradlew :neoforge-compat:compileJava --offline` → BUILD SUCCESSFUL。
- 未进入游戏内验证。

### 请你测试

第一人称装长筒镜（4 倍/8 倍，`enableScope=true`）右键开镜，看镜片是否出现**居中的圆形放大画面**（不再是下半个屏幕的梯形、不再全黑），黑色镜框只在圆外、准心只在圆内。红点和第三人称应保持正常。

工作区未提交，等你检查。

# 

```
现在红点、筒镜的ocular都正常渲染了，但是筒镜的倍率还是明显变大
- 例如4倍镜开镜后，直到屏幕边缘都没看见瞄准线跟边缘圆形的交界
- 8倍镜能正常切换不同缩放倍率，但是每个倍率都明显比正常要大

继续排查
```

# 

I found the root cause. The FOV classification flag `renderItemInHand` is broken in 1.21.6. Let me verify the exact signatures.

I've found the root cause of the magnification issue. In 1.21.6, the hand FOV is now computed **outside** `renderItemInHand` (line 702 calls `getFov(camera, f, false)` before `renderItemInHand` at line 706), and `renderItemInHand`'s signature changed from `(Camera, float, Matrix4f)` to `(float, boolean, Matrix4f)`. So the `renderItemInHand` mixin's HEAD/RETURN injections no longer cover the hand FOV moment — the `renderItemInHand` flag stays `false`, causing `_applyScopeMagnification` (world zoom) to also run on the hand FOV. Let me fix the flag classification.

The fix compiles. Let me verify my understanding of the full flow is correct by confirming the `getFov` HEAD injection fires before the neoforge FOV hook:

Fix compiles. Here's my report.

## 本次修改：1.21.6 手部 FOV 分类失效导致倍率异常

### 1. 问题原因

倍率异常不是世界缩放算错，而是**手部（枪模型）FOV 被错误地套用了世界倍率**，根因是区分「世界 FOV / 手部 FOV」的 `renderItemInHand` 标志在 1.21.6 里失效了。

`GunCameraHelper.onComputeFovEvent` 靠这个标志分流：

- 世界 FOV → `_applyScopeMagnification`（用 `scopeZoomScale` 做世界缩放，正确）；
- 手部 FOV → `_applyGunModelFovModifying`（用 `scopeViewFov[scopeViewIndex]` 定枪模组的目镜视野，正确）。

而 1.21.6 原版 `GameRenderer.render` 里，手部 FOV 改成了在 `renderItemInHand` **之外**先算好（`getFov(camera, f, false)` 在 `renderItemInHand(...)` 之前），同时 `renderItemInHand` 的签名从 `(Camera, float, Matrix4f)` 变成了 `(float, boolean, Matrix4f)`。于是原来挂在 `renderItemInHand` 的 HEAD/RETURN 上的 mixin（设置该标志）既对不上签名、又覆盖不到手部 FOV 计算时刻，`renderItemInHand` 一直为 `false`。结果手部 FOV 也走了 `_applyScopeMagnification`，枪模组（含瞄准线、镜片）被用世界倍率那个很窄的 FOV 投影 → 瞄准线/镜片被放得过大，直到屏幕边缘都看不到它与镜片圆形的交界，看起来就是「倍率比实际大」。

### 2. 修改内容（`GameRendererMixin.java`）

- 重新启用 `getFov` 的注入（`cgc$switchRenderType`），用 `getFov` 自带的 `useFovSetting` 参数区分 world（true）/ hand（false）：`renderItemInHand = !useFovSetting`。这是唯一能覆盖两个 FOV 计算点的位置，且 `getFov` 签名在 1.21.4 与 1.21.6 一致。同时把 `CallbackInfoReturnable<Double>` 改成 `<Float>`（`getFov` 返回 `float`）。
- 把 `renderItemInHand` 的两个注入签名从 1.21.4 的 `(Camera, float, Matrix4f)` 改成 1.21.6 的 `(float, boolean, Matrix4f)`，让它继续在抬手渲染期间正确置位/复位。

### 3. 验证情况

- `./gradlew :core:compileJava --offline` → BUILD SUCCESSFUL。
- 已核对 `getFov` HEAD 注入发生在 `ClientHooks.getFieldOfView`（触发 FOV 事件）之前，且 `render` 方法内两个 `getFov` 调用点 `useFovSetting` 分别为 true（世界，line 659）与 false（手部，line 702），分流正确。
- 未进入游戏内验证。

### 请你测试

第一人称装 4 倍/8 倍筒镜右键开镜，确认：

1. 世界缩放恢复到对应倍率（4 倍就是 4 倍），瞄准线与镜片圆形的交界能在屏幕内看到；
2. 8 倍镜切换各缩放档位时，每个档位倍率正常；
3. 红点、第三人称、GUI 保持正常（这些不经过手部 FOV 分类，应不受影响）。

工作区未提交，等你检查。

# 

```
筒镜ocular问题已经成功修复。

现在还剩一个问题：
- 子弹/枪械物品丢出后，在世界的ItemEntity渲染是看不见的，只有影子

现在调查并修复该问题
```

# 

The dropped-item invisibility is a separate issue from the scope/FOV work. Let me investigate the item entity rendering path in 1.21.6.

The dropped-item invisibility has a clear root cause. Let me confirm the current `NeoBEWLR` state before editing:

修复完成并编译通过。汇报如下。

## 本次修改：掉落物不可见（`getExtents` 空导致无限包围盒）

### 1. 问题原因

掉落物（`ItemEntity`）不可见、只有影子，根因是 `NeoBEWLR.getExtents` 返回空集合。

1.21.4+ 的物品模型系统里，`SpecialModelWrapper.update` 会用 `getExtents` 的结果作为该物品层的包围盒来源，`ItemStackRenderState.getModelBoundingBox()` 据此构建 AABB。当 `getExtents` 为空时：

- `AABB.Builder` 初值是 `min = +∞`、`max = -∞`，`build()` 里又做 `Math.min/Math.max`，最终得到**无限大 AABB**（`minY = -∞`、`maxY = +∞`）。
- `ItemEntityRenderer.render` 里 `float f = -((float)aabb.minY) + 0.0625F` 变成 `-(-∞) + 0.0625 = +∞`，随后 `poseStack.translate(0, f, 0)` 把物品平移到 Y=+∞ → 不可见。

影子是 `EntityRenderer` 独立渲染的，不依赖这个包围盒，所以影子还在。这解释了「只有影子」。

（我上一轮对 `getExtents` 的判断「留空没问题」是错的——它只对 GUI 无影响，但对掉落物是致命的。）

### 2. 修改内容（`NeoBEWLR.java`）

给 `getExtents` 填充一个 `1×1×1` 格的近似包围盒（item 单位，与 `getExtentsForGui` 里 `/16` 的约定一致）的 8 个角点。这样 `getModelBoundingBox()` 得到有限 AABB，掉落物按正常悬浮高度定位；同时 `getZsize()=1 > 0.0625` 保证它走「3D 物品」分支而非「平铺」分支，枪械/子弹都正确。

### 3. 为什么用固定立方体而不是算真实模型包围盒

`SpecialModelRenderer.getExtents(Set)` 的签名不带 `ItemStack` 参数，是「对所有该渲染器物品通用」的；而 CGC 的 `NeoBEWLR` 同时承接枪械/配件/子弹三类模型，无法在 `getExtents` 里针对具体模型算包围盒。真实基岩模型包围盒计算成本高且收益小（包围盒只影响悬浮高度与是否平铺，不影响模型本身的绘制），所以用 1 格的近似立方体是最小、稳妥的修复。

### 4. 验证情况

- `./gradlew :neoforge-compat:compileJava --offline` → BUILD SUCCESSFUL。
- 已核对调用链：`ItemEntityRenderer.render` → `renderState.item.getModelBoundingBox()` → `visitExtents`（读 `getExtents`）→ 得到有限 AABB。
- 未进入游戏内验证。

### 请你测试

把枪械、配件、子弹丢到地上（掉落物），确认物品模型能正常显示、有影子，且悬浮高度正常（不浮空、不陷地）。红点/筒镜、第三人称、GUI 应保持正常。

工作区未提交，等你检查。

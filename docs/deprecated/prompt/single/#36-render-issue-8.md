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

自1.21.4起：
- neoforge：IClientItemExtensions移除了initializeClient和getCustomRenderer
- 原版：BlockEntityWithoutLevelRenderer被移除
- 例如1.21.1 原版ItemRenderer#render里有isCustomRenderer()检查，在1.21.4 原版ItemRenderer变成了renderItem

现在需要寻找替代的渲染方式

经测试：
- 物品的GUI渲染变为紫黑块
- 物品提示框的文本和颜色正常渲染
- 枪械物品提示框里的子弹物品（GUI渲染）同样是紫黑块
- 所有物品的第三人称渲染都是紫黑块
- 第一人称下，枪械安装配件可见
- 第一人称下，枪械安装红点瞄准镜，红点不开镜也可见（正常应该只在ocular范围里可见）
- 第一人称下，枪械安装筒镜，瞄准镜贴图也可见（正常应该只在ocular范围里可见），且ocular为黑色
- 切入枪械时正常播放抬枪动画，切出枪械时正常keep一段时间播放收枪动画

由于自定义渲染本身不需要super.调用BlockEntityWithoutLevelRenderer的方法，因此一个兼容的BlockEntityWithoutLevelRenderer已经完成，并且保持原本的继承关系的代码1.20.1-26.2保持统一（仅import diffs）
- 如非必要，则保留这个compat的BlockEntityWithoutLevelRenderer继承关系
- 本次修复代码尽可能控制在平台层解决（对应1.21.1及以前靠平台层自动完成注册），如果实在复杂可以写在core层

#36完成了渲染基础，移植到1.21.1时通过./docs/deprecated/prompt/single/#36-render-issue-7.md第846-930行完成了1.21.1+引入的渲染问题后，继续移植到1.21.4需要解决的，在完成#36之前就知道的待解决的渲染注册问题
- 照理说该修复的代码应该也是1.21.4需要的，如果解决注册问题中出现相似问题，可以通过该对话历史排查已验证的结论

需要适配的范围有可能不止IClientItemExtensions的initializeClient和getCustomRenderer，但优先默认只需要解决这两个方法原本能承担的问题
- 如果完成后我测试出来还是有渲染问题，再继续研究其他待增加的更改
- 注意tacz没有1.21.4的版本，而26.x.x版本的解决方式可能因版本过高而不能强行使用，从其他IDEA窗口浏览源码参考解决方案的时候需要检查是否能用在1.21.4
- 如果你需要了解某个为了跨版本而奇怪的写法/查询高版本是否有变化，我打开了CGC 26.2分支的IDEA窗口，你可以对比源码实现来了解

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

I'll start by establishing context from the documentation, then investigate the TaCZ and CGC sources. Let me read the key docs in parallel.

Let me read the render architecture docs and the prior fix reference.

I've confirmed the root cause and the fix design. Let me write the code. First, the bridge renderer and its registration.

我已经完成了调查与核心修复。下面是完整汇报。

## 1. 问题原因

两个现象对应两个**独立**的根因，不要混为一谈：

**（A）GUI / 第三人称 / 掉落物 / 展示框 紫黑块 —— 本次已修复**

1.21.4 起，原版删除了 `BlockEntityWithoutLevelRenderer`，neoforge 也从 `IClientItemExtensions` 里删除了 `initializeClient` 和 `getCustomRenderer`。替代机制是全新的 **物品模型系统**：

- 物品的模型不再由 `models/item/*.json` 决定，而是由 `DataComponents.ITEM_MODEL`（默认 = 物品注册键，如 `customgun:gun`）→ `assets/<ns>/items/*.json` 决定；
- “自定义渲染”（即原 BEWLR 的 `renderByItem`）改由 `minecraft:special` 模型类型 + 一个通过 `RegisterSpecialModelRendererEvent` 注册的 `SpecialModelRenderer` 承担。

CGC 完全没有迁移这套机制：三件物品（gun / attachment / ammo）的 mixin 里仍留着已失效的 `initializeClient` / `getCustomRenderer` 死代码，也没有任何 `items/*.json` 或 `SpecialModelRenderer` 注册。于是 `ModelManager.getItemModel(customgun:gun)` 命中 missing model → 紫黑块。而第一人称是事件驱动（`FirstPersonRender`），不经过物品模型系统，所以配件正常可见。

**（B）第一人称 红点/筒镜 不开镜也可见、ocular 全黑 —— 独立问题，本次未改**

这是 1.21.4 的**模板缓冲（stencil）分配**问题：1.21.4 把 `RenderTarget.enableStencil()` 删掉，改成了构造函数参数 `RenderTarget(useDepth, useStencil)`。CGC 的 `ClientRenderHelper.ensureMainRenderTargetStencil()` 现在是个空方法（注释写“1.21.4 起变成了构造函数时的参数”），`enableItemEntityStencilTest()` 里的 `enableStencil()` 调用也被注释掉了。结果主渲染目标没有分配 stencil buffer，目镜模板遮罩失效 → ocular 全黑、红点/镜片贴图不被模板裁剪。这个需要单独修（方向是对 `MainTarget`/`Minecraft` 构造处注入 `useStencil=true`，属于“其他待增加的更改”，按你的指示留到你验证完本次修复后再处理）。

## 2. TaCZ 原始实现

TaCZ 走的是旧链路：`Item.initializeClient(consumer)` → `consumer.accept(new IClientItemExtensions(){ getCustomRenderer(){ return bewlr; } })`。物品模型 JSON 用 `"parent": "builtin/entity"`（1.21.1 里 `builtin/entity` 被 `ModelBakery` 特判成 `BLOCK_ENTITY_MARKER`，即 `isCustomRenderer()==true` + 恒等变换 + `gui_light:side`）。渲染时 `ItemRenderer.render` 先 `handleCameraTransforms`（= 应用模型 transform，此处为恒等）再 `translate(-0.5,-0.5,-0.5)`，最后调 `getCustomRenderer().renderByItem(stack, ctx, pose, buffer, light, overlay)`。

## 3. CGC 移植情况

CGC 保留了 compat `BlockEntityWithoutLevelRenderer`（`client.compat.minecraft.BlockEntityWithoutLevelRenderer`）与 `IItemBEWLR.cgc$getBEWLR()`，三个 mixin 的 `renderByItem` 逻辑完好。但 1.21.4 的“注册”这一环缺失：旧的 `initializeClient`/`getCustomRenderer` 是死代码，物品没有任何 1.21.4 物品模型，也没有 `SpecialModelRenderer`。

我核对了 1.21.4 的 `ItemStackRenderState.LayerRenderState.render` 与 1.21.1 的 `ItemRenderer.render`：两者在调用自定义渲染器前都做了 `model.applyTransform(...)` + `translate(-0.5,-0.5,-0.5)`，坐标空间**完全一致**。因此只要 base 模型是恒等变换，桥接回 `renderByItem` 就能和 1.21.1 行为一致。

## 4. 修改内容（新增 5 个文件）

1. `neoforge-compat/.../item/NeoGeoItemSpecialRenderer.java` — 实现 `SpecialModelRenderer<ItemStack>`，`extractArgument` 直接返回 `ItemStack`，`render` 里对 `IItemBEWLR` 物品桥接 `cgc$getBEWLR().renderByItem(...)`（同时覆盖 gun/attachment/ammo 三件物品），内嵌 `Unbaked` record（`MapCodec.unit`，`bake` 返回单例）。
2. `neoforge-compat/.../init/NeoGeoItemSpecialRendererRegistry.java` — `@EventBusSubscriber(Dist.CLIENT)` 监听 `RegisterSpecialModelRendererEvent`，注册 codec id `customgun:geo_item`。
3. `core/src/main/resources/assets/customgun/items/gun.json` / `attachment.json` / `ammo.json` — 新物品模型，`minecraft:special` 类型，`base` 指向下面的空模型，`model.type = customgun:geo_item`。
4. `core/src/main/resources/assets/customgun/models/item/geo_base.json` — 空模型 `{}`（恒等变换 + 默认 `gui_light:side`，与旧 `builtin/entity` 对齐）。

没有改动任何既有代码（mixin、renderer、`renderByItem` 全部保持原样，compat BEWLR 继承关系保留）。

## 5. 验证情况

- `./gradlew :neoforge-compat:compileJava --offline` → **BUILD SUCCESSFUL**，无错误（仅一条“使用了 `@Deprecated` 的 `IItemBEWLR.cgc$getBEWLR()`”的 note，属预期）。
- IDEA 对两个新 Java 文件 `get_file_problems` 返回空错误列表。

## 需要你测试

进游戏后重点看 **GUI / 背包图标、tooltip 里的子弹、第三人称手持、掉落物、展示框** 是否恢复（应该不再是紫黑块）。枪械/配件/子弹的 GUI 槽位会渲染成扁平 slot 纹理，第三人称渲染 3D 模型。

## 遗留（等你确认本次修复后我再处理）

- **红点/筒镜 ocular 问题**：独立根因已定位（1.21.4 主 framebuffer 未分配 stencil），修复方向是对 `MainTarget` 构造启用 `useStencil`。
- **`ammo_box` 物品**：它是普通 `Item`（无自定义渲染器、无 mixin），本就不走 `initializeClient`/`getCustomRenderer`，所以不在本次“默认只解决注册问题”的范围内。它目前也缺 1.21.4 物品模型，后续需要按 TACZ 的 override/材质逻辑单独补一份 item model。

按约定，我没有 commit，工作区保持现状待你检查。

# 

```
现在测试如下:
- 第三人称正常渲染模型
- 物品GUI渲染，对于透明的地方还是透明（背包背景颜色一样），但是对于不透明（有实际内容）的地方，能看见颜色，但是看起来黑了一点，我感觉是光照问题，需要排查
- 瞄准镜ocular还是黑的，并且准心也没有只在ocular范围里渲染

现在继续处理剩余问题
```

# 

两处都已完成修复并编译通过。汇报如下。

## 1. GUI 图标变暗（光照问题）—— 已修复

**根因**：我上一轮把 base 模型写成了空 `{}`，导致它走默认 `gui_light: "side"`（= `usesBlockLight()==true` = 块光照）。而原版 CGC 的 `gun.json` 是 `{"parent": "builtin/entity", "gui_light": "front"}`——子模型的 `gui_light: "front"` 会覆盖 marker 的 `side`，最终 `usesBlockLight()==false`，于是 `GuiGraphics.renderItem` 里 `flag = !usesBlockLight() = true`，会调用 `Lighting.setupForFlatItems()`（平面光照，全亮）。我上一轮用 `side` 导致走了 3D 方向光照，槽位贴图被方向光压暗。

**修复**：`geo_base.json` 改成 `{"gui_light": "front"}`，恢复平面光照。已确认 `usesBlockLight()` 只被 `GuiGraphics.renderItem` 消费（只影响 GUI 光照，不影响第三人称/掉落物），所以这个改动不会影响已经正常的第三人称渲染。

## 2. 瞄准镜 ocular 黑 + 准心溢出 —— 已修复

**根因**：1.21.4 删除了 `RenderTarget#enableStencil()`，模板缓冲改为 `RenderTarget(useDepth, useStencil)` 构造函数参数。原版 `Minecraft` 创建主渲染目标时 `useStencil=false`，导致主 framebuffer 没有模板缓冲，`GL_STENCIL_TEST` 及 `stencilFunc/stencilOp` 全部失效 → 目镜黑色遮罩没打孔、准心没有被限制在镜片圆内。

**修复**：neoforge 在 1.21.4 提供了官方钩子 `ClientHooks.instantiateMainTarget`，它会触发 `ConfigureMainRenderTargetEvent` 并把 `event.isStencilEnabled()` 传给 `new MainTarget(width, height, enableStencil)`。我新增了平台层订阅：

- 新增 `neoforge-compat/.../init/NeoClientRenderTargetSetup.java`：`@EventBusSubscriber(Dist.CLIENT)` 监听 `ConfigureMainRenderTargetEvent`，调用 `event.enableStencil()`。

这正好对应旧的 `ClientSetup.onClientSetup()` → `ensureMainRenderTargetStencil()` 的职责（那个方法在 1.21.4 已变成空方法，注释也写明“变成构造函数时的参数”）。我没去动那个空方法和 core 层代码，保持改动最小。

## 验证情况

- `./gradlew :neoforge-compat:compileJava --offline` → **BUILD SUCCESSFUL**。
- 两个改动文件（`NeoClientRenderTargetSetup.java`）IDEA 检查无错误。

## 需要你测试

1. 背包/物品栏里枪械、配件、子弹的图标是否恢复到正常亮度（不再偏黑）。
2. 第一人称开镜：红点/筒镜的镜片是否出现放大画面、黑色镜框只在镜片圆外、准心只显示在镜片圆内。

## 说明

关于目镜渲染代码本身（`renderOcularAndDivision`、`getBedrockPartCenter`），我对比了 26.2 分支：那里的结构一致，`getBedrockPartCenter` 里那行 `mulLocal(getModelViewMatrix())` 同样是注释掉的（注释“加速渲染的兼容，正常应该不需要”），说明这是 1.21.4+ 有意为之，不是漏抄。所以目前唯一缺的就是模板缓冲分配，本次已补上。如果开镜测试发现镜片圆心的位置/圆形状仍有问题，再告诉我，我再深挖圆心坐标换算那段。

按约定未 commit，工作区保持现状待你检查。

# 

```
经测试：
- 现在物品GUI渲染正常，没有变黑
- 红点瞄准镜（enableSight=true）的准心正常，确实只在ocular里渲染了
- 筒镜的准心没有在ocular以外的地方看见了，但是ocular本身还是黑色（模型颜色），而没有渲染画面出来

这个问题可能跟解决#36-render-issue-7.md的时候类似，可以添加一些模板测试的log，然后我测试完发你继续研究
```

# 

已修复筒镜 ocular 全黑的问题，并定位了根因。

## 根因（源码对比确认，未加日志）

对比 TaCZ 1.21.1 与 CGC 1.21.4 的 `getBedrockPartCenter`：

- **TaCZ 1.21.1**（`BedrockAttachmentModel.java:187`）：
  ```java
  poseStack.pushPose();
  poseStack.last().pose().mulLocal(RenderSystem.getModelViewMatrix());  // 无条件生效
  for (BedrockPart part : path) part.translateAndRotateAndScale(poseStack);
  ```
- **CGC 1.21.4**（`_AttachmentModelRender.java`）：同一行被注释掉了，注释写“加速渲染的兼容，正常应该不需要”。

这行 `mulLocal(getModelViewMatrix())` 的作用是**把目镜圆心从模型空间换算到视图空间**。`renderOcularAndDivision` 里的三角形扇（`stencilOp(GL_INVERT)` 画圆形视野孔）用 `addVertex(matrixStack.last(), centerX, centerY, -90)` 在视图空间画圆，圆心坐标必须跟它在同一坐标系。被注释后，圆心还是模型空间坐标，三角形扇被画到错误位置，`GL_INVERT` 没有在镜片圆内翻转模板值 → 镜片圆孔没形成 → 黑色目镜遮罩盖满整块镜片。

这正是 #36-render-issue-7 的同类问题（那次的根因是圆弧顶点漏 `matrixStack.last()`，这次是圆心漏视图空间换算）。

旁证很硬：`ClientRenderHelper.GL._getModelViewMatrix()` 这个 helper 的 javadoc 写着“仅 `_AttachmentModelRender#getBedrockPartCenter`”，也就是它专门为这一行存在，但唯一的调用点却被注释掉了——明显是移植时误注释/误判，而不是真的不需要。

## 修改内容

只改了一处：把 `_AttachmentModelRender.java` 的 `getBedrockPartCenter` 里那行注释恢复为生效代码（用的是 CGC 自己的 1.21.4 helper `_getModelViewMatrix()`，等价于 TaCZ 的 `RenderSystem.getModelViewMatrix()`）：

```java
poseStack.pushPose(); {
    // 把模型空间换算到视图空间，与三角形扇（圆形目镜孔）保持同一坐标系
    poseStack.last().pose().mulLocal(ClientRenderHelper.GL._getModelViewMatrix());
    for (int i = 0; i < path.size(); i++) { ... }
}
```

## 验证情况

- `./gradlew :core:compileJava --offline` → **BUILD SUCCESSFUL**。
- IDEA 对 `_AttachmentModelRender.java` 检查无错误。

## 为什么没有加模板日志

你建议可以加 stencil 日志。但我通过直接对比 TaCZ 源码定位到了明确的移植差异（TaCZ 生效、CGC 被注释，且有“只为此处服务”的 helper 作旁证），属于“源码已高度确认根因”的情况，按之前的约定我直接改代码、不再为“保险”加日志。如果这次开镜测试筒镜镜片仍不是放大画面（还是黑的），我再按 #36 的 `[CGC-STENCIL-DEBUG]` 方式加最小化的模板值读取日志继续排查。

## 请你测试

第一人称装筒镜（长筒镜，enableScope=true / enableSight=false），右键开镜，看镜片是否出现放大、圆形视野的画面（红点已经正常，重点确认筒镜的圆形镜片画面）。

# 

```
确实修复了，但是这个修改我在1.21.1neoforge不添加，也能正常渲染，1.20.1-1.21.1添加了也不影响渲染
26.2的IDEA窗口当前开着，你研究下26.2里的RenderSystem.getModelViewMatrixCopy()是否是相同的功能，如果不是，我后续移植到26.2再考虑修改这个util
以及这次修改里添加的assets/customgun/items的东西，在1.20.1-1.21.1是否有这个原版目录及文件里指定的功能？是否仅1.21.4+有？
```

# 

两个问题都查清楚了，结论如下。

## 1. 26.2 的 `getModelViewMatrixCopy()` 与 1.21.4 的 `getModelViewMatrix()` 是同一功能

对比三个版本 `RenderSystem` 的实现：

| 版本 | 方法 | 返回 |
|---|---|---|
| 1.21.1 | `getModelViewMatrix()` | `modelViewMatrix`（一个**独立缓存的 `Matrix4f` 字段**，与 `modelViewStack` 是不同字段） |
| 1.21.4 | `getModelViewMatrix()` | `modelViewStack`（直接返回栈） |
| 26.2 | `getModelViewMatrixCopy()` | `new Matrix4f(modelViewStack)`（栈的**副本**） |

- 1.21.1 源码（`RenderSystem.java:76-77,819-826`）：`modelViewStack` 和 `modelViewMatrix` 是两个字段，`getModelViewMatrix()` 返回缓存的那个。
- 1.21.4 源码（`RenderSystem.java:704-707`）：`getModelViewMatrix()` 直接 `return modelViewStack;`。
- 26.2 源码（`RenderSystem.java:204-207`）：`getModelViewMatrixCopy()` 是 `return new Matrix4f(modelViewStack);`。

**结论**：26.2 的 `getModelViewMatrixCopy()` 与 1.21.4 的 `getModelViewMatrix()` **功能相同**（都取当前的 model-view 栈矩阵），只是 26.2 返回防御性副本、1.21.4 返回活对象。对于 `mulLocal(...)` 这个用法（矩阵只作为参数被读、不修改它），两者完全等价。

所以 CGC 26.2 里那个 `_getModelViewMatrix()` util 已经 `return RenderSystem.getModelViewMatrixCopy();`，**是对的，不用改 util**。但注意：26.2 分支里 `getBedrockPartCenter` 那行 `mulLocal` 同样是注释掉的（跟我这次在 1.21.4 修的是同一处 bug），你后续移植到 26.2 时只需把它取消注释即可，util 本身不动。

顺带解释你观察到的现象：1.21.1 的 `getModelViewMatrix()` 返回的是那个**缓存字段**（`modelViewMatrix`，在 `recordRenderCall` 里才更新，第一人称手部渲染阶段那一刻它基本是过时/近似恒等值），所以 1.21.1 里 `mulLocal` 乘上去没效果——不加也正常、加了也不影响。1.21.4 把它改成直接返回 `modelViewStack`（实时值），于是这行才变成必需。

## 2. `assets/customgun/items/` 仅 1.21.4+ 有

这是 1.21.4 引入的「物品模型定义」体系，1.20.1–1.21.1 没有：

- **1.21.1**：没有 `ClientItem`、`ItemModel`、`SpecialModelRenderer`、`DataComponents.ITEM_MODEL`、`FileToIdConverter.json("items")`（我在 1.21.1 的 neoForm 源码里逐一 grep，全为空）。1.21.1 的物品模型在 `assets/<ns>/models/item/*.json`（旧 `builtin/entity` / `generated` 格式），自定义渲染靠 `IClientItemExtensions.getCustomRenderer()` + `BlockEntityWithoutLevelRenderer`。
- **1.20.1（TACZ）**：同样没有新体系，用的还是 `getCustomRenderer()`（`AbstractGunItem` / `FirstPersonRenderEvent` 里能看到），物品模型是 `models/item/*.json` + `overrides` 旧格式。

所以这次新增的 `items/gun.json`、`attachment.json`、`ammo.json`（`minecraft:special` 模型 + `customgun:geo_item` codec）以及 `RegisterSpecialModelRendererEvent` 那一整套，**只对 1.21.4+ 有意义**，不应回移植到 1.20.1–1.21.1。那两个分支继续走原来的 `getCustomRenderer()` 注册路径即可；我这次的新增文件（`items/`、`NeoGeoItemSpecialRenderer`、`NeoGeoItemSpecialRendererRegistry`）都是 1.21.4 分支专属。

（`geo_base.json` 里的 `gui_light` 属于旧模型格式里就有的字段，但它放的是给 1.21.4 `special` 模型当 `base` 用的，1.20.1–1.21.1 用不到。）

# 

```
我现在完成了对这个移植的最终修改，已经commit，当前状态是能工作的状态，以现在的代码为准：
- 检查CGC渲染文档里是否有需要补充说明的
- 修改后的内容将从1.20.1就可见，所以不是让你删掉1.20.1-1.21.1的内容改成1.21.4，而是在合适位置补充1.21.4起变成什么样
- 由于只是注册方式的兼容，注意不应出现大量的文档修改，本次修改仅是给低版本浏览的时候一个“高版本注册方式有变”的提示

完成后不要commit，我检查后会手动在core1.20.1的分支commit
```

# 

Let me check the current committed code state to write accurate doc notes, then review the render docs.

I'll add a concise cross-version note to the render entry-points doc where the item-renderer binding is described.

完成。只改了渲染文档的一处，未 commit。

## 修改内容

在 [rendering-entry-points.md](app://localhost/epitaxy/docs/architecture/client/render/rendering-entry-points.md) 的「物品与渲染器的绑定」小节末尾补了一段「跨版本」提示（blockquote，两行要点）：

- 明确「`ItemStack` → Mixin 接口 → BEWLR 渲染器」这条运行时链路各版本一致（不改动原文，保留给低版本读者）。
- 补充注册方式的差异：1.20.1–1.21.1 走 `IClientItemExtensions.initializeClient` + `getCustomRenderer()` 返回 `BlockEntityWithoutLevelRenderer`；1.21.4+ 改为 `SpecialModelRenderer`（`RegisterSpecialModelRendererEvent` 注册 codec `customgun:item_bewlr`）+ `assets/customgun/items/*.json` 的 `minecraft:special` 模型，再桥接回 `IItemBEWLR.cgc$getBEWLR().renderByItem()`。

## 为什么只改这一处

其它渲染文档里对 BEWLR 的提及（`Home.md` 的「renderStatic → BEWLR」、`gui-and-tooltip.md` 的「renderByItem(GUI)」）都是描述**运行时链路**，不涉及「注册进物品模型系统」这件事，各版本通用，不需要加提示。真正描述注册/绑定机制的地方只有 `rendering-entry-points.md` 的「物品与渲染器的绑定」一节，所以补充在这里最合适，也符合“仅加一个提示、不做大改”的要求。

## 校验

- 注释里引用的 codec id `customgun:item_bewlr` 与当前代码 `IItemBEWLR.REGISTRY_LOCATION` 一致；`NeoBEWLR` / `NeoSpecialModelRendererRegistry` / `minecraft:special` 也一一对应，没有笔误。
- `git status` 仅 `rendering-entry-points.md` 一个文件改动，未 commit，留给你在 core1.20.1 分支手动提交。

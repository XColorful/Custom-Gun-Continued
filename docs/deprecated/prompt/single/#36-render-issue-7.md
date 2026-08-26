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

经测试:
- 1.21.1neoforge版，装备筒镜(enableScope=true)时，ocular是黑色的
- 1.21.1forge版正常
- 两个版本均正确渲染模型、红点瞄准镜(enableSight=true)、正确渲染配件、正确渲染装配界面(GunRefitScreen)

待解决的问题：
- 1.21.1neoforge版，筒镜仍然是黑色（模型颜色）而不是通过瞄准镜应该看到的画面

当前IDEA打开了CGC和TaCZ窗口，可以对比CGC1.21.1neoforge跟1.21.1forge的差异，也可以看TaCZ源码实现差异
- 注意IDEA提供的MCP阅读不同窗口的源码需要用项目目录来指定

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

I'll start by establishing context from the documentation and understanding the scope/ocular rendering mechanism. Let me read the key architecture documents and migration mapping first.

I've identified the root cause. Let me implement the fix, then verify it compiles.

调查与修改完成。以下是结论。

## 1. 问题原因

筒镜（长筒镜 `enableScope=true`）的「透过镜片看世界」效果依赖 **OpenGL 模板缓冲（stencil buffer）**：先渲染目镜把镜片区域写入模板值，再用 `stencilFunc(GL_EQUAL, 0)` 让镜身和枪身在「非镜片区域」绘制，从而在镜片处镂空露出背后已渲染（并已按倍率缩放 FOV）的世界。

CGC 缺少 TaCZ 在客户端初始化阶段执行的**提前分配主渲染目标模板缓冲区**的调用（`getMainRenderTarget().enableStencil()`）。于是模板缓冲只会在**第一次开镜渲染时**、在 `ClientRenderHelper.enableItemEntityStencilTest()` 里被惰性分配。这个惰性分配发生在渲染中途（`renderItemInHand` 阶段，世界已画完），`enableStencil()` 会触发 `MainTarget.resize()` → `destroyBuffers()` + `createBuffers()`，**在帧中途销毁并重建主 framebuffer**，破坏了已渲染的世界并使模板缓冲未能在正常时机生效，导致目镜区域被镜身的黑色模型颜色覆盖（黑镜）。

（forge / neoforge 症状差异是加载器渲染循环对「帧中途重建 framebuffer」的恢复行为不同所致，核心代码与两边的原版 `RenderTarget`/`MainTarget` 我已逐行比对完全一致。这个差异通过静态分析无法 100% 钉死，但修复方向不依赖它。）

## 2. TaCZ 原始实现

`com.tacz.guns.client.init.ClientSetupEvent.onClientSetup(FMLClientSetupEvent)` 在客户端初始化时执行：

```java
Minecraft minecraft = Minecraft.getInstance();
RenderSystem.recordRenderCall(() -> minecraft.getMainRenderTarget().enableStencil());
```

`recordRenderCall` 把 `enableStencil()` 排队到渲染线程，在首帧渲染前执行，让主 framebuffer 提前带好模板缓冲。这样渲染期间的 `enableItemEntityStencilTest()` 里的 `enableStencil()`（`if(stencilEnabled) return;`）就变成空操作，不会在帧中途重建 framebuffer。

## 3. CGC 移植情况

迁移映射里 `ClientSetupEvent.onClientSetup(FMLClientSetupEvent)` 被标为 `_Deprecated_`，客户端初始化被重构为 `ClientSetup.onClientSetup()`（由 `NeoClientSetup`/`ForgeClientSetup` 通过 `event.enqueueWork` 调用），但**漏掉了上述提前 `enableStencil()` 调用**。当前全项目仅 `ClientRenderHelper.enableItemEntityStencilTest()`（`core/.../util/ClientRenderHelper.java:59`）一处惰性调用。核心渲染逻辑（`_AttachmentModelRender` / `_GunModelRender` 的模板测试）与 TaCZ 一致，且 forge/neoforge 两个分支的 core 源码相同（仅一行空白差异）。

## 4. 修改内容

在 [ClientSetup.java](core/src/main/java/dev/xcolorful/customgun/client/init/ClientSetup.java) 的 `onClientSetup()` 里补回提前分配：

```java
Minecraft minecraft = Minecraft.getInstance();
RenderSystem.recordRenderCall(() -> ClientRenderUtils.getMainRenderTarget(minecraft).enableStencil());
```

这是共享的客户端初始化钩子，对 neoforge / forge 都生效，与 TaCZ 的语义和执行时机一致。修改后，模板缓冲在首帧前就绪，开镜渲染时的惰性 `enableStencil()` 变为空操作，不再中途重建 framebuffer。

## 5. 验证情况

- 静态检查：IDEA 对该文件 `get_file_problems` 返回 `errors: []`，无编译错误；`RenderSystem.recordRenderCall(RenderCall)` 的 lambda 在两加载器下均兼容。
- 只改动了这一个文件（`git diff` 确认），未提交。

**下一步请你测试**：用 1.21.1neoforge 版启动游戏，装备一把带长筒镜（`enableScope=true`）的枪并右键开镜。预期：目镜处能看到按倍率放大、带圆形视野的画面，而不是黑色。若仍黑，请告诉我，我再基于运行时日志（针对 `enableStencil` 是否执行、`stencilEnabled` 状态、模板缓冲是否真正生效）继续定位。

Sources: [NeoForge ConfigureMainRenderTargetEvent (1.21.4 javadoc)](https://aldak0.ru/javadoc/1.21.4-21.4.x/net/neoforged/neoforge/client/event/ConfigureMainRenderTargetEvent.html)（用于佐证 NeoForge 侧主渲染目标模板缓冲是独立配置项，1.21.1 对应 `RenderTarget.enableStencil()` 注入逻辑）。

# 

```
没有生效，筒镜的ocular依旧是黑色的，请继续排查
```

# 

My first fix (early `enableStencil`) didn't resolve it. I need to dig into what actually differs between the two loader paths. Let me compare the TaCZ scope render more precisely and check the FOV/camera wiring.

I found the NeoForge `@EventBusSubscriber` ignores `bus` and auto-routes `IModBusEvent` to the mod bus, so my `onClientSetup` does fire. The issue is deeper. Let me add targeted runtime diagnostics to determine the actual stencil buffer state, since static analysis says `enableStencil()` should work.

第一个修复（提前 `enableStencil()`）没有生效，说明模板缓冲问题比我预期的更底层。我已经加了最小化的运行时诊断日志，用来确认「模板缓冲到底有没有真正分配」，而不是继续盲猜。

## 这次改了什么

只加了两处带 `[CGC-STENCIL-DEBUG]` 标记的日志（都是高频渲染路径上的**一次性**日志，不会刷屏）：

1. [ClientSetup.java](core/src/main/java/dev/xcolorful/customgun/client/init/ClientSetup.java) — 启动时打印一行，确认提前分配是否执行。
2. [ClientRenderHelper.java](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java) — 第一次渲染瞄具时，打印主渲染目标的模板缓冲**实际 GL 状态**：
    - `isStencilEnabled`（是否调过 `enableStencil`）
    - `stencilAttachType` / `depthStencilAttachType` / `depthAttachType`（帧缓冲上真正挂接的附件类型）

## 关键背景（源码确认，供你理解日志）

- NeoForge 的 `@EventBusSubscriber` 会**忽略 `bus` 参数**，按事件类型自动路由：`FMLClientSetupEvent` 是 `IModBusEvent`，所以会正确注册到 MOD 总线。也就是说我的 `onClientSetup` 确实会执行，排除「修复没跑」的简单可能。
- 两边 forge/neoforge 的 `RenderTarget.enableStencil()` / `MainTarget` / `RenderSystem` 源码我逐行比对过，逻辑一致。所以差异一定在运行时行为，日志能直接区分。

## 日志怎么读（拿到后我能直接定位）

- `stencilAttachType=5890` 或 `depthStencilAttachType=5890`（即 GL_TEXTURE）→ 模板缓冲**确实挂上了**，但镜片仍黑 → 问题在别处（渲染顺序/状态机/FOV），我继续查那个方向。
- `isStencilEnabled=true` 但三个附件类型都是 0（GL_NONE）→ `enableStencil()` 设了标志但 NeoForge 没真正重建出模板缓冲 → 走 NeoForge 特有的分配路径。
- `isStencilEnabled=false` → 我的提前分配没跑（或 lazy 调用都没到），继续查调用链。

## 请你测试

1. 用 1.21.1neoforge 版启动游戏，进世界。
2. 装备带长筒镜（`enableScope=true`）的枪，右键开镜一次（触发瞄具渲染）。
3. 在游戏日志（`latest.log` 或控制台）里搜 `CGC-STENCIL-DEBUG`，把这两行原样发给我：
    - 启动时的 `ClientSetup.onClientSetup called...`
    - 开镜时的 `isStencilEnabled=... stencilAttachType=... depthStencilAttachType=... depthAttachType=...`

拿到这两行我就能判断下一步改哪里，不需要你做其他操作。

# 

```
我将你的日志从system print改成了logger，其余没变
测试时刚进游戏就手持装有4倍镜的SLR
日志如下：
[26Aug2026 22:31:57.697] [Render thread/DEBUG] [net.neoforged.fml.DeferredWorkQueue/LOADING]: Dispatching synchronous work for work queue Sided setup: 2 jobs
[26Aug2026 22:31:57.698] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: OverlayManager: registered overlay sub manager customgun:DefaultCrosshair for overlay crosshair
[26Aug2026 22:31:57.702] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-DEBUG] ClientSetup.onClientSetup called; queued enableStencil
[26Aug2026 22:31:57.705] [Render thread/DEBUG] [net.neoforged.fml.DeferredWorkQueue/LOADING]: Synchronous work queue completed in 8.284 ms

[26Aug2026 22:32:19.198] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-DEBUG] isStencilEnabled=true fb=2 stencilAttachType=5890 depthStencilAttachType=5890 depthAttachType=5890
[26Aug2026 22:32:22.142] [Render thread/WARN] [dev.xcolorful.customgun.CustomGun/]: Failed to get soundPath from soundLocation null
[26Aug2026 22:32:26.776] [Server thread/INFO] [net.minecraft.client.server.IntegratedServer/]: Saving and pausing game...
```

# 

The log is decisive: `isStencilEnabled=true` and all attachment types are `5890` (GL_TEXTURE). **The stencil buffer IS present and correctly allocated** — so my "stencil not allocated" hypothesis is definitively wrong. The scope is black for a different reason.

The scope's lens hole depends on `rad = 80 * scopeViewRadiusModifier * aimingProgress`. If `aimingProgress` is 0, the circular hole has radius 0 and the ocular black mask covers the whole lens → black. The red-dot sight uses a different path (`renderDivisionOnly`) that has no radius, which is why it still "works". Let me add a targeted diagnostic for that.

日志给出了关键结论，先同步一个重要发现，再说下一步。

## 重要发现：模板缓冲是好的，问题在别处

日志里这行：

```
isStencilEnabled=true fb=2 stencilAttachType=5890 depthStencilAttachType=5890 depthAttachType=5890
```

- `isStencilEnabled=true` → `enableStencil()` 确实执行了（我的提前分配生效）。
- `stencilAttachType=5890`（GL_TEXTURE）等 → 帧缓冲上**确实挂接了模板附件**。

所以我之前「模板缓冲没分配」的假设是**错的**。模板缓冲在 neoforge 上已经正常存在，镜片仍黑是另一个原因。

## 新方向：镜片圆孔的半径

长筒镜的「透过镜片看世界」依赖一个圆形孔，其半径是：

```
rad = 80 * scopeViewRadiusModifier * aimingProgress
```

当开镜瞄准时 `aimingProgress` 应趋近 1。**如果 neoforge 上 `aimingProgress` 一直是 0，`rad` 就是 0 → 圆孔退化成一个点 → 黑色目镜遮罩盖满整个镜片 → 全黑。** 这能同时解释「红点瞄准镜正常」（它走的是 `renderDivisionOnly`，不依赖这个半径）和「长筒镜黑」。

我加了一行一次性/低频日志（每 60 帧约 1 秒一条），在 `renderOcularAndDivision` 里输出 `rad`、`aimingProgress`、`scopeViewRadiusModifier`、`entries`：

```java
CustomGun.LOGGER.debug("[CGC-SCOPE-DEBUG] rad={} aimingProgress={} scopeViewRadiusModifier={} entries={}", ...)
```

## 请你测试

1. 1.21.1neoforge 进世界，手持那把 4 倍镜 SLR。
2. **按住右键持续开镜瞄准几秒**（这一步很关键，让日志能捕捉到瞄准状态），可以松一下再瞄一次。
3. 从日志里搜 `CGC-SCOPE-DEBUG`，把这几条原样发给我（尤其要包含你正在瞄准时打印的那几条）。

## 拿到后我怎么判断

- 瞄准时 `aimingProgress` 仍是 0 → 瞄准进度追踪在 neoforge 上坏了，我查 `LocalShooterAim` / 输入 mixin。
- `aimingProgress > 0` 但 `rad` 仍是 0 → `scopeViewRadiusModifier` 为 0（配件显示数据问题）。
- `rad > 0` 且 `aimingProgress > 0` 但镜片仍黑 → 圆形模板掩膜绘制本身有问题（圆心坐标或 `GL_INVERT` 模板操作），我继续查那一块。

先看瞄准时的 `aimingProgress` 值，基本就能锁定方向。

# 

```
进游戏后一共执行力两次开镜，ocular均为黑色
两种日志一共如下:
[26Aug2026 22:42:51.490] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-DEBUG] ClientSetup.onClientSetup called; queued enableStencil
[26Aug2026 22:44:14.142] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-DEBUG] isStencilEnabled=true fb=2 stencilAttachType=5890 depthStencilAttachType=5890 depthAttachType=5890
[26Aug2026 22:44:14.145] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:14.795] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:15.070] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:15.386] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:15.718] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:16.110] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:16.423] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:17.061] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=36.66869 aimingProgress=0.45835862 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:17.614] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:18.244] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:18.813] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:19.385] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=13.891752 aimingProgress=0.1736469 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:19.909] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:20.445] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=73.27981 aimingProgress=0.9159976 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:20.941] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:44:21.366] [Server thread/INFO] [net.minecraft.client.server.IntegratedServer/]: Saving and pausing game...
[26Aug2026 22:44:21.407] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
```

# 

The log shows `aimingProgress` reaches `1.0` and `rad` reaches `80.0` while aiming, so the radius and aiming are **correct**. The hole isn't being created despite a valid radius — which points at the circular stencil mask's **center position**. I found a concrete porting difference earlier: TaCZ's `getBedrockPartCenter` multiplies the pose by `RenderSystem.getModelViewMatrix()`, CGC's doesn't. Let me verify that's the culprit.

The log confirms `aimingProgress` reaches `1.0` and `rad` reaches `80.0`, so the radius and aiming are correct. The hole isn't created because the circular stencil mask is drawn at the wrong screen position. I found the concrete porting bug: CGC dropped `pose.mulLocal(RenderSystem.getModelViewMatrix())` from `getBedrockPartCenter` (TaCZ has it at `BedrockAttachmentModel.java:187`; the same line was also dropped from `cacheMuzzlePosition`). Let me restore it.

关键信息拿到了，结论很清晰：

```
rad=0.0 aimingProgress=0.0  ...  (未瞄准)
rad=80.0 aimingProgress=1.0 ...  (瞄准中)
```

**瞄准进度和半径都完全正确**（瞄准时 `aimingProgress=1.0`、`rad=80.0`、`entries=1`、`scopeViewRadiusModifier=1.0`）。所以问题不是瞄准状态、不是半径，而是**圆形模板掩膜被画错了位置**，导致 `GL_INVERT` 在错误的地方翻转模板，黑色目镜遮罩盖满了整个镜片。

## 找到的根因（一个移植遗漏）

对比 TaCZ 的 `BedrockAttachmentModel.getBedrockPartCenter()`，CGC 在移植时**漏掉了一行**：

```java
poseStack.last().pose().mulLocal(RenderSystem.getModelViewMatrix());
```

这行的作用是把目镜中心从「模型空间」变换到「视图空间」，得到目镜在屏幕上的正确圆心。没有它，圆形视野孔被画在错误位置，镜片就被黑色遮罩盖住。

CGC 的 `cacheMuzzlePosition()`（枪口位置，给曳光弹用）也漏了同样一行，但那是另一个问题，这次先不动。

## 修改内容

在 [_AttachmentModelRender.java](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java) 的 `getBedrockPartCenter()` 里补回这一行（并加了 `RenderSystem` import），与 TaCZ 原实现完全一致。

## 请你测试

1. 1.21.1neoforge 进世界，手持 4 倍镜 SLR，右键开镜。
2. 看镜片是否出现放大、带圆形视野的画面（而不是黑色）。

如果这次镜片正常了，我再把之前加的调试日志清理掉；如果还是黑，请告诉我，我继续查 neoforge 渲染管线里与 forge 不同的具体位置（目前中心位置是最可疑、且有 TaCZ 明确对照的一个差异，但 forge/neoforge 为何表现不同我还没完全钉死，需要你这次结果来收敛）。

# 

```
4倍镜ocular依旧是黑色
日志如下：
[26Aug2026 22:58:09.709] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:10.114] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=16.814228 aimingProgress=0.21017785 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:10.588] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:11.025] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=43.37777 aimingProgress=0.54222214 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:11.411] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:11.805] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:12.180] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=65.14845 aimingProgress=0.8143557 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:12.558] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:12.926] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=61.322655 aimingProgress=0.7665332 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:13.300] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:13.666] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:14.038] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 22:58:14.175] [Server thread/INFO] [net.minecraft.client.server.IntegratedServer/]: Saving and pausing game...
```

# 

The center fix didn't resolve it either. I need to look at the stencil operations themselves and the batching. Let me examine a key difference I noticed between TaCZ and CGC: TaCZ flushes with `endBatch(renderType)`, CGC uses `endBatch()`.

The center fix didn't resolve it either. I need to see the actual stencil buffer contents at render time to determine which step is failing. I've added a diagnostic that reads the stencil buffer directly.

## 这一步要查什么

我已经确认：模板缓冲存在、瞄准进度正确(1.0)、半径正确(80)、目镜条目正确(1)。但镜片仍黑，说明问题在**模板缓冲的实际读写过程**里。

现在加的诊断会在镜片渲染完成后，用 `glReadPixels` 直接读屏幕上 5 个点（中心 + 上下左右各 100px）的**模板缓冲实际值**，并报告模板测试是否启用：

```
[CGC-STENCIL-VALUE] enabled=true (+0,0)=254 (+0,-100)=1 ...
```

## 这些值怎么解读（拿到后我能直接定位）

- `enabled=false` → 模板测试在渲染时被关闭了（本应开启），查 `glEnable(GL_STENCIL_TEST)` 为何失效。
- 中心 `(+0,0)=0` → 目镜没写入模板值（`renderOcularStencil` 没生效）。
- 中心 `(+0,0)=1` → 目镜写入了 1，但圆形掩膜的 `GL_INVERT` 没翻转 → 圆形孔没形成。
- 中心 `(+0,0)=254` → 圆形孔已经翻转成功，那镜片还黑就是**孔后面的世界没渲染出来**（深度/FOV 问题），我转去查那边。

## 请你测试

1. 1.21.1neoforge 进世界，手持 4 倍镜 SLR。
2. **按住右键持续开镜瞄准几秒**（让日志在瞄准状态下打印）。
3. 搜 `CGC-STENCIL-VALUE`，把瞄准时打印的那几行原样发给我（顺带把同一时间的 `CGC-SCOPE-DEBUG` 也发我，便于对齐状态）。

拿到中心点的模板值，就能精确定位是「写入失败」「翻转失败」还是「世界没画出来」，然后直接改对应那一处。

# 

```
一共执行了两次长按开镜，第一次手贱松了一下再长按，这应该不影响；第二次是一次性完整按到开镜100%
日志如下：
[26Aug2026 23:11:29.937] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-DEBUG] isStencilEnabled=true fb=2 stencilAttachType=5890 depthStencilAttachType=5890 depthAttachType=5890
[26Aug2026 23:11:29.939] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:30.769] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:30.781] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:31.438] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:31.448] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:32.061] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:32.071] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:32.633] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:32.645] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:33.218] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:33.230] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:33.732] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:33.743] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=11.235353 aimingProgress=0.14044192 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:34.214] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:34.220] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=56.81089 aimingProgress=0.7101361 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:34.674] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:34.680] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:35.096] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:35.102] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:35.536] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:35.542] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:35.994] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:36.001] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:36.408] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:36.413] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:36.817] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:36.823] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=12.835393 aimingProgress=0.16044241 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:37.295] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:37.301] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:37.735] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:37.743] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=51.94683 aimingProgress=0.6493354 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:38.174] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:38.179] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:38.647] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:38.653] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=80.0 aimingProgress=1.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:39.057] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1
[26Aug2026 23:11:39.063] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=69.33316 aimingProgress=0.8666645 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:39.482] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:39.490] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
[26Aug2026 23:11:39.833] [Server thread/INFO] [net.minecraft.client.server.IntegratedServer/]: Saving and pausing game...
[26Aug2026 23:11:39.969] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-STENCIL-VALUE] enabled=true (+0,0)=0 (+0,-100)=0 (+0,100)=0 (+-100,0)=0 (+100,0)=0
[26Aug2026 23:11:39.978] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-SCOPE-DEBUG] rad=0.0 aimingProgress=0.0 scopeViewRadiusModifier=1.0 entries=1
```

# 

Found it. The debug data is conclusive:

```
enabled=true (+0,0)=1 (+0,-100)=1 ... (所有点 stencil=1)
```

The ocular wrote stencil=1 correctly (`renderOcularStencil` works), but the circular mask's `GL_INVERT` never inverted it to 254 — the lens circle hole was never formed. And I found the exact bug: the triangle fan's center vertex uses the pose matrix, but the rim vertices don't.

找到根因了，日志给出了决定性证据：

```
enabled=true (+0,0)=1 (+0,-100)=1 (+0,100)=1 (+-100,0)=1 (+100,0)=1  (瞄准时)
```

- `enabled=true`：模板测试正常开启。
- **所有点的模板值都是 1**：说明目镜确实写入了 1（`renderOcularStencil` 正常），但圆形孔的 `GL_INVERT` 翻转**没有发生**（中心本应是 254）。

## 根因：圆形模板扇形的顶点坐标空间不一致（移植 bug）

在 `renderOcularAndDivision` 里画圆形掩膜（三角形扇 TRIANGLE_FAN）时：

```java
// 圆心顶点：用了 matrixStack.last()（带 Pose 变换）
builder.addVertex(matrixStack.last(), centerX, centerY, -90.0f)...
// 圆弧顶点：漏掉了 matrixStack.last()，变成了裸坐标
builder.addVertex(centerX + cos * rad, centerY + sin * rad, -90.0f)...
```

圆心和圆弧在**两个不同坐标系**里，扇形被画成畸形，`GL_INVERT` 根本没翻转镜片区域，于是黑色目镜遮罩盖满镜片。

对比 TaCZ 原实现（`BedrockAttachmentModel.renderOcularAndDivision`），圆弧顶点也是 `matrixStack.last()`：

```java
builder.addVertex(matrixStack.last(), centerX + cos * rad, centerY + sin * rad, -90.0F)...
```

## 修改内容

在 [_AttachmentModelRender.java](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java) 里把圆弧顶点补回 `matrixStack.last()`，与 TaCZ 完全一致。（之前补的 `mulLocal(getModelViewMatrix())` 也是同一处移植遗漏的一部分，一并保留。）

## 请你测试

1. 1.21.1neoforge 进世界，手持 4 倍镜 SLR，右键开镜。
2. 看镜片是否出现放大、圆形视野的画面。

如果这次正常了，我就把之前加的 `[CGC-STENCIL-DEBUG]`、`[CGC-SCOPE-DEBUG]`、`[CGC-STENCIL-VALUE]` 调试日志清理掉，只保留这两处真正的修复。

# 

```
成功修复。

完成以下内容:
- 去掉测试log
- 说明一下一共哪些修改，原理
- 这些修改是否应该从1.20.1就应该有，但是可能旧版本刚好能工作；如果只是移植到1.21.1的时候出现了移植问题/人工敲键盘看错字符导致的，请指出
```

# 

The fix is confirmed working. Let me clean up the debug logs and then explain everything.

Now let me investigate the 1.20.1 question — whether these were porting typos or latent issues. Let me check the original TaCZ 1.20.1 code and the 1.20.1 `VertexConsumer` semantics.

调试日志已全部清理干净（`grep` 确认无残留），最终改动只剩两个文件：

## 一共改了哪些、原理是什么

### 1. 三角形扇圆弧顶点补上 `matrixStack.last()` —— 真正的根因

文件：[_AttachmentModelRender.java](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java) 的 `renderOcularAndDivision()`

```java
// 圆心（本来就对）
builder.addVertex(matrixStack.last(), centerX, centerY, -90.0f)...
// 圆弧（修复前漏了 matrixStack.last()）
builder.addVertex(matrixStack.last(), centerX + cos * rad, centerY + sin * rad, -90.0f)...
```

原理：这个三角形扇是用来做「圆形镜片视野孔」的——它在 `stencilOp(GL_INVERT)` 下画一个圆，把目镜区域内写好的模板值 `1` 翻转成 `254`，这样黑色目镜遮罩只会画在圆环（值=1）上，圆心（值=254）就空出来露出背后的世界。

修复前圆心用了 Pose、圆弧没用 Pose，圆被画成畸形，`GL_INVERT` 根本没翻转镜片区域 → 模板值全是 1 → 黑色遮罩盖满镜片。日志里 `(+0,0)=1 ... 全 1` 就是铁证。

### 2. `getBedrockPartCenter` 补上 `mulLocal(getModelViewMatrix())`

同文件：

```java
poseStack.pushPose(); {
    poseStack.last().pose().mulLocal(RenderSystem.getModelViewMatrix());  // 补的这行
    for (...) { part.translate_rotate_scale(poseStack); }
    ...
}
```

原理：计算目镜圆心的屏幕坐标时，需要把「模型空间」换算到「视图空间」，否则圆的位置不对。这是给上面的圆形孔提供正确圆心。

### 3. 客户端初始化补上提前 `enableStencil()`

文件：[ClientSetup.java](core/src/main/java/dev/xcolorful/customgun/client/init/ClientSetup.java) 的 `onClientSetup()`

```java
Minecraft minecraft = Minecraft.getInstance();
RenderSystem.recordRenderCall(() -> ClientRenderUtils.getMainRenderTarget(minecraft).enableStencil());
```

原理：在启动时提前给主渲染目标分配模板缓冲，这样开镜时 `enableItemEntityStencilTest()` 里的 `enableStencil()` 变成空操作，避免第一次开镜时在渲染中途重建 framebuffer（会闪一帧/破坏当帧世界）。这处不是黑镜的直接原因（直接原因是第 1 处），但它对齐了 TaCZ 1.21.1 的行为。

## 这三处是不是 1.20.1 就该有的？

**都不是。这三处全是移植到 1.21.1 时产生的问题（打字/漏抄），不是 1.20.1 就该有的。**

关键依据——1.20.1 和 1.21.1 的顶点 API 变了：

| | TaCZ 1.20.1 | TaCZ 1.21.1 | CGC 1.21.1（移植后） |
|---|---|---|---|
| 三角形扇顶点 | `builder.vertex(x, y, z)`（旧 API，无需显式 Pose） | `addVertex(matrixStack.last(), x, y, z)`（所有顶点都带 Pose） | 圆心带了 Pose，**圆弧漏了** ← typo |
| getBedrockPartCenter | 无 `mulLocal`（模型空间即可） | 加了 `mulLocal(getModelViewMatrix())` | **漏加了** ← 漏抄 |
| 提前 enableStencil | 无（只有懒加载） | 加了（`ClientSetupEvent` 里 `recordRenderCall`） | **重构时弄丢了** |

解释：

- **第 1 处（圆弧漏 `matrixStack.last()`）**：1.20.1 的 `builder.vertex(x,y,z)` 用的是隐式当前矩阵，圆心圆弧同坐标系，所以 1.20.1 正常。1.21.1 把顶点 API 改成了必须显式传 Pose（`addVertex(Pose, …)`），而 `addVertex(x,y,z)` 变成了裸坐标。TaCZ 升级时把每个顶点都改成显式 Pose，CGC 移植时把圆心改了、**圆弧那行漏敲了 `matrixStack.last()`**，于是圆心圆弧落到两个坐标系，扇形画废 → 黑镜。这是典型的**人工看漏/漏打字符**。

- **第 2 处（漏 `mulLocal`）**：同样是 1.21.1 显式 Pose API 带来的配套改动——TaCZ 1.21.1 特意加上了这行来把圆心换算到视图空间，CGC 移植时**整个漏抄了这行**。

- **第 3 处（漏提前 `enableStencil`）**：这是 TaCZ 1.21.1 针对新版渲染管线**新增**的（1.20.1 没有），CGC 在重构客户端初始化（`ClientSetupEvent` 被标 `_Deprecated_`）时**把这行弄丢了**。

所以结论很明确：**1.20.1 的代码本身是对的（用的是旧 API），问题全部出在「把代码搬到 1.21.1 新顶点 API」这一步，属于移植时的漏改/漏抄，不是 1.20.1 就存在的隐患。**

最后提醒：这些修改还在工作区未提交（按约定我没有 commit）。另外我在排查过程中发现 `GunItemRenderer.cacheMuzzlePosition()` 也漏了同样的 `mulLocal(getModelViewMatrix())` 那行（枪口位置给曳光弹用的），它属于同一类移植遗漏，但和本次黑镜问题无关，我没有动它——如果需要，可以另行修。

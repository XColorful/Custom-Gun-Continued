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

1. 筒镜/红点镜的ocular开镜前后均为黑色（模型内默认），正常应该是开镜后能看见的
2. 在枪械装有配件/热键栏有配件的情况下，将背包任意物品丢到创造模式背包里的“摧毁物品”，配件就会只剩下ocular和ocular ring被渲染
3. 在装配界面（GunRefitScreen），打开配件界面进度，模型移动进度到100%但稍微再过一下的时间，肉眼观测枪械配件会快速闪没1或2次，该状况稳定触发
4. 有不明原因的OOM（疑似渲染造成的内存泄漏）问题待排查，当时记得可能在执行开镜渲染，电脑卡住了几秒，然后游戏崩溃，日志在游戏目录下hs_err_pid27136.log
优先处理前三个问题，除非顺手发现并修复，否则内存泄漏问题可能需要我之后单独开一个对话专门测试日志

本轮对话是接着上一次（/docs/deprecated/prompt/single/#36-render-issue-3.md，第771行到885行）剩余问题继续完成
- 目前git暂存区有一些log修改
- 筒镜问题找出来一个AttachmentDisplay tag没读上的问题（已经commit）
- 将enableScope/enableSight写入模型的部分尚未commit（因为引入了新问题待排查）

在提交了AttachmentDisplay tag的问题后，测试情况出现了变化，现在的代码下编译出来测试情况如下：
- 进游戏时已经手持SLR，装备3倍镜，多次打开装配界面，从创造模式tab拿物品（跟之前流程一样），摧毁物品
- 倍镜本来应该渲染在导轨上，但是渲染在了正下方（枪械握把左右）位置，开镜后，ocular ring本该渲染的位置，变成了直接透明，ocular ring遮挡住的地方直接能看到枪械后面，然后黑色的ocular变到了正上方的位置
- 我多次打开了装配界面，仍然会有闪没问题，但是显然不是在刚进游戏的时候执行的，开头的not gun可能只是客户端数据同步延迟，可以考虑忽略
- 这次摧毁物品没有导致倍镜只剩ocular和ocular ring被渲染（但是如上面提到，位置不对）
- 本次没触发OOM崩溃，可能是时间不够长，但是如上提到，该问题在本次对话不优先处理
该测试日志如下：
[25Aug2026 11:26:26.773] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ServerMessageSyncGunPack: reload AssetsInstanceManager: 74.1284 ms
[25Aug2026 11:26:26.787] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/SYNC_BASE_TIMESTAMP]: Update Client base timestamp: 1787628383990
[25Aug2026 11:26:26.789] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/cgc_sync_base_timestamp]: Update server base timestamp: 1787628386788
[25Aug2026 11:26:26.849] [Render thread/INFO] [net.minecraft.advancements.AdvancementList/]: Loaded 50 advancements
[25Aug2026 11:26:27.067] [Render thread/WARN] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] onRenderHand: not a gun, item=block.minecraft.air, empty=true
[25Aug2026 11:26:27.096] [Render thread/WARN] [dev.xcolorful.customgun.CustomGun/]: Failed to get soundPath from soundLocation null
[25Aug2026 11:26:27.224] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:27.704] [Render thread/WARN] [dev.xcolorful.customgun.CustomGun/]: Failed to get soundPath from soundLocation null
[25Aug2026 11:26:29.231] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:31.234] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:33.238] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:33.623] [Render thread/DEBUG] [io.netty.util.internal.ThreadLocalRandom/]: -Dio.netty.initialSeedUniquifier: 0x20d00ab8a72bdc77
[25Aug2026 11:26:35.245] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:37.252] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:39.257] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:41.261] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:43.263] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:45.263] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:47.266] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:49.268] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:51.275] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:53.276] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:55.281] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [CGC-debug] attachmentRender: enableScope=true, enableSight=false, scopeBodyPath=true, firstPerson=true
[25Aug2026 11:26:57.226] [Server thread/INFO] [net.minecraft.client.server.IntegratedServer/]: Saving and pausing game...

测试枪械为cbrg:slr，模型文件位于（如有需要可以查询）：
- 游戏根目录文件夹（1.20.1test）已添加到当前对话可用的folder里
- tacz版：游戏目录\tacz\classic_battleroyale_gun\assets\cbrg\geo_models\guns\slr_geo.json
- CGC版：游戏目录resourcepacks\classic_battleroyale_gun\assets\cbrg\geo_models\guns\slr_geo.json
- CGC版数据包目录位于游戏目录\saves\新的世界\datapacks

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

I'll start by understanding the current state — what's modified, what the architecture docs say, and where the relevant rendering code lives.

Let me see the actual uncommitted changes and the migration mapping.

Let me look at the key classes involved and the scope rendering architecture.

Let me look at where the TaCZ source lives and the relevant architecture docs.

`D:\Github\TACZ` is an additional working directory — likely the TaCZ source. Let me verify.

Now let me read the CGC `AttachmentModelObject` and `ClientAttachmentIndexInstance` to understand how these paths and flags are populated.

The committed fix reads `scope`/`sight` from JSON. Now the uncommitted change sets `enableScope` on the model — which flips the scope from the "render as normal model" path into the stencil `renderScope` path, exposing latent bugs. Let me compare the `renderTempPart`/`renderModelPart` transform logic — I spotted a double-transform difference.

I've confirmed the core issue. Let me verify the `getPath`/`modelMap_get` null behavior and look at the SLR scope model structure.

I now have a clear picture. Let me confirm the three bugs in `_AttachmentModelRender.java` and apply the fixes. The critical one is a **double-transform bug**: `renderModelPart` applies the last part's transform in the loop _and_ `BedrockPart.render` applies it again internally — TaCZ's `renderTempPart` loops `i < path.size() - 1`.

Now remove the debug log scaffolding (the `[CGC-debug] attachmentRender` log, the `CustomGun` import, and `lastDebugLogTime` field) since the root cause is found.

Now let me investigate problem #3 (the refit-screen flash). Let me find the refit screen and message flow.

Let me verify my edits are correct by reading the modified sections of `_AttachmentModelRender.java`.

Let me verify the changes compile. I'll check for file problems via the IDE.

Now let me compare the gun-side scope rendering (TaCZ `BedrockGunModel` vs CGC `_GunModelRender`) to check the stencil setup for the flash.

I've completed the scope fixes. Now let me remove the remaining diagnostic logs (the `GunItemRenderer` and `FirstPersonRender` debug scaffolding), since the scope root cause is found and these rate-limited logs didn't capture the flash anyway.

All files compile cleanly. Let me run a full build to be thorough.

Build succeeded — only pre-existing deprecation warnings, no errors. My changes are complete and compile cleanly.

## 调查报告与修改结果

### 1. 问题原因

当前测试暴露的「倍镜位置错乱 / ocular ring 透明 / 黑色 ocular 跑到正上方」回归，根因是一个 **移植时引入的双重变换 bug**，而不是 tag 修复本身有错。

提交 `0b341051` 修好了 `scope`/`sight` tag 没读上的问题，`enableScope` 变成了 `true`。这让倍镜从「当普通模型渲染」切换到「走模板（stencil）渲染」的 `renderScope` 分支。而这条 stencil 分支依赖的 `renderModelPart` 存在双重变换错误，之前因为 `enableScope=false` 从未走到这条路径，所以一直是潜伏的。

### 2. TaCZ 原始实现

TaCZ 的 `BedrockAttachmentModel.renderTempPart`（渲染 scope_body / ocular / ocular_ring 等单个节点路径）是这样的：

```java
for (int i = 0; i < path.size() - 1; ++i) {
    path.get(i).translateAndRotateAndScale(poseStack);
}
BedrockPart part = path.get(path.size() - 1);
part.render(...);   // part.render 内部会再对自身做一次 translateAndRotateAndScale
```

即：**循环只对「除最后一个节点外」的所有节点施加变换**，最后一个节点的变换由 `part.render()` 内部自带的 `translateAndRotateAndScale` 施加一次。每个节点变换恰好应用一次。

### 3. CGC 移植情况（差异）

CGC 的 `_AttachmentModelRender.renderModelPart` 把循环写成了对**所有**节点施加变换，然后又调用 `part.render()`（其内部同样会对自身再施加一次变换）：

```java
for (int i = 0; i < path.size(); i++) {   // 多了最后一个节点
    path.get(i).translate_rotate_scale(poseStack);
}
BedrockPart part = path.get(path.size() - 1);
part.render(...);   // 内部再施加一次 -> 最后一个节点变换被应用了两次
```

所以 scope_body / ocular / ocular_ring 这些**路径末端节点**的平移、旋转、缩放全部被翻倍。3 倍镜 `scope_body` 的 pivot 是 `[0, -8.4375, 3.375]`，双重变换后额外向下偏移约 0.53 格、向前 0.21 格 —— 正好对应你看到的「渲染在导轨正下方（握把位置）」。同理 `ocular_1` 被额外抬高，`ocular_ring` 也因位置错乱导致模板测试 `GL_EQUAL 0` 失效而变透明。

对比过程中还发现另外两处同文件内的移植遗漏：

- **非第一人称分支**把 `scopeBodyPath` 渲染了两次，第二处本应是 `ocularRingPath`（TaCZ 是先 `scopeBodyPath` 再 `ocularRingPath`）。
- **`renderOcularStencil` 的早退判断**写成了 `ocularRingPath.isEmpty()`，既会在没有 `ocular_ring` 节点时 NPE（红点镜就没有这个节点），也判断错了对象（应判断有没有目镜节点，即 `divisionOcularEntries`）。

### 4. 修改内容

只改了 `_AttachmentModelRender.java` 三处（另把上一轮遗留的三处 `[CGC-debug]` 临时日志清理掉了，`GunItemRenderer`/`FirstPersonRender` 已还原回提交状态）：

1. `renderModelPart` 循环改为 `i < path.size() - 1`，与 TaCZ `renderTempPart` 对齐，消除末端节点双重变换。
2. 非第一人称分支第二处 `renderModelPart` 由 `scopeBodyPath` 改为 `ocularRingPath`。
3. `renderOcularStencil` 的守卫由 `ocularRingPath.isEmpty()` 改为 `divisionOcularEntries.isEmpty()`。

`ClientAttachmentIndexInstance.java` 里把 `enableScope`/`enableSight` 同步到模型的改动保持原样（这是 ocular 变黑根因的正确修复，是本次问题 1 的必要部分）。

### 5. 验证情况

- `_AttachmentModelRender.java` 等 4 个相关文件经 IDEA 检查无错误；整项目 build 通过（`isSuccess: true`，仅有既有的 deprecation 警告）。
- 静态对比已确认三处修复与 TaCZ 语义一致。

**关于问题 2（摧毁物品只剩 ocular/ocular ring）**：你本轮测试已确认不再复现，之前的结论（与问题 1 同根因）成立，可视为随 tag 修复一并解决。

**关于问题 3（装配界面闪没）**：我排查了 `RefitScreenTransformState`、`GunRendererAddon`、`AttachmentRender`、`_GunModelRender`，均与 TaCZ 等价，未找到确定根因。你本轮日志里闪没期间**没有** `not a gun` / `GunDisplayInstance null` 输出，说明不是主手物品瞬时变空。我的判断是：当前 `enableScope=true` 下，闪没很可能是双重变换导致 stencil 几何错位、`GL_EQUAL 0` 的镜身测试在改装动画移动过程中间歇性通过/失败造成的闪烁 —— 若是如此，本次双重变换修复可能一并消除。**建议先测试本轮改动**；若闪没仍在，再针对它单独排查（需要改成状态变化触发、而非按秒限频的日志）。

### 下一步（需要你测试）

1. 重新编译进游戏，手持 SLR 装 3 倍镜，确认：倍镜回到导轨正确位置、开镜后 ocular ring 正常遮挡、镜内能看到放大画面。
2. 开镜前后确认红点镜（`sight` 类，无 `ocular_ring`）不再崩溃、不再黑屏。
3. 多次打开装配界面，观察闪没是否消失。

改动未 commit，工作区状态已保留，等你在游戏内验证后我再根据结果继续。

# 

```
# CGC / TaCZ 渲染问题调查与修改

## 一、固定上下文

CGC 已经基本完成 TaCZ 渲染体系的移植。

当前需要针对一个具体问题，调查 TaCZ 原始实现与 CGC 移植实现之间的关系，判断 CGC 是否完整、正确地移植了 TaCZ 对应机制，并在确认问题原因后直接修改 CGC 代码。

本次任务涉及两个代码体系：

* TaCZ：原始模组，用于确认原始设计和实现。
* CGC：TaCZ 渲染体系的移植目标，也是最终需要修改的代码。

TaCZ 类迁移到 CGC 后的位置、类名变化以及对应关系，可以通过：

`/docs-tacz/TaCZ Migration Mapping.md`

进行定位。

不要因为 CGC 中已经存在对应实现，就默认其行为与 TaCZ 一致。

需要以 TaCZ 原始源码和 CGC 当前源码分别进行调查，再对两者进行对比。

## 二、已有架构文档

之前已经专门整理过 CGC 和 TaCZ 的渲染体系文档，可以优先利用这些文档快速建立上下文、定位相关模块和减少无意义的代码搜索。

### CGC 渲染体系

`/docs/architecture/client/render/*.md`

CGC 总体架构：

`/docs/architecture/Home.md`

### TaCZ 渲染体系

`/docs-tacz/architecture/client/render/*.md`

TaCZ 总体架构：

`/docs-tacz/architecture/Home.md`

这些文档的主要用途是：

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

下面的问题描述是本次任务的具体输入：

### 问题

1. 枪械渲染时几乎固定在机瞄视角左右，手臂模型仍然保持blockbench模型文件里的位置（两只手在枪中间左右，手朝上），而不是动画指定的位置，需要检查手部渲染
2. 枪械、配件、子弹的GUI渲染都是子黑块
3. 第三人称渲染枪械没有触发dev.xcolorful.customgun.client.renderer.item.GunItemRenderer.renderByItem开头的log

以下为已测试内容：
- 枪械模型和贴图都已加载
- 转动镜头，枪会随镜头晃动
- 跳起后落地，枪会正常晃动
- 从空手切到枪之后，枪立即显示并保持大概机瞄视野的位置
- 机瞄状态下开镜后枪械的位置，跟使用TaCZ版渲染的对比为像素级对齐，仅手部渲染位置差异
- 枪械、配件、子弹的tooltip，除了draw Item显示子黑块以外，其余文本均正常，且tooltip绘制的内容固定为目前代码里绘制的，不要增减

测试枪械为cbrg:slr，模型文件位于（如有需要可以查询）：
- 游戏根目录文件夹（1.20.1test）已添加到当前对话可用的folder里
- tacz版：游戏目录\tacz\classic_battleroyale_gun\assets\cbrg\geo_models\guns\slr_geo.json
- CGC版：游戏目录resourcepacks\classic_battleroyale_gun\assets\cbrg\geo_models\guns\slr_geo.json
- CGC版数据包目录位于游戏目录\saves\新的世界\datapacks

可以自定决定三个问题的解决顺序
- 第三个问题我推测可能跟IClientItemExtension的注册有关，需要你检查现在注入到ForgeGunItem的重载方式，是否能实现跟原先相同的custom renderer getter并自动渲染，可以考虑移到forgeclient里的GunItemMixin来注入这个重载；列出这个说明仅供辅助完成任务，不一定就是问题的原因，以实际渲染时的代码调用为准

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

## 七、需要回答的核心问题

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

最终结论必须建立在实际代码调查的基础上。

## 八、修改要求

在完成 TaCZ 与 CGC 的对比并确认问题原因后，直接修改 CGC 代码。

要求：

* 修改应以恢复 TaCZ 原本行为为主要目标；
* 只修改与当前问题相关的部分；
* 不要顺手重构无关代码；
* 不要修改无关的注释或 Javadoc；
* 不要为了“代码更漂亮”而扩大修改范围；
* 如果存在多个可行方案，优先选择与 TaCZ 原始实现语义和执行时机最接近的方案；
* 如果 TaCZ 本身存在特殊的历史实现细节，需要判断其是否是当前行为所必需，而不是机械复制代码；
* 修改后检查相关调用链，确认不会明显破坏其他渲染场景。

如果问题最终确认并非 CGC 的移植缺失，而是 CGC 本身新增代码、Minecraft 行为差异或其他原因导致，也需要明确说明原因，并根据实际情况进行必要修改。

## 九、验证要求

修改完成后，需要尽可能进行验证。

优先：

* 检查编译错误；
* 检查修改代码的调用关系；
* 检查相关状态和渲染链路；
* 对比修改前后的行为逻辑；
* 如果项目提供了适合的测试，则执行相关测试。

如果无法实际进入游戏验证，需要明确说明哪些内容已经通过代码验证，哪些内容无法通过当前环境验证。

不要为了宣布完成而声称已经进行了实际上没有进行的游戏内验证。

## 十、输出要求

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

说明进行了哪些验证，以及是否存在无法验证的部分。

不要输出大量源码。

重点描述机制、调用链、差异和修改原因。

## 十一、Git 要求

完成修改和验证后：

**不要 commit。**

不要创建 commit，不要修改已有 commit，不要执行任何形式的提交操作。

保持修改后的工作区状态，最后通知我检查。

## 十二、执行原则

不要在没有调查 TaCZ 原始实现之前直接修改 CGC。

不要仅凭问题描述猜测原因。

不要只搜索与问题描述完全相同的类名或方法名。

应该：

> 先从现象确定涉及的渲染阶段 → 阅读相关架构文档 → 通过迁移映射定位 TaCZ → 调查 TaCZ 原始调用链 → 调查 CGC 对应调用链 → 对比状态、时机、变换和调用关系 → 确认根因 → 修改 CGC → 验证 → 汇报。

调查过程中，如果发现问题实际涉及架构文档中没有明确说明的模块，应继续调查该模块，而不是因为它不在当前文档中就停止。

现在开始调查并解决上面的问题。
```

# 

I'll start by establishing context from the architecture documentation and migration mapping, then investigate each of the three issues systematically.

Now let me read the render framework documentation for both projects.

I now have a solid architectural understanding. Let me locate the actual source files for the three problem areas.

Found a likely root cause for issue #3 already. Let me read the remaining CGC interface files and the item renderer base, plus verify the TaCZ source location.

Now let me read the remaining CGC client files and locate the TaCZ source for comparison.

# 
> 余额没了

```
继续
```

# 

I've completed an extensive investigation. Let me now implement the confirmed fixes. First, the item model JSONs (the root cause of issues #2 and #3):

Now let me fix the broken `getCustomRenderer()` cast. First, add the `initializeClient` override to the three mixins:

Now remove the broken `initializeClient`/`registerClientExtension` from the three Forge items:

Now let me verify the changes compile. Let me check the modified files for problems using the IDE:

Let me run a build to verify compilation:

The Gradle compile succeeded (exit code 0, no errors). Let me check the final git status to confirm my changes:

I've completed my investigation and implemented the fixes. Let me verify my final mixin changes are correct by reading one of them back:

我已完成调查和修改。以下是完整汇报。

## 1. 问题原因

三个问题中，**第 2、3 个问题共享同一个根因**，第 1 个问题独立。

### 问题 2（GUI 黑块）与问题 3（第三人称不触发 `renderByItem`）

**根因：CGC 完全缺失枪械/配件/子弹/弹药盒的 vanilla 物品模型 JSON。**

Forge 的 `ItemRenderer.render`（`net.minecraft.client.renderer.entity.ItemRenderer`）只有在 `BakedModel.isCustomRenderer()` 为 `true` 时，才会调用 `IClientItemExtensions.getCustomRenderer().renderByItem(...)`。而这个标志由物品模型决定——TaCZ 的物品模型用 `"parent": "builtin/entity"` 使 `isCustomRenderer()` 返回 `true`。

CGC 的 `core/src/main/resources` 下**没有任何 `models/item/*.json`**。游戏日志明确显示：

```
Unable to load model: 'customgun:gun#inventory' ... FileNotFoundException: customgun:models/item/gun.json
Unable to load model: 'customgun:attachment#inventory' ... FileNotFoundException: customgun:models/item/attachment.json
Unable to load model: 'customgun:ammo#inventory' ... FileNotFoundException: customgun:models/item/ammo.json
Unable to load model: 'customgun:ammo_box#inventory' ... FileNotFoundException: customgun:models/item/ammo_box.json
```

因此 `isCustomRenderer()` 为 `false`，`renderByItem` 从不被调用：

- GUI 走 vanilla missing-model 渲染 → 黑块。
- 第三人称同样不进入 BEWLR → 没有 `renderByItem` 日志。

**附带问题：** `ForgeGunItem`/`ForgeAttachmentItem`/`ForgeAmmoItem` 里的 `getCustomRenderer()` 写法是错误的——它写的是 `((IItemBEWLR) (Object) this).cgc$getBEWLR()`，这里的 `this` 是匿名 `IClientItemExtensions` 实例，**不是**实现 `IItemBEWLR` 的物品实例，一旦模型补上、BEWLR 被调度就会 `ClassCastException`。这是问题 3 里你推测的 `IClientItemExtension` 注册缺陷，确认属实。

### 问题 1（机瞄下手部停在 blockbench 默认位置）

我沿「状态机 → 动画解析 → 监听器 → 模型加载 → 功能渲染器绑定 → 手臂渲染 → 第一人称渲染」整条链路逐一比对了 TaCZ 与 CGC，**这条链路是逐行等价的**：

- `HandRender` ≡ TaCZ `LeftHandRender`/`RightHandRender`（矩阵捕获、delegate 渲染、180° 翻转完全一致）；
- 动画监听器（`ModelTranslateListener`/`ModelRotateListener`）、关键帧解析（含 `static_idle` 的静态数组写法）、`CompositeInterpolator`、`AnimStateMachine`/`LuaAnimStateMachine`、`DiscreteTrackArray` 全部一致；
- `lefthand_pos`/`righthand_pos` 是 `lefthand`/`righthand` 的子骨骼，而 `static_idle` 动画（位于 slr 动画文件内）正是驱动 `lefthand`/`righthand` 到握持位置，且该动画**确实被加载并播放**（BASE_TRACK LOOP）。

我在静态分析中**没有找到能解释手部位置差异的代码差异**。我找到的唯一真实差异是：CGC 的 `GunDisplayInstance.loadAnimController()` 有 `TODO 将默认动画填入动画控制器?`，即**没有加载 `use_default_animation: "rifle"` 对应的默认动画**（`idle`/`walk`/`run` 等，只驱动 `root` 骨骼）。这解释了「枪械几乎固定在机瞄视角」（缺少待机/移动晃动），但**不能**解释手部位置——因为手部只由 slr 文件自带的 `static_idle` 驱动，而它已加载。

因此问题 1 我做了完整验证但**未能通过静态代码比对定位到确定根因**，下面如实说明。

## 2. TaCZ 原始实现

**物品渲染器注册**（问题 2/3）：`AbstractGunItem.initializeClient` 直接 `consumer.accept(new IClientItemExtensions(){... getCustomRenderer() { return new GunItemRendererWrapper(); }})`，且 mod 资源里有 `models/item/modern_kinetic_gun.json` 等 `"parent": "builtin/entity"` 模型。这样 `isCustomRenderer()==true`，GUI/第三人称都会进入 `renderByItem`。

**手部渲染**（问题 1）：第一人称手臂不是独立渲染，而是挂在 `lefthand_pos`/`righthand_pos` 节点上的功能渲染器 `LeftHandRender`/`RightHandRender`，在节点处继承枪械全部变换（含 `static_idle` 驱动的 `lefthand`/`righthand` 骨骼动画），经 `RenderHelper.renderFirstPersonArm` 调用 `PlayerRenderer.renderLeftHand/renderRightHand` 画手臂。

## 3. CGC 移植情况

- 物品渲染器注册从「`AbstractGunItem.initializeClient` 直接 new 渲染器」被改成「ForgeGunItem + `registerClientExtension` + 类型强转」，且强转对象错误 → 问题 3 的注册缺陷。
- 手部渲染与动画链路整体等价，未发现移植差异。

## 4. 修改内容

**（1）新增 4 个物品模型 JSON**（问题 2/3 的根因修复）：

- `core/src/main/resources/assets/customgun/models/item/gun.json`
- `.../attachment.json`
- `.../ammo.json`
- `.../ammo_box.json`

内容均为 `{"parent": "builtin/entity", "gui_light": "front"}`，与 TaCZ 一致。

**（2）把 `initializeClient` 重载移到 forgeclient 的 Mixin**（问题 3 的注册缺陷修复，采纳你的建议）：

- `GunItemMixin`、`AttachmentItemMixin`、`AmmoItemMixin` 各自新增 `initializeClient`，其中 `getCustomRenderer()` 正确调用 `XxxItemMixin.this.cgc$getBEWLR()`（此时 `this` 才是实现 `IItemBEWLR` 的物品实例）。
- 删除 `ForgeGunItem`/`ForgeAttachmentItem`/`ForgeAmmoItem` 里错误的 `initializeClient` + `registerClientExtension`（它们会遮蔽 Mixin 版本；`registerClientExtension` 无外部调用者）。

修改后，物品注册 → `builtin/entity` 模型使 `isCustomRenderer()` 为 true → `getCustomRenderer()` 返回正确的 BEWLR → GUI/第三人称都进入 `renderByItem`，恢复 TaCZ 行为。

## 5. 验证情况

- **编译验证**：`./gradlew :forge-compat:compileJava` 编译通过（exit 0，仅有无害的 Java native-access 警告）；IDEA 对各改动文件 `get_file_problems` 均无错误。
- **调用链验证**：确认 `initializeClient` 由 Forge `Item.initClient()` 调用、`ForgeModItems` 将 `GunItem→ForgeGunItem` 等映射、`IClientItemExtensions.of(stack).getCustomRenderer()` 在 `ItemRenderer.render` 中被 `isCustomRenderer()` 门控；新增 JSON 的 registry name 与日志中缺失的 4 个路径完全对应。
- **未做游戏内验证**：我无法进入游戏实际观察。问题 2/3 的根因有日志硬证据（FileNotFoundException）支撑，修复方向明确；但**问题 1（手部位置）未能通过代码比对定位到确定根因**，也未做游戏内验证，需要你在实机确认。

**关于问题 1 的建议**：手部渲染与动画链路已确认与 TaCZ 等价、`static_idle` 已加载，但实机仍停在 blockbench 默认位置。若需要我继续排查，最值得进一步用运行期手段确认的是：① 第一人称下 `base_track_state.entry` 的 `runAnimation("static_idle", ...)` 是否真的执行（可在 `AnimController.run` 里临时打日志确认 `static_idle` 是否命中 prototype）；② `lefthand`/`righthand` 骨骼的 `offsetX/Y/Z` 与 `additionalQuaternion` 在渲染时是否被写入非零值。这两点能区分「状态机未初始化」还是「监听器未生效」。

未提交任何 commit，工作区保持现状待你检查。

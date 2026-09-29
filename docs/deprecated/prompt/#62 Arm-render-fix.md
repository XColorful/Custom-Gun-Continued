> #62 提示词

# 

```
解释下dev.xcolorful.customgun.client.mixin.model.PlayerModelMixin里清除默认的手臂旋转具体体现在哪？对应游戏里什么操作？怎么查看这个效果？
```

# 

I'll read the mixin file to see the arm rotation logic.

Now let me look at the related renderer and getter to understand when `ageInTicks == 0`.

Let me compare against the original TaCZ source to confirm the intent.

IDEA MCP works. Let me read the vanilla first-person arm rendering path.

Found the exact trigger. Let me check whether CGC renders the vanilla arm itself.

The `PlayerModelMixin` is currently modified in the working tree — let me check the diff.

Let me check for any other callers that could reach that path.

## 1. "清除默认的手臂旋转" 在代码里的落点

只有两处：

- [PlayerModelMixin.java:33-49](core/src/main/java/dev/xcolorful/customgun/client/mixin/model/PlayerModelMixin.java:33) — 注入 `PlayerModel.setupAnim(...)` 的 **TAIL**，也就是原版把手臂姿态算完之后再"擦掉"。
- [PlayerModelMixin.java:55-59](core/src/main/java/dev/xcolorful/customgun/client/mixin/model/PlayerModelMixin.java:55) — `cgc$resetRotation` 把 `xRot/yRot/zRot` 归零（javadoc 说"和旋转点"，其实只改了旋转），然后 `copyFrom` 把袖子跟手臂重新对齐。

被清掉的"默认旋转"来自 `HumanoidModel.setupAnim` 给手臂算出来的原版姿态，例如 [HumanoidModel.java:242-245](file:///C:/Users/XColo/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.23_mapped_official_1.20.1/forge-1.20.1-47.4.23_mapped_official_1.20.1-sources.jar!/net/minecraft/client/model/HumanoidModel.java) 里 `ArmPose.ITEM` 的 `rightArm.xRot = xRot * 0.5F - π/10`，再加上 `AnimationUtils.bobModelPart` 的行走摆动。

## 2. 对应游戏里的哪次操作

`ageInTicks == 0F` 这个判据就是"第一人称渲染手臂"。1.20.1 原版全代码里唯一以全零参数调用 `setupAnim` 的地方是 [PlayerRenderer.java:184-190](file:///C:/Users/XColo/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.23_mapped_official_1.20.1/forge-1.20.1-47.4.23_mapped_official_1.20.1-sources.jar!/net/minecraft/client/renderer/entity/player/PlayerRenderer.java)：

```java
playermodel.setupAnim(p_117779_, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
```

它只被 `renderRightHand`/`renderLeftHand` 调用，而原版只从 `ItemInHandRenderer.renderPlayerArm`（`renderArmWithItem` 里主手为空时）和 `renderMapHand` 调这两个方法。所以触发它就是"第一人称视角下渲染玩家手臂的那一帧"。

旁边的 [HumanoidModelMixin.java:32-34](core/src/main/java/dev/xcolorful/customgun/client/mixin/model/HumanoidModelMixin.java:32) 用同一个 0 判据做反面（第一人称那次直接 return，不跑第三人称手臂动画），可以互相印证这个标记的含义。TaCZ 侧对应的就是我之前对比出的 [RenderHelper.java:73-89](D:/Github/TACZ/src/main/java/com/tacz/guns/util/RenderHelper.java:73)：枪械渲染时手动 `renderer.renderRightHand(...)`，于是这一次 `setupAnim` 带着零参数走进来，mixin 才生效。

## 3. 怎么查看

- 游戏里：第一人称手持带 `righthand_pos` 节点的枪，看手臂。
- 可靠办法：在 [PlayerModelMixin.java:44](core/src/main/java/dev/xcolorful/customgun/client/mixin/model/PlayerModelMixin.java:44) 下条件断点 `ageInTicks == 0F`，或加一行 log，跑一次客户端。

不过按源码读下来，**这个注入点在当前 1.20.1 分支上应该已经打不中了**，理由是一条闭环：

- 条件里的 `cgc$getCurrentItem()` 除了 keep 窗口外返回的就是 `ItemInHandRenderer.mainHandItem`（[ItemInHandRendererMixin.java:91-105](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/ItemInHandRendererMixin.java:91)）。
- 而原版只有 `mainHandItem` **为空**时才会走 `renderArmWithItem` 的 `renderPlayerArm` 分支（[ItemInHandRenderer.java:364-367](file:///C:/Users/XColo/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.23_mapped_official_1.20.1/forge-1.20.1-47.4.23_mapped_official_1.20.1-sources.jar!/net/minecraft/client/renderer/ItemInHandRenderer.java)）——也就是说 `ageInTicks == 0` 那一趟只有在 `mainHandItem` 为空时才会发生。
- 两者唯一可能不一致的窗口是 keep：keep 期间 `cgc$getCurrentItem()` 返回被 keep 的物品，但 `cgc$keep` 和 `cgc$cancelEquippedProgress` 同时也把 `mainHandItem` 强制设成同一个物品（[ItemInHandRendererMixin.java:60-64, 87](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/ItemInHandRendererMixin.java:60)），非空 → 走"有物品"分支，根渲染不到手臂。
- CGC 自己画第一人称手臂走的是另一条路：`righthand_pos`/`lefthand_pos` 节点 → [HandRender.java:32-50](core/src/main/java/dev/xcolorful/customgun/client/renderer/model/HandRender.java:32) → [ClientRenderHelper.renderFirstPersonArm](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:141)，那里用 `arm.resetPose()` + `zRot = ±0.1F` 自己就完成了归零，不经过 `PlayerRenderer.renderHand`。（顺带：`±0.1` 正好等于 `bobModelPart` 在 `ageInTicks=0` 时的贡献，说明这条路径是把原版第一人称手臂姿态重写了一遍——和当年 mixin 干的是同一件事，只是搬了家。）

所以：TaCZ 里它是活的，移植过来把手臂渲染改成"直接 reset 再画"之后它就悬空了；你今天把条件从 `IGunGetter` 放宽到 `IAnimationItemGetter` 只影响判定范围，不改变可达性。

要确认这个结论，最省事的是上面那个条件断点跑一次客户端——如果一次都不命中，就可以连同 [customgunclient.mixins.json:11](core/src/main/resources/customgunclient.mixins.json:11) 的注册一起删掉；如果命中了（比如我漏了某条调用链），那它仍然是必要的。要我加个临时日志并跑一次客户端验证吗？

# 

```
加了日志确实没触发，你说明一下CGC是在哪里重复了一遍操作所以导致这里没用处？在javadoc里补充说明
```

# 

好问题，我去 TaCZ 源码里查一下它的第一人称手臂渲染路径和 keep 逻辑。

TaCZ 的 `cancelEquippedProgress` 整段是被注释掉的 —— 这可能是关键。再确认两处细节。

两处都对上了。先说结论，再改 javadoc。

## 1. TaCZ 还触发吗？——触发，而且是主路径每帧触发

TaCZ 的第一人称手臂**经过原版的** `PlayerRenderer.renderHand`，这正是零参数 `setupAnim` 的来源：

- `BedrockGunModel.java:79` 把 `RightHandRender` 挂到 `righthand_pos` 节点
- `RightHandRender.java:36` → `RenderHelper.renderFirstPersonArm` → `RenderHelper.java:86` `renderer.renderRightHand(...)`
- → 原版 `PlayerRenderer.renderHand` → `setupAnim(entity, 0,0,0,0,0)` → TAIL 命中

此时 `getCurrentItem()` 返回 `mainHandItem`，而 TaCZ 手持枪时它就是枪 → `IGun` 非空 → 条件成立、清零执行。

还有第二条路：TaCZ 的 `ItemInHandRendererMixin.cancelEquippedProgress` **整段（41-59 行）是注释掉的**，所以不强制 `mainHandItem`。收枪时 `keep()` 只写了 `mainHandItem`，`mainHandHeight` 仍由原版每 tick 衰减 0.4，约 3 tick 后 `< 0.1` → `mainHandItem = 实际手物品`（可能为空）→ 原版手臂路径走通，而 `getCurrentItem()` 还在 keep 窗口内返回那把枪 → 同样命中。

## 2. CGC 的改法功能一致吗？——主体一致，有三处差异

清零这件事被搬到了渲染调用点（[ClientRenderHelper.java:141-146](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:141)），思路等价，但：

| | TaCZ | CGC |
|---|---|---|
| 触发路径 | TaCZ 自己调 `renderRightHand` | 直接 `resetPose()` 后画 ModelPart，`PlayerModelMixin` 悬空 |
| `zRot` | `0`（mixin 三项全归零） | `±0.1F`（≈5.7°，等于 `bobModelPart` 在 `ageInTicks=0` 的贡献） |
| 袖子层 | 走原版 `renderHand`，手臂 + 袖子各画一次（袖子是 `entityTranslucent` 第二层） | 只 `arm.render(...)`，袖子仅被赋值 `visible`，**从未 render**（[ClientRenderHelper.java:143-144](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:143)） |

1、2 是设计选择：CGC 保留 `±0.1` 等于恢复了原版静止姿态里那一丁点外翻，TaCZ 是把它抹平的；两者相差 5.7° 滚转，肉眼基本看不出。
3 更像移植遗漏：玩家皮肤若开了 jacket/袖子层，CGC 第一人称手臂上会少掉那层，跟 TaCZ 和原版都不一致。另外 CGC 手臂用的是 `entityTranslucent`，而原版/TaCZ 手臂是 `entitySolid`——这点我不确定是有意为之，建议你自己判断。

## javadoc 已更新

你原来那句把原因归给 `cgc$cancelEquippedProgress` 强制 `mainHandItem`——那只是**次因**（只解释了"原版那条路"为什么不成立）。主因是 TaCZ 自己调 `renderRightHand` 的那条路径被 CGC 换掉了，`renderRightHand` 现在没有调用者。我把主/次两条都写进 [PlayerModelMixin.java:31-59](core/src/main/java/dev/xcolorful/customgun/client/mixin/model/PlayerModelMixin.java:31) 了，你那段原文的意思保留在次因条目里。

要我顺手把袖子那层的渲染补上（在对齐 CGC 与 TaCZ 的前提下），还是先把悬空的 `PlayerModelMixin` 连同 mixin json 注册清掉？按约定我没有提交，改动留在工作区。

# 

```
TaCZ之前是直接写在TaCZ里的，它这个cancelEquippedProgress的注释commit写的是捆绑到SBM里实现，看迁移到SBM之前是怎么渲染的？
是否直接强制在ClientRenderHelper里渲染一定会保留手臂渲染偏移？
补上袖子的渲染和修改render type是否需要破坏已有ClientRenderHelper结构？
```

# 

我去查 TaCZ 的 git 历史，同时看 SBM 里有没有接管手臂渲染。

SBM 里留着一份同样的 mixin —— 关键。再看两处。

SBM 的 `PlayerModelMixin` 还在，而且它把 `resetPose()` 用上了 —— 再看一眼 SBM 里接替 keep 的那个 handler。

三点都查清了。核心发现：**迁移只是把这三层搬进了 SBM，没有换渲染方式**——所以那个 mixin 在 SBM 里仍然是活的、必需的。

## 1. 迁移到 SBM 之前 / 之后，手臂都是怎么渲染的

提交就是 [TaCZ `34db94f3` "迁移部分实现至sbm"](D:/Github/TACZ)（2025-12-26）。三层结构在迁移前后完全一致，只是宿主换了：

| | 迁移前（TaCZ 内） | 迁移后（SBM） |
|---|---|---|
| 收枪保持 | `AnimateGeoItemRenderer.tryExit` → `KeepingItemRenderer.keep(...)`；`ItemInHandRendererMixin.cancelEquippedProgress` 强制 `mainHandHeight=1 / mainHandItem=keepItem` | `FirstPersonRenderHandler`：per-hand instance 生命周期 + `shouldLockVanilla(hand)` / `getTargetHeight(hand)`；TaCZ 侧 `keep(...)` 与 `cancelEquippedProgress` 整段被注释 |
| 手臂渲染 | `RightHandRender`（挂在 `righthand_pos`）→ `RenderHelper.renderFirstPersonArm` → **`renderer.renderRightHand`** → 原版 `PlayerRenderer.renderHand`（`setupAnim(entity,0,0,0,0,0)`） | 同左，`RenderHelper` 搬进 SBM，**仍然调 `renderRightHand`** |
| 姿态归零 | TaCZ 自己的 `PlayerModelMixin`，条件 = `getCurrentItem()` 是 IGun，手法 = 手写把 `xRot/yRot/zRot` 置 0 | SBM 的 [PlayerModelMixin](../../SimpleBedrockModel/src/main/java/com/github/mcmodderanchor/simplebedrockmodel/v1/mixin/client/PlayerModelMixin.java:31)，条件 = `getActiveAnimationInstance(hand).shouldRenderHand()`，手法换成 **`part.resetPose()`** |

所以答案是：**SBM 里这条注入依然命中**，因为它照旧经过 `renderRightHand` → `setupAnim(0,…)`。CGC 是唯一一个把 `renderRightHand` 换成"自己 `resetPose()` 再 `arm.render()`"的实现——这正是 CGC 这个 mixin 悬空的原因，跟 SBM 迁移无关。

顺带修正我之前 javadoc 的框架：SBM 的 `resetPose()` 说明 CGC 的 [ClientRenderHelper.java:141](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:141) 实质是**把 SBM 那个 mixin 内联到了调用点**，再自己补了 `±0.1F`。而"次因"（`cgc$cancelEquippedProgress`）在 SBM 里已经被 `FirstPersonRenderHandler` 整体取代，属于 CGC 保留了 TaCZ 旧机制、SBM 换掉了，二者是两条并行的差异。

## 2. 直接强制在 ClientRenderHelper 里渲染，会保留手臂偏移吗？

**在当前 1.20.1 上会**，但这是"当前等价"而非结构保证：

- 偏移的来源是调用方：`HandRender`/`RightHandRender` 用 geo 节点路径 + `ZP 180°` + delegate 捕获的 normal/pose 建栈，两边是同一份代码。`PlayerRenderer.renderHand` 自己**不加任何矩阵**（那个 `scale(0.9375F)` 在 `LivingEntityRenderer.render` 里，`renderHand` 不经那条路）。
- 手臂自身的 x/y/z：`resetPose()` 恢复 baked pose（右臂 `-5,2,0`），与 `setupAnim(0,0,0,0,0)` 在 `crouching=false` 下算出的完全一致（`HumanoidModel` L102-105 / L180-181）→ 一致。
- 不保留的只有旋转：CGC 多 0.1 的 `zRot`；TaCZ 顺带的 `attackTime/crouching/swimAmount` 归零 CGC 无所谓（它不跑 `setupAnim`，反而更不容易被别的 pass 污染）。

风险点是：一旦换模型/加铠甲手臂、或 SBM 改动 `resetPose` 语义，这个等价就不自动成立，需要重新核对。想稳妥的话，可以把它写成注释挂在方法上。

## 3. 补袖子 + 改 render type 要不要动 ClientRenderHelper 结构？

不需要，都是就地增量，但有两处小前提：

- **袖子**：在同一个 `ARCompat.setRenderingLevel()/resetRenderingLevel()` 块里，按原版 `renderHand` 的顺序 `arm.render(...)` → `sleeve.render(...)` 即可；`model.leftSleeve/rightSleeve` 这个块已经碰过（只赋了 `visible`）。顺手可修一个现存问题：现在两只袖子都被设成"这一只手"的 `isSleeveVisible`，原版 `setModelProperties` 是按各自的手判断的。
- **render type**：继续走 `ClientRenderUtils.RenderType_` 兼容层就行，但那个类里**目前没有 `entitySolid`**（只有 `entityCutout` / `entityTranslucent` / `entityTranslucentCull` / `itemEntityTranslucentCull` / `energySwirl`）。要在里面补一个带版本分支的包装（1.20.1 直调 `RenderType.entitySolid`，新版留 `RenderTypes.entitySolid` 注释行）——这正是该文件既有的写法，属于沿用抽象而非改造结构。
- 另外两个小尾巴：[ClientRenderHelper.java:129](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:129) 的 `bakedRenderType` 算出来从未使用；袖子作为第二条 RenderType，提交/结束时机要仍在模板测试窗口内（就是注释里"1.21.10 手穿透镜片"那个坑）。

---

两个待你定的点：

1. javadoc 你重排后，`<ul>` 里的"主因"/"次因"是裸文本节点（[PlayerModelMixin.java:35](core/src/main/java/dev/xcolorful/customgun/client/mixin/model/PlayerModelMixin.java:35)、[:39](core/src/main/java/dev/xcolorful/customgun/client/mixin/model/PlayerModelMixin.java:39)），javadoc 会报错；要不要我按 SBM 的实际情况重写一版（把"主因 = CGC 换了 `renderRightHand`，SBM/TaCZ 都还是活的"讲清楚，"次因"降为附注）？
2. 袖子那层要不要我现在就补上（含 `RenderType_.entitySolid` 包装）？

按约定没有提交任何改动。

# 

```
在ClientRenderHelper补上袖子渲染，没用到的不要删
以及，如果注释掉cgc$cancelEquippedProgress是否就手臂旋转正常？SBM里是否有设置mainHandItem？如果去掉的话是否会出现keep期间mainHandItem跟TaCZ预期不一致？
```

# 

先确认两件事：`ModelPart.render` 是否吃 `visible`，以及 SBM 里是谁在动 `mainHandItem`。

SBM **不设置** `mainHandItem`（只钉 height + 改 `matches`/`reequip` 语义）。先把袖子补上。

袖子已补上。现在确认 `mainHandItem` 在 CGC 里的下游读者。

袖子补完了。下面是三个问题的结论。

## 改动（[ClientRenderHelper.java:118-154](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:118)）

在同一个 `ARCompat` 块里加了 `sleeve` 的定位与渲染：`sleeve.copyFrom(arm)` 紧跟在 `arm.resetPose()` + `zRot = ±0.1F` 之后，然后和手臂共用同一个 `VertexConsumer` 依次 `render`。三点说明：

- `copyFrom` 是必需的一步，不是可选优化：`ModelPart.render` 吃自己的 `visible` 和旋转（`ModelPart.java:114`），而袖子在别的 pass 里可能残留着上一帧 `setupAnim` 的旋转。原版 `PlayerModel#setupAnim` 末尾就是 `rightSleeve.copyFrom(rightArm)`，TaCZ 的 mixin 归零后也要补一次 `copyFrom`，所以这里是同一语义。
- 没动 `sleeve.visible` 的现有两行：虽然它们把两只袖子都设成"这一只手"的可见性，但本次只渲染这一只手的袖子，取到的正好是它自己的标记，结果是对的。
- 渲染类型没动，袖子沿用 `entityTranslucent`（这本来就是原版给袖子用的类型）。`bakedRenderType` 按你说的保留。

## (a) 注释掉 `cgc$cancelEquippedProgress` 手臂旋转会正常吗？——不会，代价还更大

注释掉后注入**确实会重新命中**（keep 窗口内实际手持物已变、`mainHandItem` 衰减回落为空时），但它掰直的是**原版画的那只空手手臂**；你第一人称看到的那只是 `ClientRenderHelper.renderFirstPersonArm` 画的，早就 `resetPose()` 过。所以"旋转正常"根本不依赖这个注入。

而代价是实打实的：`mainHandItem` 不再被钉住，keep 期间它会被 vanilla 的 `mainHandHeight < 0.1F → mainHandItem = itemstack` 换掉。这不只影响手臂——见 (c)。

## (b) SBM 里设置 `mainHandItem` 吗？——不直接设置，但效果等价

SBM 的 [ItemInHandRendererMixin](../../SimpleBedrockModel/src/main/java/com/github/mcmodderanchor/simplebedrockmodel/v1/mixin/client/ItemInHandRendererMixin.java:74) 完全不碰 `mainHandItem`，只做两件事：

1. `@Inject(tick, TAIL)` 在收枪过渡期间把 `mainHandHeight/oMainHandHeight` 钉成 1.0；
2. 两个 `WrapOperation` 把 `ItemStack.matches` 和 `shouldCauseReequipAnimation` 换成 NBT 容忍语义。

推论下来 `mainHandItem` 依旧保持是枪：TAIL 每次把 height 拉回 1.0 → vanilla 的 `if (mainHandHeight < 0.1F) mainHandItem = itemstack` 永不触发；`matches` 分支因 NBT 容忍返回 false 不赋值；`requipM` 为 true 也不赋值。所以 SBM 用"钉高度"间接达到了 CGC 直接赋值的效果，**两者的预期是一致的**。

（一个细微差别：SBM 钉在 TAIL，渲染时读到的是 1.0；CGC 钉在 HEAD，vanilla 的衰减仍在同一 tick 内跑到 0.6。对枪没有可见影响，但如果以后要靠原版手臂/物品的升降偏移，这个差值要留意。）

## (c) 去掉的话，keep 期间 `mainHandItem` 会跟预期不一致吗？——会，而且看得见

关键在下游：vanilla `renderHandsWithItems` 把 `this.mainHandItem` 直接传给 Forge 的 `renderSpecificFirstPersonHand`（`ItemInHandRenderer.java:316-317`），CGC 的 [ForgeRenderHandEvent.getItemStack()](forge-compat/src/main/java/dev/xcolorful/customgun/forgeclient/event/ForgeRenderHandEvent.java:68) 就是它，而 [FirstPersonRender.java:78](core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:78) 正是用这个 stack 去拿枪并渲染。

所以 `mainHandItem` 一回落：

- 收枪动画那一帧 `event.getItemStack()` 变成空手/新物品 → `IGunGetter` 为 null → 直接 return，**枪的收枪动画掉帧甚至整段消失**；
- 同时 vanilla 的 `renderArmWithItem` 不再是"有物品"分支，会开始按升降进度画原版手臂/物品，和 CGC 自己的手臂叠在一起。

补充一点：`cgc$getCurrentItem()` 本身不受影响（keep 窗口内直接返回 `cgc$keepItem`），所以 `GunCameraHelper`、`_LocalAnimHandler`、`GunRendererAddon` 那些读它做相机/动画判断的地方照常。受影响的是"走 `mainHandItem` 这条路"的渲染。

**结论**：该注释掉的是 `PlayerModelMixin` 的注入体，不是 `cgc$cancelEquippedProgress`——后者是活的、必需的。

未验证项：我没有编译也没有跑客户端，袖子那两行是按原版 `renderHand` 的顺序写的，建议你实际看一眼（尤其是开了 jacket 层的皮肤 + 开镜时手臂是否还在模板测试窗口内）。要我跑一次 `./gradlew :core:compileJava` 确认编译吗？

# 

```
那改成跟SBM一样，不设置mainHandItem只设置1.0+修改注入位置来修复手臂渲染问题是否可行？
```

# 

这个提议的方向对，但要看 CGC 有哪些下游依赖 `mainHandItem` / 装备进度。我确认三处。

一个实现前提要确认：CGC 能不能用 `@WrapOperation`（MixinExtras）。

后台 grep 太慢，改用 Grep 工具。

可行，而且比现状干净。但关键点是**得三件一起搬**——只做"不设 `mainHandItem` + 钉 1.0 + 改 TAIL"会留下一个空档。

## 1. "只钉 1.0" 确实能替代 `mainHandItem = keepItem`（那两行赋值是冗余的）

keep 期间 vanilla `tick()` 里三条会改写缓存堆的路径全被挡住：

| vanilla 代码 | keep 期间为什么不会改写 |
|---|---|
| `if (ItemStack.matches(cached, held)) cached = held;` | 期间 held 已换成别的物品/空手，`matches` = false |
| `if (!requipM && cached != held) cached = held;` | Forge 默认判定"物品不同" → `requipM` = true，跳过 |
| `if (mainHandHeight < 0.1F) cached = held;` | height 被钉在 1.0，永不成立 |

所以 `mainHandItem` 自然停在"最后持有的那把枪"上，Forge `RenderHandEvent` 携带的还是枪 → 收枪动画照常渲染。**这一半你的判断是对的**，直接赋值可以删。

顺带能修掉一个隐患：`cgc$keep` 不区分左右手，无条件写 `mainHandItem`（[ItemInHandRendererMixin.java:87](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/ItemInHandRendererMixin.java:87)）。如果副手也会走 keep 路径，主手的缓存堆就会被换成副手那把枪（这点需要你确认副手是否真的会触发 keep，我没验证）。

## 2. 但还差"持枪常态"这半档，必须靠两个 WrapOperation 补

你现在 HEAD 里还有第二个分支：只要主手是 `IGun` 就把 height 钉成 1.0（[ItemInHandRendererMixin.java:67-73](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/ItemInHandRendererMixin.java:67)）。它同时承担了两件事：压掉原版切枪抬手动画、每 tick 用当前手持堆刷新缓存。SBM 是用两个 `WrapOperation` 达到同样效果的（`matches` 与 `shouldCauseReequipAnimation` 都换成 NBT 容忍语义）。

只搬"钉 1.0 + TAIL"而不搬这两个 wrap，持枪常态会退回原版语义：height 每 tick 掉 0.4、约 3 tick 后 `< 0.1`，接着因为 `oldStack == newStack`（同一引用）`requipM` 变回 false、height 又爬回 1.0——高度在 0↔1 之间来回抖。今天 CGC 里 `getEquipProgress()` 只有定义处、没有消费者（我 grep 过），枪也不由原版画，所以多半看不见；但这是个脆弱点，将来要用装备进度或让原版手臂露出来就会现形。

## 3. HEAD → TAIL 该改，而且是和 wrap 配套的

HEAD 钉的值会被同一 tick 里 vanilla 的衰减覆盖成 0.6；TAIL 才是渲染时的最终值（SBM 的注释也写了为什么 TAIL 安全：`matches`/`requip` 的副作用已经被 WrapOperation 修正）。副作用是：改成 TAIL 后就不能再"先赋值 `mainHandItem` 再让 vanilla 走"了——这正好是要删那两行赋值的原因，两处改动是绑定的。

## 4. 它修不了 `PlayerModelMixin`

这个方案的收益是 `mainHandItem` 语义回归 vanilla/SBM、收枪动画不再依赖"偷换缓存堆"。手臂旋转依然由 `ClientRenderHelper.renderFirstPersonArm` 的 `resetPose()` 负责，那个注入照旧是死的。两件事别混着看，否则会以为改完这个手臂就"正常"了。

## 实现前提：都满足

- CGC 已经能用 MixinExtras（`gradle.properties` 里配了 `mixin_extras_common/forge`，`MouseHandlerMixin`、`LocalPlayerMixin`、`ServerGamePacketListenerImplMixin` 已经在用 `@WrapOperation`），照 SBM 搬没有依赖问题。
- 只需要给 `KeepingItemRenderer` 加一个探针，`cgc$isKeeping()`（`cgc$keepItem != null && now - ts < keepTimeMs`），现成字段就能写；不需要引入 SBM 那套 instance/transitioning 机制。

## 两个附带缺口

- SBM 是按手分别钉的，CGC 的 `cgc$keepItem` 只有一份（主手语义），副手的 `offHandHeight`/`offHandItem` 现在完全没碰。副手如果要收枪动画，这是缺口。
- 同时装 SBM 时两个 mixin 都 inject `tick` 的 TAIL（SBM priority = 2000），两边都钉 1.0 结果一致、不冲突，但优先级这件事要知道。

要我把这个改法落成一份 diff 吗（含 `cgc$isKeeping()` + 两个 `@WrapOperation` + TAIL 钉高度，不动 `PlayerModelMixin`）？另外袖子那两行我还没编译验证过，可以一起跑一次 `:core:compileJava`。

# 

```
袖子渲染已经正常
不用考虑SBM的双手单独计算，TaCZ旧版本实现只需要单手，双手是SBM后来加的应该可以去掉
现在先把手臂渲染修改给完成，我看看效果
```

# 

收到。先把实现所需的信息一次查清：`IAnimationItem` 的语义、`core` 里能不能直接用 Forge 类、以及另一处 `KeepingItemRenderer` 实现。

`AnimateGeoItemRenderer:423` 里已经有一个等价判定 —— 先看它。

`袖子渲染已经正常` 收到 —— 但这次我得先停下来说一个前提问题，因为它会让"只钉 1.0"这个改法在 CGC 上直接坏掉。

## 根因：CGC 和 SBM 的渲染源不是同一个东西

- CGC 的第一人称枪是从**事件的物品**取的：[FirstPersonRender.java:78](core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:78) `ItemStack gunItem = event.getItemStack();`，而事件里的 stack 就是 vanilla 的 `mainHandItem` 缓存（`renderHandsWithItems` 把它传给 `renderSpecificFirstPersonHand`，[ItemInHandRenderer.java:316-317](file:///C:/Users/XColo/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.23_mapped_official_1.20.1/forge-1.20.1-47.4.23_mapped_official_1.20.1-sources.jar!/net/minecraft/client/renderer/ItemInHandRenderer.java)）。
- SBM 的第一人称枪是从**instance 取的**：[FirstPersonRenderHandler.java:342-348](../../SimpleBedrockModel/src/main/java/com/github/mcmodderanchor/simplebedrockmodel/v1/client/handler/FirstPersonRenderHandler.java:342) 用 `inst.currentItem()`，根本不看事件里的 stack。

所以 SBM 可以完全不碰 `mainHandItem`（它只钉 height + 换 `matches`/`reequip` 语义），而 CGC 不行——`mainHandItem` 就是它的渲染源。

## "只钉 1.0"会坏在哪：切到枪时枪不渲染

场景：拿着剑 → 切到枪。vanilla `tick()` 里能刷新 `mainHandItem` 的三条路全被堵：

| 刷新路径 | 剑→枪时 |
|---|---|
| `matches(cached=剑, held=枪)` | false（换了物品） |
| `!requipM` | `requipM` = true（换了物品），跳过 |
| `mainHandHeight < 0.1F` | 高度被钉在 1.0，永不成立 |

结果：`mainHandItem` 一直停在刀上 → 事件携带的是刀 → `IGunGetter` 为 null → 枪在第一人称不出现；`cgc$getCurrentItem()` 的下游（`GunCameraHelper`、`_LocalAnimHandler`、`GunRendererAddon`）也全都拿不到枪。这不是边角情况，是每次切枪都会命中。

顺带一个可能出乎意料的点：在 CGC 里**钉 height 本身几乎没有可见作用**——事件在 `renderArmWithItem` 之前就把 poseStack 交出去了，装备进度不进 CGC 的枪械姿态，而 vanilla 画枪又被跳过（`renderByItem` 对 firstPerson 直接 return）。也就是说 `cgc$cancelEquippedProgress` 里真正起作用的是那句 `mainHandItem = ...`，钉 height 基本是摆设。你提的"只留 1.0"恰好留下了不起作用的那半。

## 两条路，我推荐第二条

- **(A) 保留赋值**，只做 HEAD→TAIL + wrap：功能≈现状，等于白改（wrap 也被那句赋值覆盖了）。
- **(B) 对齐 SBM 的根因**：让渲染不再依赖 vanilla 缓存，然后赋值、钉高度都可以拆掉。改动三处：
    1. `KeepingItemRenderer.cgc$getCurrentItem()`：实现从 `mainHandItem` 改成「keep 中 → `cgc$keepItem`，否则 → `player.getMainHandItem()`」（契约本来就是"当前主手正在渲染的物品"，这样更贴切，`EMPTY`/26.3 占位同步）；
    2. `FirstPersonRender:78`：`event.getItemStack()` → `cgc$getCurrentItem()`；
    3. `ItemInHandRendererMixin`：`tick` 改 TAIL 只钉高度、加 `matches` 的 `@WrapOperation`（NBT 容忍，负责开火后弹药等刷新）、去掉两处赋值和 `@Shadow mainHandItem`（或者留着不用）。

(B) 才是"跟 SBM 一样"的真正等价物，(A) 只是把机制换个位置。

我没有在本地跑客户端的能力，(B) 动的是第一人称渲染源，所以想先跟你确认：**按 (B) 做吗？**你确认我就一次改完，然后你进游戏看这几点：切枪到枪是否立刻出现、收枪动画、开火后弹药/动画是否刷新、副手表现、空手时手臂是否正常。

# 

```
目前不修改的话只是手臂旋转差异？TaCZ将这部分移到SBM前是怎么写的？ClientRenderHelper里取消掉rot-=0.1是否可行？
```

# 

三个问题分开答。

## 1. 不动的话，剩下的差异只有手臂旋转吗？

基本是。把现在的 CGC 和 TaCZ/SBM 逐项对一下：

| 项 | TaCZ / SBM | CGC 现状 | 可见差异 |
|---|---|---|---|
| 手臂位置 (x/y/z) | `setupAnim(0,…)` 或 `resetPose()` → ∓5/2/0 | `arm.resetPose()` → 同 | 无 |
| 手臂 x/yRot | 0 | 0 | 无 |
| 手臂 **zRot** | 0 | **±0.1** | **有**，就是你说的这点 |
| 袖子层 | 渲染 | 本轮已补 | 无 |
| 归零由谁做 | `PlayerModelMixin`（活） | `ClientRenderHelper`（注入悬空） | 无，结果相同 |
| `mainHandItem` 归属 | vanilla 自持（SBM 靠钉 height 间接保持） | CGC 直接赋值 | 无（各自内部自洽） |

另外两点不算"差异"但要知道：

- 手臂的 render type：CGC 用 `entityTranslucent`，原版 `renderHand` 是 arm=`entitySolid` + sleeve=`entityTranslucent`。我不确定这在实际画面里是否看得出来（不在你的问题范围内，只是提醒）。
- 手在**模板测试窗口内**渲染是 CGC 有意为之（[ClientRenderHelper.java:134-141](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:134) 的注释：1.21.10 起 `SubmitNodeCollector` 会让手在模板关闭后才画，导致开镜穿透镜片）。这条别当成移植遗漏去"修"。

## 2. TaCZ 移到 SBM 之前是怎么写的

分三层，其中第一层就是 CGC 现在这份：

- **收枪保持**：`ItemInHandRendererMixin.cancelEquippedProgress`（`tick` HEAD）——把 `mainHandHeight/oMainHandHeight` 钉 1.0、把 `mainHandItem` 赋成 keep 的物品或当前手持的枪。**和 CGC 现在这份逐字一致**（我在 `34db94f3` 的 diff 里对比过，只差命名）。
- **手臂渲染**：`RightHandRender` / `LeftHandRender`（挂在 `righthand_pos` / `lefthand_pos` 节点）→ `RenderHelper.renderFirstPersonArm(...)` → `renderer.renderRightHand(matrixStack, buffer, combinedLight, player)`。**关键就是这一句经过原版** `PlayerRenderer.renderHand` → `setupAnim(entity,0,0,0,0,0)`。
- **归零**：`PlayerModelMixin` 在 `setupAnim` 的 TAIL 把 `rightArm/leftArm` 的 `xRot/yRot/zRot` 手写置 0，再 `copyFrom` 给袖子。

迁移后（SBM）：三层结构照搬，只换了宿主和手法——`cancelEquippedProgress` 整段注释掉、改由 `FirstPersonRenderHandler`（instance 生命周期 + 钉 height + NBT 容忍的 `matches`/`reequip`）接手；手臂渲染仍走 `renderRightHand`，所以那个 `PlayerModelMixin` 依旧活；归零改用 `part.resetPose()`。

也就是说：**CGC 让 mixin 悬空的原因不是移植哪一层写错了，而是把第二层换成了自己直接 `arm.render()`**，把第三层的活儿一并接管了。

## 3. 取消掉 `zRot = ±0.1F` 可行吗？

可行，而且就两行：[ClientRenderHelper.java:148-149](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:148)。删掉之后结果恰好是 TaCZ/SBM 的 0——因为 `arm.resetPose()` 已经把 baked 姿态（zRot=0）写回去了，这两行是唯一把 ±0.1 写回来的地方；`sleeve.copyFrom(arm)` 在其后执行，自动跟随为 0。无结构影响，不涉及任何兼容层。

要注意的是这个 ±0.1 的**来历**：它不

# 

```
继续说
```

# 

接上：这个 `±0.1` 的来历是**原版自己的值**。

`PlayerRenderer.renderHand` 用全零参数调 `setupAnim`，于是 `HumanoidModel.setupAnim` 末尾的 `AnimationUtils.bobModelPart(rightArm, ageInTicks, ±1.0)` 在 `ageInTicks == 0` 时贡献 `±(cos(0)*0.05 + 0.05) = ±0.1`——所以：

| | 手臂 zRot |
|---|---|
| 原版（不装任何模组） | `±0.1` |
| TaCZ / SBM（mixin 归零） | `0` |
| CGC（硬编码，[ClientRenderHelper.java:148-149](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:148)） | `±0.1` |

所以这不是"移植漏了"，而是一个二选一：**保留 = 跟原版一致，删掉 = 跟 TaCZ/SBM 一致**。0.1 rad ≈ 5.7° 滚转，纯外形，TaCZ 用 0 用了很久也没问题。

考虑到你这轮工作的目标就是把 CGC 与 TaCZ/SBM 对齐，删掉更自洽；而且这两行是**唯一**决定该值的地方，将来如果真把 SBM 那套 `PlayerModelMixin` 搬回来也不会打架（CGC 自己渲染手臂时仍会走这里）。

要我现在就删掉这两行吗？删完手臂姿态就和 TaCZ/SBM 完全一致了，其余差异就只剩上面表格里那个 `entityTranslucent` vs `entitySolid`（那个我建议先不动，等你在游戏里确认看不看得出来）。

# 

```
删掉之后确实跟BlockBench里看的效果一致了
那目前这个状况不需要再修改，也就是跟TaCZ现在配套SBM的方案效果一致？如果增加扩展mod比如同样是IAnimationItem，是否会导致只有第一人称是IGun的时候才渲染？检查下是否还有强制绑定IGun而没有宽松到IAnimationItem的地方，比如TaCZ移到SBM里进行了更宽松的判定来支持不同mod都使用同样的自定义渲染？
本次回答结果主要是留档用，如果有需要像现在IGun -> IAnimationItem的可以一并改，目前CGC内IAnimationItem就只有IGun实现了所以无害
```

# 

TaCZ 迁移后确实把判定换成了"有自定义渲染器"。我先确认 CGC 侧对应的抽象，再逐个判该不该放宽。

两处代码结构完全一样（第一/第三人称），一并放宽。

改完了。四条回答如下。

## ① 现在跟 TaCZ+SBM 的方案等价了吗

手臂姿态这块**已经等价**（位置、x/yRot 一致，zRot 你刚删掉 ±0.1 后也是 0）。剩下三处差异，都不是"手臂旋转"：

1. **手臂的 render type**：CGC 用 `entityTranslucent`，原版/TaCZ 是 arm=`entitySolid` + sleeve=`entityTranslucent`。是否可见未验证。
2. **CGC 不复制原版 `setModelProperties` 的效果**：原版那条路会 `setAllVisible(true)` / 旁观者时 `setAllVisible(false)`，CGC 只手动设了 `arm.visible = true` + 袖子可见性。所以**旁观者模式下第一人称手臂可能仍会画出来**（原版/TaCZ 画不出来），这条我没实测，列为待确认。
3. **`mainHandItem` 的归属**：CGC 直接赋值，TaCZ HEAD/SBM 让它归 vanilla（SBM 靠钉 height 间接保持）。效果一致，但这是架构差异——也是上一轮说"不能只删赋值"的根源。

另外 `PlayerModelMixin` 悬空、CGC 的手臂在模板窗口内渲染（刻意的），这两条无可见差异，前面已述。

## ② 扩展 mod 加一个非枪的 `IAnimationItem`，会只有 IGun 才渲染吗？

**之前会**。第一人称的渲染入口 [FirstPersonRender.java:85](core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:85) 卡的是 `IGunGetter.fromItemStack(gunItem) == null → return`，非枪物品在第一人称不会走到渲染器。

这确实是 TaCZ 迁移到 SBM 时放宽过的地方——TaCZ 现在的判定是"**该物品有自定义渲染器**"：

| TaCZ 文件 | 判定 |
|---|---|
| `FirstPersonRenderEvent.java:48` | `getCustomRenderer() instanceof AnimateGeoItemRenderer` |
| `CameraSetupEvent.java:72,89` | 同上 |
| `TickAnimationEvent.java:60` | 同上 |
| `LocalPlayerDraw.java:81,91` | 同上 |
| `LocalPlayerInspect.java:29` | 同上 |
| `InventoryEvent.java:49` | `instanceof IAnimationItem` |

CGC 的对应抽象就是 `IAnimateGeoItem.cgc$getCustomRenderer(stack)`。

## ③ 审计 + 已做的改动

**已改**（和 `PlayerModelMixin` 那次同一种放宽；今天只有 IGun 实现 `IAnimationItem`，所以是行为中立的重构）：

- [FirstPersonRender.java:71](core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:71) 副手 pass 取消门：`IGunGetter` → `IAnimationItemGetter`
- [FirstPersonRender.java:85](core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:85) 第一人称主手渲染门：`IGun` → `IAnimationItem`（`switchItemNeedReset` 调用点同步；`IGun` 只剩 javadoc 引用）
- [_LocalAnimHandler.java:128](core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/player/_LocalAnimHandler.java:128) 第三人称自渲染门：同构代码，一并放宽

**与 TaCZ 一致、不要动**：

- `ItemInHandLayerMixin:53`（第三人称主手有枪时隐藏左手物品）——TaCZ 的对应处用的是 `IGun.mainHandHoldGun(...)`，同样是枪绑定。要放宽得先定义"非枪动画物品该不该遮副手"。
- `_LocalAnimHandler:68`（tick 路径）——后面直接读 `GunDisplayInstance` 的动画状态机，枪专有。

**真需要 IGun 语义、不该动**（读枪专有数据，不是渲染入口）：`GunCameraHelper`、`GunRendererAddon`、`_GunModelRender:57` + `_GunLoader:45,51,57`（`getAttachment`/`getBuiltinAttachment`）、`HumanoidOffhandRender:47,74`（数据来自 `GunDisplay.SurroundDisplay`）、`ShellRender:92`、`BeamRender:146`、全套 `LocalShooter*`/`*Key`、HUD/准心/改装界面、`_S2CMessage*`。

**候选（本次未动，供你决定）**：同样是"先 IGun 门、再 renderer 门"的形态，语义上可放宽但属于*动作*而非渲染入口——`LocalShooterInspect:39`、`LocalShooterDraw:66,68`。另外 `HumanoidOffhandRender` 里那两个 `IGun` 判断其实是冗余的（真正把关的是 `getGunDisplayInstance`），不阻碍扩展。

**一个建议**：副手是否被"霸占"，SBM 是 per-item 的 `IFPAnimationInstance.occupiesView()`，CGC 现在是"主手是动画物品就霸占"。将来扩展 mod 若希望自己的物品不霸占副手，就得照 SBM 做成 per-item。

## ④ 留档说明

- 本次改动纯类型放宽，未编译、未跑客户端；建议跑一次 `:core:compileJava`（我也没做，需要的话我来跑）。
- 上面 ① 的第 1、2 条是**待实测**项，不是结论。
- 我没有提交，改动都在工作区。

# 

```
对于原版setModelProperties，TaCZ是否有哪里手动设置？CGC是否漏了？
目前测试下持枪的时候旁观模式第一第三人称都看不见
```

# 

我需要确认原版在旁观者模式下到底跳过了什么。直接把 MC 源码解出来查。

决定性证据找到了。再确认一下 TaCZ 侧有没有对应的处理。

## 结论：旁观者模式看不见是原版挡的，和 `setModelProperties`、`arm.visible` 都无关

两处原版闸门（1.20.1 Forge sources）：

- **第一人称**：`GameRenderer.renderItemInHand` L855 ——
  ```java
  if (this.minecraft.options.getCameraType().isFirstPerson() && !flag && !this.minecraft.options.hideGui
          && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
      ... this.itemInHandRenderer.renderHandsWithItems(...);
  }
  ```
  旁观者模式下**整个 `renderHandsWithItems` 不执行** → Forge `RenderHandEvent` 不发 → CGC 的 `FirstPersonRender`、`ClientRenderHelper.renderFirstPersonArm` 根本不进，`arm.visible = true` 也没机会执行。
- **第三人称**：`LivingEntityRenderer.render` L129 ——
  ```java
  if (!p_115308_.isSpectator()) {
     for (RenderLayer<T, M> renderlayer : this.layers) { renderlayer.render(...); }
  }
  ```
  旁观者时**所有 layer 都不执行** → `ItemInHandLayer` 不跑 → 手上的枪不画；身体只按 `setModelProperties` 画成"只剩头 + 帽子"的幽灵形态。

所以"持枪 + 旁观，第一/第三人称都看不见"是纯原版行为。

## TaCZ 有没有手动设置 `setModelProperties`？——没有

在 TaCZ 的 `client/` + `mixin/client/` 里 grep `setAllVisible` **零命中**（只有一堆 `isSpectator` 在按键/HUD 里做判断）。TaCZ 的 `RenderHelper.renderFirstPersonArm` 调 `renderer.renderRightHand(...)`，`setModelProperties` 是原版 `PlayerRenderer.renderHand` 内部顺手做的，TaCZ 自己一行都没写。TaCZ 的第一人称事件处理（`FirstPersonRenderEvent.onRenderHand`，现已由 SBM 的 `FirstPersonRenderHandler.onRenderHand` 取代）也**没有** spectator 判断。

## CGC 是否漏了？——严格说没复刻，但不需要

| `setModelProperties` 干的事 | CGC 是否需要 |
|---|---|
| `setAllVisible(true)` + ArmPose 计算 | 不需要：CGC 只画 arm/sleeve，且姿态已被 `arm.resetPose()` 归零 |
| spectator 分支 `setAllVisible(false)`、只留 head/hat | **不可达**：第一人称整条路已被 `GameType.SPECTATOR` 挡掉 |
| 对共享 `PlayerModel` 可见性的写入 | 无残留：`PlayerRenderer.render` 每帧都会重新 `setModelProperties`，且 CGC 只强制 `arm.visible = true`，和原版非旁观分支同值 |

## 顺带两条留档（我都没改）

1. **隐身 vs 旁观**：真正与*原版*不一致的是隐身——原版空手 + 隐身时 `renderArmWithItem` 会跳过手臂，而 CGC/TaCZ 自己的手臂渲染都不检查 `isInvisible()`，隐身时会照画（枪也照画）。两侧一致，所以不算 CGC 的移植遗漏，是否对齐原版由你定。
2. **TaCZ 现在的入口门比 CGC 还宽**：`FirstPersonRenderEvent`（以及实际生效的 SBM `FirstPersonRenderHandler`）连 `IGun` 门都没有，只看 `getCustomRenderer() instanceof AnimateGeoItemRenderer`（SBM 版更细：还有 per-item 的 `canRenderInHand`）。CGC 现在是 `IAnimationItem` + renderer 双层门。要完全对齐，CGC 那层 `IAnimationItem` 还能再退一步——代价是 `switchItemNeedReset` 调用要换成 TaCZ 那种精确 `ItemStack.matches`（顺带一提：TaCZ 的 init 门用精确 `matches`，CGC 用 NBT 容忍的 `switchItemNeedReset` 取反，这是有意分歧，我没动）。

要的话我把 ① 的结论写成一段注释挂在 [ClientRenderHelper.renderFirstPersonArm](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:104) 上方，免得以后有人以为"没检查 spectator/invisible"是漏写。

# 

```
我测试了下，原版隐身的时候手持地图确实不渲染手臂，这个怎么实现的需要加上，加上后不影响枪械渲染，我看代码修改情况决定是否加config
```

# 

加好了，一行判断。

## 改动

[ClientRenderHelper.java:108-112](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:108)（`renderFirstPersonArm` 入口）：

```java
if (player == null) return;
// 对齐原版：隐身玩家不渲染第一人称手臂（原版见 ItemInHandRenderer#renderArmWithItem 与 #renderOneHandedMap 的 !isInvisible() 判断），
// 物品本身仍照常渲染——原版渲染物品的那条分支并不检查隐身
if (player.isInvisible()) return;
```

核对了原版的三处判断（都在 `ItemInHandRenderer`）：L365（空手 `renderPlayerArm`）、L154（单手持图）、L183（双手持图），用的都是 `!player.isInvisible()`。

放在这里的原因：

- 它是唯一的手臂渲染入口，`HandRender` 和 `_GunModelRender._renderHandInternal` 都从这里进，所以一处覆盖两条路径（含 26.x 的提交式路径）。
- 早于 `arm.resetPose()`/`arm.render()` 返回，所以**手臂和袖子一起跳过**——和原版一致（原版是包住整个 `renderPlayerArm` 调用，内部 arm+sleeve 两段一起不画）。
- **不影响枪械渲染**：枪由 `renderFirstPerson` 自己画，和这里无关，正是你要的效果。这也符合原版语义——原版对"有物品"的那条分支不检查隐身，物品照画，只有"手臂"会被跳过。

## 若要加 config，挂点就在这一行

```java
if (!SomeConfig.HIDE_ARM_WHEN_INVISIBLE.get() && player.isInvisible()) return;
```

放这一行能同时覆盖两个调用点；如果想让手臂渲染器保持"只负责画、不做策略"，那就把判断挪到两个调用点里（`HandRender.render` 开头 + `_GunModelRender._renderHandInternal` 开头）。

## 一条需要你权衡的留档

**TaCZ 和 SBM 都是"隐身也照画手臂"**——`RenderHelper.renderFirstPersonArm` 里没有任何 `isInvisible()` 判断。所以这个改动是**向原版对齐、与 TaCZ/SBM 分叉**。你之前的目标一直是 TaCZ 行为对齐，所以如果要加 config，我建议默认值就按"对齐原版（跳过）"，并在注释里写明这是与原版对齐而非与 TaCZ 对齐，免得以后有人照 TaCZ 对比时以为是 bug。

未编译、未跑客户端。要我跑一次 `:core:compileJava` 吗？

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

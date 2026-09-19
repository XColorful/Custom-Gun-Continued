> #54 提示词

# 

```
现在要在dev.xcolorful.customgun.client.animation.shooter.animator.DefaultShooterAnimator模拟一个原版弩蓄满时的手部动画，看看是否能实现
- 在非瞄准状态下，目前是不干预动画的，即手处于原版手持物品的动画状态
- 在瞄准状态下，想办法把他做到像弩蓄满时的姿态，这样枪械看起来就是举着的
- 如果有原版出处，需要补javadoc链接，以便于高版本移植的时候出现原版差异而方便比对
```

# 

I'll start by reading the relevant animation files and documentation to understand the existing design.

Let me check the docs and how this animation hook is invoked.

Now let me read the vanilla `HumanoidModel` crossbow pose code.

Let me find where vanilla decides to apply `CROSSBOW_HOLD`.

Let me settle where vanilla selects the arm pose by grepping the sources jar.

Let me check project conventions for the APIs I'd need.

I have the full vanilla picture. Let me implement it.

Now let me compile to verify.

Let me check the project's existing render-smoothing utility before deciding on the blend source.

实现完成，`core` 模块编译通过。

## 改动

[DefaultShooterAnimator.java:55](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:55) — 瞄准状态下按 `cgc$getSynAimingProgress()` 插值到原版弩蓄满姿态。

## 找到的原版出处（已写进 javadoc）

选姿态和算姿态在两个地方，都补了链接：

1. **谁选中这个姿态** — `PlayerRenderer#getArmPose`（`net.minecraft.client.renderer.entity.player`），条件是 `CookedItem` 已蓄满且玩家未挥手 → `HumanoidModel.ArmPose.CROSSBOW_HOLD`
2. **姿态数值在哪** — `AnimationUtils#animateCrossbowHold`（`net.minecraft.client.model`），`HumanoidModel#poseRightArm` / `#poseLeftArm` 的 `CROSSBOW_HOLD` 分支调用它。公式为：
    - 持弩手（主手）：`yRot = ∓0.3 + head.yRot`，`xRot = -PI/2 + head.xRot + 0.1`
    - 副手：`yRot = ±0.6 + head.yRot`，`xRot = -1.5 + head.xRot`

注：弩还有 `CROSSBOW_CHARGE`（拉弦中，`animateCrossbowCharge`），你描述的是"蓄满"，对应 `CROSSBOW_HOLD`，所以取的是后者。

## 三点需要你判断

**1. 插值精度受限于 20Hz 同步值**  
`AIMING_PROGRESS_KEY` 只在服务端 tick 里 `setValue`（LivingEntityMixin.java:58 在客户端直接 return），`SyncedDataKey` 也没有插值。所以瞄准进度对客户端是每 tick 跳一档，手臂姿态会跟着分十几次阶梯推进，而不是连续过渡。

本机玩家其实有平滑的 `ILocalShooter.cgc$getRenderAimingProgress(partialTicks)`，但 `animateShooter` 签名里没有 `partialTicks`。要改的话有两条路，都属于接口层面的决定，我没擅自动：

- 给 `IShooterAnimator` 加 `partialTicks` 参数，本机玩家走 `getRenderAimingProgress`；
- 或者在 animator 里加 per-entity 的 dynamics 平滑（现成的 `MathUtil.SecondOrderDynamics` 是静态单例，多实体渲染会串味，不能直接用）。

**2. 潜行时的偏差**  
原版 `setupAnim` 在姿态之后还会给双臂 `xRot += 0.4`（潜行补偿）。我这里是整体替换旋转，满瞄准时这个补偿会丢失。要不要跟齐原版，你定。

**3. 新引入的平台 API**  
用到了 `LivingEntity#getMainArm()` 和 `HumanoidArm`（后者项目里已在用，前者是首次）。用途是让左撇子玩家的持枪手对应原版的持弩手。按 CLAUDE.md 报一下，看是否需要 compat 包装。

## 未验证

只做了编译验证，**没有实际进游戏看效果**——第三人称瞄准姿态需要进世界、拿枪、按 F5 才能看到，我没法自动驱动。要起客户端的话：

```bash
./gradlew :forge-compat:runClient
```

我也可以帮你启动，但会占用你的屏幕。

# 

```
目前枪械跟手的相对位置是不变的，这导致弩的右手跟胸不垂直的话，枪看起来不是朝前的：
- 如果能在这里修改物品渲染的位置，就修改成跟弩一样的物品方向微调（原版弩看起来是往正前方的），否则改成弓箭蓄力（右手跟胸垂直，只是枪不在身体正前方）
- 20hz的动画看起来还行，如果不能每个实体单独partialTick就不要做更平滑的插值
- 潜行的补偿再描述具体一点，没看出来，尽量跟原版对齐，最好是直接调用原版public static方法（允许加AT配置改权限）
```

# 

让我先查清第三人称下枪械是怎么贴到手/骨骼上的。

Let me trace how the third-person gun render gets its entity/render context.

我先把姿态计算改成直接调用原版方法，并把潜行补偿对齐。

编译通过。`AnimationUtils.animateCrossbowHold` 是 `public static`，**不需要 AT**。

## 已改：姿态改为直接调用原版

[DefaultShooterAnimator.java:58](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:58) 现在直接调 `AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, mainArmIsRight)`，先存下原姿态再用 `Mth.lerp` 过渡，所以不再手抄 `-0.3 / 0.6 / -1.5` 这些常量，原版一变会自动跟随。

## 潜行补偿具体是什么

原版 `HumanoidModel#setupAnim` 在 `poseRightArm`/`poseLeftArm` **之后**还有一段：

```java
if (this.crouching) {
   this.body.xRot = 0.5F;
   this.rightArm.xRot += 0.4F;   // ← 就是这一行
   this.leftArm.xRot += 0.4F;
   this.rightLeg.z = 4.0F; ... this.head.y = 4.2F; ...
} else { ... }
```

之前看不错是因为**插值到满瞄准时整体覆盖了手臂旋转，把 vanilla 已经加上的 +0.4 一并冲掉了**——潜行状态下身体前倾 0.5 rad，手臂少了这 0.4 就会看起来前伸/插向身体。现在按 `livingShooter.isCrouching()` 补回去了（`body` 我们没碰，`body.xRot=0.5` 由原版照常生效）。

原版这段没有可复用的 static 方法，只能照抄这一行。同段里 `bobModelPart` 我们也没复现（需要 `ageInTicks`，接口给不了），差异约 0.05 rad，已写进 javadoc。

## 关于"这里能不能改物品渲染位置"

查清楚了：

- 枪在第三人称的**手→枪相对朝向是数据决定的**——Bedrock 模型里的 `thirdperson_hand` 节点，由 [GunItemRenderer.java:463](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/GunItemRenderer.java:463) `_applyPositioningNodeTransform` 反向应用。它就是枪版的"物品方向微调"，对应原版 `crossbow.json` 里的 `display.thirdperson_righthand.rotation`。**是资源包数据，不是代码**。
- 动画器（`DefaultShooterAnimator`）只拿得到 `ModelPart`，**碰不到物品渲染**。
- 但渲染侧可以：[GunItemRenderer.java:376](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/GunItemRenderer.java:376) `renderByItem` 拿不到实体；而上一层 [ItemInHandLayerMixin.java:38](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/ItemInHandLayerMixin.java:38) 的 `renderArmWithItem` 里实体、手臂、瞄准进度都齐了。

所以"能改"，但方式和代价有几种，得你定：

The user's hypothesis is worth verifying properly. Let me check whether a `display` transform on a `builtin/entity` item model actually reaches the BEWLR.

The user's hypothesis looks right. Let me confirm against vanilla's actual crossbow model.

确认了——你的判断对。先回答，再改姿态。

**`crossbow.json` 确实就是干这个的**，而且是资源包能用的机制：

```json
"display": {
    "thirdperson_righthand": {
        "rotation": [ -90, 0, -60 ],
        "translation": [ 2, 0.1, -3 ],
        "scale": [ 0.9, 0.9, 0.9 ]
    },
    ...
}
```

`builtin/entity` 在 `ModelBakery` 里解析成 `BLOCK_ENTITY_MARKER = {"gui_light": "side"}`——**没有 `display` 块**。所以给枪的 `models/item/*.json`（现在只有 `{"parent": "builtin/entity", "gui_light": "front"}`）加一个 `display.thirdperson_righthand`，子模型会补上这个变换。

关键是它**能生效**：`ItemRenderer#render` 里 `ForgeHooksClient.handleCameraTransforms(...)` 在 `isCustomRenderer()` 判断**之前**执行，所以 BEWLR 的 `renderByItem` 接到的 poseStack 已经带上了 display 变换。也就是说枪的 `thirdperson_hand` 节点不是唯一入口——它只是模组自己那套，外面还套着原版这层。

两个注意点：

- **静态**：display 变换不感知瞄准进度，会把不瞄准时的朝向也一起改。原版弩没这个问题只是因为弩只在蓄满时才进入那个姿态。
- 每个枪的物品模型各自继承 `builtin/entity`，所以得逐把枪（或让资源包统一继承一个中间模型）来加。

现在改姿态。

姿态已换成弓箭。改动在 [DefaultShooterAnimator.java:72](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:72)。

## 现在的实现

原版 `BOW_AND_ARROW` 分支没有 `AnimationUtils` 那样的公开工具方法，只能照抄 `HumanoidModel#poseRightArm` / `#poseLeftArm` 里的常量：

||弩 `CROSSBOW_HOLD`|弓 `BOW_AND_ARROW`|
|---|---|---|
|主手 yRot|`∓0.3 + head.yRot`|`∓0.1 + head.yRot`|
|副手 yRot|`±0.6 + head.yRot`|`±0.1 ± 0.4 + head.yRot`|
|双臂 xRot|主手 `-PI/2 + head.xRot + 0.1`，副手 `-1.5 + head.xRot`|双臂均 `-PI/2 + head.xRot`|

（`∓` 上号对应右手持械。左撇子取 `poseLeftArm` 那半，因为 `setupAnim` 里 `flag3 == flag2` 会走到 `poseLeftArm`。）

潜行补偿保留：`armXRot += 0.4F`，仍对应 `setupAnim` 的 `crouching` 分支。

## 关于 `crossbow.json` 的结论（已核实）

你的推断成立，而且**比 `thirdperson_hand` 更靠外**：

- `builtin/entity` 解析为 `BLOCK_ENTITY_MARKER = {"gui_light": "side"}`，**没有 `display` 块**，所以枪的 `models/item/*.json` 可以直接补一个。
- `ItemRenderer#render` 里 `ForgeHooksClient.handleCameraTransforms(...)` 在 `isCustomRenderer()` 分支**之前**执行 → BEWLR 拿到的 poseStack 已经带上了 display 变换。

所以哪天真要用回 `CROSSBOW_HOLD` 的举枪姿态，资源包侧加 `display.thirdperson_righthand` 就能把枪口掰回视线，和原版弩 `[-90, 0, -60]` 是同一套。唯一代价是它**静态生效**——不瞄准时朝向也会跟着变。

## 两点残留

- **未实机验证**。编译通过，但举枪姿态和 ~6° 残留偏差得进游戏 F5 看：`./gradlew :forge-compat:runClient`
- 顺带发现、**没有深究**：[ItemInHandLayerMixin.java:53](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/ItemInHandLayerMixin.java:53) 会取消 `arm == LEFT` 的渲染，而原版 `ItemInHandLayer` 对左撇子实体恰好把主手物品渲染在左臂上——左撇子玩家的第三人称枪械可能整个不渲染（也可能由 `HumanoidOffhandRender` 兜住）。和我这次改的 `getMainArm()` 分支有关，但不在本次范围内，先报给你。

# 

```
弓蓄力的动画看着可以。

静态始终生效是否是因为没有设置实体状态（用LivingShooter tick同步设置原版状态），或者是资源包根据弩物品本身的状态来定（用补充枪械物品状态的方式适配）？
- 如果可以的话，看看改用以上实现需要多大改动，是否可行

如果不可行的话，解决以下问题：
- 手持枪械，例如第三人称视角从前看自己，蹲下与否，镜头朝向应该是不变的，但是蹲下的时候原版拉弓的动画会把手往下压一点，想办法让手保持跟不蹲下时的朝向一致，这样看起来蹲下时还是枪朝前的，而不是放下枪
- 如果改用了前面的方案，也要看这个问题是否存在
```

# 

让我确认两件事：手臂是不是 body 的子节点，以及 `overrides`/`display` 在 custom renderer 路径上到底走不走。

两个关键事实查清了，先看结论再改代码。

两件事都查清了。

## 为什么是静态的——以及一个推翻我上一条的发现

`display` 在模型烘焙时一次性读进 `ItemTransforms`，之后没有任何原版代码会按帧改它。但关键是我又查了弩的其它状态：

```json
// crossbow_pulling_0.json
{ "parent": "item/crossbow", "textures": { "layer0": "item/crossbow_pulling_0" } }
// crossbow_arrow.json
{ "parent": "item/crossbow", "textures": { "layer0": "item/crossbow_arrow" } }
```

**原版弩自己也是静态的**——未蓄满 / 拉弦 / 蓄满三个状态只是换贴图，`display` 完全继承 `crossbow.json` 那一套。弩"看起来朝前"不是因为按状态调了 display，而是因为**那套 display 就是照着蓄满姿态调出来的**，同时兼顾未蓄满时的 `ITEM` 落手姿态。

所以你的两个猜测：

- **tick 同步设置原版状态** → 就算把状态同步过去，**没有原版消费者会读它改 display**。除非把枪伪装成 `CrossbowItem` 去 `startUsingItem`，那是滥用原版状态。不可取。
- **资源包按物品状态定（overrides）** → 方向对，**确实可行**，见下。

## 动态化的改动量

**路径 A — `overrides` + `ItemProperties`**：实体是一路传下来的，我验过链路：

`ItemInHandRenderer.renderItem(LivingEntity,…)` (L121) → `ItemRenderer.renderStatic(LivingEntity,…)` (L223) → `getModel(stack, level, entity, seed)` (L204) → `getOverrides().resolve(…, entity, …)` (L215)

所以模组只要注册一个谓词，资源包就能写 overrides 选不同 display：

```java
ItemProperties.register(gunItem, "customgun:aiming",
        (stack, level, entity, seed) -> ILivingShooterGetter
                .cgc$fromLivingEntity(entity).cgc$getSynAimingProgress());
```

模组侧约 10 行 + 每把枪 JSON 加 overrides。**但 predicate 是 float 阈值比较，只能做阶梯**——要平滑得写十几个模型，不现实。

**路径 B — 模组代码里加变换**：平滑（直接用 `cgc$getSynAimingProgress()`）、不依赖资源包、改动小；代价是补偿数值由模组定死。

**路径 C — 就静态调**：如果以后瞄准姿态固定下来，"照着固定姿态调一套 display"反而是最贴原版做法的，和弩一模一样。

## 潜行下压：已修，我上一轮理解错了

根因不在我以为的地方。`HumanoidModel.createMesh` 里 `head` / `body` / `right_arm` / `left_arm` 全都是 `addOrReplaceChild` 到 **root** 的**兄弟节点**——`body.xRot = 0.5` 的前倾**不会传导到手臂**。

也就是说原版那条 `rightArm.xRot += 0.4F` 根本不是"抵消躯干前倾"，它就是单纯把手臂再往下压 0.4 rad。对垂手自然，对举枪就是枪口下压 23°——正是你看到的现象。

去掉后 `arm.xRot = -PI/2 + head.xRot`，手臂世界俯仰恒等于「视线俯仰 − 90°」，与是否蹲下无关。手臂的 Y 位置仍会随蹲下降低（原版 `arm.y: 2.0 → 5.2`），那是整体下沉，方向不变。

改动在 [DefaultShooterAnimator.java:88](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:88)，javadoc 里记了原因。

**这个问题在所有方案下都存在**——路径 A/B 修的是物品相对手的偏航，不是手臂的俯仰。所以无论走哪条路，都得在姿态层修，现在这版已经修好了。

依旧**未实机验证**，编译通过。

# 

```
蹲下时的枪械朝向已经修复。

- 所有枪械都是用的customgun:gun，为什么要写十几个json？不能共用？
- ItemProperties是否需要每tick更新/切物品后是否会残留？
- 是否能平滑？只能20hz也行，实在不行就两个状态也行
```

# 

这三个问题都得查证，先看事实。

三个问题都查证了。

## 1. 共用 —— 对，一个 JSON 就够

枪确实是**一个物品**：`ModItems.GUN = ITEMS.register(ItemType.GUN.getTagName(), GunItem::new)`（[ModItems.java:31](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/init/registry/ModItems.java:31)），枪的身份带在 stack 里。所以 `overrides` 只写在 `customgun:gun` 这一个文件里，**所有枪共用**。

但"十几个 json"的由来是：`overrides` 的语义是**换整个模型**，而 `display` 是**模型级**的，不是物品级。所以每个不同的 display 状态仍需要一个模型文件——只是这些文件极短：

```json
// customgun:item/gun —— 唯一要写 overrides 的文件
{ "parent": "builtin/entity", "gui_light": "front",
  "overrides": [
    { "predicate": { "customgun:aiming": 0.34 }, "model": "customgun:item/gun_aim_2" },
    { "predicate": { "customgun:aiming": 0.67 }, "model": "customgun:item/gun_aim_3" }
  ]
}

// customgun:item/gun_aim_2 —— 每个就这几行
{ "parent": "customgun:item/gun",
  "display": { "thirdperson_righthand": { "rotation": [ ... ] } } }
```

所以是「1 个带 overrides + N 个只有 display 的小文件」，不是 N 个都要手写 overrides。

## 2. 不需要每 tick 更新，也没有残留

`ItemProperties.register` 只是往静态表里塞一个**函数**，不存值。`ItemOverrides.resolve` 在 `ItemRenderer.getModel`（每次渲染）里现算：

```java
afloat[j] = itempropertyfunction.call(stack, level, entity, seed);
```

- **无残留**：全程没有持久状态。切物品后渲染的是别的物品，谓词根本不会被调用；同一把枪停止瞄准 → 函数返回 0 → 阈值不满足 → 自动回落基模型。
- **注册时机也宽松**：属性是在 `resolve` 里用 `ItemProperties.getProperty(item, id)` **运行时查表**，不是烘焙时绑定，晚注册没事。
- **一个坑**：GUI / 掉落物 / 展示框里 `entity` 是 **null**（签名本就是 `@Nullable LivingEntity`），谓词必须判空。
- API 上：`register` 收的是 `ClampedItemPropertyFunction`，要实现 `unclampedCall`；`ItemPropertyFunction` 本身标了 `@Deprecated`。

## 3. 平滑——给不了连续

`resolve` 是「取第一个所有 predicate `f >= value` 都满足的」，阈值是 float `>=` 比较 → **N 级阶梯**，N 就等于你写了几个模型。想要"20Hz 那么细"，瞄准动画约 10-15 个 tick，就要 10-15 个文件；两个状态就是中途跳一次。

还有个容易踩的：`ItemOverrides` 构造时**倒序**遍历 overrides，`resolve` 取第一个匹配 → **JSON 里后面的条目优先级更高**，要按阈值从小到大写。

## 一个值得先确认的前提

路径 A **不是纯资源包**——`ItemProperties.register` 仍然是模组代码。所以它相对「模组代码里加个变换」多出来的，只有那 N 个 JSON 文件。

既然枪是**一个物品 + 一个渲染器**，用 overrides 换 N 个模型来模拟连续量，性价比不高。三条路对比：

||平滑|数据可调|新增文件|模组代码|
|---|---|---|---|---|
|A. overrides|否（N 级阶梯）|是|N 个|注册谓词|
|B. 代码里按进度插值|是|否（数值写死）|0|~10 行|
|C. display 数据加 `third_person` 旋转字段 + 代码按进度应用|是|是（逐枪）|0|字段 + ~10 行|

三条路都收敛到同一个注入点——`ItemInHandLayer.renderArmWithItem` 里实体和瞄准进度都在手上，只是 A 把数值交给了 JSON。

我的建议是 **C**：`_ModelTransformScale` 已经有 `third_person` 缩放这个同位字段，加一个 `third_person` 旋转是同构的，一个数据字段解决"逐枪可调"，应用放在代码里就能按进度平滑。要不要我按 C 做？

# 

```
- 这个动画是模组决定的，枪包资源包改不了用弓箭还是用别的动画，没得调，这个应该由模组控制，方案C否决
- 由于转向不需要很大，没什么必要平滑，实际可以谓词只判断两个状态间切换
- B的代码里按进度插值是否跟动画绑定？是否会进一步影响动画的实现？用A的话就能资源包手动调整状态？
- 留两套代码，一份是弓的姿态，一份是弩的姿态，之后再考虑往枪包里加动画选用哪种，弩更像是手枪用来举在正前方，大多数时候长枪用弓这个姿态就够；如果A会造成这两种配置下还需要额外调整的话，就不使用A
- 默认用弓的姿态，角度偏离不是很大，不是用弩的话没什么必要修正和插值

先对以上做报告
```

# 

先报告，不动代码。

## 已记录的决策

- **方案 C 否决**：姿态选择是模组层面的决定，枪包改不了，把数值下放到枪械 display 数据里是错位的。
- **不追求平滑**：A 用谓词在两状态间切换即可。
- **默认弓姿态**：6° 偏差接受，不做修正。

## B 的两个问题

**「B 的插值是否跟动画绑定」——是，而且是硬绑定。**

B 的修正量就是当前姿态给主手加的那份偏航：弓 `0.1`，弩 `0.3`。这个数**只存在于姿态定义处**，也就是 `DefaultShooterAnimator`。渲染层要拿到它只有两条路：

1. 写死一个常量 → 换姿态就悄悄失效。
2. 从手臂实时状态反推 `c = arm.yRot - head.yRot` → **不成立**。因为插值时  
    `arm.yRot = lerp(p, from, head.yRot + c)`，而 `from`（不瞄准时的落手姿态）是 0，不是 `head.yRot`，于是  
    `arm.yRot - head.yRot = (p-1)·head.yRot + p·c`  
    在 `p<1` 时多出 `(p-1)·head.yRot` 这个误差项，量级可达整个头部偏航角。只有 `p=1` 才对。

**「是否会进一步影响动画的实现」——会。**

而且影响得挺具体。第一种不行，就只剩第二条路：**让 animator 对外发布 `c`**。即 `IShooterAnimator` 要新增一个类似 `getMainArmItemYawOffset()` 的成员，渲染层从 `GunDisplayInstance → animator → c` 取。代价是：

- 接口多一个成员，每个 animator 实现都得给；
- 姿态不能再自由换——**任何用到的姿态都必须声明自己的偏航偏移**，否则渲染层修错方向；
- 渲染层（第三人称）要反向依赖动画层，两个模块从"平行"变成"渲染读动画状态"。

另外注意：修正只在第三人称 `ItemInHandLayer` 这条路上有意义；第一人称是独立路径（`GunItemRenderer` 自己的变换），不涉及。

## A 的问题

**「资源包能手动调整什么」**

||谁定|
|---|---|
|瞄准进度值本身（谓词函数）|模组，pack 不能改|
|切换阈值（`predicate` 里的数字）|pack 可改|
|各状态的 `display` 旋转轴/角度|pack 可改|
|用手臂的哪个姿态（弓 / 弩）|模组，pack 不能改|

所以 pack 能调"什么时候切"和"切过去转多少度"，**调不了"偏航有多少需要被抵消"**——而后者才是关键量。

**「两种配置下是否需要额外调整」——需要，而且按你的规则 A 因此出局。**

弓要抵消 `0.1`、弩要抵消 `0.3`。如果姿态是按枪选的（模组侧），那同一把枪的 display 数值取决于模组给它分了哪个姿态：

- 要么 pack 得知道模组给这把枪分了什么姿态（等于把模组内部决定泄露成 pack 契约，和 C 被否的理由同源）；
- 要么模组再暴露一个"pose 类型"谓词，pack 就得写 `2 姿态 × 2 状态 = 4` 个模型。

两种都是你说的"额外调整"，所以 **A 不用**。

## 那么剩下什么

三条路清一遍：

||结论|
|---|---|
|A|出局（两种姿态下要额外调整）|
|B|唯一能让弩姿态真正"举在正前方"的路，代价是 animator 要对外发布 `c`|
|C|出局|

对应到你的两条姿态：

- **弓姿态（默认）**：6°，按你的决定不修正、不插值修正。**现状即可，零改动。**
- **弩姿态**：17°，不修就达不到"像手枪举在正前方"的目的。要用它，就得付 B 的耦合代价。

所以"留两套代码"这件事本身没有阻碍——加一个 `animateShooterAimingCrossbow` 方法、选择逻辑留空即可；**要不要为弩接上 B，可以等真正启用弩姿态时再定**，不影响现在先把两套姿态摆好。

## 一个需要你确认的歧义

"不是用弩的话没什么必要修正和插值"里的**插值**，我理解成的是 **A/B 那套朝向修正的插值**（因为整句在讲修正机制）。

但它也可能指**手臂姿态本身的 `Mth.lerp(aimingProgress, ...)` 混合**——那个是现在弓姿态正在用的，去掉的话进出瞄准时手臂会硬切一下。

这两个理解改动量差很多，我不猜。你确认一下是指哪个。

## 另记一笔

[DefaultShooterAnimator.java:37](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:37) 的 `armAnimated` 目前只赋值没被读，看着是在给后续状态铺路，我没动。

# 

```
我的意思是你得研究以上方案后再做报告，不是复述我的内容。

- B方案分成animateShooterAimingBow和animateShooterAimingCrossbow两个函数的话，偏航修正是否只在各自函数内存在？如果是的话那挺好的
- 方案A引入customgun:gun物品级别的修正，是否会导致枪包资源包配置选用不同动画姿态的时候需要对这个统一的修正单独再修正回去？
```

# 

让我先把 `ModelPart.translateAndRotate` 的旋转次序确认死，这决定了后面推导成不成立。

研究完了，两个问题的答案都有实质结论，其中一个推翻了我上一轮的说法。

## 先纠正一处：旋转次序

我上轮假设 `translateAndRotate` 的复合是 `Rx·Ry·Rz`，错了。实测 [ModelPart.java:156](app://localhost/epitaxy/local_6b29b7a3-3874-41b7-abf6-4c0ffa65e1e5)：

```java
p_104300_.mulPose((new Quaternionf()).rotationZYX(this.zRot, this.yRot, this.xRot));
```

JOML 的 `rotationZYX(z,y,x)` 等价于 `Rz·Ry·Rx`，作用于向量时 **Rx 最内层**。所以对手臂（`xRot = -PI/2 + head.xRot` 已把手臂压向前方）：

- `xRot` 先作用 → 俯仰
- `yRot` 次之 → **绕模型竖直轴的水平偏航**
- `zRot` 最外 → 滚转

手臂方向算出来是 `(-sin(yRot), 0, -cos(yRot))`。**改 `yRot` 就是让手臂连同挂在上面的枪一起水平摆动**，摆动量正好等于 `yRot` 的差值。

这跟你的实测完全吻合：弩 `-0.3` 偏 17.2°，弓 `-0.1` 偏 5.7°，差值 11.5° = 0.2 rad。**所以姿态常量 c 本身就是枪的偏航误差，"修正"和"不写常量"是同一件事。**

## Q1：修正能否只存在于各自函数内

**能，而且比你预期的更干净——前提是修正做在手臂上而不是物品上。**

各自函数里把主手那项写成 `head.yRot` 而不是 `head.yRot + c` 即可：

- 修正天然只在该函数内，**渲染层零改动**，`IShooterAnimator` 不用加任何成员；
- 我上轮说的"必须让 animator 对外发布 c"作废——那个顾虑是建立在"修正做在物品上"的前提上的。

**为什么物品侧（我上轮提的 B）反而更差**——顺手把这个也算了：

要让物品侧修正成立，需解 `Rz(z)·Ry(θ+c)·Rx(x)·Rz(α) = Rz(z)·Ry(θ)·Rx(x)`，得

```
Rz(α) = Rx(x)⁻¹ · Ry(-c) · Rx(x)
```

即**绕 `Rx(x)⁻¹·Ŷ` 转 -c**。`x = -PI/2` 时该轴恰好是手臂本地 +Z（也就是模型竖直轴），此时 α = -c，能用；**但这个轴随 `xRot` 变**——瞄准俯仰一变，固定旋转就不准了。

顺带：我上轮在选项预览里写的 `Axis.YP.rotation(0.3F * p)` 是错的，按这个推导应该是沿手臂本地 Z 轴。幸好没实现。

所以结论是物品侧**既出模块又不精确**，手臂侧**既局部又精确**。

**代价**：手臂会比原版 `BOW_AND_ARROW` 多摆 c（弓 5.7°、弩 17.2°）。这正是"让枪朝前"要付的代价，而且方向和你的诉求同向——弓去掉常量后手臂**更接近**"跟胸垂直"，因为原版那个 `-0.1` 恰恰是让它不垂直的那一项。

## Q2：A 会不会逼枪包把统一修正再修回去

**是，而且 A 还有个更根本的结构性问题。**

粒度不匹配：`customgun:gun` 是**一个物品**（`ModItems.GUN`），所以它的 `display.thirdperson_righthand` 是**全局唯一**的修正量；而姿态选择是**逐枪**的（你说的"由模组控制"）。一份 display 只能抵消一个 c（弓 0.1 或弩 0.3），换姿态后那份修正就变成**反方向的新误差**，必须再补一份抵消回去。要避免就得模组再暴露一个"姿态类型"谓词，枪包写 `2 姿态 × N 状态` 份模型——正是你说的额外调整。

结构性问题：`display` 是**资源烘焙期**读进 `ItemTransforms` 的。姿态选择如果是模组运行时决定的，**改姿态根本不会触发模型重烘焙**，A 天生跟不上。overrides 能换模型，但换到的那份 `display` 依然是死的。

所以 A 出局不是因为"多写几个 json"，是因为它**无法表达运行时决策**。

## 由此得到的结论

||结论|
|---|---|
|A|出局。粒度不匹配 + `display` 烘焙期固定，跟不上运行时姿态选择|
|B（物品侧修正）|出局。需渲染层参与，且修正轴随俯仰变化，不精确|
|**手臂侧去常量**|**可用。修正局部于各姿态函数，渲染层零改动，全俯仰精确**|

两套姿态可以各自"自带朝前"、互不干扰，不需要 A/B 里的任何东西。需要定的只剩一个量：各姿态去掉常量多少。弓 5.7° 建议全去；弩全去会让手臂偏离原版 17.2°，观感要你实机看。

是否按"手臂侧去常量"实现两套姿态？

# 

```
模组指定动画的话应该不需要枪包display写数值？直接硬编码修正？
- 是否修改范围控制在animateShooterAimingBow或animateShooterAimingCrossbow内部？
- 是通过旋转手臂还是旋转枪械来实现朝前？
- 旋转枪械的话可以立即实现；即使是旋转手臂，也在Animator里把之前Crossbow的版本补上
```

# 

三点都回答，然后按"手臂"实现。

## 回答

**1. 不需要枪包写数值，修正留在模组里。** 但严格说不是"硬编码一个修正量"——是**从源头不引入那个偏航**：主手 `yRot` 写 `head.yRot` 而不是 `head.yRot + c`。常量 c 本来就是为了让手臂好看而加的，枪模型没有弩那层 display 补正，所以干脆不加。

**2. 是，完全控制在函数内部。** 手臂侧方案只改 `mainArm.yRot` 一行，渲染层、`IShooterAnimator`、枪包全都零改动。

**3. 必须是旋转手臂——"旋转枪械"在 Animator 里做不到。**

这点要纠正一下：Animator 只拿到 `head/body/leftArm/rightArm` 四个 `ModelPart`，**没有任何物品句柄**，碰不到枪的渲染。旋转枪械只有 `ItemInHandLayer.renderArmWithItem` 那条路（那里才有实体和 poseStack）。所以：

- 要"旋转枪械"→ 必须出 Animator 进渲染层，而且上轮算过，所需修正轴 `Rx(x)⁻¹·Ŷ` **随瞄准俯仰变化**，固定旋转只在水平瞄准时准。
- 要"修改范围控制在两个函数内部"→ **旋转手臂是唯一选项**，而且恰好是全俯仰精确的那个。

两者不可兼得，而你说的"控制在函数内部"和"硬编码在模组"都指向手臂侧。这也正是我上轮的结论。

补充一点让"偏离原版"这个代价更好接受：主手 `yRot` 去掉常量后手臂**更**接近你要的"右手跟胸垂直"——原版那个 `∓0.1` 恰恰是让它不垂直的那一项。弩同理，17.2° 去掉后手臂正对目标。

现在实现。

实现完成，编译通过。

## 改动

[DefaultShooterAnimator.java:99](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:99) —— `animateShooterAimingBow` 主手 `yRot` 去掉 `∓0.1` 常量，改为 `head.yRot`。副手和双臂 `xRot` 保持原版。

[DefaultShooterAnimator.java:123](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:123) —— 新增 `animateShooterAimingCrossbow`，直接用 `AnimationUtils.animateCrossbowHold` 取原版姿态，再把主手 `yRot` 覆盖成 `head.yRot` 去掉 `∓0.3` 常量。

两处都在 javadoc 里逐条列了"刻意偏离原版之处"和推导（`rotationZYX` → `yRot` 是水平偏航 → 常量 1:1 变成枪口偏航误差），方便高版本比对。

## 两点说明

**弩版本目前没有接入 `animateShooter`**，是私有未调用的方法（IDE 会标灰）。这是按你说的"之后再考虑选用哪种"有意留的，需要接的时候改一行即可。

**行为变化**：弓姿态下手臂现在比原版 `BOW_AND_ARROW` 多摆 5.7°，方向是朝视线——也就是枪口从原来的偏 5.7° 变成正对视线。这个变化理论上应该看得出来，但**我没有实机验证**，需要你进游戏 F5 确认：

```bash
./gradlew :forge-compat:runClient
```

如果弓姿态看着没问题，弩那套也可以用同样方式临时接到 `animateShooter` 里对比一下（弩手臂多摆 17.2°，观感是否可接受是关键）。

# 

```
两个姿态都试了，看起来就差不多了

我现在要改成这样设计，两只手分别应用动画
- 一个函数让持枪手保持像现在这样指向视角朝向
- 剩余函数都收animationProgress（参数名，实际传入的是aimProgress），弓状态左右手分别拆一个函数，弩也是

后续会考虑GunDisplay里加几一个配置项：
- 一个bool控制是否始终持枪手瞄准（瞄准动画是强制覆盖右手），最优先消费掉mainArmAnimated
- 一个bool控制非瞄准时，持枪手是处于弓/弩的满进度姿态还是无动画
- 一个bool控制非瞄准时，副手是处于弓/弩的满进度姿态还是无动画
- 一个string或其他来控制选用弓还是弩，但是这个考虑下来没多大意义，瞄准进度到1的时候是始终视角朝向的，所以用弩这个副手离枪更近的就行，不用管弓的姿态了

- 瞄准进度从0到1的时候是强制持枪手要移动到视角朝向，副手也必须开始插值（如果一开始就是满进度姿态就瞄准进度0-1都始终一样）
- 副手非瞄准时无动画是用来给手枪姿态配置的，不过目前先不管GunDisplay，config项已经用占位bool实现

GunDisplay预留bool已经写好了，继续实现即可
```

# 

先看你已经写好的 GunDisplay 占位 bool。

你已经起了骨架，先读当前状态。

明白了。先把公共推导收成一处，再按手拆开——注意一个后果：拆成单手后不能再调 `AnimationUtils.animateCrossbowHold`（它会同时写两只手），所以弩的常量必须内联。

文件又变了，重新读。

文件没变，是行尾符导致 Edit 匹配失败。整文件重写。

实现完成，编译通过。

## 结构

```
animateShooter                          // 优先级 + 逐手进度 + mainArmAnimated / offArmAnimated
├─ applyArmAimPose                      // 公共：按进度插值到目标（4 个姿态共用）
├─ animateShooterAimingMainArm          // 持枪手对准视线（= 弓的持械手姿态）
├─ animateShooterAimingCrossbowMainArm  // 弩的持械手姿态
├─ animateShooterAimingBowOffArm        // 弓的副手姿态
└─ animateShooterAimingCrossbowOffArm   // 弩的副手姿态
```

除 `animateShooterAimingMainArm` 外都收 `animationProgress`。公共推导（去偏航常量、不跟随潜行补偿、未复现 bob）收进了 `applyArmAimPose` 的 javadoc，各姿态方法只留自己的原版出处。

## 三个需要你确认的点

**1. `AimWhenNoProgress` 改名 + 语义**

按你 scaffold 里的注释理解，它决定**插值起点**：`true` 时起点即终点（进度 0→1 全程不变，等同常驻满姿态），`false` 时从不瞄准时的原版手持物品姿态插值过去。我命名成 `poseWhenIdle` / `applyArmAimPose` 的 `poseWhenIdle` 参数。

如果你的本意不是"插值起点"，说一声。

**2. `animateShooterAimingMainArm` 和弓的持械手是同一个姿态**

去掉 `∓0.1` 偏航常量后，弓的持械手就是"对准视线"，所以我没再单独拆一个弓主手函数——它就是 `animateShooterAimingMainArm`。它和 `CrossbowMainArm` 的唯一区别是弩多一个 `+0.1` 俯仰偏移。

**副作用**：你的 scaffold 里 `mainArmAlwaysAim = true` 走的是弩姿态（带 `+0.1`），现在走的是不带 `+0.1` 的姿态，主手俯仰差 5.7°。这是"始终持枪手瞄准 → 严格对准视线"的必然结果，但确实是默认路径上的可见变化。

**3. 修了你 scaffold 里副手进度的一个继承**

原来 `offArmAimingProgress = mainArmAimingProgress`，所以 `mainArmAlwaysAim = true` 时副手会被连带压成 1.0。两个 bool 既然是独立的，我改成各自只看自己的 bool。

## 两点说明

- `animateShooterAimingBowOffArm` 目前**没有调用方**（当前接的是弩），按"留两套代码"保留，IDE 会标灰。
- 拆成单手后**不能再调 `AnimationUtils.animateCrossbowHold`**（它同时写两只手，调两次会互相覆盖），所以弩的常量改成内联，**失去了自动跟随版本的能力**——这条已写进 `applyArmAimPose` 的 javadoc。

依旧**未实机验证**。

# 

```
刚才我的占位逻辑有点问题，不能只判断瞄准进度，瞄准进度为0，但是可以配置处于弩满蓄力状态，并且在这个情况下持枪手的瞄准进度插值的起点也会跟着变，你重新检查一下
```

# 

你说得对，gate 写错了：`poseWhenIdle` 为 true 时进度 0 也要应用。修正两处判断。

编译通过。查了一遍，除了你指出的 gate，还有两处相关的地方。

## 已修

[DefaultShooterAnimator.java:68](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:68) 两处 gate 从 `progress > 0.001f` 改成 `poseWhenIdle || progress > 0.001f`。

## 复查结果

**插值起点那部分本来就是对的**，不用改。`applyArmAimPose` 里：

```java
float fromYRot = poseWhenIdle ? toYRot : arm.yRot;
```

`poseWhenIdle = true` 时起点就是目标本身，所以进度 0 时手臂已经处于满姿态，0→1 全程不变；`false` 时起点才是不瞄准时的原版手持物品姿态。你描述的两个分支它都覆盖了，问题只在外层没让它被调用。

**另外发现两处，都需要你确认是不是预期：**

**1. 当前占位默认值下，主手的弩姿态分支不可达**

`mainArmAlwaysAim = true` 时分支 1 一定执行并设 `mainArmAnimated = true`，所以分支 2 的主手部分（`animateShooterAimingCrossbowMainArm`）**永远进不去**，`mainArmPoseWhenIdle` 对主手也就没有作用。只有在 `mainArmAlwaysAim = false` 时这两个才能生效。

**2. `animateShooterAimingMainArm` 的 `poseWhenIdle` 参数目前是死参数**

它只在分支 1 被调用，而分支 1 传入的进度恒为 `1.0f`（`mainArmAlwaysAim` 为真才会走进去）。进度为 1 时 `lerp(1, from, to) = to`，起点取什么都不影响结果——也就是说这个臂永远被强制覆盖，正好符合"始终瞄准"的语义，但参数名会误导人。

要不要我把它改成不带 `poseWhenIdle`（或者干脆叫 `applyMainArmAimOverride`）？

**未实机验证**。

# 

```
我把mainArmAlwaysAim改成false后看效果，对比原版弩的姿态，明显持枪手在进度0时还是视角朝向而不是弩蓄满的姿态
你检查下是否哪里写错了，当前config下应该在非瞄准时双手都是弩蓄满的姿态，然后根据进度，持枪手插值到视角朝向
```

# 

找到了 —— 是我把 `poseWhenIdle` 语义写反了。你做的是**两段关键帧插值**：起点是弩蓄满姿态，终点才是视角朝向；而我写成了"起点即终点"，所以进度 0 就直接是视角朝向。

正确行为应该是：

|进度|持枪手|副手|
|---|---|---|
|0（`poseWhenIdle=true`）|原版弩蓄满姿态（含 `∓0.3` 偏航）|弩蓄满姿态|
|1|视角朝向（偏航归零）|弩蓄满姿态（副手不参与对准）|

按这个重写。

现在写正确的版本：`poseWhenIdle` 决定的是**起点**，弩持械手从弩蓄满姿态插值到视角朝向。

编译通过。

## 根因

`poseWhenIdle` 我实现成了"起点即终点"，也就是把起点和终点当成同一个值。实际你要的是**两段关键帧**：起点是弩蓄满姿态，终点是视角朝向。所以进度 0 时手臂被直接放到了终点（视角朝向）。

现在 `applyArmAimPose` 分开了两个键：

```java
private static void applyArmAimPose(ModelPart arm, boolean poseWhenIdle, float animationProgress,
                                    float idleYRot, float idleXRot,   // 起点：弩蓄满姿态
                                    float aimYRot, float aimXRot) {   // 终点：对准视线
    float fromYRot = poseWhenIdle ? idleYRot : arm.yRot;
    float fromXRot = poseWhenIdle ? idleXRot : arm.xRot;
    arm.yRot = Mth.lerp(animationProgress, fromYRot, aimYRot);
    arm.xRot = Mth.lerp(animationProgress, fromXRot, aimXRot);
}
```

## 当前配置下的行为

`mainArmAlwaysAim=false`、两手 `poseWhenIdle=true` 时：

|进度|持枪手|副手|
|---|---|---|
|0|原版弩蓄满：`yRot = ∓0.3 + head.yRot`，`xRot = -PI/2 + head.xRot + 0.1`|原版弩蓄满：`yRot = ±0.6 + head.yRot`，`xRot = -1.5 + head.xRot`|
|→1|收敛到视角朝向：`yRot = head.yRot`，`xRot = -PI/2 + head.xRot`|不变|

函数拆分也对上了你要的：`animateShooterAimingMainArm` 是"对准视线"的强制覆盖（不收进度），另外四个姿态函数都收 `animationProgress`。

## 两点要你确认

**1. 弩持械手满进度时的 `+0.1` 俯仰被我一起收敛掉了**

既然终点是"视角朝向"，我把 `xRot` 的 `+0.1` 也和 `yRot` 的 `∓0.3` 一起插值掉了。这意味着**满进度时比你上次试的那版低了 5.7°**——上次那版是保留 `+0.1` 的。

如果你要保留，把 idle 和 aim 两行的 `xRot` 都写成 `-PI/2 + head.xRot + 0.1` 即可（一行改动）。我选收敛是因为这样所有持械手姿态的终点都统一到 `animateShooterAimingMainArm`，不会出现"开启始终瞄准"和"正常瞄准"收敛到不同姿态的情况。

**2. 两个方法目前没有调用方**

- `animateShooterAimingBowMainArm`：当前接的是弩，弓这套没人调用（按"留两套代码"保留）。
- `animateShooterAimingMainArm`：只在 `mainArmAlwaysAim=true` 的分支里被引用，你现在把它设成 false，所以运行时不走。

**未实机验证**，进游戏看下进度 0 是不是弩蓄满姿态、拉满是不是正对视线。

# 

```
功能没问题。
把javadoc注释改成我现在git暂存区这种每点一个列表项的形式，大段的叙述不好阅读
```

# 

改成你暂存区那种逐条 `<li>` 的形式。

改完，编译通过。

四个姿态方法的 javadoc 都改成了逐条 `<li>`、每条一行不折行：

- [DefaultShooterAnimator.java:133](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:133) `_animateShooterAimingBowMainArm`
- [DefaultShooterAnimator.java:151](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:151) `_animateShooterAimingCrossbowMainArm`
- [DefaultShooterAnimator.java:171](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:171) `_animateShooterAimingBowOffArm`
- [DefaultShooterAnimator.java:187](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:187) `_animateShooterAimingCrossbowOffArm`

`_applyArmAimPose` 和 `_animateShooterAimingMainArm` 你暂存区里已经是这个形式了，没动。

# 

```
_animateShooterAimingMainArm的javadoc后半段也要改，不能一个列表项里大段表述
```

# 

改完，编译通过。

[DefaultShooterAnimator.java:113](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:113) —— 那段原本 3 个 `<li>`（每个都是多行长句）拆成了 10 个单行条目：

```
<li><b>不跟随潜行补偿</b></li>
<li>原因：HumanoidModel#createMesh 里 head / body / right_arm / left_arm 都挂在 root 下互为兄弟节点</li>
<li>因此原版 body.xRot = 0.5F 的前倾不会传导到手臂，手臂朝向完全由 arm.xRot 决定</li>
<li>原版在 this.crouching 分支里对双臂 xRot += 0.4F，跟随则蹲下时枪口下压 0.4 rad（约 23°）</li>
<li>不跟随时 arm.xRot = -PI/2 + head.xRot，手臂始终垂直于视线，与是否蹲下无关</li>
<li>原版加它是为了让垂手跟随前倾的躯干，与"举枪"目的相悖</li>
<li><b>未复现走路摆动</b>：原版 setupAnim 末尾对双臂调用 AnimationUtils#bobModelPart</li>
<li>原因：bobModelPart 需要 ageInTicks，当前接口拿不到，影响约 0.05 rad</li>
<li><b>常量已内联</b>：这些姿态按手拆分，而原版 animateCrossbowHold 会同时写两只手，无法再整体调用</li>
<li>因此不再自动跟随版本，需按各方法 javadoc 的原版位置比对</li>
```

三个主题句保留 `<b>`，其余每条只讲一件事。

# 

```
现在还需要修一个问题：
- 趴下的时候瞄准进度为1的时候没有指向视角朝向
- 原版弩在趴下时的手部动画跟不持弩时一样；现在需要修改成这样，非瞄准时就什么动画都不做（跟原版弩行为一致），但是瞄准进度1或者持枪手始终瞄准时，要把持枪手改到视角朝向（MouseHandlerMixin已限制持枪时的俯仰角，动画这边不负责手臂旋转到人类不可能抵达的位置，让其他地方来约束）
```

# 

先看当前的 manager 与 MouseHandlerMixin，确认趴下时是不是被提前 return 了。

让我先确认趴下时 `head.xRot` 到底是不是被强制了。

机制找到了。`head.xRot` 在趴下时**被原版强制固定**了，不再等于视角俯仰：

- `LivingShooterProne.PRONE_POSE = Pose.SWIMMING`（[LivingShooterProne.java:19](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterProne.java:19)）
- `Entity.isVisuallySwimming()` = `hasPose(Pose.SWIMMING)` → 趴下时为 true
- 于是 `HumanoidModel#setupAnim` 走进这个分支（L91-96），把 `head.xRot` 强拉向 `-PI/4`：

```java
} else if (this.swimAmount > 0.0F) {
   if (flag1) {                                    // flag1 = isVisuallySwimming()
      this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
   } else {
      this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, p_102871_ * (PI/180));
   }
}
```

我们的目标姿态是 `xRot = -PI/2 + head.xRot`，而手臂在模型空间里与头部同向（`arm.xRot = -PI/2 + h` 与 `head.xRot = h` 解出的是同一个方向向量 `(0, sin h, -cos h)`）。所以头被钉住 → 手臂俯仰也跟着被钉在 `-PI/2 - PI/4`，**偏航仍正确，只有俯仰错**。这就是"没指向视角朝向"。

先把第一点（非瞄准不做动画）改掉。

# 

```
非瞄准不做动画是指趴下的时候非瞄准不做动画，不是让你改config，改回去
```

# 

改回 config。

现在把趴姿下的变换链读准，才能推出正确的朝向。

推导出来了，先验证一下公式自洽性——用站立情形反推能还原原版数值，说明变换链读对了：

站立：`world = S·Ry(180-yaw)·d`，代入 `xRot = -PI/2+p`、`yRot = head.yRot` 得到的世界朝向正好等于视线向量。趴下时模型多套了一层 `Rx(-90°)`（[PlayerRenderer.java:220](file:///dev/null)），同一个世界朝向在该坐标架下的解变成另一组：

| | xRot | yRot |
|---|---|---|
| 站立 | `-PI/2 + 视角俯仰` | `head.yRot` |
| 趴下 | `acos(cos(head.yRot)·cos(pitch))` | `atan2(-sin(head.yRot)·cos(pitch), sin(pitch))` |

而且趴下时**必须用实体真实视角俯仰**（`getXRot()`），因为 `head.xRot` 已被钉死。开始改。

编译通过。

## 改动

**根因**：`LivingShooterProne.PRONE_POSE = Pose.SWIMMING`，于是 `isVisuallySwimming()` 为真，原版 `HumanoidModel#setupAnim` 把 `head.xRot` 强拉向 `-PI/4`。我们的目标姿态是"手臂与头同向"，头被钉住 → 偏航还对、俯仰错。

**修法**（[DefaultShooterAnimator.java:55](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:55)）：

- `prone` 时改用实体真实视角俯仰 `getXRot()`，因为 `head.xRot` 已经不代表视角
- `prone` 时手臂目标姿态换一组解：模型在趴下时多套了一层 `Rx(-90°)`（`PlayerRenderer#setupRotations` 的 swimAmount 分支），同一个世界朝向在该坐标架下的解是

  | | xRot | yRot |
    |---|---|---|
  | 站立 | `-PI/2 + p` | `head.yRot` |
  | 趴下 | `acos(cos(head.yRot)·cos(p))` | `atan2(-sin(head.yRot)·cos(p), sin(p))` |

  推导写在 `_animateShooterAimingMainArm` 的 javadoc 里。**自洽性校验**：站立情形下这组解能还原出原版 `BOW_AND_ARROW` / `CROSSBOW_HOLD` 的 `xRot = -PI/2 + head.xRot`，说明变换链读对了。

- `prone` 时强制关闭空闲姿态（两手），所以趴下不瞄准时不套任何姿态，与原版弩一致

## 三点要你确认

**1. 趴下时俯仰只能 20Hz**。`getXRot()` 是每 tick 更新的，没有 partialTick 源（`head.xRot` 本来有，但被钉住了）。趴下时左右转头会看到俯仰以 20Hz 跳。你之前说 20Hz 可接受，但这次是常态（不是进出瞄准的过渡），所以单独提一下。

**2. 副手在趴下+瞄准时仍是躯干坐标系下的弩姿态**。你只说了"持枪手改到视角朝向"，副手我没动——但它在趴下时也是同样的坐标架问题，会看着不对。要不要一并换解？

**3. `_mainArmAimYRot` 用 `atan2` 返回值域是 (-π, π]**，与手臂当前 `yRot` 插值时有走长弧的可能。

**未实机验证**——趴下的符号/数值需要你进游戏看，如果朝向反了大概率是符号。

# 

```
现在趴下瞄准的朝向不对：
- 趴下时非持枪手改成始终无动画，无论config怎么配（原版趴下时始终没有副手动画）
- 瞄准并爬行的时候，持枪手仍然是游泳地摆动手臂而不是始终视角朝向
- 当瞄准并爬行，玩家的双手动画播到往两侧张开时，当镜头从玩家前上方看向玩家时，枪就像正常人朝右持枪；当镜头从前下方看向玩家时，枪是颠倒的，像现实的人右手手背朝前握枪；当镜头从玩家前上方移到前下方看玩家时，玩家的右手朝向从低于地平线到高于地平线的角度运动
```

# 

三点里第 3 点是关键线索——那不是朝向错，是**滚转错**。我选的趴下解 `(xRot, yRot, 0)` 在俯仰接近 0 时 `yRot` 会落到 ±90°，等于绕手臂长轴滚半圈，所以枪会翻过来。

换用 `zRot` 解就没有这个问题。重推：手臂静止朝向是模型 +Y，`d = Rz(z)·Ry(y)·Rx(x)·(0,1,0)`，趴下时世界朝向 = `S(-1,-1,1)·Ry(180°-yaw)·Rx(-90°)·d`。令其等于视线向量：

| | xRot | yRot | zRot |
|---|---|---|---|
| 站立 | `-PI/2 + p` | `head.yRot` | 不变 |
| 趴下 | `p` | `0` | `head.yRot` |

站立那行能还原出原版数值，可作为校验。趴下的 `yRot = 0` 是关键——不引入那段滚转。

编译通过。

## 改动

**#3 的根因是滚转，不是朝向**。上一版趴下解 `(xRot, yRot, 0)` 里，俯仰接近 0 时 `yRot` 会落到 ±90°——而 `xRot` 也接近 0 时手臂长轴几乎就在模型 +Y 上，此时 `yRot` 基本是**绕长轴滚半圈**。枪挂在手上，就跟着翻过来了，也就是你说的"手背朝前"。镜头从前上方/前下方看会看到不同结果，正是半圈滚转的典型表现。

新解把偏航挪到 `zRot`，`yRot` 固定为 0，不引入滚转：

| | xRot | yRot | zRot |
|---|---|---|---|
| 站立 | `-PI/2 + p` | `head.yRot` | 不变 |
| 趴下 | `p` | `0` | `head.yRot` |

`_applyArmAimPose` 相应加了一对 zRot 参数（站立姿态传两次同一个值表示不动）。

**#1**：趴下时副手在 gate 上直接短路，不看 config：

```java
if (!prone && !offArmAnimated && (offArmPoseWhenIdle || offArmAimingProgress > 0.001f)) {
```

**#2**：我判断和 #3 同源——旧公式下 `yRot` 随偏航符号在 ±90° 之间翻转，爬行时视线一晃手臂就来回摆，看着像在划水。改完后 `yRot` 恒为 0、`zRot` 平滑跟随视线，应该就稳了。

## 如果改完还是"摆动"

那说明**不是公式问题，是根本进不到 animator**。两个上游嫌疑，需要你实机确认后再动：

- [ShooterAnimationManager.java:85](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/ShooterAnimationManager.java:85) 的 `entityIn.isSwimming()` 短路。理论上趴地爬行时游泳标志位是 false（`isIllegalProneState` 也把 `isSwimming()` 当非法态），但如果实测为 true，动画会被整个跳过。
- [ShooterAnimationManager.java:98](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/ShooterAnimationManager.java:98) 的 `PlayerAnimatorCompat.playAnimation(...)` 返回 true 时接管。如果枪包给了趴姿的 player_animator 动画，vanilla 路径就不会执行。

**未实机验证**。趴下那组符号如果还是反的，大概率是 `zRot` 的正负。

# 

```
现在趴下瞄准时，枪是朝后的，并且视角左右移动的时候，手是反方向移动的
另外，我之前javadoc改成了列表层级的形式，你不要私自改回扁平化的列表，例如我之前是以下这样的形式：
<ul>
    <ul>
        标题1
        <li>点1</li>
    </ul>
    <ul>
        标题2
        <li></li>
        <li></li>
    </ul>
</ul>
```

# 

两个都处理。先看你现在 javadoc 的实际层级形式。

找到了。是**系数错了**：趴下时 `zRot` 应该是 `2 * head.yRot`，我写成了 1 倍。

重解一遍：把手臂朝向 `d = (Rz·Ry·Rx)·(0,1,0)` 经 `S(-1,-1,1)·Ry(180°-yaw)·Rx(-90°)` 映射到世界，令其等于视线向量，配合 `yRot = 0` 解出的是 `zRot - 2·yaw = 0`。`Rx(-90°)` 把偏航同时带进了两个世界分量，所以系数是 2。写成 1 倍正好是"朝向偏斜 + 左右反向"。

编译通过。

## 数学修正

[DefaultShooterAnimator.java:167](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java:167)：

```java
return prone ? 2.0F * head.yRot : currentZRot;   // 原来是 1.0F * head.yRot
```

**为什么是 2 倍**：趴下时手臂的世界朝向是 `S(-1,-1,1) · Ry(180°-yaw) · Rx(-90°) · Rz(z)Ry(y)Rx(x) · (0,1,0)`。展开后 `Rx(-90°)` 把偏航同时带进了 x 和 z 两个世界分量，令其等于视线向量 `(-sin(yaw)cos(p), -sin(p), cos(yaw)cos(p))`、配合 `yRot = 0`，解出的是 `zRot = 2 * head.yRot`。

写成 1 倍时偏航只转了一半——正是你看到的"朝向偏斜 + 视角左右移动时手往反方向走"。

## Javadoc

按你给的层级形式改回来了（外层 `<ul>` 包多个内层 `<ul>`，每个内层以标题行开头再跟 `<li>`）：

- `_mainArmAimYRot` / `_mainArmAimXRot` / `_mainArmAimZRot`
- `_animateShooterAimingMainArm`（三组：去常量原因 / 推导 / 其余共同偏离）
- 四个 `_animateShooterAiming*Arm`（按"起点 / 终点 / 对比"或"出处 / 插值 / 启用条件"分组）

## 一点提醒

`zRot = 2 * head.yRot` 在偏航 90° 时就是 180°，180° 时是 360°。角度本身对模型无所谓，但插值时从当前 `zRot` 走到目标值可能绕长弧，实机如果看到手臂在某个偏航附近"绕远路"，需要给 `Mth.lerp` 做角度环绕。

**未实机验证**。如果还是反的，剩下的自由参数就是符号了。

# 

```
现在需要继续修复dev.xcolorful.customgun.client.animation.shooter.animator.DefaultShooterAnimator的问题，上次对话历史接着./docs/deprecated/prompt/#54 Simple-animation.md
- 最新javadoc写法我已经commit，以目前最新提交的为准
- 目前代码状态下，趴下瞄准时，持枪手还是朝后的，且左右方向还是颠倒的

继续修复问题。
```

# 

I'll start by reading the referenced prompt doc and the current animator code.

Now let me read the current implementation.

Let me check the current git state and look at the vanilla render chain.

Let me find the vanilla sources to verify the render chain.

Let me use IDEA MCP to inspect the vanilla source directly.

Let me read `HumanoidModel` to confirm the swim branch and arm poses.

Let me verify the exact `ModelPart` transform order.

Let me find the JOML library to verify the quaternion convention definitively.

Let me numerically validate the transform model against the reported bug before changing anything.

My model reproduces the reported symptom exactly (`dot ≈ -0.5`–`-0.7`, i.e. facing backwards) and both candidate solutions land on the look direction (`dot = 1.000`). The root cause is the chain order in the javadoc: `S(-1,-1,1)` is applied *inside* the rotations, not outside. Let me fix the two prone values.

Now correct the derivation notes, which carried the wrong chain order.

修好了。根因是 javadoc 里记的变换链顺序是错的，据此推出的趴下解自然全错。

## 根因

原 javadoc 写的是 `S(-1,-1,1) · Ry(180°-yaw) · [?] · Rz(z)Ry(y)Rx(x)`，把模型翻转放在了最外层。但实读源码 [`LivingEntityRenderer.java:98-101`](file:///C:/Users/XColo/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.16_mapped_official_1.20.1/forge-1.20.1-47.4.16_mapped_official_1.20.1-sources.jar!/net/minecraft/client/renderer/entity/LivingEntityRenderer.java) 是：

```java
this.setupRotations(...);        // mulPose(Ry) → mulPose(Rx(-90°))
p_115311_.scale(-1.0F, -1.0F, 1.0F);   // ← 在 setupRotations 之后
```

`mulPose` 是右乘，所以真实链是 `Ry(180°-yaw) · Rx(-90°) · S(-1,-1,1) · Rz(z)Ry(y)Rx(x)` —— `S` 在**内层**。按正确的链重解，趴下解变成 `xRot = p`、`yRot = PI`、`zRot = PI - head.yRot`。

## 验证

写了个临时 JOML 程序跑数值（跑完已删），先确认事实：

- `rotationZYX(z,y,x)` 确实等于 `Rz·Ry·Rx`（用 JOML 1.10.5 实测矩阵比对，不是推断）
- **链条自校验**：站立时模型头部前向与视线向量点积 = 1.000，说明链读对了
- **旧解复现你的现象**：`xRot=p, yRot=0, zRot=2*head.yRot` 点积只有 -0.50 ~ -0.71 —— 正是"朝后 + 左右反向"
- **新解**：`xRot=p, yRot=PI, zRot=PI-head.yRot` 点积 = 1.000

另一支 `yRot=0, zRot=-head.yRot` 也精确，但选 `yRot=PI` 是因为原版趴下（游泳）姿态自身双臂就是 `yRot=PI`、`zRot≈PI`（[`HumanoidModel.java:201-204`](file:///C:/Users/XColo/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.16_mapped_official_1.20.1/forge-1.20.1-47.4.16_mapped_official_1.20.1-sources.jar!/net/minecraft/client/model/HumanoidModel.java)），插值时衔接最顺；另一支要横穿半圈。

## 改动

只在 [DefaultShooterAnimator.java](core/src/main/java/dev/xcolorful/customgun/client/animation/shooter/animator/DefaultShooterAnimator.java)：`_mainArmAimYRot` 趴下分支 `0` → `PI`，`_mainArmAimZRot` 趴下分支 `2*head.yRot` → `PI - head.yRot`，加各自的推导 javadoc（保持你要的嵌套 `<ul>` + 标题行形式）。`_mainArmAimXRot` 趴下取 `viewPitch` 本来就是对的，没动。

推导组里那条错误的链顺序也改掉了，并补了一条：趴下解其实等于 `Rx(-90°) · 站立解`，也就是**枪的世界朝向和不趴下时完全一致**，只是同一旋转在 `Rz·Ry·Rx` 下的另一组欧拉角。

编译通过（`:core:compileJava`）。未新增平台 API。

**未实机验证** —— 趴下瞄准的朝向得进游戏看：`./gradlew :forge-compat:runClient`。如果还不对，剩下的自由度就只剩符号了。

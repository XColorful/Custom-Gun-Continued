> #75 提示词

# 

```
读当前git暂存区，需要给ZoomKey加一个监听鼠标滚轮切换，并根据SCROLLING_ZOOM_CYCLE传_doZoom的cycle参数
- 找dev.xcolorful.customgun.core.api.event.EventType是否有相应事件及取值，可能需要拦截事件（防止被正常滚轮切换物品）
- 预计修改范围仅限于ZoomKey内部
```

# 

我先读取暂存区改动和相关文件。

暂存区目前为空，改动都在工作区。我看下具体 diff，并查找 EventType 的滚轮事件。

找到 `MOUSE_SCROLLING_EVENT`。现在看它的实现类和现有注册用法。

看现有 handler 的注册/事件处理模式。

看下文档与原任务说明，确认滚轮方向语义和配置意图。

# 

```
这个是CGC新增的，不用搜TaCZ实现
```

# 

实现方案已确定：注册 `MOUSE_SCROLLING_EVENT`，用 `getScrollDeltaY()` 的方向作为 offset，`SCROLLING_ZOOM_CYCLE` 作为 cycle，并在瞄准持枪时取消事件。现在改 ZoomKey。

改完了，现在编译验证。先确认 JDK 17 路径。

Shell 下 ForgeGradle 解析不到 Minecraft 依赖，改用 IDEA MCP 检查该文件。

完成，改动仅在 [ZoomKey.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/input/shooter/ZoomKey.java) 内。

实现内容：

- 注册 `EventType.MOUSE_SCROLLING_EVENT`（客户端事件，`NORMAL` 优先级，不接收已取消事件），并在 `unregisterEventHandler` 对应注销。
- `handleEvent` 转调 `onMouseScrolling(IMouseScrollingEvent)`，事件取值用 `getScrollDeltaY()`。
- 触发条件与滚轮键一致：`ENABLE_SCROLLING_ZOOM` 开启、游戏内聚焦、非旁观、正在瞄准、主手持枪。
- `offset = scrollDeltaY > 0 ? +1 : -1`；`cycle` 传 `SCROLLING_ZOOM_CYCLE.get()`。
- 在满足上述条件且 delta 非零时调用 `event.setCanceled(true)`，阻止快捷栏物品切换。

两点需要你确认：

1. 方向假设：滚轮向上（正 delta）切到下一个 `scopeViewIndex`（+1），向下反之。
2. 只要瞄准持枪就拦截滚轮——即使该枪没有瞄具（`_doZoom` 内部会提前 return）。如果希望无瞄具时仍允许滚动切物品，需要让 `_doZoom` 返回布尔值再决定是否取消。

验证：IDEA 单文件检查无新增错误；完整 Gradle 编译因环境无法解析 `net.minecraft:client:1.20.1` 而失败（与本次改动无关，shell 和 IDEA 构建都是同一依赖解析错误）。另外文件里存在一个原有错误：`{@link LivingShooterAim#zoom()}` 指向的方法已被移除，未改动。

# 

```
刚才的实现会出现在scopeViewIndex 1到2来回跳，而切换不到0
加了LivingShooterAim检查没修复

现在要改成每tick累计滚轮缩放，看git工作区现在的改动（类似ShootKey），完成剩余的
```

# 

先看当前工作区的改动。

看下服务端 zoom 逻辑和用户对 LivingShooterAim 的改动。

明白了。用户已把结构搭好（队列 + 每 tick 累计 + threshold），剩余是补全入队和消费逻辑。现在完成。

为确认根因与索引语义，快速看下渲染端如何使用 scopeViewIndex。

已完成剩余部分，改动仍在 [ZoomKey.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/input/shooter/ZoomKey.java) 内。

补全的内容：

- `onMouseScrolling`：通过状态检查并取消事件后，把 `getScrollDeltaY()` 放入 `mouseScrollingQueue`。
- `tickScrollingZoom`：每 tick 先消费队列累加到 `stackedScroll`；仅当累计绝对值 ≥ `threshold` 时，算出 `offset = (int)(stackedScroll / threshold)`，扣掉已消费部分，调用一次 `_doZoom(..., SCROLLING_ZOOM_CYCLE.get())`。每 tick 最多发一条消息，同 tick 内的多次滚轮会合并成一次切换。
- 离开可缩放状态（不再瞄准、无枪、无焦点等）时清零 `stackedScroll`；`unregisterEventHandler` 同时清空队列和累计，与 `ShootKey` 的做法一致。

验证：IDEA 单文件检查无错误。Gradle 编译仍因环境无法解析 `net.minecraft:client:1.20.1` 而无法运行，且无法在此做游戏内测试。

一个需要游戏内确认的点：你在 `LivingShooterAim` 里加的“服务端索引与客户端 `from` 不一致时忽略本次切换”会在网络往返期间丢弃后续滚轮的步进；每 tick 只发一条消息时，快速滚动仍可能因该守卫丢掉到达前的若干格。如果实测仍有到不了 0 的现象，问题大概率在这里而不是客户端累计逻辑。

# 

```
找到问题了，不是缩放有问题

现在需要整个项目把tag里scopeViewIndex为0的时候对应display里第一个，目前gun item tag里viewIndex为0的时候对应的是display列表最后一项（以从小到大排列的为例，就是缩放倍率最大的一个）
```

# 

问题在索引语义。我先把项目里所有用 NBT `scopeViewIndex` 去索引 display 列表的地方找出来。

我核对了当前代码：三处使用 NBT 索引的地方（[GunDataAccessor.java:138](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:138) 的 `zoom`、[GunCameraHelper.java:241](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/gun/GunCameraHelper.java:241) 的 `views_fov`、[GunRendererAddon.java:311](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/gun/GunRendererAddon.java:311) 的 `views`）都是直接 `list[nbtIndex]`，没有找到 `0 → 最后一项` 的反转。而实际资源包里 `zoom` 多数是降序书写的（如 lpvo `[6.25, 1.25]`），所以 tag 0 会取到"最大倍率"——这与你描述的现象一致，但它是**数组书写顺序**导致的，不是索引反转。

在项目级改动前需要确认目标语义，避免改错方向。

明白：索引语义保持 `tag 0 = display 第一项`，只加 `INVERT_SCROLLING_ZOOM` 控制滚轮方向（默认 true = 滚轮向上放大）。我来接线，并顺手补上 `ClientModConfigTag` 里漏掉的分号。

完成。

改动：

- [ZoomKey.java:191](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/input/shooter/ZoomKey.java:191)：在算出每 tick 的 `offset` 后，若 `INVERT_SCROLLING_ZOOM` 为 true 则取反，再传给 `_doZoom`。默认 true 时滚轮向上（正 delta）对应 `offset = -1`，即 `scopeViewIndex` 减小；配合 `tag 0 = display 第一项`（多数包第一项是最大倍率），滚轮向上即为放大。
- [ClientModConfigTag.java:128](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/config/ClientModConfigTag.java:128)：补上了 `invertScrollingZoom_comment` 行尾缺失的分号（否则无法编译）。
- 索引语义保持原样，未改动 `GunDataAccessor` / `GunCameraHelper` / `GunRendererAddon`。

验证：三个文件 IDEA 检查均无错误；Gradle 构建仍受环境依赖解析限制，未做游戏内验证。

一个已知边界：像 `scope_scout`（`zoom: [4.5, 10]`，升序书写）这类 display，反转后滚轮向上的效果会相反。如果希望方向按“倍率”而不是“数组位置”来判断，就需要按 `scope_zoom_scale` 实际值决定方向，而不是单纯取反。

# 

```
现在还是有问题
以cbrg:8x_scope（\tacz\classic_battleroyale_gun\assets\cbrg\display\attachments\8x_scope_display.json）为例，在当前InvertScrollingZoom = true，鼠标往上滚到底的tag index是0，往下滚到底的tag index是2，但是tag为1的时候才是缩放倍率最小的
- tag index为0的时候，要对应display列表里索引0，即首个

排查是tag有问题还是渲染使用的有问题
```

# 

我先把 display 数组从加载到消费的整条链路再核一遍，确认 tag 和渲染各自用的是哪个索引。

## 排查结论

tag 和渲染端**都没有做索引变换**，两者用的是同一个 0-based 索引，`tag n` 就取 `display[n]`：

|环节|代码|取值|
|---|---|---|
|写入 tag|[LivingShooterAim.java:155](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAim.java:155)|直接写入客户端算好的 `toScopeViewIndex`，无变换|
|世界缩放|[GunDataAccessor.java:138](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:138)|`scopeZoomScale[n % len]`|
|手部 FOV|[GunCameraHelper.java:241](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/gun/GunCameraHelper.java:241)|`scopeViewFov[n % len]`|
|瞄准视角节点|[GunRendererAddon.java:311](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/gun/GunRendererAddon.java:311)|`views[n % len] - 1`|

把 `8x_scope_display.json` 的实际数据代入：

```
tag 0 -> zoom 8.0, fov 70, view 1
tag 1 -> zoom 4.0, fov 70, view 1
tag 2 -> zoom 6.0, fov 70, view 1
```

所以你的观测（两端 0 / 2、最小倍率在 1）**正好证明 tag 0 对应的是 display 索引 0（8x）**，不存在 tag 或渲染的索引错误。真正的问题是资源包里 `zoom: [8.0, 4.0, 6.0]` 本身不按倍率排序——最小倍率 4x 落在中间，所以滚到两端都不会停在最小/最大倍率上。

## 可选的修法

如果目标是“滚轮方向与倍率单调、两端分别是最大/最小”，需要改的是 display 数据的顺序，而不是索引逻辑。推荐在 `AttachmentDisplay.applyScopeBackCompatibility` 里把 `scopeZoomScale` / `scopeViewFov` / `scopeViewIndex` 三个平行数组按倍率**降序**重排：

- `[8, 4, 6]` → `[8, 6, 4]`，tag 0 = 8x（最大），tag 2 = 4x（最小）；
- 配合 `InvertScrollingZoom = true`：滚轮向上 index 减小 = 倍率增大 = 放大，向下到底 = 最小倍率；
- `tag 0 ↔ display[0]` 的语义不变。

需要注意的是这会改变旧资源包的原始书写顺序（TaCZ 原版按数组原序循环），属于行为兼容性取舍。如果确认要这样做，我再实现；如果希望保留资源包原序，那当前行为就是符合“tag 0 = display 首项”的，不需要改代码。

# 

```
排查一下tacz官包所有倍镜配件里，降序的数量，单个倍率的数量，低高低/高低高的数量，升序的数量
```

# 

扫描官方包里的倍镜 display，按 `zoom` 序列分类统计。

扫描结果（`tacz_default_gun`，按 `data/tacz/index/attachments/*.json` 的 `type: "scope"` 统计）：

## 实际游戏内倍镜配件（display 为 `scope: true`，共 13 个）

|类别|数量|明细|
|---|---|---|
|单个倍率|7|1873_6x `[6]`、98k `[4.25]`、acog_ta31 `[2.5]`、aug_default `[4.25]`、contender `[4.25]`、qmk152 `[3]`、retro_2x `[3.25]`|
|降序|4|elcan_4x `[4.25,1.25]`、hamr `[3.25,1.25]`、lpvo_1_6 `[6.25,1.25]`、vudu `[6.5,1.35]`|
|升序|1|standard_8x `[4.5,10]`（其 index 指向 `scope_scout_display`）|
|低高低|1|mk5hd `[5,25,1.25]`|
|高低高|0|—|

## 其他口径

- **按 display 资源文件统计**：`scope: true` 的 display 共 14 个，即上表基础上多一个 `scope_standard_8x_display.json`（`[5,10]`，升序）。但这个文件**没有被任何 attachment index 引用**，`scope_standard_8x` 配件实际指向 `scope_scout_display`。
- **若把红点/全息（`sight: true`，倍率 1.35~2.5）也算作瞄具**：共 32 个 `type: "scope"` 配件，其中单一倍率 26 个（7 个倍镜 + 19 个红点/全息），降序 4、升序 1、低高低 1。

补充：多倍率倍镜中只有 `mk5hd` 不是单调排序；其余非单倍率的 5 个里 4 个降序、1 个升序。

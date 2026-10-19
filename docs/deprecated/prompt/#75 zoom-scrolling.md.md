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

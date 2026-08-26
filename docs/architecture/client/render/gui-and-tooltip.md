# GUI 装配界面与提示框

> 枪械的 GUI 场景主要有三块：改装界面（GunRefitScreen）、物品提示框（tooltip）、HUD 覆盖层（overlay）。它们不直接画 3D 模型，而是复用物品的 GUI 渲染形式或驱动第一人称枪械的定位变换。

## 枪械改装界面

`GunRefitScreen` 是枪械改装界面，渲染各配件槽位按钮、可选配件列表、拆卸 / 翻页按钮。它本身只画 2D 控件，不渲染枪械模型。它进入渲染体系的方式是驱动一个共享状态：`RefitScreenTransformState`。

这个状态在每帧的 `PREPARE_RENDER_FRAME_EVENT` 里更新两个进度：

- 开启进度 `openingProgress`：当前屏幕是改装界面时递增到 1，否则递减到 0。
- 槽位切换进度 `transformProgress`：点击配件槽位时重置为 0，再平滑回到 1。

两个进度被 [枪械附加变换模块](./gun-render-addons.md) 里的第一人称定位变换读取：打开界面时，第一人称枪械从手持姿态过渡到改装界面的特写视角；切换槽位时，镜头在不同配件的特写定位组之间过渡。因此改装界面虽然不画模型，却通过状态间接控制了第一人称枪械的姿态。

槽位与配件的渲染：槽位按钮用 `CustomTexture` 的图标纹理绘制，配件在背包槽位里显示的是其物品的 GUI 渲染形式（见下）。

## 物品提示框

tooltip 系统分两层：核心层定义 `GunTooltip` 等数据（由物品的 `getTooltipMask` 决定显示哪些部分），客户端层 `ClientGunTooltip` / `ClientAttachmentTooltip` / `ClientAmmoTooltip` / `ClientAmmoBoxTooltip` 负责渲染。每个 tooltip 由若干 part（基础信息、描述、详情、附魔、状态等）组成，part 通过 `renderText` 绘制文字。

tooltip 与渲染体系的关联在于「调用其他物品的 GUI 渲染形式」：

- tooltip 顶部的物品图标由 Minecraft 标准 tooltip 渲染，最终进入该物品 BEWLR 的 `renderByItem(GUI)`，枪械此时渲染槽位图标纹理（见 [渲染入口与场景](./rendering-entry-points.md)）。
- 改装界面里悬停槽位时的 tooltip（`IStackTooltip.renderTooltip`）同样复用这条路径，把槽位里的枪械 / 配件图标按 GUI 形式画出来。

tooltip 的文字渲染不经过模型渲染层，而是直接读取 `ClientResourceApi` 拿到的实例数据（枪械名称、弹药、伤害、附件信息等）拼成文本。

## HUD 覆盖层

`OverlayManager` 管理 2D HUD 覆盖层，注册了准心（`CrosshairRender`）、枪械热量（`GunHeatOverlay`）、枪械 HUD 弹药信息（`GunHudOverlay`）、交互按键提示（`InteractKeyTextOverlay`）等子覆盖层。它们绘制屏幕 2D 元素，与 3D 枪械模型渲染正交，但同样消费枪械状态与资源数据。

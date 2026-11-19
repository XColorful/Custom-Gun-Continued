> #99 提示词

# 

```
读Issue #99，现在要做26.2+vulkan适配
研究vulkan启用检测，排查所有vulkan下gl会失效的调用点，完成适配
读`docs/architecture/client/render/PLAN.md`，docs文档修改写在core1.20.1分支，代码写在26.2
测试机器（9400F、1660S、显卡驱动621.14）暂未复现该问题，无法本地实测排查
同时研究26.3是否跟26.2适配写法一致，如果一致就先在26.2上写，否则就只适配26.3（测试机器26.2无mod无法改vulkan，26.3可以改vulkan）
```

# 

I'll start by reading the issue, the plan doc, and relevant memory.

`gh` isn't on PATH. Let me find the repo remote and fetch the issue another way.

Issue #99 is a Vulkan crash from GL calls during the extract phase. Let me read the comments and locate the relevant worktrees/code.

26.2 already ships a `blaze3d.vulkan` package, so Vulkan exists on 26.2 too. Let me investigate the backend selection/detection API in both versions.

Now let me inspect how GL state calls behave and where CGC's GL usage sits. First the stencil operator and GlStateManager.

The GUI extractor in 26.2 is the same deferred extract phase as 26.3. Now let me audit every GL entry point in CGC's source.

Now let me audit indirect GL paths: AR/Optifine compat, the baked-mesh draw hook, and the profiler. Reading key files.

Let me compare the same files across 26.2 and 26.3 to judge whether one adaptation fits both.

Now let me check the docs checkout (core1.20.1) and the existing differences doc structure.

Audit complete. Key findings: the crash is `GlStateManager._enableBlend/_disableBlend` called from the GUI extract phase (4 refit widgets), plus GL timer queries in the profiler, plus a Vulkan-only pipeline bind-group gap. Applying the fixes now.

Now let me compile the 26.2 worktree to verify.

Build passes. Let me finish the audit for remaining GL-program/mixin paths and check the render pipeline bake.

The 26.3 worktree already carries the SAMPLER1 Vulkan fix. Let me check its history for related work before finalizing.

All code changes compile. Now writing the docs in the core1.20.1 checkout.

Let me verify how this shared `core` file looks on the 1.21.11 worktree, since the removal must stay valid after merges.

The 1.21.11 copy uses immediate-mode GUI, where those GL state calls still matter. To avoid a cross-branch regression, I'll keep the calls and rely on the backend-guarded helper instead.

Let me review the final diffs and doc changes.

The 26.3 stencil operator confirms `_disableDepthTest` is load-bearing there (it strips depth state for the depth-less GUI pass), so keeping the calls was correct. Refining the doc wording.

## 结论

**26.2 与 26.3 的适配写法一致**，已先在 26.2 上完成。依据：`ClientRenderHelper`、四个装配界面控件、`GpuFrameTimer` 在两端内容一致，只差包名和无关 API（26.3 的 `GlStateManager._enableBlend(0)` 内部是 `glEnablei`，正是 Issue 堆栈里的调用）。

## 排查与修改

**Vulkan 探测**：不能用设置项（`DEFAULT` 会先试 GL、失败才到 Vulkan，崩溃恢复还会改写设置），以实际设备为准——`RenderSystem.tryGetDevice().getDeviceInfo().backendName()`，值为 `"OpenGL"` / `"Vulkan"`。26.3 里 `DeviceInfo` 换了包但 `RenderSystem#tryGetDevice()` 没动，`var` 承接即可两端通用。已封装为 [ClientRenderHelper.java:459](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderHelper.java:459) 的 `_isOpenGlBackend()`。

**GL 调用点（全部审计结果）**：

| 调用点 | 处理 |
|---|---|
| 装配界面四个控件的 `_enableBlend/_disableBlend` | 由 helper 守卫，Vulkan 下跳过——这就是 #99 的崩溃点 |
| 同四个控件的 `_disableDepthTest/_enableDepthTest` | 保留：26.2 是管线修饰器栈，不碰 GL；**26.3 必须保留**，它负责给无深度附件的 GUI 通道剥掉深度状态，删了会抛 `IllegalStateException` |
| `GpuFrameTimer`（`GL_TIME_ELAPSED`） | 初始化时非 OpenGL 直接停用，F12 采集不会崩 |
| `_clear` / `glClearStencil` | 仅 GL 实现、当前无调用者，保留签名 |
| `GL11.GL_*` 常量 | 编译期内联，安全 |

**额外发现一个 Vulkan 专属致命点**：Vulkan 会校验着色器声明的 sampler 都在 `BindGroupLayout` 里，缺失直接抛 `ShaderCompileException`（GL 只告警）。`LASER_BEAM_ENTITY_PIPELINE` 基于 `ENTITY_EMISSIVE_SNIPPET`，缺 `Sampler1`，会导致资源重载/启动黑屏——已补 `BindGroupLayouts.SAMPLER1`，与 26.3 分支已有的修法一致。

## 验证状态

`:core:compileJava`、`:neoforge-compat:compileJava` 通过。**未实机验证**（按你说的，测试机 26.2 切不了 Vulkan）；GL 后端下的装配界面、准心、激光束建议回归一次。代码未提交，文档写在 core1.20.1 检出的 [26.2 差异参考](D:\Github\Custom-Gun-Continued\docs\architecture\client\render\26.2-rendering-differences.md)「Vulkan 后端」和 TODO。

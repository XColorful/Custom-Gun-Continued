[English](#English)

# 模组兼容框架

## 设计意图

模组兼容的设计为：
- 主模组预留 _Mixin 注入点_，默认行为/默认返回值对应不启用兼容
- 将原先内容移植扩展模组

原因：
- 主模组卸下包袱，保持轻量
- 不是所有模组都全版本更新
- 兼容部分放到扩展模组则可以**并行开发**

## 简要展示

### 越肩视角

```java
package dev.xcolorful.customgun.client.compat.shouldersurfing;

public class ShoulderSurfingCompat {
  
    /**
     * @return 当前是否是越肩视角
     */
    public static boolean isShoulderSurfing() {
        // mixin注入点
        return false;
    }
}
```

当未安装`越肩视角`及兼容模组时：
- 默认返回值即表示未启用兼容
- 不需要检查`modid`是否加载
- 极简的方法体利于 JIT 进行内联优化，并触发死代码消除

### 加速渲染
```java
package dev.xcolorful.customgun.client.compat.ar;

public class GunModelAR {

    /**
     * @return 是否接管渲染
     */
    public static boolean render(PoseStack matrixStack,
                                 ItemDisplayContext transformType,
                                 RenderType renderType,
                                 int light, int overlay,
                                 ItemStack gunItem) {
        // mixin注入点
        return false;
    }
}
```

调用处：
```java
protected static void render(...) {

    if (GunModelAR.render(matrixStack, transformType, renderType, light, overlay, gunItem)) {
        return;
    }

    { // 渲染
        renderScope(_this, matrixStack, transformType, renderType, light, overlay, gunItem);
    }
}
```

当未安装`加速渲染`及兼容模组时：
- 继续执行原本逻辑
- 不需要检查`modid`是否加载

当安装`加速渲染`及兼容模组时：
- Mixin 注入接管逻辑，并修改返回值
- 由兼容模组提供随时取消接管的方式

## 开发流程

新增模组兼容时：
- 若现有代码足够满足需求，则直接在扩展模组中**独立开发**

若需要新增注入点，则：
  - fork 主模组仓库进行开发
  - 同步开发扩展模组，并**测试未安装与安装后的行为**
  - 确认兼容逻辑稳定后，仅提交必要的注入点修改 PR

提交的 PR 需满足以下限制：
- 默认**不允许在主模组仓库中引入额外的 Gradle 依赖**
- 在推进 Minecraft 版本时，主模组仓库**不负责更新注入点接口**
- 当主模组进行重构并与注入点发生冲突时，**不负责研究如何修改**该注入点

# English

## Design Intent

The mod compatibility is designed as follows:
- The main mod reserves _Mixin injection points_, where the default behavior/return value corresponds to disabling the compatibility.
- The original compatibility content is migrated to extension mods.

Reasons:
- Relieves the main mod of unnecessary burdens, keeping it lightweight.
- Not all mods are updated across all versions.
- Placing the compatibility parts into extension mods allows for **parallel development**.

## Brief Showcase

### Shoulder Surfing Reloaded

```java
package dev.xcolorful.customgun.client.compat.shouldersurfing;

public class ShoulderSurfingCompat {
  
    /**
     * @return 当前是否是越肩视角
     */
    public static boolean isShoulderSurfing() {
        // mixin注入点
        return false;
    }
}
```

When `Shoulder Surfing Reloaded` and its compatibility mod are not installed:
- The default return value indicates that the compatibility is not enabled.
- There is no need to check if the `modid` is loaded.
- The dead-simple method body facilitates JIT method inlining and triggers dead code elimination.

### Accelerated Rendering

```java
package dev.xcolorful.customgun.client.compat.ar;

public class GunModelAR {

    /**
     * @return 是否接管渲染
     */
    public static boolean render(PoseStack matrixStack,
                                 ItemDisplayContext transformType,
                                 RenderType renderType,
                                 int light, int overlay,
                                 ItemStack gunItem) {
        // mixin注入点
        return false;
    }
}
```

Call site:
```java
protected static void render(...) {

    if (GunModelAR.render(matrixStack, transformType, renderType, light, overlay, gunItem)) {
        return;
    }

    { // 渲染
        renderScope(_this, matrixStack, transformType, renderType, light, overlay, gunItem);
    }
}
```

When `Accelerated Rendering` and its compatibility mod are not installed:
- The original logic continues to execute.
- There is no need to check if the `modid` is loaded.

When `Accelerated Rendering` and its compatibility mod are installed:
- Mixin injects and takes over the logic, modifying the return value.
- The compatibility mod provides a way to cancel the takeover at any time.

## Development Workflow

When adding new mod compatibility:
- If the existing code is sufficient to meet the requirements, develop it independently within the extension mod directly.

If new injection points need to be added:
- Fork the main mod repository to proceed with development.
- Develop the extension mod concurrently, and **test the behavior both with and without the mod installed**.
- Once the compatibility logic is confirmed to be stable, submit a PR containing only the necessary modifications for the injection points.

Submitted PRs are subject to the following limitations:
- By default, **introducing additional Gradle dependencies into the main mod repository is strictly prohibited**.
- When bumping the Minecraft version, the main mod repository **is not responsible for updating the injection point interfaces**.
- When refactoring of the main mod conflicts with the injection points, **no responsibility is taken for researching how to modify or adapt** them.
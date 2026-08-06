# 渲染体系总览

> 本文档作为 TaCZ 客户端渲染架构的导航索引

## 体系总图

```mermaid
graph TB
    subgraph "资源加载层 — client.resource"
        direction LR
        RES_MGR["ClientAssetsManager<br/>资源包加载调度"]
        DISPLAY_MGR["DisplayManager<br/>AmmoDisplay / AttachmentDisplay<br/>GunDisplay / BlockDisplay"]
        MODEL_MGR["ModelManager<br/>BedrockModelPOJO"]
        ANIM_MGR["AnimationLoadManager<br/>BedrockAnimation / GltfAnimation"]
        INSTANCE["GunDisplayInstance<br/>二次校验 + 缓存构建"]

        RES_MGR --> DISPLAY_MGR
        RES_MGR --> MODEL_MGR
        RES_MGR --> ANIM_MGR
        DISPLAY_MGR --> INSTANCE
        MODEL_MGR --> INSTANCE
        ANIM_MGR --> INSTANCE
    end

    subgraph "数据索引层 — client.resource.index"
        GUN_IDX["ClientGunIndex<br/>模型 + 纹理 + 数据"]
        ATCH_IDX["ClientAttachmentIndex<br/>模型 + 纹理 + LOD"]
        AMMO_IDX["ClientAmmoIndex<br/>弹药模型 + 弹壳模型"]
    end

    subgraph "模型层 — client.model"
        BM["BedrockModel<br/>基岩版几何模型"]
        BAM["BedrockAnimatedModel<br/>动画模型 (AnimationListenerSupplier)"]
        BAT["BedrockAttachmentModel<br/>配件模型 + 模板缓冲瞄具"]
        BGM["BedrockGunModel<br/>枪械模型 + 功能性渲染器"]
        AM["BedrockAmmoModel<br/>弹药模型 + 定位组"]
        FP["FunctionalBedrockPart<br/>可替换渲染逻辑的模型部件"]

        BM --> BAM
        BAM --> BAT
        BAM --> BGM
        BM --> AM
        BM --> FP
    end

    subgraph "几何层 — client.model.bedrock"
        BP["BedrockPart<br/>场景图节点"]
        BC["BedrockCube Box/PerFace<br/>立方体面几何"]
        MRW["ModelRendererWrapper<br/>动画属性包装"]
    end

    subgraph "功能性渲染器 — client.model.functional"
        MFR["MuzzleFlashRender<br/>枪口火焰"]
        SR["ShellRender<br/>抛壳动画"]
        BR["BeamRenderer<br/>激光束"]
        AR["AttachmentRender<br/>配件渲染"]
        LHR["LeftHandRender<br/>左手手臂"]
        RHR["RightHandRender<br/>右手手臂"]
        TSR["TextShowRender<br/>模型文字"]
    end

    subgraph "动画系统 — api.client.animation"
        direction LR
        OA["ObjectAnimation<br/>动画实例"]
        OAR["ObjectAnimationRunner<br/>动画运行器"]
        AC["AnimationController<br/>轨道管理 + 过渡"]
        ASM["AnimationStateMachine<br/>Lua 状态机"]
        ALS["AnimationListenerSupplier<br/>模型 -> 动画监听器"]
    end

    subgraph "物品渲染器 — client.renderer.item"
        GW["GunItemRendererWrapper<br/>枪械渲染 (BEWLR)"]
        AGR["AnimateGeoItemRenderer<br/>动画物品基类"]
        ATTR["AttachmentItemRenderer<br/>配件物品渲染"]
        AMMOR["AmmoItemRenderer<br/>弹药物品渲染"]
        GSTR["GunSmithTableItemRenderer<br/>工作台物品渲染"]
    end

    subgraph "实体/方块渲染器 — client.renderer"
        EBR["EntityBulletRenderer<br/>子弹 + 曳光弹"]
        TR["TargetRenderer<br/>射击靶"]
        SR2["StatueRenderer<br/>雕像方块"]
        GSTR2["GunSmithTableRenderer<br/>工作台方块"]
    end

    subgraph "事件与 Mixin — client.event / client.mixin"
        FPRE["FirstPersonRenderGunEvent<br/>第一人称变换编排"]
        CS["CameraSetupEvent<br/>FOV + 后坐力"]
        TAE["TickAnimationEvent<br/>动画 Tick 驱动"]
        GRM["GameRendererMixin<br/>晃动取消"]
        IHRM["ItemInHandRendererMixin<br/>保持物品 + 渲染钩子"]
        IHLM["ItemInHandLayerMixin<br/>第三人称枪械"]
    end

    INSTANCE --> GUN_IDX
    INSTANCE --> ATCH_IDX
    INSTANCE --> AMMO_IDX

    GUN_IDX --> BGM
    ATCH_IDX --> BAT
    AMMO_IDX --> AM

    BGM --> FP
    BGM --> MFR
    BGM --> SR
    BGM --> BR
    BGM --> AR
    BGM --> LHR
    BGM --> RHR
    BGM --> TSR

    GUN_IDX --> GW
    ATCH_IDX --> ATTR
    AMMO_IDX --> AMMOR

    GW --> AGR
    AGR --> ASM
    ASM --> AC
    AC --> OA
    AC --> OAR

    BAM --> ALS
    OA --> ALS

    GW --> FPRE
    GW --> CS
    TAE --> ASM
    GRM --> GW
    IHRM --> GW
    IHLM --> GW

    EBR --> AM
    GSTR2 --> BM
    TR --> BM
    SR2 --> BM

    style RES_MGR fill:#e1f5fe
    style DISPLAY_MGR fill:#e1f5fe
    style MODEL_MGR fill:#e1f5fe
    style ANIM_MGR fill:#e1f5fe
    style INSTANCE fill:#e1f5fe
    style BM fill:#fff3e0
    style BAM fill:#fff3e0
    style BAT fill:#fff3e0
    style BGM fill:#fff3e0
    style AM fill:#fff3e0
    style FP fill:#fff3e0
    style BP fill:#f3e5f5
    style BC fill:#f3e5f5
    style MRW fill:#f3e5f5
    style MFR fill:#e8f5e9
    style SR fill:#e8f5e9
    style BR fill:#e8f5e9
    style AR fill:#e8f5e9
    style OA fill:#fce4ec
    style OAR fill:#fce4ec
    style AC fill:#fce4ec
    style ASM fill:#fce4ec
    style GW fill:#fff9c4
    style AGR fill:#fff9c4
    style EBR fill:#fff9c4
    style FPRE fill:#ede7f6
    style CS fill:#ede7f6
```

## 体系概要

渲染体系解决的核心问题：**如何将 BlockBench 导出的基岩版模型文件，结合基岩版动画和 Lua 状态机，渲染为游戏中的第一/第三人称枪械、配件、弹药和方块实体**。

整个体系分为六个层次：

|层|包路径|职责|
|---|---|---|
|资源加载层|`client.resource`|加载资源包中的 display、model、animation JSON 文件，构建 POJO → Instance 管道|
|数据索引层|`client.resource.index`|将二次校验后的 POJO 数据组织为可查询的索引（纹理、模型、变换等）|
|模型层|`client.model`|构建基岩版几何模型的场景图，提供动画监听器接口和功能性渲染注册|
|几何层|`client.model.bedrock`|场景图节点、立方体面的顶点数据、坐标转换|
|动画系统|`api.client.animation`|解析基岩版/glTF 动画数据，通过动画控制器和 Lua 状态机驱动模型变形|
|渲染器层|`client.renderer`|对接 Minecraft BEWLR / EntityRenderer / BlockEntityRenderer，执行实际渲染|

## 关键设计特征

- 基岩版坐标转换：模型加载时将 BlockBench 的 Y-up 度和绝对坐标转换为 Minecraft 的弧度相对坐标
- 功能性渲染器模式：通过 `Function<BedrockPart, IFunctionalRenderer>` 在渲染时动态决定部件的渲染行为，支持返回 null 回退至默认几何
- 两阶段模型加载：第一趟插入空占位符，第二趟填充数据，保证父子骨骼交叉引用正确解析
- 模板缓冲瞄具：使用 OpenGL stencil buffer 在瞄具镜片范围内遮盖枪身，实现透过瞄具看世界的效果
- LOD 系统：枪支和配件模型在远距离自动切换低面数模型
- Lua 动画状态机：通过 Lua 脚本定义动画状态转移，实现高度可自定义的枪械动画

## 文档导航

|文档|内容|
|---|---|
|[基岩版模型与几何系统](./bedrock-model-geometry.md)|BedrockModel 场景图构建、BedrockPart 节点树、立方体面几何、坐标转换算法|
|[动画系统](./animation-system.md)|ObjectAnimation 动画实例、AnimationController 轨道管理、基岩版/glTF 动画解析、Lua 动画状态机|
|[渲染管线](./render-pipeline.md)|物品渲染器（BEWLR）、第一/第三人称渲染流程、方块/实体渲染器、事件与 Mixin 钩子|
|[功能性渲染器](./functional-renderers.md)|枪口火焰、抛壳、激光束、配件渲染、手臂渲染、模型文字|
|[客户端资源 POJO](./client-resource-pojos.md)|Display 数据（GunDisplay / AttachmentDisplay / AmmoDisplay）、动画数据、模型数据、GunDisplayInstance 缓存|

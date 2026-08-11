# 渲染体系总览

> 本文档作为渲染架构的导航索引

## 体系总图

```mermaid
graph TB
    subgraph "资源加载层 — client.resource"
        direction LR
        RES_MGR["_AllAssetsManager<br/>资源包加载调度"]
        DISPLAY_MGR["DisplayManager<br/>DisplayManager.Gun / Attachment / Ammo / Block"]
        MODEL_MGR["ModelManager<br/>ModelManager.BedrockModelManager"]
        ANIM_MGR["AnimationManager<br/>AnimationManager.BedrockAnimation / GltfAnimation"]
        INSTANCE["_AssetsInstanceManager<br/>二次校验 → Instance Map"]

        RES_MGR --> DISPLAY_MGR
        RES_MGR --> MODEL_MGR
        RES_MGR --> ANIM_MGR
        DISPLAY_MGR --> INSTANCE
        MODEL_MGR --> INSTANCE
        ANIM_MGR --> INSTANCE
    end

    subgraph "数据索引层 — client.resource.instance"
        GUN_IDX["ClientGunIndexInstance<br/>模型 + 纹理 + 数据"]
        ATCH_IDX["ClientAttachmentIndexInstance<br/>模型 + 纹理 + LOD"]
        AMMO_IDX["ClientAmmoIndexInstance<br/>弹药模型 + 弹壳模型"]
        GUN_DISP["GunDisplayInstance<br/>二次校验 + 缓存构建"]
    end

    subgraph "模型层 — client.model"
        MO["ModelObject<br/>基岩版几何模型"]
        AMO["AnimatedModelObject<br/>动画模型 (AnimationListenerSupplier)"]
        ATMO["AttachmentModelObject<br/>配件模型 + 模板缓冲瞄具"]
        GMO["GunModelObject<br/>枪械模型 + 功能性渲染器"]
        AMMO_MO["AmmoModelObject<br/>弹药模型 + 定位组"]

        MO --> AMO
        AMO --> ATMO
        AMO --> GMO
        MO --> AMMO_MO
    end

    subgraph "几何 POJO — client.resource.assets.model.bedrock"
        GEOM["_GeometryModel<br/>模型几何体"]
        BONE["_Bone<br/>骨骼定义"]
        CUBE["_Cube<br/>立方体定义"]
        DESC["_Description<br/>纹理尺寸 + 可见边界"]
    end

    subgraph "功能性渲染器 — client.renderer.model"
        MFR["MuzzleFlashRender<br/>枪口火焰"]
        SR["ShellRender<br/>抛壳动画"]
    end

    subgraph "动画 POJO — client.resource.assets.animation"
        BA["BedrockAnimation<br/>基岩版动画数据"]
        GA["GltfAnimation<br/>glTF 动画数据"]
        AB["animation.bedrock._Animation<br/>单动画定义"]
        AKB["animation.bedrock.animation._Bone<br/>动画骨骼关键帧"]
    end

    subgraph "动画系统 — client.api.animation"
        OA["ObjectAnimation<br/>动画实例"]
        OSC["ObjectAnimationSoundChannel<br/>声音轨道"]
    end

    subgraph "物品渲染器 — client.renderer.item"
        GW["AnimateGeoItemRenderer<br/>枪械渲染 (BEWLR)"]
    end

    subgraph "实体/方块渲染器 — client.renderer"
        EBR["entity.HeadAABBRender<br/>爆头碰撞盒（已弃用）"]
        HOR["shooter.HumanoidOffhandRender<br/>副手/热键栏枪械渲染"]
        GHT["victim.GunHurtBobTweak<br/>枪击受伤晃动"]
    end

    INSTANCE --> GUN_DISP
    INSTANCE --> GUN_IDX
    INSTANCE --> ATCH_IDX
    INSTANCE --> AMMO_IDX

    GUN_DISP --> GMO

    GMO --> MFR
    GMO --> SR

    GW --> OA
    OA -->|"驱动"| AMO

    style RES_MGR fill:#e1f5fe
    style DISPLAY_MGR fill:#e1f5fe
    style MODEL_MGR fill:#e1f5fe
    style ANIM_MGR fill:#e1f5fe
    style INSTANCE fill:#e1f5fe
    style GEOM fill:#e1f5fe
    style BONE fill:#e1f5fe
    style CUBE fill:#e1f5fe
    style BA fill:#e1f5fe
    style GA fill:#e1f5fe
    style AB fill:#e1f5fe
    style AKB fill:#e1f5fe
    style MO fill:#fff3e0
    style AMO fill:#fff3e0
    style ATMO fill:#fff3e0
    style GMO fill:#fff3e0
    style AMMO_MO fill:#fff3e0
    style MFR fill:#e8f5e9
    style SR fill:#e8f5e9
    style OA fill:#fce4ec
    style GW fill:#fff9c4
    style GHT fill:#ede7f6
```

## 体系概要

渲染体系解决的核心问题：**如何将 BlockBench 导出的基岩版模型文件，结合基岩版动画和 Lua 状态机，渲染为游戏中的第一/第三人称枪械、配件、弹药和方块实体**。

整个体系分为六个层次：

|层|包路径|职责|
|---|---|---|
|资源加载层|`client.resource`|加载资源包中的 display、model、animation JSON 文件，构建 ResourcePojo → Instance 管道|
|数据索引层|`client.resource.instance`|将二次校验后的 POJO 数据组织为可查询的索引|
|模型层|`client.model`|构建基岩版几何模型的场景图，提供动画监听器接口和功能性渲染注册|
|几何 POJO 层|`client.resource.assets.model.bedrock`|基岩版几何模型的 JSON 数据结构定义（骨骼、立方体、UV）|
|动画 POJO 层|`client.resource.assets.animation`|基岩版动画和 glTF 动画的 JSON 数据结构定义|
|渲染器层|`client.renderer`|对接 Minecraft BEWLR / EntityRenderer，执行实际渲染|

## 关键设计特征

- 统一 ResourcePojo 模型：所有资源 POJO 继承 `ResourcePojo<T>`，通过 `ResourcePojoManager<T>` 管理加载和索引
- 基岩版坐标转换：模型加载时将 BlockBench 的 Y-up 度和绝对坐标转换为 Minecraft 的弧度相对坐标
- 功能性渲染器模式：通过 `IModelComponentRenderer` 接口实现动态渲染逻辑替换
- 两阶段 Instance 构建：POJO 校验（类型安全检查）+ Instance 校验（跨 POJO 索引检查）
- 资源重载后自动刷新：重载后自动切枪触发状态机重新初始化

## 文档导航

|文档|内容|
|---|---|
|[基岩版模型与几何系统](./bedrock-model-geometry.md)|BedrockModel POJO → ModelObject 场景图、骨骼/立方体数据、坐标转换算法|
|[动画系统](./animation-system.md)|ObjectAnimation 动画实例、BedrockAnimation POJO 结构、GunAnimationState 枚举|
|[渲染管线](./render-pipeline.md)|AnimateGeoItemRenderer 枪械渲染、渲染器分类、事件钩子|
|[客户端资源 POJO](./client-resource-pojos.md)|Display POJO（GunDisplay / AttachmentDisplay / AmmoDisplay）、动画 POJO、GunDisplayInstance 缓存|

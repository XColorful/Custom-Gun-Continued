> #26 提示词

```
现在需要研究TaCZ的Attachment/Modifier/Cache体系并写文档。

上下文快速交代：
- 目前重构的大致方向为枚举->具体类（强类型）来代替任意字符串->接口（扩展性）
- 语义化重命名，明确AttachmentCacheProperty为ShooterGunPropertyCache，这个缓存是绑在ILivingShooter生命周期的
- Cache的体系有读取数据、写入缓存、计算缓存、拿缓存值，并不是所有缓存都是数值类型，还有别的值类型
- 这个体系可能还涉及LuaJ脚本更新

需要了解的内容包括但不限于以下类，列出仅用于加速查找：
- xiao.customgun.core.api.item.attachment.AttachmentModifierType
- xiao.customgun.core.api.entity.shooter.ShooterGunPropertyCache
- xiao.customgun.core.item.gun.GunPropertyManager
- xiao.customgun.core.resource.data.data.AttachmentData
- xiao.customgun.core.api.entity.ILivingShooter
- xiao.customgun.core.api.entity.ShooterProperty
- com.tacz.guns.resource.modifier.AttachmentCacheProperty
- com.tacz.guns.resource.modifier.AttachmentPropertyManager
- com.tacz.guns.resource.modifier.custom.*
- com.tacz.guns.resource.pojo.data.attachment.AttachmentData
- com.tacz.guns.api.GunProperties
- com.tacz.guns.api.entity.IGunOperator
- com.tacz.guns.entity.shooter.ShooterDataHolder

请务必全面了解信息，可通过IDEA提供的MCP来进一步获取调用逻辑
- CGC对tacz的重构有\docs-tacz\TaCZ Migration Mapping.md这个迁移映射文档来快速找到重构后的对应位置
- 可以通过迁移映射文档里重构后的语义来更清晰地了解tacz的体系（修复命名和包管理混乱的问题），但仅用于辅助和加速了解，CGC的重构不完全对应tacz
- 可借助\docs\的文档来了解CGC的重构和设计思路来了解CGC重构的目的和解决的tacz混乱的地方

我目前不清楚调查哪些方面，以下供参考，你调查后列出研究计划向我报告，然后再开始：
- 从配置文件读取、格式要求
- 缓存类的生命周期，创建、更新、事件通知脚本执行逻辑
- 缓存从配置文件读取、计算，Modifier接口类跟其他部分交互的Mermaid图
- 整个Modifier体系的闭包图/说明，包含所有调用方和调用线路
- 重构后的体系闭包图/说明，各个部分的命名含义
- 文档能提供的功能，至少应覆盖：为重构做准备，为别人了解这个体系提供帮助

可以对tacz每个部分单独写一个详细的文档说明，也需要写速览介绍和全局的文档
- tacz的部分不包含重构后的内容，写在\docs-tacz\architecture\modifier文件夹下的若干文件
- 重构后的部分写在\docs\architecture\core\modifier文件夹下的若干文件
- \docs\文档不包含tacz的内容，两个文件夹下的文档都应是一个完整的体系说明文档
```

```
需要写的文档不限于你刚才提到的，如有需要可以写更多文档信息
- 核心目的是把体系文档化，能用来指导重构和让别人了解清楚
- 介绍全局的文档开头务必带上Mermaid图来直观了解各模块交互，然后再文档说明。

现在开始调查，全部完成后通知我检查。
```
